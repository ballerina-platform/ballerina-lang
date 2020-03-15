/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Pair;
import io.ballerina.plugins.idea.extensions.BallerinaLSPExtensionManager;
import io.ballerina.plugins.idea.notifiers.BallerinaAutoDetectNotifier;
import io.ballerina.plugins.idea.sdk.BallerinaSdk;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import io.ballerina.plugins.idea.settings.autodetect.BallerinaAutoDetectionSettings;
import io.ballerina.plugins.idea.settings.experimental.BallerinaExperimentalFeatureSettings;
import io.ballerina.plugins.idea.settings.langserverlogs.LangServerLogsSettings;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.ProcessBuilderServerDefinition;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_CMD;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_HOME_CMD;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_LS_CMD;
import static io.ballerina.plugins.idea.BallerinaConstants.BAL_FILE_EXT;
import static io.ballerina.plugins.idea.BallerinaConstants.ENV_DEBUG_LOG;
import static io.ballerina.plugins.idea.BallerinaConstants.ENV_DEF_STDLIBS;
import static io.ballerina.plugins.idea.BallerinaConstants.ENV_EXPERIMENTAL;
import static io.ballerina.plugins.idea.BallerinaConstants.ENV_TRACE_LOG;
import static io.ballerina.plugins.idea.BallerinaConstants.LAUNCHER_SCRIPT_PATH;
import static io.ballerina.plugins.idea.BallerinaConstants.SYS_PROP_EXPERIMENTAL;
import static io.ballerina.plugins.idea.BallerinaConstants.SYS_PROP_LS_DEBUG;
import static io.ballerina.plugins.idea.BallerinaConstants.SYS_PROP_LS_TRACE;
import static io.ballerina.plugins.idea.sdk.BallerinaSdkService.getBallerinaExecutablePath;
import static io.ballerina.plugins.idea.sdk.BallerinaSdkUtils.execBalHomeCmd;
import static io.ballerina.plugins.idea.sdk.BallerinaSdkUtils.getMajorVersion;
import static io.ballerina.plugins.idea.sdk.BallerinaSdkUtils.getMinorVersion;

/**
 * Language server protocol related utils.
 *
 * @since 1.1.4
 */
public class LSPUtils {

    private static BallerinaAutoDetectNotifier autoDetectNotifier = new BallerinaAutoDetectNotifier();
    private static final Logger LOG = Logger.getInstance(LSPUtils.class);

    /**
     * Notifies plugin settings changes to the ballerina language server.
     *
     * @param project project instance related to the plugin settings change(s).
     * @return true if the config change notification is sent successfully, or false otherwise.
     */
    public static boolean notifyConfigChanges(Project project) {

        Pair<String, Boolean> balSdk;
        try {
            balSdk = getOrDetectBalSdkHome(project);
        } catch (BallerinaCmdException e) {
            LOG.warn("unexpected error occurred when notifying plugin config changes to language server.", e);
            return false;
        }

        String balSdkPath = balSdk.first;
        String version = BallerinaSdkUtils.retrieveBallerinaVersion(balSdkPath);
        if (version == null) {
            LOG.warn("unable to retrieve ballerina version from sdk path: " + balSdkPath);
            return false;
        }

        if (!hasDidChangeConfigSupport(version)) {
            LOG.warn("on-the-fly config changes are not supported in ballerina: " + version);
            return false;
        }

        LSClientConfig clientConfig = new LSClientConfig();
        if (BallerinaExperimentalFeatureSettings.getInstance(project).isAllowedExperimental()) {
            clientConfig.setAllowExperimental(true);
            clientConfig.setGoToDefStdLibs(true);
        }
        // Checks user-configurable setting for allowing language server debug logs and sets the flag accordingly.
        if (LangServerLogsSettings.getInstance(project).isLangServerDebugLogsEnabled()) {
            clientConfig.setDebugLog(true);
        }
        // Checks user-configurable setting for allowing language server trace logs and sets the flag accordingly.
        if (LangServerLogsSettings.getInstance(project).isLangServerTraceLogsEnabled()) {
            clientConfig.setTraceLog(true);
        }

        IntellijLanguageClient.didChangeConfiguration(new DidChangeConfigurationParams(clientConfig), project);
        return true;
    }

