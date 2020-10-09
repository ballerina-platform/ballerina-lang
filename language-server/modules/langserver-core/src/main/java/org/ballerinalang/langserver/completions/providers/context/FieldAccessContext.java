/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.UnionTypeDescriptor;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.FieldCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generic Completion provider for field access providers.
 * eg: Optional Field access and Field Access
 *
 * @param <T> Field access node type
 * @since 2.0.0
 */
public abstract class FieldAccessContext<T extends Node> extends AbstractCompletionProvider<T> {

    public FieldAccessContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    /**
     * Get the entries for the given field access expression.
     * This particular logic is written in order to capture the chain completion usage as well.
     *
     * @param ctx         language server operation context
     * @param baseEntries base entries to start the field/ entry search
     * @param expr        expression node to evaluate
     * @return {@link List} of filtered scope entries
     */
    protected List<LSCompletionItem> getEntries(LSContext ctx, List<Symbol> baseEntries, ExpressionNode expr) {
//        switch (expr.kind()) {
//            case SIMPLE_NAME_REFERENCE:
//                /*
//                Captures the following
//                (1) fieldName.<cursor>
//                (2) fieldName.t<cursor>
//                 */
//                String name = ((SimpleNameReferenceNode) expr).name().text();
//                return baseEntries.stream()
//                        .filter(symbol -> symbol.name().equals(name))
//                        .findFirst()
//                        .map(symbol -> this.getEntriesForSymbol(name, symbol, ctx))
//                        .orElseGet(ArrayList::new);
//            case FUNCTION_CALL:
//                /*
//                Captures the following
//                (1) functionName().<cursor>
//                (2) functionName().t<cursor>
//                 */
//                String fName = ((SimpleNameReferenceNode) ((FunctionCallExpressionNode) expr)
//                        .functionName()).name().text();
//                return baseEntries.stream()
//                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(fName))
//                        .findFirst()
//                        .map(entry -> this.getEntriesForSymbol(fName, ((BInvokableSymbol) entry.symbol).retType, ctx))
//                        .orElseGet(ArrayList::new);
//            case METHOD_CALL: {
//                /*
//                Address the following
//                (1) test.testMethod().<cursor>
//                (2) test.testMethod().t<cursor>
//                 */
//                List<Scope.ScopeEntry> filtered = this.getEntries(ctx, baseEntries,
//                        ((MethodCallExpressionNode) expr).expression());
//                String mName = ((SimpleNameReferenceNode) ((MethodCallExpressionNode) expr)
//                        .methodName()).name().text();
//                return filtered.stream()
//                        .filter(scopeEntry -> {
//                            String[] nameComps = scopeEntry.symbol.getName().getValue().split("\\.");
//                            return nameComps[nameComps.length - 1].equals(mName);
//                        })
//                        .findFirst()
//                        .map(entry -> this.getEntriesForSymbol(mName, ((BInvokableSymbol) entry.symbol).retType, ctx))
//                        .orElseGet(ArrayList::new);
//            }
//            case FIELD_ACCESS: {
//                /*
//                Address the following
//                (1) test1.test2.<cursor>
//                (2) test1.test2.t<cursor>
//                 */
//                List<Scope.ScopeEntry> filtered = this.getEntries(ctx, baseEntries,
//                        ((FieldAccessExpressionNode) expr).expression());
//                String field = ((SimpleNameReferenceNode) ((FieldAccessExpressionNode) expr).fieldName()).name().text();
//                return filtered.stream()
//                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(field))
//                        .findFirst()
//                        .map(symbol -> this.getEntriesForSymbol(field, symbol, ctx))
//                        .orElseGet(ArrayList::new);
//            }
//            default:
//                return new ArrayList<>();
//        }

        return new ArrayList<>();
    }

    public List<LSCompletionItem> getEntriesForSymbol(String symbolName, Symbol symbol, LSContext context) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
        List<LSCompletionItem> completionItems = new ArrayList<>();

