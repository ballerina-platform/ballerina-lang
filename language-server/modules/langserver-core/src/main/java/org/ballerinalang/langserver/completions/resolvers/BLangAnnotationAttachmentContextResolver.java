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

import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.AnnotationAttachmentMetaInfo;
import org.ballerinalang.langserver.common.utils.completion.BLangRecordLiteralUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/**
 * Annotation resolver for BLangAnnotationAttachment node context.
 */
public class BLangAnnotationAttachmentContextResolver extends AbstractItemResolver {
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        BLangNode symbolEnvNode = ctx.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        if (symbolEnvNode instanceof BLangAnnotationAttachment
                && ((BLangAnnotationAttachment) symbolEnvNode).expr instanceof BLangRecordLiteral) {
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) ((BLangAnnotationAttachment) symbolEnvNode).expr;
            return BLangRecordLiteralUtil.getFieldsForMatchingRecord(recordLiteral, ctx);
        } else if (symbolEnvNode instanceof BLangAnnotationAttachment
                && ctx.get(CompletionKeys.ANNOTATION_ATTACHMENT_META_KEY) != null) {
            return findAllFieldsFromMetaInfo(ctx);
        }

        return new ArrayList<>();
    }

    private ArrayList<CompletionItem> findAllFieldsFromMetaInfo(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        Optional<BRecordType> filteredRecord = Optional.empty();
        AnnotationAttachmentMetaInfo metaInfo = ctx.get(CompletionKeys.ANNOTATION_ATTACHMENT_META_KEY);
        HashMap<PackageID, List<BAnnotationSymbol>> annotationMap =
                LSAnnotationCache.getInstance().getAnnotationMapForType(metaInfo.getAttachmentPoint(), ctx);

        /*
        Iterate over the annotations map and find the package entry for the given package alias.
        From this entry extract the particular annotation attachment and extract the record field
         */
        for (Map.Entry<PackageID, List<BAnnotationSymbol>> packageIDListEntry : annotationMap.entrySet()) {
            String packageName = CommonUtil.getLastItem(packageIDListEntry.getKey().getNameComps()).getValue();
            if (metaInfo.getPackageAlias().equals(packageName)) {
                Optional<BAnnotationSymbol> filteredAnnotation = packageIDListEntry.getValue()
                        .stream()
                        .filter(symbol -> symbol.getName().getValue().equals(metaInfo.getAttachmentName()))
                        .findFirst();
                filteredRecord = filteredAnnotation.isPresent()
                        ? getRecordType(filteredAnnotation.get())
                        : Optional.empty();
                break;
            }
        }

        // Populate the fields from the record field.
        if (filteredRecord.isPresent()) {
            List<BField> fields = metaInfo.getFieldQueue().isEmpty()
                    ? filteredRecord.get().fields
                    : findAllRecordFields(filteredRecord.get(), metaInfo.getFieldQueue().poll(),
                    metaInfo.getFieldQueue());
            completionItems.addAll(CommonUtil.getStructFieldCompletionItems(fields));
        }

        return completionItems;
    }

    /**
     * Find all the record fields from the field stack found going through the token stream.
     * 
     * Note: Here, go through the fields stack (fields stack represent the nested record structure) and get the fields
     *       for the inner most record.
     * 
     * @param recordType    BLang Record
     * @param fieldName     Field name to find
     * @param fieldsQueue   Field queue containing the remaining field hierarchy
     * @return {@link List} List of fields
     */
    private List<BField> findAllRecordFields(BRecordType recordType, String fieldName,
                                             Queue<String> fieldsQueue) {
        for (BField field : recordType.fields) {
            BType bType = field.getType();
            if (!(bType instanceof BRecordType)) {
                continue;
            }
            if (field.getName().getValue().equals(fieldName)) {
                if (fieldsQueue.isEmpty()) {
                    return ((BRecordType) bType).fields;
                }
                return findAllRecordFields((BRecordType) bType, fieldsQueue.poll(), fieldsQueue);
            }
        }

        return new ArrayList<>();
    }

    /**
     * Get the record type from the given annotation symbol.
     *
     * @param symbol                Annotation symbol to extract the record type
     * @return {@link Optional}     BRecordType extracted
     */
    private Optional<BRecordType> getRecordType(BAnnotationSymbol symbol) {
        if (symbol.attachedType instanceof BRecordTypeSymbol
                && ((BRecordTypeSymbol) symbol.attachedType).type instanceof BRecordType) {
            return Optional.of((BRecordType) ((BRecordTypeSymbol) symbol.attachedType).type);
        }
        
        return Optional.empty();
    }
}
