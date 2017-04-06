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
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeValueNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationStatementNode;
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
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtil.*;

public class BallerinaCompletionContributor extends CompletionContributor implements DumbAware {

    // File level keywords
    private static final LookupElementBuilder PACKAGE;
    private static final LookupElementBuilder IMPORT;
    private static final LookupElementBuilder CONST;
    private static final LookupElementBuilder SERVICE;
    private static final LookupElementBuilder RESOURCE;
    private static final LookupElementBuilder FUNCTION;
    private static final LookupElementBuilder CONNECTOR;
    private static final LookupElementBuilder STRUCT;
    private static final LookupElementBuilder TYPEMAPPER;
    private static final LookupElementBuilder ANNOTATION;
    private static final LookupElementBuilder ATTACH;

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
        PACKAGE = createKeywordLookupElement("package", true, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        IMPORT = createKeywordLookupElement("import", true, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        CONST = createKeywordLookupElement("const", true, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        SERVICE = createKeywordLookupElement("service", true, AddSpaceInsertHandler.INSTANCE);
        RESOURCE = createKeywordLookupElement("resource", true, AddSpaceInsertHandler.INSTANCE);
        FUNCTION = createKeywordLookupElement("function", true, AddSpaceInsertHandler.INSTANCE);
        CONNECTOR = createKeywordLookupElement("connector", true, AddSpaceInsertHandler.INSTANCE);
        STRUCT = createKeywordLookupElement("struct", true, AddSpaceInsertHandler.INSTANCE);
        TYPEMAPPER = createKeywordLookupElement("typemapper", true, AddSpaceInsertHandler.INSTANCE);
        ANNOTATION = createKeywordLookupElement("annotation", true, AddSpaceInsertHandler.INSTANCE);
        ATTACH = createKeywordLookupElement("attach", true, AddSpaceInsertHandler.INSTANCE);

        BOOLEAN = createSimpleTypeLookupElement("boolean", true, AddSpaceInsertHandler.INSTANCE);
        INT = createSimpleTypeLookupElement("int", true, AddSpaceInsertHandler.INSTANCE);
        FLOAT = createSimpleTypeLookupElement("float", true, AddSpaceInsertHandler.INSTANCE);
        STRING = createSimpleTypeLookupElement("string", true, AddSpaceInsertHandler.INSTANCE);

        MESSAGE = createReferenceTypeLookupElement("message", true, AddSpaceInsertHandler.INSTANCE);
        XML = createReferenceTypeLookupElement("xml", true, AddSpaceInsertHandler.INSTANCE);
        JSON = createReferenceTypeLookupElement("json", true, AddSpaceInsertHandler.INSTANCE);
        EXCEPTION = createReferenceTypeLookupElement("exception", true, AddSpaceInsertHandler.INSTANCE);
        MAP = createReferenceTypeLookupElement("map", true, AddSpaceInsertHandler.INSTANCE);
        DATATABLE = createReferenceTypeLookupElement("datatable", true, AddSpaceInsertHandler.INSTANCE);

        REPLY = createKeywordLookupElement("reply", true, AddSpaceInsertHandler.INSTANCE);
        RETURN = createKeywordLookupElement("return", true, AddSpaceInsertHandler.INSTANCE);
        IF = createKeywordLookupElement("if", true, ParenthesisInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        ELSE = createKeywordLookupElement("else", true, AddSpaceInsertHandler.INSTANCE);
        CREATE = createKeywordLookupElement("create", true, AddSpaceInsertHandler.INSTANCE);
    }

    private static LookupElementBuilder createLookupElement(String name, boolean withBoldness,
                                                            InsertHandler<LookupElement> insertHandler) {
        return LookupElementBuilder.create(name).withBoldness(withBoldness).withInsertHandler(insertHandler);
    }

    private static LookupElementBuilder createKeywordLookupElement(String name, boolean withBoldness,
                                                                   InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, withBoldness, insertHandler).withTypeText("Keyword", true);
    }

    private static LookupElementBuilder createSimpleTypeLookupElement(String name, boolean withBoldness,
                                                                      InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, withBoldness, insertHandler).withTypeText("Simple Type", true);
    }

    private static LookupElementBuilder createReferenceTypeLookupElement(String name, boolean withBoldness,
                                                                         InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, withBoldness, insertHandler).withTypeText("Reference Type", true);
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
                        addSuggestions(parameters, resultSet, parameters.getOriginalFile());
                    }
                }
        );
    }

    private void addSuggestions(CompletionParameters parameters, CompletionResultSet resultSet, PsiFile originalFile) {

        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement parentPrevSibling = parent.getPrevSibling();
        PsiElement prevSibling = element.getPrevSibling();

        // if the parent is a literal value node, no need to add lookup elements.
        if (parent instanceof SimpleLiteralNode) {
            return;
        }

        // Todo - Add literal value node, service definition
        if (parent instanceof PsiFile) {
            // If the parent is PsiFile, that means we can only suggest keywords including 'package' and 'import'
            // keywords.
            addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
        } else if (parentPrevSibling instanceof ImportDeclarationNode
                || parentPrevSibling instanceof PackageDeclarationNode) {
            // If the previous sibling of the parent is PackageDeclarationNode, that means we have already added
            // package declaration. If it is ImportDeclarationNode, no need to suggest 'package' keyword because we
            // cannot add package declaration after an import.
            addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
        } else if (parent instanceof PackageNameNode) {
            // Check whether we are in the package declaration
            if (parent.getParent().getParent() instanceof PackageDeclarationNode) {
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
                    // Add directories as lookup elements. We need to call the createWithIcon method. Otherwise the
                    // icons will not display for directories.
                    resultSet.addElement(LookupElementBuilder.createWithIcon(directory)
                            .withInsertHandler(insertHandler));
                }
            } else if (!(parent.getParent() instanceof AliasNode)
                    && parent.getParent().getParent() instanceof ImportDeclarationNode) {
                // If the parent is not an AliasNode and is inside the ImportDeclarationNode, we need to suggest
                // packages.

                // Suggest import packages.
                PsiDirectory[] packageDirectories = BallerinaPsiImplUtil.suggestImportPackages(element);
                // Get names of all suggested packages.
                List<String> allImportedPackages =
                        BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(element).stream()
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
                    resultSet.addElement(LookupElementBuilder.createWithIcon(directory)
                            .withInsertHandler(insertHandler));
                }
            } else {
                // Todo - Handle scenario
            }
        } else if (parent instanceof ImportDeclarationNode) {
            // Almost the same logic as before.
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
        } else if (parent instanceof PsiErrorElement) {
            // If the current position has a PsiErrorElement, this will be called.
            PsiElement superParent = parent.getParent();
            // Todo - Add throws keyword
            // Todo - Add return keyword
            // Todo - Add variables
            if (superParent instanceof ExpressionNode || superParent instanceof AnnotationAttributeValueNode
                    || superParent instanceof AnnotationAttachmentNode) {
                return;
            }

            if (superParent instanceof ResourceDefinitionNode || superParent instanceof ServiceBodyNode) {
                addKeyword(resultSet, RESOURCE, CONTEXT_KEYWORD_PRIORITY);
            }

            if (superParent instanceof StatementNode) {
                // If the superParent is StatementNode, add following lookup elements.
                addValueTypes(resultSet, VALUE_TYPE_PRIORITY);
                addReferenceTypes(resultSet, REFERENCE_TYPE_PRIORITY);

                addConnectors(resultSet, element, originalFile);
                addFunctions(resultSet, element, originalFile);
                addStructs(resultSet, originalFile);
                addPackages(resultSet, originalFile);
                addVariables(resultSet, element);
                addConstants(resultSet, originalFile);

                addKeyword(resultSet, IF, CONTEXT_KEYWORD_PRIORITY);
                addKeyword(resultSet, ELSE, CONTEXT_KEYWORD_PRIORITY);

                ResourceDefinitionNode resourceDefinitionNode =
                        PsiTreeUtil.getParentOfType(element, ResourceDefinitionNode.class);
                if (resourceDefinitionNode != null) {
                    addKeyword(resultSet, REPLY, CONTEXT_KEYWORD_PRIORITY);
                }

                FunctionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionNode.class);
                if (functionNode != null) {
                    addKeyword(resultSet, RETURN, CONTEXT_KEYWORD_PRIORITY);
                }

                TypeMapperNode typeMapperNode =
                        PsiTreeUtil.getParentOfType(element, TypeMapperNode.class);
                if (typeMapperNode != null) {
                    addKeyword(resultSet, RETURN, CONTEXT_KEYWORD_PRIORITY);
                }

            } else if (superParent instanceof CompilationUnitNode) {
                // This can be called depending on the caret location.
                if (parentPrevSibling == null) {
                    // If there are no previous siblings, show keywords including 'package' keyword.
                    addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
                } else {
                    // Todo - Move to util
                    // If there are previous siblings, get the non whitespace sibling.
                    PsiElement nonWhitespaceElement = parent.getPrevSibling();
                    while (nonWhitespaceElement != null && nonWhitespaceElement instanceof PsiWhiteSpace) {
                        nonWhitespaceElement = nonWhitespaceElement.getPrevSibling();
                    }
                    if (nonWhitespaceElement != null) {
                        // if the nonWhitespaceElement is ImportDeclarationNode or PackageDeclarationNode, we don't
                        // need to add 'package' keyword.
                        if (nonWhitespaceElement instanceof ImportDeclarationNode ||
                                nonWhitespaceElement instanceof PackageDeclarationNode) {
                            addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
                        } else {
                            // otherwise that means that the caret is located after some definition. So no need to
                            // suggest 'import' keyword.
                            addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, false, false);
                        }
                    } else {
                        // If the nonWhitespaceElement is null, that means we are the beginning of a new node after
                        // one or more nodes.
                        addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
                    }
                }
            } else if (superParent instanceof AnnotationDefinitionNode) {
                addKeyword(resultSet, ATTACH, CONTEXT_KEYWORD_PRIORITY);
            } else {
                // Todo - Check for other valid scenarios.
                // Handle all other situations.
                if (parentPrevSibling == null) {
                    addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
                } else {
                    addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
                }
            }
        } else if (parent instanceof FunctionNode || parent instanceof ParameterNode) {
            if (prevSibling != null) {
                if ("(".equals(prevSibling.getText())) {
                    // Todo - Handle scenario
                } else {
                    // Todo - Check type
                    // Todo - Handle scenario
                }
            } else {
                // Todo - Handle scenario
            }
        } else if (parent instanceof TypeNameNode || parent instanceof NameReferenceNode) {
            PsiElement sibling = parent.getParent().getPrevSibling();

            if (sibling == null) {
                addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);

                addFunctions(resultSet, element, originalFile);
                addConnectors(resultSet, element, originalFile);
                addStructs(resultSet, originalFile);
                addPackages(resultSet, originalFile);
                addVariables(resultSet, element);
                addConstants(resultSet, originalFile);
                addAnnotations(resultSet, element);

                addKeyword(resultSet, IF, CONTEXT_KEYWORD_PRIORITY);
                addKeyword(resultSet, ELSE, CONTEXT_KEYWORD_PRIORITY);
                addKeyword(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);

                // Todo - Move to utils
                PsiElement temp = parent.getParent().getParent().getParent().getParent();
                while (temp != null && !(temp instanceof PsiFile)) {
                    if (temp instanceof StatementNode) {
                        addVariables(resultSet, element);
                        addConstants(resultSet, originalFile);
                        break;
                    }
                    temp = temp.getParent();
                }
                return;
            } else if (":".equals(sibling.getText())) {

                sibling = sibling.getPrevSibling();
                if (sibling == null) {

                    addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                    addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);

                    addConnectors(resultSet, element, originalFile);
                    addStructs(resultSet, originalFile);
                    addPackages(resultSet, originalFile);
                    addAnnotations(resultSet, element);

                    // Todo - Move to utils
                    PsiElement temp = parent.getParent().getParent().getParent().getParent();
                    while (temp != null && !(temp instanceof PsiFile)) {
                        if (temp instanceof StatementNode) {
                            addVariables(resultSet, element);
                            addConstants(resultSet, originalFile);
                            break;
                        }
                        temp = temp.getParent();
                    }
                    return;
                }

                PsiElement lastChild = sibling.getLastChild();
                List<PsiElement> packages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(originalFile);
                for (PsiElement pack : packages) {

                    if (lastChild.getText().equals(pack.getText())) {
                        PsiDirectory[] psiDirectories =
                                BallerinaPsiImplUtil.resolveDirectory(((PackageNameNode) pack).getNameIdentifier());

                        if (psiDirectories.length == 1) {
                            List<PsiElement> functions =
                                    BallerinaPsiImplUtil.getAllFunctionsInPackage(psiDirectories[0]);
                            addFunctions(resultSet, functions);

                            List<PsiElement> connectors =
                                    BallerinaPsiImplUtil.getAllConnectorsInPackage(psiDirectories[0]);
                            addConnectors(resultSet, connectors);

                            List<PsiElement> structs =
                                    BallerinaPsiImplUtil.getAllStructsInPackage(psiDirectories[0]);
                            addStructs(resultSet, structs);
                        } else {
                            // This situation cannot/should not happen since all the imported packages are unique.
                            // If this happen, this should be highlighted using an annotator.
                        }
                    }
                }
            } else {
                addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);

                addConnectors(resultSet, element, originalFile);
                addStructs(resultSet, originalFile);
                addPackages(resultSet, originalFile);
                addAnnotations(resultSet, element);

                // Todo - Move to utils
                // Check parent type
                PsiElement temp = parent.getParent().getParent().getParent().getParent();
                while (temp != null && !(temp instanceof PsiFile)) {
                    // If parent type is StatementNode, add variable lookup elements
                    if (temp instanceof StatementNode) {
                        addVariables(resultSet, element);
                        addConstants(resultSet, originalFile);
                        break;
                    }
                    temp = temp.getParent();
                }
            }
        } else if (parent instanceof StatementNode) {

            PsiFile file = parameters.getOriginalFile();
            PsiElement prevToken = file.findElementAt(parameters.getOffset() - 1);

            if (prevToken != null) {
                if (":".equals(prevToken.getText())) {

                    PsiElement prevElement = file.findElementAt(parameters.getOffset() - 2);
                    if (prevElement == null) {
                        return;
                    }
                    // Get all imported packages in current file
                    List<PsiElement> packages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(originalFile);
                    for (PsiElement pack : packages) {
                        // Compare text to identify the correct package
                        if (prevElement.getText().equals(pack.getText())) {
                            // Resolve the package and get all matching directories. But all imported packages should be
                            // unique. So the maximum size of this should be 1. If this is 0, that means the package is
                            // not imported. If this is more than 1, it means there are duplicate package imports or
                            // there are multiple packages with the same name.
                            PsiDirectory[] psiDirectories =
                                    BallerinaPsiImplUtil.resolveDirectory(((PackageNameNode) pack).getNameIdentifier());

                            if (psiDirectories.length == 1) {
                                // Get all functions in the package.
                                List<PsiElement> functions =
                                        BallerinaPsiImplUtil.getAllFunctionsInPackage(psiDirectories[0]);
                                // Add all functions as lookup elements.
                                addFunctions(resultSet, functions);

                                List<PsiElement> connectors =
                                        BallerinaPsiImplUtil.getAllConnectorsInPackage(psiDirectories[0]);
                                addConnectors(resultSet, connectors);

                                List<PsiElement> structs =
                                        BallerinaPsiImplUtil.getAllStructsInPackage(psiDirectories[0]);
                                addStructs(resultSet, structs);

                                // Todo - Uncomment after grammar fix
                                // List<PsiElement> constants =
                                //         BallerinaPsiImplUtil.getAllConstantsInPackage(psiDirectories[0]);
                                // addConstants(resultSet, constants);

                            } else {
                                // This situation cannot/should not happen since all the imported packages are unique.
                                // This should be highlighted using an annotator.
                            }
                        }
                    }
                } else if (Objects.equals(".", prevToken.getText())) {
                    // Todo - Add struct, map field suggestions
                } else if ("else".equals(prevToken.getText())) {
                    resultSet.addElement(PrioritizedLookupElement.withPriority(IF, CONTEXT_KEYWORD_PRIORITY));
                } else {
                    // Todo - Add struct, map field suggestions
                    resultSet.addElement(PrioritizedLookupElement.withPriority(IF, CONTEXT_KEYWORD_PRIORITY));
                }
            } else {
                // Todo - Handle scenario
            }
            // Todo - change logic
            //            PsiElement temp = parentPrevSibling;
            //            // Get not empty sibling
            //            while (temp != null && temp.getText().isEmpty()) {
            //                temp = temp.getPrevSibling();
            //            }
            //
            //            // If there is a not empty sibling, that means it is a package. Ex:- system:
            //            if (temp != null) {
            //
            //            } else {
            //                addVariables(element, resultSet);
            //            }
        } else if (parent instanceof VariableReferenceNode) {
            // Todo - Get all variables in the scope
            // Todo - Get all constant variables
            PsiFile file = element.getContainingFile();
            PsiElement prevToken = file.findElementAt(parameters.getOffset() - 1);

            while (prevToken != null && prevToken instanceof PsiWhiteSpace) {
                prevToken = prevToken.getPrevSibling();
            }

            if (prevToken != null) {

                addKeyword(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);

                PsiElement prevTokenParent = prevToken.getParent();
                PsiElement firstChild = prevTokenParent.getFirstChild();
                PsiElement lastChild = prevTokenParent.getLastChild();

                // Don't suggest values inside MapStructInitKeyValueListNode because we cannot resolve the
                // fields yet.
                if ((firstChild != null && "{".equals(firstChild.getText()) && lastChild != null
                        && "}".equals(lastChild.getText()))) {
                    // Todo - Get all fields from struct or map
                    return;
                }

                // Don't suggest values inside MapStructInitKeyValueNode as well because we cannot resolve the
                // fields yet.
                PsiElement temp = prevToken;
                while (temp != null && !(temp instanceof PsiFile)) {
                    temp = temp.getParent();
                    //Todo - Get all fields from struct or map
                }

                // prevToken.getParent().getFirstChild()
                // prevToken.getParent().getChildren()[0].getTextOffset() == parameters.getOffset()

                while (prevToken.getParent() instanceof VariableReferenceNode) {
                    prevToken = prevToken.getParent();
                }

                PsiElement[] children = prevToken.getChildren();

                if (children.length != 0) {
                    // Check whether the current identifier is the first identifier. Then we only need to suggest
                    // following elements.
                    if (prevToken.getText().equals(children[0].getText())) {
                        addVariables(resultSet, element);
                        addConstants(resultSet, originalFile);
                        addFunctions(resultSet, element, originalFile);
                        addPackages(resultSet, originalFile);
                        return;
                    }

                    // Todo - Add struct, map field suggestions
                    for (int i = 0; i < children.length; i++) {
                        // Todo - Complete
                    }

                    if (".".equals(prevToken.getText())) {
                        // Todo - Resolve
                    } else if (prevToken instanceof IdentifierPSINode) {
                        addVariables(resultSet, element);
                        addConstants(resultSet, originalFile);
                        addFunctions(resultSet, element, originalFile);
                        addPackages(resultSet, originalFile);
                    } else {
                        addVariables(resultSet, element);
                        addConstants(resultSet, originalFile);
                        addFunctions(resultSet, element, originalFile);
                        addPackages(resultSet, originalFile);
                    }
                } else {
                    addVariables(resultSet, element);
                    addConstants(resultSet, originalFile);
                    addFunctions(resultSet, element, originalFile);
                    addPackages(resultSet, originalFile);
                }
            } else {
                // Todo - Is valid condition?
                addVariables(resultSet, element);
                addConstants(resultSet, originalFile);
                addFunctions(resultSet, element, originalFile);
                addPackages(resultSet, originalFile);
            }
        } else if (parent instanceof ActionInvocationNode) {
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
                    addActions(resultSet, allActions);
                }
            }
        } else if (parent instanceof FunctionInvocationStatementNode) {
            // Todo - Handle scenario
        } else {
            // If we are currently at an identifier node, no need to suggest.
            if (element instanceof IdentifierPSINode) {
                return;
            }
            if (parentPrevSibling == null) {
                addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
            } else {
                addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
            }
        }
    }

    private void addKeyword(CompletionResultSet resultSet, LookupElement lookupElement, int priority) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, priority));
    }

    /**
     * Adds variables in the resolvable scopes as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param element   element in the current caret position
     */
    private void addVariables(CompletionResultSet resultSet, PsiElement element) {
        PsiElement context = element.getContext();
        if (context == null) {
            context = element.getParent().getContext();
        }
        List<PsiElement> variables = BallerinaPsiImplUtil.getAllVariablesInResolvableScope(context);
        for (PsiElement variable : variables) {
            LookupElementBuilder builder = LookupElementBuilder.create(variable.getText())
                    .withTypeText("Variable").withIcon(AllIcons.Nodes.Variable);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Helper method to add constants as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param constants list of constants which needs to be added
     */
    private void addConstants(CompletionResultSet resultSet, List<PsiElement> constants) {
        for (PsiElement constant : constants) {
            LookupElementBuilder builder = LookupElementBuilder.create(constant.getText())
                    .withTypeText("Constant").withIcon(AllIcons.Nodes.Variable);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Helper method to add constants as lookup elements.
     *
     * @param resultSet    result set which needs to add the lookup elements
     * @param originalFile original file which we are editing
     */
    private void addConstants(CompletionResultSet resultSet, PsiFile originalFile) {
        PsiDirectory parentDirectory = originalFile.getParent();
        if (parentDirectory != null) {
            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsInPackage(parentDirectory);
            addConstants(resultSet, constants);
        }
    }

    /**
     * Helper method to add functions as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param functions list of functions which needs to be added
     */
    private void addFunctions(CompletionResultSet resultSet, List<PsiElement> functions) {
        for (PsiElement function : functions) {
            LookupElementBuilder builder = LookupElementBuilder.create(function.getText())
                    .withTypeText("Function").withTailText("()", true).withIcon(AllIcons.Nodes.Field)
                    .withInsertHandler(FunctionCompletionInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, FUNCTION_PRIORITY));
        }
    }

    /**
     * Helper method to add functions as lookup elements.
     *
     * @param resultSet    result set which needs to add the lookup elements
     * @param originalFile original file which we are editing
     */
    private void addFunctions(CompletionResultSet resultSet, PsiElement element, PsiFile originalFile) {
        PsiElement parent = element.getParent();
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            addFunctions(resultSet, originalFile);
        } else {
            PsiElement resolvedPackage = resolvePackage(packageNameNode);
            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllFunctionsInCurrentPackage(resolvedPackage);
            addFunctions(resultSet, connectors);
        }
    }

    private void addFunctions(CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllFunctionsInCurrentPackage(originalFile);
        addFunctions(resultSet, connectors);
    }

    /**
     * Helper method to add packages as lookup elements.
     *
     * @param resultSet    result set which needs to add the lookup elements
     * @param originalFile file which will be used to get imported packages
     */
    private void addPackages(CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> packages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(originalFile);
        for (PsiElement pack : packages) {
            LookupElementBuilder builder = LookupElementBuilder.create(pack.getText())
                    .withTypeText("Package").withIcon(AllIcons.Nodes.Package)
                    .withInsertHandler(PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY));
        }
    }

    /**
     * Helper method to add structs as lookup elements.
     *
     * @param resultSet    result set which needs to add the lookup elements
     * @param originalFile file which will be used to get structs
     */
    private void addStructs(CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsInCurrentPackage(originalFile);
        addStructs(resultSet, structs);
    }


    /**
     * Helper method to add structs as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param structs   list of structs which needs to be added
     */
    private void addStructs(CompletionResultSet resultSet, List<PsiElement> structs) {
        for (PsiElement struct : structs) {
            LookupElementBuilder builder = LookupElementBuilder.create(struct.getText())
                    .withTypeText("Struct").withIcon(AllIcons.Nodes.Static);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, STRUCT_PRIORITY));
        }
    }

    private void addConnectors(CompletionResultSet resultSet, PsiElement element, PsiFile originalFile) {
        PsiElement parent = element.getParent();
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            addConnectors(resultSet, originalFile);
        } else {
            PsiElement resolvedPackage = resolvePackage(packageNameNode);
            if (resolvedPackage != null) {
                List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInCurrentPackage(resolvedPackage);
                addConnectors(resultSet, connectors);
            }
        }
    }

    /**
     * Helper method to add connectors as lookup elements.
     *
     * @param resultSet    result set which needs to add the lookup elements
     * @param originalFile original file which we are editing
     */
    private void addConnectors(CompletionResultSet resultSet, PsiFile originalFile) {
        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInCurrentPackage(originalFile);
        addConnectors(resultSet, connectors);
    }

    /**
     * Helper method to add connections as lookup elements.
     *
     * @param resultSet  result set which needs to add the lookup elements
     * @param connectors list of connectors which needs to be added
     */
    private void addConnectors(CompletionResultSet resultSet, List<PsiElement> connectors) {
        for (PsiElement connector : connectors) {
            LookupElementBuilder builder = LookupElementBuilder.create(connector.getText())
                    .withTypeText("Connector").withIcon(AllIcons.Nodes.Class);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, CONNECTOR_PRIORITY));
        }
    }

    private PsiElement resolvePackage(PackageNameNode packageNameNode) {
        PsiElement nameIdentifier = packageNameNode.getNameIdentifier();
        if (nameIdentifier == null) {
            return null;
        }
        PsiReference reference = nameIdentifier.getReference();
        if (reference != null) {
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement != null) {
                return resolvedElement;
            }
        }
        PsiReference[] references = nameIdentifier.getReferences();
        if (references.length == 0) {
            return null;
        }
        for (PsiReference psiReference : references) {
            PsiElement resolvedElement = psiReference.resolve();
            if (resolvedElement == null) {
                continue;
            }
            return resolvedElement;
        }
        return null;
    }

    /**
     * Helper method to add annotations as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param element   element at caret position
     */
    private void addAnnotations(CompletionResultSet resultSet, PsiElement element) {
        PsiElement parent = element.getParent();
        PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
        if (packageNameNode == null) {
            return;
        }
        PsiElement resolvedPackage = resolvePackage(packageNameNode);
        if (resolvedPackage != null) {
            List<PsiElement> annotations = BallerinaPsiImplUtil.getAllAnnotationsInCurrentPackage(resolvedPackage);
            addAnnotations(resultSet, annotations);
        }
    }

    /**
     * Helper method to add annotations as lookup elements.
     *
     * @param resultSet   result set which needs to add the lookup elements
     * @param annotations list of annotations which needs to be added
     */
    private void addAnnotations(CompletionResultSet resultSet, List<PsiElement> annotations) {
        for (PsiElement annotation : annotations) {
            LookupElementBuilder builder = LookupElementBuilder.create(annotation.getText())
                    .withTypeText("Annotation").withIcon(AllIcons.Nodes.Annotationtype)
                    .withInsertHandler(BracesInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, ANNOTATION_PRIORITY));
        }
    }

    /**
     * Helper method to add actions as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param actions   list of actions which needs to be added
     */
    private void addActions(CompletionResultSet resultSet, List<PsiElement> actions) {
        for (PsiElement action : actions) {
            LookupElementBuilder builder = LookupElementBuilder.create(action.getText())
                    .withTypeText("Action").withIcon(AllIcons.Nodes.AnonymousClass)
                    .withInsertHandler(FunctionCompletionInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, ACTION_PRIORITY));
        }
    }

    /**
     * Helper method to add value types as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param priority  priority of the lookup elements
     */
    private void addValueTypes(CompletionResultSet resultSet, int priority) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(BOOLEAN, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(INT, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(FLOAT, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(STRING, priority));
    }

    /**
     * Helper method to add reference types as lookup elements.
     *
     * @param resultSet result set which needs to add the lookup elements
     * @param priority  priority of the lookup elements
     */
    private void addReferenceTypes(CompletionResultSet resultSet, int priority) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(MESSAGE, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(XML, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(JSON, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(EXCEPTION, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(MAP, priority));
        resultSet.addElement(PrioritizedLookupElement.withPriority(DATATABLE, priority));
    }

    /**
     * Helper method to add file level keywords as lookup elements.
     *
     * @param resultSet   result set which needs to add the lookup elements
     * @param priority    priority of the lookup elements
     * @param withPackage indicates whether to add 'package' keyword
     * @param withImport  indicates whether to add 'import' keyword
     */
    private void addFileLevelKeywords(CompletionResultSet resultSet, int priority, boolean withPackage,
                                      boolean withImport) {
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

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return typeChar == ':' || typeChar == '@';
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);
    }
}
