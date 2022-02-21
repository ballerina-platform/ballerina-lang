/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.returnstmt;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test functionality of the return statements which in multiple code branches.
 *
 * @since 0.8.0
 */
public class ReturnStmtInBranchTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/returnstmt/return-stmt-in-branches.bal");
    }

    @Test(description = "Test Return statements in branches")
    public void testReturnStmtInBranches1() {
        Object[] args = {(12), (13)};

        Object returns = BRunUtil.invoke(compileResult, "returnStmtBranch1", args);

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 100L);
    }

    @Test(description = "Test Return statements in branches")
    public void testReturnStmtInBranches2() {
        Object[] args = {(9), (10)};

        Object returns = BRunUtil.invoke(compileResult, "returnStmtBranch2", args);

        Assert.assertSame(returns.getClass(), Long.class);

        Assert.assertEquals(returns, 819L);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
