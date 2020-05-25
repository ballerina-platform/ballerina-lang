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
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import org.apache.commons.compress.utils.IOUtils;
import org.ballerinalang.debugadapter.launchrequest.Launch;
import org.ballerinalang.debugadapter.launchrequest.LaunchFactory;
import org.ballerinalang.debugadapter.terminator.OSUtils;
import org.ballerinalang.debugadapter.terminator.TerminatorFactory;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.ballerinalang.toml.model.Manifest;
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
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import static org.ballerinalang.debugadapter.utils.PackageUtils.findProjectRoot;
import static org.eclipse.lsp4j.debug.OutputEventArgumentsCategory.STDERR;
import static org.eclipse.lsp4j.debug.OutputEventArgumentsCategory.STDOUT;

/**
 * Ballerina debug server.
 */
public class JBallerinaDebugServer implements IDebugProtocolServer {

    private IDebugProtocolClient client;
    private DebugExecutionManager executionManager;
    private VirtualMachine debuggeeVM;
    private EventBus eventBus;
    private final DebugContext context;
    private Process launchedProcess;
    private BufferedReader launchedStdoutStream;
    private BufferedReader launchedErrorStream;
    private String orgName = "";
    private Path projectRoot;

    private final Map<Long, ThreadReference> threadsMap = new HashMap<>();
    private final AtomicInteger nextVarReference = new AtomicInteger();
    private final Map<Long, com.sun.jdi.StackFrame> stackframesMap = new HashMap<>();
    private final Map<Long, Map<String, Value>> childVariables = new HashMap<>();
    private static int systemExit = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);
    private static final String DEBUGGER_TERMINATED = "Debugger terminated";
    private static final String DEBUGGER_FAILED_TO_ATTACH = "Debugger failed to attach";

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
        this.debuggeeVM = debuggeeVM;
    }

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        Capabilities capabilities = new Capabilities();
        capabilities.setSupportsConfigurationDoneRequest(true);
        capabilities.setSupportsTerminateRequest(true);
        context.setClient(client);
        this.eventBus = new EventBus(context);
        getClient().initialized();
        return CompletableFuture.completedFuture(capabilities);
    }

    @Override
    public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
        SetBreakpointsResponse breakpointsResponse = new SetBreakpointsResponse();
        Breakpoint[] breakpoints = new Breakpoint[args.getBreakpoints().length];
        Arrays.stream(args.getBreakpoints())
                .map((SourceBreakpoint sourceBreakpoint) -> toBreakpoint(sourceBreakpoint, args.getSource()))
                .collect(Collectors.toList())
                .toArray(breakpoints);

        breakpointsResponse.setBreakpoints(breakpoints);

        String path = args.getSource().getPath();

        this.eventBus.setBreakpointsList(path, breakpoints);

        return CompletableFuture.completedFuture(breakpointsResponse);
    }

    @Override
    public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    private void updateProjectRoot(String balFile) {
        projectRoot = findProjectRoot(Paths.get(balFile));
        if (projectRoot == null) {
            // calculate projectRoot for single file
            File file = new File(balFile);
            File parentDir = file.getParentFile();
            projectRoot = parentDir.toPath();
        } else {
            Manifest manifest = TomlParserUtils.getManifest(projectRoot);
            orgName = manifest.getProject().getOrgName();
        }
    }

    @Override
    public CompletableFuture<Void> launch(Map<String, Object> args) {
        nextVarReference.set(1);
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
                            context.setDebuggee(debuggeeVM);
                            sendOutput("Compiling...", STDOUT);
                            this.eventBus.startListening();
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
        nextVarReference.set(1);
        try {
            String hostName = args.get("debuggeeHost") == null ? "" : args.get("debuggeeHost").toString();
            String portName = args.get("debuggeePort").toString();
            String entryPointFilePath = args.get("script").toString();
            updateProjectRoot(entryPointFilePath);

            executionManager = new DebugExecutionManager();
            debuggeeVM = executionManager.attach(hostName, portName);
            EventRequestManager erm = debuggeeVM.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            context.setDebuggee(debuggeeVM);
            this.eventBus.startListening();
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

        // Cannot provide threads if event bus is not initialized.
        if (eventBus == null) {
            return CompletableFuture.completedFuture(threadsResponse);
        }

        Map<Long, ThreadReference> threadsMap = eventBus.getThreadsMap();
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
        StackTraceResponse stackTraceResponse = new StackTraceResponse();
        try {
            StackFrame[] stackFrames = eventBus.getThreadsMap().get(args.getThreadId()).frames().stream()
                    .map(this::toDapStackFrame).toArray(StackFrame[]::new);
            StackFrame[] filteredStackFrames = Arrays.stream(stackFrames).filter(stackFrame ->
                    stackFrame != null && stackFrame.getSource() != null && stackFrame.getSource().getPath() != null
                            && stackFrame.getSource().getName().endsWith(".bal")).toArray(StackFrame[]::new);
            stackTraceResponse.setStackFrames(filteredStackFrames);
            return CompletableFuture.completedFuture(stackTraceResponse);
        } catch (IncompatibleThreadStateException e) {
            LOGGER.error(e.getMessage(), e);
            stackTraceResponse.setStackFrames(new StackFrame[0]);
            return CompletableFuture.completedFuture(stackTraceResponse);
        }
    }

    @Nullable
    private StackFrame toDapStackFrame(com.sun.jdi.StackFrame stackFrame) {
        long variableReference = nextVarReference.getAndIncrement();
        stackframesMap.put(variableReference, stackFrame);

        try {
            String sourcePath = getRectifiedPath(stackFrame.location());
            Source source = new Source();
            source.setPath(projectRoot + File.separator + sourcePath);
            source.setName(stackFrame.location().sourceName());

            StackFrame dapStackFrame = new StackFrame();
            dapStackFrame.setId(variableReference);
            dapStackFrame.setSource(source);
            dapStackFrame.setLine((long) stackFrame.location().lineNumber());
            dapStackFrame.setColumn(0L);
            dapStackFrame.setName(stackFrame.location().method().name());
            return dapStackFrame;
        } catch (AbsentInformationException e) {
            return null;
        }
    }

    @Override
    public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
        VariablesResponse variablesResponse = new VariablesResponse();
        com.sun.jdi.StackFrame stackFrame = stackframesMap.get(args.getVariablesReference());

        Variable[] dapVariables = new Variable[0];

        if (stackFrame == null) {
            Map<String, Value> values = childVariables.get(args.getVariablesReference());
            dapVariables = values.entrySet().stream().map(entry -> {
                Value value = entry.getValue();
                String varTypeStr = (value == null) ? "null" : value.type().name();
                String name = entry.getKey();

                BVariable variable = VariableFactory.getVariable(value, varTypeStr, name);
                if (variable == null) {
                    return null;
                } else if (variable instanceof BPrimitiveVariable) {
                    variable.getDapVariable().setVariablesReference(0L);
                } else if (variable instanceof BCompoundVariable) {
                    long variableReference = nextVarReference.getAndIncrement();
                    variable.getDapVariable().setVariablesReference(variableReference);
                    this.childVariables.put(variableReference, ((BCompoundVariable) variable).getChildVariables());
                }
                return variable.getDapVariable();
            }).filter(Objects::nonNull).toArray(Variable[]::new);
        } else {
            try {
                dapVariables = stackFrame.getValues(stackFrame.visibleVariables()).entrySet().stream()
                        .map(varValueEntry -> {
                            String varType;
                            try {
                                varType = varValueEntry.getKey().type().name();
                            } catch (ClassNotLoadedException e) {
                                varType = varValueEntry.getKey().toString();
                            }
                            String name = varValueEntry.getKey() != null ? varValueEntry.getKey().name() : "";
                            if (name.equals("__strand")) {
                                return null;
                            }

                            BVariable variable = VariableFactory.getVariable(varValueEntry.getValue(), varType, name);
                            if (variable == null) {
                                return null;
                            } else if (variable instanceof BPrimitiveVariable) {
                                variable.getDapVariable().setVariablesReference(0L);
                            } else if (variable instanceof BCompoundVariable) {
                                long variableReference = nextVarReference.getAndIncrement();
                                variable.getDapVariable().setVariablesReference(variableReference);
                                this.childVariables.put(variableReference, ((BCompoundVariable) variable)
                                        .getChildVariables());
                            }
                            return variable.getDapVariable();
                        }).filter(Objects::nonNull).toArray(Variable[]::new);
            } catch (AbsentInformationException ignored) {
            }
        }

        variablesResponse.setVariables(dapVariables);
        return CompletableFuture.completedFuture(variablesResponse);
    }

    @Override
    public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
        ScopesResponse scopesResponse = new ScopesResponse();
        String[] scopes = new String[1];
        scopes[0] = "Local";
