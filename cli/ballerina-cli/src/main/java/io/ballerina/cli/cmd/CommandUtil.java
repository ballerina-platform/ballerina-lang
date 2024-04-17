/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.cmd;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.internal.bala.BalToolJson;
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.ModuleDependency;
import io.ballerina.projects.internal.bala.PackageJson;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOL_JSON;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOL_TOML;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCY_GRAPH_JSON;
import static io.ballerina.projects.util.ProjectConstants.LIB_DIR;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;
import static io.ballerina.projects.util.ProjectConstants.TOOL_DIR;
import static io.ballerina.projects.util.ProjectUtils.deleteDirectory;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.guessPkgName;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.write;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.ANY_PLATFORM;
import static org.wso2.ballerinalang.util.RepoUtils.readSettings;

/**
 * Packerina command util.
 *
 * @since 2.0.0
 */
public class CommandUtil {
    public static final String ORG_NAME = "ORG_NAME";
    public static final String PKG_NAME = "PKG_NAME";
    public static final String DIST_VERSION = "DIST_VERSION";
    public static final String TOOL_ID = "TOOL_ID";
    public static final String USER_HOME = "user.home";
    public static final String GITIGNORE = "gitignore";
    public static final String DEVCONTAINER = "devcontainer";
    public static final String NEW_CMD_DEFAULTS = "new_cmd_defaults";
    public static final String CREATE_CMD_TEMPLATES = "create_cmd_templates";
    public static final String LIBS_DIR = "libs";
    public static final String DEFAULT_TEMPLATE = "default";
    public static final String MAIN_TEMPLATE = "main";
    public static final String FILE_STRING_SEPARATOR = ", ";
    private static FileSystem jarFs;
    private static Map<String, String> env;
    private static PrintStream errStream;
    private static PrintStream outStream;
    private static Path homeCache;
    private static boolean exitWhenFinish;
    private static String platform;

    static void setPrintStream(PrintStream errStream) {
        CommandUtil.errStream = errStream;
    }

