/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.cli.utils;

import io.ballerina.projects.AnyTarget;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.util.ProjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utilities to verify the compatibility of a package with GraalVM.
 *
 * @since 2201.7.0
 */
public class GraalVMCompatibilityUtils {

    private static boolean hasExternalPlatformDependencies(io.ballerina.projects.Package pkg, String targetPlatform) {
        // Check if external platform dependencies are defined
        PackageManifest manifest = pkg.manifest();
        return manifest.platform(targetPlatform) != null &&
                !manifest.platform(targetPlatform).dependencies().isEmpty();
    }

    /**
     * Get the GraalVM compatibility warning message for the given package.
     *
     * @param pkg            Package to be verified
     * @param targetPlatform Target platform
     * @return Warning message
     */
    public static String getWarningForPackage(io.ballerina.projects.Package pkg, String targetPlatform) {
        // Verify that Java dependencies (if exist) of this package are GraalVM compatible
        if (hasExternalPlatformDependencies(pkg, targetPlatform)) {
            PackageManifest.Platform platform = pkg.manifest().platform(targetPlatform);
            String packageName = pkg.manifest().name().value();

            if (platform == null || platform.graalvmCompatible() == null) {
                return String.format(
                        "************************************************************%n" +
                                "* WARNING: Package is not verified with GraalVM.           *%n" +
                                "************************************************************%n%n" +
                                "The GraalVM compatibility property has not been defined for the package '%s'. " +
                                "This could potentially lead to compatibility issues with GraalVM.%n%n" +
                                "To resolve this warning, please ensure that all Java dependencies of this package " +
                                "are compatible with GraalVM. Subsequently, update the Ballerina.toml file under " +
                                "the section '[platform.%s]' with the attribute 'graalvmCompatible = true'.%n%n" +
                                "************************************************************%n",
                        packageName, targetPlatform);
            } else if (!platform.graalvmCompatible()) {
                return String.format(
                        "************************************************************%n" +
                                "* WARNING: Package is not compatible with GraalVM.         *%n" +
                                "************************************************************%n%n" +
                                "The package '%s' has been marked with its GraalVM compatibility property set to " +
                                "false. This setting suggests potential compatibility issues with GraalVM.%n%n" +
                                "To ensure this package can function seamlessly with GraalVM, it's recommended to " +
                                "either modify the package dependencies or consider GraalVM-compatible alternatives" +
                                ".%n%n************************************************************%n", packageName);
            }
        }
        return null;
    }

    private static String getWarningForDependencies(io.ballerina.projects.Package pkg,
                                                    boolean isTestExec) {

        Collection<ResolvedPackageDependency> allDependencies = pkg.getCompilation().getResolution().allDependencies();
        List<ResolvedPackageDependency> nonGraalVMCompatibleDependencies = new ArrayList<>();
        List<ResolvedPackageDependency> nonVerifiedDependencies = new ArrayList<>();

        allDependencies.stream().filter(dependency -> !ProjectUtils.isLangLibPackage(
                dependency.packageInstance().descriptor().org(),
                dependency.packageInstance().descriptor().name())).forEach(dependency -> {
            Map<String, PackageManifest.Platform> platformMap = dependency.packageInstance().manifest().platforms();
            Optional<Map.Entry<String, PackageManifest.Platform>> platform =
                    platformMap.entrySet().stream().findFirst();
            if (platform.isPresent() && !AnyTarget.ANY.code().equals(platform.get().getKey())) {
                Boolean isGraalVMCompatible = platform.get().getValue().graalvmCompatible();
                if (isGraalVMCompatible == null) {
                    if (!platform.get().getValue().dependencies().isEmpty()) {
                        nonVerifiedDependencies.add(dependency);
                    }
                } else if (!isGraalVMCompatible) {
                    nonGraalVMCompatibleDependencies.add(dependency);
                }
            }
        });
        return getDependenciesWarningMessage(getDependenciesList(nonGraalVMCompatibleDependencies, isTestExec),
                getDependenciesList(nonVerifiedDependencies, isTestExec));
    }

    /**
     * Get all the applicable warning messages for GraalVM compatibility of the package.
     *
     * @param pkg            Package to be verified
     * @param targetPlatform Target platform
     * @param isTestExec     Whether it is a test execution
     * @return Warning message
     */
    public static String getAllWarnings(io.ballerina.projects.Package pkg, String targetPlatform, boolean isTestExec) {
        StringBuilder warnings = new StringBuilder();
        // Verify that Java dependencies (if exist) of this package are GraalVM compatible
        String packageWarning = getWarningForPackage(pkg, targetPlatform);
        if (packageWarning != null) {
            warnings.append(packageWarning);
        }
        // List all dependencies that are not GraalVM compatible
        String dependencyWarning = getWarningForDependencies(pkg, isTestExec);
        if (dependencyWarning != null) {
            if (warnings.length() > 0) {
                warnings.append(System.lineSeparator());
            }
            warnings.append(dependencyWarning);
        }
        return warnings.toString();
    }


    private static String getDependenciesWarningMessage(String nonGraalVMCompatibleDependencies,
                                                        String nonVerifiedDependencies) {
        // If there are no non-GraalVM compatible dependencies, return null
        if (nonGraalVMCompatibleDependencies.isEmpty() && nonVerifiedDependencies.isEmpty()) {
            return null;
        }
        StringBuilder warning = new StringBuilder(
                String.format("*************************************************************%n" +
                        "* WARNING: Some dependencies may not be GraalVM compatible. *%n" +
                        "*************************************************************%n%n" +
                        "The following Ballerina dependencies in your project could pose compatibility issues " +
                        "with GraalVM.%n"));
        if (!nonVerifiedDependencies.isEmpty()) {
            warning.append("\nPackages pending compatibility verification with GraalVM:");
            warning.append(nonVerifiedDependencies);
            warning.append("\n");
        }
        if (!nonGraalVMCompatibleDependencies.isEmpty()) {
            warning.append("\nPackages marked as incompatible with GraalVM:");
            warning.append(nonGraalVMCompatibleDependencies);
            warning.append("\n");
        }
        warning.append(String.format("%nPlease note that generating a GraalVM native image may fail or result in " +
                "runtime issues if these packages rely on features that are not supported by GraalVM's native " +
                "image generation process.%n%nIt is recommended to review the API documentation or contact the " +
                "maintainers of these packages for more information on their GraalVM compatibility status. " +
                "You may need to adjust or find alternatives for these packages before proceeding with GraalVM " +
                "native image generation.%n%n************************************************************%n"));
        return warning.toString();
    }

    private static String getDependenciesList(List<ResolvedPackageDependency> dependencies, boolean includeAllScopes) {
        StringBuilder dependenciesList = new StringBuilder();
        for (ResolvedPackageDependency dependency : dependencies) {
            if (!includeAllScopes && PackageDependencyScope.TEST_ONLY.equals(dependency.scope())) {
                continue;
            }
            dependenciesList.append(System.lineSeparator())
                    .append(getDependencyDetail(dependency));
        }
        return dependenciesList.toString();
    }

    private static String getDependencyDetail(ResolvedPackageDependency dependency) {
        StringBuilder dependencyDetail = new StringBuilder("\t" + dependency.packageInstance().descriptor().toString());
        if (PackageDependencyScope.TEST_ONLY.equals(dependency.scope())) {
            dependencyDetail.append(" [scope=").append(PackageDependencyScope.TEST_ONLY.getValue()).append("]");
        }
        return dependencyDetail.toString();
    }

}
