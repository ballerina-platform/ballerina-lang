/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.debugadapter.evaluation.ExpressionEvaluator;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.launch.Launcher;
import org.ballerinalang.debugadapter.launch.PackageLauncher;
import org.ballerinalang.debugadapter.launch.SingleFileLauncher;
import org.ballerinalang.debugadapter.utils.PackageUtils;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.NamedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.PauseArguments;
import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetFunctionBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetFunctionBreakpointsResponse;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceArguments;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.SourceResponse;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.TerminateArguments;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.DebugExecutionManager.LOCAL_HOST;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRAND_VAR_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.GENERATED_VAR_PREFIX;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.closeQuietly;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getRectifiedSourcePath;
import static org.ballerinalang.debugadapter.variable.VariableUtils.removeRedundantQuotes;
import static org.eclipse.lsp4j.debug.OutputEventArgumentsCategory.STDERR;
import static org.eclipse.lsp4j.debug.OutputEventArgumentsCategory.STDOUT;
import static org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper.LAMBDA;

/**
 * JBallerina debug server implementation.
 */
public class JBallerinaDebugServer implements IDebugProtocolServer {

    private IDebugProtocolClient client;
    private DebugExecutionManager executionManager;
    private JDIEventProcessor eventProcessor;
    private ExpressionEvaluator evaluator;
    private final ExecutionContext context;
    private Project project;
    private String projectRoot;
    private ThreadReferenceProxyImpl activeThread;
    private SuspendedContext suspendedContext;
    private boolean terminationRequestReceived = false;

