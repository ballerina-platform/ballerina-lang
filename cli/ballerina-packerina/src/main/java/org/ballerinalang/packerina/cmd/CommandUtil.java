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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Packerina command util.
 *
 * @since 0.991.0
 */
public class CommandUtil {

    private static final Path homePath = RepoUtils.createAndGetHomeReposPath();

    /**
     * Guess organization name based on user name in system.
     *
     * @return organization name
     */
    public static String guessOrgName() {
        String guessOrgName = System.getProperty("user.name");
        if (guessOrgName == null) {
            guessOrgName = "my_org";
        } else {
            guessOrgName = guessOrgName.toLowerCase(Locale.getDefault());
        }
        return guessOrgName;
    }

    /**
     * Find the project root by recursively up to the root.
     *
     * @param projectDir project path
     * @return project root
     */
    public static Path findProjectRoot(Path projectDir) {
        Path path = projectDir.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);
        if (Files.exists(path)) {
            return projectDir;
        }
        Path parentsParent = projectDir.getParent();
        if (null != parentsParent) {
            return findProjectRoot(parentsParent);
        }
        return null;
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
        stream.println("error: " + error);

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
     * Check if a ballerina module exist.
     * @param projectPath project path
     * @param moduleName module name
     * @return module exist
     */
    public static boolean isModuleExist(Path projectPath, String moduleName) {
        Path modulePath = projectPath.resolve(Constants.SOURCE_DIR).resolve(moduleName);
        return Files.exists(modulePath);
    }


    /**
     * Initialize a new ballerina project in the given path.
     *
     * @param path Project path
     */
    public static void initProject(Path path) throws IOException {
            // We will be creating following in the project directory
            // - Ballerina.toml
            // - src/
            // - tests/
            // -- resources/      <- integration test resources
            // - .gitignore       <- git ignore file

            Path manifest = path.resolve("Ballerina.toml");
            Path src = path.resolve(Constants.SOURCE_DIR);
            Path test = path.resolve("tests");
            Path testResources = test.resolve("resources");
            Path gitignore = path.resolve(".gitignore");


            Files.createFile(manifest);
            Files.createFile(gitignore);
            Files.createDirectory(src);
            Files.createDirectory(test);
            Files.createDirectory(testResources);

            String defaultManifest = BCompileUtil.readFileAsString("defaults/manifest.toml");
            String defaultGitignore = BCompileUtil.readFileAsString("defaults/gitignore");

            // replace manifest org with a guessed value.
            defaultManifest = defaultManifest.replaceAll("ORG_NAME", CommandUtil.guessOrgName());

            Files.write(manifest, defaultManifest.getBytes("UTF-8"));
            Files.write(gitignore, defaultGitignore.getBytes("UTF-8"));


    }

    /**
     * Check of given path is a valid ballerina project.
     * @param path project path
     * @return true if a project
     */
    public static boolean isProject(Path path) {
        return Files.exists(path.resolve(ProjectDirConstants.MANIFEST_FILE_NAME));
    }
}
