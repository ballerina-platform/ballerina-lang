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

import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.model.Target;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

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

        if (project instanceof BuildProject) {
            Target target;
            Path baloPath;
            try {
                target = new Target(project.sourceRoot());
                baloPath = target.getBaloPath();
            } catch (IOException e) {
                throw new RuntimeException("error occurred while writing the BALO:" + e.getMessage());
            }
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            String baloName = ProjectUtils.getBaloName(
                    project.currentPackage().packageOrg().toString(),
                    project.currentPackage().packageName().toString(),
                    project.currentPackage().packageVersion().toString(),
                    null);
            packageCompilation.emit(PackageCompilation.OutputType.BALO, baloPath.resolve(baloName));
        }
    }
}
