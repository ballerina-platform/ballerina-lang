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
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for conversion variables.
 *
 * @since 0.985.0
 */
@Test(groups = "broken")
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

    @Test(description = "Test passing tainted value with convert")
    public void testTaintedValue() {
        Assert.assertEquals(taintCheckResult.getErrorCount(), 1);
        BAssertUtil.validateError(taintCheckResult, 0, "tainted value passed to sensitive parameter 'intArg'", 28, 22);
    }

    @Test(description = "Test convert function with multiple arguments")
    public void testFloatToIntWithMultipleArguments() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 8);
        BAssertUtil.validateError(negativeCompileResult, 0, "too many arguments in call to 'convert()'", 44, 12);
    }

    @Test(description = "Test convert function with no arguments")
    public void testFloatToIntWithNoArguments() {
        BAssertUtil.validateError(negativeCompileResult, 2, "not enough arguments in call to 'convert()'", 49, 12);
    }

    @Test(description = "Test object conversions not supported")
    public void testObjectToJson() {
        BAssertUtil.validateError(negativeCompileResult, 4, "incompatible types: 'PersonObj' cannot be converted to "
                + "'json'", 54, 12);
    }

    @Test
    public void testStructToJsonConstrained1() {
        BAssertUtil.validateError(negativeCompileResult, 6, "incompatible types: 'Person' cannot be converted to "
                + "'json<Person2>'", 65, 23);
    }
}

