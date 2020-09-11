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
import io.ballerinalang.compiler.syntax.tree.MethodCallExpressionNode;
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
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
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
    protected List<Scope.ScopeEntry> getEntries(LSContext ctx,
                                                List<Scope.ScopeEntry> baseEntries,
                                                ExpressionNode expr) {
        switch (expr.kind()) {
            case SIMPLE_NAME_REFERENCE:
                /*
                Captures the following
                (1) fieldName.<cursor>
                (2) fieldName.t<cursor>
                 */
                String name = ((SimpleNameReferenceNode) expr).name().text();
                return baseEntries.stream()
                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(name))
                        .findFirst()
                        .map(scopeEntry -> this.getEntriesForSymbol(name, scopeEntry.symbol.type, ctx))
                        .orElseGet(ArrayList::new);
            case FUNCTION_CALL:
                /*
                Captures the following
                (1) functionName().<cursor>
                (2) functionName().t<cursor>
                 */
                String fName = ((SimpleNameReferenceNode) ((FunctionCallExpressionNode) expr)
                        .functionName()).name().text();
                return baseEntries.stream()
                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(fName))
                        .findFirst()
                        .map(entry -> this.getEntriesForSymbol(fName, ((BInvokableSymbol) entry.symbol).retType, ctx))
                        .orElseGet(ArrayList::new);
            case METHOD_CALL: {
                /*
                Address the following
                (1) test.testMethod().<cursor>
                (2) test.testMethod().t<cursor>
                 */
                List<Scope.ScopeEntry> filtered = this.getEntries(ctx, baseEntries,
                        ((MethodCallExpressionNode) expr).expression());
                String mName = ((SimpleNameReferenceNode) ((MethodCallExpressionNode) expr)
                        .methodName()).name().text();
                return filtered.stream()
                        .filter(scopeEntry -> {
                            String[] nameComps = scopeEntry.symbol.getName().getValue().split("\\.");
                            return nameComps[nameComps.length - 1].equals(mName);
                        })
                        .findFirst()
                        .map(entry -> this.getEntriesForSymbol(mName, ((BInvokableSymbol) entry.symbol).retType, ctx))
                        .orElseGet(ArrayList::new);
            }
            case FIELD_ACCESS: {
                /*
                Address the following
                (1) test1.test2.<cursor>
                (2) test1.test2.t<cursor>
                 */
                List<Scope.ScopeEntry> filtered = this.getEntries(ctx, baseEntries,
                        ((FieldAccessExpressionNode) expr).expression());
                String field = ((SimpleNameReferenceNode) ((FieldAccessExpressionNode) expr).fieldName()).name().text();
                return filtered.stream()
                        .filter(scopeEntry -> scopeEntry.symbol.getName().getValue().equals(field))
                        .findFirst()
                        .map(scopeEntry -> this.getEntriesForSymbol(field, scopeEntry.symbol.type, ctx))
                        .orElseGet(ArrayList::new);
            }
            default:
                return new ArrayList<>();
        }
    }

    public List<Scope.ScopeEntry> getEntriesForSymbol(String symbolName, BType symbolType, LSContext context) {
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
        List<Scope.ScopeEntry> entries = new ArrayList<>();

        /*
        LangLib checks also contains a check for the object type tag. But we skip and instead extract the entries
        from the object symbol itself
         */
        if (symbolType.tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) symbolType.tsymbol;
            entries.addAll(FilterUtils.getObjectMethodsAndFields(context, objectTypeSymbol, symbolName));
            entries.addAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
            return entries.stream()
                    .filter(entry -> (!(entry.symbol instanceof BInvokableSymbol))
                            || ((entry.symbol.flags & Flags.REMOTE) != Flags.REMOTE))
                    .collect(Collectors.toList());
        } else if (symbolType instanceof BUnionType) {
            entries.addAll(FilterUtils.getInvocationsAndFieldsForUnionType((BUnionType) symbolType, context));
        } else if (symbolType.tsymbol != null && symbolType.tsymbol.scope != null) {
            entries.addAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
            List<Scope.ScopeEntry> filteredEntries = new ArrayList<>(symbolType.tsymbol.scope.entries.values());
            // For the optional field access expression, the following will be skipped.
            if (symbolType.tsymbol instanceof BRecordTypeSymbol && this.removeOptionalFields()) {
                filteredEntries.removeIf(entry -> (entry.symbol.flags & Flags.OPTIONAL) == Flags.OPTIONAL);
            }
            entries.addAll(filteredEntries);
        } else {
            entries.addAll(FilterUtils.getLangLibScopeEntries(symbolType, symbolTable, types));
        }
        /*
        Here we add the BTypeSymbol check to skip the anyData and similar types suggested from lang lib scope entries
         */
        return entries.stream()
                .filter(entry -> (!(entry.symbol instanceof BTypeSymbol))
                        && ((!(entry.symbol instanceof BInvokableSymbol))
                        || ((entry.symbol.flags & Flags.REMOTE) != Flags.REMOTE)))
                .collect(Collectors.toList());
    }

    /**
     * Whether to remove the optional fields during the record fields filtering phase.
     *
     * @return {@link Boolean} optional field removal status
     */
    protected abstract boolean removeOptionalFields();
}
