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
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class to test the comment statement in ballerina.
 * function testCommentStmt() {
 * //comment1
 * int a = 10;
 * //comment2
 * int b = 20;
 * }
 */

public class CommentStmtTest {

    @Test(description = "Test the comment statement in the function body")
    public void testCommentInFunctionBody() {
        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/statements/comment/comments-in-function-body.bal");
        Statement[] statements = bLangProgram.getLibraryPackages()[0]
                .getFunctions()[0].getCallableUnitBody().getStatements();
        Assert.assertNotNull(statements, "statements not found");
        Assert.assertEquals(statements.length, 4, "statement count mismatched");
        Assert.assertTrue(statements[0] instanceof CommentStmt, "1st statement is not a comment statement");
        Assert.assertTrue(statements[2] instanceof CommentStmt, "3rd statement is not a comment statement");
        Assert.assertEquals(((CommentStmt) statements[0]).getComment(), "//comment1", "comment text mismatched");
        Assert.assertEquals(((CommentStmt) statements[2]).getComment(), "//comment2", "comment text mismatched");
    }

    @Test(description = "Test the error message when a comment is not inside a function block",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "comment-in-invalid-location.bal:1:0: unwanted token '//invalid .*")
    public void testCommentInInvalidLocation() {
        BTestUtils.parseBalFile("lang/statements/comment/comment-in-invalid-location.bal");
    }
}
