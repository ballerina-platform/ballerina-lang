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
import com.intellij.openapi.editor.Document;
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
 * Handles the enter key press events within ballerina documentation.
 */
public class BallerinaEnterInDocumentationHandler extends EnterHandlerDelegateAdapter {

    private static final String BAL_DOC_PREFIX = "#";

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {

        if (!file.getLanguage().is(BallerinaLanguage.INSTANCE) || editor.isDisposed()) {
            return Result.Continue;
        }
        Document doc = editor.getDocument();

        // We need to save the file before checking. Otherwise issues can occur when we press enter in a string.
        Project project = file.getProject();
        PsiDocumentManager.getInstance(project).commitDocument(doc);

        LogicalPosition caretPos = editor.getCaretModel().getLogicalPosition();
        int prevLine = caretPos.line - 1;
        String lineString = doc.getText(new TextRange(doc.getLineStartOffset(prevLine),
                doc.getLineEndOffset(prevLine)));

        if (lineString.trim().startsWith(BAL_DOC_PREFIX)) {
            addNewline(editor, doc, lineString, caretPos);
            // Commit the document.
            PsiDocumentManager.getInstance(project).commitDocument(doc);
        }
        return Result.Continue;
    }

    // Inserts the splitted documentation at the next line.
    private void addNewline(Editor editor, Document doc, String string, LogicalPosition caretPos) {

        int newCol = string.indexOf(BAL_DOC_PREFIX);
        String enteredText = doc.getText(new TextRange(doc.getLineStartOffset(caretPos.line),
                doc.getLineEndOffset(caretPos.line))).trim();
        doc.deleteString(doc.getLineStartOffset(caretPos.line), doc.getLineEndOffset(caretPos.line));
        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(caretPos.line, 1));

        StringBuilder strBuilder = new StringBuilder(BAL_DOC_PREFIX + " " + enteredText);
        // Left padding with whitespaces in order to be vertically aligned with the previous line.
        for (int i = 0; i < newCol; i++) {
            strBuilder.insert(0, ' ');
        }
        String finalText = strBuilder.toString();
        EditorModificationUtil.insertStringAtCaret(editor, finalText, false, finalText.indexOf(BAL_DOC_PREFIX) + 2);

    }
}
