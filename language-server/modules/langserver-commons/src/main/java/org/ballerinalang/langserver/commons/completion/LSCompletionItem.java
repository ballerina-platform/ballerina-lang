/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.completion;

import org.eclipse.lsp4j.CompletionItem;

/**
 * Wrapper interface for a Completion item.
 *
 * @since 1.2.0
 */
public interface LSCompletionItem {

    CompletionItem getCompletionItem();
    
    CompletionItemType getType();

    /**
     * Represents the completion item type.
     * 
     * @since 2.0.0
     */
    enum CompletionItemType {
        OBJECT_FIELD,
        RECORD_FIELD,
        SNIPPET,
        STATIC,
        SYMBOL,
        TYPE,
        FUNCTION_POINTER,
        NAMED_ARG,
    }
}
