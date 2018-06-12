package org.ballerinalang.net.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.PortBindingEventListener;

import java.io.PrintStream;

/**
 * Server Connector Port Binding Listener.
 */
public class ServerConnectorPortBindingListener implements PortBindingEventListener {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectorPortBindingListener.class);
    private static final PrintStream console;

    public ServerConnectorPortBindingListener() {}

    public void onOpen(String serverConnectorId, boolean isHttps) {
        if (isHttps) {
            console.println("ballerina: started HTTPS/WSS endpoint " + serverConnectorId);
        } else {
            console.println("ballerina: started HTTP/WS endpoint " + serverConnectorId);
        }

    }

    public void onClose(String serverConnectorId, boolean isHttps) {
        if (isHttps) {
            console.println("ballerina: stopped HTTPS/WSS endpoint " + serverConnectorId);
        } else {
            console.println("ballerina: stopped HTTP/WS endpoint " + serverConnectorId);
        }

    }

    public void onError(Throwable throwable) {
        log.error("Error in http endpoint", throwable);
    }

    static {
        console = System.out;
    }
}
