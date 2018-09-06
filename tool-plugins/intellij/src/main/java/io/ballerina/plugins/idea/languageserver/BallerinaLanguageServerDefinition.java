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

import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

public class BallerinaLanguageServerDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaLanguageServerDefinition.class);

    final String JRE_PATH = "/bre/lib/jre*";
    private final String LIB_PATH = "/bre/lib/*";
    private final String COMPOSER_LIB_PATH = "/lib/resources/composer/services/*";
    private final String MAIN_CLASS_PATH = "org.ballerinalang.langserver.launchers.stdio.Main";

    Sdk projectSdk;

    BallerinaLanguageServerDefinition(@NotNull Sdk projectSdk) {
        this.projectSdk = projectSdk;
    }

    private String getClassPaths(String sdkPath) {

        Path jarPath = Paths.get("server-build", "language-server-stdio-launcher.jar");
        String classPathSeperator = File.pathSeparator;

        return Paths.get(sdkPath, COMPOSER_LIB_PATH).toString() + classPathSeperator + Paths.get(sdkPath, LIB_PATH)
                .toString() + classPathSeperator + jarPath;
    }

    private String getExcecutable() {

        //TODO: clarify from where do we get the JVM
        return "java";
    }

    public String getInitCommand(boolean inDebugMode, boolean enableDebugLog) {

        StringJoiner finalCommand = new StringJoiner(" ");
        try {
            String executable = getExcecutable();
            String balHomeSysProp = "-Dballerina.home=" + projectSdk.getHomePath();
            String balDebugLogSysProp = "-Dballerina.debugLog=" + Boolean.toString(enableDebugLog);

            String debugArgs = "";
            if (inDebugMode) {
                LOGGER.debug("Language Server will run on debug mode");
                debugArgs = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005,quiet=y";
            }
            String classpaths = "-cp " + getClassPaths(projectSdk.getHomePath());

            finalCommand.add(executable);
            finalCommand.add(balHomeSysProp);
            finalCommand.add(balDebugLogSysProp);
            finalCommand.add(debugArgs);
            finalCommand.add(classpaths);
            finalCommand.add(MAIN_CLASS_PATH);

        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return finalCommand.toString();
    }

}