        /*
        LangLib checks also contains a check for the object type tag. But we skip and instead extract the entries
        from the object symbol itself
         */
//        if (symbol.kind() == SymbolKind.VARIABLE && ((VariableSymbol) symbol).typeDescriptor().isPresent()
//                && ((VariableSymbol) symbol).typeDescriptor().get().kind() == TypeDescKind.OBJECT) {
//            ObjectTypeDescriptor objectType = (ObjectTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor().get();
//            completionItems.addAll(this.getObjectMethods(context, objectType, symbolName));
//            completionItems.addAll(this.getObjectFields(context, objectType, symbolName));
//            // Fixme: Enable the langlib suggestions
////            entries.addAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
////            return entries.stream()
////                    .filter(entry -> (!(entry.symbol instanceof BInvokableSymbol))
////                            || ((entry.symbol.flags & Flags.REMOTE) != Flags.REMOTE))
////                    .collect(Collectors.toList());
//            
//            return completionItems;
//        }
//        if (symbol.kind() == SymbolKind.VARIABLE && ((VariableSymbol) symbol).typeDescriptor().isPresent()
//                && ((VariableSymbol) symbol).typeDescriptor().get().kind() == TypeDescKind.UNION) {
//            UnionTypeDescriptor unionType = (UnionTypeDescriptor) ((VariableSymbol) symbol).typeDescriptor().get();
//            entries.addAll(FilterUtils.getInvocationsAndFieldsForUnionType((unionType, context));
//        }
//        else if (symbolType.tsymbol != null && symbolType.tsymbol.scope != null) {
//            entries.addAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
//            List<Scope.ScopeEntry> filteredEntries = new ArrayList<>(symbolType.tsymbol.scope.entries.values());
//            // For the optional field access expression, the following will be skipped.
//            if (symbolType.tsymbol instanceof BRecordTypeSymbol && this.removeOptionalFields()) {
//                filteredEntries.removeIf(entry -> (entry.symbol.flags & Flags.OPTIONAL) == Flags.OPTIONAL);
//            }
//            entries.addAll(filteredEntries);
//        } else {
//            entries.addAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
//        }

