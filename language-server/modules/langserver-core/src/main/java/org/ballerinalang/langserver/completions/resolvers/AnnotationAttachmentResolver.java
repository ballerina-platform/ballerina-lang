/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.LSAnnotationCache;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.model.AttachmentPoint;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.ballerinalang.langserver.common.utils.CommonUtil.getDefaultValueForType;

/**
 * Annotation Attachment Resolver to resolve the corresponding annotation attachments.
 */
public class AnnotationAttachmentResolver extends AbstractItemResolver {
    
    private final List<AttachmentPoint> attachmentPointValues = Arrays.asList(AttachmentPoint.values());
    
    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        String attachmentPointType = completionContext.get(CompletionKeys.ATTACHMENT_POINT_NODE_TYPE_KEY);
        BLangNode symbolEnvNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        AttachmentPoint attachmentPoint;
        
        if (symbolEnvNode != null && symbolEnvNode instanceof BLangAnnotationAttachment) {
            return getFieldsFromBLangNode((BLangAnnotationAttachment) symbolEnvNode, completionContext);
        }
        if (completionContext.get(CompletionKeys.ATTACHMENT_POINT_NODE_TYPE_KEY) != null) {
            attachmentPoint = attachmentPointValues.stream()
                    .filter(ap -> ap.getValue().equals(attachmentPointType)).findFirst().orElse(null);
        } else {
            attachmentPoint = getAttachmentPointFromTokenStream(completionContext);
        }
        
        if (attachmentPoint == null) {
            return new ArrayList<>();
        }
        
