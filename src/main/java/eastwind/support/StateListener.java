package eastwind.support;

/**
 * Created by jan.huang on 2018/4/11.
 */
public interface StateListener<T extends State> {

    void stateChanged(StateFul<T> stateFul, Result<?> result);

}
