package org.delcom.ifs25058springbootcashflow.usecase;

import org.delcom.ifs25058springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25058springbootcashflow.domain.entity.Role;
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthUseCase {

    static final int MIN_PASSWORD = 6;
    private static final Pattern USERNAME = Pattern.compile("[a-z0-9_.-]{3,50}");

    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, String fullName) throws ValidationException {
        String name = normalize(username);
        if (!USERNAME.matcher(name).matches()) {
            throw new ValidationException("Username 3-50 karakter: huruf, angka, titik, garis bawah, atau strip");
        }
        requirePassword(password);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (users.findByUsername(name).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return users.save(name, PasswordHasher.hash(password), fullName.trim(), Role.USER);
    }

    public User login(String username, String password) throws ValidationException {
        Optional<User> found = users.findByUsername(normalize(username));
        if (found.isEmpty() || !PasswordHasher.verify(password, found.get().passwordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return found.get();
    }

    /** Membuat admin dari ROOT_* bila belum ada; bila sudah ada, dikembalikan apa adanya. */
    public User ensureRootAdmin(String username, String password) throws ValidationException {
        String name = normalize(username);
        if (name.isEmpty()) {
            throw new ValidationException("ROOT_USERNAME tidak boleh kosong");
        }
        requirePassword(password);
        Optional<User> existing = users.findByUsername(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        return users.save(name, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
    }

    static void requirePassword(String password) throws ValidationException {
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD + " karakter");
        }
    }

    private static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
