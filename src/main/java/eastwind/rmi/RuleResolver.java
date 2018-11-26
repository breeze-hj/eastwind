package eastwind.rmi;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eastwind.model.RMD;
import eastwind.service.ChannelService;
import eastwind.support.EWUtils;

public class RuleResolver {

	private RuleFactory ruleFactory;
	private Multimap<String, RMDRule> resolvedRules = HashMultimap.create();

	public RuleResolver(RuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	public Rule resolve(RMD rmd, int mod, List<ChannelService> services, boolean all) {
		RMDRule existed = getRMDRule(rmd);
		if (existed == null || existed.mod != mod || services.size() != existed.size || all != existed.all) {
			RMDRule rmdRule = new RMDRule();

			ChannelService newest = getNewestService(services);
			RMDAssign rmdAssign = newest.getRMDAssign();
			RMD remote = rmdAssign.assignFrom(rmd);
			String ruleName = remote.rule;
			Rule rule = null;
			if (existed != null && EWUtils.getSimpleName(existed.rule.getClass()).equals(ruleName)) {
				Object meta1 = existed.rmd.meta;
				Object meta2 = remote.meta;
				if (meta1 == meta2 || (meta1 != null && meta1.equals(meta2))) {
					rule = existed.rule;
				}
			}
			if (rule == null) {
				rule = ruleFactory.newRule(ruleName);
				rule.setMeta(remote.meta);
			}
			rule.prepare(services);

			rmdRule.rmd = remote;
			rmdRule.mod = mod;
			rmdRule.all = all;
			rmdRule.rule = rule;
			rmdRule.size = services.size();
			resolvedRules.put(rmdRule.rmd.path, rmdRule);
			return rmdRule.rule;
		} else {
			return existed.rule;
		}
	}

	private RMDRule getRMDRule(RMD rmd) {
		Collection<RMDRule> rules = resolvedRules.get(rmd.path);
		for (RMDRule rr : rules) {
			if (EWUtils.isAssignableTo(rmd, rr.rmd)) {
				return rr;
			}
		}
		return null;
	}

	private ChannelService getNewestService(List<ChannelService> services) {
		ChannelService service = null;
		for (ChannelService t : services) {
			if (service == null || t.getStartTime().after(service.getStartTime())) {
				service = t;
			}
		}
		return service;
	}

	static class RMDRule {
		RMD rmd;
		int mod;
		boolean all;
		Rule rule;
		int size;
	}
}
