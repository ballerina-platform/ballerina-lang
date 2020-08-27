/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.packerina.cmd;

import io.ballerina.projects.utils.FileUtils;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;
import org.ballerinalang.tool.util.BCompileUtil;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Packerina command util.
 *
 * @since 1.0.0
 */
public class CommandUtil {

    private static FileSystem jarFs;
    private static Map<String, String> env;

    public static void initJarFs() {
        URI uri = null;
        try {
            uri = CommandUtil.class.getClassLoader().getResource("create_cmd_templates").toURI();
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
     * Guess organization name based on user name in system.
     *
     * @return organization name
     */
    private static String guessOrgName() {
        String guessOrgName = System.getProperty("user.name");
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        return guessOrgName;
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
     * @param path Project path
     * @throws IOException If any IO exception occurred
     */
    public static void initProject(Path path) throws IOException {
            // We will be creating following in the project directory
            // - Ballerina.toml
            // - src/
            // - tests/
            // -- resources/      <- integration test resources
            // - .gitignore       <- git ignore file

            Path manifest = path.resolve("Ballerina.toml");
            Path src = path.resolve(ProjectDirConstants.SOURCE_DIR_NAME);
            //Path test = path.resolve("tests");
            //Path testResources = test.resolve("resources");
            Path gitignore = path.resolve(".gitignore");


            Files.createFile(manifest);
            Files.createFile(gitignore);
            Files.createDirectory(src);
            // todo need to enable integration tests
            //Files.createDirectory(test);
            //Files.createDirectory(testResources);

            String defaultManifest = BCompileUtil.readFileAsString("new_cmd_defaults/manifest.toml");
            String defaultGitignore = BCompileUtil.readFileAsString("new_cmd_defaults/gitignore");

            // replace manifest org with a guessed value.
            defaultManifest = defaultManifest.replaceAll("ORG_NAME", ProjectUtils.guessOrgName());

            Files.write(manifest, defaultManifest.getBytes("UTF-8"));
            Files.write(gitignore, defaultGitignore.getBytes("UTF-8"));
    }

    /**
     * Initialize a new ballerina project in the given path.
     * @param path project path
     * @param template package template
     * @throws IOException  If any IO exception occurred
     * @throws URISyntaxException If any URISyntaxException occurred
     */
    public static void initPackage(Path path, String template, PrintStream errStream) throws IOException,
            URISyntaxException {
        // We will be creating following in the project directory
        // - Ballerina.toml
        // - Package.md
        // - Module.md
        // - main.bal
        // - resources
        // - tests
        //      - main_test.bal
        //      - resources/
        // - .gitignore       <- git ignore file

        applyTemplate(path, template);
        Path manifest = path.resolve(ProjectConstants.BALLERINA_TOML);
        Path gitignore = path.resolve(".gitignore");

        String defaultManifest = FileUtils.readFileAsString("new_cmd_defaults" + File.separator +
                "manifest.toml");
        String defaultGitignore = FileUtils.readFileAsString("new_cmd_defaults" + File.separator + "gitignore");

        // replace manifest org and name with a guessed value.
        defaultManifest = defaultManifest.replaceAll("ORG_NAME", guessOrgName()).
                replaceAll("PKG_NAME", ProjectUtils.guessPkgName(path.getFileName().toString()));

        Files.write(manifest, defaultManifest.getBytes("UTF-8"));
        Files.write(gitignore, defaultGitignore.getBytes("UTF-8"));
    }

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

    private static Path getTemplatePath() throws URISyntaxException {
        try {
            URI uri = CommandUtil.class.getClassLoader().getResource("create_cmd_templates").toURI();
            if (uri.toString().contains("!")) {
                final String[] array = uri.toString().split("!");
                return jarFs.getPath(array[1]);
            } else {
                return Paths.get(uri);
            }
        } catch (URISyntaxException e) {
            throw new URISyntaxException("failed to get template path", e.getMessage());
        }
    }

    private static void applyTemplate(Path modulePath, String template) throws IOException, URISyntaxException {
        Path templateDir = getTemplatePath().resolve(template);

        try {
            Files.walkFileTree(templateDir, new FileUtils.Copy(templateDir, modulePath));
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
}
