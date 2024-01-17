/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.bala.literals;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for numeric literals in ballerina.
 */
public class NumericLiteralNegativeTest {
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_numeric_literals");
        negativeResult = BCompileUtil
                .compile("test-src/bala/test_bala/literals/test_numeric_literal_negative_test.bal");
    }

    @Test(description = "Test numeric literal assignment statement with errors")
    public void testNumericLiteralAssignmentNegativeCases() {
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo', found 'float'", 20, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo', found 'float'", 23, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo', found 'float'", 24, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo', found 'float'", 26, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo2', found 'int'", 30, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo2', found 'int'", 31, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo2', found 'int'", 32, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo2', found 'int'", 33, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo2', found 'int'", 35, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo2', found 'float'", 36, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo3', found 'float'", 39, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo3', found 'float'", 40, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo3', found 'float'", 41, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo3', found 'float'", 42, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'int'", 50, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 51, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 52, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 53, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 54, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 56, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'int'", 59, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 60, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 61, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 64, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'int'", 67, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 68, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 69, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 72, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'int'", 75, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 76, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 77, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 78, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 80, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 85, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 86, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 88, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 89, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 90, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'float'", 92, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'numericliteral/testproject:0.1.0:Foo5', found 'int'", 93, 22);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }
}
