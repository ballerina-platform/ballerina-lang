/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.codeinsight.daemon.impl;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.highlighting.BallerinaSyntaxHighlightingColors;
import io.ballerina.plugins.idea.psi.BallerinaAnnotationAttachment;
import io.ballerina.plugins.idea.psi.BallerinaCompletePackageName;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaGlobalVariableDefinition;
import io.ballerina.plugins.idea.psi.BallerinaNameReference;
import io.ballerina.plugins.idea.psi.BallerinaPackageReference;
import io.ballerina.plugins.idea.psi.BallerinaRecordKey;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import io.ballerina.plugins.idea.psi.BallerinaXmlItem;
import io.ballerina.plugins.idea.psi.reference.BallerinaPackageNameReference;
import org.jetbrains.annotations.NotNull;

/**
 * Handles annotating text in the runtime.
 */
public class BallerinaAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiElement parent = element.getParent();
        if (element instanceof BallerinaNameReference) {
            if (parent instanceof BallerinaAnnotationAttachment) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
                return;
            }
            PsiReference reference = ((BallerinaNameReference) element).getIdentifier().getReference();
            if (reference != null) {
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement != null && resolvedElement
                        .getParent() instanceof BallerinaGlobalVariableDefinition) {
                    Annotation annotation = holder.createInfoAnnotation(element, null);
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.GLOBAL_VARIABLE);
                }
            }
        } else if (element instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) element).getElementType();
            if (elementType == BallerinaTypes.AT) {
                if (parent instanceof BallerinaAnnotationAttachment) {
                    Annotation annotation = holder.createInfoAnnotation(element, null);
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
                }
            } else if (elementType == BallerinaTypes.STRING_TEMPLATE_LITERAL_START
                    || elementType == BallerinaTypes.XML_LITERAL_START) {
                annotateKeyword(element, holder);
                annotateTemplateStart(element, holder);
            } else if (elementType == BallerinaTypes.STRING_TEMPLATE_LITERAL_END
                    || elementType == BallerinaTypes.XML_LITERAL_END) {
                annotateText(element, holder);
            } else if (elementType == BallerinaTypes.STRING_TEMPLATE_TEXT) {
                annotateText(element, holder);
            } else if (elementType == BallerinaTypes.STRING_TEMPLATE_EXPRESSION_START
                    || elementType == BallerinaTypes.XML_TEMPLATE_TEXT
                    || elementType == BallerinaTypes.XML_TAG_EXPRESSION_START
                    || elementType == BallerinaTypes.XML_SINGLE_QUOTED_TEMPLATE_STRING
                    || elementType == BallerinaTypes.XML_DOUBLE_QUOTED_TEMPLATE_STRING
                    || elementType == BallerinaTypes.XML_PI_TEMPLATE_TEXT
                    || elementType == BallerinaTypes.XML_COMMENT_TEMPLATE_TEXT) {
                annotateExpressionTemplateStart(element, holder);
            } else if (elementType == BallerinaTypes.EXPRESSION_END) {
                annotateStringLiteralTemplateEnd(element, holder);
            } else if (elementType == BallerinaTypes.DOCUMENTATION_TEMPLATE_START
                    || elementType == BallerinaTypes.DEPRECATED_TEMPLATE_START) {
                // This uses an overloaded method so that the color can be easily changeable if required.
                annotateKeyword(element, holder, BallerinaSyntaxHighlightingColors.DOCUMENTATION);
            } else if (elementType == BallerinaTypes.DOCUMENTATION_TEMPLATE_ATTRIBUTE_START) {
                // Doc type.
                String msg = null;
                switch (element.getText().charAt(0)) {
                case 'T':
                    msg = "Receiver";
                    break;
                case 'P':
                    msg = "Parameter";
                    break;
                case 'R':
                    msg = "Return Value";
                    break;
                case 'F':
                    msg = "Field";
                    break;
                case 'V':
                    msg = "Variable";
                    break;
                case 'E':
                    msg = "Endpoint";
                    break;
                }
                // Highlight type
                TextRange textRange = element.getTextRange();
                TextRange newTextRange = new TextRange(textRange.getStartOffset(), textRange.getEndOffset() - 2);
                Annotation annotation = holder.createInfoAnnotation(newTextRange, msg);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
                newTextRange = new TextRange(textRange.getEndOffset() - 2, textRange.getEndOffset());
                // Highlight {{
                annotation = holder.createInfoAnnotation(newTextRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.STRING);
            } else if (elementType == BallerinaTypes.DOCUMENTATION_TEMPLATE_ATTRIBUTE_END) {
                annotateText(element, holder);
            } else if (elementType == BallerinaTypes.SINGLE_BACKTICK_CONTENT
                    || elementType == BallerinaTypes.DOUBLE_BACKTICK_CONTENT
                    || elementType == BallerinaTypes.TRIPLE_BACKTICK_CONTENT
                    || elementType == BallerinaTypes.SINGLE_BACK_TICK_DEPRECATED_INLINE_CODE
                    || elementType == BallerinaTypes.DOUBLE_BACK_TICK_DEPRECATED_INLINE_CODE
                    || elementType == BallerinaTypes.TRIPLE_BACK_TICK_DEPRECATED_INLINE_CODE
                    || elementType == BallerinaTypes.SINGLE_BACK_TICK_INLINE_CODE
                    || elementType == BallerinaTypes.DOUBLE_BACK_TICK_INLINE_CODE
                    || elementType == BallerinaTypes.TRIPLE_BACK_TICK_INLINE_CODE) {
                annotateInlineCode(element, holder);
            } else if (elementType == BallerinaTypes.DOCUMENTATION_TEMPLATE_TEXT
                    || elementType == BallerinaTypes.MARKDOWN_DOCUMENTATION_TEXT
                    || elementType == BallerinaTypes.DEPRECATED_TEMPLATE_TEXT) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION);
            } else if (elementType == BallerinaTypes.MARKDOWN_DOCUMENTATION_LINE_START) {
                TextRange textRange = element.getTextRange();
                // Highlights "#"
                int startOffset = textRange.getStartOffset() + ((LeafPsiElement) element).getText().indexOf('#');
                TextRange newTextRange = new TextRange(startOffset, startOffset + 1);
                Annotation annotation = holder.createInfoAnnotation(newTextRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION);
            } else if (elementType == BallerinaTypes.PARAMETER_DOCUMENTATION_START) {
                TextRange textRange = element.getTextRange();
                // Highlights "#"
                int startOffset = textRange.getStartOffset() + ((LeafPsiElement) element).getText().indexOf("#");
                TextRange newTextRange = new TextRange(startOffset, startOffset + 1);
                Annotation annotation = holder.createInfoAnnotation(newTextRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION);
                // Highlights "+"
                startOffset = textRange.getStartOffset() + ((LeafPsiElement) element).getText().indexOf("+");
                newTextRange = new TextRange(startOffset, startOffset + 1);
                annotation = holder.createInfoAnnotation(newTextRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
            } else if (elementType == BallerinaTypes.PARAMETER_NAME
                    || elementType == BallerinaTypes.DESCRIPTION_SEPARATOR) {
                // Highlights input parameter name and "-" token
                TextRange textRange = element.getTextRange();
                Annotation annotation = holder.createInfoAnnotation(textRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
            } else if (elementType == BallerinaTypes.RETURN_PARAMETER_DOCUMENTATION_START) {
                TextRange textRange = element.getTextRange();
                // Highlights "#"
                int startOffset = textRange.getStartOffset() + ((LeafPsiElement) element).getText().indexOf("#");
                TextRange newTextRange = new TextRange(startOffset, startOffset + 1);
                Annotation annotation = holder.createInfoAnnotation(newTextRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION);
                // Highlights "+"
                startOffset = textRange.getStartOffset() + ((LeafPsiElement) element).getText().indexOf("+");
                newTextRange = new TextRange(startOffset, startOffset + 1);
                annotation = holder.createInfoAnnotation(newTextRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
                // Highlights "return" and "-"
                newTextRange = new TextRange(
                        textRange.getStartOffset() + ((LeafPsiElement) element).getText().indexOf("return"),
                        textRange.getEndOffset());
                annotation = holder.createInfoAnnotation(newTextRange, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
            } else if (elementType == BallerinaTypes.SINGLE_BACKTICK_MARKDOWN_START
                    || elementType == BallerinaTypes.DOUBLE_BACKTICK_MARKDOWN_START
                    || elementType == BallerinaTypes.TRIPLE_BACKTICK_MARKDOWN_START
                    || elementType == BallerinaTypes.SINGLE_BACKTICK_MARKDOWN_END
                    || elementType == BallerinaTypes.DOUBLE_BACKTICK_MARKDOWN_END
                    || elementType == BallerinaTypes.TRIPLE_BACKTICK_MARKDOWN_END) {
                annotateInlineCode(element, holder);
            } else if (elementType == BallerinaTypes.IDENTIFIER) {
                if (parent.getNode().getElementType() == BallerinaTypes.DOCUMENTATION_TEMPLATE_ATTRIBUTE_DESCRIPTION) {
                    Annotation annotation = holder.createInfoAnnotation(element, null);
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
                } else if (parent instanceof BallerinaGlobalVariableDefinition) {
                    Annotation annotation = holder.createInfoAnnotation(element, null);
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.GLOBAL_VARIABLE);
                } else if ("self".equals(element.getText())) {
                    BallerinaTypeDefinition typeDefinition = PsiTreeUtil
                            .getParentOfType(element, BallerinaTypeDefinition.class);
                    BallerinaFunctionDefinition functionDefinition = PsiTreeUtil
                            .getParentOfType(element, BallerinaFunctionDefinition.class);
                    if (typeDefinition == null && (functionDefinition == null
                            || functionDefinition.getAttachedObject() == null)) {
                        return;
                    }
                    Annotation annotation = holder.createInfoAnnotation(element, null);
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.KEYWORD);
                }
            }
        } else if (element instanceof BallerinaPackageReference) {
            PsiReference reference = element.getReference();
            if (!(element.getParent().getParent() instanceof BallerinaAnnotationAttachment)
                    && reference instanceof BallerinaPackageNameReference) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.PACKAGE);
            }
        } else if (element instanceof BallerinaCompletePackageName) {
            Annotation annotation = holder.createInfoAnnotation(element, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.PACKAGE);
        } else if (element instanceof BallerinaXmlItem) {
            Annotation annotation = holder.createInfoAnnotation(element, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.STRING);
        } else if (element instanceof BallerinaRecordKey) {
            // Todo - Need to highlight key?
            //            Annotation annotation = holder.createInfoAnnotation(element, null);
            //            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.RECORD_KEY);
        }
    }

    private void annotateTemplateStart(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        TextRange textRange = element.getTextRange();
        TextRange newTextRange = new TextRange(textRange.getEndOffset() - 1, textRange.getEndOffset());
        Annotation annotation = holder.createInfoAnnotation(newTextRange, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.STRING);
    }

    private void annotateInlineCode(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Annotation annotation = holder.createInfoAnnotation(element, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
    }

    private void annotateKeyword(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        TextRange textRange = element.getTextRange();
        TextRange newTextRange = new TextRange(textRange.getStartOffset(), textRange.getEndOffset() - 1);
        Annotation annotation = holder.createInfoAnnotation(newTextRange, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.KEYWORD);
    }

    private void annotateKeyword(@NotNull PsiElement element, @NotNull AnnotationHolder holder,
            @NotNull TextAttributesKey textAttributesKey) {
        TextRange textRange = element.getTextRange();
        TextRange newTextRange = new TextRange(textRange.getStartOffset(), textRange.getEndOffset() - 1);
        Annotation annotation = holder.createInfoAnnotation(newTextRange, null);
        annotation.setTextAttributes(textAttributesKey);
    }

    private void annotateExpressionTemplateStart(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        TextRange textRange = element.getTextRange();
        TextRange newTextRange = new TextRange(textRange.getEndOffset() - 2, textRange.getEndOffset());
        Annotation annotation = holder.createInfoAnnotation(newTextRange, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.TEMPLATE_LANGUAGE_COLOR);
        if (textRange.getEndOffset() - 2 > textRange.getStartOffset()) {
            newTextRange = new TextRange(textRange.getStartOffset(), textRange.getEndOffset() - 2);
            annotation = holder.createInfoAnnotation(newTextRange, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.STRING);
        }
    }

    private void annotateStringLiteralTemplateEnd(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Annotation annotation = holder.createInfoAnnotation(element, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.TEMPLATE_LANGUAGE_COLOR);
    }

    private void annotateText(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Annotation annotation = holder.createInfoAnnotation(element, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.STRING);
    }
}
