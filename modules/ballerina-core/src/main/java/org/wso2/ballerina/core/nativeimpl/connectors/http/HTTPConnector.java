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
        args = { @Argument(name = "serviceUri", type = TypeEnum.STRING),
                 @Argument(name = "timeout", type = TypeEnum.INT)
        })
public class HTTPConnector extends NativeConnector {

    // HTTP connector shared attributes

    private String serviceUri;
    private int timeout;

    public HTTPConnector(String serviceUri, int timeout) {
        this.serviceUri = serviceUri;
        this.timeout = timeout;
    }

    @Override
    public boolean init() {

        return false;
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public int getTimeout() {
        return timeout;
    }
}
