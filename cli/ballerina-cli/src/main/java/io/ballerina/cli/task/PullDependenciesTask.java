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
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for pulling all the dependencies of a build project.
 *
 * @since 2201.4.0
 */
public class PullDependenciesTask implements Task {
    private final transient PrintStream err;
    private final boolean isPackageModified;

    public PullDependenciesTask(PrintStream err, boolean isPackageModified) {
        this.err = err;
        this.isPackageModified = isPackageModified;
    }

    @Override
    public void execute(Project project) {
        List<Diagnostic> diagnostics = new ArrayList<>();

        project.currentPackage().getResolution();

        if (!project.currentPackage().getResolution().diagnosticResult().hasErrors()) {
            if (isPackageModified) {
                DiagnosticResult codeGenAndModifyDiagnosticResult = project.currentPackage()
                        .runCodeGenAndModifyPlugins();
                if (codeGenAndModifyDiagnosticResult != null) {
                    diagnostics.addAll(codeGenAndModifyDiagnosticResult.diagnostics());
                }
            }
        }

        // Print diagnostics and exit when version incompatibility issues are found in package resolution.
        if (project.currentPackage().getResolution().diagnosticResult().hasErrors()) {
            // add resolution diagnostics
            diagnostics.addAll(project.currentPackage().getResolution().diagnosticResult().diagnostics());
            // add package manifest diagnostics
            diagnostics.addAll(project.currentPackage().manifest().diagnostics().diagnostics());
            // add dependency manifest diagnostics
            diagnostics.addAll(project.currentPackage().dependencyManifest().diagnostics().diagnostics());
            diagnostics.forEach(d -> err.println(d.toString()));
            throw createLauncherException("package resolution contains errors");
        }
    }
}
