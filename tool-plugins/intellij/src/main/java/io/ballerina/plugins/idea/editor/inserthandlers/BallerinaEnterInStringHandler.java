/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.ballerina.plugins.idea.BallerinaLanguage;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Enter key press handler for ballerina strings.
 */
public class BallerinaEnterInStringHandler extends EnterHandlerDelegateAdapter {

    public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Ref<Integer> caretOffset,
                                  @NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext,
                                  EditorActionHandler originalHandler) {

        if (!file.getLanguage().is(BallerinaLanguage.INSTANCE)) {
            return Result.Continue;
        }

        // We need to save the file before checking. Otherwise issues can occur when we press enter in a string.
        Project project = file.getProject();
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

        // Checks whether the cursor is placed inside the double quotes.
        int caretOff = editor.getCaretModel().getOffset();
        if (!isInStringLiteral(file, caretOff)) {
            return Result.Continue;
        }

        PsiElement element = file.findElementAt(caretOff);
        if (element == null) {
            return Result.Continue;
        }
        Document doc = editor.getDocument();
        int startOffset = element.getTextRange().getStartOffset();
        int endOffset = element.getTextRange().getEndOffset();
        int lineEndOffset = doc.getLineEndOffset(editor.getCaretModel().getLogicalPosition().line);

        // Adds the missing double quotes and + operator to the splitted text.
        String lText = doc.getText(new TextRange(startOffset, caretOff)) + "\" + \n";
        String rText = "\"" + doc.getText(new TextRange(caretOff, endOffset)) + doc.getText(new TextRange(endOffset,
                lineEndOffset));

        editor.getCaretModel().moveToOffset(startOffset);
        LogicalPosition caretPos = editor.getCaretModel().getLogicalPosition();

        // Replaces the current single string with the splitted strings.
        doc.deleteString(startOffset, lineEndOffset);
        doc.insertString(startOffset, lText);
        navigateToNextLine(editor, caretPos);
        EditorModificationUtil.insertStringAtCaret(editor, rText, false, 1);

        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        return Result.Stop;
    }

    private boolean isInStringLiteral(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);
        PsiElement prevElement = file.findElementAt(offset - 1);
        PsiElement nextElement = file.findElementAt(offset + 1);
        return isStringLiteral(element) && isStringLiteral(prevElement) && isStringLiteral(nextElement);
    }

    private boolean isStringLiteral(PsiElement element) {
        if (element == null) {
            return false;
        }
        return element.getNode().getElementType() == BallerinaTypes.QUOTED_STRING_LITERAL;
    }

    private void navigateToNextLine(Editor editor, LogicalPosition caretPos) {
        editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(caretPos.line + 1,
                caretPos.column, caretPos.leansForward));
        // This verification is needed since the column number might not be currently available. In that case, we
        // need to add the required indentation manually.
        int curColumn = editor.getCaretModel().getCurrentCaret().getLogicalPosition().column;
        if (curColumn < caretPos.column) {
            EditorModificationUtil.insertStringAtCaret(editor, StringUtils.repeat(" ", caretPos.column - curColumn));
        }
    }
}

