/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.expressionstmt;


import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for {@link org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt}.
 */
public class ExpressionStmtTest {

    @Test
    public void testIgnoredAssignment() {
        CompileResult result = BCompileUtil.compile("test-src/statements/expression/ignore-values-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "variable assignment is required", 19, 5);
        BAssertUtil.validateError(result, 1, "variable assignment is required", 20, 5);
    }

    @Test
    public void testInvalidStatements() {
        CompileResult result = BCompileUtil.compile("test-src/statements/expression/expression-stmt-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "undefined symbol 'continue'", 7, 13);
        BAssertUtil.validateError(result, 1, "undefined symbol 'foo'", 14, 5);
        BAssertUtil.validateError(result, 2, "undefined function 'bar'", 15, 5);
        BAssertUtil.validateError(result, 3, "variable assignment is required", 16, 5);
    }

    @Test
    public void testInvalid2Statements() {
        CompileResult result = BCompileUtil.compile("test-src/statements/expression/expression-stmt2-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "variable assignment is required", 3, 5);
        BAssertUtil.validateError(result, 1, "variable assignment is required", 5, 5);
        BAssertUtil.validateError(result, 2, "variable assignment is required", 7, 5);
        BAssertUtil.validateError(result, 3, "invalid statement", 9, 5);
    }

}
