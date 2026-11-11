package org.delcom.ifs00000springbootcashflow.adapter.repository;

import org.delcom.ifs00000springbootcashflow.domain.entity.Role;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs00000springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs00000springbootcashflow.domain.repository.IUserRepository;
import java.util.Optional;

public class UserRepository extends InMemoryRepository<User> implements IUserRepository {

    @Override
    protected int idOf(User entity) {
        return entity.id();
    }

    @Override
    protected User withId(User entity, int id) {
        return entity.withId(id);
    }

    @Override
    protected String entityName() {
        return "User";
    }

    @Override
    public synchronized User save(String username, String passwordHash, String fullName, Role role)
            throws ValidationException {
        if (findByUsername(username).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return insert(new User(0, username, passwordHash, fullName, role, null));
    }

    @Override
    public synchronized User save(User entity) throws DomainException {
        boolean taken = store.values().stream()
                .anyMatch(u -> u.id() != entity.id() && u.username().equalsIgnoreCase(entity.username()));
        if (taken) {
            throw new ValidationException("Username sudah dipakai");
        }
        return super.save(entity);
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) {
        return store.values().stream().filter(u -> u.username().equalsIgnoreCase(username)).findFirst();
    }
}
