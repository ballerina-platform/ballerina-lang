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

package org.ballerinalang.plugins.idea.template;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiUtilCore;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.highlighter.BallerinaSyntaxHighlighter;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnnotationNameNode;
import org.ballerinalang.plugins.idea.psi.AnnotationNode;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BallerinaCodeContextType extends TemplateContextType {

    protected BallerinaCodeContextType(@NotNull @NonNls String id, @NotNull String presentableName,
                                       @Nullable Class<? extends TemplateContextType> baseContextType) {
        super(id, presentableName, baseContextType);
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        if (PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(BallerinaLanguage.INSTANCE)) {
            PsiElement element = file.findElementAt(offset);
            if (element instanceof PsiWhiteSpace) {
                return false;
            }
            return element != null && isInContext(element);
        }

        return false;
    }

    @NotNull
    @Override
    public SyntaxHighlighter createHighlighter() {
        return new BallerinaSyntaxHighlighter();
    }

    protected abstract boolean isInContext(@NotNull PsiElement element);

    public static class File extends BallerinaCodeContextType {

        protected File() {
            super("BALLERINA_FILE", "File", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element.getParent() instanceof PsiErrorElement) {
                if (element.getParent().getParent() instanceof CompilationUnitNode) {
                    return true;
                }
            } else if (element instanceof CompilationUnitNode) {
                return true;
            }
            return false;
        }
    }

    public static class Service extends BallerinaCodeContextType {

        protected Service() {
            super("BALLERINA_SERVICE", "Service", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element.getParent() instanceof PsiErrorElement) {
                if (element.getParent().getParent() instanceof ServiceBodyNode) {
                    return true;
                }
            } else if (element instanceof ServiceBodyNode) {
                return true;
            } else {
                PsiElement parent = element.getParent();
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof ServiceBodyNode) {
                        return true;
                    }
                    parent = parent.getParent();
                    if (parent instanceof AnnotationNameNode || parent instanceof AnnotationNode
                            || parent instanceof ResourceDefinitionNode) {
                        return false;
                    }
                }
            }
            return false;
        }
    }

    public static class Connector extends BallerinaCodeContextType {

        protected Connector() {
            super("BALLERINA_CONNECTOR", "Connector", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            if (element.getParent() instanceof PsiErrorElement) {
                if (element.getParent().getParent() instanceof ConnectorBodyNode) {
                    return true;
                }
            } else if (element instanceof ConnectorBodyNode || element.getParent() instanceof ConnectorNode) {
                return true;
            } else {
                PsiElement parent = element.getParent();
                if (parent instanceof ActionDefinitionNode) {
                    return false;
                }
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof ConnectorBodyNode) {
                        return true;
                    }
                    parent = parent.getParent();
                }
            }
            return false;
        }
    }
}
