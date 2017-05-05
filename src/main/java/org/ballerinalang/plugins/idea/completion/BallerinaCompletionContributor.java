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

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeValueNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorInitExpressionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationStatementNode;
import org.ballerinalang.plugins.idea.psi.MapStructLiteralNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.ballerinalang.plugins.idea.psi.SimpleLiteralNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils.*;

public class BallerinaCompletionContributor extends CompletionContributor implements DumbAware {

    // File level keywords
    private static final LookupElementBuilder PACKAGE;
    private static final LookupElementBuilder IMPORT;
    private static final LookupElementBuilder CONST;
    private static final LookupElementBuilder SERVICE;
    private static final LookupElementBuilder RESOURCE;
    private static final LookupElementBuilder FUNCTION;
    private static final LookupElementBuilder CONNECTOR;
    private static final LookupElementBuilder ACTION;
    private static final LookupElementBuilder STRUCT;
    private static final LookupElementBuilder TYPEMAPPER;
    private static final LookupElementBuilder ANNOTATION;
    private static final LookupElementBuilder ATTACH;
    private static final LookupElementBuilder PARAMETER;

    // Simple types
    private static final LookupElementBuilder BOOLEAN;
    private static final LookupElementBuilder INT;
    private static final LookupElementBuilder FLOAT;
    private static final LookupElementBuilder STRING;

    // Reference types
    private static final LookupElementBuilder MESSAGE;
    private static final LookupElementBuilder XML;
    private static final LookupElementBuilder JSON;
    private static final LookupElementBuilder EXCEPTION;
    private static final LookupElementBuilder MAP;
    private static final LookupElementBuilder DATATABLE;

    // Other keywords
    private static final LookupElementBuilder REPLY;
    private static final LookupElementBuilder RETURN;
    private static final LookupElementBuilder IF;
    private static final LookupElementBuilder ELSE;
    private static final LookupElementBuilder CREATE;

