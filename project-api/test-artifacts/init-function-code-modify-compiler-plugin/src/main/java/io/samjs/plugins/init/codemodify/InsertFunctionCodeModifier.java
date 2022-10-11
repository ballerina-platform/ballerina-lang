/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.samjs.plugins.init.codemodify;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CodeModifierContext;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createWhitespaceMinutiae;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParameterizedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

/**
 * A {@code CodeModifier} implementation that modify each bal file by adding a specific function to the end.
 *
 * @since 2201.0.3
 */
public class InsertFunctionCodeModifier extends CodeModifier {

    @Override
    public void init(CodeModifierContext modifierContext) {
        modifierContext.addSourceModifierTask(sourceModifierContext -> {
            // Add new function to every bal file
            for (ModuleId moduleId : sourceModifierContext.currentPackage().moduleIds()) {
                Module module = sourceModifierContext.currentPackage().module(moduleId);
                for (DocumentId documentId : module.documentIds()) {
                    Document document = module.document(documentId);
                    ModulePartNode rootNode = document.syntaxTree().rootNode();
                    NodeList<ModuleMemberDeclarationNode> newMembers =
                            rootNode.members().add(createFunctionDefNode(document));
                    ModulePartNode newModulePart =
                            rootNode.modify(rootNode.imports(), newMembers, rootNode.eofToken());
                    SyntaxTree updatedSyntaxTree = document.syntaxTree().modifyWith(newModulePart);
                    sourceModifierContext.modifySourceFile(updatedSyntaxTree.textDocument(), documentId);
                }
            }
            // Add new function to every test bal file
            for (ModuleId moduleId : sourceModifierContext.currentPackage().moduleIds()) {
                Module module = sourceModifierContext.currentPackage().module(moduleId);
                for (DocumentId documentId : module.testDocumentIds()) {
                    Document document = module.document(documentId);
                    ModulePartNode rootNode = document.syntaxTree().rootNode();
                    NodeList<ModuleMemberDeclarationNode> newMembers =
                            rootNode.members().add(createFunctionDefNode(document));
                    ModulePartNode newModulePart =
                            rootNode.modify(rootNode.imports(), newMembers, rootNode.eofToken());
                    SyntaxTree updatedSyntaxTree = document.syntaxTree().modifyWith(newModulePart);
                    sourceModifierContext.modifyTestSourceFile(updatedSyntaxTree.textDocument(), documentId);
                }
            }
        });
    }

    private static FunctionDefinitionNode createFunctionDefNode(Document document) {
        List<Token> qualifierList = new ArrayList<>();
        Token publicToken = createToken(SyntaxKind.PUBLIC_KEYWORD, generateMinutiaeListWithTwoNewline(),
                generateMinutiaeListWithWhitespace());
        qualifierList.add(publicToken);

        SimpleNameReferenceNode simpleNameRefNode = createSimpleNameReferenceNode(
                createIdentifierToken("string", createEmptyMinutiaeList(),
                        generateMinutiaeListWithWhitespace()));

        RequiredParameterNode requiredParameterNode =
                createRequiredParameterNode(createEmptyNodeList(), simpleNameRefNode,
                        createIdentifierToken("params"));

        OptionalTypeDescriptorNode optionalErrorTypeDescriptorNode =
                createOptionalTypeDescriptorNode(
                        createParameterizedTypeDescriptorNode(SyntaxKind.ERROR_TYPE_DESC,
                                createToken(SyntaxKind.ERROR_KEYWORD), null),
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN, createEmptyMinutiaeList(),
                                generateMinutiaeListWithWhitespace()));

        ReturnTypeDescriptorNode returnTypeDescriptorNode =
                createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD,
                                createEmptyMinutiaeList(), generateMinutiaeListWithWhitespace()),
                        createEmptyNodeList(), optionalErrorTypeDescriptorNode);

        FunctionSignatureNode functionSignatureNode =
                createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                        createSeparatedNodeList(requiredParameterNode),
                        createToken(SyntaxKind.CLOSE_PAREN_TOKEN,
                                createEmptyMinutiaeList(), generateMinutiaeListWithWhitespace()),
                        returnTypeDescriptorNode);

        FunctionBodyBlockNode emptyFunctionBodyNode =
                createFunctionBodyBlockNode(
                        createToken(SyntaxKind.OPEN_BRACE_TOKEN, createEmptyMinutiaeList(),
                                generateMinutiaeListWithNewline()), null,
                        createEmptyNodeList(), createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

        return createFunctionDefinitionNode(
                SyntaxKind.FUNCTION_DEFINITION, null, createNodeList(qualifierList),
                createToken(SyntaxKind.FUNCTION_KEYWORD, createEmptyMinutiaeList(),
                        generateMinutiaeListWithWhitespace()),
                createIdentifierToken("newFunctionByCodeModifier"
                        + document.name().replace(".bal", "").replace("/", "_")
                .replace("-", "_")),
                createEmptyNodeList(), functionSignatureNode, emptyFunctionBodyNode);
    }

    private static MinutiaeList generateMinutiaeListWithWhitespace() {
        return createMinutiaeList(createWhitespaceMinutiae(" "));
    }

    private static MinutiaeList generateMinutiaeListWithNewline() {
        return createMinutiaeList(createWhitespaceMinutiae("\n"));
    }

    private static MinutiaeList generateMinutiaeListWithTwoNewline() {
        return createMinutiaeList(createWhitespaceMinutiae("\n\n"));
    }
}
