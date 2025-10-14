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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.projects.Project;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.ballerina.cli.cmd.CommandUtil.getSHA256Digest;
import static io.ballerina.cli.cmd.Constants.BACKUP;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;


public class CreateFingerprintTask implements Task {
    public static final String JAR = ".jar";
    private final boolean isTestExecution;
    private final boolean skipExecutable;

    public CreateFingerprintTask(boolean isTestExecution, boolean skipExecutable) {
        this.isTestExecution = isTestExecution;
        this.skipExecutable = skipExecutable;
    }

    @Override
    public void execute(Project project) {
        Path buildFilePath = project.targetDir().resolve(BUILD_FILE);
        if (!buildFilePath.toFile().exists()) {
            return;
        }
        try {
            BuildJson buildJson = readBuildJson(buildFilePath);
            buildJson.setBuildOptions(project.buildOptions());
            createFingerPrintForProjectFiles(project, buildJson, isTestExecution);
            createFingerPrintForArtifacts(project, buildJson, isTestExecution, skipExecutable);
            createFingerPrintForSettingsToml(buildJson);
            createFingerPrintForBallerinaToml(buildJson, project);
            writeBuildFile(buildFilePath, buildJson);
        } catch (JsonSyntaxException | IOException | NoSuchAlgorithmException e) {
            // ignore
        }
    }

    private void createFingerPrintForBallerinaToml(BuildJson buildJson, Project project) throws
            IOException, NoSuchAlgorithmException {
        File ballerinaTomlFile = project.sourceRoot().resolve(ProjectConstants.BALLERINA_TOML).toFile();
        BuildJson.FileMetaInfo ballerinaTomlMetaInfo = getFileMetaInfo(ballerinaTomlFile);
        buildJson.setBallerinaTomlMetaInfo(ballerinaTomlMetaInfo);
    }

    private static void createFingerPrintForProjectFiles(Project project, BuildJson buildJson, boolean isTestExecution)
            throws IOException, NoSuchAlgorithmException {
        List<BuildJson.FileMetaInfo> fileMetaInfoList = new ArrayList<>();
        List<File> srcToEvaluate = CommandUtil.getSrcFiles(project);
        for (File fileToEvaluate : srcToEvaluate) {
            fileMetaInfoList.add(getFileMetaInfo(fileToEvaluate));
        }
        buildJson.setSrcMetaInfo(fileMetaInfoList.toArray(new BuildJson.FileMetaInfo[0]));
        if (isTestExecution) {
            List<BuildJson.FileMetaInfo> testSrcMetaInfoList = new ArrayList<>();
            List<File> testSrcToEvaluate = CommandUtil.getTestSrcFiles(project);
            for (File fileToEvaluate : testSrcToEvaluate) {
                testSrcMetaInfoList.add(getFileMetaInfo(fileToEvaluate));
            }
            buildJson.setTestSrcMetaInfo(testSrcMetaInfoList.toArray(new BuildJson.FileMetaInfo[0]));
        }
    }

    private static void createFingerPrintForSettingsToml(BuildJson buildJson) throws IOException,
            NoSuchAlgorithmException {
        File settingsFile = RepoUtils.createAndGetHomeReposPath().resolve(ProjectConstants.SETTINGS_FILE_NAME)
                .toFile();
        if (!settingsFile.exists()) {
            buildJson.setSettingsMetaInfo(null);
            return;
        }
        buildJson.setSettingsMetaInfo(getFileMetaInfo(settingsFile));
    }

    private static void createFingerPrintForArtifacts(Project project, BuildJson buildJson, boolean isTestExecution,
                                                      boolean skipExecutable)
                                                        throws IOException, NoSuchAlgorithmException {
        Target target = new Target(project.targetDir().resolve(BACKUP));
        if (isTestExecution) {
            List<BuildJson.FileMetaInfo> testArtifactMetaInfoList = new ArrayList<>();
            File testSuiteFile = target.getTestsCachePath().resolve(ProjectConstants.TEST_SUITE_JSON).toFile();
            testArtifactMetaInfoList.add(getFileMetaInfo(testSuiteFile));

            String packageOrg = project.currentPackage().packageOrg().toString();
            String packageName = project.currentPackage().packageName().toString();
            String packageVersion = project.currentPackage().packageVersion().toString();
            List<Path> testJarArtifactsPath =
                    Files.walk(target.cachesPath().resolve(packageOrg).resolve(packageName).resolve(packageVersion))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(JAR))
                    .toList();
            for (Path testJarArtifactPath : testJarArtifactsPath) {
                testArtifactMetaInfoList.add(getFileMetaInfo(testJarArtifactPath.toFile()));
            }
            buildJson.setTestArtifactMetaInfo(testArtifactMetaInfoList.toArray(new BuildJson.FileMetaInfo[0]));
            return;
        }
        if (!skipExecutable) {
            File execFile = target.getExecutablePath(project.currentPackage()).toAbsolutePath().toFile();
            buildJson.setTargetExecMetaInfo(getFileMetaInfo(execFile));
        }
    }

    private static BuildJson.FileMetaInfo getFileMetaInfo(File execFile) throws IOException, NoSuchAlgorithmException {
        BuildJson.FileMetaInfo execMetaInfo = new BuildJson.FileMetaInfo();
        execMetaInfo.setFile(execFile.getAbsolutePath());
        execMetaInfo.setLastModifiedTime(Files.getLastModifiedTime(execFile.toPath()).toMillis());
        execMetaInfo.setSize(Files.size(execFile.toPath()));
        execMetaInfo.setHash(getSHA256Digest(execFile));
        return execMetaInfo;
    }

    private static void writeBuildFile(Path buildFilePath, BuildJson buildJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!buildFilePath.toFile().canWrite()) {
            return;
        }
        try {
            Files.write(buildFilePath, Collections.singleton(gson.toJson(buildJson)));
        } catch (IOException e) {
            // ignore
        }
    }
}
