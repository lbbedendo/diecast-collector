package com.diecastcollector.api.domain;

import com.diecastcollector.api.enums.ModelCondition;
import com.diecastcollector.api.enums.ModelPackaging;
import com.diecastcollector.api.enums.ModelScale;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "model")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false)
    @NotEmpty
    private String name;

    @Column(name = "model_year")
    private Integer modelYear;

    @Column(name = "scale", length = 10)
    @Enumerated(EnumType.STRING)
    private ModelScale scale;

    @Column(name = "color")
    private String color;

    @Column(name = "packaging", length = 20)
    @Enumerated(EnumType.STRING)
    private ModelPackaging packaging;

    @Column(name = "condition", length = 20)
    @Enumerated(EnumType.STRING)
    private ModelCondition condition;

    /** Position within the Series as printed by the Brand, kept as written (e.g. "10/10"). */
    @Column(name = "series_number", length = 50)
    private String seriesNumber;

    /** Generic stand-in for brand-specific "rare variant" flags (Treasure Hunt, Super TH, Premium, etc). */
    @Column(name = "is_chase", nullable = false)
    private boolean chase = false;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchased_from")
    private String purchasedFrom;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "photo_url", columnDefinition = "text")
    private String photoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automaker_id")
    private Automaker automaker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Model() {}

    public Model(Long id) {
        this.id = id;
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public void setModelYear(Integer modelYear) {
        this.modelYear = modelYear;
    }

    public ModelScale getScale() {
        return scale;
    }

    public void setScale(ModelScale scale) {
        this.scale = scale;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ModelPackaging getPackaging() {
        return packaging;
    }

    public void setPackaging(ModelPackaging packaging) {
        this.packaging = packaging;
    }

    public ModelCondition getCondition() {
        return condition;
    }

    public void setCondition(ModelCondition condition) {
        this.condition = condition;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public boolean isChase() {
        return chase;
    }

    public void setChase(boolean chase) {
        this.chase = chase;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPurchasedFrom() {
        return purchasedFrom;
    }

    public void setPurchasedFrom(String purchasedFrom) {
        this.purchasedFrom = purchasedFrom;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Automaker getAutomaker() {
        return automaker;
    }

    public void setAutomaker(Automaker automaker) {
        this.automaker = automaker;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
