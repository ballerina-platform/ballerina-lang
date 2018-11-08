/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.command.executors;

import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.io.IOUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.command.LSCommandExecutor;
import org.ballerinalang.langserver.command.LSCommandExecutorException;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.langserver.command.CommandUtil.clearDiagnostics;
import static org.ballerinalang.langserver.command.CommandUtil.notifyClient;

/**
 * Command executor for pulling a package from central.
 *
 * @since 0.983.0
 */
@JavaSPIService("org.ballerinalang.langserver.command.LSCommandExecutor")
public class PullModuleExecutor implements LSCommandExecutor {

    // A newCachedThreadPool with a limited max-threads
    private static ExecutorService executor = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(), 60L,
                                                                     TimeUnit.SECONDS, new SynchronousQueue<>());

    private static final String COMMAND = "PULL_MODULE";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        executor.submit(() -> {
            // Derive module name and document uri
            String moduleName = "";
            String documentUri = "";
            for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
                String argKey = ((LinkedTreeMap) arg).get(ARG_KEY).toString();
                String argVal = ((LinkedTreeMap) arg).get(ARG_VALUE).toString();
                if (argKey.equals(CommandConstants.ARG_KEY_MODULE_NAME)) {
                    moduleName = argVal;
                } else if (argKey.equals(CommandConstants.ARG_KEY_DOC_URI)) {
                    documentUri = argVal;
                }
            }
            // If no package, or no doc uri; then just skip
            if (moduleName.isEmpty() || documentUri.isEmpty()) {
                return;
            }
            // Execute `ballerina pull` command
            String ballerinaHome = Paths.get(CommonUtil.BALLERINA_HOME).resolve("bin").resolve("ballerina").toString();
            ProcessBuilder processBuilder = new ProcessBuilder(ballerinaHome, "pull", moduleName);
            LanguageClient client = context.get(ExecuteCommandKeys.LANGUAGE_SERVER_KEY).getClient();
            LSCompiler lsCompiler = context.get(ExecuteCommandKeys.LS_COMPILER_KEY);
            DiagnosticsHelper diagnosticsHelper = context.get(ExecuteCommandKeys.DIAGNOSTICS_HELPER_KEY);
            try {
                notifyClient(client, MessageType.Info, "Pulling '" + moduleName + "' from the Ballerina Central...");
                Process process = processBuilder.start();
                InputStream inputStream = process.getInputStream();
                // Consume and skip input-stream
                int data = inputStream.read();
                while (data != -1) {
                    data = inputStream.read();
                }
                // Check error stream for errors
                InputStream errorStream = process.getErrorStream();
                String error = IOUtils.toString(errorStream, StandardCharsets.UTF_8);

                if (error == null || error.isEmpty()) {
                    notifyClient(client, MessageType.Info, "Pulling success for the '" + moduleName + "' module!");
                    clearDiagnostics(client, lsCompiler, diagnosticsHelper, documentUri);
                } else {
                    notifyClient(client, MessageType.Error,
                            "Pulling failed for the '" + moduleName + "' module!\n" + error);
                }
            } catch (IOException e) {
                notifyClient(client, MessageType.Error, "Pulling failed for the '" + moduleName + "' module!");
            }
        });

        return new Object();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }
}
