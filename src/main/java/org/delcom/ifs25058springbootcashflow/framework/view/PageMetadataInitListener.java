package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Menambahkan bahasa dan judul bawaan pada halaman awal Vaadin (aksesibilitas &amp; SEO). */
@Component
public class PageMetadataInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> {
            var document = response.getDocument();
            document.select("html").attr("lang", "id");
            if (document.title().isBlank()) {
                document.title("Cashflow");
            }
        });
    }
}
