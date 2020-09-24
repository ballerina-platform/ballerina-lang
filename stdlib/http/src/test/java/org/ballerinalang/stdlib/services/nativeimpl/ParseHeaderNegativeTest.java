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

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;

/**
 * Negative test cases for ballerina/http parseHeader native function.
 */
public class ParseHeaderNegativeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/services/nativeimpl/parse-header.bal");
    }

    @Test(description = "Test function with single header value")
    public void testWithNullValue() {
        BString value = new BString(null);
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BError);
        Assert.assertEquals(((BError) returnVals[0]).getMessage(), "failed to parse: header value cannot be null");
    }

    @Test(description = "Test function with missing header value. i.e ';a=2;b=0.9'")
    public void testWithMissingValue() {
        BString value = new BString(";a = 2");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BError);
        Assert.assertEquals(((BError) returnVals[0]).getMessage(),
                            "failed to parse: error(\"invalid header value: ;a = 2\")");
    }

    @Test(description = "Test function with invalid param values")
    public void testInvalidParams1() {
        BString value = new BString(TEXT_PLAIN + ";a = ");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BError);
        Assert.assertEquals(((BError) returnVals[0]).getMessage(),
                            "failed to parse: error(\"invalid header parameter: a =\")");
    }

    @Test(description = "Test function with invalid param values")
    public void testInvalidParams2() {
        BString value = new BString(TEXT_PLAIN + "; = ");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BError);
        Assert.assertEquals(((BError) returnVals[0]).getMessage(),
                            "failed to parse: error(\"invalid header parameter: =\")");
    }

    @Test(description = "Test function with invalid param values")
    public void testInvalidParams3() {
        BString value = new BString(TEXT_PLAIN + "; = 2");
        BValue[] inputArg = {value};
        BValue[] returnVals = BRunUtil.invoke(result, "testParseHeader", inputArg);

        Assert.assertFalse(returnVals == null || returnVals.length == 0, "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BError);
        Assert.assertEquals(((BError) returnVals[0]).getMessage(),
                            "failed to parse: error(\"invalid header parameter: = 2\")");
    }
}
