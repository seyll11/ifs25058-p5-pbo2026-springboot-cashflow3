package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouterLink;

/** Halaman 404 yang aksesibel: punya judul, landmark main, dan heading level satu. */
public class NotFoundView extends VerticalLayout
        implements HasErrorParameter<NotFoundException>, HasDynamicTitle {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        removeAll();
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getElement().setAttribute("role", "main");

        PageCard card = new PageCard(
                new H1("Halaman tidak ditemukan"),
                new Paragraph("Alamat yang Anda buka tidak tersedia di aplikasi ini."),
                new RouterLink("Kembali ke beranda", FinanceView.class));
        card.addClassName("cf-auth-card");
        add(card);
        return 404;
    }

    @Override
    public String getPageTitle() {
        return "Halaman tidak ditemukan | Cashflow";
    }
}
