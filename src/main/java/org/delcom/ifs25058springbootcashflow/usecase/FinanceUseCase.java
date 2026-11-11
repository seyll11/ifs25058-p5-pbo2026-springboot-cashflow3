package org.delcom.ifs25058springbootcashflow.usecase;

import org.delcom.ifs25058springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25058springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25058springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25058springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25058springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25058springbootcashflow.domain.repository.ITransactionRepository;
import java.util.List;
import java.util.Locale;

/** Proses bisnis cashflow, selalu dibatasi per pengguna. Tanpa komponen UI. */
public class FinanceUseCase {

    /** Ringkasan keuangan satu pengguna. */
    public record Summary(int count, long income, long expense) {
        public long balance() {
            return income - expense;
        }
    }

    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction addIncome(int userId, String description, long amount, String proofPath)
            throws DomainException {
        return add(userId, description, amount, TransactionType.INCOME, proofPath);
    }

    public Transaction addExpense(int userId, String description, long amount, String proofPath)
            throws DomainException {
        return add(userId, description, amount, TransactionType.EXPENSE, proofPath);
    }

    private Transaction add(int userId, String description, long amount, TransactionType type, String proofPath)
            throws DomainException {
        requireValidId(userId);
        String desc = requireDescription(description);
        requirePositive(amount);
        return repository.save(userId, desc, amount, type, proofPath);
    }

    public Transaction update(int userId, int id, String description, long amount, TransactionType type,
                              String proofPath) throws DomainException {
        Transaction existing = findOwned(userId, id);
        String desc = requireDescription(description);
        requirePositive(amount);
        return repository.save(existing.withDetails(desc, amount, type, proofPath));
    }

    /** Menghapus transaksi milik userId dan mengembalikan data yang dihapus (untuk bersih-bersih file bukti). */
    public Transaction delete(int userId, int id) throws DomainException {
        Transaction existing = findOwned(userId, id);
        repository.deleteById(id);
        return existing;
    }

    public Transaction get(int userId, int id) throws DomainException {
        return findOwned(userId, id);
    }

    public List<Transaction> list(int userId, String keyword, SortOption sort) throws ValidationException {
        requireValidId(userId);
        String key = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        SortOption order = sort == null ? SortOption.NEWEST : sort;
        return repository.findByUserId(userId).stream()
                .filter(t -> key.isEmpty() || t.description().toLowerCase(Locale.ROOT).contains(key))
                .sorted(order.comparator())
                .toList();
    }

    public Summary summary(int userId) throws ValidationException {
        requireValidId(userId);
        List<Transaction> all = repository.findByUserId(userId);
        long income = all.stream().filter(Transaction::isIncome).mapToLong(Transaction::amount).sum();
        long expense = all.stream().filter(t -> !t.isIncome()).mapToLong(Transaction::amount).sum();
        return new Summary(all.size(), income, expense);
    }

    public long getBalance(int userId) throws ValidationException {
        return summary(userId).balance();
    }

    /** Hanya ADMIN yang boleh melihat transaksi pengguna lain (read-only). */
    public List<Transaction> listAsAdmin(User actor, int targetUserId, String keyword, SortOption sort)
            throws ValidationException {
        requireAdmin(actor);
        return list(targetUserId, keyword, sort);
    }

    public Summary summaryAsAdmin(User actor, int targetUserId) throws ValidationException {
        requireAdmin(actor);
        return summary(targetUserId);
    }

    /** Mengubah teks angka dari form menjadi long; menolak input non-numerik. */
    public long parseAmount(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Jumlah wajib diisi");
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat (tanpa titik/koma)");
        }
    }

    private Transaction findOwned(int userId, int id) throws DomainException {
        requireValidId(userId);
        requireValidId(id);
        Transaction t = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
        if (t.userId() != userId) {
            throw new ValidationException("Transaksi bukan milik Anda");
        }
        return t;
    }

    private static void requireAdmin(User actor) throws ValidationException {
        if (actor == null || !actor.isAdmin()) {
            throw new ValidationException("Hanya admin yang dapat melihat data pengguna lain");
        }
    }

    private static void requireValidId(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid: " + id);
        }
    }

    private static String requireDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private static void requirePositive(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
    }
}
