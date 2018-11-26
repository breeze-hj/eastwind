package eastwind.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;

import eastwind.annotation.Feign;
import eastwind.rmi.RMDRegistry;
import eastwind.rmi.RmiEntity;
import eastwind.service.BootstrapService;
import eastwind.service.BootstrapServiceable;
import eastwind.support.EWUtils;

@Feign(group = "eastwind")
public class ActuatorController extends BootstrapServiceable {

	public ActuatorController(BootstrapService bootstrapService) {
		super(bootstrapService);
	}

	public Object _info() {
		LinkedHashMap<Object, Object> info = new LinkedHashMap<>();
		info.put("group", bootstrapService.getGroup());
		info.put("version", bootstrapService.getVersion());
		info.put("address", bootstrapService.getAddress());
		info.put("startTime", bootstrapService.getStartTime());

		RMDRegistry rmdRegistry = bootstrapService.getRMdRegistry();
		Multimap<Object, RmiEntity> entitys = rmdRegistry.getEntitys();
		List<Map<String, Object>> l = new ArrayList<>();
		for (Entry<Object, Collection<RmiEntity>> en1 : entitys.asMap().entrySet()) {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("feign", EWUtils.getFeignName(en1.getKey().getClass()));
			map.put("class", en1.getKey().getClass().getName());
			List<String> methods = en1.getValue().stream().map(e -> e.getMethod().toString())
					.collect(Collectors.toList());
			map.put("methods", methods);
			l.add(map);
		}
		info.put("providers", l);

		return info;
	}

}
