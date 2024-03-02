/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildTool;
import io.ballerina.projects.Diagnostics;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.ToolResolution;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ToolUtils;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.launcher.util.BalToolsUtil.findJarFiles;
import static io.ballerina.projects.JBallerinaBalaWriter.LIBS;
import static io.ballerina.projects.JBallerinaBalaWriter.TOOL;
import static io.ballerina.projects.PackageManifest.Tool;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectConstants.TOOL_DIAGNOSTIC_CODE_PREFIX;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * Task for running tools integrated with the build.
 *
 * @since 2201.9.0
 */
public class RunBallerinaPreBuildToolsTask implements Task {
    public static final String DEFAULT_VERSION = "0.0.0";
    private final PrintStream outStream;
    private ClassLoader toolClassLoader = this.getClass().getClassLoader();
    ServiceLoader<CodeGeneratorTool> toolServiceLoader = ServiceLoader.load(CodeGeneratorTool.class, toolClassLoader);
    private final Map<String, ToolContext> toolContextMap = new HashMap<>();

    public RunBallerinaPreBuildToolsTask(PrintStream out) {
        this.outStream = out;
    }

    @Override
    public void execute(Project project) {
        project.setToolContextMap(toolContextMap);

        // Print all build tool manifest diagnostics
        Collection<Diagnostic> toolManifestDiagnostics = project.currentPackage().manifest().diagnostics()
                .diagnostics().stream().filter(diagnostic -> diagnostic.diagnosticInfo().code()
                        .startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX)).toList();
        toolManifestDiagnostics.forEach(outStream::println);

        // Read the build tool entries specified the Ballerina.toml
        List<Tool> toolEntries = project.currentPackage().manifest().tools();
        if (toolEntries.isEmpty()) {
            return;
        }
        this.outStream.println("\nExecuting Build Tools");

        // Populate the tool context map
        for (Tool toolEntry : toolEntries) {
            // Populate tool context
            ToolContext toolContext = ToolContext.from(toolEntry, project.currentPackage());
            toolContextMap.put(toolEntry.id(), toolContext);
        }
        ToolResolution toolResolution = project.currentPackage().getToolResolution();
        toolResolution.getDiagnosticList().forEach(outStream::println);
        List<BuildTool> resolvedTools = toolResolution.getResolvedTools();
        List<BuildTool> centralDeliveredResolvedTools = resolvedTools.stream().filter(tool -> !DEFAULT_VERSION
                .equals(tool.version().toString())).toList();
        if (!centralDeliveredResolvedTools.isEmpty()) {
            pullLocallyUnavailableTools(centralDeliveredResolvedTools);
            toolClassLoader = createToolClassLoader(centralDeliveredResolvedTools);
            toolServiceLoader = ServiceLoader.load(CodeGeneratorTool.class, toolClassLoader);
        }

