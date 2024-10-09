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

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.identifier.Utils;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.debugadapter.breakpoint.BalBreakpoint;
import org.ballerinalang.debugadapter.completion.CompletionGenerator;
import org.ballerinalang.debugadapter.completion.context.CompletionContext;
import org.ballerinalang.debugadapter.config.ClientAttachConfigHolder;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.ballerinalang.debugadapter.config.ClientConfigurationException;
import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;
import org.ballerinalang.debugadapter.evaluation.DebugExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.ballerinalang.debugadapter.jdi.VirtualMachineProxyImpl;
import org.ballerinalang.debugadapter.runner.BPackageRunner;
import org.ballerinalang.debugadapter.runner.BProgramRunner;
import org.ballerinalang.debugadapter.runner.BSingleFileRunner;
import org.ballerinalang.debugadapter.utils.PackageUtils;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BSimpleVariable;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.NamedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.ballerinalang.debugadapter.variable.VariableUtils;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.CompletionItem;
import org.eclipse.lsp4j.debug.CompletionsArguments;
import org.eclipse.lsp4j.debug.CompletionsResponse;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.PauseArguments;
import org.eclipse.lsp4j.debug.RunInTerminalRequestArguments;
import org.eclipse.lsp4j.debug.RunInTerminalResponse;
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
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.eclipse.lsp4j.debug.TerminateArguments;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.GenericEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ballerinalang.debugadapter.DebugExecutionManager.LOCAL_HOST;
import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.getInjectedExpressionNode;
import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.getResolverNode;
import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.getTriggerCharacters;
import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.getVisibleSymbolCompletions;
import static org.ballerinalang.debugadapter.completion.util.CompletionUtil.triggerCharactersFound;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.GENERATED_VAR_PREFIX;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getQualifiedClassName;

/**
 * JBallerina debug server implementation.
 */
public class JBallerinaDebugServer implements IDebugProtocolServer {

    private IDebugProtocolClient client;
    private ClientConfigHolder clientConfigHolder;
    private DebugExecutionManager executionManager;
    private JDIEventProcessor eventProcessor;
    private DebugExpressionEvaluator evaluator;
    private final ExecutionContext context;
    private ThreadReferenceProxyImpl activeThread;
    private SuspendedContext suspendedContext;
    private DebugOutputLogger outputLogger;

    private final AtomicInteger nextVarReference = new AtomicInteger();
    private final Map<Integer, StackFrameProxyImpl> stackFramesMap = new ConcurrentHashMap<>();
    private final Map<Long, StackFrame[]> loadedThreadFrames = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> variableToStackFrameMap = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> scopeIdToFrameIdMap = new ConcurrentHashMap<>();

