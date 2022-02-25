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

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.AbstractLSCompletionItem;
import org.ballerinalang.langserver.completions.builder.FieldCompletionItemBuilder;
import org.eclipse.lsp4j.CompletionItem;

/**
 * Represents a Record Field Descriptor Completion Item.
 *
 * @since 2.0.0
 */
public class RecordFieldCompletionItem extends AbstractLSCompletionItem {
    private final RecordFieldSymbol fieldSymbol;

    public RecordFieldCompletionItem(BallerinaCompletionContext context, RecordFieldSymbol fieldSymbol,
                                     CompletionItem completionItem) {
        super(context, completionItem, CompletionItemType.RECORD_FIELD);
        this.fieldSymbol = fieldSymbol;

        // If the field type doesn't contain nil type and the field is optional, since we allow direct field access,
        // we have to set the "?" to the field type manually
        if (!FieldCompletionItemBuilder.hasNilType(fieldSymbol) && fieldSymbol.isOptional()) {
            completionItem.setDetail(CommonUtil.getModifiedTypeName(context, fieldSymbol.typeDescriptor()) + "?");
        } else {
            completionItem.setDetail(CommonUtil.getModifiedTypeName(context, fieldSymbol.typeDescriptor()));
        }
    }

    public RecordFieldCompletionItem(BallerinaCompletionContext context, RecordFieldSymbol fieldSymbol,
                                     CompletionItem completionItem, String detail) {
        super(context, completionItem, CompletionItemType.RECORD_FIELD);
        this.fieldSymbol = fieldSymbol;
        completionItem.setDetail(detail);
    }

    public RecordFieldSymbol getFieldSymbol() {
        return fieldSymbol;
    }
}
