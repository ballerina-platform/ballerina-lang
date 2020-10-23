/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.services.nativeimpl.request;

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.core.model.util.StringUtils;
import org.ballerinalang.mime.util.MimeConstants;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.ResponseReader;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.stdlib.utils.TestEntityUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.CHARSET;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.HEADERS_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.mime.util.MimeUtil.isNotNullAndEmpty;
import static org.ballerinalang.net.http.HttpConstants.HTTP_HEADERS;
import static org.ballerinalang.net.http.ValueCreatorUtils.createEntityObject;
import static org.ballerinalang.net.http.ValueCreatorUtils.createRequestObject;
import static org.ballerinalang.stdlib.utils.TestEntityUtils.enrichEntityWithDefaultMsg;
import static org.ballerinalang.stdlib.utils.TestEntityUtils.enrichTestEntity;

/**
 * Test cases for ballerina/http request success native functions.
 */
public class RequestNativeFunctionSuccessTest {

    private CompileResult compileResult;
    private static final int TEST_PORT = 9090;

    @BeforeClass
    public void setup() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src", "services", "nativeimpl",
                                    "request");
        compileResult = BCompileUtil.compile(sourceRoot.resolve("in-request-native-function.bal").toString());
    }

    @Test
    public void testContentType() {
        BObject inRequest = createRequestObject();
        Object[] inputArg = {inRequest, BStringUtils.fromString("application/x-custom-type+json")};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testContentType", inputArg);
        Assert.assertNotNull(returnVals[0]);
        Assert.assertEquals(((BString) returnVals[0]).value(), "application/x-custom-type+json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddHeader() {
        String headerName = "header1";
        String headerValue = "abc, xyz";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testAddHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        HttpHeaders httpHeaders = (HttpHeaders) ((BMap) returnVals[0]).getNativeData(HTTP_HEADERS);
        Assert.assertEquals(httpHeaders.getAll(headerName).get(0), "1stHeader");
        Assert.assertEquals(httpHeaders.getAll(headerName).get(1), headerValue);
    }

    @Test(description = "Test addHeader function within a service")
    public void testServiceAddHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/addheader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(ResponseReader.getReturnValue(response));
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get(key).stringValue(), value);
    }

    @Test(description = "Test getBinaryPayload method of the request")
    public void testGetBinaryPayloadMethod() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "ballerina";
        enrichTestEntity(entity, OCTET_STREAM, payload);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetBinaryPayload", new Object[]{ inRequest });

        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(new String(((BValueArray) returnVals[0]).getBytes()), payload);
    }

    @Test(description = "Test getBinaryPayload method behaviour over non-blocking execution")
    public void testGetBinaryPayloadNonBlocking() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "ballerina";
        enrichEntityWithDefaultMsg(entity, payload);
        TestEntityUtils.enrichTestMessageHeaders(inRequest, OCTET_STREAM);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetBinaryPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(new String(((BValueArray) returnVals[0]).getBytes()), payload);
    }

    @Test(description = "Enable this once the getContentLength() is added back in http package")
    public void testGetContentLength() {
        BObject inRequest = createRequestObject();
        String payload = "ballerina";
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(HttpHeaderNames.CONTENT_LENGTH.toString(), payload.length());
        inRequest.addNativeData(HTTP_HEADERS, httpHeaders);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetContentLength", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(payload.length(), ((BString) returnVals[0]).intValue());
    }

    @Test
    public void testGetHeader() {
        BObject inRequest = createRequestObject();
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("", HttpConstants.HTTP_METHOD_GET);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);

        BObject entity = createEntityObject();
        HttpUtil.populateInboundRequest(inRequest, entity, inRequestMsg);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetHeader", new Object[]{inRequest,
                BStringUtils.fromString(HttpHeaderNames.CONTENT_TYPE.toString())});
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String path = "/hello/getHeader";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("value").stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        BObject inRequest = createRequestObject();
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage("", HttpConstants.HTTP_METHOD_GET);
        HttpHeaders headers = inRequestMsg.getHeaders();
        headers.set("test-header", APPLICATION_FORM);
        headers.add("test-header", TEXT_PLAIN);

        BObject entity = createEntityObject();
        HttpUtil.populateInboundRequest(inRequest, entity, inRequestMsg);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetHeaders", new Object[]{inRequest,
                BStringUtils.fromString("test-header")});
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BValueArray) returnVals[0]).getString(0), APPLICATION_FORM);
        Assert.assertEquals(((BValueArray) returnVals[0]).getString(1), TEXT_PLAIN);
    }

    @Test
    public void testGetJsonPayload() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "{'code':'123'}";
        enrichTestEntity(entity, APPLICATION_JSON, payload);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetJsonPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        Assert.assertEquals(((BMap) returnVals[0]).get("code").stringValue(), "123");
    }

    @Test
    public void testGetJsonPayloadNonBlocking() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "{'code':'123'}";
        enrichEntityWithDefaultMsg(entity, payload);
        TestEntityUtils.enrichTestMessageHeaders(entity, APPLICATION_JSON);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetJsonPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        Assert.assertEquals(((BMap) returnVals[0]).get("code").stringValue(), "123");
    }

    @Test(description = "Test GetJsonPayload function within a service")
    public void testServiceGetJsonPayload() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/getJsonPayload";
        String jsonString = "{\"" + key + "\":\"" + value + "\"}";
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_JSON);
        HTTPTestRequest inRequestMsg =
                MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, jsonString);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(JsonParser.parse(ResponseReader.getReturnValue(response)).stringValue(), value);
    }

    @Test
    public void testGetTextPayload() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "ballerina";
        enrichTestEntity(entity, TEXT_PLAIN, payload);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetTextPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetTextPayloadNonBlocking() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "ballerina";
        enrichEntityWithDefaultMsg(entity, payload);
        TestEntityUtils.enrichTestMessageHeaders(entity, TEXT_PLAIN);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetTextPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test getTextPayload method with JSON payload")
    public void testGetTextPayloadMethodWithJsonPayload() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "{\"code\":\"123\"}";
        enrichEntityWithDefaultMsg(entity, payload);
        TestEntityUtils.enrichTestMessageHeaders(entity, APPLICATION_JSON);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetTextPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test getTextPayload method with Xml payload")
    public void testGetTextPayloadMethodWithXmlPayload() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "<name>ballerina</name>";
        enrichEntityWithDefaultMsg(entity, payload);
        TestEntityUtils.enrichTestMessageHeaders(entity, APPLICATION_XML);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetTextPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetTextPayload function within a service")
    public void testServiceGetTextPayload() {
        String value = "ballerina";
        String path = "/hello/GetTextPayload";
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
        HTTPTestRequest inRequestMsg =
                MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, value);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), value);
    }

    @Test
    public void testGetXmlPayload() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "<name>ballerina</name>";
        enrichTestEntity(entity, APPLICATION_XML, payload);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetXmlPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test
    public void testGetXmlPayloadNonBlocking() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();

        String payload = "<name>ballerina</name>";
        enrichEntityWithDefaultMsg(entity, payload);
        TestEntityUtils.enrichTestMessageHeaders(entity, APPLICATION_XML);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetXmlPayload", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test GetXmlPayload function within a service")
    public void testServiceGetXmlPayload() {
        String value = "ballerina";
        String path = "/hello/GetXmlPayload";
        String bxmlItemString = "<name>ballerina</name>";
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_XML);
        HTTPTestRequest inRequestMsg =
                MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, bxmlItemString);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), value);
    }

    @Test
    public void testGetMethod() {
        String path = "/hello/11";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                org.ballerinalang.model.util.StringUtils
                        .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()),
                HttpConstants.HTTP_METHOD_GET);
    }

    @Test
    public void testGetRequestURL() {
        String path = "/hello/12";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                org.ballerinalang.model.util.StringUtils
                        .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), path);
    }

    @Test(description = "Test GetByteChannel function within a service. Send a json content as a request " +
            "and then get a byte channel from the Request and set that ByteChannel as the response content")
    public void testServiceGetByteChannel() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/GetByteChannel";
        String jsonString = "{\"" + key + "\":\"" + value + "\"}";
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("Content-Type", APPLICATION_JSON);
        HTTPTestRequest inRequestMsg =
                MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers, jsonString);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get(key).stringValue(), value);
    }

    @Test(description = "Test RemoveAllHeaders function within a service")
    public void testServiceRemoveAllHeaders() {
        String path = "/hello/RemoveAllHeaders";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("value").stringValue(), "value is null");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetHeader() {
        String headerName = "lang";
        String headerValue = "ballerina; a=6";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSetHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        HttpHeaders httpHeaders = (HttpHeaders) ((BMap) returnVals[0]).getNativeData(HTTP_HEADERS);
        Assert.assertEquals(httpHeaders.get(headerName), headerValue);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetMultipleHeader() {
        String headerName = "team";
        String headerValue = "lang, composer";
        BString key = new BString(headerName);
        BString value = new BString(headerValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSetHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        HttpHeaders httpHeaders = (HttpHeaders) ((BMap) returnVals[0]).getNativeData(HTTP_HEADERS);
        Assert.assertEquals(httpHeaders.get(headerName), headerValue);
    }

    @Test(description = "Test SetHeader function within a service")
    public void testServiceSetHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/setHeader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("value").stringValue(), value);
    }

    @Test
    public void testSetJsonPayload() {
        BValue value = JsonParser.parse("{'name':'wso2'}");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(REQUEST_ENTITY_FIELD.getValue());
        BMap<BString, Object> bJson = (BMap<BString, Object>) TestEntityUtils.getMessageDataSource(
                entity);
        Assert.assertEquals(bJson.get(BStringUtils.fromString("name")).toString(), "wso2",
                            "Payload is not set properly");
    }

    @Test(enabled = false, description = "Test SetJsonPayload function within a service")
    public void testServiceSetJsonPayload() {
        String value = "ballerina";
        String path = "/hello/SetJsonPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("lang").stringValue(), value);
    }

    @Test
    public void testSetStringPayload() {
        BString value = new BString("Ballerina");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSetStringPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(REQUEST_ENTITY_FIELD.getValue());
        String stringValue = (String) TestEntityUtils.getMessageDataSource(entity);
        Assert.assertEquals(stringValue, "Ballerina", "Payload is not set properly");
    }

    @Test(description = "Test SetStringPayload function within a service")
    public void testServiceSetStringPayload() {
        String value = "ballerina";
        String path = "/hello/SetStringPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("lang").stringValue(), value);
    }

    @Test
    public void testSetXmlPayload() {
        BXML value = XMLFactory.parse("<name>Ballerina</name>");
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSetXmlPayload", new Object[]{ value });

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(REQUEST_ENTITY_FIELD.getValue());
        BXML xmlValue = (xmlValue) TestEntityUtils.getMessageDataSource(entity);
        Assert.assertEquals(xmlValue.getTextValue(), "Ballerina", "Payload is not set properly");
    }

    @Test(description = "Test SetXmlPayload function within a service")
    public void testServiceSetXmlPayload() {
        String value = "Ballerina";
        String path = "/hello/SetXmlPayload/";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("lang").stringValue(), value);
    }

    @Test(description = "Test setBinaryPayload() function within a service")
    public void testServiceSetBinaryPayload() {
        String value = "Ballerina";
        String path = "/hello/SetBinaryPayload/";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("lang").stringValue(), value);
    }

    @Test(description = "Test getBinaryPayload() function within a service")
    public void testServiceGetBinaryPayload() {
        String payload = "ballerina";
        String path = "/hello/GetBinaryPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, payload);
        HttpCarbonMessage response = Services.invoke(TEST_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                org.ballerinalang.model.util.StringUtils
                        .getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), payload);
    }

    @Test(description = "Test setBinaryPayload() function")
    public void testSetBinaryPayload() {
        BValueArray value = new BValueArray("Ballerina".getBytes());
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSetBinaryPayload", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(REQUEST_ENTITY_FIELD.getValue());
        BArray messageDataSource = (BArray) TestEntityUtils.getMessageDataSource(entity);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        messageDataSource.serialize(outStream);
        Assert.assertEquals(new String(outStream.toByteArray(), StandardCharsets.UTF_8), "Ballerina",
                            "Payload is not set properly");
    }

    @Test(description = "Test setEntityBody() function")
    public void testSetEntityBody() throws IOException {
        File file = File.createTempFile("test", ".json");
        file.deleteOnExit();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write("{'name':'wso2'}");
        bufferedWriter.close();

        BValue[] inputArg = {new BString(file.getAbsolutePath()), new BString(APPLICATION_JSON)};
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testSetEntityBody", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(REQUEST_ENTITY_FIELD.getValue());
        /*
         * BMap<String, BValue> returnFileStruct = (BMap<String, BValue>) entity.get(OVERFLOW_DATA_INDEX);
         *
         * String returnJsonValue = new String(Files.readAllBytes(Paths.get(returnFileStruct.getStringField(0))),
         * UTF_8);
         */
        BValue bJson = constructJsonDataSource(entity);
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("name").stringValue(), "wso2", "Payload is not set properly");

    }

    /**
     * Construct JsonDataSource from the underneath byte channel which is associated with the entity object.
     *
     * @param entityObj Represent an entity object
     * @return BJSON data source which is kept in memory
     */
    private static BRefType<?> constructJsonDataSource(BMap<String, BValue> entityObj) {
        try {
            Channel byteChannel = getByteChannel(entityObj);
            if (byteChannel == null) {
                return null;
            }
            BRefType<?> jsonData = constructJsonDataSource(entityObj, byteChannel.getInputStream());
            byteChannel.close();
            return jsonData;
        } catch (IOException e) {
            throw new BallerinaException("Error occurred while closing connection", e);
        }
    }

    private static Channel getByteChannel(BMap<String, BValue> entityObj) {
        return entityObj.getNativeData(ENTITY_BYTE_CHANNEL) != null ? (Channel) entityObj.getNativeData
                (ENTITY_BYTE_CHANNEL) : null;
    }

    private static BRefType<?> constructJsonDataSource(BMap<String, BValue> entity, InputStream inputStream) {
        BRefType<?> jsonData;
        String contentTypeValue = getHeaderValue(entity, HttpHeaderNames.CONTENT_TYPE.toString());
        if (isNotNullAndEmpty(contentTypeValue)) {
            String charsetValue = MimeUtil.getContentTypeBParamValue(contentTypeValue, CHARSET);
            if (isNotNullAndEmpty(charsetValue)) {
                jsonData = JsonParser.parse(inputStream, charsetValue);
            } else {
                jsonData = JsonParser.parse(inputStream);
            }
        } else {
            jsonData = JsonParser.parse(inputStream);
        }
        return jsonData;
    }

    private static String getHeaderValue(BMap<String, BValue> bodyPart, String headerName) {
        BMap headers = (BMap) bodyPart.get(HEADERS_MAP_FIELD.getValue());
        if (headers == null) {
            return null;
        }
        BValueArray headerValues = (BValueArray) headers.get(headerName.toLowerCase());
        if (headerValues == null || headerValues.size() == 0) {
            return null;
        }
        return headerValues.getString(0);
    }

    @Test
    public void testSetPayloadAndGetText() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetPayloadAndGetText", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), textContent.stringValue());
    }

    @Test
    public void testAccessingPayloadAsTextAndJSON() {
        BCompileUtil.compile("test-src/services/nativeimpl/request/get_request_as_string_and_json.bal");
        String payload = "{ \"foo\" : \"bar\"}";
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), MimeConstants.APPLICATION_JSON);
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/foo/bar", "POST", headers, payload);
        HttpCarbonMessage responseMsg = Services.invoke(9093, requestMsg);
        Assert.assertEquals(ResponseReader.getReturnValue(responseMsg), "bar");
    }

    @Test
    public void testAddCookies() {
        String headerName = "Cookie";
        String headerValue = "SID1=31d4d96e407aad42; SID3=782638747668bce72; SID2=2638747623468bce72";
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testAddCookies", new Object[]{inRequest});
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        HttpHeaders httpHeaders = (HttpHeaders) ((BMap) returnVals[0]).getNativeData(HTTP_HEADERS);
        Assert.assertEquals(httpHeaders.getAll(headerName).get(0), headerValue);
    }

    @Test
    public void testGetCookies() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetCookies", new Object[]{inRequest});
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "No cookie objects in the Return Values");
        Assert.assertTrue(returnVals.length == 1);
    }

    @Test
    public void testGetCookiesWithEmptyValue() {
        BObject inRequest = createRequestObject();
        BObject entity = createEntityObject();
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetCookiesWithEmptyValue",
                new Object[]{inRequest});
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null,
                "No cookie objects in the Return Values");
        Assert.assertEquals(returnVals.length, 1);
    }
}
