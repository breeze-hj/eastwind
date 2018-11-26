package eastwind.rmi;

import java.lang.reflect.Proxy;

import eastwind.annotation.Feign;
import eastwind.service.BootstrapService;
import eastwind.service.BootstrapServiceable;

public class FeignClientBuilder extends BootstrapServiceable {

	public FeignClientBuilder(BootstrapService bootstrapService) {
		super(bootstrapService);
	}

	public <T> T build(Class<T> feignCls) {
		Feign feign = feignCls.getAnnotation(Feign.class);
		String group = feign.group();
		String version = feign.version();
		String feignName = feign.name();
		RmiTemplateImpl rmiTemplate = bootstrapService.rmiTemplateBuilder().build(group, version);
		FeignInvocationHandler handler = new FeignInvocationHandler(feignName, rmiTemplate);
		@SuppressWarnings("unchecked")
		T t = (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { feignCls }, handler);
		return t;
	}

}
