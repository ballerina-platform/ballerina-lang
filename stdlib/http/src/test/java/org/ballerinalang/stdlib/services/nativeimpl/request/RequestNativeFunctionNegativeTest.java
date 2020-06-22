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

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.stdlib.utils.TestEntityUtils;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.net.http.ValueCreatorUtils.createEntityObject;
import static org.ballerinalang.net.http.ValueCreatorUtils.createRequestObject;

/**
 * Test cases for ballerina/http request negative native functions.
 */
public class RequestNativeFunctionNegativeTest {

    private CompileResult compileResultNegative, compileResult;

    @BeforeClass
    public void setup() {
        String basePath = "test-src/services/nativeimpl/request/";
        compileResult = BCompileUtil.compile(basePath + "in-request-native-function-negative.bal");
        compileResultNegative = BCompileUtil.compile(basePath + "in-request-compile-negative.bal");
    }

    @Test(description = "Test when the content length header is not set")
    public void testGetContentLength() {
        ObjectValue inRequest = createRequestObject();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetContentLength", new Object[]{ inRequest });
        Assert.assertEquals(returnVals[0].stringValue(), "Content-length is not found");
    }

    @Test
    public void testGetHeader() {
        ObjectValue inRequest = createRequestObject();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetHeader", new Object[]{inRequest,
                StringUtils.fromString(HttpHeaderNames.CONTENT_TYPE.toString())});
        Assert.assertNotNull(returnVals[0]);
        Assert.assertEquals(((BString) returnVals[0]).value(), "Header not found!");
    }

    @Test(description = "Test method without json payload")
    public void testGetJsonPayloadWithoutPayload() {
        ObjectValue inRequest = createRequestObject();
        ObjectValue entity = createEntityObject();
        TestEntityUtils.enrichTestEntityHeaders(entity, APPLICATION_JSON);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetJsonPayload", new Object[]{ inRequest });
        Assert.assertNotNull(returnVals[0]);
        BError err = (BError) (returnVals[0]);
        Assert.assertEquals(err.getMessage(), "No payload");
        Assert.assertEquals(err.getCause().getMessage(), "Error occurred while extracting json data from entity");
        Assert.assertEquals(err.getCause().getCause().getMessage(), "Empty content");
    }

    @Test(description = "Test method with string payload")
    public void testGetJsonPayloadWithStringPayload() {
        ObjectValue inRequest = createRequestObject();
        ObjectValue entity = createEntityObject();

        String payload = "ballerina";
        TestEntityUtils.enrichTestEntity(entity, TEXT_PLAIN, payload);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetJsonPayload", new Object[]{ inRequest });
        Assert.assertNotNull(returnVals[0]);
        BError err = (BError) (returnVals[0]);
        Assert.assertEquals(err.getMessage(), "Error occurred while retrieving the json payload from the request");
        Assert.assertEquals(err.getCause().getMessage(), "Error occurred while extracting json data from entity: " +
                "unrecognized token 'ballerina' at line: 1 column: 11");
    }

    @Test(description = "Test getEntity method on a outRequest without a entity")
    public void testGetEntityNegative() {
        ObjectValue outRequest = createRequestObject();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetEntity", new Object[]{outRequest});
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertNotNull(returnVals[0]);
    }

    @Test(description = "Test getTextPayload method without a payload")
    public void testGetTextPayloadNegative() {
        ObjectValue inRequest = createRequestObject();
        ObjectValue entity = createEntityObject();
        TestEntityUtils.enrichTestEntityHeaders(entity, TEXT_PLAIN);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetTextPayload", new Object[]{ inRequest });
        BError err = (BError) (returnVals[0]);
        Assert.assertEquals(err.getMessage(), "No payload");
        Assert.assertEquals(err.getCause().getMessage(), "Error occurred while extracting text data from entity");
        Assert.assertEquals(err.getCause().getCause().getMessage(), "Empty content");
    }

    @Test
    public void testGetXmlPayloadNegative() {
        ObjectValue inRequest = createRequestObject();
        ObjectValue entity = createEntityObject();
        TestEntityUtils.enrichTestEntityHeaders(entity, APPLICATION_XML);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetXmlPayload", new Object[]{ inRequest });
        BError err = (BError) (returnVals[0]);
        Assert.assertEquals(err.getMessage(), "No payload");
        Assert.assertEquals(err.getCause().getMessage(), "Error occurred while extracting xml data from entity");
        Assert.assertEquals(err.getCause().getCause().getMessage(), "Empty content");
    }

    @Test
    public void testGetXmlPayloadWithStringPayload() {
        ObjectValue inRequest = createRequestObject();
        ObjectValue entity = createEntityObject();
        String payload = "ballerina";
        TestEntityUtils.enrichTestEntity(entity, TEXT_PLAIN, payload);
        inRequest.set(REQUEST_ENTITY_FIELD, entity);
        inRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);

        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetXmlPayload", new Object[]{inRequest});
        Assert.assertNotNull(returnVals[0]);
        BError err = (BError) (returnVals[0]);
        Assert.assertEquals(err.getMessage(), "Error occurred while retrieving the xml payload from the request");
        Assert.assertEquals(err.getCause().getMessage(), "Error occurred while extracting xml data from entity");
        Assert.assertTrue(err.getCause().getCause().getMessage().contains("failed to create xml: Unexpected " +
                                                                                  "character 'b'"));
    }

    @Test
    public void testGetMethodNegative() {
        ObjectValue inRequest = createRequestObject();
        HttpCarbonMessage inRequestMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(inRequest, inRequestMsg);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetMethod", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "");
    }

    @Test
    public void testGetRequestURLNegative() {
        ObjectValue inRequest = createRequestObject();
        HttpCarbonMessage inRequestMsg = HttpUtil.createHttpCarbonMessage(true);
        HttpUtil.addCarbonMsg(inRequest, inRequestMsg);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testGetRequestURL", new Object[]{ inRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVals[0].stringValue(), "no url");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveHeaderNegative() {
        ObjectValue outRequest = createRequestObject();
        ObjectValue entity = createEntityObject();
        String range = "Range";
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add("Expect", "100-continue");
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);
        outRequest.set(REQUEST_ENTITY_FIELD, entity);
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testRemoveHeader",
                                              new Object[]{outRequest, StringUtils.fromString(range)});

        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entityStruct =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(REQUEST_ENTITY_FIELD.getValue());
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertNull(returnHeaders.get(range));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveAllHeadersNegative() {
        ObjectValue outRequest = createRequestObject();
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testRemoveAllHeaders", new Object[]{ outRequest });
        Assert.assertFalse(returnVals.length == 0 || returnVals[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BMap);
        BMap<String, BValue> entityStruct =
                (BMap<String, BValue>) ((BMap<String, BValue>) returnVals[0]).get(REQUEST_ENTITY_FIELD.getValue());
        HttpHeaders returnHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        Assert.assertEquals(returnHeaders.size(), 0);
    }

    @Test
    public void testCompilationErrorTestCases() {
        Assert.assertEquals(compileResultNegative.getErrorCount(), 2);
        //testRequestSetStatusCode
        BAssertUtil.validateError(compileResultNegative, 0,
                                  "undefined method 'setStatusCode' in object 'ballerina/http:1.0.0:Request'", 4, 9);
        BAssertUtil.validateError(compileResultNegative, 1,
                                  "undefined field 'statusCode' in object 'ballerina/http:1.0.0:Request'", 5, 8);
    }
}
