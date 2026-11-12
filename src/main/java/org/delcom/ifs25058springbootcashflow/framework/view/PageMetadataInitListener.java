package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Menambahkan atribut bahasa pada elemen html (aksesibilitas &amp; SEO) di halaman awal Vaadin. */
@Component
public class PageMetadataInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response ->
                response.getDocument().select("html").attr("lang", "id"));
    }
}