    private final AtomicLong nextVarReference = new AtomicLong();
    private final Map<Long, StackFrameProxyImpl> stackFramesMap = new HashMap<>();
    private final Map<Long, BCompoundVariable> loadedVariables = new HashMap<>();
    private final Map<Long, Long> variableToStackFrameMap = new HashMap<>();
    private final Map<Long, Long> scopeIdToFrameIdMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);
    private static final String STRAND_FIELD_NAME = "name";
    private static final String FRAME_TYPE_START = "start";
    private static final String FRAME_TYPE_WORKER = "worker";
    private static final String FRAME_TYPE_ANONYMOUS = "anonymous";
    private static final String FRAME_SEPARATOR = ":";
    private static final String WORKER_LAMBDA_REGEX = "(\\$lambda\\$)\\b(.*)\\b(\\$lambda)(.*)";
    private static final String SCOPE_NAME_LOCAL = "Local";
    private static final String SCOPE_NAME_GLOBAL = "Global";

    public JBallerinaDebugServer() {
        context = new ExecutionContext(this);
    }

    public ExecutionContext getContext() {
        return context;
    }

    private IDebugProtocolClient getClient() {
        return client;
    }

    public void setExecutionManager(DebugExecutionManager executionManager) {
        this.executionManager = executionManager;
    }

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        Capabilities capabilities = new Capabilities();
        // supported capabilities
        capabilities.setSupportsConfigurationDoneRequest(true);
        capabilities.setSupportsTerminateRequest(true);
        capabilities.setSupportTerminateDebuggee(true);
        // Todo - Implement
        capabilities.setSupportsCompletionsRequest(false);
        capabilities.setSupportsRestartRequest(false);
        capabilities.setSupportsConditionalBreakpoints(false);
        // unsupported capabilities
        capabilities.setSupportsHitConditionalBreakpoints(false);
        capabilities.setSupportsModulesRequest(false);
        capabilities.setSupportsStepBack(false);
        capabilities.setSupportsTerminateThreadsRequest(false);
        capabilities.setSupportsFunctionBreakpoints(false);
        capabilities.setSupportsFunctionBreakpoints(false);

        context.setClient(client);
        eventProcessor = new JDIEventProcessor(context);
        getClient().initialized();
        return CompletableFuture.completedFuture(capabilities);
    }

    @Override
    public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
        Breakpoint[] breakpoints = Arrays.stream(args.getBreakpoints())
                .map((SourceBreakpoint sourceBreakpoint) -> toBreakpoint(sourceBreakpoint, args.getSource()))
                .toArray(Breakpoint[]::new);

        SetBreakpointsResponse breakpointsResponse = new SetBreakpointsResponse();
        breakpointsResponse.setBreakpoints(breakpoints);
        String path = args.getSource().getPath();
        eventProcessor.setBreakpointsList(path, breakpoints);
        return CompletableFuture.completedFuture(breakpointsResponse);
    }

    @Override
    public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> launch(Map<String, Object> args) {
        clearState();
        loadProjectInfo(args);
        Launcher launcher = project instanceof SingleFileProject ? new SingleFileLauncher(projectRoot, args) :
                new PackageLauncher(projectRoot, args);
        try {
            context.setLaunchedProcess(launcher.start());
        } catch (IOException e) {
            sendOutput("Failed to launch the ballerina program due to: " + e.toString(), STDERR);
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture.runAsync(() -> {
            if (context.getLaunchedProcess().isPresent()) {
                BufferedReader errorStream = context.getErrorStream();
                String line;
                try {
                    while ((line = errorStream.readLine()) != null) {
                        // Todo - Redirect back to error stream, once the ballerina program output is fixed to use
                        //  the STDOUT stream.
                        sendOutput(line, STDOUT);
                    }
                } catch (IOException ignored) {
                }
            }
        });

        CompletableFuture.runAsync(() -> {
            if (context.getLaunchedProcess().isPresent()) {
                BufferedReader inputStream = context.getInputStream();
                String line;
                try {
                    sendOutput("Waiting for debug process to start...", STDOUT);
                    while ((line = inputStream.readLine()) != null) {
                        if (line.contains("Listening for transport dt_socket")) {
                            launcher.attachToLaunchedProcess(this);
                            eventProcessor.startListening();
                        }
                        sendOutput(line, STDOUT);
                    }
                } catch (IOException ignored) {
                }
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> attach(Map<String, Object> args) {
        clearState();
        loadProjectInfo(args);
        String hostName = args.get("debuggeeHost") == null ? "" : args.get("debuggeeHost").toString();
        String portName = args.get("debuggeePort").toString();

        try {
            executionManager = new DebugExecutionManager(this);
            context.setDebuggee(new VirtualMachineProxyImpl(executionManager.attach(hostName, portName)));
            EventRequestManager erm = context.getEventManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            eventProcessor.startListening();
        } catch (IOException | IllegalConnectorArgumentsException e) {
            String host = !hostName.isEmpty() ? hostName : LOCAL_HOST;
            sendOutput(String.format("Failed to attach to the target VM, address: '%s:%s'.", host, portName), STDERR);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ThreadsResponse> threads() {
        ThreadsResponse threadsResponse = new ThreadsResponse();
        if (eventProcessor == null) {
            return CompletableFuture.completedFuture(threadsResponse);
        }
        Map<Long, ThreadReferenceProxyImpl> threadsMap = getActiveStrandThreads();
        if (threadsMap == null) {
            return CompletableFuture.completedFuture(threadsResponse);
        }
        Thread[] threads = new Thread[threadsMap.size()];
        threadsMap.values().stream().map(this::toThread).collect(Collectors.toList()).toArray(threads);
        threadsResponse.setThreads(threads);
        return CompletableFuture.completedFuture(threadsResponse);
    }

    @Override
    public CompletableFuture<Void> pause(PauseArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
        activeThread = getAllThreads().get(args.getThreadId());
        StackTraceResponse stackTraceResponse = new StackTraceResponse();
        stackTraceResponse.setStackFrames(new StackFrame[0]);
        try {
            List<StackFrame> balFrames = activeThread.frames().stream()
                    .map(this::toDapStackFrame)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // If the last instruction is step-in and there are no valid source information in the top-most stack
            // frame, that means that the debugger has stepped into an unsupported source(i.e. lang library, standard
            // library, imported module from central).
            // Therefore we need to manually rollback into the previous debugging state by sending a step-out request
            // or otherwise, this might produce unpredictable behaviors under different contexts as described in
            // (https://github.com/ballerina-platform/ballerina-lang/issues/28071).
            //
            // Todo - Enable and refactor accordingly after adding support for external module debugging support.
            // if (!isValidFrame(balFrames.get(0)) && lastInstruction == DebugInstruction.STEP_IN) {
            //     sendOutput("Trying to step into an unsupported source! Rolling back into the previous state..",
            //                       ONSOLE);
            //     stepOut(activeThread.uniqueID());
            //     return CompletableFuture.completedFuture(stackTraceResponse);
            //  }

            StackFrame[] validFrames = balFrames.stream()
                    .filter(JBallerinaDebugServer::isValidFrame)
                    .toArray(StackFrame[]::new);

            stackTraceResponse.setStackFrames(validFrames);
            return CompletableFuture.completedFuture(stackTraceResponse);
        } catch (JdiProxyException e) {
            LOGGER.error(e.getMessage(), e);
            return CompletableFuture.completedFuture(stackTraceResponse);
        }
    }

    @Override
    public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
        // Creates local variable scope.
        Scope localScope = new Scope();
        localScope.setName(SCOPE_NAME_LOCAL);
        scopeIdToFrameIdMap.put(nextVarReference.get(), args.getFrameId());
        localScope.setVariablesReference(nextVarReference.getAndIncrement());
        // Creates global variable scope.
        Scope globalScope = new Scope();
        globalScope.setName(SCOPE_NAME_GLOBAL);
        scopeIdToFrameIdMap.put(nextVarReference.get(), -args.getFrameId());
        globalScope.setVariablesReference(nextVarReference.getAndIncrement());

        Scope[] scopes = {localScope, globalScope};
        ScopesResponse scopesResponse = new ScopesResponse();
        scopesResponse.setScopes(scopes);
        return CompletableFuture.completedFuture(scopesResponse);
    }

    @Override
    public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
        VariablesResponse variablesResponse = new VariablesResponse();
        variablesResponse.setVariables(new Variable[0]);
        try {
            // If frameId < 0, returns global variables.
            // IF frameId >= 0, returns local variables.
            Long frameId = scopeIdToFrameIdMap.get(args.getVariablesReference());
            if (frameId != null && frameId < 0) {
                StackFrameProxyImpl stackFrame = stackFramesMap.get(-frameId);
                if (stackFrame == null) {
                    variablesResponse.setVariables(new Variable[0]);
                    return CompletableFuture.completedFuture(variablesResponse);
                }
                suspendedContext = new SuspendedContext(project, context.getDebuggee(), activeThread, stackFrame);
                variablesResponse.setVariables(computeGlobalVariables(suspendedContext, args.getVariablesReference()));
            } else if (frameId != null) {
                StackFrameProxyImpl stackFrame = stackFramesMap.get(frameId);
                if (stackFrame == null) {
                    variablesResponse.setVariables(new Variable[0]);
                    return CompletableFuture.completedFuture(variablesResponse);
                }
                suspendedContext = new SuspendedContext(project, context.getDebuggee(), activeThread, stackFrame);
                variablesResponse.setVariables(computeStackFrameVariables(args));
            } else {
                variablesResponse.setVariables(computeChildVariables(args));
            }
            return CompletableFuture.completedFuture(variablesResponse);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            variablesResponse.setVariables(new Variable[0]);
            return CompletableFuture.completedFuture(variablesResponse);
        }
    }

    @Override
    public CompletableFuture<SourceResponse> source(SourceArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
        prepareFor(DebugInstruction.CONTINUE);
        context.getDebuggee().resume();
        ContinueResponse continueResponse = new ContinueResponse();
        continueResponse.setAllThreadsContinued(true);
        context.setLastInstruction(DebugInstruction.CONTINUE);
        return CompletableFuture.completedFuture(continueResponse);
    }

    @Override
    public CompletableFuture<Void> next(NextArguments args) {
        prepareFor(DebugInstruction.STEP_OVER);
        eventProcessor.sendStepRequest(args.getThreadId(), StepRequest.STEP_OVER);
        context.setLastInstruction(DebugInstruction.STEP_OVER);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepIn(StepInArguments args) {
        prepareFor(DebugInstruction.STEP_IN);
        eventProcessor.sendStepRequest(args.getThreadId(), StepRequest.STEP_INTO);
        context.setLastInstruction(DebugInstruction.STEP_IN);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepOut(StepOutArguments args) {
        stepOut(args.getThreadId());
        return CompletableFuture.completedFuture(null);
    }

    void stepOut(long threadId) {
        prepareFor(DebugInstruction.STEP_OUT);
        eventProcessor.sendStepRequest(threadId, StepRequest.STEP_OUT);
        context.setLastInstruction(DebugInstruction.STEP_OUT);
    }

    public void sendOutput(String output, String category) {
        if (output.contains("Listening for transport dt_socket")
                || output.contains("Please start the remote debugging client to continue")
                || output.contains("JAVACMD")
                || output.contains("Stream closed")) {
            return;
        }
        OutputEventArguments outputEventArguments = new OutputEventArguments();
        outputEventArguments.setOutput(output + System.lineSeparator());
        outputEventArguments.setCategory(category);
        getClient().output(outputEventArguments);
    }

    @Override
    public CompletableFuture<Void> setExceptionBreakpoints(SetExceptionBreakpointsArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<EvaluateResponse> evaluate(EvaluateArguments args) {
        EvaluateResponse response = new EvaluateResponse();
        // If the execution manager is not active, sends null response.
        if (executionManager == null || !executionManager.isActive()) {
            return CompletableFuture.completedFuture(response);
        }
        try {
            StackFrameProxyImpl frame = stackFramesMap.get(args.getFrameId());
            SuspendedContext ctx = new SuspendedContext(project, context.getDebuggee(), activeThread, frame);
            evaluator = Objects.requireNonNullElse(evaluator, new ExpressionEvaluator(ctx));

            Value result = evaluator.evaluate(args.getExpression());
            BVariable variable = VariableFactory.getVariable(ctx, result);
            if (variable == null) {
                return CompletableFuture.completedFuture(response);
            } else if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0L);
            } else if (variable instanceof BCompoundVariable) {
                long variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedVariables.put(variableReference, (BCompoundVariable) variable);
                updateVariableToStackFrameMap(args.getFrameId(), variableReference);
            }
            Variable dapVariable = variable.getDapVariable();
            response.setResult(dapVariable.getValue());
            response.setType(dapVariable.getType());
            response.setIndexedVariables(dapVariable.getIndexedVariables());
            response.setNamedVariables(dapVariable.getNamedVariables());
            response.setVariablesReference(dapVariable.getVariablesReference());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return CompletableFuture.completedFuture(response);
        }
    }

    @Override
    public CompletableFuture<SetFunctionBreakpointsResponse> setFunctionBreakpoints(
            SetFunctionBreakpointsArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    private Breakpoint toBreakpoint(SourceBreakpoint sourceBreakpoint, Source source) {
        Breakpoint breakpoint = new Breakpoint();
        breakpoint.setLine(sourceBreakpoint.getLine());
        breakpoint.setSource(source);
        breakpoint.setVerified(true);
        return breakpoint;
    }

    private Thread toThread(ThreadReferenceProxyImpl threadReference) {
        Thread thread = new Thread();
        thread.setId(threadReference.uniqueID());
        thread.setName(threadReference.name());
        return thread;
    }

    @Override
    public CompletableFuture<Void> disconnect(DisconnectArguments args) {
        terminationRequestReceived = true;
        boolean terminateDebuggee = Objects.requireNonNullElse(args.getTerminateDebuggee(), false);
        terminateServer(terminateDebuggee);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> terminate(TerminateArguments args) {
        terminationRequestReceived = true;
        terminateServer(true);
        return CompletableFuture.completedFuture(null);
    }

    void terminateServer(boolean terminateDebuggee) {
        // Destroys launched process, if presents.
        if (context.getLaunchedProcess().isPresent() && context.getLaunchedProcess().get().isAlive()) {
            killProcessWithDescendants(context.getLaunchedProcess().get());
        }
        // Destroys remote VM process, if `terminteDebuggee' flag is set.
        if (terminateDebuggee) {
            int exitCode = 0;
            if (context.getDebuggee().process() != null) {
                exitCode = killProcessWithDescendants(context.getDebuggee().process());
            }
            context.getDebuggee().exit(exitCode);
        }
        // If 'terminationRequestReceived' is false, debug server termination should have been triggered from the
        // JDI event processor, after receiving a 'VMDisconnected'/'VMExited' event.
        if (!terminationRequestReceived) {
            ExitedEventArguments exitedEventArguments = new ExitedEventArguments();
            exitedEventArguments.setExitCode(0L);
            context.getClient().exited(exitedEventArguments);
        }
        // Notifies user.
        String address = (executionManager.getHost().isPresent() && executionManager.getPort().isPresent()) ?
                executionManager.getHost().get() + ":" + executionManager.getPort().get() : "unknown";
        sendOutput(String.format("Disconnected from the target VM, address: '%s'", address), STDOUT);

        // Exits from the debug server VM.
        new java.lang.Thread(() -> {
            try {
                java.lang.Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }).start();
    }

    private static int killProcessWithDescendants(Process parent) {
        // Closes I/O streams.
        closeQuietly(parent.getInputStream());
        closeQuietly(parent.getOutputStream());
        closeQuietly(parent.getErrorStream());
        // Kills the descendants of the process. The descendants of a process are the children
        // of the process and the descendants of those children, recursively.
        parent.descendants().forEach(processHandle -> {
            boolean successful = processHandle.destroy();
            if (!successful) {
                processHandle.destroyForcibly();
            }
        });
        // Kills the parent process. Whether the process represented by this Process object will be normally
        // terminated or not, is implementation dependent.
        parent.destroy();
        try {
            parent.waitFor();
        } catch (InterruptedException ignored) {
        }
        return parent.exitValue();
    }

    public void connect(IDebugProtocolClient client) {
        this.client = client;
    }

    private synchronized void updateVariableToStackFrameMap(long parent, long child) {
        if (!variableToStackFrameMap.containsKey(parent)) {
            variableToStackFrameMap.put(child, parent);
            return;
        }

        Long parentRef;
        do {
            parentRef = variableToStackFrameMap.get(parent);
        } while (variableToStackFrameMap.containsKey(parentRef));
        variableToStackFrameMap.put(child, parentRef);
    }

    private StackFrame toDapStackFrame(StackFrameProxyImpl stackFrame) {
        try {
            long variableReference = nextVarReference.getAndIncrement();
            stackFramesMap.put(variableReference, stackFrame);

            if (!isBalStackFrame(stackFrame.getStackFrame())) {
                return null;
            }

            StackFrame dapStackFrame = new StackFrame();
            dapStackFrame.setId(variableReference);
            dapStackFrame.setName(getStackFrameName(stackFrame));
            dapStackFrame.setLine((long) stackFrame.location().lineNumber());
            dapStackFrame.setColumn(0L);

            // Adds ballerina source information.
            Path sourcePath = getRectifiedSourcePath(stackFrame.location(), project, projectRoot);
            if (sourcePath != null) {
                Source source = new Source();
                source.setPath(sourcePath.toString());
                source.setName(stackFrame.location().sourceName());
                dapStackFrame.setSource(source);
            }
            return dapStackFrame;
        } catch (AbsentInformationException | JdiProxyException e) {
            return null;
        }
    }

    /**
     * Can be used to get the stack frame name.
     *
     * @param stackFrame stackFrame.
     * @return Stack frame name.
     */
    private String getStackFrameName(StackFrameProxyImpl stackFrame) {
        ObjectReference strand;
        String stackFrameName;
        try {
            if (stackFrame.location().method().name().matches(WORKER_LAMBDA_REGEX)
                    && stackFrame.visibleVariableByName(STRAND_VAR_NAME) == null) {
                strand = (ObjectReference) ((ArrayReference) stackFrame.getStackFrame().getArgumentValues().get(0))
                        .getValue(0);
                stackFrameName = String.valueOf(strand.getValue(strand.referenceType().fieldByName(STRAND_FIELD_NAME)));
                stackFrameName = removeRedundantQuotes(stackFrameName);
                return FRAME_TYPE_WORKER + FRAME_SEPARATOR + stackFrameName;
            } else if (stackFrame.location().method().name().contains(LAMBDA)
                    && stackFrame.visibleVariableByName(STRAND_VAR_NAME) == null) {
                strand = (ObjectReference) ((ArrayReference) stackFrame.getStackFrame().getArgumentValues().get(0))
                        .getValue(0);
                Value stackFrameValue = strand.getValue(strand.referenceType().fieldByName(STRAND_FIELD_NAME));
                if (stackFrameValue == null) {
                    stackFrameName = FRAME_TYPE_ANONYMOUS;
                } else {
                    stackFrameName = removeRedundantQuotes(stackFrameValue.toString());
                }
                return FRAME_TYPE_START + FRAME_SEPARATOR + stackFrameName;
            } else if (stackFrame.location().method().name().contains(LAMBDA)
                    && stackFrame.visibleVariableByName(STRAND_VAR_NAME) != null) {
                strand = (ObjectReference) stackFrame.getValue(stackFrame.visibleVariableByName(STRAND_VAR_NAME));
                stackFrameName = String.valueOf(strand.getValue(strand.referenceType().fieldByName(STRAND_FIELD_NAME)));
                stackFrameName = removeRedundantQuotes(stackFrameName);
                return stackFrameName;
            } else {
                return stackFrame.location().method().name();
            }
        } catch (Exception e) {
            return FRAME_TYPE_ANONYMOUS;
        }
    }

    private Variable[] computeGlobalVariables(SuspendedContext context, long stackFrameReference) {
        String classQName = PackageUtils.getQualifiedClassName(context, INIT_CLASS_NAME);
        List<ReferenceType> cls = context.getAttachedVm().classesByName(classQName);
        if (cls.size() != 1) {
            return new Variable[0];
        }
        ArrayList<Variable> globalVars = new ArrayList<>();
        ReferenceType initClassReference = cls.get(0);
        for (Field field : initClassReference.allFields()) {
            String fieldName = IdentifierUtils.decodeIdentifier(field.name());
            if (!field.isPublic() || !field.isStatic() || fieldName.startsWith(GENERATED_VAR_PREFIX)) {
                continue;
            }
            Value fieldValue = initClassReference.getValue(field);
            BVariable variable = VariableFactory.getVariable(context, fieldName, fieldValue);
            if (variable == null) {
                continue;
            }
            if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0L);
            } else if (variable instanceof BCompoundVariable) {
                long variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedVariables.put(variableReference, (BCompoundVariable) variable);
                updateVariableToStackFrameMap(stackFrameReference, variableReference);
            }
            globalVars.add(variable.getDapVariable());
        }
        return globalVars.toArray(new Variable[0]);
    }

    private Variable[] computeStackFrameVariables(VariablesArguments args) throws Exception {
        StackFrameProxyImpl stackFrame = suspendedContext.getFrame();
        List<Variable> variables = new ArrayList<>();
        List<LocalVariableProxyImpl> localVariableProxies = stackFrame.visibleVariables();
        for (LocalVariableProxyImpl localVar : localVariableProxies) {
            String name = localVar.name();
            Value value = stackFrame.getValue(localVar);
            BVariable variable = VariableFactory.getVariable(suspendedContext, name, value);
            if (variable == null) {
                continue;
            } else if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0L);
            } else if (variable instanceof BCompoundVariable) {
                long variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedVariables.put(variableReference, (BCompoundVariable) variable);
                updateVariableToStackFrameMap(args.getVariablesReference(), variableReference);
            }
            Variable dapVariable = variable.getDapVariable();
            if (dapVariable != null) {
                variables.add(dapVariable);
            }
        }
        return variables.toArray(new Variable[0]);
    }

    private Variable[] computeChildVariables(VariablesArguments args) {
        BCompoundVariable parentVar = loadedVariables.get(args.getVariablesReference());
        Long stackFrameId = variableToStackFrameMap.get(args.getVariablesReference());
        if (stackFrameId == null) {
            return new Variable[0];
        }

        if (parentVar instanceof IndexedCompoundVariable) {
            // Handles indexed variables.
            int startIndex = (args.getStart() != null) ? args.getStart().intValue() : 0;
            int count = (args.getCount() != null) ? args.getCount().intValue() : 0;

            Either<Map<String, Value>, List<Value>> childVars = ((IndexedCompoundVariable) parentVar)
                    .getIndexedChildVariables(startIndex, count);
            if (childVars.isLeft()) {
                // Handles map-type indexed variables.
                return createVariableArrayFrom(args, childVars.getLeft());
            } else if (childVars.isRight()) {
                // Handles list-type indexed variables.
                return createVariableArrayFrom(args, childVars.getRight());
            }
            return new Variable[0];
        } else if (parentVar instanceof NamedCompoundVariable) {
            // Handles named variables.
            Map<String, Value> childVars = ((NamedCompoundVariable) parentVar).getNamedChildVariables();
            return createVariableArrayFrom(args, childVars);
        }

        return new Variable[0];
    }

    private Variable[] createVariableArrayFrom(VariablesArguments args, Map<String, Value> varMap) {
        return varMap.entrySet().stream().map(entry -> {
            String name = entry.getKey();
            Value value = entry.getValue();
            BVariable variable = VariableFactory.getVariable(suspendedContext, name, value);
            if (variable == null) {
                return null;
            } else if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0L);
            } else if (variable instanceof BCompoundVariable) {
                long variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedVariables.put(variableReference, (BCompoundVariable) variable);
                updateVariableToStackFrameMap(args.getVariablesReference(), variableReference);
            }
            return variable.getDapVariable();
        }).filter(Objects::nonNull).toArray(Variable[]::new);
    }

    private Variable[] createVariableArrayFrom(VariablesArguments args, List<Value> varMap) {
        int startIndex = (args.getStart() != null) ? args.getStart().intValue() : 0;
        AtomicInteger index = new AtomicInteger(startIndex);

        return varMap.stream().map(value -> {
            String name = String.format("[%d]", index.getAndIncrement());
            BVariable variable = VariableFactory.getVariable(suspendedContext, name, value);
            if (variable == null) {
                return null;
            } else if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0L);
            } else if (variable instanceof BCompoundVariable) {
                long variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedVariables.put(variableReference, (BCompoundVariable) variable);
                updateVariableToStackFrameMap(args.getVariablesReference(), variableReference);
            }
            return variable.getDapVariable();
        }).filter(Objects::nonNull).toArray(Variable[]::new);
    }

    private void loadProjectInfo(Map<String, Object> clientArgs) {
        String entryFilePath = clientArgs.get("script").toString();
        project = ProjectLoader.loadProject(Paths.get(entryFilePath));
        context.setSourceProject(project);
        projectRoot = project.sourceRoot().toAbsolutePath().toString();
    }

    /**
     * Returns a map of all currently running threads in the remote VM, against their unique ID.
     * <p>
     * Thread objects that have not yet been started (see {@link java.lang.Thread#start Thread.start()})
     * and thread objects that have completed their execution are not included in the returned list.
     */
    Map<Long, ThreadReferenceProxyImpl> getAllThreads() {
        if (context.getDebuggee() == null) {
            return null;
        }
        Collection<ThreadReference> threadReferences = context.getDebuggee().getVirtualMachine().allThreads();
        Map<Long, ThreadReferenceProxyImpl> threadsMap = new HashMap<>();

        // Filter thread references which are suspended, whose thread status is running, and which represents an active
        // ballerina strand.
        for (ThreadReference threadReference : threadReferences) {
            threadsMap.put(threadReference.uniqueID(), new ThreadReferenceProxyImpl(context.getDebuggee(),
                    threadReference));
        }
        return threadsMap;
    }

    /**
     * Returns a map of thread instances which correspond to an active ballerina strand, against their unique ID.
     */
    private Map<Long, ThreadReferenceProxyImpl> getActiveStrandThreads() {
        Map<Long, ThreadReferenceProxyImpl> allThreads = getAllThreads();
        if (allThreads == null) {
            return null;
        }

        Map<Long, ThreadReferenceProxyImpl> balStrandThreads = new HashMap<>();
        // Filter thread references which are suspended, whose thread status is running, and which represents an active
        // ballerina strand.
        allThreads.forEach((id, threadProxy) -> {
            ThreadReference threadReference = threadProxy.getThreadReference();
            if (threadReference.status() == ThreadReference.THREAD_STATUS_RUNNING
                    && !threadReference.name().equals("Reference Handler")
                    && !threadReference.name().equals("Signal Dispatcher")
                    && threadReference.isSuspended()
                    && isBalStrand(threadReference)
            ) {
                balStrandThreads.put(id, new ThreadReferenceProxyImpl(context.getDebuggee(), threadReference));
            }
        });
        return balStrandThreads;
    }

    /**
     * Validates whether the given DAP thread reference represents a ballerina strand.
     * <p>
     *
     * @param threadReference DAP thread reference
     * @return true if the given DAP thread reference represents a ballerina strand.
     */
    private static boolean isBalStrand(ThreadReference threadReference) {
        // Todo - Refactor to use thread proxy implementation
        try {
            return isBalStackFrame(threadReference.frames().get(0));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates whether the given DAP stack frame represents a ballerina call stack frame.
     *
     * @param frame DAP stack frame
     * @return true if the given DAP stack frame represents a ballerina call stack frame.
     */
    private static boolean isBalStackFrame(com.sun.jdi.StackFrame frame) {
        // Todo - Refactor to use stack frame proxy implementation
        try {
            return frame.location().sourceName().endsWith(BAL_FILE_EXT);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates a given ballerina stack frame for for its source information.
     *
     * @param stackFrame ballerina stack frame
     * @return true if its a valid ballerina frame
     */
    private static boolean isValidFrame(StackFrame stackFrame) {
        return stackFrame.getSource() != null && stackFrame.getLine() > 0;
    }

    /**
     * Clears previous state information and prepares for the given debug instruction type execution.
     */
    private void prepareFor(DebugInstruction instruction) {
        clearState();
        eventProcessor.restoreBreakpoints(instruction);
    }

    /**
     * Clears state information.
     */
    private void clearState() {
        suspendedContext = null;
        evaluator = null;
        activeThread = null;
        stackFramesMap.clear();
        loadedVariables.clear();
        variableToStackFrameMap.clear();
        nextVarReference.set(1);
    }

    /**
     * Returns whether any 'terminate' or 'disconnect' requests are received from the debug client.
     *
     * @return if any 'terminate' or 'disconnect' requests are received from the debug client.
     */
    public boolean isTerminationRequestReceived() {
        return terminationRequestReceived;
    }
}
