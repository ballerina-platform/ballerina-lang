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
package org.wso2.ballerina.lang.statements;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Test class to test the comment statement in ballerina.
 * function testCommentStmt() {
 *    int a = 10;
 *    //comment1
 *    int b = 20;
 *    //comment2
 *  }
 */

public class CommentStmtTest {

    @Test(description = "Test the comment statement in the function body")
    public void testCommentInFunctionBody() {
        BallerinaFile bFile = ParserUtils.parseBalFile("lang/statements/comment/comments-in-function-body.bal");
        Statement[] statements = bFile.getFunctions()[0].getCallableUnitBody().getStatements();
        Assert.assertNotNull(statements, "statements not found");
        Assert.assertEquals(statements.length, 4, "statement count mismatched");
        Assert.assertTrue(statements[1] instanceof CommentStmt, "Not a comment statement");
        Assert.assertTrue(statements[3] instanceof CommentStmt, "Not a comment statement");
        Assert.assertEquals(((CommentStmt) statements[1]).getComment(), "//comment1", "comment text mismatched");
        Assert.assertEquals(((CommentStmt) statements[3]).getComment(), "//comment2", "comment text mismatched");
    }

    @Test(description = "Test the error message when a comment is not inside a function block",
          expectedExceptions = { ParseCancellationException.class },
          expectedExceptionsMessageRegExp = "comment-in-invalid-location.bal:1:0: unwanted token '//invalid .*")
    public void testCommentInInvalidLocation() {
        ParserUtils.parseBalFile("lang/statements/comment/comment-in-invalid-location.bal");
    }
}
