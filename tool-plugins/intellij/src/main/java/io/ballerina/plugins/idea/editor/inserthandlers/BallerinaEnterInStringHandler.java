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

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretModel;
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
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Enter key press handler for ballerina strings.
 */
public class BallerinaEnterInStringHandler extends EnterHandlerDelegateAdapter {

    public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Ref<Integer> caretOffset,
                                  @NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext,
                                  EditorActionHandler originalHandler) {

        if (!file.getLanguage().is(BallerinaLanguage.INSTANCE) || editor.isDisposed()) {
            return Result.Continue;
        }

        // If the user has configured to automatically hard wrap when typing, skips the processing and lets
        // "AutoHardWrapHandler" to format the string.
        if (CodeStyle.getSettings(file).isWrapOnTyping(null)) {
            return Result.Continue;
        }

        // We need to save the file before checking. Otherwise issues can occur when we press enter in a string.
        Project project = file.getProject();
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        CaretModel caretModel = editor.getCaretModel();

        // Checks whether the cursor is placed within the double quotes.
        int caretOff = caretModel.getOffset();
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
        int lineEndOffset = doc.getLineEndOffset(doc.getLineNumber(endOffset));

        // Adds the missing double quotes and + operator to the splitted text.
        // Note - Don't replace the "\n" with the "System.lineSeparator()" as Docuemnt.insertString() method only
        // accepts the unix line separator.
        String lText = doc.getText(new TextRange(startOffset, caretOff)) + "\" + \n";
        String rText = "\"" + doc.getText(new TextRange(caretOff, endOffset)) + doc.getText(new TextRange(endOffset,
                lineEndOffset));

        caretModel.moveToOffset(startOffset);
        // Replaces the current single string with the left part of the split strings.
        doc.deleteString(startOffset, lineEndOffset);
        doc.insertString(startOffset, lText);

        // This is to update the PsiTree of document with the changes we added above.
        PsiDocumentManager.getInstance(project).commitDocument(doc);

        // Moves the cursor to the end of the first string.
        caretOff = caretModel.getOffset();
        element = file.findElementAt(caretOff);
        if (!isStringLiteral(element)) {
            return Result.Continue;
        }
        caretModel.moveToOffset(element.getTextRange().getEndOffset());

        String lineText = doc.getText(new TextRange(doc.getLineStartOffset(caretModel.getLogicalPosition().line),
                doc.getLineEndOffset(caretModel.getLogicalPosition().line)));
        int column = lineText.replace("\t", "    ").indexOf(lineText.trim());
        // Moves the cursor to the beginning of the line.
        caretModel.moveToLogicalPosition(new LogicalPosition(caretModel.getLogicalPosition().line, column));
        // Moves the caret to the next immediate line with proper indentation.
        navigateToNextLine(editor, caretModel.getLogicalPosition());
        EditorModificationUtil.insertStringAtCaret(editor, rText, false, 1);

        PsiDocumentManager.getInstance(project).commitDocument(doc);
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
