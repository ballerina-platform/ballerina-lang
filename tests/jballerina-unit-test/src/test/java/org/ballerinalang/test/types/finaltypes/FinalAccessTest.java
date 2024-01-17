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
package org.ballerinalang.test.types.finaltypes;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for final fields.
 */
public class FinalAccessTest {

    private CompileResult compileResult;
    private CompileResult finalLocalNoInitVarResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/finaltypes/TestProject");
        finalLocalNoInitVarResult = BCompileUtil.compile("test-src/types/finaltypes/test_final_local_no_init_var.bal");
    }

    @Test(description = "Test final field access failures")
    public void testFinalFailCase() {
        CompileResult compileResultNegative = BCompileUtil.compile(
                "test-src/types/finaltypes/test_implicitly_final_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'a'", 11, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'a'", 17, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'f'", 22, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 's'", 23, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'b'", 24, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'j'", 25, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'a'", 38, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "invalid assignment: 'listener' declaration is final",
                45, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'p2'", 49, 5);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'b'", 53, 9);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'b'", 59, 9);
        BAssertUtil.validateError(compileResultNegative, i++, "cannot assign a value to function argument 'b'", 66, 9);
        Assert.assertEquals(compileResultNegative.getErrorCount(), i);
    }

    @Test
    public void testFinalVariableNegative() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/types/finaltypes/test_final_var_negative.bal");
        int i = 0;
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'v1'", 20, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'globalFinalInt'", 21, 5);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'name'", 25, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'name'", 26, 5);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'name'", 30, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'name'", 31, 5);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'name'", 35, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'name'", 36, 5);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'name'", 40, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'name'", 41, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 'i'", 52, 5);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 's'", 54, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 's'", 63, 5);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 'i'", 67, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to potentially initialized final 'i'",
                75, 5);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 's'", 77, 5);
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to potentially initialized final 's'",
                82, 5);
        BAssertUtil.validateError(resultNegative, i++, "variable 'i' may not have been initialized", 94, 13);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 't'", 101, 5);
        BAssertUtil.validateError(resultNegative, i++, "variable 's' may not have been initialized", 101, 16);

        Assert.assertEquals(resultNegative.getErrorCount(), i - 9);
        Assert.assertEquals(resultNegative.getWarnCount(), 9);
    }

    @Test(description = "Test final global variable")
    public void testFinalAccess() {

        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testFinalAccess");

        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 10L);

        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals(returns.get(1), 100L);

        Assert.assertTrue(returns.get(2) instanceof Long);
        Assert.assertEquals(returns.get(2), 10L);

        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(3), 100L);
    }

    @Test(description = "Test final global variable")
    public void testFinalStringAccess() {

        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testFinalStringAccess");

        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals((returns.get(0)).toString(), "hello");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals((returns.get(1)).toString(), "world");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals((returns.get(2)).toString(), "hello");

        Assert.assertTrue(returns.get(3) instanceof BString);
        Assert.assertEquals((returns.get(3)).toString(), "world");
    }

    @Test(description = "Test final global variable as a parameter")
    public void testFinalFieldAsParameter() {
        Object returns = BRunUtil.invoke(compileResult, "testFinalFieldAsParameter");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test final parameter")
    public void testFieldAsFinalParameter() {
        Object returns = BRunUtil.invoke(compileResult, "testFieldAsFinalParameter");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 50L);
    }

    @Test(description = "Test final local variable with type")
    public void testLocalFinalValueWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithType");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test(description = "Test final local variable without type")
    public void testLocalFinalValueWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithoutType");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test(description = "Test final local variable with type initialized from a function")
    public void testLocalFinalValueWithTypeInitializedFromFunction() {
        Object returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithTypeInitializedFromFunction");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test(description = "Test final local variable without type initialized from a function")
    public void testLocalFinalValueWithoutTypeInitializedFromFunction() {
        Object returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithoutTypeInitializedFromFunction");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test(dataProvider = "finalLocalNoInitVarTests")
    public void testFinalLocalNoInitVar(String function) {
        BRunUtil.invoke(finalLocalNoInitVarResult, function);
    }

    @DataProvider(name = "finalLocalNoInitVarTests")
    public Object[][] finalLocalNoInitVarTests() {
        return new Object[][]{
                {"testSubsequentInitializationOfFinalVar"},
                {"testSubsequentInitializationOfFinalVarInBranches"}
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        finalLocalNoInitVarResult = null;
    }
}
