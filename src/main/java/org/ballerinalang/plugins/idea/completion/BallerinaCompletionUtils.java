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

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BallerinaCompletionUtils {

    static final int KEYWORD_PRIORITY = 20;
    static final int CONTEXT_KEYWORD_PRIORITY = 25;
    static final int VALUE_TYPE_PRIORITY = 30;
    static final int REFERENCE_TYPE_PRIORITY = 30;
    static final int PACKAGE_PRIORITY = 35;
    static final int CONNECTOR_PRIORITY = 35;
    static final int ANNOTATION_PRIORITY = 35;
    static final int FUNCTION_PRIORITY = 40;
    static final int STRUCT_PRIORITY = 35;
    static final int VARIABLE_PRIORITY = 35;
    static final int ACTION_PRIORITY = 40;
    static final int FIELD_PRIORITY = 40;

    private BallerinaCompletionUtils() {

    }

    /**
     * Creates a lookup element.
     *
     * @param name          name of the lookup
     * @param insertHandler insert handler of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    private static LookupElementBuilder createLookupElement(@NotNull String name,
                                                            @Nullable InsertHandler<LookupElement> insertHandler) {
        return LookupElementBuilder.create(name).withBoldness(true).withInsertHandler(insertHandler);
    }

    /**
     * Creates a keyword lookup element.
     *
     * @param name          name of the lookup
     * @param insertHandler insert handler of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    static LookupElementBuilder createKeywordLookupElement(@NotNull String name,
                                                           @Nullable InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, insertHandler);
    }

    /**
     * Creates a <b>Simple Type</b> lookup element.
     *
     * @param name          name of the lookup
     * @param insertHandler insert handler of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    static LookupElementBuilder createSimpleTypeLookupElement(@NotNull String name,
                                                              @Nullable InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, insertHandler).withTypeText("Simple Type", true);
    }

    /**
     * Creates a <b>Reference Type</b> lookup element.
     *
     * @param name          name of the lookup
     * @param insertHandler insert handler of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    static LookupElementBuilder createReferenceTypeLookupElement(@NotNull String name,
                                                                 @Nullable InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, insertHandler).withTypeText("Reference Type", true);
    }

    /**
     * Identify the annotation attachment type of the given definition node.
     *
     * @param definitionNode a definition node which we want to check
     * @return annotation attachment type.
     */
    static String getAnnotationAttachmentType(PsiElement definitionNode) {
        String type = null;
        if (definitionNode instanceof ServiceDefinitionNode) {
            type = "service";
        } else if (definitionNode instanceof FunctionNode) {
            type = "function";
        } else if (definitionNode instanceof ConnectorNode) {
            type = "connector";
        } else if (definitionNode instanceof StructDefinitionNode) {
            type = "struct";
        } else if (definitionNode instanceof TypeMapperNode) {
            type = "typemapper";
        } else if (definitionNode instanceof ConstantDefinitionNode) {
            type = "const";
        } else if (definitionNode instanceof AnnotationDefinitionNode) {
            type = "annotation";
        } else if (definitionNode instanceof ResourceDefinitionNode) {
            type = "resource";
        } else if (definitionNode instanceof ActionDefinitionNode) {
            type = "action";
        }
        return type;
    }

    /**
     * Handles situations where we want to consider previous node content before adding lookup elements to the
     * current node.
     *
     * @param parameters    parameters which passed to completion contributor
     * @param resultSet     result list which is used to add lookups
     * @param offset        offset of  the current element
     * @param iNodeStrategy lookup element adding strategy if the previous element is an identifier node
     * @param lNodeStrategy lookup element adding strategy if the previous element is a leaf node
     * @param oNodeStrategy lookup element adding strategy if the previous element is not an identifier or leaf node
     */
    static void checkPrevNodeAndHandle(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet, int offset,
                                       Strategy<CompletionParameters, CompletionResultSet, PsiElement> iNodeStrategy,
                                       Strategy<CompletionParameters, CompletionResultSet, PsiElement> lNodeStrategy,
                                       Strategy<CompletionParameters, CompletionResultSet, PsiElement> oNodeStrategy) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement prevElement = getPreviousNonEmptyElement(originalFile, offset);
        if (prevElement instanceof IdentifierPSINode && iNodeStrategy != null) {
            iNodeStrategy.execute(parameters, resultSet, prevElement);
        } else if (prevElement instanceof LeafPsiElement && lNodeStrategy != null) {
            lNodeStrategy.execute(parameters, resultSet, prevElement);
        } else {
            if (oNodeStrategy != null) {
                oNodeStrategy.execute(parameters, resultSet, prevElement);
            }
        }
    }

    /**
     * Returns the non empty element which is a previous sibling of the current node.
     *
     * @param originalFile file which contains the current element
     * @param offset       offset of the current node
     * @return {@code null} if there is no previous non empty node. Otherwise returns the corresponding
     * {@link PsiElement} node.
     */
    public static PsiElement getPreviousNonEmptyElement(PsiFile originalFile, int offset) {
        int count = 1;
        PsiElement prevElement = originalFile.findElementAt(offset - count++);
        while (prevElement instanceof PsiWhiteSpace) {
            prevElement = originalFile.findElementAt(offset - count++);
        }
        return prevElement;
    }

    /**
     * Identifies whether the given element type is an expression separator element type.
     * <p>
     * Eg: +, -, /, *, <, >, >=, <=, ==, !=, &&, ||, =
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an expression separator element type, {@code false}
     * otherwise.
     */
    public static boolean isExpressionSeparator(IElementType elementType) {
        return isArithmeticOperator(elementType) || isRelationalOperator(elementType)
                || isLogicalOperator(elementType) || isAssignmentOperator(elementType);
    }

    /**
     * Identifies whether the given element type is an arithmetic operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an arithmetic operator type, {@code false} otherwise.
     */
    private static boolean isArithmeticOperator(IElementType elementType) {
        return isAdditiveOperator(elementType) || isMultiplicativeOperator(elementType);
    }

    /**
     * Identifies whether the given element type is an additive operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an additive operator type, {@code false} otherwise.
     */
    private static boolean isAdditiveOperator(IElementType elementType) {
        return elementType == BallerinaTypes.ADD || elementType == BallerinaTypes.SUB;
    }

    /**
     * Identifies whether the given element type is a multiplicative operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a multiplicative operator type, {@code false} otherwise.
     */
    private static boolean isMultiplicativeOperator(IElementType elementType) {
        return elementType == BallerinaTypes.MUL || elementType == BallerinaTypes.DIV ||
                elementType == BallerinaTypes.MOD;
    }

    /**
     * Identifies whether the given element type is a relational operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a relational operator type, {@code false} otherwise.
     */
    private static boolean isRelationalOperator(IElementType elementType) {
        return isComparisonOperator(elementType) || isEqualityOperator(elementType);
    }

    /**
     * Identifies whether the given element type is a comparison operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a comparison operator type, {@code false} otherwise.
     */
    private static boolean isComparisonOperator(IElementType elementType) {
        return elementType == BallerinaTypes.LE || elementType == BallerinaTypes.GE ||
                elementType == BallerinaTypes.GT || elementType == BallerinaTypes.LT;
    }

    /**
     * Identifies whether the given element type is an equality operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an equality operator type, {@code false} otherwise.
     */
    private static boolean isEqualityOperator(IElementType elementType) {
        return elementType == BallerinaTypes.EQUAL || elementType == BallerinaTypes.NOTEQUAL;
    }

    /**
     * Identifies whether the given element type is a logical operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a logical operator type, {@code false} otherwise.
     */
    private static boolean isLogicalOperator(IElementType elementType) {
        return elementType == BallerinaTypes.AND || elementType == BallerinaTypes.OR;
    }

    /**
     * Identifies whether the given element type is an assignment operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an assignment operator type, {@code false} otherwise.
     */
    private static boolean isAssignmentOperator(IElementType elementType) {
        return elementType == BallerinaTypes.ASSIGN;
    }
}
