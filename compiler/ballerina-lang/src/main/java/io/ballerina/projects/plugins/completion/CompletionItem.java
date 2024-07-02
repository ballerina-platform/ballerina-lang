/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.projects.plugins.completion;

import io.ballerina.tools.text.TextEdit;

import java.util.List;

/**
 * Represents a completion item.
 *
 * @since 2201.7.0
 */
public class CompletionItem {
    /**
     * The label of this completion item. By default, also the text that is inserted when selecting
     * this completion.
     */
    private final String label;
    
    /**
     * Indicates the priority(sorted position) of the completion item.
     */
    private final Priority priority;

    /**
     * An optional array of additional text edits that are applied when selecting this completion. 
     * Edits must not overlap (including the same insert position) with the main edit nor with themselves.
     * Additional text edits should be used to change text unrelated to the 
     * current cursor position (for example adding an import statement at the top of the file if the completion
     * item will insert a qualified type).
     */
    private List<TextEdit> additionalTextEdits;

    /**
     * A string that should be inserted a document when selecting this completion. 
     * When omitted or empty, the label is used as the insert text for this item.
     */
    private final String insertText;
    
    public CompletionItem(String label, String insertText, Priority priority) {
        this.label = label;
        this.insertText = insertText;
        this.priority = priority;
    }

    public String getInsertText() {
        return insertText;
    }

    public String getLabel() {
        return label;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setAdditionalTextEdits(List<TextEdit> additionalTextEdits) {
        this.additionalTextEdits = additionalTextEdits;
    }

    public List<TextEdit> getAdditionalTextEdits() {
        return additionalTextEdits;
    }

    /**
     * Represents the priority of the completion item. If priority is high the completion item
     * will be sorted to the top of the completion item list. If low a default priority based on
     * the completion item kind (Snippet) will be assigned.
     */
    public enum Priority {
        HIGH,
        LOW
    }
}
