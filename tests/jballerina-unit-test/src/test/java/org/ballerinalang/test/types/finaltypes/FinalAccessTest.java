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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
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
        Assert.assertEquals(compileResultNegative.getErrorCount(), 8);
        BAssertUtil.validateError(compileResultNegative, 0, "cannot assign a value to function argument 'a'", 11, 5);
        BAssertUtil.validateError(compileResultNegative, 1, "cannot assign a value to function argument 'a'", 17, 5);
        BAssertUtil.validateError(compileResultNegative, 2, "cannot assign a value to function argument 'f'", 22, 5);
        BAssertUtil.validateError(compileResultNegative, 3, "cannot assign a value to function argument 's'", 23, 5);
        BAssertUtil.validateError(compileResultNegative, 4, "cannot assign a value to function argument 'b'", 24, 5);
        BAssertUtil.validateError(compileResultNegative, 5, "cannot assign a value to function argument 'j'", 25, 5);
        BAssertUtil.validateError(compileResultNegative, 6, "cannot assign a value to function argument 'a'", 38, 5);
        BAssertUtil.validateError(compileResultNegative, 7, "invalid assignment: 'listener' declaration is final",
                                  45, 5);
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
        BAssertUtil.validateError(resultNegative, i++, "cannot assign a value to final 's'", 82, 5);
        BAssertUtil.validateError(resultNegative, i++, "variable 'i' may not have been initialized", 94, 13);
        BAssertUtil.validateWarning(resultNegative, i++, "unused variable 't'", 101, 5);

        Assert.assertEquals(resultNegative.getDiagnostics().length, i);
    }

    @Test(description = "Test final global variable")
    public void testFinalAccess() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testFinalAccess");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);

        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 100);

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 10);

        Assert.assertTrue(returns[3] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 100);
    }

    @Test(description = "Test final global variable")
    public void testFinalStringAccess() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testFinalStringAccess");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals((returns[0]).stringValue(), "hello");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals((returns[1]).stringValue(), "world");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals((returns[2]).stringValue(), "hello");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals((returns[3]).stringValue(), "world");
    }

    @Test(description = "Test final global variable as a parameter")
    public void testFinalFieldAsParameter() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFinalFieldAsParameter");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test(description = "Test final parameter")
    public void testFieldAsFinalParameter() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFieldAsFinalParameter");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
    }

    @Test(description = "Test final local variable with type")
    public void testLocalFinalValueWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithType");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test(description = "Test final local variable without type")
    public void testLocalFinalValueWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithoutType");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test(description = "Test final local variable with type initialized from a function")
    public void testLocalFinalValueWithTypeInitializedFromFunction() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithTypeInitializedFromFunction");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test(description = "Test final local variable without type initialized from a function")
    public void testLocalFinalValueWithoutTypeInitializedFromFunction() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLocalFinalValueWithoutTypeInitializedFromFunction");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
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
