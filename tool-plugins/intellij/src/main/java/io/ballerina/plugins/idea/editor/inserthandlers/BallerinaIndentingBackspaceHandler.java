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

import com.intellij.codeInsight.editorActions.BackspaceHandlerDelegate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import io.ballerina.plugins.idea.BallerinaLanguage;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Preserves indentation of the caret when using backspace inside a code block.
 */
public class BallerinaIndentingBackspaceHandler extends BackspaceHandlerDelegate {

    private boolean jumpPredicate = false;

    @Override
    public void beforeCharDeleted(char c, @NotNull PsiFile file, @NotNull Editor editor) {
        if (!file.getLanguage().is(BallerinaLanguage.INSTANCE)) {
            jumpPredicate = false;

        }
        jumpPredicate = c == ' ';
    }

    @Override
    public boolean charDeleted(char c, @NotNull PsiFile file, @NotNull Editor editor) {
        if (!jumpPredicate) {
            return false;
        }

        Document doc = editor.getDocument();
        LogicalPosition position = editor.getCaretModel().getLogicalPosition();
        int lineStartOff = doc.getLineStartOffset(position.line);
        int lineEndOff = doc.getLineEndOffset(position.line);
        int caretOffset = editor.getCaretModel().getPrimaryCaret().getOffset();
        if (caretOffset < lineStartOff || caretOffset > lineEndOff) {
            return false;
        }

        String textBeforeCaret = doc.getText(new TextRange(lineStartOff, caretOffset)).trim();
        String textToBeMoved = doc.getText(new TextRange(caretOffset, lineEndOff)).trim();
        // only if the text before the caret position is empty in a given line, caret should be moved to the previous
        // line.
        if (!textBeforeCaret.trim().isEmpty() || position.line <= 1) {
            return false;
        }

        // if the text before the caret is empty in a given line, caret should be moved to the previous line.
        int prevLineStartOff = doc.getLineStartOffset(position.line - 1);
        int prevLineEndOff = doc.getLineEndOffset(position.line - 1);
        doc.deleteString(prevLineEndOff, lineEndOff);
        String prevLine = doc.getText(new TextRange(prevLineStartOff, prevLineEndOff)).trim();

        if (!prevLine.isEmpty()) {
            // if the previous line is not empty, caret should be moved to the end of that line.
            editor.getCaretModel().moveToOffset(prevLineEndOff);
        } else {
            // if the previous line is empty, caret should be moved to the previous line while preserving
            // indentation.
            int lineToBeMoved = position.line - 1;
            int columnToBeMoved = position.column + 1;
            editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(lineToBeMoved,
                    columnToBeMoved, position.leansForward));

            // This verification is needed since the column number might not be currently available. In that case, we
            // need to add the required indentation manually.
            int curColumn = editor.getCaretModel().getCurrentCaret().getLogicalPosition().column;
            if (curColumn < columnToBeMoved) {
                EditorModificationUtil.insertStringAtCaret(editor, StringUtils.repeat(" ",
                        columnToBeMoved - curColumn));
            }
        }

        EditorModificationUtil.insertStringAtCaret(editor, textToBeMoved, false, 0);
        // Commit the document.
        PsiDocumentManager.getInstance(file.getProject()).commitDocument(doc);
        return true;

    }
}
