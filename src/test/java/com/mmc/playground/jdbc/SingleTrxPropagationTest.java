package com.mmc.playground.jdbc;

import com.mmc.playground.jdbc.model.PersonUnversioned;
import jakarta.persistence.LockModeType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

@SpringBootTest
@Slf4j
class SingleTrxPropagationTest {

    static Isolation DEFAULTISOLATION = Isolation.READ_UNCOMMITTED;

    @Autowired
    PersonManager personManager;
    @Autowired
    InTransactionExecutor executor;
    long currentId;

    @Test
    @SneakyThrows
    void shouldUpdateWithinSingleTrxWhenPropagationIsREQUIRED() {
        //given
        Propagation trxPropagation = Propagation.REQUIRED;

        //and
        Runnable operation = findAndIncreaseTwoOperation(LockModeType.NONE);

        //and
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //when
        executor.runInTransaction(operation, trxPropagation, DEFAULTISOLATION);

        //then
        PersonUnversioned actual = personManager.findUnversioned(currentId, LockModeType.NONE);
        Assertions.assertThat(actual.getAge()).isEqualTo(2);
    }

    @Test
    @SneakyThrows
    void shouldUpdateWithinSingleTrxWhenPropagationIsREQUIRES_NEW() {
        //given
        Propagation trxPropagation = Propagation.REQUIRES_NEW;

        //and
        Runnable operation = findAndIncreaseTwoOperation(LockModeType.NONE);

        //and
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //when
        executor.runInTransaction(operation, trxPropagation, DEFAULTISOLATION);

        //then
        PersonUnversioned actual = personManager.findUnversioned(currentId, LockModeType.NONE);
        Assertions.assertThat(actual.getAge()).isEqualTo(2);
    }

    @Test
    @SneakyThrows
    void shouldThrowWhenTrxNotExistsAndPropagationIsMANDATORY() {
        //given
        Propagation trxPropagation = Propagation.MANDATORY;

        //and
        Runnable operation = findAndIncreaseTwoOperation(LockModeType.NONE);

        //when
        //then
        Assertions.assertThatThrownBy(() -> executor.runInTransaction(operation, trxPropagation, DEFAULTISOLATION))
                .isExactlyInstanceOf(IllegalTransactionStateException.class);
    }

    private Runnable findAndIncreaseTwoOperation(LockModeType lock) {
        return () -> {
            log.debug("Operation starts.");
            PersonUnversioned person = personManager.findUnversioned(currentId, lock);
            person.agePlusTwo();
            log.debug("Operation ends.");
        };
    }
}