# eastwind
    eastwind是一个有趣的soa框架，基于netty、etcd、kryo实现，包含二进制/HTTP RPC、EventBus、多种负载均衡、重定向功能。
    分布式Map、流控/灰度发布、自定义路由等高级功能尚在规划中。

### 1.Getting Started
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
    
### 2.EventBus

    EventBus用于向集群内所有成员广播消息。
    
  构建：
  
    public static EastWindApplication newApplicationOn(int port, String name) {
      EastWindApplicationBuilder builder = EastWindApplicationBuilder.newBuilder("test-eventbus");
      builder.onPort(port).withProperty("name", name);
      builder.onEvents(new EventBusConfig<>("hello", (t, a, b) -> {
        System.out.println(b.getProperty("name") + "-->" + a.getProperty("name") + ": " + t);
      }));
      builder.withFixedServers(":11111,:12222,:13333,:14444");
      return builder.build();
    }

  启动4个Server，端口分别为11111、12222、13333、14444,设置自定义属性name，分别为Mercury、Venus、Earth、Mars:
   
    newApplicationOn(11111, "Mercury");
    newApplicationOn(12222, "Venus");
    EastWindApplication app = newApplicationOn(13333, "Earth");
    newApplicationOn(14444, "Mars");
    app.waitForOthers().get();  // 等待与集群握手完毕
    app.eventBus("hello").publish("hello, brothers!");
    app.waitForShutdown().get();
    
  输出：
    
    Earth-->Mars: hello, brothers!
    Earth-->Venus: hello, brothers!
    Earth-->Mercury: hello, brothers!
