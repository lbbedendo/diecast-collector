package com.diecastcollector.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** The diecast toy brand/manufacturer (e.g. Hot Wheels, Matchbox, California Collectibles). */
@Entity
@Table(name = "brand")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    @NotBlank
    private String name;

    public Brand() {}

    public Brand(Long id) {
        this.id = id;
    }

    public Brand(String name) {
        this.name = name;
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
}
