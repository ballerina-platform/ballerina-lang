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
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.SingleFileProject;
import org.ballerinalang.central.client.CentralClientConstants;

import java.io.PrintStream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for compiling a package.
 *
 * @since 2.0.0
 */
public class CompileTask implements Task {
    private final transient PrintStream out;
    private final transient PrintStream err;

    public CompileTask(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    @Override
    public void execute(Project project) {
        this.out.println("Compiling source");

        String sourceName;
        if (project instanceof SingleFileProject) {
            sourceName = project.currentPackage().getDefaultModule().document(
                    project.currentPackage().getDefaultModule().documentIds().iterator().next()).name();
        } else {
            sourceName = project.currentPackage().packageOrg().toString() + "/" +
                    project.currentPackage().packageName().toString() + ":" +
                    project.currentPackage().packageVersion();
        }
        // Print the source
        this.out.println("\t" + sourceName);

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");

        try {
            long start = 0;
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
                project.currentPackage().getResolution();
                BuildTime.getInstance().packageResolutionDuration = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();
            }

            // run built-in code generator compiler plugins
            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                // SingleFileProject cannot hold additional sources or resources
                // and BalaProjects is a read-only project.
                // Hence we run the code generators only for BuildProject
                project.currentPackage().runCodeGeneratorPlugins();
            }

            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().packageCompilationDuration = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();
            }
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().codeGenDuration = System.currentTimeMillis() - start;
            }
            DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();
            diagnosticResult.diagnostics(false).forEach(d -> err.println(d.toString()));
            if (diagnosticResult.hasErrors()) {
                throw createLauncherException("compilation contains errors");
            }
            project.save();
        } catch (ProjectException e) {
            throw createLauncherException("compilation failed: " + e.getMessage());
        }
    }
}
