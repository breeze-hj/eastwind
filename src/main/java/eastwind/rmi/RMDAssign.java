package eastwind.rmi;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import eastwind.model.RMD;
import eastwind.support.EWUtils;

public class RMDAssign {

	protected Multimap<String, RMD> rmds = HashMultimap.create();
	
	public List<RMD> get(String path) {
		return Lists.newArrayList(rmds.get(path));
	}
	
	public void put(RMD rmd) {
		rmds.put(rmd.path, rmd);
	}
	
	public RMD assignFrom(RMD rmd) {
		Collection<RMD> subRMDs = rmds.get(rmd.path);
		for (RMD t : subRMDs) {
			if (t.argTypes.length == rmd.argTypes.length) {
				if (EWUtils.isAssignableTo(rmd, t)) {
					return t;
				}
			}
		}
		return null;
	}

}
