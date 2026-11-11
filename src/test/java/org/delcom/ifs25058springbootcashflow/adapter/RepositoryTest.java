package org.delcom.ifs25058springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import javax.sql.DataSource;
import org.delcom.ifs25058springbootcashflow.adapter.repository.*;
import org.delcom.ifs25058springbootcashflow.domain.entity.*;
import org.delcom.ifs25058springbootcashflow.domain.exception.*;
import org.delcom.ifs25058springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Menguji implementasi InMemory dan JDBC (H2) terhadap kontrak yang sama. */
class RepositoryTest {

    private static DataSource h2(boolean withSchema) {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        if (withSchema) {
            SchemaInitializer.initialize(ds);
            SchemaInitializer.initialize(ds); // idempotent (IF NOT EXISTS)
        }
        return ds;
    }

    private static void userContract(IUserRepository repo) throws Exception {
        User a = repo.save("andi", "h1", "Andi", Role.USER);
        User b = repo.save("bella", "h2", "Bella", Role.ADMIN);
        assertTrue(a.id() > 0 && b.id() > a.id());
        assertThrows(ValidationException.class, () -> repo.save("andi", "h", "Dup", Role.USER));

        assertEquals("Andi", repo.findById(a.id()).orElseThrow().fullName());
        assertTrue(repo.findById(999).isEmpty());
        assertEquals(b.id(), repo.findByUsername("BELLA").orElseThrow().id());
        assertTrue(repo.findByUsername("zzz").isEmpty());
        assertEquals(2, repo.findAll().size());
        assertEquals(1, repo.findBy(u -> u.role() == Role.ADMIN).size());

        User updated = repo.save(a.withProfile("Andi Baru", "h9").withPhoto("p.png"));
        assertEquals("Andi Baru", repo.findById(a.id()).orElseThrow().fullName());
        assertEquals("p.png", updated.photoPath());
        // mengganti username menjadi milik user lain harus ditolak
        assertThrows(ValidationException.class,
                () -> repo.save(new User(a.id(), "bella", "h", "X", Role.USER, null)));
        assertThrows(EntityNotFoundException.class, () -> repo.save(new User(999, "ghost", "h", "G", Role.USER, null)));

        repo.deleteById(b.id());
        assertEquals(1, repo.findAll().size());
        assertThrows(EntityNotFoundException.class, () -> repo.deleteById(b.id()));
    }

    private static void transactionContract(ITransactionRepository repo, int userId, int otherUserId) throws Exception {
        Transaction t1 = repo.save(userId, "Gaji", 1000, TransactionType.INCOME, null);
        Transaction t2 = repo.save(userId, "Makan", 40, TransactionType.EXPENSE, "b.png");
        repo.save(otherUserId, "Lain", 5, TransactionType.INCOME, null);
        assertTrue(t1.id() > 0 && t2.id() > t1.id());
        assertNotNull(t1.createdAt());

        assertEquals(2, repo.findByUserId(userId).size());
        assertEquals(0, repo.findByUserId(12345).size());
        assertEquals(3, repo.findAll().size());
        assertEquals(1, repo.findBy(Transaction::isIncome).stream().filter(t -> t.userId() == userId).count());
        Transaction loaded = repo.findById(t2.id()).orElseThrow();
        assertEquals("b.png", loaded.proofPath());
        assertEquals(TransactionType.EXPENSE, loaded.type());
        assertTrue(repo.findById(999).isEmpty());

        repo.save(t2.withDetails("Makan Malam", 50, TransactionType.EXPENSE, null));
        Transaction changed = repo.findById(t2.id()).orElseThrow();
        assertEquals("Makan Malam", changed.description());
        assertEquals(50, changed.amount());
        assertNull(changed.proofPath());
        assertThrows(EntityNotFoundException.class, () -> repo.save(t2.withId(999)));

        repo.deleteById(t1.id());
        assertEquals(1, repo.findByUserId(userId).size());
        assertThrows(EntityNotFoundException.class, () -> repo.deleteById(t1.id()));
    }

    @Test
    void inMemoryUserRepository() throws Exception {
        userContract(new UserRepository());
    }

    @Test
    void inMemoryTransactionRepository() throws Exception {
        transactionContract(new TransactionRepository(), 1, 2);
    }

    @Test
    void jdbcUserRepository() throws Exception {
        userContract(new JdbcUserRepository(h2(true)));
    }

    @Test
    void jdbcTransactionRepository() throws Exception {
        DataSource ds = h2(true);
        IUserRepository users = new JdbcUserRepository(ds);
        int a = users.save("andi", "h", "Andi", Role.USER).id();
        int b = users.save("bella", "h", "Bella", Role.USER).id();
        transactionContract(new JdbcTransactionRepository(ds), a, b);
    }

    @Test
    void jdbcWrapsTechnicalFailuresInRepositoryException() {
        DataSource broken = h2(false); // tabel tidak ada -> SQLException
        JdbcUserRepository users = new JdbcUserRepository(broken);
        JdbcTransactionRepository txs = new JdbcTransactionRepository(broken);
        User u = new User(1, "x", "h", "X", Role.USER, null);
        Transaction t = new Transaction(1, 1, "x", 1, TransactionType.INCOME, null, null);

        assertThrows(RepositoryException.class, () -> SchemaInitializer.initialize(new DriverManagerDataSource("jdbc:h2:mem:x;INIT=RUNSCRIPT FROM 'tidak-ada.sql'", "sa", "")));
        assertThrows(RepositoryException.class, () -> users.save("x", "h", "X", Role.USER));
        assertThrows(RepositoryException.class, () -> users.save(u));
        assertThrows(RepositoryException.class, () -> users.findById(1));
        assertThrows(RepositoryException.class, () -> users.findByUsername("x"));
        assertThrows(RepositoryException.class, users::findAll);
        assertThrows(RepositoryException.class, () -> users.deleteById(1));

        assertThrows(RepositoryException.class, () -> txs.save(1, "x", 1, TransactionType.INCOME, null));
        assertThrows(RepositoryException.class, () -> txs.save(t));
        assertThrows(RepositoryException.class, () -> txs.findById(1));
        assertThrows(RepositoryException.class, txs::findAll);
        assertThrows(RepositoryException.class, () -> txs.findByUserId(1));
        assertThrows(RepositoryException.class, () -> txs.deleteById(1));
    }
}
