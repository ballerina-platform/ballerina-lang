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

    public static final int KEYWORD_PRIORITY = 20;
    public static final int CONTEXT_KEYWORD_PRIORITY = 25;
    public static final int VALUE_TYPE_PRIORITY = 30;
    public static final int REFERENCE_TYPE_PRIORITY = 30;
    public static final int PACKAGE_PRIORITY = 35;
    public static final int CONNECTOR_PRIORITY = 35;
    public static final int ANNOTATION_PRIORITY = 35;
    public static final int FUNCTION_PRIORITY = 40;
    public static final int STRUCT_PRIORITY = 35;
    public static final int VARIABLE_PRIORITY = 35;
    public static final int ACTION_PRIORITY = 40;
    public static final int FIELD_PRIORITY = 40;

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
    static LookupElementBuilder createLookupElement(@NotNull String name,
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
                                                           @Nullable InsertHandler<LookupElement>
                                                                   insertHandler) {
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
                                                              @Nullable InsertHandler<LookupElement>
                                                                      insertHandler) {
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
                                                                 @Nullable InsertHandler<LookupElement>
                                                                         insertHandler) {
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
     * @param parameters parameters which passed to completion contributor
     * @param resultSet  result list which is used to add lookups
     * @param offset     offset of  the current element
     * @param iNode      lookup element adding strategy if the previous element is an identifier node
     * @param lNode      lookup element adding strategy if the previous element is a leaf node
     * @param oNode      lookup element adding strategy if the previous element is not an identifier or leaf node
     */
    static void checkPrevNodeAndHandle(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet, int offset,
                                       CompletionInterface<CompletionParameters, CompletionResultSet, PsiElement> iNode,
                                       CompletionInterface<CompletionParameters, CompletionResultSet, PsiElement> lNode,
                                       CompletionInterface<CompletionParameters, CompletionResultSet, PsiElement>
                                               oNode) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement prevElement = getPreviousNonEmptyElement(originalFile, offset);
        if (prevElement instanceof IdentifierPSINode && iNode != null) {
            iNode.call(parameters, resultSet, prevElement);
        } else if (prevElement instanceof LeafPsiElement && lNode != null) {
            lNode.call(parameters, resultSet, prevElement);
        } else {
            if (oNode != null) {
                oNode.call(parameters, resultSet, prevElement);
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
    static PsiElement getPreviousNonEmptyElement(PsiFile originalFile, int offset) {
        int count = 1;
        PsiElement prevElement = originalFile.findElementAt(offset - count++);
        while (prevElement instanceof PsiWhiteSpace) {
            prevElement = originalFile.findElementAt(offset - count++);
        }
        return prevElement;
    }
}
