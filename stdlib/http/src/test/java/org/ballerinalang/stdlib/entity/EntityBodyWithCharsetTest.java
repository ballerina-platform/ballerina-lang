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

package org.ballerinalang.stdlib.entity;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

/**
 * Test entity body content, with and without charset param.
 *
 * @since 0.970.0
 */
public class EntityBodyWithCharsetTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/entity/entity-body-with-charset-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
        BCompileUtil.compile(sourceFilePath);
    }

    @Test
    public void testSetJsonPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetJsonPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/json");
    }

    @Test
    public void testCharsetWithExistingContentType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/json;charset=ISO_8859-1:1987");
    }

    @Test
    public void testSetHeaderAfterJsonPayload() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterJsonPayload");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/json;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void testJsonPayloadWithDefaultCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonPayloadWithDefaultCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "{\"test\":\"菜鸟驿站\"}");
    }

    @Test
    public void testJsonPayloadWithCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonPayloadWithCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "{\"test\":\"ߢߚߟ\"}");
    }

    //Request - Xml tests with charset
    @Test
    public void testSetXmlPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetXmlPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/xml");
    }

    @Test
    public void testCharsetWithExistingContentTypeXml() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeXml");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/xml");
    }

    @Test
    public void testSetHeaderAfterXmlPayload() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterXmlPayload");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/xml;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void testXmlPayloadWithDefaultCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXmlPayloadWithDefaultCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "<菜鸟驿站><name>菜鸟驿站</name></菜鸟驿站>");
    }

    @Test
    public void testXmlPayloadWithCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXmlPayloadWithCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "<菜鸟驿站><name>菜鸟驿站</name></菜鸟驿站>");
    }

    //Request - String payload with charset
    @Test
    public void testSetStringPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetStringPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "text/plain");
    }

    @Test
    public void testCharsetWithExistingContentTypeString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "text/plain");
    }

    @Test
    public void testSetHeaderAfterStringPayload() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterStringPayload");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "text/plain;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void testTextPayloadWithDefaultCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTextPayloadWithDefaultCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "菜鸟驿站");
    }

    @Test
    public void testTextPayloadWithCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTextPayloadWithCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].toString(), "菜鸟驿站");
    }

    //Response tests - Json with charset
    @Test
    public void testSetJsonPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetJsonPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/json");
    }

    @Test
    public void testCharsetWithExistingContentTypeResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/json");
    }

    @Test
    public void testSetHeaderAfterJsonPayloadResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterJsonPayloadResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/json;charset=\"ISO_8859-1:1987\"");
    }

    //Response - Xml tests with charset
    @Test
    public void testSetXmlPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetXmlPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/xml");
    }

    @Test
    public void testCharsetWithExistingContentTypeXmlResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeXmlResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/xml");
    }

    @Test
    public void testSetHeaderAfterXmlPayloadResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterXmlPayloadResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "application/xml;charset=\"ISO_8859-1:1987\"");
    }

    //Response - String payload with charset
    @Test
    public void testSetStringPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetStringPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "text/plain");
    }

    @Test
    public void testCharsetWithExistingContentTypeStringResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeStringResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "text/plain");
    }

    @Test
    public void testSetHeaderAfterStringPayloadResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterStringPayloadResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "text/plain;charset=\"ISO_8859-1:1987\"");
    }

    @Test
    public void jsonTest() {
        String path = "/test/jsonTest";
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("content-type", "application/json");
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage(path, "POST", headers, "{\"test\":\"菜鸟驿站\"}");
        HttpCarbonMessage response = Services.invoke(9090, cMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(JsonParser.parse(new HttpMessageDataStreamer(response).getInputStream()).stringValue(),
                            "{\"test\":\"菜鸟驿站\"}");
    }
}
