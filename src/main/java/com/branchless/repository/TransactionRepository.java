package com.branchless.repository;

import com.branchless.entity.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySourceAccountOrDestinationAccountOrderByDateDesc(String sourceAccount, String destinationAccount);
}
