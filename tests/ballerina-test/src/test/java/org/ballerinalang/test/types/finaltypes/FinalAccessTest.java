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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for final fields.
 */
public class FinalAccessTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/finaltypes/final-field-test.bal");
    }

    @Test(description = "Test final field access failures")
    public void testFinalFailCase() {
        CompileResult compileResultNegative = BCompileUtil
                .compile("test-src/types/finaltypes/final-field-test-negative.bal");
        Assert.assertEquals(compileResultNegative.getErrorCount(), 11);
        BAssertUtil.validateError(compileResultNegative, 0, "cannot assign a value to final 'globalFinalInt'", 9, 5);
        BAssertUtil.validateError(compileResultNegative, 1, "cannot assign a value to final 'bar:globalBarInt'", 11, 5);
        BAssertUtil
                .validateError(compileResultNegative, 2, "cannot assign a value to final 'bar:globalBarString'", 13, 5);
        BAssertUtil.validateError(compileResultNegative, 3, "cannot assign a value to final 'a'", 25, 5);
        BAssertUtil.validateError(compileResultNegative, 4, "cannot assign a value to function argument 'a'", 31, 5);
        BAssertUtil.validateError(compileResultNegative, 5, "cannot assign a value to function argument 'f'", 36, 5);
        BAssertUtil.validateError(compileResultNegative, 6, "cannot assign a value to function argument 's'", 37, 5);
        BAssertUtil.validateError(compileResultNegative, 7, "cannot assign a value to function argument 'b'", 38, 5);
        BAssertUtil.validateError(compileResultNegative, 8, "cannot assign a value to function argument 'j'", 39, 5);
        BAssertUtil.validateError(compileResultNegative, 9,
                "annotation 'ballerina/builtin:final' is not allowed in function", 43, 1);
        BAssertUtil.validateError(compileResultNegative, 10,
                "annotation 'ballerina/builtin:final' is not allowed in service", 47, 1);
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

    @Test(description = "Test final global variable as a paramter")
    public void testFinalFieldAsParameter() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testFinalFieldAsParameter");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test(description = "Test final paramter")
    public void testFieldAsFinalParameter() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testFieldAsFinalParameter");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
    }
}
