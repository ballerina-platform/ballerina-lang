/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.RecordFieldCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonKeys.PKG_DELIMITER_KEYWORD;

/**
 * Carries a set of utilities used to derive record field completions.
 *
 * @since 2201.1.1
 */
public class RecordUtil {

    /**
     * Get completion items list for struct fields.
     *
     * @param context Language server operation context
     * @param fields  Map of field descriptors
     * @param wrapper  Pair of Raw TypeSymbol and broader TypeSymbol
     * @return {@link List} List of completion items for the struct fields
     */
    public static List<LSCompletionItem> getRecordFieldCompletionItems(BallerinaCompletionContext context,
                                                                       Map<String, RecordFieldSymbol> fields,
                                                                       RawTypeSymbolWrapper wrapper) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        fields.forEach((name, field) -> {
            String insertText =
                    getRecordFieldCompletionInsertText(field, 1);

            String detail;
            if (wrapper.getRawType().getName().isPresent()) {
                detail = NameUtil.getModifiedTypeName(context, wrapper.getRawType()) + "." + name;
            } else if (wrapper.getBroaderType().getName().isPresent()) {
                detail = NameUtil.getModifiedTypeName(context, wrapper.getBroaderType()) + "." + name;
            } else {
                detail = "(" + wrapper.getRawType().signature() + ")." + name;
            }
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(insertText);
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(name);
            fieldItem.setKind(CompletionItemKind.Field);
            fieldItem.setSortText(Priority.PRIORITY120.toString());
            completionItems.add(new RecordFieldCompletionItem(context, field, fieldItem, detail));
        });

