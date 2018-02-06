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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

public class AliasCompletionInsertHandler implements InsertHandler<LookupElement> {

    public final static InsertHandler<LookupElement> INSTANCE = new AliasCompletionInsertHandler(false);

    private final boolean myTriggerAutoPopup;

    private AliasCompletionInsertHandler(boolean triggerAutoPopup) {
        myTriggerAutoPopup = triggerAutoPopup;
    }

    public void handleInsert(InsertionContext context, LookupElement item) {
        Editor editor = context.getEditor();
        char completionChar = context.getCompletionChar();
        if (completionChar == ' ') {
            return;
        }
        Project project = editor.getProject();
        if (project == null) {
            return;
        }
        if (!isCompletionCharAtSpace(editor)) {
            EditorModificationUtil.insertStringAtCaret(editor, " as ;", false, 4);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        } else {
            editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + 1);
        }
        if (myTriggerAutoPopup) {
            AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
        }
    }

    private static boolean isCompletionCharAtSpace(Editor editor) {
        // Todo - rewrite logic to identify already existing 'as' keyword
        final int startOffset = editor.getCaretModel().getOffset();
        final Document document = editor.getDocument();
        return document.getTextLength() > startOffset && document.getCharsSequence().charAt(startOffset) == '(';
    }
}
