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
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test case insensitivity of entity headers.
 *
 * @since 0.966.0
 */
public class HeaderTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/mime/header-test.bal";
        compileResult = BCompileUtil.compile(sourceFilePath);
    }

    @Test(description = "Test whether the correct http header value is returned when the header exist as requested")
    public void testGetHeaderAsIs() {
        BString headerName = new BString("Content-Type");
        BString headerValue = new BString("application/json");
        BString headerNameToBeUsedForRetrieval = new BString("Content-Type");
        BValue[] args = {headerName, headerValue, headerNameToBeUsedForRetrieval};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/json");
    }

    @Test(description = "Test whether the case is ignored when dealing with http headers")
    public void testCaseInsensitivityOfHeaders() {
        BString headerName = new BString("content-type");
        BString headerValue = new BString("application/json");
        BString headerNameToBeUsedForRetrieval = new BString("ConTeNT-TYpE");
        BValue[] args = {headerName, headerValue, headerNameToBeUsedForRetrieval};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "application/json");
    }

    @Test(description = "Test adding multiple headers to entity")
    public void testAddingMultipleHeaders() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddingMultipleHeaders", args);
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(returns[0].stringValue(), "value1");
        Assert.assertEquals(returns[1].stringValue(), "value2");
        Assert.assertEquals(returns[2].stringValue(), "value3");
    }

    @Test(description = "Test adding multiple values to same header")
    public void testAddingMultipleValuesToSameHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddingMultipleValuesToSameHeader", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "[\"value1\", \"value2\", \"value3\"]");
        Assert.assertEquals(returns[1].stringValue(), "value3");
    }

    @Test(description = "Test set header function")
    public void testSetHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeader", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "[]");
        Assert.assertEquals(returns[1].stringValue(), "totally different value");
    }

    @Test(description = "Test set header after add header")
    public void testSetHeaderAfterAddheader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetHeaderAfterAddHeader", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "[\"value1\", \"value2\", \"value3\"]");
        Assert.assertEquals(returns[1].stringValue(), "totally different value");
    }

    @Test(description = "Test add header after set header")
    public void testAddHeaderAfterSetheader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAddHeaderAfterSetHeader", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "[\"totally different value\", \"value4\"]");
        Assert.assertEquals(returns[1].stringValue(), "totally different value");
    }

    @Test(description = "Test remove header function")
    public void testRemoveHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveHeader", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "[]");
        Assert.assertEquals(returns[1].stringValue(), "totally different value");
    }

    @Test(description = "Test getting a value out of a non existence header")
    public void testNonExistenceHeader() {
        try {
            BValue[] returns = BRunUtil.invoke(compileResult, "testNonExistenceHeader");
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), "");
        } catch (Exception exception) {
            String errorMessage = exception.getMessage();
            Assert.assertTrue(errorMessage.contains(" message: http Header does not exist!"));
        }

    }

    @Test(description = "Test getting all header names")
    public void testGetHeaderNames() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetHeaderNames");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[\"heAder1\", \"hEader2\", \"HEADER3\"]");
    }

    @Test(description = "Test manipulating return headers")
    public void testManipulatingReturnHeaders() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testManipulateHeaders");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "[]");
    }

    @Test(description = "Test has header function")
    public void testHasHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(Boolean.parseBoolean(returns[0].stringValue()));
    }

    @Test(description = "Test has header function for a non-existence header")
    public void testHasHeaderForNonExistenceHeader() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasHeaderForNonExistenceHeader", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertFalse(Boolean.parseBoolean(returns[0].stringValue()));
    }
}
