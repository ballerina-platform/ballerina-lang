package org.wso2.ballerina.core.nativeimpl.connectors.http.websocket.server;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;

/**
 * {@code SendText} is the sendText action for WebSocket Connector
 */

@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "sendText",
        connectorName = WebSocketConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connector", type = TypeEnum.CONNECTOR),
                @Argument(name = "message", type = TypeEnum.MESSAGE),
                @Argument(name = "text", type = TypeEnum.STRING)
        })
@Component(
        name = "action.net.http.websocket.sendText",
        immediate = true,
        service = AbstractNativeAction.class)
public class SendText extends AbstractNativeAction {

    private static final  Logger LOGGER = LoggerFactory.getLogger(SendText.class);

    @Override
    public BValue execute(Context context) {
        BConnector connector = (BConnector) getArgument(context, 0);
        WebSocketConnector wsConnector = (WebSocketConnector) connector.value();
        String text = getArgument(context, 2).stringValue();
        LOGGER.info("The text is " + text);
        wsConnector.sendText(text);
        return null;
    }

}
