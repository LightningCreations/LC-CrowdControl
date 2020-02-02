package github.lightningcreations.crowdcontrol.tcpv1;

import github.lightningcreations.crowdcontrol.CrowdControlConnector;
import github.lightningcreations.crowdcontrol.CrowdControlRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPv1Connector implements CrowdControlConnector {
    private Map<String,CompletableFuture<Void>> effectsToSend = new TreeMap<>();
    private Socket sock;
    private Thread t;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private Deque<CrowdControlRequest> reqQueue = new ConcurrentLinkedDeque<>();
    private Deque<CompletableFuture<CrowdControlRequest>> pollQueue = new ConcurrentLinkedDeque<>();

    private static final int port = (int)(long)Long.getLong("crowdcontrol.connector.tcp.port",58430);
    private static final InetAddress toConnect;
    static{
        String addr = System.getProperty("crowdcontrol.connector.tcp.address");
        try {
            if(addr.isEmpty())
                toConnect = InetAddress.getLocalHost();
            else
                toConnect = InetAddress.getByName(addr);
        } catch (UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private void run(){
        // TODO Thonk
    }

    @Override
    public CompletableFuture<CrowdControlRequest> pollRequest() {
        if(reqQueue.isEmpty()){
            CompletableFuture<CrowdControlRequest> ret = new CompletableFuture<>();
            pollQueue.add(ret);
            return ret;
        }
        return CompletableFuture.completedFuture(reqQueue.removeFirst());
    }

    @Override
    public CompletableFuture<Void> sendResult(CrowdControlRequest.Response response) {
        return null;
    }

    @Override
    public boolean isConnected() {
        return sock != null&&sock.isConnected();
    }

    @Override
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> ret = new CompletableFuture<>();
        CompletableFuture.runAsync(()->{
            sock = new Socket();
            try {
                sock.connect(new InetSocketAddress(toConnect,port));
                t = new Thread(this::run);
                t.start();
                ret.complete(null);
            } catch (IOException e) {
                e.printStackTrace();
                ret.completeExceptionally(e);
            }
        });
        return ret;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        return CompletableFuture.runAsync(()-> {
            shutdown.set(true);
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> sendEffect(String effect) {
        CompletableFuture<Void> ret = new CompletableFuture<>();
        if (sock!=null)
            ret.completeExceptionally(new IllegalStateException(""));
        else
            effectsToSend.put(effect,ret);
        return ret;
    }
}
