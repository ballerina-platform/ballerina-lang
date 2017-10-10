/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in TransactionStatement.
 */
public class TransactionStmtFlowTest {

    CompileResult programFile;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.compile("test-src/statements/transaction/transaction-stmt.bal");
        resultNegative = BTestUtils.compile("test-src/statements/transaction/transaction-stmt-negative.bal");
    }

    @Test
    public void testTransactionStmt1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx inCmt end");
    }

    @Test
    public void testTransactionStmt2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testTransactionStmt3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx inAbt err end");
    }

    @Test
    public void testTransactionStmt4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }


    @Test
    public void testOptionalAborted1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx inCmt end");
    }

    @Test
    public void testOptionalAborted2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort end");
    }

    @Test
    public void testOptionalAborted3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx err end");
    }

    @Test
    public void testOptionalAborted4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }

    @Test
    public void testOptionalCommitted1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx end");
    }

    @Test
    public void testOptionalCommitted2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testOptionalCommittedStmt3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx inAbt err end");
    }

    @Test
    public void testOptionalCommitted4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx end");
    }

    @Test
    public void testNestedTransaction1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");
    }

    @Test
    public void testNestedTransaction2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BTestUtils.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx abort innerAborted endOuterTrx inOuterCmt  end");
    }

    @Test
    public void testNestedTransaction3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
              "start inOuterTrx inInnerTrx inInnerTrx inInnerTrx innerAbortedinOuterTrx inInnerTrx inInnerTrx "
              + "inInnerTrx innerAbortedinOuterTrx inInnerTrx inInnerTrx inInnerTrx innerAborted outerAborted err end");

    }

    @Test
    public void testNestedTransaction4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx trxErr endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");

    }

    @Test
    public void testNestedTransactionWithFailed1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTrx inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed inOuterTrx "
                     + "inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed inOuterTrx "
                     + "inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed outerAborted err end");

    }

    @Test
    public void testNestedTransactionWithFailed2() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BTestUtils.invoke(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTrx inInnerTrx trxErr endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");

    }

    @Test
    public void testTransactionStmtWithFailed1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed inAbt err end");
    }

    @Test
    public void testTransactionStmtWithFailed2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testTransactionStmtWithFailed3() {
        BValue[] args = {new BInteger(-2)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }

    @Test
    public void testTransactionStmtWithFailed4() {
        BValue[] args = {new BInteger(1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx success endTrx inCmt end");
    }

    @Test
    public void testTransactionStmtWithRetryOff() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtWithRetryOff", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inAbt err end");
    }

    @Test
    public void testTransactionStmtWithoutFailed() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtWithoutFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx err end");
    }

    @Test
    public void testTransactionStmtConstRetry() {
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtConstRetry");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed err end");
    }

    @Test
    public void testTransactionStmtSuccess() {
        BValue[] returns = BTestUtils.invoke(programFile, "testTransactionStmtSuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inCmt end");
    }

    @Test
    public void testMultipleTransactionStmtSuccess() {
        BValue[] returns = BTestUtils.invoke(programFile, "testMultipleTransactionStmtSuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstCmt "
                + "inFirstTrxEnd inSecTrxBlockBegin inSecTrxBlockEnd inSecCmt inFSecTrxEnd end");
    }

    @Test
    public void testMultipleTransactionStmtError() {
        BValue[] returns = BTestUtils.invoke(programFile, "testMultipleTransactionStmtError");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstTFld "
                + "inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstTFld inFirstTAbt err end");
    }


    /*@Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry2.bal:7: " +
            "unreachable statement*")
    public void testTransactionInvalidRetry2() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry2.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry3.bal:7: " +
            "invalid retry count*")
    public void testTransactionInvalidRetry3() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry3.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry5.bal:8: " +
            "retry statement should be a root level statement within failed block*")
    public void testTransactionInvalidRetry5() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry5.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry6.bal:8: " +
            "invalid retry count*")
    public void testTransactionInvalidRetry6() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry6.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*break statement cannot " +
            "be used to exit from a transaction.*")
    public void testTransactionWithBreakInvalid() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/trx-with-break-invalid.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*continue statement " +
            "cannot be used to exit from a transaction.*")
    public void testTransactionWithNextInvalid() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/trx-with-continue-invalid.bal");
    }
    */

    @Test()
    public void testValidAbortAndReturn() {
        BValue[] returns = BTestUtils.invoke(programFile, "test", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx inAbt outAbt");

        returns = BTestUtils.invoke(programFile, "testReturn1", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx com");

        returns = BTestUtils.invoke(programFile, "testReturn2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx abt");
    }

    @Test()
    public void testTransactionWithBreakValid() {
        BValue[] returns = BTestUtils.invoke(programFile, "transactionWithBreak1", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BTestUtils.invoke(programFile, "transactionWithBreak2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BTestUtils.invoke(programFile, "transactionWithBreak3", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test()
    public void testTransactionWithNextValid() {
        BValue[] returns = BTestUtils.invoke(programFile, "transactionWithNext1", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BTestUtils.invoke(programFile, "transactionWithNext2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BTestUtils.invoke(programFile, "transactionWithNext3", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }


    @Test(description = "Test transaction statement with errors")
    public void testAssignmentNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 5);
        BTestUtils.validateError(resultNegative, 0, "incompatible types: expected 'int', found 'float'", 19, 15);
        BTestUtils.validateError(resultNegative, 1, "retry cannot be used outside of a transaction failed block", 7, 9);
        BTestUtils.validateError(resultNegative, 2, "abort cannot be used outside of a transaction block", 33, 9);
        BTestUtils.validateError(resultNegative, 3, "abort cannot be used outside of a transaction block", 43, 9);
        BTestUtils.validateError(resultNegative, 4, "abort cannot be used outside of a transaction block", 49, 5);
    }

}
