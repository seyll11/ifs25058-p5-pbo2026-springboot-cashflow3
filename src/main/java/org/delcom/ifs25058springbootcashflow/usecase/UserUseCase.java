package org.delcom.ifs25058springbootcashflow.usecase;

import org.delcom.ifs25058springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25058springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25058springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import java.util.Comparator;
import java.util.List;

public class UserUseCase {

    private final IUserRepository users;

    public UserUseCase(IUserRepository users) {
        this.users = users;
    }

    public List<User> list() {
        return users.findAll().stream().sorted(Comparator.comparingInt(User::id)).toList();
    }

    public User get(int id) throws EntityNotFoundException {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    /** newPassword kosong/null = password tidak diubah. */
    public User updateProfile(int id, String fullName, String newPassword) throws DomainException {
        User user = get(id);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        String hash = user.passwordHash();
        if (newPassword != null && !newPassword.isEmpty()) {
            AuthUseCase.requirePassword(newPassword);
            hash = PasswordHasher.hash(newPassword);
        }
        return users.save(user.withProfile(fullName.trim(), hash));
    }

    public User updatePhoto(int id, String photoPath) throws DomainException {
        return users.save(get(id).withPhoto(photoPath));
    }
}
