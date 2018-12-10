package top.laijie.study.rabbitmq.demo.rpc;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RPCServer {
	private static final String EXCHANGE_NAME = "rpc.exchange_demo";
	private static final String ROUTING_KEY = "rpc.routingkey_demo";
	private static final String RPC_QUEUE_NAME ="rpc.queue_demo";
	private static final String IP_ADDRESS ="127.0.0.1";
	private static final int PORT = 5672;
	
	public static void main(String[] args) throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(IP_ADDRESS);
		factory.setPort(PORT);
		factory.setUsername("root");
		factory.setPassword("root123");
		Connection connection = factory.newConnection();
		final Channel channel = connection.createChannel();
		channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
		channel.basicQos(1);
		System.out.println("[x] Awaiting RPC requests");
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
				AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder().correlationId(properties.getCorrelationId()).build();
				String response = "";
				try {
					String message = new String(body,"UTF-8");
					int n = Integer.parseInt(message);
					System.out.println(" [.] fib("+message+")");
					response += fib(n);
				}catch (RuntimeException e) {
					System.out.println(" [.]"+e.toString());
				}finally {
					channel.basicPublish("", properties.getReplyTo(), replyProps,response.getBytes("UTF-8"));
					channel.basicAck(envelope.getDeliveryTag(), false);
				}	
			}
		};
		channel.basicConsume(RPC_QUEUE_NAME, false,consumer);
		//关闭资源
		channel.close();
		connection.close();
		// channel = connection.createChannel();
	
	}
	
	private static int fib(int n) {
		if(n==0||n==1) return n;
		return fib(n-1)+fib(n-2);
	}
}
