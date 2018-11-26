package eastwind.rmi;

import eastwind.support.EWUtils;

public class RuleFactory {

	public Rule newRule(String rule) {
		if (rule.equals(EWUtils.getSimpleName(RandomRule.class))) {
			return new RandomRule();
		} else if (rule.equals(EWUtils.getSimpleName(ConsistentHashRule.class))) {
			return new ConsistentHashRule();
		}
		return null;
	}
	
}
