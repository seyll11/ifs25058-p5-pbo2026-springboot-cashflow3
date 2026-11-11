package org.delcom.ifs00000springbootcashflow.domain.repository;

import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs00000springbootcashflow.domain.exception.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port CRUD generik. Implementasi: InMemory (unit test) dan JDBC (runtime). */
public interface IRepository<T, ID> {

    /** Menyimpan perubahan entity yang sudah ada (id valid). */
    T save(T entity) throws DomainException;

    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findBy(Predicate<T> filter);

    void deleteById(ID id) throws EntityNotFoundException;
}
