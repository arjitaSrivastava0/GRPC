package com.learn.stock.service;


import com.stocktrading.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class StockClientService {

  @GrpcClient("stockService")
  private StockTradingServiceGrpc.StockTradingServiceStub stockTradingServiceStub;

//  public StockResponse getStockPrice(String stockSymbol) {
//    StockRequest stockRequest = StockRequest.newBuilder().setStockSymbol(stockSymbol).build();
//    return stockTradingServiceStub.getStockPrice(stockRequest);
//  }



  public void subscribeStockPrice(String stockSymbol) {
    StockRequest stockRequest = StockRequest.newBuilder().setStockSymbol(stockSymbol).build();
    stockTradingServiceStub.subscribeStockPrice(stockRequest, new StreamObserver<StockResponse>() {
      @Override
      public void onNext(StockResponse response) {
        System.out.println("Stock price update: " + response.getStockSymbol() +
          " Price: " + response.getPrice() +
          " Time: " + response.getTimestamp());
      }

      @Override
      public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
      }

      @Override
      public void onCompleted() {
        System.out.println("Stream completed");
      }
    });
  }

  public void placeStockOrder() {

    StreamObserver<OrderSummary> responseObserver = new StreamObserver<OrderSummary>() {
      @Override
      public void onNext(OrderSummary value) {
        System.out.println("Order summary receive from the server ");
        System.out.println("Total orders: " + value.getTotalOrders());
        System.out.println("Total amount: " + value.getTotalAmount());
        System.out.println("Successfull orders: " + value.getSuccessCount());
      }

      @Override
      public void onError(Throwable t) {
        System.out.println("Order summary received an error from the surver" + t.getMessage());
      }

      @Override
      public void onCompleted() {
        System.out.println("Stream completed");
      }
    };

    StreamObserver<StockOrder> requestObserver = stockTradingServiceStub.bulkStockOrder(responseObserver);
    //send multiple stream of stock order message/request

    try {
      requestObserver.onNext(
        StockOrder.newBuilder()
          .setOrderId("1")
          .setStockSymbol("AAPL")
          .setQuantity(10)
          .setPrice(150.5)
          .setOrderType("BUY")
          .build()
      );

      requestObserver.onNext(
        StockOrder.newBuilder()
          .setOrderId("2")
          .setStockSymbol("GOOGL")
          .setQuantity(5)
          .setPrice(2500.0)
          .setOrderType("SELL")
          .build()
      );

      requestObserver.onNext(
        StockOrder.newBuilder()
          .setOrderId("3")
          .setStockSymbol("MSFT")
          .setQuantity(8)
          .setPrice(300.0)
          .setOrderType("BUY")
          .build()
      );

      //done sending orders
      requestObserver.onCompleted();
    } catch (Exception e) {
      requestObserver.onError(e);
    }
  }


  public void startLiveTrading() throws InterruptedException {
    StreamObserver<StockOrder> requestObserver = stockTradingServiceStub.liveTrading(new StreamObserver<>() {
      @Override
      public void onNext(TradeStatus status) {
        System.out.println("Server response : " + status);
      }

      @Override
      public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
      }

      @Override
      public void onCompleted() {
        System.out.println("Stream completed");
      }
    });

    //send multiple request from client
    for(int i = 1; i <= 10; i++) {
      StockOrder stockOrder = StockOrder.newBuilder()
        .setOrderId(String.valueOf("ORDER-"+i))
        .setStockSymbol("AAPL")
        .setQuantity(i*10)
        .setPrice(150.5+i)
        .setOrderType("BUY")
        .build();

      requestObserver.onNext(stockOrder);
      Thread.sleep(1000);
    }
    requestObserver.onCompleted();
  }
}
