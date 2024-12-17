/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.validator.impl;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.debugadapter.evaluation.parser.DebugParser;
import org.ballerinalang.debugadapter.evaluation.validator.Validator;

import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.validator.ValidatorException.UNSUPPORTED_INPUT_IMPORT;
import static org.ballerinalang.debugadapter.evaluation.validator.ValidatorException.UNSUPPORTED_INPUT_TOPLEVEL_DCLN;

/**
 * Validator implementation for ballerina top-level declarations(i.e. imports, services, etc.).
 *
 * @since 2.0.0
 */
public class TopLevelDeclarationValidator extends Validator {

    private static final String IMPORT_START = "import ";

    public TopLevelDeclarationValidator(DebugParser parser) {
        super(parser);
    }

    @Override
    public void validate(String source) throws Exception {
        SyntaxTree syntaxTree = debugParser.getSyntaxTreeFor(source);
        ModulePartNode moduleNode = syntaxTree.rootNode();

        // Checks for import declarations in user input.
        // Needs to do an additional string check due to false positive import declarations matched by the ballerina
        // parser.
        NodeList<ImportDeclarationNode> imports = moduleNode.imports();
        failIf(!imports.isEmpty() && imports.stream().allMatch(importNode ->
                importNode.toSourceCode().trim().startsWith(IMPORT_START)), UNSUPPORTED_INPUT_IMPORT);

        // Checks for top-level declarations in user input.
        NodeList<ModuleMemberDeclarationNode> memberNodes = moduleNode.members();
        // Needs to filter out module variable declarations, since variable assignment statements can be parsed into
        // module-level variable declaration during this validation phase.
        List<ModuleMemberDeclarationNode> members = memberNodes.stream()
                .filter(node -> node.kind() != SyntaxKind.MODULE_VAR_DECL && !node.hasDiagnostics())
                .toList();
        failIf(!members.isEmpty(), UNSUPPORTED_INPUT_TOPLEVEL_DCLN);
    }
}
