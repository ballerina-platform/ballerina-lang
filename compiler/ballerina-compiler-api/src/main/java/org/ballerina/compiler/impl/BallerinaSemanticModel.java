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
package org.ballerina.compiler.impl;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextRange;
import org.ballerina.compiler.api.SemanticModel;
import org.ballerina.compiler.api.symbols.Symbol;
import org.ballerina.compiler.impl.symbols.SymbolFactory;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifiableNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;

/**
 * Semantic model representation of a given syntax tree.
 *
 * @since 2.0.0
 */
public class BallerinaSemanticModel implements SemanticModel {

    private final BLangPackage bLangPackage;
    private final CompilerContext compilerContext;
    private final EnvironmentResolver envResolver;

    public BallerinaSemanticModel(BLangPackage bLangPackage, CompilerContext context) {
        this.compilerContext = context;
        this.bLangPackage = bLangPackage;

        SymbolTable symbolTable = SymbolTable.getInstance(context);
        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(bLangPackage.symbol);
        this.envResolver = new EnvironmentResolver(pkgEnv);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Symbol> visibleSymbols(String fileName, LinePosition linePosition) {
        List<Symbol> compiledSymbols = new ArrayList<>();
        SymbolResolver symbolResolver = SymbolResolver.getInstance(this.compilerContext);
        BLangCompilationUnit compilationUnit = getCompilationUnit(fileName);
        Map<Name, List<Scope.ScopeEntry>> scopeSymbols =
                symbolResolver.getAllVisibleInScopeSymbols(this.envResolver.lookUp(compilationUnit, linePosition));

        DiagnosticPos cursorPos = new DiagnosticPos(new BDiagnosticSource(bLangPackage.packageID, compilationUnit.name),
                                                    linePosition.line(), linePosition.line(),
                                                    linePosition.offset(), linePosition.offset());

        for (Map.Entry<Name, List<Scope.ScopeEntry>> entry : scopeSymbols.entrySet()) {
            Name name = entry.getKey();
            List<Scope.ScopeEntry> scopeEntries = entry.getValue();

            for (Scope.ScopeEntry scopeEntry : scopeEntries) {
                BSymbol symbol = scopeEntry.symbol;

                if (isSymbolInUserProject(symbol, cursorPos) || isImportedSymbol(symbol)) {
                    compiledSymbols.add(SymbolFactory.getBCompiledSymbol(symbol, name.getValue()));
                }
            }
        }

        return compiledSymbols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Symbol> symbol(String srcFile, LinePosition position) {
        BLangCompilationUnit compilationUnit = getCompilationUnit(srcFile);
        NodeResolver nodeResolver = new NodeResolver();
        BLangNode node = nodeResolver.lookup(compilationUnit, position);

        if (node instanceof IdentifiableNode) {
            BSymbol symbol = (BSymbol) ((IdentifiableNode) node).getSymbol();
            return Optional.ofNullable(SymbolFactory.getBCompiledSymbol(symbol, symbol.name.value));
        } else if (node instanceof BLangUserDefinedType) {
            return Optional.ofNullable(
                    SymbolFactory.createTypeDefinition(node.type.tsymbol, node.type.tsymbol.name.value));
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Symbol> moduleLevelSymbols() {
        List<Symbol> compiledSymbols = new ArrayList<>();

        for (Map.Entry<Name, Scope.ScopeEntry> e : bLangPackage.symbol.scope.entries.entrySet()) {
            Name key = e.getKey();
            Scope.ScopeEntry value = e.getValue();

            if (value.symbol.origin == SOURCE) {
                compiledSymbols.add(SymbolFactory.getBCompiledSymbol(value.symbol, key.value));
            }
        }

        return compiledSymbols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> diagnostics(TextRange range) {
        return new ArrayList<>();
    }

    private boolean isSymbolInUserProject(BSymbol symbol, DiagnosticPos cursorPos) {
        return symbol.origin == SOURCE &&
                (cursorPos.compareTo(symbol.pos) > 0
                        || symbol.owner.getKind() == SymbolKind.PACKAGE
                        || Symbols.isFlagOn(symbol.flags, Flags.WORKER));
    }

    private boolean isImportedSymbol(BSymbol symbol) {
        return symbol.origin == COMPILED_SOURCE &&
                (Symbols.isFlagOn(symbol.flags, Flags.PUBLIC) || symbol.getKind() == SymbolKind.PACKAGE);
    }

    private BLangCompilationUnit getCompilationUnit(String srcFile) {
        return bLangPackage.compUnits.stream()
                .filter(unit -> unit.name.equals(srcFile))
                .findFirst()
                .get();
    }
}
