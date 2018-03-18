package org.ballerinalang.net.http.websub;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.net.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;


/**
 * HTTP Connection Listener for Ballerina WebSub services.
 */
public class BallerinaWebSubConnectionListener extends BallerinaHTTPConnectorListener {

    private WebSubServicesRegistry webSubServicesRegistry;

    public BallerinaWebSubConnectionListener(WebSubServicesRegistry webSubServicesRegistry) {
        super(webSubServicesRegistry.getHttpServicesRegistry());
        this.webSubServicesRegistry = webSubServicesRegistry;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
        try {
            HttpResource httpResource = WebSubDispatcher.findResource(webSubServicesRegistry, httpCarbonMessage);
            extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
        } catch (BallerinaException ex) {
            HttpUtil.handleFailure(httpCarbonMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
        }
    }

}
