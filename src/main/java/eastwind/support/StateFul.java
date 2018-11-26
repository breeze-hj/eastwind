package eastwind.support;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jan.huang on 2018/4/11.
 */
public class StateFul<T extends State> {

	protected T state;
	protected Map<T, StateListener<T>> stateListeners = new HashMap<>();

	protected void changeState(T newState, Result<?> result) {
		this.state = newState;
		StateListener<T> stateListener = stateListeners.get(newState);
		if (stateListener != null) {
			stateListener.stateChanged(this, result);
		}
	}

	protected void setStateListener(T state, StateListener<T> stateListener) {
		stateListeners.put(state, stateListener);
	}

	public T getState() {
		return state;
	}
}
