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

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.shell.parser.TrialTreeParser;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
    public Collection<Node> parseSource(String source) throws ParserTrialFailedException {
        TextDocument document = TextDocuments.from(source);
        SyntaxTree tree;
        try {
            tree = getSyntaxTreeAsStatements(document);
        } catch (Exception e) {
            document = TextDocuments.from(source + ";");
            tree = getSyntaxTreeAsStatements(document);
        }
        List<Node> nodes = new ArrayList<>();
        NonTerminalNode rootNode = tree.rootNode();
        ChildNodeList children = rootNode.children();
        Iterator iterator = children.iterator();
        while (iterator.hasNext()) {
            Node element = (Node) iterator.next();
            nodes.add(element);
        }
//        assertIf(!nodes.isEmpty(),"error");
        return nodes;
    }
}
