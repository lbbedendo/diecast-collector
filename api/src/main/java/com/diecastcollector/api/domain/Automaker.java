package com.diecastcollector.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** The real-world car manufacturer the model depicts (e.g. Honda, Porsche). */
@Entity
@Table(name = "automaker")
public class Automaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "country", length = 100)
    private String country;

    public Automaker() {}

    public Automaker(Long id) {
        this.id = id;
    }

    public Automaker(String name, String country) {
        this.name = name;
        this.country = country;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
