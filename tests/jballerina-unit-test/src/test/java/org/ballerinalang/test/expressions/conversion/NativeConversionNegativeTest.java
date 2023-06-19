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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BErrorType;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

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
        Object returns = BRunUtil.invoke(negativeResult, "testIncompatibleJsonToStructWithErrors",
                new Object[]{});

        // check the error
        Assert.assertTrue(returns instanceof BError);
        String errorMsg = ((BMap<String, Object>) ((BError) returns).getDetails()).get(
                StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "'map<json>' value cannot be converted to 'Person': \n\t\t{" +
                "\n\t\t  field 'parent.parent' in record 'Person' should be of type 'Person?', found '\"Parent\"" +
                "'\n\t\t}");
    }

    @Test
    public void testEmptyJSONtoStructWithoutDefaults() {
        Object returns = BRunUtil.invoke(negativeResult, "testEmptyJSONtoStructWithoutDefaults");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "'map<json>' value cannot be converted to 'StructWithoutDefaults': " +
                "\n\t\tmissing required field 'a' of type 'int' in record 'StructWithoutDefaults'");
    }

    @Test
    public void testEmptyMaptoStructWithoutDefaults() {
        Object returns = BRunUtil.invoke(negativeResult, "testEmptyMaptoStructWithoutDefaults");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "'map<anydata>' value cannot be converted to 'StructWithoutDefaults': " +
                "\n\t\tmissing required field 'a' of type 'int' in record 'StructWithoutDefaults'");
    }

    @Test(description = "Test converting an unsupported array to json")
    public void testArrayToJsonFail() {
        Object returns = BRunUtil.invoke(negativeResult, "testArrayToJsonFail");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
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
        Object returns = BRunUtil.invoke(negativeResult, "testIncompatibleImplicitConversion");
        Assert.assertTrue(returns instanceof BError);
        String errorMsg =
                ((BMap<String, Object>) ((BError) returns).getDetails()).get(StringUtils.fromString("message"))
                        .toString();
        Assert.assertEquals(errorMsg, "'string' value cannot be converted to 'int'");
    }

    @Test(description = "Test converting record to record which has cyclic reference to its own value.")
    public void testConvertRecordToRecordWithCyclicValueReferences() {
        Object results = BRunUtil.invoke(negativeResult, "testConvertRecordToRecordWithCyclicValueReferences");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Manager' value has cyclic reference");
    }

    @Test(description = "Test converting record to map having cyclic reference.")
    public void testConvertRecordToMapWithCyclicValueReferences() {
        Object results = BRunUtil.invoke(negativeResult, "testConvertRecordToMapWithCyclicValueReferences");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Manager' value has cyclic reference");
    }

    @Test(description = "Test converting record to json having cyclic reference.")
    public void testConvertRecordToJsonWithCyclicValueReferences() {
        Object results = BRunUtil.invoke(negativeResult, "testConvertRecordToJsonWithCyclicValueReferences");
        Object error = results;
        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'Manager' value has cyclic reference");
    }

    @Test(dataProvider = "testConversionFunctionList")
    public void testConversionNegative(String funcName) {
        BRunUtil.invoke(negativeResult, funcName);
    }

    @DataProvider(name = "testConversionFunctionList")
    public Object[] testConversionFunctions() {
        return new Object[]{
                "testConvertFromJsonWithCyclicValueReferences"
        };
    }

    @AfterClass
    public void tearDown() {
        taintCheckResult = null;
        negativeResult = null;
    }
}
