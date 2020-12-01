/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.util;

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ProjectException;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.packaging.converters.URIDryConverter;
import org.wso2.ballerinalang.util.Lists;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import static io.ballerina.projects.util.FileUtils.getFileNameWithoutExtension;
import static io.ballerina.projects.util.ProjectConstants.ASM_COMMONS_JAR;
import static io.ballerina.projects.util.ProjectConstants.ASM_JAR;
import static io.ballerina.projects.util.ProjectConstants.ASM_TREE_JAR;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_HOME;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_HOME_BRE;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_PACK_VERSION;
import static io.ballerina.projects.util.ProjectConstants.BALLERINA_TOML;
import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT;
import static io.ballerina.projects.util.ProjectConstants.DIFF_UTILS_JAR;
import static io.ballerina.projects.util.ProjectConstants.JACOCO_CORE_JAR;
import static io.ballerina.projects.util.ProjectConstants.JACOCO_REPORT_JAR;
import static io.ballerina.projects.util.ProjectConstants.LIB_DIR;
import static io.ballerina.projects.util.ProjectConstants.PROPERTIES_FILE;
import static io.ballerina.projects.util.ProjectConstants.TEST_CORE_JAR_PREFIX;
import static io.ballerina.projects.util.ProjectConstants.TEST_RUNTIME_JAR_PREFIX;
import static io.ballerina.projects.util.ProjectConstants.USER_NAME;

/**
 * Project related util methods.
 *
 * @since 2.0.0
 */
public class ProjectUtils {
    private static final String USER_HOME = "user.home";

    /**
     * Validates the org-name.
     *
     * @param orgName The org-name
     * @return True if valid org-name or package name, else false.
     */
    public static boolean validateOrgName(String orgName) {
        String validRegex = "^[a-zA-Z0-9_]*$";
        return Pattern.matches(validRegex, orgName);
    }

    /**
     * Validates the package name.
     *
     * @param packageName The package name.
     * @return True if valid package name, else false.
     */
    public static boolean validatePkgName(String packageName) {
        String validLanglib = "^lang.[a-z0-9_]*$";
        String validRegex = "^[a-zA-Z0-9_]*$";
        // We have special case for lang. packages
        // todo consider orgname when checking is it is a lang lib
        return Pattern.matches(validRegex, packageName) || Pattern.matches(validLanglib, packageName);
    }

    /**
     * Validates the module name.
     *
     * @param moduleName The module name.
     * @return True if valid module name, else false.
     */
    public static boolean validateModuleName(String moduleName) {
        String validRegex = "^[a-zA-Z0-9_.]*$";
        return Pattern.matches(validRegex, moduleName);
    }

    /**
     * Find the project root by recursively up to the root.
     *
     * @param filePath project path
     * @return project root
     */
    public static Path findProjectRoot(Path filePath) {
        if (filePath != null) {
            filePath = filePath.toAbsolutePath().normalize();
            if (filePath.toFile().isDirectory()) {
                if (Files.exists(filePath.resolve(BALLERINA_TOML))) {
                    return filePath;
                }
            }
            return findProjectRoot(filePath.getParent());
        }
        return null;
    }

    /**
     * Checks if the path is a Ballerina project.
     *
     * @param sourceRoot source root of the project.
     * @return true if the directory is a project repo, false if its the home repo
     */
    public static boolean isBallerinaProject(Path sourceRoot) {
        Path ballerinaToml = sourceRoot.resolve(BALLERINA_TOML);
        return Files.isDirectory(sourceRoot)
                && Files.exists(ballerinaToml)
                && Files.isRegularFile(ballerinaToml);
    }

    /**
     * Guess organization name based on user name in system.
     *
     * @return organization name
     */
    public static String guessOrgName() {
        String guessOrgName = System.getProperty(USER_NAME);
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        return guessOrgName;
    }

    /**
     * Guess package name with valid pattern.
     *
     * @param packageName package name
     * @return package name
     */
    public static String guessPkgName(String packageName) {
        if (!validatePkgName(packageName)) {
            return packageName.replaceAll("[^a-zA-Z0-9_]", "_");
        }
        return packageName;
    }

    public static String getBaloName(PackageManifest pkgDesc) {
        return ProjectUtils.getBaloName(pkgDesc.org().toString(),
                pkgDesc.name().toString(),
                pkgDesc.version().toString(),
                null
        );
    }

    public static String getBaloName(String org, String pkgName, String version, String platform) {
        // <orgname>-<packagename>-<platform>-<version>.balo
        if (platform == null || "".equals(platform)) {
            platform = "any";
        }
        return org + "-" + pkgName + "-" + platform + "-" + version + BLANG_COMPILED_PKG_BINARY_EXT;
    }

    public static String getJarFileName(Package pkg) {
        // <orgname>-<packagename>-<version>.jar
        return pkg.packageOrg().toString() + "-" + pkg.packageName().toString()
                + "-" + pkg.packageVersion() + BLANG_COMPILED_JAR_EXT;
    }

    public static String getExecutableName(Package pkg) {
        // <packagename>.jar
        return pkg.packageName().toString() + BLANG_COMPILED_JAR_EXT;
    }

