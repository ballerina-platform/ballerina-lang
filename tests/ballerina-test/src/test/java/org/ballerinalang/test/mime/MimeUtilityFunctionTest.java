/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.test.mime;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.EntityBody;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.nativeimpl.io.channels.base.AbstractChannel;
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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

import static org.ballerinalang.mime.util.Constants.FILE;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.PARAMETER_MAP_INDEX;
import static org.ballerinalang.mime.util.Constants.PRIMARY_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_FILE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.SUBTYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUFFIX_INDEX;

/**
 * Unit tests for MIME package utilities.
 *
 * @since 0.963.0
 */
public class MimeUtilityFunctionTest {
    private static final Logger log = LoggerFactory.getLogger(MimeUtilityFunctionTest.class);

    private CompileResult compileResult, serviceResult;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String mediaTypeStruct = MEDIA_TYPE;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/mime-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test 'getMediaType' function in ballerina.mime package")
    public void testGetMediaType() {
        String contentType = "multipart/form-data; boundary=032a1ab685934650abbe059cb45d6ff3";
        BValue[] args = {new BString(contentType)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetMediaType", args);
        Assert.assertEquals(returns.length, 1);
        BStruct mediaType = (BStruct) returns[0];
        Assert.assertEquals(mediaType.getStringField(PRIMARY_TYPE_INDEX), "multipart");
        Assert.assertEquals(mediaType.getStringField(SUBTYPE_INDEX), "form-data");
        Assert.assertEquals(mediaType.getStringField(SUFFIX_INDEX), "");
        BMap map = (BMap) mediaType.getRefField(PARAMETER_MAP_INDEX);
        Assert.assertEquals(map.get("boundary").stringValue(), "032a1ab685934650abbe059cb45d6ff3");
    }

    @Test(description = "Test 'toString' function in ballerina.mime package")
    public void testToStringOnMediaType() {
        BStruct mediaType = BCompileUtil
                .createAndGetStruct(compileResult.getProgFile(), protocolPackageMime, mediaTypeStruct);
        mediaType.setStringField(PRIMARY_TYPE_INDEX, "application");
        mediaType.setStringField(SUBTYPE_INDEX, "test+xml");
        BValue[] args = {mediaType};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToStringOnMediaType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/test+xml");
    }

    @Test(description = "Test 'toStringWithParameters' function in ballerina.mime package")
    public void testToStringWithParametersOnMediaType() {
        BStruct mediaType = BCompileUtil
                .createAndGetStruct(compileResult.getProgFile(), protocolPackageMime, mediaTypeStruct);
        mediaType.setStringField(PRIMARY_TYPE_INDEX, "application");
        mediaType.setStringField(SUBTYPE_INDEX, "test+xml");
        BMap map = new BMap();
        map.put("charset", new BString("utf-8"));
        mediaType.setRefField(PRIMARY_TYPE_INDEX, map);
        BValue[] args = {mediaType};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToStringWithParametersOnMediaType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/test+xml; charset=utf-8");
    }

    @Test(description = "Test 'testMimeBase64Encode' function in ballerina.mime package")
    public void testMimeBase64Encode() {
        BBlob blob = new BBlob("a".getBytes());
        BValue[] args = {blob};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64Encode", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "YQ==");
    }

    @Test(description = "Test 'testMimeBase64EncodeString' function in ballerina.mime package")
    public void testMimeBase64EncodeString() {
        BValue[] args = {new BString("Ballerina"), new BString("utf-8")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64EncodeString", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "QmFsbGVyaW5h");
    }

    @Test(description = "Test 'testMimeBase64Decode' function in ballerina.mime package")
    public void testMimeBase64Decode() {
        BBlob blob = new BBlob("YQ==".getBytes());
        BValue[] args = {blob};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64Decode", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "a");
    }

    @Test(description = "Test 'testMimeBase64DecodeString' function in ballerina.mime package")
    public void testMimeBase64DecodeString() {
        BValue[] args = {new BString("QmFsbGVyaW5h"), new BString("utf-8")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64DecodeString", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test(description = "Set json data to entity and get the content back from entity as json")
    public void testGetAndSetJson() {
        BJSON jsonContent = new BJSON("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetJson", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BJSON) returns[0]).value().get("code").asText(), "123");
    }

    @Test(description = "Test whether the json content can be retrieved properly when it is called multiple times")
    public void testGetJsonMoreThanOnce() {
        BJSON jsonContent = new BJSON("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetJsonMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BJSON) returns[0]).getMessageAsString(),
                "{\"concatContent\":[{\"code\":\"123\"},{\"code\":\"123\"},{\"code\":\"123\"}]}");
    }

    @Test(description = "Set xml data to entity and get the content back from entity as xml")
    public void testGetAndSetXml() {
        BXML xmlContent = XMLUtils.parse("<name>ballerina</name>");
        BValue[] args = {xmlContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetXml", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BXML) returns[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test whether the xml content can be retrieved properly when it is called multiple times")
    public void testGetXmlMoreThanOnce() {
        BXML xmlContent = XMLUtils.parse("<name>ballerina</name>");
        BValue[] args = {xmlContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetXmlMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BXML<Object>) returns[0]).getMessageAsString(),
                "<name>ballerina</name><name>ballerina</name><name>ballerina</name>");
    }

    @Test(description = "Set text data to entity and get the content back from entity as text")
    public void testGetAndSetText() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetText", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina !");
    }

    @Test(description = "Test whether the text content can be retrieved properly when it is called multiple times")
    public void testGetTextMoreThanOnce() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTextMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "Hello Ballerina !Hello Ballerina !Hello Ballerina !");
    }

    @Test(description = "Set blob data to entity and get the content back from entity as a blob")
    public void testGetAndSetBlob() {
        String content = "ballerina";
        BBlob byteContent = new BBlob(content.getBytes());
        BValue[] args = {byteContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetBlob", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), content);
    }

    @Test(description = "Test whether the blob content can be retrieved properly when it is called multiple times")
    public void testGetBlobMoreThanOnce() {
        String content = "ballerina";
        BBlob byteContent = new BBlob(content.getBytes());
        BValue[] args = {byteContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetBlobMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "ballerinaballerinaballerina");
    }

    @Test(description = "Set file as entity body and get the content back as a blob")
    public void testSetFileAsEntityBody() {
        try {
            File file = File.createTempFile("testFile", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Hello Ballerina!");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil
                    .createAndGetStruct(compileResult.getProgFile(), PROTOCOL_PACKAGE_FILE, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            BValue[] args = {fileStruct};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetFileAsEntityBody", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina!",
                    "Entity body is not properly set");
        } catch (IOException e) {
            log.error("Error occurred in testSetFileAsEntityBody", e.getMessage());
        }
    }

    @Test(description = "Set byte channel as entity body and get the content back as a blob")
    public void testSetByteChannel() {
        try {
            File file = File.createTempFile("testFile", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Hello Ballerina!");
            bufferedWriter.close();
            BStruct byteChannelStruct = Util.getByteChannelStruct(compileResult);
            byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, EntityBodyHandler.getByteChannelForTempFile
                    (file.getAbsolutePath()));
            BValue[] args = {byteChannelStruct};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetByteChannel", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina!",
                    "Entity body is not properly set");
        } catch (IOException e) {
            log.error("Error occurred in testSetByteChannel", e.getMessage());
        }
    }

    @Test(description = "Set byte channel as entity body and get that channel back")
    public void testGetByteChannel() {
        try {
            File file = File.createTempFile("testFile", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Hello Ballerina!");
            bufferedWriter.close();
            BStruct byteChannelStruct = Util.getByteChannelStruct(compileResult);
            byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, EntityBodyHandler.getByteChannelForTempFile
                    (file.getAbsolutePath()));
            BValue[] args = {byteChannelStruct};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetByteChannel", args);
            Assert.assertEquals(returns.length, 1);
            BStruct returnByteChannelStruct = (BStruct) returns[0];
            EntityBody entityBody = EntityBodyHandler.getEntityBody(returnByteChannelStruct.getNativeData
                    (IOConstants.BYTE_CHANNEL_NAME));
            FileIOChannel fileIOChannel = entityBody.getFileIOChannel();
            Assert.assertEquals(StringUtils.getStringFromInputStream(new ByteArrayInputStream(fileIOChannel.readAll())),
                    "Hello Ballerina!");
        } catch (IOException e) {
            log.error("Error occurred in testSetByteChannel", e.getMessage());
        }
    }

    @Test(description = "Set entity body as a byte channel get the content back as a string")
    public void testSetEntityBodyMultipleTimes() {
        try {
            File file = File.createTempFile("testFile", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("File Content");
            bufferedWriter.close();
            BStruct byteChannelStruct = Util.getByteChannelStruct(compileResult);
            byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                    EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            BValue[] args = {byteChannelStruct, new BString("Hello Ballerina!")};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetEntityBodyMultipleTimes", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "File Content");
        } catch (IOException e) {
            log.error("Error occurred in testSetByteChannel", e.getMessage());
        }
    }

    @Test(description = "Once the file channel is created and temp file deleted, check whether the content can " +
            "still be retrieved properly from the file channel")
    public void testTempFileDeletion() {
        File file;
        try {
            file = File.createTempFile("testFile", ".tmp");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("File Content");
            bufferedWriter.close();
            FileIOChannel fileIOChannel = EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath());
            Assert.assertFalse(file.exists());
            InputStream inputStream = fileIOChannel.getInputStream();
            Assert.assertNotNull(inputStream);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            EntityBodyHandler.writeInputToOutputStream(result, inputStream);
            Assert.assertEquals(result.toString("UTF-8"), "File Content");
        } catch (IOException e) {
            log.error("Error occurred in testTempFileDeletion", e.getMessage());
        }
    }

    @Test(description = "When the payload exceeds 2MB check whether the response received back matches the original " +
            "content length")
    public void testLargePayload() {
        String path = "/test/largepayload";
        try {
            ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/fileThatExceeds2MB.txt");
            AbstractChannel channel = new MockByteChannel(byteChannel, 10);
            CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
            String responseValue = characterChannel.readAll();
            characterChannel.close();
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST",
                    responseValue);
            HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
            Assert.assertNotNull(response, "Response message not found");
            InputStream inputStream = new HttpMessageDataStreamer(response).getInputStream();
            Assert.assertNotNull(inputStream, "Inputstream is null");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] bytes = new byte[1024];
            int count;
            while ((count = inputStream.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            Assert.assertEquals(out.size(), 2323779);
        } catch (IOException | URISyntaxException e) {
            log.error("Error occurred in testLargePayload", e.getMessage());
        }
    }
}
