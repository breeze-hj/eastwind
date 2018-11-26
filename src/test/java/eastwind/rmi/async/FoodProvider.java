package eastwind.rmi.async;

import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import eastwind.InvocationContext;

public class FoodProvider implements FoodFeign {

	@Override
	public String cook(String food) {
		InvocationContext<String> context = InvocationContext.getContext();
		context.async();
		ForkJoinPool.commonPool().execute(()->{
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
			}
			StringBuilder result = new StringBuilder();
			result.append(food).append(" with");
			for (Entry<Object, Object> en : context.getInvocationPropertys().entrySet()) {
				result.append(" ").append(en.getKey());
				result.append("-").append(en.getValue());
			}
			context.complete(result.toString());
		});
		return null;
	}

}
