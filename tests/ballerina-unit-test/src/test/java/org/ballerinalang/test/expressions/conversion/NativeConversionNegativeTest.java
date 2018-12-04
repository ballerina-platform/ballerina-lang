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
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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

    @Test(description = "Test converting a struct with map of blob to a JSON",
          expectedExceptions = { BLangRuntimeException.class },
          expectedExceptionsMessageRegExp = ".*'Info' value cannot be stamped as 'json'.*")
    public void testStructWithIncompatibleTypeToJson() {
        BRunUtil.invoke(negativeResult, "testStructWithIncompatibleTypeToJson");
    }

    @Test(description = "Test passing tainted value with create")
    public void testTaintedValue() {
        Assert.assertEquals(taintCheckResult.getErrorCount(), 1);
        BAssertUtil.validateError(taintCheckResult, 0, "tainted value passed to sensitive parameter 'intArg'", 28, 22);
    }

    @Test(description = "Test create function with multiple arguments")
    public void testFloatToIntWithMultipleArguments() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 4);
        BAssertUtil.validateError(negativeCompileResult, 0, "too many arguments in call to 'create()'", 19, 12);
    }

    @Test(description = "Test create function with no arguments")
    public void testToIntWithNoArguments() {
        BAssertUtil.validateError(negativeCompileResult, 2, "not enough arguments in call to 'create()'", 24, 12);
    }
}

