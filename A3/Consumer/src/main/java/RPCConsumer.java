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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public class RPCConsumer{
  private final static String QUEUE = "rmq_queue";
  public static Gson gson = new Gson();
  public static ConcurrentHashMap<Integer, List<JsonObject>> map = new ConcurrentHashMap<>();
  static Connection connection;

  public static void main(String[] args) throws IOException, TimeoutException {
    // init the factory
    ConnectionFactory factory = new ConnectionFactory();
    // factory.setHost("18.237.28.211");
    factory.setHost("54.245.143.213");
    factory.setPort(5672);
    factory.setUsername("guest");
    factory.setPassword("guest");
    System.out.println("start consumer");

    // try connection
    connection = factory.newConnection();

    // Jedis-pool config
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(256);
    JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379, 2000);
    System.out.println("connect to Redis");

    //AWS AMI, target group, elb

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try{
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE, false, false, false, null);
          // Per consumer limit
          channel.basicQos(1);

          // call back function
          DeliverCallback deliverCallback = (consumerTag, delivery) ->{
            String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
            // {"skierId":16668,"time":218,"liftId":39,"waitTime":0,"resortID":10,"seasonID":2022,"dayID":1,"vertical":390}
            /*
            “For skier N, how many days have they skied this season?”
            “For skier N, what are the vertical totals for each ski day?” (calculate vertical as liftID*10)
            “For skier N, show me the lifts they rode on each ski day”
            “How many unique skiers visited resort X on day N?”
             */
            JsonObject jsonObject = gson.fromJson(msg, JsonObject.class);
            String skierId = String.valueOf(jsonObject.get("skierId"));
            Integer vertical = Integer.valueOf(String.valueOf(jsonObject.get("vertical")));
            Integer liftId = Integer.valueOf(String.valueOf(jsonObject.get("liftId")));
            String resortId = String.valueOf(jsonObject.get("resortId"));
            String dayId = String.valueOf(jsonObject.get("dayID"));
            // System.out.println(jsonObject);
            try (Jedis jedis = jedisPool.getResource()) {
              // get skierId from jedis
              // skier Info
              try{
//                if(jedis.exists(skierId)){
////                  // update vertical
////                  Map<String, String> fields = jedis.hgetAll(skierId);
////                  System.out.println(fields);
////
////                  Integer verticalOrigin = Integer.valueOf(jedis.hget(skierId, "vertical"));
////                  Integer verticalUpdate = verticalOrigin + vertical;
////                  jedis.hset(skierId, "vertical", String.valueOf(verticalUpdate));
////
////                  // update lift
////                  Integer liftOrigin = Integer.valueOf(jedis.hget(skierId, "lifts"));
////                  Integer liftUpdate = liftOrigin + liftId;
////                  jedis.hset(skierId, "lifts", String.valueOf(liftUpdate));
//
//                }else{
//
//                }
                jedis.hset(skierId, "days", dayId);
                jedis.hset(skierId, "vertical", String.valueOf(vertical));
                jedis.hset(skierId, "lifts", String.valueOf(liftId));
                jedis.hset(skierId, "resort", resortId);
                // add skierId to set of resortId for day1
                String resort = "resort" + resortId;
                jedis.sadd(resort, skierId);

              }catch (JedisDataException exception){
                exception.printStackTrace();
              }
            }catch (JedisConnectionException exception){
              exception.printStackTrace();
            }
//            // put key and values into map
//            if(map.containsKey(skierId)){
//              map.get(skierId).add(jsonObject);
//            }else{
//              List<JsonObject> values = new ArrayList<>();
//              values.add(jsonObject);
//              map.put(skierId, values);
//            }
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          };
          channel.basicConsume(QUEUE, false, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    };

    for(int i = 0; i < 256; i ++){
      Thread thread = new Thread(runnable);
      thread.start();
    }
  }
}
