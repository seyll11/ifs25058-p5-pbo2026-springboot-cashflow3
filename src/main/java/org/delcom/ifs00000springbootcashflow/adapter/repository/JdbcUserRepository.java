package org.delcom.ifs00000springbootcashflow.adapter.repository;

import org.delcom.ifs00000springbootcashflow.domain.entity.Role;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs00000springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs00000springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs00000springbootcashflow.domain.repository.IUserRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;

public class JdbcUserRepository implements IUserRepository {

    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";
    private static final String UNIQUE_VIOLATION = "23505";

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) throws ValidationException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getInt(1), username, passwordHash, fullName, role, null);
            }
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new ValidationException("Username sudah dipakai");
            }
            throw new RepositoryException("Gagal menyimpan user", e);
        }
    }

    @Override
    public User save(User user) throws DomainException {
        String sql = "UPDATE users SET username = ?, password_hash = ?, full_name = ?, role = ?, photo_path = ? WHERE id = ?";
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, user.passwordHash());
            ps.setString(3, user.fullName());
            ps.setString(4, user.role().name());
            ps.setString(5, user.photoPath());
            ps.setInt(6, user.id());
            if (ps.executeUpdate() == 0) {
                throw new EntityNotFoundException("User", user.id());
            }
            return user;
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new ValidationException("Username sudah dipakai");
            }
            throw new RepositoryException("Gagal memperbarui user", e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE LOWER(username) = LOWER(?)",
                ps -> ps.setString(1, username));
    }

    @Override
    public List<User> findAll() {
        List<User> result = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Gagal membaca user", e);
        }
    }

    @Override
    public List<User> findBy(Predicate<User> filter) {
        return findAll().stream().filter(filter).toList();
    }

    @Override
    public void deleteById(Integer id) throws EntityNotFoundException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new EntityNotFoundException("User", id);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Gagal menghapus user", e);
        }
    }

    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Optional<User> queryOne(String sql, Binder binder) {
        try (Connection c = dataSource.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Gagal membaca user", e);
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"),
                rs.getString("full_name"), Role.valueOf(rs.getString("role")), rs.getString("photo_path"));
    }
}
