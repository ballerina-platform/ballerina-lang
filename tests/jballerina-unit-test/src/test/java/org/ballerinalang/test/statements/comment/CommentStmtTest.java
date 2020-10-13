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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        compiledPackage = BCompileUtil.compileAndGetPackage("test-src/statements/comment/comments.bal",
                                                            CompilerPhase.BIR_GEN);
    }

    @Test (enabled = false)
    public void commentsTest() {
        Assert.assertEquals(result.getErrorCount(), 0);
        List<BLangStatement> statements = ((BLangBlockFunctionBody) compiledPackage.functions.get(0).body)
                .getStatements();
        Assert.assertNotNull(statements, "statements not found");
        //since return statement is added at desugar phase, expected statement count is 8
        Assert.assertEquals(statements.size(), 8, "statement count mismatched");

        statements = ((BLangBlockFunctionBody) compiledPackage.functions.get(1).body).getStatements();
        Assert.assertNotNull(statements, "statements not found");
        Assert.assertEquals(statements.size(), 3, "statement count mismatched");
    }
}
