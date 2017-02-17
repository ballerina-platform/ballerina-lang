/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

/**
 * Contains utility methods for executing a Ballerina program
 *
 * @since 0.8.0
 */
public class LauncherUtils {

    public static BLauncherException createUsageException(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("ballerina: " + errorMsg);
        launcherException.addMessage("Run 'ballerina help' for usage.");
        return launcherException;
    }

    static BLauncherException createLauncherException(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage(errorMsg);
        return launcherException;
    }

    static void printLauncherException(BLauncherException e, PrintStream outStream) {
        List<String> errorMessages = e.getMessages();
        errorMessages.forEach(outStream::println);
    }

    static String makeFirstLetterLowerCase(String s) {
        if (s == null) {
            return null;
        }
        char c[] = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    /**
     * Write the process ID of this process to the file.
     *
     * @param ballerinaHome ballerina.home sys property value.
     */
    static void writePID(String ballerinaHome) {

        String[] cmd = {"bash", "-c", "echo $PPID"};
        Process p;
        String pid = "";
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            //Ignored. We might be invoking this on a Window platform. Therefore if an error occurs
            //we simply ignore the error.
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            pid = builder.toString();
        } catch (Throwable e) {
            throw createLauncherException("error: fail to write ballerina.pid file: " +
                    makeFirstLetterLowerCase(e.getMessage()));
        }

        if (pid.length() != 0) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Paths.get(ballerinaHome, "ballerina.pid").toString()),
                    StandardCharsets.UTF_8))) {
                writer.write(pid);
            } catch (IOException e) {
                throw createLauncherException("error: fail to write ballerina.pid file: " +
                        makeFirstLetterLowerCase(e.getMessage()));
            }
        }
    }
}
