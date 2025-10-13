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
import io.ballerina.projects.CodeGeneratorResult;
import io.ballerina.projects.CodeModifierResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralClientConstants;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.internal.ProjectDiagnosticErrorCode.CORRUPTED_DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectConstants.TOOL_DIAGNOSTIC_CODE_PREFIX;

/**
 * Task for compiling a package.
 *
 * @since 2.0.0
 */
public class CompileTask implements Task {
    private final transient PrintStream out;
    private final transient PrintStream err;
    private final boolean compileForBalPack;
    private final boolean compileForBalBuild;
    private final boolean skipTask;
    private final List<Diagnostic> buildToolDiagnostics;

    public CompileTask(PrintStream out,
                       PrintStream err,
                       boolean compileForBalPack,
                       boolean compileForBalBuild) {
        this(out, err, compileForBalPack, compileForBalBuild, false, new ArrayList<>());
    }

    public CompileTask(PrintStream out,
                       PrintStream err,
                       boolean compileForBalPack,
                       boolean compileForBalBuild,
                       boolean skipTask, List<Diagnostic> buildToolDiagnostics) {
        this.out = out;
        this.err = err;
        this.compileForBalPack = compileForBalPack;
        this.compileForBalBuild = compileForBalBuild;
        this.skipTask = skipTask;
        this.buildToolDiagnostics = buildToolDiagnostics;
    }

    @Override
    public void execute(Project project) {
        if (ProjectUtils.isProjectEmpty(project) && skipCompilationForBalPack(project)) {
            throw createLauncherException("package is empty. Please add at least one .bal file.");
        }
        if (project.workspaceProject().isPresent()) {
            this.out.println();
        }
        this.out.println("Compiling source" + (skipTask ? " (UP-TO-DATE)" : ""));

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
        if (skipTask) {
            return;
        }
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");

        try {
            if (project.buildOptions().lockingMode() != PackageLockingMode.SOFT) {
                ProjectUtils.getWarningForHigherDistribution(project, project.buildOptions().rawLockingMode())
                        .ifPresent(err::println);
            }
            List<Diagnostic> diagnostics = new ArrayList<>(buildToolDiagnostics);
            if (this.compileForBalBuild) {
                addDiagnosticForProvidedPlatformLibs(project, diagnostics);
            }
            long start = 0;

            if (project.currentPackage().compilationOptions().dumpGraph()
                    || project.currentPackage().compilationOptions().dumpRawGraphs()) {
                this.out.println();
                this.out.println("Resolving dependencies");
            }

            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }
            Set<String> packageImports = ProjectUtils.getPackageImports(project.currentPackage());
            PackageResolution packageResolution = project.currentPackage().getResolution();
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().packageResolutionDuration = System.currentTimeMillis() - start;
            }

            // Print the old imports diagnostic, if any
            project.currentPackage().getResolution().diagnosticResult().diagnostics().stream().filter(
                    diagnostic -> diagnostic.diagnosticInfo().code().equals(
                            ProjectDiagnosticErrorCode.OLD_IMPORTS.diagnosticId())).forEach(err::println);

