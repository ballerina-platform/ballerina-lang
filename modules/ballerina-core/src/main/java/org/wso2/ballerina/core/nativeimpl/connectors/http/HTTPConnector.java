package org.wso2.ballerina.core.nativeimpl.connectors.http;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

/**
 * Native HTTP Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.net.connectors.http",
        connectorName = "HTTPConnector",
        args = {
                @Argument(name = "serviceUri",
                          type = TypeEnum.STRING), @Argument(name = "timeout",
                                                             type = TypeEnum.INT)
        })
@Component(
        name = "ballerina.net.connectors.http",
        immediate = true,
        service = AbstractNativeConnector.class)
public class HTTPConnector extends AbstractNativeConnector implements ServiceFactory {

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

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {
        return new HTTPConnector();
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {

    }
}
