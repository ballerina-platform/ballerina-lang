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
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inAbt err end");
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
        Assert.assertEquals(returns[0].stringValue(), "start inTrx err end");
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
        Assert.assertEquals(returns[0].stringValue(), "start inTrx inAbt err end");
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
                "start inOuterTrx inInnerTrx innerAborted outerAborted err end");

    }

    @Test
    public void testNestedTransaction4() {
        BValue[] args = {new BInteger(-10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedTransaction", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "start inOuterTrx inInnerTrx trxErr endInnerTrx inInnerCmt endOuterTrx inOuterCmt  end");

    }

}
