package org.delcom.ifs00000springbootcashflow.domain.entity;

import java.util.Comparator;

/** Opsi pengurutan transaksi; tiap opsi membawa Comparator-nya sendiri. */
public enum SortOption {
    NEWEST("Terbaru",
            Comparator.comparing(Transaction::createdAt).reversed().thenComparing(Transaction::id, Comparator.reverseOrder())),
    OLDEST("Terlama",
            Comparator.comparing(Transaction::createdAt).thenComparing(Transaction::id)),
    AMOUNT_ASC("Jumlah naik",
            Comparator.comparingLong(Transaction::amount).thenComparing(Transaction::id)),
    AMOUNT_DESC("Jumlah turun",
            Comparator.comparingLong(Transaction::amount).reversed().thenComparing(Transaction::id)),
    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparing((Transaction t) -> t.type() != TransactionType.INCOME).thenComparing(Transaction::id)),
    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparing((Transaction t) -> t.type() != TransactionType.EXPENSE).thenComparing(Transaction::id));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
