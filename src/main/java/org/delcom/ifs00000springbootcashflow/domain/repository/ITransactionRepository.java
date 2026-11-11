package org.delcom.ifs00000springbootcashflow.domain.repository;

import org.delcom.ifs00000springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs00000springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /** Membuat transaksi baru; id dan createdAt diisi repository. */
    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath)
            throws DomainException;

    List<Transaction> findByUserId(int userId);
}
