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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.parser.ParserConstants;
import io.ballerina.shell.parser.TrialTreeParser;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Attempts to capture a module member declaration.
 * Checks if this is a possible module dcln. If it is definitely as module dcln,
 * any error is rejected. Otherwise, it is still checked.
 *
 * @since 2.0.0
 */
public class ModuleMemberTrial extends TreeParserTrial {

    public ModuleMemberTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        TextDocument document = TextDocuments.from(source);
        SyntaxTree tree;
        try {
            tree = getSyntaxTree(document);
        } catch (ParserTrialFailedException e) {
            document = TextDocuments.from(source + ";");
            tree = getSyntaxTree(document);
        }
        List<Node> nodes = new ArrayList<>();
        ModulePartNode node = tree.rootNode();
        NodeList<ModuleMemberDeclarationNode> members = node.members();
        Iterator<ImportDeclarationNode> importIterator = node.imports().iterator();
        Iterator memberIterator = members.iterator();
        while (importIterator.hasNext()) {
            nodes.add(importIterator.next());
        }
        while (memberIterator.hasNext()) {
            ModuleMemberDeclarationNode dclnNode = (ModuleMemberDeclarationNode) memberIterator.next();
            validateModuleDeclaration(dclnNode);
            nodes.add(dclnNode);
        }
        return nodes;
    }

    private void validateModuleDeclaration(ModuleMemberDeclarationNode declarationNode) {
        if (declarationNode instanceof FunctionDefinitionNode functionDefinitionNode) {
            String functionName = functionDefinitionNode.functionName().text();
            if (ParserConstants.isFunctionNameRestricted(functionName)) {
                String message = "Function name " + "'" + functionName + "'" + " not allowed in Ballerina Shell.\n";
                throw new InvalidMethodException(message);
            }
        }
    }
}
