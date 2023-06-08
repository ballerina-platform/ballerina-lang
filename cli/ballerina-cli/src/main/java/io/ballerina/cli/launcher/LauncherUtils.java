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
package io.ballerina.cli.launcher;

import io.ballerina.runtime.api.values.BError;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Contains utility methods for executing a Ballerina program.
 *
 * @since 0.8.0
 */
public class LauncherUtils {

    public static Path getSourceRootPath(String sourceRoot) {
        // Get source root path.
        Path sourceRootPath;
        if (sourceRoot == null || sourceRoot.isEmpty()) {
            sourceRootPath = Paths.get(System.getProperty("user.dir"));
        } else {
            try {
                sourceRootPath = Paths.get(sourceRoot).toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                throw new RuntimeException("error reading from directory: " + sourceRoot + " reason: " +
                        e.getMessage(), e);
            }

            if (!Files.isDirectory(sourceRootPath, LinkOption.NOFOLLOW_LINKS)) {
                throw new RuntimeException("source root must be a directory");
            }
        }
        return sourceRootPath;
    }


    public static BLauncherException createUsageExceptionWithHelp(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("ballerina: " + errorMsg);
        launcherException.addMessage("Run 'bal help' for usage.");
        return launcherException;
    }

    public static BLauncherException createLauncherException(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorMsg);
        return launcherException;
    }

    public static BLauncherException createLauncherException(String errorPrefix, Throwable cause) {
        String message;
        if (cause instanceof BError) {
            message = ((BError) cause).getPrintableStackTrace();
        } else {
            message = cause.toString();
        }
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorPrefix + message);
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
        char[] c = s.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    static String wrapString(String str, int wrapLength, int indent) {
        StringBuilder wrappedStr = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (Character.isWhitespace(str.charAt(i))) {
                i++;
                continue;
            }
            if (i > 0) {
                wrappedStr.append("\n");
                wrappedStr.append(" ".repeat(indent));
            }
            int lineEnd = Math.min(i + wrapLength, str.length());
            if (lineEnd < str.length() && !Character.isWhitespace(str.charAt(lineEnd))) {
                // find the last whitespace character before the maximum line length
                int lastWhitespace = str.lastIndexOf(' ', lineEnd);
                if (lastWhitespace > i) {
                    lineEnd = lastWhitespace;
                }
            }
            wrappedStr.append(str, i, lineEnd);
            i = lineEnd;
            // skip any whitespace characters at the beginning of the next line
            while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
                i++;
            }
        }
        return wrappedStr.toString();
    }

}
