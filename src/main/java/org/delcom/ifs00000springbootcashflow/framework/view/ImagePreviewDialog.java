package org.delcom.ifs00000springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/**
 * Modal pratinjau gambar. Klik gambar untuk toggle zoom 1.0x <-> 1.8x;
 * tombol Perkecil / Perbesar / Tutup di kanan footer.
 */
public class ImagePreviewDialog extends ModalDialog {

    private static final double MIN = 0.5;
    private static final double MAX = 3.0;
    private static final double STEP = 0.2;
    private static final double DEFAULT = 1.0;
    private static final double TOGGLED = 1.8;

    private final Image image;
    private double scale = DEFAULT;

    public ImagePreviewDialog(String title, String url) {
        super(title);
        setWidth("min(900px, 95vw)");

        image = new Image(url, title);
        image.addClassName("cf-preview-image");
        image.addClickListener(e -> setScale(scale == DEFAULT ? TOGGLED : DEFAULT));

        Div viewport = new Div(image);
        viewport.addClassName("cf-preview-viewport");
        addBody(viewport);

        Button zoomOut = new Button("Perkecil", e -> setScale(Math.max(MIN, scale - STEP)));
        Button zoomIn = new Button("Perbesar", e -> setScale(Math.min(MAX, scale + STEP)));
        Button close = new Button("Tutup", e -> close());
        addActions(zoomOut, zoomIn, close);
    }

    private void setScale(double value) {
        scale = value;
        image.getStyle().set("transform", "scale(" + value + ")");
        image.getStyle().set("transform-origin", "top left");
        image.getStyle().set("cursor", value == DEFAULT ? "zoom-in" : "zoom-out");
    }

    public static void open(String title, String url) {
        new ImagePreviewDialog(title, url).open();
    }
}
