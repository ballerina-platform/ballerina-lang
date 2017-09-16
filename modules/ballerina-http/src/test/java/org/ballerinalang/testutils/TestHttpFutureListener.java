package org.ballerinalang.testutils;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.CorsHeaderGenerator;
import org.ballerinalang.net.http.session.Session;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.concurrent.Semaphore;

/**
 * Test future implementation for service tests
 */
public class TestHttpFutureListener implements ConnectorFutureListener {

    private volatile Semaphore executionWaitSem;
    private HTTPCarbonMessage requestMessage;
    private BValue request;

    private HTTPCarbonMessage responseMsg;

    public TestHttpFutureListener(HTTPCarbonMessage requestMessage, BValue request) {
        executionWaitSem = new Semaphore(0);
        this.requestMessage = requestMessage;
        this.request = request;
    }

    @Override
    public void notifySuccess() {

    }

    @Override
    public void notifyReply(BValue response) {
        HTTPCarbonMessage responseMessage = (HTTPCarbonMessage) ((BStruct) response)
                .getNativeData(org.ballerinalang.net.http.Constants.TRANSPORT_MESSAGE);
        Session session = (Session) ((BStruct) request).getNativeData(Constants.HTTP_SESSION);
        if (session != null) {
            session.generateSessionHeader(responseMessage);
        }
        //Process CORS if exists.
        if (requestMessage.getHeader("Origin") != null) {
            CorsHeaderGenerator.process(requestMessage, responseMessage, true);
        }

        //Process CORS if exists.
        if (requestMessage.getHeader("Origin") != null) {
            CorsHeaderGenerator.process(requestMessage, responseMessage, true);
        }
        this.responseMsg = responseMessage;
        this.executionWaitSem.release();
    }

    @Override
    public void notifyFailure(BallerinaConnectorException ex) {

    }

    public void sync() {
        try {
            executionWaitSem.acquire();
        } catch (InterruptedException e) {
            //ignore
        }
    }

    public HTTPCarbonMessage getResponseMsg() {
        return responseMsg;
    }
}
