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
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for validating semantic related to transaction.
 */
public class TransactionStmtNegativeTest {

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*break statement cannot " +
            "be used to exit from a transaction.*")
    public void testTransactionWithBreakInvalid() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/trx-with-break-invalid.bal");
    }

    @Test()
    public void testTransactionWithBreakValid() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/statements/transactionStmt/trx-with-break-valid.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BLangFunctions.invokeNew(programFile, "test2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BLangFunctions.invokeNew(programFile, "test3", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }

    @Test(expectedExceptions = SemanticException.class, expectedExceptionsMessageRegExp = ".*continue statement " +
            "cannot be used to exit from a transaction.*")
    public void testTransactionWithContinueInvalid() {
        BTestUtils.getProgramFile("lang/statements/transactionStmt/trx-with-continue-invalid.bal");
    }

    @Test()
    public void testTransactionWithContinueValid() {
        ProgramFile programFile =
                BTestUtils.getProgramFile("lang/statements/transactionStmt/trx-with-continue-valid.bal");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "test", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BLangFunctions.invokeNew(programFile, "test2", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");

        returns = BLangFunctions.invokeNew(programFile, "test3", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "done");
    }
}