    private final Map<Integer, BCompoundVariable> loadedCompoundVariables = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);
    private static final String SCOPE_NAME_LOCAL = "Local";
    private static final String SCOPE_NAME_GLOBAL = "Global";
    private static final String VALUE_UNKNOWN = "unknown";
    private static final String EVAL_ARGS_CONTEXT_VARIABLES = "variables";
    private static final String COMPILATION_ERROR_MESSAGE = "error: compilation contains errors";
    private static final String TERMINAL_TITLE = "Ballerina Debug Terminal";
    private static final String RUN_IN_TERMINAL_REQUEST = "runInTerminal";

    public JBallerinaDebugServer() {
        context = new ExecutionContext(this);
    }

    public ExecutionContext getContext() {
        return context;
    }

    ClientConfigHolder getClientConfigHolder() {
        return clientConfigHolder;
    }

    public DebugOutputLogger getOutputLogger() {
        return outputLogger;
    }

    @Override
    public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
        Capabilities capabilities = new Capabilities();
        // supported capabilities
        capabilities.setSupportsConfigurationDoneRequest(true);
        capabilities.setSupportsTerminateRequest(true);
        capabilities.setSupportTerminateDebuggee(true);
        capabilities.setSupportsConditionalBreakpoints(true);
        capabilities.setSupportsLogPoints(true);
        capabilities.setSupportsCompletionsRequest(true);
        capabilities.setCompletionTriggerCharacters(getTriggerCharacters().toArray(String[]::new));
        // Todo - Implement
        capabilities.setSupportsRestartRequest(false);
        // unsupported capabilities
        capabilities.setSupportsHitConditionalBreakpoints(false);
        capabilities.setSupportsModulesRequest(false);
        capabilities.setSupportsStepBack(false);
        capabilities.setSupportsTerminateThreadsRequest(false);
        capabilities.setSupportsFunctionBreakpoints(false);
        capabilities.setSupportsExceptionOptions(false);
        capabilities.setSupportsExceptionFilterOptions(false);
        capabilities.setSupportsExceptionInfoRequest(false);

        context.setClient(client);
        context.setSupportsRunInTerminalRequest(args.getSupportsRunInTerminalRequest() != null &&
                args.getSupportsRunInTerminalRequest());
        eventProcessor = new JDIEventProcessor(context);
        client.initialized();
        this.outputLogger = new DebugOutputLogger(client);
        return CompletableFuture.completedFuture(capabilities);
    }

    @Override
    public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
        return CompletableFuture.supplyAsync(() -> {
            BalBreakpoint[] balBreakpoints = Arrays.stream(args.getBreakpoints())
                    .map((SourceBreakpoint sourceBreakpoint) -> toBreakpoint(sourceBreakpoint, args.getSource()))
                    .toArray(BalBreakpoint[]::new);

            LinkedHashMap<Integer, BalBreakpoint> breakpointsMap = new LinkedHashMap<>();
            for (BalBreakpoint bp : balBreakpoints) {
                breakpointsMap.put(bp.getLine(), bp);
            }

            SetBreakpointsResponse breakpointsResponse = new SetBreakpointsResponse();
            String sourcePathUri = args.getSource().getPath();
            Optional<String> qualifiedClassName = getQualifiedClassName(context, sourcePathUri);
            qualifiedClassName.ifPresent(className -> {
                eventProcessor.enableBreakpoints(className, breakpointsMap);
                BreakpointProcessor breakpointProcessor = eventProcessor.getBreakpointProcessor();
                Breakpoint[] breakpoints = breakpointProcessor.getUserBreakpoints().get(qualifiedClassName.get())
                        .values().stream()
                        .map(BalBreakpoint::getAsDAPBreakpoint)
                        .toArray(Breakpoint[]::new);
                breakpointsResponse.setBreakpoints(breakpoints);
            });
            return breakpointsResponse;
        });
    }

    @Override
    public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> launch(Map<String, Object> args) {
        try {
            clearState();
            context.setDebugMode(ExecutionContext.DebugMode.LAUNCH);
            clientConfigHolder = new ClientLaunchConfigHolder(args);
            Project sourceProject = context.getProjectCache().getProject(Path.of(clientConfigHolder.getSourcePath()));
            context.setSourceProject(sourceProject);
            String sourceProjectRoot = context.getSourceProjectRoot();
            BProgramRunner programRunner = context.getSourceProject() instanceof SingleFileProject ?
                    new BSingleFileRunner((ClientLaunchConfigHolder) clientConfigHolder, sourceProjectRoot) :
                    new BPackageRunner((ClientLaunchConfigHolder) clientConfigHolder, sourceProjectRoot);

            if (context.getSupportsRunInTerminalRequest() && clientConfigHolder.getRunInTerminalKind() != null) {
                launchInTerminal(programRunner);
            } else {
                context.setLaunchedProcess(programRunner.start());
                startListeningToProgramOutput();
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            outputLogger.sendErrorOutput("Failed to launch the ballerina program due to: " + e);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> attach(Map<String, Object> args) {
        try {
            clearState();
            context.setDebugMode(ExecutionContext.DebugMode.ATTACH);
            clientConfigHolder = new ClientAttachConfigHolder(args);
            Project sourceProject = context.getProjectCache().getProject(Path.of(clientConfigHolder.getSourcePath()));
            context.setSourceProject(sourceProject);
            ClientAttachConfigHolder configHolder = (ClientAttachConfigHolder) clientConfigHolder;

            String hostName = configHolder.getHostName().orElse("");
            int portName = configHolder.getDebuggePort();
            attachToRemoteVM(hostName, portName);
        } catch (Exception e) {
            String host = ((ClientAttachConfigHolder) clientConfigHolder).getHostName().orElse(LOCAL_HOST);
            String portName;
            try {
                portName = Integer.toString(clientConfigHolder.getDebuggePort());
            } catch (ClientConfigurationException clientConfigurationException) {
                portName = VALUE_UNKNOWN;
            }
            LOGGER.error(e.getMessage());
            outputLogger.sendErrorOutput(String.format("Failed to attach to the target VM, address: '%s:%s'.",
                    host, portName));
            terminateDebugServer(context.getDebuggeeVM() != null, false);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<ThreadsResponse> threads() {
        ThreadsResponse threadsResponse = new ThreadsResponse();
        if (eventProcessor == null) {
            return CompletableFuture.completedFuture(threadsResponse);
        }
        Map<Integer, ThreadReferenceProxyImpl> threadsMap = getActiveStrandThreads();
        if (threadsMap == null) {
            return CompletableFuture.completedFuture(threadsResponse);
        }
        Thread[] threads = new Thread[threadsMap.size()];
        threadsMap.values().stream().map(this::toDapThread).toList().toArray(threads);
        threadsResponse.setThreads(threads);
        return CompletableFuture.completedFuture(threadsResponse);
    }

    @Override
    public CompletableFuture<Void> pause(PauseArguments args) {
        VirtualMachineProxyImpl debuggeeVM = context.getDebuggeeVM();
        // Checks if the program VM is a read-only VM. (If a method which would modify the state of the VM is called
        // on a read-only VM, a `VMCannotBeModifiedException` will be thrown.
        if (!debuggeeVM.canBeModified()) {
            getOutputLogger().sendConsoleOutput("Failed to suspend the remote VM due to: pause requests are not " +
                    "supported on read-only VMs");
            return CompletableFuture.completedFuture(null);
        }
        // Suspends all the threads and notify the `stopped` event to client.
        debuggeeVM.suspend();
        eventProcessor.notifyStopEvent(StoppedEventArgumentsReason.PAUSE, args.getThreadId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
        StackTraceResponse stackTraceResponse = new StackTraceResponse();
        try {
            activeThread = getAllThreads().get(args.getThreadId());
            if (loadedThreadFrames.containsKey(activeThread.uniqueID())) {
                stackTraceResponse.setStackFrames(loadedThreadFrames.get(activeThread.uniqueID()));
            } else {
                StackFrame[] validFrames = activeThread.frames().stream()
                        .map(this::toDapStackFrame)
                        .filter(JBallerinaDebugServer::isValidFrame)
                        .toArray(StackFrame[]::new);
                stackTraceResponse.setStackFrames(validFrames);
                loadedThreadFrames.put(activeThread.uniqueID(), validFrames);
            }
            return CompletableFuture.completedFuture(stackTraceResponse);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            stackTraceResponse.setStackFrames(new StackFrame[0]);
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
        try {
            // 1. If frameId < 0, returns global variables.
            // 2. If frameId >= 0, returns local variables.
            // 3. If frameId is NULL, returns child variables.
            Integer frameId = scopeIdToFrameIdMap.get(args.getVariablesReference());
            if (frameId != null && frameId < 0) {
                StackFrameProxyImpl stackFrame = stackFramesMap.get(-frameId);
                if (stackFrame == null) {
                    variablesResponse.setVariables(new Variable[0]);
                    return CompletableFuture.completedFuture(variablesResponse);
                }

                suspendedContext = new SuspendedContext(context, activeThread, stackFrame);
                variablesResponse.setVariables(computeGlobalVariables(suspendedContext, args.getVariablesReference()));
            } else if (frameId != null) {
                StackFrameProxyImpl stackFrame = stackFramesMap.get(frameId);
                if (stackFrame == null) {
                    variablesResponse.setVariables(new Variable[0]);
                    return CompletableFuture.completedFuture(variablesResponse);
                }
                suspendedContext = new SuspendedContext(context, activeThread, stackFrame);
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
        context.getDebuggeeVM().resume();
        ContinueResponse continueResponse = new ContinueResponse();
        continueResponse.setAllThreadsContinued(true);
        return CompletableFuture.completedFuture(continueResponse);
    }

    @Override
    public CompletableFuture<Void> next(NextArguments args) {
        prepareFor(DebugInstruction.STEP_OVER);
        eventProcessor.sendStepRequest(args.getThreadId(), StepRequest.STEP_OVER);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepIn(StepInArguments args) {
        prepareFor(DebugInstruction.STEP_IN);
        eventProcessor.sendStepRequest(args.getThreadId(), StepRequest.STEP_INTO);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> stepOut(StepOutArguments args) {
        stepOut(args.getThreadId());
        return CompletableFuture.completedFuture(null);
    }

    void stepOut(int threadId) {
        prepareFor(DebugInstruction.STEP_OUT);
        eventProcessor.sendStepRequest(threadId, StepRequest.STEP_OUT);
    }

    @Override
    public CompletableFuture<Void> setExceptionBreakpoints(SetExceptionBreakpointsArguments args) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<EvaluateResponse> evaluate(EvaluateArguments args) {
        EvaluateResponse response = new EvaluateResponse();
        // If the execution manager is not active, it implies that the debug server is still not connected to the
        // remote VM and therefore the request should be rejected immediately.
        if (executionManager == null || !executionManager.isActive()) {
            context.getOutputLogger().sendErrorOutput(EvaluationExceptionKind.PREFIX + "Debug server is not " +
                    "connected to any program VM.");
            return CompletableFuture.completedFuture(response);
        }
        // If the frame ID is missing in the client args, it implies that remote program is still running and therefore
        // the request should be rejected immediately.
        if (args.getFrameId() == null) {
            context.getOutputLogger().sendErrorOutput(EvaluationExceptionKind.PREFIX + "Remote VM is not suspended " +
                    "and still in running state.");
            return CompletableFuture.completedFuture(response);
        }

        // Evaluate arguments context becomes `variables` when we do a `Copy Value` from VS Code, and
        // evaluate arguments context becomes `repl` when we evaluate expressions from VS Code.
        // If evaluate arguments context is equal to `variables`, then respond with expression as it is without
        // evaluation process.
        if (args.getContext() != null && args.getContext().equals(EVAL_ARGS_CONTEXT_VARIABLES)) {
            response.setResult(args.getExpression());
            return CompletableFuture.completedFuture(response);
        }
        try {
            StackFrameProxyImpl frame = stackFramesMap.get(args.getFrameId());
            SuspendedContext suspendedContext = new SuspendedContext(context, activeThread, frame);
            EvaluationContext evaluationContext = new EvaluationContext(suspendedContext);
            evaluator = Objects.requireNonNullElse(evaluator, new DebugExpressionEvaluator(evaluationContext));
            evaluator.setExpression(args.getExpression());
            BVariable variable = evaluator.evaluate().getBVariable();

            if (variable == null) {
                return CompletableFuture.completedFuture(response);
            } else if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0);
            } else if (variable instanceof BCompoundVariable bCompoundVariable) {
                int variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedCompoundVariables.put(variableReference, bCompoundVariable);
                updateVariableToStackFrameMap(args.getFrameId(), variableReference);
            }
            Variable dapVariable = variable.getDapVariable();
            response.setResult(dapVariable.getValue());
            response.setType(dapVariable.getType());
            response.setIndexedVariables(dapVariable.getIndexedVariables());
            response.setNamedVariables(dapVariable.getNamedVariables());
            response.setVariablesReference(dapVariable.getVariablesReference());
            return CompletableFuture.completedFuture(response);
        } catch (EvaluationException e) {
            context.getOutputLogger().sendErrorOutput(e.getMessage());
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            context.getOutputLogger().sendErrorOutput(EvaluationExceptionKind.PREFIX + "internal error");
            return CompletableFuture.completedFuture(response);
        }
    }

    @Override
    public CompletableFuture<CompletionsResponse> completions(CompletionsArguments args) {
        return CompletableFuture.supplyAsync(() -> {
            CompletionsResponse completionsResponse = new CompletionsResponse();

            if (suspendedContext == null) {
                return completionsResponse;
            }
            CompletionContext completionContext = new CompletionContext(suspendedContext);

            // If the debug console expression doesn't have any trigger characters,
            // get the visible symbols at the breakpoint line using semantic API.
            if (!triggerCharactersFound(args.getText())) {
                CompletionItem[] visibleSymbolCompletions = getVisibleSymbolCompletions(completionContext);
                completionsResponse.setTargets(visibleSymbolCompletions);
                return completionsResponse;
            }

            // If the debug console expression has any trigger characters,
            // identify the node instance at the breakpoint appropriately and inject the expression accordingly.
            try {
                NonTerminalNode injectedExpressionNode = getInjectedExpressionNode(completionContext, args,
                        clientConfigHolder.getSourcePath(), suspendedContext.getLineNumber());
                completionContext.setNodeAtCursor(injectedExpressionNode);
                Optional<Node> resolverNode = getResolverNode(injectedExpressionNode);

                if (resolverNode.isEmpty()) {
                    return completionsResponse;
                }

                CompletionItem[] completionItems;
                switch (resolverNode.get().kind()) {
                    case FIELD_ACCESS:
                        completionItems = CompletionGenerator
                                .getFieldAccessCompletions(completionContext, resolverNode.get());
                        completionsResponse.setTargets(completionItems);
                        break;
                    case REMOTE_METHOD_CALL_ACTION:
                        completionItems = CompletionGenerator
                                .getRemoteMethodCallActionCompletions(completionContext, resolverNode.get());
                        completionsResponse.setTargets(completionItems);
                        break;
                    case ASYNC_SEND_ACTION:
                        completionItems = CompletionGenerator
                                .getAsyncSendActionCompletions(completionContext, resolverNode.get());
                        completionsResponse.setTargets(completionItems);
                        break;
                    default:
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
            return completionsResponse;
        });
    }

    private BalBreakpoint toBreakpoint(SourceBreakpoint sourceBreakpoint, Source source) {
        BalBreakpoint breakpoint = new BalBreakpoint(source, sourceBreakpoint.getLine());
        breakpoint.setCondition(sourceBreakpoint.getCondition());
        breakpoint.setLogMessage(sourceBreakpoint.getLogMessage());
        return breakpoint;
    }

    Thread toDapThread(ThreadReferenceProxyImpl threadReference) {
        Thread thread = new Thread();
        thread.setId((int) threadReference.uniqueID());
        thread.setName(threadReference.name());
        return thread;
    }

    @Override
    public CompletableFuture<Void> disconnect(DisconnectArguments args) {
        context.setTerminateRequestReceived(true);
        boolean terminateDebuggee = Objects.requireNonNullElse(args.getTerminateDebuggee(), true);
        terminateDebugServer(terminateDebuggee, true);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> terminate(TerminateArguments args) {
        context.setTerminateRequestReceived(true);
        terminateDebugServer(true, true);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<RunInTerminalResponse> runInTerminal(RunInTerminalRequestArguments args) {
        Endpoint endPoint = new GenericEndpoint(context.getClient());
        endPoint.request(RUN_IN_TERMINAL_REQUEST, args).thenApply((response) -> {
            int tryCounter = 0;

            // attach to target VM
            while (context.getDebuggeeVM() == null && tryCounter < 10) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    attachToRemoteVM("", clientConfigHolder.getDebuggePort());
                } catch (IOException ignored) {
                    tryCounter++;
                } catch (IllegalConnectorArgumentsException | ClientConfigurationException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // if the VM is not attached within 60 seconds
            if (context.getDebuggeeVM() == null) {
                // shut down debug server
                outputLogger.sendErrorOutput("Failed to attach to the target VM");
                terminateDebugServer(false, true);

                // shut down client terminal
                int shellProcessId = ((RunInTerminalResponse) response).getShellProcessId();
                ProcessHandle.of(shellProcessId).ifPresent(ProcessHandle::destroyForcibly);
            } else {
                outputLogger.sendDebugServerOutput("Attached to target VM");
            }
            return CompletableFuture.completedFuture(null);
        });

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Launches the debug process in a separate terminal by setting the required request params and calling the request.
     *
     * @param programRunner - the instantiated Ballerina program runner for the source
     */
    private void launchInTerminal(BProgramRunner programRunner) throws ClientConfigurationException {
        String sourceProjectRoot = context.getSourceProjectRoot();
        RunInTerminalRequestArguments runInTerminalRequestArguments = new RunInTerminalRequestArguments();

        runInTerminalRequestArguments.setKind(clientConfigHolder.getRunInTerminalKind());
        runInTerminalRequestArguments.setTitle(TERMINAL_TITLE);
        runInTerminalRequestArguments.setCwd(sourceProjectRoot);

        String[] command = new String[programRunner.getBallerinaCommand(sourceProjectRoot).size()];
        programRunner.getBallerinaCommand(sourceProjectRoot).toArray(command);
        runInTerminalRequestArguments.setArgs(command);

        outputLogger.sendConsoleOutput("Launching debugger in terminal");
        context.getAdapter().runInTerminal(runInTerminalRequestArguments);
    }

    /**
     * Terminates the debug server.
     *
     * @param terminateDebuggee indicates whether the remote VM should also be terminated
     * @param logsEnabled       indicates whether the debug server logs should be sent to the client
     */
    void terminateDebugServer(boolean terminateDebuggee, boolean logsEnabled) {
        // Destroys launched process, if presents.
        if (context.getLaunchedProcess().isPresent() && context.getLaunchedProcess().get().isAlive()) {
            killProcessWithDescendants(context.getLaunchedProcess().get());
        }
        // Destroys remote VM process, if `terminteDebuggee' flag is set.
        if (terminateDebuggee && context.getDebuggeeVM() != null) {
            int exitCode = 0;
            if (context.getDebuggeeVM().process() != null) {
                exitCode = killProcessWithDescendants(context.getDebuggeeVM().process());
            }
            try {
                context.getDebuggeeVM().exit(exitCode);
            } catch (Exception ignored) {
                // It is okay to ignore the VM exit Exceptions, in-case the remote debuggee is already terminated.
            }
        }

        // If 'terminationRequestReceived' is false, debug server termination should have been triggered from the
        // JDI event processor, after receiving a 'VMDisconnected'/'VMExited' event.
        if (!context.isTerminateRequestReceived()) {
            ExitedEventArguments exitedEventArguments = new ExitedEventArguments();
            exitedEventArguments.setExitCode(0);
            context.getClient().exited(exitedEventArguments);
        }

        // Notifies user.
        if (executionManager != null && logsEnabled) {
            String address = (executionManager.getHost().isPresent() && executionManager.getPort().isPresent()) ?
                    executionManager.getHost().get() + ":" + executionManager.getPort().get() : VALUE_UNKNOWN;
            outputLogger.sendDebugServerOutput(String.format(System.lineSeparator() + "Disconnected from the target " +
                    "VM, address: '%s'", address));
        }

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
        try {
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
            parent.destroyForcibly();
            parent.waitFor();
            return parent.exitValue();
        } catch (InterruptedException ignored) {
            return 0;
        } catch (Exception e) {
            return 1;
        }
    }

    public void connect(IDebugProtocolClient client) {
        this.client = client;
    }

    private synchronized void updateVariableToStackFrameMap(int parent, int child) {
        if (!variableToStackFrameMap.containsKey(parent)) {
            variableToStackFrameMap.put(child, parent);
            return;
        }

        Integer parentRef;
        do {
            parentRef = variableToStackFrameMap.get(parent);
        } while (variableToStackFrameMap.containsKey(parentRef));
        variableToStackFrameMap.put(child, parentRef);
    }

    public StackFrame toDapStackFrame(StackFrameProxyImpl stackFrameProxy) {
        try {
            if (!isBalStackFrame(stackFrameProxy.getStackFrame())) {
                return null;
            }

            int referenceId = nextVarReference.getAndIncrement();
            stackFramesMap.put(referenceId, stackFrameProxy);
            BallerinaStackFrame balStackFrame = new BallerinaStackFrame(context, referenceId, stackFrameProxy);
            return balStackFrame.getAsDAPStackFrame().orElse(null);
        } catch (JdiProxyException e) {
            return null;
        }
    }

    private Variable[] computeGlobalVariables(SuspendedContext context, int stackFrameReference) {
        String classQName = PackageUtils.getQualifiedClassName(context, INIT_CLASS_NAME);
        List<ReferenceType> cls = context.getAttachedVm().classesByName(classQName);
        if (cls.size() != 1) {
            return new Variable[0];
        }
        ArrayList<Variable> globalVars = new ArrayList<>();
        ReferenceType initClassReference = cls.get(0);
        for (Field field : initClassReference.allFields()) {
            String fieldName = Utils.decodeIdentifier(field.name());
            if (!field.isPublic() || !field.isStatic() || fieldName.startsWith(GENERATED_VAR_PREFIX)) {
                continue;
            }
            Value fieldValue = initClassReference.getValue(field);
            BVariable variable = VariableFactory.getVariable(context, fieldName, fieldValue);
            if (variable == null) {
                continue;
            }
            if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0);
            } else if (variable instanceof BCompoundVariable bCompoundVariable) {
                int variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedCompoundVariables.put(variableReference, bCompoundVariable);
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
        for (LocalVariableProxyImpl var : localVariableProxies) {
            String name = var.name();
            Value value = stackFrame.getValue(var);
            // Since the ballerina variables used inside lambda functions are converted into maps during the
            // ballerina runtime code generation, such local variables needs to be extracted in a separate manner.
            if (VariableUtils.isLambdaParamMap(var)) {
                variables.addAll(fetchLocalVariablesFromMap(args, stackFrame, var));
            } else {
                Variable dapVariable = getAsDapVariable(name, value, args.getVariablesReference());
                if (dapVariable != null) {
                    variables.add(dapVariable);
                }
            }
        }
        return variables.toArray(new Variable[0]);
    }

    /**
     * Returns the list of local variables extracted from the given variable map, which contains local variables used
     * within lambda functions.
     *
     * @param args              variable args
     * @param stackFrame        parent stack frame instance
     * @param lambdaParamMapVar map variable instance
     * @return list of local variables extracted from the given variable map
     */
    private List<Variable> fetchLocalVariablesFromMap(VariablesArguments args, StackFrameProxyImpl stackFrame,
                                                      LocalVariableProxyImpl lambdaParamMapVar) {
        try {
            Value value = stackFrame.getValue(lambdaParamMapVar);
            Variable dapVariable = getAsDapVariable("lambdaArgMap", value, args.getVariablesReference());
            if (dapVariable == null || !dapVariable.getType().equals(BVariableType.MAP.getString())) {
                return new ArrayList<>();
            }
            VariablesArguments childVarRequestArgs = new VariablesArguments();
            childVarRequestArgs.setVariablesReference(dapVariable.getVariablesReference());
            Variable[] childVariables = computeChildVariables(childVarRequestArgs);
            return Arrays.asList(childVariables);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Coverts a given ballerina runtime value instance into a debugger adapter protocol supported variable instance.
     *
     * @param name          variable name
     * @param value         runtime value of the variable
     * @param stackFrameRef reference ID of the parent stack frame
     */
    private Variable getAsDapVariable(String name, Value value, Integer stackFrameRef) {
        BVariable variable = VariableFactory.getVariable(suspendedContext, name, value);
        if (variable == null) {
            return null;
        } else if (variable instanceof BSimpleVariable) {
            variable.getDapVariable().setVariablesReference(0);
        } else if (variable instanceof BCompoundVariable bCompoundVariable) {
            int variableReference = nextVarReference.getAndIncrement();
            variable.getDapVariable().setVariablesReference(variableReference);
            loadedCompoundVariables.put(variableReference, bCompoundVariable);
            updateVariableToStackFrameMap(stackFrameRef, variableReference);
        }
        return variable.getDapVariable();
    }

    private Variable[] computeChildVariables(VariablesArguments args) {
        BCompoundVariable parentVar = loadedCompoundVariables.get(args.getVariablesReference());
        Integer stackFrameId = variableToStackFrameMap.get(args.getVariablesReference());
        if (stackFrameId == null) {
            return new Variable[0];
        }

        if (parentVar instanceof IndexedCompoundVariable indexedCompoundVariable) {
            // Handles indexed variables.
            int startIndex = (args.getStart() != null) ? args.getStart() : 0;
            int count = (args.getCount() != null) ? args.getCount() : 0;

            Either<Map<String, Value>, List<Value>> childVars = indexedCompoundVariable
                    .getIndexedChildVariables(startIndex, count);
            if (childVars.isLeft()) {
                // Handles map-type indexed variables.
                return createVariableArrayFrom(args, childVars.getLeft());
            } else if (childVars.isRight()) {
                // Handles list-type indexed variables.
                return createVariableArrayFrom(args, childVars.getRight());
            }
            return new Variable[0];
        } else if (parentVar instanceof NamedCompoundVariable namedCompoundVariable) {
            // Handles named variables.
            Map<String, Value> childVars = namedCompoundVariable.getNamedChildVariables();
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
                variable.getDapVariable().setVariablesReference(0);
            } else if (variable instanceof BCompoundVariable bCompoundVariable) {
                int variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedCompoundVariables.put(variableReference, bCompoundVariable);
                updateVariableToStackFrameMap(args.getVariablesReference(), variableReference);
            }
            return variable.getDapVariable();
        }).filter(Objects::nonNull).toArray(Variable[]::new);
    }

    private Variable[] createVariableArrayFrom(VariablesArguments args, List<Value> varMap) {
        int startIndex = (args.getStart() != null) ? args.getStart() : 0;
        AtomicInteger index = new AtomicInteger(startIndex);

        return varMap.stream().map(value -> {
            String name = String.format("[%d]", index.getAndIncrement());
            BVariable variable = VariableFactory.getVariable(suspendedContext, name, value);
            if (variable == null) {
                return null;
            } else if (variable instanceof BSimpleVariable) {
                variable.getDapVariable().setVariablesReference(0);
            } else if (variable instanceof BCompoundVariable bCompoundVariable) {
                int variableReference = nextVarReference.getAndIncrement();
                variable.getDapVariable().setVariablesReference(variableReference);
                loadedCompoundVariables.put(variableReference, bCompoundVariable);
                updateVariableToStackFrameMap(args.getVariablesReference(), variableReference);
            }
            return variable.getDapVariable();
        }).filter(Objects::nonNull).toArray(Variable[]::new);
    }

    /**
     * Returns a map of all currently running threads in the remote VM, against their unique ID.
     * <p>
     * Thread objects that have not yet been started (see {@link java.lang.Thread#start Thread.start()})
     * and thread objects that have completed their execution are not included in the returned list.
     */
    Map<Integer, ThreadReferenceProxyImpl> getAllThreads() {
        if (context.getDebuggeeVM() == null) {
            return null;
        }
        Collection<ThreadReference> threadReferences = context.getDebuggeeVM().getVirtualMachine().allThreads();
        Map<Integer, ThreadReferenceProxyImpl> threadsMap = new HashMap<>();

        // Filter thread references which are suspended, whose thread status is running, and which represents an active
        // ballerina strand.
        for (ThreadReference threadReference : threadReferences) {
            threadsMap.put((int) threadReference.uniqueID(), new ThreadReferenceProxyImpl(context.getDebuggeeVM(),
                    threadReference));
        }
        return threadsMap;
    }

    /**
     * Returns a map of thread instances which correspond to an active ballerina strand, against their unique ID.
     */
    Map<Integer, ThreadReferenceProxyImpl> getActiveStrandThreads() {
        Map<Integer, ThreadReferenceProxyImpl> allThreads = getAllThreads();
        if (allThreads == null) {
            return null;
        }

        Map<Integer, ThreadReferenceProxyImpl> balStrandThreads = new HashMap<>();
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
                balStrandThreads.put(id, new ThreadReferenceProxyImpl(context.getDebuggeeVM(), threadReference));
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
    static boolean isBalStackFrame(com.sun.jdi.StackFrame frame) {
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
    static boolean isValidFrame(StackFrame stackFrame) {
        return stackFrame != null && stackFrame.getSource() != null && stackFrame.getLine() > 0
                && !isCompilerGeneratedFrame(stackFrame);
    }

    /**
     * Validates whether the provided stack frame is a compiler generated one during the codegen phase.
     *
     * @param stackFrame stack frame instance
     * @return true if the provided stack frame is a compiler generated one during the codegen phase
     */
    private static boolean isCompilerGeneratedFrame(StackFrame stackFrame) {
        String frameName = stackFrame.getName();
        return frameName != null && frameName.startsWith("$") && frameName.endsWith("$");
    }

    /**
     * Asynchronously listens to remote debuggee stdout + error streams and redirects the output to the client debug
     * console.
     */
    private void startListeningToProgramOutput() {
        CompletableFuture.runAsync(() -> {
            if (context.getLaunchedProcess().isEmpty()) {
                return;
            }

            try (BufferedReader errorStream = context.getErrorStream()) {
                String line;
                while ((line = errorStream.readLine()) != null) {
                    // Todo - Redirect back to error category, once the ballerina compiler output is changed to use
                    //  the STDOUT stream.
                    outputLogger.sendConsoleOutput(line);
                    if (context.getDebuggeeVM() == null && line.contains(COMPILATION_ERROR_MESSAGE)) {
                        terminateDebugServer(false, true);
                    }
                }
            } catch (IOException ignored) {
            }
        });

        CompletableFuture.runAsync(() -> {
            if (context.getLaunchedProcess().isEmpty()) {
                return;
            }

            try (BufferedReader inputStream = context.getInputStream()) {
                String line;
                outputLogger.sendDebugServerOutput("Waiting for debug process to start..." + System.lineSeparator()
                        + System.lineSeparator());
                while ((line = inputStream.readLine()) != null) {
                    if (line.contains("Listening for transport dt_socket")) {
                        attachToRemoteVM("", clientConfigHolder.getDebuggePort());
                    } else if (context.getDebuggeeVM() == null && line.contains(COMPILATION_ERROR_MESSAGE)) {
                        terminateDebugServer(false, true);
                    }
                    outputLogger.sendProgramOutput(line);
                }
            } catch (Exception e) {
                String host = clientConfigHolder instanceof ClientAttachConfigHolder clientAttachConfigHolder ?
                        clientAttachConfigHolder.getHostName().orElse(LOCAL_HOST) : LOCAL_HOST;
                String portName;
                try {
                    portName = Integer.toString(clientConfigHolder.getDebuggePort());
                } catch (ClientConfigurationException clientConfigurationException) {
                    portName = VALUE_UNKNOWN;
                }
                LOGGER.error(e.getMessage());
                outputLogger.sendDebugServerOutput(String.format("Failed to attach to the target VM, address: '%s:%s'.",
                        host, portName));
                terminateDebugServer(context.getDebuggeeVM() != null, false);
            }
        });
    }

    /**
     * Attach to the remote VM using host address and port.
     *
     * @param hostName host address
     * @param portName host port
     */
    private void attachToRemoteVM(String hostName, int portName) throws IOException,
            IllegalConnectorArgumentsException {
        executionManager = new DebugExecutionManager(this);
        VirtualMachine attachedVm = executionManager.attach(hostName, portName);
        context.setDebuggeeVM(new VirtualMachineProxyImpl(attachedVm));
        EventRequestManager erm = context.getEventManager();
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.enable();
        eventProcessor.startListening();
    }

    /**
     * Clears previous state information and prepares for the given debug instruction type execution.
     */
    private void prepareFor(DebugInstruction instruction) {
        clearState();
        eventProcessor.getBreakpointProcessor().restoreUserBreakpoints(instruction);
        context.setLastInstruction(instruction);
    }

    /**
     * Clears state information.
     */
    private void clearState() {
        suspendedContext = null;
        evaluator = null;
        activeThread = null;
        stackFramesMap.clear();
        loadedCompoundVariables.clear();
        variableToStackFrameMap.clear();
        scopeIdToFrameIdMap.clear();
        loadedThreadFrames.clear();
        nextVarReference.set(1);
    }
}
