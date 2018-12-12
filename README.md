# eastwind
    eastwind是一个有趣的SOA框架(半成品，供学习交流)，基于netty实现，包含二进制/HTTP RPC、EventBus、多种负载均衡、重定向、自动发现功能。
    分布式Map、流控/灰度发布、自定义路由等高级功能尚在规划中。
    以下所有代码都在test package下。

## 目录
* [Getting-Started](#1-Getting-Started)
* [EventBus](#2-EventBus)
* [异步](#3-异步)
* [另一种负载均衡](#4-另一种负载均衡)
* [重定向](#5-重定向)
  * [例子：踢皮球](#5-1例子-踢皮球)
* [自动发现与选举](#6-自动发现与选举)

### 1-Getting-Started
#### 1-1启动服务
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

#### 1-2握手
  
  客户端/服务端握手时，双方会交互基本信息，上文的 helloFeign.hello， 服务端有两种实现：
  
    @Override
    public String hello(String group) { // 显式传递客户端名称
        return "hello, " + group + "!";
    }

    @Override
    public String hello() {
        // 从InvocationContext中获得RemoteApplication的group
        InvocationContext<String> context = InvocationContext.getContext();
        String group = context.getRemoteApplication().getGroup();
        return "hello, " + group + "!";
    }
    
#### 1-3调用服务
  启动客户端：
  
    EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("huanghe");
    EastWindApplication huanghe = builder.onPort(18829).withFixedServers("changjiang", ":18729").build();
    HelloFeign helloFeign = huanghe.createFeignClient(HelloFeign.class);
    // 在入参中传递己方group  
    System.out.println("changjiang return:" + helloFeign.hello("huanghe"));
    // 不显式传递group
    System.out.println("changjiang return:" + helloFeign.hello());
    
  输出：
    
    changjiang return:hello, huanghe!
    changjiang return:hello, huanghe!

#### 1-4HTTP调用
  HTTP调用方式可以用于本机调试，需开启Java8 -parameters javac选项。
  
    http://127.0.0.1:18729/hello?group=huanghe
  
  返回：
  
    "hello, huanghe!"
  
  说明：
    
    EastWind不依赖现成的HTTP服务器，内部由netty实现，根据输入格式识别协议，单端口支持二进制和HTTP。
    
### 2-EventBus

    EventBus用于向集群内所有成员广播消息。
    
  构建：
  
    public static EastWindApplication newApplicationOn(int port, String name) {
        EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("test-eventbus");
        builder.onPort(port).withProperty("name", name);
        builder.onEvents(new EventBusConfig<>("hello", (msg, local, remote) -> {
	        System.out.println(remote.getProperty("name") + "-->" + local.getProperty("name") + ": " + msg);
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

### 3-异步

  接口：
    
    String cook(String food);
    
#### 3-1服务端异步
  
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
    

#### 3-2客户端异步

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
    
### 4-另一种负载均衡

    负载均衡方式有多种，有些场景下，假如根据业务id路由，在JVM内存里解决一些问题，然后定时批量地刷入数据库(LSM)，能极大地降低数据库压力。
    
  ShoppingTrolley：
    
    private int uid;
    private List<String> products;
    
  ShoppingTrolleyFeign：
    
    @LoadBalanced.CONSISTENT_HASH  // 用一致性hash负载均衡
    ShoppingTrolley find(@Hashable int uid);
	
    @LoadBalanced.CONSISTENT_HASH  // 用一致性hash负载均衡
    void create(@Hashable ShoppingTrolley shoppingTrolley);
    
  @Hashable：
  
    表示以该参数为一致性hash算法入参。服务端，简单类型可直接加注解，复杂类型需配置HashPropertyBuilder。对客户端透明。
    
  HashPropertyBuilder:
  
    HashPropertyBuilder<ShoppingTrolley> hashPropertyBuilder = new HashPropertyBuilder<>(ShoppingTrolley.class);
    // 表示由该属性计算hash值
    hashPropertyBuilder.getTarget().getUid();
    builder.withHashPropertyBuilders(hashPropertyBuilder);
    
  客户端：略
  输出效果：同一ud的find/create RPC由同一server处理。
  
### 5-重定向

    接口调用是幂等的，但是服务是有状态的。一致性hash能优化低一致性要求的分布式调用场景。
    某些情形下，对一致性要求较高，比如秒杀，严格要求对同一商品的请求，路由到同一服务器。
    这时，需要引入路由表，若有请求落到意外的server，重定向至目标server。
    
  API：

    InvocationContext<Boolean> context = InvocationContext.getContext();
    context.redirectTo(redirectTo);
    
#### 5-1例子-踢皮球

    发球员向球员踢铅球，球员不太想接球，将球踢给别的球员；尝试若干次，直到接球或抛异常。(For Fun)
    
  Feign:
    
    Boolean kick(Object ball);
    
  服务端实现：
    
    @Override
    public Boolean kick(Object ball) {
        try {
	    TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
        }
        // 1/20 几率调用成功
        if (new Random().nextInt(20) == 0) {
            return true;
        }
        // 判断重定向次数
        InvocationContext<Boolean> context = InvocationContext.getContext();
        int times = context.redirectTimes();
        if (times >= 10) {
            throw new RuntimeException("Reach TTL!");
        }
        // 随机重定向至其他可用server
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
    
### 6-自动发现与选举
#### 6-1自动发现

    对于[EventBus](#2-EventBus)中的例子，
    builder.withFixedServers(":11111,:12222,:13333,:14444")，
    无须写全所有地址，只要同组服务节点之间有链路，所有节点都能互相发现。内部通过选举-广播实现。
    
 #### 6-2选举
 
     假设某服务有a、b、c、d四个节点，选举名称最小的节点即a为leader。选举规则：
     1、每个节点尽可能多地连接其他节点
     2、每个follower有1次投票机会，投票/转投时，广播所有相邻节点
     3、follower收到其他节点投票广播消息时，假如投票目标不是己方candidate，通知己方candidate(有分歧)
     4、candidate冲突时，低优先级让位，并通知follower转投高优先级candidate(消除分歧)
     5、整个quorum消除分歧后，产生leader
     
 ##### 情况1
 
     {a、b、c、d} --> {a、b、c、d}
     表示abcd都设置Servers为abcd，这是最简单的情况，各节点都能感知其他节点的存在。
     此时选举分两步：
     1、abcd选a
     2、a成为leader
     
##### 情况2

    {b、c、d} --> {b、c、d}
    a --> b
    
  选举过程：
    
    1、bcd选b
    2、a选a
    3、b选a并通知cd选a
    4、a成为leader
    
##### 情况3

    a --> d
    c --> b
    b --> {} // 空
    d --> c
    
  链路，a--d--c--b，可能的选举过程：
  
    1、ad选a，bc选b
    2、d将b通知a，c将a通知b(向candidate通知分歧)
    3、b选a，并通知c选a
    4、a成为leader
    
##### 脑裂

    由于集群节点数不固定不明确，节点不知道所有成员节点，所以情况2、3存在脑裂的可能。
    配置地址时，应采用情况1配置方式，各新旧节点配置同样的服务地址，避免脑裂。
