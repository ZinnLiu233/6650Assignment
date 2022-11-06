package org;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP.Basic.Deliver;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RPCConsumer{
  private final static String QUEUE = "rmq_queue";
  public static Gson gson = new Gson();
  public static ConcurrentHashMap<Integer, List<JsonObject>> map = new ConcurrentHashMap<>();
  static Connection connection;

  public static void main(String[] args) throws IOException, TimeoutException {
    // init the factory
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("35.90.189.13");
    factory.setPort(5672);
    factory.setUsername("guest");
    factory.setPassword("guest");

    // try connection
    connection = factory.newConnection();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try{
          System.out.println(map.size());
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE, false, false, false, null);
          // Per consumer limit
          channel.basicQos(1);

          // call back function
          DeliverCallback deliverCallback = (consumerTag, delivery) ->{
            String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
            JsonObject jsonObject = gson.fromJson(msg, JsonObject.class);
            Integer skierId = Integer.valueOf(String.valueOf(jsonObject.get("skierId")));

            // put key and values into map
            if(map.containsKey(skierId)){
              map.get(skierId).add(jsonObject);
            }else{
              List<JsonObject> values = new ArrayList<>();
              values.add(jsonObject);
              map.put(skierId, values);
            }
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          };
          channel.basicConsume(QUEUE, false, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    };

    for(int i = 0; i < 10; i ++){
      Thread thread = new Thread(runnable);
      thread.start();
    }
  }
}
