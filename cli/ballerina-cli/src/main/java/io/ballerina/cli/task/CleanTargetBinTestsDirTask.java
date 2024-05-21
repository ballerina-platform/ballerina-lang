// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.cli.task;

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.model.Target;

import java.io.IOException;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Cleans up the target bin's tests directory.
 *
 * @since 2201.9.0
 */
public class CleanTargetBinTestsDirTask implements Task {
    @Override
    public void execute(Project project) {
        try {
            Target target = new Target(project.targetDir());
            target.cleanBinTests();
        } catch (IOException | ProjectException e) {
            throw createLauncherException("unable to clean the target bin's tests directory: " + e.getMessage());
        }
    }
}
