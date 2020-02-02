package github.lightningcreations.crowdcontrol;

import github.lightningcreations.crowdcontrol.CrowdControlRequest.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;


/**
 * Higher level wrapper around {@link CrowdControlConnector}.
 *  This class provides methods to register handlers for various effects
 */
public final class CrowdControlHub implements AutoCloseable {
    private CrowdControlConnector connector;
    private Map<String,Function<CrowdControlRequest, CompletableFuture<Response>>> effects;
    //stable
    private boolean locked;

    private static final Function<CrowdControlRequest,CompletableFuture<Response>> DEFAULT = r->CompletableFuture.completedFuture(r.unavailable(String.format("Effect %s is not registered yet",r.getCode())));

    /**
     * Wraps the given CrowdControlConnector in this class.
     *  This method is usually not necessary, and only required if a particular connection method is either required or desired.
     */
    public static CrowdControlHub connectTo(CrowdControlConnector connector){
        return new CrowdControlHub(connector);
    }

    /**
     * Connects to the Default CrowdControlConnector.
     * If there is exactly one class that matches the service {@link CrowdControlConnector},
     *  a new instance of that class is created and a new CrowdControlHub wraps that instance.
     * If there is more than one class, one of those classes are chosen. It is unspecified which one is chosen (this choice cannot be relied upon).
     * If there are no classes that match that service, a {@link java.util.NoSuchElementException} is thrown.
     */
    public static CrowdControlHub connectToDefault(){
        ServiceLoader<CrowdControlConnector> connector = ServiceLoader.load(CrowdControlConnector.class);
        Iterator<CrowdControlConnector> iter = connector.iterator();
        return connectTo(iter.next());
    }

    private CrowdControlHub(CrowdControlConnector connector){
        this.connector = connector;
        this.effects = new HashMap<>();
    }

    /**
     * Disconnects the wrapped connector. The resulting future has the same completion requirements and notes as {@link CrowdControlConnector#disconnect()}
     */
    public synchronized CompletableFuture<Void> disconnect(){
        return connector.disconnect();
    }

    /**
     * Closes this connector, as if by a call to {@link #disconnect()}, and awaiting the completion of the result.
     */
    public void close() throws ExecutionException, InterruptedException {
        disconnect().get();
    }

    /**
     * If the hub has not already been started, registers a new effect with the given name and handler.
     * If the effect with the name is already registered, an {@link IllegalArgumentException} is thrown.
     * If the hub is locked (IE. it has previously been started), an {@link IllegalStateException} is thrown.
     *
     * The resulting future has the same completion requirements as {@link CrowdControlConnector#sendEffect(String)}.
     */
    public synchronized CompletableFuture<Void> register(String effect, Function<CrowdControlRequest, CompletableFuture<Response>> on){
        if(locked)
            throw new IllegalStateException("The hub has already been started, new effects cannot be registered");
        if(effects.putIfAbsent(effect,on)!=null)
            throw new IllegalArgumentException(String.format("Effect %s is already registered",effect));
        return connector.sendEffect(effect);
    }

    /**
     * Starts the connector. The resulting future completes when the connection is closed (not when it is started,
     *  as with {@link CrowdControlConnector#start()}
     */
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
