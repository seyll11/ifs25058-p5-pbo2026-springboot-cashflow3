package org.delcom.ifs25058springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.delcom.ifs25058springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25058springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25058springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25058springbootcashflow.domain.entity.*;
import org.delcom.ifs25058springbootcashflow.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseCaseTest {

    private UserRepository userRepo;
    private AuthUseCase auth;
    private UserUseCase userUseCase;
    private FinanceUseCase finance;
    private User admin;
    private User budi;
    private User siti;

    @BeforeEach
    void setUp() throws Exception {
        userRepo = new UserRepository();
        auth = new AuthUseCase(userRepo);
        userUseCase = new UserUseCase(userRepo);
        finance = new FinanceUseCase(new TransactionRepository());
        admin = auth.ensureRootAdmin("admin", "admin123");
        budi = auth.register("Budi", "rahasia1", "Budi S");
        siti = auth.register("siti", "rahasia2", "Siti");
    }

    // ---------------- AuthUseCase
    @Test
    void registerNormalizesAndValidates() {
        assertEquals("budi", budi.username());
        assertEquals(Role.USER, budi.role());
        assertEquals(PasswordHasher.hash("rahasia1"), budi.passwordHash());
        assertThrows(ValidationException.class, () -> auth.register("budi", "rahasia1", "Dup"));
        assertThrows(ValidationException.class, () -> auth.register("a", "rahasia1", "Pendek"));
        assertThrows(ValidationException.class, () -> auth.register(null, "rahasia1", "Null"));
        assertThrows(ValidationException.class, () -> auth.register("ok.name", "123", "PwPendek"));
        assertThrows(ValidationException.class, () -> auth.register("ok.name", null, "PwNull"));
        assertThrows(ValidationException.class, () -> auth.register("ok.name", "rahasia1", " "));
        assertThrows(ValidationException.class, () -> auth.register("ok.name", "rahasia1", null));
    }

    @Test
    void loginChecksCredentials() throws Exception {
        assertEquals(budi.id(), auth.login(" BUDI ", "rahasia1").id());
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
        assertThrows(ValidationException.class, () -> auth.login("tidakada", "rahasia1"));
        assertThrows(ValidationException.class, () -> auth.login(null, "x"));
    }

    @Test
    void ensureRootAdminSeedsOnceAndValidates() throws Exception {
        assertTrue(admin.isAdmin());
        assertEquals("Administrator", admin.fullName());
        assertEquals(admin.id(), auth.ensureRootAdmin("ADMIN", "lain-lagi").id());
        assertEquals(3, userRepo.findAll().size());
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin(" ", "admin123"));
        assertThrows(ValidationException.class, () -> auth.ensureRootAdmin("root", "x"));
    }

    // ---------------- FinanceUseCase
    @Test
    void addValidatesInput() {
        assertThrows(ValidationException.class, () -> finance.addIncome(0, "x", 1, null));
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.id(), "  ", 1, null));
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.id(), null, 1, null));
        assertThrows(ValidationException.class, () -> finance.addExpense(budi.id(), "x", 0, null));
        assertThrows(ValidationException.class, () -> finance.addExpense(budi.id(), "x", -1, null));
    }

    @Test
    void balanceAndSummaryAreScopedPerUser() throws Exception {
        finance.addIncome(budi.id(), "Gaji", 5_000_000, null);
        finance.addExpense(budi.id(), "Makan", 25_000, "bukti.png");
        finance.addIncome(siti.id(), "Freelance", 77, null);
        assertEquals(4_975_000, finance.getBalance(budi.id()));
        FinanceUseCase.Summary s = finance.summary(budi.id());
        assertEquals(2, s.count());
        assertEquals(5_000_000, s.income());
        assertEquals(25_000, s.expense());
        assertEquals(4_975_000, s.balance());
        assertEquals(77, finance.getBalance(siti.id()));
        assertThrows(ValidationException.class, () -> finance.summary(-1));
    }

    @Test
    void listSearchesAndSorts() throws Exception {
        finance.addIncome(budi.id(), "Gaji Bulanan", 300, null);
        finance.addExpense(budi.id(), "Makan Siang", 100, null);
        finance.addExpense(budi.id(), "Bensin", 200, null);
        assertEquals(3, finance.list(budi.id(), null, null).size());
        assertEquals(3, finance.list(budi.id(), "   ", null).size());
        assertEquals(1, finance.list(budi.id(), "gAjI", SortOption.NEWEST).size());
        assertEquals(0, finance.list(budi.id(), "tidak ada", null).size());
        assertEquals("Bensin", finance.list(budi.id(), "", SortOption.NEWEST).get(0).description());
        assertEquals(List.of(100L, 200L, 300L),
                finance.list(budi.id(), "", SortOption.AMOUNT_ASC).stream().map(Transaction::amount).toList());
        assertEquals(0, finance.list(siti.id(), "", null).size());
        assertThrows(ValidationException.class, () -> finance.list(0, "", null));
    }

    @Test
    void getUpdateAndDeleteRespectOwnership() throws Exception {
        Transaction t = finance.addExpense(budi.id(), "Makan", 25_000, "a.png");
        assertEquals("Makan", finance.get(budi.id(), t.id()).description());
        assertThrows(ValidationException.class, () -> finance.get(siti.id(), t.id()));
        assertThrows(ValidationException.class, () -> finance.get(budi.id(), 0));
        assertThrows(EntityNotFoundException.class, () -> finance.get(budi.id(), 999));

        Transaction u = finance.update(budi.id(), t.id(), " Makan Malam ", 30_000, TransactionType.EXPENSE, "b.png");
        assertEquals("Makan Malam", u.description());
        assertEquals(30_000, u.amount());
        assertEquals("b.png", u.proofPath());
        assertThrows(ValidationException.class,
                () -> finance.update(budi.id(), t.id(), "", 1, TransactionType.EXPENSE, null));
        assertThrows(ValidationException.class,
                () -> finance.update(budi.id(), t.id(), "x", 0, TransactionType.EXPENSE, null));
        assertThrows(ValidationException.class,
                () -> finance.update(siti.id(), t.id(), "x", 1, TransactionType.EXPENSE, null));

        assertThrows(ValidationException.class, () -> finance.delete(siti.id(), t.id()));
        Transaction removed = finance.delete(budi.id(), t.id());
        assertEquals("b.png", removed.proofPath());
        assertEquals(0, finance.getBalance(budi.id()));
        assertThrows(EntityNotFoundException.class, () -> finance.delete(budi.id(), t.id()));
    }

    @Test
    void adminCanReadOthersButUsersCannot() throws Exception {
        finance.addIncome(siti.id(), "Gaji", 500, null);
        assertEquals(1, finance.listAsAdmin(admin, siti.id(), "", null).size());
        assertEquals(500, finance.summaryAsAdmin(admin, siti.id()).balance());
        assertThrows(ValidationException.class, () -> finance.listAsAdmin(budi, siti.id(), "", null));
        assertThrows(ValidationException.class, () -> finance.summaryAsAdmin(budi, siti.id()));
        assertThrows(ValidationException.class, () -> finance.listAsAdmin(null, siti.id(), "", null));
    }

    @Test
    void parseAmountRejectsNonNumeric() throws Exception {
        assertEquals(1500, finance.parseAmount(" 1500 "));
        assertThrows(ValidationException.class, () -> finance.parseAmount(null));
        assertThrows(ValidationException.class, () -> finance.parseAmount("   "));
        assertThrows(ValidationException.class, () -> finance.parseAmount("12abc"));
        assertThrows(ValidationException.class, () -> finance.parseAmount("1.5"));
    }

    // ---------------- UserUseCase
    @Test
    void userUseCaseListGetAndUpdate() throws Exception {
        assertEquals(List.of(admin.id(), budi.id(), siti.id()), userUseCase.list().stream().map(User::id).toList());
        assertEquals("Budi S", userUseCase.get(budi.id()).fullName());
        assertThrows(EntityNotFoundException.class, () -> userUseCase.get(999));

        User keep = userUseCase.updateProfile(budi.id(), " Budi Baru ", null);
        assertEquals("Budi Baru", keep.fullName());
        assertEquals(budi.passwordHash(), keep.passwordHash());
        assertEquals(budi.passwordHash(), userUseCase.updateProfile(budi.id(), "Budi Baru", "").passwordHash());

        User changed = userUseCase.updateProfile(budi.id(), "Budi Baru", "passbaru1");
        assertEquals(PasswordHasher.hash("passbaru1"), changed.passwordHash());
        assertEquals(budi.id(), auth.login("budi", "passbaru1").id());

        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.id(), " ", null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.id(), "X", "123"));
        assertThrows(EntityNotFoundException.class, () -> userUseCase.updateProfile(999, "X", null));

        assertEquals("foto.png", userUseCase.updatePhoto(budi.id(), "foto.png").photoPath());
        assertThrows(EntityNotFoundException.class, () -> userUseCase.updatePhoto(999, "x.png"));
    }
}
