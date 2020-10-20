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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.langserver.commons.LSContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Holds the set of utilities to get the qualified name reference associated Completion Items.
 *
 * @since 2.0.0
 */
public class QNameReferenceUtil {
    private QNameReferenceUtil() {
    }

    /**
     * Get the completions for the qualified name reference context.
     *
     * @param ctx      language server operation context
     * @param qNameRef qualified name reference
     * @return {@link List} of completion items
     */
    public static List<Symbol> getExpressionContextEntries(LSContext ctx, QualifiedNameReferenceNode qNameRef) {
        String moduleAlias = QNameReferenceUtil.getAlias(qNameRef);
        Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(ctx, moduleAlias);

        return moduleSymbol.map(value -> value.allSymbols().stream()
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION
                        || symbol.kind() == SymbolKind.TYPE
                        || symbol instanceof VariableSymbol)
                .collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    /**
     * Get the module alias from the {@link QualifiedNameReferenceNode} instance.
     *
     * @param qNameRef qualified name reference
     * @return {@link String} extracted alias
     */
    public static String getAlias(QualifiedNameReferenceNode qNameRef) {
        String alias = qNameRef.modulePrefix().text();
        return alias.startsWith("'") ? alias.substring(1) : alias;
    }

    /**
     * Get module content, given the given a qualified name reference and a predicate to filter.
     *
     * @param context   Language server operation context
     * @param qNameRef  qualified name reference
     * @param predicate predicate to filer
     * @return {@link List} of filtered module entries
     */
    public static List<Symbol> getModuleContent(LSContext context,
                                                QualifiedNameReferenceNode qNameRef,
                                                Predicate<Symbol> predicate) {
        Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, QNameReferenceUtil.getAlias(qNameRef));
        return module.map(moduleSymbol -> moduleSymbol.allSymbols().stream()
                .filter(predicate)
                .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    /**
     * Get the type items in a module, when given a qualified name reference.
     *
     * @param context  Language server operation context context
     * @param qNameRef Qualified name reference
     * @return {@link List} of type entries extracted
     */
    public static List<Symbol> getTypesInModule(LSContext context, QualifiedNameReferenceNode qNameRef) {
        Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, QNameReferenceUtil.getAlias(qNameRef));
        return module.map(symbol -> symbol.allSymbols().stream()
                .filter(moduleItem -> moduleItem instanceof TypeSymbol)
                .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }
}
