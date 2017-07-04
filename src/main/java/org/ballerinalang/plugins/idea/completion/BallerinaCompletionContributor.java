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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeValueNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnnotationReferenceNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ForkJoinStatementNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.MapStructKeyValueNode;
import org.ballerinalang.plugins.idea.psi.MapStructLiteralNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ReturnTypeListNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.SimpleLiteralNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TransformStatementNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.*;

public class BallerinaCompletionContributor extends CompletionContributor implements DumbAware {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        // The file will be loaded to memory and and will be edited. parameters.getOriginalFile()
        // contains the original file. parameters.getPosition().getContainingFile() will return null
        // because it only exists in the memory. So use parameters.getOriginalFile().getContainingFile()
        // if you want to get the details like containing directory, etc.
        addSuggestions(parameters, result);
    }

    /**
     * Adds lookup according to the current node caret is at.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    private void addSuggestions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet) {

        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiFile originalFile = parameters.getOriginalFile();

        PsiDirectory containingPackage = originalFile.getParent();

        if (parent instanceof NameReferenceNode) {
            if (containingPackage != null) {
                // Todo - Get all functions, connectors, structs, variables
                List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage);

                for (PsiElement function : functions) {
                    LookupElement lookupElement = BallerinaCompletionUtils.createFunctionsLookupElement(function);
                    resultSet.addElement(lookupElement);
                }
            }
        }
//        else if (parent instanceof AnnotationReferenceNode) {
//            PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
//            if (packageNameNode == null) {
//                List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(originalFile);
//                // Todo - add unimported packages
//                for (PsiElement importedPackage : importedPackages) {
//                    PsiReference reference = importedPackage.findReferenceAt(0);
//                    if (reference != null) {
//                        PsiElement resolvedElement = reference.resolve();
//                        if (resolvedElement != null) {
//                            PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
//                            LookupElement lookupElement =
//                                    BallerinaCompletionUtils.createPackageLookupElement(psiDirectory,
//                                            PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
//                            resultSet.addElement(lookupElement);
//                        }
//                    }
//                }
//            } else {
//                PsiReference reference = packageNameNode.findReferenceAt(0);
//                if (reference != null) {
//                    PsiElement resolvedElement = reference.resolve();
//                    if (resolvedElement != null) {
//                        PsiDirectory psiDirectory = (PsiDirectory) resolvedElement;
//
//
//                        List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInPackage(psiDirectory);
//
//                        for (PsiElement annotation : annotations) {
//
//                            LookupElement lookupElement =
//                                    BallerinaCompletionUtils.createAnnotationLookupElement(annotation);
//                            resultSet.addElement(lookupElement);
//                        }
//                        //
//                        //                        LookupElementBuilder lookupElement = BallerinaCompletionUtils
//                        // .createPackageLookupElement
//                        //                                (psiDirectory,
//                        //                                        PackageCompletionInsertHandler
//                        //                                                .INSTANCE_WITH_AUTO_POPUP);
//                        //                        resultSet.addElement(lookupElement);
//                    }
//                }
//            }
//        }


        //        PsiElement parentPrevSibling = parent.getPrevSibling();

        // If the parent is a simple literal value node, no need to add lookup elements.
        //        if (parent instanceof SimpleLiteralNode) {
        //            return;
        //        }
        //
        //        // If the current element is a Quoted string, we don't need to add suggestions.
        //        if (element instanceof LeafPsiElement) {
        //            if (((LeafPsiElement) element).getElementType() == BallerinaTypes.QUOTED_STRING) {
        //                return;
        //            }
        //        }
        //
        //        if (parent instanceof PsiFile) {
        //            // If the parent is PsiFile, that means we can only suggest keywords including 'package' and
        // 'import'
        //            // keywords.
        //            addFileLevelKeywordsAsLookups(resultSet, true, true);
        //        } else if (parentPrevSibling instanceof ImportDeclarationNode
        //                || parentPrevSibling instanceof PackageDeclarationNode) {
        //            // If the previous sibling of the parent is PackageDeclarationNode, that means we have already
        // added
        //            // package declaration. If it is ImportDeclarationNode, no need to suggest 'package' keyword
        // because we
        //            // cannot add package declaration after an import.
        //            addFileLevelKeywordsAsLookups(resultSet, false, true);
        //        } else if (parent instanceof PackageNameNode) {
        //            handlePackageNameNode(parameters, resultSet);
        //        } else if (parent instanceof ImportDeclarationNode) {
        //            handleImportDeclarationNode(parameters, resultSet);
        //        } else if (parent instanceof ConstantDefinitionNode) {
        //            handleConstantDefinitionNode(parameters, resultSet);
        //        } else if (parent instanceof TypeNameNode || parent instanceof NameReferenceNode) {
        //            handleNameReferenceNode(parameters, resultSet);
        //        } else if (parent instanceof StatementNode || parent instanceof WorkerInterationStatementNode) {
        //            handleStatementNode(parameters, resultSet);
        //        } else if (parent instanceof VariableReferenceNode) {
        //            handleVariableReferenceNode(parameters, resultSet);
        //        } else if (parent instanceof ActionInvocationNode) {
        //            handleActionInvocationNode(parameters, resultSet);
        //        } else if (parent instanceof FunctionInvocationStatementNode) {
        //            handleFunctionInvocationStatementNode(parameters, resultSet);
        //        } else if (parent instanceof PsiErrorElement) {
        //            handlePsiErrorElement(parameters, resultSet);
        //        } else if (parent instanceof VariableDefinitionNode) {
        //            identifyAndAddSuggestions(parameters, resultSet);
        //        } else if (parent instanceof AnnotationAttributeNode) {
        //            handleAnnotationAttributeNode(parameters, resultSet);
        //        } else if (parent instanceof ResourceDefinitionNode) {
        //            handleResourceDefinitionNode(parameters, resultSet);
        //        } else if (parent instanceof ActionDefinitionNode) {
        //            handleActionDefinitionNode(parameters, resultSet);
        //        } else if (parent instanceof WorkerReplyNode || parent instanceof TriggerWorkerNode) {
        //            handleWorkerReferenceNode(parameters, resultSet);
        //        } else if (parent instanceof JoinConditionNode) {
        //            handleJoinConditionNode(parameters, resultSet);
        //        } else if (parent instanceof FieldNode) {
        //            handleFieldNode(parameters, resultSet);
        //        } else {
        //            // If we are currently at an identifier node or a comment node, no need to suggest.
        //            if (element instanceof IdentifierPSINode || element instanceof PsiComment) {
        //                return;
        //            }
        //            if (parentPrevSibling == null) {
        //                addFileLevelKeywordsAsLookups(resultSet, true, true);
        //            } else {
        //                addFileLevelKeywordsAsLookups(resultSet, false, true);
        //            }
        //        }
    }

    private void handleFieldNode(CompletionParameters parameters, CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        int offset = parameters.getOffset();
        PsiElement prevElement = getPreviousNonEmptyElement(originalFile, offset);
        PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile,
                prevElement.getTextOffset());
        if (previousNonEmptyElement instanceof LeafPsiElement &&
                ((LeafPsiElement) previousNonEmptyElement).getElementType() == BallerinaTypes.RBRACK) {
            if (resolveElementAndSuggestArrayLength(parameters, resultSet, previousNonEmptyElement)) {
                return;
            }
            return;
        }
        // Eg: person.<caret>
        PsiElement structReference = originalFile.findElementAt(prevElement.getTextOffset() - 1);
        addStructFields(parameters, resultSet, structReference, null, false, false);
    }

    private void handleJoinConditionNode(CompletionParameters parameters, CompletionResultSet resultSet) {
        PsiElement position = parameters.getPosition();
        PsiElement parent = position.getParent();
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile, position.getTextOffset());
        if (previousNonEmptyElement instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) previousNonEmptyElement).getElementType();
            if (elementType == BallerinaTypes.INTEGER_LITERAL || elementType == BallerinaTypes.COMMA
                    || elementType == BallerinaTypes.ALL) {
                List<WorkerDeclarationNode> workerDeclarations = BallerinaPsiImplUtil.getWorkerDeclarations(parent);
                addWorkersAsLookup(resultSet, workerDeclarations);
            }
        }
    }

    private void handleWorkerReferenceNode(CompletionParameters parameters, CompletionResultSet resultSet) {
        PsiElement position = parameters.getPosition();
        PsiElement parent = position.getParent();
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile, position.getTextOffset());

        if (previousNonEmptyElement instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) previousNonEmptyElement).getElementType();
            if (elementType == BallerinaTypes.SENDARROW || elementType == BallerinaTypes.RECEIVEARROW) {
                List<WorkerDeclarationNode> workerDeclarations = BallerinaPsiImplUtil.getWorkerDeclarations(parent);
                addWorkersAsLookup(resultSet, workerDeclarations);
                addDefaultAsLookup(resultSet);
            }
        }
    }

    private void handleResourceDefinitionNode(CompletionParameters parameters, CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement prevSibling = getPreviousNonEmptyElement(originalFile, element.getTextOffset());
        if (prevSibling != null) {
            if (prevSibling instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                if (elementType == BallerinaTypes.RBRACE) {
                    addServiceSpecificKeywords(resultSet);
                }
            }
        }
    }

    private void handleActionDefinitionNode(CompletionParameters parameters, CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement prevSibling = getPreviousNonEmptyElement(originalFile, element.getTextOffset());
        if (prevSibling != null) {
            if (prevSibling instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                if (elementType == BallerinaTypes.RBRACE) {
                    addConnectorSpecificKeywords(resultSet);
                }
            }
        }
    }

    /**
     * Add lookups for package declarations.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    private void handlePackageNameNode(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement superParent = parent.getParent();
        // Check whether we are in a package declaration node
        if (superParent.getParent() instanceof PackageDeclarationNode) {
            // If we are in a package declaration node, suggest packages.
            addPackageSuggestions(resultSet, element);
        } else if (superParent.getParent() instanceof ImportDeclarationNode &&
                !(superParent instanceof AliasNode)) {
            // If the parent is not an AliasNode and is inside the ImportDeclarationNode, we need to suggest
            // packages.
            addImportSuggestions(resultSet, element);
        } else {
            BasicInsertHandler basicInsertHandler = new BasicInsertHandler();
            InsertHandler[] insertHandlers = new InsertHandler[]{basicInsertHandler,
                    BallerinaAutoImportInsertHandler.INSTANCE};
            addLookups(resultSet, originalFile, true, false, false, false, insertHandlers);
        }
    }

    /**
     * Adds package suggestions in the package declaration nodes.
     *
     * @param resultSet      result list which is used to add lookups
     * @param identifierNode node which contains the package name
     */
    private void addPackageSuggestions(@NotNull CompletionResultSet resultSet, @NotNull PsiElement identifierNode) {
        // If we are in a package declaration, we only need to suggest the path to the current file.
        PsiDirectory[] psiDirectories = BallerinaPsiImplUtil.suggestCurrentPackagePath(identifierNode);
        for (PsiDirectory directory : psiDirectories) {
            InsertHandler<LookupElement> insertHandler;
            // If the package does have any sub packages, we need to add the insert handler with auto popup
            // enabled with will add '.' at the end of the package and show the sub packages in the popup.
            if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            } else {
                // If the package does not have sub packages, we need to add the ';' at the end.
                insertHandler = StatementCompletionInsertHandler.INSTANCE;
            }
            // Add directories as lookup elements.
            addPackagesAsLookups(resultSet, directory, insertHandler);
        }
    }

    /**
     * Adds package suggestions in the import declaration nodes.
     *
     * @param resultSet      result list which is used to add lookups
     * @param identifierNode node which contains the package name
     */
    private void addImportSuggestions(@NotNull CompletionResultSet resultSet, @NotNull PsiElement identifierNode) {
        // Suggest import packages.
        PsiDirectory[] packageDirectories = BallerinaPsiImplUtil.suggestImportPackages(identifierNode);
        // Get names of all imported packages.
        List<String> allImportedPackages =
                BallerinaPsiImplUtil.getImportedPackages(identifierNode.getContainingFile()).stream()
                        .map(PsiElement::getText)
                        .collect(Collectors.toList());

        // Add each directory as lookup elements.
        for (PsiDirectory directory : packageDirectories) {
            InsertHandler<LookupElement> insertHandler;
            // If the package has sub packages, use the auto popup insert handler.
            if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            } else {
                // If there are no sub packages and the current package name is already imported, use the
                // AliasCompletionInsertHandler.
                if (allImportedPackages.contains(directory.getName())) {
                    insertHandler = AliasCompletionInsertHandler.INSTANCE;
                } else {
                    // If the current package is not imported previously, use StatementCompletionInsertHandler.
                    insertHandler = StatementCompletionInsertHandler.INSTANCE;
                }
            }
            addPackagesAsLookups(resultSet, directory, insertHandler);
        }
    }

    /**
     * Add lookups for import declarations.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleImportDeclarationNode(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiDirectory[] psiDirectories = BallerinaPsiImplUtil.suggestImportPackages(element);
        for (PsiDirectory directory : psiDirectories) {
            InsertHandler<LookupElement> insertHandler;
            if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            } else {
                insertHandler = StatementCompletionInsertHandler.INSTANCE;
            }
            resultSet.addElement(LookupElementBuilder.create(directory).withInsertHandler(insertHandler));
        }
    }

    /**
     * Add lookups for constant definitions.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleConstantDefinitionNode(@NotNull CompletionParameters parameters,
                                              @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiElement prevSibling = element.getPrevSibling();
        // Eg: const <caret>
        if (prevSibling != null && prevSibling instanceof ValueTypeNameNode) {
            addValueTypesAsLookups(resultSet);
        }
    }

    /**
     * Add lookups for name references.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleNameReferenceNode(@NotNull CompletionParameters parameters,
                                         @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiFile originalFile = parameters.getOriginalFile();

        ParameterNode parameterNode = PsiTreeUtil.getParentOfType(parent, ParameterNode.class);
        if (parameterNode != null) {
            checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                    this::handleIdentifierInParameter,
                    this::handleLeafElementInParameter,
                    this::handleOtherTypesInParameter);
            return;
        }

        ReturnTypeListNode returnTypeListNode = PsiTreeUtil.getParentOfType(parent, ReturnTypeListNode.class);
        if (returnTypeListNode != null) {
            addTypeNamesAsLookups(resultSet);
            addLookups(resultSet, originalFile, true, false, true, true);
        }

        AnnotationDefinitionNode annotationDefinitionNode = PsiTreeUtil.getParentOfType(element,
                AnnotationDefinitionNode.class);
        if (annotationDefinitionNode != null) {
            addAttachmentPointsAsLookups(resultSet);
            return;
        }
        if (handleGlobalVariableDefinition(parameters, resultSet)) {
            return;
        }
        if (handleServiceBodyInReferenceNode(parameters, resultSet)) {
            return;
        }
        if (handleConnectorBodyInReferenceNode(parameters, resultSet)) {
            return;
        }
        PsiElement superParent = parent.getParent();
        if (superParent instanceof AnnotationAttachmentNode && handleAnnotationAttachmentNode(parameters, resultSet)) {
            return;
        }

        checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                this::handleIdentifierInNameReference,
                this::handleLeafElementInNameReference,
                null);
    }

    private boolean handleGlobalVariableDefinition(@NotNull CompletionParameters parameters,
                                                   @NotNull CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();

        GlobalVariableDefinitionNode globalVariableNode = PsiTreeUtil.getParentOfType(element,
                GlobalVariableDefinitionNode.class);
        if (globalVariableNode != null) {
            PsiElement e = parameters.getPosition();
            checkPrevNodeAndHandle(parameters, resultSet, e.getTextOffset(),
                    (p, r, prevElement) -> {
                        PsiElement parent = prevElement.getParent();
                        if (parent instanceof AnnotationDefinitionNode) {
                            addAttachKeyword(resultSet);
                        } else {
                            addTypeNamesAsLookups(resultSet);
                            if (parent.getTextOffset() == 0) {
                                addFileLevelKeywordsAsLookups(resultSet, true, true);
                            } else {
                                addLookups(resultSet, originalFile, true, false, true, true);
                            }
                        }
                    },
                    (p, r, prevElement) -> {
                        IElementType elementType = ((LeafPsiElement) prevElement).getElementType();
                        if (elementType == BallerinaTypes.SEMI) {
                            addFileLevelKeywordsAsLookups(resultSet, false, true);
                            addTypeNamesAsLookups(resultSet);
                            addLookups(resultSet, originalFile, true, false, true, true);
                        } else if (elementType == BallerinaTypes.COLON) {
                            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                            if (packageNode != null) {
                                PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile,
                                        packageNode.getTextOffset());
                                if ("@".equals(previousNonEmptyElement.getText())) {
                                    String type = getAttachmentType(parameters);
                                    if (type == null) {
                                        return;
                                    }
                                    suggestAnnotationsFromPackage(parameters, resultSet, packageNode, type);
                                } else if (prevElement.getParent() instanceof AnnotationAttributeNode) {
                                    addValueKeywords(resultSet);
                                } else {
                                    suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true,
                                            true, true);
                                }
                            }
                        } else if (elementType == BallerinaTypes.RBRACE || prevElement instanceof PsiWhiteSpace) {
                            addFileLevelKeywordsAsLookups(resultSet, true, true);
                            addTypeNamesAsLookups(resultSet);
                        } else if (elementType == BallerinaTypes.ASSIGN || isExpressionSeparator(elementType)) {
                            addLookups(resultSet, originalFile, true, true, false, false);
                        } else if (elementType == BallerinaTypes.LPAREN || elementType == BallerinaTypes.COMMA) {
                            addLookups(resultSet, originalFile, true, true, true, false);
                        }
                        if (prevElement.getTextOffset() == 0) {
                            addFileLevelKeywordsAsLookups(resultSet, true, true);
                        }
                    },
                    (p, r, prevElement) -> {
                        addTypeNamesAsLookups(resultSet);
                        addFileLevelKeywordsAsLookups(resultSet, true, true);
                    });
            return true;
        }
        return false;
    }

    private boolean handleServiceBodyInReferenceNode(@NotNull CompletionParameters parameters,
                                                     @NotNull CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();

        ServiceBodyNode serviceBodyNode = PsiTreeUtil.getParentOfType(parent, ServiceBodyNode.class);
        if (serviceBodyNode == null) {
            return false;
        }
        ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(parent,
                ResourceDefinitionNode.class);
        if (resourceDefinitionNode != null) {
            return false;
        }
        AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(parent,
                AnnotationAttachmentNode.class);
        if (annotationAttachmentNode == null) {
            if (handleBodyNode(parameters, resultSet, originalFile, element, true)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleConnectorBodyInReferenceNode(@NotNull CompletionParameters parameters,
                                                       @NotNull CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();

        ConnectorBodyNode connectorBodyNode = PsiTreeUtil.getParentOfType(parent, ConnectorBodyNode.class);
        if (connectorBodyNode == null) {
            return false;
        }
        ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(parent,
                ActionDefinitionNode.class);
        if (actionDefinitionNode != null) {
            return false;
        }
        AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(parent,
                AnnotationAttachmentNode.class);
        if (annotationAttachmentNode == null) {
            if (handleBodyNode(parameters, resultSet, originalFile, element, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleBodyNode(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet,
                                   PsiFile originalFile, PsiElement element, boolean isResource) {
        PsiElement prevSibling = getPreviousNonEmptyElement(originalFile, element.getTextOffset());
        if ((prevSibling instanceof LeafPsiElement)) {
            IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
            if (elementType == BallerinaTypes.AT) {
                addLookups(resultSet, originalFile, true, false, false, false);
                checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                        this::handleIdentifierInNameReference,
                        this::handleLeafElementInNameReference,
                        null);
            } else if (elementType == BallerinaTypes.COLON) {
                if (element.getParent().getText().matches("@.*")) {
                    String type = getAttachmentType(parameters);
                    if (type == null) {
                        return true;
                    }
                    PsiElement packageNode = originalFile.findElementAt(prevSibling.getTextOffset() - 2);
                    suggestAnnotationsFromPackage(parameters, resultSet, packageNode, type);
                } else {
                    PsiElement packageNode = originalFile.findElementAt(prevSibling.getTextOffset() - 2);
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, false, true,
                            true, true);
                }
            } else if (elementType == BallerinaTypes.ASSIGN) {
                addCreateKeyword(resultSet);
                addLookups(resultSet, originalFile, true, true, true, false);
            } else if (elementType == BallerinaTypes.CREATE) {
                addLookups(resultSet, originalFile, true, true, true, false);
            } else if (elementType == BallerinaTypes.LBRACE || elementType == BallerinaTypes.RBRACE
                    || elementType == BallerinaTypes.SEMI) {
                addLookups(resultSet, originalFile, true, false, true, true);
                addTypeNamesAsLookups(resultSet);
                if (isResource) {
                    addServiceSpecificKeywords(resultSet);
                } else {
                    addConnectorSpecificKeywords(resultSet);
                }
            }
            return true;
        }
        return false;
    }

    private void handleIdentifierInNameReference(@NotNull CompletionParameters parameters,
                                                 @NotNull CompletionResultSet resultSet,
                                                 @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement token = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset());

        checkPrevNodeAndHandle(parameters, resultSet, prevElement.getTextOffset(),
                null,
                (p, r, prev) -> {
                    IElementType elementType = ((LeafPsiElement) token).getElementType();
                    if (elementType == BallerinaTypes.ASSIGN) {
                        // Eg: function test(){ string s = <caret> + " world"; }
                        addCreateKeyword(resultSet);
                        addLookups(resultSet, originalFile, true, true, true, true);
                        addVariableTypesAsLookups(resultSet, originalFile, element);
                    } else if (elementType == BallerinaTypes.COLON) {
                        // This will be called instead of handle statement node if there are codes after the caret.
                        // Eg: function test(){ test:te<caret> \n some_other_codes}
                        if (prevElement.getParent() instanceof NameReferenceNode) {
                            // Eg: User user = { name: get<caret> }
                            PsiElement prevSibling = prevElement.getPrevSibling();
                            if (prevSibling != null && prevSibling.getText().matches(".*:.*")) {
                                // Eg: User user = { name: utils:get<caret> }
                                PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                                suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true, true, true);
                            } else {
                                addLookups(resultSet, originalFile, true, true, true, true);
                                addVariableTypesAsLookups(resultSet, originalFile, element);
                            }
                        } else {
                            // Eg: User user = { name: <caret> }
                            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                            suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true, true, false);
                        }
                    } else if (elementType == BallerinaTypes.DOT) {
                        PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile,
                                prevElement.getTextOffset());
                        if (previousNonEmptyElement instanceof LeafPsiElement &&
                                ((LeafPsiElement) previousNonEmptyElement).getElementType() == BallerinaTypes.RBRACK) {
                            if (resolveElementAndSuggestArrayLength(parameters, resultSet, previousNonEmptyElement)) {
                                return;
                            }
                            return;
                        }
                        // Eg: person.<caret>
                        PsiElement structReference = originalFile.findElementAt(prevElement.getTextOffset() - 1);
                        addStructFields(parameters, resultSet, structReference, null, false, false);
                    } else if (elementType == BallerinaTypes.AT) {
                        // Eg: annotation TEST attach action {} connector C(){ @T<caret> action A()(message) {} }
                        String attachmentType = getAttachmentType(parameters);
                        if (attachmentType != null) {
                            suggestAnnotationsFromPackage(parameters, resultSet, null, attachmentType);
                        }
                    } else if (elementType == BallerinaTypes.LBRACE || elementType == BallerinaTypes.COMMA) {
                        MapStructLiteralNode node = PsiTreeUtil.getParentOfType(element, MapStructLiteralNode.class);
                        if (node != null) {
                            // Eg: User user = {n<caret>}
                            addStructFields(parameters, resultSet, element);
                        } else {
                            FieldDefinitionNode fieldDefinitionNode = PsiTreeUtil.getParentOfType(element,
                                    FieldDefinitionNode.class);
                            if (fieldDefinitionNode != null) {
                                addLookups(resultSet, originalFile, true, false, true, true);
                            } else {
                                addLookups(resultSet, originalFile, true, true, true, false);
                                addVariableTypesAsLookups(resultSet, originalFile, element);
                                addFunctionSpecificKeywords(parameters, resultSet);
                                addCommonKeywords(resultSet);
                            }
                            addTypeNamesAsLookups(resultSet);
                        }
                    } else if (isExpressionSeparator(elementType) || elementType == BallerinaTypes.LPAREN) {
                        // Eg: int a = 10 +
                        // Eg: system:println(<caret>)
                        addLookups(resultSet, originalFile, true, true, true, false);
                        addVariableTypesAsLookups(resultSet, originalFile, element);
                    } else {
                        ForkJoinStatementNode node = PsiTreeUtil.getParentOfType(element, ForkJoinStatementNode.class);
                        if (node != null) {
                            PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile,
                                    element.getTextOffset() - 1);
                            if (previousNonEmptyElement instanceof LeafPsiElement) {
                                IElementType prevTokenElementType = ((LeafPsiElement) previousNonEmptyElement)
                                        .getElementType();
                                if (prevTokenElementType == BallerinaTypes.JOIN) {
                                    addJoinConditionKeywords(resultSet);
                                    return;
                                }
                            }
                        }
                        // Eg: function test(){t<caret>}
                        addTypeNamesAsLookups(resultSet);
                        addLookups(resultSet, originalFile, true, true, true, true);
                        addVariableTypesAsLookups(resultSet, originalFile, element);
                        addCommonKeywords(resultSet);
                    }
                },
                (p, r, prev) -> {
                    addLookups(resultSet, originalFile, true, true, true, true);
                    addVariableTypesAsLookups(resultSet, originalFile, element);
                });
    }


    private void handleLeafElementInNameReference(@NotNull CompletionParameters parameters,
                                                  @NotNull CompletionResultSet resultSet,
                                                  @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        IElementType elementType = ((LeafPsiElement) prevElement).getElementType();
        // Cannot use a switch statement since the types are not constants and declaring them final does not fix
        // the issue as well.
        if (elementType == BallerinaTypes.ASSIGN) {
            addCreateKeyword(resultSet);
            addLookups(resultSet, originalFile, true, true, true, true);
            addVariableTypesAsLookups(resultSet, originalFile, element);
        } else if (elementType == BallerinaTypes.COLON) {
            // Eg: function test(){ test:<caret> }
            // Eg: User user = { name:<caret> }
            if (prevElement.getParent() instanceof MapStructKeyValueNode) {
                PsiElement prevSibling = prevElement.getPrevSibling();
                if (prevSibling != null && prevSibling.getText().matches(".+:.+")) {
                    // Eg: User user = { name: utils:<caret> }
                    PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true, false, true);
                } else {
                    addLookups(resultSet, originalFile, true, true, true, false);
                    addVariableTypesAsLookups(resultSet, originalFile, element);
                }
            } else {
                PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true, true, true);
            }
        } else if (elementType == BallerinaTypes.DOT) {
            PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset());
            if (previousNonEmptyElement instanceof LeafPsiElement &&
                    ((LeafPsiElement) previousNonEmptyElement).getElementType() == BallerinaTypes.RBRACK) {
                if (resolveElementAndSuggestArrayLength(parameters, resultSet, previousNonEmptyElement)) {
                    return;
                }
                return;
            }
            // Eg: person.<caret>
            PsiElement structReference = originalFile.findElementAt(prevElement.getTextOffset() - 1);
            addStructFields(parameters, resultSet, structReference, null, false, false);
        } else if (elementType == BallerinaTypes.CREATE) {
            // Eg: test:Connector c = create <caret>
            addLookups(resultSet, originalFile, true, true, true, true);
            addVariableTypesAsLookups(resultSet, originalFile, element);
        } else if (elementType == BallerinaTypes.AT) {
            addLookups(resultSet, originalFile, true, false, false, false);
            String attachmentType = getAttachmentType(parameters);
            if (attachmentType != null) {
                suggestAnnotationsFromPackage(parameters, resultSet, null, attachmentType);
            }
        } else if (elementType == BallerinaTypes.LBRACE || elementType == BallerinaTypes.COMMA) {
            MapStructLiteralNode node = PsiTreeUtil.getParentOfType(element, MapStructLiteralNode.class);
            if (node != null) {
                // Eg: User user = {<caret>}
                addStructFields(parameters, resultSet, element);
            } else {
                FieldDefinitionNode fieldDefinitionNode = PsiTreeUtil.getParentOfType(element,
                        FieldDefinitionNode.class);
                if (fieldDefinitionNode != null) {
                    addLookups(resultSet, originalFile, true, false, true, true);
                } else {
                    addLookups(resultSet, originalFile, true, true, true, false);
                    addVariableTypesAsLookups(resultSet, originalFile, element);
                    addFunctionSpecificKeywords(parameters, resultSet);
                    addCommonKeywords(resultSet);
                }
                addTypeNamesAsLookups(resultSet);
            }
        } else if (isExpressionSeparator(elementType) || elementType == BallerinaTypes.LPAREN) {
            // Eg: int a = 10 +
            // Eg: system:println(<caret>)
            addLookups(resultSet, originalFile, true, true, true, false);
            addVariableTypesAsLookups(resultSet, originalFile, element);
        } else {
            ForkJoinStatementNode node = PsiTreeUtil.getParentOfType(element, ForkJoinStatementNode.class);
            if (node != null) {
                PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(originalFile,
                        element.getTextOffset() - 1);
                if (previousNonEmptyElement instanceof LeafPsiElement) {
                    IElementType prevTokenElementType = ((LeafPsiElement) previousNonEmptyElement).getElementType();
                    if (prevTokenElementType == BallerinaTypes.JOIN) {
                        addJoinConditionKeywords(resultSet);
                        return;
                    }
                }
            }
            // Eg: function test(){ <caret> \n OTHER_CODES }
            addTypeNamesAsLookups(resultSet);
            addLookups(resultSet, originalFile, true, true, true, true);
            addVariableTypesAsLookups(resultSet, originalFile, element);
            addCommonKeywords(resultSet);
        }
    }

    private boolean handleAnnotationAttachmentNode(@NotNull CompletionParameters parameters,
                                                   @NotNull CompletionResultSet resultSet) {
        final String attachmentType = getAttachmentType(parameters);
        if (attachmentType == null) {
            return false;
        }
        PsiFile originalFile = parameters.getOriginalFile();
        checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                (p, r, prev) -> {
                    PsiElement token = getPreviousNonEmptyElement(originalFile, prev.getTextOffset());
                    if (token != null && token instanceof LeafPsiElement) {
                        IElementType elementType = ((LeafPsiElement) token).getElementType();
                        if (elementType == BallerinaTypes.COLON) {
                            PsiElement packageNode = originalFile.findElementAt(prev.getTextOffset() - 2);
                            suggestAnnotationsFromPackage(parameters, resultSet, packageNode, attachmentType);
                        } else {
                            addLookups(resultSet, originalFile, true, false, false, false);
                            suggestAnnotationsFromPackage(parameters, resultSet, null, attachmentType);
                        }
                    } else {
                        PsiElement packageNode = originalFile.findElementAt(prev.getTextOffset() - 2);
                        suggestAnnotationsFromPackage(parameters, resultSet, packageNode, attachmentType);
                    }
                },
                (p, r, prev) -> {
                    IElementType elementType = ((LeafPsiElement) prev).getElementType();
                    // Cannot use a switch statement since the types are not constants and declaring them final does
                    // not fix the issue as well.
                    if (elementType == BallerinaTypes.COLON) {
                        PsiElement packageNode = originalFile.findElementAt(prev.getTextOffset() - 2);
                        suggestAnnotationsFromPackage(parameters, resultSet, packageNode, attachmentType);
                    } else if (elementType == BallerinaTypes.AT) {
                        addLookups(resultSet, originalFile, true, false, false, false);
                        suggestAnnotationsFromPackage(parameters, resultSet, null, attachmentType);
                    }
                },
                null
        );
        return true;
    }

    /**
     * Returns the attachment type by checking the nodes.
     *
     * @param parameters parameters which passed to completion contributor
     * @return attachment type.
     */
    @Nullable
    private String getAttachmentType(@NotNull CompletionParameters parameters) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement superParent = parent.getParent();
        PsiElement nextSibling = PsiTreeUtil.skipSiblingsForward(superParent, PsiWhiteSpace.class, PsiComment.class,
                AnnotationAttachmentNode.class);
        String type = null;
        if (nextSibling == null) {
            AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(element,
                    AnnotationAttachmentNode.class);
            if (annotationAttachmentNode != null) {
                PsiElement definitionNode = annotationAttachmentNode.getParent();
                type = getAnnotationAttachmentType(definitionNode);
            }
        } else if (nextSibling instanceof DefinitionNode) {
            PsiElement[] children = nextSibling.getChildren();
            if (children.length != 0) {
                PsiElement definitionNode = children[0];
                type = getAnnotationAttachmentType(definitionNode);
            }
        } else if (nextSibling.getParent() instanceof ResourceDefinitionNode) {
            type = "resource";
        } else if (nextSibling instanceof ActionDefinitionNode
                || nextSibling.getParent() instanceof ActionDefinitionNode || parent instanceof ActionDefinitionNode) {
            type = "action";
        } else if (nextSibling.getParent() instanceof ParameterNode) {
            type = "parameter";
        }
        return type;
    }

    /**
     * Contains logic to add lookup elements for a parameter node if the previous node is an identifier node.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleIdentifierInParameter(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet, @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement token = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset());
        if (token != null && token instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) token).getElementType();
            if (elementType == BallerinaTypes.COLON) {
                PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                if (packageNode == null) {
                    return;
                }
                PsiElement prevToken = getPreviousNonEmptyElement(originalFile, packageNode.getTextOffset());
                if (prevToken != null && prevToken instanceof LeafPsiElement) {
                    IElementType tokenElementType = ((LeafPsiElement) prevToken).getElementType();
                    if (tokenElementType == BallerinaTypes.AT) {
                        String attachmentType = getAttachmentType(parameters);
                        if (attachmentType == null) {
                            return;
                        }
                        suggestAnnotationsFromPackage(parameters, resultSet, packageNode, attachmentType);
                    } else if (tokenElementType == BallerinaTypes.LPAREN) {
                        //Eg: function test(test:Na<caret> ){ }
                        suggestElementsFromAPackage(parameters, resultSet, packageNode, false, true, true, false);
                    }
                } else {
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, false, true, true, false);
                }
            } else {
                // Eg: function test(s<caret>){ }
                addTypeNamesAsLookups(resultSet);
                addLookups(resultSet, originalFile, true, false, true, true);
            }
            if (elementType == BallerinaTypes.LBRACE || elementType == BallerinaTypes.SEMI) {
                addTypeNamesAsLookups(resultSet);
                addLookups(resultSet, originalFile, false, false, false, true);
            }
        } else {
            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            suggestElementsFromAPackage(parameters, resultSet, packageNode, false, true, true, false);
        }
    }

    /**
     * Contains logic to add lookup elements for a parameter node if the previous node is an leaf element node.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleLeafElementInParameter(@NotNull CompletionParameters parameters,
                                              @NotNull CompletionResultSet resultSet, @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        IElementType elementType = ((LeafPsiElement) prevElement).getElementType();
        // Cannot use a switch statement since the types are not constants and declaring them final does not fix
        // the issue as well.
        if (elementType == BallerinaTypes.COLON) {
            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            if (packageNode == null) {
                return;
            }
            PsiElement prevToken = getPreviousNonEmptyElement(originalFile, packageNode.getTextOffset());
            if (prevToken != null && prevToken instanceof LeafPsiElement) {
                IElementType tokenElementType = ((LeafPsiElement) prevToken).getElementType();
                if (tokenElementType == BallerinaTypes.AT) {
                    String attachmentType = getAttachmentType(parameters);
                    if (attachmentType == null) {
                        return;
                    }
                    suggestAnnotationsFromPackage(parameters, resultSet, packageNode, attachmentType);
                } else if (tokenElementType == BallerinaTypes.LPAREN) {
                    //Eg: function test(test:<caret> ){ }
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, false, true, true, false);
                }
            } else {
                suggestElementsFromAPackage(parameters, resultSet, packageNode, false, true, true, false);
            }
        } else if (elementType == BallerinaTypes.LPAREN || elementType == BallerinaTypes.COMMA) {
            // Eg: function test(<caret>) { }
            addTypeNamesAsLookups(resultSet);
            addLookups(resultSet, originalFile, true, false, true, true);
        } else if (elementType == BallerinaTypes.AT) {
            addLookups(resultSet, originalFile, true, false, false, false);
            String attachmentType = getAttachmentType(parameters);
            if (attachmentType == null) {
                return;
            }
            suggestAnnotationsFromPackage(parameters, resultSet, null, attachmentType);
        }
    }

    /**
     * Contains logic to add lookup elements for a parameter node if the previous node is not an identifier or leaf
     * element node.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleOtherTypesInParameter(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet, @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();

        addCreateKeyword(resultSet);
        addTypeNamesAsLookups(resultSet);
        addCommonKeywords(resultSet);
        addFunctionSpecificKeywords(parameters, resultSet);
        addResourceSpecificKeywords(parameters, resultSet);
        addActionSpecificKeywords(parameters, resultSet);
        addVariableTypesAsLookups(resultSet, originalFile, prevElement);
    }

    /**
     * Handles function invocation statements. Mostly this will be called when the parameters (for a function
     * invocation, etc) are being typed.
     * <p>
     * Eg: system:println(test:|)
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleFunctionInvocationStatementNode(@NotNull CompletionParameters parameters,
                                                       @NotNull CompletionResultSet resultSet) {
        identifyAndAddSuggestions(parameters, resultSet);
    }

    /**
     * Identifies package calls and adds relevant lookup elements from the package.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void identifyAndAddSuggestions(CompletionParameters parameters, CompletionResultSet resultSet) {
        // Get element at the caret.
        PsiElement element = parameters.getPosition();
        // Get the previous sibling element.
        PsiElement prevSibling = element.getPrevSibling();

        if (prevSibling == null) {
            return;
        }
        // Previous sibling will most probably contain the package. Eg: "package:<caret>". Check whether previous
        // element text matches the pattern.
        if (prevSibling.getText().matches(".+:")) {
            // In the previous sibling node, IdentifierPSINode will contain the package name.
            IdentifierPSINode packageName = PsiTreeUtil.findElementOfClassAtOffset(prevSibling.getContainingFile(),
                    prevSibling.getTextOffset() + prevSibling.getTextLength() - 2, IdentifierPSINode.class, false);
            if (packageName == null) {
                return;
            }
            suggestElementsFromAPackage(parameters, resultSet, packageName, true, true, true, true);
        }
    }

    private void handleVariableReferenceNode(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        // If we are in the {} of a struct variable reference node, we need to show fields.
        if (addStructFields(parameters, resultSet, element)) {
            return;
        }

        checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                (p, r, prev) -> {
                    PsiElement token = getPreviousNonEmptyElement(originalFile, prev.getTextOffset());
                    if (token != null && token instanceof LeafPsiElement) {
                        IElementType elementType = ((LeafPsiElement) token).getElementType();
                        // Eg: system:println(person.n<caret>)
                        if (elementType == BallerinaTypes.DOT) {
                            PsiElement structReferenceNode = getPreviousNonEmptyElement(originalFile, prev
                                    .getTextOffset() - 2);
                            addStructFields(parameters, resultSet, structReferenceNode, null, false, false);
                        } else if (elementType == BallerinaTypes.COLON) {
                            // Eg: Person p = {name:n<caret>}
                            addLookups(resultSet, originalFile, false, true, false, false);
                            addVariableTypesAsLookups(resultSet, originalFile, prev);
                        } else {
                            MapStructLiteralNode mapStructLiteralNode = PsiTreeUtil.getParentOfType(prev,
                                    MapStructLiteralNode.class);
                            if (mapStructLiteralNode != null) {
                                VariableDefinitionNode structDefinitionNode = PsiTreeUtil.getParentOfType(prev,
                                        VariableDefinitionNode.class);
                                if (structDefinitionNode != null) {
                                    addStructFields(parameters, resultSet, null, structDefinitionNode, true, true);
                                }
                            } else {
                                addLookups(resultSet, originalFile, true, true, true, false);
                                addVariableTypesAsLookups(resultSet, originalFile, element);
                            }
                        }
                    } else {
                        addLookups(resultSet, originalFile, true, true, false, false);
                        addVariableTypesAsLookups(resultSet, originalFile, element);
                    }
                },
                (p, r, prev) -> {
                    IElementType elementType = ((LeafPsiElement) prev).getElementType();
                    if (elementType == BallerinaTypes.DOT) {
                        // Eg: system:println(person.<caret>)
                        PsiElement structReferenceNode = getPreviousNonEmptyElement(originalFile, prev.getTextOffset());
                        addStructFields(parameters, resultSet, structReferenceNode, null, false, false);
                    } else if (elementType == BallerinaTypes.COLON) {
                        // Eg: Person p = {name:<caret>}
                        addLookups(resultSet, originalFile, false, true, false, false);
                        addVariableTypesAsLookups(resultSet, originalFile, prev);
                    } else {
                        MapStructLiteralNode mapStructLiteralNode = PsiTreeUtil.getParentOfType(prev,
                                MapStructLiteralNode.class);
                        if (mapStructLiteralNode != null) {
                            VariableDefinitionNode structDefinitionNode = PsiTreeUtil.getParentOfType(prev,
                                    VariableDefinitionNode.class);
                            if (structDefinitionNode != null) {
                                addStructFields(parameters, resultSet, null, structDefinitionNode, true, true);
                            }
                        } else {
                            addLookups(resultSet, originalFile, true, true, true, false);
                            addVariableTypesAsLookups(resultSet, originalFile, element);
                        }
                    }
                },
                null);

        AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(element,
                AssignmentStatementNode.class);
        if (assignmentStatementNode != null) {
            VariableReferenceNode variableReferenceNode = PsiTreeUtil.findChildOfType(assignmentStatementNode,
                    VariableReferenceNode.class);
            if (variableReferenceNode != null) {
                addStructFields(parameters, resultSet, variableReferenceNode.getNameIdentifier(), null, true, true);
            }
        }
    }

    private boolean addStructFields(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet resultSet,
                                    PsiElement element) {
        // If we are in the {} of a struct variable reference node, we need to show fields.
        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(element,
                VariableDefinitionNode.class);
        if (variableDefinitionNode != null) {
            ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(element, ExpressionNode.class);
            if (expressionNode == null || expressionNode.getText().contains("IntellijIdeaRulezzz")) {
                addStructFields(parameters, resultSet, null, variableDefinitionNode, true, true);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds lookups for a statement node.
     *
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     */
    private void handleStatementNode(@NotNull CompletionParameters parameters,
                                     @NotNull CompletionResultSet resultSet) {
        checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(), this::handleIdentifierInStatement,
                this::handleLeafElementsInStatement, this::handleOtherTypesInStatement);
    }

    private void handleIdentifierInStatement(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet, @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement token = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset());
        // Eg: http:get<caret>
        if (token != null && token instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) token).getElementType();
            if (elementType == BallerinaTypes.COLON) {
                // This will be called if there are no lines after the caret. If there are codes after the caret, it
                // will be identified as reference node.
                // Eg: function test(){ test:<caret> }
                PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true,
                        true, true);
            } else if (elementType == BallerinaTypes.DOT) {
                PsiElement element = getPreviousNonEmptyElement(originalFile, token.getTextOffset());
                if (element instanceof LeafPsiElement &&
                        ((LeafPsiElement) element).getElementType() == BallerinaTypes.RBRACK) {
                    if (resolveElementAndSuggestArrayLength(parameters, resultSet, element)) {
                        return;
                    }
                    return;
                }
                if (!(element instanceof IdentifierPSINode)) {
                    element = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset() - 1);
                }
                PsiReference reference = element.getReference();
                if (reference == null) {
                    return;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return;
                }
                if (resolvedElement.getParent() instanceof ConnectorDefinitionNode) {
                    // Eg. Twitter.<caret>
                    handleActionInvocationNode(parameters, resultSet);
                } else {
                    // Eg: user.<caret>
                    addStructFields(parameters, resultSet, element, null, false, true);
                }
            } else if (elementType == BallerinaTypes.ASSIGN) {
                PsiElement currentElement = originalFile.findElementAt(parameters.getOffset());
                addLookups(resultSet, originalFile, true, true, true, true);
                addVariableTypesAsLookups(resultSet, originalFile, currentElement);
                addCreateKeyword(resultSet);
            } else {
                PsiElement currentElement = originalFile.findElementAt(parameters.getOffset());
                if (currentElement instanceof PsiWhiteSpace) {
                    currentElement = getPreviousNonEmptyElement(originalFile, currentElement.getTextOffset());
                }
                addLookups(resultSet, originalFile, true, true, true, true);
                addVariableTypesAsLookups(resultSet, originalFile, currentElement);
                addFunctionSpecificKeywords(parameters, resultSet);
                addResourceSpecificKeywords(parameters, resultSet);
                addActionSpecificKeywords(parameters, resultSet);
                addCommonKeywords(resultSet);
            }

            if (elementType == BallerinaTypes.LBRACE || elementType == BallerinaTypes.RBRACE
                    || elementType == BallerinaTypes.SEMI) {
                addTypeNamesAsLookups(resultSet);
            }
        } else {
            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true, true, true);
        }
    }

    private void handleLeafElementsInStatement(@NotNull CompletionParameters parameters,
                                               @NotNull CompletionResultSet resultSet,
                                               @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        IElementType elementType = ((LeafPsiElement) prevElement).getElementType();

        PsiElement element = getPreviousNonEmptyElement(originalFile, parameters.getOffset());
        // Cannot use a switch statement since the types are not constants and declaring them final does not fix
        // the issue as well.
        if (elementType == BallerinaTypes.COLON) {
            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            suggestElementsFromAPackage(parameters, resultSet, packageNode, true, true, true, true);
        } else if (isExpressionSeparator(elementType)) {
            // Eg: string response = <caret>
            addLookups(resultSet, originalFile, true, true, true, false);
            addVariableTypesAsLookups(resultSet, originalFile, element);
            if (elementType == BallerinaTypes.ASSIGN) {
                addCreateKeyword(resultSet);
            }
        } else if (elementType == BallerinaTypes.DOT) {
            element = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset());
            if (element instanceof LeafPsiElement &&
                    ((LeafPsiElement) element).getElementType() == BallerinaTypes.RBRACK) {
                if (resolveElementAndSuggestArrayLength(parameters, resultSet, element)) {
                    return;
                }
                return;
            }
            if (!(element instanceof IdentifierPSINode)) {
                element = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset() - 1);
            }
            PsiReference reference = element.getReference();
            if (reference == null) {
                return;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return;
            }
            if (resolvedElement.getParent() instanceof ConnectorDefinitionNode) {
                // Eg. Twitter.<caret>
                handleActionInvocationNode(parameters, resultSet);
            } else {
                // Eg: user.<caret>
                addStructFields(parameters, resultSet, element, null, false, true);
            }
        } else {
            PsiElement currentElement = originalFile.findElementAt(parameters.getOffset());
            addTypeNamesAsLookups(resultSet);
            addLookups(resultSet, originalFile, true, true, true, true);
            addVariableTypesAsLookups(resultSet, originalFile, currentElement);
            addFunctionSpecificKeywords(parameters, resultSet);
            addResourceSpecificKeywords(parameters, resultSet);
            addActionSpecificKeywords(parameters, resultSet);
            addCommonKeywords(resultSet);
        }
    }

    private boolean resolveElementAndSuggestArrayLength(@NotNull CompletionParameters parameters,
                                                        @NotNull CompletionResultSet resultSet,
                                                        @NotNull PsiElement element) {

        PsiFile originalFile = parameters.getOriginalFile();
        // This is used to get the dimension of the reference. Eg: args.<caret>
        PsiElement parent = element.getParent();
        // We need to get the array reference which we are at. For this, we go back checking each element for a
        // reference.
        PsiElement temp = element;
        PsiReference reference = originalFile.findReferenceAt(temp.getTextOffset());
        while (reference == null && temp.getTextOffset() > 0) {
            temp = getPreviousNonEmptyElement(originalFile, temp.getTextOffset());
            if (temp instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) temp).getElementType();
                // If semicolon or left brace is found, that means we did not find any reference and we are now at
                // the previous statement. So we break from the loop.
                if (elementType == BallerinaTypes.SEMI || element == BallerinaTypes.LBRACE) {
                    break;
                }
            }
            // Get the reference at the current element location.
            reference = originalFile.findReferenceAt(temp.getTextOffset());
        }
        if (reference == null) {
            return true;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return true;
        }
        // Resolved element can be either a VariableDefinitionNode or a ParameterNode or a GlobalVariableDefinitionNode.
        // This happens if the reference is StatementReference.
        // If the reference is a NameReference, resolved element will be an identifier node. So the definition node
        // is the parent node.
        if (resolvedElement instanceof VariableDefinitionNode || resolvedElement instanceof ParameterNode
                || resolvedElement instanceof GlobalVariableDefinitionNode) {
            suggestArrayLength(resultSet, resolvedElement, parent);
        } else {
            PsiElement parentNode = resolvedElement.getParent();
            if (parentNode instanceof VariableDefinitionNode || parentNode instanceof ParameterNode
                    || parentNode instanceof GlobalVariableDefinitionNode) {
                VariableReferenceNode variableReferenceNode = PsiTreeUtil.getParentOfType(parent,
                        VariableReferenceNode.class, false, ExpressionNode.class);
                if (variableReferenceNode != null) {
                    suggestArrayLength(resultSet, parentNode, variableReferenceNode);
                } else {
                    suggestArrayLength(resultSet, parentNode, parent);
                }
            }
        }
        return false;
    }

    private void handleOtherTypesInStatement(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet, @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        addTypeNamesAsLookups(resultSet);
        addLookups(resultSet, originalFile, true, true, false, false);
        addVariableTypesAsLookups(resultSet, originalFile, prevElement);
        addFunctionSpecificKeywords(parameters, resultSet);
        addResourceSpecificKeywords(parameters, resultSet);
        addActionSpecificKeywords(parameters, resultSet);
        addCommonKeywords(resultSet);
        addCreateKeyword(resultSet);
    }

    /**
     * Adds struct fields as lookup elements.
     *
     * @param parameters                 parameters which contain details of completion invocation
     * @param resultSet                  result list which is used to add lookups
     * @param prevElement                element which can be used to resolve and get the struct variable definition
     * @param resolvedVariableDefElement struct variable definition element if it is available
     * @param withInsertHandler          insert handler which is to be used with the lookup elements
     */
    private void addStructFields(@NotNull CompletionParameters parameters,
                                 @NotNull CompletionResultSet resultSet, @Nullable PsiElement prevElement,
                                 @Nullable PsiElement resolvedVariableDefElement, boolean withInsertHandler,
                                 boolean withAutoCompletion) {
        if (resolvedVariableDefElement == null) {
            resolvedVariableDefElement = resolveStructReferenceToDefinition(parameters, prevElement);
            if (resolvedVariableDefElement == null) {
                if (prevElement != null) {
                    resolveElementAndSuggestArrayLength(parameters, resultSet, prevElement);
                }
                return;
            }
            if (resolvedVariableDefElement instanceof IdentifierPSINode) {
                resolvedVariableDefElement = resolvedVariableDefElement.getParent();
            }
        }
        // Todo - change method signature to var def
        PsiElement structDefinitionNode = BallerinaPsiImplUtil.resolveStructFromDefinitionNode
                (((VariableDefinitionNode) resolvedVariableDefElement));
        if (structDefinitionNode == null) {
            if (prevElement != null && resolvedVariableDefElement instanceof VariableDefinitionNode
                    || resolvedVariableDefElement instanceof ParameterNode
                    || resolvedVariableDefElement instanceof GlobalVariableDefinitionNode) {
                if (prevElement != null) {
                    suggestArrayLength(resultSet, resolvedVariableDefElement, prevElement);
                }
            }
            return;
        }
        Collection<FieldDefinitionNode> fieldDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(structDefinitionNode.getParent(), FieldDefinitionNode.class);
        for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
            PsiElement fieldNameIdentifier = fieldDefinitionNode.getNameIdentifier();
            if (fieldNameIdentifier == null) {
                continue;
            }
            TypeNameNode typeName = PsiTreeUtil.findChildOfType(fieldDefinitionNode, TypeNameNode.class);
            if (typeName == null) {
                continue;
            }
            addFieldAsLookup(resultSet, fieldNameIdentifier, structDefinitionNode, typeName, withInsertHandler,
                    withAutoCompletion);
        }
    }

    private void suggestArrayLength(@NotNull CompletionResultSet resultSet,
                                    @NotNull PsiElement definitionNode,
                                    @NotNull PsiElement prevElement) {
        boolean canSuggestArrayLength = BallerinaPsiImplUtil.canSuggestArrayLength(definitionNode,
                prevElement);
        if (canSuggestArrayLength) {
            addArrayLengthAsLookup(resultSet);
        }
    }

    @Nullable
    private PsiElement resolveStructReferenceToDefinition(@NotNull CompletionParameters parameters,
                                                          @Nullable PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();
        if (prevElement == null) {
            prevElement = originalFile.findElementAt(parameters.getOffset() - 2);
        }
        if (prevElement == null) {
            return null;
        }
        PsiReference prevElementReference = prevElement.getReference();
        if (prevElementReference == null) {
            return null;
        }
        PsiElement resolvedVariableDefElement = prevElementReference.resolve();
        if (resolvedVariableDefElement == null) {
            return null;
        }
        return resolvedVariableDefElement;
    }


    private void suggestElementsFromAPackage(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet, PsiElement packageElement,
                                             boolean withFunctions, boolean withConnectors, boolean withStructs,
                                             boolean withConstants) {
        PsiFile originalFile = parameters.getOriginalFile();
        // Get all imported packages in current file
        List<PsiElement> packages = BallerinaPsiImplUtil.getImportedPackages(originalFile);
        for (PsiElement pack : packages) {
            if (!(pack instanceof PackageNameNode)) {
                continue;
            }
            // Compare text to identify the correct package
            if (packageElement.getText().equals(pack.getText())) {
                // Resolve the package and get all matching directories. But all imported packages should be
                // unique. So the maximum size of this should be 1. If this is 0, that means the package is
                // not imported. If this is more than 1, it means there are duplicate package imports or
                // there are multiple packages with the same name.
                //                PsiDirectory[] psiDirectories =
                //                        BallerinaPsiImplUtil.resolveDirectory(((PackageNameNode) pack)
                // .getNameIdentifier());
                //                if (psiDirectories.length == 1) {
                //                    if (withFunctions) {
                //                        List<PsiElement> functions =
                //                                BallerinaPsiImplUtil.getAllFunctionsFromPackage(psiDirectories[0]);
                //                        addFunctionsAsLookups(resultSet, functions);
                //                    }
                //                    if (withConnectors) {
                //                        List<PsiElement> connectors =
                //                                BallerinaPsiImplUtil.getAllConnectorsInPackage(psiDirectories[0]);
                //                        addConnectorsAsLookups(resultSet, connectors);
                //                    }
                //                    if (withStructs) {
                //                        List<PsiElement> structs =
                //                                BallerinaPsiImplUtil.getAllStructsFromPackage(psiDirectories[0]);
                //                        addStructsAsLookups(resultSet, structs);
                //                    }
                //                    if (withConstants) {
                //                        List<PsiElement> constants =
                //                                BallerinaPsiImplUtil.getAllConstantsFromPackage(psiDirectories[0]);
                //                        addConstantsAsLookups(resultSet, constants);
                //                    }
                //
                //                    List<PsiElement> globalVariables =
                //                            BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(psiDirectories[0]);
                //                    addGlobalVariablesAsLookups(resultSet, globalVariables);
                //                }
                // Else situation cannot/should not happen since all the imported packages are unique.
                // This should be highlighted using an annotator.
            }
        }
    }

    private void handleActionInvocationNode(@NotNull CompletionParameters parameters,
                                            @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement nameIdentifier;

        // Get the NameReferenceNode.
        NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(parent, NameReferenceNode.class);
        if (nameReferenceNode == null) {
            // Eg: Twitter.<caret>
            PsiElement prevSibling = parent.getPrevSibling();
            if (prevSibling == null || !prevSibling.getText().isEmpty()) {
                return;
            }
            PsiElement prevPrevSibling = prevSibling.getPrevSibling();
            if (prevPrevSibling == null) {
                return;
            }
            PsiElement previousNonEmptyElement = getPreviousNonEmptyElement(parameters.getOriginalFile(),
                    prevSibling.getTextOffset());

            if (previousNonEmptyElement == null) {
                return;
            }
            if (!(previousNonEmptyElement instanceof LeafPsiElement)) {
                return;
            }
            IElementType elementType = ((LeafPsiElement) previousNonEmptyElement).getElementType();
            if (elementType != BallerinaTypes.DOT) {
                return;
            }
            nameIdentifier = getPreviousNonEmptyElement(parameters.getOriginalFile(),
                    ((LeafPsiElement) previousNonEmptyElement).getStartOffset());
        } else {
            // Eg: http:ClientConnector.<caret>
            nameIdentifier = nameReferenceNode.getNameIdentifier();
        }
        // Check whether the nameIdentifier is null.
        if (nameIdentifier == null) {
            return;
        }
        // Get the reference.
        PsiReference reference = nameIdentifier.getReference();
        if (reference == null) {
            return;
        }
        // Multi resolve the reference.
        //        ResolveResult[] resolvedElement;
        //        if (reference instanceof NameReference) {
        //            resolvedElement = ((NameReference) reference).multiResolve(false);
        //        } else if (reference instanceof StatementReference) {
        //            resolvedElement = ((StatementReference) reference).multiResolve(false);
        //        } else {
        //            return;
        //        }
        //
        //        // For each resolve result, get all actions and add them as lookup elements.
        //        for (ResolveResult resolveResult : resolvedElement) {
        //            PsiElement resolveResultElement = resolveResult.getElement();
        //            if (resolveResultElement == null) {
        //                continue;
        //            }
        //            // Resolved element will be an identifier. So we need to get the parent which is a connector or
        //            // native connector.
        //            List<PsiElement> allActions =
        //                    BallerinaPsiImplUtil.getAllActionsFromAConnector(resolveResultElement.getParent());
        //            if (!allActions.isEmpty()) {
        //                // Add all actions as lookup elements.
        //                addActionsAsLookups(resultSet, allActions);
        //            }
        //        }
    }

    private void handleAnnotationAttributeNode(@NotNull CompletionParameters parameters,
                                               @NotNull CompletionResultSet resultSet) {

        PsiElement element = parameters.getOriginalPosition();
        AnnotationAttachmentNode attachmentNode = PsiTreeUtil.getParentOfType(element, AnnotationAttachmentNode.class);
        if (attachmentNode == null) {
            return;
        }
        AnnotationDefinitionNode definitionNode = BallerinaPsiImplUtil.getAnnotationDefinitionNode(attachmentNode);
        if (definitionNode == null) {
            return;
        }

        List<FieldDefinitionNode> annotationFields = BallerinaPsiImplUtil.getAnnotationFields(definitionNode);
        for (FieldDefinitionNode annotationField : annotationFields) {
            PsiElement fieldNameIdentifier = annotationField.getNameIdentifier();
            if (fieldNameIdentifier == null) {
                continue;
            }
            TypeNameNode typeName = PsiTreeUtil.findChildOfType(annotationField, TypeNameNode.class);
            if (typeName == null) {
                continue;
            }
            addFieldAsLookup(resultSet, annotationField.getNameIdentifier(), definitionNode.getNameIdentifier(),
                    typeName, true, true);
        }
    }

    /**
     * If the current caret position has a PsiErrorElement, this method will be called.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    private void handlePsiErrorElement(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement parentPrevSibling = parent.getPrevSibling();
        PsiElement superParent = parent.getParent();
        if (superParent instanceof ExpressionNode || superParent instanceof AnnotationAttributeValueNode
                || superParent instanceof SimpleLiteralNode || superParent instanceof ConstantDefinitionNode) {
            return;
        }
        if (superParent instanceof ResourceDefinitionNode || superParent instanceof ServiceBodyNode) {
            addServiceSpecificKeywords(resultSet);
            return;
        }
        if (superParent instanceof ActionDefinitionNode || superParent instanceof ConnectorDefinitionNode) {
            addConnectorSpecificKeywords(resultSet);
            return;
        }
        if (superParent instanceof StatementNode || superParent instanceof ForkJoinStatementNode
                || superParent instanceof TransformStatementNode) {
            handleStatementNode(parameters, resultSet);
        } else if (superParent instanceof CompilationUnitNode) {
            // This can be called depending on the caret location.
            if (parentPrevSibling == null) {
                // If there are no previous siblings, show keywords including 'package' keyword.
                addFileLevelKeywordsAsLookups(resultSet, true, true);
            } else {
                // If there are previous siblings, get the non whitespace sibling.
                PsiElement nonWhitespaceElement = getPreviousNonEmptyElement(originalFile, parent.getTextOffset());
                if (nonWhitespaceElement != null) {
                    // if the nonWhitespaceElement is ImportDeclarationNode or PackageDeclarationNode, we don't
                    // need to add 'package' keyword.
                    if (nonWhitespaceElement.getParent() instanceof ImportDeclarationNode ||
                            nonWhitespaceElement.getParent() instanceof PackageDeclarationNode) {
                        addFileLevelKeywordsAsLookups(resultSet, false, true);
                    } else {
                        // otherwise that means that the caret is located after some definition. So no need to
                        // suggest 'import' keyword.
                        addFileLevelKeywordsAsLookups(resultSet, false, false);
                    }
                } else {
                    // If the nonWhitespaceElement is null, that means we are the beginning of a new node after
                    // one or more nodes.
                    addFileLevelKeywordsAsLookups(resultSet, false, true);
                }
            }
        } else if (superParent instanceof AnnotationDefinitionNode) {
            addAttachKeyword(resultSet);
        } else if (superParent instanceof AttachmentPointNode) {
            addAttachmentPointsAsLookups(resultSet);
        } else if (superParent instanceof VariableReferenceNode) {
            // Eg: int a = 10; int b = a <caret>+ a;
            handleVariableReferenceNode(parameters, resultSet);
        } else if (superParent instanceof ValueTypeNameNode) {
            addValueTypesAsLookups(resultSet);
        } else if (superParent instanceof AnnotationAttachmentNode) {
            addValueKeywords(resultSet);
        } else {
            // Handle all other situations.
            if (parentPrevSibling == null) {
                addFileLevelKeywordsAsLookups(resultSet, true, true);
            } else {
                addFileLevelKeywordsAsLookups(resultSet, false, true);
            }
        }
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return typeChar == ':' || typeChar == '@';
    }
}
