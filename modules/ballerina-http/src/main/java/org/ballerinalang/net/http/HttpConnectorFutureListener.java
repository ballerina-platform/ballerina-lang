package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajith on 9/7/17.
 */
public class HttpConnectorFutureListener implements ConnectorFutureListener {
    private static final Logger log = LoggerFactory.getLogger(HttpConnectorFutureListener.class);
    CarbonCallback carbonCallback;

    public HttpConnectorFutureListener(CarbonCallback carbonCallback) {
        this.carbonCallback = carbonCallback;
    }

    @Override
    public void notifySuccess(BValue response) {
        //TODO
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {
        //TODO
    }

    @Override
    public void notifySuccess(CarbonMessage carbonMessage) {

    }

    @Override
    public void notifyFailure(Exception ex, CarbonMessage carbonMessage) {
        Object carbonStatusCode = carbonMessage.getProperty(Constants.HTTP_STATUS_CODE);
        int statusCode = (carbonStatusCode == null) ? 500 : Integer.parseInt(carbonStatusCode.toString());
        String errorMsg = ex.getMessage();
        log.error(errorMsg);
        ErrorHandlerUtils.printError(ex);
        if (statusCode == 404) {
            // TODO Temporary solution. Fix Me!!!
            carbonCallback.done(createErrorMessage(errorMsg, statusCode));
        } else {
            // TODO If you put just "", then we got a NPE. Need to find why
            carbonCallback.done(createErrorMessage("  ", statusCode));
        }
    }

    private CarbonMessage createErrorMessage(String payload, int statusCode) {

        DefaultCarbonMessage response = new DefaultCarbonMessage();

        response.setStringMessageBody(payload);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        // TODO: Set following according to the request
        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONNECTION,
                org.wso2.carbon.transport.http.netty.common.Constants.CONNECTION_KEEP_ALIVE);
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_TYPE,
                org.wso2.carbon.transport.http.netty.common.Constants.TEXT_PLAIN);
        transportHeaders.put(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_LENGTH,
                (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        return response;

    }
}
