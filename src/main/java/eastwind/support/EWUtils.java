package eastwind.support;

import org.apache.commons.lang3.StringUtils;

import eastwind.annotation.Feign;
import eastwind.model.RMD;

public class EWUtils {

	public static boolean isAssignableTo(RMD local, RMD remote) {
		if (local.argTypes.length != remote.argTypes.length) {
			return false;
		}
		boolean assignTo = true;
		for (int i = 0; i < remote.argTypes.length; i++) {
			Class<?> cls1 = local.argTypes[i];
			if (cls1 == null) {
				continue;
			}
			Class<?> cls2 = remote.argTypes[i];
			if (cls1.equals(cls2) || cls2.isAssignableFrom(cls1)) {
				continue;
			} else {
				assignTo = false;
				break;
			}
		}
		return assignTo;
	}

	public static String getFeignName(Class<?> cls) {
		Feign feign = cls.getAnnotation(Feign.class);
		if (feign == null) {
			Class<?>[] is = cls.getInterfaces();
			if (is.length == 0) {
				return null;
			}
			feign = is[0].getAnnotation(Feign.class);
			if (feign == null) {
				return null;
			}
		}
		String name = feign.name();
		if (StringUtils.isBlank(name)) {
			return null;
		}
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		return name;
	}

	public static String getSimpleName(Class<?> cls) {
		String simpleName = cls.getSimpleName();
		if (simpleName.length() == 1) {
			return simpleName.substring(0, 1).toLowerCase();
		}
		return simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
	}

	public static Object returnNull(Class<?> type) {
		// boolean, char, byte, short, int, long, float, double
		if (type.isPrimitive()) {
			if (type == boolean.class) {
				return false;
			}

			if (type == int.class) {
				return Integer.MIN_VALUE;
			}
			if (type == long.class) {
				return Long.MIN_VALUE;
			}

			if (type == byte.class) {
				return Byte.MIN_VALUE;
			}
			if (type == short.class) {
				return Short.MIN_VALUE;
			}
			if (type == float.class) {
				return Float.MIN_VALUE;
			}
			if (type == double.class) {
				return Double.MIN_VALUE;
			}

			if (type == char.class) {
				return (char) 0xffff;
			}
		} else {
			return null;
		}
		return null;
	}

}