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

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KeywordCompletionProvider extends CompletionProvider<CompletionParameters> {

    private final int myPriority;
    @Nullable
    private final InsertHandler<LookupElement> myInsertHandler;
    @Nullable
    private final AutoCompletionPolicy myCompletionPolicy;
    @NotNull
    private final String[] myKeywords;

    public KeywordCompletionProvider(int priority, @Nullable AutoCompletionPolicy completionPolicy,
                                     @NotNull String... keywords) {
        this(priority, null, completionPolicy, keywords);
    }

    private KeywordCompletionProvider(int priority,
                                      @Nullable InsertHandler<LookupElement> insertHandler,
                                      @Nullable AutoCompletionPolicy completionPolicy,
                                      @NotNull String... keywords) {
        myPriority = priority;
        myInsertHandler = insertHandler;
        myCompletionPolicy = completionPolicy;
        myKeywords = keywords;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        for (String keyword : myKeywords) {
            result.addElement(createKeywordLookupElement(keyword));
        }
    }

    @NotNull
    private LookupElement createKeywordLookupElement(@NotNull String keyword) {
        InsertHandler<LookupElement> insertHandler = ObjectUtils.chooseNotNull(myInsertHandler, null
                //                createTemplateBasedInsertHandler("go_lang_" + keyword)
        );
        LookupElement result = createKeywordLookupElement(keyword, myPriority, myInsertHandler);
        return myCompletionPolicy != null ? myCompletionPolicy.applyPolicy(result) : result;
    }

    public static LookupElement createKeywordLookupElement(@NotNull String keyword,
                                                           int priority,
                                                           @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.create(keyword).withBoldness(true).withInsertHandler
                (insertHandler);
        return PrioritizedLookupElement.withPriority(builder, priority);
    }

    @Nullable
    public static InsertHandler<LookupElement> createTemplateBasedInsertHandler(@NotNull String templateId) {
        return (context, item) -> {
            Template template = TemplateSettings.getInstance().getTemplateById(templateId);
            Editor editor = context.getEditor();
            if (template != null) {
                editor.getDocument().deleteString(context.getStartOffset(), context.getTailOffset());
                TemplateManager.getInstance(context.getProject()).startTemplate(editor, template);
            } else {
                int currentOffset = editor.getCaretModel().getOffset();
                CharSequence documentText = editor.getDocument().getImmutableCharSequence();
                if (documentText.length() <= currentOffset || documentText.charAt(currentOffset) != ' ') {
                    EditorModificationUtil.insertStringAtCaret(editor, " ");
                } else {
                    EditorModificationUtil.moveCaretRelatively(editor, 1);
                }
            }
        };
    }
}
