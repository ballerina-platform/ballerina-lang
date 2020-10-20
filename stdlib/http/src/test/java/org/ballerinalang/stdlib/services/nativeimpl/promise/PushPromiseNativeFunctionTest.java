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
package org.ballerinalang.stdlib.services.nativeimpl.promise;

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import static org.ballerinalang.net.http.ValueCreatorUtils.createPushPromiseObject;

/**
 * Test cases for ballerina/http inbound inResponse success native functions.
 */
public class PushPromiseNativeFunctionTest {

    private CompileResult result;
    private final String promiseStruct = HttpConstants.PUSH_PROMISE;
    private final String protocolPackageHttp = HttpConstants.PROTOCOL_PACKAGE_HTTP;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/services/nativeimpl/promise/promise-native-function.bal";
        result = BCompileUtil.compile(sourceFilePath);
    }

    @Test(description = "Test addHeader function of PushPromise")
    public void testAddHeader() {
        BObject promise = createPushPromiseObject();
        String headerName = "header1";
        String headerValue = "value1";
        BValue[] returnVal = BRunUtil.invoke(result, "testAddHeader",
                                             new Object[]{promise, BStringUtils.fromString(headerName),
                                                     BStringUtils.fromString(headerValue)});

        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVal[0] instanceof BMap);
        Http2PushPromise http2PushPromise = (Http2PushPromise) ((BMap<String, BValue>) returnVal[0])
                .getNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE);
        Assert.assertEquals(http2PushPromise.getHeader(headerName), headerValue);
    }

    @Test(description = "Test getHeader function of PushPromise")
    public void testGetHeader() {
        BObject promise = createPushPromiseObject();
        Http2PushPromise http2PushPromise =
                new Http2PushPromise(HttpConstants.HTTP_METHOD_GET, HttpConstants.DEFAULT_BASE_PATH);
        String headerName = "header1";
        String headerValue = "value1";
        http2PushPromise.addHeader(headerName, headerValue);
        HttpUtil.populatePushPromiseStruct(promise, http2PushPromise);

        BValue[] returnVal = BRunUtil.invoke(result, "testGetHeader",
                                             new Object[]{promise, BStringUtils.fromString(headerName)});

        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");
        Assert.assertEquals(returnVal[0].stringValue(), headerValue);
    }

    @Test(description = "Test getHeaders function of PushPromise")
    public void testGetHeaders() {
        BObject promise = createPushPromiseObject();
        Http2PushPromise http2PushPromise =
                new Http2PushPromise(HttpConstants.HTTP_METHOD_GET, HttpConstants.DEFAULT_BASE_PATH);
        String headerName = "header";
        String headerValue1 = "value1";
        String headerValue2 = "value2";
        http2PushPromise.addHeader(headerName, headerValue1);
        http2PushPromise.addHeader(headerName, headerValue2);
        HttpUtil.populatePushPromiseStruct(promise, http2PushPromise);

        BValue[] returnVal = BRunUtil.invoke(result, "testGetHeaders",
                                             new Object[]{promise, BStringUtils.fromString(headerName)});

        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");
        Assert.assertEquals(((BValueArray) returnVal[0]).getString(0), headerValue1);
        Assert.assertEquals(((BValueArray) returnVal[0]).getString(1), headerValue2);
    }

    @Test(description = "Test removeHeader function of PushPromise")
    public void testRemoveHeader() {
        BObject promise = createPushPromiseObject();
        Http2PushPromise http2PushPromise =
                new Http2PushPromise(HttpConstants.HTTP_METHOD_GET, HttpConstants.DEFAULT_BASE_PATH);
        String headerName = "header1";
        String headerValue = "value1";
        http2PushPromise.addHeader(headerName, headerValue);
        HttpUtil.populatePushPromiseStruct(promise, http2PushPromise);

        BValue[] returnVal = BRunUtil.invoke(result, "testRemoveHeader",
                                             new Object[]{promise, BStringUtils.fromString(headerName)});
        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");

        Assert.assertTrue(returnVal[0] instanceof BMap);
        Http2PushPromise retrievedHttp2PushPromise = (Http2PushPromise) ((BMap<String, BValue>) returnVal[0])
                .getNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE);
        Assert.assertNull(retrievedHttp2PushPromise.getHeader(headerName));
    }

    @Test(description = "Test removeAllHeaders function of PushPromise")
    public void testRemoveAllHeaders() {
        BObject promise = createPushPromiseObject();
        Http2PushPromise http2PushPromise =
                new Http2PushPromise(HttpConstants.HTTP_METHOD_GET, HttpConstants.DEFAULT_BASE_PATH);
        String header1Name = "header1";
        String header1Value = "value1";
        http2PushPromise.addHeader(header1Name, header1Value);
        String header2Name = "header2";
        String header2Value = "value2";
        http2PushPromise.addHeader(header2Name, header2Value);
        HttpUtil.populatePushPromiseStruct(promise, http2PushPromise);

        BValue[] returnVal = BRunUtil.invoke(result, "testRemoveAllHeaders", new Object[]{ promise });
        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");

        Assert.assertTrue(returnVal[0] instanceof BMap);
        Http2PushPromise retrievedHttp2PushPromise = (Http2PushPromise) ((BMap<String, BValue>) returnVal[0])
                .getNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE);
        Assert.assertNull(retrievedHttp2PushPromise.getHeader(header1Name));
        Assert.assertNull(retrievedHttp2PushPromise.getHeader(header2Name));
    }

    @Test(description = "Test setHeader function of PushPromise")
    public void testSetHeader() {
        BObject promise = createPushPromiseObject();
        Http2PushPromise http2PushPromise =
                new Http2PushPromise(HttpConstants.HTTP_METHOD_GET, HttpConstants.DEFAULT_BASE_PATH);
        String headerName = "header1";
        String headerValue = "value1";
        http2PushPromise.addHeader(headerName, headerValue);
        HttpUtil.populatePushPromiseStruct(promise, http2PushPromise);

        String targetHeaderValue = "value2";
        BValue[] returnVal = BRunUtil.invoke(result, "testSetHeader",
                                             new Object[]{ promise, BStringUtils.fromString(headerName),
                                                     BStringUtils.fromString(targetHeaderValue) });

        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");
        Assert.assertTrue(returnVal[0] instanceof BMap);
        Http2PushPromise retrievedHttp2PushPromise =
                (Http2PushPromise) ((BMap) returnVal[0]).getNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE);
        Assert.assertEquals(retrievedHttp2PushPromise.getHeader(headerName), targetHeaderValue);
    }

    @Test(description = "Test hasHeader function of PushPromise")
    public void testHasHeader() {
        BObject promise = createPushPromiseObject();
        Http2PushPromise http2PushPromise =
                new Http2PushPromise(HttpConstants.HTTP_METHOD_GET, HttpConstants.DEFAULT_BASE_PATH);
        String headerName = "header1";
        String headerValue = "value1";
        http2PushPromise.addHeader(headerName, headerValue);
        HttpUtil.populatePushPromiseStruct(promise, http2PushPromise);

        BValue[] returnVal = BRunUtil.invoke(result, "testHasHeader",
                                             new Object[]{promise, BStringUtils.fromString(headerName)});

        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");
        Assert.assertTrue(((BBoolean) returnVal[0]).booleanValue(), "hasHeader function failed");
    }

    @Test(description = "Test getHeaderNames function of PushPromise")
    public void testGetHeaderNames() {
        BObject promise = createPushPromiseObject();
        Http2PushPromise http2PushPromise =
                new Http2PushPromise(HttpConstants.HTTP_METHOD_GET, HttpConstants.DEFAULT_BASE_PATH);
        String headerName1 = "header1";
        String headerName2 = "header2";
        http2PushPromise.addHeader(headerName1, "value1");
        http2PushPromise.addHeader(headerName2, "value2");
        HttpUtil.populatePushPromiseStruct(promise, http2PushPromise);

        BValue[] returnVal = BRunUtil.invoke(result, "testGetHeaderNames", new Object[]{ promise });

        Assert.assertFalse(returnVal.length == 0 || returnVal[0] == null, "Invalid Return Values.");
        String result1 = ((BValueArray) returnVal[0]).getString(0);
        String result2 = ((BValueArray) returnVal[0]).getString(1);
        Assert.assertTrue((result1.equals(headerName1) && result2.equals(headerName2) ||
                                  result1.equals(headerName2) && result2.equals(headerName1)),
                          "Expected header names not found");
        Assert.assertEquals(((BValueArray) returnVal[0]).getString(1), headerName2);
    }
}
