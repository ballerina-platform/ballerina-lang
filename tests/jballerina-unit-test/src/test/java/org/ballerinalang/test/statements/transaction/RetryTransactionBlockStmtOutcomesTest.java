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

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to validate retry transaction block statement outcomes.
 *
 * @since Swan Lake
 */
@Test(groups = {"disableOnOldParser"})
public class RetryTransactionBlockStmtOutcomesTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/transaction/" +
                "retry_transaction_block_stmt_outcomes.bal");
    }

    @Test(description = "Test commit success and success in block stmt outcome")
    public void testCommitSuccessWithSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testCommitSuccessWithSuccessOutcome");
    }

    @Test
    public void testCommitSuccessWithNoRetryFailOutcome() {
        BValue[] result = BRunUtil.invoke(compileResult, "testCommitSuccessWithNoRetryFailOutcome");
        Assert.assertTrue(result[0] instanceof BError);
        Assert.assertEquals(((BError) result[0]).getMessage(), "Error in block statement");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Error in increment count.*",
            description = "Test commit success and panic in block stmt outcome")
    public void testCommitSuccessWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testCommitSuccessWithPanicOutcome");
    }

    @Test(description = "Test commit fail and unusual success in block stmt outcome")
    public void testCommitFailWithUnusualSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testCommitFailWithUnusualSuccessOutcome");
    }

    @Test(description = "Test commit fail and fail in block stmt outcome")
    public void testCommitFailWithFailOutcome() {
        BValue[] result = BRunUtil.invoke(compileResult, "testCommitFailWithFailOutcome");
        Assert.assertTrue(result[0] instanceof BError);
        Assert.assertEquals(((BError) result[0]).getMessage(), "rollback only is set, hence commit failed !");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic due to failed commit.*",
            description = "Test commit fail and panic in block stmt outcome")
    public void testCommitFailWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testCommitFailWithPanicOutcome");
    }

    @Test(description = "Test rollback and success in block stmt outcome")
    public void testRollbackWithSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testRollbackWithSuccessOutcome");
    }

    @Test
    public void testRollbackWithFailOutcome() {
        BValue[] result = BRunUtil.invoke(compileResult, "testRollbackWithFailOutcome");
        Assert.assertTrue(result[0] instanceof BError);
        Assert.assertEquals(((BError) result[0]).getMessage(), "Invalid number");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Invalid number.*",
            description = "Test rollback and panic in block stmt outcome")
    public void testRollbackWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testRollbackWithPanicOutcome");
    }

    @Test(description = "Test panic from rollback and unusual success in block stmt outcome")
    public void testPanicFromRollbackWithUnusualSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromRollbackWithUnusualSuccessOutcome");
    }

    @Test(description = "Test panic from commit and unusual success in block stmt outcome")
    public void testPanicFromCommitWithUnusualSuccessOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromCommitWithUnusualSuccessOutcome");
    }

    @Test
    public void testPanicFromRollbackWithUnusualFailOutcome() {
        BValue[] result = BRunUtil.invoke(compileResult, "testPanicFromRollbackWithUnusualFailOutcome");
        Assert.assertNotNull(result);
        Assert.assertTrue(result[0] instanceof BError);
        Assert.assertEquals(((BError) result[0]).getMessage(), "Invalid number");
    }

    @Test
    public void testPanicFromCommitWithUnusualFailOutcome() {
        BValue[] result = BRunUtil.invoke(compileResult, "testPanicFromCommitWithUnusualFailOutcome");
        Assert.assertTrue(result[0] instanceof BError);
        Assert.assertEquals(((BError) result[0]).getMessage(), "rollback only is set, hence commit failed !");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Invalid number.*",
            description = "Test panic from rollback and panic in block stmt outcome")
    public void testPanicFromRollbackWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromRollbackWithPanicOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: rollback only is set, hence commit failed ! " +
                    "cause: rollback only is set, hence commit failed !.*",
            description = "Test panic from commit and panic in block stmt outcome")
    public void testPanicFromCommitWithPanicOutcome() {
        BRunUtil.invoke(compileResult, "testPanicFromCommitWithPanicOutcome");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Error in increment count.*",
            description = "Test no commit or rollback performed, rollback and panic in block stmt outcome")
    public void testNoCommitOrRollbackPerformedWithRollbackAndPanicOutcome() {
        BRunUtil.invoke(compileResult, "testNoCommitOrRollbackPerformedWithRollbackAndPanicOutcome");
    }

    @Test(description = "Test no commit or rollback performed, rollback and fail in block stmt outcome")
    public void testNoCommitOrRollbackPerformedWithRollbackAndFailOutcome() {
        BValue[] result = BRunUtil.invoke(compileResult, "testNoCommitOrRollbackPerformedWithRollbackAndFailOutcome");
        Assert.assertTrue(result[0] instanceof BError);
        Assert.assertEquals(((BError) result[0]).getMessage(), "Error in increment count");
    }

    @Test(description = "Test commit success and success in block stmt outcome with nested retry stmts")
    public void testCommitSuccessWithSuccessOutcomeInNestedRetry() {
        BRunUtil.invoke(compileResult, "testCommitSuccessWithSuccessOutcomeInNestedRetry");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic in nested retry.*",
            description = "Test commit success and panic in block stmt outcome with nested retry stmts")
    public void testCommitSuccessWithPanicOutcomeInNestedRetry() {
        BRunUtil.invoke(compileResult, "testCommitSuccessWithPanicOutcomeInNestedRetry");
    }

    @Test(description = "Test commit fail and unusual success in block stmt outcome with nested retry stmts")
    public void testCommitFailWithUnusualSuccessOutcomeInNestedRetry() {
        BRunUtil.invoke(compileResult, "testCommitFailWithUnusualSuccessOutcomeInNestedRetry");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic in nested retry.*",
            description = "Test commit fail and panic in block stmt outcome with nested retry stmt")
    public void testCommitFailWithPanicOutcomeInNestedRetry() {
        BRunUtil.invoke(compileResult, "testCommitFailWithPanicOutcomeInNestedRetry");
    }

    @Test(description = "Test rollback and fail in block stmt outcome with first nested retry stmt")
    public void testRollbackWithFailOutcomeInFirstNestedRetryStmt() {
        BRunUtil.invoke(compileResult, "testRollbackWithFailOutcomeInFirstNestedRetryStmt");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic in nested retry 1.*",
            description = "Test rollback and panic in block stmt outcome with second nested retry stmt")
    public void testRollbackWithPanicOutcomeInFirstNestedRetryStmt() {
        BRunUtil.invoke(compileResult, "testRollbackWithPanicOutcomeInFirstNestedRetryStmt");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Rollback due to error in trx 2.*",
            description = "Test panic from rollback and panic in block stmt outcome with second nested retry stmt")
    public void testPanicFromRollbackWithPanicOutcomeInSecondNestedRetryStmt() {
        BRunUtil.invoke(compileResult, "testPanicFromRollbackWithPanicOutcomeInSecondNestedRetryStmt");
    }
}
