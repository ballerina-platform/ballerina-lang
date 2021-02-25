/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.parser.trials;

import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.shell.parser.TrialTreeParser;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

/**
 * Attempts to parse source as a statement.
 * Puts in the main function statement level and checks for the the entry.
 * TODO: Improve performance.
 *
 * @since 2.0.0
 */
public class StatementTrial extends DualTreeParserTrial {
    public StatementTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Node parseSource(String source) throws ParserTrialFailedException {
        String sourceCode = String.format("function main(){%s}", source);
        TextDocument document = TextDocuments.from(sourceCode);
        SyntaxTree tree = getSyntaxTree(document);

        ModulePartNode node = tree.rootNode();
        NodeList<ModuleMemberDeclarationNode> moduleDclns = node.members();
        assertIf(!moduleDclns.isEmpty(), "expected at least one member");
        ModuleMemberDeclarationNode moduleDeclaration = moduleDclns.get(0);
        FunctionDefinitionNode mainFunction = (FunctionDefinitionNode) moduleDeclaration;
        FunctionBodyBlockNode mainFunctionBody = (FunctionBodyBlockNode) mainFunction.functionBody();

        if (mainFunctionBody.namedWorkerDeclarator().isPresent()) {
            return mainFunctionBody.namedWorkerDeclarator().get();
        }
        assertIf(!mainFunctionBody.statements().isEmpty(), "expected at least one statement");

        StatementNode statementNode = mainFunctionBody.statements().get(0);
        if (statementNode instanceof ExpressionStatementNode) {
            return ((ExpressionStatementNode) statementNode).expression();
        }
        if (statementNode instanceof VariableDeclarationNode) {
            assertIf(((VariableDeclarationNode) statementNode).initializer().isPresent(),
                    "variable declarations must has an initializer expression");
        }
        return statementNode;
    }
}
