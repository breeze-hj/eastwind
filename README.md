# eastwind
    eastwind是一个有趣的soa框架，基于netty、etcd、kryo实现，包含二进制/HTTP RPC、EventBus、多种负载均衡、重定向功能。
    分布式Map、流控/灰度发布、自定义路由等高级功能尚在规划中。
    以下所有代码都在test package下。

### 1. Getting Started
#### 1.1 启动服务
  声明接口：
  
    @Feign(group = "changjiang")
    public interface HelloFeign {
        String hello();
        String hello(String group);
    }
    
  启动服务：
  
    EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("changjiang");
    // HelloProvider 实现了 HelloFeign
    EastWindApplication changjiang = builder.withProviders(new HelloProvider()).build();
    changjiang.waitForShutdown().get();
    
  EastWind默认使用18729端口，服务启动后，可以访问 http://127.0.0.1:18729 获得服务基本信息。
  
     {
      "group": "changjiang",
      "version": "default",
      "address": "0.0.0.0:18729",
      "startTime": 1543199999093,
      "providers": [{
        "feign": null,
        "class": "eastwind.rmi.HelloProvider",
        "methods": ["public abstract java.lang.String eastwind.rmi.HelloFeign.hello()", "public abstract java.lang.String eastwind.rmi.HelloFeign.hello(java.lang.String)"]
      }]
    }

#### 1.2 调用服务
  启动客户端：
  
    EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("huanghe");
    EastWindApplication huanghe = builder.onPort(18829).withFixedServers("changjiang", ":18729").build();
    HelloFeign helloFeign = huanghe.createFeignClient(HelloFeign.class);
    // 在入参中传递己方group  
    System.out.println("changjiang return:" + helloFeign.hello("huanghe"));
    // 不显示传递group
    System.out.println("changjiang return:" + helloFeign.hello());
    
  输出：
    
    changjiang return:hello, huanghe!
    changjiang return:hello, huanghe!
    
#### 1.3 握手
  客户端/服务端握手时，双方会交互基本信息，上文的 helloFeign.hello 有两种实现：
  
    @Override
    public String hello(String group) {
        return "hello, " + group + "!";
    }

    @Override
    public String hello() {
        // 从InvocationContext中获得RemoteApplication的group
        InvocationContext<String> context = InvocationContext.getContext();
        String group = context.getRemoteApplication().getGroup();
        return "hello, " + group + "!";
    }
    
#### 1.4 HTTP调用
  HTTP调用方式可以用于本机调试，需开启Java8 -parameters javac选项。
  
    http://127.0.0.1:18729/hello?group=huanghe
  
  返回：
  
    "hello, huanghe!"
  
  说明：
    
    EastWind不依赖现成的HTTP服务器，根据输入格式识别协议，单端口支持二进制和HTTP。
    
### 2. EventBus

    EventBus用于向集群内所有成员广播消息。
    
  构建：
  
    public static EastWindApplication newApplicationOn(int port, String name) {
        EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("test-eventbus");
        builder.onPort(port).withProperty("name", name);
        builder.onEvents(new EventBusConfig<>("hello", (t, a, b) -> {
	        System.out.println(b.getProperty("name") + "-->" + a.getProperty("name") + ": " + t);
		})
        );
        // 设置集群所有server地址
        builder.withFixedServers(":11111,:12222,:13333,:14444");
        return builder.build();
    }

  启动4个Server，端口分别为11111、12222、13333、14444,设置自定义属性name，分别为Mercury、Venus、Earth、Mars:
   
    newApplicationOn(11111, "Mercury");
    newApplicationOn(12222, "Venus");
    EastWindApplication app = newApplicationOn(13333, "Earth");
    newApplicationOn(14444, "Mars");
    app.waitForOthers().get();  // 等待与集群所有server握手完毕
    app.eventBus("hello").publish("hello, brothers!");  // 向集群发送消息
    app.waitForShutdown().get();
    
  输出：
    
    Earth-->Mars: hello, brothers!
    Earth-->Venus: hello, brothers!
    Earth-->Mercury: hello, brothers!

### 3. 异步

  接口：
    
    String cook(String food);
    
