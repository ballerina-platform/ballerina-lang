/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDependency;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ProjectEnvironmentContext;
import io.ballerina.projects.model.Target;
import org.wso2.ballerinalang.util.Lists;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Task for creating the executable jar file.
 */
public class CreateExecutableTask implements Task {
    private final transient PrintStream out;
    private Path outputPath;

    private static HashSet<String> excludeExtensions = new HashSet<>(Lists.of("DSA", "SF"));

    public CreateExecutableTask(PrintStream out, Path outputPath) {
        this.out = out;
        this.outputPath = outputPath;
    }

    @Override
    public void execute(Project project) {
        Path executablePath;
        try {
            Target target = new Target(project.sourceRoot());
            if (outputPath != null) {
                target.setOutputPath(project.currentPackage(), outputPath);
            }
            executablePath = target.getExecutablePath(project.currentPackage());
            Files.copy(executablePath, this.outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw createLauncherException("unable to set executable path: " + e.getMessage());
        }

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        packageCompilation.emit(PackageCompilation.OutputType.EXEC, executablePath);

        Path relativePathToExecutable = project.sourceRoot().relativize(executablePath);
        if (relativePathToExecutable.toString().contains("..") ||
                relativePathToExecutable.toString().contains("." + File.separator)) {
            this.out.println("\t" + executablePath.toString());
        } else {
            this.out.println("\t" + relativePathToExecutable.toString());
        }
    }
}
