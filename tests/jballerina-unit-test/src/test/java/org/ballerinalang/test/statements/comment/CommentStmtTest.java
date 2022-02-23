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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;

import java.util.List;

/**
 * Class to test comments.
 */
@Test
public class CommentStmtTest {

    private CompileResult result;
    private BLangPackage compiledPackage;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/comment/comments.bal");
        CompileResult compile = BCompileUtil.compile("test-src/statements/comment/comments.bal");
        compiledPackage = (BLangPackage) compile.getAST();
    }

    @Test
    public void commentsTest() {
        Assert.assertEquals(result.getErrorCount(), 0);
        List<BLangStatement> statements = ((BLangBlockFunctionBody) compiledPackage.functions.get(0).body)
                .getStatements();
        Assert.assertNotNull(statements, "statements not found");
        // Return statement is added at desugar phase and an empty block statement is added internally
        // following the `if` without an `else`. Therefore the statement count is 9.
        Assert.assertEquals(statements.size(), 9, "statement count mismatched");

        statements = ((BLangBlockFunctionBody) compiledPackage.functions.get(1).body).getStatements();
        Assert.assertNotNull(statements, "statements not found");
        Assert.assertEquals(statements.size(), 3, "statement count mismatched");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        compiledPackage = null;
    }
}
