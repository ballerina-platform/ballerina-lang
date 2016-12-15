package org.wso2.ballerina.core.nativeimpl.connectors.http;


import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.NativeConnector;

/**
 * Native HTTP Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.net.connectors.http",
        connectorName = "HTTPConnector",
        args = {@Argument(name = "uri", type = TypeEnum.STRING),
                @Argument(name = "timeout", type = TypeEnum.INT)}
)
public class HTTPConnector extends NativeConnector {

// HTTP connector shared attributes

    @Override
    public boolean init() {

        return false;
    }


}
