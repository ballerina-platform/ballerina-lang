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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.remoteServer.util.CloudNotifier;
import com.intellij.util.messages.MessageBusConnection;
import io.ballerina.plugins.idea.codeinsight.autodetect.BallerinaAutoDetectionSettings;
import io.ballerina.plugins.idea.sdk.BallerinaPathModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.RawCommandServerDefinition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.plugins.idea.preloading.OSUtils.MAC;
import static io.ballerina.plugins.idea.preloading.OSUtils.UNIX;
import static io.ballerina.plugins.idea.preloading.OSUtils.WINDOWS;
import static io.ballerina.plugins.idea.preloading.OSUtils.getOperatingSystem;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaPreloadingActivity extends PreloadingActivity {

    private static final Logger LOG = Logger.getInstance(BallerinaPreloadingActivity.class);
    private static final String launcherScriptPath = "lib/tools/lang-server/launcher";
    private static final String ballerinaSourcePath = "lib/repo";
    private static CloudNotifier notifier = new CloudNotifier("Ballerina Home Auto Detection");

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
        boolean autoDetected = false;
        //If the project does not have a ballerina SDK attached, ballerinaSdkPath will be null.
        String balSdkPath = getBallerinaSdk(project);
        // Checks for the user-configured auto detection settings.
        if (balSdkPath == null && BallerinaAutoDetectionSettings.getInstance().autoDetectBalHome()) {
            //If a ballerina SDK is not configured for the project, Plugin tries to auto detect the ballerina SDK.
            balSdkPath = autoDetectSdk();
            autoDetected = true;
        }
        if (balSdkPath != null && !balSdkPath.isEmpty()) {
            boolean success = doRegister(balSdkPath);
            if (success && autoDetected) {
                LOG.info("Auto-detected Ballerina Home: " + balSdkPath + " for the project: " + project.getBasePath());
                String finalBalSdkPath = balSdkPath;
                ApplicationManager.getApplication().invokeLater(() -> {
                    notifier.showMessage("No ballerina SDK is found for: " + project.getBasePath()
                            + "\nAuto Detected Ballerina Home: " + finalBalSdkPath, MessageType.INFO);
                });
            }
            return success;
        }
        return false;
    }

    private static boolean doRegister(@NotNull String sdkPath) {
        String os = OSUtils.getOperatingSystem();
        if (os != null) {
            String arg = "";
            if (os.equals(OSUtils.UNIX) || os.equals(OSUtils.MAC)) {
                arg = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.sh").toString();
            } else if (os.equals(OSUtils.WINDOWS)) {
                arg = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.bat").toString();
            }

            if (arg != null && !arg.isEmpty()) {
                // Provides ballerina language server launcher script location for the language client library.
                IntellijLanguageClient.addServerDefinition(new RawCommandServerDefinition("bal", new String[] { arg }));
                LOG.info("registered language server definition using Sdk path: " + sdkPath);
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
                LOG.error("unsupported operating system");
                return;
            }
            Terminator terminator = new TerminatorFactory().getTerminator(os);
            if (terminator == null) {
                LOG.error("unsupported operating system");
                return;
            }
            terminator.terminate();
        } catch (Exception e) {
            LOG.error("Error occurred", e);
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
        return (isValidBallerinaSdk(sdkPath)) ? sdkPath : null;
    }

    private static boolean isValidBallerinaSdk(String sdkPath) {
        // Checks for either shell script or batch file, since the shell script recognition error in windows.
        String balShellScript = Paths.get(sdkPath, "bin", "ballerina").toString();
        String balBatchScript = Paths.get(sdkPath, "bin", "ballerina.bat").toString();
        String launcherShellScript = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.sh").toString();
        String launcherBatchScript = Paths.get(sdkPath, launcherScriptPath, "language-server-launcher.bat").toString();
        return (new File(balShellScript).exists() || new File(balBatchScript).exists()) && (
                new File(launcherShellScript).exists() || new File(launcherBatchScript).exists());
    }

    private static String autoDetectSdk() {
        String ballerinaPath = "";
        String platform = getOperatingSystem();
        switch (platform) {
        case (WINDOWS):
            String ballerinaHome = System.getenv("BALLERINA_HOME");
            if (ballerinaHome != null && !ballerinaHome.isEmpty()) {
                return ballerinaHome;
            }
            ballerinaPath = getByCommand("where ballerina").trim();
            if (!ballerinaPath.isEmpty()) {
                ballerinaPath = ballerinaPath.replace("\\bin\\ballerina.bat", "");
            }
        case MAC:
            ballerinaPath = getByCommand("which ballerina");
            // remove ballerina bin from ballerinaPath
            if (!ballerinaPath.isEmpty()) {
                ballerinaPath = ballerinaPath.replace("/bin/ballerina", "");
                // For homebrew installations ballerina executables are in libexcec
                File homebrewBallerinaPath = new File(ballerinaPath, "libexec");
                if (homebrewBallerinaPath.exists()) {
                    ballerinaPath = homebrewBallerinaPath.getAbsolutePath();
                }
            }
            return ballerinaPath;
        case UNIX:
            ballerinaPath = getByCommand("which ballerina");
            // remove ballerina bin from path
            if (!ballerinaPath.isEmpty()) {
                ballerinaPath = ballerinaPath.replace("/bin/ballerina", "");
            }
            return ballerinaPath;
        }
        // If we cannot find ballerina home return empty.
        return ballerinaPath;
    }

    private static String getByCommand(String cmd) {
        java.util.Scanner s;
        try {
            // This may returns a symlink which links to the real path.
            s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
            String path = s.hasNext() ? s.next().trim().replace(System.lineSeparator(), "") : "";
            LOG.info("Which ballerina command returned: " + path);
            if (path.isEmpty()) {
                return path;
            }

            // Gets the actual file path if there are the symbolic links using "toRealPath()".
            String realPath = new File(path).toPath().toRealPath().toString();
            return realPath;
        } catch (IOException e) {
            LOG.warn("Error occurred when executing the command: " + cmd);
            return "";
        }
    }

    private enum ProjectStatus {
        OPENED, CLOSED
    }
}
