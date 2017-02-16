package org.wso2.ballerina.lang.service;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.runtime.ServerConnectorMessageHandler;
import org.wso2.ballerina.core.runtime.dispatching.jms.Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

/**
 * Testing the JMS Service Dispatcher.
 */
public class JMSServiceTest {
    private Application application;

    @BeforeClass
    public void setup() {
        application = EnvironmentInitializer.setup("lang/service/serviceLevelVariable.bal");
    }

    @Test(description = "Test for exceptions when a jms message does not have a service id")
    public void testJMSServiceAvailabilityCheckWithoutJmsServiceId() {
        try {
            CarbonMessage cMsg = new TextCarbonMessage("test");
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_JMS);
            ServerConnectorMessageHandler.handleInbound(cMsg, null);
            Assert.fail("Exception is not thrown when a message is passed without jms service id");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(), "org.wso2.ballerina.core.exception.BallerinaException: "
                            + "error in ballerina program: service Id is not found in JMS Message",
                    "Exception message does not match when the message is passed without service id");
        }
    }

    @Test(description = "est for exceptions when a jms message contains non-existing service id")
    public void testJMSServiceAvailabilityWithWrongServiceId() {
        try {
            CarbonMessage cMsg = new TextCarbonMessage("test");
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_JMS);
            cMsg.setProperty(Constants.JMS_SERVICE_ID, "testabc");
            ServerConnectorMessageHandler.handleInbound(cMsg, null);
            Assert.fail("Exception is not thrown when a non-existing service is called");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(), "org.wso2.ballerina.core.exception.BallerinaException: "
                    + "error in ballerina program: no jms service is registered with the service id testabc",
                    "Exception message does not match when the message is passed is dispatched to non-existing "
                    + "service");
        }
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(application);
    }
}