    static {
        PACKAGE = createKeywordLookupElement("package", AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        IMPORT = createKeywordLookupElement("import", AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        CONST = createKeywordLookupElement("const", AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        SERVICE = createKeywordLookupElement("service", AddSpaceInsertHandler.INSTANCE);
        RESOURCE = createKeywordLookupElement("resource", AddSpaceInsertHandler.INSTANCE);
        FUNCTION = createKeywordLookupElement("function", AddSpaceInsertHandler.INSTANCE);
        CONNECTOR = createKeywordLookupElement("connector", AddSpaceInsertHandler.INSTANCE);
        ACTION = createKeywordLookupElement("action", AddSpaceInsertHandler.INSTANCE);
        STRUCT = createKeywordLookupElement("struct", AddSpaceInsertHandler.INSTANCE);
        TYPEMAPPER = createKeywordLookupElement("typemapper", AddSpaceInsertHandler.INSTANCE);
        ANNOTATION = createKeywordLookupElement("annotation", AddSpaceInsertHandler.INSTANCE);
        ATTACH = createKeywordLookupElement("attach", AddSpaceInsertHandler.INSTANCE);
        PARAMETER = createKeywordLookupElement("parameter", AddSpaceInsertHandler.INSTANCE);

        BOOLEAN = createSimpleTypeLookupElement("boolean", AddSpaceInsertHandler.INSTANCE);
        INT = createSimpleTypeLookupElement("int", AddSpaceInsertHandler.INSTANCE);
        FLOAT = createSimpleTypeLookupElement("float", AddSpaceInsertHandler.INSTANCE);
        STRING = createSimpleTypeLookupElement("string", AddSpaceInsertHandler.INSTANCE);

        MESSAGE = createReferenceTypeLookupElement("message", AddSpaceInsertHandler.INSTANCE);
        XML = createReferenceTypeLookupElement("xml", AddSpaceInsertHandler.INSTANCE);
        JSON = createReferenceTypeLookupElement("json", AddSpaceInsertHandler.INSTANCE);
        EXCEPTION = createReferenceTypeLookupElement("exception", AddSpaceInsertHandler.INSTANCE);
        MAP = createReferenceTypeLookupElement("map", AddSpaceInsertHandler.INSTANCE);
        DATATABLE = createReferenceTypeLookupElement("datatable", AddSpaceInsertHandler.INSTANCE);

        REPLY = createKeywordLookupElement("reply", AddSpaceInsertHandler.INSTANCE);
        RETURN = createKeywordLookupElement("return", AddSpaceInsertHandler.INSTANCE);
        IF = createKeywordLookupElement("if", ParenthesisInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        ELSE = createKeywordLookupElement("else", AddSpaceInsertHandler.INSTANCE);
        CREATE = createKeywordLookupElement("create", AddSpaceInsertHandler.INSTANCE);
    }

    public BallerinaCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {
                        // The file will be loaded to memory and and will be edited. parameters.getOriginalFile()
                        // contains the original file. parameters.getPosition().getContainingFile() will return null
                        // because it only exists in the memory. So use parameters.getOriginalFile().getContainingFile()
                        // if you want to get the details like containing directory, etc.
                        addSuggestions(parameters, resultSet);
                    }
                }
        );
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
        PsiElement parentPrevSibling = parent.getPrevSibling();

        // If the parent is a simple literal value node, no need to add lookup elements.
        if (parent instanceof SimpleLiteralNode) {
            return;
        }

        if (parent instanceof PsiFile) {
            // If the parent is PsiFile, that means we can only suggest keywords including 'package' and 'import'
            // keywords.
            addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
        } else if (parentPrevSibling instanceof ImportDeclarationNode
                || parentPrevSibling instanceof PackageDeclarationNode) {
            // If the previous sibling of the parent is PackageDeclarationNode, that means we have already added
            // package declaration. If it is ImportDeclarationNode, no need to suggest 'package' keyword because we
            // cannot add package declaration after an import.
            addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
        } else if (parent instanceof PackageNameNode) {
            handlePackageNameNode(parameters, resultSet);
        } else if (parent instanceof ImportDeclarationNode) {
            handleImportDeclarationNode(parameters, resultSet);
        } else if (parent instanceof ConstantDefinitionNode) {
            handleConstantDefinitionNode(parameters, resultSet);
        } else if (parent instanceof TypeNameNode || parent instanceof NameReferenceNode
                || parent instanceof ActionDefinitionNode) {
            handleNameReferenceNode(parameters, resultSet);
        } else if (parent instanceof StatementNode) {
            handleStatementNode(parameters, resultSet);
        } else if (parent instanceof VariableReferenceNode) {
            handleVariableReferenceNode(parameters, resultSet);
        } else if (parent instanceof ActionInvocationNode) {
            handleActionInvocationNode(parameters, resultSet);
        } else if (parent instanceof FunctionInvocationStatementNode) {
            handleFunctionInvocationStatementNode(parameters, resultSet);
        } else if (parent instanceof PsiErrorElement) {
            handlePsiErrorElement(parameters, resultSet);
        } else if (parent instanceof VariableDefinitionNode) {
            identifyAndAddSuggestions(parameters, resultSet);
        } else if (parent instanceof ConnectorInitExpressionNode) {
            //            identifyAndAddSuggestions(parameters, resultSet);
        } else {
            // If we are currently at an identifier node or a comment node, no need to suggest.
            if (element instanceof IdentifierPSINode || element instanceof PsiComment) {
                return;
            }
            if (parentPrevSibling == null) {
                addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
            } else {
                addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
            }
        }
    }

    /**
     * Adds file level keywords as lookup elements.
     *
     * @param resultSet   result list which is used to add lookups
     * @param priority    priority of the lookup elements
     * @param withPackage indicates whether to add 'package' keyword
     * @param withImport  indicates whether to add 'import' keyword
     */
    private void addFileLevelKeywordsAsLookups(@NotNull CompletionResultSet resultSet, int priority,
                                               boolean withPackage, boolean withImport) {
        if (withPackage) {
            resultSet.addElement(PrioritizedLookupElement.withPriority(PACKAGE, priority));
        }
        if (withImport) {
            resultSet.addElement(PrioritizedLookupElement.withPriority(IMPORT, priority));
        }
        resultSet.addElement(PrioritizedLookupElement.withPriority(CONST, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(SERVICE, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(FUNCTION, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(CONNECTOR, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(STRUCT, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(TYPEMAPPER, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(ANNOTATION, priority));
    }

    /**
     * Add lookups for package declarations.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    private void handlePackageNameNode(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement superParent = parent.getParent();
        // Check whether we are in a package declaration node
        if (superParent.getParent() instanceof PackageDeclarationNode) {
            addPackageSuggestions(resultSet, element);
        } else if (superParent.getParent() instanceof ImportDeclarationNode &&
                !(superParent instanceof AliasNode)) {
            // If the parent is not an AliasNode and is inside the ImportDeclarationNode, we need to suggest packages.
            addImportSuggestions(resultSet, element);
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
            addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
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
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();

        ParameterNode parameterNode = PsiTreeUtil.getParentOfType(parent, ParameterNode.class);
        if (parameterNode != null) {
            checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                    this::handleIdentifierInParameter,
                    this::handleLeafElementInParameter, this::handleOtherTypesInParameter);
            return;
        }

        ServiceBodyNode serviceBodyNode = PsiTreeUtil.getParentOfType(parent, ServiceBodyNode.class);
        if (serviceBodyNode != null) {

            ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(parent,
                    ResourceDefinitionNode.class);
            if (resourceDefinitionNode == null) {
                AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(parent,
                        AnnotationAttachmentNode.class);
                if (annotationAttachmentNode == null) {
                    addKeywordAsLookup(resultSet, RESOURCE, CONTEXT_KEYWORD_PRIORITY);
                    return;
                }
            }
        }

        ConnectorBodyNode connectorBodyNode = PsiTreeUtil.getParentOfType(parent, ConnectorBodyNode.class);
        if (connectorBodyNode != null) {

            ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(parent,
                    ActionDefinitionNode.class);
            if (actionDefinitionNode == null) {
                AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(parent,
                        AnnotationAttachmentNode.class);
                if (annotationAttachmentNode == null) {

                    PsiElement prevSibling = getPreviousNonEmptyElement(originalFile, element.getTextOffset());

                    if ((prevSibling instanceof LeafPsiElement)) {

                        IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                        if (elementType == BallerinaTypes.AT) {
                            addAllImportedPackagesAsLookups(resultSet, originalFile);
                            checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                                    this::handleIdentifierInNameReference,
                                    this::handleLeafElementInNameReference,
                                    null);
                        } else if (elementType == BallerinaTypes.COLON) {
                            String type = getAttachmentType(parameters);
                            if (type == null) {
                                return;
                            }
                            PsiElement packageNode = originalFile.findElementAt(prevSibling.getTextOffset() - 2);
                            suggestAnnotationsFromAPackage(parameters, resultSet, packageNode, type);
                        }
                    } else {
                        addKeywordAsLookup(resultSet, ACTION, CONTEXT_KEYWORD_PRIORITY);
                    }
                    return;
                }
            }
        }

        PsiElement superParent = parent.getParent();
        if (superParent instanceof AnnotationAttachmentNode) {
            if (handleAnnotationAttachmentNode(parameters, resultSet)) {
                return;
            }
        }

        checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(), this::handleIdentifierInNameReference,
                this::handleLeafElementInNameReference, null);
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
                        addKeywordAsLookup(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
                        addFunctionsAsLookups(resultSet, element, originalFile);
                        addConnectorsAsLookups(resultSet, element, originalFile);
                        addStructsAsLookups(resultSet, originalFile);
                        addAllImportedPackagesAsLookups(resultSet, originalFile);
                        addVariablesAsLookups(resultSet, element);
                        addConstantsAsLookups(resultSet, originalFile);
                    } else if (elementType == BallerinaTypes.COLON) {
                        // This will be called instead of handle statement node if there are codes after the caret.
                        // Eg: function test(){ test:te<caret> \n some_other_codes}
                        PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                        suggestElementsFromAPackage(parameters, resultSet, packageNode, prevElement, true, true,
                                true, true);
                    } else if (elementType == BallerinaTypes.DOT) {
                        // Eg: person.n<caret>
                        PsiElement structReference = originalFile.findElementAt(token.getTextOffset() - 2);
                        addStructFields(parameters, resultSet, structReference, null, false, false);
                    } else if (elementType == BallerinaTypes.AT) {
                        // Eg: annotation TEST attach action {} connector C(){ @T<caret> action A()(message) {} }
                        String attachmentType = getAttachmentType(parameters);
                        if (attachmentType != null) {
                            suggestAnnotationsFromAPackage(parameters, resultSet, null, attachmentType);
                        }
                    } else {
                        // Eg: function test(){t<caret>}
                        addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
                        addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);

                        addFunctionsAsLookups(resultSet, element, originalFile);
                        addConnectorsAsLookups(resultSet, element, originalFile);
                        addStructsAsLookups(resultSet, originalFile);
                        addAllImportedPackagesAsLookups(resultSet, originalFile);
                        addVariablesAsLookups(resultSet, element);
                        addConstantsAsLookups(resultSet, originalFile);
                    }
                },
                (p, r, prev) -> {
                    addFunctionsAsLookups(resultSet, element, originalFile);
                    addConnectorsAsLookups(resultSet, element, originalFile);
                    addStructsAsLookups(resultSet, originalFile);
                    addAllImportedPackagesAsLookups(resultSet, originalFile);
                    addVariablesAsLookups(resultSet, element);
                    addConstantsAsLookups(resultSet, originalFile);
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
            addKeywordAsLookup(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);

            addFunctionsAsLookups(resultSet, element, originalFile);
            addConnectorsAsLookups(resultSet, element, originalFile);
            addStructsAsLookups(resultSet, originalFile);
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            addVariablesAsLookups(resultSet, element);
            addConstantsAsLookups(resultSet, originalFile);
        } else if (elementType == BallerinaTypes.COLON) {
            // Eg: function test(){ test:<caret> }
            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            suggestElementsFromAPackage(parameters, resultSet, packageNode, null, true,
                    true, true, false);
        } else if (elementType == BallerinaTypes.DOT) {
            // Eg: person.<caret>
            PsiElement structReference = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            addStructFields(parameters, resultSet, structReference, null, false, false);
        } else if (elementType == BallerinaTypes.CREATE) {
            // Eg: test:Connector c = create <caret>
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            addConnectorsAsLookups(resultSet, element, originalFile);
            addFunctionsAsLookups(resultSet, element, originalFile);
        } else if (elementType == BallerinaTypes.AT) {
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            String attachmentType = getAttachmentType(parameters);
            if (attachmentType != null) {
                suggestAnnotationsFromAPackage(parameters, resultSet, null, attachmentType);
            }
        } else {
            // Eg: function test(){ <caret> \n OTHER_CODES }
            addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
            addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);

            addFunctionsAsLookups(resultSet, element, originalFile);
            addConnectorsAsLookups(resultSet, element, originalFile);
            addStructsAsLookups(resultSet, originalFile);
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            addVariablesAsLookups(resultSet, element);
            addConstantsAsLookups(resultSet, originalFile);
        }
    }

    private boolean handleAnnotationAttachmentNode(@NotNull CompletionParameters parameters,
                                                   @NotNull CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();

        final String attachmentType = getAttachmentType(parameters);
        if (attachmentType == null) {
            return false;
        }
        checkPrevNodeAndHandle(parameters, resultSet, parameters.getOffset(),
                (p, r, prev) -> {
                    PsiElement token = getPreviousNonEmptyElement(originalFile, prev.getTextOffset());
                    if (token != null && token instanceof LeafPsiElement) {
                        IElementType elementType = ((LeafPsiElement) token).getElementType();
                        if (elementType == BallerinaTypes.COLON) {
                            PsiElement packageNode = originalFile.findElementAt(prev.getTextOffset() - 2);
                            suggestAnnotationsFromAPackage(parameters, resultSet, packageNode, attachmentType);
                        } else {
                            addAllImportedPackagesAsLookups(resultSet, originalFile);
                            suggestAnnotationsFromAPackage(parameters, resultSet, null, attachmentType);
                        }
                    } else {
                        PsiElement packageNode = originalFile.findElementAt(prev.getTextOffset() - 2);
                        suggestAnnotationsFromAPackage(parameters, resultSet, packageNode, attachmentType);
                    }
                },
                (p, r, prev) -> {
                    IElementType elementType = ((LeafPsiElement) prev).getElementType();
                    // Cannot use a switch statement since the types are not constants and declaring them final does
                    // not fix the issue as well.
                    if (elementType == BallerinaTypes.COLON) {
                        PsiElement packageNode = originalFile.findElementAt(prev.getTextOffset() - 2);
                        suggestAnnotationsFromAPackage(parameters, resultSet, packageNode, attachmentType);
                    } else if (elementType == BallerinaTypes.AT) {
                        addAllImportedPackagesAsLookups(resultSet, originalFile);
                        suggestAnnotationsFromAPackage(parameters, resultSet, null, attachmentType);
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
        } else if (nextSibling.getParent() instanceof ActionDefinitionNode || parent instanceof ActionDefinitionNode) {
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
                        suggestAnnotationsFromAPackage(parameters, resultSet, packageNode, attachmentType);
                    }
                } else {
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, prevElement, false, true, true,
                            false);
                }
            } else {
                // Eg: function test(s<caret>){ }
                addAllImportedPackagesAsLookups(resultSet, originalFile);
                addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addStructsAsLookups(resultSet, originalFile);
                addConnectorsAsLookups(resultSet, originalFile);
            }
            if (elementType == BallerinaTypes.LBRACE || elementType == BallerinaTypes.SEMI) {
                addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addStructsAsLookups(resultSet, originalFile);
            }
        } else {
            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            suggestElementsFromAPackage(parameters, resultSet, packageNode, prevElement, false, true, true, false);
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
                    suggestAnnotationsFromAPackage(parameters, resultSet, packageNode, attachmentType);
                }
            } else {
                suggestElementsFromAPackage(parameters, resultSet, packageNode, null, false, true, true, false);
            }
        } else if (elementType == BallerinaTypes.LPAREN || elementType == BallerinaTypes.COMMA) {
            // Eg: function test(<caret>) { }
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
            addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
            addStructsAsLookups(resultSet, originalFile);
            addConnectorsAsLookups(resultSet, originalFile);
        } else if (elementType == BallerinaTypes.AT) {
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            String attachmentType = getAttachmentType(parameters);
            if (attachmentType == null) {
                return;
            }
            suggestAnnotationsFromAPackage(parameters, resultSet, null, attachmentType);
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

        addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
        addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
        addKeywordAsLookup(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
        addOtherCommonKeywords(resultSet);

        addFunctionSpecificKeywords(parameters, resultSet, originalFile);
        addResourceSpecificKeywords(parameters, resultSet, originalFile);
        addActionSpecificKeywords(parameters, resultSet, originalFile);

        addVariablesAsLookups(resultSet, prevElement);
        addConstantsAsLookups(resultSet, originalFile);
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
            suggestElementsFromAPackage(parameters, resultSet, packageName, null, true, true, true, true);
        }
    }

    private void handleVariableReferenceNode(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet) {

        PsiFile originalFile = parameters.getOriginalFile();

        PsiElement element = parameters.getPosition();
        // If we are in the {} of a struct variable reference node, we need to show fields.
        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(element,
                VariableDefinitionNode.class);
        if (variableDefinitionNode != null) {
            ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(element, ExpressionNode.class);
            if (expressionNode == null) {

                addStructFields(parameters, resultSet, null, variableDefinitionNode, true, true);
                return;
            }
        }

        // Todo - Get all variables in the scope
        // Todo - Get all constant variables
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
                            addVariablesAsLookups(resultSet, prev);
                            addFunctionsAsLookups(resultSet, originalFile);
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
                                addVariablesAsLookups(resultSet, element);
                                addConstantsAsLookups(resultSet, originalFile);
                                addFunctionsAsLookups(resultSet, element, originalFile);
                                addAllImportedPackagesAsLookups(resultSet, originalFile);
                                addStructsAsLookups(resultSet, originalFile);
                            }
                        }
                    } else {
                        addVariablesAsLookups(resultSet, element);
                        addConstantsAsLookups(resultSet, originalFile);
                        addFunctionsAsLookups(resultSet, element, originalFile);
                        addAllImportedPackagesAsLookups(resultSet, originalFile);
                    }
                },
                (p, r, prev) -> {

                    IElementType elementType = ((LeafPsiElement) prev).getElementType();
                    if (elementType == BallerinaTypes.EQUAL) {
                        //                        addKeywordAsLookup(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
                    } else if (elementType == BallerinaTypes.DOT) {
                        // Eg: system:println(person.<caret>)
                        PsiElement structReferenceNode = getPreviousNonEmptyElement(originalFile, prev.getTextOffset());
                        addStructFields(parameters, resultSet, structReferenceNode, null, false, false);
                    } else if (elementType == BallerinaTypes.COLON) {
                        // Eg: Person p = {name:<caret>}
                        addVariablesAsLookups(resultSet, prev);
                        addFunctionsAsLookups(resultSet, originalFile);
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
                            addVariablesAsLookups(resultSet, element);
                            addConstantsAsLookups(resultSet, originalFile);
                            addFunctionsAsLookups(resultSet, element, originalFile);
                            addAllImportedPackagesAsLookups(resultSet, originalFile);
                        }
                    }
                }, null);

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
        // Eg: http:get
        if (token != null && token instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) token).getElementType();
            if (elementType == BallerinaTypes.COLON) {
                // This will be called if there are no lines after the caret. If there are codes after the caret, it
                // will be identified as reference node.
                // Eg: function test(){ test:<caret> }
                PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                suggestElementsFromAPackage(parameters, resultSet, packageNode, prevElement, true, true,
                        true, true);
            } else if (elementType == BallerinaTypes.DOT) {
                PsiElement element = getPreviousNonEmptyElement(originalFile, token.getTextOffset());
                addStructFields(parameters, resultSet, element, null, true, true);
            } else if (elementType == BallerinaTypes.ASSIGN) {
                PsiElement currentElement = originalFile.findElementAt(parameters.getOffset());
                addFunctionsAsLookups(resultSet, prevElement, originalFile);
                addAllImportedPackagesAsLookups(resultSet, originalFile);
                addVariablesAsLookups(resultSet, currentElement);
                addConstantsAsLookups(resultSet, originalFile);
                addStructsAsLookups(resultSet, originalFile);
                addKeywordAsLookup(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
            } else {
                PsiElement currentElement = originalFile.findElementAt(parameters.getOffset());
                addFunctionsAsLookups(resultSet, prevElement, originalFile);
                addAllImportedPackagesAsLookups(resultSet, originalFile);
                addVariablesAsLookups(resultSet, currentElement);
                addConstantsAsLookups(resultSet, originalFile);
                addStructsAsLookups(resultSet, originalFile);

                addFunctionSpecificKeywords(parameters, resultSet, originalFile);
                addResourceSpecificKeywords(parameters, resultSet, originalFile);
                addActionSpecificKeywords(parameters, resultSet, originalFile);

                addOtherCommonKeywords(resultSet);
            }

            if (elementType == BallerinaTypes.LBRACE || elementType == BallerinaTypes.SEMI) {
                addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
            }
        } else {
            PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
            suggestElementsFromAPackage(parameters, resultSet, packageNode, prevElement, true, true, true,
                    true);
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
            suggestElementsFromAPackage(parameters, resultSet, packageNode, null, true, true, true, true);
        } else if (isExpressionSeparator(elementType)) {
            addFunctionsAsLookups(resultSet, prevElement, originalFile);
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            addVariablesAsLookups(resultSet, element);
            addConstantsAsLookups(resultSet, originalFile);

            if (elementType == BallerinaTypes.ASSIGN) {
                addKeywordAsLookup(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
            }
        } else if (elementType == BallerinaTypes.DOT) {

            element = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset());
            if (!(element instanceof IdentifierPSINode)) {
                element = getPreviousNonEmptyElement(originalFile, prevElement.getTextOffset() - 1);
            }
            addStructFields(parameters, resultSet, element, null, false, false);
        } else {
            PsiElement currentElement = originalFile.findElementAt(parameters.getOffset());
            addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
            addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);

            addFunctionsAsLookups(resultSet, prevElement, originalFile);
            addAllImportedPackagesAsLookups(resultSet, originalFile);
            addVariablesAsLookups(resultSet, currentElement);
            addConstantsAsLookups(resultSet, originalFile);
            addStructsAsLookups(resultSet, originalFile);

            addFunctionSpecificKeywords(parameters, resultSet, originalFile);
            addResourceSpecificKeywords(parameters, resultSet, originalFile);
            addActionSpecificKeywords(parameters, resultSet, originalFile);

            addOtherCommonKeywords(resultSet);
        }
    }


    private void handleOtherTypesInStatement(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet, @NotNull PsiElement prevElement) {
        PsiFile originalFile = parameters.getOriginalFile();

        addValueTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
        addReferenceTypesAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY);
        addKeywordAsLookup(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
        addOtherCommonKeywords(resultSet);

        addFunctionSpecificKeywords(parameters, resultSet, originalFile);
        addResourceSpecificKeywords(parameters, resultSet, originalFile);
        addActionSpecificKeywords(parameters, resultSet, originalFile);

        addFunctionsAsLookups(resultSet, prevElement, originalFile);
        addAllImportedPackagesAsLookups(resultSet, originalFile);
        addVariablesAsLookups(resultSet, prevElement);
        addConstantsAsLookups(resultSet, originalFile);
    }

    /**
     * Adds function specific keywords like <b>return</b> as lookup elements.
     *
     * @param parameters   parameters which contain details of completion invocation
     * @param resultSet    result list which is used to add lookups
     * @param originalFile original file which we are editing
     */
    private void addFunctionSpecificKeywords(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet,
                                             @NotNull PsiFile originalFile) {
        PsiElement element = originalFile.findElementAt(parameters.getOffset());
        FunctionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionNode.class);
        if (functionNode != null) {
            addKeywordAsLookup(resultSet, RETURN, CONTEXT_KEYWORD_PRIORITY);
        }
    }

    /**
     * Adds function specific keywords like <b>return</b> as lookup elements.
     *
     * @param parameters   parameters which contain details of completion invocation
     * @param resultSet    result list which is used to add lookups
     * @param originalFile original file which we are editing
     */
    private void addActionSpecificKeywords(@NotNull CompletionParameters parameters,
                                           @NotNull CompletionResultSet resultSet,
                                           @NotNull PsiFile originalFile) {
        PsiElement element = originalFile.findElementAt(parameters.getOffset());
        ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(element, ActionDefinitionNode.class);
        if (actionDefinitionNode != null) {
            addKeywordAsLookup(resultSet, RETURN, CONTEXT_KEYWORD_PRIORITY);
        }
    }

    /**
     * Adds action specific keywords like <b>reply</b> as lookup elements.
     *
     * @param parameters   parameters which contain details of completion invocation
     * @param resultSet    result list which is used to add lookups
     * @param originalFile original file which we are editing
     */
    private void addResourceSpecificKeywords(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet,
                                             @NotNull PsiFile originalFile) {
        PsiElement element = originalFile.findElementAt(parameters.getOffset());
        ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(element,
                ResourceDefinitionNode.class);
        if (resourceDefinitionNode != null) {
            addKeywordAsLookup(resultSet, REPLY, CONTEXT_KEYWORD_PRIORITY);
        }
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
                                 boolean autoComplete) {
        if (resolvedVariableDefElement == null) {
            resolvedVariableDefElement = resolveStructReferenceToDefinition(parameters, prevElement);
            if (resolvedVariableDefElement == null) {
                return;
            }

            if (!(resolvedVariableDefElement instanceof VariableDefinitionNode)) {
                resolvedVariableDefElement = resolvedVariableDefElement.getParent();
            }
        }

        PsiElement structDefinitionNode = resolveStruct(resolvedVariableDefElement);
        if (structDefinitionNode == null) {
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
            LookupElementBuilder builder = LookupElementBuilder.create(fieldNameIdentifier.getText())
                    .withTypeText(typeName.getText()).withIcon(BallerinaIcons.FIELD)
                    .withTailText(" -> " + structDefinitionNode.getText(), true);
            if (withInsertHandler) {
                if (autoComplete) {
                    builder = builder.withInsertHandler(PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                } else {
                    builder = builder.withInsertHandler(PackageCompletionInsertHandler.INSTANCE);
                }
            }
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, FIELD_PRIORITY));
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

    @Nullable
    private PsiElement resolveStruct(PsiElement resolvedVariableDefElement) {
        TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(resolvedVariableDefElement, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        }
        NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(typeNameNode, NameReferenceNode.class);
        if (nameReferenceNode == null) {
            return null;
        }
        PsiElement nameIdentifier = nameReferenceNode.getNameIdentifier();
        if (nameIdentifier == null) {
            return null;
        }
        PsiReference typeNameNodeReference = nameIdentifier.getReference();
        if (typeNameNodeReference == null) {
            return null;
        }
        PsiElement resolvedDefElement = typeNameNodeReference.resolve();
        if (resolvedDefElement == null) {
            return null;
        }
        if (!(resolvedDefElement.getParent() instanceof StructDefinitionNode)) {
            return null;
        }
        return resolvedDefElement;
    }

    private void suggestElementsFromAPackage(@NotNull CompletionParameters parameters,
                                             @NotNull CompletionResultSet resultSet, PsiElement packageElement,
                                             PsiElement partialIdentifier, boolean suggestFunctions,
                                             boolean suggestConnectors, boolean suggestStructs, boolean
                                                     suggestConstants) {

        PsiFile originalFile = parameters.getOriginalFile();

        // Get all imported packages in current file
        List<PsiElement> packages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(originalFile);
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
                PsiDirectory[] psiDirectories =
                        BallerinaPsiImplUtil.resolveDirectory(((PackageNameNode) pack).getNameIdentifier());

                if (psiDirectories.length == 1) {
                    // Get all functions in the package.
                    if (suggestFunctions) {
                        List<PsiElement> functions =
                                BallerinaPsiImplUtil.getAllFunctionsInPackage(psiDirectories[0]);
                        //                        if (partialIdentifier != null) {
                        //                            functions = functions.stream()
                        //                                    .filter(function -> function.getText().contains
                        // (partialIdentifier.getText()))
                        //                                    .collect(Collectors.toList());
                        //                        }
                        addFunctionsAsLookups(resultSet, functions);
                    }
                    if (suggestConnectors) {
                        List<PsiElement> connectors =
                                BallerinaPsiImplUtil.getAllConnectorsInPackage(psiDirectories[0]);
                        //                        if (partialIdentifier != null) {
                        //                            connectors = connectors.stream()
                        //                                    .filter(connector -> connector.getText().contains
                        // (partialIdentifier.getText()))
                        //                                    .collect(Collectors.toList());
                        //                        }
                        addConnectorsAsLookups(resultSet, connectors);
                    }
                    if (suggestStructs) {
                        List<PsiElement> structs =
                                BallerinaPsiImplUtil.getAllStructsInPackage(psiDirectories[0]);
                        //                        if (partialIdentifier != null) {
                        //                            structs = structs.stream()
                        //                                    .filter(struct -> struct.getText().contains
                        // (partialIdentifier.getText()))
                        //                                    .collect(Collectors.toList());
                        //                        }
                        addStructsAsLookups(resultSet, structs);
                    }
                    if (suggestConstants) {
                        List<PsiElement> constants =
                                BallerinaPsiImplUtil.getAllConstantsInPackage(psiDirectories[0]);
                        addConstantsAsLookups(resultSet, constants);
                    }
                } else {
                    // This situation cannot/should not happen since all the imported packages are unique.
                    // This should be highlighted using an annotator.
                }
            }
        }
    }

    private void suggestAnnotationsFromAPackage(@NotNull CompletionParameters parameters,
                                                @NotNull CompletionResultSet resultSet,
                                                PsiElement packageElement, String type) {
        PsiFile originalFile = parameters.getOriginalFile();
        if (packageElement != null) {
            // Get all imported packages in current file
            List<PsiElement> packages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(originalFile);
            for (PsiElement pack : packages) {
                // Compare text to identify the correct package
                if (packageElement.getText().equals(pack.getText())) {
                    // Resolve the package and get all matching directories. But all imported packages should be
                    // unique. So the maximum size of this should be 1. If this is 0, that means the package is
                    // not imported. If this is more than 1, it means there are duplicate package imports or
                    // there are multiple packages with the same name.
                    PsiDirectory[] psiDirectories =
                            BallerinaPsiImplUtil.resolveDirectory(((PackageNameNode) pack).getNameIdentifier());
                    if (psiDirectories.length == 1) {
                        // Get all annotations in the package.
                        List<PsiElement> attachmentsForType =
                                BallerinaPsiImplUtil.getAllAnnotationAttachmentsForType(psiDirectories[0], type);
                        addAnnotationsAsLookups(resultSet, attachmentsForType);
                    } else {
                        // This situation cannot/should not happen since all the imported packages are unique.
                        // This should be highlighted using an annotator.
                    }
                }
            }
        } else {
            // If the packageElement is null, that means we want to get all annotations in the current package.
            PsiDirectory containingDirectory = originalFile.getContainingDirectory();
            if (containingDirectory != null) {
                List<PsiElement> attachments =
                        BallerinaPsiImplUtil.getAllAnnotationAttachmentsForType(containingDirectory, type);
                addAnnotationsAsLookups(resultSet, attachments);
            }
        }
    }

    private void addOtherCommonKeywords(@NotNull CompletionResultSet resultSet) {
        addKeywordAsLookup(resultSet, IF, CONTEXT_KEYWORD_PRIORITY);
        addKeywordAsLookup(resultSet, ELSE, CONTEXT_KEYWORD_PRIORITY);
        // todo - add fork/join, while, etc
    }

    private void handleActionInvocationNode(@NotNull CompletionParameters parameters,
                                            @NotNull CompletionResultSet resultSet) {
        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        // Get the NameReferenceNode.
        NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(parent, NameReferenceNode.class);
        if (nameReferenceNode == null) {
            return;
        }
        // Get the TypeNameNode.
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(nameReferenceNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return;
        }
        // Get the identifier.
        PsiElement nameIdentifier = typeNameNode.getNameIdentifier();
        if (nameIdentifier == null) {
            return;
        }
        // Get the reference.
        PsiReference reference = nameIdentifier.getReference();
        if (reference == null || !(reference instanceof NameReference)) {
            return;
        }
        // Multi resolve the reference.
        ResolveResult[] resolvedElement = ((NameReference) reference).multiResolve(false);
        // For each resolve result, get all actions and add them as lookup elements.
        for (ResolveResult resolveResult : resolvedElement) {
            PsiElement resolveResultElement = resolveResult.getElement();
            if (resolveResultElement == null) {
                continue;
            }
            // Resolved element will be an identifier. So we need to get the parent which is a connector or
            // native connector.
            List<PsiElement> allActions =
                    BallerinaPsiImplUtil.getAllActionsFromAConnector(resolveResultElement.getParent());
            if (!allActions.isEmpty()) {
                // Add all actions as lookup elements.
                addActionsAsLookups(resultSet, allActions);
            }
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
        PsiElement prevSibling = element.getPrevSibling();

        //
        PsiElement superParent = parent.getParent();
        if (superParent instanceof ExpressionNode || superParent instanceof AnnotationAttributeValueNode
                || superParent instanceof AnnotationAttachmentNode || superParent instanceof SimpleLiteralNode ||
                superParent instanceof ConstantDefinitionNode) {
            return;
        }

        if (superParent instanceof ResourceDefinitionNode || superParent instanceof ServiceBodyNode) {
            addKeywordAsLookup(resultSet, RESOURCE, CONTEXT_KEYWORD_PRIORITY);
        }

        if (superParent instanceof StatementNode) {
            handleStatementNode(parameters, resultSet);
        } else if (superParent instanceof CompilationUnitNode) {
            // This can be called depending on the caret location.
            if (parentPrevSibling == null) {
                // If there are no previous siblings, show keywords including 'package' keyword.
                addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
            } else {
                // If there are previous siblings, get the non whitespace sibling.
                PsiElement nonWhitespaceElement = getPreviousNonEmptyElement(originalFile, parent.getTextOffset());
                if (nonWhitespaceElement != null) {
                    // if the nonWhitespaceElement is ImportDeclarationNode or PackageDeclarationNode, we don't
                    // need to add 'package' keyword.
                    if (nonWhitespaceElement.getParent() instanceof ImportDeclarationNode ||
                            nonWhitespaceElement.getParent() instanceof PackageDeclarationNode) {
                        addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
                    } else {
                        // otherwise that means that the caret is located after some definition. So no need to
                        // suggest 'import' keyword.
                        addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, false, false);
                    }
                } else {
                    // If the nonWhitespaceElement is null, that means we are the beginning of a new node after
                    // one or more nodes.
                    addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
                }
            }
        } else if (superParent instanceof AnnotationDefinitionNode) {
            addKeywordAsLookup(resultSet, ATTACH, CONTEXT_KEYWORD_PRIORITY);
        } else if (superParent instanceof AttachmentPointNode) {
            addKeywordAsLookup(resultSet, CONST, CONTEXT_KEYWORD_PRIORITY);
            addKeywordAsLookup(resultSet, SERVICE, CONTEXT_KEYWORD_PRIORITY);
            addKeywordAsLookup(resultSet, FUNCTION, CONTEXT_KEYWORD_PRIORITY);
            addKeywordAsLookup(resultSet, CONNECTOR, CONTEXT_KEYWORD_PRIORITY);
            addKeywordAsLookup(resultSet, STRUCT, CONTEXT_KEYWORD_PRIORITY);
            addKeywordAsLookup(resultSet, TYPEMAPPER, CONTEXT_KEYWORD_PRIORITY);
            addKeywordAsLookup(resultSet, ACTION, CONTEXT_KEYWORD_PRIORITY);
            addKeywordAsLookup(resultSet, PARAMETER, CONTEXT_KEYWORD_PRIORITY);
        } else if (superParent instanceof VariableReferenceNode) {
            // Eg: int a = 10; int b = a <caret>+ a;
            handleVariableReferenceNode(parameters, resultSet);
        } else {
            // Handle all other situations.
            if (parentPrevSibling == null) {
                addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
            } else {
                addFileLevelKeywordsAsLookups(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
            }
        }
    }

    private void addPackageSuggestions(@NotNull CompletionResultSet resultSet, PsiElement element) {
        // If we are in a package declaration, we only need to suggest the path to the current file.
        PsiDirectory[] psiDirectories = BallerinaPsiImplUtil.suggestCurrentPackagePath(element);
        for (PsiDirectory directory : psiDirectories) {
            InsertHandler<LookupElement> insertHandler;
            // If the package does have any sub packages, we need to add the insert handler with auto popup
            // enabled with will add '.' at the end of the package and show the sub packages in the popup.
            if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            } else {
                // If the package does not have sub packages, we need to add the insert handler to add ';' at
                // the end.
                insertHandler = StatementCompletionInsertHandler.INSTANCE;
            }
            // Add directories as lookup elements.
            addPackagesAsLookups(resultSet, directory, insertHandler);
        }
    }

    private void addImportSuggestions(@NotNull CompletionResultSet resultSet, PsiElement element) {
        // Suggest import packages.
        PsiDirectory[] packageDirectories = BallerinaPsiImplUtil.suggestImportPackages(element);
        // Get names of all imported packages.
        List<String> allImportedPackages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(element).stream()
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

    private void addKeywordAsLookup(@NotNull CompletionResultSet resultSet, @NotNull LookupElement lookupElement,
                                    int priority) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, priority));
    }

    /**
     * Adds variables in the resolvable scopes as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param element   element in the current caret position
     */
    private void addVariablesAsLookups(@NotNull CompletionResultSet resultSet, PsiElement element) {
        PsiElement context = element.getContext();
        if (context == null) {
            context = element.getParent().getContext();
        }
        List<PsiElement> variables = BallerinaPsiImplUtil.getAllVariablesInResolvableScope(element, context);
        for (PsiElement variable : variables) {
            LookupElementBuilder builder = LookupElementBuilder.create(variable.getText())
                    .withTypeText("Variable").withIcon(BallerinaIcons.VARIABLE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Helper method to add constants as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param constants list of constants which needs to be added
     */
    private void addConstantsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> constants) {
        for (PsiElement constant : constants) {
            LookupElementBuilder builder = LookupElementBuilder.create(constant.getText())
                    .withTypeText("Constant").withIcon(BallerinaIcons.VARIABLE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Helper method to add constants as lookup elements.
     *
     * @param resultSet    result list which is used to add lookups
     * @param originalFile original file which we are editing
     */
    private void addConstantsAsLookups(@NotNull CompletionResultSet resultSet, PsiFile originalFile) {
        PsiDirectory parentDirectory = originalFile.getParent();
        if (parentDirectory != null) {
            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsInPackage(parentDirectory);
            addConstantsAsLookups(resultSet, constants);
        }
    }

    /**
     * Helper method to add functions as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param functions list of functions which needs to be added
     */
    private void addFunctionsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> functions) {
        for (PsiElement function : functions) {
            LookupElementBuilder builder = LookupElementBuilder.create(function.getText())
                    .withTypeText("Function").withTailText("()", true).withIcon(BallerinaIcons.FUNCTION)
                    .withInsertHandler(FunctionCompletionInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, FUNCTION_PRIORITY));
        }
    }

    /**
     * Helper method to add functions as lookup elements.
     *
     * @param resultSet    result list which is used to add lookups
     * @param originalFile original file which we are editing
     */
    private void addFunctionsAsLookups(@NotNull CompletionResultSet resultSet, PsiElement element,
                                       PsiFile originalFile) {
        PsiElement parent = element.getParent();
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            addFunctionsAsLookups(resultSet, originalFile);
        } else {
            PsiElement resolvedPackage = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllFunctionsInCurrentPackage(resolvedPackage);
            addFunctionsAsLookups(resultSet, connectors);
        }
    }

    private void addFunctionsAsLookups(@NotNull CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsInCurrentPackage(originalFile);
        addFunctionsAsLookups(resultSet, functions);
    }

    /**
     * Helper method to add packages as lookup elements.
     *
     * @param resultSet    result list which is used to add lookups
     * @param originalFile file which will be used to get imported packages
     */
    private void addAllImportedPackagesAsLookups(@NotNull CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> packages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(originalFile);
        for (PsiElement pack : packages) {
            LookupElementBuilder builder = LookupElementBuilder.create(pack.getText())
                    .withTypeText("Package").withIcon(BallerinaIcons.PACKAGE)
                    .withInsertHandler(PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY));
        }
    }

    /**
     * Helper method to add packages as lookup elements.
     *
     * @param resultSet     result list which is used to add lookups
     * @param directory     directory which needs to be added as a lookup element
     * @param insertHandler insert handler of the lookup element
     */
    private void addPackagesAsLookups(@NotNull CompletionResultSet resultSet, PsiDirectory directory,
                                      InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.create(directory.getName())
                .withInsertHandler(insertHandler).withTypeText("Package").withIcon(BallerinaIcons.PACKAGE);
        resultSet.addElement(builder);
    }

    /**
     * Helper method to add structs as lookup elements.
     *
     * @param resultSet    result list which is used to add lookups
     * @param originalFile file which will be used to get structs
     */
    private void addStructsAsLookups(@NotNull CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsInCurrentPackage(originalFile);
        addStructsAsLookups(resultSet, structs);
    }

    /**
     * Helper method to add structs as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param structs   list of structs which needs to be added
     */
    private void addStructsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> structs) {
        for (PsiElement struct : structs) {
            LookupElementBuilder builder = LookupElementBuilder.create(struct.getText())
                    .withTypeText("Struct").withIcon(BallerinaIcons.STRUCT);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, STRUCT_PRIORITY));
        }
    }

    private void addConnectorsAsLookups(@NotNull CompletionResultSet resultSet, PsiElement element,
                                        PsiFile originalFile) {
        PsiElement parent = element.getParent();
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            addConnectorsAsLookups(resultSet, originalFile);
        } else {
            PsiElement resolvedPackage = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
            if (resolvedPackage != null) {
                List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInCurrentPackage(resolvedPackage);
                addConnectorsAsLookups(resultSet, connectors);
            }
        }
    }