//        scopes[1] = "Global";
        Scope[] scopeArr = new Scope[1];
        Arrays.stream(scopes).map(scopeName -> {
            Scope scope = new Scope();
            scope.setVariablesReference(args.getFrameId());
            scope.setName(scopeName);
            return scope;
        }).collect(Collectors.toList())
                .toArray(scopeArr);
        scopesResponse.setScopes(scopeArr);

        return CompletableFuture.completedFuture(scopesResponse);
    }

    @Override
    public CompletableFuture<SourceResponse> source(SourceArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
        eventBus.resetBreakpoints();
        debuggeeVM.resume();
        ContinueResponse continueResponse = new ContinueResponse();
        continueResponse.setAllThreadsContinued(true);
        return CompletableFuture.completedFuture(continueResponse);
    }

    @Override
    public CompletableFuture<Void> next(NextArguments args) {
        eventBus.createStepOverRequest(args.getThreadId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepIn(StepInArguments args) {
        eventBus.resetBreakpoints();
        eventBus.createStepRequest(args.getThreadId(), StepRequest.STEP_INTO);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepOut(StepOutArguments args) {
        eventBus.resetBreakpoints();
        eventBus.createStepRequest(args.getThreadId(), StepRequest.STEP_OUT);
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
            com.sun.jdi.StackFrame frame = stackframesMap.get(args.getFrameId());
            Optional<Value> result = executionManager.evaluate(frame, args.getExpression());
            if (result.isPresent()) {
                Value value = result.get();
                String valueTypeName = value.type().name();
                BVariable variable = VariableFactory.getVariable(value, valueTypeName, args.getExpression());
                if (variable == null) {
                    return CompletableFuture.completedFuture(response);
                } else if (variable instanceof BPrimitiveVariable) {
                    variable.getDapVariable().setVariablesReference(0L);
                } else if (variable instanceof BCompoundVariable) {
                    long variableReference = nextVarReference.getAndIncrement();
                    variable.getDapVariable().setVariablesReference(variableReference);
                    this.childVariables.put(variableReference, ((BCompoundVariable) variable).getChildVariables());
                }
                Variable dapVariable = variable.getDapVariable();
                response.setResult(dapVariable.getValue());
                response.setType(dapVariable.getType());
                response.setIndexedVariables(dapVariable.getIndexedVariables());
                response.setNamedVariables(dapVariable.getNamedVariables());
                response.setVariablesReference(dapVariable.getVariablesReference());
                return CompletableFuture.completedFuture(response);
            }
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
        threadsMap.put(threadReference.uniqueID(), threadReference);
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
        boolean terminateDebuggee = args.getTerminateDebuggee() == null ? false : args.getTerminateDebuggee();
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

    /**
     * Some additional processing is required to rectify the source path, as the source name will be the
     * relative path instead of just the file name, for the ballerina module sources.
     */
    private String getRectifiedPath(Location location) throws AbsentInformationException {
        String sourcePath = location.sourcePath();
        String sourceName = location.sourceName();

        // Note: directly using file separator as a regex will fail on windows.
        String[] srcNames = sourceName.split(File.separatorChar == '\\' ? "\\\\" : File.separator);
        String fileName = srcNames[srcNames.length - 1];
        String relativePath = sourcePath.replace(sourceName, fileName);

        if (!orgName.isEmpty() && relativePath.startsWith(orgName)) {
            relativePath = relativePath.replaceFirst(orgName, "src");
        }
        return relativePath;
    }
}
