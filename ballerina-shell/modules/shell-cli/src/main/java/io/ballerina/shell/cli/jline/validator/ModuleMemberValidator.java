/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.jline.validator;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.shell.cli.utils.IncompleteInputFinder;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

/**
 * Validates user input as a complete module member.
 *
 * @since 2.0.0
 */
public class ModuleMemberValidator implements Validator {

    private Validator nextInValidator;

    public ModuleMemberValidator() {
        nextInValidator = null;
    }

    @Override
    public void setNextValidator(Validator nextValidator) {
        this.nextInValidator = nextValidator;
    }

    @Override
    public boolean evaluate(String source) {
        IncompleteInputFinder incompleteInputFinder = new IncompleteInputFinder();
        TextDocument document = TextDocuments.from(source);
        SyntaxTree tree = SyntaxTree.from(document);
        ModulePartNode node = tree.rootNode();
        NodeList<ModuleMemberDeclarationNode> members = node.members();
        if (members.size() > 1) {
            return false;
        }

        boolean isIncomplete = NodeParser.parseModuleMemberDeclaration(source).apply(incompleteInputFinder);
        if (!NodeParser.parseModuleMemberDeclaration(source).hasDiagnostics()) {
            return false;
        } else {
            if (!source.endsWith(";") && !NodeParser.parseModuleMemberDeclaration(source + ";").hasDiagnostics()) {
                return false;
            }
        }

        if (!isIncomplete) {
            return  nextInValidator.evaluate(source);
        } else {
            return true;
        }

    }
}
