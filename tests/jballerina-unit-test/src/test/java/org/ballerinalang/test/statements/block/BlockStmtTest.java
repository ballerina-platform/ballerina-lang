/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.block;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test block statements.
 */
public class BlockStmtTest {

    private CompileResult result, resultNegative, resultSemanticsNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/block/block-stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/block/block-stmt-negative.bal");
        resultSemanticsNegative = BCompileUtil.compile("test-src/statements/block/block-stmt-semantics-negative.bal");
    }

    @Test
    public void blockStmtTest() {
        Assert.assertEquals(result.getErrorCount(), 0);
    }


    @Test
    public void testVariableShadowingBasic() {
        BValue[] returns = BRunUtil.invoke(result, "test1");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 9);
    }

    @Test
    public void testVariableShadowingInCurrentScope1() {
        BValue[] returns = BRunUtil.invoke(result, "test2");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 8);
    }

    @Test
    public void testVariableShadowingInCurrentScope2() {
        BValue[] returns = BRunUtil.invoke(result, "test3");
        Assert.assertEquals(returns[0].stringValue(), "K25");
    }

    @Test
    public void testScopeOfBlock() {
        BRunUtil.invoke(result, "testScopeOfBlock");
    }

    @Test
    public void testStmtInBlock() {
        BRunUtil.invoke(result, "testStmtInBlock");
    }

    @Test
    public void testReturnStmtLocationInBlock() {
        BRunUtil.invoke(result, "testReturnStmtLocationInBlock");
    }

    @Test(description = "Test block statement with errors")
    public void testBlockStmtSemanticsNegative() {
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), 3);
        BAssertUtil.validateError(resultSemanticsNegative, 0, "redeclared symbol 'value'", 8, 9);
        BAssertUtil.validateError(resultSemanticsNegative, 1, "redeclared symbol 'value'", 19, 13);
        BAssertUtil.validateError(resultSemanticsNegative, 2, "redeclared symbol 'value'", 31, 17);
    }

    @Test(description = "Test block statement with errors")
    public void testBlockStmtNegativeCases() {
        int index = 0;
        //testUnreachableStmtInIfFunction1
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 9, 5);

        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'b'", 17, 5);

        //testUnreachableStmtInIfFunction2
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 25, 5);
        //testUnreachableStmtInIfBlock
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 33, 9);
        //testUnreachableStmtInWhileBlock
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 46, 13);
        //testCommentAfterReturnStmt
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 62, 5);
        //testUnreachableTrapExpression
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 73, 5);
        //testUnreachableNext
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 84, 9);
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 86, 5);
        //testUnreachableBreak
        BAssertUtil.validateError(resultNegative, index++, "break cannot be used outside of a loop", 92, 9);
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 92, 9);
        //testUnreachableThrow
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 107, 9);
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 116, 9);
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 126, 9);
        //testUninitializedVariableAssignInBlock
        BAssertUtil.validateError(resultNegative, index++, "variable 'a' is not initialized", 136, 17);
        BAssertUtil.validateError(resultNegative, index++, "variable 'a' is not initialized", 143, 9);

        Assert.assertEquals(resultNegative.getDiagnostics().length, index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
        resultSemanticsNegative = null;
    }
}
