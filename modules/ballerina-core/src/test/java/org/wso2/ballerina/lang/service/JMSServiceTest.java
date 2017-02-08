package org.wso2.ballerina.lang.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.EnvironmentInitializer;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Application;
import org.wso2.ballerina.core.nativeimpl.connectors.jms.utils.JMSConstants;
import org.wso2.ballerina.core.runtime.ServerConnectorMessageHandler;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

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

    @Test(description = "Test for jms service availability check without service id",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = ".* service Id is not found in JMS Message.*")
    public void testJMSServiceAvailabilityCheckWithoutJmsServiceId() {
        CarbonMessage cMsg = new TextCarbonMessage("test");
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, JMSConstants.PROTOCOL_JMS);
        ServerConnectorMessageHandler.handleInbound(cMsg, null);
    }

    @Test(description = "Test for jms service availability check with a non-existing service id",
            expectedExceptions = { BallerinaException.class },
            expectedExceptionsMessageRegExp = ".* no jms service is registered with the service id testabc.*")
    public void testJMSServiceAvailabilityWithWrongServiceId() {
        CarbonMessage cMsg = new TextCarbonMessage("test");
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, JMSConstants.PROTOCOL_JMS);
        cMsg.setProperty(JMSConstants.JMS_SERVICE_ID, "testabc");
        ServerConnectorMessageHandler.handleInbound(cMsg, null);
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
