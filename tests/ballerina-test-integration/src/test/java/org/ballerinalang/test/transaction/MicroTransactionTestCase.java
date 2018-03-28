/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.transaction;

import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Testing micro transaction header behaviour.
 */
public class MicroTransactionTestCase {
    private ServerInstance initiator;
    private ServerInstance participant1;
    private ServerInstance participant2;

    @BeforeClass
    private void setup() throws Exception {
        initiator = ServerInstance.initBallerinaServer(8888);
        initiator.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "initiator.bal").getAbsolutePath());

        participant1 = ServerInstance.initBallerinaServer(8889);
        participant1.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "participant1.bal").getAbsolutePath());

        participant2 = ServerInstance.initBallerinaServer(8890);
        participant2.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "participant2.bal").getAbsolutePath());
    }

    @Test(description = "Test participant1 transaction id")
    public void testParticipantTransactionId() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp(""));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertEquals(response.getData(), "equal id", "payload mismatched");
    }

    @Test(dependsOnMethods = {"testParticipantTransactionId"})
    public void testInitiatorAbort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testInitiatorAbort"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        InitiatorState initiatorState = new InitiatorState(initiatorStateRes.getData());
        assertTrue(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        ParticipantState participantState = new ParticipantState(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testInitiatorAbort"})
    public void testRemoteParticipantAbort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testRemoteParticipantAbort"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        InitiatorState initiatorState = new InitiatorState(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        ParticipantState participantState = new ParticipantState(participant1StateRes.getData());
        assertTrue(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testRemoteParticipantAbort"})
    public void testLocalParticipantSuccess() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testLocalParticipantSuccess"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        //TODO:
//        Assert.assertEquals(response.getData(), "equal id", "payload mismatched");
    }

    @Test(dependsOnMethods = {"testLocalParticipantSuccess"})
    public void testLocalParticipantAbort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testLocalParticipantAbort"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        InitiatorState initiatorState = new InitiatorState(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertTrue(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        ParticipantState participantState = new ParticipantState(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testLocalParticipantAbort"})
    public void testTransactionInfectableFalse() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testTransactionInfectableFalse"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        // Initiator has committed even though participant1 has aborted because participant1 is non-infectable
        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        InitiatorState initiatorState = new InitiatorState(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.abortedFunctionCalled);
        assertTrue(initiatorState.committedFunctionCalled);
        assertTrue(initiatorState.localParticipantCommittedFunctionCalled);
        assertFalse(initiatorState.localParticipantAbortedFunctionCalled);

        // Since the participant is non-infectable, it will not participate in the coordination, and its abort will not
        // affect the initiator. Here the "participant" has aborted.
        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        ParticipantState participantState = new ParticipantState(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testTransactionInfectableFalse"})
    public void testTransactionInfectableTrue() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testTransactionInfectableTrue"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        InitiatorState initiatorState = new InitiatorState(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        ParticipantState participantState = new ParticipantState(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    public void testSaveToDatabaseSuccessfulInParticipant() {

    }

    public void testSaveToDatabaseFailedInParticipant() {

    }

    @AfterClass
    private void cleanup() throws Exception {
        initiator.stopServer();
        participant1.stopServer();
        participant2.stopServer();
    }

    private static class InitiatorState {
        boolean abortedByInitiator;
        boolean abortedByLocalParticipant;
        boolean abortedFunctionCalled;
        boolean committedFunctionCalled;
        boolean localParticipantAbortedFunctionCalled;
        boolean localParticipantCommittedFunctionCalled;

        InitiatorState(String stateString) {
            String[] stateVars = stateString.split(",");
            for (String stateVar : stateVars) {
                String[] nvPair = stateVar.split("=");
                try {
                    Field field = this.getClass().getDeclaredField(nvPair[0]);
                    field.set(this, Boolean.parseBoolean(nvPair[1]));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static class ParticipantState {
        boolean abortedByParticipant;
        boolean abortedFunctionCalled;
        boolean committedFunctionCalled;
        boolean localParticipantAbortedFunctionCalled;
        boolean localParticipantCommittedFunctionCalled;

        ParticipantState(String stateString) {
            String[] stateVars = stateString.split(",");
            for (String stateVar : stateVars) {
                String[] nvPair = stateVar.split("=");
                try {
                    Field field = this.getClass().getDeclaredField(nvPair[0]);
                    field.set(this, Boolean.parseBoolean(nvPair[1]));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
