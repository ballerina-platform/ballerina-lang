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

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import org.ballerinalang.langserver.commons.SnippetContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Carries a set of utilities used in default value generation.
 *
 * @since 2201.1.1
 */
public class DefaultValueGenerationUtil {

    /**
     * Get the default value for the given BType.
     *
     * @param bType              Type descriptor to get the default value
     * @param snippetContext     Snippet context
     * @return {@link String}   Default value as a String
     */
    public static Optional<String> getDefaultValueForType(TypeSymbol bType, SnippetContext snippetContext) {
        return getDefaultValueForType(bType, true, snippetContext);
    }

    public static Optional<String> getDefaultPlaceholderForType(TypeSymbol bType) {
        return getDefaultValueForType(bType)
                .map(defaultValue -> defaultValue.replace("}", "\\}"));
    }

    /**
     * Get the default value for the given BType.
     *
     * @param bType Type descriptor to get the default value
     * @return {@link String}   Default value as a String
     */
    public static Optional<String> getDefaultValueForType(TypeSymbol bType) {
        return getDefaultValueForType(bType, false, new SnippetContext());
    }
    
    /**
     * Used to get the default values for a {@link TypeDescKind}. {@link #getDefaultValueForType(TypeSymbol)}
     * is preferred over this function. This function should be used as a compliment to .
     *
     * @param typeKind Type desc kind
     * @return Optional default value
     * @see #getDefaultValueForType(TypeSymbol)
     */
    public static Optional<String> getDefaultValueForTypeDescKind(TypeDescKind typeKind) {
        String defaultValue = null;
        switch (typeKind) {
            case FLOAT:
                defaultValue = Float.toString(0);
                break;
            case BOOLEAN:
                defaultValue = Boolean.toString(false);
                break;
            case RECORD:
            case MAP:
                defaultValue = "{}";
                break;
            case STREAM:
                defaultValue = "new ()";
                break;
            case XML:
                defaultValue = "xml ``";
                break;
            case DECIMAL:
                defaultValue = Integer.toString(0);
                break;
            case ARRAY:
                defaultValue = "[]";
                break;
            case SINGLETON:
                defaultValue = "\"\"";
                break;
            case ERROR:
                defaultValue = "error(\"\")";
                break;
            default:
                if (typeKind.isIntegerType()) {
                    defaultValue = Integer.toString(0);
                    break;
                } else if (typeKind.isStringType()) {
                    defaultValue = "\"\"";
                    break;
                }
        }
        return Optional.ofNullable(defaultValue);
    }

    /**
     * Used to get the default values for {@link TypeDescKind} of a {@link TypeSymbol}.
     * {@link #getDefaultValueForType(TypeSymbol)} is preferred over this function.
     * This function should be used as a compliment to .
     *
     * @param typeSymbol Type symbol
     * @return Optional default value
     * @see #getDefaultValueForType(TypeSymbol)
     */
    public static Optional<String> getDefaultValueForTypeDescKind(TypeSymbol typeSymbol) {
        String defaultValue;
        TypeDescKind typeKind = typeSymbol.typeKind();
        switch (typeKind) {
            case SINGLETON:
                defaultValue = typeSymbol.signature();
                break;
            default:
                defaultValue = getDefaultValueForTypeDescKind(typeKind).orElse(null);
        }
        return Optional.ofNullable(defaultValue);
    }

    private static String generateSnippetEntry(String value, int offset) {
        return "${" + offset + ":" + value + "}";
    }

