/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.os;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Native function ballerina.os:runCommand.
 */
@BallerinaFunction(
    orgName = "ballerina", packageName = "os",
    functionName = "runCommand",
    args = {
        @Argument(name = "command", type = TypeKind.STRING),
        @Argument(name = "workspace", type = TypeKind.STRING),
        @Argument(name = "timeoutValue", type = TypeKind.INT)
    },
    returnType = {
        @ReturnType(type = TypeKind.STRUCT)
    },
    isPublic = true
)
public class RunCommand extends BlockingNativeCallableUnit {

    private static final String PACKAGE_NAME = "ballerina.os";

    private static Logger logger = LoggerFactory.getLogger(RunCommand.class);

    @Override public void execute(Context context) {
        String command = context.getStringArgument(0);
        String workspace = context.getStringArgument(1);
        Long timeout = context.getIntArgument(0);

        if (logger.isDebugEnabled()) {
            logger.debug("Running shell command : " + command + ", from directory : " + workspace
                         + ", with a timeout : " + timeout);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));

        if (workspace.isEmpty()) {
            workspace = System.getProperty("user.dir");
        }
        File workingFile = new File(workspace);
        if (!workingFile.exists()) {
            String msg = "Workspace doesn't exist.";
            context.setReturnValues(createError(context, msg));
        }

        try {
            Process process = processBuilder.directory(workingFile)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();

            // If timeout value is negative, we block the thread till the process returns
            if (timeout > 0) {
                if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                    // The execution is successful
                    String msg = "The user specified timeout : " + timeout + " exceeded.";
                    process.destroyForcibly();
                    context.setReturnValues(createError(context, msg));
                } else if (process.exitValue() == 0) {
                    return;
                } else if (process.exitValue() == 1) {
                    String msg = "The command didn't execute successfully.";
                    context.setReturnValues(createError(context, msg));
                }
            } else {
                // Block the process till execution is completed
                if (process.waitFor() == 0) {
                    return;
                } else {
                    // There is a command execution error
                    String msg = "The command didn't execute successfully.";
                    context.setReturnValues(createError(context, msg));
                }
            }
        } catch (IOException | InterruptedException | SecurityException e) {
            String msg = "Error occurred while executing the command : " + e.getMessage();
            logger.error(msg, e);
            context.setReturnValues(createError(context, msg));
        }
    }

    private BStruct createError(Context context, String msg) {
        return BLangVMErrors.createError(context, msg);
    }
}
