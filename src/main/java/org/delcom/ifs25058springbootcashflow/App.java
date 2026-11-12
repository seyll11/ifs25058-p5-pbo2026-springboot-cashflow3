package org.delcom.ifs25058springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import java.net.URI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@Meta(name = "description",
        content = "Cashflow: aplikasi catatan keuangan untuk mencatat pemasukan dan pengeluaran, "
                + "lengkap dengan bukti transaksi, pencarian, dan ringkasan saldo.")
@PWA(name = "Cashflow", shortName = "Cashflow", iconPath = "icons/icon.png")
public class App implements AppShellConfigurator {

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        applyDatabaseUrl(System.getenv("DATABASE_URL"));
        SpringApplication.run(App.class, args);
    }

    /**
     * Mendukung URL bawaan Render/Heroku (postgresql://user:pass@host/db) lewat variabel DATABASE_URL.
     * Dikonversi menjadi DB_URL, DB_USERNAME, DB_PASSWORD kecuali sudah diatur manual.
     */
    private static void applyDatabaseUrl(String raw) {
        if (raw == null || raw.isBlank() || System.getenv("DB_URL") != null) {
            return;
        }
        String value = raw.trim();
        if (value.startsWith("jdbc:")) {
            System.setProperty("DB_URL", value);
            return;
        }
        URI uri = URI.create(value);
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        System.setProperty("DB_URL", "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath());
        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            int colon = userInfo.indexOf(':');
            System.setProperty("DB_USERNAME", colon < 0 ? userInfo : userInfo.substring(0, colon));
            if (colon >= 0) {
                System.setProperty("DB_PASSWORD", userInfo.substring(colon + 1));
            }
        }
    }
}
