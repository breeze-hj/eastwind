package eastwind.channel;

import java.util.concurrent.CompletableFuture;

import eastwind.support.Result;

/**
 * Created by jan.huang on 2018/4/17.
 */
public class MasterChannelStateListener extends ChannelStateListener<MasterChannel> {

    private CompletableFuture<Void> openFuture = new CompletableFuture<>();

    @Override
    public void onActive(MasterChannel channel) {
        openFuture.complete(null);
    }

    @Override
    public void onInactive(MasterChannel channel, Result<?> result) {
        openFuture.completeExceptionally(result.getTh());
    }

    public CompletableFuture<Void> getOpenFuture() {
        return openFuture;
    }
}
