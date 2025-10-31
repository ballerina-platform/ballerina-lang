/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
import io.ballerina.projects.BuildToolResolution;
import io.ballerina.projects.Diagnostics;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.internal.PackageConfigCreator;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.BalToolsUtil;
import io.ballerina.projects.util.BuildToolsUtil;
import io.ballerina.projects.util.CustomURLClassLoader;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.launcher.util.BalToolsUtil.findJarFiles;
import static io.ballerina.projects.JBallerinaBalaWriter.LIBS;
import static io.ballerina.projects.JBallerinaBalaWriter.TOOL;
import static io.ballerina.projects.PackageManifest.Tool;
import static io.ballerina.projects.util.ProjectConstants.TOOL_DIAGNOSTIC_CODE_PREFIX;

/**
 * Task for running tools integrated with the build.
 *
 * @since 2201.9.0
 */
public class RunBuildToolsTask implements Task {
    private final PrintStream outStream;
    private final List<Diagnostic> diagnostics;
    private final boolean exitWhenFinish;
    private final Map<Tool.Field, ToolContext> toolContextMap = new HashMap<>();
    private final boolean skipTask;

    public RunBuildToolsTask(PrintStream out, boolean skipTask, List<Diagnostic> diagnostics) {
        this.skipTask = skipTask;
        this.outStream = out;
        this.diagnostics = diagnostics;
        this.exitWhenFinish = true;
    }

    @Override
    public void execute(Project project) {
        project.setToolContextMap(toolContextMap);

        // Print all build tool manifest diagnostics
        Collection<Diagnostic> toolManifestDiagnostics = project.currentPackage().manifest().diagnostics()
                .diagnostics().stream().filter(diagnostic -> diagnostic.diagnosticInfo().code()
                        .startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX)).toList();
        toolManifestDiagnostics.forEach(outStream::println);
        List<Diagnostic> toolDiagnostics = new ArrayList<>(toolManifestDiagnostics);

        // Read the build tool entries specified the Ballerina.toml
        List<Tool> toolEntries = project.currentPackage().manifest().tools();
        if (toolEntries.isEmpty()) {
            return;
        }
        this.outStream.println("\nExecuting Build Tools" + (skipTask ? " (UP-TO-DATE)" : ""));
        if (skipTask) {
            return;
        }

