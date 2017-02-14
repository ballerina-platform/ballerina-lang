package org.wso2.ballerina.lang.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.runtime.ServerConnectorMessageHandler;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import javax.jms.ConnectionFactory;

import static org.testng.Assert.fail;

/**
 * Testing the JMS Service Dispatcher.
 */
public class JMSServiceTest {
    private static final String ACTIVEMQ_PROVIDER_URL = "vm://localhost?broker.persistent=false";
    private Application application;

    @BeforeClass
    public void setup() {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_PROVIDER_URL);
        application = EnvironmentInitializer.setup("lang/service/jmsService.bal");
    }

    @Test(description = "Test for exceptions when a jms message does not have a service id")
    public void testJMSServiceAvailabilityCheckWithoutJmsServiceId() {
        try {
            CarbonMessage cMsg = new TextCarbonMessage("test");
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, JMSConstants.PROTOCOL_JMS);
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
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, JMSConstants.PROTOCOL_JMS);
            cMsg.setProperty(JMSConstants.JMS_SERVICE_ID, "testabc");
            ServerConnectorMessageHandler.handleInbound(cMsg, null);
            Assert.fail("Exception is not thrown when a non-existing service is called");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(), "org.wso2.ballerina.core.exception.BallerinaException: "
                    + "error in ballerina program: no jms service is registered with the service id testabc",
                    "Exception message does not match when the message is passed is dispatched to non-existing "
                    + "service");
        }
    }

    @Test(description = "Test for exception when the jms message is for a service without any resources")
    public void testJMSResourceAvailability() {
        try {
            CarbonMessage cMsg = new TextCarbonMessage("test");
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, JMSConstants.PROTOCOL_JMS);
            cMsg.setProperty(JMSConstants.JMS_SERVICE_ID, "jmsServiceWithoutResource");
            ServerConnectorMessageHandler.handleInbound(cMsg, null);
            Assert.fail("Exception is not thrown when there is no jms resource to handle the jms message");
        } catch (BallerinaException ex) {
            Assert.assertEquals(ex.getCause().getMessage(), "org.wso2.ballerina.core.exception.BallerinaException: "
                    + "error in ballerina program: no resource found to handle the request to Service : "
                    + "jmsServiceWithoutResource : Resource to handle the jms message is not found in jms service "
                    + "jmsServiceWithoutResource", "Exception message does not match when the message is passed is "
                    + "dispatched to a service without a resource");
        }
    }

    @Test(description = "Test for resource availability check when there are all the required information")
    public void testJMSResourceWithAllProperties() {
        try {
            CarbonMessage cMsg = new TextCarbonMessage("test");
            cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, JMSConstants.PROTOCOL_JMS);
            cMsg.setProperty(JMSConstants.JMS_SERVICE_ID, "jmsService");
            cMsg.setProperty(JMSConstants.JMS_MESSAGE_TYPE, JMSConstants.GENERIC_MESSAGE_TYPE);
            ServerConnectorMessageHandler.handleInbound(cMsg, null);
        } catch (BallerinaException e) {
            fail("Exception is thrown even when all the information are provided for resource invoking");
        }
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(application);
    }
}
