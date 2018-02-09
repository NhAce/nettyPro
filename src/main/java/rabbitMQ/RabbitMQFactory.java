package rabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public abstract class RabbitMQFactory {
    protected List<Map<String, Channel>> channels;

    protected List<Connection> connections;

    protected Connection connection;

    protected Channel channel;

    protected String queueName;

    protected ConnectionFactory factory;

    public RabbitMQFactory(String host, String username, String password) throws IOException,TimeoutException{
        //Create a connection factory
        this.factory = new ConnectionFactory();

        //hostname of your rabbitmq server
        this.factory.setHost(host);
        this.factory.setUsername(username);
        this.factory.setPassword(password);
    }

    public void init(String queueName, int channelNum) throws Exception{
        this.queueName = queueName;
        //getting a connection
        connection = this.factory.newConnection();
        //creating a channel
        for (int i = 0; i < channelNum; i++) {
            channel = connection.createChannel();
            //declaring a queue for this channel. If queue does not exist,
            //it will be created on the server.
            channel.queueDeclare(queueName,true,false,false,null);
        }
    }

    public void close() throws IOException,TimeoutException{
        this.channel.close();
        this.connection.close();
    }
}
