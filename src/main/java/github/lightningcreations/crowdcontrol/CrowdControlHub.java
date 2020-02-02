package github.lightningcreations.crowdcontrol;

import github.lightningcreations.crowdcontrol.CrowdControlRequest.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;


public final class CrowdControlHub implements AutoCloseable {
    private CrowdControlConnector connector;
    private Map<String,Function<CrowdControlRequest, CompletableFuture<Response>>> effects;
    //stable
    private boolean locked;

    private static final Function<CrowdControlRequest,CompletableFuture<Response>> DEFAULT = r->CompletableFuture.completedFuture(r.unavailable(String.format("Effect %s is not registered yet",r.getCode())));

    public static CrowdControlHub connectTo(CrowdControlConnector connector){
        return new CrowdControlHub(connector);
    }

    public static CrowdControlHub connectToDefault(){
        ServiceLoader<CrowdControlConnector> connector = ServiceLoader.load(CrowdControlConnector.class);
        Iterator<CrowdControlConnector> iter = connector.iterator();
        return connectTo(iter.next());
    }

    private CrowdControlHub(CrowdControlConnector connector){
        this.connector = connector;
        this.effects = new HashMap<>();
    }

    public synchronized CompletableFuture<Void> disconnect(){
        return connector.disconnect();
    }

    public void close() throws ExecutionException, InterruptedException {
        disconnect().get();
    }

    public synchronized CompletableFuture<Void> register(String effect, Function<CrowdControlRequest, CompletableFuture<Response>> on){
        if(locked)
            throw new IllegalStateException("The hub has already been started, new effects cannot be registered");
        if(effects.putIfAbsent(effect,on)!=null)
            throw new IllegalArgumentException(String.format("Effect %s is already registered",effect));
        return connector.sendEffect(effect);
    }

    private void run(){

    }

    public synchronized CompletableFuture<Void> start(){
        if(locked)
            throw new IllegalStateException("Hub has already started");
        this.locked = true;
        CompletableFuture<Void> result = new CompletableFuture<>();

        return connector.start().thenCompose(v->{
            CompletableFuture.runAsync(()->{
                try {
                    while (connector.isConnected()) {
                        connector.pollRequest().thenCompose(r -> effects.getOrDefault(r.getCode(), DEFAULT).apply(r)).thenCompose(r -> connector.sendResult(r)).join();
                    }
                    result.complete(null);
                }catch(Throwable t){
                    result.completeExceptionally(t);
                }
            });
            return result;
        });
    }
}
