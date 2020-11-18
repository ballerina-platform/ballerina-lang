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
package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.api.types.FieldDescriptor;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.AbstractLSCompletionItem;
import org.eclipse.lsp4j.CompletionItem;

/**
 * Represents a Field Descriptor Completion Item.
 *
 * @since 1.2.0
 */
public class FieldCompletionItem extends AbstractLSCompletionItem {
    private final FieldDescriptor fieldDescriptor;

    public FieldCompletionItem(LSContext lsContext, FieldDescriptor fieldDescriptor, CompletionItem completionItem) {
        super(lsContext, completionItem);
        this.fieldDescriptor = fieldDescriptor;
    }

    public FieldDescriptor getFieldDescriptor() {
        return fieldDescriptor;
    }
}
