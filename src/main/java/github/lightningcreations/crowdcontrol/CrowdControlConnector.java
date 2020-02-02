package github.lightningcreations.crowdcontrol;


import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Represents the client side of a Crowd Control Connection.
 * Abstracted into an interface due to planned version changes.
 */
public interface CrowdControlConnector extends AutoCloseable {
    public CompletableFuture<CrowdControlRequest> pollRequest();
    public CompletableFuture<Void> sendResult(CrowdControlRequest.Response response);

    public boolean isConnected();

    public CompletableFuture<Void> start();

    public CompletableFuture<Void> disconnect();

    public default void close() throws ExecutionException, InterruptedException {
        disconnect().get();
    }

    /**
     * For dynamic connectors that can indicate to the server that a particular effect is available.<br/>
     * It should not be relied upon that an effect not sent will not be requested by the server,
     *  and unavailable effects should still result in the appropriate response.<br/>
     * It should also not be relied upon that exceptional completetion occurs if the request is unsupported.
     * @param effect The effect to indicate availability.
     * @return An future that, when completed, indicates that the connector has sent (if possible, and supported)
     * @implNote The default implementation simply returns a CompletableFuture that is already complete
     */
    public default CompletableFuture<Void> sendEffect(String effect){
        return CompletableFuture.allOf();
    }
}
