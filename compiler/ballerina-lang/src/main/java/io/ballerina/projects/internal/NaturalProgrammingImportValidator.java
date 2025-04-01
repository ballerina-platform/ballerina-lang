/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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

package io.ballerina.projects.internal;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NaturalExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;

/**
 * Check and add an import for the natural programming module if there is a natural expression.
 *
 * @since 2201.13.0
 */
public class NaturalProgrammingImportValidator extends NodeVisitor {

    private boolean importNaturalProgrammingModule;

    public boolean shouldImportNaturalProgrammingModule(ModulePartNode modulePartNode) {
        modulePartNode.accept(this);
        return this.importNaturalProgrammingModule;
    }

    @Override
    public void visit(NaturalExpressionNode naturalExpressionNode) {
        this.importNaturalProgrammingModule = true;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        // TODO: check for compile-time function
        super.visit(functionDefinitionNode);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        if (this.importNaturalProgrammingModule) {
            return;
        }
        super.visitSyntaxNode(node);
    }
}