        return completionItems;
        /*
        Here we add the BTypeSymbol check to skip the anyData and similar types suggested from lang lib scope entries
         */
//        return entries.stream()
//                .filter(entry -> (!(entry.symbol instanceof BTypeSymbol))
//                        && ((!(entry.symbol instanceof BInvokableSymbol))
//                        || ((entry.symbol.flags & Flags.REMOTE) != Flags.REMOTE)))
//                .collect(Collectors.toList());
    }

    public List<LSCompletionItem> getObjectMethods(LSContext context,
                                                   ObjectTypeDescriptor objectTypeDesc,
                                                   String symbolName) {
        String currentModule = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String objectOwnerModule = objectTypeDesc.moduleID().moduleName();
        boolean symbolInCurrentModule = currentModule.equals(objectOwnerModule);

        // Extract the method entries
        List<Symbol> methods = objectTypeDesc.methods().stream()
                .filter(symbol -> {
                    boolean isPrivate = symbol.qualifiers().contains(Qualifier.PRIVATE);
                    boolean isPublic = symbol.qualifiers().contains(Qualifier.PUBLIC);

                    return !((symbol.name().contains(".init") && !"self".equals(symbolName))
                            || (isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                }).collect(Collectors.toList());

        return this.getCompletionItemList(methods, context);
    }

    public List<LSCompletionItem> getObjectFields(LSContext context,
                                                  ObjectTypeDescriptor objectTypeDesc,
                                                  String symbolName) {
        String currentModule = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String objectOwnerModule = objectTypeDesc.moduleID().moduleName();
        boolean symbolInCurrentModule = currentModule.equals(objectOwnerModule);

        // Extract Field Entries
        List<FieldDescriptor> fields = objectTypeDesc.fieldDescriptors().stream()
                .filter(fieldDescriptor -> {
                    boolean isPrivate = fieldDescriptor.qualifier().isPresent()
                            && fieldDescriptor.qualifier().get() == Qualifier.PRIVATE;
                    boolean isPublic = fieldDescriptor.qualifier().isPresent()
                            && fieldDescriptor.qualifier().get() == Qualifier.PUBLIC;
                    return !((isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                })
                .collect(Collectors.toList());

        List<LSCompletionItem> completionItems = new ArrayList<>();

        for (FieldDescriptor field : fields) {
            CompletionItem fieldItem = new CompletionItem();
            fieldItem.setInsertText(field.name());
            fieldItem.setInsertTextFormat(InsertTextFormat.Snippet);
            fieldItem.setLabel(field.name());
            fieldItem.setDetail(ItemResolverConstants.FIELD_TYPE);
            fieldItem.setKind(CompletionItemKind.Field);
            fieldItem.setSortText(Priority.PRIORITY120.toString());

            completionItems.add(new FieldCompletionItem(context, field, fieldItem));
        }

        return completionItems;
    }

    /**
     * Whether to remove the optional fields during the record fields filtering phase.
     *
     * @return {@link Boolean} optional field removal status
     */
    protected abstract boolean removeOptionalFields();

    /**
     * When given a union of record types, iterate over the member types to extract the fields with the same name.
     * If a certain field with the same name contains in all records we extract the field entries
     *
     * @param unionType union type to evaluate
     * @param context   Language server operation context
     * @return {@link Map} map of scope entries
     */
    public List<LSCompletionItem> getInvocationsAndFieldsForUnionType(UnionTypeDescriptor unionType,
                                                                      LSContext context) {
        ArrayList<BallerinaTypeDescriptor> memberTypes = new ArrayList<>(unionType.memberTypeDescriptors());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
//        Integer invocationTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        // check whether union consists of same type tag symbols
        BallerinaTypeDescriptor firstMemberType = memberTypes.get(0);
        boolean allMatch = memberTypes.stream()
                .allMatch(typeDescriptor -> typeDescriptor.kind() == firstMemberType.kind());
        // If all the members are not same types we stop proceeding
        if (!allMatch) {
            // Fixme: enable the langlib suggestions
//            resultEntries.addAll(getLangLibScopeEntries(unionType, symbolTable, types));
            return completionItems;
        }

        // Fixme: enable the langlib suggestions
////        resultEntries.addAll(getLangLibScopeEntries(firstMemberType, symbolTable, types));
//        if (firstMemberType.kind() == TypeDescKind.RECORD) {
//            // Keep track of the occurrences of each of the field names
//            LinkedHashMap<String, Integer> memberOccurrenceCounts = new LinkedHashMap<>();
//            List<String> firstMemberFieldKeys = new ArrayList<>();
//        /*
//        We keep only the name fields of the first member type since a field has to appear in all the member types
//         */
//            for (int memberCounter = 0; memberCounter < memberTypes.size(); memberCounter++) {
//                int finalMemberCounter = memberCounter;
//                ((RecordTypeDescriptor) memberTypes.get(memberCounter)).fieldDescriptors()
//                        .forEach(fieldDescriptor -> {
//                            if (memberOccurrenceCounts.containsKey(fieldDescriptor.name())) {
//                                memberOccurrenceCounts.put(fieldDescriptor.name(),
//                                        memberOccurrenceCounts.get(fieldDescriptor.name()) + 1);
//                            } else if (finalMemberCounter == 0) {
//                                // Fields are only captured for the first member type, otherwise the count is increased
//                                firstMemberFieldKeys.add(fieldDescriptor.name());
//                                memberOccurrenceCounts.put(fieldDescriptor.name(), 1);
//                            }
//                        });
//            }
//            if (memberOccurrenceCounts.size() == 0) {
//                return completionItems;
//            }
//            List<Integer> counts = new ArrayList<>(memberOccurrenceCounts.values());
//            List<FieldDescriptor> firstMemberEntries = ((RecordTypeDescriptor) firstMemberType).fieldDescriptors();
//            for (int i = 0; i < counts.size(); i++) {
//                if (counts.get(i) != memberTypes.size()) {
//                    continue;
//                }
//                String name = firstMemberFieldKeys.get(i);
//                BSymbol symbol = firstMemberEntries.get(name);
//                if (firstMemberType.kind() == TypeDescKind.RECORD /*&& (invocationTokenType == BallerinaParser.DOT
//                        || invocationTokenType == BallerinaParser.NOT)*/
//                        && (org.ballerinalang.jvm.util.Flags.isFlagOn(symbol.flags, Flags.OPTIONAL))) {
//                    continue;
//                }
//                resultEntries.add(firstMemberEntries.get(name));
//            }
//        }

        return new ArrayList<>();
    }
}
