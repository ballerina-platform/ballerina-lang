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
package io.ballerina.plugins.idea.preloading;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

import static io.ballerina.plugins.idea.preloading.OperatingSystemUtils.getOperatingSystem;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaLanguageServerPreloadingActivity extends PreloadingActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaLanguageServerPreloadingActivity.class);
    private static final String launcherScriptPath = "lib/tools/lang-server/launcher";

    /**
     * Preloading of the ballerina plugin.
     */
    @Override
    public void preload(@NotNull ProgressIndicator indicator) {

        //Tries to register language server definition, if a ballerina project is being opened initially.
        registerServerDefinition();

        final MessageBusConnection connect = ApplicationManager.getApplication().getMessageBus().connect();
        connect.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@Nullable final Project project) {
                registerServerDefinition(project);
            }
        });

        ProjectManager.getInstance().addProjectManagerListener(project -> {
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            if (openProjects.length <= 1) {
                stopProcesses();
            }
            return true;
        });
    }

    /**
     * Registered language server definition using currently opened ballerina projects.
     *
     * @return Returns true if a definition is registered successfully.
     */
    public static boolean registerServerDefinition() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            if (registerServerDefinition(project)) {
                return true;
            }
        }
        return false;
    }

    public static boolean registerServerDefinition(Project project) {
        //If the project does not have a ballerina SDK attached, ballerinaSdkPath will be null.
        String balSdkPath = getBallerinaSdk(project);
        if (balSdkPath != null) {
            return doRegister(balSdkPath);
        }
        return false;
    }

    public static boolean doRegister(@NotNull String sdkPath) {

        String os = OperatingSystemUtils.getOperatingSystem();
        if (os != null) {
            String[] args = new String[1];
            if (os.equals(OperatingSystemUtils.UNIX) || os.equals(OperatingSystemUtils.MAC)) {
                args[0] = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.sh").toString();
            } else if (os.equals(OperatingSystemUtils.WINDOWS)) {
                args[0] = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.bat").toString();
            }

            if (args[0] != null) {
                LanguageServerRegisterService.register(args);
                LOGGER.info("registered language server definition using Sdk path: " + sdkPath);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Stops running language server instances.
     */
    public static void stopProcesses() {
        try {
            String os = getOperatingSystem();
            if (os == null) {
                LOGGER.error("unsupported operating system");
                return;
            }
            Terminator terminator = new TerminatorFactory().getTerminator(os);
            if (terminator == null) {
                LOGGER.error("unsupported operating system");
                return;
            }
            terminator.terminate();

        } catch (Exception e) {
            LOGGER.error("Error occured", e);
        }
    }

    @Nullable
    private static String getBallerinaSdk(Project project) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk == null) {
            return null;
        }
        String sdkPath = projectSdk.getHomePath();
        return (isBallerinaSdkHome(sdkPath)) ? sdkPath : null;
    }

    private static boolean isBallerinaSdkHome(String sdkPath) {
        if (sdkPath == null || sdkPath.equals("")) {
            return false;
        }
        String balScriptPath = Paths.get(sdkPath, "bin", "ballerina").toString();
        String scriptPath = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.sh").toString();
        return new File(balScriptPath).exists() && new File(scriptPath).exists();
    }
}
