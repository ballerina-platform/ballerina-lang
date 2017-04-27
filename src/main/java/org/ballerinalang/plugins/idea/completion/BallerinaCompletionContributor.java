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
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeValueNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
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
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
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
        PACKAGE = createKeywordLookupElement("package", AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        IMPORT = createKeywordLookupElement("import", AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        CONST = createKeywordLookupElement("const", AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        SERVICE = createKeywordLookupElement("service", AddSpaceInsertHandler.INSTANCE);
        RESOURCE = createKeywordLookupElement("resource", AddSpaceInsertHandler.INSTANCE);
        FUNCTION = createKeywordLookupElement("function", AddSpaceInsertHandler.INSTANCE);
        CONNECTOR = createKeywordLookupElement("connector", AddSpaceInsertHandler.INSTANCE);
        STRUCT = createKeywordLookupElement("struct", AddSpaceInsertHandler.INSTANCE);
        TYPEMAPPER = createKeywordLookupElement("typemapper", AddSpaceInsertHandler.INSTANCE);
        ANNOTATION = createKeywordLookupElement("annotation", AddSpaceInsertHandler.INSTANCE);
        ATTACH = createKeywordLookupElement("attach", AddSpaceInsertHandler.INSTANCE);

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

    private static LookupElementBuilder createLookupElement(String name,
                                                            InsertHandler<LookupElement> insertHandler) {
        return LookupElementBuilder.create(name).withBoldness(true).withInsertHandler(insertHandler);
    }

    private static LookupElementBuilder createKeywordLookupElement(String name,
                                                                   InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, insertHandler).withTypeText("Keyword", true);
    }

    private static LookupElementBuilder createSimpleTypeLookupElement(String name,
                                                                      InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, insertHandler).withTypeText("Simple Type", true);
    }

    private static LookupElementBuilder createReferenceTypeLookupElement(String name,
                                                                         InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, insertHandler).withTypeText("Reference Type", true);
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

    private void addSuggestions(CompletionParameters parameters, CompletionResultSet resultSet) {

        PsiElement element = parameters.getPosition();
        PsiElement parent = element.getParent();
        PsiElement parentPrevSibling = parent.getPrevSibling();
        PsiElement prevSibling = element.getPrevSibling();

        // if the parent is a literal value node, no need to add lookup elements.
        if (parent instanceof SimpleLiteralNode) {
            return;
        }

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
            handlePackageNameNode(resultSet, element, parent);
        } else if (parent instanceof ImportDeclarationNode) {
            handleImportDeclarationNode(resultSet, element);
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
            handleNameReferenceNode(parameters, resultSet, element, parent);
        } else if (parent instanceof StatementNode) {
            handleStatementNode(parameters, resultSet);
        } else if (parent instanceof VariableReferenceNode) {
            handleVariableReferenceNode(parameters, resultSet, element);
        } else if (parent instanceof ActionInvocationNode) {
            handleActionInvocationNode(resultSet, parent);
        } else if (parent instanceof FunctionInvocationStatementNode) {
            handleFunctionInvocationStatementNode(parameters, resultSet, prevSibling);
        } else if (parent instanceof PsiErrorElement) {
            handlePsiErrorElement(parameters, resultSet, element, parent, parentPrevSibling);
        } else if (parent instanceof ConstantDefinitionNode) {
            handleConstantDefinitionNode(resultSet, prevSibling);
        } else if (parent instanceof VariableDefinitionNode) {
            // This will be called when invoking a element from a package. Ex- system:println(package:|)
            // Previous sibling will contain the "package:".
            if (prevSibling == null) {
                return;
            }
            // Check whether previous element text matches the pattern.
            if (!prevSibling.getText().matches(".+:")) {
                return;
            }
            // Identifier node will contain the package name.
            IdentifierPSINode packageName = PsiTreeUtil.findElementOfClassAtOffset(prevSibling.getContainingFile(),
                    prevSibling.getTextOffset() + prevSibling.getTextLength() - 2, IdentifierPSINode.class, false);
            //                PsiTreeUtil.findChildOfType(prevSibling, IdentifierPSINode.class);
            if (packageName == null) {
                return;
            }
            suggestElementsFromAPackage(parameters, resultSet, packageName, null, true, true, true, true);
        } else {
            // If we are currently at an identifier node or a comment node, no need to suggest.
            if (element instanceof IdentifierPSINode || element instanceof PsiComment) {
                return;
            }
            if (parentPrevSibling == null) {
                addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, true, true);
            } else {
                addFileLevelKeywords(resultSet, CONTEXT_KEYWORD_PRIORITY, false, true);
            }
        }
    }

    private void handleConstantDefinitionNode(CompletionResultSet resultSet, PsiElement prevSibling) {
        if (prevSibling != null && prevSibling instanceof ValueTypeNameNode) {
            addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
        }
    }

    private void handleFunctionInvocationStatementNode(CompletionParameters parameters, CompletionResultSet resultSet,
                                                       PsiElement prevSibling) {

        PsiFile originalFile = parameters.getOriginalFile();

        // This will be called when invoking a element from a package. Ex- system:println(package:|)
        // Previous sibling will contain the "package:".
        if (prevSibling == null) {
            return;
        }
        // Check whether previous element text matches the pattern.
        if (!prevSibling.getText().matches(".+:")) {
            return;
        }
        // Identifier node will contain the package name.
        IdentifierPSINode packageName = PsiTreeUtil.findElementOfClassAtOffset(prevSibling.getContainingFile(),
                prevSibling.getTextOffset() + prevSibling.getTextLength() - 2, IdentifierPSINode.class, false);
        //                PsiTreeUtil.findChildOfType(prevSibling, IdentifierPSINode.class);
        if (packageName == null) {
            return;
        }
        // Add suggestions.
        suggestElementsFromAPackage(parameters, resultSet, packageName, null, true, true, true, true);
    }

    private void handleVariableReferenceNode(CompletionParameters parameters, CompletionResultSet resultSet,
                                             PsiElement element) {

        PsiFile originalFile = parameters.getOriginalFile();

        //        // If we are in the {} of a struct variable reference node, we need to show fields.
        //        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(element,
        //                VariableDefinitionNode.class);
        //        if (variableDefinitionNode != null) {
        //            // Todo - add struct fields
        //            return;
        //        }

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
    }

    private void handleStatementNode(CompletionParameters parameters, CompletionResultSet resultSet) {
        PsiFile originalFile = parameters.getOriginalFile();
        int count = 1;
        PsiElement prevElement = originalFile.findElementAt(parameters.getOffset() - count++);
        while (prevElement instanceof PsiWhiteSpace) {
            prevElement = originalFile.findElementAt(parameters.getOffset() - count++);
        }
        if (prevElement instanceof IdentifierPSINode) {
            count = 1;
            PsiElement token = originalFile.findElementAt(prevElement.getTextOffset() - count++);
            while (token instanceof PsiWhiteSpace) {
                token = originalFile.findElementAt(prevElement.getTextOffset() - count++);
            }
            if (token != null && token instanceof LeafPsiElement) {
                //                IElementType elementType = ((LeafPsiElement) token).getElementType();
                if (":".equals(token.getText())) {
                    PsiElement partialIdentifier = prevElement;
                    PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, partialIdentifier, true, true,
                            true, true);
                } else {
                    addFunctions(resultSet, prevElement, originalFile);
                    addPackages(resultSet, originalFile);
                    addVariables(resultSet, prevElement);
                    addConstants(resultSet, originalFile);
                }

                if ("{".equals(token.getText()) || ";".equals(token.getText())) {
                    addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                    addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                }
            } else {
                PsiElement partialIdentifier = prevElement;
                PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                suggestElementsFromAPackage(parameters, resultSet, packageNode, partialIdentifier, true, true, true,
                        true);
            }
        } else if (prevElement instanceof LeafPsiElement) {

            IElementType elementType = ((LeafPsiElement) prevElement).getElementType();

            // Cannot use a switch statement since the types are not constants and declaring them final does not fix
            // the issue as well.
            if (elementType == BallerinaTypes.COLON) {
                PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                suggestElementsFromAPackage(parameters, resultSet, packageNode, null, true, true, true, true);
            } else if (elementType == BallerinaTypes.ADD || elementType == BallerinaTypes.SUB ||
                    elementType == BallerinaTypes.MUL || elementType == BallerinaTypes.DIV ||
                    elementType == BallerinaTypes.ASSIGN || elementType == BallerinaTypes.GT ||
                    elementType == BallerinaTypes.LT || elementType == BallerinaTypes.EQUAL ||
                    elementType == BallerinaTypes.LE || elementType == BallerinaTypes.GE ||
                    elementType == BallerinaTypes.NOTEQUAL || elementType == BallerinaTypes.AND ||
                    elementType == BallerinaTypes.OR || elementType == BallerinaTypes.MOD) {
                addFunctions(resultSet, prevElement, originalFile);
                addPackages(resultSet, originalFile);
                addVariables(resultSet, prevElement);
                addConstants(resultSet, originalFile);
            } else {
                addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addOtherCommonKeywords(resultSet);

                addFunctionSpecificKeywords(parameters, resultSet, originalFile);

                addFunctions(resultSet, prevElement, originalFile);
                addPackages(resultSet, originalFile);
                addVariables(resultSet, prevElement);
                addConstants(resultSet, originalFile);
            }


        } else {
            addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
            addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
            addKeyword(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
            addOtherCommonKeywords(resultSet);

            addFunctionSpecificKeywords(parameters, resultSet, originalFile);

            addFunctions(resultSet, prevElement, originalFile);
            addPackages(resultSet, originalFile);
            addVariables(resultSet, prevElement);
            addConstants(resultSet, originalFile);
        }


        //        int count = 1;
        //        PsiFile file = parameters.getOriginalFile();
        //        PsiElement prevElement = file.findElementAt(parameters.getOffset() - count);
        //        //        PsiTreeUtil.skipSiblingsBackward(,   PsiWhiteSpace.class);
        //
        //        while (prevElement instanceof PsiWhiteSpace) {
        //            prevElement = file.findElementAt(parameters.getOffset() - ++count);
        //        }
        //
        //        if (prevElement == null /*|| !(prevElement.getParent() instanceof StatementNode ||
        //                prevElement instanceof CallableUnitBodyNode)*/) {
        //            return;
        //        }
        //        // Todo - Add struct field suggestions
        //        if (":".equals(prevElement.getText())) {
        //            // Nothing typed after "package:". So we suggest all elements in the package.
        //            prevElement = file.findElementAt(parameters.getOffset() - 2);
        //            if (prevElement == null) {
        //                return;
        //            }
        //            suggestElementsFromAPackage(resultSet, originalFile, prevElement, null);
        //        } else if (".".equals(prevElement.getText())) {
        //            addStructFields(parameters, resultSet, file);
        //        } else if (("=".equals(prevElement.getText()) && prevElement instanceof IdentifierPSINode)
        //                || "+".equals(prevElement.getText())) {
        //            addFunctions(resultSet, prevElement, originalFile);
        //            addPackages(resultSet, originalFile);
        //            addVariables(resultSet, prevElement);
        //            addConstants(resultSet, originalFile);
        //        } else {
        //            // Something typed after "package:". In that case we need to suggest elements which starts with
        // what is
        //            // typed after "package:".
        //
        //            count = 2;
        //            PsiElement element = file.findElementAt(parameters.getOffset() - count);
        //            while (element != null && !":".equals(element.getText())) {
        //
        //                int offset = parameters.getOffset() - ++count;
        //                if (offset >= prevElement.getTextOffset()) {
        //                    element = file.findElementAt(offset);
        //                } else {
        //                    break;
        //                }
        //            }
        //            if (element == null) {
        //                return;
        //            }
        //            PsiElement packageNode = file.findElementAt(parameters.getOffset() - (count + 1));
        //            if (packageNode == null) {
        //                return;
        //            }
        //            PsiElement identifierNode = file.findElementAt(parameters.getOffset() - (count - 1));
        //            if (identifierNode == null) {
        //                return;
        //            }
        //            suggestElementsFromAPackage(resultSet, originalFile, packageNode, identifierNode);
        //        }
    }

    private void addFunctionSpecificKeywords(CompletionParameters parameters, CompletionResultSet resultSet, PsiFile
            originalFile) {
        PsiElement element = originalFile.findElementAt(parameters.getOffset());
        FunctionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionNode.class);
        if (functionNode != null) {
            addKeyword(resultSet, RETURN, CONTEXT_KEYWORD_PRIORITY);
        }
    }

    private void addStructFields(CompletionParameters parameters, CompletionResultSet resultSet, PsiFile file) {
        PsiElement prevElement;// struct field access.
        prevElement = file.findElementAt(parameters.getOffset() - 2);
        if (prevElement == null) {
            return;
        }
        PsiReference prevElementReference = prevElement.getReference();
        if (prevElementReference == null) {
            return;
        }
        PsiElement resolvedVariableDefElement = prevElementReference.resolve();
        if (resolvedVariableDefElement == null) {
            return;
        }
        if (!(resolvedVariableDefElement instanceof VariableDefinitionNode)) {
            resolvedVariableDefElement = resolvedVariableDefElement.getParent();
        }
        TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(resolvedVariableDefElement, TypeNameNode.class);
        if (typeNameNode == null) {
            return;
        }
        NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(typeNameNode, NameReferenceNode.class);
        if (nameReferenceNode == null) {
            return;
        }
        PsiElement nameIdentifier = nameReferenceNode.getNameIdentifier();
        if (nameIdentifier == null) {
            return;
        }
        PsiReference typeNameNodeReference = nameIdentifier.getReference();
        if (typeNameNodeReference == null) {
            return;
        }
        PsiElement resolvedDefElement = typeNameNodeReference.resolve();
        if (resolvedDefElement == null) {
            return;
        }
        if (!(resolvedDefElement.getParent() instanceof StructDefinitionNode)) {
            return;
        }
        Collection<FieldDefinitionNode> fieldDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(resolvedDefElement.getParent(), FieldDefinitionNode.class);
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
                    .withTailText(" -> " + resolvedDefElement.getText(), true);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, FIELD_PRIORITY));
        }
    }

    private void suggestElementsFromAPackage(CompletionParameters parameters, CompletionResultSet resultSet,
                                             PsiElement packageElement, PsiElement partialIdentifier,
                                             boolean suggestFunctions, boolean suggestConnectors,
                                             boolean suggestStructs, boolean suggestConstants) {

        PsiFile originalFile = parameters.getOriginalFile();

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
                    // Get all functions in the package.
                    if (suggestFunctions) {
                        List<PsiElement> functions =
                                BallerinaPsiImplUtil.getAllFunctionsInPackage(psiDirectories[0]);

                        if (partialIdentifier != null) {
                            functions = functions.stream()
                                    .filter(function -> function.getText().startsWith(partialIdentifier.getText()))
                                    .collect(Collectors.toList());
                        }
                        // Add all functions as lookup elements.
                        addFunctions(resultSet, functions);
                    }
                    if (suggestConnectors) {
                        List<PsiElement> connectors =
                                BallerinaPsiImplUtil.getAllConnectorsInPackage(psiDirectories[0]);
                        addConnectors(resultSet, connectors);
                    }
                    if (suggestStructs) {
                        List<PsiElement> structs =
                                BallerinaPsiImplUtil.getAllStructsInPackage(psiDirectories[0]);
                        if (partialIdentifier != null) {
                            structs = structs.stream()
                                    .filter(struct -> struct.getText().startsWith(partialIdentifier.getText()))
                                    .collect(Collectors.toList());
                        }
                        addStructs(resultSet, structs);
                    }
                    // Todo - Uncomment after grammar fix
                    // if (suggestConstants) {
                    // List<PsiElement> constants =
                    //         BallerinaPsiImplUtil.getAllConstantsInPackage(psiDirectories[0]);
                    // addConstants(resultSet, constants);
                    // }
                } else {
                    // This situation cannot/should not happen since all the imported packages are unique.
                    // This should be highlighted using an annotator.
                }
            }
        }
    }

    private void handleNameReferenceNode(CompletionParameters parameters, CompletionResultSet resultSet,
                                         PsiElement element, PsiElement parent) {

        PsiFile originalFile = parameters.getOriginalFile();

        ParameterNode parameterNode = PsiTreeUtil.getParentOfType(parent, ParameterNode.class);
        if (parameterNode != null) {

            int count = 1;
            PsiElement prevElement = originalFile.findElementAt(parameters.getOffset() - count++);
            while (prevElement instanceof PsiWhiteSpace) {
                prevElement = originalFile.findElementAt(parameters.getOffset() - count++);
            }
            if (prevElement instanceof IdentifierPSINode) {
                count = 1;
                PsiElement token = originalFile.findElementAt(prevElement.getTextOffset() - count++);
                while (token instanceof PsiWhiteSpace) {
                    token = originalFile.findElementAt(prevElement.getTextOffset() - count++);
                }
                if (token != null && token instanceof LeafPsiElement) {
                    //                IElementType elementType = ((LeafPsiElement) token).getElementType();
                    if (":".equals(token.getText())) {
                        PsiElement partialIdentifier = prevElement;
                        PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                        suggestElementsFromAPackage(parameters, resultSet, packageNode, partialIdentifier, false,
                                true, true, false);
                    } else {
                        //                        addFunctions(resultSet, prevElement, originalFile);
                        addPackages(resultSet, originalFile);
                        //                        addVariables(resultSet, prevElement);
                        //                        addConstants(resultSet, originalFile);
                    }

                    if ("{".equals(token.getText()) || ";".equals(token.getText())) {
                        addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                        addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                    }
                } else {
                    PsiElement partialIdentifier = prevElement;
                    PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, partialIdentifier, false, true,
                            true, false);
                }
            } else if (prevElement instanceof LeafPsiElement) {

                IElementType elementType = ((LeafPsiElement) prevElement).getElementType();

                // Cannot use a switch statement since the types are not constants and declaring them final does not fix
                // the issue as well.
                if (elementType == BallerinaTypes.COLON) {
                    PsiElement packageNode = originalFile.findElementAt(prevElement.getTextOffset() - 2);
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, null, false, true, true, false);
                } else if (elementType == BallerinaTypes.LPAREN) {

                    addPackages(resultSet, originalFile);
                    addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                    addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                }
                //                else {
                //                    addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                //                    addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                //                    addOtherCommonKeywords(resultSet);
                //
                //                    addFunctionSpecificKeywords(parameters, resultSet, originalFile);
                //
                //                    addFunctions(resultSet, prevElement, originalFile);
                //                    addPackages(resultSet, originalFile);
                //                    addVariables(resultSet, prevElement);
                //                    addConstants(resultSet, originalFile);
                //                }

            } else {
                addValueTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addReferenceTypes(resultSet, CONTEXT_KEYWORD_PRIORITY);
                addKeyword(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);
                addOtherCommonKeywords(resultSet);

                addFunctionSpecificKeywords(parameters, resultSet, originalFile);

                //                addFunctions(resultSet, prevElement, originalFile);
                //                addPackages(resultSet, originalFile);
                addVariables(resultSet, prevElement);
                addConstants(resultSet, originalFile);
            }

            return;
        }

        PsiFile file = parameters.getOriginalFile();
        PsiElement prevElement = file.findElementAt(parameters.getOffset() - 1);
        if (prevElement != null) {
            if (".".equals(prevElement.getText())) {
                addStructFields(parameters, resultSet, file);
                return;
            } else if (":".equals(prevElement.getText())) {

                prevElement = file.findElementAt(parameters.getOffset() - 2);
                if (prevElement == null) {
                    return;
                }
                suggestElementsFromAPackage(parameters, resultSet, prevElement, null, true, true, true, true);

                return;
            } else {
                PsiElement partialIdentifier = file.findElementAt(parameters.getOffset() - 1);

                int count = 2;
                prevElement = file.findElementAt(parameters.getOffset() - count);
                if (prevElement == null) {
                    return;
                }
                while (prevElement != null && !":".equals(prevElement.getText())) {
                    if (prevElement.getTextOffset() == 0) {
                        break;
                    }
                    prevElement = file.findElementAt(parameters.getOffset() - ++count);
                }
                if (prevElement == null) {
                    return;
                }
                PsiElement prevElementParent = prevElement.getParent();
                if (prevElementParent == null) {
                    return;
                }
                PsiElement prevElementSuperParent = prevElementParent.getParent();
                if (prevElementSuperParent == null) {
                    return;
                }
                PsiElement[] children = prevElementSuperParent.getChildren();
                if (children == null || children.length == 0) {
                    return;
                }
                PsiElement packageNode = children[0];
                if (packageNode != null) {
                    suggestElementsFromAPackage(parameters, resultSet, packageNode, partialIdentifier, true, true,
                            true, true);
                }
                return;
            }
        }


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

            addKeyword(resultSet, CREATE, CONTEXT_KEYWORD_PRIORITY);

            addOtherCommonKeywords(resultSet);

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
            suggestElementsFromAPackage(parameters, resultSet, lastChild, null, true, true, true, true);
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
    }

    private void addOtherCommonKeywords(CompletionResultSet resultSet) {
        addKeyword(resultSet, IF, CONTEXT_KEYWORD_PRIORITY);
        addKeyword(resultSet, ELSE, CONTEXT_KEYWORD_PRIORITY);

    }

    private void handleActionInvocationNode(CompletionResultSet resultSet, PsiElement parent) {
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
    }

    private void handlePsiErrorElement(CompletionParameters parameters, CompletionResultSet resultSet,
                                       PsiElement element, PsiElement parent, PsiElement parentPrevSibling) {

        PsiFile originalFile = parameters.getOriginalFile();

        // If the current position has a PsiErrorElement, this will be called.
        PsiElement superParent = parent.getParent();
        if (superParent instanceof ExpressionNode || superParent instanceof AnnotationAttributeValueNode
                || superParent instanceof AnnotationAttachmentNode || superParent instanceof SimpleLiteralNode ||
                superParent instanceof ConstantDefinitionNode) {
            return;
        }

        if (superParent instanceof ResourceDefinitionNode || superParent instanceof ServiceBodyNode) {
            addKeyword(resultSet, RESOURCE, CONTEXT_KEYWORD_PRIORITY);
        }

        if (superParent instanceof StatementNode) {

            handleStatementNode(parameters, resultSet);


            //            String text = superParent.getPrevSibling().getText();
            //            if(text.isEmpty() && superParent.getText().equals("IntellijIdeaRulezzz")){
            //                addConnectors(resultSet, element, originalFile);
            //                addFunctions(resultSet, element, originalFile);
            //                addStructs(resultSet, originalFile);
            //                addPackages(resultSet, originalFile);
            //                addVariables(resultSet, element);
            //                addConstants(resultSet, originalFile);
            //            }else{
            //
            //                // If the superParent is StatementNode, add following lookup elements.
            //                addValueTypes(resultSet, VALUE_TYPE_PRIORITY);
            //                addReferenceTypes(resultSet, REFERENCE_TYPE_PRIORITY);
            //
            //                addConnectors(resultSet, element, originalFile);
            //                addFunctions(resultSet, element, originalFile);
            //                addStructs(resultSet, originalFile);
            //                addPackages(resultSet, originalFile);
            //                addVariables(resultSet, element);
            //                addConstants(resultSet, originalFile);
            //
            //                addKeyword(resultSet, IF, CONTEXT_KEYWORD_PRIORITY);
            //                addKeyword(resultSet, ELSE, CONTEXT_KEYWORD_PRIORITY);
            //
            //                ResourceDefinitionNode resourceDefinitionNode =
            //                        PsiTreeUtil.getParentOfType(element, ResourceDefinitionNode.class);
            //                if (resourceDefinitionNode != null) {
            //                    addKeyword(resultSet, REPLY, CONTEXT_KEYWORD_PRIORITY);
            //                }
            //
            //                FunctionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionNode.class);
            //                if (functionNode != null) {
            //                    addKeyword(resultSet, RETURN, CONTEXT_KEYWORD_PRIORITY);
            //                }
            //
            //                TypeMapperNode typeMapperNode =
            //                        PsiTreeUtil.getParentOfType(element, TypeMapperNode.class);
            //                if (typeMapperNode != null) {
            //                    addKeyword(resultSet, RETURN, CONTEXT_KEYWORD_PRIORITY);
            //                }
            //            }


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
    }

    private void handleImportDeclarationNode(CompletionResultSet resultSet, PsiElement element) {
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

    private void handlePackageNameNode(CompletionResultSet resultSet, PsiElement element, PsiElement parent) {
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

    private void addPackageSuggestions(CompletionResultSet resultSet, PsiElement element) {
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
            addPackage(resultSet, directory, insertHandler);
        }
    }

    private void addImportSuggestions(CompletionResultSet resultSet, PsiElement element) {
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
            addPackage(resultSet, directory, insertHandler);
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
     * @param resultSet result set which needs to add the lookup elements
     * @param constants list of constants which needs to be added
     */
    private void addConstants(CompletionResultSet resultSet, List<PsiElement> constants) {
        for (PsiElement constant : constants) {
            LookupElementBuilder builder = LookupElementBuilder.create(constant.getText())
                    .withTypeText("Constant").withIcon(BallerinaIcons.VARIABLE);
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
                    .withTypeText("Function").withTailText("()", true).withIcon(BallerinaIcons.FUNCTION)
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
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsInCurrentPackage(originalFile);
        addFunctions(resultSet, functions);
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
                    .withTypeText("Package").withIcon(BallerinaIcons.PACKAGE)
                    .withInsertHandler(PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY));
        }
    }

    /**
     * Helper method to add packages as lookup elements.
     *
     * @param resultSet     result set which needs to add the lookup elements
     * @param directory     directory which needs to be added as a lookup element
     * @param insertHandler insert handler of the lookup element
     */
    private void addPackage(CompletionResultSet resultSet, PsiDirectory directory,
                            InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.create(directory.getName())
                .withInsertHandler(insertHandler).withTypeText("Package").withIcon(BallerinaIcons.PACKAGE);
        resultSet.addElement(builder);
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
                    .withTypeText("Struct").withIcon(BallerinaIcons.STRUCT);
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
                    .withTypeText("Connector").withIcon(BallerinaIcons.CONNECTOR);
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
                    .withTypeText("Annotation").withIcon(BallerinaIcons.ANNOTATION)
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
                    .withTypeText("Action").withIcon(BallerinaIcons.ACTION)
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
}
