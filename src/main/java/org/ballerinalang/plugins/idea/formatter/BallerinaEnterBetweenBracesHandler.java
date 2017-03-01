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

package org.ballerinalang.plugins.idea.formatter;

import com.intellij.codeInsight.editorActions.enter.EnterBetweenBracesHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.jetbrains.annotations.NotNull;

public class BallerinaEnterBetweenBracesHandler extends EnterBetweenBracesHandler {

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        if (!file.getLanguage().is(BallerinaLanguage.INSTANCE)) {
            return Result.Continue;
        }
        if (isInsideABlock(file, editor)) {
            Project project = file.getProject();
            // Todo - get the spacing from settings
            EditorModificationUtil.insertStringAtCaret(editor, "    ", false, 4);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        }
        return Result.Continue;
    }

    private boolean isInsideABlock(PsiFile file, Editor editor) {
        // Get the offset of the caret
        int offset = editor.getCaretModel().getOffset();
        // Get the element at the offset
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return false;
        }

        // Check whether the previous non whitespace element is '{'. If so, that means the caret is within a block.
        int tempOffset = offset;
        PsiElement tempElement = element;
        while (tempElement instanceof PsiWhiteSpace && tempOffset > 0) {
            tempElement = file.findElementAt(--tempOffset);
        }
        if (tempElement != null && "{".equals(tempElement.getText())) {
            return true;
        }

        // Check whether the next non whitespace element is '}'. If so, that means the caret is within a block.
        tempOffset = offset;
        tempElement = element;
        while (tempElement instanceof PsiWhiteSpace && tempOffset < file.getTextLength()) {
            tempElement = file.findElementAt(++tempOffset);
        }
        if (tempElement != null && "}".equals(tempElement.getText())) {
            return true;
        }
        // Return false if the above conditions are not satisfied.
        return false;
    }

    @Override
    protected boolean isBracePair(char c1, char c2) {
        return c1 == '{' && c2 == '}';
    }
}