    /**
     * Helper method to add connectors as lookup elements.
     *
     * @param resultSet    result list which is used to add lookups
     * @param originalFile original file which we are editing
     */
    private void addConnectorsAsLookups(@NotNull CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInCurrentPackage(originalFile);
        addConnectorsAsLookups(resultSet, connectors);
    }

    /**
     * Helper method to add connections as lookup elements.
     *
     * @param resultSet  result list which is used to add lookups
     * @param connectors list of connectors which needs to be added
     */
    private void addConnectorsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> connectors) {
        for (PsiElement connector : connectors) {
            LookupElementBuilder builder = LookupElementBuilder.create(connector.getText())
                    .withTypeText("Connector").withIcon(BallerinaIcons.CONNECTOR);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, CONNECTOR_PRIORITY));
        }
    }

    /**
     * Helper method to add annotations as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param element   element at caret position
     */
    private void addAnnotationsAsLookups(@NotNull CompletionResultSet resultSet, PsiElement element) {
        PsiElement parent = element.getParent();
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return;
        }
        PsiElement resolvedPackage = BallerinaPsiImplUtil.resolvePackage(packageNameNode);
        if (resolvedPackage != null) {
            List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInCurrentPackage(resolvedPackage);
            addAnnotationsAsLookups(resultSet, annotations);
        }
    }

    /**
     * Helper method to add annotations as lookup elements.
     *
     * @param resultSet   result list which is used to add lookups
     * @param annotations list of annotations which needs to be added
     */
    private void addAnnotationsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> annotations) {
        for (PsiElement annotation : annotations) {
            LookupElementBuilder builder = LookupElementBuilder.create(annotation.getText())
                    .withTypeText("Annotation").withIcon(BallerinaIcons.ANNOTATION)
                    .withInsertHandler(BracesInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, ANNOTATION_PRIORITY));
        }
    }

    /**
     * Helper method to add actions as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param actions   list of actions which needs to be added
     */
    private void addActionsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> actions) {
        for (PsiElement action : actions) {
            LookupElementBuilder builder = LookupElementBuilder.create(action.getText())
                    .withTypeText("Action").withIcon(BallerinaIcons.ACTION)
                    .withInsertHandler(FunctionCompletionInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, ACTION_PRIORITY));
        }
    }

    /**
     * Helper method to add value types as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param priority  priority of the lookup elements
     */
    private void addValueTypesAsLookups(@NotNull CompletionResultSet resultSet, int priority) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(BOOLEAN, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(INT, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(FLOAT, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(STRING, priority));
    }

    /**
     * Helper method to add reference types as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param priority  priority of the lookup elements
     */
    private void addReferenceTypesAsLookups(@NotNull CompletionResultSet resultSet, int priority) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(MESSAGE, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(XML, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(JSON, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(EXCEPTION, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(MAP, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(DATATABLE, priority));
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return typeChar == ':' || typeChar == '@';
    }
}
