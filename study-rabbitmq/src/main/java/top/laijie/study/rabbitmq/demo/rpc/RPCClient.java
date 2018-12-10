package top.laijie.study.rabbitmq.demo.rpc;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.AMQP.BasicProperties;


public class RPCClient {
	private String requestQueueName ="rpc.queue_demo";
	private String replyQueueName;
	private static final String IP_ADDRESS ="127.0.0.1";
	private static final int PORT = 5672;
	private Connection connection;
	private Channel channel;
	private String corrId ;
	private String response = null;
	public RPCClient() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
//		factory.setHost(IP_ADDRESS);
//		factory.setPort(PORT);
		Address[] address = new Address[] {new Address(IP_ADDRESS,PORT)};
		factory.setUsername("root");
		factory.setPassword("root123");
		 connection = factory.newConnection(address);
		channel = connection.createChannel();
		replyQueueName = channel.queueDeclare().getQueue();
		corrId = UUID.randomUUID().toString();
	}
	
	Consumer consumer = new DefaultConsumer(channel) {
		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
			if(properties.getCorrelationId().equals(corrId)) {
				response= new String(body);
			}
		}
	};
	public String call(String message) throws IOException {
		
		
		BasicProperties props = new BasicProperties.Builder()
			.correlationId(corrId)
			.replyTo(replyQueueName)
			.build();
		channel.basicPublish("", requestQueueName, props, message.getBytes());
		channel.basicConsume(replyQueueName, true, consumer);
		return response;
	}
	
	public void close() throws IOException {
		connection.close();
	}
	public static void main(String[] args) throws IOException, TimeoutException, CloneNotSupportedException {
		RPCClient fibRpc = new RPCClient();
		System.out.println(" [x] Requesting fib(30)");
		String response = fibRpc.call("30");
		System.out.println(" [.] Got '"+response+"'");
		fibRpc.clone();
	}
	
	
}
