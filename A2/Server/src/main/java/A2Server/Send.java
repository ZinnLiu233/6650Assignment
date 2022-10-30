package A2Server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Send {

  private final static String QUEUE = "skier_queue";


  public static void main(String[] args) throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");

    // try connect
    Connection connection = factory.newConnection();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try{
          Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE, false, false, false, null);
          for(int i = 0; i < 20; i ++){
            String msg = "No." + i + ":";
            channel.basicPublish("", QUEUE, null, msg.getBytes(StandardCharsets.UTF_8));
          }
          channel.close();
        } catch (IOException | TimeoutException e) {
          throw new RuntimeException(e);
        }
      }
    };

    Thread thread = new Thread(runnable);
    thread.start();
    connection.close();
  }
}
