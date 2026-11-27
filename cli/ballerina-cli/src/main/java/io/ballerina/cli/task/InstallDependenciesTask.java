/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.task;

import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Initializes the dependencies of a package: pulls them, compiles them into the dependency cache and writes
 * Dependencies.toml.
 * <p>
 * The package is compiled as in a build, so that the dependencies are compiled in the same compiler context, against
 * the same versions and into the same caches as in a build. Only errors in resolving or compiling the dependencies,
 * including imports of packages that cannot be resolved, fail the task. Other errors in the package itself are
 * reported by the build.
 *
 * @since 2201.14.2
 */
public class InstallDependenciesTask implements Task {

    private final transient PrintStream out;
    private final transient PrintStream err;

    public InstallDependenciesTask(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
    }

    @Override
    public void execute(Project project) {
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
        try {
            PackageResolution resolution = project.currentPackage().getResolution();
            if (resolution.diagnosticResult().hasErrors()) {
                resolution.diagnosticResult().errors().forEach(err::println);
                throw createLauncherException("package resolution contains errors");
            }

            List<Package> dependencies = resolution.dependencyGraph().getNodes().stream()
                    .map(ResolvedPackageDependency::packageInstance)
                    .filter(dependency -> dependency.project().kind() == ProjectKind.BALA_PROJECT)
                    .toList();
            long cached = dependencies.stream().filter(InstallDependenciesTask::isCached).count();

            // Compiling the package compiles its dependencies first, which caches their BIRs and JARs.
            PackageCompilation compilation = project.currentPackage().getCompilation();
            JBallerinaBackend backend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_25);
            Package currentPackage = project.currentPackage();
            List<Diagnostic> dependencyErrors = backend.diagnosticResult().errors().stream()
                    .filter(diagnostic -> isInDependency(diagnostic, dependencies)
                            || isUnresolvedDependency(diagnostic, currentPackage))
                    .toList();
            if (!dependencyErrors.isEmpty()) {
                dependencyErrors.forEach(err::println);
                throw createLauncherException("compilation of dependencies contains errors");
            }

            project.save();
            int count = dependencies.size();
            out.println("Installed " + count + (count == 1 ? " dependency" : " dependencies") + " ("
                    + (count - cached) + " compiled, " + cached + " already cached).");
        } catch (ProjectException e) {
            throw createLauncherException("dependency installation failed: " + e.getMessage());
        }
    }

    /** Whether every module of the dependency has a cached BIR. */
    private static boolean isCached(Package dependency) {
        CompilationCache cache = dependency.project().projectEnvironmentContext().getService(CompilationCache.class);
        for (ModuleId moduleId : dependency.moduleIds()) {
            if (cache.getBir(dependency.module(moduleId).moduleName()).length == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether the diagnostic reports an import of another package that cannot be resolved. Such a package would be
     * missing from Dependencies.toml. An import of a missing module of the package itself is an error in its code.
     */
    private static boolean isUnresolvedDependency(Diagnostic diagnostic, Package currentPackage) {
        if (!DiagnosticErrorCode.MODULE_NOT_FOUND.diagnosticId().equals(diagnostic.diagnosticInfo().code())) {
            return false;
        }
        String message = diagnostic.message();
        int start = message.indexOf('\'');
        int end = message.lastIndexOf('\'');
        if (start < 0 || end <= start) {
            return false;
        }
        String module = message.substring(start + 1, end);
        int orgSeparator = module.indexOf('/');
        if (orgSeparator < 0) {
            // An import without an organization is of a module of the package itself.
            return false;
        }
        String org = module.substring(0, orgSeparator);
        String moduleName = module.substring(orgSeparator + 1);
        String packageName = currentPackage.packageName().value();
        boolean isOwnModule = org.equals(currentPackage.packageOrg().value())
                && (moduleName.equals(packageName) || moduleName.startsWith(packageName + "."));
        return !isOwnModule;
    }

    /** Whether the diagnostic is reported in a source file of one of the dependencies. */
    private static boolean isInDependency(Diagnostic diagnostic, List<Package> dependencies) {
        if (diagnostic.diagnosticInfo().severity() != DiagnosticSeverity.ERROR) {
            return false;
        }
        Path file;
        try {
            file = Path.of(diagnostic.location().lineRange().fileName());
        } catch (InvalidPathException e) {
            return false;
        }
        if (!file.isAbsolute()) {
            return false;
        }
        file = file.normalize();
        Path dependencyFile = file;
        return dependencies.stream().anyMatch(dependency ->
                dependencyFile.startsWith(dependency.project().sourceRoot().toAbsolutePath().normalize()));
    }
}
