package io.ballerina.cli.utils;

import io.ballerina.projects.AnyTarget;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.util.ProjectUtils;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code GraalVMCompatibilityUtils} has utilities to verify the compatibility of a package with GraalVM.
 *
 * @since 2201.7.0
 */
public class GraalVMCompatibilityUtils {
    private static boolean isPackageBelongsToTarget(io.ballerina.projects.Package pkg, String targetPlatform) {
        ResolvedPackageDependency resolvedPackageDependency = new ResolvedPackageDependency(pkg,
                PackageDependencyScope.DEFAULT);
        // 1) Check direct dependencies of imports in the package have any `ballerina/java` dependency
        for (ResolvedPackageDependency dependency :
                pkg.getCompilation().getResolution().dependencyGraph().getDirectDependencies(
                        resolvedPackageDependency)) {
            if (dependency.packageInstance().packageOrg().value().equals(
                    Names.BALLERINA_ORG.value) && dependency.packageInstance().packageName().value().equals(
                    Names.JAVA.value)) {
                return true;
            }
        }

        // 2) Check package has defined any platform dependency
        PackageManifest manifest = pkg.manifest();
        if ((manifest.platform(targetPlatform) != null &&
                !manifest.platform(targetPlatform).dependencies().isEmpty())) {
            return true;
        }
        return false;
    }

    /**
     * Get the GraalVM compatibility warning message for the given package.
     *
     * @param pkg            Package to be verified
     * @param targetPlatform Target platform
     * @return Warning message
     */
    public static String getGraalVMCompatibilityWarning(io.ballerina.projects.Package pkg, String targetPlatform) {
        // Verify that Java dependencies (if exist) of this package are GraalVM compatible
        if (isPackageBelongsToTarget(pkg, targetPlatform)) {
            PackageManifest.Platform platform = pkg.manifest().platform(targetPlatform);
            String packageName = pkg.manifest().name().value();

            if (platform == null || platform.graalvmCompatible() == null) {
                return String.format("warning: GraalVM compatibility is not defined for the package '%s'. " +
                        "To dismiss this warning," + " please verify that Java dependencies of this package are " +
                        "GraalVM compatible and add" + " 'graalvmCompatible = true' under '[platform.%s]' in the" +
                        " Ballerina.toml file.%n", packageName, targetPlatform);
            } else if (!platform.graalvmCompatible()) {
                return String.format("warning: Java dependencies of the package '%s' are not GraalVM compatible. " +
                        "This package may fail during execution on GraalVM.%n", packageName);
            }
        }
        return null;
    }


    /**
     * Get all GraalVM compatibility warnings of a package including it's dependencies.
     *
     * @param pkg            Package to get warnings
     * @param targetPlatform String
     * @return All warnings as a String
     */
    public static String getAllGraalVMCompatibilityWarnings(io.ballerina.projects.Package pkg, String targetPlatform) {
        StringBuilder warning = new StringBuilder();

        // Verify that Java dependencies (if exist) of this package are GraalVM compatible
        String packageWarning = getGraalVMCompatibilityWarning(pkg, targetPlatform);
        if (packageWarning != null) {
            warning.append(packageWarning);
        }
        // List all dependencies that are not GraalVM compatible
        Collection<ResolvedPackageDependency> allDependencies = pkg.getCompilation().getResolution().allDependencies();
        List<String> nonGraalVMCompatibleDependencies = new ArrayList<>();
        List<String> nonVerifiedDependencies = new ArrayList<>();

        allDependencies.stream().filter(dependency -> !ProjectUtils.isLangLibPackage(
                dependency.packageInstance().descriptor().org(),
                dependency.packageInstance().descriptor().name())).forEach(dependency -> {
            Map<String, PackageManifest.Platform> platformMap = dependency.packageInstance().manifest().platforms();
            Optional<Map.Entry<String, PackageManifest.Platform>> platform =
                    platformMap.entrySet().stream().findFirst();
            if (platform.isPresent() && !AnyTarget.ANY.code().equals(platform.get().getKey())) {
                Boolean isGraalVMCompatible = platform.get().getValue().graalvmCompatible();
                String dependencyName = dependency.packageInstance().descriptor().toString();
                if (isGraalVMCompatible == null) {
                    nonVerifiedDependencies.add(dependencyName);
                } else if (!isGraalVMCompatible) {
                    nonGraalVMCompatibleDependencies.add(dependencyName);
                }
            }
        });
        if (!nonVerifiedDependencies.isEmpty()) {
            warning.append("\nwarning: GraalVM compatibility is not defined for the following " +
                    "dependencies of this package. These packages may or may not be compatible with GraalVM.").append(
                            "\n\t").append(String.join("\n\t", nonVerifiedDependencies)).append("\n");
        }
        if (!nonGraalVMCompatibleDependencies.isEmpty()) {
            warning.append("\nwarning: The following dependencies of this package are not GraalVM " +
                    "compatible. This package may fail during execution on GraalVM.").append("\n\t").append(
                            String.join("\n\t", nonGraalVMCompatibleDependencies)).append("\n");
        }
        return warning.toString();
    }

}
