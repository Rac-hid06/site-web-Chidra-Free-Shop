package com.chidrafreeshop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cat;
    private Integer price;
    private Integer oldPrice;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String image;
    private String badge;
    private String badgeType;

    public Product() {}

    public Product(String name, String cat, Integer price, Integer oldPrice, String image, String badge, String badgeType) {
        this.name = name;
        this.cat = cat;
        this.price = price;
        this.oldPrice = oldPrice;
        this.image = image;
        this.badge = badge;
        this.badgeType = badgeType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCat() { return cat; }
    public void setCat(String cat) { this.cat = cat; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getOldPrice() { return oldPrice; }
    public void setOldPrice(Integer oldPrice) { this.oldPrice = oldPrice; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }
}
