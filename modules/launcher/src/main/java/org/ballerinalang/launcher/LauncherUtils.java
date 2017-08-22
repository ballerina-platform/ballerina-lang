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

import org.ballerinalang.BLangCompiler;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.runtime.model.BLangRuntimeRegistry;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.ProgramFile;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Contains utility methods for executing a Ballerina program.
 *
 * @since 0.8.0
 */
public class LauncherUtils {

    public static void runProgram(Path sourceRootPath, Path sourcePath, boolean runServices, String[] args) {
        ProgramFile programFile;

        String srcPathStr = sourcePath.toString();
        if (srcPathStr.endsWith(BLangConstants.BLANG_EXEC_FILE_SUFFIX)) {
            programFile = BLangProgramLoader.read(sourcePath);
        } else {
            programFile = BLangCompiler.compile(sourceRootPath, sourcePath);
        }

        // If there is no main or service entry point, throw an error
        if (!programFile.isMainEPAvailable() && !programFile.isServiceEPAvailable()) {
            throw new RuntimeException("main function not found in '" + programFile.getProgramFilePath() + "'");
        }

        if (runServices || !programFile.isMainEPAvailable()) {
            if (args.length > 0) {
                throw LauncherUtils.createUsageException("too many arguments");
            }
            runServices(programFile);
        } else {
            runMain(programFile, args);
        }
    }

    private static void runMain(ProgramFile programFile, String[] args) {
        // Load Client Connectors
        BallerinaConnectorManager.getInstance().setMessageProcessor(new MessageProcessor());

        BLangProgramRunner.runMain(programFile, args);
        try {
            ThreadPoolFactory.getInstance().getWorkerExecutor().shutdown();
            ThreadPoolFactory.getInstance().getWorkerExecutor().awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            // Ignore the error
        }
        Runtime.getRuntime().exit(0);
    }

    private static void runServices(ProgramFile programFile) {
        PrintStream outStream = System.out;

        // TODO : Fix this properly.
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());
        BLangRuntimeRegistry.getInstance().initialize();

        outStream.println("ballerina: deploying service(s) in '" + programFile.getProgramFilePath() + "'");
        BLangProgramRunner.runService(programFile);

        try {
            List<ServerConnector> startedConnectors = BallerinaConnectorManager.getInstance()
                    .startPendingConnectors();
            startedConnectors.forEach(serverConnector -> outStream.println("ballerina: started server connector " +
                    serverConnector));

            // Starting up HTTP Server connectors
            List<org.wso2.carbon.transport.http.netty.contract.ServerConnector> startedHTTPConnectors =
                    BallerinaConnectorManager.getInstance().startPendingHTTPConnectors();
            startedHTTPConnectors.forEach(serverConnector -> outStream.println("ballerina: started server connector " +
                                                                                       serverConnector));
        } catch (ServerConnectorException e) {
            throw new RuntimeException("error starting server connectors: " + e.getMessage(), e);
        }
    }

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
