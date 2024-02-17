package com.mmc.playground.jdbc.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonUnversioned {

    @Id
    @GeneratedValue
    Long id;

    int age;

    public void agePlusOne() {
        age++;
    }

    public void agePlusTwo() {
        age += 2;
    }
}