package com.diecastcollector.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** A named group of models a Brand releases together in a given year (e.g. "HW Starting Grid" 2026). */
@Entity
@Table(name = "series")
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "year")
    private Integer year;

    public Series() {}

    public Series(Long id) {
        this.id = id;
    }

    public Series(String name, Integer year) {
        this.name = name;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
