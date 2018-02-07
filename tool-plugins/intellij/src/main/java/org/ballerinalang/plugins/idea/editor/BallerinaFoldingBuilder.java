/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.CallableUnitBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructBodyNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BallerinaFoldingBuilder extends CustomFoldingBuilder implements DumbAware {

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root,
                                            @NotNull Document document, boolean quick) {
        if (!(root instanceof BallerinaFile)) {
            return;
        }
        buildImportFoldingRegion(descriptors, root);
        buildFunctionFoldRegions(descriptors, root);
        buildConnectorFoldRegions(descriptors, root);
        buildServiceFoldingRegions(descriptors, root);
        buildStructFoldingRegions(descriptors, root);
    }

    private void buildImportFoldingRegion(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        Collection<ImportDeclarationNode> importDeclarationNodes = PsiTreeUtil.findChildrenOfType(root,
                ImportDeclarationNode.class);
        if (!importDeclarationNodes.isEmpty()) {
            ImportDeclarationNode[] importDeclarationNodesArray =
                    importDeclarationNodes.toArray(new ImportDeclarationNode[importDeclarationNodes.size()]);
            ImportDeclarationNode firstImport = importDeclarationNodesArray[0];
            ImportDeclarationNode lastImport = importDeclarationNodesArray[importDeclarationNodes.size() - 1];

            FullyQualifiedPackageNameNode fullyQualifiedPackageNameNode = PsiTreeUtil.findChildOfType(firstImport, FullyQualifiedPackageNameNode.class);
            if (fullyQualifiedPackageNameNode == null || fullyQualifiedPackageNameNode.getText().isEmpty()) {
                return;
            }
            int startOffset = fullyQualifiedPackageNameNode.getTextRange().getStartOffset();
            int endOffset = lastImport.getTextRange().getEndOffset();
            descriptors.add(new NamedFoldingDescriptor(firstImport, startOffset, endOffset, null, "..."));
        }
    }

    private void buildFunctionFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        // Get all function nodes.
        Collection<FunctionDefinitionNode> functionNodes = PsiTreeUtil.findChildrenOfType(root, FunctionDefinitionNode.class);
        for (FunctionDefinitionNode functionNode : functionNodes) {
            // Get the function body. This is used to calculate the start offset.
            CallableUnitBodyNode callableUnitBodyNode = PsiTreeUtil.getChildOfType(functionNode,
                    CallableUnitBodyNode.class);
            if (callableUnitBodyNode == null) {
                continue;
            }
            // Add folding descriptor.
            addFoldingDescriptor(descriptors, functionNode, callableUnitBodyNode);
        }
    }

    private void buildConnectorFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        // Get all connectors.
        Collection<ConnectorDefinitionNode> connectorNodes = PsiTreeUtil.findChildrenOfType(root, ConnectorDefinitionNode.class);
        for (ConnectorDefinitionNode connectorNode : connectorNodes) {
            // Get the connector body. This is used to calculate the start offset.
            ConnectorBodyNode connectorBodyNode = PsiTreeUtil.getChildOfType(connectorNode, ConnectorBodyNode.class);
            if (connectorBodyNode == null) {
                continue;
            }
            // Add folding descriptor.
            addFoldingDescriptor(descriptors, connectorNode, connectorBodyNode);
            // We need to add folding support to actions as well. So get all actions in the connector.
            Collection<ActionDefinitionNode> actionDefinitionNodes = PsiTreeUtil.findChildrenOfType(root,
                    ActionDefinitionNode.class);
            for (ActionDefinitionNode actionDefinitionNode : actionDefinitionNodes) {
                // Get the action body. This is used to calculate the start offset.
                CallableUnitBodyNode callableUnitBodyNode = PsiTreeUtil.getChildOfType(actionDefinitionNode,
                        CallableUnitBodyNode.class);
                if (callableUnitBodyNode == null) {
                    continue;
                }
                // Add folding descriptor.
                addFoldingDescriptor(descriptors, actionDefinitionNode, callableUnitBodyNode);
            }
        }
    }

    private void buildServiceFoldingRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        // Get all services.
        Collection<ServiceDefinitionNode> serviceDefinitionNodes = PsiTreeUtil.findChildrenOfType(root,
                ServiceDefinitionNode.class);
        for (ServiceDefinitionNode serviceDefinitionNode : serviceDefinitionNodes) {
            // Get the service body. This is used to calculate the start offset.
            ServiceBodyNode serviceBodyNode = PsiTreeUtil.getChildOfType(serviceDefinitionNode, ServiceBodyNode.class);
            if (serviceBodyNode == null) {
                continue;
            }
            // Add folding descriptor.
            addFoldingDescriptor(descriptors, serviceDefinitionNode, serviceBodyNode);
            // We need to add folding support to resources as well. So get all resources in the service.
            Collection<ResourceDefinitionNode> resourceDefinitionNodes = PsiTreeUtil.findChildrenOfType(root,
                    ResourceDefinitionNode.class);
            for (ResourceDefinitionNode resourceDefinitionNode : resourceDefinitionNodes) {
                // Get the resource body. This is used to calculate the start offset.
                CallableUnitBodyNode callableUnitBodyNode = PsiTreeUtil.getChildOfType(resourceDefinitionNode,
                        CallableUnitBodyNode.class);
                if (callableUnitBodyNode == null) {
                    continue;
                }
                // Add folding descriptor.
                addFoldingDescriptor(descriptors, resourceDefinitionNode, callableUnitBodyNode);
            }
        }
    }

    private void buildStructFoldingRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root) {
        Collection<StructDefinitionNode> structDefinitionNodes = PsiTreeUtil.findChildrenOfType(root,
                StructDefinitionNode.class);
        for (StructDefinitionNode structDefinitionNode : structDefinitionNodes) {
            // Get the strcut body. This is used to calculate the start offset.
            StructBodyNode structBodyNode = PsiTreeUtil.getChildOfType(structDefinitionNode, StructBodyNode.class);
            if (structBodyNode == null) {
                continue;
            }
            // Add folding descriptor.
            addFoldingDescriptor(descriptors, structDefinitionNode, structBodyNode);
        }
    }

    private void addFoldingDescriptor(@NotNull List<FoldingDescriptor> descriptors, PsiElement node,
                                      PsiElement bodyNode) {
        // Sometimes the body node might start with a comment node.
        PsiElement prevSibling = bodyNode.getPrevSibling();
        while (prevSibling != null && (prevSibling instanceof PsiComment
                || prevSibling instanceof PsiWhiteSpace)) {
            prevSibling = prevSibling.getPrevSibling();
        }
        if (prevSibling != null) {
            // Calculate the start and end offsets.
            int startOffset = prevSibling.getTextRange().getStartOffset();
            int endOffset = node.getTextRange().getEndOffset();
            // Add the new folding descriptor.
            descriptors.add(new NamedFoldingDescriptor(node, startOffset, endOffset, null, "{...}"));
        }
    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return "...";
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
