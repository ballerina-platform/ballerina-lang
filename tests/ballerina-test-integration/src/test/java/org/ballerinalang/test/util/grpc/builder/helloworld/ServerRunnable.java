package org.ballerinalang.test.util.grpc.builder.helloworld;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.ServerInstance;

import java.io.File;

public class ServerRunnable implements Runnable {
    private ServerInstance ballerinaServer;
    
    public ServerRunnable(ServerInstance ballerinaServer) {
        this.ballerinaServer = ballerinaServer;
    }
    
    @Override
    public void run() {
        String balFile = new File("src/test/resources/grpcService/helloWorld-server-connector.bal")
                .getAbsolutePath();
        try {
            ballerinaServer.startBallerinaServer(balFile);
        } catch (BallerinaTestException e) {
            // TODO: 3/5/18  
        }
    }
    
    public  void stop(){
        try {
            ballerinaServer.stopServer();
        } catch (BallerinaTestException e) {
            // TODO: 3/5/18
        }
    }
}
