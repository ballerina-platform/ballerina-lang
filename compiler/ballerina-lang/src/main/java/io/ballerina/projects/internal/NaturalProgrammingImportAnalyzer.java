/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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

package io.ballerina.projects.internal;

import io.ballerina.compiler.syntax.tree.ExternalFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NaturalExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;

/**
 * Check and add an import for the natural programming module if there is a natural expression.
 *
 * @since 2201.13.0
 */
public class NaturalProgrammingImportAnalyzer extends NodeVisitor {

    private boolean shouldImportNaturalProgrammingModule = false;
    private static final String CODE_ANNOTATION = "code";

    public boolean shouldImportNaturalProgrammingModule(ModulePartNode modulePartNode) {
        modulePartNode.accept(this);
        return this.shouldImportNaturalProgrammingModule;
    }

    @Override
    public void visit(NaturalExpressionNode naturalExpressionNode) {
        this.shouldImportNaturalProgrammingModule = true;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (this.shouldImportNaturalProgrammingModule) {
            return;
        }

        if (isExternalFunctionWithCodeAnnotation(functionDefinitionNode)) {
            this.shouldImportNaturalProgrammingModule = true;
            return;
        }

        super.visit(functionDefinitionNode);
    }

    private boolean isExternalFunctionWithCodeAnnotation(FunctionDefinitionNode functionDefinitionNode) {
        if (!(functionDefinitionNode.functionBody() instanceof ExternalFunctionBodyNode externalFunctionBodyNode)) {
            return false;
        }

        return externalFunctionBodyNode.annotations().stream()
                .anyMatch(annotation ->
                        annotation.annotReference() instanceof SimpleNameReferenceNode annotReference &&
                                CODE_ANNOTATION.equals(annotReference.name().text()));
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (this.shouldImportNaturalProgrammingModule) {
            return;
        }
        super.visitSyntaxNode(node);
    }
}
