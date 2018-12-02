/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TransactionLocalParticipantFunctionTest {
    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/functions/transaction-local-participant-function.bal");
    }

    @Test
    public void testTransactionAnnotatedFunction() {
        Diagnostic[] diagnostics = result.getDiagnostics();
        Assert.assertTrue(diagnostics.length == 0, "Transaction annotation error");
    }

    @Test
    public void testTransactionLocalParticipantFunctionInvocation() {
        BValue[] params = {new BBoolean(false), new BBoolean(false)};
        BValue[] ret = BRunUtil.invoke(result, "initiatorFunc", params);
        String s = ret[0].stringValue();
        Assert.assertEquals(s, " in-trx-block in-participantFoo in-trx-lastline " +
                "commitFun committed-block after-trx");
    }

    @Test
    public void testTransactionLocalParticipantFunctionInvocationErrorThenNoError() {
        BValue[] params = {new BBoolean(true), new BBoolean(false)};
        BValue[] ret = BRunUtil.invoke(result, "initiatorFunc", params);
        String s = ret[0].stringValue();
        Assert.assertEquals(s, " in-trx-block in-participantFoo in-participantErroredFunc TransactionError " +
                "in-trx-lastline onretry-block " +
                "in-trx-block in-participantFoo in-trx-lastline commitFun committed-block after-trx");
    }

    @Test
    public void testTransactionLocalParticipantFunctionInvocationParticipantErrorError() {
        BValue[] params = {new BBoolean(true), new BBoolean(true)};
        BValue[] ret = BRunUtil.invoke(result, "initiatorFunc", params);
        String s = ret[0].stringValue();
        Assert.assertEquals(s, " in-trx-block in-participantFoo in-participantErroredFunc TransactionError " +
                "in-trx-lastline onretry-block " +
                "in-trx-block in-participantFoo in-participantErroredFunc TransactionError " +
                "in-trx-lastline aborted-block after-trx");
    }

    @Test
    public void testTransactionLocalNonParticipantStartTransaction() {
        BValue[] params = {};
        BValue[] ret = BRunUtil.invoke(result, "initiatorWithLocalNonParticipantError", params);
        String s = ret[0].stringValue();
        Assert.assertEquals(s, " in-trx trapped:[dynamically nested transactions are not allowed] " +
                "last-line committed");
    }

    @Test
    public void testTransactionTransactionOnlyInfectCallsInSameStrand() {
        BValue[] params = {};
        BValue[] ret = BRunUtil.invoke(result, "participantInNonStrand", params);
        String s = ret[0].stringValue();
        Assert.assertEquals(s, " in-trx from-startANewStrand last-line committed | error in otherStrand: error!!!");
    }
}
