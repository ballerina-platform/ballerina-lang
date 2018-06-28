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
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Item Resolver for BLangRecordLiteral.
 */
public class BLangRecordLiteralContextResolver extends AbstractItemResolver {
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode bLangNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        BType recordType = bLangNode.type;
        // TODO: test the completion item with new BField type.
        if (recordType instanceof BStructureType) {
            List<BField> fields = ((BStructureType) recordType).getFields();
            completionItems.addAll(CommonUtil.getStructFieldPopulateCompletionItems(fields));
            completionItems.add(CommonUtil.getFillAllStructFieldsItem(fields));
        }

        return completionItems;
    }
}
