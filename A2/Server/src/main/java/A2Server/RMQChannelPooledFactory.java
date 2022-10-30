package A2Server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;

import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RMQChannelPooledFactory extends BasePooledObjectFactory<Channel> {
  private int count;
  private final Connection connection;
  private final ConnectionFactory factory;
  public static final String LOCAL_HOST = "localhost";
  public static final String EC2_HOST = "";
  public static final String USER_PWD = "guest";

  // init factory
  public RMQChannelPooledFactory() throws IOException, TimeoutException {
    this.factory = new ConnectionFactory();
    this.factory.setHost(LOCAL_HOST);
    this.factory.setUsername(USER_PWD);
    this.factory.setPassword(USER_PWD);
    this.count = 0;
    this.connection = factory.newConnection();
  }

  @Override
  public Channel create() throws Exception {
    count ++;
    return this.connection.createChannel();
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<>(channel);
  }
}
