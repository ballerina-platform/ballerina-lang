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
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralClientConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.util.ProjectConstants.DOT;
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
    private final boolean isPackageModified;
    private final boolean cachesEnabled;

    public CompileTask(PrintStream out, PrintStream err) {
        this(out, err, false, false, true, false);
    }

    public CompileTask(PrintStream out,
                       PrintStream err,
                       boolean compileForBalPack,
                       boolean compileForBalBuild,
                       boolean isPackageModified,
                       boolean cachesEnabled) {
        this.out = out;
        this.err = err;
        this.compileForBalPack = compileForBalPack;
        this.compileForBalBuild = compileForBalBuild;
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

        try {
            printWarningForHigherDistribution(project);
            List<Diagnostic> diagnostics = new ArrayList<>();
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
                        if (this.isPackageModified || !this.cachesEnabled) {
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
            }

            // We dump the raw graphs twice only if code generator/modifier plugins are engaged
            // since the package has changed now

            Set<String> newPackageImports = ProjectUtils.getPackageImports(project.currentPackage());
            ResolutionOptions resolutionOptions = ResolutionOptions.builder().setOffline(true).build();
            if (!packageImports.equals(newPackageImports)) {
                resolutionOptions = ResolutionOptions.builder().setOffline(false).build();
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
                    if (!d.diagnosticInfo().code().startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX)) {
                        err.println(d);
                    }
                });
                throw createLauncherException("package resolution contains errors");
            }

            // Package resolution is successful. Continue compiling the package.
            if (project.buildOptions().dumpBuildTime()) {
                start = System.currentTimeMillis();
            }
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().packageCompilationDuration = System.currentTimeMillis() - start;
                start = System.currentTimeMillis();
            }
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_17);
            if (project.buildOptions().dumpBuildTime()) {
                BuildTime.getInstance().codeGenDuration = System.currentTimeMillis() - start;
            }

            // Report package compilation and backend diagnostics
            diagnostics.addAll(jBallerinaBackend.diagnosticResult().diagnostics(false));
            diagnostics.forEach(d -> {
                if (d.diagnosticInfo().code() == null || (!d.diagnosticInfo().code().equals(
                        ProjectDiagnosticErrorCode.BUILT_WITH_OLDER_SL_UPDATE_DISTRIBUTION.diagnosticId()) &&
                        !d.diagnosticInfo().code().startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX))) {
                    err.println(d);
                }
            });
            // Add tool resolution diagnostics to diagnostics
            diagnostics.addAll(project.currentPackage().getBuildToolResolution().getDiagnosticList());
            // Report build tool execution diagnostics
            if (project.getToolContextMap() != null) {
                for (ToolContext tool : project.getToolContextMap().values()) {
                    diagnostics.addAll(tool.diagnostics());
                }
            }
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

    /**
     * Prints the warning that explains the dependency update due to the detection of a new distribution.
     *
     * @param project project instance
     */
    private void printWarningForHigherDistribution(Project project) {
        SemanticVersion prevDistributionVersion = project.currentPackage().dependencyManifest().distributionVersion();
        SemanticVersion currentDistributionVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());

        if (project.currentPackage().dependencyManifest().dependenciesTomlVersion() != null) {
            String currentVersionForDiagnostic = String.valueOf(currentDistributionVersion.minor());
            if (currentDistributionVersion.patch() != 0) {
                currentVersionForDiagnostic += DOT + currentDistributionVersion.patch();
            }
            String prevVersionForDiagnostic;
            if (null != prevDistributionVersion) {
                prevVersionForDiagnostic = String.valueOf(prevDistributionVersion.minor());
                if (prevDistributionVersion.patch() != 0) {
                    prevVersionForDiagnostic += DOT + prevDistributionVersion.patch();
                }
            } else {
                prevVersionForDiagnostic = "4 or an older Update";
            }
            String warning = null;
            // existing project
            if (prevDistributionVersion == null
                    || ProjectUtils.isNewUpdateDistribution(prevDistributionVersion, currentDistributionVersion)) {
                // Built with a previous Update. Therefore, we issue a warning
                warning = "Detected an attempt to compile this package using Swan Lake Update "
                        + currentVersionForDiagnostic +
                        ". However, this package was built using Swan Lake Update " + prevVersionForDiagnostic + ".";
                if (project.buildOptions().sticky()) {
                    warning += "\nHINT: Execute the bal command with --sticky=false";
                } else {
                    warning += " To ensure compatibility, the Dependencies.toml file will be updated with the " +
                            "latest versions that are compatible with Update " + currentVersionForDiagnostic + ".";
                }
            }
            if (warning != null) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.BUILT_WITH_OLDER_SL_UPDATE_DISTRIBUTION.diagnosticId(),
                        warning, DiagnosticSeverity.WARNING);
                PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                        project.currentPackage().descriptor().name().toString());
                err.println(diagnostic);
            }
        }
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
}
