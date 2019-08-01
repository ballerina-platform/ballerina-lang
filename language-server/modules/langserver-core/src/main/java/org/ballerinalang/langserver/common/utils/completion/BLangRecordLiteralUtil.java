/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.common.utils.completion;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility operations on the BLangRecordLiterals.
 */
public class BLangRecordLiteralUtil {

    private BLangRecordLiteralUtil() {
    }

    /**
     * Get the record field completion itmes.
     * 
     * @param recordLiteral             Record Literal
     * @return {@link CompletionItem}   List of Completion Items
     */
    public static ArrayList<CompletionItem> getFieldsForMatchingRecord(BLangRecordLiteral recordLiteral) {
        if (!(recordLiteral.type instanceof BRecordType)) {
            return new ArrayList<>();
        }
        List<BField> fields = ((BRecordType) recordLiteral.type).fields;
        ArrayList<CompletionItem> completionItems = new ArrayList<>(CommonUtil.getRecordFieldCompletionItems(fields));
        completionItems.add(CommonUtil.getFillAllStructFieldsItem(fields));

        return completionItems;
    }
}
