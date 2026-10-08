package com.diecastcollector.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** A named group of models a Brand releases together in a given year (e.g. "HW Starting Grid" 2026). */
@Entity
@Table(name = "series", uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "name", "year"}))
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "year")
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    public Series() {}

    public Series(Long id) {
        this.id = id;
    }

    public Series(Brand brand, String name, Integer year) {
        this.brand = brand;
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

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
