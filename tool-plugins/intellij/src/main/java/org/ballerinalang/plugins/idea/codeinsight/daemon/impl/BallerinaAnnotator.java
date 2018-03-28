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
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.highlighter.BallerinaSyntaxHighlightingColors;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttributeValueNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnnotationReferenceNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DeprecatedTextNode;
import org.ballerinalang.plugins.idea.psi.DocumentationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.DocumentationTemplateAttributeDescriptionNode;
import org.ballerinalang.plugins.idea.psi.DoubleBackTickDeprecatedInlineCodeNode;
import org.ballerinalang.plugins.idea.psi.DoubleBackTickInlineCodeNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.IntegerLiteralNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.SimpleLiteralNode;
import org.ballerinalang.plugins.idea.psi.SingleBackTickDeprecatedInlineCodeNode;
import org.ballerinalang.plugins.idea.psi.SingleBackTickDocInlineCodeNode;
import org.ballerinalang.plugins.idea.psi.TripleBackTickDeprecatedInlineCodeNode;
import org.ballerinalang.plugins.idea.psi.TripleBackTickInlineCodeNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.XmlAttribNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.psi.references.RecordKeyReference;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles annotating text in the runtime.
 */
public class BallerinaAnnotator implements Annotator {

    private static final String VALID_ESCAPE_CHARACTERS = "\\\\[btnfr\"'\\\\]|\\\\u[0-f]{4}|\\\\[0-3][0-7]{2}" +
            "|\\\\[0-7]{1,2}";
    private static final Pattern VALID_ESCAPE_CHAR_PATTERN = Pattern.compile(VALID_ESCAPE_CHARACTERS);
    private static final String INVALID_ESCAPE_CHARACTERS = "((\\\\\\\\)+|(\\\\([^btnfru\"'\\\\0-7]" +
            "|(u[0-f]{0,3}[^0-f]))))|(\\\\(?!.))";
    private static final Pattern INVALID_ESCAPE_CHAR_PATTERN = Pattern.compile(INVALID_ESCAPE_CHARACTERS);

