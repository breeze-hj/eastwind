package eastwind.service;

import eastwind.support.State;

/**
 * Created by jan.huang on 2018/5/3.
 */
public class ServiceGroupState extends State {

    public static final ServiceGroupState INITIAL = new ServiceGroupState("INITIAL");

    public static final ServiceGroupState SERVICEABLE = new ServiceGroupState("SERVICEABLE");

    public static final ServiceGroupState UNSERVICEABLE = new ServiceGroupState("UNSERVICEABLE");

    public ServiceGroupState(String state) {
        super(state);
    }

}
