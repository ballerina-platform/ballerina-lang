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
import java.util.HashSet;
import java.util.List;
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

    public TestProcessStreamConnectionProvider(List<String> commands, String workingDir) {
        this.commands = commands;
        this.workingDir = workingDir;
    }

    private Process process = null;

    public void start() throws IOException {
        if (workingDir == null || commands == null || commands.isEmpty() || commands.contains(null)) {
            throw new IOException("Unable to start debug server: " + this.toString());
        }
        ProcessBuilder builder = createProcessBuilder();
        LOG.info("Starting server process with commands " + commands + " and workingDir " + workingDir);
        // builder.environment().put("BAL_JAVA_DEBUG", "5006");
        process = builder.start();
        if (!process.isAlive()) {
            throw new IOException("Unable to start debug server: " + this.toString());
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
