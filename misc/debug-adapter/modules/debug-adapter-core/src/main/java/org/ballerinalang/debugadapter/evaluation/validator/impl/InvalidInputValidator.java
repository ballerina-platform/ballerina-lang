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
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.debugadapter.evaluation.parser.DebugParser;
import org.ballerinalang.debugadapter.evaluation.validator.Validator;

import static org.ballerinalang.debugadapter.evaluation.validator.ValidatorException.ERROR_INPUT_DOCUMENTATION;
import static org.ballerinalang.debugadapter.evaluation.validator.ValidatorException.ERROR_INPUT_EMPTY_EXPRESSION;

/**
 * Validator implementation to validate invalid ballerina sources (i.e. empty input, comments, documentation, etc.).
 *
 * @since 2.0.0
 */
public class InvalidInputValidator extends Validator {

    private static final String DOCUMENTATION_START = "# ";

    public InvalidInputValidator(DebugParser parser) {
        super(parser);
    }

    @Override
    public void validate(String source) throws Exception {
        // validates for blank statements.
        failIf(source.isBlank(), ERROR_INPUT_EMPTY_EXPRESSION);

        // validates for documentation.
        failIf(source.trim().startsWith(DOCUMENTATION_START), ERROR_INPUT_DOCUMENTATION);

        // validates for empty expressions (which could be whitespaces, comments, etc).
        SyntaxTree syntaxTree = debugParser.getSyntaxTreeFor(source);
        failIf(!syntaxTree.containsModulePart(), ERROR_INPUT_EMPTY_EXPRESSION);
        ModulePartNode moduleNode = syntaxTree.rootNode();
        NodeList<ModuleMemberDeclarationNode> members = moduleNode.members();
        NodeList<ImportDeclarationNode> imports = moduleNode.imports();
        failIf(imports.isEmpty() && members.isEmpty(), ERROR_INPUT_EMPTY_EXPRESSION);
    }
}
