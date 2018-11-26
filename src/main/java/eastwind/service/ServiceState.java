package eastwind.service;

import eastwind.support.State;

/**
 * Created by jan.huang on 2018/5/3.
 */
public class ServiceState extends State {

    public static final ServiceState INITIAL = new ServiceState("INITIAL");

    public static final ServiceState ONLINE = new ServiceState("ONLINE");

    public static final ServiceState SUSPEND = new ServiceState("SUSPEND");

    public static final ServiceState OFFLINE = new ServiceState("OFFLINE");

    public ServiceState(String state) {
        super(state);
    }

}
