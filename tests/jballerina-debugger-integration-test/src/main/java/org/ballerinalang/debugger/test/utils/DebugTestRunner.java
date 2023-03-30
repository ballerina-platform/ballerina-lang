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
package org.ballerinalang.debugger.test.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.utils.client.DAPClientConnector;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.eclipse.lsp4j.debug.CompletionItem;
import org.eclipse.lsp4j.debug.CompletionsArguments;
import org.eclipse.lsp4j.debug.CompletionsResponse;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.OutputEventArgumentsCategory;
import org.eclipse.lsp4j.debug.PauseArguments;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;

import static org.ballerinalang.debugger.test.utils.DebugUtils.findFreePort;

/**
 * Test util class for all debugger integration test cases.
 */
public class DebugTestRunner {

    public List<BallerinaTestDebugPoint> testBreakpoints = new ArrayList<>();
    public Path testProjectPath;
    public Path testEntryFilePath;
    private static Path testProjectBaseDir;
    private static Path testSingleFileBaseDir;
    private static BalServer balServer;
    private DAPClientConnector debugClientConnector;
    private boolean isConnected = false;
    private int port;
    private BMainInstance balClient = null;
    private Process debuggeeProcess;
    private DebugHitListener hitListener;
    private AssertionMode assertionMode;
    private SoftAssert softAsserter;
    private final boolean isProjectBasedTest;
    private boolean clientSupportsRunInTerminal;

