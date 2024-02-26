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

package io.ballerina.cli.task;

import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.Diagnostics;
import io.ballerina.projects.Project;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.PackageManifest.Tool;
import static io.ballerina.projects.util.ProjectConstants.TOOL_DIAGNOSTIC_CODE_PREFIX;

/**
 * Task for running tools integrated with the build.
 *
 * @since 2201.9.0
 */
public class RunBallerinaPreBuildToolsTask implements Task {
    private final PrintStream outStream;

    public RunBallerinaPreBuildToolsTask(PrintStream out) {
        this.outStream = out;
    }

    @Override
    public void execute(Project project) {
        // Print all build tool manifest diagnostics
        Collection<Diagnostic> toolManifestDiagnostics = project.currentPackage().manifest().diagnostics()
                .diagnostics().stream().filter(diagnostic -> diagnostic.diagnosticInfo().code()
                .startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX)).collect(Collectors.toList());
        toolManifestDiagnostics.forEach(outStream::println);

        // Build tool execution
        Map<String, ToolContext> toolContextMap = new HashMap<>();
        List<Tool> tools = project.currentPackage().manifest().tools();
        if (!tools.isEmpty()) {
            this.outStream.println("\nExecuting Build Tools");
        }
        ServiceLoader<CodeGeneratorTool> buildRunners = ServiceLoader.load(CodeGeneratorTool.class);
        for (Tool tool : tools) {
            String commandName = tool.type();
            ToolContext toolContext = ToolContext.from(tool, project.currentPackage());
            boolean hasOptionErrors = false;
            try {
                hasOptionErrors = validateOptionsToml(tool.optionsToml(), commandName);
                if (hasOptionErrors) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.diagnosticId(),
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.messageKey(),
                            DiagnosticSeverity.ERROR);
                    PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                            tool.type());
                    toolContext.reportDiagnostic(diagnostic);
                    toolContextMap.put(tool.id(), toolContext);
                }
            } catch (IOException e) {
                outStream.println(String.format("WARNING: Skipping validation of tool options for tool %s(%s) " +
                        "due to: %s", tool.type(), tool.id(), e.getMessage()));
            }
            if (!tool.hasErrorDiagnostic() && !hasOptionErrors) {
                try {
                    CodeGeneratorTool targetTool = getTargetTool(commandName, buildRunners);
                    if (targetTool == null) {
                        // TODO: Installing tool if not found to be implemented at a later phase
                        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(),
                                "Build tool '" + tool.type() + "' not found",
                                DiagnosticSeverity.ERROR);
                        PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                                tool.type());
                        this.outStream.println(diagnostic);
                        toolContext.reportDiagnostic(diagnostic);
                        toolContextMap.put(tool.id(), toolContext);
                        continue;
                    }
                    this.outStream.println(String.format("\t%s(%s)%n", tool.type(), tool.id()));
                    targetTool.execute(toolContext);
                    for (Diagnostic d : toolContext.diagnostics()) {
                        if (d.toString().contains("(1:1,1:1)")) {
                            outStream.println(new PackageDiagnostic(d.diagnosticInfo(), toolContext.toolId()));
                        } else {
                            outStream.println(new PackageDiagnostic(d.diagnosticInfo(), d.location()));
                        }
                    }
                    toolContextMap.put(tool.id(), toolContext);
                } catch (Exception e) {
                    throw createLauncherException(e.getMessage());
                }
            } else {
                outStream.println(String.format("WARNING: Skipping execution of build tool %s(%s) as Ballerina.toml " +
                        "contains errors%n", tool.type(), tool.id() != null ? tool.id() : ""));
            }
        }
        project.setToolContextMap(toolContextMap);
    }

    private CodeGeneratorTool getTargetTool(String commandName, ServiceLoader<CodeGeneratorTool> buildRunners) {
        for (CodeGeneratorTool buildRunner : buildRunners) {
            if (buildRunner.toolName().equals(commandName)) {
                return buildRunner;
            }
        }
        return null;
    }

    private boolean validateOptionsToml(Toml optionsToml, String toolName) throws IOException {
        if (optionsToml == null) {
            return validateEmptyOptionsToml(toolName);
        }
        FileUtils.validateToml(optionsToml, toolName);
        optionsToml.diagnostics().forEach(outStream::println);
        return !Diagnostics.filterErrors(optionsToml.diagnostics()).isEmpty();
    }

    private boolean validateEmptyOptionsToml(String toolName) throws IOException {
        Schema schema = Schema.from(io.ballerina.projects.util.FileUtils
                .readFileAsString(toolName + "-options-schema.json"));
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
        this.outStream.println(String.format("WARNING: Skipping validation of tool options for tool %s due to: " +
                "No tool options found for", toolName));
        return false;
    }
}
