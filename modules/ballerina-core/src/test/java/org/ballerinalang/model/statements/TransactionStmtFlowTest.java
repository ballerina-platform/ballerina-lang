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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in TransactionStatement.
 */
public class TransactionStmtFlowTest {

    ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/statements/transactionStmt/transaction-test.bal");
    }

    @Test
    public void testTransactionStmt1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx inCmt end");
    }

    @Test
    public void testTransactionStmt2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testTransactionStmt3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx inAbt err end");
    }

    @Test
    public void testTransactionStmt4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }


    @Test
    public void testOptionalAborted1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx inCmt end");
    }

    @Test
    public void testOptionalAborted2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort end");
    }

    @Test
    public void testOptionalAborted3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx err end");
    }

    @Test
    public void testOptionalAborted4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalAborted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }

    @Test
    public void testOptionalCommitted1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx endTrx end");
    }

    @Test
    public void testOptionalCommitted2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testOptionalCommittedStmt3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx inAbt err end");
    }

    @Test
    public void testOptionalCommitted4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testOptionalCommitted", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx end");
    }

    @Test
    public void testNestedTransaction1() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");
    }

    @Test
    public void testNestedTransaction2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx abort innerAborted endOuterTrx inOuterCmt  end");
    }

    @Test
    public void testNestedTransaction3() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
              "start inOuterTrx inInnerTrx inInnerTrx inInnerTrx innerAbortedinOuterTrx inInnerTrx inInnerTrx "
              + "inInnerTrx innerAbortedinOuterTrx inInnerTrx inInnerTrx inInnerTrx innerAborted outerAborted err end");

    }

    @Test
    public void testNestedTransaction4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx trxErr endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");

    }

    @Test
    public void testNestedTransactionWithFailed1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTrx inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed inOuterTrx "
                     + "inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed inOuterTrx "
                     + "inInnerTrx innerFailed inInnerTrx innerFailed innerAborted outerFailed outerAborted err end");

    }

    @Test
    public void testNestedTransactionWithFailed2() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedTransactionWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start  inOuterTrx inInnerTrx trxErr endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");

    }

    @Test
    public void testTransactionStmtWithFailed1() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed inAbt err end");
    }

    @Test
    public void testTransactionStmtWithFailed2() {
        BValue[] args = {new BInteger(0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx abort inAbt end");
    }

    @Test
    public void testTransactionStmtWithFailed3() {
        BValue[] args = {new BInteger(-2)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx trxErr endTrx inCmt end");
    }

    @Test
    public void testTransactionStmtWithFailed4() {
        BValue[] args = {new BInteger(1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtWithFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx success endTrx inCmt end");
    }

    @Test
    public void testTransactionStmtWithRetryOff() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtWithRetryOff", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inAbt err end");
    }

    @Test
    public void testTransactionStmtWithoutFailed() {
        BValue[] args = {new BInteger(-1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtWithoutFailed", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inTrx inTrx err end");
    }

    @Test
    public void testTransactionStmtConstRetry() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtConstRetry");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inFailed inTrx inFailed inTrx inFailed inTrx "
                + "inFailed err end");
    }

    @Test
    public void testTransactionStmtSuccess() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTransactionStmtSuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inCmt end");
    }

    @Test
    public void testMultipleTransactionStmtSuccess() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testMultipleTransactionStmtSuccess");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstCmt "
                + "inFirstTrxEnd inSecTrxBlockBegin inSecTrxBlockEnd inSecCmt inFSecTrxEnd end");
    }

    @Test
    public void testMultipleTransactionStmtError() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testMultipleTransactionStmtError");

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "start inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstTFld "
                + "inFirstTrxBlockBegin inFirstTrxBlockEnd inFirstTFld inFirstTAbt err end");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry1.bal:7: " +
            "retry statement not allowed here.*")
    public void testTransactionInvalidRetry1() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry1.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry2.bal:7: " +
            "unreachable statement*")
    public void testTransactionInvalidRetry2() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry2.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry3.bal:7: " +
            "invalid retry count*")
    public void testTransactionInvalidRetry3() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry3.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-retry4.bal:7: " +
            "invalid retry count*")
    public void testTransactionInvalidRetry4() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-retry4.bal");
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

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-abort1.bal:7: " +
            "abort statement is not allowed here.*")
    public void testInvalidAbort1() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-abort1.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-abort2.bal:7: " +
            "abort statement is not allowed here.*")
    public void testInvalidAbort2() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-abort2.bal");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*invalid-abort3.bal:3: " +
            "abort statement is not allowed here.*")
    public void testInvalidAbort3() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/invalid-abort3.bal");
    }

    @Test()
    public void testValidAbortAndReturn() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/statements/transactionStmt/valid-abort.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx inAbt outAbt");

        returns = BLangFunctions.invokeNew(programFile, "testReturn1", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx com");

        returns = BLangFunctions.invokeNew(programFile, "testReturn2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "st inTrx abt");
    }

}
