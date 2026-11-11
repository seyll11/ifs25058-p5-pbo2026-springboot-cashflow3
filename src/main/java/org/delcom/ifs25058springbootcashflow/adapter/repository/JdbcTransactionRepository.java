package org.delcom.ifs25058springbootcashflow.adapter.repository;

import org.delcom.ifs25058springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25058springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25058springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25058springbootcashflow.domain.repository.ITransactionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;

public class JdbcTransactionRepository implements ITransactionRepository {

    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        LocalDateTime now = LocalDateTime.now();
        String sql = "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, description);
            ps.setLong(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, proofPath);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Transaction(keys.getInt(1), userId, description, amount, type, proofPath, now);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Gagal menyimpan transaksi", e);
        }
    }

    @Override
    public Transaction save(Transaction t) throws EntityNotFoundException {
        String sql = "UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.description());
            ps.setLong(2, t.amount());
            ps.setString(3, t.type().name());
            ps.setString(4, t.proofPath());
            ps.setInt(5, t.id());
            if (ps.executeUpdate() == 0) {
                throw new EntityNotFoundException("Transaksi", t.id());
            }
            return t;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal memperbarui transaksi", e);
        }
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        List<Transaction> rows = query("SELECT " + COLUMNS + " FROM transactions WHERE id = ?", ps -> ps.setInt(1, id));
        return rows.stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return query("SELECT " + COLUMNS + " FROM transactions ORDER BY id", ps -> { });
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> filter) {
        return findAll().stream().filter(filter).toList();
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return query("SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id", ps -> ps.setInt(1, userId));
    }

    @Override
    public void deleteById(Integer id) throws EntityNotFoundException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new EntityNotFoundException("Transaksi", id);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Gagal menghapus transaksi", e);
        }
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Transaction> query(String sql, Binder binder) {
        List<Transaction> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Transaction(rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                            rs.getLong("amount"), TransactionType.valueOf(rs.getString("type")),
                            rs.getString("proof_path"), rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal membaca transaksi", e);
        }
    }
}