    private static Optional<String> getDefaultValueForType(TypeSymbol bType, boolean isSnippet,
                                                           SnippetContext context) {
        String valueString;
        if (bType == null) {
            return Optional.empty();
        }

        TypeSymbol rawType = CommonUtil.getRawType(bType);
        TypeDescKind typeKind = rawType.typeKind();
        switch (typeKind) {
            case TUPLE:
                TupleTypeSymbol tupleType = (TupleTypeSymbol) rawType;
                List<String> memberDefaultValues = tupleType.memberTypeDescriptors().stream()
                        .map(member -> getDefaultValueForTypeDescKind(member).orElse(""))
                        .collect(Collectors.toList());

                if (memberDefaultValues.isEmpty()) {
                    valueString = isSnippet ? "[${" + context.incrementAndGetPlaceholderCount() + "}]" : "[]";
                    break;
                }
                List<String> memberSnippets = new ArrayList<>();
                for (String value : memberDefaultValues) {
                    memberSnippets.add(isSnippet ?
                            generateSnippetEntry(value, context.incrementAndGetPlaceholderCount()) : value);
                }
                valueString = "[" + String.join(", ", memberSnippets) + "]";
                context.incrementAndGetPlaceholderCount();
                break;
            case ARRAY:
                // Filler value of an array is []
                ArrayTypeSymbol arrayType = (ArrayTypeSymbol) rawType;
                if (arrayType.memberTypeDescriptor().typeKind() == TypeDescKind.ARRAY) {
                    String value = getDefaultValueForTypeDescKind(arrayType.memberTypeDescriptor())
                            .orElse("");
                    valueString = "[" + (isSnippet ?
                            generateSnippetEntry(value, context.incrementAndGetPlaceholderCount()) : value) + "]";
                    break;
                }
                valueString = isSnippet ? "[${" + context.incrementAndGetPlaceholderCount() + "}]" : "[]";
                break;
            case RECORD:
                // TODO: Here we have disregarded the formatting of the record fields. Need to consider that in future
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) rawType;
                List<String> fieldInsertText = new ArrayList<>();
                valueString = "{";
                List<RecordFieldSymbol> mandatoryFieldSymbols = RecordUtil
                        .getMandatoryRecordFields(recordTypeSymbol).stream()
                        .filter(recordFieldSymbol -> recordFieldSymbol.getName().isPresent())
                        .collect(Collectors.toList());
                for (RecordFieldSymbol mandatoryField : mandatoryFieldSymbols) {
                    String value = getDefaultValueForTypeDescKind(CommonUtil.getRawType(mandatoryField
                            .typeDescriptor())).orElse("");
                    fieldInsertText.add(mandatoryField.getName().get() + ": " + (isSnippet ?
                            generateSnippetEntry(value, context.incrementAndGetPlaceholderCount()) : value));
                }
                valueString += String.join(", ", fieldInsertText);
                valueString += "}";
                break;
            case OBJECT:
                ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;
                if (objectTypeSymbol.kind() == SymbolKind.CLASS) {
                    ClassSymbol classSymbol = (ClassSymbol) objectTypeSymbol;
                    if (classSymbol.initMethod().isPresent()) {
                        List<ParameterSymbol> params = classSymbol.initMethod().get().typeDescriptor().params().get();
                        List<String> paramSnippets = new ArrayList<>();
                        for (ParameterSymbol param : params) {
                            String value =
                                    getDefaultValueForTypeDescKind(CommonUtil.getRawType(param.typeDescriptor()))
                                            .orElse("");
                            paramSnippets.add(isSnippet ?
                                    generateSnippetEntry(value, context.incrementAndGetPlaceholderCount()) : value);
                        }
                        valueString = "new (" + String.join(", ", paramSnippets) + ")";
                    } else {
                        valueString = isSnippet ?
                                "${" + context.incrementAndGetPlaceholderCount() + ":new ()}" : "new ()";
                    }
                } else {
                    valueString = isSnippet ?
                            "${" + context.incrementAndGetPlaceholderCount() + ":object {}}" : "object {}";
                }
                break;
            case UNION:
                List<TypeSymbol> members =
                        new ArrayList<>(((UnionTypeSymbol) rawType).memberTypeDescriptors());
                List<TypeSymbol> nilMembers = members.stream()
                        .filter(member -> member.typeKind() == TypeDescKind.NIL).collect(Collectors.toList());
                if (nilMembers.isEmpty()) {
                    valueString = getDefaultValueForTypeDescKind(CommonUtil.getRawType(members.get(0))).orElse("");
                    valueString = isSnippet ?
                            generateSnippetEntry(valueString, context.incrementAndGetPlaceholderCount()) : valueString;
                } else {
                    valueString = isSnippet ?
                            generateSnippetEntry("()", context.incrementAndGetPlaceholderCount()) : "()";
                }
                break;
            case INTERSECTION:
                TypeSymbol effectiveType = ((IntersectionTypeSymbol) rawType).effectiveTypeDescriptor();
                effectiveType = CommonUtil.getRawType(effectiveType);
                if (effectiveType.typeKind() == TypeDescKind.INTERSECTION) {
                    // Right now, intersection types can only have readonly and another type only. Therefore, not doing 
                    // further checks here. Get the member type from intersection which is not readonly and get its 
                    // default value
                    valueString = isSnippet ?
                            generateSnippetEntry("()", context.incrementAndGetPlaceholderCount()) : "()";
                    Optional<TypeSymbol> memberType = ((IntersectionTypeSymbol) effectiveType)
                            .memberTypeDescriptors().stream()
                            .filter(typeSymbol -> typeSymbol.typeKind() != TypeDescKind.READONLY)
                            .findAny();
                    if (memberType.isPresent()) {
                        valueString =
                                getDefaultValueForTypeDescKind(CommonUtil.getRawType(memberType.get())).orElse("");
                        valueString = isSnippet ? generateSnippetEntry(valueString,
                                context.incrementAndGetPlaceholderCount()) : valueString;
                    }
                } else {
                    valueString = getDefaultValueForTypeDescKind(CommonUtil.getRawType(effectiveType))
                            .orElse("");
                    valueString = isSnippet ?
                            generateSnippetEntry(valueString, context.incrementAndGetPlaceholderCount()) : valueString;
                }
                break;
            case TABLE:
                TypeSymbol rowType = ((TableTypeSymbol) rawType).rowTypeParameter();
                String rowValue = getDefaultValueForTypeDescKind(rowType).orElse("");
                valueString = "table [" + (isSnippet ?
                        generateSnippetEntry(rowValue, context.incrementAndGetPlaceholderCount()) : rowValue) + "]";
                break;
            case ERROR:
                TypeSymbol errorType = CommonUtil.getRawType(((ErrorTypeSymbol) rawType).detailTypeDescriptor());
                StringBuilder errorString = new StringBuilder("error(\"\"");
                if (errorType.typeKind() == TypeDescKind.RECORD) {
                    List<RecordFieldSymbol> mandatoryFields = RecordUtil
                            .getMandatoryRecordFields((RecordTypeSymbol) errorType);
                    if (!mandatoryFields.isEmpty()) {
                        errorString.append(", ");
                        List<String> detailFieldSnippets = new ArrayList<>();
                        for (RecordFieldSymbol field : mandatoryFields) {
                            String defValue =
                                    getDefaultValueForTypeDescKind(CommonUtil.getRawType(field.typeDescriptor()))
                                            .orElse("");
                            detailFieldSnippets.add(field.getName().get() + " = " + (isSnippet
                                    ? generateSnippetEntry(defValue, context.incrementAndGetPlaceholderCount())
                                    : defValue));
                        }
                        errorString.append(String.join(", ", detailFieldSnippets));
                    }
                }
                errorString.append(")");
                valueString = errorString.toString();
                break;
            case SINGLETON:
                valueString = rawType.signature();
                valueString = isSnippet ?
                        generateSnippetEntry(valueString, context.incrementAndGetPlaceholderCount()) : valueString;
                break;
            case JSON:
            case NIL:
            case ANY:
                valueString = "()";
                valueString = isSnippet ?
                        generateSnippetEntry(valueString, context.incrementAndGetPlaceholderCount()) : valueString;
                break;
            default:
                Optional<String> value = getDefaultValueForTypeDescKind(typeKind);
                if (value.isEmpty()) {
                    return value;
                }
                valueString = isSnippet ?
                        generateSnippetEntry(value.get(), context.incrementAndGetPlaceholderCount()) : value.get();
        }

        return Optional.of(valueString);
    }
}
