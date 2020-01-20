/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.editor.inserthandlers;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.ballerina.plugins.idea.BallerinaLanguage;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Handles auto completion for the closing quotes.
 */
public class BallerinaQuotesInsertHandler extends TypedHandlerDelegate {

    @NotNull
    @Override
    public Result beforeCharTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file,
                                  @NotNull FileType fileType) {

        if (c != '\"' || !file.getLanguage().is(BallerinaLanguage.INSTANCE)) {
            return Result.CONTINUE;
        }

        // We need to save the file before checking. Otherwise issues can occur when we press enter in a string.
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

        // Checks whether the cursor is already placed inside a ballerina string.
        int caretOff = editor.getCaretModel().getOffset();
        if (isInStringLiteral(file, caretOff)) {
            // If the cursor is already placed inside a string, auto closing shouldn't be triggered.
            char prevChar = editor.getDocument().getText(new TextRange(caretOff - 1, caretOff)).charAt(0);
            char nextChar = editor.getDocument().getText(new TextRange(caretOff, caretOff + 1)).charAt(0);

            if (c == prevChar && c == nextChar) {
                EditorModificationUtil.moveCaretRelatively(editor, 1);
                return Result.STOP;
            } else {
                return Result.CONTINUE;
            }
        } else {
            // Adds the closing quotes and places the cursor in-between the quotes.
            EditorModificationUtil.insertStringAtCaret(editor, "\"", false, 0);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            return Result.CONTINUE;
        }
    }

    private boolean isInStringLiteral(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return false;
        }
        return element.getNode().getElementType() == BallerinaTypes.QUOTED_STRING_LITERAL;
    }
}
