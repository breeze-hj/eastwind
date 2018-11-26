package eastwind.rmi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eastwind.annotation.Hashable;
import eastwind.annotation.LoadBalanced;
import eastwind.model.RMD;
import eastwind.support.EWUtils;

public class RMDRegistry extends RMDAssign {

	private HashPropertyRegistry hashPropertyRegistry;
	private Multimap<Object, RmiEntity> provider2entitys = HashMultimap.create();
	private Map<String, RmiEntity> alias2entitys = new HashMap<>();

	public RMDRegistry(HashPropertyRegistry hashPropertyRegistry) {
		this.hashPropertyRegistry = hashPropertyRegistry;
	}

	public void register(Object provider) {
		Class<?> cls = provider.getClass();
		Class<?>[] interfaces = cls.getInterfaces();
		if (interfaces.length == 0) {
			interfaces = new Class<?>[] { cls };
		}
		for (Class<?> c : interfaces) {
			String feignName = EWUtils.getFeignName(c);

			Method[] methods = c.getMethods();
			for (Method method : methods) {
				RMD rmd = RMDBuilder.from(feignName, method);
				rmd.meta = processMeta(method);

				int size = rmds.get(rmd.path).size();
				if (size == 0) {
					rmd.alias = rmd.path;
				} else {
					rmd.alias += "#" + String.valueOf(size + 1);
				}
				rmd.meta = processMeta(method);
				put(rmd);
				
				RmiEntity entity = new RmiEntity(rmd.alias, method, provider);
				alias2entitys.put(rmd.alias, entity);
				provider2entitys.put(provider, entity);
			}
		}
	}

	public RmiEntity getEntity(String alias) {
		return alias2entitys.get(alias);
	}

	public Multimap<Object, RmiEntity> getEntitys() {
		return provider2entitys;
	}
	
	private Object processMeta(Method method) {
		LoadBalanced.CONSISTENT_HASH ch = method.getAnnotation(LoadBalanced.CONSISTENT_HASH.class);
		LoadBalanced loadBalanced = method.getAnnotation(LoadBalanced.class);
		if (ch != null || (loadBalanced != null
				&& loadBalanced.value().equals(EWUtils.getSimpleName(ConsistentHashRule.class)))) {
			Annotation[][] anns = method.getParameterAnnotations();
			int j = -1;
			for (int i = 0; i < anns.length; i++) {
				Annotation[] ann = anns[i];
				for (Annotation an : ann) {
					if (an instanceof Hashable) {
						j = i;
					}
				}
			}
			if (j != -1) {
				Class<?> cls = method.getParameterTypes()[j];
				HashProperty hashProperty = hashPropertyRegistry.get(cls);
				if (hashProperty == null) {
					return new ConsistentHashMeta(j, null, null);
				} else {
					return new ConsistentHashMeta(j, hashProperty.getCls().getName(), hashProperty.getPropertys());
				}
			}
		}
		return null;
	}
}
