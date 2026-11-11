package org.delcom.ifs00000springbootcashflow.adapter.repository;

import org.delcom.ifs00000springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs00000springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs00000springbootcashflow.domain.repository.ITransactionRepository;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionRepository extends InMemoryRepository<Transaction> implements ITransactionRepository {

    @Override
    protected int idOf(Transaction entity) {
        return entity.id();
    }

    @Override
    protected Transaction withId(Transaction entity, int id) {
        return entity.withId(id);
    }

    @Override
    protected String entityName() {
        return "Transaksi";
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return insert(new Transaction(0, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.userId() == userId);
    }
}
