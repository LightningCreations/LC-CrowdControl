package github.lightningcreations.crowdcontrol.test;


import github.lightningcreations.crowdcontrol.CrowdControlHub;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class TestGame {
    @Test
    public void runGameTest() throws IOException {
        CrowdControlHub hub = CrowdControlHub.connectToDefault();
        hub.register("test1",r-> CompletableFuture.runAsync(()->System.out.println("test1 firing"))
                .thenCompose(v->CompletableFuture.supplyAsync(()->r.success("test1"))));
        hub.register("failure_test1",r-> CompletableFuture.runAsync(()->System.out.println("test1 firing"))
                .thenCompose(v->CompletableFuture.supplyAsync(()->r.failure("failure_test1"))));
        CompletableFuture<Void> start = hub.start();

        //noinspection
        while(!start.isDone()&&System.in.available()==0);//Yes this is intentional IntelliJ
        if(start.isCompletedExceptionally())
            System.out.println("Failed to connect to the Connector Server, is the SDK Running and is the github.lightningcreations.crowdcontrol.test script loaded?");
    }
}
