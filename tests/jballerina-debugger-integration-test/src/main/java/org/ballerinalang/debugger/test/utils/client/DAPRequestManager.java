/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.debugger.test.utils.client;

import org.eclipse.lsp4j.debug.BreakpointEventArguments;
import org.eclipse.lsp4j.debug.CapabilitiesEventArguments;
import org.eclipse.lsp4j.debug.CompletionsArguments;
import org.eclipse.lsp4j.debug.CompletionsResponse;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.ExitedEventArguments;
import org.eclipse.lsp4j.debug.LoadedSourceEventArguments;
import org.eclipse.lsp4j.debug.ModuleEventArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.PauseArguments;
import org.eclipse.lsp4j.debug.ProcessEventArguments;
import org.eclipse.lsp4j.debug.RunInTerminalRequestArguments;
import org.eclipse.lsp4j.debug.RunInTerminalRequestArgumentsKind;
import org.eclipse.lsp4j.debug.RunInTerminalResponse;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Debugger Adaptor protocol based client request handler implementation.
 */
public class DAPRequestManager {

    private final DAPClientConnector clientConnector;
    private final IDebugProtocolServer server;
    private boolean didRunInIntegratedTerminal;
    private boolean isProjectBasedTest;

    private static final Logger LOGGER = LoggerFactory.getLogger(DAPRequestManager.class);

    public DAPRequestManager(DAPClientConnector clientConnector, IDebugProtocolServer server) {
        this.clientConnector = clientConnector;
        this.server = server;
    }

    // ------------------------------- Client to Server --------------------------------------------//

    public SetBreakpointsResponse setBreakpoints(SetBreakpointsArguments args) throws Exception {
        return setBreakpoints(args, DefaultTimeouts.SET_BREAKPOINTS.getValue());
    }

