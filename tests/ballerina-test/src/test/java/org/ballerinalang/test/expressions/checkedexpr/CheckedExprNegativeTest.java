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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class contains a set of negative test cases related to the checked operator
 */
public class CheckedExprNegativeTest {

    @Test
    public void testSemanticErrors() {
        CompileResult compile = BCompileUtil.compile(
                "test-src/expressions/checkedexpr/checked_expr_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 4);
        BAssertUtil.validateError(compile, 0, "invalid usage of the checked expression " +
                "operator: no expression type is equivalent to error type", 13, 25);
        BAssertUtil.validateError(compile, 1, "invalid usage of the checked expression " +
                "operator: all expression types are equivalent to error type", 17, 25);
        BAssertUtil.validateError(compile, 2, "invalid usage of the checked expression " +
                "operator: all expression types are equivalent to error type", 39, 25);
        BAssertUtil.validateError(compile, 3, "invalid usage of the checked expression " +
                "operator: no expression type is equivalent to error type", 47, 25);
    }
}
