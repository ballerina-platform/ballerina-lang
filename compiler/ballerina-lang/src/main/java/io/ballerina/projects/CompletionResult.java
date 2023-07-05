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
package io.ballerina.projects;

import io.ballerina.projects.plugins.completion.CompletionException;
import io.ballerina.projects.plugins.completion.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of the completion operation.
 *
 * @since 2201.7.0
 */
public class CompletionResult {

    private final List<CompletionItem> completionItems = new ArrayList<>();
    private final List<CompletionException> errors = new ArrayList<>();

    public void addCompletionItems(List<CompletionItem> completionItems) {
        this.completionItems.addAll(completionItems);
    }

    public void addError(CompletionException ex) {
        errors.add(ex);
    }

    /**
     * Get completion items provided by compiler plugins.
     *
     * @return List of completion items
     */
    public List<CompletionItem> getCompletionItems() {
        return completionItems;
    }

    /**
     * Get errors catch while processing compiler plugin completion providers.
     *
     * @return List of errors
     */
    public List<CompletionException> getErrors() {
        return errors;
    }
}
