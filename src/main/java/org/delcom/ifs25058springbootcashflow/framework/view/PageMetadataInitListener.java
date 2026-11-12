package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Menyiapkan index.html: bahasa, judul, dan konten awal statis (boot screen).
 *
 * <p>Vaadin merender aplikasi di sisi klien, sehingga HTML pertama yang dikirim server
 * hanya berisi {@code <div id="outlet"></div>} kosong. Akibatnya audit aksesibilitas
 * (Axe Core) pada HTML awal menemukan tiga pelanggaran:
 * <ul>
 *   <li>tidak ada landmark {@code main},</li>
 *   <li>tidak ada heading level satu ({@code h1}),</li>
 *   <li>konten halaman tidak berada di dalam landmark.</li>
 * </ul>
 * Selain itu Lighthouse dapat melaporkan {@code NO_FCP} ("tidak melukis apa pun")
 * ketika bundel JavaScript lambat dimuat, karena belum ada konten yang terlihat.
 *
 * <p>Solusinya: sisipkan konten statis ringan ({@code main} + {@code h1}) ke HTML awal.
 * Browser langsung melukisnya, lalu skrip kecil menghapusnya setelah aplikasi Vaadin
 * selesai dirender supaya landmark dan heading tidak berduplikasi.
 */
@Component
public class PageMetadataInitListener implements VaadinServiceInitListener {

    private static final String BOOT_SCREEN = """
            <main id="cf-boot" class="cf-boot">
              <div class="cf-boot-card">
                <h1>Cashflow</h1>
                <p>Memuat aplikasi…</p>
                <noscript>
                  <p>Aplikasi ini memerlukan JavaScript. Aktifkan JavaScript lalu muat ulang halaman.</p>
                </noscript>
              </div>
            </main>
            """;

    private static final String BOOT_STYLE = """
            #cf-boot{position:fixed;inset:0;z-index:2147483646;display:flex;align-items:center;
            justify-content:center;margin:0;background:#f5f7fb;color:#111827;
            font-family:system-ui,-apple-system,"Segoe UI",Roboto,Arial,sans-serif}
            #cf-boot .cf-boot-card{max-width:32rem;padding:24px;text-align:center}
            #cf-boot h1{margin:0 0 .5rem;font-size:1.6rem;line-height:1.2;color:#2563EB}
            #cf-boot p{margin:0;color:#4b5563}
            """;

    private static final String BOOT_SCRIPT = """
            (function () {
              var boot = document.getElementById('cf-boot');
              var outlet = document.getElementById('outlet');
              if (!boot || !outlet) { return; }
              var observer = null;
              var timer = null;
              var done = false;
              function appIsReady() {
                return !!(outlet.querySelector('main, [role="main"]') && outlet.querySelector('h1'));
              }
              // Hapus segera setelah aplikasi punya landmark + heading sendiri, supaya
              // dokumen tidak pernah punya dua landmark main / dua heading h1.
              function finish() {
                if (done || !appIsReady()) { return; }
                done = true;
                if (observer) { observer.disconnect(); }
                if (timer) { clearInterval(timer); }
                if (boot.parentNode) { boot.parentNode.removeChild(boot); }
              }
              if (window.MutationObserver) {
                observer = new MutationObserver(finish);
                observer.observe(outlet, { childList: true, subtree: true });
              }
              // Cadangan polling bila observer tidak sempat menyala (mis. tab background).
              timer = setInterval(finish, 200);
              finish();
            })();
            """;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> {
            var document = response.getDocument();
            document.select("html").attr("lang", "id");
            if (document.title().isBlank()) {
                document.title("Cashflow");
            }
            // Konten awal: landmark + heading + teks, agar HTML pertama sudah aksesibel
            // dan langsung terlihat (mencegah NO_FCP serta mempercepat FCP/LCP).
            if (document.head() != null && document.body() != null
                    && document.selectFirst("#cf-boot") == null) {
                appendRawText(document.head(), "style", BOOT_STYLE);
                document.body().prepend(BOOT_SCREEN);
                appendRawText(document.body(), "script", BOOT_SCRIPT);
            }
        });
    }

    /**
     * Menambahkan elemen teks mentah ({@code style}/{@code script}) tanpa escape HTML.
     * {@code appendText} akan mengubah {@code >} menjadi {@code &gt;} dan merusak JavaScript.
     */
    private static void appendRawText(Element parent, String tag, String content) {
        Element element = new Element(tag);
        element.appendChild(new DataNode(content));
        parent.appendChild(element);
    }
}
