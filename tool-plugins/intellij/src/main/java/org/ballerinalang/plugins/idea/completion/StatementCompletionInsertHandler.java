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

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;

/**
 * Provides statement completion support.
 */
public class StatementCompletionInsertHandler implements InsertHandler<LookupElement> {

    public static final InsertHandler<LookupElement> INSTANCE = new StatementCompletionInsertHandler();

    private final String myIgnoreOnChars;

    public StatementCompletionInsertHandler() {
        this("");
    }

    public StatementCompletionInsertHandler(String ignoreOnChars) {
        myIgnoreOnChars = ignoreOnChars;
    }

    public void handleInsert(InsertionContext context, LookupElement item) {
        Editor editor = context.getEditor();
        char completionChar = context.getCompletionChar();
        if (completionChar == ' ' || StringUtil.containsChar(myIgnoreOnChars, completionChar)) {
            return;
        }
        Project project = editor.getProject();
        if (project != null) {
            if (!isCompletionCharAtSpace(editor)) {
                EditorModificationUtil.insertStringAtCaret(editor, ";");
                PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            } else {
                editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + 1);
            }
        }
    }

    private static boolean isCompletionCharAtSpace(Editor editor) {
        final int startOffset = editor.getCaretModel().getOffset();
        final Document document = editor.getDocument();
        return document.getTextLength() > startOffset && document.getCharsSequence().charAt(startOffset) == ';';
    }
}
