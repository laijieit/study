package top.laijie.study.rabbitmq.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.omg.CORBA.PERSIST_STORE;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class RabbitProducer {
	private static final String EXCHANGE_NAME = "exchange_demo";
	private static final String EXCHANGE_NANE2 = "exchange_demo2";
	private static final String ROUTING_KEY = "routingkey_demo";
	private static final String QUEUE_NAME ="queue_demo";
	private static final String IP_ADDRESS ="127.0.0.1";
	private static final int PORT = 5672;
	public static void normal() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(IP_ADDRESS);
		factory.setPort(PORT);
		factory.setUsername("root");
		factory.setPassword("root123");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		//创建一个type为direct 持久化的 非自动删除的 交换器 
		//交换器类型可以为 fanout direct topic
		channel.exchangeDeclare(EXCHANGE_NAME, "direct",true,false,null);
		channel.exchangeDeclare(EXCHANGE_NANE2, "fanout",true,false,null);
		//交换器绑定 -持久化的交换器无法绑定非持久化的交换器
		channel.exchangeBind(EXCHANGE_NANE2, EXCHANGE_NAME, ROUTING_KEY);
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		//交换器和队列绑定
		channel.queueBind(QUEUE_NAME, EXCHANGE_NANE2, ROUTING_KEY);
		String message = "Hello world!";
		for (Integer i = 0; i < 100; i++) {
			channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY,MessageProperties.PERSISTENT_TEXT_PLAIN , message.concat(i.toString()).getBytes());
		}
		//关闭资源
		channel.close();
		connection.close();
		// channel = connection.createChannel();
	}
	
	public static void alternateExchange() throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(IP_ADDRESS);
		factory.setPort(PORT);
		factory.setUsername("root");
		factory.setPassword("root123");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		Map<String,Object> args = new HashMap<String,Object>();
		//设置 备份交换器 备胎交换器
		args.put("alternate-exchange", "myAge");
		channel.exchangeDeclare("normalExchange", "direct",true,false,args);
		channel.exchangeDeclare("myAge", "fanout",true,false,null);
		channel.queueDeclare("normalQueue", true, false, false, null);
		channel.queueBind("normalQueue", "normalExchange", "normalKey");
		
		Map<String,Object> argss = new HashMap<String,Object>();
		//ttl 队列
		argss.put("x-message-ttl", 6000);
		// 指定死信
		argss.put("x-dead-letter-exchange", "exchange.dlx");
		argss.put("x-dead-letter-routing-key", "routingkey");
		//优先级队列
		argss.put("x--max-priority", "10");
		
		channel.queueDeclare("unroutedQueue", true, false, false, argss);
		channel.queueBind("unroutedQueue", "myAge", "");
		String message = "Hello world!2333";
		//单条消息 expire
		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
		builder.deliveryMode(2);
		builder.expiration("6000");
		AMQP.BasicProperties properties = builder.build();
		channel.basicPublish("normalExchange", "errorlKey",properties, message.getBytes());
		System.out.println("发送者发送成功～");
		//关闭资源
		channel.close();
		connection.close();
		// channel = connection.createChannel();
	
		
	}
	public static void main(String[] args) throws IOException, TimeoutException {
		//RabbitProducer.normal();
		RabbitProducer.alternateExchange();
	}
	
}
