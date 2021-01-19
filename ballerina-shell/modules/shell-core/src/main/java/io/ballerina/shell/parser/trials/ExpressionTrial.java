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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.shell.parser.TrialTreeParser;

/**
 * Attempts to parse source as a expression.
 *
 * @since 2.0.0
 */
public class ExpressionTrial extends StatementTrial {
    public ExpressionTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Node parse(String source) throws ParserTrialFailedException {
        // TODO: [Bug in Parser] a >>= 4 gets accepted as a >> 4
        String statementCode = String.format("return %s", source);
        Node statement = super.parseSource(statementCode);

        assertIf(statement instanceof ReturnStatementNode, "expected a return statement");
        assert statement instanceof ReturnStatementNode;
        ReturnStatementNode returnStatement = (ReturnStatementNode) statement;
        assertIf(returnStatement.expression().isPresent(), "expected an expression on return");
        ExpressionNode expressionNode = returnStatement.expression().get();
        // TEMP FIX
        assertIf(!source.contains(">>=") || expressionNode.toSourceCode().contains(">>="), "" +
                "Compound statement is not an expression.");
        return expressionNode;
    }
}
