/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.utils;

import io.ballerina.cli.cmd.RunCommand;
import io.ballerina.cli.launcher.BLauncherException;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.cli.launcher.RuntimePanicException;
import io.ballerina.runtime.internal.utils.RuntimeUtils;
import org.ballerinalang.compiler.BLangCompilerException;

import java.io.PrintStream;

/**
 * Represents a wrapper for the RunCommand to be run in a different thread.
 *
 * @since 2201.10.0
 */
public class RunCommandExecutor extends Thread {
    private static final String COMPILATION_ERROR_MESSAGE = "compilation contains errors";

    private final RunCommand runCommand;
    private final PrintStream outStream;
    private volatile boolean runtimePanic;

    public RunCommandExecutor(RunCommand runCommand, PrintStream outStream) {
        this.runCommand = runCommand;
        this.outStream = outStream;
        this.runtimePanic = false;
    }

    @Override
    public void run() {
        // We use the original runCommand instance with the watch field set to false. That will preserve all the
        // build options passed by the developer.
        try {
            runCommand.unsetWatch();
            runCommand.setInitialWatch();
            runCommand.execute();
        } catch (BLangCompilerException e) {
            if (!(e.getMessage().contains(COMPILATION_ERROR_MESSAGE))) {
                // print the error message only if the exception was not thrown due to compilation errors
                outStream.println(LauncherUtils.prepareCompilerErrorMessage(e.getMessage()));
            }
            // These are compiler errors, and are already logged. Hence simply exit.
        } catch (BLauncherException e) {
            LauncherUtils.printLauncherException(e, outStream);
        } catch (RuntimePanicException ignored) {
            runtimePanic = true;
        } catch (Throwable e) {
            RuntimeUtils.logBadSad(e);
            runtimePanic = true;
        }
    }

    public synchronized void terminate() {
        runCommand.killProcess();
        this.interrupt();
    }

    public synchronized boolean shouldWatch() {
        return runCommand.containsService() && !runtimePanic;
    }
}
