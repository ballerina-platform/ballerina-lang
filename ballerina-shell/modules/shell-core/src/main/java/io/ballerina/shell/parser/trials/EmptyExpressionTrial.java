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
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.shell.parser.TrialTreeParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Attempts to capture an empty expression.
 * This could be a comment, white space, etc...
 *
 * @since 2.0.0
 */
public class EmptyExpressionTrial extends TreeParserTrial {

    public EmptyExpressionTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        List<Node> nodes = new ArrayList<>();
        ExpressionNode node;
        try {
            node = NodeParser.parseExpression(source);
        } catch (Exception e) {
            node = NodeParser.parseExpression(source.substring(0, source.length() - 1));
        }
        nodes.add(node);
        return nodes;
    }
}