        // Populate the tool context map
        for (Tool toolEntry : toolEntries) {
            // Populate tool context
            ToolContext toolContext = ToolContext.from(toolEntry, project.currentPackage(), outStream);
            toolContextMap.put(toolEntry.id(), toolContext);
        }
        BuildToolResolution buildToolResolution;
        try {
            buildToolResolution = project.currentPackage().getBuildToolResolution();
        } catch (ProjectException e) {
            CommandUtil.printError(this.outStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        buildToolResolution.getDiagnosticList().forEach(outStream::println);
        toolDiagnostics.addAll(buildToolResolution.getDiagnosticList());
        List<BuildTool> resolvedTools = buildToolResolution.getResolvedTools();

        Map<String, ClassLoader> classLoaderMap = createToolClassLoader(resolvedTools);

        // We run only the entries of resolved tools.
        List<Tool> resolvedToolEntries = toolEntries.stream()
                .filter(toolEntry -> resolvedTools.stream()
                        .anyMatch(tool -> tool.id().value().equals(toolEntry.type().value().split("\\.")[0])))
                .toList();
        for (Tool toolEntry : resolvedToolEntries) {
            String commandName = toolEntry.type().value();
            ToolContext toolContext = toolContextMap.get(toolEntry.id());
            ClassLoader toolClassLoader = classLoaderMap.get(commandName.split("\\.")[0]);
            Thread.currentThread().setContextClassLoader(toolClassLoader);
            ServiceLoader<CodeGeneratorTool> toolServiceLoader = ServiceLoader.load(
                    CodeGeneratorTool.class, toolClassLoader);
            Optional<CodeGeneratorTool> targetTool = BuildToolsUtil.getTargetTool(commandName, toolServiceLoader);
            if (targetTool.isEmpty()) {
                // If the tool is not found, we skip the execution and report a diagnostic
                Diagnostic diagnostic = BuildToolsUtil.getBuildToolCommandNotFoundDiagnostic(
                        commandName, toolEntry.type().location());
                toolContext.reportDiagnostic(diagnostic);
                this.outStream.println(diagnostic);
                printToolSkipWarning(toolEntry);
                continue;
            }
            // Here, we can safely pass the tool type value given in Ballerina.toml instead of
            // the aggregation of the ToolConfig annotation name fields of a command/ subcommand field
            // since we have verified that those two are identical in the ToolUtils.getTargetTool method
            Optional<TomlDiagnostic> cmdNameDiagnostic = BuildToolsUtil
                    .getDiagnosticIfInvalidCommandName(commandName, toolEntry.id().location());
            if (cmdNameDiagnostic.isPresent()) {
                toolContext.reportDiagnostic(cmdNameDiagnostic.get());
                this.outStream.println(cmdNameDiagnostic.get());
                printToolSkipWarning(toolEntry);
                continue;
            }
            boolean hasOptionErrors = false;
            try {
                // validate the options toml and report diagnostics
                hasOptionErrors = validateOptionsToml(toolEntry.optionsToml(), toolEntry.type(), toolClassLoader);
                if (hasOptionErrors) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.diagnosticId(),
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.messageKey(),
                            DiagnosticSeverity.ERROR);
                    TomlNodeLocation location = toolEntry.optionsToml() == null ? toolEntry.type().location() :
                            toolEntry.optionsTable().location();
                    TomlDiagnostic diagnostic = new TomlDiagnostic(location, diagnosticInfo, toolEntry.type().value());
                    toolContext.reportDiagnostic(diagnostic);
                }
            } catch (IOException e) {
                // if there is no options toml, we warn the user and continue
                outStream.printf("WARNING: Validation of tool options of '%s' for '%s' is skipped due to: %s%n",
                        commandName, toolEntry.id().value(), e.getMessage());
            }
            // if manifest errors or options table validation errors
            if (toolEntry.hasErrorDiagnostic() || hasOptionErrors) {
                printToolSkipWarning(toolEntry);
                continue;
            }

            // Execute the build tool
            try {
                this.outStream.printf("\t%s(%s)%n", toolEntry.type().value(), toolEntry.id().value());
                targetTool.get().execute(toolContext);
                toolDiagnostics.addAll(toolContext.diagnostics());
                for (Diagnostic d : toolContext.diagnostics()) {
                    if (d.toString().contains("(1:1,1:1)")) {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), toolContext.toolId()));
                    } else {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), d.location()));
                    }
                }
            } catch (Exception e) {
                throw createLauncherException(e.getMessage());
            }
        }

        // Exit if there is any error diagnostic
        boolean hasErrors = toolDiagnostics.stream()
                .anyMatch(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR));
        if (hasErrors) {
            throw createLauncherException("build tool execution contains errors");
        }
        diagnostics.addAll(buildToolResolution.getDiagnosticList());
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        // Reload the project to load the generated code
        reloadProject(project);
        this.outStream.println();
    }

    private boolean validateOptionsToml(Toml optionsToml, Tool.Field toolType, ClassLoader toolClassLoader)
            throws IOException {
        if (optionsToml == null) {
            return validateEmptyOptionsToml(toolType, toolClassLoader);
        }
        FileUtils.validateToml(optionsToml, toolType.value(), toolClassLoader);
        optionsToml.diagnostics().forEach(outStream::println);
        return !Diagnostics.filterErrors(optionsToml.diagnostics()).isEmpty();
    }

    private boolean validateEmptyOptionsToml(Tool.Field toolType, ClassLoader toolClassLoader) throws IOException {
        Schema schema = Schema.from(FileUtils.readSchema(toolType.value(), toolClassLoader));
        List<String> requiredFields = schema.required();
        if (!requiredFields.isEmpty()) {
            for (String field: requiredFields) {
                String message = "missing required optional field '" + field + "'";
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML.diagnosticId(),
                        ProjectDiagnosticErrorCode.MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML.messageKey(),
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(toolType.location(), diagnosticInfo, message);
                this.outStream.println(diagnostic);
            }
            return true;
        }
        return false;
    }

    private Map<String, ClassLoader> createToolClassLoader(List<BuildTool> resolvedTools) {
        Map<String, ClassLoader> classLoaderMap = new HashMap<>();
        for (BuildTool resolvedTool : resolvedTools) {
            List<File> toolJars = getToolCommandJarAndDependencyJars(resolvedTool);
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
            classLoaderMap.put(resolvedTool.id().value(), new CustomURLClassLoader(urls, systemClassLoader));
        }
        return classLoaderMap;
    }

    private void printToolSkipWarning(Tool toolEntry) {
        outStream.printf("WARNING: Execution of '%s:%s' is skipped due to errors%n", toolEntry
                .type().value(), toolEntry.id() != null ? toolEntry.id().value() : "");
    }

    private void reloadProject(Project project) {
        PackageConfig packageConfig = PackageConfigCreator.createBuildProjectConfig(project.sourceRoot(),
                project.buildOptions().disableSyntaxTree());
        project.addPackage(packageConfig);
    }

    private static List<File> getToolCommandJarAndDependencyJars(BuildTool buildTool) {
        return findJarFiles(CommandUtil.getPlatformSpecificBalaPath(
                        buildTool.org().value(),
                        buildTool.name().value(),
                        buildTool.version().toString(),
                        BalToolsUtil.getRepoPath(buildTool.repository().orElse(null)))
                .resolve(TOOL).resolve(LIBS)
                .toFile());
    }
}
