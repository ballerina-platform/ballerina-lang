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

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.SQLDBUtils;
import org.ballerinalang.test.util.SQLDBUtils.DBType;
import org.ballerinalang.test.util.SQLDBUtils.TestDatabase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
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
@Test(groups = "transactions-test")
public class MicroTransactionTestCase extends BaseTest {
    private static BServerInstance serverInstance;
    private TestDatabase sqlServer;
    private static final String DB_NAME = "TEST_SQL_CONNECTOR";
    private final int initiatorServicePort = 8888;
    private final int participant1ServicePort = 8889;
    private final int participant2ServicePort = 8890;

    @BeforeClass(groups = "transactions-test", alwaysRun = true)
    public void start() throws BallerinaTestException, IOException {
        int[] requiredPorts = new int[]{initiatorServicePort, participant1ServicePort, participant2ServicePort};
        sqlServer = new SQLDBUtils.FileBasedTestDatabase(DBType.H2, "transaction" + File.separator + "data.sql",
                SQLDBUtils.DB_DIRECTORY, DB_NAME);
        String basePath = new File("src" + File.separator + "test" + File.separator + "resources" + File.separator +
                "transaction").getAbsolutePath();
        String[] args = new String[]{"-e", "http.coordinator.host=127.0.0.1"};

        serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(basePath, "transactionservices", args, requiredPorts);
    }

    @AfterClass(groups = "transactions-test", alwaysRun = true)
    public void stop() throws Exception {
        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
        sqlServer.stop();
    }

    @Test(description = "Test participant1 transaction id")
    public void testParticipantTransactionId() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort, ""));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertEquals(response.getData(), "equal id", "payload mismatched");
    }

    @Test(dependsOnMethods = {"testParticipantTransactionId"})
    public void testInitiatorAbort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "testInitiatorAbort"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertTrue(initiatorState.abortedByInitiator);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testInitiatorAbort"})
    public void testRemoteParticipantAbort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "testRemoteParticipantAbort"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
        State participantState = new State(participant1StateRes.getData());
        assertTrue(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testRemoteParticipantAbort"})
    public void testLocalParticipantSuccess() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "testLocalParticipantSuccess"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(initiatorServicePort, "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.localParticipantAbortedFunctionCalled);
        assertTrue(initiatorState.committedFunctionCalled);
        assertTrue(initiatorState.localParticipantCommittedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertFalse(participantState.abortedFunctionCalled);
        assertFalse(participantState.localParticipantAbortedFunctionCalled);
        assertTrue(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testLocalParticipantSuccess"})
    public void testLocalParticipantAbort() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "testLocalParticipantAbort"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(initiatorServicePort, "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertTrue(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertTrue(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertTrue(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testLocalParticipantAbort"})
    public void testTransactionInfectableFalse() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "testTransactionInfectableFalse"));
        assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        assertEquals(response.getData(), "cannot create transaction context: resource is not transactionInfectable");

        // Initiator has committed even though participant1 has aborted because participant1 is non-infectable
        HttpResponse initiatorStateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(initiatorServicePort, "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertTrue(initiatorState.abortedByInitiator);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);

        // Since the participant is non-infectable, it will not participate in the coordination, and its abort will not
        // affect the initiator. Here the "participant" has aborted.
        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
        State participantState = new State(participant1StateRes.getData());
        assertFalse(participantState.abortedByParticipant);
        assertFalse(participantState.abortedFunctionCalled);
        assertFalse(participantState.committedFunctionCalled);
        assertFalse(participantState.localParticipantAbortedFunctionCalled);
        assertFalse(participantState.localParticipantCommittedFunctionCalled);
    }

    @Test(dependsOnMethods = {"testTransactionInfectableFalse"})
    public void testTransactionInfectableTrue() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                "testTransactionInfectableTrue"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(initiatorServicePort, "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
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
                HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                        "testSaveToDatabaseSuccessfulInParticipant"));
        String data = response.getData();
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse participantRes =
                HttpClientRequest.doGet(serverInstance.getServiceURLHttp(participant2ServicePort,
                        "checkCustomerExists/" + data));
        assertEquals(participantRes.getData(), data, "Record insertion failed");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(initiatorServicePort, "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.abortedFunctionCalled);
        assertFalse(initiatorState.localParticipantAbortedFunctionCalled);
        assertTrue(initiatorState.committedFunctionCalled);
        assertTrue(initiatorState.localParticipantCommittedFunctionCalled);

        // Participant 1 is just passthru, and will propogate the transaction to participant2
        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
        State participant1State = new State(participant1StateRes.getData());
        assertFalse(participant1State.abortedByParticipant);
        assertFalse(participant1State.abortedFunctionCalled);
        assertFalse(participant1State.localParticipantAbortedFunctionCalled);
        assertFalse(participant1State.committedFunctionCalled);
        assertFalse(participant1State.localParticipantCommittedFunctionCalled);

        HttpResponse participant2StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant2ServicePort, "getState"));
        State participant2State = new State(participant2StateRes.getData());
        assertFalse(participant2State.abortedFunctionCalled);
        assertFalse(participant2State.localParticipantAbortedFunctionCalled);
        assertTrue(participant2State.committedFunctionCalled);
        assertTrue(participant2State.localParticipantCommittedFunctionCalled);
    }

    @Ignore
    @Test(dependsOnMethods = {"testSaveToDatabaseSuccessfulInParticipant"})
    public void testSaveToDatabaseFailedInParticipant() throws IOException {
        HttpResponse response =
                HttpClientRequest.doGet(serverInstance.getServiceURLHttp(initiatorServicePort,
                        "testSaveToDatabaseFailedInParticipant"));
        String data = response.getData();
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");

        HttpResponse participantRes =
                HttpClientRequest.doGet(serverInstance.getServiceURLHttp(participant2ServicePort,
                        "checkCustomerExists/" + data));
        assertNotEquals(participantRes.getData(), data, "Abort failed");

        HttpResponse initiatorStateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(initiatorServicePort, "getState"));
        State initiatorState = new State(initiatorStateRes.getData());
        assertFalse(initiatorState.abortedByInitiator);
        assertFalse(initiatorState.abortedByLocalParticipant);
        assertFalse(initiatorState.committedFunctionCalled);
        assertFalse(initiatorState.localParticipantCommittedFunctionCalled);
        assertTrue(initiatorState.abortedFunctionCalled);
        assertTrue(initiatorState.localParticipantAbortedFunctionCalled);

        HttpResponse participant1StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant1ServicePort, "getState"));
        State participant1State = new State(participant1StateRes.getData());
        assertFalse(participant1State.abortedByParticipant);
        assertFalse(participant1State.committedFunctionCalled);
        assertFalse(participant1State.localParticipantCommittedFunctionCalled);
        assertTrue(participant1State.abortedFunctionCalled);
        assertTrue(participant1State.localParticipantAbortedFunctionCalled);

        HttpResponse participant2StateRes = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(participant2ServicePort, "getState"));
        State participant2State = new State(participant2StateRes.getData());
        assertFalse(participant2State.committedFunctionCalled);
        assertFalse(participant2State.localParticipantCommittedFunctionCalled);
        assertTrue(participant2State.abortedFunctionCalled);
        assertTrue(participant2State.localParticipantAbortedFunctionCalled);
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
