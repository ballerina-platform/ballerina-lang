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
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import io.ballerina.runtime.IdentifierUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.launchrequest.Launch;
import org.ballerinalang.debugadapter.launchrequest.LaunchFactory;
import org.ballerinalang.debugadapter.terminator.OSUtils;
import org.ballerinalang.debugadapter.terminator.TerminatorFactory;
import org.ballerinalang.debugadapter.utils.PackageUtils;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.STRAND_VAR_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.GENERATED_VAR_PREFIX;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.findProjectRoot;
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
    private final DebugContext context;
    private Process launchedProcess;
    private BufferedReader launchedStdoutStream;
    private BufferedReader launchedErrorStream;
    private Path projectRoot;
    private VirtualMachineProxyImpl debuggeeVM;
    private ThreadReferenceProxyImpl activeThread;
    private SuspendedContext suspendedContext;

    private final Map<Long, ThreadReferenceProxyImpl> threadsMap = new HashMap<>();
    private final AtomicLong nextVarReference = new AtomicLong();
    private final Map<Long, StackFrameProxyImpl> stackFramesMap = new HashMap<>();
    private final Map<Long, BCompoundVariable> loadedVariables = new HashMap<>();
    private final Map<Long, Long> variableToStackFrameMap = new HashMap<>();
    private final Map<Long, Long> scopeIdToFrameIdMap = new HashMap<>();
    private static int systemExit = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);
    private static final String DEBUGGER_TERMINATED = "Debugger is terminated";
    private static final String DEBUGGER_FAILED_TO_ATTACH = "Debugger is failed to attach";
    private static final String STRAND_FIELD_NAME = "name";
    private static final String FRAME_TYPE_START = "start";
    private static final String FRAME_TYPE_WORKER = "worker";
    private static final String FRAME_TYPE_ANONYMOUS = "anonymous";
    private static final String FRAME_SEPARATOR = ":";
    private static final String WORKER_LAMBDA_REGEX = "(\\$lambda\\$)\\b(.*)\\b(\\$lambda)(.*)";

    public JBallerinaDebugServer() {
        context = new DebugContext();
    }

    private IDebugProtocolClient getClient() {
        return client;
    }

    public void setExecutionManager(DebugExecutionManager executionManager) {
        this.executionManager = executionManager;
    }

    public void setDebuggeeVM(VirtualMachine debuggeeVM) {
        this.debuggeeVM = new VirtualMachineProxyImpl(debuggeeVM);
    }

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        Capabilities capabilities = new Capabilities();
        capabilities.setSupportsConfigurationDoneRequest(true);
        capabilities.setSupportsTerminateRequest(true);
        context.setClient(client);
        eventProcessor = new JDIEventProcessor(context);
        getClient().initialized();
        return CompletableFuture.completedFuture(capabilities);
    }

    @Override
    public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
        SetBreakpointsResponse breakpointsResponse = new SetBreakpointsResponse();
        Breakpoint[] breakpoints = new Breakpoint[args.getBreakpoints().length];
        Arrays.stream(args.getBreakpoints()).map((SourceBreakpoint sourceBreakpoint) ->
                toBreakpoint(sourceBreakpoint, args.getSource())).collect(Collectors.toList()).toArray(breakpoints);
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
        Launch launcher = new LaunchFactory().getLauncher(args);
        String balFile = args.get("script").toString();
        updateProjectRoot(balFile);
        try {
            launchedProcess = launcher.start();
        } catch (IOException e) {
            sendOutput("Unable to launch debug adapter: " + e.toString(), STDERR);
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture.runAsync(() -> {
            if (launchedProcess != null) {
                launchedErrorStream = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream(),
                        StandardCharsets.UTF_8));
                String line;
                try {
                    while ((line = launchedErrorStream.readLine()) != null) {
                        sendOutput(line, STDERR);
                    }
                } catch (IOException ignored) {
                } finally {
                    this.exit(false);
                }
            }
        });

        CompletableFuture.runAsync(() -> {
            if (launchedProcess != null) {
                launchedStdoutStream = new BufferedReader(new InputStreamReader(launchedProcess.getInputStream(),
                        StandardCharsets.UTF_8));
                String line;
                try {
                    sendOutput("Waiting for debug process to start...", STDOUT);
                    while ((line = launchedStdoutStream.readLine()) != null) {
                        if (line.contains("Listening for transport dt_socket")) {
                            launcher.attachToLaunchedProcess(this);
                            context.setDebuggee(debuggeeVM.getVirtualMachine());
                            sendOutput("Compiling...", STDOUT);
                            eventProcessor.startListening();
                        }
                        sendOutput(line, STDOUT);
                    }
                } catch (IOException ignored) {
                } finally {
                    this.exit(false);
                }
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> attach(Map<String, Object> args) {
        clearState();
        try {
            String hostName = args.get("debuggeeHost") == null ? "" : args.get("debuggeeHost").toString();
            String portName = args.get("debuggeePort").toString();
            String entryPointFilePath = args.get("script").toString();
            updateProjectRoot(entryPointFilePath);

            executionManager = new DebugExecutionManager();
            debuggeeVM = new VirtualMachineProxyImpl(executionManager.attach(hostName, portName));
            EventRequestManager erm = debuggeeVM.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            context.setDebuggee(debuggeeVM.getVirtualMachine());
            eventProcessor.startListening();
        } catch (IOException | IllegalConnectorArgumentsException e) {
            this.sendOutput(DEBUGGER_FAILED_TO_ATTACH, STDERR);
            LOGGER.error(DEBUGGER_FAILED_TO_ATTACH);
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
        Map<Long, ThreadReference> threadsMap = eventProcessor.getThreadsMap();
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
        activeThread = new ThreadReferenceProxyImpl(debuggeeVM, eventProcessor.getThreadsMap().get(args.getThreadId()));
        StackTraceResponse stackTraceResponse = new StackTraceResponse();
        try {
            StackFrame[] filteredFrames = activeThread.frames().stream().map(this::toDapStackFrame)
                    .filter(Objects::nonNull).filter(frame -> frame.getSource().getName().endsWith(BAL_FILE_EXT)
                            && frame.getLine() > 0)
                    .toArray(StackFrame[]::new);
            stackTraceResponse.setStackFrames(filteredFrames);
            return CompletableFuture.completedFuture(stackTraceResponse);
        } catch (JdiProxyException e) {
            LOGGER.error(e.getMessage(), e);
            stackTraceResponse.setStackFrames(new StackFrame[0]);
            return CompletableFuture.completedFuture(stackTraceResponse);
        }
    }

    @Override
    public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
        // Creates local variable scope.
        Scope localScope = new Scope();
        localScope.setName("Local");
        scopeIdToFrameIdMap.put(nextVarReference.get(), args.getFrameId());
        localScope.setVariablesReference(nextVarReference.getAndIncrement());
        // Creates global variable scope.
        Scope globalScope = new Scope();
        globalScope.setName("Global");
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
                suspendedContext = new SuspendedContext(projectRoot, debuggeeVM, activeThread, stackFrame);
                variablesResponse.setVariables(computeGlobalVariables(suspendedContext, args.getVariablesReference()));
            } else if (frameId != null) {
                StackFrameProxyImpl stackFrame = stackFramesMap.get(frameId);
                if (stackFrame == null) {
                    variablesResponse.setVariables(new Variable[0]);
                    return CompletableFuture.completedFuture(variablesResponse);
                }
                suspendedContext = new SuspendedContext(projectRoot, debuggeeVM, activeThread, stackFrame);
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
        clearState();
        eventProcessor.restoreBreakpoints();
        debuggeeVM.resume();
        ContinueResponse continueResponse = new ContinueResponse();
        continueResponse.setAllThreadsContinued(true);
        return CompletableFuture.completedFuture(continueResponse);
    }

    @Override
    public CompletableFuture<Void> next(NextArguments args) {
        clearState();
        eventProcessor.sendStepRequest(args.getThreadId(), StepRequest.STEP_OVER);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepIn(StepInArguments args) {
        clearState();
        eventProcessor.restoreBreakpoints();
        eventProcessor.sendStepRequest(args.getThreadId(), StepRequest.STEP_INTO);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepOut(StepOutArguments args) {
        clearState();
        eventProcessor.restoreBreakpoints();
        eventProcessor.sendStepRequest(args.getThreadId(), StepRequest.STEP_OUT);
        return CompletableFuture.completedFuture(null);
    }

    private void sendOutput(String output, String category) {
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
            SuspendedContext context = new SuspendedContext(projectRoot, debuggeeVM, activeThread, frame);
            Value result = executionManager.evaluate(context, args.getExpression());
            BVariable variable = VariableFactory.getVariable(context, result);
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

    private Thread toThread(ThreadReference threadReference) {
        Thread thread = new Thread();
        threadsMap.put(threadReference.uniqueID(), new ThreadReferenceProxyImpl(debuggeeVM, threadReference));
        thread.setId(threadReference.uniqueID());
        thread.setName(threadReference.name());
        return thread;
    }

    private void exit(boolean terminateDebuggee) {
        if (terminateDebuggee) {
            new TerminatorFactory().getTerminator(OSUtils.getOperatingSystem()).terminate();
        }
        IOUtils.closeQuietly(launchedErrorStream);
        IOUtils.closeQuietly(launchedStdoutStream);
        if (launchedProcess != null) {
            launchedProcess.destroy();
        }
        systemExit = 0;
        new java.lang.Thread(() -> {
            try {
                java.lang.Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            System.exit(systemExit);
        }).start();
    }

    @Override
    public CompletableFuture<Void> disconnect(DisconnectArguments args) {
        boolean terminateDebuggee = args.getTerminateDebuggee() != null && args.getTerminateDebuggee();
        this.exit(terminateDebuggee);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> terminate(TerminateArguments args) {
        this.exit(true);
        LOGGER.info(DEBUGGER_TERMINATED);
        sendOutput(DEBUGGER_TERMINATED, STDOUT);
        return CompletableFuture.completedFuture(null);
    }

    public void connect(IDebugProtocolClient client) {
        this.client = client;
    }

    private synchronized void updateVariableToStackFrameMap(long parent, long child) {
        if (variableToStackFrameMap.get(parent) == null) {
            variableToStackFrameMap.put(child, parent);
            return;
        }
        Long rootNode;
        do {
            rootNode = variableToStackFrameMap.get(parent);
        } while (variableToStackFrameMap.get(rootNode) != null);
        variableToStackFrameMap.put(child, rootNode);
    }

    private StackFrame toDapStackFrame(StackFrameProxyImpl stackFrame) {
        try {
            long variableReference = nextVarReference.getAndIncrement();
            stackFramesMap.put(variableReference, stackFrame);
            String sourcePath = getRectifiedSourcePath(stackFrame.location(), projectRoot);
            Source source = new Source();
            source.setPath(sourcePath);
            source.setName(stackFrame.location().sourceName());

            StackFrame dapStackFrame = new StackFrame();
            dapStackFrame.setId(variableReference);
            dapStackFrame.setSource(source);
            dapStackFrame.setLine((long) stackFrame.location().lineNumber());
            dapStackFrame.setColumn(0L);
            dapStackFrame.setName(getStackFrameName(stackFrame));
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
        Map<String, Value> childVariables = parentVar.getChildVariables();
        Long stackFrameId = variableToStackFrameMap.get(args.getVariablesReference());
        if (stackFrameId == null) {
            return new Variable[0];
        }
        return childVariables.entrySet().stream().map(entry -> {
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

    private void updateProjectRoot(String balFilePath) {
        projectRoot = findProjectRoot(Paths.get(balFilePath));
        // If a ballerina project root is not detected, source type will be assumed as a single bal file.
        if (projectRoot == null) {
            // calculate projectRoot for single file
            File file = new File(balFilePath);
            File parentDir = file.getParentFile();
            projectRoot = parentDir.toPath();
        }
    }

    /**
     * Clears all the debug hit context information once the debuggee program is resumed.
     */
    private void clearState() {
        suspendedContext = null;
        activeThread = null;
        threadsMap.clear();
        stackFramesMap.clear();
        loadedVariables.clear();
        variableToStackFrameMap.clear();
        nextVarReference.set(1);
    }
}
