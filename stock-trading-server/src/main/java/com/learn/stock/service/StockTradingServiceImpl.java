package com.learn.stock.service;


import com.learn.stock.entity.Stock;
import com.learn.stock.repository.StockRepository;
import com.stocktrading.*;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

  @Autowired
  private final StockRepository stockRepository;

  public StockTradingServiceImpl(StockRepository stockRepository) {
    this.stockRepository = stockRepository;
  }

  @Override
  public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
    //stockName -> fetch from DB ->map response obj -> return
    Stock stockEntity = stockRepository.findByStockSymbol(request.getStockSymbol());

    if (stockEntity == null) {
      responseObserver.onError(new RuntimeException("Stock not found"));
      return;
    }

    StockResponse stockResponse = StockResponse.newBuilder()
        .setStockSymbol(stockEntity.getStockSymbol())
        .setPrice(stockEntity.getPrice())
        .setTimestamp(stockEntity.getLastUpdated().toString())
        .build();

    responseObserver.onNext(stockResponse);
    responseObserver.onCompleted();

  }

  @Override
  public void subscribeStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
    String symbol = request.getStockSymbol();
    try {
      for (int i = 0; i <=10; i++) {
        StockResponse stockResponse = StockResponse.newBuilder()
          .setStockSymbol(symbol)
          .setPrice(new Random().nextDouble(200))
          .setTimestamp(Instant.now().toString())
          .build();

        responseObserver.onNext(stockResponse);
        TimeUnit.SECONDS.sleep(1);
      }
      responseObserver.onCompleted();
    } catch (InterruptedException e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public StreamObserver<StockOrder> bulkStockOrder(StreamObserver<OrderSummary> responseObserver) {
    return new StreamObserver<StockOrder>() {

      private int totalOrders = 0;
      private double totalAmount = 0;
      private int successCount = 0;

      @Override
      public void onNext(StockOrder stockOrder) {
        totalOrders++;
        totalAmount += stockOrder.getPrice() * stockOrder.getQuantity();
        successCount++;
        System.out.println("Order received: " + stockOrder);
      }

      @Override
      public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
      }

      @Override
      public void onCompleted() {
        OrderSummary summary = OrderSummary.newBuilder()
          .setTotalOrders(totalOrders)
          .setTotalAmount(totalAmount)
          .setSuccessCount(successCount)
          .build();
        responseObserver.onNext(summary);
        responseObserver.onCompleted();
        System.out.println("Completed");
      }
    };
  }

  @Override
  public StreamObserver<StockOrder> liveTrading(StreamObserver<TradeStatus> responseObserver) {
    return new StreamObserver<StockOrder>() {
      @Override
      public void onNext(StockOrder stockOrder) {
        System.out.println("Order received: " + stockOrder);
        String status = "EXECUTED";
        String message = "Order placed successfully";
        if (stockOrder.getQuantity() <= 0) {
          status = "FAILED";
          message = "Invalid quantity";
        }

        TradeStatus tradeStatus = TradeStatus.newBuilder()
          .setOrderId(stockOrder.getOrderId())
          .setStatus(status)
          .setMessage(message)
          .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
          .build();

        responseObserver.onNext(tradeStatus);
      }

      @Override
      public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
      }

      @Override
      public void onCompleted() {
        System.out.println("Completed");
        responseObserver.onCompleted();
      }
    };
  }
}
