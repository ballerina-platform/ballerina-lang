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
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralClientConstants;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Task for compiling a package.
 *
 * @since 2.0.0
 */
public class CompileTask implements Task {
    private final transient PrintStream out;
    private final transient PrintStream err;
    private final boolean compileForBalPack;
    private final boolean isPackageModified;
    private final boolean cachesEnabled;

    public CompileTask(PrintStream out, PrintStream err) {
        this(out, err, false, true, false);
    }

    public CompileTask(PrintStream out,
                       PrintStream err,
                       boolean compileForBalPack,
                       boolean isPackageModified,
                       boolean cachesEnabled) {
        this.out = out;
        this.err = err;
        this.compileForBalPack = compileForBalPack;
        this.isPackageModified = isPackageModified;
        this.cachesEnabled = cachesEnabled;
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

        List<Diagnostic> diagnostics = new ArrayList<>();

        try {
            long start = 0;
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }

            if (project.currentPackage().compilationOptions().dumpGraph()
                    || project.currentPackage().compilationOptions().dumpRawGraphs()) {
                this.out.println();
                this.out.println("Resolving dependencies");
            }

            PackageResolution packageResolution = project.currentPackage().getResolution();

            if (project.currentPackage().compilationOptions().dumpRawGraphs()) {
                packageResolution.dumpGraphs(out);
            }

            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().packageResolutionDuration = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();
            }

            // run built-in code generator compiler plugins
            // We only continue with next steps if package resolution does not have errors.
            // Errors in package resolution denotes version incompatibility errors. Hence, we do not continue further.
            if (!project.currentPackage().getResolution().diagnosticResult().hasErrors()) {
                if (!project.kind().equals(ProjectKind.BALA_PROJECT)) {
                    // BalaProject is a read-only project.
                    // Hence, we run the code generators/ modifiers only for BuildProject and SingleFileProject

                    if (!project.kind().equals(ProjectKind.BALA_PROJECT) && !isPackCmdForATemplatePkg(project)) {
                        // SingleFileProject cannot hold additional sources or resources
                        // and BalaProjects is a read-only project.r
                        // Hence, we run the code generators only for BuildProject.
                        if (this.isPackageModified || !this.cachesEnabled) {
                            // Run code gen and modify plugins, if project has updated only
                            DiagnosticResult codeGenAndModifyDiagnosticResult = project.currentPackage()
                                    .runCodeGenAndModifyPlugins();
                            if (codeGenAndModifyDiagnosticResult != null) {
                                diagnostics.addAll(codeGenAndModifyDiagnosticResult.diagnostics());
                            }
                        }
                    }
                }
            }

            // We dump the raw graphs twice only if code generator/modifier plugins are engaged
            // since the package has changed now
            if (packageResolution != project.currentPackage().getResolution()) {
                packageResolution = project.currentPackage().getResolution();
                if (project.currentPackage().compilationOptions().dumpRawGraphs()) {
                    packageResolution.dumpGraphs(out);
                }
            }
            if (project.currentPackage().compilationOptions().dumpGraph()) {
                packageResolution.dumpGraphs(out);
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

            // Package resolution is successful. Continue compiling the package.
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().packageCompilationDuration = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();
            }
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().codeGenDuration = System.currentTimeMillis() - start;
            }

            // Report package compilation and backend diagnostics
            diagnostics.addAll(jBallerinaBackend.diagnosticResult().diagnostics(false));
            boolean hasErrors = false;
            for (Diagnostic d : diagnostics) {
                if (d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR)) {
                    hasErrors = true;
                }
                err.println(d.toString());
            }
            if (hasErrors) {
                throw createLauncherException("compilation contains errors");
            }
            project.save();
        } catch (ProjectException e) {
            throw createLauncherException("compilation failed: " + e.getMessage());
        }
    }

    private boolean isPackCmdForATemplatePkg(Project project) {
        return compileForBalPack && project.currentPackage().manifest().template();
    }
}
