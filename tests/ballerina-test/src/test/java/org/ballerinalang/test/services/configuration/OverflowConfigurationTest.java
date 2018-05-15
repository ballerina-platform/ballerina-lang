package org.ballerinalang.test.services.configuration;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OverflowConfigurationTest {
    private static final Logger log = LoggerFactory.getLogger(OverflowConfigurationTest.class);

    private static final String MOCK_ENDPOINT_NAME = "mockEP";
    private CompileResult serviceResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/services/configuration/overflow_annotation_test.bal";
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test default overflow settings (memory threshold 2MB) without actual overflow")
    public void testDefaultWithoutOverflow() {
        String path = "/defaultOverflow";
        try {
            String content = "ballerina";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", content);
            HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
            Assert.assertNotNull(response, "Response message not found");
            InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
            Assert.assertNotNull(inputStream, "Inputstream is null");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MimeUtil.writeInputToOutputStream(inputStream, outputStream);
            Assert.assertEquals(outputStream.size(), 2323779);
        } catch (IOException e) {
            log.error("Error occurred in testLargePayload", e.getMessage());
        }
    }
}
