package org.wso2.ballerina.core.nativeimpl.connectors.http;

import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.NativeConnector;

/**
 * Native HTTP Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.net.connectors.http",
        connectorName = "HTTPConnector",
        args = {
                @Argument(name = "serviceUri", type = TypeEnum.STRING),
                @Argument(name = "timeout", type = TypeEnum.INT)
        })
public class HTTPConnector extends NativeConnector {

    // HTTP connector shared attributes

    private String serviceUri;
    private int timeout;

    @Override
    public boolean init(BValueRef[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 2) {
            serviceUri = bValueRefs[0].getString();
            timeout = bValueRefs[1].getInt();
        }
        return true;
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public String getPackageName() {
        return null;
    }
}
