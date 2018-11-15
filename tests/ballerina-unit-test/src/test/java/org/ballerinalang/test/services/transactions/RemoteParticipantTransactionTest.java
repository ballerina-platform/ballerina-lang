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
package org.ballerinalang.test.services.transactions;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for remote participant annotation.
 *
 * @since 0.985.0
 */
public class RemoteParticipantTransactionTest {

    private CompileResult programFile;
    private CompileResult negativeProgramFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/services/transaction/transaction_remote_participant_test.bal");
        //todo: temp, activate after figuring out annotation processing ::::: negativeProgramFile = BCompileUtil.compile("test-src/services/transaction/transaction_remote_participant_negative.bal");
    }

    @Test
    public void participantServiceCannotInitiateTransaction() {
        Assert.assertTrue(negativeProgramFile.getDiagnostics().length > 0, "'canInitiate: true' annotation is illegal"
                + " for participant resource");
        BAssertUtil.validateError(negativeProgramFile, 0,
                "Participant resource cannot initiate a transaction", 10, 5);
    }

    @Test
    public void remoteParticipantTransactionSuccessTest() {
        String result = invokeInitiatorFunc(false, false);
        String target = "in-trx-block in-remote <payload-from-remote> in-trx-lastline " +
                "in-baz[oncommittedFunc] committed-block after-trx";
        Assert.assertEquals(result, target);
    }

    @Test
    public void remoteParticipantTransactionFailSuccessTest() {
        String result = invokeInitiatorFunc(true, false);
        String target = "in-trx-block in-remote <payload-from-remote> in-trx-lastline " +
                "in-baz[oncommittedFunc] committed-block after-trx";
        Assert.assertEquals(result, target);
    }

    private String invokeInitiatorFunc(boolean throw1, boolean throw2) {
        BValue[] params = {new BBoolean(throw1), new BBoolean(throw2)};
        BValue[] ret = BRunUtil.invoke(programFile, "initiatorFunc", params);
        return ret[0].stringValue();
    }
}
