package org.ballerinalang.nativeimpl.actions.http;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.osgi.service.component.annotations.Component;
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
        args = {@Argument(name = "c", type = TypeEnum.CONNECTOR)
        },
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeEnum.STRING)
        }
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
                                                       value = "The init action implementation for HTTP connector.") })
@Component(
        name = "action.net.http.init",
        immediate = true,
        service = AbstractNativeAction.class)
public class Init extends AbstractHTTPAction {

    @Override
    public BValue execute(Context context) {
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
        return null;
    }

    @Override
    public boolean isNonBlockingAction() {
        return false;
    }
}
