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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test entity body content, with and without charset param.
 *
 * @since 0.967.0
 */
public class EntityBodyWithCharsetTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/entity-body-with-charset-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
    }

    //Request - Json tests with charset
    @Test(description = "Set json payload with a given charset and when the content-type header is not present in request")
    public void testSetJsonPayloadWithCharset() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetJsonPayloadWithCharset", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=US-ASCII");
    }

    @Test(description = "Set json payload without a charset param and when the content-type header is not present in request")
    public void testSetJsonPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetJsonPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=utf-8");
    }

    @Test(description = "Set json payload with a given charset and when the content-type header is present with a different charset")
    public void testCharsetWithExistingContentType() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentType", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=US-ASCII");
    }

    @Test(description = "Set json payload with a given charset and when the content-type header is present with a different charset")
    public void testNoCharsetWithExistingContentType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNoCharsetWithExistingContentType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=UNICODE-1-1-UTF-7");
    }

    @Test(description = "Set content-type header after setting json payload")
    public void testSetHeaderAfterJsonPayload() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterJsonPayload", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=\"ISO_8859-1:1987\"");
    }

    //Request - Xml tests with charset
    @Test(description = "Set xml payload with a given charset and when the content-type header is not present in request")
    public void testSetXmlPayloadWithCharset() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetXmlPayloadWithCharset", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=US-ASCII");
    }

    @Test(description = "Set xml payload without a charset param and when the content-type header is not present in request")
    public void testSetXmlPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetXmlPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=utf-8");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testCharsetWithExistingContentTypeXml() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeXml", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=US-ASCII");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testNoCharsetWithExistingContentTypeXml() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNoCharsetWithExistingContentTypeXml");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=UNICODE-1-1-UTF-7");
    }

    @Test(description = "Set content-type header after setting xml payload")
    public void testSetHeaderAfterXmlPayload() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterXmlPayload", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=\"ISO_8859-1:1987\"");
    }

    //Request - String payload with charset
    @Test(description = "Set xml payload with a given charset and when the content-type header is not present in request")
    public void testSetStringPayloadWithCharset() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetStringPayloadWithCharset", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=US-ASCII");
    }

    @Test(description = "Set xml payload without a charset param and when the content-type header is not present in request")
    public void testSetStringPayloadWithoutCharset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetStringPayloadWithoutCharset");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=utf-8");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testCharsetWithExistingContentTypeString() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeString", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=US-ASCII");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testNoCharsetWithExistingContentTypeString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNoCharsetWithExistingContentTypeString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=UNICODE-1-1-UTF-7");
    }

    @Test(description = "Set content-type header after setting xml payload")
    public void testSetHeaderAfterStringPayload() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterStringPayload", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=\"ISO_8859-1:1987\"");
    }

    //Response tests - Json with charset
    @Test(description = "Set json payload with a given charset and when the content-type header is not present in response")
    public void testSetJsonPayloadWithCharsetResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetJsonPayloadWithCharsetResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=US-ASCII");
    }

    @Test(description = "Set json payload without a charset param and when the content-type header is not present in response")
    public void testSetJsonPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetJsonPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=utf-8");
    }

    @Test(description = "Set json payload with a given charset and when the content-type header is present with a different charset")
    public void testCharsetWithExistingContentTypeResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=US-ASCII");
    }

    @Test(description = "Set json payload with a given charset and when the content-type header is present with a different charset")
    public void testNoCharsetWithExistingContentTypeResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNoCharsetWithExistingContentTypeResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=UNICODE-1-1-UTF-7");
    }

    @Test(description = "Set content-type header after setting json payload")
    public void testSetHeaderAfterJsonPayloadResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterJsonPayloadResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/json;charset=\"ISO_8859-1:1987\"");
    }

    //Response - Xml tests with charset
    @Test(description = "Set xml payload with a given charset and when the content-type header is not present in request")
    public void testSetXmlPayloadWithCharsetResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetXmlPayloadWithCharsetResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=US-ASCII");
    }

    @Test(description = "Set xml payload without a charset param and when the content-type header is not present in request")
    public void testSetXmlPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetXmlPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=utf-8");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testCharsetWithExistingContentTypeXmlResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeXmlResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=US-ASCII");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testNoCharsetWithExistingContentTypeXmlResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNoCharsetWithExistingContentTypeXmlResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=UNICODE-1-1-UTF-7");
    }

    @Test(description = "Set content-type header after setting xml payload")
    public void testSetHeaderAfterXmlPayloadResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterXmlPayloadResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "application/xml;charset=\"ISO_8859-1:1987\"");
    }

    //Response - String payload with charset
    @Test(description = "Set xml payload with a given charset and when the content-type header is not present in request")
    public void testSetStringPayloadWithCharsetResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetStringPayloadWithCharsetResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=US-ASCII");
    }

    @Test(description = "Set xml payload without a charset param and when the content-type header is not present in request")
    public void testSetStringPayloadWithoutCharsetResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetStringPayloadWithoutCharsetResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=utf-8");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testCharsetWithExistingContentTypeStringResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCharsetWithExistingContentTypeStringResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=US-ASCII");
    }

    @Test(description = "Set xml payload with a given charset and when the content-type header is present with a different charset")
    public void testNoCharsetWithExistingContentTypeStringResponse() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNoCharsetWithExistingContentTypeStringResponse");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=UNICODE-1-1-UTF-7");
    }

    @Test(description = "Set content-type header after setting xml payload")
    public void testSetHeaderAfterStringPayloadResponse() {
        BString charset = new BString("US-ASCII");
        BValue[] args = {charset};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterStringPayloadResponse", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BStringArray)returns[0]).get(0), "text/plain;charset=\"ISO_8859-1:1987\"");
    }
}
