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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.shell.parser.TrialTreeParser;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Attempts to parse source as a statement.
 * TODO: Improve performance.
 *
 * @since 2.0.0
 */
public class StatementTrial extends TreeParserTrial {
    public StatementTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        NodeList<StatementNode> statementNodes;
        Collection<Node> finalNodes = new ArrayList<>();
        try {
            statementNodes = NodeParser.parseStatements(source);
            for (StatementNode nodeInst : statementNodes) {
                if (nodeInst.hasDiagnostics()) {
                    throw new ParserTrialFailedException("Error occurred during parsing as a statement");
                }
            }
        } catch (ParserTrialFailedException e) {
            statementNodes = NodeParser.parseStatements(source + ';');
            for (StatementNode nodeInst:statementNodes) {
                if (nodeInst.hasDiagnostics()) {
                    throw new ParserTrialFailedException("Error occurred during parsing as a statement");
                }
            }
        }
        for (Node node : statementNodes) {
            if (node.kind() == SyntaxKind.CALL_STATEMENT) {
                node = ((ExpressionStatementNode) node).expression();
            }
            finalNodes.add(node);
        }
        return finalNodes;
    }
}
