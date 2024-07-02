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
package org.ballerinalang.testerina.compiler;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Arrays;

import static org.ballerinalang.testerina.compiler.TesterinaCompilerPluginConstants.TEST_MODULE_NAME;

/**
 * Code analyzer for the Testerina module.
 *
 * @since 2201.5.0
 */
public class TesterinaCodeAnalyzer extends CodeAnalyzer {

    private static final String INVALID_USAGE_ERROR_CODE = "TEST_101";
    private static final String INVALID_USAGE_ERROR_MESSAGE = "invalid usage of test annotation";

    @Override
    public void init(CodeAnalysisContext analysisContext) {
        analysisContext.addSyntaxNodeAnalysisTask(syntaxNodeAnalysisContext -> {
            // Traverses class and service definition nodes to check if test definitions are specified within
            // the class for functions
            if (syntaxNodeAnalysisContext.node() instanceof ClassDefinitionNode classDefinitionNode) {
                classDefinitionNode.members().forEach(member ->
                    validateTestAnnotation(syntaxNodeAnalysisContext, member));
            } else if (syntaxNodeAnalysisContext.node() instanceof ServiceDeclarationNode serviceDeclarationNode) {
                serviceDeclarationNode.members().forEach(member ->
                    validateTestAnnotation(syntaxNodeAnalysisContext, member));
            }
        }, Arrays.asList(SyntaxKind.CLASS_DEFINITION, SyntaxKind.SERVICE_DECLARATION));
    }

    private static void validateTestAnnotation(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext, Node member) {
        if (member instanceof FunctionDefinitionNode funcDefNode) {
            funcDefNode.metadata().ifPresent(metadata ->
                metadata.annotations().forEach(annotation -> {
                    if (annotation.annotReference().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                        QualifiedNameReferenceNode qualifiedNameReferenceNode =
                                (QualifiedNameReferenceNode) annotation.annotReference();
                        String modulePrefix = qualifiedNameReferenceNode.modulePrefix().text();
                        if (TEST_MODULE_NAME.equals(modulePrefix)) {
                            // Report invalid test definition diagnostic
                            Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(
                                    new DiagnosticInfo(INVALID_USAGE_ERROR_CODE,
                                            INVALID_USAGE_ERROR_MESSAGE,
                                            DiagnosticSeverity.ERROR), annotation.location());
                            syntaxNodeAnalysisContext.reportDiagnostic(diagnostic);
                        }
                    }
                }));
        }
    }
}
