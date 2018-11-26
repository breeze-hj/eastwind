package eastwind;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface RmiTemplate {

	<T> CompletableFuture<T> execute(String path, Map<Object, Object> properties, Object... args);
}
