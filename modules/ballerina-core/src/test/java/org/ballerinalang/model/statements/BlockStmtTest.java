/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * Test class for block statements.
 */
public class BlockStmtTest {

    @Test(description = "Testing the valid block with return statement")
    public void testReturnStmtLocationValidity() {
        BTestUtils.parseBalFile("lang/statements/block/valid-block-with-return.bal");
    }

    @Test(description = "Testing the comment statement after return")
    public void testCommentStmtAfterReturnStmt() {
        BTestUtils.parseBalFile("lang/statements/block/comment-after-return-stmt.bal");
    }

    @Test(description = "Testing the unreachable statement in function block",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "function-unreachable-stmt1.bal:9: unreachable statement")
    public void testUnreachableStmtInFunction1() {
        BTestUtils.parseBalFile("lang/statements/block/function-unreachable-stmt1.bal");
    }

    @Test(description = "Testing the unreachable statement in function block",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "function-unreachable-stmt2.bal:11: unreachable statement")
    public void testUnreachableStmtInFunction2() {
        BTestUtils.parseBalFile("lang/statements/block/function-unreachable-stmt2.bal");
    }

    @Test(description = "Testing the unreachable statement in if block",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "ifblock-unreachable-stmt.bal:6: unreachable statement")
    public void testUnreachableStmtInIfBlock() {
        BTestUtils.parseBalFile("lang/statements/block/ifblock-unreachable-stmt.bal");
    }

    @Test(description = "Testing the unreachable statement in while block",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "whileblock-unreachable-stmt.bal:7: unreachable statement")
    public void testUnreachableStmtInWhileBlock() {
        BTestUtils.parseBalFile("lang/statements/block/whileblock-unreachable-stmt.bal");
    }

    @Test(description = "Testing the unreachable statement after comment statement",
          expectedExceptions = SemanticException.class,
          expectedExceptionsMessageRegExp = "unreachable-stmt-after-comment.bal:12: unreachable statement")
    public void testUnreachableStmtAfterComment() {
        BTestUtils.parseBalFile("lang/statements/block/unreachable-stmt-after-comment.bal");
    }
}
