/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.projects.util;

import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;

/**
 * Utility methods required for tools.
 *
 * @since 2201.9.0
 */
public class ToolUtils {
    private ToolUtils() {}

    /**
     * Report a package diagnostic for tool not found.
     *
     * @param toolId tool id of the build tool
     * @return diagnostic
     */
    public static PackageDiagnostic getBuildToolNotFoundDiagnostic(String toolId) {
        String message = "Build tool '" + toolId + "' not found";
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(), message, DiagnosticSeverity.ERROR);
        return new PackageDiagnostic(diagnosticInfo, toolId);
    }

    /**
     * Report a package diagnostic for tool not found.
     *
     * @param toolId tool id of the build tool
     * @return diagnostic
     */
    public static PackageDiagnostic getBuildToolOfflineResolveDiagnostic(String toolId) {
        String message = "Build tool '" + toolId + "' cannot be resolved. Please try again without '--offline' flag";
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(), message, DiagnosticSeverity.ERROR);
        return new PackageDiagnostic(diagnosticInfo, toolId);
    }

    /**
     * Validate the id field of a BalTool.toml.
     *
     * @param toolId id of a bal tool
     * @throws ProjectException an exception based on the validation violation
     */
    public static void validateBalToolId(String toolId) {
        StringBuilder errorMessage = new StringBuilder("Tool id should ");
        if (toolId == null || toolId.isEmpty()) {
            errorMessage.append("not be empty");
        } else if (!toolId.matches("^[a-zA-Z0-9_]+$")) {
            errorMessage.append("only contain alphanumeric characters and underscores");
        } else if (toolId.startsWith("_")) {
            errorMessage.append("not start with an underscore");
        } else if (toolId.endsWith("_")) {
            errorMessage.append("not end with an underscore");
        } else if (toolId.contains("__")) {
            errorMessage.append("not contain consecutive underscores");
        } else {
            return;
        }
        errorMessage.append(" in BalTool.toml file");
        throw new ProjectException(errorMessage.toString());
    }

    /**
     * Selects the class matching the name from the ServiceLoader.
     *
     * @param commandName name of the tool (tool type)
     * @param buildRunners service loader of CodeGeneratorTool interface
     * @return class that matches the tool name
     */
    public static Optional<CodeGeneratorTool> getTargetTool(
            String commandName, ServiceLoader<CodeGeneratorTool> buildRunners) {
        for (CodeGeneratorTool buildRunner : buildRunners) {
            if (ToolUtils.deriveSubcommandName(buildRunner.toolName()).equals(commandName)) {
                return Optional.of(buildRunner);
            }
        }
        return Optional.empty();
    }


    /**
     * Derive the fully qualified tool id using the array of strings passed.
     * Eg:- {"health", "fhir"} -> "health.fhir"
     *
     * @param toolName list of subcommands of a tool
     * @return the full qualified name of the tool
     */
    public static String deriveSubcommandName(String[] toolName) {
        return Arrays.stream(toolName).reduce((s1, s2) -> s1 + "." + s2).orElse("");
    }

    /**
     * Find the tool versions that reside in the central cache in user home, filter the tools compatible with the
     * current Ballerina distribution and return.
     *
     * @param org organization of the tool package
     * @param name package name of the tool package
     * @return list of all compatible tool versions
     */
    public static List<SemanticVersion> getCompatibleToolVersionsInLocalCache(PackageOrg org, PackageName name) {
        List<SemanticVersion> availableVersions = new ArrayList<>();
        List<Path> versions = new ArrayList<>();
        try {
            Path userHomeDirPath = RepoUtils.createAndGetHomeReposPath();
            Path centralBalaDirPath = userHomeDirPath.resolve(
                    Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
            Path balaPackagePath = centralBalaDirPath.resolve(org.value()).resolve(name.value());
            if (Files.exists(balaPackagePath)) {
                try (Stream<Path> versionFiles = Files.list(balaPackagePath)) {
                    versions.addAll(versionFiles.toList());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while accessing Distribution cache: " + e.getMessage());
        }
        versions.removeAll(getIncompatibleVersion(versions, org, name));
        versions.stream().map(path -> Optional.ofNullable(path)
                .map(Path::getFileName)
                .map(Path::toString)
                .orElse("0.0.0")).forEach(version -> {
            try {
                availableVersions.add(SemanticVersion.from(version));
            } catch (ProjectException ignored) {
            }
        });
        return availableVersions;
    }

    /**
     * Out of the passed list of tool versions, select the ones within the compatibility range of package locking mode,
     * and get the latest out of them.
     *
     * @param version currently recorded version
     * @param versions list of versions that are candidates for the latest version
     * @param packageLockingMode locking mode of the package
     * @return return the latest compatible version if any
     */
    public static Optional<SemanticVersion> getLatestCompatibleVersion(
            SemanticVersion version,
            List<SemanticVersion> versions,
            PackageLockingMode packageLockingMode) {
        CompatibleRange compatibleRange = getCompatibleRange(version, packageLockingMode);
        List<SemanticVersion> versionsInCompatibleRange = getVersionsInCompatibleRange(
                version, versions, compatibleRange);
        return getLatestVersion(versionsInCompatibleRange);
    }

    private static List<Path> getIncompatibleVersion(List<Path> versions, PackageOrg org, PackageName name) {
        List<Path> incompatibleVersions = new ArrayList<>();
        if (!versions.isEmpty()) {
            for (Path ver : versions) {
                Path pkgJsonPath = getPackagePath(org.value(), name.value(),
                        Optional.of(ver.getFileName()).get().toFile().getName()).resolve(ProjectConstants.PACKAGE_JSON);
                if (Files.exists(pkgJsonPath)) {
                    String packageVer = BalaFiles.readPkgJson(pkgJsonPath).getBallerinaVersion();
                    String packVer = RepoUtils.getBallerinaShortVersion();
                    if (!isCompatible(packageVer, packVer)) {
                        incompatibleVersions.add(ver);
                    }
                } else {
                    incompatibleVersions.add(ver);
                }
            }
        }
        return incompatibleVersions;
    }

    private static CompatibleRange getCompatibleRange(SemanticVersion version, PackageLockingMode packageLockingMode) {
        if (version == null) {
            return CompatibleRange.LATEST;
        }
        if (packageLockingMode.equals(PackageLockingMode.HARD)) {
            return CompatibleRange.EXACT;
        }
        if (packageLockingMode.equals(PackageLockingMode.MEDIUM) || version.isInitialVersion()) {
            return CompatibleRange.LOCK_MINOR;
        }
        // Locking mode SOFT
        return CompatibleRange.LOCK_MAJOR;
    }

    private static List<SemanticVersion> getVersionsInCompatibleRange(
            SemanticVersion minVersion,
            List<SemanticVersion> versions,
            CompatibleRange compatibleRange) {
        List<SemanticVersion> inRangeVersions = new ArrayList<>();
        if (compatibleRange.equals(CompatibleRange.LATEST)) {
            // If minVersion is null, range is LATEST
            inRangeVersions.addAll(versions);
        } else if (compatibleRange.equals(CompatibleRange.LOCK_MAJOR)) {
            inRangeVersions.addAll(
                    versions.stream().filter(version -> version.major() == minVersion.major()
            ).toList());
        } else if (compatibleRange.equals(CompatibleRange.LOCK_MINOR)) {
            inRangeVersions.addAll(
                    versions.stream().filter(
                            version -> version.major() == minVersion.major()
                            && version.minor() == minVersion.minor()
                    ).toList());
        } else if (versions.contains(minVersion)) {
            inRangeVersions.add(minVersion);
        }
        return inRangeVersions;
    }

    private static Path getPackagePath(String org, String name, String version) {
        Path userHomeDirPath = RepoUtils.createAndGetHomeReposPath();
        Path centralBalaDirPath = userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
        Path balaPath = centralBalaDirPath.resolve(
                ProjectUtils.getRelativeBalaPath(org, name, version, null));
        if (!Files.exists(balaPath)) {
            // If bala for any platform not exist check for specific platform
            for (JvmTarget jvmTarget : JvmTarget.values()) {
                balaPath = centralBalaDirPath.resolve(
                        ProjectUtils.getRelativeBalaPath(org, name, version, jvmTarget.code()));
                if (Files.exists(balaPath)) {
                    break;
                }
            }
        }
        return balaPath;
    }

    private static boolean isCompatible(String pkgBalVersion, String distBalVersion) {
        SemanticVersion pkgSemVer;
        SemanticVersion distSemVer;
        try {
            pkgSemVer = SemanticVersion.from(pkgBalVersion);
            distSemVer = SemanticVersion.from(distBalVersion);
        } catch (ProjectException ignore) {
            return false;
        }
        if (pkgSemVer.major() == distSemVer.major()) {
            if (pkgSemVer.minor() == distSemVer.minor()) {
                return true;
            }
            return !pkgSemVer.greaterThan(distSemVer);
        }
        return false;
    }

    private static Optional<SemanticVersion> getLatestVersion(List<SemanticVersion> versions) {
        if (versions.isEmpty()) {
            return Optional.empty();
        }
        SemanticVersion latestVersion = versions.get(0);
        for (SemanticVersion version: versions) {
            if (version.greaterThan(latestVersion)) {
                latestVersion = version;
            }
        }
        return Optional.of(latestVersion);
    }

    /**
     * Denote the compatibility range of a given tool version.
     */
    private enum CompatibleRange {
        /**
         * Latest stable (if any), else latest pre-release.
         */
        LATEST,
        /**
         * Latest minor version of the locked major version.
         */
        LOCK_MAJOR,
        /**
         * Latest patch version of the locked major and minor versions.
         */
        LOCK_MINOR,
        /**
         * Exact version provided.
         */
        EXACT
    }
}
