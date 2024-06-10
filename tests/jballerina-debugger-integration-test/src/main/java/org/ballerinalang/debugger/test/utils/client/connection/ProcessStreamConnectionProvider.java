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
import java.nio.file.Path;
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
public class ProcessStreamConnectionProvider implements StreamConnectionProvider {

    private final List<String> commands;
    private final String workingDir;
    private final String balHome;

    private static final String ENV_JAVA_OPTS = "JAVA_OPTS";
    private static final String ENV_DEBUGGER_TEST_MODE = "BAL_DEBUGGER_TEST";
    private static final String JACOCO_AGENT_ARGS = " -javaagent:%s=destfile=%s ";
    private static final Logger LOG = LoggerFactory.getLogger(ProcessStreamConnectionProvider.class);

    public ProcessStreamConnectionProvider(List<String> commands, String workingDir, String balHome) {
        this.commands = commands;
        this.workingDir = workingDir;
        this.balHome = balHome;
    }

    private Process process = null;

    @Override
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
        Path jacocoAgentPath = Paths.get(balHome).resolve("bre").resolve("lib").resolve("jacocoagent.jar");
        Path destinationFile = Paths.get(System.getProperty("user.dir")).resolve("build").resolve("jacoco")
                .resolve("debugger-core-test.exec");
        String agentArgs = String.format(JACOCO_AGENT_ARGS, jacocoAgentPath, destinationFile);

        String javaOpts = "";
        if (envProperties.containsKey(ENV_JAVA_OPTS)) {
            javaOpts = envProperties.get(ENV_JAVA_OPTS);
        }
        if (javaOpts.contains("jacoco.agent")) {
            return;
        }
        javaOpts = agentArgs + javaOpts;
        envProperties.put(ENV_JAVA_OPTS, javaOpts);
        // env variable to run debug server in test mode. This flag will enable jacoco coverage report generation for
        // all the sub-processes (JVMs) that will be running during the debug session.
        envProperties.put(ENV_DEBUGGER_TEST_MODE, String.valueOf(true));
    }

    @Override
    public InputStream getInputStream() {
        return process != null ? process.getInputStream() : null;
    }

    @Override
    public OutputStream getOutputStream() {
        return process != null ? process.getOutputStream() : null;
    }

    @Override
    public void stop() {
        if (process != null) {
            killProcessWithDescendants(process);
        }
    }

    private void killProcessWithDescendants(Process parent) {
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
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessStreamConnectionProvider other) {
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
