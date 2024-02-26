/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.samjs.plugins.remove.codemodify;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.CodeModifier;
import io.ballerina.projects.plugins.CodeModifierContext;

import java.util.Optional;

/**
 * A {@code CodeModifier} implementation that modify each bal file by removing empty functions.
 *
 * @since 2201.9.0
 */
public class RemoveFunctionCodeModifier extends CodeModifier {

    @Override
    public void init(CodeModifierContext modifierContext) {
        modifierContext.addSourceModifierTask(sourceModifierContext -> {
            // Look for the first function with name "bar" in each bal file and remove if available
            Package currentPackage = sourceModifierContext.currentPackage();
            for (ModuleId moduleId : currentPackage.moduleIds()) {
                io.ballerina.projects.Module module = currentPackage.module(moduleId);
                for (DocumentId documentId : module.documentIds()) {
                    Document document = module.document(documentId);
                    ModulePartNode rootNode = document.syntaxTree().rootNode();
                    Optional<ModuleMemberDeclarationNode> barFunctionNode = rootNode.members().stream()
                            .filter(member -> member.kind() == SyntaxKind.FUNCTION_DEFINITION
                                    && "bar".equals(((FunctionDefinitionNode) member).functionName().text()))
                            .findFirst();
                    if (barFunctionNode.isEmpty()) {
                        continue;
                    }
                    NodeList<ModuleMemberDeclarationNode> updatedMembers =
                            rootNode.members().remove(barFunctionNode.get());
                    ModulePartNode newModulePart =
                            rootNode.modify(rootNode.imports(), updatedMembers, rootNode.eofToken());
                    SyntaxTree updatedSyntaxTree = document.syntaxTree().modifyWith(newModulePart);
                    sourceModifierContext.modifySourceFile(updatedSyntaxTree.textDocument(), documentId);
                }
            }
        });
    }
}
