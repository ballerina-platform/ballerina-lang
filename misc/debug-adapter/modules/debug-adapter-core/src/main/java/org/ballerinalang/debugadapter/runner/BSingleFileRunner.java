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


package org.ballerinalang.debugadapter.runner;

import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Ballerina single file runner.
 */
public class BSingleFileRunner extends BProgramRunner {

    public BSingleFileRunner(ClientLaunchConfigHolder configHolder, String fileRoot) {
        super(configHolder, fileRoot);
    }

    @Override
    public Process start() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String balFilePath = configHolder.getSourcePath();
        processBuilder.command(getBallerinaCommand(balFilePath));

        Path singleFileRoot = Paths.get(projectRoot).getParent();
        if (singleFileRoot != null) {
            processBuilder.directory(singleFileRoot.toFile());
        }

        Map<String, String> env = processBuilder.environment();
        // Need to ignore the "BAL_JAVA_DEBUG" env variable, as otherwise the program compiler will also run in debug
        // mode by honoring inherited environment variables.
        env.remove(ENV_OPTION_BAL_JAVA_DEBUG);
        // Adds environment variables configured by the user.
        if (configHolder.getEnv().isPresent()) {
            configHolder.getEnv().get().forEach(env::put);
        }

        // If the debugger is running on test mode, modifies jacoco agent args to instrument debugger runtime classes.
        if (env.containsKey(ENV_DEBUGGER_TEST_MODE) && env.containsKey(ENV_JAVA_OPTS)) {
            String javaOpts = env.get(ENV_JAVA_OPTS);
            if (javaOpts.contains(DEBUGGER_CORE_TEST_FILE)) {
                javaOpts = javaOpts.replace(DEBUGGER_CORE_TEST_FILE, DEBUGGER_RUNTIME_TEST_FILE);
            }
            env.put(ENV_JAVA_OPTS, javaOpts);
        }

        return processBuilder.start();
    }
}
