package eastwind.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jan.huang on 2017/10/18.
 */
public interface DelayedExecutor {

    Logger LOGGER = LoggerFactory.getLogger(DelayedExecutor.class);

    void delayExecute(DelayedTask delayedTask);

    void cancel(DelayedTask delayedTask);

    default void execute(DelayedTask delayedTask) {
        try {
            if (!delayedTask.isCancel()) {
                delayedTask.getConsumer().accept(this);
            }
        } catch (Exception e) {
            LOGGER.error("execute delayed task error", e);
        }
    }

}
