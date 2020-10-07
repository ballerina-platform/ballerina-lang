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
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in TransactionStatement.
 */
@Test(groups = {"disableOnOldParser"})
public class TransactionStmtTest {

    private CompileResult programFile, resultNegative, trxHandlersNegative;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/transaction/transaction_stmt.bal");
        resultNegative = BCompileUtil.compile("test-src/statements/transaction/transaction_stmt_negative.bal");
        trxHandlersNegative = BCompileUtil.compile("test-src/statements/transaction/transaction_handlers_negative.bal");
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
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 44L);
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
    public void testTransactionalInvoWithinMultiLevelFunc() {
        BValue[] result = BRunUtil.invoke(programFile, "testTransactionalInvoWithinMultiLevelFunc");
        Assert.assertEquals(result[0].stringValue(), "trxStarted -> within transactional func2 " +
                "-> within transactional func1 -> trxEnded.");
    }

    @Test
    public void testNewStrandWithTransactionalFunc() {
        BRunUtil.invoke(programFile, "testNewStrandWithTransactionalFunc");
    }

    @Test
    public void testRollbackWithBlockFailure() {
        BRunUtil.invoke(programFile, "testRollbackWithBlockFailure");
    }

    @Test
    public void testRollbackWithCommitFailure() {
        BRunUtil.invoke(programFile, "testRollbackWithCommitFailure");
    }

    @Test(description = "Test transaction statement with errors")
    public void testTransactionNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 28);
        BAssertUtil.validateError(resultNegative, 0, "invalid transaction commit count",
                6, 5);
        BAssertUtil.validateError(resultNegative, 1, "rollback not allowed here",
                12, 9);
        BAssertUtil.validateError(resultNegative, 2, "transaction statement cannot be used " +
                        "within a transactional scope", 21, 5);
        BAssertUtil.validateError(resultNegative, 3, "usage of start within a transactional " +
                "scope is prohibited", 30, 19);
        BAssertUtil.validateError(resultNegative, 4, "usage of start within a transactional " +
                "scope is prohibited", 39, 21);
        BAssertUtil.validateError(resultNegative, 5, "invoking transactional function outside " +
                        "transactional scope is prohibited", 41, 15);
        BAssertUtil.validateError(resultNegative, 6, "commit not allowed here",
                60, 17);
        BAssertUtil.validateError(resultNegative, 7, "invoking transactional function outside " +
                        "transactional scope is prohibited", 71, 21);
        BAssertUtil.validateError(resultNegative, 8, "commit not allowed here",
                74, 17);
        BAssertUtil.validateError(resultNegative, 9, "rollback not allowed here",
                97, 17);
        BAssertUtil.validateError(resultNegative, 10, "rollback not allowed here",
                101, 9);
        BAssertUtil.validateError(resultNegative, 11, "commit not allowed here",
                103, 17);
        BAssertUtil.validateError(resultNegative, 12, "invalid transaction commit count",
                117, 9);
        BAssertUtil.validateError(resultNegative, 13, "break statement cannot be used to " +
                        "exit from a transaction without a commit or a rollback statement",
                119, 17);
        BAssertUtil.validateError(resultNegative, 14, "commit cannot be used outside " +
                "a transaction statement", 123, 13);
        BAssertUtil.validateError(resultNegative, 15, "continue statement cannot be used " +
                "to exit from a transaction without a commit or a rollback statement", 133, 17);
        BAssertUtil.validateError(resultNegative, 16, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 148, 17);
        BAssertUtil.validateError(resultNegative, 17, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 165, 13);
        BAssertUtil.validateError(resultNegative, 18, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 185, 21);
        BAssertUtil.validateError(resultNegative, 19, "return statement cannot be used to exit " +
                "from a transaction without a commit or a rollback statement", 189, 21);
        BAssertUtil.validateError(resultNegative, 20, "invoking transactional function outside " +
                "transactional scope is prohibited", 208, 16);
        BAssertUtil.validateError(resultNegative, 21, "invoking transactional function outside " +
                "transactional scope is prohibited", 237, 9);
        BAssertUtil.validateError(resultNegative, 22, "invoking transactional function outside " +
                "transactional scope is prohibited", 238, 9);
        BAssertUtil.validateError(resultNegative, 23, "invoking transactional function outside " +
                "transactional scope is prohibited", 239, 34);
        BAssertUtil.validateError(resultNegative, 24, "invoking transactional function outside " +
                "transactional scope is prohibited", 241, 17);
        BAssertUtil.validateError(resultNegative, 25, "invoking transactional function outside " +
                "transactional scope is prohibited", 274, 17);
        BAssertUtil.validateError(resultNegative, 26, "invoking transactional function outside " +
                "transactional scope is prohibited", 291, 27);
        BAssertUtil.validateError(resultNegative, 27, "invoking transactional function outside " +
                "transactional scope is prohibited", 292, 26);
    }

    @Test(description = "Test incompatible transaction handlers")
    public void testIncompatibleTrxHandlers() {
        Assert.assertEquals(trxHandlersNegative.getErrorCount(), 2);
        BAssertUtil.validateError(trxHandlersNegative, 0, "incompatible types: expected " +
                        "'function (ballerina/lang.transaction:0.0.1:Info,error?,boolean) returns ()', " +
                        "found 'function (boolean) returns ()'",
                16, 33);
        BAssertUtil.validateError(trxHandlersNegative, 1, "incompatible types: expected " +
                        "'function (ballerina/lang.transaction:0.0.1:Info) returns ()', found " +
                        "'function (string) returns ()'",
                17, 31);
    }

    @Test
    public void testInvokeRemoteTransactionalMethodInTransactionalScope() {
        BRunUtil.invoke(programFile, "testInvokeRemoteTransactionalMethodInTransactionalScope");
    }

    @Test
    public void testAsyncReturn() {
        BValue[] result = BRunUtil.invoke(programFile, "testAsyncReturn");
        Assert.assertTrue(result[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) result[0]).intValue(), 10);
    }
}
