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

package io.ballerina.cli.task;

import io.ballerina.cli.utils.BuildTime;
import io.ballerina.projects.EmitResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.model.Target;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for creating bala file. Bala file writer is meant for modules only and not for single files.
 *
 * @since 2.0.0
 */
public class CreateBalaTask implements Task {
    private final transient PrintStream out;

    public CreateBalaTask(PrintStream out) {
        this.out = out;
    }

    @Override
    public void execute(Project project) {
        this.out.println();
        this.out.println("Creating bala");

        Target target;
        Path balaPath;
        try {
            target = project.getTarget();
            balaPath = target.getBalaPath();
        } catch (IOException | ProjectException e) {
            throw createLauncherException(e.getMessage());
        }

        JBallerinaBackend jBallerinaBackend;
        EmitResult emitResult;

        try {
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
            long start = 0;
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }
            emitResult = jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().emitArtifactDuration = System.currentTimeMillis() - start;
                BuildTime.getInstance().compile = true;
            }
        } catch (ProjectException e) {
            throw createLauncherException("BALA creation failed:" + e.getMessage());
        }

        // Print the path of the BALA file
        Path relativePathToExecutable = project.sourceRoot().relativize(emitResult.generatedArtifactPath());
        if (relativePathToExecutable.toString().contains("..") ||
                relativePathToExecutable.toString().contains("." + File.separator)) {
            this.out.println("\t" + emitResult.generatedArtifactPath().toString());
        } else {
            this.out.println("\t" + relativePathToExecutable.toString());
        }
    }

}
