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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class contains a set of negative test cases related to the checked operator.
 */
public class CheckedExprNegativeTest {

    @Test
    public void testSemanticErrors() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 5, compile.toString());
        BAssertUtil.validateError(compile, 0, "invalid usage of the 'check' expression " +
                "operator: no expression type is equivalent to error type", 11, 25);
        BAssertUtil.validateError(compile, 1, "invalid usage of the 'check' expression " +
                "operator: all expression types are equivalent to error type", 16, 25);
        BAssertUtil.validateError(compile, 2, "invalid usage of the 'check' expression " +
                "operator: all expression types are equivalent to error type", 30, 25);
        BAssertUtil.validateError(compile, 3, "incompatible types: expected '(string|error)'" +
                                              ", found '(string|int)'", 39, 25);
        BAssertUtil.validateError(compile, 4, "invalid usage of the 'check' expression " +
                "operator: no error type return in enclosing invokable", 49, 19);
    }

    @Test
    public void testSemanticErrorsWithResources() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_within_resource_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 1);
        BAssertUtil.validateError(compile, 0, "invalid usage of the 'check' expression " +
                "operator: no error type return in enclosing invokable", 28, 22);
    }
}
