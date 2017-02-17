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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test functionality of the return statements which in multiple code branches
 *
 * @since 0.8.0
 */
public class ReturnStmtInBranchTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/statements/returnstmt/return-stmt-in-branches.bal");
    }

    @Test(description = "Test Return statements in branches")
    public void testReturnStmtInBranches1() {
        BValue[] args = {new BInteger(12), new BInteger(13)};

        BValue[] returns = BLangFunctions.invoke(bLangProgram, "returnStmtBranch1", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
    }

    @Test(description = "Test Return statements in branches")
    public void testReturnStmtInBranches2() {
        BValue[] args = {new BInteger(9), new BInteger(10)};

        BValue[] returns = BLangFunctions.invoke(bLangProgram, "returnStmtBranch2", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 819);
    }

    public static void main(String[] args) {
        ReturnStmtInBranchTest returnStmtInBranchTest = new ReturnStmtInBranchTest();
        returnStmtInBranchTest.setup();
        returnStmtInBranchTest.testReturnStmtInBranches2();
    }
}
