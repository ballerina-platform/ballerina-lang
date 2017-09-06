package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.BallerinaServerConnector;
import org.ballerinalang.connector.api.Registry;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpService;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;

import java.io.PrintStream;
import java.util.List;

/**
 * Created by rajith on 9/4/17.
 */
public class WebSocketServerConnector implements BallerinaServerConnector {
    @Override
    public String getProtocolPackage() {
        return Constants.WEBSOCKET_PACKAGE_PATH;
    }

    @Override
    public void serviceRegistered(Service service) throws BallerinaConnectorException {
        HttpService httpService = new HttpService(service);
        WebSocketServicesRegistry.getInstance().registerServiceEndpoint(httpService);
    }

    @Override
    public void serviceUnregistered(Service service) throws BallerinaConnectorException {
        HttpService httpService = new HttpService(service);
        WebSocketServicesRegistry.getInstance().unregisterService(httpService);
    }

    @Override
    public void complete() throws BallerinaConnectorException {
        try {
            // Starting up HTTP Server connectors
            //TODO move this to a common location and use in both http and ws server connectors
            PrintStream outStream = System.out;
            List<ServerConnector> startedHTTPConnectors = HttpConnectionManager.getInstance()
                    .startPendingHTTPConnectors();
            startedHTTPConnectors.forEach(serverConnector -> outStream.println("ballerina: started " +
                    "server connector " + serverConnector));
        } catch (ServerConnectorException e) {
            throw new BallerinaConnectorException(e);
        }
    }

    @Override
    public Registry getRegistry() {
        return null;
    }
}
