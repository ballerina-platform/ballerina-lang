/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.completions.builder;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * This is used for building parameter completions.
 *
 * @since 2.0.0
 */
public final class ParameterCompletionItemBuilder {
    private ParameterCompletionItemBuilder() {
    }

    /**
     * Creates and returns a parameter completion item.
     *
     * @param label label
     * @param type  parameter type
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(String label, String type) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        String insertText = CommonUtil.escapeEscapeCharsInIdentifier(label);
        item.setInsertText(insertText);
        item.setDetail((type.equals("")) ? ItemResolverConstants.NONE : type);
        item.setKind(CompletionItemKind.Variable);
        return item;
    }
}
