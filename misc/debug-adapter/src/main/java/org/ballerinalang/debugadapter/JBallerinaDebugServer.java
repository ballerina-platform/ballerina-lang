package org.ballerinalang.debugadapter;/*
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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import events.EventBus;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JBallerinaDebugServer implements IDebugProtocolServer {

    private IDebugProtocolClient client;
    private Map<String, SourceBreakpoint[]> breakpointsMap;
    private VirtualMachine debuggee;

    private EventBus eventBus;
    private Map<Long, Variable> variableMap;
    private Map<Long, ThreadReference> threadsMap = new HashMap<>();
    private String sourceRoot;

    AtomicInteger variableReference = new AtomicInteger();

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        Capabilities capabilities = new Capabilities();
        capabilities.setSupportsConfigurationDoneRequest(true);
        capabilities.setSupportsTerminateRequest(true);
        this.eventBus = new EventBus();
        return CompletableFuture.completedFuture(capabilities);
    }

    @Override
    public CompletableFuture<Void> launch(Map<String, Object> args) {
        try {
            sourceRoot = args.get("sourceRoot").toString();
            int debuggeePort = Integer.parseInt(args.get("debuggeePort").toString());
            debuggee = new DebuggerAttachingVM(debuggeePort).initialize();

            EventRequestManager erm = debuggee.eventRequestManager();
            erm.createClassPrepareRequest().enable();

            this.eventBus.startListening(debuggee, client);

        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        }
        client.initialized();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> attach(Map<String, Object> args) {
        try {
            sourceRoot = args.get("sourceRoot").toString();
            if (!sourceRoot.endsWith(File.separator)) {
                sourceRoot += File.separator;
            }
            int debuggeePort = Integer.parseInt(args.get("debuggeePort").toString());
            debuggee = new DebuggerAttachingVM(debuggeePort).initialize();

            EventRequestManager erm = debuggee.eventRequestManager();
            erm.createClassPrepareRequest().enable();

            this.eventBus.startListening(debuggee, client);

        } catch (IOException e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        }
        client.initialized();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
        return CompletableFuture.completedFuture(null);
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
    public CompletableFuture<ThreadsResponse> threads() {
        ThreadsResponse threadsResponse = new ThreadsResponse();
        List<ThreadReference> threadReferences = debuggee.allThreads();
        Thread[] threads = new Thread[threadReferences.size()];

        threadReferences.stream().map(this::toThread).collect(Collectors.toList()).toArray(threads);
        threadsResponse.setThreads(threads);
        return CompletableFuture.completedFuture(threadsResponse);
    }

    @Override
    public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
        try {
            StackTraceResponse stackTraceResponse = new StackTraceResponse();
            List<com.sun.jdi.StackFrame> frames = threadsMap.get(args.getThreadId()).frames();
            StackFrame[] stackFrames = new StackFrame[frames.size()];
            frames.stream()
                    .map(this::toStackFrame)
                    .collect(Collectors.toList()).toArray(stackFrames);
            stackTraceResponse.setStackFrames(stackFrames);
            return CompletableFuture.completedFuture(stackTraceResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
        VariablesResponse variablesResponse = new VariablesResponse();
        return CompletableFuture.completedFuture(variablesResponse);
    }

    @Override
    public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
        ScopesResponse scopesResponse = new ScopesResponse();
        String[] scopes = new String[2];
        scopes[0] = "Local";
        scopes[1] = "Global";
        Scope[] scopeArr = new Scope[2];
        Arrays.stream(scopes).map(this::toScope)
                .collect(Collectors.toList())
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

        request.addCountFilter(1);// next step only
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

        request.addCountFilter(1);// next step only
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

        request.addCountFilter(1);// next step only
        request.enable();
        debuggee.resume();
        return null;
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

    private Thread toThread(ThreadReference threadReference){
        Thread thread = new Thread();
        threadsMap.put(threadReference.uniqueID(), threadReference);
        thread.setId(threadReference.uniqueID());
        thread.setName(threadReference.name());

        return thread;
    }

    private StackFrame toStackFrame(com.sun.jdi.StackFrame stackFrame) {
        StackFrame newStackFrame = null;
        Source source = new Source();
        try {
            newStackFrame = new StackFrame();
            source.setPath(sourceRoot + stackFrame.location().sourcePath());
            source.setName(stackFrame.location().sourceName());
            newStackFrame.setSource(source);
            newStackFrame.setLine((long)stackFrame.location().lineNumber());
        } catch (AbsentInformationException e) {
            e.printStackTrace();
        }

        return newStackFrame;
    }

    private Scope toScope(String scopeName) {
        Scope scope = new Scope();
        scope.setName(scopeName);
        scope.setVariablesReference((long)variableReference.incrementAndGet());
        scope.setExpensive(false);
        return scope;
    }

    @Override
    public CompletableFuture<Void> disconnect(DisconnectArguments args) {
        System.exit(0);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> terminate(TerminateArguments args) {
        debuggee.exit(0);
        return CompletableFuture.completedFuture(null);
    }

    public void connect(IDebugProtocolClient client) {
        this.client = client;
    }

}
