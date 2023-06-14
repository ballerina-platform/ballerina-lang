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

import com.google.gson.JsonPrimitive;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.eclipse.lsp4j.LogTraceParams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Command executor for running a Ballerina file. Each project at most has a single instance running at a time.
 * See {@link org.ballerinalang.langserver.command.executors.StopExecutor} for stopping a running instance.
 *
 * @since 2201.6.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class RunExecutor implements LSCommandExecutor {

    @Override
    public Boolean execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        try {
            Optional<Process> process = context.workspace().run(extractPath(context));
            if (process.isEmpty()) {
                return false;
            }
            listenOutput(process.get(), context.getLanguageClient());
            return true;
        } catch (IOException e) {
            throw new LSCommandExecutorException(e);
        }
    }

    private static Path extractPath(ExecuteCommandContext context) {
        return Path.of(context.getArguments().get(0).<JsonPrimitive>value().getAsString());
    }

    public void listenOutput(Process process, ExtendedLanguageClient client) throws IOException {
        Thread thread = new Thread(() -> listenOutputAsync(client, process));
        thread.setDaemon(true);
        thread.start();
    }

    private static void listenOutputAsync(ExtendedLanguageClient client, Process process) {
        InputStream in = process.getInputStream();
        try { // Can't use resource style due to SpotBugs bug.
            byte[] buffer = new byte[1024];
            int count;
            while ((count = in.read(buffer)) > 0) {
                String str = new String(buffer, 0, count, StandardCharsets.UTF_8);
                client.logTrace(new LogTraceParams(str, ""));
            }
        } catch (IOException ignored) {
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
        client.logTrace(new LogTraceParams("", "stopped"));
    }

    @Override
    public String getCommand() {
        return "RUN";
    }
}
