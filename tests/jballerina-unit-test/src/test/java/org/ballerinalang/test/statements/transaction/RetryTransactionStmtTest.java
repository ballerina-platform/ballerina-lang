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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for flow validation in RetryTransactionStatement.
 */

public class RetryTransactionStmtTest {

    private CompileResult programFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/statements/transaction/retry_transaction_stmt.bal");
    }

    @Test
    public void testRetry() {
        BRunUtil.invoke(programFile, "testRetry");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: TransactionError.*")
    public void testPanic() {
        BRunUtil.invoke(programFile, "testPanic");
    }

    @Test
    public void testFailedTransactionOutput() {
        BValue[] values = BRunUtil.invoke(programFile, "testFailedTransactionOutput", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void multipleTrxSequenceSuccess() {
        String result = executeMultipleTrxSequence(false, false, false, false);
        Assert.assertEquals(result, "start in-trx-1 trxCommited-1 end-1 in-trx-2 trxCommited-2 end-2");
    }

    @Test
    public void multipleTrxSequenceAbortFirst() {
        String result = executeMultipleTrxSequence(true, false, false, false);
        Assert.assertEquals(result, "start in-trx-1 trxRollbacked-1 end-1 in-trx-2 trxCommited-2 end-2");
    }

    @Test
    public void multipleTrxSequenceAbortSecond() {
        String result = executeMultipleTrxSequence(false, true, false, false);
        Assert.assertEquals(result, "start in-trx-1 trxCommited-1 end-1 in-trx-2 trxRollbacked-2 end-2");
    }

    @Test
    public void multipleTrxSequenceAbortBoth() {
        String result = executeMultipleTrxSequence(true, true, false, false);
        Assert.assertEquals(result, "start in-trx-1 trxRollbacked-1 end-1 in-trx-2 trxRollbacked-2 end-2");
    }

    private String executeMultipleTrxSequence(boolean abort1, boolean abort2, boolean fail1, boolean fail2) {
        BValue[] params = {new BBoolean(abort1), new BBoolean(abort2),
                new BBoolean(fail1), new BBoolean(fail2)};
        BValue[] result = BRunUtil.invoke(programFile, "multipleTrxSequence", params);
        return result[0].stringValue();
    }

    @Test
    public void testCustomRetryManager() {
        BValue[] result = BRunUtil.invoke(programFile, "testCustomRetryManager", new BValue[]{});
        Assert.assertEquals(result[0].stringValue(), "start attempt 1:error, attempt 2:error, attempt 3:result " +
                "returned end.");
    }
}
