import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;


public class RMQChannelPoolFactory extends BasePooledObjectFactory<Channel>{
  private static final String QUEUE = "rmq_queue";
  private static final String LOCALHOST = "localhost";
  private static final String EC2HOST = "18.237.28.211";
  private static final String USER = "guest";
  private static final String PWD = "guest";
  private static final int PORT = 5672;

  private final Connection connection;
  private ConnectionFactory factory;

  public RMQChannelPoolFactory() throws IOException, TimeoutException {
    this.factory = new ConnectionFactory();
    this.factory.setHost(EC2HOST);
    this.factory.setUsername(USER);
    this.factory.setPassword(PWD);
    this.factory.setPort(PORT);

    this.connection = factory.newConnection();
  }

  @Override
  synchronized public Channel create() throws Exception {
    Channel channel = this.connection.createChannel();
    return channel;
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject(channel);
  }
}