    private static final String PATH_PARAMETERS = "(?<!=)(\\{(\\w+?)})";
    private static final Pattern PATH_PARAMETERS_PATTERN = Pattern.compile(PATH_PARAMETERS);

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        PsiElement parent = element.getParent();
        if (element instanceof AnnotationReferenceNode) {
            annotateNameReferenceNodes(element, holder);
        } else if (element instanceof LeafPsiElement) {
            annotateLeafPsiElementNodes(element, holder);
        } else if (element instanceof ConstantDefinitionNode) {
            annotateConstants(element, holder);
        } else if (parent instanceof ConstantDefinitionNode) {
            annotateConstants(parent, holder);
        } else if (element instanceof IntegerLiteralNode) {
            annotateInteger(parent, holder);
        } else if (element instanceof VariableReferenceNode) {
            annotateVariableReferenceNodes((VariableReferenceNode) element, holder);
        } else if (element instanceof AnnotationDefinitionNode) {
            annotateAnnotationDefinitionNodes((AnnotationDefinitionNode) element, holder);
        } else if (element instanceof ImportDeclarationNode) {
            annotateImportDeclarations(element, holder);
        } else if (element instanceof PackageNameNode) {
            annotatePackageNameNodes(element, holder);
        } else if (element instanceof GlobalVariableDefinitionNode) {
            annotateGlobalVariable(element, holder);
        } else if (parent instanceof GlobalVariableDefinitionNode) {
            annotateGlobalVariable(parent, holder);
        } else if (parent instanceof DocumentationAttachmentNode || parent instanceof DeprecatedTextNode) {
            // Highlight documentations.
            annotateDocumentation(parent, holder);
        } else if (element instanceof SingleBackTickDocInlineCodeNode || element instanceof DoubleBackTickInlineCodeNode
                || element instanceof TripleBackTickInlineCodeNode
                || element instanceof SingleBackTickDeprecatedInlineCodeNode
                || element instanceof DoubleBackTickDeprecatedInlineCodeNode
                || element instanceof TripleBackTickDeprecatedInlineCodeNode) {
            // Highlighting inline codes in comments.
            annotateInlineCode(element, holder);
        }
    }

    private void annotateNameReferenceNodes(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Annotation annotation = holder.createInfoAnnotation(element, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
    }

    private void annotateLeafPsiElementNodes(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        IElementType elementType = ((LeafPsiElement) element).getElementType();
        PsiElement parentElement = element.getParent();
        if (elementType == BallerinaTypes.AT && parentElement instanceof AnnotationAttachmentNode) {
            Annotation annotation = holder.createInfoAnnotation(element, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
        } else if (elementType == BallerinaTypes.QUOTED_STRING) {
            // In here, we annotate valid escape characters.
            String text = element.getText();
            Matcher matcher = VALID_ESCAPE_CHAR_PATTERN.matcher(text);
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

            // Annotate invalid escape characters.
            matcher = INVALID_ESCAPE_CHAR_PATTERN.matcher(text);
            // Get the start offset of the element.
            startOffset = ((LeafPsiElement) element).getStartOffset();
            // Iterate through each match.
            while (matcher.find()) {
                // Get the matching group.
                String group = matcher.group(3);
                if (group != null) {
                    // Calculate the start and end offsets and create the range.
                    TextRange range = new TextRange(startOffset + matcher.start(3),
                            startOffset + matcher.start(3) + group.length());
                    // Create the annotation.
                    Annotation annotation = holder.createInfoAnnotation(range, "Invalid string escape");
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.INVALID_STRING_ESCAPE);
                }
            }

            AnnotationAttributeNode annotationAttributeNode = PsiTreeUtil.getParentOfType(element,
                    AnnotationAttributeNode.class);
            boolean canHighlightParameters = canHighlightParameters(element);
            if (canHighlightParameters && annotationAttributeNode != null) {
                // Annotate query parameters in annotation attachments.
                matcher = PATH_PARAMETERS_PATTERN.matcher(text);
                // Get the start offset of the element.
                startOffset = ((LeafPsiElement) element).getStartOffset();
                // Iterate through each match.
                while (matcher.find()) {
                    // Get the matching value without the enclosing {}.
                    String value = matcher.group(2);
                    if (value == null) {
                        continue;
                    }
                    // Calculate the start and end offsets and create the range. We need to add 2 to include the
                    // {} ignored.
                    TextRange range = new TextRange(startOffset + matcher.start(1),
                            startOffset + matcher.start(1) + value.length() + 2);
                    // Check whether a matching resource parameter is available.
                    boolean isMatchAvailable = isMatchingParamAvailable(annotationAttributeNode, value);
                    // Create the annotation.
                    if (isMatchAvailable) {
                        Annotation annotation = holder.createInfoAnnotation(range,
                                "Path parameter '" + value + "'");
                        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.TEMPLATE_LANGUAGE_COLOR);
                    } else {
                        Annotation annotation = holder.createErrorAnnotation(range,
                                "Path parameter '" + value + "' not found in the resource signature");
                        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.INVALID_STRING_ESCAPE);
                    }
                }
            }
        } else if (elementType == BallerinaTypes.STRING_TEMPLATE_LITERAL_START
                || elementType == BallerinaTypes.XML_START) {
            annotateKeyword(element, holder);
        } else if (elementType == BallerinaTypes.DOCUMENTATION_TEMPLATE_START ||
                elementType == BallerinaTypes.DEPRECATED_TEMPLATE_START) {
            // This uses an overloaded method so that the color can be easily changeable if required.
            annotateKeyword(element, holder, BallerinaSyntaxHighlightingColors.KEYWORD);
        } else if (elementType == BallerinaTypes.STRING_TEMPLATE_EXPRESSION_START
                || elementType == BallerinaTypes.XML_EXPRESSION_START) {
            annotateExpressionTemplateStart(element, holder);
        } else if (elementType == BallerinaTypes.STRING_TEMPLATE_TEXT || elementType == BallerinaTypes.XML_TEXT) {
            annotateText(element, holder);
        } else if (elementType == BallerinaTypes.EXPRESSION_END) {
            annotateStringLiteralTemplateEnd(element, holder);
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
            }
            TextRange textRange = element.getTextRange();
            TextRange newTextRange = new TextRange(textRange.getStartOffset(), textRange.getEndOffset() - 2);
            Annotation annotation = holder.createInfoAnnotation(newTextRange, msg);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
        } else if (element instanceof IdentifierPSINode) {
            if (parentElement.getParent() instanceof AnnotationAttachmentNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
                return;
            }
            if (parentElement instanceof DocumentationTemplateAttributeDescriptionNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
            }
            PsiReference reference = element.getReference();
            if (reference == null || reference instanceof RecordKeyReference) {
                return;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return;
            }
            PsiElement parent = resolvedElement.getParent();
            if (parent instanceof ConstantDefinitionNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.CONSTANT);
            } else if (parent instanceof GlobalVariableDefinitionNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.GLOBAL_VARIABLE);
            }
        }
    }

    /**
     * Checks whether the query,path parameters can be highlighted in the given element.
     *
     * @param element element which needs to be checked
     * @return {@code true} if parameters can be highlighted, {@code false} otherwise.
     */
    private boolean canHighlightParameters(@NotNull PsiElement element) {
        AnnotationAttributeNode annotationAttributeNode = PsiTreeUtil.getParentOfType(element,
                AnnotationAttributeNode.class);
        if (annotationAttributeNode == null) {
            return false;
        }
        AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(annotationAttributeNode,
                AnnotationAttachmentNode.class);
        if (annotationAttachmentNode == null) {
            return false;
        }
        AnnotationReferenceNode annotationReferenceNode = PsiTreeUtil.getChildOfType(annotationAttachmentNode,
                AnnotationReferenceNode.class);
        if (annotationReferenceNode == null) {
            return false;
        }
        IdentifierPSINode annotationName = PsiTreeUtil.getChildOfType(annotationReferenceNode, IdentifierPSINode.class);
        return annotationName != null && "resourceConfig".equals(annotationName.getText());
    }

    private boolean isMatchingParamAvailable(@NotNull AnnotationAttributeNode annotationAttributeNode,
                                             @NotNull String value) {
        ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(annotationAttributeNode,
                ResourceDefinitionNode.class);
        if (resourceDefinitionNode == null) {
            return false;
        }
        ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(resourceDefinitionNode,
                ParameterListNode.class);
        if (parameterListNode == null) {
            return false;
        }
        ParameterNode[] parameterNodes = PsiTreeUtil.getChildrenOfType(parameterListNode, ParameterNode.class);
        if (parameterNodes == null) {
            return false;
        }
        for (ParameterNode parameterNode : parameterNodes) {
            AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getChildOfType(parameterNode,
                    AnnotationAttachmentNode.class);
            if (annotationAttachmentNode == null) {
                continue;
            }
            AnnotationReferenceNode annotationReferenceNode = PsiTreeUtil.getChildOfType(annotationAttachmentNode,
                    AnnotationReferenceNode.class);
            if (annotationReferenceNode == null) {
                continue;
            }
            PsiElement paramType = annotationReferenceNode.getNameIdentifier();
            if (paramType == null) {
                continue;
            }
            if (!"PathParam".equals(paramType.getText())) {
                continue;
            }
            Collection<AnnotationAttributeValueNode> annotationAttributeValueNodes =
                    PsiTreeUtil.findChildrenOfType(annotationAttachmentNode, AnnotationAttributeValueNode.class);
            for (AnnotationAttributeValueNode annotationAttributeValueNode : annotationAttributeValueNodes) {
                SimpleLiteralNode simpleLiteralNode = PsiTreeUtil.getChildOfType(annotationAttributeValueNode,
                        SimpleLiteralNode.class);
                if (simpleLiteralNode == null || simpleLiteralNode.getFirstChild() == null) {
                    continue;
                }
                PsiElement firstChild = simpleLiteralNode.getFirstChild();
                if (!(firstChild instanceof LeafPsiElement)) {
                    continue;
                }
                if (((LeafPsiElement) firstChild).getElementType() != BallerinaTypes.QUOTED_STRING) {
                    continue;
                }
                String text = firstChild.getText();
                text = text.substring(1, text.length() - 1);
                if (value.equals(text)) {
                    return true;
                }
            }
        }
        // At this point, match might not be found for query params since the annotation attachment for the parameter
        // is optional. So we need to go through all the parameters and check the identifiers of parameters which
        // does not have an annotation attachments.
        for (ParameterNode parameterNode : parameterNodes) {
            AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getChildOfType(parameterNode,
                    AnnotationAttachmentNode.class);
            if (annotationAttachmentNode != null) {
                continue;
            }
            PsiElement nameIdentifier = parameterNode.getNameIdentifier();
            if (nameIdentifier == null) {
                continue;
            }
            if (value.equals(nameIdentifier.getText())) {
                return true;
            }
        }
        return false;
    }

    private void annotateConstants(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        ValueTypeNameNode valueTypeNameNode = PsiTreeUtil.findChildOfType(element, ValueTypeNameNode.class);
        if (valueTypeNameNode == null || valueTypeNameNode.getText().isEmpty()) {
            return;
        }
        PsiElement nameIdentifier = ((ConstantDefinitionNode) element).getNameIdentifier();
        if (nameIdentifier == null) {
            return;
        }
        Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.CONSTANT);
    }

    private void annotateInteger(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Annotation annotation = holder.createInfoAnnotation(element, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.NUMBER);
    }

    private void annotateGlobalVariable(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        ValueTypeNameNode valueTypeNameNode = PsiTreeUtil.findChildOfType(element, ValueTypeNameNode.class);
        if (valueTypeNameNode == null || valueTypeNameNode.getText().isEmpty()) {
            return;
        }
        PsiElement nameIdentifier = ((GlobalVariableDefinitionNode) element).getNameIdentifier();
        if (nameIdentifier == null) {
            return;
        }
        Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.GLOBAL_VARIABLE);
    }

    private void annotateDocumentation(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Annotation annotation = holder.createInfoAnnotation(element, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION);
    }

    private void annotateInlineCode(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        Annotation annotation;
        if (element instanceof SingleBackTickDocInlineCodeNode
                || element instanceof SingleBackTickDeprecatedInlineCodeNode) {
            TextRange currentTextRange = element.getTextRange();
            TextRange newTextRange = new TextRange(currentTextRange.getStartOffset() + 1,
                    currentTextRange.getEndOffset() - 1);
            annotation = holder.createInfoAnnotation(newTextRange, null);
        } else {
            annotation = holder.createInfoAnnotation(element, null);
        }
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.DOCUMENTATION_INLINE_CODE);
    }

    private void annotateVariableReferenceNodes(@NotNull VariableReferenceNode element,
                                                @NotNull AnnotationHolder holder) {
        PsiElement nameIdentifier = element.getNameIdentifier();
        if (nameIdentifier == null) {
            annotateArrayLengthField(element, holder);
            return;
        }
        PsiReference[] references = nameIdentifier.getReferences();
        for (PsiReference reference : references) {
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return;
            }
            PsiElement parent = resolvedElement.getParent();
            if (parent instanceof ConstantDefinitionNode) {
                Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.CONSTANT);
            }
            if (parent instanceof GlobalVariableDefinitionNode) {
                Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.GLOBAL_VARIABLE);
            }
        }
    }

    private void annotateArrayLengthField(@NotNull VariableReferenceNode element, @NotNull AnnotationHolder holder) {
        PsiElement lastChild = element.getLastChild();
        if (lastChild == null) {
            return;
        }
        String text = lastChild.getText();
        if (!".length".equals(text)) {
            return;
        }
        PsiElement firstChild = element.getFirstChild();
        if (firstChild == null) {
            return;
        }
        PsiFile containingFile = element.getContainingFile();
        if (containingFile == null) {
            return;
        }
        PsiReference reference = containingFile.findReferenceAt(firstChild.getTextOffset());
        if (reference == null) {
            return;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return;
        }
        PsiElement parent = resolvedElement.getParent();
        if (!(parent instanceof VariableDefinitionNode || parent instanceof ParameterNode
                || parent instanceof GlobalVariableDefinitionNode)) {
            return;
        }
        boolean isArrayDefinition = BallerinaPsiImplUtil.isArrayDefinition(parent);
        if (isArrayDefinition) {
            Annotation annotation = holder.createInfoAnnotation(lastChild, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.STATIC_FIELD);
        }
    }

    private void annotateAnnotationDefinitionNodes(@NotNull AnnotationDefinitionNode element,
                                                   @NotNull AnnotationHolder holder) {
        PsiElement nameIdentifier = element.getNameIdentifier();
        if (nameIdentifier == null) {
            return;
        }
        Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
    }


    private void annotateImportDeclarations(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        AliasNode aliasNode = PsiTreeUtil.findChildOfType(element, AliasNode.class);
        if (aliasNode != null) {
            // Create the annotation.
            Annotation annotation = holder.createInfoAnnotation(aliasNode.getTextRange(), null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.PACKAGE);
        } else {
            Collection<PackageNameNode> packageNameNodes = PsiTreeUtil.findChildrenOfType(element,
                    PackageNameNode.class);
            if (!packageNameNodes.isEmpty()) {
                PackageNameNode lastPackageName =
                        (PackageNameNode) packageNameNodes.toArray()[packageNameNodes.size() - 1];
                // Create the annotation.
                Annotation annotation = holder.createInfoAnnotation(lastPackageName.getTextRange(), null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.PACKAGE);
            }
        }
    }

    private void annotatePackageNameNodes(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        PsiElement parent = element.getParent();
        if (parent != null) {
            PsiElement superParent = parent.getParent();
            if (superParent != null && superParent instanceof AnnotationAttachmentNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
                return;
            }
        }

        ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(element, ImportDeclarationNode.class);
        if (importDeclarationNode != null) {
            return;
        }
        PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.getParentOfType(element,
                PackageDeclarationNode.class);
        if (packageDeclarationNode != null) {
            return;
        }
        AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(element,
                AnnotationAttachmentNode.class);
        if (annotationAttachmentNode != null) {
            return;
        }
        XmlAttribNode xmlAttribNode = PsiTreeUtil.getParentOfType(element, XmlAttribNode.class);
        if (xmlAttribNode != null) {
            return;
        }
        // Create the annotation.
        Annotation annotation = holder.createInfoAnnotation(element.getTextRange(), null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.PACKAGE);
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
