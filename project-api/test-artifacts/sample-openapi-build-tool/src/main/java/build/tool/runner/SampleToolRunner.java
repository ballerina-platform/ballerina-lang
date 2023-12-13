/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package build.tool.runner;

import build.tool.runner.diagnostics.ToolDiagnostic;
import io.ballerina.cli.tool.BuildToolRunner;
import io.ballerina.projects.ToolContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A sample tool to be integrated with the build
 *
 * @since 2201.9.0
 */
public class SampleToolRunner implements BuildToolRunner {
    List<Diagnostic> diagnostics = new ArrayList<>();

    @Override
    public void execute(ToolContext toolContext) {
        Path absFilePath = toolContext.currentPackage().project().sourceRoot().resolve(toolContext.filePath());
        if (!absFilePath.toFile().exists()) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo("001", "The provided filePath does not exist", DiagnosticSeverity.ERROR);
            diagnostics.add(new ToolDiagnostic(diagnosticInfo, diagnosticInfo.messageFormat()));
        }
        System.out.println("Running sample build tool: " + toolContext.toolType());
        System.out.println("Cache created at: " + toolContext.cachePath());
    }

    @Override
    public String toolName() {
        return "openapi";
    }
}
