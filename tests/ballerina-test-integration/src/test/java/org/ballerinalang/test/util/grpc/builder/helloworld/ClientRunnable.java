package org.ballerinalang.test.util.grpc.builder.helloworld;

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;

import java.io.File;

public class ClientRunnable implements Runnable {
    
    private ServerInstance ballerinaClient;
    
    public ClientRunnable(ServerInstance ballerinaClient) {
        this.ballerinaClient = ballerinaClient;
    }
    
    @Override
    public void run() {
        String[] clientArgs = {new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "grpcService" + File.separator + "helloWorldClient.bal").getAbsolutePath()};
        try {
            ballerinaClient.runMain(clientArgs);
        } catch (BallerinaTestException e) {
            // TODO: 3/5/18  
        }
    }
    
    public  void stop(){
        try {
            ballerinaClient.stopServer();
        } catch (BallerinaTestException e) {
            // TODO: 3/5/18
        }
    }
}
