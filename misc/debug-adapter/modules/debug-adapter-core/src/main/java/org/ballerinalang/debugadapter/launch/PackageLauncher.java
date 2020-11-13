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


package org.ballerinalang.debugadapter.launch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Ballerina package runner.
 */
public class PackageLauncher extends Launcher {

    public PackageLauncher(String projectRoot, Map<String, Object> args) {
        super(projectRoot, args);
    }

    public Process start() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // Adds ballerina run/test command with debug flag.
        processBuilder.command(getLauncherCommand(null));

        Map<String, String> env = processBuilder.environment();
        // set environment ballerina home
        env.put("BALLERINA_HOME", getBallerinaHome());
        processBuilder.directory(Paths.get(projectRoot).toFile());
        return processBuilder.start();
    }
}