    public SetBreakpointsResponse setBreakpoints(SetBreakpointsArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<SetBreakpointsResponse> resp = server.setBreakpoints(args);
            return resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void configurationDone(ConfigurationDoneArguments args) throws Exception {
        configurationDone(args, DefaultTimeouts.CONFIG_DONE.getValue());
    }

    public void configurationDone(ConfigurationDoneArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.configurationDone(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void attach(Map<String, Object> args) throws Exception {
        attach(args, DefaultTimeouts.ATTACH.getValue());
    }

    public void attach(Map<String, Object> args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.attach(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void launch(Map<String, Object> args) throws Exception {
        launch(args, DefaultTimeouts.LAUNCH.getValue());
    }

    public void launch(Map<String, Object> args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.launch(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public ThreadsResponse threads() throws Exception {
        return threads(DefaultTimeouts.THREADS.getValue());
    }

    public ThreadsResponse threads(long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<ThreadsResponse> resp = server.threads();
            return resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public CompletionsResponse completions(CompletionsArguments args) throws Exception {
        return completions(args, DefaultTimeouts.COMPLETIONS.getValue());
    }

    public CompletionsResponse completions(CompletionsArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<CompletionsResponse> resp = server.completions(args);
            return resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public StackTraceResponse stackTrace(StackTraceArguments args) throws Exception {
        return stackTrace(args, DefaultTimeouts.STACK_TRACE.getValue());
    }

    public StackTraceResponse stackTrace(StackTraceArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<StackTraceResponse> resp = server.stackTrace(args);
            return resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public ScopesResponse scopes(ScopesArguments args) throws Exception {
        return scopes(args, DefaultTimeouts.SCOPES.getValue());
    }

    public ScopesResponse scopes(ScopesArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<ScopesResponse> resp = server.scopes(args);
            return resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public VariablesResponse variables(VariablesArguments args) throws Exception {
        return variables(args, DefaultTimeouts.VARIABLES.getValue());
    }

    public VariablesResponse variables(VariablesArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            Instant start = Instant.now();
            CompletableFuture<VariablesResponse> resp = server.variables(args);
            VariablesResponse variablesResponse = resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
            Instant end = Instant.now();
            // Todo - remove after monitoring if the debugger integration tests are failing due to averagely high
            //  response times or, due to sudden spikes (potential concurrency issues?)
            LOGGER.info(String.format("debug variables request took %s ms", Duration.between(start, end).toMillis()));
            return variablesResponse;
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public EvaluateResponse evaluate(EvaluateArguments args) throws Exception {
        return evaluate(args, DefaultTimeouts.EVALUATE.getValue());
    }

    public EvaluateResponse evaluate(EvaluateArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            Instant start = Instant.now();
            CompletableFuture<EvaluateResponse> resp = server.evaluate(args);
            EvaluateResponse evaluateResponse = resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
            Instant end = Instant.now();
            // Todo - remove after monitoring if the debugger integration tests are failing due to averagely high
            //  response times or, due to sudden spikes (potential concurrency issues?)
            LOGGER.info(String.format("evaluation request took %s ms", Duration.between(start, end).toMillis()));
            return evaluateResponse;
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void next(NextArguments args) throws Exception {
        next(args, DefaultTimeouts.STEP_OVER.getValue());
    }

    public void next(NextArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.next(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void stepIn(StepInArguments args) throws Exception {
        stepIn(args, DefaultTimeouts.STEP_IN.getValue());
    }

    public void stepIn(StepInArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.stepIn(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void stepOut(StepOutArguments args) throws Exception {
        stepOut(args, DefaultTimeouts.STEP_OUT.getValue());
    }

    public void stepOut(StepOutArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.stepOut(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public ContinueResponse resume(ContinueArguments args) throws Exception {
        return resume(args, DefaultTimeouts.RESUME.getValue());
    }

    public ContinueResponse resume(ContinueArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<ContinueResponse> resp = server.continue_(args);
            return resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void pause(PauseArguments args) throws Exception {
        pause(args, DefaultTimeouts.PAUSE.getValue());
    }

    public void pause(PauseArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.pause(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void disconnect(DisconnectArguments args) throws Exception {
        disconnect(args, DefaultTimeouts.DISCONNECT.getValue());
    }

    public void disconnect(DisconnectArguments args, long timeoutMillis) throws Exception {
        if (checkStatus()) {
            CompletableFuture<Void> resp = server.disconnect(args);
            resp.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    // -------------------------------- Server to Client --------------------------------------------//

    public void initialized() {
        // Todo
    }

    public void stopped(StoppedEventArguments args) {
        clientConnector.getServerEventHolder().addStoppedEvent(args);
    }

    public void continued(ContinuedEventArguments args) {
        // Todo
    }

    public void exited(ExitedEventArguments args) {
        clientConnector.getServerEventHolder().addExitedEvent(args);
    }

    public void terminated(TerminatedEventArguments args) {
        clientConnector.getServerEventHolder().addTerminatedEvent(args);
    }

    public void thread(ThreadEventArguments args) {
        // Todo
    }

    public void output(OutputEventArguments args) {
        clientConnector.getServerEventHolder().addOutputEvent(args);
    }

    public void breakpoint(BreakpointEventArguments args) {
        clientConnector.getServerEventHolder().addBreakpointEvent(args);
    }

    public void module(ModuleEventArguments args) {
        // Todo
    }

    public void loadedSource(LoadedSourceEventArguments args) {
        // Todo
    }

    public void process(ProcessEventArguments args) {
        // Todo
    }

    public void capabilities(CapabilitiesEventArguments args) {
        // Todo
    }

    private boolean checkStatus() {
        return clientConnector != null && clientConnector.isConnected();
    }

    public CompletableFuture<RunInTerminalResponse> runInTerminal(RunInTerminalRequestArguments args) throws Exception {
        if (checkStatus()) {
            // check whether it is a project based test or single file test, and get the cwd respectively
            String cwd = isProjectBasedTest ? clientConnector.getProjectPath().toString() :
                    clientConnector.getEntryFilePath().toString();

            if (args.getKind() == RunInTerminalRequestArgumentsKind.INTEGRATED && Objects.equals(args.getCwd(), cwd)) {
                this.didRunInIntegratedTerminal = true;
                return CompletableFuture.completedFuture(null);
            } else {
                throw new Exception("RunInTerminal request failed");
            }
        } else {
            throw new IllegalStateException("DAP request manager is not active");
        }
    }

    public void setIsProjectBasedTest(Boolean isProjectBasedTest) {
        this.isProjectBasedTest = isProjectBasedTest;
    }

    public boolean getDidRunInIntegratedTerminal() {
        return this.didRunInIntegratedTerminal;
    }

    private enum DefaultTimeouts {
        SET_BREAKPOINTS(10000),
        CONFIG_DONE(2000),
        ATTACH(5000),
        LAUNCH(10000),
        THREADS(2000),
        STACK_TRACE(7000),
        SCOPES(2000),
        VARIABLES(30000),
        EVALUATE(20000),
        COMPLETIONS(10000),
        STEP_OVER(5000),
        STEP_IN(10000),
        STEP_OUT(5000),
        RESUME(5000),
        PAUSE(5000),
        DISCONNECT(5000);

        private final long value;

        DefaultTimeouts(long value) {
            this.value = value;
        }

        public long getValue() {
            return this.value;
        }
    }
}
