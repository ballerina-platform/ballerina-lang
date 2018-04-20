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
import org.ballerinalang.test.util.SQLDBUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Testing micro transaction header behaviour.
 */
public class MicroTransactionTestCase {
    private ServerInstance initiator;
    private ServerInstance participant1;
    private ServerInstance participant2;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";
    private static final String[] ARGS = {"-e", "http.coordinator.host=127.0.0.1"};

    @BeforeClass
    private void setup() throws Exception {
        initiator = ServerInstance.initBallerinaServer(8888);
        initiator.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "initiator.bal").getAbsolutePath(), ARGS);

        participant1 = ServerInstance.initBallerinaServer(8889);
        participant1.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources" +
                        File.separator + "transaction" + File.separator + "participant1.bal").getAbsolutePath(), ARGS);

        participant2 = ServerInstance.initBallerinaServer(8890);
        copyFile(new File(System.getProperty("hsqldb.jar")),
                new File(participant2.getServerHome() + File.separator + "bre" + File.separator + "lib" +
                        File.separator + "hsqldb.jar"));
        SQLDBUtils.deleteFiles(new File(SQLDBUtils.DB_DIRECTORY), DB_NAME);
        SQLDBUtils.initDatabase(SQLDBUtils.DB_DIRECTORY, DB_NAME, "transaction/data.sql");
        participant2.
                startBallerinaServer(new File("src" + File.separator + "test" + File.separator + "resources"
                        + File.separator + "transaction" + File.separator + "participant2.bal").getAbsolutePath(),
                        ARGS);
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
        State initiatorState = new State(initiatorStateRes.getData());
        assertTrue(initiatorState.abortedByInitiator);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participantState = new State(participant1StateRes.getData());
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
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participantState = new State(participant1StateRes.getData());
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

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.localParticipantAbortedFunctionCalled);
        assertTrue(initiatorState.committedFunctionCalled);
        assertTrue(initiatorState.localParticipantCommittedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertFalse(participantState.abortedFunctionCalled);
        assertFalse(participantState.localParticipantAbortedFunctionCalled);
        assertTrue(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testLocalParticipantSuccess"})
    public void testLocalParticipantAbort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testLocalParticipantAbort"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertTrue(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testLocalParticipantAbort"})
//    @Test
    public void testTransactionInfectableFalse() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testTransactionInfectableFalse"));
        assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        assertEquals(response.getData(), "cannot create transaction context: resource is not transactionInfectable");

        // Initiator has committed even though participant1 has aborted because participant1 is non-infectable
        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertTrue(initiatorState.abortedByInitiator);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);

        // Since the participant is non-infectable, it will not participate in the coordination, and its abort will not
        // affect the initiator. Here the "participant" has aborted.
        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertFalse(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertFalse(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testTransactionInfectableFalse"})
    public void testTransactionInfectableTrue() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(initiator.getServiceURLHttp("testTransactionInfectableTrue"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testTransactionInfectableTrue"})
    public void testSaveToDatabaseSuccessfulInParticipant() throws IOException {
        HttpResponse response =
                HttpClientRequest.doGet(initiator.getServiceURLHttp("testSaveToDatabaseSuccessfulInParticipant"));
        String data = response.getData();
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse participantRes =
                HttpClientRequest.doGet(participant2.getServiceURLHttp("checkCustomerExists/" + data));
        assertEquals(participantRes.getData(), data, "Record insertion failed");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.localParticipantAbortedFunctionCalled);
        assertTrue(initiatorState.committedFunctionCalled);
        assertTrue(initiatorState.localParticipantCommittedFunctionCalled);

        // Participant 1 is just passthru, and will propogate the transaction to participant2
        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participant1State = new State(participant1StateRes.getData());
        assertFalse(participant1State.abortedByParticipant);
        assertFalse(participant1State.abortedFunctionCalled);
        assertFalse(participant1State.localParticipantAbortedFunctionCalled);
        assertFalse(participant1State.committedFunctionCalled);
        assertFalse(participant1State.localParticipantCommittedFunctionCalled);

        HttpResponse participant2StateRes = HttpClientRequest.doGet(participant2.getServiceURLHttp("getState"));
        State participant2State = new State(participant2StateRes.getData());
        assertFalse(participant2State.abortedFunctionCalled);
        assertFalse(participant2State.localParticipantAbortedFunctionCalled);
        assertTrue(participant2State.committedFunctionCalled);
        assertTrue(participant2State.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testSaveToDatabaseSuccessfulInParticipant"})
    public void testSaveToDatabaseFailedInParticipant() throws IOException {
        HttpResponse response =
                HttpClientRequest.doGet(initiator.getServiceURLHttp("testSaveToDatabaseFailedInParticipant"));
        String data = response.getData();
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse participantRes =
                HttpClientRequest.doGet(participant2.getServiceURLHttp("checkCustomerExists/" + data));
        assertNotEquals(participantRes.getData(), data, "Abort failed");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(initiator.getServiceURLHttp("getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(participant1.getServiceURLHttp("getState"));
        State participant1State = new State(participant1StateRes.getData());
        assertFalse(participant1State.abortedByParticipant);
        assertFalse(participant1State.committedFunctionCalled);
        assertFalse(participant1State.localParticipantCommittedFunctionCalled);
        assertTrue(participant1State.abortedFunctionCalled);
        assertTrue(participant1State.localParticipantAbortedFunctionCalled);

        HttpResponse participant2StateRes = HttpClientRequest.doGet(participant2.getServiceURLHttp("getState"));
        State participant2State = new State(participant2StateRes.getData());
        assertFalse(participant2State.committedFunctionCalled);
        assertFalse(participant2State.localParticipantCommittedFunctionCalled);
        assertTrue(participant2State.abortedFunctionCalled);
        assertTrue(participant2State.localParticipantAbortedFunctionCalled);
    }

    @AfterClass
    private void cleanup() throws Exception {
        initiator.stopServer();
        participant1.stopServer();
        participant2.stopServer();
    }

    private static void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING);
    }

    private static class State {
        boolean abortedByInitiator;
        boolean abortedByParticipant;
        boolean abortedByLocalParticipant;
        boolean abortedFunctionCalled;
        boolean committedFunctionCalled;
        boolean localParticipantAbortedFunctionCalled;
        boolean localParticipantCommittedFunctionCalled;

        State(String stateString) {
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
