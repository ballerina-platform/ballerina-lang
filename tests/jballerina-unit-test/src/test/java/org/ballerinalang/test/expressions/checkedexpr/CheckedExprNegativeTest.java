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
package org.ballerinalang.test.expressions.checkedexpr;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * This class contains a set of negative test cases related to the checked operator.
 */
public class CheckedExprNegativeTest {

    private static final String ERROR_MISMATCH_ERR_MSG = "invalid usage of the 'check' expression operator: no " +
            "matching error return type(s) in the enclosing invokable";
    private static final String WARN_FUNCTION_SHOULD_RETURN_NIL = "this function should explicitly return a value";

    @Test
    public void testSemanticErrors() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_semantics_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 3, compile.toString());
        BAssertUtil.validateError(compile, 0, "invalid usage of the 'check' expression " +
                "operator: no expression type is equivalent to error type", 6, 25);
        BAssertUtil.validateError(compile, 1, "incompatible types: expected '(string|error)'" +
                                              ", found '(string|int)'", 19, 25);
        BAssertUtil.validateError(compile, 2, "invalid expression, expected a call expression", 34, 11);
    }

    @Test
    public void testErrors() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 3, compile.toString());
        BAssertUtil.validateError(compile, 0, ERROR_MISMATCH_ERR_MSG, 11, 19);
        BAssertUtil.validateError(compile, 1, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 15, 19);
        BAssertUtil.validateError(compile, 2, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 25, 19);
    }

    @Test
    public void testSemanticErrorsWithResources() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_within_resource_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 1);
        BAssertUtil.validateError(compile, 0, ERROR_MISMATCH_ERR_MSG, 23, 22);
    }

    @Test
    public void testCheckedErrorvsReturnTypeMismatch() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_error_return_type_mismatch_negative.bal");
        int i = 0;
        validateWarning(compile, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 23, 24);
        BAssertUtil.validateError(compile, i++, ERROR_MISMATCH_ERR_MSG, 24, 13);
        validateWarning(compile, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 36, 23);
        validateWarning(compile, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 44, 23);
        BAssertUtil.validateError(compile, i++, ERROR_MISMATCH_ERR_MSG, 45, 17);
        validateWarning(compile, i++, WARN_FUNCTION_SHOULD_RETURN_NIL, 54, 34);
        BAssertUtil.validateError(compile, i++, ERROR_MISMATCH_ERR_MSG, 55, 23);
        BAssertUtil.validateError(compile, i++, ERROR_MISMATCH_ERR_MSG, 56, 13);
        BAssertUtil.validateError(compile, i++, ERROR_MISMATCH_ERR_MSG, 57, 20);
        BAssertUtil.validateError(compile, i++, ERROR_MISMATCH_ERR_MSG, 58, 23);
        Assert.assertEquals(compile.getErrorCount(), i - 4);
        Assert.assertEquals(compile.getWarnCount(), 4);
    }

    @Test
    public void testCheckedErrorsWithReadOnlyInUnionNegative() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expression_with_readonly_in_union_negative.bal");
        int index = 0;
        BAssertUtil.validateError(compile, index++, ERROR_MISMATCH_ERR_MSG, 23, 13);
        BAssertUtil.validateError(compile, index++, ERROR_MISMATCH_ERR_MSG, 29, 13);
        validateWarning(compile, index++, WARN_FUNCTION_SHOULD_RETURN_NIL, 35, 24);
        BAssertUtil.validateError(compile, index++, ERROR_MISMATCH_ERR_MSG, 37, 13);
        Assert.assertEquals(compile.getErrorCount(), index - 1);
        Assert.assertEquals(compile.getWarnCount(), 1);
    }
}
