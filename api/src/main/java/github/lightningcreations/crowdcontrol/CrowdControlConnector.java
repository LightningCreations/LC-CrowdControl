package github.lightningcreations.crowdcontrol;


import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Represents the client side of a Crowd Control Connection.
 * Abstracted into an interface due to planned version changes.
 *
 * This class is usually not necessary to operate on directly.
 *  The higher level {@link CrowdControlHub} is recommended for use instead.
 */
public interface CrowdControlConnector extends AutoCloseable {
    /**
     * Polls the connector for a request. The future is completed when a request is available.
     */
    public CompletableFuture<CrowdControlRequest> pollRequest();

    /**
     * Sends a response to the connector's server.
     */
    public CompletableFuture<Void> sendResult(CrowdControlRequest.Response response);

    /**
     * Checks if the connector is connected.
     * Note that a bound connector may be disconnected from the server side, or spuriously.
     */
    public boolean isConnected();

    /**
     * Connects to the server. The returned future completes when the connection is made.
     */
    public CompletableFuture<Void> start();

    /**
     * Disconnects from the server, if connected. The returned future completes when the connection is closed.
     *
     * The result of disconnecting while not connected, is unspecified. In particular, it may complete immediately,
     *  or result in an exception (either in the future, or at the call).
     */
    public CompletableFuture<Void> disconnect();

    /**
     * Disconnects from the server, waiting until the connection is closed.
     */
    public default void close() throws ExecutionException, InterruptedException {
        disconnect().get();
    }

    /**
     * For dynamic connectors that can indicate to the server that a particular effect is available.<br/>
     * It should not be relied upon that an effect not sent will not be requested by the server,
     *  and unavailable effects should still result in the appropriate response.<br/>
     * It should also not be relied upon that exceptional completetion occurs if the request is unsupported. <br/>
     *
     * The effect is not required to be sent (and therefore the request completed) on any particular schedule.
     *  Completetion of this future should not be relied upon for operation.
     * @param effect The effect to indicate availability.
     * @return An future that, when completed, indicates that the connector has sent (if possible, and supported)
     * @implNote The default implementation simply returns a CompletableFuture that is already complete
     */
    public default CompletableFuture<Void> sendEffect(String effect){
        return CompletableFuture.allOf();
    }
}
