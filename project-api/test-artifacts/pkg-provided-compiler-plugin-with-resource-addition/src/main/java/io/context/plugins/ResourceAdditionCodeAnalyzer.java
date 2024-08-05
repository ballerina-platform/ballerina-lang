/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.context.plugins;

import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class representing a code analyzer that adds resources.
 *
 * @since 2201.10.0
 */
public class ResourceAdditionCodeAnalyzer extends CodeAnalyzer {

    @Override
    public void init(CodeAnalysisContext analysisContext) {
        analysisContext.addCompilationAnalysisTask(compAnalysisCtx -> {
            // Write sample meta data files
            Path resourcePath = compAnalysisCtx.currentPackage().project().targetDir().resolve("resources")
                    .resolve("generated_meta.txt");
            String content = "test meta data is here";

            try {
                Path parentPath = resourcePath.getParent();
                if (parentPath != null) {
                    Files.createDirectories(parentPath);
                }
                // Write the content to the file
                Files.write(resourcePath, content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                // Handle any I/O exceptions
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(null,
                        "An error occurred while writing the meta data file:", DiagnosticSeverity.ERROR);
                compAnalysisCtx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                        new NullLocation()));
            }
        });
    }

    private static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }

}
