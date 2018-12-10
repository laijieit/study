###消息中间件的作用

	1、解耦
	2、冗余（存储）
	3、扩展性
	4、削峰
	5、可恢复性
	6、顺序保证
	7、缓冲
	8、异步通信
###RabbitMQ的具体特点

	1、可靠性：基于机制实现 持久化 传输确认 发布确认
	2、灵活的路由：内置交换器 互相绑定 依据插件实现自己的交换器
	3、扩展性：动态的集群扩展
	4、高可用性：集群之间构建镜像
	5、多种协议：除了支持AMQP 之外 还支持 STOMP MQTT
	6、多语言客户端
	7、管理界面
	8、插件机制 

###一些操作
	添加新用户 rabbitmqctl add_user root root123
	为新用户添加权限  rabbitmqctl set_permissions -p / root ".*" ".*" ".*"
	设置root为管理员角色 rabbitmqctl set_user_tags root administrator
 
NIO Reactor模式

###AMQP协议 三层
	Module Layer 协议最高层 定义了客户端调用的命令 eg Queue.Declare
	Session Layer	中间层 为客户端和服务器之间通信提供可靠性同步机制和错误处理
	Transport Layer	最底层 传输 二进制数据流 提供帧的处理 信道复用 错误检测 和数据表示

### RabbitMq消费模式分为两种 consumer/get

查看消息信息

	rabbitmqctl list_queues name messages_ready messages_unacknowledged
	
拒绝消息
	- Basic.Reject 单次拒绝
	- Basic.Nack 批量拒绝
	
消息何去何从 channel.basicPubish 两个参数
	- mandatory true 时 交换器根据自身的类型和路由健找到一个合适的队列 调用 Basic.Return() 返回数据 or 丢弃
	- immediate true 时 交换器在将消息路由到队列时发现队列上并不存在消费者，则这条消息不会存在队列里，当与路由健匹配的所有队列都没有消费者的话 返回
### 备份交换器 备胎交换器
			Map<String,Object> args = new HashMap<String,Object>();
		//设置备胎队列
		args.put("alternate-exchange", "myAge");
		channel.exchangeDeclare("normalExchange", "direct",true,false,args);
		channel.exchangeDeclare("myAge", "fanout",true,false,null);
		channel.queueDeclare("normalQueue", true, false, false, null);
		channel.queueBind("normalQueue", "normalExchange", "normalKey");	
###TTL time to live 
	- 1.通过队列属性删除 过期自动删除，因为队列里面的消息过期时间相同，只需要扫描队列首的数据就可以了
		//ttl 队列
		Map<String,Object> argss = new HashMap<String,Object>();
		argss.put("x-message-ttl", 6000);
		channel.queueDeclare("unroutedQueue", true, false, false, argss);
	-2.通过单条消息的expire属性删除 不保证过期自动删除，在消费之前判断是否过期，每条消息的过期时间都不同，如果过期就删除得重复扫描整个队列。	
		//单条消息 expire
		AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
		builder.deliveryMode(2);
		builder.expiration("6000");
		AMQP.BasicProperties properties = builder.build();
		channel.basicPublish("normalExchange", "errorlKey",properties, message.getBytes());
###死信队列
	DLX - Dead-Letter-Exchange -死信交换器，死信邮箱
		指定死信队列，消息过期或者被拒绝转发到死信队列中处理 代替immediate
		Map<String,Object> argss = new HashMap<String,Object>();
		//ttl 队列
		argss.put("x-message-ttl", 6000);
		argss.put("x-dead-letter-exchange", "exchange.dlx");
		argss.put("x-dead-letter-routing-key", "routingkey");
		channel.queueDeclare("unroutedQueue", true, false, false, argss);
### 延迟队列
	ttl 过期不被消费 normal过期后进入死信队列 正好延迟了 模拟延迟队列
### 优先级队列
	- 优先级队列
	argss.put("x--max-priority", "10");
	channel.queueDeclare("unroutedQueue", true, false, false, argss);
	- 消息的优先级	
	AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
		builder.priority(2);
		AMQP.BasicProperties properties = builder.build();	
## rpc 实现
	- 回调队列

### 持久化
	分类
	- 交换器的持久化
	- 队列的持久化
	- 消息的持久化

保证数据的安全  
	- 设置镜像队列
	- 引入事物机制
	- 发送者确认机制


	