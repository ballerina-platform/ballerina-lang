/*
 * Copyright (c) 2023, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.RunContext;
import org.ballerinalang.langserver.commons.workspace.RunResult;
import org.eclipse.lsp4j.LogTraceParams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * Command executor for running a Ballerina file. Each project at most has a single instance running at a time.
 * See {@link StopExecutor} for stopping a running instance.
 *
 * @since 2201.6.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class RunExecutor implements LSCommandExecutor {

    private static final String RUN_COMMAND = "RUN";

    // commands arg names
    private static final String ARG_PATH = "path";
    private static final String ARG_PROGRAM_ARGS = "programArgs";
    private static final String ARG_ENV = "env";
    private static final String ARG_DEBUG_PORT = "debugPort";

    // output channels
    private static final String ERROR_CHANNEL = "err";
    private static final String OUT_CHANNEL = "out";

    @Override
    public Boolean execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        try {
            RunContext workspaceRunContext = getWorkspaceRunContext(context);
            RunResult runResult = context.workspace().run(workspaceRunContext);

            Collection<Diagnostic> diagnostics = runResult.diagnostics();
            for (Diagnostic diagnostic : diagnostics) {
                LogTraceParams diagnosticMessage = new LogTraceParams(diagnostic.toString(), ERROR_CHANNEL);
                context.getLanguageClient().logTrace(diagnosticMessage);
            }
            if (diagnostics.stream().anyMatch(d -> d.diagnosticInfo().severity() == DiagnosticSeverity.ERROR)) {
                LogTraceParams error = new LogTraceParams("error: compilation contains errors", ERROR_CHANNEL);
                context.getLanguageClient().logTrace(error);
                return false;
            }

            Process process = runResult.process();
            if (Objects.isNull(process)) {
                return false;
            }

            listenOutputAsync(context.getLanguageClient(), process::getInputStream, OUT_CHANNEL);
            listenOutputAsync(context.getLanguageClient(), process::getErrorStream, ERROR_CHANNEL);
            return true;
        } catch (IOException e) {
            LogTraceParams error = new LogTraceParams("Error while running the program in fast-run mode: " +
                    e.getMessage(), ERROR_CHANNEL);
            context.getLanguageClient().logTrace(error);
            throw new LSCommandExecutorException(e);
        } catch (Exception e) {
            LogTraceParams error = new LogTraceParams("Unexpected error while executing the fast-run: " +
                    e.getMessage(), ERROR_CHANNEL);
            context.getLanguageClient().logTrace(error);
            throw new LSCommandExecutorException(e);
        }
    }

    private RunContext getWorkspaceRunContext(ExecuteCommandContext context) {
        RunContext.Builder builder = new RunContext.Builder(extractPath(context));
        builder.withProgramArgs(extractProgramArgs(context));
        builder.withEnv(extractEnvVariables(context));
        builder.withDebugPort(extractDebugArgs(context));

        return builder.build();
    }

    private Path extractPath(ExecuteCommandContext context) {
        return getCommandArgWithName(context, ARG_PATH)
                .map(CommandArgument::<JsonPrimitive>value)
                .map(JsonPrimitive::getAsString)
                .map(pathStr -> {
                    try {
                        Path path = Path.of(pathStr);
                        if (!Files.exists(path)) {
                            throw new IllegalArgumentException("Specified path does not exist: " + pathStr);
                        }
                        return path;
                    } catch (InvalidPathException e) {
                        throw new IllegalArgumentException("Invalid path: " + pathStr, e);
                    }
                })
                .orElseThrow(() -> new IllegalArgumentException("Path argument is required"));
    }

    private int extractDebugArgs(ExecuteCommandContext context) {
        return getCommandArgWithName(context, ARG_DEBUG_PORT)
                .map(CommandArgument::<JsonPrimitive>value)
                .map(JsonPrimitive::getAsInt)
                .orElse(-1);
    }

    private List<String> extractProgramArgs(ExecuteCommandContext context) {
        return getCommandArgWithName(context, ARG_PROGRAM_ARGS)
                .map(arg -> arg.<JsonArray>value().getAsJsonArray())
                .map(jsonArray -> StreamSupport.stream(jsonArray.spliterator(), false)
                        .filter(JsonElement::isJsonPrimitive)
                        .map(JsonElement::getAsJsonPrimitive)
                        .filter(JsonPrimitive::isString)
                        .map(JsonPrimitive::getAsString)
                        .toList())
                .orElse(Collections.emptyList());
    }

    private Map<String, String> extractEnvVariables(ExecuteCommandContext context) {
        return getCommandArgWithName(context, ARG_ENV)
                .map(CommandArgument::<JsonObject>value)
                .map(jsonObject -> {
                    Map<String, String> envMap = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                            envMap.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                    return Collections.unmodifiableMap(envMap);
                })
                .orElse(Map.of());
    }

    private static Optional<CommandArgument> getCommandArgWithName(ExecuteCommandContext context, String name) {
        return context.getArguments().stream()
                .filter(commandArg -> commandArg.key().equals(name))
                .findAny();
    }

    public void listenOutputAsync(ExtendedLanguageClient client, Supplier<InputStream> getInputStream, String channel) {
        Thread.startVirtualThread(() -> listenOutput(client, getInputStream, channel));
    }

    private static void listenOutput(ExtendedLanguageClient client, Supplier<InputStream> inSupplier, String channel) {
        try (InputStream in = inSupplier.get()) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) >= 0) {
                String str = new String(buffer, 0, count, StandardCharsets.UTF_8);
                client.logTrace(new LogTraceParams(str, channel));
            }
        } catch (IOException ignored) {
            // ignore
        }
        client.logTrace(new LogTraceParams("", "stopped"));
    }

    @Override
    public String getCommand() {
        return RUN_COMMAND;
    }
}
