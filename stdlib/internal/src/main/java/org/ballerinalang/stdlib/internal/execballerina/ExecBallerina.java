/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.internal.execballerina;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Native function ballerina/internal:execBallerina.
 *
 * @since 0.974.0-beta14
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "execBallerina",
        args = {@Argument(name = "command", type = TypeKind.STRING),
                @Argument(name = "packagePath", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING),
                      @ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class ExecBallerina extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {

        Properties property = System.getProperties();
        String path = (String) property.get("ballerina.home");

        String command = context.getRefArgument(0).stringValue();
        String packagePath = context.getStringArgument(0);

        String balCommand = Paths.get(path, "bin", "ballerina ") + command + " " + packagePath;

        Process process;
        BufferedReader reader = null;
        BufferedReader readerEr = null;
        try {
            StringBuilder output = new StringBuilder();
            process = Runtime.getRuntime().exec(balCommand);
            process.waitFor();
            reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            readerEr =
                    new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            String lineEr;
            while ((lineEr = readerEr.readLine()) != null) {
                output.append(lineEr).append("\n");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            String adjusted = output.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
            context.setReturnValues(new BString(adjusted));
        } catch (InterruptedException e) {
            context.setReturnValues(BLangVMErrors.createError(context,
                    "Error occurred while waiting for ballerina command to finish: " + e.getMessage()));
            throw new BallerinaException(e.getMessage());
        } catch (IOException e) {
            context.setReturnValues(BLangVMErrors.createError(context,
                    "Error occurred executing command or reading out of the command: " + e.getMessage()));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                context.setReturnValues(BLangVMErrors.createError(context,
                        "Error occurred closing standard output: " + e.getMessage()));
            }
            try {
                if (readerEr != null) {
                    readerEr.close();
                }
            } catch (IOException e) {
                context.setReturnValues(BLangVMErrors.createError(context,
                        "Error occurred closing standard error output: " + e.getMessage()));
            }
        }

    }
}
