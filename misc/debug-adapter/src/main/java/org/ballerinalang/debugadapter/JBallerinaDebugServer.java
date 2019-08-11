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
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.ballerinalang.debugadapter.launchrequest.Launch;
import org.ballerinalang.debugadapter.launchrequest.LaunchFactory;
import org.ballerinalang.debugadapter.terminator.OSUtils;
import org.ballerinalang.debugadapter.terminator.TerminatorFactory;
import org.ballerinalang.toml.model.Manifest;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.PackageUtils.findProjectRoot;
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
    Context context;
    private Process launchedProcess;
    private BufferedReader launchedStdoutStream;
    private BufferedReader launchedErrorStream;
    private String orgName = "";
    private Path projectRoot;

    AtomicInteger nextVarReference = new AtomicInteger();
    private Map<Long, com.sun.jdi.StackFrame> stackframesMap = new HashMap<Long, com.sun.jdi.StackFrame>();
    private Map<Long, Map<String, Value>> childVariables = new HashMap<>();

    private IDebugProtocolClient getClient() {
        return client;
    }

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        context = new Context();
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

        if (breakpoints.length > 0) {
            Breakpoint breakpoint = breakpoints[0];
            projectRoot = findProjectRoot(Paths.get(breakpoint.getSource().getPath()));
            if (projectRoot == null) {
                // calculate projectRoot for single file
                File file = new File(breakpoint.getSource().getPath());
                File parentDir = file.getParentFile();
                projectRoot = parentDir.toPath();
            } else {
                Manifest manifest = TomlParserUtils.getManifest(projectRoot);
                orgName = manifest.getProject().getOrgName();
            }
        }

        this.eventBus.setBreakpointsList(breakpoints);

        return CompletableFuture.completedFuture(breakpointsResponse);
    }


    @Override
    public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> launch(Map<String, Object> args) {
        nextVarReference.set(1);
        Launch launcher = new LaunchFactory().getLauncher(args);
        launchedProcess = launcher.start();
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
                    sendOutput(e.toString(), STDERR);
                }
            }
        });

        CompletableFuture.runAsync(() -> {
            if (launchedProcess != null) {
                launchedStdoutStream = new BufferedReader(new InputStreamReader(launchedProcess.getInputStream(),
                        StandardCharsets.UTF_8));
                String line;
                try {
                    while ((line = launchedStdoutStream.readLine()) != null) {
                        if (line.contains("Listening for transport dt_socket")) {
                            debuggee = launcher.attachToLaunchedProcess();
                            context.setDebuggee(debuggee);
                            this.eventBus.startListening();
                        }
                        sendOutput(line, STDOUT);
                    }
                } catch (IOException e) {

                }
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> attach(Map<String, Object> args) {
        nextVarReference.set(1);
        try {
            int debuggeePort = Integer.parseInt(args.get("debuggeePort").toString());
            debuggee = new DebuggerAttachingVM(debuggeePort).initialize();

            EventRequestManager erm = debuggee.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            context.setDebuggee(debuggee);
            this.eventBus.startListening();

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
        try {
            StackFrame[] stackFrames = eventBus.getThreadsMap().get(args.getThreadId())
                    .frames().stream()
                    .map(this::toDapStackFrame).toArray(StackFrame[]::new);

            StackFrame[] filteredStackFrames = Arrays.stream(stackFrames)
                    .filter(stackFrame -> {
                        if (stackFrame.getSource() == null || stackFrame.getSource().getPath() == null) {
                            return false;
                        } else {
                            return stackFrame.getSource().getName().endsWith(".bal");
                        }
                    }).toArray(StackFrame[]::new);
            stackTraceResponse.setStackFrames(filteredStackFrames);
        } catch (IncompatibleThreadStateException e) {

        }
        return CompletableFuture.completedFuture(stackTraceResponse);
    }

    private StackFrame toDapStackFrame(com.sun.jdi.StackFrame stackFrame) {
        long variableReference = (long) nextVarReference.getAndIncrement();
        stackframesMap.put(variableReference, stackFrame);

        StackFrame dapStackFrame = new StackFrame();
        Source source = new Source();
        try {
            String sourcePath = stackFrame.location().sourcePath();
            if (orgName.length() > 0 && sourcePath.startsWith(orgName)) {
                sourcePath = sourcePath.replaceFirst(orgName, "src");
            }

            source.setPath(projectRoot + File.separator + sourcePath);
            source.setName(stackFrame.location().sourceName());
        } catch (AbsentInformationException e) {
        }
        dapStackFrame.setId(variableReference);

        dapStackFrame.setSource(source);
        dapStackFrame.setLine((long) stackFrame.location().lineNumber());
        dapStackFrame.setName(stackFrame.location().method().name());

        return dapStackFrame;
    }

    @Override
    public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
        VariablesResponse variablesResponse = new VariablesResponse();
        com.sun.jdi.StackFrame stackFrame = stackframesMap.get(args.getVariablesReference());

        Variable[] dapVariables = new Variable[0];

        if (stackFrame == null) {
            Map<String, Value> values = childVariables.get(args.getVariablesReference());
            dapVariables = values.entrySet().stream().map(entry -> {
                Variable variable = new Variable();
                Value value = entry.getValue();
                String valueStr = (value == null) ? "null" : value.toString();
                variable.setValue(valueStr);
                variable.setName(entry.getKey());
                return variable;
            }).toArray(Variable[]::new);
        } else {
            try {
                dapVariables = stackFrame.getValues(stackFrame.visibleVariables())
                        .entrySet()
                        .stream()
                        .map(localVariableValueEntry -> getVariable(localVariableValueEntry)).toArray(Variable[]::new);
            } catch (AbsentInformationException ignored) {
            }
        }

        variablesResponse.setVariables(dapVariables);
        return CompletableFuture.completedFuture(variablesResponse);
    }

    private Variable getVariable(Map.Entry<LocalVariable, Value> localVariableValueEntry) {
        LocalVariable key = localVariableValueEntry.getKey();
        Value value = localVariableValueEntry.getValue();
        String varType = null;
        try {
            varType = key.type().name();
        } catch (ClassNotLoadedException ignored) {
        }
        Variable dapVariable = new Variable();

        dapVariable.setName(key.name());

        if ("org.ballerinalang.jvm.values.ArrayValue".equalsIgnoreCase(varType)) {
            List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();
            String stringValue;
            long variableReference = (long) nextVarReference.getAndIncrement();

            Field arrayValue = ((ObjectReferenceImpl) value)
                    .getValues(fields).entrySet().stream()
                    .filter(fieldValueEntry ->
                            fieldValueEntry.getValue() != null
                                    && fieldValueEntry.getKey().toString().endsWith("Values"))
                    .map(fieldValueEntry -> fieldValueEntry.getKey())
                    .collect(Collectors.toList()).get(0);
            List<Value> valueList = ((ArrayReference) ((ObjectReferenceImpl) value).getValue(arrayValue)).getValues();
            stringValue = valueList.toString();
            Map<String, Value> values = new HashMap<>();
            AtomicInteger nextVarIndex = new AtomicInteger(0);
            valueList.stream().forEach(item -> {
                int varIndex = nextVarIndex.getAndIncrement();
                values.put("[" + varIndex + "]", valueList.get(varIndex));
            });
            childVariables.put(variableReference,
                    values);
            dapVariable.setVariablesReference(variableReference);

            dapVariable.setType(varType);
            dapVariable.setValue(stringValue);
            return dapVariable;
        } else if ("java.lang.Object".equalsIgnoreCase(varType)) {
            // json
            // TODO : fix for json
//            List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();
//            Map<Field, Value> childValues = ((ObjectReferenceImpl) value).getValues(fields);

//            List<Field> childFields = fields.get(1).declaringType().allFields();
            return dapVariable;
        } else if ("org.ballerinalang.jvm.values.ObjectValue".equalsIgnoreCase(varType)) {
            Map<Field, Value> fieldValueMap = ((ObjectReferenceImpl) value)
                    .getValues(((ObjectReferenceImpl) value).referenceType().allFields());
            Map<String, Value> values = new HashMap<>();
            fieldValueMap.forEach((field, value1) -> {
                values.put(field.toString(), value1);
            });

            long variableReference = (long) nextVarReference.getAndIncrement();
            childVariables.put(variableReference, values);
            dapVariable.setVariablesReference(variableReference);
            dapVariable.setType(varType);
            dapVariable.setValue("Obj");
            return dapVariable;
        } else {
            dapVariable.setType(varType);
            String stringValue = value == null ? "" : value.toString();
            dapVariable.setValue(stringValue);
            return dapVariable;
        }
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

    private void exit(boolean terminateDebuggee) {
        if (terminateDebuggee) {
            new TerminatorFactory().getTerminator(OSUtils.getOperatingSystem()).terminate();
        }

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
        boolean terminateDebuggee = args.getTerminateDebuggee() == null ? false : args.getTerminateDebuggee();
        this.exit(terminateDebuggee);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> terminate(TerminateArguments args) {
        this.exit(true);
        return CompletableFuture.completedFuture(null);
    }

    public void connect(IDebugProtocolClient client) {
        this.client = client;
    }

}
