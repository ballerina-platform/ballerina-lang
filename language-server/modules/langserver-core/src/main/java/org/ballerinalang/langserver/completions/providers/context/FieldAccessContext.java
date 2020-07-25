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

import io.ballerinalang.compiler.syntax.tree.ExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the completions within the variable declaration node context/
 *
 * @since 2.0.0
 */
public abstract class FieldAccessContext<T extends Node> extends AbstractCompletionProvider<T> {
    public FieldAccessContext(Kind kind) {
        super(kind);
    }

    protected List<Scope.ScopeEntry> getEntries(LSContext ctx, List<Scope.ScopeEntry> baseEntries, ExpressionNode expr) {
        switch (expr.kind()) {
            case SIMPLE_NAME_REFERENCE:
                String name = ((SimpleNameReferenceNode) expr).name().text();
                return baseEntries.stream()
                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(name))
                        .findFirst()
                        .map(scopeEntry -> this.getEntriesForSymbol(name, scopeEntry.symbol.type, ctx))
                        .orElseGet(ArrayList::new);
            case FUNCTION_CALL:
                String fName = ((SimpleNameReferenceNode) ((FunctionCallExpressionNode) expr)
                        .functionName()).name().text();
                return baseEntries.stream()
                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(fName))
                        .findFirst()
                        .map(entry -> this.getEntriesForSymbol(fName, ((BInvokableSymbol) entry.symbol).retType, ctx))
                        .orElseGet(ArrayList::new);
            case FIELD_ACCESS:
                List<Scope.ScopeEntry> filtered = this.getEntries(ctx, baseEntries,
                        ((FieldAccessExpressionNode) expr).expression());
                String field = ((SimpleNameReferenceNode) ((FieldAccessExpressionNode) expr).fieldName()).name().text();
                return filtered.stream()
                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(field))
                        .findFirst()
                        .map(scopeEntry -> this.getEntriesForSymbol(field, scopeEntry.symbol.type, ctx))
                        .orElseGet(ArrayList::new);
            default:
                return new ArrayList<>();
        }
    }

    public List<Scope.ScopeEntry> getEntriesForSymbol(String symbolName, BType symbolType, LSContext context) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
        Map<Name, Scope.ScopeEntry> entries = new HashMap<>();

        /*
        LangLib checks also contains a check for the object type tag. But we skip and instead extract the entries
        from the object symbol itself
         */
        if (symbolType.tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) symbolType.tsymbol;
            entries.putAll(FilterUtils.getObjectMethodsAndFields(context, objectTypeSymbol, symbolName));
            entries.putAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
            return entries.values().stream()
                    .filter(entry -> (!(entry.symbol instanceof BInvokableSymbol))
                            || ((entry.symbol.flags & Flags.REMOTE) != Flags.REMOTE))
                    .collect(Collectors.toList());
        } else if (symbolType instanceof BUnionType) {
            entries.putAll(FilterUtils.getInvocationsAndFieldsForUnionType((BUnionType) symbolType, context));
        } else if (symbolType.tsymbol != null && symbolType.tsymbol.scope != null) {
            entries.putAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
            Map<Name, Scope.ScopeEntry> filteredEntries = symbolType.tsymbol.scope.entries;
            // For the optional field access expression, the following will be skipped.
            if (symbolType.tsymbol instanceof BRecordTypeSymbol && this.removeOptionalFields()) {
                filteredEntries.entrySet()
                        .removeIf(entry -> (entry.getValue().symbol.flags & Flags.OPTIONAL) == Flags.OPTIONAL);
            }
            entries.putAll(filteredEntries);
        } else {
            entries.putAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
        }
        /*
        Here we add the BTypeSymbol check to skip the anyData and similar types suggested from lang lib scope entries
         */
        return entries.values().stream()
                .filter(entry -> (!(entry.symbol instanceof BTypeSymbol))
                        && ((!(entry.symbol instanceof BInvokableSymbol))
                        || ((entry.symbol.flags & Flags.REMOTE) != Flags.REMOTE)))
                .collect(Collectors.toList());
    }

    protected abstract boolean removeOptionalFields();
}
