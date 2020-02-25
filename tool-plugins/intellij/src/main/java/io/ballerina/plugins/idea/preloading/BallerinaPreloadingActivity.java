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
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.messages.MessageBusConnection;
import io.ballerina.plugins.idea.extensions.BallerinaLSPExtensionManager;
import io.ballerina.plugins.idea.notifiers.BallerinaAutoDetectNotifier;
import io.ballerina.plugins.idea.sdk.BallerinaSdk;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import io.ballerina.plugins.idea.settings.autodetect.BallerinaAutoDetectionSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.ProcessBuilderServerDefinition;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.plugins.idea.BallerinaConstants.BAL_FILE_EXT;
import static io.ballerina.plugins.idea.BallerinaConstants.LAUNCHER_SCRIPT_PATH;
import static io.ballerina.plugins.idea.BallerinaConstants.SYS_PROP_EXPERIMENTAL;
import static io.ballerina.plugins.idea.BallerinaConstants.SYS_PROP_LS_DEBUG;
import static io.ballerina.plugins.idea.BallerinaConstants.SYS_PROP_LS_TRACE;
import static io.ballerina.plugins.idea.preloading.OSUtils.getOperatingSystem;

/**
 * Preloading Activity of ballerina plugin.
 */
public class BallerinaPreloadingActivity extends PreloadingActivity {

    private static final Logger LOG = Logger.getInstance(BallerinaPreloadingActivity.class);
    private static BallerinaAutoDetectNotifier autoDetectNotifier = new BallerinaAutoDetectNotifier();

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
     */
    private static void registerServerDefinition() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            registerServerDefinition(project);
        }
    }

    private static boolean registerServerDefinition(Project project) {

        boolean autoDetected = false;

        //If the project does not have a ballerina SDK attached, ballerinaSdkPath will be null.
        BallerinaSdk balSdk = BallerinaSdkUtils.getBallerinaSdkFor(project);
        String balSdkPath = balSdk.getSdkPath();

        // Checks for the user-configured auto detection settings.
        if (balSdkPath == null && BallerinaAutoDetectionSettings.getInstance(project).getIsAutoDetectionEnabled()) {

            //If a ballerina SDK is not configured for the project, Plugin tries to auto detect the ballerina SDK.
            showInIdeaEventLog(project, String.format("No ballerina SDK is found for project: %s\n " +
                    "Trying to Auto detect Ballerina Home...", project.getBasePath()));

            balSdkPath = BallerinaSdkUtils.autoDetectSdk(project);
            autoDetected = true;
        }

        if (!Strings.isNullOrEmpty(balSdkPath)) {
            boolean success = doRegister(project, balSdkPath);
            if (success && autoDetected) {
                LOG.info(String.format("Auto-detected Ballerina Home: %s for the project: %s", balSdkPath,
                        project.getBasePath()));
                showInIdeaEventLog(project, "Auto-Detected Ballerina Home: " + balSdkPath);
            }
            return success;
        } else {
            if (BallerinaAutoDetectionSettings.getInstance(project).getIsAutoDetectionEnabled()) {
                showInIdeaEventLog(project, "Auto-Detection Failed for: " + project.getBasePath());
            }
        }
        return false;
    }

    private static boolean doRegister(@NotNull Project project, @NotNull String sdkPath) {
        String os = OSUtils.getOperatingSystem();
        if (os == null) {
            return false;
        }

        // Creates the args list to register the language server definition using the ballerina lang-server launcher
        // script.
        ProcessBuilder processBuilder;
        List<String> args = new ArrayList<>();
        if (os.equals(OSUtils.UNIX) || os.equals(OSUtils.MAC)) {
            args.add(Paths.get(sdkPath, LAUNCHER_SCRIPT_PATH, "language-server-launcher.sh").toString());
        } else if (os.equals(OSUtils.WINDOWS)) {
            args.add(Paths.get(sdkPath, LAUNCHER_SCRIPT_PATH, "language-server-launcher.bat").toString());
        }

        processBuilder = new ProcessBuilder(args);
        // Checks user-configurable setting for allowing ballerina experimental features and sets the flag accordingly.
        if (BallerinaAutoDetectionSettings.getInstance(project).getIsAutoDetectionEnabled()) {
            processBuilder.environment().put(SYS_PROP_EXPERIMENTAL, "true");
        }

        // Checks user-configurable setting for allowing language server debug logs and sets the flag accordingly.
        if (BallerinaAutoDetectionSettings.getInstance(project).getIsAutoDetectionEnabled()) {
            processBuilder.environment().put(SYS_PROP_LS_DEBUG, "true");
        }

        // Checks user-configurable setting for allowing language server trace logs and sets the flag accordingly.
        if (BallerinaAutoDetectionSettings.getInstance(project).getIsAutoDetectionEnabled()) {
            processBuilder.environment().put(SYS_PROP_LS_TRACE, "true");
        }

        // Adds ballerina-specific custom LSP extensions by creating a ballerina lsp extension manager.
        IntellijLanguageClient.addExtensionManager(BAL_FILE_EXT, new BallerinaLSPExtensionManager());

        // Registers language server definition in the lsp4intellij lang-client library.
        IntellijLanguageClient
                .addServerDefinition(new ProcessBuilderServerDefinition(BAL_FILE_EXT, processBuilder), project);

        LOG.info("language server definition is registered using sdk path: " + sdkPath);
        return true;
    }

    private static void showInIdeaEventLog(Project project, String message) {
        ApplicationManager.getApplication().invokeLater(() -> autoDetectNotifier.showMessage(project, message,
                MessageType.INFO));
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
            LOG.error("Error occurred when trying to terminate ballerina processes", e);
        }
    }
}
