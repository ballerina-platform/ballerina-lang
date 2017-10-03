package org.ballerinalang.net.http.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.net.http.Constants;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;

import java.util.ServiceLoader;

/**
 * {@code Init} is the Init action implementation of the HTTP Connector.
 *
 * @since 0.9
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "<init>",
        connectorName = Constants.CONNECTOR_NAME,
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR)},
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeKind.STRING),
                @Argument(name = "options", type = TypeKind.STRUCT, structType = "Options",
                          structPackage = "ballerina.net.http")
        }
)
public class Init extends AbstractHTTPAction {

    @Override
    public ConnectorFuture execute(Context context) {
        if (BallerinaConnectorManager.getInstance().
                getClientConnector(Constants.PROTOCOL_HTTP) == null) {
            CarbonMessageProcessor carbonMessageProcessor = BallerinaConnectorManager.getInstance()
                    .getMessageProcessor();
            ServiceLoader<ClientConnector> clientConnectorLoader = ServiceLoader.load(ClientConnector.class);
            clientConnectorLoader.forEach((clientConnector) -> {
                clientConnector.setMessageProcessor(carbonMessageProcessor);
                BallerinaConnectorManager.getInstance().registerClientConnector(clientConnector);
            });
        }
        ClientConnectorFuture future = new ClientConnectorFuture();
        future.notifySuccess();
        return future;
    }

    @Override
    public boolean isNonBlockingAction() {
        return false;
    }
}
