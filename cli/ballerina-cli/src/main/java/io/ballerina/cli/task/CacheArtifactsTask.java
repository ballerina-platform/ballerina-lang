/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.task;

import io.ballerina.projects.Project;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static io.ballerina.cli.cmd.CommandUtil.JAR;
import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.cli.cmd.Constants.RUN_COMMAND;
import static io.ballerina.cli.cmd.Constants.TEST_COMMAND;

public class CacheArtifactsTask implements Task {
    private final String currTask;
    private boolean skipExecutable = false;
    public CacheArtifactsTask(String currTask) {
        this.currTask = currTask;
    }

    public CacheArtifactsTask(String currTask, boolean skipExecutable) {
        this.currTask = currTask;
        this.skipExecutable = skipExecutable;
    }

    @Override
    public void execute(Project project) {
        try {
            Path targetDir = project.targetDir();
            Path backupDir = project.targetDir().resolve(ProjectConstants.EXEC_BACKUP_DIR_NAME);
            Target target = new Target(targetDir);
            if ((this.currTask.equals(BUILD_COMMAND) || this.currTask.equals(RUN_COMMAND)) && !skipExecutable) {
                copyFile(target.getExecutablePath(project.currentPackage()), targetDir, backupDir);
            } else if (this.currTask.equals(TEST_COMMAND)) {
                Path testSuitePath = target.getTestsCachePath().resolve(ProjectConstants.TEST_SUITE_JSON);
                if (Files.exists(testSuitePath)) {
                    copyFile(testSuitePath, targetDir, backupDir);
                }
                String packageOrg = project.currentPackage().packageOrg().toString();
                String packageName = project.currentPackage().packageName().toString();
                String packageVersion = project.currentPackage().packageVersion().toString();
                List<Path> testArtifactsPaths;
                try (var paths = Files.walk(
                        target.cachesPath().resolve(packageOrg).resolve(packageName).resolve(packageVersion))) {
                    testArtifactsPaths = paths
                            .filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(JAR))
                            .toList();
                }
                for (Path testArtifactPath : testArtifactsPaths) {
                    copyFile(testArtifactPath, targetDir, backupDir);
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private static void copyFile(Path sourceFilePath , Path sourceRootPath, Path destRootPath) throws IOException {
        Path relativePath = sourceRootPath.relativize(sourceFilePath);
        Path destFile = destRootPath.resolve(relativePath);
        Files.createDirectories(destFile.getParent());
        Files.copy(sourceFilePath, destFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
