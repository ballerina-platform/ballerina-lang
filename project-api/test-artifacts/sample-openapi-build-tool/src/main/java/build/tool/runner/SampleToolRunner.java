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

import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolConfig;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.nio.file.Path;

/**
 * A sample tool to be integrated with the build
 *
 * @since 2201.9.0
 */
@ToolConfig(name = "openapi")
public class SampleToolRunner implements CodeGeneratorTool {
    @Override
    public void execute(ToolContext toolContext) {
        Path absFilePath = toolContext.currentPackage().project().sourceRoot().resolve(toolContext.filePath());
        if (!absFilePath.toFile().exists()) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo("001",
                    "The provided filePath does not exist", DiagnosticSeverity.ERROR);
            toolContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
        }
        System.out.println("Running sample build tool: " + toolContext.toolId());
        System.out.println("Cache created at: " + toolContext.cachePath());
    }

    private static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("openAPI sample build tool", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
