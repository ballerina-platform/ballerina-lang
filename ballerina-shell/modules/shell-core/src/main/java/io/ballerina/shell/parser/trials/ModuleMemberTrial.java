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
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.parser.TrialTreeParser;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.Set;

/**
 * Attempts to capture a module member declaration.
 * Puts in the module level and checks for module level entries.
 * Checks if this is a possible module dcln. If it is definitely as module dcln,
 * any error is rejected. Otherwise, it is still checked.
 *
 * @since 2.0.0
 */
public class ModuleMemberTrial extends DualTreeParserTrial {

    private static final Set<String> RESTRICTED_FUNCTION_NAMES = Set.of("main", "init");

    public ModuleMemberTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Node parseSource(String source) throws ParserTrialFailedException {
        TextDocument document = TextDocuments.from(source);
        SyntaxTree tree = getSyntaxTree(document);

        ModulePartNode node = tree.rootNode();
        assertIf(!node.members().isEmpty(), "expected at least one member");
        ModuleMemberDeclarationNode dclnNode = node.members().get(0);
        validateModuleDeclaration(dclnNode);
        if (dclnNode instanceof ModuleVariableDeclarationNode) {
            // If there are no qualifiers or metadata then this can be also a statement/expression.
            // eg: `mp[a] = f()` (mp is a map) is also valid as `mp [a] = f()` (mp is a type) which is a var-dcln.
            // So, this will be passed down to be parsed by statement/expression trial.
            ModuleVariableDeclarationNode varNode = (ModuleVariableDeclarationNode) dclnNode;
            assertIf(varNode.metadata().isPresent() || varNode.qualifiers().size() > 0
                            || varNode.visibilityQualifier().isPresent(),
                    "meta data nor qualifiers not present - not accepted as module-dcln");
        }
        return dclnNode;
    }

    private void validateModuleDeclaration(ModuleMemberDeclarationNode declarationNode) {
        if (declarationNode instanceof FunctionDefinitionNode) {
            String functionName = ((FunctionDefinitionNode) declarationNode).functionName().text();
            if (RESTRICTED_FUNCTION_NAMES.contains(functionName)) {
                String message = "Function name " + "'" + functionName + "'" + " not allowed in Ballerina Shell.\n";
                throw new InvalidMethodException(message);
            }
            if (functionName.startsWith("__")) {
                String message = "Functions starting with" + " '__' " + "not allowed in Ballerina Shell.\n";
                throw new InvalidMethodException(message);
            }
        }
    }
}
