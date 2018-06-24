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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            return findAllFields((BLangRecordLiteral) ((BLangAnnotationAttachment) symbolEnvNode).expr, ctx);
        } else if (symbolEnvNode instanceof BLangAnnotationAttachment
                && ctx.get(CompletionKeys.ANNOTATION_ATTACHMENT_META_KEY) != null) {
            return findAllFieldsFromMetaInfo(ctx);
        }

        return new ArrayList<>();
    }

    /**
     * Find all the struct fields for the record literal.
     * @param recordLiteral             Record Literal
     * @param context                   Completion Context
     * @return {@link CompletionItem}   List of Completion Items
     */
    private ArrayList<CompletionItem> findAllFields(BLangRecordLiteral recordLiteral, LSContext context) {
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
                    return findAllFields((BLangRecordLiteral) keyValuePair.valueExpr, context);
                }
            }
        }

        if (nodeStartLine < line && nodeEndLine > line && recordLiteral.type instanceof BRecordType) {
            completionItems.addAll(
                    CommonUtil.getStructFieldPopulateCompletionItems(((BRecordType) recordLiteral.type).fields)
            );
            completionItems.add(CommonUtil.getFillAllStructFieldsItem(((BRecordType) recordLiteral.type).fields));
        }

        return completionItems;
    }
    
    private ArrayList<CompletionItem> findAllFieldsFromMetaInfo(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        AnnotationAttachmentMetaInfo annotationMeta = ctx.get(CompletionKeys.ANNOTATION_ATTACHMENT_META_KEY);
        HashMap<PackageID, List<BAnnotationSymbol>> annotationMap =
                LSAnnotationCache.getInstance().getAnnotationMapForType(annotationMeta.getAttachmentPoint());
        BAnnotationSymbol filteredAnnotation = null;
        for (Map.Entry<PackageID, List<BAnnotationSymbol>> packageIDListEntry : annotationMap.entrySet()) {
            List<Name> pkgNameComps = packageIDListEntry.getKey().getNameComps();
            if (annotationMeta.getPackageAlias().equals(pkgNameComps.get(pkgNameComps.size() - 1).getValue())) {
                String finalAnnotationName = annotationMeta.getAttachmentName();
                filteredAnnotation = packageIDListEntry.getValue().stream().filter(annotationSymbol ->
                        annotationSymbol.getName().getValue().equals(finalAnnotationName)).findFirst().orElse(null);
                break;
            }
        }

        if (filteredAnnotation == null || filteredAnnotation.kind != SymbolKind.ANNOTATION) {
            return null;
        }
        if (filteredAnnotation.attachedType instanceof BRecordTypeSymbol
                && ((BRecordTypeSymbol) filteredAnnotation.attachedType).type instanceof BRecordType) {
            
            BRecordType recordType = (BRecordType) ((BRecordTypeSymbol) filteredAnnotation.attachedType).type;
            if (annotationMeta.getFieldQueue().isEmpty()) {
                completionItems.addAll(CommonUtil.getStructFieldPopulateCompletionItems(recordType.fields));
            } else {
                completionItems.addAll(CommonUtil.getStructFieldPopulateCompletionItems(
                        findAllRecordFieldsOnFieldsQueue(recordType, annotationMeta.getFieldQueue().poll(), 
                                annotationMeta.getFieldQueue())
                ));
            }
            
        }
        
        return completionItems;
    }

    /**
     * Find all the record fields from the field stack found going through the token stream.
     * @param recordType    BLang Record
     * @param fieldName     Field name to find
     * @param fieldsQueue   Field queue containing the remaining field hierarchy
     * @return {@link List} List of fields
     */
    private List<BField> findAllRecordFieldsOnFieldsQueue(BRecordType recordType, String fieldName,
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
                return findAllRecordFieldsOnFieldsQueue((BRecordType) bType, fieldsQueue.poll(), fieldsQueue);
            }
        }

        return new ArrayList<>();
    }
}