    private static final int SCHEDULER_INTERVAL_MS = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugTestRunner.class);

    public DebugTestRunner(String testProjectName, String testModuleFileName, boolean isProjectBasedTest) {
        if (isProjectBasedTest) {
            testProjectPath = testProjectBaseDir.resolve(testProjectName);
            testEntryFilePath = testProjectPath.resolve(testModuleFileName);
        } else {
            testProjectPath = testProjectBaseDir.resolve(testProjectName);
            testEntryFilePath = testSingleFileBaseDir.resolve(testModuleFileName);
        }

        this.isProjectBasedTest = isProjectBasedTest;
        // Hard assertions will be used by default.
        assertionMode = AssertionMode.HARD_ASSERT;
    }

    public static void initialize(Path projectDir, Path singleFileDir) throws BallerinaTestException, IOException {
        balServer = new BalServer();
        Path tempProjectDirectory = Files.createTempDirectory("bal-test-integration-debugger-project-");

        // Copy all the test resources to a temp dir.
        testSingleFileBaseDir = tempProjectDirectory.resolve("single-file-tests");
        FileUtils.copyFolder(singleFileDir, testSingleFileBaseDir);

        testProjectBaseDir = tempProjectDirectory.resolve("project-based-tests");
        FileUtils.copyFolder(projectDir, testProjectBaseDir);
    }

    public void setAssertionMode(AssertionMode assertionMode) {
        this.assertionMode = assertionMode;
    }

    public boolean isSoftAssertionsEnabled() {
        return assertionMode == AssertionMode.SOFT_ASSERT;
    }

    public void runDebuggeeProgram(String projectPath, int port) throws BallerinaTestException {
        String msg = "Listening for transport dt_socket at address: " + port;
        LogLeecher clientLeecher = new LogLeecher(msg);
        balClient = new BMainInstance(balServer);
        debuggeeProcess = balClient.debugMain("build", new String[]{"--debug", String.valueOf(port)},
                null, new String[]{}, new LogLeecher[]{clientLeecher}, projectPath, 20, true);
        clientLeecher.waitForText(20000);
    }

    /**
     * Initialize test debug session.
     *
     * @param executionKind Defines ballerina command type to be used to launch the debuggee.(If set to null, adapter
     *                      will try to attach to the debuggee, instead of launching)
     * @throws BallerinaTestException if any exception is occurred during initialization.
     */
    public void initDebugSession(DebugUtils.DebuggeeExecutionKind executionKind) throws BallerinaTestException {
        initDebugSession(executionKind, "");
    }

    /**
     * Initialize test debug session.
     *
     * @param executionKind Defines ballerina command type to be used to launch the debuggee.(If set to null, adapter
     *                      will try to attach to the debuggee, instead of launching)
     * @param terminalKind The terminal type, if the debug session should be launched in a separate terminal
     * @throws BallerinaTestException if any exception is occurred during initialization.
     */
    public boolean initDebugSession(DebugUtils.DebuggeeExecutionKind executionKind, String terminalKind)
            throws BallerinaTestException {
        HashMap<String, Object> launchConfigs = new HashMap<>();
        HashMap<String, Object> extendedCapabilities = new HashMap<>();
        extendedCapabilities.put("supportsReadOnlyEditors", true);
        launchConfigs.put("capabilities", extendedCapabilities);
        if (!terminalKind.isEmpty()) {
            launchConfigs.put("terminal", terminalKind);
        }
        initDebugSession(executionKind, launchConfigs);
        return debugClientConnector.getRequestManager().getDidRunInIntegratedTerminal();
    }

    /**
     * Initialize test debug session.
     *
     * @param executionKind Defines ballerina command type to be used to launch the debuggee.(If set to null, adapter
     *                      will try to attach to the debuggee, instead of launching)
     * @param port          debug session port
     * @throws BallerinaTestException if any exception is occurred during initialization.
     */
    public void initDebugSession(DebugUtils.DebuggeeExecutionKind executionKind, int port)
            throws BallerinaTestException {
        initDebugSession(executionKind, port, new HashMap<>());
    }

    /**
     * Initialize test debug session.
     *
     * @param executionKind Defines ballerina command type to be used to launch the debuggee.(If set to null, adapter
     *                      will try to attach to the debuggee, instead of launching)
     * @param launchArgs    debug launch request arguments
     * @throws BallerinaTestException if any exception is occurred during initialization.
     */
    public void initDebugSession(DebugUtils.DebuggeeExecutionKind executionKind, Map<String, Object> launchArgs)
            throws BallerinaTestException {
        port = findFreePort();
        initDebugSession(executionKind, port, launchArgs);
    }

    /**
     * Initialize test debug session.
     *
     * @param executionKind Defines ballerina command type to be used to launch the debuggee.(If set to null, adapter
     *                      will try to attach to the debuggee, instead of launching)
     * @param port          debug session port
     * @param launchArgs    debug launch request arguments
     * @throws BallerinaTestException if any exception is occurred during initialization.
     */
    public void initDebugSession(DebugUtils.DebuggeeExecutionKind executionKind,
                                 int port, Map<String, Object> launchArgs) throws BallerinaTestException {

        debugClientConnector = new DAPClientConnector(balServer.getServerHome(), testProjectPath, testEntryFilePath,
                port, clientSupportsRunInTerminal);
        debugClientConnector.createConnection();
        if (debugClientConnector.isConnected()) {
            isConnected = true;
            LOGGER.info(String.format("Connected to the remote server at %s.%n%n", debugClientConnector.getAddress()),
                    false);
        } else {
            throw new BallerinaTestException(String.format("Failed to connect to the debug server at %s%n",
                    debugClientConnector.getAddress()));
        }

        setBreakpoints(testBreakpoints);
        if (executionKind == DebugUtils.DebuggeeExecutionKind.BUILD) {
            attachToDebuggee();
        } else {
            debugClientConnector.getRequestManager().setIsProjectBasedTest(isProjectBasedTest);
            launchDebuggee(executionKind, launchArgs);
        }
    }

    private void attachToDebuggee() throws BallerinaTestException {
        try {
            // Sends "configuration done" notification to the debug server.
            debugClientConnector.getRequestManager().configurationDone(new ConfigurationDoneArguments());
        } catch (Exception e) {
            LOGGER.error("ConfigurationDone request failed.", e);
            throw new BallerinaTestException("ConfigurationDone request failed.", e);
        }
        debugClientConnector.attachToServer();
    }

    private void launchDebuggee(DebugUtils.DebuggeeExecutionKind launchKind, Map<String, Object> args)
            throws BallerinaTestException {
        try {
            // Sends "configuration done" notification to the debug server.
            debugClientConnector.getRequestManager().configurationDone(new ConfigurationDoneArguments());
        } catch (Exception e) {
            LOGGER.error("ConfigurationDone request failed.", e);
            throw new BallerinaTestException("ConfigurationDone request failed.", e);
        }
        debugClientConnector.launchServer(launchKind, args);
    }

    /**
     * Can be used to add a new test breakpoint on-the-fly.
     *
     * @param breakpoint breakpoint to be set
     * @throws BallerinaTestException if any exception is occurred during action.
     */
    public Optional<SetBreakpointsResponse> addBreakPoint(BallerinaTestDebugPoint breakpoint)
            throws BallerinaTestException {
        testBreakpoints.add(breakpoint);
        List<BallerinaTestDebugPoint> breakpointsToBeSent = new ArrayList<>();
        for (org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint bp : testBreakpoints) {
            if (bp.getSource().getPath().equals(breakpoint.getSource().getPath())) {
                breakpointsToBeSent.add(bp);
            }
        }

        if (debugClientConnector != null && debugClientConnector.isConnected()) {
            return Optional.ofNullable(setBreakpoints(breakpointsToBeSent));
        }
        return Optional.empty();
    }

    private SetBreakpointsResponse setBreakpoints(List<BallerinaTestDebugPoint> breakPoints)
            throws BallerinaTestException {

        if (!isConnected) {
            return null;
        }
        Map<Source, List<SourceBreakpoint>> sourceBreakpoints = new HashMap<>();
        for (BallerinaTestDebugPoint bp : breakPoints) {
            sourceBreakpoints.computeIfAbsent(bp.getSource(), k -> new ArrayList<>());
            sourceBreakpoints.get(bp.getSource()).add(bp.getDAPBreakPoint());
        }

        // Sends "setBreakpoints()" requests per source file.
        for (Map.Entry<Source, List<SourceBreakpoint>> entry : sourceBreakpoints.entrySet()) {
            SetBreakpointsArguments breakpointRequestArgs = new SetBreakpointsArguments();
            breakpointRequestArgs.setSource(entry.getKey());
            breakpointRequestArgs.setBreakpoints(entry.getValue().toArray(new SourceBreakpoint[0]));
            try {
                return debugClientConnector.getRequestManager().setBreakpoints(breakpointRequestArgs);
            } catch (Exception e) {
                LOGGER.error("SetBreakpoints request failed.", e);
                throw new BallerinaTestException("Breakpoints request failed.", e);
            }
        }
        return null;
    }

    /**
     * Can be used to remove an already added test breakpoint on-the-fly.
     *
     * @param breakpoint breakpoint to be removed.
     * @throws BallerinaTestException if any exception is occurred during action.
     */
    public void removeBreakPoint(BallerinaTestDebugPoint breakpoint) throws BallerinaTestException {
        testBreakpoints.remove(breakpoint);
        List<BallerinaTestDebugPoint> breakpointsToBeSent = new ArrayList<>();
        for (org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint bp : testBreakpoints) {
            if (bp.getSource().getPath().equals(breakpoint.getSource().getPath())) {
                breakpointsToBeSent.add(bp);
            }
        }

        if (debugClientConnector != null && debugClientConnector.isConnected()) {
            setBreakpoints(breakpointsToBeSent);
        }
    }

    /**
     * Resumes the execution of the debuggee program.
     *
     * @param context program suspended context.
     * @param kind    debug option to be used to continue the debuggee execution.
     * @throws BallerinaTestException if an error occurs when resuming program.
     */
    public void resumeProgram(StoppedEventArguments context, DebugResumeKind kind) throws BallerinaTestException {
        if (kind == DebugResumeKind.NEXT_BREAKPOINT) {
            ContinueArguments continueArgs = new ContinueArguments();
            continueArgs.setThreadId(context.getThreadId());
            try {
                debugClientConnector.getRequestManager().resume(continueArgs);
            } catch (Exception e) {
                LOGGER.warn("continue request failed.", e);
                throw new BallerinaTestException("continue request failed.", e);
            }
        } else if (kind == DebugResumeKind.STEP_IN) {
            StepInArguments stepInArgs = new StepInArguments();
            stepInArgs.setThreadId(context.getThreadId());
            try {
                debugClientConnector.getRequestManager().stepIn(stepInArgs);
            } catch (Exception e) {
                LOGGER.warn("Step in request failed", e);
                throw new BallerinaTestException("Step in request failed", e);
            }
        } else if (kind == DebugResumeKind.STEP_OUT) {
            StepOutArguments stepOutArgs = new StepOutArguments();
            stepOutArgs.setThreadId(context.getThreadId());
            try {
                debugClientConnector.getRequestManager().stepOut(stepOutArgs);
            } catch (Exception e) {
                LOGGER.warn("Step out request failed", e);
                throw new BallerinaTestException("Step out request failed", e);
            }
        } else if (kind == DebugResumeKind.STEP_OVER) {
            NextArguments nextArgs = new NextArguments();
            nextArgs.setThreadId(context.getThreadId());
            try {
                debugClientConnector.getRequestManager().next(nextArgs);
            } catch (Exception e) {
                LOGGER.warn("Step over request failed", e);
                throw new BallerinaTestException("Step over request failed", e);
            }
        }
    }

    /**
     * Pauses(suspends) the execution of the debuggee program.
     *
     * @throws BallerinaTestException if an error occurs when resuming program.
     */
    public void pauseProgram(int threadId) throws BallerinaTestException {
        PauseArguments pauseArgs = new PauseArguments();
        pauseArgs.setThreadId(threadId);
        try {
            debugClientConnector.getRequestManager().pause(pauseArgs);
        } catch (Exception e) {
            LOGGER.warn("Pause request failed", e);
            throw new BallerinaTestException("Pause request failed", e);
        }
    }

    /**
     * Waits for a debug hit within a given time.
     *
     * @param timeoutMillis timeout.
     * @return pair of the debug point and context details.
     * @throws BallerinaTestException if a debug point is not found within the given time.
     */
    public Pair<BallerinaTestDebugPoint, StoppedEventArguments> waitForDebugHit(long timeoutMillis) throws
            BallerinaTestException {

        hitListener = new DebugHitListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(hitListener, 0, SCHEDULER_INTERVAL_MS);

        long retries = 2 * timeoutMillis / SCHEDULER_INTERVAL_MS;
        for (int i = 0; i < retries; i++) {
            try {
                Thread.sleep(SCHEDULER_INTERVAL_MS / 2);
                if (hitListener.isDebugHitFound()) {
                    break;
                }
            } catch (InterruptedException ignored) {
            }
        }
        timer.cancel();

        if (!hitListener.isDebugHitFound()) {
            throw new BallerinaTestException("Timeout expired waiting for the debug hit");
        }
        return new ImmutablePair<>(hitListener.getDebugHitpoint(), hitListener.getDebugHitContext());
    }

    /**
     * Waits for a debugger output within the given timeout.
     *
     * @param timeoutMillis timeout.
     * @return pair of the debug point and context details.
     * @throws BallerinaTestException if a debug point is not found within the given time.
     */
    public Pair<String, OutputEventArguments> waitForLastDebugOutput(long timeoutMillis) throws BallerinaTestException {
        DebugOutputListener outputListener = new DebugOutputListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(outputListener, 0, 1000);
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException ignored) {
        }
        timer.cancel();

        if (!outputListener.isDebugOutputFound()) {
            throw new BallerinaTestException("Timeout expired waiting for the debugger output");
        }
        return new ImmutablePair<>(outputListener.getLastOutputLog(), outputListener.getLastOutputContext());
    }

    /**
     * Waits for 'breakpoint' events from the debug server within the given timeout.
     *
     * @param timeoutMillis timeout.
     * @return pair of the debug point and context details.
     * @throws BallerinaTestException if a debug point is not found within the given time.
     */
    public List<BallerinaTestDebugPoint> waitForModifiedBreakpoints(long timeoutMillis) throws BallerinaTestException {
        BreakpointEventListener breakpointEventListener = new BreakpointEventListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(breakpointEventListener, 0, 1000);
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException ignored) {
        }
        timer.cancel();

        if (!breakpointEventListener.isBreakpointEventFound()) {
            throw new BallerinaTestException("Timeout expired waiting for the breakpoint events");
        }
        return breakpointEventListener.getModifiedBreakpoints();
    }

    /**
     * Waits for a debugger output within the given timeout.
     *
     * @param timeoutMillis timeout.
     * @return pair of the debug point and context details.
     * @throws BallerinaTestException if a debug point is not found within the given time.
     */
    public List<Pair<String, OutputEventArguments>> waitForDebugOutputs(long timeoutMillis)
            throws BallerinaTestException {
        DebugOutputListener outputListener = new DebugOutputListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(outputListener, 0, 1000);
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException ignored) {
        }
        timer.cancel();

        if (!outputListener.isDebugOutputFound()) {
            throw new BallerinaTestException("Timeout expired waiting for the debugger output");
        }

        List<Pair<String, OutputEventArguments>> debugOutputs = new ArrayList<>();
        for (OutputEventArguments outputEvent : outputListener.getAllDebugOutputs()) {
            debugOutputs.add(new ImmutablePair<>(outputEvent.getOutput(), outputEvent));
        }
        return debugOutputs;
    }

    /**
     * Waits for debug termination within a given time.
     *
     * @param timeoutMillis timeout.
     * @return boolean true if debug termination is found.
     */
    public boolean waitForDebugTermination(long timeoutMillis) {
        DebugTerminationListener terminationListener = new DebugTerminationListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(terminationListener, 0, 1000);
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException ignored) {
        }
        timer.cancel();

        return terminationListener.isTerminationFound();
    }

    /**
     * Can be used to fetch variable values when a debug hit is occurred.
     *
     * @param args  debug stopped event arguments
     * @param scope required variable scope
     * @return variable map with debug hit variables information
     * @throws BallerinaTestException if an error occurs when fetching debug hit variables.
     */
    public Map<String, Variable> fetchVariables(StoppedEventArguments args, VariableScope scope)
            throws BallerinaTestException {
        Map<String, Variable> variables = new HashMap<>();
        if (!hitListener.getConnector().isConnected()) {
            return variables;
        }
        StackTraceArguments stackTraceArgs = new StackTraceArguments();
        VariablesArguments variableArgs = new VariablesArguments();
        ScopesArguments scopeArgs = new ScopesArguments();
        stackTraceArgs.setThreadId(args.getThreadId());

        try {
            StackTraceResponse stackResp = hitListener.getConnector().getRequestManager().stackTrace(stackTraceArgs);
            StackFrame[] stackFrames = stackResp.getStackFrames();
            if (stackFrames.length == 0) {
                return variables;
            }
            scopeArgs.setFrameId(scope == VariableScope.LOCAL ? stackFrames[0].getId() : -stackFrames[0].getId());
            ScopesResponse scopesResp = hitListener.getConnector().getRequestManager().scopes(scopeArgs);
            variableArgs.setVariablesReference(scopesResp.getScopes()[0].getVariablesReference());
            VariablesResponse variableResp = hitListener.getConnector().getRequestManager().variables(variableArgs);
            Arrays.stream(variableResp.getVariables()).forEach(variable -> variables.put(variable.getName(), variable));
            return variables;
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching debug hit variables", e);
            throw new BallerinaTestException("Error occurred when fetching debug hit variables", e);
        }
    }

    /**
     * Can be used to fetch stack frames when a debug hit is occurred.
     *
     * @param args debug stopped event arguments.
     * @return StackFrame array with stack frames information.
     * @throws BallerinaTestException if an error occurs when fetching stack frames.
     */
    public StackFrame[] fetchStackFrames(StoppedEventArguments args) throws BallerinaTestException {
        if (!hitListener.getConnector().isConnected()) {
            throw new BallerinaTestException("DAP Client connector is not connected");
        }
        StackTraceArguments stackTraceArgs = new StackTraceArguments();
        stackTraceArgs.setThreadId(args.getThreadId());
        StackFrame[] stackFrames;
        StackTraceResponse stackTraceResp;

        try {
            stackTraceResp = hitListener.getConnector().getRequestManager().stackTrace(stackTraceArgs);
            stackFrames = stackTraceResp.getStackFrames();
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching stack frames", e);
            throw new BallerinaTestException("Error occurred when fetching stack frames", e);
        }
        return stackFrames;
    }

    /**
     * Can be used to fetch threads when a debug hit is occurred.
     *
     * @return Thread array with threads information.
     * @throws BallerinaTestException if an error occurs when fetching threads.
     */
    public org.eclipse.lsp4j.debug.Thread[] fetchThreads() throws BallerinaTestException {
        if (!hitListener.getConnector().isConnected()) {
            throw new BallerinaTestException("DAP Client connector is not connected");
        }
        org.eclipse.lsp4j.debug.Thread[] threads;
        ThreadsResponse threadsResponse;

        try {
            threadsResponse = hitListener.getConnector().getRequestManager().threads();
            threads = threadsResponse.getThreads();
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching threads", e);
            throw new BallerinaTestException("Error occurred when fetching threads", e);
        }
        return threads;
    }

    /**
     * Can be used to get child variables from parent variable.
     *
     * @param parentVariable parent variable
     * @param start          starting index of child variable
     * @param count          number of child variable count
     * @return variable map with child variables information
     * @throws BallerinaTestException if an error occurs when fetching debug hit child variables
     */
    public Map<String, Variable> fetchChildVariables(Variable parentVariable, int start, int count)
            throws BallerinaTestException {
        try {
            Map<String, Variable> variables = new HashMap<>();
            VariablesArguments childVarArgs = new VariablesArguments();
            childVarArgs.setVariablesReference(parentVariable.getVariablesReference());
            if (start >= 0 && count >= 0) {
                childVarArgs.setStart(start);
                childVarArgs.setCount(count);
            } else {
                childVarArgs.setCount(parentVariable.getIndexedVariables());
            }
            VariablesResponse response = hitListener.getConnector().getRequestManager().variables(childVarArgs);
            Arrays.stream(response.getVariables()).forEach(variable -> variables.put(variable.getName(), variable));
            return variables;
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching debug hit child variables", e);
            throw new BallerinaTestException("Error occurred when fetching debug hit child variables", e);
        }
    }

    /**
     * Can be used to get child variables from parent variable.
     *
     * @param parentVariable parent variable
     * @return variable map with child variables information
     * @throws BallerinaTestException if an error occurs when fetching debug hit child variables
     */
    public Map<String, Variable> fetchChildVariables(Variable parentVariable) throws BallerinaTestException {
        return this.fetchChildVariables(parentVariable, -1, -1);
    }

    /**
     * Can be used to assert variable name, value and type.
     *
     * @param variables debug hit variables
     * @param name      variable name
     * @param value     variable value
     * @param type      variable type
     */
    public void assertVariable(Map<String, Variable> variables, String name, String value, String type) {
        switch (assertionMode) {
            case HARD_ASSERT:
                Assert.assertTrue(variables.containsKey(name));
                Assert.assertEquals(variables.get(name).getValue(), value);
                Assert.assertEquals(variables.get(name).getType(), type);
                return;
            case SOFT_ASSERT:
                softAsserter.assertTrue(variables.containsKey(name));
                softAsserter.assertEquals(variables.get(name).getValue(), value);
                softAsserter.assertEquals(variables.get(name).getType(), type);
        }
    }

    /**
     * Can be used to assert any expression evaluation result.
     *
     * @param context     suspended context.
     * @param expression  expression.
     * @param resultValue result value.
     * @param resultType  result type.
     * @throws BallerinaTestException if an error occurs when evaluating the expression.
     */
    public void assertExpression(StoppedEventArguments context, String expression, String resultValue,
                                 String resultType) throws BallerinaTestException {
        Variable result = evaluateExpression(context, expression);
        switch (assertionMode) {
            case HARD_ASSERT:
                Assert.assertEquals(result.getValue(), resultValue);
                Assert.assertEquals(result.getType(), resultType);
                return;
            case SOFT_ASSERT:
                softAsserter.assertEquals(result.getValue(), resultValue);
                softAsserter.assertEquals(result.getType(), resultType);
        }
    }

    /**
     * Can be used to evaluate any given evaluation failure, against its expected error message.
     *
     * @param context      suspended context.
     * @param expression   expression.
     * @param errorMessage error message.
     * @throws BallerinaTestException if an error occurs when evaluating the expression.
     */
    public void assertEvaluationError(StoppedEventArguments context, String expression, String errorMessage)
            throws BallerinaTestException {
        Variable result = evaluateExpression(context, expression);
        if (assertionMode == AssertionMode.HARD_ASSERT) {
            if (result.getType() == null) {
                Pair<String, OutputEventArguments> output = waitForLastDebugOutput(2000);
                String errorOutput = output.getLeft();
                if (errorOutput.endsWith(System.lineSeparator())) {
                    errorOutput = errorOutput.replaceAll(System.lineSeparator() + "$", "");
                }
                Assert.assertNull(result.getValue());
                Assert.assertNull(result.getType());
                Assert.assertEquals(output.getRight().getCategory(), OutputEventArgumentsCategory.STDERR);
                Assert.assertEquals(errorOutput, errorMessage);
            } else {
                Assert.assertEquals(result.getType(), "error");
                Assert.assertEquals(result.getValue(), errorMessage);
            }
        } else if (assertionMode == AssertionMode.SOFT_ASSERT) {
            if (result.getType() == null) {
                Pair<String, OutputEventArguments> output = waitForLastDebugOutput(2000);
                String errorOutput = output.getLeft();
                if (errorOutput.endsWith(System.lineSeparator())) {
                    errorOutput = errorOutput.replaceAll(System.lineSeparator() + "$", "");
                }
                softAsserter.assertNull(result.getValue());
                softAsserter.assertNull(result.getType());
                softAsserter.assertEquals(output.getRight().getCategory(), OutputEventArgumentsCategory.STDERR);
                softAsserter.assertEquals(errorOutput, errorMessage);
            } else {
                softAsserter.assertEquals(result.getType(), "error");
                softAsserter.assertEquals(result.getValue(), errorMessage);
            }
        }
    }

    /**
     * Can be used to assert stack frame name, line and source.
     *
     * @param frame  debug hit stack frame.
     * @param name   stack frame name.
     * @param line   stack frame line.
     * @param source stack frame source.
     */
    public void assertCallStack(StackFrame frame, String name, int line, String source) {
        switch (assertionMode) {
            case HARD_ASSERT:
                Assert.assertEquals(frame.getName(), name);
                Assert.assertEquals(frame.getLine(), line);
                Assert.assertEquals(frame.getSource().getName(), source);
                return;
            case SOFT_ASSERT:
                softAsserter.assertEquals(frame.getName(), name);
                softAsserter.assertEquals(frame.getLine(), line);
                softAsserter.assertEquals(frame.getSource().getName(), source);
        }
    }

    /**
     * Can be used to evaluate any given expression, when a debug hit is occurred.
     *
     * @param args debug stopped event arguments.
     * @return the evaluation result as a variable.
     * @throws BallerinaTestException if an error occurs when evaluating the expression.
     */
    private Variable evaluateExpression(StoppedEventArguments args, String expr) throws BallerinaTestException {
        if (!hitListener.getConnector().isConnected()) {
            throw new BallerinaTestException("Connection error occurred when trying to fetch information from the " +
                    "debug server");
        }
        try {
            StackTraceArguments stackTraceArgs = new StackTraceArguments();
            stackTraceArgs.setThreadId(args.getThreadId());
            StackTraceResponse stackResp = hitListener.getConnector().getRequestManager().stackTrace(stackTraceArgs);
            StackFrame[] stackFrames = stackResp.getStackFrames();
            if (stackFrames.length == 0) {
                throw new BallerinaTestException("Error occurred when trying to fetch stack frames from the suspended" +
                        " thread.");
            }

            EvaluateArguments evaluateArguments = new EvaluateArguments();
            evaluateArguments.setFrameId(stackFrames[0].getId());
            evaluateArguments.setExpression(expr);
            EvaluateResponse evaluateResp = hitListener.getConnector().getRequestManager().evaluate(evaluateArguments);

            Variable result = new Variable();
            result.setName("Result");
            result.setType(evaluateResp.getType());
            if (evaluateResp.getResult() != null) {
                result.setValue(evaluateResp.getResult());
            }
            result.setNamedVariables(evaluateResp.getNamedVariables());
            result.setIndexedVariables(evaluateResp.getIndexedVariables());
            result.setVariablesReference(evaluateResp.getVariablesReference());
            return result;
        } catch (Exception e) {
            LOGGER.warn("Error occurred when evaluating expression", e);
            throw new BallerinaTestException("Error occurred when evaluating expression", e);
        }
    }

    /**
     * Can be used to fetch completion items for expressions typed in debug console, when a debug hit is occurred.
     *
     * @param args       debug stopped event arguments.
     * @param expression expression typed in debug console.
     * @return the completion items.
     */
    public Map<String, CompletionItem> fetchCompletions(StoppedEventArguments args, String expression)
            throws BallerinaTestException {
        Map<String, CompletionItem> completions = new HashMap<>();
        if (!hitListener.getConnector().isConnected()) {
            throw new BallerinaTestException("DAP Client connector is not connected");
        }

        CompletionsResponse completionsResponse;
        CompletionsArguments completionsArguments = new CompletionsArguments();
        StackFrame[] stackFrames = fetchStackFrames(args);
        completionsArguments.setText(expression);
        completionsArguments.setColumn(expression.length() + 1);
        completionsArguments.setLine(1);
        completionsArguments.setFrameId(stackFrames[0].getId());

        try {
            completionsResponse = hitListener.getConnector().getRequestManager().completions(completionsArguments);
            Arrays.stream(completionsResponse.getTargets()).forEach(completion -> completions.put(completion.getLabel(),
                    completion));
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching completions", e);
            throw new BallerinaTestException("Error occurred when fetching completions", e);
        }
        return completions;
    }

    /**
     * Can be used to assert completion item label, text and type.
     *
     * @param completionItem debug expression completion item.
     * @param label          completion item label.
     */
    public void assertCompletions(Map<String, CompletionItem> completionItem, String label) {
        switch (assertionMode) {
            case HARD_ASSERT:
                Assert.assertTrue(completionItem.containsKey(label));
                Assert.assertEquals(completionItem.get(label).getLabel(), label);
                Assert.assertEquals(completionItem.get(label).getText(), label);
                return;
            case SOFT_ASSERT:
                softAsserter.assertTrue(completionItem.containsKey(label));
                softAsserter.assertEquals(completionItem.get(label).getLabel(), label);
                softAsserter.assertEquals(completionItem.get(label).getText(), label);
        }
    }

    /**
     * Terminates the debug session.
     */
    public void terminateDebugSession() {
        testBreakpoints.clear();
        if (debugClientConnector != null && debugClientConnector.isConnected()) {
            try {
                debugClientConnector.disconnectFromServer();
            } catch (Exception e) {
                LOGGER.warn("Error occurred when terminating debug session");
            }
        }
        isConnected = false;
        debugClientConnector = null;

        if (balClient != null) {
            balClient.terminateProcessWithDescendants(debuggeeProcess);
            balClient = null;
        }
    }

    public void beginSoftAssertions() {
        softAsserter = new SoftAssert();
    }

    public void endSoftAssertions() {
        softAsserter.assertAll();
    }

    public void setClientSupportsRunInTerminal(boolean clientSupportsRunInTerminal) {
        this.clientSupportsRunInTerminal = clientSupportsRunInTerminal;
    }

    /**
     * Program resume options.
     */
    public enum DebugResumeKind {
        NEXT_BREAKPOINT,
        STEP_IN,
        STEP_OUT,
        STEP_OVER
    }

    /**
     * Debug variable scope types.
     */
    public enum VariableScope {
        GLOBAL,
        LOCAL
    }

    public static void destroy() {
        balServer.cleanup();
    }

    public BalServer getBalServer() {
        return balServer;
    }

    /**
     * Debugger test framework supports both modes (hard assertions and soft assertions).
     */
    public enum AssertionMode {
        HARD_ASSERT,
        SOFT_ASSERT,
    }
}
