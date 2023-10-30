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

import io.ballerina.cli.tool.BuildToolRunner;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ToolContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.PackageManifest.Tool;

/**
 * Run .
 */
public class RunBallerinaPreBuildToolsTask implements Task {

    private final PrintStream outStream;

    public RunBallerinaPreBuildToolsTask(PrintStream out) {
        this.outStream = out;
    }

    @Override
    public void execute(Project project) {
        Collection<Diagnostic> toolDiagnostics = project.currentPackage().manifest().diagnostics().diagnostics();
        boolean hasTomlErrors = false;
        for (Diagnostic d: toolDiagnostics) {
            if (d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR)) {
                outStream.println(d);
                hasTomlErrors = true;
            }
        }
        if (hasTomlErrors) {
            throw createLauncherException("Ballerina toml validation for pre build tool execution contains errors");
        }
        List<Tool> tools = project.currentPackage().manifest().tools();
        ServiceLoader<BuildToolRunner> buildRunners = ServiceLoader.load(BuildToolRunner.class);
        for (Tool tool : tools) {
            try {
                String commandName = tool.getType();
                BuildToolRunner targetTool = null;
                for (BuildToolRunner buildRunner : buildRunners) {
                    if (buildRunner.getToolName().equals(commandName)) {
                        targetTool = buildRunner;
                        break;
                    }
                }
                if (targetTool != null) {
                    try {
                        if (tool.getOptionsToml() != null) {
                            boolean hasErrors = false;
                            List<Diagnostic> optionsDiagnostics = FileUtils.validateToml(tool.getOptionsToml(),
                                tool.getType());
                            for (Diagnostic d : optionsDiagnostics) {
                                outStream.println(d);
                                if (d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR)) {
                                    hasErrors = true;
                                }
                            }
                            if (hasErrors) {
                                throw new ProjectException(
                                    "Ballerina toml validation for pre build tool execution contains errors");
                            }
                        }
                    } catch (IOException e) {
                        outStream.println("WARNING: Skipping the validation of tool options due to : " +
                            e.getMessage());
                    }
                    ToolContext toolContext = ToolContext.from(tool, project.currentPackage());
                    targetTool.executeTool(toolContext);
                    boolean hasToolErrors = false;
                    for (Diagnostic d : targetTool.diagnostics()) {
                        outStream.println(d);
                        if (d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR)) {
                            hasToolErrors = true;
                        }
                    }
                    if (hasToolErrors) {
                        throw new ProjectException("Pre build tool " + tool.getType() + " execution contains errors");
                    }
                } else {
                    // TODO: Install tool if not found
                    outStream.println("Command not found: " + commandName);
                }
            } catch (ProjectException e) {
                throw createLauncherException(e.getMessage());
            }
        }

    }

}
