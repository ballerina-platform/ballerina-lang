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

import io.ballerina.projects.Package;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

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
        // - service.bal
        // - .gitignore       <- git ignore file
        // - .devcontainer.json

        applyTemplate(path, template);
        if (template.equalsIgnoreCase("lib")) {
            initLibPackage(path, packageName);
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
        Files.write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));
    }

    public static Path getJarPathFromHomeCache(Package pkg) {
        try {
            Path packageJarCacheDir = Files.createDirectories(getJarCacheFromHome()
                    .resolve(pkg.packageOrg().toString())
                    .resolve(pkg.packageName().toString())
                    .resolve(pkg.packageVersion().toString()));
            return packageJarCacheDir.resolve(ProjectUtils.getJarFileName(pkg) + BLANG_COMPILED_JAR_EXT);
        } catch (IOException e) {
            throw new BLangCompilerException("error resolving bir_cache dir for package: " + pkg.packageName());
        }
    }

    private static void initLibPackage(Path path, String packageName) throws IOException {
        Path ballerinaToml = path.resolve(ProjectConstants.BALLERINA_TOML);
        Files.createFile(ballerinaToml);

        String defaultManifest = FileUtils.readFileAsString(NEW_CMD_DEFAULTS + "/" + "manifest-lib.toml");
        // replace manifest org and name with a guessed value.
        defaultManifest = defaultManifest.replaceAll(ORG_NAME, ProjectUtils.guessOrgName())
                .replaceAll(PKG_NAME, ProjectUtils.guessPkgName(packageName))
                .replaceAll(DIST_VERSION, RepoUtils.getBallerinaShortVersion());

        Files.write(ballerinaToml, defaultManifest.getBytes(StandardCharsets.UTF_8));
    }

    public static Path getJarCacheFromHome() {
        return RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.JAR_CACHE_DIR_NAME + "-" +
                RepoUtils.getBallerinaVersion());
    }

}
