package top.laijie.study.rabbitmq.demo;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

public class RabbitConsumer implements Runnable{
	private static final String QUEUE_NAME ="queue_demo";
	private static final String IP_ADDRESS ="127.0.0.1";
	private static final int PORT = 5672;
	private static CountDownLatch countdown = new CountDownLatch(1);
	
	private volatile static Channel channel;
	
	private volatile static Connection connection;
	
	private Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
					// TODO Auto-generated method stub
					//super.handleDelivery(consumerTag, envelope, properties, body);
					System.out.println("recv message :"+new String(body));
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("consumerTag:"+envelope.getDeliveryTag());
					channel.basicAck(envelope.getDeliveryTag(), false);
					countdown.countDown();
				};
			};
	
	public RabbitConsumer() throws IOException, TimeoutException {
		this.channel = getChannel();
	}
	public static Channel getChannel() throws IOException, TimeoutException {
		if(channel==null) {
			synchronized (RabbitConsumer.class) {
				if(channel==null) {
					Address[] address = new Address[] {new Address(IP_ADDRESS,PORT)};
					ConnectionFactory factory = new ConnectionFactory();
					factory.setUsername("root");
					factory.setPassword("root123");
					 connection = factory.newConnection(address);
					channel = connection.createChannel();
					channel.basicQos(64); //客户端最多接受未被ack的消息的个数
					System.out.println("singleton only one");
				}
			}
		}
		return channel;
	}
	private ShutdownListener listener = new ShutdownListener() {
		
		public void shutdownCompleted(ShutdownSignalException cause) {
			System.out.println("触发 close");
			if(cause.isHardError()) {
				Connection conn = (Connection)cause.getReference();
				if(cause.isInitiatedByApplication()) {
					Method reason = cause.getReason();
					System.out.println("Method");
				}
			}
		}
	};
	public void run() {
		boolean autoAck = false;
		
		try {
			connection.addShutdownListener(listener);
			channel.basicConsume("unroutedQueue", autoAck,consumer);
			countdown.await();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				channel.close();
				connection.close();
				System.out.println("to close");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		new Thread(new RabbitConsumer()).start();
		
		//new Thread(new RabbitConsumer()).start();
	}
}