            if (project.currentPackage().compilationOptions().dumpRawGraphs()) {
                packageResolution.dumpGraphs(out);
            }

            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().codeGeneratorPluginDuration = 0;
                BuildTime.getInstance().codeModifierPluginDuration = 0;
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
                        // Run code gen and modify plugins, if project has updated only
                        CodeGeneratorResult codeGeneratorResult = project.currentPackage()
                                .runCodeGeneratorPlugins();
                        diagnostics.addAll(codeGeneratorResult.reportedDiagnostics().diagnostics());
                        if (project.buildOptions().dumpBuildTime()) {
                            BuildTime.getInstance().codeGeneratorPluginDuration =
                                    System.currentTimeMillis() - start;
                            start = System.currentTimeMillis();
                        }
                        CodeModifierResult codeModifierResult = project.currentPackage()
                                .runCodeModifierPlugins();
                        diagnostics.addAll(codeModifierResult.reportedDiagnostics().diagnostics());
                        if (project.buildOptions().dumpBuildTime()) {
                            BuildTime.getInstance().codeModifierPluginDuration =
                                    System.currentTimeMillis() - start;
                        }
                    }
                }
            }

            // We dump the raw graphs twice only if code generator/modifier plugins are engaged
            // since the package has changed now

            Set<String> newPackageImports = ProjectUtils.getPackageImports(project.currentPackage());
            ResolutionOptions resolutionOptions = ResolutionOptions.builder().setOffline(true).build();
            if (!packageImports.equals(newPackageImports)) {
                resolutionOptions = ResolutionOptions.builder()
                        .setOffline(false)
                        .setPackageLockingMode(project.buildOptions().lockingMode())
                        .build();
            }

            if (packageResolution != project.currentPackage().getResolution(resolutionOptions)) {
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
                diagnostics.forEach(d -> {
                    if (!d.diagnosticInfo().code().startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX)
                            && !d.diagnosticInfo().code().equals(
                                    ProjectDiagnosticErrorCode.OLD_IMPORTS.diagnosticId())) {
                        err.println(d);
                    }
                });
                throw createLauncherException("package resolution contains errors");
            }

            // Add corrupted dependencies toml diagnostic
            project.currentPackage().dependencyManifest().diagnostics().diagnostics().forEach(diagnostic -> {
                if (diagnostic.diagnosticInfo().code().equals(CORRUPTED_DEPENDENCIES_TOML.diagnosticId())) {
                    diagnostics.add(diagnostic);
                }
            });

            // Package resolution is successful. Continue compiling the package.
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }

            Optional<Diagnostic> projectLoadingDiagnostic = ProjectUtils.getProjectLoadingDiagnostic().stream().filter(
                    diagnostic -> diagnostic.diagnosticInfo().code().equals(
                            ProjectDiagnosticErrorCode.DEPRECATED_RESOURCES_STRUCTURE.diagnosticId())).findAny();

            projectLoadingDiagnostic.ifPresent(out::println);
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().packageCompilationDuration = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();
            }
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_21);
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().codeGenDuration = System.currentTimeMillis() - start;
            }

            // Report package compilation and backend diagnostics
            diagnostics.addAll(jBallerinaBackend.diagnosticResult().diagnostics(false));
            diagnostics.forEach(d -> {
                if (d.diagnosticInfo().code() == null || (!d.diagnosticInfo().code().equals(
                        ProjectDiagnosticErrorCode.BUILT_WITH_OLDER_SL_UPDATE_DISTRIBUTION.diagnosticId()) &&
                        !d.diagnosticInfo().code().startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX))
                        && !d.diagnosticInfo().code().equals(ProjectDiagnosticErrorCode.OLD_IMPORTS.diagnosticId())) {
                    err.println(d);
                }
            });
            boolean hasErrors = false;
            for (Diagnostic d : diagnostics) {
                if (d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR)) {
                    hasErrors = true;
                }
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

    private void addDiagnosticForProvidedPlatformLibs(Project project, List<Diagnostic> diagnostics) {
        Map<String, PackageManifest.Platform> platforms = project.currentPackage().manifest().platforms();
        for (PackageManifest.Platform javaPlatform : platforms.values()) {
            if (javaPlatform == null || javaPlatform.dependencies().isEmpty()) {
                continue;
            }
            for (Map<String, Object> dependency : javaPlatform.dependencies()) {
                if (Objects.equals(dependency.get("scope"), PlatformLibraryScope.PROVIDED.getStringValue())) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            ProjectDiagnosticErrorCode.INVALID_PROVIDED_SCOPE_IN_BUILD.diagnosticId(),
                            String.format("'%s' scope for platform dependencies is not allowed with package build%n",
                                    PlatformLibraryScope.PROVIDED.getStringValue()),
                            DiagnosticSeverity.ERROR);
                    diagnostics.add(new PackageDiagnostic(diagnosticInfo,
                            project.currentPackage().descriptor().name().toString()));
                    return;
                }
            }
        }
    }

    /**
     * If CompileTask is triggered by `bal pack` command, and project does not have CompilerPlugin.toml or BalTool.toml,
     * skip the compilation if project is empty. The project should be evaluated for emptiness before calling this.
     *
     * @param project project instance
     * @return true if compilation should be skipped, false otherwise
     */
    private boolean skipCompilationForBalPack(Project project) {
        return (!compileForBalPack || project.currentPackage().compilerPluginToml().isEmpty() &&
                project.currentPackage().balToolToml().isEmpty());
    }
}
