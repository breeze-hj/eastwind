package eastwind.model;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jan.huang on 2017/9/20.
 */
public class Shake {

    public String group;
    public String uuid;
    public Date startTime;
    public String version;
    public InetSocketAddress address;
    public ElectionState electionState;
    public List<InetSocketAddress> others;
    public transient Map<Object, Object> properties;
    
}
