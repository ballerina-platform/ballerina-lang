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
 * Class to test block statements.
 */
public class BlockStmtTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/block/block-stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/block/block-stmt-negative.bal");
    }

    @Test
    public void blockStmtTest() {
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void testVariableShadowingInCurrentScope1() {
        BValue[] returns = BRunUtil.invoke(result, "test1");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testVariableShadowingInCurrentScope2() {
        BValue[] returns = BRunUtil.invoke(result, "test2");
        Assert.assertEquals(returns[0].stringValue(), "K17");
    }

    @Test(description = "Test block statement with errors")
    public void testBlockStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 10);
        //testUnreachableStmtInIfFunction1
        BAssertUtil.validateError(resultNegative, 0, "unreachable code", 9, 5);
        //testUnreachableStmtInIfFunction2
        BAssertUtil.validateError(resultNegative, 1, "unreachable code", 25, 5);
        //testUnreachableStmtInIfBlock
        BAssertUtil.validateError(resultNegative, 2, "unreachable code", 33, 9);
        //testUnreachableStmtInWhileBlock
        BAssertUtil.validateError(resultNegative, 3, "unreachable code", 46, 13);
        //testCommentAfterReturnStmt
        BAssertUtil.validateError(resultNegative, 4, "unreachable code", 62, 5);
        //testUnreachableTryCatch
        BAssertUtil.validateError(resultNegative, 5, "unreachable code", 73, 5);
        //testUnreachableNext
        BAssertUtil.validateError(resultNegative, 6, "unreachable code", 84, 9);
        //testUnreachableBreak
        BAssertUtil.validateError(resultNegative, 7, "unreachable code", 92, 9);
        BAssertUtil.validateError(resultNegative, 8, "break cannot be used outside of a loop", 92, 9);
        //testUnreachableThrow
        BAssertUtil.validateError(resultNegative, 9, "unreachable code", 107, 9);

    }
}