        return completionItems;
    }

    /**
     * Get the completion items to fill all record fields.
     *
     * @param context Language Server Operation Context
     * @param fields  A non empty map of fields
     * @param wrapper  A wrapper containing record type symbol and broader type symbol
     * @return {@link Optional}   Completion Item to fill all the options
     */
    public static Optional<LSCompletionItem> getFillAllRecordFieldCompletionItems(
            BallerinaCompletionContext context,
            Map<String, RecordFieldSymbol> fields,
            RawTypeSymbolWrapper<RecordTypeSymbol> wrapper) {
        List<String> fieldEntries = new ArrayList<>();

        Map<String, RecordFieldSymbol> requiredFields = new HashMap<>();
        for (Map.Entry<String, RecordFieldSymbol> entry : fields.entrySet()) {
            if (!entry.getValue().isOptional() && !entry.getValue().hasDefaultValue()) {
                requiredFields.put(CommonUtil.escapeReservedKeyword(entry.getKey()), entry.getValue());
            }
        }

        String label;
        String detail = NameUtil.getRecordTypeName(context, wrapper);
        if (!requiredFields.isEmpty()) {
            label = "Fill " + detail + " Required Fields";
            int count = 1;
            for (Map.Entry<String, RecordFieldSymbol> entry : requiredFields.entrySet()) {
                String fieldEntry = entry.getKey()
                        + PKG_DELIMITER_KEYWORD + " "
                        + DefaultValueGenerationUtil.getDefaultValueForType(entry.getValue().typeDescriptor(), count)
                        .orElse(" ");
                fieldEntries.add(fieldEntry);
                count++;
            }

            String insertText = String.join(("," + CommonUtil.LINE_SEPARATOR), fieldEntries);
            CompletionItem completionItem = new CompletionItem();
            completionItem.setFilterText("fill");
            completionItem.setLabel(label);
            completionItem.setInsertText(insertText);
            completionItem.setDetail(detail);
            completionItem.setKind(CompletionItemKind.Property);
            completionItem.setSortText(Priority.PRIORITY110.toString());

            return Optional.of(new StaticCompletionItem(context, completionItem, StaticCompletionItem.Kind.OTHER));
        }

        return Optional.empty();
    }

    /**
     * Get the insert text to fill the record fields.
     *
     * @param fields A non-empty map of fields
     * @return Insert text to fill the record fields
     */
    public static String getFillAllRecordFieldInsertText(Map<String, RecordFieldSymbol> fields) {
        List<String> fieldEntries = new ArrayList<>();
        Map<String, RecordFieldSymbol> requiredFields = new HashMap<>();
        for (Map.Entry<String, RecordFieldSymbol> entry : fields.entrySet()) {
            if (!entry.getValue().isOptional() && !entry.getValue().hasDefaultValue()) {
                requiredFields.put(CommonUtil.escapeReservedKeyword(entry.getKey()), entry.getValue());
            }
        }

        String insertText = "";
        if (!requiredFields.isEmpty()) {
            for (Map.Entry<String, RecordFieldSymbol> entry : requiredFields.entrySet()) {
                String fieldEntry = entry.getKey()
                        + PKG_DELIMITER_KEYWORD + " "
                        + DefaultValueGenerationUtil.getDefaultValueForType(entry.getValue().typeDescriptor())
                        .orElse(" ");
                fieldEntries.add(fieldEntry);
            }

            insertText = String.join(", ", fieldEntries);
        }
        return insertText;
    }

    /**
     * Returns the fields of the record.
     *
     * @param wrapper        A wrapper containing record type wrapper and broader type wrapper
     * @param existingFields Existing record fields
     * @return Fields of the record
     */
    public static Map<String, RecordFieldSymbol> getRecordFields(RawTypeSymbolWrapper<RecordTypeSymbol> wrapper,
                                                                 List<String> existingFields) {
        return wrapper.getRawType().fieldDescriptors().entrySet().stream()
                .filter(e -> !existingFields.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Extract the required fields from the records.
     *
     * @param recordType record type descriptor to evaluate
     * @return {@link List} of required fields captured
     */
    public static List<RecordFieldSymbol> getMandatoryRecordFields(RecordTypeSymbol recordType) {
        return recordType.fieldDescriptors().values().stream()
                .filter(field -> !field.hasDefaultValue() && !field.isOptional())
                .collect(Collectors.toList());
    }

    /**
     * Get the completion item insert text for a BField.
     *
     * @param bField BField to evaluate
     * @return {@link String} Insert text
     */
    public static String getRecordFieldCompletionInsertText(RecordFieldSymbol bField, int tabOffset) {

        StringBuilder insertText = new StringBuilder(CommonUtil.escapeReservedKeyword(bField.getName().get()) + ": ");
        insertText.append(DefaultValueGenerationUtil.getDefaultValueForType(bField.typeDescriptor(), tabOffset)
                .orElse(" "));
        return insertText.toString();
    }

    /**
     * Given a type symbol, this method will get the record type symbols in the provided type. i.e. the provided type
     * can be a record or a union of records for this to work.
     *
     * @param typeSymbol Type symbol from which record types need to be extracted.
     * @return List of record type symbols, wrapped with a raw type and broader type container.
     */
    public static List<RawTypeSymbolWrapper<RecordTypeSymbol>> getRecordTypeSymbols(TypeSymbol typeSymbol) {
        TypeSymbol rawType = CommonUtil.getRawType(typeSymbol);
        if (rawType.typeKind() == TypeDescKind.RECORD) {
            return Collections.singletonList(RawTypeSymbolWrapper.from(typeSymbol, (RecordTypeSymbol) rawType));
        }
        if (rawType.typeKind() == TypeDescKind.UNION) {
            // This will only consider the record type members and disregard other types
            return ((UnionTypeSymbol) rawType).memberTypeDescriptors().stream()
                    .filter(tSymbol -> CommonUtil.getRawType(tSymbol).typeKind() == TypeDescKind.RECORD)
                    .map(tSymbol -> {
                        RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) CommonUtil.getRawType(tSymbol);
                        return RawTypeSymbolWrapper.from(tSymbol, recordTypeSymbol);
                    }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
