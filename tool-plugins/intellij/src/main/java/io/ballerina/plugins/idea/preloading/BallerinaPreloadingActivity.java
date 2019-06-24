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

import com.google.common.base.Strings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import io.ballerina.plugins.idea.extensions.BallerinaLSPExtensionManager;
import io.ballerina.plugins.idea.sdk.BallerinaPathModificationTracker;
import io.ballerina.plugins.idea.sdk.BallerinaSdk;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.RawCommandServerDefinition;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINAX_SOURCE_PATH;
import static io.ballerina.plugins.idea.BallerinaConstants.LAUNCHER_SCRIPT_PATH;
import static io.ballerina.plugins.idea.preloading.OperatingSystemUtils.getOperatingSystem;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaPreloadingActivity extends PreloadingActivity {

    private static final Logger LOGGER = Logger.getInstance(BallerinaPreloadingActivity.class);

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

        BallerinaSdk balSdk = BallerinaSdkUtils.getBallerinaSdkFor(project);
        if (balSdk.hasLangServerSupport()) {
            return doRegister(balSdk.getSdkPath());
        }
        return false;
    }

    private static boolean doRegister(@NotNull String sdkPath) {
        String os = OperatingSystemUtils.getOperatingSystem();
        if (os != null) {
            String args = null;
            if (os.equals(OperatingSystemUtils.UNIX) || os.equals(OperatingSystemUtils.MAC)) {
                args = Paths.get(sdkPath, LAUNCHER_SCRIPT_PATH, "language-server-launcher.sh").toString();
            } else if (os.equals(OperatingSystemUtils.WINDOWS)) {
                args = Paths.get(sdkPath, LAUNCHER_SCRIPT_PATH, "language-server-launcher.bat").toString();
            }

            if (!Strings.isNullOrEmpty(args)) {
                IntellijLanguageClient.addServerDefinition(new RawCommandServerDefinition("bal", new String[]{args}));
                IntellijLanguageClient.addExtensionManager("bal", new BallerinaLSPExtensionManager());
                LOGGER.info("registered language server definition using Sdk path: " + sdkPath);
                return true;
            }
            return false;
        }
        return false;
    }

    private static void updateBallerinaPathModificationTracker(Project project, ProjectStatus status) {
        BallerinaSdk balSdk = BallerinaSdkUtils.getBallerinaSdkFor(project);
        if (balSdk.getSdkPath() == null) {
            return;
        }
        Path balxPath = Paths.get(balSdk.getSdkPath(), BALLERINAX_SOURCE_PATH);
        if (balxPath.toFile().isDirectory()) {
            if (status == ProjectStatus.OPENED) {
                BallerinaPathModificationTracker.addPath(balxPath.toString());
            } else if (status == ProjectStatus.CLOSED) {
                BallerinaPathModificationTracker.removePath(balxPath.toString());
            }
        }
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
            LOGGER.error("Error occurred", e);
        }
    }

    private enum ProjectStatus {
        OPENED, CLOSED
    }
}
