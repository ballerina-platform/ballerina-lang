/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.central.client.CentralClientConstants;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for creating the dependency graph.
 *
 * @since 2201.2.0
 */
public class CreateDependencyGraphTask implements Task {
    private final transient PrintStream err;

    public CreateDependencyGraphTask(PrintStream err) {
        this.err = err;
    }

    @Override
    public void execute(Project project) {

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");

        try {
            List<Diagnostic> diagnostics = new ArrayList<>();

            project.currentPackage().getResolution();

            if (!project.currentPackage().getResolution().diagnosticResult().hasErrors()) {
                if (!project.kind().equals(ProjectKind.BALA_PROJECT)) {
                    DiagnosticResult codeGenAndModifyDiagnosticResult = project.currentPackage()
                            .runCodeGenAndModifyPlugins();
                    if (codeGenAndModifyDiagnosticResult != null) {
                        diagnostics.addAll(codeGenAndModifyDiagnosticResult.diagnostics());
                    }
                }
            }

            if (project.currentPackage().getResolution().diagnosticResult().hasErrors()) {
                diagnostics.addAll(project.currentPackage().getResolution().diagnosticResult().diagnostics());
                diagnostics.forEach(d -> err.println(d.toString()));
                throw createLauncherException("package resolution contains errors");
            }
        } catch (ProjectException e) {
            throw createLauncherException("dependency graph resolution failed: " + e.getMessage());
        }
    }
}
