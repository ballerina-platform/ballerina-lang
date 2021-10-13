/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.debugger.test.utils.client.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class symbolizing a stream to a process.
 * <p>
 * commands - The commands to start the process
 * workingDir - The working directory of the process
 */
public class TestProcessStreamConnectionProvider implements TestStreamConnectionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TestProcessStreamConnectionProvider.class);
    private final List<String> commands;
    private final String workingDir;
    private final String balHome;
    private static final String JAVA_OPTS = "JAVA_OPTS";

    public TestProcessStreamConnectionProvider(List<String> commands, String workingDir, String balHome) {
        this.commands = commands;
        this.workingDir = workingDir;
        this.balHome = balHome;
    }

    private Process process = null;

    public void start() throws IOException {
        if (workingDir == null || commands == null || commands.isEmpty() || commands.contains(null)) {
            throw new IOException("Unable to start debug server: " + this);
        }
        ProcessBuilder builder = createProcessBuilder();
        LOG.info("Starting server process with commands " + commands + " and workingDir " + workingDir);
        configureJacocoAgentArgs(builder.environment());
        process = builder.start();
        if (!process.isAlive()) {
            throw new IOException("Unable to start debug server: " + this);
        } else {
            LOG.info("Server process started " + process);
        }
    }

    private ProcessBuilder createProcessBuilder() {
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.directory(new File(workingDir));
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return builder;
    }

    /**
     * Injects jacoco agent args into the debug server VM environment.
     */
    private void configureJacocoAgentArgs(Map<String, String> envProperties) {
        String agentArgs = String.format("-javaagent:%s=destfile=%s ", Paths.get(balHome).resolve("bre").resolve("lib")
                .resolve("jacocoagent.jar"), Paths.get(System.getProperty("user.dir")).resolve("build")
                .resolve("jacoco").resolve("test.exec"));

        String javaOpts = "";
        if (envProperties.containsKey(JAVA_OPTS)) {
            javaOpts = envProperties.get(JAVA_OPTS);
        }
        if (javaOpts.contains("jacoco.agent")) {
            return;
        }
        javaOpts = agentArgs + javaOpts;
        envProperties.put(JAVA_OPTS, javaOpts);
    }

    @Override
    public InputStream getInputStream() {
        return process != null ? process.getInputStream() : null;
    }

    @Override
    public OutputStream getOutputStream() {
        return process != null ? process.getOutputStream() : null;
    }

    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestProcessStreamConnectionProvider) {
            TestProcessStreamConnectionProvider other = (TestProcessStreamConnectionProvider) obj;
            return commands.size() == other.commands.size()
                    && new HashSet<>(commands).equals(new HashSet<>(other.commands))
                    && workingDir.equals(other.workingDir);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commands) ^ Objects.hashCode(workingDir);
    }
}
