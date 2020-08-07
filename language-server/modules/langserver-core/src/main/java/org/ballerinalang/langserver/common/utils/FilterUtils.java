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
package org.ballerinalang.langserver.common.utils;

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.CommonToken;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

/**
 * Utilities for filtering the symbols from completion context and symbol information lists.
 */
public class FilterUtils {

    /**
     * Get the actions defined over and endpoint.
     *
     * @param objectTypeSymbol Endpoint variable symbol to evaluate
     * @return {@link List} List of extracted actions as Symbol Info
     */
    public static List<Scope.ScopeEntry> getClientActions(BObjectTypeSymbol objectTypeSymbol) {
        return objectTypeSymbol.methodScope.entries.values().stream()
                .filter(scopeEntry -> (scopeEntry.symbol.flags & Flags.REMOTE) == Flags.REMOTE)
                .collect(Collectors.toList());
    }

    /**
     * Get the variable symbol info by the name.
     *
     * @param name    name of the variable
     * @param symbols list of symbol info
     * @return {@link org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry} Scope Entry extracted
     */
    public static Scope.ScopeEntry getVariableByName(String name, List<Scope.ScopeEntry> symbols) {
        return symbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol.name.getValue().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static Optional<BSymbol> getBTypeEntry(Scope.ScopeEntry entry) {
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) == SymTag.TYPE) {
                if (!CommonUtil.symbolContainsInvalidChars(entry.symbol) && entry.symbol instanceof BTypeSymbol) {
                    return Optional.of(entry.symbol);
                }
            }
            entry = entry.next;
        }
        return Optional.empty();
    }

    public static boolean isBTypeEntry(Scope.ScopeEntry entry) {
        return getBTypeEntry(entry).isPresent();
    }

    ///////////////////////////
    ///// Private Methods /////
    ///////////////////////////

    private static BType getBTypeForUnionType(BUnionType bType) {
        List<BType> memberTypeList = new ArrayList<>(bType.getMemberTypes());
        memberTypeList.removeIf(type -> type.tsymbol instanceof BErrorTypeSymbol || type instanceof BNilType);

        if (memberTypeList.size() == 1) {
            return memberTypeList.get(0);
        }

        return bType;
    }

    private static List<Scope.ScopeEntry> loadActionsFunctionsAndTypesFromScope(Map<Name, Scope.ScopeEntry> entryMap) {
        List<Scope.ScopeEntry> actionFunctionList = new ArrayList<>();
        entryMap.forEach((name, scopeEntry) -> {
            BSymbol symbol = scopeEntry.symbol;
            if (((symbol instanceof BInvokableSymbol && ((BInvokableSymbol) symbol).receiverSymbol == null)
                    || isBTypeEntry(scopeEntry)
                    || symbol instanceof BVarSymbol)
                    && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                actionFunctionList.add(scopeEntry);
            }
        });

        return actionFunctionList;
    }

    private static BType getModifiedBType(BSymbol bSymbol, LSContext context, InvocationFieldType fieldType) {
        Integer invocationType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        BType actualType;
        if ((bSymbol.tag & SymTag.TYPE) == SymTag.TYPE) {
            actualType = new BTypedescType(bSymbol.type, null);
        } else if (bSymbol instanceof BInvokableSymbol) {
            actualType = ((BInvokableSymbol) bSymbol).retType;
        } else if (bSymbol.type instanceof BArrayType && fieldType == InvocationFieldType.ARRAY_ACCESS) {
            return ((BArrayType) bSymbol.type).eType;
        } else if (bSymbol.type instanceof BMapType && fieldType == InvocationFieldType.ARRAY_ACCESS) {
            LinkedHashSet<BType> types = new LinkedHashSet<>();
            types.add(((BMapType) bSymbol.type).constraint);
            types.add(new BNilType());
            actualType = BUnionType.create(((BMapType) bSymbol.type).constraint.tsymbol, types);
        } else {
            actualType = bSymbol.type;
        }
        return actualType instanceof BUnionType && invocationType == BallerinaParser.NOT ?
                getBTypeForUnionType((BUnionType) actualType) : actualType;
    }

    private static List<ChainedFieldModel> getInvocationFieldList(List<CommonToken> defaultTokens, int startIndex,
                                                                  boolean captureValidField) {
        int traverser = startIndex;
        int rightBracketCount = 0;
        int gtCount = 0;
        boolean invocation = false;
        boolean arrayAccess = false;
        List<ChainedFieldModel> fieldList = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\w+$");
        boolean captureNextValidField = captureValidField;
        while (traverser >= 0) {
            CommonToken token = defaultTokens.get(traverser);
            int type = token.getType();
            Matcher matcher = pattern.matcher(token.getText());
            boolean foundMatch = matcher.find();
            if (type == BallerinaParser.RIGHT_PARENTHESIS) {
                Pair<Boolean, Integer> isGroupedExpr = isGroupedExpression(defaultTokens, traverser - 1);
                if (isGroupedExpr.getLeft()) {
                    List<CommonToken> subList = defaultTokens.subList(isGroupedExpr.getRight() + 1, traverser);
                    List<ChainedFieldModel> groupedFields = getInvocationFieldList(subList, subList.size() - 1,
                            captureNextValidField);
                    fieldList.addAll(Lists.reverse(groupedFields));
                } else {
                    invocation = true;
                }
                traverser = isGroupedExpr.getRight() - 1;
            } else if (type == BallerinaParser.RIGHT_BRACKET) {
                // Mapped to both xml and array variables
                if (!arrayAccess) {
                    arrayAccess = true;
                }
                rightBracketCount++;
                traverser--;
            } else if (type == BallerinaParser.LEFT_PARENTHESIS
                    || type == BallerinaParser.DIV || type == BallerinaParser.MUL || (foundMatch && gtCount > 0)) {
                // DIV and MUL added in order to skip the xml navigation
                // Also we do not capture the tokens within the xml tags (when navigating xml)
                // ex: xmlVal/<hello>.*.
                traverser--;
            } else if (type == BallerinaParser.LEFT_BRACKET && rightBracketCount > 0) {
                // Mapped to array variables
                rightBracketCount--;
                traverser--;
            } else if (type == BallerinaParser.GT) {
                gtCount++;
                traverser--;
            } else if (type == BallerinaParser.LT) {
                gtCount--;
                traverser--;
            } else if (type == BallerinaParser.DOT || type == BallerinaParser.NOT
                    || type == BallerinaParser.OPTIONAL_FIELD_ACCESS || rightBracketCount > 0) {
                captureNextValidField = true;
                traverser--;
            } else if (foundMatch && rightBracketCount == 0
                    && captureNextValidField) {
                InvocationFieldType fieldType;
                CommonToken packageAlias = null;
                traverser--;

                if (invocation) {
                    fieldType = InvocationFieldType.INVOCATION;
                    invocation = false;
                } else if (arrayAccess) {
                    // Mapped to both xml and array variables
                    fieldType = InvocationFieldType.ARRAY_ACCESS;
                    arrayAccess = false;
                } else {
                    fieldType = InvocationFieldType.FIELD;
                }

                if (traverser > 0 && defaultTokens.get(traverser).getType() == BallerinaParser.COLON) {
                    packageAlias = defaultTokens.get(traverser - 1);
                }
                ChainedFieldModel model = new ChainedFieldModel(fieldType, token, packageAlias);
                fieldList.add(model);
                captureNextValidField = false;
            } else {
                break;
            }
        }

        return Lists.reverse(fieldList);
    }

    private static Pair<Boolean, Integer> isGroupedExpression(List<CommonToken> defaultTokens, int startIndex) {
        int traverser = startIndex;
        int rightParenCount = 0;
        Pattern pattern = Pattern.compile("^\\w+$");
        while (true) {
            int type = defaultTokens.get(traverser).getType();
            if (type == BallerinaParser.RIGHT_PARENTHESIS) {
                rightParenCount++;
            } else if (type == BallerinaParser.LEFT_PARENTHESIS) {
                if (rightParenCount == 0) {
                    if (traverser <= 0) {
                        return Pair.of(true, traverser);
                    } else {
                        CommonToken tokenBefore = defaultTokens.get(traverser - 1);
                        Matcher matcher = pattern.matcher(tokenBefore.getText());
                        return Pair.of(!matcher.find(), traverser);
                    }
                } else {
                    rightParenCount--;
                }
            }
            traverser--;
        }
    }

    public static List<Scope.ScopeEntry> getObjectMethodsAndFields(LSContext context,
                                                                   BObjectTypeSymbol objectSymbol,
                                                                   String symbolName) {
        String currentModule = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String objectOwnerModule = objectSymbol.pkgID.getName().getValue();
        boolean symbolInCurrentModule = currentModule.equals(objectOwnerModule);

        // Extract the method entries
        Map<Name, Scope.ScopeEntry> entries = objectSymbol.methodScope.entries.entrySet().stream()
                .filter(entry -> {
                    BSymbol entrySymbol = entry.getValue().symbol;
                    boolean isPrivate = (entrySymbol.flags & Flags.PRIVATE) == Flags.PRIVATE;
                    boolean isPublic = (entrySymbol.flags & Flags.PUBLIC) == Flags.PUBLIC;

                    return !((entrySymbol.getName().getValue().contains(".init") && !"self".equals(symbolName))
                            || (isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Extract Field Entries
        Map<Name, Scope.ScopeEntry> fieldEntries = objectSymbol.scope.entries.entrySet().stream()
                .filter(entry -> {
                    BSymbol entrySymbol = entry.getValue().symbol;
                    boolean isPrivate = (entrySymbol.flags & Flags.PRIVATE) == Flags.PRIVATE;
                    boolean isPublic = (entrySymbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
                    return !((isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        entries.putAll(fieldEntries);

        return new ArrayList<>(entries.values());
    }

    /**
     * When given a union of record types, iterate over the member types to extract the fields with the same name.
     * If a certain field with the same name contains in all records we extract the field entries
     *
     * @param unionType union type to evaluate
     * @param context   Language server operation context
     * @return {@link Map} map of scope entries
     */
    public static List<Scope.ScopeEntry> getInvocationsAndFieldsForUnionType(BUnionType unionType,
                                                                             LSContext context) {
        ArrayList<BType> memberTypes = new ArrayList<>(unionType.getMemberTypes());
        List<Scope.ScopeEntry> resultEntries = new ArrayList<>();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
//        Integer invocationTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        // check whether union consists of same type tag symbols
        BType firstMemberType = memberTypes.get(0);
        boolean allMatch = memberTypes.stream().allMatch(bType -> bType.tag == firstMemberType.tag);
        // If all the members are not same types we stop proceeding
        if (!allMatch) {
            resultEntries.addAll(getLangLibScopeEntries(unionType, symbolTable, types));
            return resultEntries;
        }

        resultEntries.addAll(getLangLibScopeEntries(firstMemberType, symbolTable, types));
        if (firstMemberType.tag == TypeTags.RECORD) {
            // Keep track of the occurrences of each of the field names
            LinkedHashMap<String, Integer> memberOccurrenceCounts = new LinkedHashMap<>();
            List<Name> firstMemberFieldKeys = new ArrayList<>();
        /*
        We keep only the name fields of the first member type since a field has to appear in all the member types
         */
            for (int memberCounter = 0; memberCounter < memberTypes.size(); memberCounter++) {
                int finalMemberCounter = memberCounter;
                ((BRecordType) memberTypes.get(memberCounter)).tsymbol.scope.entries.keySet()
                        .forEach(name -> {
                            if (memberOccurrenceCounts.containsKey(name.value)) {
                                memberOccurrenceCounts.put(name.value, memberOccurrenceCounts.get(name.value) + 1);
                            } else if (finalMemberCounter == 0) {
                                // Fields are only captured for the first member type, otherwise the count is increased
                                firstMemberFieldKeys.add(name);
                                memberOccurrenceCounts.put(name.value, 1);
                            }
                        });
            }
            if (memberOccurrenceCounts.size() == 0) {
                return resultEntries;
            }
            List<Integer> counts = new ArrayList<>(memberOccurrenceCounts.values());
            Map<Name, Scope.ScopeEntry> firstMemberEntries = ((BRecordType) firstMemberType).tsymbol.scope.entries;
            for (int i = 0; i < counts.size(); i++) {
                if (counts.get(i) != memberTypes.size()) {
                    continue;
                }
                Name name = firstMemberFieldKeys.get(i);
                BSymbol symbol = firstMemberEntries.get(name).symbol;
                if (firstMemberType.tag == TypeTags.RECORD /*&& (invocationTokenType == BallerinaParser.DOT
                        || invocationTokenType == BallerinaParser.NOT)*/
                        && (org.ballerinalang.jvm.util.Flags.isFlagOn(symbol.flags, Flags.OPTIONAL))) {
                    continue;
                }
                resultEntries.add(firstMemberEntries.get(name));
            }
        }

        return resultEntries;
    }

    private static Optional<Scope.ScopeEntry> getScopeEntryWithName(Map<Name, Scope.ScopeEntry> entries,
                                                                    ChainedFieldModel fieldModel) {
        return entries.values().stream().filter(scopeEntry -> {
            BSymbol symbol = scopeEntry.symbol;
            String[] symbolNameComponents = symbol.getName().getValue().split("\\.");
            String symbolName = symbolNameComponents[symbolNameComponents.length - 1];
            if (!symbolName.equals(fieldModel.name.getText())) {
                return false;
            }
            return (fieldModel.fieldType == InvocationFieldType.INVOCATION && symbol instanceof BInvokableSymbol)
                    || (fieldModel.fieldType == InvocationFieldType.FIELD && !(symbol instanceof BInvokableSymbol));

        }).findAny();
    }

    /**
     * Retrieve lang-lib scope entries for this typeTag.
     *
     * @param bType    {@link BType}
     * @param symTable {@link SymbolTable}
     * @param types    {@link Types} analyzer
     * @return map of scope entries
     */
    public static List<Scope.ScopeEntry> getLangLibScopeEntries(BType bType, SymbolTable symTable, Types types) {
        List<Scope.ScopeEntry> entries = new ArrayList<>();
        entries.addAll(symTable.langValueModuleSymbol.scope.entries.values());
        entries.addAll(symTable.langQueryModuleSymbol.scope.entries.values());
        switch (bType.tag) {
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                entries.addAll(symTable.langArrayModuleSymbol.scope.entries.values());
                break;
            case TypeTags.DECIMAL:
                entries.addAll(symTable.langDecimalModuleSymbol.scope.entries.values());
                break;
            case TypeTags.ERROR:
                entries.addAll(symTable.langErrorModuleSymbol.scope.entries.values());
                break;
            case TypeTags.FLOAT:
                entries.addAll(symTable.langFloatModuleSymbol.scope.entries.values());
                break;
            case TypeTags.FUTURE:
                entries.addAll(symTable.langFutureModuleSymbol.scope.entries.values());
                break;
            case TypeTags.INT:
                entries.addAll(symTable.langIntModuleSymbol.scope.entries.values());
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                entries.addAll(symTable.langMapModuleSymbol.scope.entries.values());
                return entries;
            case TypeTags.OBJECT:
                entries.addAll(symTable.langObjectModuleSymbol.scope.entries.values());
                break;
            case TypeTags.STREAM:
                entries.addAll(symTable.langStreamModuleSymbol.scope.entries.values());
                break;
            case TypeTags.TABLE:
                entries.addAll(symTable.langTableModuleSymbol.scope.entries.values());
                break;
            case TypeTags.STRING:
                entries.addAll(symTable.langStringModuleSymbol.scope.entries.values());
                break;
            case TypeTags.TYPEDESC:
                entries.addAll(symTable.langTypedescModuleSymbol.scope.entries.values());
                break;
            case TypeTags.XML:
                entries.addAll(symTable.langXmlModuleSymbol.scope.entries.values());
                break;
            case TypeTags.BOOLEAN:
                entries.addAll(symTable.langBooleanModuleSymbol.scope.entries.values());
                break;
            default:
                break;
        }

        return entries.stream().filter(entry -> {
            BSymbol symbol = entry.symbol;
            if (symbol instanceof BInvokableSymbol) {
                List<BVarSymbol> params = ((BInvokableSymbol) symbol).params;
                return params.isEmpty() || params.get(0).type.tag == bType.tag ||
                        (types.isAssignable(bType, params.get(0).type));
            }
            return symbol.kind != null && symbol.kind != SymbolKind.OBJECT;
        }).collect(Collectors.toList());
    }

    private static Predicate<Map.Entry<Name, Scope.ScopeEntry>> optionalFieldFilter(BType symbolType,
                                                                                    Integer invocationTkn,
                                                                                    LSContext context) {
        BLangNode scope = context.get(CompletionKeys.SCOPE_NODE_KEY);
        List<Integer> defaultTokenTypes = context.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY)
                .stream()
                .map(CommonToken::getType).collect(Collectors.toList());


        return entry -> {
            if (symbolType.tag == TypeTags.RECORD && (invocationTkn == BallerinaParser.DOT
                    || invocationTkn == BallerinaParser.NOT) && (scope instanceof BLangBlockStmt
                    || scope instanceof BLangFunctionBody) && defaultTokenTypes.contains(BallerinaParser.ASSIGN)) {
                return !org.ballerinalang.jvm.util.Flags.isFlagOn(entry.getValue().symbol.flags,
                        Flags.OPTIONAL);
            }
            return true;
        };
    }

    /**
     * Data model for the chained field.
     */
    private static class ChainedFieldModel {
        InvocationFieldType fieldType;
        CommonToken name;
        CommonToken pkgAlias;

        ChainedFieldModel(InvocationFieldType fieldType, CommonToken name, CommonToken pkgAlias) {
            this.fieldType = fieldType;
            this.name = name;
            this.pkgAlias = pkgAlias;
        }
    }

    /**
     * Enum for chained field type.
     */
    public enum InvocationFieldType {
        FIELD,
        INVOCATION,
        ARRAY_ACCESS
    }
}
