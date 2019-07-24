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

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import events.EventBus;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsArguments;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.eclipse.lsp4j.debug.OutputEventArgumentsCategory.STDERR;
import static org.eclipse.lsp4j.debug.OutputEventArgumentsCategory.STDOUT;

/**
 * Ballerina debug server.
 */
public class JBallerinaDebugServer implements IDebugProtocolServer {

    private IDebugProtocolClient client;
    private VirtualMachine debuggee;
    private int systemExit = 1;

    private EventBus eventBus;
    private Map<Long, ThreadReference> threadsMap = new HashMap<>();
    private String sourceRoot;
    private Process launchedProcess;
    private BufferedReader launchedStdoutStream;
    private BufferedReader launchedErrorStream;

    private IDebugProtocolClient getClient() {
        return client;
    }

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        Capabilities capabilities = new Capabilities();
        capabilities.setSupportsConfigurationDoneRequest(true);
        capabilities.setSupportsTerminateRequest(true);
        this.eventBus = new EventBus(client);
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

        this.eventBus.setBreakpointsList(breakpoints);

        return CompletableFuture.completedFuture(breakpointsResponse);
    }


    @Override
    public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> launch(Map<String, Object> args) {
        try {
            sourceRoot = args.get("sourceRoot").toString();
            String ballerinaHome = args.get("ballerina.home").toString();
            String ballerinaExec = ballerinaHome + File.separator + "bin" + File.separator + "ballerina";
            String balFile = args.get("script").toString();

            String packageName = args.get("package") == null ? "" : args.get("package").toString();
            if (!sourceRoot.endsWith(File.separator)) {
                sourceRoot += File.separator;
            }

            String debuggeePort = args.get("debuggeePort").toString();
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(ballerinaExec, "run", "--debug", debuggeePort, "--experimental",
                    balFile);
            launchedProcess = processBuilder.start();

            CompletableFuture.runAsync(() -> {
                if (launchedProcess != null) {
                    launchedStdoutStream = new BufferedReader(new InputStreamReader(launchedProcess.getInputStream(),
                            StandardCharsets.UTF_8));
                    String line;
                    try {
                        while ((line = launchedStdoutStream.readLine()) != null) {
                            if (line.contains("Listening for transport dt_socket")) {
                                attachToLaunchedProcess(Integer.parseInt(debuggeePort), packageName);
                            }
                            sendOutput(line, STDOUT);
                        }
                    } catch (IOException e) {

                    }
                }
            });

            CompletableFuture.runAsync(() -> {
                if (launchedProcess != null) {
                    launchedErrorStream = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream(),
                            StandardCharsets.UTF_8));
                    String line;
                    try {
                        while ((line = launchedErrorStream.readLine()) != null) {
                            sendOutput(line, STDERR);
                        }
                    } catch (IOException e) {

                    }
                }
            });
        } catch (IOException e) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.completedFuture(null);
    }

    private void attachToLaunchedProcess(int debuggeePort, String packageName) {
        try {
            debuggee = new DebuggerAttachingVM(debuggeePort).initialize();
            EventRequestManager erm = debuggee.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            this.eventBus.setDebuggee(debuggee);
            this.eventBus.startListening(sourceRoot, packageName);
        } catch (IOException e) {
        } catch (IllegalConnectorArgumentsException e) {
        }
    }

    @Override
    public CompletableFuture<Void> attach(Map<String, Object> args) {
        try {
            sourceRoot = args.get("sourceRoot").toString();
            String packageName = args.get("package") != null ? args.get("package").toString() : "";
            if (!sourceRoot.endsWith(File.separator)) {
                sourceRoot += File.separator;
            }
            int debuggeePort = Integer.parseInt(args.get("debuggeePort").toString());
            debuggee = new DebuggerAttachingVM(debuggeePort).initialize();

            EventRequestManager erm = debuggee.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            this.eventBus.setDebuggee(debuggee);
            this.eventBus.startListening(sourceRoot, packageName);

        } catch (IOException e) {
            return CompletableFuture.completedFuture(null);
        } catch (IllegalConnectorArgumentsException e) {
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ThreadsResponse> threads() {
        try {
            ThreadsResponse threadsResponse = new ThreadsResponse();
            Map<Long, ThreadReference> threadsMap = eventBus.getThreadsMap();
            Thread[] threads = new Thread[threadsMap.size()];
            threadsMap.values().stream().map(this::toThread).collect(Collectors.toList()).toArray(threads);
            threadsResponse.setThreads(threads);
            return CompletableFuture.completedFuture(threadsResponse);
        } catch (NullPointerException e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
        StackTraceResponse stackTraceResponse = new StackTraceResponse();
        StackFrame[] stackFrames = eventBus.getStackframesMap().get(args.getThreadId());
        StackFrame[] filteredStackFrames = Arrays.stream(stackFrames)
                .filter(stackFrame -> {
                    if (stackFrame.getSource() == null || stackFrame.getSource().getPath() == null) {
                        return false;
                    } else {
                        return stackFrame.getSource().getName().endsWith(".bal");
                    }
                }).toArray(StackFrame[]::new);
        stackTraceResponse.setStackFrames(filteredStackFrames);
        return CompletableFuture.completedFuture(stackTraceResponse);
    }

    @Override
    public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
        VariablesResponse variablesResponse = new VariablesResponse();
        Variable[] variables = eventBus.getVariablesMap().get(args.getVariablesReference());
        if (variables != null) {
            variablesResponse.setVariables(variables);
        }
        return CompletableFuture.completedFuture(variablesResponse);
    }

    @Override
    public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
        ScopesResponse scopesResponse = new ScopesResponse();
        String[] scopes = new String[2];
        scopes[0] = "Local";
        scopes[1] = "Global";
        Scope[] scopeArr = new Scope[2];
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
        debuggee.resume();
        ContinueResponse continueResponse = new ContinueResponse();
        continueResponse.setAllThreadsContinued(true);
        return CompletableFuture.completedFuture(continueResponse);
    }

    @Override
    public CompletableFuture<Void> next(NextArguments args) {
        ThreadReference thread = debuggee.allThreads()
                .stream()
                .filter(t -> t.uniqueID() == args.getThreadId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find thread"));
        StepRequest request = debuggee.eventRequestManager().createStepRequest(thread,
                StepRequest.STEP_LINE, StepRequest.STEP_OVER);

        request.addCountFilter(1); // next step only
        request.enable();
        debuggee.resume();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepIn(StepInArguments args) {
        ThreadReference thread = debuggee.allThreads()
                .stream()
                .filter(t -> t.uniqueID() == args.getThreadId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find thread"));
        StepRequest request = debuggee.eventRequestManager().createStepRequest(thread,
                StepRequest.STEP_LINE, StepRequest.STEP_INTO);

        request.addCountFilter(1); // next step only
        request.enable();
        debuggee.resume();
        return null;
    }

    @Override
    public CompletableFuture<Void> stepOut(StepOutArguments args) {
        ThreadReference thread = debuggee.allThreads()
                .stream()
                .filter(t -> t.uniqueID() == args.getThreadId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find thread"));
        StepRequest request = debuggee.eventRequestManager().createStepRequest(thread,
                StepRequest.STEP_LINE, StepRequest.STEP_OUT);

        request.addCountFilter(1); // next step only
        request.enable();
        debuggee.resume();
        return null;
    }

    private void sendOutput(String output, String category) {
        OutputEventArguments outputEventArguments = new OutputEventArguments();
        outputEventArguments.setOutput(output + System.lineSeparator());
        outputEventArguments.setCategory(category);
        getClient().output(outputEventArguments);
    }

    @Override
    public CompletableFuture<Void> setExceptionBreakpoints(SetExceptionBreakpointsArguments args) {

        return CompletableFuture.completedFuture(null);
    }

    private Breakpoint toBreakpoint(SourceBreakpoint sourceBreakpoint, Source source) {
        Breakpoint breakpoint = new Breakpoint();
        breakpoint.setLine(sourceBreakpoint.getLine());
        breakpoint.setSource(source);
        return breakpoint;
    }

    private Thread toThread(ThreadReference threadReference) {
        Thread thread = new Thread();
        threadsMap.put(threadReference.uniqueID(), threadReference);
        thread.setId(threadReference.uniqueID());
        thread.setName(threadReference.name());

        return thread;
    }

    private void exit() {
        if (launchedErrorStream != null) {
            try {
                launchedErrorStream.close();
            } catch (IOException e) {
            }
        }
        if (launchedStdoutStream != null) {
            try {
                launchedStdoutStream.close();
            } catch (IOException e) {
            }
        }
        if (launchedProcess != null) {
            launchedProcess.destroy();
        }

        systemExit = 0;
        new java.lang.Thread(() -> {
            try {
                java.lang.Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            System.exit(systemExit);
        }).start();
    }

    @Override
    public CompletableFuture<Void> disconnect(DisconnectArguments args) {
        this.exit();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> terminate(TerminateArguments args) {
        this.exit();
        debuggee.dispose();
        return CompletableFuture.completedFuture(null);
    }

    public void connect(IDebugProtocolClient client) {
        this.client = client;
    }

}
