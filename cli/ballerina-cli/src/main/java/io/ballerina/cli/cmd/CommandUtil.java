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
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.bala.PackageJson;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.guessPkgName;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.write;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.ANY_PLATFORM;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;
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
    public static final String GITIGNORE = "gitignore";
    public static final String DEVCONTAINER = "devcontainer";
    public static final String NEW_CMD_DEFAULTS = "new_cmd_defaults";
    public static final String CREATE_CMD_TEMPLATES = "create_cmd_templates";
    private static FileSystem jarFs;
    private static Map<String, String> env;
    private static PrintStream errStream;
    private static PrintStream outStream;
    private static Path homeCache;
    private static boolean exitWhenFinish;

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
                              Path projectPath, Path balaCache) {
        Path balaPath = balaCache.resolve(
                ProjectUtils.getRelativeBalaPath(orgName, templatePkgName, version, null));
        //First we will check for a bala that match any platform
        String platform = findPlatform(balaPath);
        balaPath = balaCache.resolve(
                ProjectUtils.getRelativeBalaPath(orgName, templatePkgName, version, platform));
        if (!Files.exists(balaPath)) {
            CommandUtil.printError(errStream,
                    "unable to find the bala: " + balaPath,
                    null,
                    false);
            CommandUtil.exitError(exitWhenFinish);
        }
        try {
            addModules(balaPath, projectPath, packageName, platform);
        } catch (IOException e) {
            try {
                FileUtils.deletePath(projectPath);
            } catch (IOException exception) {
                // ignore
            }
            CommandUtil.printError(errStream,
                    "error occurred while creating the package: " + e.getMessage(),
                    null,
                    false);
            CommandUtil.exitError(exitWhenFinish);
        }
    }

    private static void addModules(Path balaPath, Path projectPath, String packageName, String platform)
            throws IOException {
        Gson gson = new Gson();
        Path packageJsonPath = balaPath.resolve("package.json");
        PackageJson packageJson = null;
        try (InputStream inputStream = new FileInputStream(String.valueOf(packageJsonPath))) {
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            packageJson = gson.fromJson(fileReader, PackageJson.class);
        } catch (IOException e) {
            printError(errStream,
                    "Error while reading the package json file: " + e.getMessage(),
                    null,
                    false);
            getRuntime().exit(1);
        }

        if (!packageJson.getTemplate()) {
            throw createLauncherException("unable to create the package: " +
                    "specified package is not a template");
        }

        // Create Ballerina.toml
        Path ballerinaToml = projectPath.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createDirectories(projectPath);
        Files.createFile(ballerinaToml);
        writeBallerinaToml(ballerinaToml, packageJson, packageName, platform);

        // Create Package.md
        Path packageMDFilePath = balaPath.resolve("docs")
                .resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
        Path toPackageMdPath = projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
        if (Files.exists(packageMDFilePath)) {
            Files.copy(packageMDFilePath, toPackageMdPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Create modules
        String templatePkgName = packageJson.getName();
        Path modulesRoot = balaPath.resolve(ProjectConstants.MODULES_ROOT);
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
            Files.walkFileTree(moduleRoot, new FileUtils.Copy(moduleRoot, destDir));
        }

        // Copy platform libraries
        Path platformLibPath = balaPath.resolve("platform").resolve(platform);
        if (Files.exists(platformLibPath)) {
            Path libs = projectPath.resolve("libs");
            Files.createDirectories(libs);
            Files.walkFileTree(platformLibPath, new FileUtils.Copy(platformLibPath, libs));
        }
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
            //First we will check for a bala that match any platform
            Path balaPath = balaCache.resolve(
                    ProjectUtils.getRelativeBalaPath(orgName, packageName, version, null));
            String platform = findPlatform(balaPath);
            balaPath = balaCache.resolve(
                    ProjectUtils.getRelativeBalaPath(orgName, packageName, version, platform));
            if (Files.exists(balaPath)) {
                return balaPath;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void initPackageFromCentral(Path balaCache, Path projectPath, String packageName, String template) {
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
        applyTemplate(orgName, templatePackageName, version, packageName, projectPath, balaCache);
    }

    private static void pullPackageFromRemote(String orgName, String packageName, String version, Path destination)
            throws CentralClientException {
        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            Settings settings;
            try {
                settings = readSettings();
                // Ignore Settings.toml diagnostics in the pull command
            } catch (SettingsTomlException e) {
                // Ignore 'Settings.toml' parsing errors and return empty Settings object
                settings = Settings.from();
            }
            CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                    initializeProxy(settings.getProxy()),
                    getAccessTokenOfCLI(settings));
            client.pullPackage(orgName, packageName, version, destination, supportedPlatform,
                    RepoUtils.getBallerinaVersion(), false);
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

        Files.writeString(balTomlPath, "\nexport = [" + stringJoiner.toString() + "]"
                .replaceFirst(packageJson.getName(), packageName), StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\nballerina_version = \"" + packageJson.getBallerinaVersion()
                + "\"", StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\nimplementation_vendor = \"" + packageJson.getImplementationVendor()
                + "\"", StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\nlanguage_spec_version = \"" + packageJson.getLanguageSpecVersion()
                + "\"", StandardOpenOption.APPEND);

        Files.writeString(balTomlPath, "\n\n[build-options]", StandardOpenOption.APPEND);
        Files.writeString(balTomlPath, "\nobservabilityIncluded = true", StandardOpenOption.APPEND);

        JsonArray platformLibraries = packageJson.getPlatformDependencies();
        if (platformLibraries == null) {
            return;
        }
        Files.writeString(balTomlPath, "\n[[platform." + platform + ".dependency]]", StandardOpenOption.APPEND);
        for (Object dependencies : platformLibraries) {
            JsonObject dependeciesObj = (JsonObject) dependencies;
            String libPath = dependeciesObj.get("path").getAsString();
            Path libName = Optional.of(Paths.get(libPath).getFileName()).get();
            Path libRelPath = Paths.get("libs", libName.toString());
            Files.writeString(balTomlPath, "\npath = \"" + libRelPath + "\"", StandardOpenOption.APPEND);

            if (dependeciesObj.get("artifactId") != null) {
                String artifactId = dependeciesObj.get("artifactId").getAsString();
                Files.writeString(balTomlPath, "\nartifactId = \"" + artifactId + "\"",
                        StandardOpenOption.APPEND);
            }
            if (dependeciesObj.get("groupId") != null) {
                String groupId = dependeciesObj.get("groupId").getAsString();
                Files.writeString(balTomlPath, "\ngroupId = \"" + groupId + "\"", StandardOpenOption.APPEND);
            }
            if (dependeciesObj.get("version") != null) {
                String dependencyVersion = dependeciesObj.get("version").getAsString();
                Files.writeString(balTomlPath, "\nversion = \"" + dependencyVersion + "\"\n",
                        StandardOpenOption.APPEND);
            }
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
     * Find the platform of the module for a given template.
     *
     * @param balaPath path to the module
     * @return platform - platform of the module
     */
    public static String findPlatform(Path balaPath) {
        String platform = "";
        if (!Files.exists(balaPath)) {
            //If bala for any platform not exist check for specific platform
            platform = JvmTarget.JAVA_11.code();
        } else {
            platform = ANY_PLATFORM;
        }
        return platform;
    }

    /**
     * Initialize a new ballerina project in the given path.
     *
     * @param path project path
     * @param packageName name of the package
     * @param template package template
     * @throws IOException  If any IO exception occurred
     * @throws URISyntaxException If any URISyntaxException occurred
     */
    public static void initPackageByTemplate(Path path, String packageName, String template) throws IOException,
            URISyntaxException {
        // We will be creating following in the project directory
        // - Ballerina.toml
        // - main.bal
        // - .gitignore       <- git ignore file
        // - .devcontainer.json

        applyTemplate(path, template);
        if (template.equalsIgnoreCase("lib")) {
            initLibPackage(path, packageName);
            Path source = path.resolve("lib.bal");
            Files.move(source, source.resolveSibling(guessPkgName(packageName) + ".bal"),
                    StandardCopyOption.REPLACE_EXISTING);
        } else {
            initPackage(path);
        }
        Path gitignore = path.resolve(ProjectConstants.GITIGNORE_FILE_NAME);
        if (Files.notExists(gitignore)) {
            Files.createFile(gitignore);
        }
        String defaultGitignore = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + GITIGNORE);
        Files.write(gitignore, defaultGitignore.getBytes(StandardCharsets.UTF_8));
        // Create dev container
        Path devcontainer = path.resolve(ProjectConstants.DEVCONTAINER);
        if (Files.notExists(devcontainer)) {
            Files.createFile(devcontainer);
        }


        String defaultDevcontainer = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + DEVCONTAINER);
        defaultDevcontainer = defaultDevcontainer.replace("latest", RepoUtils.getBallerinaVersion());
        Files.write(devcontainer, defaultDevcontainer.getBytes(StandardCharsets.UTF_8));
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
     * @throws IOException if any IOException occurred
     * @throws URISyntaxException if any URISyntaxException occurred
     */
    public static void applyTemplate(Path modulePath, String template) throws IOException, URISyntaxException {
        Path templateDir = getTemplatePath().resolve(template);
        if (template.equalsIgnoreCase("main")) {
            templateDir = getTemplatePath().resolve("default");
            Path tempDirTest = getTemplatePath().resolve("main");
            Files.walkFileTree(templateDir, new FileUtils.Copy(templateDir, modulePath));
            Files.walkFileTree(tempDirTest, new FileUtils.Copy(tempDirTest, modulePath));
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
    public static void initPackage(Path path) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-app.toml");
        // replace manifest distribution with a guessed value
        defaultManifest = defaultManifest.replaceAll(DIST_VERSION, RepoUtils.getBallerinaShortVersion());
        Files.write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));
    }

    private static void initLibPackage(Path path, String packageName) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-lib.toml");
        // replace manifest org and name with a guessed value.
        defaultManifest = defaultManifest.replaceAll(ORG_NAME, ProjectUtils.guessOrgName())
                .replaceAll(PKG_NAME, packageName)
                .replaceAll(DIST_VERSION, RepoUtils.getBallerinaShortVersion());

        write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));

        // Create Package.md
        String packageMd = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/Package.md");
        write(path.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME), packageMd.getBytes(StandardCharsets.UTF_8));
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
}
