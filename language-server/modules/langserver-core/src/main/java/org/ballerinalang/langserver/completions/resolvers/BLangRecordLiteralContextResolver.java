/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for BLangRecordLiteral.
 */
public class BLangRecordLiteralContextResolver extends AbstractItemResolver {
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode bLangNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        BType recordType = bLangNode.type;
        
        if (!recordType.getKind().equals(TypeKind.RECORD)) {
            return completionItems;
        }

        List<BField> fields = this.getFieldList((BLangRecordLiteral) bLangNode, completionContext);
        completionItems.addAll(CommonUtil.getStructFieldCompletionItems(fields));
        completionItems.add(CommonUtil.getFillAllStructFieldsItem(fields));

        return completionItems;
    }

    private List<BField> getFieldList(BLangRecordLiteral recordLiteral, LSContext context) {
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        List<BField> filteredFields = new ArrayList<>();
        int line = cursor.getLine();
        List<BLangExpression> values = recordLiteral.keyValuePairs.stream()
                .map(BLangRecordLiteral.BLangRecordKeyValue::getValue)
                .collect(Collectors.toList());

        for (BLangExpression value : values) {
            DiagnosticPos diagnosticPos = CommonUtil.toZeroBasedPosition(value.getPosition());
            int exprSLine = diagnosticPos.getStartLine();
            int exprELine = diagnosticPos.getEndLine();
            if (exprSLine < line && exprELine > line) {
                filteredFields.addAll(this.getFieldList((BLangRecordLiteral) value, context));
                break;
            }
        }
        if (filteredFields.isEmpty()) {
            filteredFields.addAll(((BStructureType) recordLiteral.type).getFields());
        }

        return filteredFields;
    }
}
