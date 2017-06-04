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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.highlighter.BallerinaSyntaxHighlightingColors;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BallerinaAnnotator implements Annotator {

    private static final String VALID_ESCAPE_CHARACTERS = "\\\\[btnfr\"'\\\\]";
    private static final Pattern VALID_ESCAPE_CHAR_PATTERN = Pattern.compile(VALID_ESCAPE_CHARACTERS);
    private static final String INVALID_ESCAPE_CHARACTERS = "((\\\\\\\\)+|(\\\\[^btnfr\"'\\\\]))|(\\\\(?!.))";
    private static final Pattern INVALID_ESCAPE_CHAR_PATTERN = Pattern.compile(INVALID_ESCAPE_CHARACTERS);

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof NameReferenceNode) {
            annotateNameReferenceNodes(element, holder);
        } else if (element instanceof LeafPsiElement) {
            annotateLeafPsiElementNodes(element, holder);
        } else if (element instanceof ConstantDefinitionNode) {
            annotateConstants(element, holder);
        } else if (element.getParent() instanceof ConstantDefinitionNode) {
            annotateConstants(element.getParent(), holder);
        } else if (element instanceof VariableReferenceNode) {
            annotateVariableReferenceNodes((VariableReferenceNode) element, holder);
        } else if (element instanceof AnnotationDefinitionNode) {
            annotateAnnotationDefinitionNodes((AnnotationDefinitionNode) element, holder);
        } else if (element instanceof ImportDeclarationNode) {
            annotateImportDeclarations(element, holder);
        } else if (element instanceof PackageNameNode) {
            annotatePackageNameNodes(element, holder);
        }
    }

    private void annotateNameReferenceNodes(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getParent() instanceof AnnotationAttachmentNode) {
            Annotation annotation = holder.createInfoAnnotation(element, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
        }
    }

    private void annotateLeafPsiElementNodes(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        IElementType elementType = ((LeafPsiElement) element).getElementType();
        if (elementType == BallerinaTypes.AT && element.getParent() instanceof AnnotationAttachmentNode) {
            Annotation annotation = holder.createInfoAnnotation(element, null);
            annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.ANNOTATION);
        } else if (elementType == BallerinaTypes.QUOTED_STRING) {
            // In here, we annotate escape characters.
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

            matcher = INVALID_ESCAPE_CHAR_PATTERN.matcher(text);
            // Get the start offset of the element.
            startOffset = ((LeafPsiElement) element).getStartOffset();
            // Iterate through each match.
            while (matcher.find()) {
                // Get the matching group.
                String group = matcher.group(3);
                if (group != null) {
                    // Calculate the start and end offsets and create the range.
                    TextRange range = new TextRange(startOffset + matcher.start(),
                            startOffset + matcher.start() + group.length());
                    // Create the annotation.
                    Annotation annotation = holder.createInfoAnnotation(range, "Invalid string escape");
                    annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.INVALID_STRING_ESCAPE);
                }
            }
        } else if (element instanceof IdentifierPSINode) {
            PsiReference reference = element.getReference();
            if (reference == null) {
                return;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return;
            }
            if (resolvedElement.getParent() instanceof ConstantDefinitionNode) {
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.CONSTANT);
            }
        }
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
            if (resolvedElement.getParent() instanceof ConstantDefinitionNode) {
                Annotation annotation = holder.createInfoAnnotation(nameIdentifier, null);
                annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.CONSTANT);
            }
        }
    }

    private void annotateArrayLengthField(@NotNull VariableReferenceNode element, @NotNull AnnotationHolder holder) {
        PsiElement lastChild = element.getLastChild();
        if (lastChild == null) {
            return;
        }
        String text = lastChild.getText();
        if (!"length".equals(text)) {
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
        if (!(parent instanceof VariableDefinitionNode)) {
            return;
        }
        VariableDefinitionNode definitionNode = (VariableDefinitionNode) parent;
        boolean isArrayDefinition = BallerinaPsiImplUtil.isArrayDefinition(definitionNode);
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
        ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(element,
                ImportDeclarationNode.class);
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
        // Create the annotation.
        Annotation annotation = holder.createInfoAnnotation(element.getTextRange(), null);
        annotation.setTextAttributes(BallerinaSyntaxHighlightingColors.PACKAGE);
    }
}
