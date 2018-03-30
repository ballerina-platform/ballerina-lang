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
public class EntityBodyTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/entity-body-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
    }

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
}
