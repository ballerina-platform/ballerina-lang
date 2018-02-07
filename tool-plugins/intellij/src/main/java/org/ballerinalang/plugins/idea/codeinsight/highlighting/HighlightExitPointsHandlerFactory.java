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

package org.ballerinalang.plugins.idea.codeinsight.highlighting;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Create exit point handlers.
 */
public class HighlightExitPointsHandlerFactory extends HighlightUsagesHandlerFactoryBase {

    @Override
    public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file,
                                                                   @NotNull PsiElement target) {
        if (!(target instanceof LeafPsiElement)) {
            return null;
        }
        IElementType elementType = ((LeafPsiElement) target).getElementType();
        if (elementType == BallerinaTypes.RETURN || elementType == BallerinaTypes.THROW) {
            return new HighlightExitPointsHandler(editor, file, target);
        }
        return null;
    }
}
