package github.lightningcreations.crowdcontrol.tcpv1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import github.lightningcreations.crowdcontrol.CrowdControlConnector;
import github.lightningcreations.crowdcontrol.CrowdControlRequest;
import github.lightningcreations.crowdcontrol.CrowdControlRequestType;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TCPv1Connector implements CrowdControlConnector {
    private Map<String,CompletableFuture<Void>> effectsToSend = new TreeMap<>();
    private Socket sock;
    private Thread t;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private class Pair{
        CrowdControlRequest.Response res;
        CompletableFuture<Void> v;

        public Pair(CrowdControlRequest.Response res, CompletableFuture<Void> v) {
            this.res = res;
            this.v = v;
        }
    }

    private Deque<CrowdControlRequest> reqQueue = new ConcurrentLinkedDeque<>();
    private Deque<CompletableFuture<CrowdControlRequest>> pollQueue = new ConcurrentLinkedDeque<>();
    private final List<Pair> resList = Collections.synchronizedList(new LinkedList<>());

    private static final Gson gson = new GsonBuilder().create();

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
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
            Writer w = new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8);
            while(!shutdown.get()){
                synchronized(resList){
                    for(Pair p:resList){
                        w.write(gson.toJson(p.res));
                        w.append((char) 0);
                    }
                }
                StringBuilder builder = new StringBuilder();
                char c;
                while((c = (char)r.read())!=0)
                    builder.append(c);
                CrowdControlRequest req = gson.fromJson(builder.toString(),CrowdControlRequest.class);
                if(req.getCode().equals("__DISCOVER")) {
                    if (req.getType() == CrowdControlRequestType.Test) {
                        String s = String.join(", ", effectsToSend.keySet());
                        effectsToSend.values().forEach(f -> f.complete(null));
                        w.write(gson.toJson(req.success(s)));
                    }
                }
                else if(pollQueue.isEmpty())
                    reqQueue.add(req);
                else
                    pollQueue.removeFirst().complete(req);
            }
            sock.close();
        } catch (IOException ignored) {}
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
        CompletableFuture<Void> ret = new CompletableFuture<>();
        Pair p = new Pair(response,ret);
        resList.add(p);
        return ret;
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
