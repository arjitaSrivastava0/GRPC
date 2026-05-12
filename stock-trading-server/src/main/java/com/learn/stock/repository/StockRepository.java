package com.learn.stock.repository;

import com.learn.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
  Stock findByStockSymbol(String stockSymbol);
}