    public static String getOrgFromBaloName(String baloName) {
        return baloName.split("-")[0];
    }

    public static String getPackageNameFromBaloName(String baloName) {
        return baloName.split("-")[1];
    }

    public static String getVersionFromBaloName(String baloName) {
        // TODO validate this method of getting the version of the balo
        String versionAndExtension = baloName.split("-")[3];
        int extensionIndex = versionAndExtension.indexOf(BLANG_COMPILED_PKG_BINARY_EXT);
        return versionAndExtension.substring(0, extensionIndex);
    }

    private static final HashSet<String> excludeExtensions = new HashSet<>(Lists.of("DSA", "SF"));

    public static Path getBalHomePath() {
        return Paths.get(System.getProperty(BALLERINA_HOME));
    }

    public static String getBallerinaPackVersion() {
        try (InputStream inputStream = ProjectUtils.class.getResourceAsStream(PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(BALLERINA_PACK_VERSION);
        } catch (Throwable ignore) {
        }
        return "unknown";
    }

    public static Path getBallerinaRTJarPath() {
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "ballerina-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return getBalHomePath().resolve("bre").resolve("lib").resolve(runtimeJarName);
    }

    public static Path getChoreoRuntimeJarPath() {
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "ballerina-choreo-extension-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return getBalHomePath().resolve("bre").resolve("lib").resolve(runtimeJarName);
    }

    public static List<Path> testDependencies() {
        List<Path> dependencies = new ArrayList<>();
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        Path homeLibPath = getBalHomePath().resolve(BALLERINA_HOME_BRE).resolve(LIB_DIR);
        String testRuntimeJarName = TEST_RUNTIME_JAR_PREFIX + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        String testCoreJarName = TEST_CORE_JAR_PREFIX + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        String langJarName = "ballerina-lang-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;

        Path testRuntimeJarPath = homeLibPath.resolve(testRuntimeJarName);
        Path testCoreJarPath = homeLibPath.resolve(testCoreJarName);
        Path langJarPath = homeLibPath.resolve(langJarName);
        Path jacocoCoreJarPath =  homeLibPath.resolve(JACOCO_CORE_JAR);
        Path jacocoReportJarPath = homeLibPath.resolve(JACOCO_REPORT_JAR);
        Path asmJarPath = homeLibPath.resolve(ASM_JAR);
        Path asmTreeJarPath = homeLibPath.resolve(ASM_TREE_JAR);
        Path asmCommonsJarPath = homeLibPath.resolve(ASM_COMMONS_JAR);
        Path diffUtilsJarPath = homeLibPath.resolve(DIFF_UTILS_JAR);

        dependencies.add(testRuntimeJarPath);
        dependencies.add(testCoreJarPath);
        dependencies.add(langJarPath);
        dependencies.add(jacocoCoreJarPath);
        dependencies.add(jacocoReportJarPath);
        dependencies.add(asmJarPath);
        dependencies.add(asmTreeJarPath);
        dependencies.add(asmCommonsJarPath);
        dependencies.add(diffUtilsJarPath);
        return dependencies;
    }

    public static void assembleExecutableJar(Manifest manifest,
                                             List<CompiledJarFile> compiledPackageJarList,
                                             Path targetPath) throws IOException {

        // Used to prevent adding duplicated entries during the final jar creation.
        HashSet<String> copiedEntries = new HashSet<>();

        try (ZipArchiveOutputStream outStream = new ZipArchiveOutputStream(
                new BufferedOutputStream(new FileOutputStream(targetPath.toString())))) {
            copyRuntimeJar(outStream, getBallerinaRTJarPath(), copiedEntries);

            JarArchiveEntry e = new JarArchiveEntry(JarFile.MANIFEST_NAME);
            outStream.putArchiveEntry(e);
            manifest.write(new BufferedOutputStream(outStream));
            outStream.closeArchiveEntry();

            for (CompiledJarFile compiledJarFile : compiledPackageJarList) {
                for (Map.Entry<String, byte[]> keyVal : compiledJarFile.getJarEntries().entrySet()) {
                    copyEntry(copiedEntries, outStream, keyVal);
                }
            }
        }
    }

    private static void copyEntry(HashSet<String> copiedEntries,
                                  ZipArchiveOutputStream outStream,
                                  Map.Entry<String, byte[]> keyVal) throws IOException {
        String entryName = keyVal.getKey();
        if (!isCopiedOrExcludedEntry(entryName, copiedEntries)) {
            byte[] entryContent = keyVal.getValue();
            JarArchiveEntry entry = new JarArchiveEntry(entryName);
            outStream.putArchiveEntry(entry);
            outStream.write(entryContent);
            outStream.closeArchiveEntry();
        }
    }

    /**
     * Copies a given jar file into the executable fat jar.
     *
     * @param ballerinaRTJarPath Ballerina runtime jar path.
     * @throws IOException If jar file copying is failed.
     */
    public static void copyRuntimeJar(ZipArchiveOutputStream outStream,
                                      Path ballerinaRTJarPath,
                                      HashSet<String> copiedEntries) throws IOException {
        // TODO This code is copied from the current executable jar creation logic. We may need to refactor this.
        HashMap<String, StringBuilder> services = new HashMap<>();
        ZipFile zipFile = new ZipFile(ballerinaRTJarPath.toString());
        ZipArchiveEntryPredicate predicate = entry -> {

            String entryName = entry.getName();
            if (entryName.equals("META-INF/MANIFEST.MF")) {
                return false;
            }

            if (entryName.startsWith("META-INF/services")) {
                StringBuilder s = services.get(entryName);
                if (s == null) {
                    s = new StringBuilder();
                    services.put(entryName, s);
                }
                char c = '\n';

                int len;
                try (BufferedInputStream inStream = new BufferedInputStream(zipFile.getInputStream(entry))) {
                    while ((len = inStream.read()) != -1) {
                        c = (char) len;
                        s.append(c);
                    }
                } catch (IOException e) {
                    throw new ProjectException(e);
                }
                if (c != '\n') {
                    s.append('\n');
                }

                // Its not required to copy SPI entries in here as we'll be adding merged SPI related entries
                // separately. Therefore the predicate should be set as false.
                return false;
            }

            // Skip already copied files or excluded extensions.
            if (isCopiedOrExcludedEntry(entryName, copiedEntries)) {
                return false;
            }
            // SPIs will be merged first and then put into jar separately.
            copiedEntries.add(entryName);
            return true;
        };

        // Transfers selected entries from this zip file to the output stream, while preserving its compression and
        // all the other original attributes.
        zipFile.copyRawEntries(outStream, predicate);
        zipFile.close();

        for (Map.Entry<String, StringBuilder> entry : services.entrySet()) {
            String s = entry.getKey();
            StringBuilder service = entry.getValue();
            JarArchiveEntry e = new JarArchiveEntry(s);
            outStream.putArchiveEntry(e);
            outStream.write(service.toString().getBytes(StandardCharsets.UTF_8));
            outStream.closeArchiveEntry();
        }
    }

    private static boolean isCopiedOrExcludedEntry(String entryName, HashSet<String> copiedEntries) {
        return copiedEntries.contains(entryName) ||
                excludeExtensions.contains(entryName.substring(entryName.lastIndexOf(".") + 1));
    }

    /**
     * Construct and return the thin jar name of the provided module.
     *
     * @param module Module instance
     * @return the name of the thin jar
     */
    public static String getJarFileName(Module module) {
        String jarName;
        if (module.packageInstance().manifest().org().anonymous()) {
            DocumentId documentId = module.documentIds().iterator().next();
            String documentName = module.document(documentId).name();
            jarName = getFileNameWithoutExtension(documentName);
        } else {
            ModuleName moduleName = module.moduleName();
            if (moduleName.isDefaultModuleName()) {
                jarName = moduleName.packageName().toString();
            } else {
                jarName = moduleName.moduleNamePart();
            }
        }
        return jarName;
    }

    /**
     * Create and get the home repository path.
     *
     * @return home repository path
     */
    public static Path createAndGetHomeReposPath() {
        Path homeRepoPath;
        String homeRepoDir = System.getenv(ProjectConstants.HOME_REPO_ENV_KEY);
        if (homeRepoDir == null || homeRepoDir.isEmpty()) {
            String userHomeDir = System.getProperty(USER_HOME);
            if (userHomeDir == null || userHomeDir.isEmpty()) {
                throw new BLangCompilerException("Error creating home repository: unable to get user home directory");
            }
            homeRepoPath = Paths.get(userHomeDir, ProjectConstants.HOME_REPO_DEFAULT_DIRNAME);
        } else {
            // User has specified the home repo path with env variable.
            homeRepoPath = Paths.get(homeRepoDir);
        }

        homeRepoPath = homeRepoPath.toAbsolutePath();
        if (Files.exists(homeRepoPath) && !Files.isDirectory(homeRepoPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new BLangCompilerException("Home repository is not a directory: " + homeRepoPath.toString());
        }
        return homeRepoPath;
    }

    /**
     * Check if a ballerina module exist.
     * @param projectPath project path
     * @param moduleName module name
     * @return module exist
     */
    public static boolean isModuleExist(Path projectPath, String moduleName) {
        Path modulePath = projectPath.resolve(ProjectConstants.MODULES_ROOT).resolve(moduleName);
        return Files.exists(modulePath);
    }

    /**
     * Initialize proxy if proxy is available in settings.toml.
     *
     * @param proxy toml model proxy
     * @return proxy
     */
    public static Proxy initializeProxy(org.ballerinalang.toml.model.Proxy proxy) {
        if (!"".equals(proxy.getHost())) {
            InetSocketAddress proxyInet = new InetSocketAddress(proxy.getHost(), proxy.getPort());
            if (!"".equals(proxy.getUserName()) && "".equals(proxy.getPassword())) {
                Authenticator authenticator = new URIDryConverter.RemoteAuthenticator();
                Authenticator.setDefault(authenticator);
            }
            return new Proxy(Proxy.Type.HTTP, proxyInet);
        }

        return null;
    }
}
