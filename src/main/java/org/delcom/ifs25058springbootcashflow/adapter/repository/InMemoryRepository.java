package org.delcom.ifs25058springbootcashflow.adapter.repository;

import org.delcom.ifs25058springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25058springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25058springbootcashflow.domain.repository.IRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Basis generik penyimpanan in-memory (untuk unit test). Id bertipe Integer, dimulai dari 1. */
public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {

    protected final Map<Integer, T> store = new LinkedHashMap<>();
    private int sequence = 0;

    protected abstract int idOf(T entity);

    protected abstract T withId(T entity, int id);

    protected abstract String entityName();

    protected synchronized T insert(T entity) {
        int id = ++sequence;
        T saved = withId(entity, id);
        store.put(id, saved);
        return saved;
    }

    @Override
    public synchronized T save(T entity) throws DomainException {
        int id = idOf(entity);
        if (!store.containsKey(id)) {
            throw new EntityNotFoundException(entityName(), id);
        }
        store.put(id, entity);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized List<T> findBy(Predicate<T> filter) {
        return store.values().stream().filter(filter).toList();
    }

    @Override
    public synchronized void deleteById(Integer id) throws EntityNotFoundException {
        if (store.remove(id) == null) {
            throw new EntityNotFoundException(entityName(), id);
        }
    }
}
