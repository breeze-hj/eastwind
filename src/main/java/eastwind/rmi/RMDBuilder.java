package eastwind.rmi;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ClassUtils;

import eastwind.annotation.LoadBalanced;
import eastwind.model.RMD;
import eastwind.support.EWUtils;

public class RMDBuilder {

	public static RMD from(String path, Object[] args) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		RMD rmd = new RMD();
		rmd.path = path;
		if (args == null) {
			rmd.argTypes = new Class<?>[0];
		} else {
			rmd.argTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] != null) {
					Class<?> cls = args[i].getClass();
					if (cls.isPrimitive()) {
						cls = ClassUtils.primitiveToWrapper(cls);
					}
					rmd.argTypes[i] = cls;
				}
			}
		}
		return rmd;
	}

	public static RMD from(String feignName, Method m) {
		RMD rmd = new RMD();
		if (feignName == null) {
			rmd.path = m.getName();
		} else {
			rmd.path = feignName + "/" + m.getName();
		}
		if (!rmd.path.startsWith("/")) {
			rmd.path = "/" + rmd.path;
		}
		rmd.supply = true;
		rmd.argTypes = ClassUtils.primitivesToWrappers(m.getParameterTypes());
		if (m.getAnnotation(LoadBalanced.CONSISTENT_HASH.class) != null) {
			rmd.rule = EWUtils.getSimpleName(ConsistentHashRule.class);
		} else if (m.getAnnotation(LoadBalanced.RANDOM.class) != null) {
			rmd.rule = EWUtils.getSimpleName(ConsistentHashRule.class);
		} else {
			LoadBalanced loadBalanced = m.getAnnotation(LoadBalanced.class);
			if (loadBalanced == null) {
				rmd.rule = EWUtils.getSimpleName(RandomRule.class);
			} else {
				rmd.rule = loadBalanced.value();
			}
		}
		return rmd;
	}
}
