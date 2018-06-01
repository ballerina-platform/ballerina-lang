package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class HTTPClientActionsTestCase extends IntegrationTestCase {
    private ServerInstance ballerinaServer;
    String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "httpService" + File.separator + "http_client_actions.bal").getAbsolutePath();

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test
    public void testPostAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test2/clientPost"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Entity body is not text compatible since the received " +
                "content-type is : null", "Message content mismatched");
    }


    @Test
    public void testGetAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("test2/clientGet"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "HelloHelloHello", "Message content mismatched");
    }


    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
