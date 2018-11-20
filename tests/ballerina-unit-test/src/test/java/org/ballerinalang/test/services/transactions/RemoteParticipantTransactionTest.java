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
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

/**
 * Test cases for remote participant annotation.
 *
 * @since 0.985.0
 */
public class RemoteParticipantTransactionTest {

    // todo: remove all the behavioral tests to integration test suite, as we need be able to free the port after each run.
    private CompileResult programFile;
    private CompileResult negativeProgramFile;

    @BeforeClass
    public void setup() {
        programFile = BCompileUtil.compile("test-src/services/transaction/transaction_remote_participant_test.bal");
        //todo: temp, activate after figuring out annotation processing ::::: negativeProgramFile = BCompileUtil.compile("test-src/services/transaction/transaction_remote_participant_negative.bal");
    }

    @Test
    @Ignore // todo: remote this ignore (this was ignore to help reduce clutter when debugging code-gen.
    public void participantServiceCannotInitiateTransaction() {
        Assert.assertTrue(negativeProgramFile.getDiagnostics().length > 0, "'canInitiate: true' annotation is illegal"
                + " for participant resource");
        BAssertUtil.validateError(negativeProgramFile, 0,
                "Participant resource cannot initiate a transaction", 10, 5);
    }

    @Test
    public void remoteParticipantTransactionSuccessTest() {
        String result = invokeInitiatorFunc(false, false,
                true, false, false, false);
        String target = " in-trx-block in-remote <payload-from-remote> in-trx-lastline " +
                "in-baz[oncommittedFunc] committed-block after-trx";
        Assert.assertEquals(result, target);
    }

    @Test
    public void remoteParticipantTransactionFailSuccessTest() {
        String result = invokeInitiatorFunc(true, false,
                true, true, false, false);
        String target = " in-trx-block in-remote <payload-from-remote> throw-1 onretry-block " +
                "in-trx-block in-remote <payload-from-remote> in-trx-lastline " +
                "in-baz[oncommittedFunc] committed-block after-trx";
        Assert.assertEquals(result, target);
    }

    @Test
    public void remoteParticipantTransactionExceptionInRemoteNoSecondRemoteCall() {
        String result = invokeInitiatorFunc(false, false,
                true, false,
                true, false);
        String target = " in-trx-block in-remote remote1-blown in-trx-lastline onretry-block " +
                "in-trx-block in-trx-lastline committed-block after-trx";
        Assert.assertEquals(result, target);
    }

    @Test
    public void remoteParticipantTransactionExceptionInRemoteThenSuccessInRemote() {
        String result = invokeInitiatorFunc(false, false,
                true, true,
                true, false);
        String target = " in-trx-block in-remote remote1-blown in-trx-lastline " +
                "onretry-block in-trx-block in-remote <payload-from-remote> in-trx-lastline " +
                "in-baz[oncommittedFunc] committed-block after-trx";
        Assert.assertEquals(result, target);
    }

    @Test
    public void remoteParticipantTransactionExceptionInRemoteThenExceptionInRemote() {
        String result = invokeInitiatorFunc(false, false,
                true, true,
                true, true);
        String target = " in-trx-block in-remote remote1-blown in-trx-lastline onretry-block " +
                "in-trx-block in-remote remote2-blown in-trx-lastline aborted-block after-trx";
        Assert.assertEquals(result, target);
    }

    private String invokeInitiatorFunc(boolean throw1, boolean throw2, boolean remote1, boolean remote2,
                                       boolean blowRemote, boolean blowRemote2) {
        BValue[] params = {new BBoolean(throw1), new BBoolean(throw2),
                new BBoolean(remote1), new BBoolean(remote2),
                new BBoolean(blowRemote), new BBoolean(blowRemote2)};
        BValue[] ret = BRunUtil.invoke(programFile, "initiatorFunc", params);
        return ret[0].stringValue();
    }
}