        for (Tool toolEntry : toolEntries) {
            String commandName = toolEntry.type();
            ToolContext toolContext = toolContextMap.get(toolEntry.id());
            Optional<CodeGeneratorTool> targetTool = ToolUtils.getTargetTool(commandName, toolServiceLoader);
            if (targetTool.isEmpty()) {
                PackageDiagnostic diagnostic = ToolUtils.getBuildToolNotFoundDiagnostic(toolEntry.id());
                toolContextMap.get(toolEntry.id()).reportDiagnostic(diagnostic);
                this.outStream.println(diagnostic);
                continue;
            }
            boolean hasOptionErrors = false;
            try {
                // validate the options toml and report diagnostics
                hasOptionErrors = validateOptionsToml(toolEntry.optionsToml(), commandName);
                if (hasOptionErrors) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.diagnosticId(),
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.messageKey(),
                            DiagnosticSeverity.ERROR);
                    PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo, toolEntry.type());
                    toolContext.reportDiagnostic(diagnostic);
                }
            } catch (IOException e) {
                // if there is no options toml, we warn the user and continue
                outStream.printf("WARNING: Skipping validation of tool options for tool %s(%s) " +
                        "due to: %s%n", toolEntry.type(), toolEntry.id(), e.getMessage());
            }
            // if manifest errors or options table validation errors
            if (toolEntry.hasErrorDiagnostic() || hasOptionErrors) {
                outStream.printf("WARNING: Skipping execution of build tool %s(%s) as Ballerina.toml " +
                        "contains errors%n%n", toolEntry.type(), toolEntry.id() != null ? toolEntry.id() : "");
                continue;
            }

            // Execute the build tool
            try {
                this.outStream.printf("\t%s(%s)%n%n", toolEntry.type(), toolEntry.id());
                targetTool.get().execute(toolContext);
                for (Diagnostic d : toolContext.diagnostics()) {
                    if (d.toString().contains("(1:1,1:1)")) {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), toolContext.toolId()));
                    } else {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), d.location()));
                    }
                }
                toolContextMap.put(toolEntry.id(), toolContext);
            } catch (Exception e) {
                throw createLauncherException(e.getMessage());
            }
        }
    }

    private boolean validateOptionsToml(Toml optionsToml, String toolName) throws IOException {
        if (optionsToml == null) {
            return validateEmptyOptionsToml(toolName);
        }
        FileUtils.validateToml(optionsToml, toolName, toolClassLoader);
        optionsToml.diagnostics().forEach(outStream::println);
        return !Diagnostics.filterErrors(optionsToml.diagnostics()).isEmpty();
    }

    private boolean validateEmptyOptionsToml(String toolName) throws IOException {
        Schema schema = Schema.from(FileUtils.readSchema(toolName, toolClassLoader));
        List<String> requiredFields = schema.required();
        if (!requiredFields.isEmpty()) {
            for (String field: requiredFields) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML.diagnosticId(),
                        String.format("missing required optional field '%s'", field),
                        DiagnosticSeverity.ERROR);
                PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                        toolName);
                this.outStream.println(diagnostic);
            }
            return true;
        }
        this.outStream.printf("WARNING: Skipping validation of tool options for tool %s due to: " +
                "No tool options found for%n", toolName);
        return false;
    }

    private void pullLocallyUnavailableTools(List<BuildTool> tools) {
        for (BuildTool tool: tools) {
            String toolId = tool.id().value();
            String version = tool.version().toString();
            String org = tool.org().value();
            String name = tool.name().value();
            try {
                if (isToolLocallyAvailable(org, name, version)) {
                    continue;
                }
                pullToolFromCentral(toolId, version);
            } catch (CentralClientException e) {
                throw createLauncherException(
                        "failed to pull build tool '" + toolId + ":" + version + "' from Ballerina Central: "
                                + e.getMessage());
            } catch (ProjectException e) {
                throw createLauncherException(
                        "failed to resolve build tool '" + toolId + ":" + version + "' from the local cache: "
                                + e.getMessage());
            }
        }
    }

    private boolean isToolLocallyAvailable(String org, String name, String version) {
        Path toolCacheDir = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME).resolve(org).resolve(name);
        if (toolCacheDir.toFile().isDirectory()) {
            try (Stream<Path> versions = Files.list(toolCacheDir)) {
                return versions.anyMatch(path -> path.getFileName().toString().equals(version));
            } catch (IOException e) {
                throw new ProjectException("Error while looking for locally available tools: " + e);
            }
        }
        return false;
    }

    private void pullToolFromCentral(String toolId, String version) throws CentralClientException {
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        Path balaCacheDirPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME);
        Settings settings;
        try {
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the pull command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, Boolean.TRUE.toString());
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout());
        String[] toolInfo = client.pullTool(toolId, version, balaCacheDirPath, supportedPlatform,
                RepoUtils.getBallerinaVersion(), false);
        boolean isPulled = Boolean.parseBoolean(toolInfo[0]);
        if (isPulled) {
            outStream.println("tool '" + toolId + ":" + version + "' pulled successfully.");
        } else {
            outStream.println("tool '" + toolId + ":" + version + "' is already available locally.");
        }
    }

    private ClassLoader createToolClassLoader(List<BuildTool> resolvedTools) {
        List<File> toolJars = getToolCommandJarAndDependencyJars(resolvedTools);
        URL[] urls = toolJars.stream()
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw LauncherUtils.createUsageExceptionWithHelp("invalid tool jar: " + file
                                .getAbsolutePath());
                    }
                })
                .toArray(URL[]::new);
        ClassLoader systemClassLoader = this.getClass().getClassLoader();
        return new URLClassLoader(urls, systemClassLoader);
    }

    private static List<File> getToolCommandJarAndDependencyJars(List<BuildTool> resolvedTools) {
        Path userHomeDirPath = RepoUtils.createAndGetHomeReposPath();
        Path centralBalaDirPath = userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
        return resolvedTools.stream()
                .map(tool -> findJarFiles(CommandUtil.getPlatformSpecificBalaPath(
                                tool.org().value(),
                                tool.name().value(),
                                tool.version().toString(),
                                centralBalaDirPath)
                        .resolve(TOOL).resolve(LIBS)
                        .toFile()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
