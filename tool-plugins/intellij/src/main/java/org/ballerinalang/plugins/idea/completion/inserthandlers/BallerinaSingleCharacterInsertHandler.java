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
 *
 */

package org.ballerinalang.plugins.idea.completion.inserthandlers;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

/**
 * Provides support to insert a single character.
 */
public class BallerinaSingleCharacterInsertHandler extends BasicInsertHandler<LookupElement> {

    public static final BallerinaSingleCharacterInsertHandler ORGANIZATION_SEPARATOR =
            new BallerinaSingleCharacterInsertHandler('/');

    private final char myCharacter;

    public BallerinaSingleCharacterInsertHandler(char c) {
        myCharacter = c;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, LookupElement item) {
        Editor editor = context.getEditor();
        int tailOffset = context.getTailOffset();
        Document document = editor.getDocument();
        context.commitDocument();
        boolean staysAtChar = (document.getTextLength() > tailOffset) &&
                (document.getCharsSequence().charAt(tailOffset) == myCharacter);
        context.setAddCompletionChar(false);
        if (!staysAtChar) {
            document.insertString(tailOffset, String.valueOf(myCharacter));
        }
        editor.getCaretModel().moveToOffset(tailOffset + 1);
        AutoPopupController.getInstance(context.getProject()).scheduleAutoPopup(editor);
    }
}
