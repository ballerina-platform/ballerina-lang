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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.sun.tools.jdi.ObjectReferenceImpl;
import org.apache.commons.compress.utils.IOUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);
    public static final String DEBUGGER_TERMINATED = "Debugger terminated";
    public static final String DEBUGGER_FAILED_TO_ATTACH = "Debugger failed to attach";
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
            sendOutput("Unable to launch debug adapter", STDERR);
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
                } catch (IOException e) {
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
                            debuggee = launcher.attachToLaunchedProcess();
                            context.setDebuggee(debuggee);
                            sendOutput("Compiling...", STDOUT);
                            this.eventBus.startListening();
                        }
                        sendOutput(line, STDOUT);
                    }
                } catch (IOException e) {
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
            int debuggeePort = Integer.parseInt(args.get("debuggeePort").toString());
            String debuggeeHost = args.get("debuggeeHost") == null ? "" : args.get("debuggeeHost").toString();

            String balFile = args.get("script").toString();
            updateProjectRoot(balFile);

            debuggee = new DebuggerAttachingVM(debuggeePort, debuggeeHost).initialize();

            EventRequestManager erm = debuggee.eventRequestManager();
            ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
            classPrepareRequest.enable();
            context.setDebuggee(debuggee);
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
        stackTraceResponse.setStackFrames(new StackFrame[0]);

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
            LOGGER.error(e.getMessage(), e);
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
            sourcePath = sourcePath != null ? sourcePath : "";
            sourcePath = sourcePath.replaceFirst("tests" + File.separator + "tests", "tests");
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
                Value value = entry.getValue();
                String varTypeStr = (value == null) ? "null" : value.type().name();
                String name = entry.getKey();

                return getVariable(value, varTypeStr, name);
            }).filter(Objects::nonNull).toArray(Variable[]::new);
        } else {
            try {
                dapVariables = stackFrame.getValues(stackFrame.visibleVariables())
                        .entrySet()
                        .stream()
                        .map(localVariableValueEntry -> {
                            String varType;
                            try {
                                varType = localVariableValueEntry.getKey().type().name();
                            } catch (ClassNotLoadedException e) {
                                varType = localVariableValueEntry.getKey().toString();
                            }
                            String name = localVariableValueEntry.getKey() == null ? "" :
                                    localVariableValueEntry.getKey().name();
                            if (name.equals("__strand")) {
                                return null;
                            }

                            return getVariable(localVariableValueEntry.getValue(),
                                    varType, name);
                        }).filter(Objects::nonNull).toArray(Variable[]::new);
            } catch (AbsentInformationException ignored) {
            }
        }

        variablesResponse.setVariables(dapVariables);
        return CompletableFuture.completedFuture(variablesResponse);
    }

    private Variable getVariable(Value value, String varType, String varName) {

        Variable dapVariable = new Variable();
        dapVariable.setName(varName);

        if (value == null) {
            return null;
        }

        if ("org.ballerinalang.jvm.values.ArrayValue".equalsIgnoreCase(varType)) {
            long variableReference = (long) nextVarReference.getAndIncrement();
            Map<String, Value> values = VariableUtils.getChildVariables((ObjectReferenceImpl) value);
            this.childVariables.put(variableReference, values);
            dapVariable.setVariablesReference(variableReference);

            dapVariable.setType(varType);
            dapVariable.setValue("Array");
            return dapVariable;
        } else if ("java.lang.Object".equalsIgnoreCase(varType)
                || "org.ballerinalang.jvm.values.MapValue".equalsIgnoreCase(varType)
                || "org.ballerinalang.jvm.values.MapValueImpl".equalsIgnoreCase(varType) // for nested json arrays
        ) {
            // JSONs
            dapVariable.setType("Object");
            if (value.type() == null || value.type().name() == null) {
                dapVariable.setValue("null");
                return dapVariable;
            }
            if ("org.ballerinalang.jvm.values.ArrayValue".equalsIgnoreCase(value.type().name())) {
                // JSON array
                dapVariable.setValue("Array");
                long variableReference = (long) nextVarReference.getAndIncrement();
                Map<String, Value> values = VariableUtils.getChildVariables((ObjectReferenceImpl) value);
                this.childVariables.put(variableReference, values);
                dapVariable.setVariablesReference(variableReference);
                return dapVariable;
            } else if ("java.lang.Long".equalsIgnoreCase(value.type().name())
                    || "java.lang.Boolean".equalsIgnoreCase(value.type().name())
                    || "java.lang.Double".equalsIgnoreCase(value.type().name())) {
                // anydata
                Field valueField = ((ObjectReferenceImpl) value).referenceType().allFields().stream().filter(
                        field -> "value".equals(field.name())).collect(Collectors.toList()).get(0);
                Value longValue = ((ObjectReferenceImpl) value).getValue(valueField);
                dapVariable.setType(varType.split("\\.")[2]);
                dapVariable.setValue(longValue.toString());
                return dapVariable;
            } else if ("java.lang.String".equalsIgnoreCase(value.type().name())) {
                // union
                dapVariable.setType("String");
                String stringValue = value.toString();
                dapVariable.setValue(stringValue);
                return dapVariable;
            } else if ("org.ballerinalang.jvm.values.ErrorValue".equalsIgnoreCase(value.type().name())) {

                List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();

                Field valueField = fields.stream().filter(field ->
                        field.name().equals("reason"))
                        .collect(Collectors.toList()).get(0);

                Value error = ((ObjectReferenceImpl) value).getValue(valueField);
                dapVariable.setType("BError");
                dapVariable.setValue(error.toString());
                return dapVariable;
            } else if ("org.ballerinalang.jvm.values.XMLItem".equalsIgnoreCase(value.type().name())) {
                // TODO: support xml values
                dapVariable.setType("xml");
                dapVariable.setValue(value.toString());
                return dapVariable;
            } else {
                dapVariable.setType("Object");
                List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();

                Optional<Field> valueField = fields.stream().filter(field ->
                        field.typeName().equals("java.util.HashMap$Node[]")).findFirst();
                if (!valueField.isPresent()) {
                    return dapVariable;
                }
                Value jsonValue = ((ObjectReferenceImpl) value).getValue(valueField.get());
                String stringValue = jsonValue == null ? "null" : "Object";
                if (jsonValue == null) {
                    dapVariable.setValue(stringValue);
                    return dapVariable;
                }
                Map<String, Value> values = new HashMap<>();
                ((ArrayReference) jsonValue).getValues().stream().filter(Objects::nonNull).forEach(jsonMap -> {
                    List<Field> jsonValueFields = ((ObjectReferenceImpl) jsonMap).referenceType().visibleFields();
                    Optional<Field> jsonKeyField = jsonValueFields.stream().filter(field ->
                            field.name().equals("key")).findFirst();

                    Optional<Field> jsonValueField = jsonValueFields.stream().filter(field ->
                            field.name().equals("value")).findFirst();

                    if (jsonKeyField.isPresent() && jsonValueField.isPresent()) {
                        Value jsonKey = ((ObjectReferenceImpl) jsonMap).getValue(jsonKeyField.get());
                        Value jsonValue1 = ((ObjectReferenceImpl) jsonMap).getValue(jsonValueField.get());
                        values.put(jsonKey.toString(), jsonValue1);
                    }
                });
                long variableReference = (long) nextVarReference.getAndIncrement();
                childVariables.put(variableReference, values);
                dapVariable.setVariablesReference(variableReference);
                dapVariable.setValue(stringValue);
                return dapVariable;
            }
        } else if ("org.ballerinalang.jvm.values.ObjectValue".equalsIgnoreCase(varType)) {
            Map<Field, Value> fieldValueMap = ((ObjectReferenceImpl) value)
                    .getValues(((ObjectReferenceImpl) value).referenceType().allFields());
            Map<String, Value> values = new HashMap<>();
            fieldValueMap.forEach((field, value1) -> {
                // Filter out internal variables
                if (!field.name().startsWith("$") && !field.name().startsWith("nativeData")) {
                    values.put(field.name(), value1);
                }
            });

            long variableReference = (long) nextVarReference.getAndIncrement();
            childVariables.put(variableReference, values);
            dapVariable.setVariablesReference(variableReference);
            dapVariable.setType("Object");
            dapVariable.setValue("Object");
            return dapVariable;
        } else if ("java.lang.Long".equalsIgnoreCase(varType) || "java.lang.Boolean".equalsIgnoreCase(varType)
         || "java.lang.Double".equalsIgnoreCase(varType)) {
            Field valueField = ((ObjectReferenceImpl) value).referenceType().allFields().stream().filter(
                    field -> "value".equals(field.name())).collect(Collectors.toList()).get(0);
            Value longValue = ((ObjectReferenceImpl) value).getValue(valueField);
            dapVariable.setType(varType.split("\\.")[2]);
            dapVariable.setValue(longValue.toString());
            return dapVariable;
        } else if ("java.lang.String".equalsIgnoreCase(varType)) {
            dapVariable.setType("String");
            String stringValue = value.toString();
            dapVariable.setValue(stringValue);
            return dapVariable;
        } else if (varType.contains("$value$")) {
            // Record type
            String stringValue = value.type().name();

            List<Field> fields = ((ObjectReferenceImpl) value).referenceType().allFields();

            Optional<Field> valueField = fields.stream().filter(field ->
                    field.typeName().equals("java.util.HashMap$Node[]")).findFirst();

            if (!valueField.isPresent()) {
                dapVariable.setValue(stringValue);
                return dapVariable;
            }
            Value jsonValue = ((ObjectReferenceImpl) value).getValue(valueField.get());

            Map<String, Value> values = new HashMap<>();
            ((ArrayReference) jsonValue).getValues().stream().filter(Objects::nonNull).forEach(jsonMap -> {
                List<Field> jsonValueFields = ((ObjectReferenceImpl) jsonMap).referenceType().visibleFields();


                Optional<Field> jsonKeyField = jsonValueFields.stream().filter(field ->
                        field.name().equals("key")).findFirst();


                Optional<Field> jsonValueField = jsonValueFields.stream().filter(field ->
                        field.name().equals("value")).findFirst();

                if (jsonKeyField.isPresent() && jsonValueField.isPresent()) {
                    Value jsonKey = ((ObjectReferenceImpl) jsonMap).getValue(jsonKeyField.get());
                    Value jsonValue1 = ((ObjectReferenceImpl) jsonMap).getValue(jsonValueField.get());
                    values.put(jsonKey.toString(), jsonValue1);
                }
            });

            long variableReference = (long) nextVarReference.getAndIncrement();
            childVariables.put(variableReference, values);
            dapVariable.setVariablesReference(variableReference);
            stringValue = stringValue.replace("$value$", "");
            dapVariable.setType(stringValue);
            dapVariable.setValue(stringValue);
            return dapVariable;
        } else if ("org.ballerinalang.jvm.types.BObjectType".equalsIgnoreCase(varType)) {
            Value typeName = ((ObjectReferenceImpl) value)
                    .getValue(((ObjectReferenceImpl) value).referenceType().fieldByName("typeName"));
            dapVariable.setType(varType);
            String stringValue = typeName.toString();
            dapVariable.setValue(stringValue);
            return dapVariable;
        } else {
            dapVariable.setType(varType);
            String stringValue = value.toString();
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
        eventBus.resetBreakpoints();
        debuggee.resume();
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
        return CompletableFuture.completedFuture(null);
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
        LOGGER.info(DEBUGGER_TERMINATED);
        sendOutput(DEBUGGER_TERMINATED, STDOUT);
        return CompletableFuture.completedFuture(null);
    }

    public void connect(IDebugProtocolClient client) {
        this.client = client;
    }

}
