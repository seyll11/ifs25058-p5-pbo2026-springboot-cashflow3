package org.delcom.ifs00000springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.List;
import org.delcom.ifs00000springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs00000springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs00000springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs00000springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs00000springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs00000springbootcashflow.usecase.FinanceUseCase;

/**
 * Panel transaksi: kartu ringkasan, toolbar cari/urut, dan grid.
 * Dipakai FinanceView (milik sendiri, bisa diubah) dan UserTransactionsView (admin, read-only).
 */
public class TransactionPanel extends VerticalLayout {

    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;
    private final User viewer;
    private final int ownerId;
    private final boolean readOnly;

    private final Grid<Transaction> grid = new Grid<>();
    private final TextField search = new TextField();
    private final Select<SortOption> sort = new Select<>();
    private final Div summaryRow = new Div();

    public TransactionPanel(FinanceUseCase finance, FinancePresenter presenter, FileStorage storage,
                            User viewer, int ownerId, boolean readOnly) {
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        this.viewer = viewer;
        this.ownerId = ownerId;
        this.readOnly = readOnly;

        setSizeFull();
        setPadding(false);
        summaryRow.addClassName("cf-summary-row");

        configureToolbarAndGrid();
        refresh();
    }

    private void configureToolbarAndGrid() {
        search.setPlaceholder("Cari keterangan...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        sort.setLabel("Urutkan");
        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::label);
        sort.setValue(SortOption.NEWEST);
        sort.addValueChangeListener(e -> {
            grid.sort(List.of());
            refresh();
        });

        Button reload = new Button("Muat ulang", VaadinIcon.REFRESH.create(), e -> refresh());
        HorizontalLayout toolbar = new HorizontalLayout(search, sort, reload);
        toolbar.setAlignItems(Alignment.END);
        toolbar.setWidthFull();
        toolbar.getStyle().set("flex-wrap", "wrap");

        if (!readOnly) {
            Button income = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                    e -> openForm(TransactionType.INCOME, null));
            income.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            Button expense = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                    e -> openForm(TransactionType.EXPENSE, null));
            expense.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            toolbar.add(income, expense);
        }

        GridStyles.apply(grid);
        grid.addColumn(Transaction::id).setHeader("ID").setSortable(true).setWidth("80px").setFlexGrow(0);
        grid.addColumn(Transaction::description).setHeader("Keterangan").setSortable(true).setFlexGrow(2);
        grid.addComponentColumn(t -> {
            Span amount = new Span(presenter.money(t.amount()));
            amount.addClassName(t.isIncome() ? "cf-amount-income" : "cf-amount-expense");
            return amount;
        }).setHeader("Jumlah").setComparator(Transaction::amount).setSortable(true);
        grid.addComponentColumn(t -> {
            Span badge = new Span(presenter.typeLabel(t.type()));
            badge.addClassName(t.isIncome() ? "cf-badge cf-badge-income" : "cf-badge cf-badge-expense");
            return badge;
        }).setHeader("Tipe").setComparator(Transaction::type).setSortable(true);
        grid.addComponentColumn(this::proofThumbnail).setHeader("Bukti").setFlexGrow(0).setWidth("110px");
        grid.addColumn(t -> presenter.date(t.createdAt())).setHeader("Waktu")
                .setComparator(Transaction::createdAt).setSortable(true);
        if (!readOnly) {
            grid.addComponentColumn(this::actions).setHeader("Tindakan").setFlexGrow(0).setWidth("170px");
        }

        add(summaryRow, toolbar, grid);
        expand(grid);
    }

    private Div proofThumbnail(Transaction t) {
        Div holder = new Div();
        String url = FileStorage.urlOf(t.proofPath());
        if (url == null) {
            holder.add(new Span("-"));
            return holder;
        }
        Image thumb = new Image(url, "Bukti transaksi " + t.id());
        thumb.addClassName("cf-thumb");
        thumb.addClickListener(e -> ImagePreviewDialog.open("Bukti transaksi [" + t.id() + "]", url));
        holder.add(thumb);
        return holder;
    }

    private HorizontalLayout actions(Transaction t) {
        Button edit = new Button("Ubah", VaadinIcon.EDIT.create(), e -> openForm(t.type(), t));
        edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        Button delete = new Button("Hapus", VaadinIcon.TRASH.create(), e -> confirmDelete(t));
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(edit, delete);
    }

    /** Memuat ulang data dan kartu ringkasan sesuai kata kunci + urutan saat ini. */
    public final void refresh() {
        try {
            String keyword = search.getValue();
            SortOption order = sort.getValue();
            List<Transaction> items;
            FinanceUseCase.Summary summary;
            if (readOnly) {
                items = finance.listAsAdmin(viewer, ownerId, keyword, order);
                summary = finance.summaryAsAdmin(viewer, ownerId);
            } else {
                items = finance.list(ownerId, keyword, order);
                summary = finance.summary(ownerId);
            }
            grid.setItems(items);
            renderSummary(summary);
        } catch (DomainException e) {
            Notifier.error(presenter.errorMessage(e));
        }
    }

    private void renderSummary(FinanceUseCase.Summary s) {
        summaryRow.removeAll();
        summaryRow.add(
                card("Total transaksi", Integer.toString(s.count()), ""),
                card("Pemasukan", presenter.money(s.income()), "cf-amount-income"),
                card("Pengeluaran", presenter.money(s.expense()), "cf-amount-expense"),
                card("Saldo", presenter.money(s.balance()),
                        s.balance() < 0 ? "cf-amount-expense" : "cf-amount-income"));
    }

    private static Div card(String caption, String value, String valueClass) {
        Span cap = new Span(caption);
        cap.addClassName("cf-summary-caption");
        Span val = new Span(value);
        val.addClassNames("cf-summary-value", valueClass.isEmpty() ? "cf-summary-neutral" : valueClass);
        Div card = new Div(cap, val);
        card.addClassName("cf-summary-card");
        return card;
    }

    // ------------------------------------------------------------------ form tambah / ubah

    private void openForm(TransactionType type, Transaction existing) {
        boolean editing = existing != null;
        String title = (editing ? "Ubah " : "Tambah ") + presenter.typeLabel(type);
        ModalDialog dialog = new ModalDialog(title);
        dialog.setWidth("min(520px, 95vw)");

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        description.setRequiredIndicatorVisible(true);
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setWidthFull();
        amount.setPlaceholder("contoh: 1000000");
        amount.setRequiredIndicatorVisible(true);
        ImageUploadField proof = new ImageUploadField();
        Paragraph proofHint = new Paragraph(editing && existing.proofPath() != null
                ? "Bukti saat ini tersimpan. Unggah gambar baru untuk menggantinya."
                : "Bukti transaksi (opsional): jpg, png, gif, atau webp, maks. 5 MB.");
        proofHint.addClassName("cf-hint");
        if (editing) {
            description.setValue(existing.description());
            amount.setValue(Long.toString(existing.amount()));
        }
        VerticalLayout form = new VerticalLayout(description, amount, proofHint, proof);
        form.setPadding(false);
        dialog.addBody(form);

        Button cancel = new Button("Batal", e -> dialog.close());
        Button saveButton = new Button("Simpan");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            dialog.setBusy(true);
            saveButton.setEnabled(false);
            try {
                if (save(type, existing, description.getValue(), amount.getValue(), proof)) {
                    dialog.close();
                }
            } finally {
                dialog.setBusy(false);
                saveButton.setEnabled(true);
            }
        });
        dialog.addActions(cancel, saveButton);
        dialog.open();
    }

    /** Mengembalikan true bila tersimpan. File bukti baru dibersihkan bila penyimpanan gagal. */
    private boolean save(TransactionType type, Transaction existing, String description, String rawAmount,
                         ImageUploadField proof) {
        String newProof = null;
        try {
            long amount = finance.parseAmount(rawAmount);
            if (proof.hasFile()) {
                newProof = storage.store(proof.data(), proof.fileName());
            }
            Transaction saved;
            if (existing == null) {
                saved = type == TransactionType.INCOME
                        ? finance.addIncome(ownerId, description, amount, newProof)
                        : finance.addExpense(ownerId, description, amount, newProof);
                Notifier.success(presenter.addedMessage(saved));
            } else {
                String proofPath = newProof != null ? newProof : existing.proofPath();
                saved = finance.update(ownerId, existing.id(), description, amount, existing.type(), proofPath);
                if (newProof != null) {
                    storage.delete(existing.proofPath());
                }
                Notifier.success(presenter.updatedMessage(saved));
            }
            refresh();
            return true;
        } catch (DomainException ex) {
            storage.delete(newProof);
            Notifier.error(presenter.errorMessage(ex));
        } catch (RuntimeException ex) {
            storage.delete(newProof);
            Notifier.error("Terjadi kesalahan sistem: " + ex.getMessage());
        }
        return false;
    }

    // ------------------------------------------------------------------ konfirmasi hapus

    private void confirmDelete(Transaction t) {
        ModalDialog dialog = new ModalDialog("Hapus transaksi?");
        dialog.setWidth("min(480px, 95vw)");
        dialog.addBody(new Paragraph("Transaksi berikut akan dihapus permanen:"), new Paragraph(presenter.line(t)));
        Button cancel = new Button("Batal", e -> dialog.close());
        Button delete = new Button("Hapus");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        delete.addClickListener(e -> {
            dialog.setBusy(true);
            try {
                Transaction removed = finance.delete(ownerId, t.id());
                storage.delete(removed.proofPath());
                Notifier.success(presenter.deletedMessage(removed));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                Notifier.error(presenter.errorMessage(ex));
            } finally {
                dialog.setBusy(false);
            }
        });
        dialog.addActions(cancel, delete);
        dialog.open();
    }
}
