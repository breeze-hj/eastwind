package eastwind.codec;

import java.io.InputStream;

public interface Serializer {
	
	void serialize(Object object);
	
	Object deserialize(InputStream inputStream);
}
