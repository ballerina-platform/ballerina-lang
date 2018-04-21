package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
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

public class TestReusabilityOfRequest {

    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "httpService" + File.separator + "test-reusability-of-request.bal")
                .getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test
    public void reuseRequestWithEntity() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer
                .getServiceURLHttp("reuseObj/request_without_entity"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Hello from GET!Hello from GET!", "Message content mismatched");
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
