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

package org.ballerinalang.plugins.idea.codeinsight.daemon.impl;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.highlighter.BallerinaSyntaxHighlightingColors;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BallerinaAnnotator implements Annotator {

    private static final String ESCAPE_CHARACTERS = "\\\\[btnfr\"'\\\\]";
    private static final Pattern ESCAPE_CHAR_PATTERN = Pattern.compile(ESCAPE_CHARACTERS);

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof NameReferenceNode) {
            if (element.getParent() instanceof AnnotationAttachmentNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
            }
        } else if (element instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) element).getElementType();
            if (elementType == BallerinaTypes.AT && element.getParent() instanceof AnnotationAttachmentNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
            } else if (elementType == BallerinaTypes.QUOTED_STRING) {
                // In here, we annotate escape characters.
                String text = element.getText();
                Matcher matcher = ESCAPE_CHAR_PATTERN.matcher(text);
                // Get the start offset of the element.
                int startOffset = ((LeafPsiElement) element).getStartOffset();
                // Iterate through each match.
                while (matcher.find()) {
                    // Get the matching group.
                    String group = matcher.group(0);
                    // Calculate the start and end offsets and create the range.
                    TextRange range = new TextRange(startOffset + matcher.start(),
                            startOffset + matcher.start() + group.length());
                    // Create the annotation.
                    Annotation annotation = holder.createInfoAnnotation(range, null);
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.VALID_STRING_ESCAPE);
                }
            }
        } else if (element instanceof ConstantDefinitionNode) {
            PsiElement nameIdentifier = ((ConstantDefinitionNode) element).getNameIdentifier();
            if (nameIdentifier == null) {
                return;
            }
            Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.CONSTANT);
        } else if (element instanceof VariableReferenceNode) {
            PsiElement nameIdentifier = ((VariableReferenceNode) element).getNameIdentifier();
            if (nameIdentifier == null) {
                return;
            }
            PsiReference[] references = nameIdentifier.getReferences();
            for (PsiReference reference : references) {
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return;
                }
                if (resolvedElement.getParent() instanceof ConstantDefinitionNode) {
                    Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.CONSTANT);
                }
            }
        } else if (element instanceof AnnotationDefinitionNode) {
            PsiElement nameIdentifier = ((AnnotationDefinitionNode) element).getNameIdentifier();
            if (nameIdentifier == null) {
                return;
            }
            Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
        }
    }
}
