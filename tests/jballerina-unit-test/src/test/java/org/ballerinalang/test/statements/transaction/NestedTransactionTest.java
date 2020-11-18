/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.statements.transaction;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in Nested transactions.
 */

public class NestedTransactionTest {

    private CompileResult programFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/transaction/nested_transaction_test.bal");
    }

    @Test
    public void testRollback() {
        BRunUtil.invoke(programFile, "testRollback");
    }

    @Test
    public void testCommit() {
        BRunUtil.invoke(programFile, "testCommit");
    }

    @Test
    public void testPanic() {
        BRunUtil.invoke(programFile, "testPanic");
    }

    @Test
    public void testMultipleTrxBlocks() {
        BRunUtil.invoke(programFile, "testMultipleTrxBlocks");
    }

    @Test
    public void testTransactionHandlers() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testTrxHandlers", params);
        Assert.assertEquals(result[0].stringValue(), "started within transactional func trxCommited endTrx");
    }

    @Test
    public void testTransactionInsideIfStmt() {
        BValue[] returns = BRunUtil.invoke(programFile, "testTransactionInsideIfStmt");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 18L);
    }

    @Test
    public void testArrowFunctionInsideTransaction() {
        BValue[] returns = BRunUtil.invoke(programFile, "testArrowFunctionInsideTransaction");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 72L);
    }

    @Test
    public void testAssignmentToUninitializedVariableOfOuterScopeFromTrxBlock() {
        BValue[] result = BRunUtil.invoke(programFile,
                "testAssignmentToUninitializedVariableOfOuterScopeFromTrxBlock");
        Assert.assertEquals(result[0].stringValue(), "init-in-transaction-block");
    }

    @Test
    public void testTrxReturnVal() {
        BValue[] result = BRunUtil.invoke(programFile, "testTrxReturnVal");
        Assert.assertEquals(result[0].stringValue(), "start within transaction end.");
    }

    @Test
    public void testMultipleTrxReturnVal() {
        BValue[] result = BRunUtil.invoke(programFile, "testMultipleTrxReturnVal");
        Assert.assertEquals(result[0].stringValue(), "start -> within transaction 1 " +
                "-> within transaction 2 -> within transaction 3 -> returned.");
    }

    @Test
    public void testInvokingMultipleTrx() {
        BValue[] result = BRunUtil.invoke(programFile, "testInvokingTrxFunc");
        Assert.assertEquals(result[0].stringValue(), "start within transaction end.");
    }

    @Test
    public void testTransactionLangLibFunction() {
        BRunUtil.invoke(programFile, "testTransactionLangLib");
    }

    @Test
    public void testWithinTrxMode() {
        BValue[] result = BRunUtil.invoke(programFile, "testWithinTrxMode");
        Assert.assertEquals(result[0].stringValue(), "trxStarted -> within invoked function "
                + "-> strand in transactional mode -> invoked function returned -> strand in transactional mode "
                + "-> trxCommited -> strand in non-transactional mode -> trxEnded.");
    }

    @Test
    public void testUnreachableCode() {
        BValue[] result = BRunUtil.invoke(programFile, "testUnreachableCode");
        Assert.assertEquals(result[0].stringValue(), "trxStarted -> trxCommited -> trxEnded.");
    }

    @Test
    public void testNestedReturns() {
        BRunUtil.invoke(programFile, "testNestedReturns");
    }
}
