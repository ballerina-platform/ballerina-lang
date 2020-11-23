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

import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.util.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for creating balo file. Balo file writer is meant for modules only and not for single files.
 *
 * @since 2.0.0
 */
public class CreateBaloTask implements Task {
    private final transient PrintStream out;

    public CreateBaloTask(PrintStream out) {
        this.out = out;
    }

    @Override
    public void execute(Project project) {
        this.out.println();
        this.out.println("Creating balos");

        Target target;
        Path baloPath;
        try {
            target = new Target(project.sourceRoot());
            baloPath = target.getBaloPath();
        } catch (IOException e) {
            throw createLauncherException("error occurred while writing the BALO: " + e.getMessage());
        }
        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        String baloName = ProjectUtils.getBaloName(
                project.currentPackage().packageOrg().toString(),
                project.currentPackage().packageName().toString(),
                project.currentPackage().packageVersion().toString(),
                null);

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JdkVersion.JAVA_11);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALO, baloPath.resolve(baloName));

        // Print the path of the BALO file
        Path relativePathToExecutable = project.sourceRoot().relativize(baloPath.resolve(baloName));
        if (relativePathToExecutable.toString().contains("..") ||
                relativePathToExecutable.toString().contains("." + File.separator)) {
            this.out.println("\t" + baloPath.resolve(baloName).toString());
        } else {
            this.out.println("\t" + relativePathToExecutable.toString());
        }
    }

}
