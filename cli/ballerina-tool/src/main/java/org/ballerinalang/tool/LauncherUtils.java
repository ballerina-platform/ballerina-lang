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
package org.ballerinalang.tool;

import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        launcherException.addMessage("Run 'ballerina help' for usage.");
        return launcherException;
    }

    public static BLauncherException createLauncherException(String errorMsg) {
        BLauncherException launcherException = new BLauncherException();
        launcherException.addMessage("error: " + errorMsg);
        return launcherException;
    }

    public static BLauncherException createLauncherException(String errorPrefix, Throwable cause) {
        String message;
        if (cause instanceof ErrorValue) {
            message = ((ErrorValue) cause).getPrintableStackTrace();
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

    /**
     * Get the executable program ({@link ProgramFile}) given the compiled program
     * ({@link org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile}).
     *
     * @param programFile Compiled program
     * @return Executable program
     */
    public static ProgramFile getExecutableProgram(CompiledBinaryFile.ProgramFile programFile) {
        ByteArrayInputStream byteIS = null;
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            ProgramFileWriter.writeProgram(programFile, byteOutStream);

            ProgramFileReader reader = new ProgramFileReader();
            byteIS = new ByteArrayInputStream(byteOutStream.toByteArray());
            return reader.readProgram(byteIS);
        } catch (Throwable e) {
            throw createLauncherException("failed to compile file: " + makeFirstLetterLowerCase(e.getMessage()));
        } finally {
            if (byteIS != null) {
                try {
                    byteIS.close();
                } catch (IOException ignore) {
                }
            }

            try {
                byteOutStream.close();
            } catch (IOException ignore) {
            }
        }
    }
}
