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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.parser.TrialTreeParser;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Attempts to capture an empty expression.
 * This could be a comment, white space, etc...
 * Puts in the module level and checks for empty module level entry.
 * Empty entries are converted to ().
 *
 * @since 2.0.0
 */
public class EmptyExpressionTrial extends TreeParserTrial {
    private static final Node EMPTY_NODE = NodeFactory.createNilLiteralNode(
            NodeFactory.createToken(SyntaxKind.OPEN_PAREN_TOKEN),
            NodeFactory.createToken(SyntaxKind.CLOSE_PAREN_TOKEN));

    public EmptyExpressionTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        TextDocument document = TextDocuments.from(source);
        SyntaxTree tree = getSyntaxTree(document);
        ModulePartNode node = tree.rootNode();
        Collection<Node> nodes = new ArrayList<>();
        assertIf(node.members().isEmpty(), "expected no members");
        assertIf(node.imports().isEmpty(), "expected no imports");
        nodes.add(EMPTY_NODE);
        return nodes;
    }
}
