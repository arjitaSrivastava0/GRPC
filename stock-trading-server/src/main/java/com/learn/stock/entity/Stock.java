package com.learn.stock.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
public class Stock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // works with SERIAL
  private Long id;

  @Column(name = "stock_symbol", nullable = false, unique = true, length = 10)
  private String stockSymbol;

  @Column(nullable = false)
  private Double price;

  @Column(name = "last_updated")
  private LocalDateTime lastUpdated;

  @Column(nullable = false, length = 3)
  private String currency;

  // --- Constructors ---
  public Stock() {}

  public Stock(String stockSymbol, Double price, String currency) {
    this.stockSymbol = stockSymbol;
    this.price = price;
    this.currency = currency;
  }

  // --- Lifecycle hooks (auto timestamp) ---
  @PrePersist
  public void onCreate() {
    this.lastUpdated = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.lastUpdated = LocalDateTime.now();
  }

  // --- Getters & Setters ---
  public Long getId() {
    return id;
  }

  public String getStockSymbol() {
    return stockSymbol;
  }

  public void setStockSymbol(String stockSymbol) {
    this.stockSymbol = stockSymbol;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}