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
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import io.ballerina.plugins.idea.BallerinaLanguage;
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
        String line = doc.getText(new TextRange(lineStartOff, lineEndOff)).trim();

        // if the line is empty, caret should be moved to the previous line.
        if (line.isEmpty() && position.line > 1) {
            int prevLineEndOff = doc.getLineEndOffset(position.line - 1);
            doc.deleteString(prevLineEndOff, lineEndOff);

            lineStartOff = doc.getLineStartOffset(position.line - 1);
            lineEndOff = doc.getLineEndOffset(position.line - 1);
            line = doc.getText(new TextRange(lineStartOff, lineEndOff)).trim();

            if (!line.isEmpty()) {
                // if the previous line is not empty, caret should be moved to the end of that line.
                editor.getCaretModel().moveToOffset(lineEndOff);
            } else {
                // if the previous line is empty, caret should be moved to the previous line while preserving
                // indentation.
                editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(position.line - 1,
                        position.column + 1));
            }

            // Commit the document.
            PsiDocumentManager.getInstance(file.getProject()).commitDocument(doc);
            return true;
        }

        return false;
    }
}
