/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.testerina.compiler;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.text.MessageFormat;

import static org.ballerinalang.testerina.compiler.TesterinaCompilerPluginConstants.TEST_MODULE_NAME;

/**
 * Code analyzer for the Testerina @test:Config annotation analysis.
 *
 * @since 2201.13.0
 */
public class ConfigAnnotationAnalyzerTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    private static final String INVALID_MIN_PASS_RATE_ERROR_CODE = "TEST_102";
    private static final String INVALID_MIN_PASS_RATE_ERROR_MESSAGE = "invalid config:" +
            " 'minPassRate' must be between 0.0 and 1.0, found: {0}";
    private static final String INVALID_RUNS_ERROR_CODE = "TEST_102";
    private static final String INVALID_RUNS_ERROR_MESSAGE = "invalid config:" +
            " 'runs' must be a positive integer, found: {0}";

    @Override
    public void perform(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        if (syntaxNodeAnalysisContext.node() instanceof AnnotationNode annotationNode) {
            validateEvalConfigAnnotation(syntaxNodeAnalysisContext, annotationNode);
        }
    }

    private static void validateEvalConfigAnnotation(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext,
                                                     AnnotationNode annotation) {
        if (!(annotation.annotReference() instanceof QualifiedNameReferenceNode qualifiedNameReferenceNode)) {
            return;
        }
        String modulePrefix = qualifiedNameReferenceNode.modulePrefix().text();
        if (!TEST_MODULE_NAME.equals(modulePrefix) || annotation.annotValue().isEmpty()) {
            return;
        }
        MappingConstructorExpressionNode mappingConstructorExpressionNode = annotation.annotValue().get();
        for (MappingFieldNode field : mappingConstructorExpressionNode.fields()) {
            if (field instanceof SpecificFieldNode specificFieldNode) {
                validateField(syntaxNodeAnalysisContext, annotation, specificFieldNode);
            }
        }
    }

    private static void validateField(SyntaxNodeAnalysisContext context, AnnotationNode annotation,
                                      SpecificFieldNode fieldNode) {
        String fieldName = fieldNode.fieldName().toSourceCode().trim();
        switch (fieldName) {
            case "minPassRate" -> validateMinPassRate(context, annotation, fieldNode);
            case "runs" -> validateRun(context, annotation, fieldNode);
            default -> {
            }
        }
    }

    private static void validateMinPassRate(SyntaxNodeAnalysisContext context, AnnotationNode annotation,
                                            SpecificFieldNode fieldNode) {
        fieldNode.valueExpr().ifPresent(valueExpr -> {
            String confidenceStr = valueExpr.toSourceCode().trim();
            try {
                float confidence = Float.parseFloat(confidenceStr);
                if (confidence < 0.0f || confidence > 1.0f) {
                    reportDiagnosticError(context, annotation, INVALID_MIN_PASS_RATE_ERROR_CODE,
                            MessageFormat.format(INVALID_MIN_PASS_RATE_ERROR_MESSAGE, confidenceStr));
                }
            } catch (NumberFormatException e) {
                reportDiagnosticError(context, annotation, INVALID_MIN_PASS_RATE_ERROR_CODE,
                        MessageFormat.format(INVALID_MIN_PASS_RATE_ERROR_MESSAGE, confidenceStr));
            }
        });
    }

    private static void validateRun(SyntaxNodeAnalysisContext context, AnnotationNode annotation,
                                    SpecificFieldNode fieldNode) {
        fieldNode.valueExpr().ifPresent(valueExpr -> {
            String iterationsStr = valueExpr.toSourceCode().trim();
            try {
                int iterations = Integer.parseInt(iterationsStr);
                if (iterations <= 0) {
                    reportDiagnosticError(context, annotation, INVALID_RUNS_ERROR_CODE,
                            MessageFormat.format(INVALID_RUNS_ERROR_MESSAGE, iterationsStr));
                }
            } catch (NumberFormatException e) {
                reportDiagnosticError(context, annotation, INVALID_RUNS_ERROR_CODE,
                        MessageFormat.format(INVALID_RUNS_ERROR_MESSAGE, iterationsStr));
            }
        });
    }

    private static void reportDiagnosticError(SyntaxNodeAnalysisContext context, AnnotationNode annotation,
                                              String errorCode, String errorMessage) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(errorCode, errorMessage, DiagnosticSeverity.ERROR);
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, annotation.location());
        context.reportDiagnostic(diagnostic);
    }
}
