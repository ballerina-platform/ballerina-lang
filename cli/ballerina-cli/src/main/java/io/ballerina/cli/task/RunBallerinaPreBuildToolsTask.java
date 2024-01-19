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

import io.ballerina.cli.tool.CodeGeneratorTool;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.Diagnostics;
import io.ballerina.projects.Project;
import io.ballerina.projects.ToolContext;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.toml.api.Toml;
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
        this.outStream.println("\nExecuting pre build tools\n");
        // Print all build tool manifest diagnostics
        Collection<Diagnostic> toolManifestDiagnostics = project.currentPackage().manifest().diagnostics()
                .diagnostics().stream().filter(diagnostic -> diagnostic.diagnosticInfo().code().startsWith("BCE54"))
                .collect(Collectors.toList());
        toolManifestDiagnostics.forEach(outStream::println);

        //Build tool execution
        Map<String, ToolContext> toolContextMap = new HashMap<>();
        List<Tool> tools = project.currentPackage().manifest().tools();
        ServiceLoader<CodeGeneratorTool> buildRunners = ServiceLoader.load(CodeGeneratorTool.class);
        for (Tool tool : tools) {
            String commandName = tool.getType();
            boolean hasOptionErrors = false;
            try {
                hasOptionErrors = validateOptionsToml(tool.getOptionsToml(), commandName);
            } catch (IOException e) {
                outStream.println("\nWARNING: Skipping the validation of tool options due to: " +
                        e.getMessage());
            }
            if (!tool.hasErrorDiagnostic() && !hasOptionErrors) {
                try {
                    CodeGeneratorTool targetTool = getTargetTool(commandName, buildRunners);
                    ToolContext toolContext = ToolContext.from(tool, project.currentPackage());
                    if (targetTool == null) {
                        // TODO: Installing tool if not found to be implemented at a later phase
                        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(),
                                "Build tool '" + tool.getType() + "' not found",
                                DiagnosticSeverity.ERROR);
                        PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                                tool.getType());
                        this.outStream.println(diagnostic);
                        toolContext.reportDiagnostic(diagnostic);
                        toolContextMap.put(tool.getId(), toolContext);
                        continue;
                    }
                    this.outStream.println("\nExecuting build tool '" + tool.getType() +
                            "' for tool configurations '" + tool.getId() + "'.");
                    targetTool.execute(toolContext);
                    toolContext.diagnostics().forEach(outStream::println);
                    if (!Diagnostics.filterErrors(toolContext.diagnostics()).isEmpty()) {
                        outStream.println("WARNING: Execution of Build tool '" + tool.getType() +
                            "' for tool configurations '" + tool.getId() + "' contains errors\n");
                    }
                    toolContextMap.put(tool.getId(), toolContext);
                } catch (Exception e) {
                    throw createLauncherException(e.getMessage());
                }
            } else {
                outStream.println("WARNING: Skipping execution of build tool '" + tool.getType() +
                        "' as tool configurations in Ballerina.toml contains errors\n");
            }
        }
        project.currentPackage().setPackageContextTools(toolContextMap);
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
            throw new IOException("No tool options found");
        }
        FileUtils.validateToml(optionsToml, toolName);
        optionsToml.diagnostics().forEach(outStream::println);
        return !Diagnostics.filterErrors(optionsToml.diagnostics()).isEmpty();
    }
}
