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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A sample tool to be integrated with the build
 *
 * @since 2201.9.0
 */
@ToolConfig(name = "sample-build-tool")
public class SampleToolRunner implements CodeGeneratorTool {
    @Override
    public void execute(ToolContext toolContext) {
        // Generate source based on mode
        String mode = toolContext.options().get("mode").value().toString();
        try {
            Files.createDirectories(toolContext.outputPath());
            String generatedCode;
            if ("consolidator".equals(mode)) {
                @SuppressWarnings("unchecked")
                List<String> imports = (List<String>) toolContext.options().get("imports").value();
                StringBuilder sb = new StringBuilder();
                for (String imp : imports) {
                    sb.append("import ").append(imp).append(" as _;\n");
                }
                sb.append("\npublic function main() {\n}\n");
                generatedCode = sb.toString();
            } else {
                Path absFilePath = toolContext.currentPackage().project().sourceRoot().resolve(toolContext.filePath());
                if (!absFilePath.toFile().exists()) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo("001",
                            "The provided filePath does not exist", DiagnosticSeverity.ERROR);
                    toolContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
                }
                generatedCode = """
                        function generatedFunction() returns string {
                            return "This is a generated function";
                        }""";
            }
            Files.write(Paths.get(toolContext.outputPath().resolve("gen.bal").toString()),
                    generatedCode.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
