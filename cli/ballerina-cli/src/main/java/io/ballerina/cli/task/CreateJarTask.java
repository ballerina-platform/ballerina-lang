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
import io.ballerina.projects.model.Target;

import java.io.IOException;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

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
            PackageCompilation pkgCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JdkVersion.JAVA_11);
            jBallerinaBackend.emit(JBallerinaBackend.OutputType.JAR, target.getJarCachePath());
            jBallerinaBackend.emit(JBallerinaBackend.OutputType.TESTABLE_JAR, target.getTestsCachePath());
        } catch (IOException e) {
            throw createLauncherException(
                    "error occurred while creating the JAR files for package " + e.getMessage());
        }
    }
}
