/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.services.basics;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.values.BString;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.core.model.util.StringUtils;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.ResponseReader;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;

/**
 * Service/Resource dispatchers test class.
 */
public class ServiceTest {

    private static final int TEST_ENDPOINT_1_PORT = 9090;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compile("test-src/services/echo-service.bal");
        negativeResult = BCompileUtil.compile("test-src/services/service-negative.bal");

        if (compileResult.getErrorCount() > 0) {
            throw new IllegalStateException(compileResult.toString());
        }
    }

    @Test
    public void testServiceDispatching() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/message", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        Assert.assertNotNull(responseMsg);
        // TODO: Improve with more assets
    }

    @Test
    public void testMostSpecificBasePathIdentificationWithDuplicatedPath() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/message/echo/message", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload,
                            "no matching resource found for path : /echo/message/echo/message , method : GET");
    }

    @Test
    public void testMostSpecificBasePathIdentificationWithUnmatchedBasePath() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/abcd/message/echo/message", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "no matching service found for path : /abcd/message/echo/message");
    }

    @Test
    public void testServiceDispatchingWithWorker() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/message_worker", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        Assert.assertNotNull(responseMsg);
    }

    //TODO: validate the dispatching logic and fix this
    @Test(description = "Test for service availability check")
    public void testServiceAvailabilityCheck() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/foo/message", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "no matching service found for path : /foo/message");
    }

    @Test(description = "Test for resource availability check")
    public void testResourceAvailabilityCheck() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/bar", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "no matching resource found for path : /echo/bar , method : GET");
    }

    @Test
    public void testSetString() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("Content-Type", TEXT_PLAIN);
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/setString", "POST", headers, null);
        requestMsg.waitAndReleaseAllEntities();
        requestMsg.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer("hello".getBytes())));
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg);
    }

    @Test(dependsOnMethods = "testSetString")
    public void testGetString() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        Assert.assertNotNull(responseMsg);
        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, "hello");
    }

    @Test(description = "Test using constant as annotation attribute value")
    public void testConstantValueAsAnnAttributeVal() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/constantPath", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "constant path test");
    }

    @Test(description = "Test getString after setting string")
    public void testGetStringAfterSetString() {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("Content-Type", TEXT_PLAIN);
        HTTPTestRequest setStringrequestMsg = MessageUtils
                .generateHTTPMessage("/echo/setString", "POST", headers, null);
        String stringresponseMsgPayload = "hello";
        setStringrequestMsg.waitAndReleaseAllEntities();
        setStringrequestMsg.addHttpContent(
                new DefaultLastHttpContent(Unpooled.wrappedBuffer(stringresponseMsgPayload.getBytes())));
        Services.invoke(TEST_ENDPOINT_1_PORT, setStringrequestMsg);

        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/getString", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        Assert.assertNotNull(responseMsg);

        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertNotNull(responseMsgPayload);
        Assert.assertEquals(responseMsgPayload, stringresponseMsgPayload);
    }

    @Test(description = "Test remove headers native function")
    public void testRemoveHeadersNativeFunction() {
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage("/echo/removeHeaders", "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);
        Assert.assertNotNull(responseMsg);

        Assert.assertNull(responseMsg.getHeader("header1"));
        Assert.assertNull(responseMsg.getHeader("header2"));
        Assert.assertNull(responseMsg.getHeader("header3"));
    }

    @Test(description = "Test GetFormParams Native Function")
    public void testGetFormParamsNativeFunction() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST", "firstName=WSO2&team=BalDance");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        BValue bJson = JsonParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);
        BMap<String, BValue> jsonObject = (BMap<String, BValue>) bJson;
        Assert.assertEquals(jsonObject.get("Name").stringValue(), "WSO2", "Name variable not set properly.");
        Assert.assertEquals(jsonObject.get("Team").stringValue(), "BalDance", "Team variable not set properly.");
    }

    @Test(description = "Test GetFormParams with undefined key")
    public void testGetFormParamsForUndefinedKey() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST", "firstName=WSO2&company=BalDance");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        Object bJson = JSONParser.parse(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertTrue(bJson instanceof BMap);

        Assert.assertTrue(((BMap<BString, BString>) bJson).get(
                BStringUtils.fromString("Team")).toString().isEmpty(),
                          "Team variable not set properly");
    }

    @Test(description = "Test GetFormParams empty responseMsgPayloads")
    public void testGetFormParamsEmptyResponseMsgPayload() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST", "");
        requestMsg.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), APPLICATION_FORM);
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        assertResponseMessage(responseMsg,
                              "Error occurred while extracting text data from entity");
    }

    @Test(description = "Test GetFormParams with unsupported media type")
    public void testGetFormParamsWithUnsupportedMediaType() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST",
                "firstName=WSO2&company=BalDance");
        requestMsg.setHeader("Content-Type", APPLICATION_JSON);
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        assertResponseMessage(responseMsg, "Invalid content type : expected 'application/x-www-form-urlencoded'");
    }

    @Test(description = "Test GetFormParams with different media type mutations")
    public void testGetFormParamsWithDifferentMediaTypeMutations() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST", "firstName=WSO2&company=BalDance");
        requestMsg.setHeader("Content-Type", APPLICATION_FORM + "; charset=UTF-8");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        assertResponseMessage(responseMsg, "{\"Name\":\"WSO2\", \"Team\":\"\"}");

        //Test GetFormParams with case insensitive media type + params
        requestMsg = MessageUtils.generateHTTPMessage(path, "POST", "firstName=WSO2&company=BalDance");
        requestMsg.setHeader("Content-Type", "Application/x-www-Form-urlencoded; ");
        responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        assertResponseMessage(responseMsg, "{\"Name\":\"WSO2\", \"Team\":\"\"}");
    }

    @Test(description = "Test GetFormParams without inbound content type header media type")
    public void testGetFormParamsWithoutContentType() {
        String path = "/echo/getFormParams";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST", "firstName=WSO2&company=BalDance");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        assertResponseMessage(responseMsg, "Content-Type header is not available");
    }

    private void assertResponseMessage(HttpCarbonMessage responseMsg, String expectedMessage) {
        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(responseMsg), expectedMessage);
    }

    @Test(description = "Test Http PATCH verb dispatching with a responseMsgPayload")
    public void testPATCHMethodWithBody() {
        String path = "/echo/modify";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "PATCH", "WSO2");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        Assert.assertEquals((int) responseMsg.getHttpStatusCode(), 204);
    }

    @Test(description = "Test Http PATCH verb dispatching without a responseMsgPayload")
    public void testPATCHMethodWithoutBody() {
        String path = "/echo/modify";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "PATCH");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        Assert.assertEquals((int) responseMsg.getHttpStatusCode(), 204);
    }

    //TODO: add more test cases

    //TODO Transaction
    /* Negative cases */
    @Test(description = "verify code analyzer errors in services.", enabled = false)
    public void testCheckCodeAnalyzerErrors() {
        BAssertUtil.validateError(negativeResult, 0, "break cannot be used outside of a loop", 10, 9);
        BAssertUtil.validateError(negativeResult, 1, "continue cannot be used outside of a loop", 16, 9);
        BAssertUtil.validateError(negativeResult, 2, "abort cannot be used outside of a transaction block", 22, 9);
        BAssertUtil.validateError(negativeResult, 3, "unreachable code", 29, 9);
        // BAssertUtil.validateError(negativeResult, 4, "worker send/receive interactions are invalid; worker(s) " +
        // "cannot move onwards from the state: '[a -> w2, b -> w1, FINISHED]'", 33, 9);
    }

    @Test(description = "Test uninitialized service/resource config annotations")
    public void testUninitializedAnnotations() {
        String path = "/hello/echo";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "Uninitialized configs");
    }

    @Test(description = "Test non remote function invocation")
    public void testNonRemoteFunctionInvocation() {
        String path = "/hello/testFunctionCall";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "GET");
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg, "responseMsg message not found");
        String responseMsgPayload = org.ballerinalang.model.util.StringUtils
                .getStringFromInputStream(new HttpMessageDataStreamer(responseMsg).getInputStream());
        Assert.assertEquals(responseMsgPayload, "Non remote function invoked");
    }

    @Test(description = "Test error returning from resource")
    public void testErrorReturn() {
        String path = "/echo/parseJSON";
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("Content-Type", "application/json");
        String invalidJSON = "{name: \"John Doe\"}";
        HTTPTestRequest requestMsg = MessageUtils.generateHTTPMessage(path, "POST", headers, invalidJSON);
        HttpCarbonMessage responseMsg = Services.invoke(TEST_ENDPOINT_1_PORT, requestMsg);

        Assert.assertNotNull(responseMsg);
        Assert.assertEquals((int) responseMsg.getHttpStatusCode(), 500);
    }
}
