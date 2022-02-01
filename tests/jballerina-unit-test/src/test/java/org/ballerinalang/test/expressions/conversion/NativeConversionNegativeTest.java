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

import org.ballerinalang.core.model.types.BErrorType;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Negative test cases for conversion variables.
 *
 * @since 0.985.0
 */
public class NativeConversionNegativeTest {

    private CompileResult negativeResult;
    private CompileResult taintCheckResult;

    @BeforeClass
    public void setup() {
        negativeResult = BCompileUtil.compile("test-src/expressions/conversion/native-conversion-negative.bal");
        taintCheckResult =
                BCompileUtil.compile("test-src/expressions/conversion/native-conversion-taint-negative.bal");
    }

    @Test
    public void testIncompatibleJsonToStructWithErrors() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testIncompatibleJsonToStructWithErrors",
                                           new BValue[] {});

        // check the error
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'map<json>' value cannot be converted to 'Person': " +
                "\n\t\tfield 'parent.parent' in record 'Person' should be of type 'Person?', found '\"Parent\"'");
    }

    @Test
    public void testEmptyJSONtoStructWithoutDefaults() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testEmptyJSONtoStructWithoutDefaults");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'map<json>' value cannot be converted to 'StructWithoutDefaults': " +
                "\n\t\tmissing required field 'a' of type 'int' in record 'StructWithoutDefaults'");
    }

    @Test
    public void testEmptyMaptoStructWithoutDefaults() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testEmptyMaptoStructWithoutDefaults");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'map<anydata>' value cannot be converted to 'StructWithoutDefaults': " +
                "\n\t\tmissing required field 'a' of type 'int' in record 'StructWithoutDefaults'");
    }

    @Test(description = "Test converting an unsupported array to json")
    public void testArrayToJsonFail() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testArrayToJsonFail");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'TX[]' value cannot be converted to 'json'");
    }

    @Test(enabled = false, description = "Test passing tainted value with convert")
    public void testTaintedValue() {
        Assert.assertEquals(taintCheckResult.getErrorCount(), 1);
        BAssertUtil.validateError(taintCheckResult, 0, "tainted value passed to untainted " +
                "parameter 'intArg'", 28, 22);
    }

    @Test(description = "Test object conversions not supported")
    public void testObjectToJson() {
        CompileResult negativeCompileResult =
                BCompileUtil.compile("test-src/expressions/conversion/native-conversion--compile-negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'anydata', found 'PersonObj'", 48, 32);
        Assert.assertEquals(i, negativeCompileResult.getErrorCount());
    }

    @Test
    public void testIncompatibleImplicitConversion() {
        BValue[] returns = BRunUtil.invoke(negativeResult, "testIncompatibleImplicitConversion");
        Assert.assertTrue(returns[0] instanceof BError);
        String errorMsg = ((BMap<String, BValue>) ((BError) returns[0]).getDetails()).get("message").stringValue();
        Assert.assertEquals(errorMsg, "'string' value cannot be converted to 'int'");
    }

    @Test(description = "Test converting record to record which has cyclic reference to its own value.")
    public void testConvertRecordToRecordWithCyclicValueReferences() {
        BValue[] results = BRunUtil.invoke(negativeResult, "testConvertRecordToRecordWithCyclicValueReferences");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Manager' value has cyclic reference");
    }

    @Test(description = "Test converting record to map having cyclic reference.")
    public void testConvertRecordToMapWithCyclicValueReferences() {
        BValue[] results = BRunUtil.invoke(negativeResult, "testConvertRecordToMapWithCyclicValueReferences");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Manager' value has cyclic reference");
    }

    @Test(description = "Test converting record to json having cyclic reference.")
    public void testConvertRecordToJsonWithCyclicValueReferences() {
        BValue[] results = BRunUtil.invoke(negativeResult, "testConvertRecordToJsonWithCyclicValueReferences");
        BValue error = results[0];
        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'Manager' value has cyclic reference");
    }

    @Test(dataProvider = "testConversionFunctionList")
    public void testConversionNegative(String funcName) {
        BRunUtil.invoke(negativeResult, funcName);
    }

    @DataProvider(name = "testConversionFunctionList")
    public Object[] testConversionFunctions() {
        return new Object[] {
                "testConvertFromJsonWithCyclicValueReferences"
        };
    }
}
