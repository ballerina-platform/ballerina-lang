/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.sdk;

import com.google.common.base.Strings;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.BallerinaConstants;
import io.ballerina.plugins.idea.configuration.BallerinaProjectSettings;
import io.ballerina.plugins.idea.preloading.BallerinaCmdException;
import io.ballerina.plugins.idea.preloading.OSUtils;
import io.ballerina.plugins.idea.project.BallerinaApplicationLibrariesService;
import io.ballerina.plugins.idea.project.BallerinaLibrariesService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.intellij.util.containers.ContainerUtil.newLinkedHashSet;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_CMD;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_COMPOSER_LIB_PATH;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_CONFIG_FILE_NAME;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_EXECUTABLE_NAME;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_EXEC_PATH;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_HOME_CMD;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_LS_LAUNCHER_NAME;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_LS_LAUNCHER_PATH;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_PLUGIN_ID;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_VERSION_PATTERN;
import static io.ballerina.plugins.idea.preloading.OSUtils.MAC;
import static io.ballerina.plugins.idea.preloading.OSUtils.UNIX;
import static io.ballerina.plugins.idea.preloading.OSUtils.WINDOWS;

/**
 * Contains util classes related to Ballerina SDK.
 */
public class BallerinaSdkUtils {

    private static final Logger LOG = Logger.getInstance(BallerinaSdkUtils.class);
    private static final Key<String> VERSION_DATA_KEY = Key.create("BALLERINA_VERSION_KEY");

    private static final String INSTALLER_PATH_UNIX = "/usr/bin/ballerina";
    private static final String INSTALLER_PATH_MAC = "/etc/paths.d/ballerina";
    private static final String HOMEBREW_PATH_MAC = "/usr/local/bin/ballerina";
    // Todo
    private static final String INSTALLER_PATH_WINDOWS = "C:\\Program Files\\Ballerina\\ballerina";

    private BallerinaSdkUtils() {

    }

    @Nullable
    public static VirtualFile suggestSdkDirectory() {
        if (SystemInfo.isWindows) {
            return ObjectUtils.chooseNotNull(LocalFileSystem.getInstance().findFileByPath("C:\\ballerina"), null);
        }
        if (SystemInfo.isMac || SystemInfo.isLinux) {
            String fromEnv = suggestSdkDirectoryPathFromEnv();
            if (fromEnv != null) {
                return LocalFileSystem.getInstance().findFileByPath(fromEnv);
            }
            VirtualFile usrLocal = LocalFileSystem.getInstance().findFileByPath("/usr/local/ballerina");
            if (usrLocal != null) {
                return usrLocal;
            }
        }
        if (SystemInfo.isMac) {
            String macPorts = "/opt/local/lib/ballerina";
            String homeBrew = "/usr/local/Cellar/ballerina";
            File file = FileUtil.findFirstThatExist(macPorts, homeBrew);
            if (file != null) {
                return LocalFileSystem.getInstance().findFileByIoFile(file);
            }
        }
        return null;
    }

    @Nullable
    private static String suggestSdkDirectoryPathFromEnv() {
        File fileFromPath = PathEnvironmentVariableUtil.findInPath("ballerina");
        if (fileFromPath != null) {
            File canonicalFile;
            try {
                canonicalFile = fileFromPath.getCanonicalFile();
                String path = canonicalFile.getPath();
                if (path.endsWith(BALLERINA_EXEC_PATH)) {
                    return StringUtil.trimEnd(path, BALLERINA_EXEC_PATH);
                }
            } catch (IOException ignore) {
            }
        }
        return null;
    }

