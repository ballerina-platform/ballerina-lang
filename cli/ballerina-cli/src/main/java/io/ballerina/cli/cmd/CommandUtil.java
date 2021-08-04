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
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.PackageJson;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectUtils.guessPkgName;
import static java.nio.file.Files.write;

/**
 * Packerina command util.
 *
 * @since 2.0.0
 */
public class CommandUtil {
    public static final String ORG_NAME = "ORG_NAME";
    public static final String PKG_NAME = "PKG_NAME";
    public static final String GITIGNORE = "gitignore";
    public static final String NEW_CMD_DEFAULTS = "new_cmd_defaults";
    public static final String CREATE_CMD_TEMPLATES = "create_cmd_templates";
    private static FileSystem jarFs;
    private static Map<String, String> env;
    private static PrintStream errStream;
    private static Path homeCache;

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

        if (CommandUtil.getTemplates().contains(template)) {
            applyTemplate(path, template);
            if (template.equalsIgnoreCase("lib")) {
                initLibPackage(path, packageName);
                Path source = path.resolve("lib.bal");
                Files.move(source, source.resolveSibling(guessPkgName(packageName) + ".bal"),
                        StandardCopyOption.REPLACE_EXISTING);

                String packageMd = FileUtils.readFileAsString(
                        NEW_CMD_DEFAULTS + "/" + ProjectConstants.PACKAGE_MD_FILE_NAME);

                write(path.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME),
                        packageMd.getBytes(StandardCharsets.UTF_8));
            } else {
                initPackage(path);
            }
            Path gitignore = path.resolve(ProjectConstants.GITIGNORE_FILE_NAME);
            if (Files.notExists(gitignore)) {
                Files.createFile(gitignore);
            }
            String defaultGitignore = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + GITIGNORE);
            write(gitignore, defaultGitignore.getBytes(StandardCharsets.UTF_8));
        } else {
            applyBalaTemplate(path, template);
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
     * @throws IOException if any IOException occurred
     * @throws URISyntaxException if any URISyntaxException occurred
     */
    public static void applyTemplate(Path modulePath, String template) throws IOException, URISyntaxException {
        Path templateDir = getTemplatePath().resolve(template);
        Files.walkFileTree(templateDir, new FileUtils.Copy(templateDir, modulePath));
    }

    /**
     * Apply the template to the created module.
     *
     * @param modulePath path to the module
     * @param template template name
     * @throws IOException if any IOException occurred
     * @throws URISyntaxException if any URISyntaxException occurred
     */
    public static void applyBalaTemplate(Path modulePath, String template) {
        // find all balas matching org and package name.
        Path balaTemplate = findBalaTemplate(template);
        String packageName = findPkgName(template);
        if (balaTemplate != null) {
            try {
                // Copy docs
                Path packageMDFilePath = balaTemplate.resolve("docs").resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
                Path toPackageMdPath = modulePath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
                Files.copy(packageMDFilePath, toPackageMdPath, StandardCopyOption.REPLACE_EXISTING);

                Path moduleMd = balaTemplate.resolve("docs").resolve("modules").resolve(packageName)
                        .resolve(ProjectConstants.MODULE_MD_FILE_NAME);
                Path toModuleMd = modulePath.resolve(ProjectConstants.MODULE_MD_FILE_NAME);
                if (Files.exists(moduleMd)) {
                    Files.copy(moduleMd, toModuleMd);
                }

                // Copy modules
                Path sourceModulesDir = balaTemplate.resolve("modules").resolve(packageName);
                if (Files.exists(sourceModulesDir)) {
                    Files.walkFileTree(sourceModulesDir, new FileUtils.Copy(sourceModulesDir, modulePath));
                }

                // Copy Package.json to Ballerina.toml
                Gson gson = new Gson();
                Path packageJsonPath = balaTemplate.resolve("package.json");
                try (FileReader packageReader = new FileReader(String.valueOf(packageJsonPath))) {
                    PackageJson packageJson = gson.fromJson(packageReader, PackageJson.class);
                    if (Files.exists(packageJsonPath)) {
                        Path balaToml = modulePath.resolve(ProjectConstants.BALLERINA_TOML);
                        Files.createFile(balaToml);
                        writeBallerinaToml(balaToml, packageJson);
                    }
                    // Copy Dependency-graph.json to Dependencies.toml
                    Path dependencyGraphJsonPath = balaTemplate.resolve("dependency-graph.json");
                    FileReader dependencyReader = new FileReader(String.valueOf(dependencyGraphJsonPath));
                    DependencyGraphJson dependencyGraphJson = gson.fromJson(dependencyReader,
                            DependencyGraphJson.class);
                    if (Files.exists(packageJsonPath)) {
                        Path dependenciesToml = modulePath.resolve(ProjectConstants.DEPENDENCIES_TOML);
                        Files.createFile(dependenciesToml);
                        writeDependenciesToml(dependenciesToml, dependencyGraphJson, packageName);
                    }
                }
            } catch (IOException e) {
                CommandUtil.printError(errStream,
                        "Error while applying template : " + e.getMessage(),
                        null,
                        false);
                Runtime.getRuntime().exit(1);
            }
        }
    }

    public static void writeBallerinaToml(Path balTomlPath, PackageJson packageJson) throws IOException {

            Files.writeString(balTomlPath, "[package]" , StandardOpenOption.APPEND);
            Files.writeString(balTomlPath, "\norg = \"" + packageJson.getOrganization() + "\"",
                    StandardOpenOption.APPEND);
            Files.writeString(balTomlPath, "\nname = \"" + packageJson.getName() + "\"", StandardOpenOption.APPEND);
            Files.writeString(balTomlPath, "\nversion = \"" + packageJson.getVersion() + "\"",
                    StandardOpenOption.APPEND);
            Files.writeString(balTomlPath, "\nexport = [" + packageJson.getExport().toString().replace("[", "\"")
                    .replace("]", "\"") + "]", StandardOpenOption.APPEND);
            Files.writeString(balTomlPath, "\nballerina_version = \"" + packageJson.getBallerinaVersion()
                    + "\"", StandardOpenOption.APPEND);
            Files.writeString(balTomlPath, "\nimplementation_vendor = \"" + packageJson.getImplementationVendor()
                    + "\"", StandardOpenOption.APPEND);
            Files.writeString(balTomlPath, "\nlanguage_spec_version = \"" + packageJson.getLanguageSpecVersion()
                    + "\"\n", StandardOpenOption.APPEND);

            Files.writeString(balTomlPath, "\n[[platform.java11.dependencies]]", StandardOpenOption.APPEND);
            JsonArray platformLibraries = packageJson.getPlatformDependencies();
            for (Object dependencies: platformLibraries) {
                JsonObject dependeciesObj = (JsonObject) dependencies;
                String path = dependeciesObj.get("path").getAsString();
                Files.writeString(balTomlPath, "\npath = \"" + path + "\"", StandardOpenOption.APPEND);

                String artifactId = dependeciesObj.get("artifactId").getAsString();
                Files.writeString(balTomlPath, "\nartifactId = \"" + artifactId + "\"", StandardOpenOption.APPEND);

                String groupId = dependeciesObj.get("groupId").getAsString();
                Files.writeString(balTomlPath, "\ngroupId = \"" + groupId + "\"", StandardOpenOption.APPEND);

                String version = dependeciesObj.get("version").getAsString();
                Files.writeString(balTomlPath, "\nversion = \"" + version + "\"\n", StandardOpenOption.APPEND);

            }
    }

    public static void writeDependenciesToml(Path dependenciesToml, DependencyGraphJson dependencyGraphJson,
                                             String packageName) throws IOException {

        List<Dependency> pkgDependency = dependencyGraphJson.getPackageDependencyGraph();
        Files.writeString(dependenciesToml, "[[dependencies]]" , StandardOpenOption.APPEND);
        for (int i = 0; i < pkgDependency.size(); i++) {
            if (!pkgDependency.get(i).getName().equals(packageName)) {
                Files.writeString(dependenciesToml, "\norg = \"" + pkgDependency.get(i).getOrg() + "\"",
                        StandardOpenOption.APPEND);
                Files.writeString(dependenciesToml, "\nname = \"" + pkgDependency.get(i).getName() + "\"",
                        StandardOpenOption.APPEND);
                Files.writeString(dependenciesToml, "\nversion = \"" + pkgDependency.get(i).getVersion() + "\"\n",
                        StandardOpenOption.APPEND);
            }
        }
    }

    public static Path findBalaTemplate(String template) {
        homeCache = RepoUtils.createAndGetHomeReposPath();
        String packageName = findPkgName(template);
        String[] orgSplit = template.split("/");
        String orgName = orgSplit[0].trim();
        String version = "*";
        String packagePart = (orgSplit.length > 1) ? orgSplit[1] : "";
        String[] pkgSplit = packagePart.split(":");
        version = (pkgSplit.length > 1) ? pkgSplit[1].trim() : version;

        String balaGlob = "glob:**/" + orgName + "/" + packageName + "/" + version + "/java11";
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(balaGlob);

        Path balaCache = Paths.get(String.valueOf(homeCache.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME).resolve(ProjectConstants.BALA_DIR_NAME)));

        try (Stream<Path> walk = Files.walk(balaCache)) {

            List<Path> balaList = walk
                    .filter(pathMatcher::matches)
                    .collect(Collectors.toList());

            Collections.sort(balaList);
            // get the latest
            if (balaList.size() > 0) {
                return balaList.get(balaList.size() - 1);
            } else {
                return null;
            }
        } catch (IOException e) {
            CommandUtil.printError(errStream,
                    "Unable to read home cache",
                    null,
                    false);
            Runtime.getRuntime().exit(1);
        }

        return homeCache.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME).resolve(ProjectConstants.BALA_DIR_NAME)
                .resolve(orgName).resolve(packageName).resolve(version).resolve("java11");
    }

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
     * Initialize a new ballerina project in the given path.
     *
     * @param path Project path
     * @throws IOException If any IO exception occurred
     */
    public static void initPackage(Path path) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-app.toml");
        write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));
    }

    private static void initLibPackage(Path path, String packageName) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-lib.toml");
        // replace manifest org and name with a guessed value.
        defaultManifest = defaultManifest.replaceAll(ORG_NAME, ProjectUtils.guessOrgName()).
                replaceAll(PKG_NAME, ProjectUtils.guessPkgName(packageName));

        write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));
    }
}
