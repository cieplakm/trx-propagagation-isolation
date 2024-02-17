package com.mmc.playground.jdbc;

import com.mmc.playground.jdbc.model.PersonUnversioned;
import com.mmc.playground.jdbc.model.PersonVersioned;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PersonManager {

    @PersistenceContext
    private EntityManager entityManager;


    long createVersioned() {
        PersonVersioned person = PersonVersioned.builder()
                .build();

        entityManager.persist(person);
        entityManager.flush();

        return person.getId();
    }

    long createUnversioned() {
        PersonUnversioned person = PersonUnversioned.builder()
                .build();

        entityManager.persist(person);
        entityManager.flush();

        return person.getId();
    }

    void deleteAllEntities() {
        entityManager.createQuery("DELETE FROM PersonVersioned").executeUpdate();
    }

    public PersonVersioned findVersioned(Long id, LockModeType lockModeType) {
        return entityManager.find(PersonVersioned.class, id, lockModeType);
    }

    public PersonUnversioned findUnversioned(Long id, LockModeType lockModeType) {
        return entityManager.find(PersonUnversioned.class, id, lockModeType, Map.of("javax.persistence.lock.timeout", 0));
    }
}