    public static void initJarFs() {
        URI uri = null;
        try {
            uri = CommandUtil.class.getClassLoader().getResource(CREATE_CMD_TEMPLATES).toURI();
            if (uri.toString().contains("!")) {
                final String[] array = uri.toString().split("!");
                if (null == jarFs) {
                    env = new HashMap<>();
                    jarFs = FileSystems.newFileSystem(URI.create(array[0]), env);
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new AssertionError();
        }
    }

    /**
     * Print command errors with a standard format.
     *
     * @param stream error will be sent to this stream
     * @param error error message
     * @param usage usage if any
     * @param help if the help message should be printed
     */
    public static void printError(PrintStream stream, String error, String usage, boolean help) {
        stream.println("ballerina: " + error);

        if (null != usage) {
            stream.println();
            stream.println("USAGE:");
            stream.println("    " + usage);
        }

        if (help) {
            stream.println();
            stream.println("For more information try --help");
        }
    }
    
    /**
     * Exit with error code 1.
     *
     * @param exit Whether to exit or not.
     */
    public static void exitError(boolean exit) {
        if (exit) {
            Runtime.getRuntime().exit(1);
        }
    }


    static void applyTemplate(String orgName, String templatePkgName, String version, String packageName,
                              Path projectPath, Path balaCache, List<Path> filesInDir) {
        Path balaPath = getPlatformSpecificBalaPath(orgName, templatePkgName, version, balaCache);
        if (!Files.exists(balaPath)) {
            CommandUtil.printError(errStream,
                    "unable to find the bala: " + balaPath,
                    null,
                    false);
            CommandUtil.exitError(exitWhenFinish);
        }
        try {
            addModules(balaPath, projectPath, packageName);
        } catch (IOException e) {
            ProjectUtils.deleteSelectedFilesInDirectory(projectPath, filesInDir);
            CommandUtil.printError(errStream,
                    "error occurred while creating the package: " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(exitWhenFinish);
        }
    }

    private static void addModules(Path balaPath, Path projectPath, String packageName)
            throws IOException {
        Gson gson = new Gson();
        Path packageJsonPath = balaPath.resolve(PACKAGE_JSON);
        Path dependencyGraphJsonPath = balaPath.resolve(DEPENDENCY_GRAPH_JSON);
        Path balToolJsonPath = balaPath.resolve(TOOL_DIR).resolve(ProjectConstants.BAL_TOOL_JSON);
        PackageJson templatePackageJson = null;
        DependencyGraphJson templateDependencyGraphJson = null;
        BalToolJson templateBalToolJson = null;

        try (InputStream inputStream = new FileInputStream(String.valueOf(packageJsonPath))) {
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            templatePackageJson = gson.fromJson(fileReader, PackageJson.class);
        } catch (IOException e) {
            printError(errStream,
                    "Error while reading the package json file: " + e.getMessage(),
                    null,
                    false);
            getRuntime().exit(1);
        }

        if (dependencyGraphJsonPath.toFile().exists()) {
            try (InputStream inputStream = new FileInputStream(String.valueOf(dependencyGraphJsonPath))) {
                Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                templateDependencyGraphJson = gson.fromJson(fileReader, DependencyGraphJson.class);
            } catch (IOException e) {
                printError(errStream,
                        "Error while reading the dependency graph json file: " + e.getMessage(),
                        null,
                        false);
                getRuntime().exit(1);
            }
        }

        if (balToolJsonPath.toFile().exists()) {
            try (InputStream inputStream = new FileInputStream(String.valueOf(balToolJsonPath))) {
                Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                templateBalToolJson = gson.fromJson(fileReader, BalToolJson.class);
            } catch (IOException e) {
                printError(errStream,
                        "Error while reading the " + BAL_TOOL_JSON + " file: " + e.getMessage(),
                        null,
                        false);
                getRuntime().exit(1);
            }
        }

        if (!templatePackageJson.getTemplate()) {
            throw createLauncherException("unable to create the package: " +
                    "specified package is not a template");
        }

        // Create Ballerina.toml
        Path ballerinaToml = projectPath.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createDirectories(projectPath);
        Files.createFile(ballerinaToml);
        writeBallerinaToml(ballerinaToml, templatePackageJson, packageName, platform);

        if (dependencyGraphJsonPath.toFile().exists()) {
            // Create Dependencies.toml
            Path dependenciesToml = projectPath.resolve(DEPENDENCIES_TOML);
            Files.createFile(dependenciesToml);
            writeDependenciesToml(projectPath, templateDependencyGraphJson, templatePackageJson);
        }

        if (balToolJsonPath.toFile().exists()) {
            // Create BalTool.toml and copy dependency jars
            Path balToolToml = projectPath.resolve(BAL_TOOL_TOML);
            Files.createFile(balToolToml);
            writeBalToolToml(balToolToml, templateBalToolJson, packageName);
            copyToolDependencies(projectPath, balaPath.resolve(TOOL_DIR).resolve(LIBS_DIR));
        }

        // Create Package.md
        Path packageMDFilePath = balaPath.resolve("docs")
                .resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
        Path toPackageMdPath = projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
        if (Files.exists(packageMDFilePath)) {
            Files.copy(packageMDFilePath, toPackageMdPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Create default .gitignore
        createDefaultGitignore(projectPath);
        // Create default devcontainer.json
        createDefaultDevContainer(projectPath);

        // Create modules
        String templatePkgName = templatePackageJson.getName();
        Path modulesRoot = balaPath.resolve(ProjectConstants.MODULES_ROOT);
        Path moduleMdDirRoot = balaPath.resolve("docs").resolve(ProjectConstants.MODULES_ROOT);
        List<Path> modulesList;
        try (Stream<Path> pathStream = Files.list(modulesRoot)) {
            modulesList = pathStream.collect(Collectors.toList());
        }
        for (Path moduleRoot : modulesList) {
            Path moduleDir = Optional.of(moduleRoot.getFileName()).get();
            Path destDir;
            if (moduleDir.toString().equals(templatePkgName)) {
                destDir = projectPath;
            } else {
                String moduleDirName = moduleDir.toString().split(templatePkgName + ProjectConstants.DOT, 2)[1];
                destDir = projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleDirName);
                Files.createDirectories(destDir);
            }
            Files.walkFileTree(moduleRoot, new FileUtils.Copy(moduleRoot, destDir, templatePkgName, packageName));

            // Copy Module.md
            Path moduleMdSource = moduleMdDirRoot.resolve(moduleDir).resolve(ProjectConstants.MODULE_MD_FILE_NAME);
            if (Files.exists(moduleMdSource)) {
                Files.copy(moduleMdSource, destDir.resolve(ProjectConstants.MODULE_MD_FILE_NAME),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }

        copyIcon(balaPath, projectPath);
        copyPlatformLibraries(balaPath, projectPath);
        copyIncludeFiles(balaPath, projectPath, templatePackageJson);
    }

    private static void copyIcon(Path balaPath, Path projectPath) {
        Path docsPath = balaPath.resolve(ProjectConstants.BALA_DOCS_DIR);
        try (Stream<Path> pathStream = Files.walk(docsPath, 1)) {
            List<Path> icon = pathStream
                    .filter(FileSystems.getDefault().getPathMatcher("glob:**.png")::matches)
                    .collect(Collectors.toList());
            if (!icon.isEmpty()) {
                Path projectDocsDir = projectPath.resolve(ProjectConstants.BALA_DOCS_DIR);
                Files.createDirectory(projectDocsDir);
                Path projectIconPath = projectDocsDir.resolve(Optional.of(icon.get(0).getFileName()).get());
                Files.copy(icon.get(0), projectIconPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            printError(errStream,
                    "Error while retrieving the icon: " + e.getMessage(),
                    null,
                    false);
            getRuntime().exit(1);
        }
    }

    private static void copyPlatformLibraries(Path balaPath, Path projectPath) throws IOException {
        Path platformLibPath = balaPath.resolve("platform").resolve(platform);
        if (Files.exists(platformLibPath)) {
            Path libs = projectPath.resolve("libs");
            Files.createDirectories(libs);
            Files.walkFileTree(platformLibPath, new FileUtils.Copy(platformLibPath, libs));
        }
    }

    private static void copyIncludeFiles(Path balaPath, Path projectPath, PackageJson templatePackageJson)
            throws IOException {
        if (templatePackageJson.getInclude() != null) {
            String templatePkgName = templatePackageJson.getName();
            List<Path> includePaths = ProjectUtils.getPathsMatchingIncludePatterns(
                    templatePackageJson.getInclude(), balaPath);

            for (Path includePath : includePaths) {
                Path moduleNameUpdatedIncludePath = updateModuleDirectoryNaming(includePath, balaPath, templatePkgName);
                Path fromIncludeFilePath = balaPath.resolve(includePath);
                Path toIncludeFilePath = projectPath.resolve(moduleNameUpdatedIncludePath);
                if (Files.notExists(toIncludeFilePath)) {
                    Files.createDirectories(toIncludeFilePath);
                    Files.walkFileTree(fromIncludeFilePath, new FileUtils.Copy(fromIncludeFilePath, toIncludeFilePath));
                }
            }
        }
    }

    private static Path updateModuleDirectoryNaming(Path includePath, Path balaPath, String templatePkgName) {
        Path modulesDirPath = balaPath.resolve(ProjectConstants.MODULES_ROOT);
        Path absoluteIncludePath = balaPath.resolve(includePath);
        if (absoluteIncludePath.startsWith(modulesDirPath)) {
            Path moduleRootPath = modulesDirPath.relativize(absoluteIncludePath).subpath(0, 1);
            String moduleDirName = Optional.of(moduleRootPath.getFileName()).get().toString();
            String destinationDirName = moduleDirName.split(templatePkgName + ProjectConstants.DOT, 2)[1];
            Path includePathRelativeToModuleRoot = modulesDirPath.resolve(moduleRootPath)
                    .relativize(absoluteIncludePath);
            Path updatedIncludePath = Paths.get(ProjectConstants.MODULES_ROOT).resolve(destinationDirName)
                    .resolve(includePathRelativeToModuleRoot);
            return updatedIncludePath;
        }
        return includePath;
    }

    /**
     * Find the bala path for a given template.
     *
     * @param template template name
     */
    static Path findBalaTemplate(String template, Path balaCache) {
        String packageName = findPkgName(template);
        String orgName = findOrg(template);
        String version = findPkgVersion(template);
        if (version != null) {
            Path balaPath = getPlatformSpecificBalaPath(orgName, packageName, version, balaCache);
            if (Files.exists(balaPath)) {
                return balaPath;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void initPackageFromCentral(Path balaCache, Path projectPath, String packageName, String template,
                                              List<Path> filesInDir) {
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
        String templatePackageName = findPkgName(template);
        String orgName = findOrg(template);
        String version = findPkgVersion(template);

        Path pkgCacheParent = balaCache.resolve(orgName).resolve(templatePackageName);
        try {
            pullPackageFromRemote(orgName, templatePackageName, version, pkgCacheParent);
        } catch (PackageAlreadyExistsException e) {
            if (version == null) {
                List<PackageVersion> packageVersions = getPackageVersions(pkgCacheParent);
                PackageVersion latest = findLatest(packageVersions);
                if (latest == null) {
                    // This is not supposed to execute
                    throw createLauncherException("unable to find package in the filesystem cache." +
                            " This is an unexpected error : " + e.getMessage());
                }
                version = latest.toString();
            }
        } catch (CentralClientException e) {
            errStream.println("Warning: Unable to pull the package from Ballerina Central: " + e.getMessage());
            if (findBalaTemplate(template, balaCache) == null) {
                List<PackageVersion> packageVersions = getPackageVersions(pkgCacheParent);
                PackageVersion latest = findLatest(packageVersions);
                if (latest == null) {
                    throw createLauncherException("template not found in filesystem cache.");
                }
                version = latest.toString();
            }
        }
        if (version == null) {
            List<PackageVersion> packageVersions = getPackageVersions(pkgCacheParent);
            PackageVersion latest = findLatest(packageVersions);
            version = Objects.requireNonNull(latest).toString();
        }
        applyTemplate(orgName, templatePackageName, version, packageName, projectPath, balaCache, filesInDir);
    }

    private static void pullPackageFromRemote(String orgName, String packageName, String version, Path destination)
            throws CentralClientException {
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(target -> target.code())
                .collect(Collectors.joining(","));
            Settings settings;
        try {
            settings = readSettings();
            // Ignore Settings.toml diagnostics in the pull command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(),
                getAccessTokenOfCLI(settings), settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout());
        try {
            client.pullPackage(orgName, packageName, version, destination, supportedPlatform,
                    RepoUtils.getBallerinaVersion(), false);
        } catch (CentralClientException e) {
            throw e;
        }
    }

    public static void writeBallerinaToml(Path balTomlPath, PackageJson packageJson,
                                          String packageName, String platform)
            throws IOException {

        Files.writeString(balTomlPath, "[package]", StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\norg = \"" + packageJson.getOrganization() + "\"",
                StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\nname = \"" + packageName + "\"", StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\nversion = \"" + packageJson.getVersion() + "\"",
                StandardOpenOption.APPEND);
        List<String> newModuleNames = packageJson.getExport().stream().map(module ->
                module.replaceFirst(packageJson.getName(), packageName)).collect(Collectors.toList());

        StringJoiner stringJoiner = new StringJoiner(",");
        for (String newModuleName : newModuleNames) {
            stringJoiner.add("\"" + newModuleName + "\"");
        }

        Files.writeString(balTomlPath, "\nexport = [" + stringJoiner + "]"
                .replaceFirst(packageJson.getName(), packageName), StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\ndistribution = \"" + packageJson.getBallerinaVersion()
                + "\"", StandardOpenOption.APPEND);

        writePackageAttributeArray(balTomlPath, packageJson.getLicenses(), "license");
        writePackageAttributeArray(balTomlPath, packageJson.getAuthors(), "authors");
        writePackageAttributeArray(balTomlPath, packageJson.getKeywords(), "keywords");
        writePackageAttributeValue(balTomlPath, packageJson.getSourceRepository(), "repository");
        writePackageAttributeValue(balTomlPath, packageJson.getVisibility(), "visibility");
        writePackageAttributeValue(balTomlPath, packageJson.getIcon(), "icon");

        Files.writeString(balTomlPath, "\n\n[build-options]", StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\nobservabilityIncluded = true\n", StandardOpenOption.APPEND);

        JsonArray platformLibraries = packageJson.getPlatformDependencies();
        if (platformLibraries == null) {
            return;
        }
        Files.writeString(balTomlPath, "\n[[platform." + platform + ".dependency]]", StandardOpenOption.APPEND);
        for (Object dependencies : platformLibraries) {
            JsonObject dependenciesObj = (JsonObject) dependencies;
            if (null == dependenciesObj.get("scope")) {
                String libPath = dependenciesObj.get("path").getAsString();
                Path libName = Optional.of(Paths.get(libPath).getFileName()).get();
                Path libRelPath = Paths.get("libs", libName.toString());
                Files.writeString(balTomlPath, "\npath = \"" + libRelPath + "\"", StandardOpenOption.APPEND);
            }

            if (dependenciesObj.get("artifactId") != null) {
                String artifactId = dependenciesObj.get("artifactId").getAsString();
                Files.writeString(balTomlPath, "\nartifactId = \"" + artifactId + "\"",
                        StandardOpenOption.APPEND);
            }
            if (dependenciesObj.get("groupId") != null) {
                String groupId = dependenciesObj.get("groupId").getAsString();
                Files.writeString(balTomlPath, "\ngroupId = \"" + groupId + "\"", StandardOpenOption.APPEND);
            }
            if (dependenciesObj.get("version") != null) {
                String dependencyVersion = dependenciesObj.get("version").getAsString();
                Files.writeString(balTomlPath, "\nversion = \"" + dependencyVersion + "\"\n",
                        StandardOpenOption.APPEND);
            }
            if (null != dependenciesObj.get("scope") && dependenciesObj.get("scope").getAsString().equals("provided")) {
                String scope = dependenciesObj.get("scope").getAsString();
                Files.writeString(balTomlPath, "scope = \"" + scope + "\"\n",
                        StandardOpenOption.APPEND);
                String artifactId = dependenciesObj.get("artifactId").getAsString();
                printError(errStream,
                        "WARNING: path for the platform dependency " + artifactId + " with provided scope " +
                                "should be specified in the Ballerina.toml",
                        null,
                        false);
            }
        }
    }

    public static void writeDependenciesToml(Path projectPath, DependencyGraphJson templateDependencyGraphJson,
                                             PackageJson templatePackageJson)
            throws IOException {
        Path depsTomlPath = projectPath.resolve(DEPENDENCIES_TOML);
        String autoGenCode = "# AUTO-GENERATED FILE. DO NOT MODIFY.\n" +
                "\n" +
                "# This file is auto-generated by Ballerina for managing dependency versions.\n" +
                "# It should not be modified by hand.\n" +
                "\n";
        Files.writeString(depsTomlPath, autoGenCode, StandardOpenOption.APPEND);
        String balTomlVersion = "[ballerina]\n" +
                "dependencies-toml-version = \"" + ProjectConstants.DEPENDENCIES_TOML_VERSION + "\"\n" +
                "\n";
        Files.writeString(depsTomlPath, balTomlVersion, StandardOpenOption.APPEND);

        // Get current package module dependencies from dependency graph modules list
        List<ModuleDependency> currentPkgModules = new ArrayList<>();
        for (ModuleDependency module : templateDependencyGraphJson.getModuleDependencies()) {
            if (module.getOrg().equals(templatePackageJson.getOrganization())
                    && module.getPackageName().equals(templatePackageJson.getName())) {
                List<ModuleDependency> currentPkgModuleDeps = module.getDependencies();
                currentPkgModules.addAll(currentPkgModuleDeps);
            }
        }

        StringBuilder pkgDesc = new StringBuilder();
        for (Dependency packageDependency : templateDependencyGraphJson.getPackageDependencyGraph()) {
            // Current package
            if (templatePackageJson.getOrganization().equals(packageDependency.getOrg())
                    && templatePackageJson.getName().equals(packageDependency.getName())) {
                pkgDesc.append("[[package]]\n")
                        .append("org = \"").append(packageDependency.getOrg()).append("\"\n")
                        .append("name = \"").append(ProjectUtils.defaultName(projectPath)).append("\"\n")
                        .append("version = \"").append(packageDependency.getVersion()).append("\"\n");
                // write `dependencies` array content
                pkgDesc.append(getDependenciesArrayContent(packageDependency));
                // Get current package modules from dependency graph modules list
                // Write them to the `modules` array
                pkgDesc.append(getDependencyModulesArrayContent(
                        templateDependencyGraphJson.getModuleDependencies(), true, projectPath));
            } else {
                // Not current package
                pkgDesc.append("[[package]]\n")
                        .append("org = \"").append(packageDependency.getOrg()).append("\"\n")
                        .append("name = \"").append(packageDependency.getName()).append("\"\n")
                        .append("version = \"").append(packageDependency.getVersion()).append("\"\n");
                // write `dependencies` array content
                pkgDesc.append(getDependenciesArrayContent(packageDependency));
                // Check this package dependency has current package modules
                // If yes, add to `packageDependencyModules` list to write to the `modules` array
                List<ModuleDependency> packageDependencyModules = new ArrayList<>();
                for (ModuleDependency module : currentPkgModules) {
                    if (packageDependency.getOrg().equals(module.getOrg())
                            && packageDependency.getName().equals(module.getPackageName())) {
                        packageDependencyModules.add(module);
                    }
                }
                // Write `packageDependencyModules` to `modules` array
                if (!packageDependencyModules.isEmpty()) {
                    pkgDesc.append(getDependencyModulesArrayContent(packageDependencyModules, false, projectPath));
                }
            }
            pkgDesc.append("\n");
        }
        Files.writeString(depsTomlPath, pkgDesc.toString(), StandardOpenOption.APPEND);
    }

    public static Path getPlatformSpecificBalaPath(String orgName, String pkgName, String version,
                                                    Path balaCache) {
        Path balaPath = balaCache.resolve(
                ProjectUtils.getRelativeBalaPath(orgName, pkgName, version, null));
        //First we will check for a bala that match any platform
        platform = ANY_PLATFORM;
        if (!Files.exists(balaPath)) {
            for (JvmTarget supportedPlatform : JvmTarget.values()) {
                balaPath = balaCache.resolve(
                        ProjectUtils.getRelativeBalaPath(orgName, pkgName, version, supportedPlatform.code()));
                if (Files.exists(balaPath)) {
                    platform = supportedPlatform.code();
                    break;
                }
            }
        }
        return balaPath;
    }

    /**
     * Write to BalTool.toml file.
     *
     * @param balToolTomlPath path to BalTool.toml
     * @param balToolJson Bal-tool.json content
     */
    public static void writeBalToolToml(Path balToolTomlPath, BalToolJson balToolJson, String packageName)
            throws IOException {
        Files.writeString(balToolTomlPath, "[tool]", StandardOpenOption.APPEND);
        Files.writeString(balToolTomlPath, "\nid = \"" + packageName + "\"\n",
                StandardOpenOption.APPEND);

        List<String> dependencyPaths = balToolJson.dependencyPaths();
        StringBuilder dependenciesContent = new StringBuilder();
        for (String dependencyPath: dependencyPaths) {
            dependenciesContent.append("\n[[dependency]]\n").append("path = \"").append(dependencyPath).append("\"\n");
        }
        Files.writeString(balToolTomlPath, dependenciesContent.toString(), StandardOpenOption.APPEND);
    }

    /**
     * Copy dependency jars to new package from template package.
     *
     * @param projectPath path to new project
     * @param toolsLibPath Path to /tool/libs directory containing dependencies
     */
    public static void copyToolDependencies(Path projectPath, Path toolsLibPath) throws IOException {
        Path toolDirectory = projectPath.resolve(TOOL_DIR);
        Files.createDirectory(toolDirectory);
        Files.createDirectory(toolDirectory.resolve(LIBS_DIR));
        Files.walkFileTree(toolsLibPath, new FileUtils.Copy(toolsLibPath, toolDirectory.resolve(LIBS_DIR)));

    }

    /**
     * Get formatted dependencies array content for Dependencies.toml dependency.
     *
     * @param packageDependency package dependency
     * @return formatted dependencies array content
     */
    private static String getDependenciesArrayContent(Dependency packageDependency) {
        StringBuilder dependenciesContent = new StringBuilder();
        if (!packageDependency.getDependencies().isEmpty()) {
            for (Dependency dependency : packageDependency.getDependencies()) {
                dependenciesContent.append("\t{org = \"").append(dependency.getOrg())
                        .append("\", name = \"").append(dependency.getName())
                        .append("\"},\n");
            }
            String dependenciesPart = dependenciesContent.toString();
            dependenciesPart = removeLastCharacter(trimStartingWhitespaces(dependenciesPart));
            return "dependencies = [\n"
                    + dependenciesPart
                    + "\n]\n";
        }
        return "";
    }

    /**
     * Get formatted modules array content for Dependencies.toml dependency.
     * <code>
     * modules = [
     * {org = "ballerinax", packageName = "redis", moduleName = "redis"}
     * ]
     * </code>
     *
     * @param dependencyModules modules of the given dependency package
     * @param isCurrentPackage  is modules array generating for current package
     * @param projectPath       project path
     * @return formatted modules array content
     */
    private static String getDependencyModulesArrayContent(List<ModuleDependency> dependencyModules,
                                                           boolean isCurrentPackage, Path projectPath) {
        StringBuilder modulesContent = new StringBuilder();
        if (isCurrentPackage) {
            for (ModuleDependency module : dependencyModules) {
                String currentPkgName = ProjectUtils.defaultName(projectPath).value();
                String modulePkgPart = module.getModuleName().split("\\.")[0];
                String currentPkgModuleName = module.getModuleName().replaceFirst(modulePkgPart, currentPkgName);
                modulesContent.append("\t{org = \"").append(module.getOrg())
                        .append("\", packageName = \"").append(currentPkgName)
                        .append("\", moduleName = \"").append(currentPkgModuleName)
                        .append("\"},\n");
            }
        } else {
            for (ModuleDependency module : dependencyModules) {
                modulesContent.append("\t{org = \"").append(module.getOrg())
                        .append("\", packageName = \"").append(module.getPackageName())
                        .append("\", moduleName = \"").append(module.getModuleName())
                        .append("\"},\n");
            }
        }
        String modulesPart = modulesContent.toString();
        modulesPart = removeLastCharacter(trimStartingWhitespaces(modulesPart));
        return "modules = [\n" + modulesPart + "\n]\n";
    }

    /**
     * Write Ballerina.toml package attribute array from template package.json to new project Ballerina.toml.
     *
     * @param balTomlPath    Ballerina.toml path of the new project
     * @param attributeArray package attribute values array
     * @param attributeName  package attribute name
     * @throws IOException when error occurs writing to the Ballerina.toml
     */
    private static void writePackageAttributeArray(Path balTomlPath, List<String> attributeArray, String attributeName)
            throws IOException {
        if (attributeArray != null && !attributeArray.isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (String attributeElement : attributeArray) {
                stringJoiner.add("\"" + attributeElement + "\"");
            }
            Files.writeString(balTomlPath, "\n" + attributeName + " = [" + stringJoiner + "]",
                    StandardOpenOption.APPEND);
        }
    }

    /**
     * Write Ballerina.toml package attribute from template package.json to new project Ballerina.toml.
     *
     * @param balTomlPath    Ballerina.toml path of the new project
     * @param attributeValue package attribute value
     * @param attributeName  package attribute name
     * @throws IOException when error occurs writing to the Ballerina.toml
     */
    private static void writePackageAttributeValue(Path balTomlPath, String attributeValue, String attributeName)
            throws IOException {
        if (attributeValue != null && !attributeValue.isEmpty()) {
            Files.writeString(balTomlPath, "\n" + attributeName + " = \"" + attributeValue + "\"",
                    StandardOpenOption.APPEND);
        }
    }

    /**
     * Find the package name for a given template.
     *
     * @param template template name
     * @return packageName - package name of the module
     */
    public static String findPkgName(String template) {
        // Split the template in to parts
        String[] orgSplit = template.split("/");
        String packageName = "";
        String packagePart = (orgSplit.length > 1) ? orgSplit[1] : "";
        String[] pkgSplit = packagePart.split(":");
        packageName = pkgSplit[0].trim();
        return packageName;
    }

    /**
     * Find the organization for a given template.
     *
     * @param template template name
     * @return orgName - org of the module
     */
    public static String findOrg(String template) {
        String[] orgSplit = template.split("/");
        return orgSplit[0].trim();
    }

    /**
     * Find the package version for a given template.
     *
     * @param template template name
     * @return version - version of the module
     */
    public static String findPkgVersion(String template) {
        String[] orgSplit = template.split("/");
        String packagePart = (orgSplit.length > 1) ? orgSplit[1] : "";
        String[] pkgSplit = packagePart.split(":");

        if (pkgSplit.length > 1) {
            return pkgSplit[1].trim();
        } else {
            return null;
        }
    }

    /**
     * Initialize a new ballerina project in the given path.
     *
     * @param path project path
     * @param packageName name of the package
     * @param template package template
     * @param balFilesExist if bal files exist in the project
     * @throws IOException  If any IO exception occurred
     * @throws URISyntaxException If any URISyntaxException occurred
     */
    public static void initPackageByTemplate(Path path, String packageName, String template, boolean balFilesExist)
            throws IOException, URISyntaxException {
        // We will be creating following in the project directory
        // - Ballerina.toml
        // - main.bal
        // - .gitignore       <- git ignore file
        // - .devcontainer.json

        applyTemplate(path, template, balFilesExist);
        if (template.equalsIgnoreCase(LIB_DIR)) {
            initLibPackage(path, packageName);
            Path source = path.resolve("lib.bal");
            Files.move(source, source.resolveSibling(guessPkgName(packageName, template) + ".bal"),
                    StandardCopyOption.REPLACE_EXISTING);
        } else if (template.equalsIgnoreCase(TOOL_DIR)) {
            initToolPackage(path, packageName);
        } else {
            initPackage(path, packageName);
        }
        createDefaultGitignore(path);
        createDefaultDevContainer(path);
    }

    private static void createDefaultGitignore(Path path) throws IOException {
        Path gitignore = path.resolve(ProjectConstants.GITIGNORE_FILE_NAME);
        if (Files.notExists(gitignore)) {
            Files.createFile(gitignore);
        }
        if (Files.size(gitignore) == 0) {
            String defaultGitignore = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + GITIGNORE);
            Files.write(gitignore, defaultGitignore.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void createDefaultDevContainer(Path path) throws IOException {
        Path devContainer = path.resolve(ProjectConstants.DEVCONTAINER);
        if (Files.notExists(devContainer)) {
            Files.createFile(devContainer);
        }
        if (Files.size(devContainer) == 0) {
            String defaultDevContainer = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + DEVCONTAINER);
            defaultDevContainer = defaultDevContainer.replace("latest", RepoUtils.getBallerinaVersion());
            Files.write(devContainer, defaultDevContainer.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Get the list of templates.
     *
     * @return list of templates
     */
    public static List<String> getTemplates() {
        try {
            Path templateDir = getTemplatePath();
            Stream<Path> walk = Files.walk(templateDir, 1);

            List<String> templates = walk.filter(Files::isDirectory)
                    .filter(directory -> !templateDir.equals(directory))
                    .filter(directory -> directory.getFileName() != null)
                    .map(directory -> directory.getFileName())
                    .map(fileName -> fileName.toString())
                    .collect(Collectors.toList());

            if (null != jarFs) {
                return templates.stream().map(t -> t
                        .replace(jarFs.getSeparator(), ""))
                        .collect(Collectors.toList());
            } else {
                return templates;
            }

        } catch (IOException | URISyntaxException e) {
            // we will return an empty list if error.
            return new ArrayList<String>();
        }
    }

    /**
     * Get the path to the given template.
     *
     * @return path of the given template
     * @throws URISyntaxException if any URISyntaxException occured
     */
    private static Path getTemplatePath() throws URISyntaxException {
        URI uri = CommandUtil.class.getClassLoader().getResource(CREATE_CMD_TEMPLATES).toURI();
        if (uri.toString().contains("!")) {
            final String[] array = uri.toString().split("!");
            return jarFs.getPath(array[1]);
        } else {
            return Paths.get(uri);
        }
    }

    /**
     * Apply the template to the created module.
     *
     * @param modulePath path to the module
     * @param template template name
     * @param balFilesExist if bal files exist in the project
     * @throws IOException if any IOException occurred
     * @throws URISyntaxException if any URISyntaxException occurred
     */
    public static void applyTemplate(Path modulePath, String template, boolean balFilesExist)
            throws IOException, URISyntaxException {
        Path templateDir = getTemplatePath().resolve(template);
        if (template.equalsIgnoreCase(MAIN_TEMPLATE)) {
            templateDir = getTemplatePath().resolve(DEFAULT_TEMPLATE);
            Path tempDirTest = getTemplatePath().resolve(MAIN_TEMPLATE);
            Files.walkFileTree(templateDir, new FileUtils.Copy(templateDir, modulePath));
            Files.walkFileTree(tempDirTest, new FileUtils.Copy(tempDirTest, modulePath));
        } else if (template.equalsIgnoreCase(DEFAULT_TEMPLATE)) {
            if (!balFilesExist) {
                Files.walkFileTree(templateDir, new FileUtils.Copy(templateDir, modulePath));
            }
        } else {
            Files.walkFileTree(templateDir, new FileUtils.Copy(templateDir, modulePath));
        }
    }

    /**
     * Initialize a new ballerina project in the given path.
     *
     * @param path Project path
     * @throws IOException If any IO exception occurred
     */
    public static void initPackage(Path path, String packageName) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-app.toml");
        // replace manifest distribution with a guessed value
        defaultManifest = defaultManifest
                .replaceAll(ORG_NAME, ProjectUtils.guessOrgName())
                .replaceAll(PKG_NAME, guessPkgName(packageName, "app"))
                .replaceAll(DIST_VERSION, RepoUtils.getBallerinaShortVersion());
        Files.write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));
    }

    private static void initLibPackage(Path path, String packageName) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-lib.toml");
        // replace manifest org and name with a guessed value.
        defaultManifest = defaultManifest.replaceAll(ORG_NAME, ProjectUtils.guessOrgName())
                .replaceAll(PKG_NAME, guessPkgName(packageName, "lib"))
                .replaceAll(DIST_VERSION, RepoUtils.getBallerinaShortVersion());

        write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));

        // Create Package.md
        String packageMd = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/Package.md");
        write(path.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME), packageMd.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Initialize a new ballerina tool package in the given path.
     *
     * @param path Project path
     * @param packageName package name
     * @throws IOException If any IO exception occurred
     */
    private static void initToolPackage(Path path, String packageName) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-app.toml");
        defaultManifest = defaultManifest
                .replaceAll(ORG_NAME, ProjectUtils.guessOrgName())
                .replaceAll(PKG_NAME, guessPkgName(packageName, TOOL_DIR))
                .replaceAll(DIST_VERSION, RepoUtils.getBallerinaShortVersion());
        Files.write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));

        Path balToolToml = path.resolve(ProjectConstants.BAL_TOOL_TOML);
        Files.createFile(balToolToml);

        String balToolManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-tool.toml");
        balToolManifest = balToolManifest.replaceAll(TOOL_ID, guessPkgName(packageName, TOOL_DIR));

        write(balToolToml, balToolManifest.getBytes(StandardCharsets.UTF_8));
    }

    protected static PackageVersion findLatest(List<PackageVersion> packageVersions) {
        if (packageVersions.isEmpty()) {
            return null;
        }
        PackageVersion latestVersion = packageVersions.get(0);
        for (PackageVersion pkgVersion : packageVersions) {
            latestVersion = getLatest(latestVersion, pkgVersion);
        }
        return latestVersion;
    }

    protected static PackageVersion getLatest(PackageVersion v1, PackageVersion v2) {
        SemanticVersion semVer1 = v1.value();
        SemanticVersion semVer2 = v2.value();
        boolean isV1PreReleaseVersion = semVer1.isPreReleaseVersion();
        boolean isV2PreReleaseVersion = semVer2.isPreReleaseVersion();
        if (isV1PreReleaseVersion ^ isV2PreReleaseVersion) {
            return isV1PreReleaseVersion ? v2 : v1;
        } else {
            return semVer1.greaterThanOrEqualTo(semVer2) ? v1 : v2;
        }
    }

    public static List<PackageVersion> getPackageVersions(Path balaPackagePath) {
        List<Path> versions = new ArrayList<>();
        if (Files.exists(balaPackagePath)) {
            Stream<Path> collectVersions;
            try {
                collectVersions = Files.list(balaPackagePath);
            } catch (IOException e) {
                throw new RuntimeException("Error while accessing Distribution cache: " + e.getMessage());
            }
            versions.addAll(collectVersions.collect(Collectors.toList()));
        }
        return pathToVersions(versions);
    }

    protected static List<PackageVersion> pathToVersions(List<Path> versions) {
        List<PackageVersion> availableVersions = new ArrayList<>();
        versions.stream().map(path -> Optional.ofNullable(path)
                .map(Path::getFileName)
                .map(Path::toString)
                .orElse("0.0.0")).forEach(version -> {
            try {
                availableVersions.add(PackageVersion.from(version));
            } catch (ProjectException ignored) {
                // We consider only the semver compatible versions as valid
                // bala directories. Since we only allow building and pushing
                // semver compatible packages, it is safe to pick only
                // the semver compatible versions.
            }
        });
        return availableVersions;
    }

    /**
     * Remove starting whitespaces of a string.
     *
     * @param str given string
     * @return starting whitespaces removed string
     */
    private static String trimStartingWhitespaces(String str) {
        return str.replaceFirst("\\s++$", "");
    }

    /**
     * Remove last character of a string.
     *
     * @param str given string
     * @return last character removed string
     */
    private static String removeLastCharacter(String str) {
        return str.substring(0, str.length() - 1);
    }

    /**
     * Check if files of the given template exist in a given path.
     *
     * @param template given string
     * @param packagePath given path
     * @throws URISyntaxException if URI syntax exception occurred
     * @throws IOException if IO exception occurred
     */
    public static String checkTemplateFilesExists(String template, Path packagePath) throws URISyntaxException,
            IOException {
        Path templateDir = getTemplatePath().resolve(template);
        Stream<Path> paths = Files.list(templateDir);
        List<Path> templateFilePathList = paths.collect(Collectors.toList());
        StringBuilder existingFiles = new StringBuilder();
        for (Path path : templateFilePathList) {
            Optional<String> fileNameOptional = Optional.ofNullable(path.getFileName()).map(path1 -> path1.toString());
            if (fileNameOptional.isPresent()) {
                String fileName = fileNameOptional.get();
                if (!fileName.endsWith(ProjectConstants.BLANG_SOURCE_EXT) &&
                        Files.exists(packagePath.resolve(fileName))) {
                    existingFiles.append(fileName).append(FILE_STRING_SEPARATOR);
                }
            }
        }
        return existingFiles.toString();
    }

    /**
     * Check if common files of a package exist in a given path.
     *
     * @param packagePath given path
     */
    public static String checkPackageFilesExists(Path packagePath) {
        String[] packageFiles = {DEPENDENCIES_TOML, BAL_TOOL_TOML, ProjectConstants.PACKAGE_MD_FILE_NAME,
                ProjectConstants.MODULE_MD_FILE_NAME, ProjectConstants.MODULES_ROOT, ProjectConstants.TEST_DIR_NAME};
        StringBuilder existingFiles = new StringBuilder();
        for (String file : packageFiles) {
            if (Files.exists(packagePath.resolve(file))) {
                existingFiles.append(file).append(FILE_STRING_SEPARATOR);
            }
        }
        return existingFiles.toString();
    }

    /**
     * Check if .bal files exist in a given path.
     *
     * @param packagePath given path
     * @return error message if files exists
     */
    public static boolean balFilesExists(Path packagePath) throws IOException {
        //Only skip the bal file to be created if any other .bal files exists
        return Files.list(packagePath).anyMatch(path -> path.toString().endsWith(ProjectConstants.BLANG_SOURCE_EXT));
    }

    /**
     * Get the latest version from a given list of versions.
     *
     * @param versions the list of strings
     * @return the latest version
     */
    static String getLatestVersion(List<String> versions) {
        String latestVersion = versions.get(0);
        for (String version : versions) {
            if (SemanticVersion.from(version).greaterThan(SemanticVersion.from(latestVersion))) {
                latestVersion = version;
            }
        }
        return latestVersion;
    }

    /**
     * Pull the dependencies of a given package from central.
     *
     * @param orgName org name of the dependent package
     * @param packageName name of the dependent package
     * @param version version of the dependent package
     * @return true if the dependent package compilation has errors
     */
    static boolean pullDependencyPackages(String orgName, String packageName, String version) {
        Path ballerinaUserHomeDirPath = ProjectUtils.createAndGetHomeReposPath();
        Path centralRepositoryDirPath = ballerinaUserHomeDirPath.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME);
        Path balaDirPath = centralRepositoryDirPath.resolve(ProjectConstants.BALA_DIR_NAME);
        Path balaPath = ProjectUtils.getPackagePath(balaDirPath, orgName, packageName, version);
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        Path cacheDir = centralRepositoryDirPath.resolve(
                ProjectConstants.CACHES_DIR_NAME + "-" + ballerinaShortVersion);

        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(new FileSystemCache.FileSystemCacheFactory(cacheDir));
        BalaProject balaProject = BalaProject.loadProject(defaultBuilder, balaPath);

        // Delete package cache if available
        Path packageCacheDir = cacheDir.resolve(orgName).resolve(packageName).resolve(version);
        if (packageCacheDir.toFile().exists()) {
            deleteDirectory(packageCacheDir);
        }

        // getResolution pulls all dependencies of the pulled package
        PackageCompilation packageCompilation = balaProject.currentPackage().getCompilation();
        Collection<Diagnostic> resolutionDiagnostics = packageCompilation.getResolution()
                .diagnosticResult().diagnostics();
        if (!resolutionDiagnostics.isEmpty()) {
            printDiagnostics(resolutionDiagnostics);
        }
        if (packageCompilation.getResolution().diagnosticResult().hasErrors()) {
            return true;
        }
        if (!hasProvidedPlatformDeps(balaProject.currentPackage())) {
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
            Collection<Diagnostic> backendDiagnostics = jBallerinaBackend.diagnosticResult().diagnostics(false);
            if (!backendDiagnostics.isEmpty()) {
                printDiagnostics(backendDiagnostics);
            }
            return jBallerinaBackend.diagnosticResult().hasErrors();
        }
        errStream.println("Warning: Cache generation skipped due to platform dependencies with 'provided' scope");
        return false;
    }

    private static void printDiagnostics(Collection<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic: diagnostics) {
            CommandUtil.printError(errStream, diagnostic.toString(), null, false);
        }
    }

    private static boolean hasProvidedPlatformDeps(Package pkg) {
        for (PackageManifest.Platform platform: pkg.manifest().platforms().values()) {
            for (Map<String, Object> dependency: platform.dependencies()) {
                if (PlatformLibraryScope.PROVIDED.getStringValue().equals(dependency.get("scope"))) {
                    return true;
                }
            }
        }
        PackageCompilation packageCompilation = pkg.getCompilation();
        Set<Object> providedDeps = new HashSet<>();
        packageCompilation.getResolution().allDependencies()
                .stream()
                .map(ResolvedPackageDependency::packageInstance)
                .map(Package::manifest)
                .flatMap(pkgManifest -> pkgManifest.platforms().values().stream())
                .filter(Objects::nonNull)
                .flatMap(pkgPlatform -> pkgPlatform.dependencies().stream())
                .filter(dependency -> PlatformLibraryScope.PROVIDED.equals(dependency.get("scope")))
                .forEach(providedDeps::add);

        return !providedDeps.isEmpty();
    }


}
