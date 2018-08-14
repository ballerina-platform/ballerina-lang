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
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.util.ArrayList;
import java.util.List;

/**
 * Test entity body content, with and without charset param.
 *
 * @since 0.970.0
 */
public class EntityBodyWithCharsetTest {

    private CompileResult compileResult, serviceResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/entity-body-with-charset-test.bal";
        compileResult = BCompileUtil.compileAndSetup(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test
    public void testSetJsonPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetJsonPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/json");
    }

    @Test
    public void testCharsetWithExistingContentType() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testCharsetWithExistingContentType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/json;charset=ISO_8859-1:1987");
    }

    @Test
    public void testSetHeaderAfterJsonPayload() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetHeaderAfterJsonPayload");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/json;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void testJsonPayloadWithDefaultCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testJsonPayloadWithDefaultCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "{\"test\":\"菜鸟驿站\"}");
    }

    @Test
    public void testJsonPayloadWithCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testJsonPayloadWithCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "{\"test\":\"ߢߚߟ\"}");
    }

    //Request - Xml tests with charset
    @Test
    public void testSetXmlPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetXmlPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/xml");
    }

    @Test
    public void testCharsetWithExistingContentTypeXml() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testCharsetWithExistingContentTypeXml");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/xml");
    }

    @Test
    public void testSetHeaderAfterXmlPayload() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetHeaderAfterXmlPayload");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/xml;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void testXmlPayloadWithDefaultCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testXmlPayloadWithDefaultCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "<菜鸟驿站><name>菜鸟驿站</name></菜鸟驿站>");
    }

    @Test
    public void testXmlPayloadWithCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testXmlPayloadWithCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "<菜鸟驿站><name>菜鸟驿站</name></菜鸟驿站>");
    }

    //Request - String payload with charset
    @Test
    public void testSetStringPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetStringPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "text/plain");
    }

    @Test
    public void testCharsetWithExistingContentTypeString() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testCharsetWithExistingContentTypeString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "text/plain");
    }

    @Test
    public void testSetHeaderAfterStringPayload() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetHeaderAfterStringPayload");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "text/plain;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void testTextPayloadWithDefaultCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testTextPayloadWithDefaultCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "菜鸟驿站");
    }

    @Test
    public void testTextPayloadWithCharset() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testTextPayloadWithCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "菜鸟驿站");
    }

    //Response tests - Json with charset
    @Test
    public void testSetJsonPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetJsonPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/json");
    }

    @Test
    public void testCharsetWithExistingContentTypeResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testCharsetWithExistingContentTypeResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/json");
    }

    @Test
    public void testSetHeaderAfterJsonPayloadResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetHeaderAfterJsonPayloadResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/json;charset=\"ISO_8859-1:1987\"");
    }

    //Response - Xml tests with charset
    @Test
    public void testSetXmlPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetXmlPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/xml");
    }

    @Test
    public void testCharsetWithExistingContentTypeXmlResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testCharsetWithExistingContentTypeXmlResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/xml");
    }

    @Test
    public void testSetHeaderAfterXmlPayloadResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetHeaderAfterXmlPayloadResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "application/xml;charset=\"ISO_8859-1:1987\"");
    }

    //Response - String payload with charset
    @Test
    public void testSetStringPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetStringPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "text/plain");
    }

    @Test
    public void testCharsetWithExistingContentTypeStringResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testCharsetWithExistingContentTypeStringResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "text/plain");
    }

    @Test
    public void testSetHeaderAfterStringPayloadResponse() {
        BValue[] returns = BRunUtil.invokeStateful(compileResult, "testSetHeaderAfterStringPayloadResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "text/plain;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void jsonTest() {
        String path = "/test/jsonTest";
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("content-type", "application/json"));
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", headers, "{\"test\":\"菜鸟驿站\"}");
        HttpCarbonMessage response = Services.invokeNew(serviceResult, "mockEP", cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream()).stringValue(),
                "{\"test\":\"菜鸟驿站\"}");
    }
}
