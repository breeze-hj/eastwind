package eastwind.rmi;

import java.util.List;

public class ConsistentHashMeta {

	public int i;
	public String clsName;
	public List<String> propertys;

	public ConsistentHashMeta() {
	}

	public ConsistentHashMeta(int i, String clsName, List<String> propertys) {
		this.i = i;
		this.clsName = clsName;
		this.propertys = propertys;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clsName == null) ? 0 : clsName.hashCode());
		result = prime * result + i;
		result = prime * result + ((propertys == null) ? 0 : propertys.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConsistentHashMeta other = (ConsistentHashMeta) obj;
		if (clsName == null) {
			if (other.clsName != null)
				return false;
		} else if (!clsName.equals(other.clsName))
			return false;
		if (i != other.i)
			return false;
		if (propertys == null) {
			if (other.propertys != null)
				return false;
		} else if (!propertys.equals(other.propertys))
			return false;
		return true;
	}
	
}
