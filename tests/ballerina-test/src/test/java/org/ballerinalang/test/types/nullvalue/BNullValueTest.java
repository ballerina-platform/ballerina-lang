/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.nullvalue;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina null value.
 */
public class BNullValueTest {

    private CompileResult positiveCompileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        positiveCompileResult = BCompileUtil.compile("test-src/types/null/null-value.bal");
        negativeResult = BCompileUtil.compile("test-src/types/null/null-value-negative.bal");
    }

    @Test(description = "Test null value of a xml")
    public void testXmlNull() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testXmlNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(5));
    }

    @Test(description = "Test null value of a json")
    public void testJsonNull() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testJsonNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(6));
    }

    @Test(description = "Test null value of a struct")
    public void testStructNull() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testStructNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(7));
    }

    @Test(description = "Test null value of a array")
    public void testArrayNull() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testArrayNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(9));
    }

    @Test(description = "Test null value of a array")
    public void testArrayNotNull() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testArrayNotNull", new BValue[]{});
        Assert.assertEquals(((BInteger) vals[0]).intValue(), 9);
    }

    @Test(description = "Test null value of a map")
    public void testMapNull() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testMapNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
        Assert.assertEquals(vals[2], new BInteger(10));
    }

    @Test(description = "Test casting a nullified value",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error:.*NullReferenceException.*")
    void testCastingNullValue() {
        BRunUtil.invoke(positiveCompileResult, "testCastingNull", new BValue[]{null});
    }

    @Test(description = "Test passing null to a function expects a reference type")
    public void testFunctionCallWithNull() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testFunctionCallWithNull", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    @Test(description = "Test comparing null vs null")
    public void testNullLiteralComparison() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testNullLiteralComparison", new BValue[]{});
        Assert.assertTrue(vals[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) vals[0]).booleanValue(), true);
    }

    @Test(description = "Test comparing null vs null")
    public void testNullLiteralNotEqualComparison() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testNullLiteralNotEqualComparison", new BValue[]{});
        Assert.assertTrue(vals[0] instanceof BBoolean);
        Assert.assertEquals(((BBoolean) vals[0]).booleanValue(), false);
    }

    @Test(description = "Test returning a null literal")
    public void testReturnNullLiteral() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testReturnNullLiteral", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }


    @Test(description = "Test null json in worker")
    public void testJSONNullInWorker() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testNullInWorker1", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    @Test(description = "Test null string in worker")
    public void testStringNullInWorker() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testNullInWorker2", new BValue[]{});
        Assert.assertEquals(vals[0], null);
    }

    @Test(description = "Test null in fork-join1")
    public void testNullInForkJoin1() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testNullInForkJoin1", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
    }

    @Test(description = "Test null in fork-join2")
    public void testNullInForkJoin2() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testNullInForkJoin2", new BValue[]{});
        Assert.assertEquals(vals[0], null);
        Assert.assertEquals(vals[1], null);
    }

    @Test(description = "Test array of null values")
    public void testArrayOfNulls() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testArrayOfNulls", new BValue[]{});
        Assert.assertEquals(vals.length, 4);
        Assert.assertTrue(vals[0] instanceof BStruct);
        Assert.assertTrue(vals[1] instanceof BStruct);
        Assert.assertEquals(vals[2], null);
        Assert.assertTrue(vals[3] instanceof BStruct);
    }

    @Test(description = "Test map of null values")
    public void testMapOfNulls() {
        BValue[] vals = BRunUtil.invoke(positiveCompileResult, "testMapOfNulls", new BValue[]{});
        @SuppressWarnings("unchecked")
        BMap<String, BValue> nullMap = (BMap<String, BValue>) vals[0];
        Assert.assertEquals(nullMap.get("x2"), null);
        Assert.assertEquals(nullMap.get("x3"), null);
        Assert.assertTrue(nullMap.get("x4") instanceof BString);
        Assert.assertEquals(nullMap.get("x5"), null);
    }

    @Test(description = "Test accessing an element in a null array", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error:.*message: array index out of range.*")
    void testNullArrayAccess() {
        BRunUtil.invoke(positiveCompileResult, "testNullArrayAccess", new BValue[]{});
    }

    @Test(description = "Test accessing an element in a null map", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error:.*message: cannot find key 'maths'.*")
    void testNullMapAccess() {
        BRunUtil.invoke(positiveCompileResult, "testNullMapAccess", new BValue[]{});
    }

    @Test(description = "Test negative test cases")
    void testNullValueNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 5);
        BAssertUtil.validateError(negativeResult, 0, "operator '==' not defined for 'xml?' and 'json'", 5, 9);
        BAssertUtil.validateError(negativeResult, 1, "operator '>' not defined for '()' and 'xml'", 23, 13);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'int', found '()'", 27, 13);
        BAssertUtil.validateError(negativeResult, 3, "operator '+' not defined for '()' and '()'", 31, 13);
        BAssertUtil.validateError(negativeResult, 4, "incompatible types: expected 'string', found '()'", 35, 16);
    }
}
