package org.delcom.ifs00000springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.delcom.ifs00000springbootcashflow.domain.entity.*;
import org.delcom.ifs00000springbootcashflow.domain.exception.*;
import org.junit.jupiter.api.Test;

class DomainTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 10, 0);

    private static Transaction tx(int id, long amount, TransactionType type, int minute) {
        return new Transaction(id, 1, "t" + id, amount, type, null, T0.plusMinutes(minute));
    }

    @Test
    void passwordHasherHashesAndVerifies() {
        String h = PasswordHasher.hash("rahasia");
        assertEquals(64, h.length());
        assertEquals(h, PasswordHasher.hash("rahasia"));
        assertTrue(PasswordHasher.verify("rahasia", h));
        assertFalse(PasswordHasher.verify("salah", h));
        assertFalse(PasswordHasher.verify(null, h));
        assertFalse(PasswordHasher.verify("rahasia", null));
    }

    @Test
    void transactionHelpers() {
        Transaction in = tx(1, 100, TransactionType.INCOME, 0);
        Transaction out = tx(2, 40, TransactionType.EXPENSE, 1);
        assertTrue(in.isIncome());
        assertFalse(out.isIncome());
        assertEquals(100, in.signedAmount());
        assertEquals(-40, out.signedAmount());
        assertEquals(9, in.withId(9).id());
        Transaction changed = in.withDetails("baru", 5, TransactionType.EXPENSE, "p.png");
        assertEquals("baru", changed.description());
        assertEquals(5, changed.amount());
        assertEquals(TransactionType.EXPENSE, changed.type());
        assertEquals("p.png", changed.proofPath());
        assertEquals(in.id(), changed.id());
        assertEquals(in.createdAt(), changed.createdAt());
    }

    @Test
    void userHelpers() {
        User u = new User(0, "budi", "h", "Budi", Role.USER, null);
        assertFalse(u.isAdmin());
        assertTrue(new User(1, "a", "h", "A", Role.ADMIN, null).isAdmin());
        assertEquals(7, u.withId(7).id());
        User p = u.withProfile("Budi B", "h2");
        assertEquals("Budi B", p.fullName());
        assertEquals("h2", p.passwordHash());
        assertEquals("f.png", u.withPhoto("f.png").photoPath());
    }

    @Test
    void sortOptionsOrderCorrectly() {
        Transaction a = tx(1, 300, TransactionType.INCOME, 5);
        Transaction b = tx(2, 100, TransactionType.EXPENSE, 10);
        Transaction c = tx(3, 200, TransactionType.INCOME, 1);
        Transaction d = tx(4, 100, TransactionType.EXPENSE, 10);

        assertEquals(List.of(4, 2, 1, 3), ids(SortOption.NEWEST, a, b, c, d));
        assertEquals(List.of(3, 1, 2, 4), ids(SortOption.OLDEST, a, b, c, d));
        assertEquals(List.of(2, 4, 3, 1), ids(SortOption.AMOUNT_ASC, a, b, c, d));
        assertEquals(List.of(1, 3, 2, 4), ids(SortOption.AMOUNT_DESC, a, b, c, d));
        assertEquals(List.of(1, 3, 2, 4), ids(SortOption.INCOME_FIRST, a, b, c, d));
        assertEquals(List.of(2, 4, 1, 3), ids(SortOption.EXPENSE_FIRST, a, b, c, d));
        for (SortOption o : SortOption.values()) {
            assertNotNull(o.label());
            assertNotNull(o.comparator());
        }
    }

    private static List<Integer> ids(SortOption option, Transaction... items) {
        List<Transaction> list = new ArrayList<>(List.of(items));
        list.sort(option.comparator());
        return list.stream().map(Transaction::id).toList();
    }

    @Test
    void exceptionsCarryMessages() {
        DomainException base = new DomainException("x", new RuntimeException("sebab"));
        assertEquals("x", base.getMessage());
        assertEquals("sebab", base.getCause().getMessage());
        assertEquals("Transaksi dengan ID 5 tidak ditemukan", new EntityNotFoundException("Transaksi", 5).getMessage());
        assertEquals("bad", new ValidationException("bad").getMessage());
        assertInstanceOf(DomainException.class, new ValidationException("v"));
        assertInstanceOf(DomainException.class, new EntityNotFoundException("T", 1));
    }
}
