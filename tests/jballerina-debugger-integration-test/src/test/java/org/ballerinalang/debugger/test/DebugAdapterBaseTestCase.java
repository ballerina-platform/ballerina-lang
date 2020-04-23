/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.debugger.test;

import org.ballerinalang.debugger.test.utils.BallerinaTestBreakPoint;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.debugger.test.utils.client.TestDAPClientConnector;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.debugger.test.utils.DebugUtils.findFreePort;

/**
 * Parent test class for all of the debug adapter integration test cases. This will provide basic functionality for
 * debug adapter integration tests.
 */
public class DebugAdapterBaseTestCase extends BaseTestCase {

    protected static TestDAPClientConnector debugClientConnector;
    protected static String entryFilePath;
    protected static int port;
    protected boolean isConnected = false;
    protected static final int MAX_RETRY_COUNT = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTestCase.class);

    @Override
    @BeforeSuite(alwaysRun = true)
    public void initialize() throws BallerinaTestException, IOException {
        super.initialize();
    }

    protected void initDebugSession() throws BallerinaTestException {

        port = findFreePort();
        debugClientConnector = new TestDAPClientConnector(balServer.getServerHome(), testProjectPath.toString(),
                entryFilePath, port);

        final int[] retryAttempt = {0};

        if (debugClientConnector.isConnected()) {
            isConnected = true;
            LOGGER.info("Connection is already created.");
            return;
        }

        // Else, tries to initiate the socket connection.
        while (!isConnected && (++retryAttempt[0] <= MAX_RETRY_COUNT)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (!debugClientConnector.isConnected()) {
                LOGGER.debug("Not connected. Retrying...");
                debugClientConnector.createConnection();
                if (debugClientConnector.isConnected()) {
                    isConnected = true;
                    LOGGER.info(String.format("Connected to the remote server at %s.\n\n",
                            debugClientConnector.getAddress()), false);
                    break;
                }
            } else {
                LOGGER.debug("Connection is already created.");
                isConnected = true;
                break;
            }
        }
        if (!debugClientConnector.isConnected()) {
            destroy();
            throw new BallerinaTestException(String.format("Connection to debug server at %s could not be " +
                    "established\n", debugClientConnector.getAddress()));
        }
    }

    protected void setBreakpoints(List<BallerinaTestBreakPoint> breakpointList) throws BallerinaTestException {

        if (!isConnected) {
            return;
        }

        Map<Source, List<SourceBreakpoint>> sourceBreakpoints = new HashMap<>();
        for (BallerinaTestBreakPoint bp : breakpointList) {
            if (sourceBreakpoints.get(bp.getSource()) == null) {
                sourceBreakpoints.put(bp.getSource(), Collections.singletonList(bp.getDAPBreakPoint()));
            } else {
                sourceBreakpoints.get(bp.getSource()).add(bp.getDAPBreakPoint());
            }
        }

        // Sends "setBreakpoints()" requests per source file.
        for (Map.Entry<Source, List<SourceBreakpoint>> entry : sourceBreakpoints.entrySet()) {
            SetBreakpointsArguments breakpointRequestArgs = new SetBreakpointsArguments();
            breakpointRequestArgs.setSource(entry.getKey());
            breakpointRequestArgs.setBreakpoints(entry.getValue().toArray(new SourceBreakpoint[0]));
            try {
                debugClientConnector.getRequestManager().setBreakpoints(breakpointRequestArgs);
            } catch (Exception e) {
                LOGGER.error("SetBreakpoints request failed.", e);
                throw new BallerinaTestException("Breakpoints request failed.", e);
            }
        }

        try {
            // Sends "configuration done" notification to the debug server.
            debugClientConnector.getRequestManager().configurationDone(new ConfigurationDoneArguments());
        } catch (Exception e) {
            LOGGER.error("ConfigurationDone request failed.", e);
            throw new BallerinaTestException("ConfigurationDone request failed.", e);
        }
    }

    protected void attachToDebuggee() throws BallerinaTestException {
        debugClientConnector.attachToServer();
    }

    protected void launchDebuggee(DebugUtils.DebuggeeExecutionKind launchKind) throws BallerinaTestException {
        debugClientConnector.launchServer(launchKind);
    }

    @AfterSuite(alwaysRun = true)
    public void destroy() {
        super.destroy();
        // Todo - more resource disposing?
    }

}
