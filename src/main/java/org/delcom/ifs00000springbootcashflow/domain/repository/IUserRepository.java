package org.delcom.ifs00000springbootcashflow.domain.repository;

import org.delcom.ifs00000springbootcashflow.domain.entity.Role;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.exception.ValidationException;
import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {

    /** Membuat user baru. Melempar ValidationException bila username sudah dipakai. */
    User save(String username, String passwordHash, String fullName, Role role) throws ValidationException;

    Optional<User> findByUsername(String username);
}