    /**
     * Registered language server definition using currently opened ballerina projects.
     */
    static void registerServerDefinition() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            registerServerDefinition(project);
        }
    }

    static boolean registerServerDefinition(Project project) {

        Pair<String, Boolean> balSdk;
        try {
            balSdk = getOrDetectBalSdkHome(project);

            String balSdkPath = balSdk.first;
            boolean autoDetected = balSdk.second;

            if (!Strings.isNullOrEmpty(balSdkPath)) {
                boolean success = doRegister(project, balSdkPath, autoDetected);
                if (success && autoDetected) {
                    BallerinaPreloadingActivity.LOG.info(String.format("Auto-detected Ballerina Home: %s for the " +
                            "project: %s", balSdkPath, project.getBasePath()));
                    showInIdeaEventLog(project, "Auto-Detected Ballerina Home: " + balSdkPath);
                }
                return success;
            } else if (BallerinaAutoDetectionSettings.getInstance(project).isAutoDetectionEnabled()) {
                showInIdeaEventLog(project, "Auto-Detection Failed for: " + project.getBasePath());
            }
            return false;
        } catch (BallerinaCmdException e) {
            showInIdeaEventLog(project, String.format("Auto-Detection Failed for project: %s, due to: %s",
                    project.getBasePath(), e.getMessage()));
            return false;
        }
    }

    private static boolean doRegister(@NotNull Project project, @NotNull String sdkPath, boolean autoDetected) {

        ProcessBuilder processBuilder = getLangServerProcessBuilder(project, sdkPath, autoDetected);
        if (processBuilder == null || project.getBasePath() == null) {
            return false;
        }
        processBuilder.directory(new File(project.getBasePath()));

        // Adds ballerina-specific custom LSP extensions by creating a ballerina lsp extension manager.
        IntellijLanguageClient.addExtensionManager(BAL_FILE_EXT, new BallerinaLSPExtensionManager());
        // Registers language server definition in the lsp4intellij lang-client library.
        IntellijLanguageClient.addServerDefinition(new ProcessBuilderServerDefinition(BAL_FILE_EXT, processBuilder),
                project);
        BallerinaPreloadingActivity.LOG.info("language server definition is registered using sdk path: " + sdkPath);
        return true;
    }

    /**
     * Returns ballerina sdk location for a given project. First checks for a user-configured ballerina SDK for the
     * project if nothing found, tries to auto detect the active ballerina distribution location.
     *
     * @param project Project instance.
     * @return SDK location path string and a flag which indicates whether the location is auto detected.
     */
    public static Pair<String, Boolean> getOrDetectBalSdkHome(Project project) throws BallerinaCmdException {

        //If the project does not have a ballerina SDK attached, ballerinaSdkPath will be null.
        BallerinaSdk balSdk = BallerinaSdkUtils.getBallerinaSdkFor(project);
        String balSdkPath = balSdk.getSdkPath();

        if (balSdkPath != null) {
            return new Pair<>(balSdkPath, false);
        } else if (BallerinaAutoDetectionSettings.getInstance(project).isAutoDetectionEnabled()) {
            showInIdeaEventLog(project, String.format("No ballerina SDK is found for project: %s\n " +
                    "Trying to Auto detect Ballerina Home...", project.getBasePath()));
            // If a ballerina SDK is not configured for the project, Plugin tries to auto detect the ballerina SDK.
            balSdkPath = BallerinaSdkUtils.autoDetectSdk(project);
            return new Pair<>(balSdkPath, true);
        } else {
            return new Pair<>(null, false);
        }
    }

    @Nullable
    private static ProcessBuilder getLangServerProcessBuilder(Project project, String sdkPath, boolean autoDetected) {

        String version = BallerinaSdkUtils.retrieveBallerinaVersion(sdkPath);
        if (version == null) {
            LOG.warn("unable to retrieve ballerina version from sdk path: " + sdkPath);
            return null;
        }

        return hasLangServerCmdSupport(version) ? createCmdBasedProcess(project, sdkPath, autoDetected) :
                createScriptBasedProcess(project, sdkPath);
    }

    /**
     * Creates a process builder instance based on language server launcher scripts, which are used in ballerina
     * v1.2.0 and earlier.
     */
    @Nullable
    private static ProcessBuilder createScriptBasedProcess(Project project, String sdkPath) {

        String os = OSUtils.getOperatingSystem();
        if (os == null) {
            return null;
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
        if (BallerinaExperimentalFeatureSettings.getInstance(project).isAllowedExperimental()) {
            processBuilder.environment().put(SYS_PROP_EXPERIMENTAL, "true");
        }

        // Checks user-configurable setting for allowing language server debug logs and sets the flag accordingly.
        if (LangServerLogsSettings.getInstance(project).isLangServerDebugLogsEnabled()) {
            processBuilder.environment().put(SYS_PROP_LS_DEBUG, "true");
        }

        // Checks user-configurable setting for allowing language server trace logs and sets the flag accordingly.
        if (LangServerLogsSettings.getInstance(project).isLangServerTraceLogsEnabled()) {
            processBuilder.environment().put(SYS_PROP_LS_TRACE, "true");
        }

        return processBuilder;
    }

    /**
     * Creates a process builder instance based on language server CLI command, introduced with ballerina
     * v1.2.0.
     */
    @Nullable
    private static ProcessBuilder createCmdBasedProcess(Project project, String balSdkPath, boolean autoDetected) {

        // Creates the args list to register the language server definition using the ballerina lang-server launcher
        // command.
        List<String> args = getLangServerCmdArgs(project, balSdkPath, autoDetected);
        if (args == null || args.isEmpty()) {
            LOG.warn("Couldn't find ballerina executable to execute language server launch command.");
            return null;
        }

        ProcessBuilder cmdProcessBuilder = new ProcessBuilder(args);
        // Checks user-configurable setting for allowing ballerina experimental features and sets the flag accordingly.
        if (BallerinaExperimentalFeatureSettings.getInstance(project).isAllowedExperimental()) {
            cmdProcessBuilder.environment().put(ENV_EXPERIMENTAL, "true");
            cmdProcessBuilder.environment().put(ENV_DEF_STDLIBS, "true");
        }

        // Checks user-configurable setting for allowing language server debug logs and sets the flag accordingly.
        if (LangServerLogsSettings.getInstance(project).isLangServerDebugLogsEnabled()) {
            cmdProcessBuilder.environment().put(ENV_DEBUG_LOG, "true");
        }

        // Checks user-configurable setting for allowing language server trace logs and sets the flag accordingly.
        if (LangServerLogsSettings.getInstance(project).isLangServerTraceLogsEnabled()) {
            cmdProcessBuilder.environment().put(ENV_TRACE_LOG, "true");
        }

        return cmdProcessBuilder;
    }

    @Nullable
    private static List<String> getLangServerCmdArgs(Project project, String balSdkPath, boolean autoDetected) {

        List<String> cmdArgs = new ArrayList<>();
        try {
            // If the user has configured an SDK.
            if (!autoDetected) {
                String balExecPath = getBallerinaExecutablePath(balSdkPath);
                String homeCmd = OSUtils.isWindows() ? String.format("\"%s\" %s", balExecPath, BALLERINA_HOME_CMD) :
                        String.format("%s %s", balExecPath, BALLERINA_HOME_CMD);
                String ballerinaPath = execBalHomeCmd(homeCmd);
                if (!ballerinaPath.isEmpty()) {
                    cmdArgs.add(balExecPath);
                    cmdArgs.add(BALLERINA_LS_CMD);
                    return cmdArgs;
                }
            } else {
                // Checks if the ballerina command works.
                String ballerinaPath = execBalHomeCmd(String.format("%s %s", BALLERINA_CMD, BALLERINA_HOME_CMD));
                if (!ballerinaPath.isEmpty()) {
                    cmdArgs.add(BALLERINA_CMD);
                    cmdArgs.add(BALLERINA_LS_CMD);
                    return cmdArgs;
                }
                // Todo - Verify
                // Tries for default installer based locations since "ballerina" commands might not work
                // because of the IntelliJ issue of PATH variable might not being identified by the IntelliJ java
                // runtime.
                String routerScriptPath = BallerinaSdkUtils.getByDefaultPath();
                if (routerScriptPath.isEmpty()) {
                    // Returns the empty list.
                    return cmdArgs;
                }
                cmdArgs.add(OSUtils.isWindows() ? String.format("\"%s\"", routerScriptPath) : routerScriptPath);
            }

            cmdArgs.add(BALLERINA_LS_CMD);
            return cmdArgs;
        } catch (BallerinaCmdException e) {
            showInIdeaEventLog(project, String.format("Failed to start language server for project: %s, due to: %s",
                    project.getBasePath(), e.getMessage()));
            return null;
        }
    }

    private static boolean hasLangServerCmdSupport(String balVersion) {
        int majorV = Integer.parseInt(getMajorVersion(balVersion));
        int minorV = Integer.parseInt(getMinorVersion(balVersion));

        // returns true if the ballerina version >= 1.2.0.
        return majorV == 1 && minorV >= 2;
    }

    public static boolean hasDidChangeConfigSupport(String balVersion) {
        int majorV = Integer.parseInt(getMajorVersion(balVersion));
        int minorV = Integer.parseInt(getMinorVersion(balVersion));

        // returns true if the ballerina version >= 1.2.0.
        return majorV == 1 && minorV >= 2;
    }

    private static void showInIdeaEventLog(@NotNull Project project, String message) {
        ApplicationManager.getApplication().invokeLater(() -> autoDetectNotifier.showMessage(project, message,
                MessageType.INFO));
    }
}
