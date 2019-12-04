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
package org.ballerinalang.stdlib.services.nativeimpl.response;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.axiom.om.OMNode;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
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

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.RESPONSE_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.net.http.ValueCreatorUtils.createEntityObject;
import static org.ballerinalang.net.http.ValueCreatorUtils.createResponseObject;
import static org.ballerinalang.stdlib.utils.TestEntityUtils.enrichEntityWithDefaultMsg;
import static org.ballerinalang.stdlib.utils.TestEntityUtils.enrichTestEntity;
import static org.ballerinalang.stdlib.utils.TestEntityUtils.enrichTestEntityHeaders;

/**
 * Test cases for ballerina/http inbound inResponse success native functions.
 */
public class ResponseNativeFunctionSuccessTest {

    private CompileResult result;
    private static final int MOCK_ENDPOINT_PORT = 9090;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/services/nativeimpl/response/in-response-native-function.bal";
        result = BCompileUtil.compile(sourceFilePath);
    }

    @Test
    public void testContentType() {
        ObjectValue response = createResponseObject();
        BValue[] returnVals = BRunUtil.invoke(result, "testContentType",
                                              new Object[]{ response, "application/x-custom-type+json" });
        Assert.assertNotNull(returnVals[0]);
        Assert.assertEquals(((BString) returnVals[0]).value(), "application/x-custom-type+json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddHeader() {
        ObjectValue outResponse = createResponseObject();
        String headerName = "header1";
        String headerValue = "abc, xyz";
        BValue[] returnVals = BRunUtil.invoke(result, "testAddHeader",
                                              new Object[]{ outResponse, headerName, headerValue });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entityStruct =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(RESPONSE_ENTITY_FIELD);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertEquals(returnHeaders.get(headerName), headerValue);
    }

    @Test(description = "Test addHeader function within a service")
    public void testServiceAddHeader() {
        String key = "lang";
        String value = "ballerina";
        String path = "/hello/addheader/" + key + "/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get(key).stringValue(), value);
    }

    @Test
    public void testGetBinaryPayloadMethod() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "ballerina";
        enrichTestEntity(entity, OCTET_STREAM, payload);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", new Object[]{ inResponse });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(new String(((BValueArray) returnVals[0]).getBytes()), payload);
    }

    @Test
    public void testGetBinaryPayloadNonBlocking() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "ballerina";
        enrichEntityWithDefaultMsg(entity, payload);
        enrichTestEntityHeaders(entity, OCTET_STREAM);
        inResponse.set(REQUEST_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetBinaryPayload", new Object[]{ inResponse });

        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(new String(((BValueArray) returnVals[0]).getBytes()), payload);
    }

    @Test
    public void testGetContentLength() {
        ObjectValue inResponse = createResponseObject();
        HttpCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(false);

        String payload = "ballerina";
        inResponseMsg.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(payload.length()));
        inResponseMsg.setHttpStatusCode(200);
        HttpUtil.addCarbonMsg(inResponse, inResponseMsg);

        ObjectValue entity = createEntityObject();
        HttpUtil.populateInboundResponse(inResponse, entity, inResponseMsg);

        BValue[] returnVals = BRunUtil.invoke(result, "testGetContentLength", new Object[]{ inResponse });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(String.valueOf(payload.length()), returnVals[0].stringValue());
    }

    @Test
    public void testGetHeader() {
        ObjectValue inResponse = createResponseObject();
        HttpCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(false);
        inResponseMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);
        inResponseMsg.setHttpStatusCode(200);
        HttpUtil.addCarbonMsg(inResponse, inResponseMsg);

        ObjectValue entity = createEntityObject();
        HttpUtil.populateInboundResponse(inResponse, entity, inResponseMsg);

        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeader",
                                              new Object[]{ inResponse, HttpHeaderNames.CONTENT_TYPE.toString() });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_FORM);
    }

    @Test(description = "Test GetHeader function within a service")
    public void testServiceGetHeader() {
        String value = "test-header-value";
        String path = "/hello/getHeader/" + "test-header-name" + "/" + value;
        HTTPTestRequest inRequest = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequest);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("value").stringValue(), value);
    }

    @Test(description = "Test GetHeaders function within a function")
    public void testGetHeaders() {
        ObjectValue inResponse = createResponseObject();
        HttpCarbonMessage inResponseMsg = HttpUtil.createHttpCarbonMessage(false);
        HttpHeaders headers = inResponseMsg.getHeaders();
        headers.set("test-header", APPLICATION_FORM);
        headers.add("test-header", TEXT_PLAIN);

        inResponseMsg.setHttpStatusCode(200);
        ObjectValue entity = createEntityObject();
        HttpUtil.populateInboundResponse(inResponse, entity, inResponseMsg);

        BValue[] returnVals = BRunUtil.invoke(result, "testGetHeaders", new Object[]{ inResponse, "test-header" });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BValueArray) returnVals[0]).getString(0), APPLICATION_FORM);
        Assert.assertEquals(((BValueArray) returnVals[0]).getString(1), TEXT_PLAIN);
    }

    @Test
    public void testGetJsonPayload() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "{'code':'123'}";
        enrichTestEntity(entity, APPLICATION_JSON, payload);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", new Object[]{ inResponse });

        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        Assert.assertEquals(((BMap) returnVals[0]).get("code").stringValue(), "123");
    }


    @Test
    public void testGetJsonPayloadNonBlocking() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "{'code':'123'}";
        enrichEntityWithDefaultMsg(entity, payload);
        enrichTestEntityHeaders(entity, APPLICATION_JSON);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetJsonPayload", new Object[]{ inResponse });

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        Assert.assertEquals(((BMap) returnVals[0]).get("code").stringValue(), "123");
    }

    @Test(description = "Test GetJsonPayload function within a service")
    public void testServiceGetJsonPayload() {
        String value = "ballerina";
        String path = "/hello/getJsonPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage responseMsg = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(responseMsg, "Response message not found");
        BValue json = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(json.stringValue(), value);
    }

    @Test
    public void testGetTextPayload() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "ballerina";
        enrichTestEntity(entity, TEXT_PLAIN, payload);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetTextPayload", new Object[]{ inResponse });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    public void testGetTextPayloadNonBlocking() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "ballerina";
        enrichEntityWithDefaultMsg(entity, payload);
        enrichTestEntityHeaders(entity, TEXT_PLAIN);
        enrichTestEntity(entity, TEXT_PLAIN, payload);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetTextPayload", new Object[]{ inResponse });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test GetTextPayload function within a service")
    public void testServiceGetTextPayload() {
        String value = "ballerina";
        String path = "/hello/GetTextPayload/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream()), value);
    }

    @Test
    public void testGetXmlPayload() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "<name>ballerina</name>";
        enrichTestEntity(entity, APPLICATION_XML, payload);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", new Object[]{ inResponse });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test
    public void testGetXmlPayloadNonBlocking() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "<name>ballerina</name>";
        enrichEntityWithDefaultMsg(entity, payload);
        enrichTestEntityHeaders(entity, APPLICATION_XML);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetXmlPayload", new Object[]{ inResponse });

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(((BXML) returnVals[0]).getTextValue().stringValue(), "ballerina");
    }

    @Test(description = "Test GetXmlPayload function within a service")
    public void testServiceGetXmlPayload() {
        String value = "ballerina";
        String path = "/hello/GetXmlPayload";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        String returnvalue =
                StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertEquals(returnvalue, value);
    }

    @Test
    public void testForwardMethod() {
        String path = "/hello/11";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
    }

    @Test(description = "Test getTextPayload method with JSON payload")
    public void testGetTextPayloadMethodWithJsonPayload() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "{\"code\":\"123\"}";
        enrichTestEntity(entity, APPLICATION_JSON, payload);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(result, "testGetTextPayload", new Object[]{ inResponse });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test(description = "Test getTextPayload method with Xml payload")
    public void testGetTextPayloadMethodWithXmlPayload() {
        ObjectValue inResponse = createResponseObject();
        ObjectValue entity = createEntityObject();

        String payload = "<name>ballerina</name>";
        enrichTestEntity(entity, APPLICATION_XML, payload);
        inResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
        BValue[] returnVals = BRunUtil.invoke(result, "testGetTextPayload", new Object[]{ inResponse });

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), payload);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveHeader() {
        ObjectValue outResponse = createResponseObject();
        String expect = "Expect";
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveHeader", new Object[]{ outResponse, expect });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entityStruct =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(RESPONSE_ENTITY_FIELD);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertNull(returnHeaders.get(expect));
    }

    @Test(description = "Test RemoveHeader function within a service")
    public void testServiceRemoveHeader() {
        String value = "x-www-form-urlencoded";
        String path = "/hello/RemoveHeader/Content-Type/" + value;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("value").stringValue(), "value is null");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllHeaders() {
        ObjectValue outResponse = createResponseObject();
        ObjectValue entity = createEntityObject();
        String expect = "Expect";
        String range = "Range";

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(expect, "100-continue");
        httpHeaders.add(expect, "bytes=500-999");
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);

        outResponse.set(RESPONSE_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(result, "testRemoveAllHeaders", new Object[]{ outResponse });
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entityStruct =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(RESPONSE_ENTITY_FIELD);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertNull(returnHeaders.get(expect));
        Assert.assertNull(returnHeaders.get(range));
    }

    @Test(description = "Test RemoveAllHeaders function within a service")
    public void testServiceRemoveAllHeaders() {
        String path = "/hello/RemoveAllHeaders";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        Assert.assertEquals(((BMap) bJson).get("value").stringValue(), "value is null");
    }

    @Test
    public void testRespondMethod() {
        String path = "/hello/11";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSetHeader() {
        String range = "Range";
        String rangeValue = "bytes=500-999; a=4";
        BString key = new BString(range);
        BString value = new BString(rangeValue);
        BValue[] inputArg = {key, value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entityStruct =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(RESPONSE_ENTITY_FIELD);
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertEquals(returnHeaders.get(range), rangeValue);
    }

    @Test
    public void testSetJsonPayload() {
        BValue value = JsonParser.parse("{'name':'wso2'}");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetJsonPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(RESPONSE_ENTITY_FIELD);
        Object bJson = TestEntityUtils.getMessageDataSource(entity);
        Assert.assertEquals(((MapValueImpl) bJson).get("name"), "wso2", "Payload is not set properly");    }

    @Test
    public void testSetReasonPhase() {
        String phase = "ballerina";
        String path = "/hello/12/" + phase;
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(response.getProperty(HttpConstants.HTTP_REASON_PHRASE), phase);
    }

    @Test
    public void testSetStatusCode() {
        String path = "/hello/13";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_GET);
        HttpCarbonMessage response = Services.invoke(MOCK_ENDPOINT_PORT, inRequestMsg);

        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals((int) response.getHttpStatusCode(), 203);
    }

    @Test
    public void testSetStringPayload() {
        BString value = new BString("Ballerina");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetStringPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(RESPONSE_ENTITY_FIELD);
        String stringValue = (String) TestEntityUtils.getMessageDataSource(entity);
        Assert.assertEquals(stringValue, "Ballerina", "Payload is not set properly");
    }

    @Test
    public void testSetXmlPayload() {
        OMNode omNode = (OMNode) XMLFactory.parse("<name>Ballerina</name>").value();
        BXMLItem value = new BXMLItem(omNode);
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testSetXmlPayload", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entity =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(RESPONSE_ENTITY_FIELD);
        Object xmlValue = TestEntityUtils.getMessageDataSource(entity);
        Assert.assertEquals(((XMLItem) xmlValue).getTextValue(), "Ballerina", "Payload is not set properly");
    }

    @Test
    public void testSetPayloadAndGetText() {
        BString textContent = new BString("Hello Ballerina !");
        BValue[] args = {textContent};
        BValue[] returns = BRunUtil.invoke(result, "testSetPayloadAndGetText", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), textContent.stringValue());
    }
}
