/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.conversion;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for conversion variables.
 *
 * @since 0.985.0
 */
public class NativeConversionNegativeTest {

    private CompileResult negativeResult;

    private CompileResult negativeCompileResult;

    private CompileResult taintCheckResult;

    @BeforeClass
    public void setup() {
        negativeResult = BCompileUtil.compile("test-src/expressions/conversion/native-conversion-negative.bal");
        negativeCompileResult =
                BCompileUtil.compile("test-src/expressions/conversion/native-conversion--compile-negative.bal");
        taintCheckResult =
                BCompileUtil.compile("test-src/expressions/conversion/native-conversion-taint-negative.bal");
    }

    @Test
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testIncompatibleJsonToStructWithErrors",
                                           new BValue[] {});

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).details).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible stamp operation: 'json' value cannot be stamped as 'Person'");
    }

    @Test
    public void testEmptyJSONtoStructWithoutDefaults() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testEmptyJSONtoStructWithoutDefaults");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).details).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible stamp operation: 'json' value cannot be stamped as "
                + "'StructWithoutDefaults'");
    }

    @Test
    public void testEmptyMaptoStructWithDefaults() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testEmptyMaptoStructWithDefaults");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).details).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible stamp operation: 'map' value cannot be stamped as "
                + "'StructWithDefaults'");
    }

    @Test
    public void testEmptyMaptoStructWithoutDefaults() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testEmptyMaptoStructWithoutDefaults");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).details).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible stamp operation: 'map' value cannot be stamped as "
                + "'StructWithoutDefaults'");
    }

    @Test(description = "Test performing an invalid tuple conversion")
    public void testTupleConversionFail() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testTupleConversionFail");
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).details).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible stamp operation: '(T1,T1)' value cannot be stamped as '(T1,T2)'");
    }

    @Test(description = "Test converting an unsupported array to json")
    public void testArrayToJsonFail() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testArrayToJsonFail");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).details).get("message").stringValue();
        Assert.assertEquals(errorMsg, "incompatible stamp operation: 'TX[]' value cannot be stamped as 'json'");
    }

    @Test(description = "Test passing tainted value with convert")
    public void testTaintedValue() {
        Assert.assertEquals(taintCheckResult.getErrorCount(), 1);
        BAssertUtil.validateError(taintCheckResult, 0, "tainted value passed to sensitive parameter 'intArg'", 28, 22);
    }

    @Test(description = "Test convert function with multiple arguments")
    public void testFloatToIntWithMultipleArguments() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 4);
        BAssertUtil.validateError(negativeCompileResult, 0, "too many arguments in call to 'convert()'", 51, 12);
    }

    @Test(description = "Test convert function with no arguments")
    public void testFloatToIntWithNoArguments() {
        BAssertUtil.validateError(negativeCompileResult, 1, "not enough arguments in call to 'convert()'", 56, 12);
    }

    @Test(description = "Test object conversions not supported")
    public void testObjectToJson() {
        BAssertUtil.validateError(negativeCompileResult, 2, "incompatible types: 'PersonObj' cannot be converted to "
                + "'json'", 61, 12);
    }

    @Test
    public void testTypeCheckingRecordToMapConversion() {
        BAssertUtil.validateError(negativeCompileResult, 3, "incompatible types: 'Person2' cannot be converted to "
                + "'map<int>'", 66, 12);
    }

    @Test
    public void testIncompatibleImplicitConversion() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testIncompatibleImplicitConversion");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).details).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'string' cannot be converted to 'int'");
    }

    @Test(description = "Test converting record to record which has cyclic reference to its own value.")
    public void testConvertRecordToRecordWithCyclicValueReferences() {
        BValue[] results = BRunUtil.invoke(negativeResult, "testConvertRecordToRecordWithCyclicValueReferences");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).details).get("message").stringValue(),
                            "'Manager' value has cyclic reference");
    }

    @Test(description = "Test converting record to map having cyclic reference.")
    public void testConvertRecordToMapWithCyclicValueReferences() {
        BValue[] results = BRunUtil.invoke(negativeResult, "testConvertRecordToMapWithCyclicValueReferences");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).details).get("message").stringValue(),
                            "'Manager' value has cyclic reference");
    }

    @Test(description = "Test converting record to json having cyclic reference.")
    public void testConvertRecordToJsonWithCyclicValueReferences() {
        BValue[] results = BRunUtil.invoke(negativeResult, "testConvertRecordToJsonWithCyclicValueReferences");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).details).get("message").stringValue(),
                            "'Manager' value has cyclic reference");
    }
}

