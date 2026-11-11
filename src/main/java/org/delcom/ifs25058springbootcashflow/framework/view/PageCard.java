package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;

/** Kartu konten halaman (putih, sudut membulat, bayangan halus). */
public class PageCard extends Div {

    public PageCard() {
        addClassName("cf-card");
    }

    public PageCard(com.vaadin.flow.component.Component... components) {
        this();
        add(components);
    }
}
