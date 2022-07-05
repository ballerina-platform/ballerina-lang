/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.model.Target;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for generating docs using docerina.
 *
 * @since 2.0.0
 */
public class CreateDocsTask implements Task {

    private final transient PrintStream out;
    private Path outputPath;

    public CreateDocsTask(PrintStream out, Path outputPath) {
        this.out = out;
        this.outputPath = outputPath;
    }

    @Override
    public void execute(Project project) {
        Path sourceRootPath = project.targetDir();
        Target target;
        if (outputPath == null) {
            try {
                target = new Target(sourceRootPath);
                outputPath = target.getDocPath();
            } catch (IOException | ProjectException e) {
                throw createLauncherException("error occurred while generating docs: " + e.getMessage());
            }
        }
        this.out.println("Generating API Documentation");
        try {
            BallerinaDocGenerator.generateAPIDocs(project, outputPath.toString(), false);
            this.out.println("Saved to: " + sourceRootPath.relativize(outputPath).toString());

        } catch (IOException e) {
            throw createLauncherException("Unable to generate API Documentation.", e.getCause());
        }

    }
}
