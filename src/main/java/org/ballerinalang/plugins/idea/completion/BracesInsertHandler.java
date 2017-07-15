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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;

public class BracesInsertHandler implements InsertHandler<LookupElement> {

    public final static InsertHandler<LookupElement> INSTANCE = new BracesInsertHandler(false);
    public final static InsertHandler<LookupElement> INSTANCE_WITH_AUTO_POPUP =
            new BracesInsertHandler(true);

    private final String myIgnoreOnChars;
    private final boolean myTriggerAutoPopup;

    private BracesInsertHandler(boolean triggerAutoPopup) {
        this("", triggerAutoPopup);
    }

    private BracesInsertHandler(String ignoreOnChars, boolean triggerAutoPopup) {
        myIgnoreOnChars = ignoreOnChars;
        myTriggerAutoPopup = triggerAutoPopup;
    }

    public void handleInsert(InsertionContext context, LookupElement item) {
        Editor editor = context.getEditor();
        char completionChar = context.getCompletionChar();
        if (completionChar == ' ' || StringUtil.containsChar(myIgnoreOnChars, completionChar)) {
            return;
        }
        Project project = editor.getProject();
        if (project != null) {
            int completionCharOffset = getCompletionCharOffset(editor);
            if (completionCharOffset == -1) {
                EditorModificationUtil.insertStringAtCaret(editor, " {}", false, 2);
                PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            } else {
                editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + completionCharOffset + 1);
            }
            if (myTriggerAutoPopup) {
                AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
            }
        }
    }

    private static int getCompletionCharOffset(Editor editor) {
        int startOffset = editor.getCaretModel().getOffset();
        Document document = editor.getDocument();
        int textLength = document.getTextLength();
        CharSequence charsSequence = document.getCharsSequence();

        char c;
        for (int i = startOffset; i < textLength; i++) {
            c = charsSequence.charAt(i);
            if (c == '{') {
                return i - startOffset;
            } else if (!Character.isSpaceChar(c)) {
                break;
            }
        }
        return -1;
    }
}
