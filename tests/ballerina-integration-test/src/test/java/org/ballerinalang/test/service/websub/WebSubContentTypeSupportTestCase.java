package org.ballerinalang.test.service.websub;

import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_JSON;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_STRING;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.CONTENT_TYPE_XML;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_INTERNAL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.HUB_MODE_REMOTE;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.PUBLISHER_NOTIFY_URL;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.requestUpdate;

/**
 * This class tests support for WebSub notifications of different content types.
 * Tests:
 * 1. Content delivery to multiple subscribers
 * 2. Content delivery for subscriptions made with and without specifying a secret
 * 3. Content delivery for JSON, string and XML types.
 */
@Test(groups = "websub-test")
public class WebSubContentTypeSupportTestCase extends WebSubBaseTest {
    private BServerInstance webSubSubscriber;

    private boolean allowExec = true;

    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - Mode"
            + " [subscribe], Topic [http://one.websub.topic.com], Lease Seconds [3000]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed - Mode"
            + " [subscribe], Topic [http://one.websub.topic.com], Lease Seconds [1000]";

    private static final String XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "XML WebSub Notification Received by websubSubscriber: "
                    + "<websub><request>Notification</request><type>Internal</type></websub>";
    private static final String TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "Text WebSub Notification Received by websubSubscriber: Text update for internal Hub";
    private static final String JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "JSON WebSub Notification Received by websubSubscriber: {\"action\":\"publish\", "
                    + "\"mode\":\"internal-hub\"}";
    private static final String XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "XML WebSub Notification Received by websubSubscriberTwo: "
                    + "<websub><request>Notification</request><type>Internal</type></websub>";
    private static final String TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "Text WebSub Notification Received by websubSubscriberTwo: Text update for internal Hub";
    private static final String JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "JSON WebSub Notification Received by websubSubscriberTwo: {\"action\":\"publish\", "
                    + "\"mode\":\"internal-hub\"}";

    private static final String XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "XML WebSub Notification Received by websubSubscriber: "
                    + "<websub><request>Notification</request><type>Remote</type></websub>";
    private static final String TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "Text WebSub Notification Received by websubSubscriber: Text update for remote Hub";
    private static final String JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "JSON WebSub Notification Received by websubSubscriber: {\"action\":\"publish\", \"mode\":\"remote-hub\"}";
    private static final String XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "XML WebSub Notification Received by websubSubscriberTwo: "
                    + "<websub><request>Notification</request><type>Remote</type></websub>";
    private static final String TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "Text WebSub Notification Received by websubSubscriberTwo: Text update for remote Hub";
    private static final String JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "JSON WebSub Notification Received by websubSubscriberTwo: {\"action\":\"publish\","
                    + " \"mode\":\"remote-hub\"}";

    private LogLeecher intentVerificationLogLeecherOne = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher intentVerificationLogLeecherTwo = new LogLeecher(INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG);

    private LogLeecher internalHubXmlNotificationLogLeecherOne
            = new LogLeecher(XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubXmlNotificationLogLeecherTwo
            = new LogLeecher(XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher internalHubTextNotificationLogLeecherOne
            = new LogLeecher(TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubTextNotificationLogLeecherTwo
            = new LogLeecher(TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher internalHubJsonNotificationLogLeecherOne
            = new LogLeecher(JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher internalHubJsonNotificationLogLeecherTwo
            = new LogLeecher(JSON_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);


    private LogLeecher remoteHubXmlNotificationLogLeecherOne
            = new LogLeecher(XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubXmlNotificationLogLeecherTwo
            = new LogLeecher(XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher remoteHubTextNotificationLogLeecherOne
            = new LogLeecher(TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubTextNotificationLogLeecherTwo
            = new LogLeecher(TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher remoteHubJsonNotificationLogLeecherOne
            = new LogLeecher(JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubJsonNotificationLogLeecherTwo
            = new LogLeecher(JSON_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);


    @BeforeMethod
    public void setup() throws BallerinaTestException {
        if (!allowExec) {
            return;
        }
        allowExec = false;
        webSubSubscriber = new BServerInstance(balServer);

        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "websub" + File.separator + "test_different_content_type_subscribers.bal")
                .getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubXmlNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubXmlNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubTextNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubTextNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubJsonNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubJsonNotificationLogLeecherTwo);

        webSubSubscriber.addLogLeecher(remoteHubXmlNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubXmlNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(remoteHubTextNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubTextNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(remoteHubJsonNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubJsonNotificationLogLeecherTwo);

        String[] subscriberArgs = {};
        webSubSubscriber.startServer(subscriberBal, subscriberArgs, new int[]{8282});
    }

    @Test
    public void testSubscriptionAndIntentVerification() throws BallerinaTestException {
        intentVerificationLogLeecherOne.waitForText(30000);
        intentVerificationLogLeecherTwo.waitForText(30000);
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_INTERNAL, CONTENT_TYPE_STRING);
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_REMOTE, CONTENT_TYPE_STRING);
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_INTERNAL, CONTENT_TYPE_XML);
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_REMOTE, CONTENT_TYPE_XML);
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_INTERNAL, CONTENT_TYPE_JSON);
        requestUpdate(PUBLISHER_NOTIFY_URL, HUB_MODE_REMOTE, CONTENT_TYPE_JSON);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedTextContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubTextNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testAuthenticatedTextContentReceiptForInternalHub")
    public void testAuthenticatedTextContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubTextNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testAuthenticatedTextContentReceiptForRemoteHub")
    public void testUnauthenticatedTextContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubTextNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testUnauthenticatedTextContentReceiptForInternalHub")
    public void testUnauthenticatedTextContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubTextNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testUnauthenticatedTextContentReceiptForRemoteHub")
    public void testAuthenticatedXmlContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubXmlNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testAuthenticatedXmlContentReceiptForInternalHub")
    public void testAuthenticatedXmlContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubXmlNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testAuthenticatedXmlContentReceiptForRemoteHub")
    public void testUnauthenticatedXmlContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubXmlNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testUnauthenticatedXmlContentReceiptForInternalHub")
    public void testUnauthenticatedXmlContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubXmlNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testUnauthenticatedXmlContentReceiptForRemoteHub")
    public void testAuthenticatedJsonContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubJsonNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testAuthenticatedJsonContentReceiptForInternalHub")
    public void testAuthenticatedJsonContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubJsonNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testAuthenticatedJsonContentReceiptForRemoteHub")
    public void testUnauthenticatedJsonContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubJsonNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testUnauthenticatedJsonContentReceiptForInternalHub")
    public void testUnauthenticatedJsonContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubJsonNotificationLogLeecherTwo.waitForText(45000);
        allowExec = true;
    }

    @AfterMethod
    private void teardown() throws Exception {
        if (!allowExec) {
            return;
        }
        webSubSubscriber.shutdownServer();
    }
}
