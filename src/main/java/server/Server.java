package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.log4j.Logger;
import rabbitMQ.Producer;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2018/2/1 0001.
 */
public class Server {
    private static Logger logger = Logger.getLogger(Server.class);
    private int port;

    public Server(){}

    public Server(int port){
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void run(final Producer producer, final int queueNum) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();//用于处理服务器端接收客户端连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();//进行网络通信（读写）
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ObjectEncoder());
                            socketChannel.pipeline().addLast(new ServerHandler(producer, queueNum));

                        }
                    })
                    /**
                     * 对于ChannelOption.SO_BACKLOG的解释：
                     * 服务器端TCP内核维护有两个队列，我们称之为A、B队列。客户端向服务器端connect时，会发送带有SYN标志的包（第一次握手），服务器端
                     * 接收到客户端发送的SYN时，向客户端发送SYN ACK确认（第二次握手），此时TCP内核模块把客户端连接加入到A队列中，然后服务器接收到
                     * 客户端发送的ACK时（第三次握手），TCP内核模块把客户端连接从A队列移动到B队列，连接完成，应用程序的accept会返回。也就是说accept
                     * 从B队列中取出完成了三次握手的连接。
                     * A队列和B队列的长度之和就是backlog。当A、B队列的长度之和大于ChannelOption.SO_BACKLOG时，新的连接将会被TCP内核拒绝。
                     * 所以，如果backlog过小，可能会出现accept速度跟不上，A、B队列满了，导致新的客户端无法连接。要注意的是，backlog对程序支持的
                     * 连接数并无影响，backlog影响的只是还没有被accept取出的连接
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            logger.info("listening on port: " + port);
            f.channel().closeFuture().sync();

        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    public static void main(String[] args) throws Exception{
        Server server = new Server();
        InputStream inputStream = server.getClass().getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
        Producer producer = init(properties);
        server.setPort(Integer.valueOf(properties.getProperty("severPort")));
        server.run(producer, Integer.valueOf(properties.getProperty("queue_num")));
    }

    public static Producer init(Properties properties) throws Exception{
        String host = properties.getProperty("mq_host");
        String mq_userName = properties.getProperty("mq_userName");
        String mq_password = properties.getProperty("mq_password");
        Producer producer = new Producer(host, mq_userName, mq_password);
        int queue_num = Integer.valueOf(properties.getProperty("queue_num"));
        int channel_num_in_each_queue = Integer.valueOf(properties.getProperty("channel_num_in_each_queue"));
        for (int i = 0; i < queue_num; i++){
            producer.init("test_queue_" + i, channel_num_in_each_queue);
        }
        return producer;
    }
}
