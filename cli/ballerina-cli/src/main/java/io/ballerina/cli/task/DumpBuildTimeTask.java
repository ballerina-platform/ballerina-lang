/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.cli.task;

import com.google.gson.Gson;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for generation build time file.
 *
 * @since 2.0.0
 */
public class DumpBuildTimeTask implements Task {
    private static final String BUILD_TIME_JSON = "build-time.json";
    private final transient PrintStream out;
    private final Path currentDir = Paths.get(System.getProperty("user.dir"));

    public DumpBuildTimeTask(PrintStream out) {
        this.out = out;
    }

    @Override
    public void execute(Project project) {
        if (project.buildOptions().dumpBuildTime()) {
            BuildTime.getInstance().totalDuration = System.currentTimeMillis() - BuildTime.getInstance().timestamp;
            BuildTime.getInstance().offline = project.buildOptions().offlineBuild();
            Path buildTimeFile = getBuildTimeFilePath(project);
            Path buildTimeFileRelativePath = Paths.get(System.getProperty("user.dir")).relativize(buildTimeFile);
            this.out.println("\nDumping build time information\n\t" + buildTimeFileRelativePath);
            persistBuildTimeToFile(buildTimeFile);
        }
    }

    private void persistBuildTimeToFile(Path filepath) {
        File jsonFile = new File(filepath.toString());
        try (FileOutputStream fileOutputStream = new FileOutputStream(jsonFile)) {
            try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                BuildTime buildTime = BuildTime.getInstance();
                String json = gson.toJson(buildTime);
                writer.write(new String(json.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                printBuildTime(buildTime);
            } catch (IOException e) {
                throw createLauncherException("couldn't write build time to file : " + e.getMessage());
            }
        } catch (IOException e) {
            throw createLauncherException("couldn't write build time to file : " + e.getMessage());
        }
    }

    private Path getBuildTimeFilePath(Project project) {
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            return project.sourceRoot().resolve("target").resolve(BUILD_TIME_JSON).toAbsolutePath();
        }
        return currentDir.resolve(BUILD_TIME_JSON).toAbsolutePath();
    }

    private void printBuildTime(BuildTime buildTime) {
        this.out.println("\ttimestamp : " + buildTime.timestamp);
        this.out.println("\toffline : " + buildTime.offline);
        this.out.println("\tcompile : " + buildTime.compile);
        this.out.println("\tprojectLoadDuration : " + buildTime.projectLoadDuration);
        this.out.println("\tpackageResolutionDuration : " + buildTime.packageResolutionDuration);
        this.out.println("\tpackageCompilationDuration : " + buildTime.packageCompilationDuration);
        this.out.println("\tcodeGenDuration : " + buildTime.codeGenDuration);
        this.out.println("\temitArtifactDuration : " + buildTime.emitArtifactDuration);
        this.out.println("\ttestingExecutionDuration : " + buildTime.testingExecutionDuration);
        this.out.println("\ttotalDuration : " + buildTime.totalDuration);
    }
}
