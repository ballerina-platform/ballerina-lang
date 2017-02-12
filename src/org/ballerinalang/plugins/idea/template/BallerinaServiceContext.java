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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ObjectUtils;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.AnnotationNameNode;
import org.ballerinalang.plugins.idea.psi.AnnotationNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceBodyNode;
import org.jetbrains.annotations.NotNull;

public class BallerinaServiceContext extends TemplateContextType {

    protected BallerinaServiceContext() {
        super("BALLERINA_SERVICE", "Ballerina Service");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        if (PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(BallerinaLanguage.INSTANCE)) {
            PsiElement psiElement = ObjectUtils.notNull(file.findElementAt(offset), file);
            if (psiElement.getParent() instanceof PsiErrorElement) {
                if (psiElement.getParent().getParent() instanceof ServiceBodyNode) {
                    return true;
                }
            } else if (psiElement instanceof ServiceBodyNode) {
                return true;
            } else {
                PsiElement parent = psiElement.getParent();
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
        }
        return false;
    }
}
