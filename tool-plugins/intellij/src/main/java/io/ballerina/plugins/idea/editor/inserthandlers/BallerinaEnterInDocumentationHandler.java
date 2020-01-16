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

package io.ballerina.plugins.idea.editor.inserthandlers;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import io.ballerina.plugins.idea.BallerinaLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the enter key press in braces.
 */
public class BallerinaEnterInDocumentationHandler extends EnterHandlerDelegateAdapter {

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
        if (!file.getLanguage().is(BallerinaLanguage.INSTANCE)) {
            return Result.Continue;
        }
        // We need to save the file before checking. Otherwise issues can occur when we press enter in a string.
        Project project = file.getProject();
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

        // Checks whether the previous line starts with "#".
        LogicalPosition caretPos = editor.getCaretModel().getLogicalPosition();
        int prevLine = caretPos.line - 1;
        String lineString = editor.getDocument().getText(
                new TextRange(editor.getDocument().getLineStartOffset(prevLine),
                        editor.getDocument().getLineEndOffset(prevLine))).trim();
        if (lineString.startsWith("#")) {
            int newCol = lineString.replace("\t", "    ").indexOf("#");
            String enteredText = editor.getDocument().getText(
                    new TextRange(editor.getDocument().getLineStartOffset(caretPos.line),
                            editor.getDocument().getLineEndOffset(caretPos.line))).trim();
            editor.getDocument().deleteString(editor.getDocument().getLineStartOffset(caretPos.line),
                    editor.getDocument().getLineEndOffset(caretPos.line));
            editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(caretPos.line, 1));
            enterNewLine(editor, enteredText, newCol);

            // Commit the document.
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        }
        return Result.Continue;
    }

    private void enterNewLine(Editor editor, String str, int col) {
        StringBuilder strBuilder = new StringBuilder("# " + str);
        // Left padding with whitespaces in order to be vertically aligned with the previous doc line.
        for (int i = 0; i < col; i++) {
            strBuilder.insert(0, ' ');
        }
        str = strBuilder.toString();
        EditorModificationUtil.insertStringAtCaret(editor, str, false, str.indexOf('#') + 2);
    }
}
