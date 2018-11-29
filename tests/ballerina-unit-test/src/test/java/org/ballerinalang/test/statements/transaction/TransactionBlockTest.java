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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Test cases for committed aborted clauses in TransactionStatement.
 */
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
        Assert.assertEquals(error.reason, "TransactionError");
    }

    @Test
    public void testTransactionStmtWithTrxStatementInCalleeFunctionErrorAbortTrapped() {
        BValue[] params = {};
        BValue[] result = BRunUtil.invoke(programFile, "rentimeNestedTransactionErrorTraped", params);
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
    public void testTransactionStmtWithFailureAndAbort() {
        BValue[] returns = runFunctionWithTxConfig(1, true);
        Assert.assertEquals(returns[0].stringValue(), "start fc-1 inTrx blowUp retry inTrx aborting aborted end");
    }

    @Test
    public void testMultipleCommittedAbortedBlocks() {
        Assert.assertEquals(negativeProgramFile.getErrorCount(), 3);
        BAssertUtil.validateError(negativeProgramFile, 0,
                "only one aborted block is allowed per transaction statement", 21, 7);
        BAssertUtil.validateError(negativeProgramFile, 1,
                "only one committed block is allowed per transaction statement", 34, 7);
        BAssertUtil.validateError(negativeProgramFile, 2,
                "transaction statement cannot be nested within another transaction block", 43, 9);
    }
}
