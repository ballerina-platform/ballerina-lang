package org.ballerinalang.test.service.websub;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.awaitility.Duration;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.awaitility.Awaitility.given;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.updateNotified;
import static org.ballerinalang.test.service.websub.WebSubTestUtils.updateSubscribed;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This class tests support for WebSub notifications of different content types.
 * Tests:
 * 1. Content delivery to multiple subscribers
 * 2. Content delivery for subscriptions made with and without specifying a secret
 * 3. Content delivery for string and XML types. Other WebSub tests cover JSON content.
 */
public class WebSubContentTypeSupportTestCase extends BaseTest {
    private BServerInstance webSubSubscriber;
    private BMainInstance webSubPublisher;

    private final int servicePort = 8686;
    private final String helperServicePortAsString = "8094";

    private static String hubUrl = "https://localhost:9696/websub/hub";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_ONE_LOG = "ballerina: Intent Verification agreed - Mode"
            + " [subscribe], Topic [http://www.websubpubtopic.com], Lease Seconds [3000]";
    private static final String INTENT_VERIFICATION_SUBSCRIBER_TWO_LOG = "ballerina: Intent Verification agreed - Mode"
            + " [subscribe], Topic [http://www.websubpubtopic.com], Lease Seconds [1000]";

    private static final String XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "XML WebSub Notification Received by websubSubscriber: "
                    + "<websub><request>Notification</request><type>Internal</type></websub>";
    private static final String TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "Text WebSub Notification Received by websubSubscriber: Text update for internal Hub";
    private static final String XML_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "XML WebSub Notification Received by websubSubscriberTwo: "
                    + "<websub><request>Notification</request><type>Internal</type></websub>";
    private static final String TEXT_INTERNAL_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "Text WebSub Notification Received by websubSubscriberTwo: Text update for internal Hub";

    private static final String XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "XML WebSub Notification Received by websubSubscriber: "
                    + "<websub><request>Notification</request><type>Remote</type></websub>";
    private static final String TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG =
            "Text WebSub Notification Received by websubSubscriber: Text update for remote Hub";
    private static final String XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "XML WebSub Notification Received by websubSubscriberTwo: "
                    + "<websub><request>Notification</request><type>Remote</type></websub>";
    private static final String TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG =
            "Text WebSub Notification Received by websubSubscriberTwo: Text update for remote Hub";

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
    private LogLeecher remoteHubXmlNotificationLogLeecherOne
            = new LogLeecher(XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubXmlNotificationLogLeecherTwo
            = new LogLeecher(XML_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);
    private LogLeecher remoteHubTextNotificationLogLeecherOne
            = new LogLeecher(TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_ONE_LOG);
    private LogLeecher remoteHubTextNotificationLogLeecherTwo
            = new LogLeecher(TEXT_REMOTE_HUB_NOTIFICATION_SUBSCRIBER_TWO_LOG);


    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);
        webSubPublisher = new BMainInstance(balServer);

        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "websub" + File.separator + "content_types" + File.separator
                + "publisher.bal").getAbsolutePath();
        String[] publisherArgs = {"-e b7a.websub.hub.remotepublish=true",
                "-e test.helper.service.port=" + helperServicePortAsString};

        String subscriberBal = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "websub" + File.separator + "content_types" + File.separator
                + "subscribers.bal").getAbsolutePath();
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(intentVerificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubXmlNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubXmlNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(internalHubTextNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(internalHubTextNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(remoteHubXmlNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubXmlNotificationLogLeecherTwo);
        webSubSubscriber.addLogLeecher(remoteHubTextNotificationLogLeecherOne);
        webSubSubscriber.addLogLeecher(remoteHubTextNotificationLogLeecherTwo);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                webSubPublisher.runMain(balFile, publisherArgs, new String[]{});
            } catch (BallerinaTestException e) {
                //ignored since any errors here would be reflected as test failures
            }
        });

        //Allow to bring up the hub
        given().ignoreException(ConnectException.class).with().pollInterval(Duration.FIVE_SECONDS).and()
                .with().pollDelay(Duration.TEN_SECONDS).await().atMost(60, SECONDS).until(() -> {
            //using same pack location, hence server home is same
            HttpResponse response = HttpsClientRequest.doGet(hubUrl, webSubSubscriber.getServerHome());
            return response.getResponseCode() == 202;
        });

        String[] subscriberArgs = {};
        webSubSubscriber.startServer(subscriberBal, subscriberArgs, new int[]{servicePort});

        //Allow to start up the subscriber service
        given().ignoreException(ConnectException.class).with().pollInterval(Duration.FIVE_SECONDS).and()
                .with().pollDelay(Duration.TEN_SECONDS).await().atMost(60, SECONDS).until(() -> {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            headers.put("X-Hub-Signature", "SHA256=5262411828583e9dc7eaf63aede0abac8e15212e06320bb021c433a20f27d553");
            HttpResponse response = HttpClientRequest.doPost(
                    webSubSubscriber.getServiceURLHttp(servicePort, "websub"), "{\"dummy\":\"body\"}",
                    headers);
            return response.getResponseCode() == 202;
        });
    }

    @Test
    public void testSubscriptionAndIntentVerification() throws BallerinaTestException, IOException {
        intentVerificationLogLeecherOne.waitForText(30000);
        intentVerificationLogLeecherTwo.waitForText(30000);
        updateSubscribed(helperServicePortAsString);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedTextContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubTextNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedTextContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubTextNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedTextContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubTextNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedTextContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubTextNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedXmlContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubXmlNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testAuthenticatedXmlContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubXmlNotificationLogLeecherOne.waitForText(45000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedXmlContentReceiptForInternalHub() throws BallerinaTestException {
        internalHubXmlNotificationLogLeecherTwo.waitForText(45000);
    }

    @Test(dependsOnMethods = "testSubscriptionAndIntentVerification")
    public void testUnauthenticatedXmlContentReceiptForRemoteHub() throws BallerinaTestException {
        remoteHubXmlNotificationLogLeecherTwo.waitForText(45000);
    }

    @AfterClass
    private void cleanup() throws Exception {
        updateNotified(helperServicePortAsString);
        webSubSubscriber.shutdownServer();
    }
}
