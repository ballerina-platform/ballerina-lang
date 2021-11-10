/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.completions.builder;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * This class is used to build named arg completion item.
 *
 * @since 2.0.0
 */
public class NamedArgCompletionItemBuilder {

    private NamedArgCompletionItemBuilder() {

    }

    /**
     * Creates and returns a completion item.
     *
     * @param argName      argument name.
     * @param defaultValue default value for the argument.
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(String argName, String defaultValue) {
        String label = argName + " = ...";
        String insertText = argName + " = ${1:" + defaultValue + "}";
        String detail = argName + " = " + defaultValue;
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setDetail(detail);
        item.setKind(CompletionItemKind.Snippet);
        item.setFilterText(argName);
        return item;
    }
}
