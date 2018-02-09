package rabbitMQ;

import com.rabbitmq.client.MessageProperties;
import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

public class Producer extends RabbitMQFactory {
    private static String prefix = "test_queue_";
    public Producer(String host, String username, String password) throws IOException, TimeoutException {
        super(host, username, password);
    }

    public void sendMessage(Serializable Object, int no, int queueNum) throws IOException{
        channel.basicPublish("",generateQueueName(no, queueNum), MessageProperties.PERSISTENT_TEXT_PLAIN, SerializationUtils.serialize(Object));
    }

    public String generateQueueName(int no, int queueNum){
        int m = no % queueNum;
        return prefix + m;
    }

}
