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
import io.ballerina.plugins.idea.sdk.BallerinaPathModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.plugins.idea.preloading.OperatingSystemUtils.getOperatingSystem;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaPreloadingActivity extends PreloadingActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaPreloadingActivity.class);
    private static final String launcherScriptPath = "lib/tools/lang-server/launcher";
    private static final String ballerinaSourcePath = "lib/repo";

    /**
     * Preloading of the ballerina plugin.
     */
    @Override
    public void preload(@NotNull ProgressIndicator indicator) {

        // Stops all running language server instances.
        stopProcesses();
        // Registers language server definitions for initially opened projects.
        registerServerDefinition();

        final MessageBusConnection connect = ApplicationManager.getApplication().getMessageBus().connect();
        connect.subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@Nullable final Project project) {
                registerServerDefinition(project);
                updateBallerinaPathModificationTracker(project, ProjectStatus.OPENED);
            }
        });

        ProjectManager.getInstance().addProjectManagerListener(project -> {
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            if (openProjects.length <= 1) {
                stopProcesses();
                updateBallerinaPathModificationTracker(project, ProjectStatus.CLOSED);
            }
            return true;
        });
    }

    /**
     * Registered language server definition using currently opened ballerina projects.
     */
    private static void registerServerDefinition() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            registerServerDefinition(project);
            updateBallerinaPathModificationTracker(project, ProjectStatus.OPENED);
        }
    }

    private static boolean registerServerDefinition(Project project) {
        //If the project does not have a ballerina SDK attached, ballerinaSdkPath will be null.
        String balSdkPath = getBallerinaSdk(project);
        return balSdkPath != null && doRegister(balSdkPath);
    }

    private static boolean doRegister(@NotNull String sdkPath) {

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
    private static void stopProcesses() {
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

    private static void updateBallerinaPathModificationTracker(Project project, ProjectStatus status) {
        String balSdkPath = getBallerinaSdk(project);
        if (balSdkPath != null) {
            Path balxPath = Paths.get(balSdkPath, ballerinaSourcePath);
            if (balxPath.toFile().isDirectory()) {
                if (status == ProjectStatus.OPENED) {
                    BallerinaPathModificationTracker.addPath(balxPath.toString());
                } else if (status == ProjectStatus.CLOSED) {
                    BallerinaPathModificationTracker.removePath(balxPath.toString());
                }
            }
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

        // Checks for either shell script or batch file, since the shell script recognition error in windows.
        String balShellScript = Paths.get(sdkPath, "bin", "ballerina").toString();
        String balBatchScript = Paths.get(sdkPath, "bin", "ballerina.bat").toString();
        String launcherShellScript = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.sh").toString();
        String launcherBatchScript = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.bat").toString();
        return (new File(balShellScript).exists() || new File(balBatchScript).exists())
                && (new File(launcherShellScript).exists() || new File(launcherBatchScript).exists());
    }

    private enum ProjectStatus {
        OPENED, CLOSED
    }
}
