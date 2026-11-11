package org.delcom.ifs25058springbootcashflow.adapter.presenter;

import org.delcom.ifs25058springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25058springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25058springbootcashflow.domain.exception.DomainException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format uang, baris, dan pesan notifikasi. Bukan tanggung jawab entity. */
public class FinancePresenter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    /** 1000000 -> "Rp 1,000,000.00"; negatif -> "-Rp 5.00". */
    public String money(long amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        String body = "Rp " + df.format(Math.abs((double) amount));
        return amount < 0 ? "-" + body : body;
    }

    public String typeLabel(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** [id] keterangan | Rp 1,000,000.00 | [Pemasukan] */
    public String line(Transaction t) {
        return "[" + t.id() + "] " + t.description() + " | " + money(t.amount()) + " | [" + typeLabel(t.type()) + "]";
    }

    public String balance(long balance) {
        return "Saldo: " + money(balance);
    }

    public String date(LocalDateTime time) {
        return time == null ? "-" : DATE.format(time);
    }

    public String addedMessage(Transaction t) {
        return typeLabel(t.type()) + " " + money(t.amount()) + " berhasil dicatat";
    }

    public String updatedMessage(Transaction t) {
        return "Transaksi [" + t.id() + "] berhasil diperbarui";
    }

    public String deletedMessage(Transaction t) {
        return "Transaksi [" + t.id() + "] " + t.description() + " dihapus";
    }

    public String errorMessage(DomainException e) {
        return "Gagal: " + e.getMessage();
    }
}
