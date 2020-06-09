/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.transaction;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Test cases for committed aborted clauses in TransactionStatement.
 */
@Test(groups = "brokenOnErrorChange")
public class TransactionBlockTest {

    private CompileResult programFile;
    private CompileResult negativeProgramFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/transaction/transaction_block_test.bal");
        negativeProgramFile = BCompileUtil.compile("test-src/statements/transaction/transaction_block_negative.bal");
    }

    private BValue[] runFunctionWithTxConfig(int txFailures, boolean abort) {
        BValue[] params = {new BInteger(txFailures), new BBoolean(abort)};
        return BRunUtil.invoke(programFile, "testTransactionStmtWithCommitedAndAbortedBlocks", params);
    }

    @Test
    public void testTransactionStmtWithnoAbortNoFailure() {
        BValue[] returns = runFunctionWithTxConfig(0, false);
        Assert.assertEquals(returns[0].stringValue(), "start fc-0 inTrx endTrx committed end");
    }

    @Test
    public void testTransactionStmtWithAbortNoFailure() {
        BValue[] returns = runFunctionWithTxConfig(0, true);
        Assert.assertEquals(returns[0].stringValue(), "start fc-0 inTrx aborting aborted end");
    }

    @Test
    public void testTransactionStmtWithNoAbortSingleFailure() {
        BValue[] returns = runFunctionWithTxConfig(1, false);
        Assert.assertEquals(returns[0].stringValue(), "start fc-1 inTrx blowUp retry inTrx endTrx committed end");
    }

    @Test
    public void testTransactionStmtWithnoAbortFailureFailure() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testTransactionFailing", params);
        Assert.assertEquals(result[0].getType().getTag(), TypeTags.ERROR);
        BError error = (BError) result[0];
        Assert.assertEquals(error.getReason(), "TransactionError");
    }

    @Test
    public void testTransactionStmtWithTrxStatementInCalleeFunctionErrorAbortTrapped() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "runtimeNestedTransactionErrorTraped", params);
        Assert.assertEquals(result[0].stringValue(), " in func trapped" +
                "[err: dynamically nested transactions are not allowed] outer-committed endTrx");
    }

    @Test
    public void testTransactionStmtWithTrxStatementInCalleeFunctionErrorAbort() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "runtimeNestedTransactionsError", params);
        Assert.assertEquals(result[0].stringValue(), " in func retry in func retry in func outer-aborted " +
                "[err: dynamically nested transactions are not allowed]");
    }

    @Test
    public void testAbortStatementWithNoAbortBlock() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testAbortStatementWithNoAbortBlock", params);
        Assert.assertEquals(result[0].stringValue(), "start in-trx end");
    }

    @Test
    public void testAbortStatementWithAbortBlock() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testAbortStatementWithAbortBlock", params);
        Assert.assertEquals(result[0].stringValue(), "start in-trx aborted end");
    }

    @Test
    public void testTrxSuccessWithAbortBlock() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testTrxSuccessWithAbortBlock", params);
        Assert.assertEquals(result[0].stringValue(), "start in-trx committed end");
    }

    @Test
    public void testAbortedCommitedBlockMixedUpAborted() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testAbortedCommittedBlockMixedUpAborted", params);
        Assert.assertEquals(result[0].stringValue(), "start in-trx aborted end");
    }

    @Test
    public void testAbortedCommitedBlockMixedUpCommitted() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testAbortedCommittedBlockMixedUpCommitted", params);
        Assert.assertEquals(result[0].stringValue(), "start in-trx committed end");
    }

    @Test
    public void testAbortedCommitedBlockMixedUpNoRetryBlockCommitted() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testAbortedCommittedBlockMixedUpNoRetryBlockCommitted", params);
        Assert.assertEquals(result[0].stringValue(), "start in-trx committed end");
    }

    @Test
    public void testAbortedCommitedBlockMixedUpNoRetryBlockAborted() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "testAbortedCommittedBlockMixedUpNoRetryBlockAborted", params);
        Assert.assertEquals(result[0].stringValue(), "start in-trx aborted end");
    }

    @Test
    public void multipleTrxSequenceSuccess() {
        String result = executeMultipleTrxSequence(false, false, false, false);
        Assert.assertEquals(result, "start in-trx-1 committed-1 end-1 in-trx-2 committed-2 end-2");
    }

    @Test
    public void multipleTrxSequenceAbortFirst() {
        String result = executeMultipleTrxSequence(true, false, false, false);
        Assert.assertEquals(result, "start in-trx-1 aborted-1 end-1 in-trx-2 committed-2 end-2");
    }

    @Test
    public void multipleTrxSequenceAbortSecond() {
        String result = executeMultipleTrxSequence(false, true, false, false);
        Assert.assertEquals(result, "start in-trx-1 committed-1 end-1 in-trx-2 aborted-2 end-2");
    }

    @Test
    public void multipleTrxSequenceAbortBoth() {
        String result = executeMultipleTrxSequence(true, true, false, false);
        Assert.assertEquals(result, "start in-trx-1 aborted-1 end-1 in-trx-2 aborted-2 end-2");
    }

    @Test
    public void multipleTrxSequenceFailFirst() {
        String result = executeMultipleTrxSequence(false, false, true, false);
        Assert.assertEquals(result, "start in-trx-1 retry-1 in-trx-1 committed-1 end-1 in-trx-2 committed-2 end-2");
    }

    @Test
    public void multipleTrxSequenceFailSecond() {
        String result = executeMultipleTrxSequence(false, false, false, true);
        Assert.assertEquals(result, "start in-trx-1 committed-1 end-1 in-trx-2 retry-2 in-trx-2 committed-2 end-2");
    }

    @Test
    public void multipleTrxSequenceFailBoth() {
        String result = executeMultipleTrxSequence(false, false, true, true);
        Assert.assertEquals(result, "start in-trx-1 retry-1 in-trx-1 committed-1 end-1 " +
                "in-trx-2 retry-2 in-trx-2 committed-2 end-2");
    }

    private String executeMultipleTrxSequence(boolean abort1, boolean abort2, boolean fail1, boolean fail2) {
        BValue[] params = {new BBoolean(abort1), new BBoolean(abort2),
                new BBoolean(fail1), new BBoolean(fail2)};
        BValue[] result = BRunUtil.invoke(programFile, "multipleTrxSequence", params);
        return result[0].stringValue();
    }

    @Test
    public void testTransactionStmtWithFailureAndAbort() {
        BValue[] returns = runFunctionWithTxConfig(1, true);
        Assert.assertEquals(returns[0].stringValue(), "start fc-1 inTrx blowUp retry inTrx aborting aborted end");
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
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 44L);
    }

    @Test
    public void testAssignmentToUninitializedVariableOfOuterScopeFromTrxBlock() {
        BValue[] result = BRunUtil.invoke(programFile, "testAssignmentToUninitializedVariableOfOuterScopeFromTrxBlock");
        Assert.assertEquals(result[0].stringValue(), "init-in-transaction-block");
    }

    @Test
    public void testMultipleCommittedAbortedBlocks() {
        int i = 0;
        BAssertUtil.validateError(negativeProgramFile, i++,
                "transaction statement cannot be nested within another transaction block", 17, 9);
        BAssertUtil.validateError(negativeProgramFile, i++,
                "'check' expression cannot be used within transaction block", 31, 17);

        Assert.assertEquals(negativeProgramFile.getErrorCount(), i);
    }
}
