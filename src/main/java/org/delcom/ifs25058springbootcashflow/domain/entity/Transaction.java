package org.delcom.ifs25058springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/** Satu transaksi keuangan milik satu pengguna. Amount selalu positif; arah ditentukan oleh type. */
public record Transaction(
        int id,
        int userId,
        String description,
        long amount,
        TransactionType type,
        String proofPath,
        LocalDateTime createdAt) {

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    /** Pemasukan bernilai positif, pengeluaran negatif. */
    public long signedAmount() {
        return isIncome() ? amount : -amount;
    }

    public Transaction withId(int newId) {
        return new Transaction(newId, userId, description, amount, type, proofPath, createdAt);
    }

    public Transaction withDetails(String newDescription, long newAmount, TransactionType newType, String newProofPath) {
        return new Transaction(id, userId, newDescription, newAmount, newType, newProofPath, createdAt);
    }
}
