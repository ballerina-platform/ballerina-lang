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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;

/**
 * Utility operations on the BLangRecordLiterals.
 */
public class BLangRecordLiteralUtil {

    private BLangRecordLiteralUtil() {
    }

    /**
     * Find all the record fields for the matching record literal.
     * 
     * When there are inner record fields, those are analyzed as well and matching innermost record's
     * fields are extracted
     * @param recordLiteral             Record Literal
     * @param context                   Completion Context
     * @return {@link CompletionItem}   List of Completion Items
     */
    public static ArrayList<CompletionItem> getFieldsForMatchingRecord(BLangRecordLiteral recordLiteral,
                                                                       LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition(recordLiteral.getPosition());
        int line = context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int nodeStartLine = nodePos.getStartLine();
        int nodeEndLine = nodePos.getEndLine();

        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            if (keyValuePair.valueExpr.type instanceof BRecordType) {
                DiagnosticPos exprPos = CommonUtil.toZeroBasedPosition(keyValuePair.valueExpr.getPosition());
                int exprStartLine = exprPos.getStartLine();
                int exprEndLine = exprPos.getEndLine();

                if (exprStartLine < line && exprEndLine > line
                        && keyValuePair.valueExpr instanceof BLangRecordLiteral) {
                    return getFieldsForMatchingRecord((BLangRecordLiteral) keyValuePair.valueExpr, context);
                }
            }
        }

        if (nodeStartLine < line && nodeEndLine > line && recordLiteral.type instanceof BRecordType) {
            completionItems.addAll(
                    CommonUtil.getStructFieldCompletionItems(((BRecordType) recordLiteral.type).fields)
            );
            completionItems.add(CommonUtil.getFillAllStructFieldsItem(((BRecordType) recordLiteral.type).fields));
        }

        return completionItems;
    }
}
