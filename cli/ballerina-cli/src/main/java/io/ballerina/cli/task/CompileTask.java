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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

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
        this.out.println();
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

        try {
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
            DiagnosticResult diagnosticResult = jBallerinaBackend.diagnosticResult();
            diagnosticResult.diagnostics().forEach(d -> err.println(convertDiagnosticToString(d)));
            if (diagnosticResult.hasErrors()) {
                throw createLauncherException("compilation contains errors");
            }
        } catch (ProjectException e) {
            throw createLauncherException("compilation failed: " + e.getMessage());
        }
    }

    private String convertDiagnosticToString(Diagnostic diagnostic) {
        LineRange lineRange = diagnostic.location().lineRange();

        LineRange oneBasedLineRange = LineRange.from(
                lineRange.filePath(),
                LinePosition.from(lineRange.startLine().line() + 1, lineRange.startLine().offset() + 1),
                LinePosition.from(lineRange.endLine().line() + 1, lineRange.endLine().offset() + 1));

        return diagnostic.diagnosticInfo().severity().toString() + " [" +
                oneBasedLineRange.filePath() + ":" + oneBasedLineRange + "] " + diagnostic.message();
    }
}