    @Nullable
    public static String retrieveBallerinaVersion(@NotNull String sdkPath) {
        try {
            // need this hack to avoid an IDEA bug caused by "AssertionError: File accessed outside allowed roots"
            VfsRootAccess.allowRootAccess(sdkPath);

            VirtualFile sdkRoot = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(sdkPath));
            if (sdkRoot != null) {
                String cachedVersion = sdkRoot.getUserData(VERSION_DATA_KEY);
                if (cachedVersion != null) {
                    return !cachedVersion.isEmpty() ? cachedVersion : null;
                }

                VirtualFile versionFile = sdkRoot.findFileByRelativePath(
                        BallerinaConstants.BALLERINA_VERSION_FILE_PATH);
                if (versionFile == null) {
                    VfsRootAccess.allowRootAccess();
                    versionFile = sdkRoot.findFileByRelativePath(
                            BallerinaConstants.BALLERINA_NEW_VERSION_FILE_PATH);
                }
                // Please note that if the above versionFile is null, we can check on other locations as well.
                if (versionFile != null) {
                    String text = VfsUtilCore.loadText(versionFile);
                    String version = parseBallerinaVersion(text);
                    if (version == null) {
                        BallerinaSdkService.LOG.debug("Cannot retrieve Ballerina version from version file: " + text);
                    }
                    sdkRoot.putUserData(VERSION_DATA_KEY, StringUtil.notNullize(version));
                    return version;
                } else {
                    BallerinaSdkService.LOG.debug("Cannot find Ballerina version file in sdk path: " + sdkPath);
                }
            }
        } catch (Exception e) {
            BallerinaSdkService.LOG.debug("Cannot retrieve Ballerina version from sdk path: " + sdkPath, e);
        }
        return null;
    }

    @NotNull
    public static String getMajorVersion(@NotNull String version) {
        return version.contains(".") ? version.replace('-', '.').split("[.]")[0] : "";
    }

    @NotNull
    public static String getMinorVersion(@NotNull String version) {
        return version.contains(".") ? version.replace('-', '.').split("[.]")[1] : "";
    }

    @Nullable
    public static String getBallerinaPluginVersion() {
        IdeaPluginDescriptor balPluginDescriptor = PluginManager.getPlugin(PluginId.getId(BALLERINA_PLUGIN_ID));
        if (balPluginDescriptor != null) {
            return balPluginDescriptor.getVersion();
        }
        return null;
    }

    @NotNull
    public static String adjustSdkPath(@NotNull String path) {
        return path;
    }

    @Nullable
    public static String parseBallerinaVersion(@NotNull String text) {
        Matcher matcher = BALLERINA_VERSION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @NotNull
    public static Collection<VirtualFile> getSdkDirectoriesToAttach(@NotNull String sdkPath,
                                                                    @NotNull String versionString) {
        return ContainerUtil.createMaybeSingletonList(getSdkSrcDir(sdkPath, versionString));
    }

    @Nullable
    private static VirtualFile getSdkSrcDir(@NotNull String sdkPath, @NotNull String sdkVersion) {
        String srcPath = getSrcLocation(sdkVersion);
        VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(
                VfsUtilCore.pathToUrl(FileUtil.join(sdkPath, srcPath)));
        return file != null && file.isDirectory() ? file : null;
    }

    @NotNull
    public static BallerinaSdk getBallerinaSdkFor(Project project, Module module) {
        String sdkPath = BallerinaSdkService.getInstance(project).getSdkHomePath(module);
        if (sdkPath == null) {
            return new BallerinaSdk(null, false, false);
        }
        if (!isValidSdk(sdkPath)) {
            return new BallerinaSdk(null, false, false);
        }
        boolean langServerSupport = hasLangServerSupport(sdkPath);
        boolean webviewSupport = hasWebviewSupport(sdkPath);
        return new BallerinaSdk(sdkPath, langServerSupport, webviewSupport);
    }

    @NotNull
    public static BallerinaSdk getBallerinaSdkFor(Project project) {

        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();

        if (projectSdk == null) {
            return new BallerinaSdk(null, false, false);
        }
        String sdkPath = projectSdk.getHomePath();

        if (!isValidSdk(sdkPath)) {
            return new BallerinaSdk(null, false, false);
        }
        boolean langServerSupport = hasLangServerSupport(sdkPath);
        boolean webviewSupport = hasWebviewSupport(sdkPath);
        return new BallerinaSdk(sdkPath, langServerSupport, webviewSupport);
    }

    /**
     * @return an empty string if it fails to auto-detect the ballerina home automatically.
     */
    public static String autoDetectSdk(Project project) throws BallerinaCmdException {

        // Checks for the user-configured auto detection settings.
        if (!BallerinaProjectSettings.getStoredSettings(project).isAutodetect()) {
            return "";
        }

        String ballerinaPath = "";
        try {
            ballerinaPath = execBalHomeCmd(String.format("%s %s", BALLERINA_CMD, BALLERINA_HOME_CMD));
        } catch (BallerinaCmdException ignored) {
            // We don nothing here as we need to fallback for default installer specific locations, since "ballerina"
            // commands might not work because of the IntelliJ issue of PATH variable might not being identified by
            // the IntelliJ java runtime.
        }
        if (ballerinaPath.isEmpty()) {
            // Todo - Verify
            // Tries for default installer based locations since "ballerina" commands might not work
            // because of the IntelliJ issue of PATH variable might not being identified by the IntelliJ java
            // runtime.
            String routerScriptPath = getByDefaultPath();
            if (OSUtils.isWindows()) {
                ballerinaPath = execBalHomeCmd(String.format("\"%s\" %s", routerScriptPath, BALLERINA_HOME_CMD));
            } else {
                ballerinaPath = execBalHomeCmd(String.format("%s %s", routerScriptPath, BALLERINA_HOME_CMD));
            }
        }

        return ballerinaPath;
    }

    /**
     * Executes ballerina home command and returns the result.
     *
     * @param cmd command to be executed.
     * @return ballerina distribution location.
     */
    public static String execBalHomeCmd(String cmd) throws BallerinaCmdException {
        java.util.Scanner s;
        // This may returns a symlink which links to the real path.
        try {
            s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");

            String path = s.hasNext() ? s.next().trim().replace(System.lineSeparator(), "").trim() : "";
            LOG.info(String.format("%s command returned: %s", cmd, path));

            // Todo - verify
            // Since errors might be coming from the input stream when retrieving ballerina distribution path, we need
            // an additional check.
            if (!new File(path).exists()) {
                throw new BallerinaCmdException(String.format("unexpected output received by \"%s\" command. " +
                        "received output: %s", cmd, path));
            }
            return path;
        } catch (IOException e) {
            throw new BallerinaCmdException("Unexpected error occurred when executing ballerina command: " + cmd, e);
        }
    }

    /**
     * Tries for default installer based locations since "ballerina" commands might not work
     * because of the IntelliJ issue of PATH variable might not being identified by the IntelliJ java
     * runtime.
     */
    public static String getByDefaultPath() {
        try {
            String path = getDefaultPath();
            if (path.isEmpty()) {
                return path;
            }
            // Resolves actual path using "toRealPath()".
            return new File(path).toPath().toRealPath().toString();
        } catch (Exception e) {
            LOG.warn("Error occurred when resolving symlinks to auto detect ballerina home.");
            return "";
        }
    }

    private static String getDefaultPath() {
        String os = OSUtils.getOperatingSystem();
        switch (os) {
            case UNIX:
                return INSTALLER_PATH_UNIX;
            case MAC:
                return getMacDefaultPath();
            case WINDOWS:
                return getWinDefaultPath();
            default:
                return "";
        }
    }

    private static String getWinDefaultPath() {
        try {
            String pluginVersion = getBallerinaPluginVersion();
            if (pluginVersion == null) {
                return "";
            }
            String routerPath = String.join("-", INSTALLER_PATH_WINDOWS, pluginVersion);
            return Paths.get(routerPath, "bin", "ballerina.bat").toString();
        } catch (Exception e) {
            LOG.warn("Error occurred when trying to auto detect using default installation path.", e);
            return "";
        }
    }

    private static String getMacDefaultPath() {
        try {
            Stream<String> stream = Files.lines(Paths.get(INSTALLER_PATH_MAC), StandardCharsets.UTF_8);
            StringBuilder contentBuilder = new StringBuilder();
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
            String balHomePath = contentBuilder.toString().trim();
            balHomePath = !Strings.isNullOrEmpty(balHomePath) && balHomePath.endsWith("/bin") ?
                    balHomePath.replace("/bin", "/bin/ballerina") : "";

            // If a ballerina router script does not exist in this directory, falls-back to homebrew specific symlinks.
            if (balHomePath.isEmpty() || !new File(balHomePath).exists()) {
                return HOMEBREW_PATH_MAC;
            }
            return balHomePath;
        } catch (Exception ignored) {
            return "";
        }
    }

    private static boolean isValidSdk(String sdkPath) {

        if (Strings.isNullOrEmpty(sdkPath)) {
            return false;
        }
        // Checks for either shell scripts or batch files, since the shell script recognition error in windows.
        String balShellScript = Paths.get(sdkPath, "bin", BALLERINA_EXECUTABLE_NAME).toString();
        String balBatchScript = Paths.get(sdkPath, "bin", BALLERINA_EXECUTABLE_NAME).toString() + ".bat";

        return (new File(balShellScript).exists() || new File(balBatchScript).exists());
    }

    private static boolean hasLangServerSupport(String sdkPath) {

        if (Strings.isNullOrEmpty(sdkPath)) {
            return false;
        }
        // Checks for either shell scripts or batch files, since the shell script recognition error in windows.
        String launcherShellScript = Paths.get(sdkPath, BALLERINA_LS_LAUNCHER_PATH,
                BALLERINA_LS_LAUNCHER_NAME).toString() + ".sh";
        String launcherBatchScript = Paths.get(sdkPath, BALLERINA_LS_LAUNCHER_PATH,
                BALLERINA_LS_LAUNCHER_NAME).toString() + ".bat";

        return new File(launcherShellScript).exists() || new File(launcherBatchScript).exists();
    }

    private static boolean hasWebviewSupport(String sdkPath) {

        if (Strings.isNullOrEmpty(sdkPath)) {
            return false;
        }
        // Checks for composer library resource directory existence.
        String composerLib = Paths.get(sdkPath, BALLERINA_COMPOSER_LIB_PATH).toString();
        return new File(composerLib).exists();
    }

    public static LinkedHashSet<VirtualFile> getSourcesPathsToLookup(@NotNull Project project, @Nullable Module
            module) {
        LinkedHashSet<VirtualFile> sdkAndBallerinaPath = newLinkedHashSet();
        ContainerUtil.addIfNotNull(sdkAndBallerinaPath, getSdkSrcDir(project, module));
        // Todo  - add Ballerina Path
        // ContainerUtil.addAllNotNull(sdkAndBallerinaPath, getBallerinaPathSources(project, module));
        return sdkAndBallerinaPath;
    }

    @NotNull
    private static String getSrcLocation(@NotNull String version) {
        return "src";
    }

    public static String getSdkHome(Project project, Module module) {
        // Get the module SDK.
        Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
        // If the SDK is Ballerina SDK, return the home path.
        if (moduleSdk != null && moduleSdk.getSdkType() == BallerinaSdkType.getInstance()) {
            return moduleSdk.getHomePath();
        }
        // Ge the project SDK.
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        // If the SDK is Ballerina SDK, return the home path.
        if (projectSdk != null && projectSdk.getSdkType() == BallerinaSdkType.getInstance()) {
            return projectSdk.getHomePath();
        }
        return "";
    }

    @NotNull
    public static Collection<VirtualFile> getBallerinaPathsRootsFromEnvironment() {

        return BallerinaPathModificationTracker.getBallerinaEnvironmentPathRoots();
    }

    @NotNull
    private static List<VirtualFile> getInnerBallerinaPathSources(@NotNull Project project, @Nullable Module module) {
        return ContainerUtil.mapNotNull(getBallerinaPathRoots(project, module), new
                RetrieveSubDirectoryOrSelfFunction("src"));
    }

    @NotNull
    public static Collection<VirtualFile> getBallerinaPathRoots(@NotNull Project project, @Nullable Module module) {
        Collection<VirtualFile> roots = ContainerUtil.newArrayList();
        if (BallerinaApplicationLibrariesService.getInstance().isUseBallerinaPathFromSystemEnvironment()) {
            roots.addAll(getBallerinaPathsRootsFromEnvironment());
        }
        roots.addAll(module != null ? BallerinaLibrariesService.getUserDefinedLibraries(module) :
                BallerinaLibrariesService.getUserDefinedLibraries(project));
        return roots;
    }

    private static class RetrieveSubDirectoryOrSelfFunction implements Function<VirtualFile, VirtualFile> {
        @NotNull
        private final String mySubdirName;

        public RetrieveSubDirectoryOrSelfFunction(@NotNull String subdirName) {
            mySubdirName = subdirName;
        }

        @Override
        public VirtualFile fun(VirtualFile file) {
            return file == null || FileUtil.namesEqual(mySubdirName, file.getName()) ? file : file.findChild
                    (mySubdirName);
        }
    }

    @NotNull
    public static Collection<Module> getBallerinaModules(@NotNull Project project) {
        if (project.isDefault()) {
            return Collections.emptyList();
        }
        BallerinaSdkService sdkService = BallerinaSdkService.getInstance(project);
        return ContainerUtil.filter(ModuleManager.getInstance(project).getModules(), sdkService::isBallerinaModule);
    }

    @Nullable
    public static VirtualFile getSdkSrcDir(@NotNull Project project, @Nullable Module module) {
        if (module != null) {
            return CachedValuesManager.getManager(project).getCachedValue(module, () -> {
                BallerinaSdkService sdkService = BallerinaSdkService.getInstance(module.getProject());
                return CachedValueProvider.Result.create(getInnerSdkSrcDir(sdkService, module), sdkService);
            });
        }
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            BallerinaSdkService sdkService = BallerinaSdkService.getInstance(project);
            return CachedValueProvider.Result.create(getInnerSdkSrcDir(sdkService, null), sdkService);
        });
    }

    /**
     * Searches for a ballerina project root using outward recursion starting from the file directory, until the given
     * root directory is found. Returns and empty string if unable to detect any ballerina project under the current
     * intellij project source root.
     */
    public static String searchForBallerinaProjectRoot(String currentPath, String root) {

        if (currentPath.isEmpty() || root.isEmpty()) {
            return "";
        }

        File currentDir = new File(currentPath);
        File[] files = currentDir.listFiles();
        if (files != null) {
            for (File f : files) {
                // Searches for the ballerina config file (Ballerina.toml).
                if (f.isFile() && f.getName().equals(BALLERINA_CONFIG_FILE_NAME)) {
                    return currentDir.getAbsolutePath();
                }
            }
        }

        if (currentPath.equals(root) || currentDir.getParentFile() == null) {
            return "";
        }
        return searchForBallerinaProjectRoot(currentDir.getParentFile().getAbsolutePath(), root);
    }

    @Messages.YesNoResult
    public static void showRestartDialog(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String action = project != null && ProjectManagerEx.getInstanceEx().canClose(project) ?
                    "Reload Project" : "Restart IDE";
            String message = "Project/IDE reloading action is required to apply changes. Do you wish to continue?";
            if (Messages.showYesNoDialog(message, "Apply Changes", action, "Postpone",
                    Messages.getQuestionIcon()) == Messages.YES) {
                if (action.equals("Reload Project")) {
                    ProjectManagerEx.getInstanceEx().reloadProject(project);
                } else {
                    ApplicationManagerEx.getApplicationEx().restart(true);
                }
            }
        });
    }

    @Nullable
    private static VirtualFile getInnerSdkSrcDir(@NotNull BallerinaSdkService sdkService, @Nullable Module module) {
        String sdkHomePath = sdkService.getSdkHomePath(module);
        String sdkVersionString = sdkService.getSdkVersion(module);
        return sdkHomePath != null && sdkVersionString != null ? getSdkSrcDir(sdkHomePath, sdkVersionString) : null;
    }
}