        if (isCursorAtAnnotationStart(completionContext)) {
            return filterAnnotations(attachmentPoint);
        } else {
            // Filter the field list for a particular annotation
            return filerFieldList(completionContext, attachmentPoint);
        }
    }

    /**
     * Filter the annotations from the data model.
     * 
     * @return {@link List}
     */
    private ArrayList<CompletionItem> filterAnnotations(AttachmentPoint attachmentPoint) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        
        LSAnnotationCache.getInstance().getAnnotationMapForType(attachmentPoint).entrySet().forEach(annotationLists ->
                annotationLists.getValue().forEach(bLangAnnotation -> {
            completionItems.add(CommonUtil.getAnnotationCompletionItem(annotationLists.getKey(), bLangAnnotation));
        }));
        
        return completionItems;
    }

    /**
     * Filter the fields list for a particular attachment point.
     * @param context               Completion Context
     * @param attachmentPoint       Attachment point
     * @return {@link List}         List of completion items
     */
    private ArrayList<CompletionItem> filerFieldList(LSServiceOperationContext context,
                                                     AttachmentPoint attachmentPoint) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        Position cursorPosition = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        Stack<String> fieldStack = new Stack<>();
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList("function", "service", "endpoint", "("));
        int startIndex = context.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        int line = cursorPosition.getLine();
        int col = cursorPosition.getCharacter();
        String annotationName = "";
        String tempTokenString = "";
        String pkgAlias = "";
        
        while (true) {
            if (startIndex > tokenStream.size()) {
                break;
            }
            Token token = CommonUtil.getNextDefaultToken(tokenStream, startIndex);
            if (token.getText().equals(UtilSymbolKeys.OPEN_BRACE_KEY)) {
                annotationName = tempTokenString;
                startIndex = token.getTokenIndex();
                break;
            }
            tempTokenString = token.getText();
            startIndex = token.getTokenIndex();
        }
        
        if (annotationName.isEmpty()) {
            return completionItems;
        }
        
        if (UtilSymbolKeys.PKG_DELIMITER_KEYWORD
                .equals(CommonUtil.getNthDefaultTokensToLeft(tokenStream, startIndex, 2).getText())) {
            pkgAlias = CommonUtil.getNthDefaultTokensToLeft(tokenStream, startIndex, 3).getText();
        }
        
        while (true) {
            if (startIndex > tokenStream.size()) {
                break;
            }
            Token token = CommonUtil.getNextDefaultToken(tokenStream, startIndex);
            int tokenLine = token.getLine() - 1;
            int tokenCol = token.getCharPositionInLine();
            if (terminalTokens.contains(token.getText())) {
                return completionItems;
            } else if (line < tokenLine || (line == tokenLine - 1 && col - 1 < tokenCol)) {
                break;
            } else if (UtilSymbolKeys.OPEN_BRACE_KEY.equals(token.getText())) {
                fieldStack.push(tempTokenString);
            } else if (UtilSymbolKeys.CLOSE_BRACE_KEY.equals(token.getText())) {
                fieldStack.pop();
            } else if (!UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(token.getText())) {
                tempTokenString = token.getText();
            }
            startIndex = token.getTokenIndex();
        }

        HashMap<PackageID, List<BLangAnnotation>> annotationMap =
                LSAnnotationCache.getInstance().getAnnotationMapForType(attachmentPoint);
        BLangAnnotation filteredAnnotation = null;
        for (Map.Entry<PackageID, List<BLangAnnotation>> packageIDListEntry : annotationMap.entrySet()) {
            List<Name> pkgNameComps = packageIDListEntry.getKey().getNameComps();
            if (pkgAlias.equals(pkgNameComps.get(pkgNameComps.size() - 1).getValue())) {
                String finalAnnotationName = annotationName;
                filteredAnnotation = packageIDListEntry.getValue().stream().filter(annotation ->
                        annotation.getName().getValue().equals(finalAnnotationName)).findFirst().orElse(null);
                break;
            }
        }

        if (filteredAnnotation == null || !(filteredAnnotation.typeNode.type instanceof BStructType)) {
            return null;
        }
        
        if (!fieldStack.isEmpty()) {            
            completionItems.addAll(getFieldCompletionItems(
                    findAllStructFields((BStructType) filteredAnnotation.typeNode.type, fieldStack.pop(), fieldStack)
            ));
        } else {
            completionItems.addAll(getFieldCompletionItems(((BStructType) filteredAnnotation.typeNode.type).fields));
        }
        
        return completionItems;
    }

    /**
     * Get the attachment point from the token stream when the source is incomplete.
     * @param completionContext         Completion context
     * @return {@link AttachmentPoint}  Extracted attachment point
     */
    private AttachmentPoint getAttachmentPointFromTokenStream(LSServiceOperationContext completionContext) {
        String terminalToken = UtilSymbolKeys.SEMI_COLON_SYMBOL_KEY;
        List<AttachmentPoint> nodeTypeKeywords = new ArrayList<>(Arrays.asList(AttachmentPoint.SERVICE,
                AttachmentPoint.FUNCTION));
        AttachmentPoint attachmentPoint = null;
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int startIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);

        while (true) {
            if (startIndex > tokenStream.size()) {
                break;
            }
            Token token = CommonUtil.getNextDefaultToken(tokenStream, startIndex);
            String tokenString = token.getText();
            AttachmentPoint apForToken = attachmentPointValues.stream()
                    .filter(ap -> ap.getValue().equals(tokenString)).findFirst().orElse(null);
            if (tokenString.equals(terminalToken)) {
                break;
            } else if (nodeTypeKeywords.contains(apForToken)) {
                attachmentPoint = apForToken;
                break;
            }
            startIndex = token.getTokenIndex();
        }

        return attachmentPoint;
    }

    /**
     * Check the cursor is at the start of annotation start.
     * @param context               Completion Context
     * @return {@link Boolean}      whether the cursor is at annotation start or not  
     */
    private boolean isCursorAtAnnotationStart(LSServiceOperationContext context) {
        Position cursorPosition = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        Token token = tokenStream.get(context.get(DocumentServiceKeys.TOKEN_INDEX_KEY));
        int tokenLine = token.getLine() - 1;
        int tokenCol = token.getCharPositionInLine();
        int line = cursorPosition.getLine();
        int col = cursorPosition.getCharacter();
        
        return line == tokenLine && tokenCol == col - 1;
    }

    /**
     * Find all the struct fields from the field stack found going through the token stream.
     * @param structType    BLang Struct
     * @param fieldName     Field name to find
     * @param fieldStack    Field stack containing the field hierarchy
     * @return {@link org.wso2.ballerinalang.compiler.semantics.model.types.BStructType.BStructField} list of fields
     */
    private List<BStructType.BStructField> findAllStructFields(BStructType structType, String fieldName,
                                                               Stack<String> fieldStack) {
        for (BStructType.BStructField field : structType.fields) {
            BType bType = field.getType();
            if (!(bType instanceof BStructType)) {
                continue;
            }
            if (field.getName().getValue().equals(fieldName)) {
                if (fieldStack.isEmpty()) {
                    return ((BStructType) bType).fields;
                }
                return findAllStructFields((BStructType) bType, fieldStack.pop(), fieldStack);
            }
        }
        
        return new ArrayList<>();
    }

    /**
     * Get the completion items for the given set of struct fields.
     * @param structFields              List of strut fields
     * @return {@link CompletionItem}   List of completion Items
     */
    private List<CompletionItem> getFieldCompletionItems(List<BStructType.BStructField> structFields) {
        List<CompletionItem> completionItems = new ArrayList<>();
        structFields.forEach(bStructField -> {
            StringBuilder insertText = new StringBuilder(bStructField.getName().getValue() + ": ");
            if (bStructField.getType() instanceof BStructType) {
                insertText.append(getStructInsertText(((BStructType) bStructField.getType()).fields));
            } else {
                insertText.append("${1:").append(getDefaultValueForType(bStructField.getType())).append("}");
            }
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(insertText.toString());
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(bStructField.getName().getValue());
            fieldItem.setDetail(ItemResolverConstants.FIELD_TYPE);
            fieldItem.setSortText(Priority.PRIORITY120.toString());
            completionItems.add(fieldItem);
        });
        
        return completionItems;
    }

    /**
     * Get the insert text for the struct considering the struct fields.
     * @param structFields      List of struct fields
     * @return {@link String}   Insert text
     */
    private String getStructInsertText(List<BStructType.BStructField> structFields) {
        return  "{" + LINE_SEPARATOR + "\t${1}" + LINE_SEPARATOR + "}";
        // Note: Code has been commented on purpose since the implementation can be reverted back

//        List<String> fields = new ArrayList<>();
//        
//        structFields.forEach(bStructField -> {
//            fields.add(System.lineSeparator() + "\t" + bStructField.getName().getValue() + ": "
//                    + getDefaultValueForType(bStructField.getType()));
//        });
        
//        return insertText + String.join(",", fields) + System.lineSeparator() + "}";
    }

    /**
     * Get the fields for the record.
     * @param annotationAttachment      Annotation attachment
     * @param context                   Completion Context
     * @return {@link ArrayList}        List of completion Items
     */
    private ArrayList<CompletionItem> getFieldsFromBLangNode(BLangAnnotationAttachment annotationAttachment,
                                                        LSServiceOperationContext context) {
        if (annotationAttachment.expr instanceof BLangRecordLiteral) {
            return findAllStructFields((BLangRecordLiteral) annotationAttachment.expr, context);
        }
        
        return new ArrayList<>();
    }

    /**
     * Find all the struct fields for the record literal.
     * @param recordLiteral             Record Literal
     * @param context                   Completion Context
     * @return {@link CompletionItem}   List of Completion Items
     */
    private ArrayList<CompletionItem> findAllStructFields(BLangRecordLiteral recordLiteral,
                                                     LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        DiagnosticPos nodePos = CommonUtil.toZeroBasedPosition(recordLiteral.getPosition());
        int line = context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int nodeStartLine = nodePos.getStartLine();
        int nodeEndLine = nodePos.getEndLine();

        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            if (keyValuePair.valueExpr.type instanceof BStructType) {
                DiagnosticPos exprPos = CommonUtil.toZeroBasedPosition(keyValuePair.valueExpr.getPosition());
                int exprStartLine = exprPos.getStartLine();
                int exprEndLine = exprPos.getEndLine();

                if (exprStartLine < line && exprEndLine > line
                        && keyValuePair.valueExpr instanceof BLangRecordLiteral) {
                    return findAllStructFields((BLangRecordLiteral) keyValuePair.valueExpr, context);
                }
            }
        }
        
        if (nodeStartLine < line && nodeEndLine > line && recordLiteral.type instanceof BStructType) {
            completionItems.addAll(getFieldCompletionItems(((BStructType) recordLiteral.type).fields));
            completionItems.add(getFillAllOptionItem(((BStructType) recordLiteral.type).fields));
        }
        
        return completionItems;
    }
    
    private CompletionItem getFillAllOptionItem(List<BStructType.BStructField> fields) {
        List<String> fieldEntries = new ArrayList<>();
        
        fields.forEach(bStructField -> {
            String defaultFieldEntry = bStructField.getName().getValue()
                    + UtilSymbolKeys.PKG_DELIMITER_KEYWORD + getDefaultValueForType(bStructField.getType());
            fieldEntries.add(defaultFieldEntry);
        });
        
        String insertText = String.join(("," + LINE_SEPARATOR), fieldEntries);
        String label = "Add All Attributes";
        
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(label);
        completionItem.setInsertText(insertText);
        completionItem.setSortText(Priority.PRIORITY110.toString());
        
        return completionItem;
    }
}
