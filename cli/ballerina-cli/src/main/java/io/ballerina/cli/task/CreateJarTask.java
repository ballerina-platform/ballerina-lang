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
import io.ballerina.projects.model.Target;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.IOException;

/**
 * Task for creating jar file.
 *
 * @since 2.0.0
 */
public class CreateJarTask implements Task {

    @Override
    public void execute(Project project) {
        Target target;
        try {
            target = new Target(project.sourceRoot());
            String jarName = ProjectUtils.getJarName(project.currentPackage());
            project.currentPackage().getCompilation().emit(PackageCompilation.OutputType.JAR,
                    target.getJarCachePath());
        } catch (IOException e) {
            throw new RuntimeException(
                    "error occurred while creating the target directory at " + project.sourceRoot(), e);
        }
    }
}
