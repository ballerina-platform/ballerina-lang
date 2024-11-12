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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.util.Arrays;
import java.util.Map;

/**
 * A class representing a code analyzer that uses shared data within the plugin.
 *
 * @since 2201.8.7
 */
public class SharedDataTestCodeAnalyzer extends CodeAnalyzer {
    private final Map<String, Object> userData;

    /**
     * Constructor accepting user data map.
     *
     * @param userData map of user data
     */
    public SharedDataTestCodeAnalyzer(Map<String, Object> userData) {
        this.userData = userData;
    }

    @Override
    public void init(CodeAnalysisContext analysisContext) {
        analysisContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisContext -> {
            if (syntaxNodeAnalysisContext.node() instanceof FunctionDefinitionNode) {
                Object isCompleted = this.userData.get("isCompleted");
                if (isCompleted == null || (isCompleted instanceof Boolean &&
                        !(Boolean) isCompleted)) {
                    // Report a test diagnostic
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(null,
                            "diagnostic message from Analyzer", DiagnosticSeverity.ERROR);
                    syntaxNodeAnalysisContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo,
                            new NullLocation()));
                }
            }
        }, Arrays.asList(SyntaxKind.FUNCTION_DEFINITION));
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
