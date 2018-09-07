/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.ballerina.plugins.idea.languageserver;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

/**
 * Ballerina Language server launcher definition.
 */
public class BallerinaLanguageServerDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaLanguageServerDefinition.class);

    final String jrePath = "/bre/lib/jre";
    private final String libPath = "/bre/lib/*";
    private final String composerLibPath = "/lib/resources/composer/services/*";
    private final String mainClassPath = "org.ballerinalang.langserver.launchers.stdio.Main";

    private String myProjectSdkPath;

    BallerinaLanguageServerDefinition(@NotNull String sdkPath) {
        this.myProjectSdkPath = sdkPath;
    }

    private String getClassPaths(String sdkPath) {

        Path jarPath = Paths.get(myProjectSdkPath, "server-build", "language-server-stdio-launcher.jar");
        String classPathSeperator = File.pathSeparator;

        return Paths.get(sdkPath, composerLibPath).toString() + classPathSeperator + Paths.get(sdkPath, libPath)
                .toString() + classPathSeperator + jarPath;
    }

    private String getExecutable() {

        String executable = "java";

        if (myProjectSdkPath != "") {

            Path breLibPath = Paths.get(myProjectSdkPath, jrePath);
            File dir = new File(breLibPath.toString());
            File[] jres = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith("jre-");
                }
            });

            if (jres.length == 0) {
                LOGGER.debug("Couldn't find valid jre directory in ballerina.home" + jrePath.toString());
            } else {
                Path jrePath = Paths.get(jres[0].getAbsolutePath(), "bin", "java");
                //TODO: clarify whether we need to check JAVA_HOME if the jre was not found in ballerina.home
                if (Files.exists(jrePath)) {
                    LOGGER.debug("Using java from ballerina.home:" + jrePath.toString());
                    executable = jrePath.toString();
                } else {
                    LOGGER.debug("Couldn't find valid java executable in ballerina.home" + jrePath.toString());
                }
            }
        }
        return executable;
    }

    public String getInitCommand(boolean inDebugMode, boolean enableDebugLog) {

        StringJoiner finalCommand = new StringJoiner(" ");
        try {
            String executable = getExecutable();
            String balHomeSysProp = "-Dballerina.home=" + myProjectSdkPath;
            String balDebugLogSysProp = "-Dballerina.debugLog=" + Boolean.toString(enableDebugLog);

            String debugArgs = "";
            if (inDebugMode) {
                LOGGER.debug("Language Server will be running on debug mode");
                debugArgs = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y";
            }
            String classpaths = "-cp " + getClassPaths(myProjectSdkPath);

            finalCommand.add(executable);
            finalCommand.add(balHomeSysProp);
            finalCommand.add(balDebugLogSysProp);
            finalCommand.add(debugArgs);
            finalCommand.add(classpaths);
            finalCommand.add(mainClassPath);

        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return finalCommand.toString();
    }

}
