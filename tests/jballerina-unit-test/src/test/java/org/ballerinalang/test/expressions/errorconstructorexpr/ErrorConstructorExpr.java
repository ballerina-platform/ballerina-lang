/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.errorconstructorexpr;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test error-constructor-expr
 */
public class ErrorConstructorExpr {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setUp() {
        result = BCompileUtil.compile("test-src/expressions/errorconstructorexpr/error-constructor-expr.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/errorconstructorexpr/error-constructor-expr" +
                "-negative.bal");
    }

    @Test
    public void testErrorConstructorExpr1() {
        BRunUtil.invoke(result, "testErrorConstructorExpr1");
    }

    @Test
    public void testErrorConstructorExpr2() {
        BRunUtil.invoke(result, "testErrorConstructorExpr2");
    }

    @Test
    public void testErrorConstructorExpr3() {
        BRunUtil.invoke(result, "testErrorConstructorExpr3");
    }

    @Test
    public void testErrorConstructorExpr4() {
        BRunUtil.invoke(result, "testErrorConstructorExpr4");
    }

    @Test
    public void testErrorConstructorExpr5() {
        BRunUtil.invoke(result, "testErrorConstructorExpr5");
    }

    @Test
    public void testErrorConstructorExpr6() {
        BRunUtil.invoke(result, "testErrorConstructorExpr6");
    }

    @Test
    public void testErrorConstructorExpr7() {
        BRunUtil.invoke(result, "testErrorConstructorExpr7");
    }

    @Test
    public void testErrorConstructorExpr8() {
        BRunUtil.invoke(result, "testErrorConstructorExpr8");
    }

    @Test
    public void testErrorConstructorExpr9() {
        BRunUtil.invoke(result, "testErrorConstructorExpr9");
    }

    @Test
    public void testErrorConstructorExprNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "missing arg within parenthesis", 18, 21);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int'", 19, 22);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'string', found 'error'", 20, 22);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'error?', found 'string'", 21, 28);
        BAssertUtil.validateError(negativeResult, i++, "additional positional arg in error constructor", 22, 27);
        BAssertUtil.validateError(negativeResult, i++, "undefined error type descriptor 'MyError'", 29, 20);
        BAssertUtil.validateError(negativeResult, i++, "invalid arg type in error detail field 'c', expected " +
                "'string', found 'int'", 30, 46);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }
}
