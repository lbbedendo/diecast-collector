package com.diecastcollector.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** A named, dated set a model can belong to (e.g. a "Factory Fresh" Hot Wheels series year). */
@Entity
@Table(name = "collection")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "year")
    private Integer year;

    public Collection() {}

    public Collection(Long id) {
        this.id = id;
    }

    public Collection(String name, Integer year) {
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