#### 3.1 服务端异步
  
  接口实现代码：
  
    InvocationContext<String> context = InvocationContext.getContext();
    // 设为异步方式
    context.async();
    // 由另外的线程处理
    ForkJoinPool.commonPool().execute(()->{
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
    }
    StringBuilder result = new StringBuilder();
    result.append(food).append(" with");
    // 获取额外属性
    for (Entry<Object, Object> en : context.getInvocationPropertys().entrySet()) {
        result.append(" ").append(en.getKey());
        result.append("-").append(en.getValue());
    }
    context.complete(result.toString());
    });
    return null;
    

#### 3.2 客户端异步

    // 创建 RmiTemplate
    RmiTemplate rmiTemplate = application.createRmiTemplate("food");
    Map<Object, Object> propertys = new HashMap<Object, Object>();
    propertys.put("eggs", 2);
    // 在rpc时，传入额外的propertys
    CompletableFuture<String> cf = rmiTemplate.execute("/cook", propertys, "egg-fried-rice");
    cf.thenAccept(s -> {
    	System.out.println("your " + s + " is done!");
    });
    
  输出：
  
    your egg-fried-rice with eggs-2 is done!
    
### 4. 另一种负载均衡

    负载均衡方式有多种，有些场景下，假如根据业务id路由，在JVM内存里解决一些问题，然后定时批量地刷入数据库(LSM)，能极大地降低数据库压力。
    
  ShoppingTrolley：
    
    private int uid;
    private List<String> products;
    
  ShoppingTrolleyFeign：
    
    @LoadBalanced.CONSISTENT_HASH  // 用一致性hash负载均衡
    ShoppingTrolley find(@Hashable int uid);
	
    @LoadBalanced.CONSISTENT_HASH
    void create(@Hashable ShoppingTrolley shoppingTrolley);
    
  @Hashable：
  
    表示以该参数为一致性hash算法入参。服务端，简单类型可直接加注解，复杂类型需配置HashPropertyBuilder。对客户端透明。
    
  HashPropertyBuilder:
  
    HashPropertyBuilder<ShoppingTrolley> hashPropertyBuilder = new HashPropertyBuilder<>(ShoppingTrolley.class);
    hashPropertyBuilder.getTarget().getUid();
    builder.withHashPropertyBuilders(hashPropertyBuilder);
    
  客户端：略
  
### 5. 重定向

    接口调用是幂等的，但是服务是有状态的。一致性hash能优化低一致性要求的分布式调用场景。
    某些情形下，对一致性要求较高，比如秒杀，严格要求对同一商品的请求，路由到同一服务器。
    这时，需要引入路由表，若有请求落到意外的进程，重定向至目标进程。
    
  API：

    InvocationContext<Boolean> context = InvocationContext.getContext();
    context.redirectTo(redirectTo);
    
### 5.1 例子：踢皮球

    发球员向球员踢铅球，球员不太想接球，将球踢给别的球员；尝试若干次，直到接球或抛异常。
    
  Feign:
    
    Boolean kick(Object ball);
    
  服务端实现：
    
    @Override
    public Boolean kick(Object ball) {
        try {
	    TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
        }
        if (new Random().nextInt(20) == 0) {
            return true;
        }
        InvocationContext<Boolean> context = InvocationContext.getContext();
        int times = context.redirectTimes();
        if (times >= 10) {
            throw new RuntimeException("Reach TTL!");
        }
        // 随机重定向
        List<Application> others = context.getMasterApplication().getOthers(true);
        Application redirectTo = others.get(new Random().nextInt(others.size()));
        return context.redirectTo(redirectTo);
    }
  
  客户端：
    
    KickFeign kickFeign = client.createFeignClient(KickFeign.class);
    Object ball = new Object();
    if (kickFeign.kick(ball)) {
        System.out.println("serve succeeded!");
    }
  
  客户端输出：
  
    Exception in thread "main" java.lang.RuntimeException: Reach TTL!
	at eastwind.rmi.FeignInvocationHandler.invoke(FeignInvocationHandler.java:34)
	at com.sun.proxy.$Proxy5.kick(Unknown Source)
	at eastwind.rmi.redirect.Client.main(Client.java:14)
	
  或：
    
    serve succeeded!
    
