package org.ballerinalang.test.services.configuration;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.CharacterChannel;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            Assert.assertEquals(outputStream.size(), content.getBytes().length);
        } catch (IOException e) {
            log.error("Error occurred in testLargePayload", e.getMessage());
        }
    }

    @Test(description = "Test default overflow settings (memory threshold 2MB) with actual overflow")
    public void testDefaultWithOverflow() {
        String path = "/defaultOverflow";
        try {
            String filePath = "datafiles" + File.separator + "io" + File.separator + "text" + File.separator +
                    "fileThatExceeds2MB.txt";
            ByteChannel byteChannel = TestUtil.openForReading(filePath);
            Channel channel = new MockByteChannel(byteChannel, 10);
            CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
            String responseValue = characterChannel.readAll();
            characterChannel.close();
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST",
                    responseValue);
            HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
            Assert.assertNotNull(response, "Response message not found");
            InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
            Assert.assertNotNull(inputStream, "Inputstream is null");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MimeUtil.writeInputToOutputStream(inputStream, outputStream);
            Assert.assertEquals(outputStream.size(), 2323779);
        } catch (IOException | URISyntaxException e) {
            log.error("Error occurred in testLargePayload", e.getMessage());
        }
    }

    @Test(description = "Test custom overflow settings (memory threshold 0.00002MB) and tempLocation='testOverFlow')" +
            "without actual overflow")
    public void testCustomWithoutOverflow() {
        String path = "/customOverflow";
        try {
            String content = "ballerina";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", content);
            HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
            Assert.assertNotNull(response, "Response message not found");
            InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
            Assert.assertNotNull(inputStream, "Inputstream is null");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MimeUtil.writeInputToOutputStream(inputStream, outputStream);
            Assert.assertEquals(outputStream.size(), content.getBytes().length);
        } catch (IOException e) {
            log.error("Error occurred in testLargePayload", e.getMessage());
        }
    }

    @Test(description = "Test custom overflow settings (memory threshold=0.00002MB and tempLocation='testOverFlow')" +
            "with actual overflow")
    public void testCustomWithOverflow() {
        String path = "/customOverflow";
        try {
            String content = "ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina" +
                    "ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina" +
                    "ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina" +
                    "ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina " +
                    "ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina" +
                    "ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina ballerina";
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", content);
            HTTPCarbonMessage response = Services.invokeNew(serviceResult, MOCK_ENDPOINT_NAME, cMsg);
            Assert.assertNotNull(response, "Response message not found");
            InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
            Assert.assertNotNull(inputStream, "Inputstream is null");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MimeUtil.writeInputToOutputStream(inputStream, outputStream);
            Assert.assertEquals(outputStream.size(), content.getBytes().length);
            Path userDefinedTempLocation = Paths.get("testBallerinaOverFlow").toAbsolutePath();
            Assert.assertTrue(Files.exists(userDefinedTempLocation));
            Files.delete(userDefinedTempLocation);
            Assert.assertFalse(Files.exists(userDefinedTempLocation));
        } catch (IOException e) {
            log.error("Error occurred in testLargePayload", e.getMessage());
        }
    }
}
