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

package org.ballerinalang.stdlib.services.nativeimpl;

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_FORM;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_JSON;
import static org.ballerinalang.mime.util.MimeConstants.APPLICATION_XML;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;

/**
 * Success test cases for ballerina/http parseHeader native function.
 */
public class ParseHeaderSuccessTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/services/nativeimpl/parse-header.bal");
    }

    @Test(description = "Test function with single header value")
    public void testSingleHeaderValue() {
        BString value = new BString(TEXT_PLAIN);
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), TEXT_PLAIN);
        Assert.assertTrue(returnVals[1] instanceof BMap);
        BMap<String, BString> params = (BMap<String, BString>) returnVals[1];
        Assert.assertTrue(params.isEmpty());
    }

    @Test(description = "Test function with single header value and params")
    @SuppressWarnings("unchecked")
    public void testSingleHeaderValueWithParam() {
        BString value = new BString(TEXT_PLAIN + ";a=2;b=0.9");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), TEXT_PLAIN);
        Assert.assertTrue(returnVals[1] instanceof BMap);
        BMap<String, BString> params = (BMap<String, BString>) returnVals[1];
        Assert.assertEquals(params.get("a").stringValue(), String.valueOf(2));
        Assert.assertEquals(params.get("b").stringValue(), String.valueOf(0.9));

    }

    @Test(description = "Test function with multiple header values")
    public void testMultipleHeaderValue() {
        BString value = new BString(TEXT_PLAIN + " , " + APPLICATION_FORM);
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), TEXT_PLAIN);
        Assert.assertTrue(returnVals[1] instanceof BMap);
        BMap<String, BString> params = (BMap<String, BString>) returnVals[1];
        Assert.assertTrue(params.isEmpty());
    }

    @Test(description = "Test function with extra space in between values and params")
    @SuppressWarnings("unchecked")
    public void testWithExtraSpaceInBetweenParams() {
        BString value = new BString(APPLICATION_JSON + " ; a = 2 ;    b  =    0.9");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_JSON);
        Assert.assertTrue(returnVals[1] instanceof BMap);
        BMap<String, BString> params = (BMap<String, BString>) returnVals[1];
        Assert.assertEquals(params.get("a").stringValue(), String.valueOf(2));
        Assert.assertEquals(params.get("b").stringValue(), String.valueOf(0.9));
    }

    @Test(description = "Test function with header value ends with semicolon")
    public void testHeaderValueEndingWithSemiColon() {
        BString value = new BString(APPLICATION_XML + ";");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), APPLICATION_XML);
        Assert.assertTrue(returnVals[1] instanceof BMap);
        BMap<String, BString> params = (BMap<String, BString>) returnVals[1];
        Assert.assertTrue(params.isEmpty());
    }

    @Test(description = "Test function with empty header value")
    public void testWithEmptyValue() {
        BString value = new BString("");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), "");
        Assert.assertTrue(returnVals[1] instanceof BMap);
        BMap<String, BString> params = (BMap<String, BString>) returnVals[1];
        Assert.assertTrue(params.isEmpty());
    }

    @Test(description = "Test function when param value is optional. i.e 'text/plain;a, application/xml' ")
    @SuppressWarnings("unchecked")
    public void testValueWithOptionalParam() {
        BString value = new BString(TEXT_PLAIN + ";a, " + APPLICATION_XML);
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BString);
        Assert.assertEquals(returnVals[0].stringValue(), TEXT_PLAIN);
        Assert.assertTrue(returnVals[1] instanceof BMap);
        BMap<String, BString> params = (BMap<String, BString>) returnVals[1];
        Assert.assertNull(params.get("a"));
    }
}
