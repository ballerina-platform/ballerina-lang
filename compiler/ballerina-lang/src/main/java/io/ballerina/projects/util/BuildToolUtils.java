/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolConfig;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.DiagnosticCode;
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
import static io.ballerina.projects.util.ProjectUtils.CompatibleRange;

/**
 * Utility methods required for build tools.
 *
 * @since 2201.9.0
 */
public class BuildToolUtils {
    private BuildToolUtils() {}

    /**
     * Report a package diagnostic for tool command not found.
     *
     * @param commandName command/ subcommand name of the build tool
     * @return diagnostic
     */
    public static TomlDiagnostic getBuildToolCommandNotFoundDiagnostic(String commandName, TomlNodeLocation location) {
        String message = "Build tool command '" + commandName + "' not found";
        DiagnosticCode diagnosticCode = ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND;
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                diagnosticCode.diagnosticId(),
                diagnosticCode.messageKey(),
                DiagnosticSeverity.ERROR);
        return new TomlDiagnostic(location, diagnosticInfo, message);
    }

    /**
     * Report a package diagnostic for tool not resolved.
     *
     * @param toolId tool id of the build tool
     * @return diagnostic
     */
    public static TomlDiagnostic getCannotResolveBuildToolDiagnostic(String toolId, TomlNodeLocation location) {
        String message = "Build tool '" + toolId + "' cannot be resolved";
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(),
                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.messageKey(),
                DiagnosticSeverity.ERROR);
        return new TomlDiagnostic(location, diagnosticInfo, message);
    }

    /**
     * Select the class matching the name from the ServiceLoader.
     *
     * @param commandName name of the tool (tool type)
     * @param buildRunners service loader of CodeGeneratorTool interface
     * @return class that matches the tool name
     */
    public static Optional<CodeGeneratorTool> getTargetTool(
            String commandName, ServiceLoader<CodeGeneratorTool> buildRunners) {
        String[] subcommandNames = commandName.split("\\.");
        CodeGeneratorTool[] subcommands = buildRunners.stream()
                .map(ServiceLoader.Provider::get)
                .toArray(CodeGeneratorTool[]::new);
        return getTargetToolRec(subcommandNames, 0, subcommands);
    }

    /**
     * Return a diagnostic if the command name is invalid.
     *
     * @param fullCommandName the full name of the command. eg: health.fhir
     * @return diagnostic wrapped in Optional
     */
    public static Optional<TomlDiagnostic> getDiagnosticIfInvalidCommandName(
            String fullCommandName, TomlNodeLocation location) {
        String[] subcommandNames = fullCommandName.split("\\.");
        for (String subcommandName : subcommandNames) {
            ValidationStatus validationStatus = validateCommandName(subcommandName);
            if (validationStatus != ValidationStatus.VALID) {
                return Optional.of(getInvalidCommandNameDiagnostic(
                        fullCommandName, validationStatus.message(), location));
            }
        }
        return Optional.empty();
    }

    private static TomlDiagnostic getInvalidCommandNameDiagnostic(
            String cmdName, String reason, TomlNodeLocation location) {
        String message = "Command name '" + cmdName + "' is invalid because " + reason;
        DiagnosticCode diagnosticCode = ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND;
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                diagnosticCode.diagnosticId(), diagnosticCode.messageKey(), DiagnosticSeverity.ERROR);
        return new TomlDiagnostic(location, diagnosticInfo, message);
    }

    private static Optional<CodeGeneratorTool> getTargetToolRec(
            String[] subcommandNames, int level, CodeGeneratorTool[] commands) {
        for (CodeGeneratorTool buildRunner : commands) {
            Class<?> codeGeneratorToolClass = buildRunner.getClass();
            if (codeGeneratorToolClass.isAnnotationPresent(ToolConfig.class)) {
                ToolConfig toolConfig = codeGeneratorToolClass.getAnnotation(ToolConfig.class);
                if (toolConfig.name().equals(subcommandNames[level])) {
                    if (level + 1 == subcommandNames.length) {
                        if (toolConfig.hidden()) {
                            return Optional.empty();
                        }
                        return Optional.of(buildRunner);
                    }
                    CodeGeneratorTool[] subcommands = Arrays.stream(toolConfig.subcommands()).map(cmdClz -> {
                        try {
                            return cmdClz.getConstructor().newInstance();
                        } catch (ReflectiveOperationException e) {
                            throw new ProjectException("Error while fetching target tool: " + e);
                        }
                    }).toArray(CodeGeneratorTool[]::new);
                    return getTargetToolRec(subcommandNames, level + 1, subcommands);
                }
            }
        }
        return Optional.empty();
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
            Path centralBalaDirPath = getCentralBalaDirPath();
            Path balaPackagePath = centralBalaDirPath.resolve(org.value()).resolve(name.value());
            if (Files.exists(balaPackagePath)) {
                try (Stream<Path> versionFiles = Files.list(balaPackagePath)) {
                    versions.addAll(versionFiles.toList());
                }
            }
        } catch (IOException e) {
            throw new ProjectException("Error while accessing Distribution cache: " + e.getMessage());
        }
        versions.removeAll(getIncompatibleVersions(versions, org, name));
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
        CompatibleRange compatibleRange = ProjectUtils.getCompatibleRange(version, packageLockingMode);
        List<SemanticVersion> versionsInCompatibleRange = ProjectUtils.getVersionsInCompatibleRange(
                version, versions, compatibleRange);
        return getLatestVersion(versionsInCompatibleRange);
    }

    /**
     * Validate the name field of the ToolConfigAnnotation.
     *
     * @param cmdName name of the (sub)command
     * @return validation status
     */
    private static ValidationStatus validateCommandName(String cmdName) {
        ValidationStatus status = ValidationStatus.VALID;
        if (cmdName == null || cmdName.isEmpty()) {
            status = ValidationStatus.EMPTY;
        } else if (!cmdName.matches("^\\w+$")) {
            status = ValidationStatus.NON_ALPHANUMERIC;
        } else if (cmdName.startsWith("_")) {
            status = ValidationStatus.LEADING_UNDERSCORE;
        } else if (cmdName.endsWith("_")) {
            status = ValidationStatus.TRAILING_UNDERSCORE;
        } else if (cmdName.contains("__")) {
            status = ValidationStatus.CONSECUTIVE_UNDERSCORES;
        }
        return status;
    }

    /**
     * Get the path to the central bala directory.
     *
     * @return path to the central bala directory
     */
    public static Path getCentralBalaDirPath() {
        Path userHomeDirPath = RepoUtils.createAndGetHomeReposPath();
        return userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
    }


    /**
     * Get the location of the first tool entry in the Ballerina.toml file. If the tool entry is not found, return null.
     * This is used to get the diagnostic location for the tool entry error.
     *
     * @param toolId      tool id
     * @param toolEntries tool entries in Ballerina.toml
     * @return location of the first tool entry
     */
    public static TomlNodeLocation getFirstToolEntryLocation(String toolId, List<PackageManifest.Tool> toolEntries) {
        for (PackageManifest.Tool toolEntry : toolEntries) {
            if (toolEntry.type().value().equals(toolId)) {
                return toolEntry.type().location();
            }
        }
        return null;
    }

    private static List<Path> getIncompatibleVersions(List<Path> versions, PackageOrg org, PackageName name) {
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

    private static Path getPackagePath(String org, String name, String version) {
        Path centralBalaDirPath = getCentralBalaDirPath();
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
     * Denote all possible validation failures of a command name plus a valid status.
     */
    private enum ValidationStatus {
        EMPTY("command name should not be empty."),
        NON_ALPHANUMERIC("command name should only contain alphanumeric characters and underscores."),
        LEADING_UNDERSCORE("command name should not start with an underscore."),
        TRAILING_UNDERSCORE("command name should not end with an underscore."),
        CONSECUTIVE_UNDERSCORES("command name should not contain consecutive underscores."),
        VALID("Valid command name.");

        private final String message;

        ValidationStatus(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }
}
