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
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorBodyNode;
import org.ballerinalang.plugins.idea.psi.ConnectorNode;
import org.jetbrains.annotations.NotNull;

public class BallerinaConnectorContext extends TemplateContextType {

    protected BallerinaConnectorContext() {
        super("BALLERINA_CONNECTOR", "Ballerina Connector");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        if (PsiUtilCore.getLanguageAtOffset(file, offset).isKindOf(BallerinaLanguage.INSTANCE)) {
            PsiElement psiElement = ObjectUtils.notNull(file.findElementAt(offset), file);
            if (psiElement.getParent() instanceof PsiErrorElement) {
                if (psiElement.getParent().getParent() instanceof ConnectorBodyNode) {
                    return true;
                }
            } else if (psiElement instanceof ConnectorBodyNode || psiElement.getParent() instanceof ConnectorNode) {
                return true;
            } else {
                PsiElement parent = psiElement.getParent();
                if(parent instanceof ActionDefinitionNode){
                    return false;
                }
                while (parent != null && !(parent instanceof PsiFile)) {
                    if (parent instanceof ConnectorBodyNode) {
                        return true;
                    }
                    parent = parent.getParent();
                }
            }
        }
        return false;
    }
}
