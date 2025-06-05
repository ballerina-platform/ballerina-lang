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
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;


public class CreateFingerprintTask implements Task {

    @Override
    public void execute(Project project) {
        Path buildFilePath = project.targetDir().resolve(BUILD_FILE);
        if (!buildFilePath.toFile().exists()) {
            return;
        }
        try {
            List<BuildJson.FileMetaInfo> fileMetaInfoList = new ArrayList<>();
            BuildJson buildJson = readBuildJson(buildFilePath);
            buildJson.setBuildOptions(project.buildOptions());
            List<File> filesToEvaluate = CommandUtil.getAllProjectFiles(project);
            for (File fileToEvaluate : filesToEvaluate) {
                BuildJson.FileMetaInfo fileMetaInfo = new BuildJson.FileMetaInfo();
                fileMetaInfo.setFile(fileToEvaluate.getAbsolutePath());
                fileMetaInfo.setLastModifiedTime(Files.getLastModifiedTime(fileToEvaluate.toPath()).toMillis());
                fileMetaInfo.setSize(Files.size(fileToEvaluate.toPath()));
                fileMetaInfo.setHash(getSHA256Digest(fileToEvaluate));
                fileMetaInfoList.add(fileMetaInfo);
            }

            Target target = new Target(project.targetDir());
            File execFile = target.getExecutablePath(project.currentPackage()).toAbsolutePath().toFile();
            BuildJson.FileMetaInfo execMetaInfo = new BuildJson.FileMetaInfo();
            execMetaInfo.setFile(execFile.getAbsolutePath());
            execMetaInfo.setLastModifiedTime(Files.getLastModifiedTime(execFile.toPath()).toMillis());
            execMetaInfo.setSize(Files.size(execFile.toPath()));
            execMetaInfo.setHash(getSHA256Digest(execFile));
            buildJson.setTargetExecMetaInfo(execMetaInfo);
            buildJson.setSrcMetaInfo(fileMetaInfoList.toArray(new BuildJson.FileMetaInfo[0]));

            File settingsFile = RepoUtils.createAndGetHomeReposPath().resolve(ProjectConstants.SETTINGS_FILE_NAME)
                    .toFile();
            BuildJson.FileMetaInfo settingsMetaInfo = new BuildJson.FileMetaInfo();
            settingsMetaInfo.setFile(settingsFile.getAbsolutePath());
            settingsMetaInfo.setLastModifiedTime(Files.getLastModifiedTime(settingsFile.toPath()).toMillis());
            settingsMetaInfo.setSize(Files.size(settingsFile.toPath()));
            settingsMetaInfo.setHash(getSHA256Digest(settingsFile));
            buildJson.setSettingsMetaInfo(settingsMetaInfo);
            writeBuildFile(buildFilePath, buildJson);
        } catch (JsonSyntaxException | IOException | NoSuchAlgorithmException e) {
            // ignore
        }
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
