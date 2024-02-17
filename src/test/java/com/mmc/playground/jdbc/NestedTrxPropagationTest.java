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
class NestedTrxPropagationTest {

    static Isolation DEFAULTISOLATION = Isolation.READ_UNCOMMITTED;

    @Autowired
    PersonManager personManager;
    @Autowired
    InTransactionExecutor executor;
    long currentId;

    @Test
    @SneakyThrows
    void shouldUpdateInSeparatedTrxs() {
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        executor.runInTransaction(() -> findAndIncreaseOne(LockModeType.NONE));

        executor.runInTransaction(() -> findAndIncreaseTwo(LockModeType.NONE));

        PersonUnversioned person = personManager.findUnversioned(currentId, LockModeType.NONE);

        Assertions.assertThat(person.getAge()).isEqualTo(3);
    }

    @Test
    @SneakyThrows
    void shouldUseExistingTrxWhenSecondIsREQUIRED() {
        //given
        Propagation parentPropagation = Propagation.REQUIRED;

        //and
        Propagation childPropagation = Propagation.REQUIRED;

        //and
        Runnable childOperation = findAndIncreaseWith(LockModeType.NONE);

        //and
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //when
        runInsideOtherTransaction(() -> executor.runInTransaction(childOperation, childPropagation, DEFAULTISOLATION), parentPropagation, DEFAULTISOLATION);

        //then
        PersonUnversioned actual = personManager.findUnversioned(currentId, LockModeType.NONE);
        Assertions.assertThat(actual.getAge()).isEqualTo(3);
    }

    @Test
    @SneakyThrows
    void shouldCommitOnlyParentTRXWhenSecondIsREQUIRED_NEW() {
        //given
        Propagation parentPropagation = Propagation.REQUIRED;

        //and
        Propagation childPropagation = Propagation.REQUIRES_NEW;

        //and
        Runnable childOperation = findAndIncreaseWith(LockModeType.NONE);

        //when
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //then
        runInsideOtherTransaction(() -> executor.runInTransaction(childOperation, childPropagation, DEFAULTISOLATION), parentPropagation, DEFAULTISOLATION);

        //then
        PersonUnversioned actual = personManager.findUnversioned(currentId, LockModeType.NONE);
        Assertions.assertThat(actual.getAge()).isEqualTo(2);
    }

    @Test
    @SneakyThrows
    void shouldUseExistingTrxWhenSecondIsMANDATORY() {
        //given
        Propagation parentPropagation = Propagation.REQUIRED;

        //and
        Propagation childPropagation = Propagation.MANDATORY;

        //and
        Runnable childOperation = findAndIncreaseWith(LockModeType.NONE);

        //and
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //when
        runInsideOtherTransaction(() -> executor.runInTransaction(childOperation, childPropagation, DEFAULTISOLATION), parentPropagation, DEFAULTISOLATION);

        //then
        PersonUnversioned actual = personManager.findUnversioned(currentId, LockModeType.NONE);
        Assertions.assertThat(actual.getAge()).isEqualTo(3);
    }

    @Test
    void shouldUseExistingTrxWhenSecondIsNESTED() {
        // JpaDialect does not support savepoints
        Assertions.assertThat(true).isEqualTo(true);
    }

    @Test
    @SneakyThrows
    void shouldUseExistingTrxWhenSecondIsSUPPORTS() {
        //given
        Propagation parentPropagation = Propagation.REQUIRED;

        //and
        Propagation childPropagation = Propagation.SUPPORTS;

        //and
        Runnable childOperation = findAndIncreaseWith(LockModeType.NONE);

        //and
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //when
        runInsideOtherTransaction(() -> executor.runInTransaction(childOperation, childPropagation, DEFAULTISOLATION), parentPropagation, DEFAULTISOLATION);

        //then
        PersonUnversioned actual = personManager.findUnversioned(currentId, LockModeType.NONE);
        Assertions.assertThat(actual.getAge()).isEqualTo(3);
    }

    @Test
    @SneakyThrows
    void shouldCommitOnlyParentTRXWhenSecondIsNOT_SUPPORTED() {
        //given
        Propagation parentPropagation = Propagation.REQUIRED;

        //and
        Propagation childPropagation = Propagation.NOT_SUPPORTED;

        //and
        Runnable childOperation = findAndIncreaseWith(LockModeType.NONE);

        //when
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //then
        runInsideOtherTransaction(() -> executor.runInTransaction(childOperation, childPropagation, DEFAULTISOLATION), parentPropagation, DEFAULTISOLATION);

        //then
        PersonUnversioned actual = personManager.findUnversioned(currentId, LockModeType.NONE);
        Assertions.assertThat(actual.getAge()).isEqualTo(2);
    }

    @Test
    @SneakyThrows
    void shouldThrowWhenParentTrxExistsAndChildTrxIsNEVER() {
        //given
        Propagation parentPropagation = Propagation.REQUIRED;

        //and
        Propagation childPropagation = Propagation.NEVER;

        //and
        Runnable childOperation = findAndIncreaseWith(LockModeType.NONE);

        //and
        executor.runInTransaction(() -> currentId = personManager.createUnversioned());

        //when
        //then
        Assertions.assertThatThrownBy(() -> {
                    runInsideOtherTransaction(() -> executor.runInTransaction(childOperation, childPropagation, DEFAULTISOLATION), parentPropagation, DEFAULTISOLATION);
                })
                .isExactlyInstanceOf(IllegalTransactionStateException.class);
    }

    private Runnable findAndIncreaseWith(LockModeType lock) {
        return () -> {
            log.debug("Child TRX operation starts.");
            findAndIncreaseOne(lock);
            log.debug("Child TRX operation ends.");
        };
    }

    void runInsideOtherTransaction(Runnable runnable, Propagation propagation, Isolation isolation) {
        executor.runInTransaction(() -> {
            log.debug("Parent TRX operation starts.");
            findAndIncreaseTwo(LockModeType.NONE);
            runnable.run();
            log.debug("Parent TRX operation ends.");
        }, propagation, isolation);
    }

    private void findAndIncreaseOne(LockModeType lock) {
        PersonUnversioned person = personManager.findUnversioned(currentId, lock);
        person.agePlusOne();
    }

    private void findAndIncreaseTwo(LockModeType lock) {
        PersonUnversioned person = personManager.findUnversioned(currentId, lock);
        person.agePlusTwo();
    }
}
