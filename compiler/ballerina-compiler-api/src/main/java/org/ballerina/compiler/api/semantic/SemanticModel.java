/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.compiler.api.semantic;

import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.TextRange;
import org.ballerina.compiler.api.semantic.visitors.SymbolsLookupVisitor;
import org.ballerina.compiler.api.symbol.BCompiledSymbol;
import org.ballerina.compiler.api.symbol.SymbolFactory;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Semantic model representation of a given syntax tree.
 *
 * @since 1.3.0
 */
public class SemanticModel extends AbstractSemanticModel {

    private BLangPackage bLangPackage;
    private CompilerContext compilerContext;
    private SymbolTable symbolTable;
    private Types types;

    public SemanticModel(BLangCompilationUnit compilationUnit, BLangPackage bLangPackage, CompilerContext context) {
        super(compilationUnit);
        this.compilerContext = context;
        this.bLangPackage = bLangPackage;
        this.symbolTable = SymbolTable.getInstance(compilerContext);
        this.types = Types.getInstance(compilerContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BCompiledSymbol> lookupSymbols(LinePosition linePosition) {
        List<BCompiledSymbol> compiledSymbols = new ArrayList<>();
        SymbolResolver symbolResolver = SymbolResolver.getInstance(this.compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(this.compilerContext);
        SymbolEnv symbolEnv = symbolTable.pkgEnvMap.get(this.bLangPackage.symbol);
        SymbolsLookupVisitor lookupVisitor = new SymbolsLookupVisitor(linePosition, symbolEnv);
        Map<Name, List<Scope.ScopeEntry>> scopeSymbols =
                symbolResolver.getAllVisibleInScopeSymbols(lookupVisitor.lookUp(this.compilationUnit));

        scopeSymbols.forEach((name, scopeEntries) -> scopeEntries.forEach(scopeEntry -> {
            if (!(scopeEntry.symbol instanceof BOperatorSymbol)) {
                compiledSymbols.add(SymbolFactory.getBCompiledSymbol(scopeEntry.symbol, name.getValue()));
            }
        }));

        return compiledSymbols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BCompiledSymbol lookupSymbol(int position) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BCompiledSymbol> getSymbolByName(LinePosition linePosition, String name) {
        List<BCompiledSymbol> compiledSymbols = this.lookupSymbols(linePosition);
        return compiledSymbols.parallelStream()
                .filter(symbol -> name.equals(symbol.name()))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> getDiagnostics(TextRange range, DiagnosticKind kind) {
        return null;
    }

    /**
     * Retrieve lang-lib scope entries for this typeTag.
     *
     * @param bType {@link BType}
     * @return map of scope entries
     */
    public List<BCompiledSymbol> getLangLibSymbols(BallerinaTypeDesc bType) {
        List<BCompiledSymbol> compiledSymbols = new ArrayList<>();
        Map<Name, Scope.ScopeEntry> entries = new HashMap<>(this.symbolTable.langValueModuleSymbol.scope.entries);
        switch (bType.kind()) {
            case ARRAY:
            case TUPLE:
                entries.putAll(this.symbolTable.langArrayModuleSymbol.scope.entries);
                break;
            case XML:
                entries.putAll(this.symbolTable.langXmlModuleSymbol.scope.entries);
                break;
            case BOOLEAN:
                entries.putAll(this.symbolTable.langBooleanModuleSymbol.scope.entries);
                break;
            case DECIMAL:
                entries.putAll(this.symbolTable.langDecimalModuleSymbol.scope.entries);
                break;
            case STRING:
                entries.putAll(this.symbolTable.langStringModuleSymbol.scope.entries);
                break;
            case FLOAT:
                entries.putAll(this.symbolTable.langFloatModuleSymbol.scope.entries);
                break;
            case INT:
                entries.putAll(this.symbolTable.langIntModuleSymbol.scope.entries);
                break;
            case ERROR:
                entries.putAll(this.symbolTable.langErrorModuleSymbol.scope.entries);
                break;
            case FUTURE:
                entries.putAll(this.symbolTable.langFutureModuleSymbol.scope.entries);
                break;
            case MAP:
            case RECORD:
                entries.putAll(this.symbolTable.langMapModuleSymbol.scope.entries);
                break;
            case OBJECT:
                entries.putAll(this.symbolTable.langObjectModuleSymbol.scope.entries);
                break;
            case STREAM:
                entries.putAll(this.symbolTable.langStreamModuleSymbol.scope.entries);
                break;
            case TYPEDESC:
                entries.putAll(this.symbolTable.langTypedescModuleSymbol.scope.entries);
                break;
            default:
                break;
        }

        Map<Name, Scope.ScopeEntry> filteredList = entries.entrySet().stream().filter(entry -> {
            BSymbol symbol = entry.getValue().symbol;
            if (symbol instanceof BInvokableSymbol) {
                List<BVarSymbol> params = ((BInvokableSymbol) symbol).params;
                return params.isEmpty() || params.get(0).type.tag == bType.getBType().tag ||
                        (types.isAssignable(bType.getBType(), params.get(0).type));
            }
            return symbol.kind != null && symbol.kind != SymbolKind.OBJECT;
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        filteredList.forEach((name, scopeEntry) -> {
            if (!(scopeEntry.symbol instanceof BOperatorSymbol)) {
                compiledSymbols.add(SymbolFactory.getBCompiledSymbol(scopeEntry.symbol, name.getValue()));
            }
        });

        return compiledSymbols;
    }
}
