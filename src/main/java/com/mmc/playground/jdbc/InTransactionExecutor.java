package com.mmc.playground.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class InTransactionExecutor {

    private final PlatformTransactionManager transactionManager;

    public void runInTransaction(Runnable runnable) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        transactionTemplate.setIsolationLevel(Isolation.DEFAULT.value());

        transactionTemplate.execute(status -> {
            runnable.run();
            return null;
        });
    }
    public void runInTransaction(Runnable runnable, Propagation propagation, Isolation isolation) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(propagation.value());
        transactionTemplate.setIsolationLevel(isolation.value());

        transactionTemplate.execute(status -> {
            runnable.run();
            return null;
        });
    }
}
