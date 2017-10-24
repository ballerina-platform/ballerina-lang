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
package org.ballerinalang.test.statements.comment;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.tree.NodeKind;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;

import java.util.List;

public class CommentStmtTest {

    CompileResult result;
    CompileResult resultNegative;
    BLangPackage compiledPackage;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/comment/comment-stmt.bal");
        compiledPackage = BCompileUtil.compileAndGetPackage("test-src/statements/comment/comment-stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/comment/comment-stmt-negative.bal");
    }

    @Test
    public void commentStmtTest() {
        Assert.assertEquals(result.getErrorCount(), 0);
        List<BLangStatement> statements = compiledPackage.functions.get(0).body.getStatements();
        Assert.assertNotNull(statements, "statements not found");
        //since return statement is added at desugar phase, expected statement count is 5
        Assert.assertEquals(statements.size(), 5, "statement count mismatched");
        Assert.assertEquals(statements.get(0).getKind(), NodeKind.COMMENT, "1st statement is not a comment statement");
        Assert.assertEquals(statements.get(2).getKind(), NodeKind.COMMENT, "1st statement is not a comment statement");
        Assert.assertEquals(((BLangComment) statements.get(0)).getComment(), "//comment1");
        Assert.assertEquals(((BLangComment) statements.get(2)).getComment(), "//comment2");
    }

    @Test(description = "Test comment statement with errors")
    public void testCommentStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "extraneous input '//invalid location to have a comments'", 1, 1);
    }
}
