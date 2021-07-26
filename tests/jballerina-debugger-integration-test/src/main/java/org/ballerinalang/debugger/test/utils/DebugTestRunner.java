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
import org.ballerinalang.debugger.test.utils.client.TestDAPClientConnector;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BalServer;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Collectors;

import static org.ballerinalang.debugger.test.utils.DebugUtils.findFreePort;

/**
 * Test util class for all debugger integration test cases.
 */
public class DebugTestRunner {

    public List<BallerinaTestDebugPoint> testBreakpoints = new ArrayList<>();
    public String testProjectPath;
    public String testEntryFilePath;

    private static Path testProjectBaseDir;
    private static Path testSingleFileBaseDir;
    private static BalServer balServer;
    private TestDAPClientConnector debugClientConnector;
    private boolean isConnected = false;
    private int port;
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugTestRunner.class);
    private static final int MAX_RETRY_COUNT = 3;
    private BMainInstance balClient = null;
    private Process debuggeeProcess;
    private DebugHitListener listener;
    private AssertionMode assertionMode;
    private SoftAssert softAsserter;

    public DebugTestRunner(String testProjectName, String testModuleFileName, boolean isProjectBasedTest) {
        if (isProjectBasedTest) {
            testProjectPath = testProjectBaseDir.toString() + File.separator + testProjectName;
            testEntryFilePath = Paths.get(testProjectPath, testModuleFileName).toString();
        } else {
            testProjectPath = Paths.get(testProjectBaseDir.toString(), testProjectName).toString();
            testEntryFilePath = Paths.get(testSingleFileBaseDir.toString(), testModuleFileName).toString();
        }

        // Hard assertions will be used by default.
        assertionMode = AssertionMode.HARD_ASSERT;
    }

    public static void initialize(Path projectResources, Path singleFileResources)
        throws BallerinaTestException, IOException {
        balServer = new BalServer();
        Path tempProjectDirectory = Files.createTempDirectory("bal-test-integration-debugger-project-");

        // Copy all the test resources to a temp dir.
        testSingleFileBaseDir = tempProjectDirectory.resolve("single-file-tests");
        FileUtils.copyFolder(singleFileResources, testSingleFileBaseDir);

        testProjectBaseDir = tempProjectDirectory.resolve("project-based-tests");
        FileUtils.copyFolder(projectResources, testProjectBaseDir);
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
        initDebugSession(executionKind, new HashMap<>());
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

        debugClientConnector = new TestDAPClientConnector(balServer.getServerHome(), testProjectPath,
                testEntryFilePath, port);

        if (debugClientConnector.isConnected()) {
            isConnected = true;
            LOGGER.info("Connection is already created.");
            return;
        }

        final int[] retryAttempt = {0};
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
                    LOGGER.info(String.format("Connected to the remote server at %s.%n%n",
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
            throw new BallerinaTestException(String.format("Connection to debug server at %s could not be " +
                "established%n", debugClientConnector.getAddress()));
        }

        setBreakpoints(testBreakpoints);

        if (executionKind == DebugUtils.DebuggeeExecutionKind.BUILD) {
            attachToDebuggee();
        } else {
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
    public void addBreakPoint(BallerinaTestDebugPoint breakpoint) throws BallerinaTestException {
        testBreakpoints.add(breakpoint);
        List<BallerinaTestDebugPoint> breakpointsToBeSent = testBreakpoints.stream().filter(bp ->
            bp.getSource().getPath().equals(breakpoint.getSource().getPath())).collect(Collectors.toList());

        if (debugClientConnector != null && debugClientConnector.isConnected()) {
            setBreakpoints(breakpointsToBeSent);
        }
    }

    public void setBreakpoints(List<BallerinaTestDebugPoint> breakPoints) throws BallerinaTestException {

        if (!isConnected) {
            return;
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
                debugClientConnector.getRequestManager().setBreakpoints(breakpointRequestArgs);
            } catch (Exception e) {
                LOGGER.error("SetBreakpoints request failed.", e);
                throw new BallerinaTestException("Breakpoints request failed.", e);
            }
        }
    }

    /**
     * Can be used to remove an already added test breakpoint on-the-fly.
     *
     * @param breakpoint breakpoint to be removed.
     * @throws BallerinaTestException if any exception is occurred during action.
     */
    public void removeBreakPoint(BallerinaTestDebugPoint breakpoint) throws BallerinaTestException {
        testBreakpoints.remove(breakpoint);
        List<BallerinaTestDebugPoint> breakpointsToBeSent = testBreakpoints.stream().filter(bp ->
            bp.getSource().getPath().equals(breakpoint.getSource().getPath())).collect(Collectors.toList());

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
    public void resumeProgram(StoppedEventArguments context, DebugResumeKind kind)
        throws BallerinaTestException {

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
     * Waits for a debug hit within a given time.
     *
     * @param timeoutMillis timeout.
     * @return pair of the debug point and context details.
     * @throws BallerinaTestException if a debug point is not found within the given time.
     */
    public Pair<BallerinaTestDebugPoint, StoppedEventArguments> waitForDebugHit(long timeoutMillis) throws
        BallerinaTestException {

        listener = new DebugHitListener(debugClientConnector);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(listener, 0, 1000);
        try {
            Thread.sleep(timeoutMillis);
        } catch (InterruptedException ignored) {
        }
        timer.cancel();

        if (!listener.isDebugHitFound()) {
            throw new BallerinaTestException("Timeout expired waiting for the debug hit");
        }
        return new ImmutablePair<>(listener.getDebugHitpoint(), listener.getDebugHitContext());
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
        if (!listener.getConnector().isConnected()) {
            return variables;
        }
        StackTraceArguments stackTraceArgs = new StackTraceArguments();
        VariablesArguments variableArgs = new VariablesArguments();
        ScopesArguments scopeArgs = new ScopesArguments();
        stackTraceArgs.setThreadId(args.getThreadId());

        try {
            StackTraceResponse stackTraceResp = listener.getConnector().getRequestManager()
                .stackTrace(stackTraceArgs);
            StackFrame[] stackFrames = stackTraceResp.getStackFrames();
            if (stackFrames.length == 0) {
                return variables;
            }
            scopeArgs.setFrameId(scope == VariableScope.LOCAL ? stackFrames[0].getId() : -stackFrames[0].getId());
            ScopesResponse scopesResp = listener.getConnector().getRequestManager().scopes(scopeArgs);
            variableArgs.setVariablesReference(scopesResp.getScopes()[0].getVariablesReference());
            VariablesResponse variableResp = listener.getConnector().getRequestManager().variables(variableArgs);
            Arrays.stream(variableResp.getVariables())
                .forEach(variable -> variables.put(variable.getName(), variable));
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
        if (!listener.getConnector().isConnected()) {
            throw new BallerinaTestException("DAP Client connector is not connected");
        }
        StackTraceArguments stackTraceArgs = new StackTraceArguments();
        stackTraceArgs.setThreadId(args.getThreadId());
        StackFrame[] stackFrames;
        StackTraceResponse stackTraceResp;

        try {
            stackTraceResp = listener.getConnector().getRequestManager().stackTrace(stackTraceArgs);
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
        if (!listener.getConnector().isConnected()) {
            throw new BallerinaTestException("DAP Client connector is not connected");
        }
        org.eclipse.lsp4j.debug.Thread[] threads;
        ThreadsResponse threadsResponse;

        try {
            threadsResponse = listener.getConnector().getRequestManager().threads();
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
     * @return variable map with child variables information
     * @throws BallerinaTestException if an error occurs when fetching debug hit child variables
     */
    public Map<String, Variable> fetchChildVariables(Variable parentVariable) throws BallerinaTestException {
        try {
            Map<String, Variable> variables = new HashMap<>();
            VariablesArguments childVarArgs = new VariablesArguments();
            childVarArgs.setVariablesReference(parentVariable.getVariablesReference());
            childVarArgs.setCount(parentVariable.getIndexedVariables());
            VariablesResponse response = listener.getConnector().getRequestManager().variables(childVarArgs);
            Arrays.stream(response.getVariables()).forEach(variable -> variables.put(variable.getName(), variable));
            return variables;
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching debug hit child variables", e);
            throw new BallerinaTestException("Error occurred when fetching debug hit child variables", e);
        }
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
        switch (assertionMode) {
            case HARD_ASSERT:
                Assert.assertEquals(result.getValue(), errorMessage);
                Assert.assertTrue(result.getType().equals("string") || result.getType().equals("error"));
                return;
            case SOFT_ASSERT:
                softAsserter.assertEquals(result.getValue(), errorMessage);
                softAsserter.assertTrue(result.getType().equals("string") || result.getType().equals("error"));
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
        if (!listener.getConnector().isConnected()) {
            throw new BallerinaTestException("Connection error occurred when trying to fetch information from the " +
                "debug server");
        }
        try {
            StackTraceArguments stackTraceArgs = new StackTraceArguments();
            stackTraceArgs.setThreadId(args.getThreadId());
            StackTraceResponse stackTraceResp = listener.getConnector().getRequestManager()
                .stackTrace(stackTraceArgs);
            StackFrame[] stackFrames = stackTraceResp.getStackFrames();
            if (stackFrames.length == 0) {
                throw new BallerinaTestException("Error occurred when trying to fetch stack frames from the suspended" +
                    " thread.");
            }

            EvaluateArguments evaluateArguments = new EvaluateArguments();
            evaluateArguments.setFrameId(stackFrames[0].getId());
            evaluateArguments.setExpression(expr);
            EvaluateResponse evaluateResp = listener.getConnector().getRequestManager().evaluate(evaluateArguments);

            Variable result = new Variable();
            result.setName("Result");
            result.setType(evaluateResp.getType());
            result.setValue(evaluateResp.getResult());
            result.setNamedVariables(evaluateResp.getNamedVariables());
            result.setIndexedVariables(evaluateResp.getIndexedVariables());
            result.setVariablesReference(evaluateResp.getVariablesReference());
            return result;
        } catch (Exception e) {
            LOGGER.warn("Error occurred when fetching debug hit variables", e);
            throw new BallerinaTestException("Error occurred when fetching debug hit variables", e);
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
            balClient.terminateProcess(debuggeeProcess, String.valueOf(port));
            balClient = null;
        }
    }

    public void beginSoftAssertions() {
        softAsserter = new SoftAssert();
    }

    public void endSoftAssertions() {
        softAsserter.assertAll();
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
