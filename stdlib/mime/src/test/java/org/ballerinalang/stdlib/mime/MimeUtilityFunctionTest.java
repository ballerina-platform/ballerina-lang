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

package org.ballerinalang.stdlib.mime;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.util.XMLUtils;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.utils.IOConstants;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.jvnet.mimepull.MIMEPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_FILENAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_NAME_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.CONTENT_DISPOSITION_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.DISPOSITION_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PARAMETER_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PRIMARY_TYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_IO_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.mime.util.MimeConstants.READABLE_BYTE_CHANNEL_STRUCT;
import static org.ballerinalang.mime.util.MimeConstants.SUBTYPE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.SUFFIX_FIELD;
import static org.ballerinalang.stdlib.common.CommonTestUtils.assertJBytesWithBBytes;
import static org.ballerinalang.stdlib.mime.Util.getTemporaryFile;
import static org.ballerinalang.stdlib.mime.Util.validateBodyPartContent;
import static org.ballerinalang.stdlib.mime.Util.verifyMimeError;

/**
 * Unit tests for MIME package utilities.
 *
 * @since 0.963.0
 */
public class MimeUtilityFunctionTest {
    private static final Logger log = LoggerFactory.getLogger(MimeUtilityFunctionTest.class);

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime-test.bal";
        compileResult = BCompileUtil.compileOffline(sourceFilePath);
    }

    @Test(description = "Test 'getMediaType' function in ballerina/mime package")
    public void testGetMediaType() {
        Object[] args = {StringUtils.fromString("multipart/form-data; boundary=032a1ab685934650abbe059cb45d6ff3")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetMediaType", args);
        Assert.assertEquals(returns.length, 1);
        BMap<String, BValue> mediaType = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(mediaType.get(PRIMARY_TYPE_FIELD.getValue()).stringValue(), "multipart");
        Assert.assertEquals(mediaType.get(SUBTYPE_FIELD.getValue()).stringValue(), "form-data");
        Assert.assertEquals(mediaType.get(SUFFIX_FIELD.getValue()).stringValue(), "");
        BMap map = (BMap) mediaType.get(PARAMETER_MAP_FIELD.getValue());
        Assert.assertEquals(map.get("boundary").stringValue(), "032a1ab685934650abbe059cb45d6ff3");
    }

    @Test(description = "Test whether an error is returned while constructing MediaType object with an " +
            "incorrect content type value")
    public void getMediaTypeWithIncorrectContentType() {
        Object[] args = {StringUtils.fromString("testContentType")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetMediaType", args);
        Assert.assertEquals(returns.length, 1);
        verifyMimeError(returns[0], "error(\"Unable to find a sub type.\")");
    }

    @Test(description = "Test 'getBaseType' function in ballerina/mime package")
    public void testGetBaseTypeOnMediaType() {
        BObject mediaType = ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
        mediaType.set(PRIMARY_TYPE_FIELD, StringUtils.fromString("application"));
        mediaType.set(SUBTYPE_FIELD, StringUtils.fromString("test+xml"));
        Object[] args = {mediaType};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetBaseTypeOnMediaType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/test+xml");
    }

    @Test(description = "Test 'testToStringOnMediaType' function in ballerina/mime package")
    public void testToStringOnMediaType() {
        BObject mediaType = ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
        mediaType.set(PRIMARY_TYPE_FIELD, StringUtils.fromString("application"));
        mediaType.set(SUBTYPE_FIELD, StringUtils.fromString("test+xml"));
        io.ballerina.runtime.api.values.BMap<io.ballerina.runtime.api.values.BString, Object> mapValue =
                ValueCreator.createMapValue(TypeCreator.createMapType(PredefinedTypes.TYPE_STRING));;
        mapValue.put(StringUtils.fromString("charset"), StringUtils.fromString("utf-8"));
        mediaType.set(PARAMETER_MAP_FIELD, mapValue);
        Object[] args = {mediaType};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToStringOnMediaType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/test+xml; charset=utf-8");
    }

    @Test(description = "Test 'getContentDispositionObject' function in ballerina/mime package")
    public void testGetContentDispositionObject() {
        Object[] args = {StringUtils.fromString("form-data; name=\"filepart\"; filename=\"file-01.txt\"")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetContentDispositionObject", args);
        Assert.assertEquals(returns.length, 1);
        BMap<String, BValue> contentDisposition = (BMap<String, BValue>) returns[0];
        Assert.assertEquals(contentDisposition.get(CONTENT_DISPOSITION_FILENAME_FIELD.getValue()).stringValue(),
                            "file-01.txt");
        Assert.assertEquals(contentDisposition.get(CONTENT_DISPOSITION_NAME_FIELD.getValue()).stringValue(),
                            "filepart");
        Assert.assertEquals(contentDisposition.get(DISPOSITION_FIELD.getValue()).stringValue(), "form-data");
        BMap map = (BMap) contentDisposition.get(PARAMETER_MAP_FIELD.getValue());
        Assert.assertEquals(map.size(), 0);
    }

    @Test
    public void testToStringOnContentDisposition() {
        BObject contentDisposition = ValueCreator.createObjectValue(PROTOCOL_MIME_PKG_ID,
                                                                    CONTENT_DISPOSITION_STRUCT);
        contentDisposition.set(CONTENT_DISPOSITION_FILENAME_FIELD, StringUtils.fromString("file-01.txt"));
        contentDisposition.set(DISPOSITION_FIELD, StringUtils.fromString("form-data"));
        contentDisposition.set(CONTENT_DISPOSITION_NAME_FIELD, StringUtils.fromString("test"));
        Object[] args = {contentDisposition};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToStringOnContentDisposition", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "form-data;name=\"test\";filename=\"file-01.txt\"");
    }

    @Test(enabled = false)
    public void testMimeBase64EncodeBlob() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        Object[] args = {ValueCreator.createArrayValue("Hello Ballerina".getBytes())};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testMimeBase64EncodeBlob", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        assertJBytesWithBBytes(((BValueArray) returnValues[0]).getBytes(), expectedValue.getBytes());
    }

    @Test(enabled = false)
    public void testMimeBase64DecodeBlob() {
        String expectedValue = "Hello Ballerina";
        Object[] args = {ValueCreator.createArrayValue("SGVsbG8gQmFsbGVyaW5h".getBytes())};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testMimeBase64DecodeBlob", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        assertJBytesWithBBytes(((BValueArray) returnValues[0]).getBytes(), expectedValue.getBytes());
    }

    @Test(description = "Set json data to entity and get the content back from entity as json")
    public void testGetAndSetJson() {
        BValue jsonContent = JsonParser.parse("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetJson", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).get("code").stringValue(), "123");
    }

    @Test(description = "Test whether the json content can be retrieved properly when it is called multiple times")
    public void testGetJsonMoreThanOnce() {
        BValue jsonContent = JsonParser.parse("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetJsonMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                            "{\"concatContent\":[{\"code\":\"123\"}, {\"code\":\"123\"}, {\"code\":\"123\"}]}");
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
        Assert.assertEquals(((BXML<Object>) returns[0]).stringValue(),
                            "<name>ballerina</name><name>ballerina</name><name>ballerina</name>");
    }

    @Test(description = "Set text data to entity and get the content back from entity as text")
    public void testGetAndSetText() {
        Object[] args = {StringUtils.fromString("Hello Ballerina !")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetText", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "Hello Ballerina !");
    }

    @Test(description = "Test whether the text content can be retrieved properly when it is called multiple times")
    public void testGetTextMoreThanOnce() {
        Object[] args = {StringUtils.fromString("Hello Ballerina !")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTextMultipleTimes", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                            "Hello Ballerina !Hello Ballerina !Hello Ballerina !");
    }

    @Test(description = "Set byte array data to entity and get the content back from entity as a byte array")
    public void testGetAndSetByteArray() {
        String content = "ballerina";
        Object[] args = {ValueCreator.createArrayValue(content.getBytes())};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetAndGetByteArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), content);
    }

    @Test(description = "Test whether the byte array content can be " +
            "retrieved properly when it is called multiple times")
    public void testGetByteArrayMoreThanOnce() {
        String content = "ballerina";
        BValueArray byteContent = new BValueArray(content.getBytes());
        BValue[] args = {byteContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetByteArrayMultipleTimes", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), content);
        Assert.assertEquals(new String(((BValueArray) returns[1]).getBytes()), content);
        Assert.assertEquals(new String(((BValueArray) returns[2]).getBytes()), content);
    }

    @Test(description = "Set file as entity body and get the content back as a byte array")
    public void testSetFileAsEntityBody() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "Hello Ballerina!");
            Object[] args = {StringUtils.fromString(file.getAbsolutePath())};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetFileAsEntityBody", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "Hello Ballerina!",
                                "Entity body is not properly set");
        } catch (IOException e) {
            log.error("Error occurred in testSetFileAsEntityBody", e.getMessage());
        }
    }

    @Test(description = "Set byte channel as entity body and get the content back as a byte array")
    public void testSetByteChannel() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "Hello Ballerina!");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                      EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetByteChannel", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "Hello Ballerina!",
                                "Entity body is not properly set");
        } catch (IOException e) {
            log.error("Error occurred in testSetByteChannel", e.getMessage());
        }
    }

    @Test(description = "Set byte channel as entity body and get that channel back")
    public void testGetByteChannel() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "Hello Ballerina!");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetByteChannel", args);
            Assert.assertEquals(returns.length, 1);
            BMap<String, BValue> returnByteChannelStruct = (BMap<String, BValue>) returns[0];
            Channel channel = (Channel) returnByteChannelStruct.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            Assert.assertEquals(StringUtils.getStringFromInputStream(channel.getInputStream()).getValue(),
                                "Hello Ballerina!");
        } catch (IOException e) {
            log.error("Error occurred in testSetByteChannel", e.getMessage());
        }
    }

    @Test(description = "Set entity body as a byte channel get the content back as a string")
    public void testSetEntityBodyMultipleTimes() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "File Content");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel, StringUtils.fromString("Hello Ballerina!")};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetEntityBodyMultipleTimes", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "File Content");
        } catch (IOException e) {
            log.error("Error occurred in testSetByteChannel", e.getMessage());
        }
    }

    @Test(description = "Once the temp file channel is closed, check whether the temp file gets deleted")
    public void testTempFileDeletion() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "File Content");
            Channel tempFileIOChannel = EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath());
            Assert.assertTrue(file.exists());
            InputStream inputStream = tempFileIOChannel.getInputStream();
            Assert.assertNotNull(inputStream);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            MimeUtil.writeInputToOutputStream(inputStream, result);
            Assert.assertEquals(result.toString("UTF-8"), "File Content");
            tempFileIOChannel.close();
            Assert.assertFalse(file.exists());
        } catch (IOException e) {
            log.error("Error occurred in testTempFileDeletion", e.getMessage());
        }
    }

    @Test(description = "An EntityError should be returned in case the byte channel is null")
    public void testGetByteChannelForNull() {
        BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID, READABLE_BYTE_CHANNEL_STRUCT);
        byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME, null);
        Object[] args = {byteChannel};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetByteChannel", args);
        Assert.assertEquals(returns.length, 1);
        verifyMimeError(returns[0], "Byte channel is not available as payload");
    }

    @Test(description = "An EntityError should be returned from 'getByteChannel()', in case the payload " +
            "is in data source form")
    public void testByteChannelWhenPayloadInDataSource() {
        BValue jsonContent = JsonParser.parse("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetJsonAndGetByteChannel", args);
        Assert.assertEquals(returns.length, 1);
        verifyMimeError(returns[0], "Byte channel is not available but payload can be obtain either as xml, json, " +
                "string or byte[] type");
    }

    @Test(description = "Once the byte channel is consumed by the user, check whether the content retrieved " +
            "as a text data source is empty")
    public void testGetTextDataSource() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "{'code':'123'}");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                      EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetTextDataSource", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "");
        } catch (IOException e) {
            log.error("Error occurred in testTempFileDeletion", e.getMessage());
        }
    }

    @Test(description = "Once the byte channel is consumed by the user, check whether the content retrieved " +
            "as a json data source return an error")
    public void testGetJsonDataSource() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "Hello Ballerina!");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetJsonDataSource", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertNotNull(returns[0]);
            Assert.assertTrue(returns[0].stringValue().contains("Error occurred while extracting json " +
                                                                        "data from entity: empty JSON document"));
        } catch (IOException e) {
            log.error("Error occurred in testTempFileDeletion", e.getMessage());
        }
    }

    @Test(description = "Test whether the Content-Disposition header value can be built from ContentDisposition " +
            "object values.")
    public void testContentDispositionForFormData() {
        BObject bodyPart = Util.createEntityObject();
        BObject contentDispositionStruct = Util.getContentDispositionStruct();
        MimeUtil.setContentDisposition(contentDispositionStruct, bodyPart,
                                       "form-data; name=\"filepart\"; filename=\"file-01.txt\"");
        String contentDispositionValue = MimeUtil.getContentDisposition(bodyPart);
        Assert.assertEquals(contentDispositionValue, "form-data;name=\"filepart\";filename=\"file-01.txt\"");
    }

    @Test
    public void testFileNameWithoutQuotes() {
        BObject bodyPart = Util.createEntityObject();
        BObject contentDispositionStruct = Util.getContentDispositionStruct();
        MimeUtil.setContentDisposition(contentDispositionStruct, bodyPart,
                                       "form-data; name=filepart; filename=file-01.txt");
        String contentDispositionValue = MimeUtil.getContentDisposition(bodyPart);
        Assert.assertEquals(contentDispositionValue, "form-data;name=\"filepart\";filename=\"file-01.txt\"");
    }

    @Test
    public void testContentDispositionWithoutParams() {
        BObject bodyPart = Util.createEntityObject();
        BObject contentDispositionStruct = Util.getContentDispositionStruct();
        MimeUtil.setContentDisposition(contentDispositionStruct, bodyPart,
                                       "form-data");
        String contentDispositionValue = MimeUtil.getContentDisposition(bodyPart);
        Assert.assertEquals(contentDispositionValue, "form-data");
    }

    @Test
    public void testGetXmlWithSuffix() {
        BXML xmlContent = XMLUtils.parse("<name>ballerina</name>");
        BValue[] args = {xmlContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetXmlWithSuffix", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BXML) returns[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Get xml content from entity that has a non compatible xml content-type")
    public void testGetXmlWithNonCompatibleMediaType() {
        BXML xmlContent = XMLUtils.parse("<name>ballerina</name>");
        BValue[] args = {xmlContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetXmlWithNonCompatibleMediaType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "<name>ballerina</name>");
    }

    @Test
    public void testGetJsonWithSuffix() {
        BValue jsonContent = JsonParser.parse("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetJsonWithSuffix", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"code\":\"123\"}");
    }

    @Test
    public void testGetJsonWithNonCompatibleMediaType() {
        BValue jsonContent = JsonParser.parse("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetJsonWithNonCompatibleMediaType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"code\":\"123\"}");
    }

    @Test
    public void testGetTextContentWithNonCompatibleMediaType() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTextWithNonCompatibleMediaType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), textContent.stringValue());
    }

    @Test
    public void testSetBodyAndGetText() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetBodyAndGetText", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), textContent.stringValue());
    }

    @Test
    public void testSetBodyAndGetXml() {
        BXML xmlContent = XMLUtils.parse("<name>ballerina</name>");
        BValue[] args = {xmlContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetBodyAndGetXml", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "<name>ballerina</name>");
    }

    @Test
    public void testSetBodyAndGetJson() {
        BValue jsonContent = JsonParser.parse("{'code':'123'}");
        BValue[] args = {jsonContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetBodyAndGetJson", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).get("code").stringValue(), "123");
    }

    @Test
    public void testSetBodyAndGetByteArray() {
        String content = "ballerina";
        BValueArray byteContent = new BValueArray(content.getBytes());
        BValue[] args = {byteContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetBodyAndGetByteArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), content);
    }

    @Test
    public void testSetBodyAndGetByteChannel() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "Hello Ballerina!");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel};
            BValue[] returns = BRunUtil.invoke(compileResult, "testSetBodyAndGetByteChannel", args);
            Assert.assertEquals(returns.length, 1);
            BMap<String, BValue> returnByteChannelStruct = (BMap<String, BValue>) returns[0];
            Channel channel = (Channel) returnByteChannelStruct.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
            Assert.assertEquals(StringUtils.getStringFromInputStream(channel.getInputStream()).getValue(),
                                "Hello Ballerina!");
        } catch (IOException e) {
            log.error("Error occurred in testSetBodyAndGetByteChannel", e.getMessage());
        }
    }

    @Test
    public void testSetMediaTypeToEntity() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetMediaTypeToEntity");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/my-custom-type+json");
    }

    @Test
    public void testSetMediaTypeAndGetValueAsHeader() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetMediaTypeAndGetValueAsHeader");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/my-custom-type+json");
    }

    @Test
    public void testSetHeaderAndGetMediaType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAndGetMediaType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "text/plain; charset=UTF-8");
    }

    @Test
    public void testSetContentDispositionToEntity() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetContentDispositionToEntity");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "inline;name=\"test\";filename=\"test_file.xml\"");
    }

    @Test
    public void testSetContentDispositionAndGetValueAsHeader() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetContentDispositionAndGetValueAsHeader");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "inline;name=\"test\";filename=\"test_file.xml\"");
    }

    @Test
    public void testSetHeaderAndGetContentDisposition() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAndGetContentDisposition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "inline;name=\"test\";filename=\"test_file.xml\"");
    }

    @Test
    public void testSetContentLengthToEntity() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetContentLengthToEntity");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "45555");
    }

    @Test
    public void testSetContentLengthAndGetValueAsHeader() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetContentLengthAndGetValueAsHeader");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "45555");
    }

    @Test
    public void testSetContentIdToEntity() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetContentIdToEntity");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "test-id");
    }

    @Test
    public void testSetContentIdAndGetValueAsHeader() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetContentIdAndGetValueAsHeader");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "test-id");
    }

    @Test
    public void testGetAnyStreamAsString() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "{'code':'123'}");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel, StringUtils.fromString("application/json")};
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetAnyStreamAsString", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "{'code':'123'}");
        } catch (IOException e) {
            log.error("Error occurred in testGetAnyStreamAsString", e.getMessage());
        }
    }

    @Test(description = "Once an entity body has been constructed as json, get the body as a byte[]")
    public void testByteArrayWithContentType() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "{'code':'123'}");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel, StringUtils.fromString("application/json")};
            BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayWithContentType", args);
            Assert.assertEquals(returns.length, 1);
            //Change this accordingly when https://github.com/ballerina-platform/ballerina-lang/issues/10079 is fixed
            Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "{\"code\":\"123\"}",
                                "Entity body is not properly set");
        } catch (IOException e) {
            log.error("Error occurred in testByteArrayWithContentType", e.getMessage());
        }
    }

    @Test(description = "Once the entity body has been constructed as json and a charset value has been included " +
            "in the content-type, get the body as a byte[]")
    public void testByteArrayWithCharset() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "{\"test\":\"菜鸟驿站\"}");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel, StringUtils.fromString("application/json; charset=utf8")};
            BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayWithContentType", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(new String(((BValueArray) returns[0]).getBytes()), "{\"test\":\"菜鸟驿站\"}",
                                "Entity body is not properly set");
        } catch (IOException e) {
            log.error("Error occurred in testByteArrayWithCharset", e.getMessage());
        }
    }

    @Test(description = "Test whether the body parts in a multipart entity can be retrieved as a byte channel")
    public void testGetBodyPartsAsChannel() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetBodyPartsAsChannel");
        Assert.assertEquals(returns.length, 1);
        BMap<String, BValue> byteChannel = (BMap<String, BValue>) returns[0];
        Channel channel = (Channel) byteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        try {
            List<MIMEPart> mimeParts = MultipartDecoder.decodeBodyParts("multipart/mixed; " +
                    "boundary=e3a0b9ad7b4e7cdt", channel.getInputStream());
            Assert.assertEquals(mimeParts.size(), 4);
            BObject bodyPart = Util.createEntityObject();
            validateBodyPartContent(mimeParts, bodyPart);
        } catch (MimeTypeParseException e) {
            log.error("Error occurred while testing mulitpart/mixed encoding", e.getMessage());
        } catch (IOException e) {
            log.error("Error occurred while decoding binary part", e.getMessage());
        }
    }

    @Test(description = "Test whether an error is returned when trying to extract body parts from an " +
            "entity that has discrete media type content")
    public void getBodyPartsFromDiscreteTypeEntity() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getBodyPartsFromDiscreteTypeEntity");
        Assert.assertEquals(returns.length, 1);
        verifyMimeError(returns[0], "Entity body is not a type of composite media type. " +
                "Received content-type : application/json");
    }

    @Test(description = "Test whether an error is returned when trying convert body parts as a " +
            "byte channel when the actual content is not composite media type")
    public void getChannelFromParts() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getChannelFromParts");
        Assert.assertEquals(returns.length, 1);
        verifyMimeError(returns[0], "Entity doesn't contain body parts");
    }

    @Test(description = "Test whether an error is returned when trying to retrieve a byte channel from a multipart" +
            "entity")
    public void getChannelFromMultipartEntity() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getChannelFromMultipartEntity");
        Assert.assertEquals(returns.length, 1);
        verifyMimeError(returns[0], "Byte channel is not available since payload " +
                "contains a set of body parts");
    }

    @Test(description = "Test whether the string body is retrieved from the cache")
    public void getAnyStreamAsStringFromCache() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "{'code':'123'}");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel, StringUtils.fromString("application/json")};
            BValue[] returns = BRunUtil.invoke(compileResult, "getAnyStreamAsStringFromCache", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "{'code':'123'}{'code':'123'}");
        } catch (IOException e) {
            log.error("Error occurred in getAnyStreamAsStringFromCache", e.getMessage());
        }
    }

    @Test(description = "Test whether the xml content can be constructed properly once the body has been " +
            "retrieved as a byte array first")
    public void testXmlWithByteArrayContent() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "<name>Ballerina xml content</name>");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel, StringUtils.fromString("application/xml; charset=utf8")};
            BValue[] returns = BRunUtil.invoke(compileResult, "testXmlWithByteArrayContent", args);
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "<name>Ballerina xml content</name>");
        } catch (IOException e) {
            log.error("Error occurred in testXmlWithByteArrayContent", e.getMessage());
        }
    }

    @Test(description = "Test whether an error is returned when trying to construct body parts from an invalid " +
            "channel")
    public void getPartsFromInvalidChannel() {
        try {
            File file = getTemporaryFile("testFile", ".tmp", "test file");
            BObject byteChannel = ValueCreator.createObjectValue(PROTOCOL_IO_PKG_ID,
                                                                 READABLE_BYTE_CHANNEL_STRUCT);
            byteChannel.addNativeData(IOConstants.BYTE_CHANNEL_NAME,
                                            EntityBodyHandler.getByteChannelForTempFile(file.getAbsolutePath()));
            Object[] args = {byteChannel, StringUtils.fromString("multipart/form-data")};
            BValue[] returns = BRunUtil.invoke(compileResult, "getPartsFromInvalidChannel", args);
            Assert.assertEquals(returns.length, 1);
            verifyMimeError(returns[0], "Error occurred while extracting body parts from entity: Missing start " +
                    "boundary");
        } catch (IOException e) {
            log.error("Error occurred in getPartsFromInvalidChannel", e.getMessage());
        }
    }
}
