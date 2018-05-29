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

package io.ballerina.plugins.idea.template;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import io.ballerina.plugins.idea.BallerinaLanguage;
import io.ballerina.plugins.idea.highlighting.BallerinaSyntaxHighlighter;
import org.ballerinalang.plugins.idea.psi.BallerinaEndpointDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaGlobalVariableDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaResourceDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaServiceBody;
import org.ballerinalang.plugins.idea.psi.BallerinaSimpleTypeName;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Ballerina context for live templates.
 */
public abstract class BallerinaCodeContextType extends TemplateContextType {

    BallerinaCodeContextType(@NotNull @NonNls String id, @NotNull String presentableName,
                             @Nullable Class<? extends TemplateContextType> baseContextType) {
        super(id, presentableName, baseContextType);
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        if (PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(BallerinaLanguage.INSTANCE)) {
            PsiElement element = file.findElementAt(offset);
            return !(element instanceof PsiWhiteSpace) && element != null && isInContext(element);
        }
        return false;
    }

    @NotNull
    @Override
    public SyntaxHighlighter createHighlighter() {
        return new BallerinaSyntaxHighlighter();
    }

    protected abstract boolean isInContext(@NotNull PsiElement element);

    /**
     * Represents a Ballerina file context.
     */
    public static class File extends BallerinaCodeContextType {

        protected File() {
            super("BALLERINA_FILE", "File", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            BallerinaSimpleTypeName ballerinaReferenceTypeName = PsiTreeUtil.getParentOfType(element,
                    BallerinaSimpleTypeName.class);
            if (ballerinaReferenceTypeName == null) {
                return false;
            }
            BallerinaGlobalVariableDefinition ballerinaGlobalVariableDefinition = PsiTreeUtil.getParentOfType(element,
                    BallerinaGlobalVariableDefinition.class);
            if (ballerinaGlobalVariableDefinition == null) {
                return false;
            }
            return ballerinaReferenceTypeName.equals(ballerinaGlobalVariableDefinition.getFirstChild());
        }
    }

    /**
     * Represents a Ballerina service context.
     */
    public static class Service extends BallerinaCodeContextType {

        protected Service() {
            super("BALLERINA_SERVICE", "Service", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            BallerinaResourceDefinition resourceDefinition = PsiTreeUtil.getParentOfType(element,
                    BallerinaResourceDefinition.class);
            BallerinaEndpointDefinition endpointDefinition = PsiTreeUtil.getParentOfType(element,
                    BallerinaEndpointDefinition.class);
            BallerinaServiceBody serviceDefinition = PsiTreeUtil.getParentOfType(element,
                    BallerinaServiceBody.class);
            if (serviceDefinition != null && endpointDefinition == null) {
                if (resourceDefinition == null || resourceDefinition.getChildren().length == 1) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Represents a Ballerina function context.
     */
    public static class Function extends BallerinaCodeContextType {

        protected Function() {
            super("BALLERINA_FUNCTION", "Function", BallerinaEverywhereContextType.class);
        }

        @Override
        protected boolean isInContext(@NotNull PsiElement element) {
            return false;
        }
    }
}
