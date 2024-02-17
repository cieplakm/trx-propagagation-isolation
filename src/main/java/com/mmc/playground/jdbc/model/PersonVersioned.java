package com.mmc.playground.jdbc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonVersioned {

    @Id
    @GeneratedValue
    Long id;

    int age;

    @Version
    int version;

    public void agePlusOne() {
        age++;
    }
}
