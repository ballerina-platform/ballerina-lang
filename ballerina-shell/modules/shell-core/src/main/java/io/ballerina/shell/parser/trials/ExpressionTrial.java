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

import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.shell.parser.TrialTreeParser;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Attempts to parse source as an expression.
 *
 * @since 2.0.0
 */
public class ExpressionTrial extends TreeParserTrial {

    private static final String SEMICOLON = ";";

    public ExpressionTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        Collection<Node> nodes = new ArrayList<>();
        ExpressionNode expressionNode;
        Node parsedNode;
        if (source.endsWith(SEMICOLON)) {
            parsedNode = NodeParser.parseStatements("assignment = " + source).get(0);
        } else {
            parsedNode = NodeParser.parseStatements("assignment = " + source + ";").get(0);
        }
        if (parsedNode.hasDiagnostics()) {
            throw new ParserTrialFailedException("Error occurred during parsing node as statement node");
        }
        expressionNode = ((AssignmentStatementNode) parsedNode).expression();
        if (expressionNode.hasDiagnostics()) {
            throw new ParserTrialFailedException("Error occurred during extracting expression from the statement");
        }
        nodes.add(expressionNode);
        return nodes;
    }
}
