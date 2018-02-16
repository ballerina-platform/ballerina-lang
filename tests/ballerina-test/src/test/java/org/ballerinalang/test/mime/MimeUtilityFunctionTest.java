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
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.net.http.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
 * @since 0.964.0
 */
public class MimeUtilityFunctionTest {
    private static final Logger LOG = LoggerFactory.getLogger(MimeUtilityFunctionTest.class);

    private CompileResult compileResult, serviceResult;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String protocolPackageFile = PROTOCOL_PACKAGE_FILE;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/mime-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
        // serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test setting json content to and entity and get the content back from entity as json")
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

    @Test(description = "Test setting json content to and entity and get the content back from entity as json")
    public void testGetAndSetXml() {
        BXML xmlContent = XMLUtils.parse("<name>ballerina</name>");
        BValue[] args = {xmlContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetXml", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BXML) returns[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test whether the json content can be retrieved properly when it is called multiple times")
    public void testGetXmlMoreThanOnce() {
        BXML xmlContent = XMLUtils.parse("<name>ballerina</name>");
        BValue[] args = {xmlContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetXmlMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BXML<Object>) returns[0]).getMessageAsString(),
                "<name>ballerina</name><name>ballerina</name><name>ballerina</name>");
    }

    @Test(description = "Test setting json content to and entity and get the content back from entity as json")
    public void testGetAndSetText() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetText", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina !");
    }

    @Test(description = "Test whether the json content can be retrieved properly when it is called multiple times")
    public void testGetTextMoreThanOnce() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTextMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "Hello Ballerina !Hello Ballerina !Hello Ballerina !");
    }

    @Test(description = "Test setting json content to and entity and get the content back from entity as json")
    public void testGetAndSetBlob() {
        String content = "ballerina";
        BBlob byteContent = new BBlob(content.getBytes());
        BValue[] args = {byteContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndBlob", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), content);
    }

    @Test(description = "Test whether the json content can be retrieved properly when it is called multiple times")
    public void testGetBlobMoreThanOnce() {
        String content = "ballerina";
        BBlob byteContent = new BBlob(content.getBytes());
        BValue[] args = {byteContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetBlobMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "ballerinaballerinaballerina");
    }

    @Test(description = "Test whether the json content can be retrieved properly when it is called multiple times")
    public void testSetFileAsEntityBody() {
        try {
            File file = File.createTempFile("testFile", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Hello Ballerina!");
            bufferedWriter.close();
            BStruct fileStruct = BCompileUtil
                    .createAndGetStruct(compileResult.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            BValue[] args = {fileStruct};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetFileAsEntityBody", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina!",
                    "Entity body is not properly set");
        } catch (IOException e) {
            LOG.error("Error occured in testGetJsonFromFile", e.getMessage());
        }
    }

   /* @Test(description = "Test 'getText' function in ballerina.net.mime package")
    public void testGetTextFromFile() {
        BStruct entity = BCompileUtil
                .createAndGetStruct(compileResult.getProgFile(), protocolPackageMime, entityStruct);
        try {
            String payload = "ballerina";
            File file = File.createTempFile("testText", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(payload);
            bufferedWriter.close();

            BStruct fileStruct = BCompileUtil
                    .createAndGetStruct(compileResult.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            entity.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            BValue[] args = {entity};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetTextFromFile", args);

            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), payload);
        } catch (IOException e) {
            LOG.error("Error occured in testGetTextFromFile", e.getMessage());
        }
    }

    @Test(description = "Test 'getJson' function in ballerina.net.mime package")
    public void testGetJsonFromFile() {
        BStruct entity = BCompileUtil
                .createAndGetStruct(compileResult.getProgFile(), protocolPackageMime, entityStruct);
        try {
            File file = File.createTempFile("testJson", ".json");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("{'code':'123'}");
            bufferedWriter.close();

            BStruct fileStruct = BCompileUtil
                    .createAndGetStruct(compileResult.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            entity.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            BValue[] args = {entity};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetJsonFromFile", args);

            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(((BJSON) returns[0]).value().get("code").asText(), "123");
        } catch (IOException e) {
            LOG.error("Error occured in testGetJsonFromFile", e.getMessage());
        }
    }

    @Test(description = "Test 'getXml' function in ballerina.net.mime package")
    public void testGetXmlFromFile() {
        BStruct entity = BCompileUtil
                .createAndGetStruct(compileResult.getProgFile(), protocolPackageMime, entityStruct);
        try {
            File file = File.createTempFile("testXml", ".xml");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("<test>ballerina</test>");
            bufferedWriter.close();

            BStruct fileStruct = BCompileUtil
                    .createAndGetStruct(compileResult.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            entity.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            BValue[] args = {entity};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetXmlFromFile", args);

            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(((BXMLItem) returns[0]).getTextValue().stringValue(), "ballerina");
        } catch (IOException e) {
            LOG.error("Error occured in testGetXmlFromFile", e.getMessage());
        }
    }

    @Test(description = "Test 'getBlob' function in ballerina.net.mime package")
    public void testGetBlobFromFile() {
        BStruct entity = BCompileUtil
                .createAndGetStruct(compileResult.getProgFile(), protocolPackageMime, entityStruct);
        try {
            File file = File.createTempFile("testBinary", ".tmp");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("Hello Ballerina!");
            bufferedWriter.close();

            BStruct fileStruct = BCompileUtil
                    .createAndGetStruct(compileResult.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            entity.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            BValue[] args = {entity};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetBlobFromFile", args);

            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina!",
                    "Payload is not set properly");
        } catch (IOException e) {
            LOG.error("Error occured in testGetBlobFromFile", e.getMessage());
        }
    }

    */

    @Test(description = "Test 'getMediaType' function in ballerina.net.mime package")
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

    @Test(description = "Test 'toString' function in ballerina.net.mime package")
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

    @Test(description = "Test 'toStringWithParameters' function in ballerina.net.mime package")
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

    @Test(description = "Test 'testMimeBase64Encode' function in ballerina.net.mime package")
    public void testMimeBase64Encode() {
        BBlob blob = new BBlob("a".getBytes());
        BValue[] args = {blob};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64Encode", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "YQ==");
    }

    @Test(description = "Test 'testMimeBase64EncodeString' function in ballerina.net.mime package")
    public void testMimeBase64EncodeString() {
        BValue[] args = {new BString("Ballerina"), new BString("utf-8")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64EncodeString", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "QmFsbGVyaW5h");
    }

    @Test(description = "Test 'testMimeBase64Decode' function in ballerina.net.mime package")
    public void testMimeBase64Decode() {
        BBlob blob = new BBlob("YQ==".getBytes());
        BValue[] args = {blob};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64Decode", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "a");
    }

    @Test(description = "Test 'testMimeBase64DecodeString' function in ballerina.net.mime package")
    public void testMimeBase64DecodeString() {
        BValue[] args = {new BString("QmFsbGVyaW5h"), new BString("utf-8")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMimeBase64DecodeString", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

/*
    @Test(description = "Test whether the system keeps the payload in a temp file in case the size exceeds 2MB")
    public void testLargePayload() {
        String path = "/test/largepayload";
        try {
            ByteChannel byteChannel = TestUtil.openForReading("datafiles/io/text/fileThatExceeds2MB.txt");
            AbstractChannel channel = new MockByteChannel(byteChannel, 10);
            CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
            String responseValue = characterChannel.readAll();
            characterChannel.close();
            HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, responseValue);
            HTTPCarbonMessage response = Services.invokeNew(serviceResult, cMsg);
            Assert.assertNotNull(response, "Response message not found");
            String temporaryFilePath = ResponseReader.getReturnValue(response);
            Assert.assertNotNull(temporaryFilePath);
            Assert.assertFalse(temporaryFilePath.isEmpty(), "Temporary file has not been created");
            File file = new File(temporaryFilePath);
            file.delete();
        } catch (IOException | URISyntaxException e) {
            LOG.error("Error occured in testLargePayload", e.getMessage());
        }
    }*/
}
