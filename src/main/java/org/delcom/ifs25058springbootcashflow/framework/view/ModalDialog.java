package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * Dialog standar aplikasi: header biru primer (#2563EB) + teks putih, footer abu-abu (#F3F4F6)
 * dengan seluruh tombol di sisi kanan. Warna diatur lewat styles.css (kelas cf-dialog).
 */
public class ModalDialog extends Dialog {

    private final ProgressBar progress = new ProgressBar();
    private final HorizontalLayout footer = new HorizontalLayout();

    public ModalDialog(String title) {
        setClassName("cf-dialog");
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setCloseOnOutsideClick(false);

        Span heading = new Span(title);
        heading.addClassName("cf-dialog-title");
        getHeader().add(heading);

        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.setWidthFull();

        footer.addClassName("cf-dialog-footer");
        footer.setWidthFull();
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        getFooter().add(footer);
    }

    /** Menambah komponen ke badan dialog (di atas progress bar). */
    public void addBody(Component... components) {
        add(components);
        add(progress);
    }

    /** Menambah tombol aksi ke footer (rata kanan). */
    public void addActions(Button... buttons) {
        footer.add(buttons);
    }

    /** Menampilkan/menyembunyikan progress bar indeterminat saat menunggu proses. */
    public void setBusy(boolean busy) {
        progress.setVisible(busy);
    }
}
