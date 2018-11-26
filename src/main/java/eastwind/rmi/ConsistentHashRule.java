package eastwind.rmi;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import eastwind.service.ChannelService;

public class ConsistentHashRule implements Rule {

	private static int VIRTUAL = 50;
	
	private TreeMap<Integer, Node> tree = new TreeMap<>();
	private ConsistentHashMeta consistentHashMeta;

	@Override
	public void prepare(List<ChannelService> services) {
		tree.clear();
		for (ChannelService service : services) {
			for (int i = 0; i < VIRTUAL; i++) {
				Hasher hasher = Hashing.murmur3_32().newHasher();
				hasher.putString(service.getUuid(), Charset.forName("utf-8"));
				hasher.putInt(i);
				int h = hasher.hash().asInt() & Integer.MAX_VALUE;
				tree.put(h, new Node(h, service));
			}
		}
	}

	@Override
	public ChannelService pick(List<ChannelService> picked, RequestContext context) {
		int k = getKey(context.getArgs());
		return getNode(k).service;
	}

	private Node getNode(int hash) {
		Entry<Integer, Node> en = tree.ceilingEntry(hash);
		if (en == null) {
			en = tree.firstEntry();
		}
		return en.getValue();
	}
	
	private int getKey(Object[] args) {
		Object obj = args[consistentHashMeta.i];
		if (obj == null) {
			return 0;
		}
		List<String> propertys = consistentHashMeta.propertys;
		Hasher hasher = Hashing.murmur3_32().newHasher();
		if (propertys != null && propertys.size() > 0) {
			for (String property : propertys) {
				try {
					// cache
					PropertyDescriptor pd = new PropertyDescriptor(property, obj.getClass());
					Object result = pd.getReadMethod().invoke(obj);
					hasher.putString(String.valueOf(result), Charset.forName("utf-8"));
				} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		} else {
			hasher.putString(String.valueOf(obj), Charset.forName("utf-8"));
		}
		return hasher.hash().asInt() & Integer.MAX_VALUE;
	}

	@Override
	public void setMeta(Object meta) {
		this.consistentHashMeta = (ConsistentHashMeta) meta;
	}
	
	static class Node {
		int hash;
		ChannelService service;
		
		public Node(int hash, ChannelService service) {
			this.hash = hash;
			this.service = service;
		}
	}
}
