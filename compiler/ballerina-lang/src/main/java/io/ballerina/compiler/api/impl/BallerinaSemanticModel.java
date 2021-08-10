/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.ballerina.compiler.api.symbols.SymbolKind.TYPE;
import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.tree.SourceKind.REGULAR_SOURCE;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.ANNOTATION;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.PACKAGE;

/**
 * Semantic model representation of a given syntax tree.
 *
 * @since 2.0.0
 */
public class BallerinaSemanticModel implements SemanticModel {

    private final BLangPackage bLangPackage;
    private final CompilerContext compilerContext;
    private final SymbolFactory symbolFactory;
    private final TypesFactory typesFactory;
    private final SymbolTable symbolTable;

    public BallerinaSemanticModel(BLangPackage bLangPackage, CompilerContext context) {
        this.compilerContext = context;
        this.bLangPackage = bLangPackage;
        this.symbolFactory = SymbolFactory.getInstance(context);
        this.typesFactory = TypesFactory.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Symbol> visibleSymbols(Document srcFile, LinePosition linePosition) {
        return visibleSymbols(srcFile, linePosition, DiagnosticState.VALID, DiagnosticState.UNKNOWN_TYPE);
    }

    @Override
    public List<Symbol> visibleSymbols(Document sourceFile, LinePosition position, DiagnosticState... states) {
        BLangCompilationUnit compilationUnit = getCompilationUnit(sourceFile);
        BPackageSymbol moduleSymbol = getModuleSymbol(compilationUnit);
        SymbolTable symbolTable = SymbolTable.getInstance(this.compilerContext);
        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(moduleSymbol);
        EnvironmentResolver envResolver = new EnvironmentResolver(pkgEnv);

        SymbolResolver symbolResolver = SymbolResolver.getInstance(this.compilerContext);
        Map<Name, List<Scope.ScopeEntry>> scopeSymbols =
                symbolResolver.getAllVisibleInScopeSymbols(envResolver.lookUp(compilationUnit, position));

        Location cursorPos = new BLangDiagnosticLocation(compilationUnit.name,
                                                         position.line(), position.line(),
                                                         position.offset(), position.offset());

        Set<DiagnosticState> statesSet = new HashSet<>(Arrays.asList(states));
        Set<Symbol> compiledSymbols = new HashSet<>();
        for (Map.Entry<Name, List<Scope.ScopeEntry>> entry : scopeSymbols.entrySet()) {
            Name name = entry.getKey();
            List<Scope.ScopeEntry> scopeEntries = entry.getValue();

            for (Scope.ScopeEntry scopeEntry : scopeEntries) {
                addToCompiledSymbols(compiledSymbols, scopeEntry, cursorPos, name, statesSet);
            }
        }

        return new ArrayList<>(compiledSymbols);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Symbol> symbol(Document sourceDocument, LinePosition position) {
        BLangCompilationUnit compilationUnit = getCompilationUnit(sourceDocument);
        return lookupSymbol(compilationUnit, position);
    }

    @Override
    public Optional<Symbol> symbol(Node node) {
        Optional<Location> nodeIdentifierLocation = node.apply(new SyntaxNodeToLocationMapper());

        if (nodeIdentifierLocation.isEmpty()) {
            return Optional.empty();
        }

        BLangCompilationUnit compilationUnit = getCompilationUnit(nodeIdentifierLocation.get().lineRange().filePath());
        return lookupSymbol(compilationUnit, nodeIdentifierLocation.get().lineRange().startLine());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Symbol> moduleSymbols() {
        List<Symbol> compiledSymbols = new ArrayList<>();

        for (Map.Entry<Name, Scope.ScopeEntry> e : bLangPackage.symbol.scope.entries.entrySet()) {
            Scope.ScopeEntry value = e.getValue();

            BSymbol symbol = value.symbol;
            if (symbol.origin == SOURCE) {
                compiledSymbols.add(symbolFactory.getBCompiledSymbol(symbol, symbol.getOriginalName().getValue()));
            }
        }

        return compiledSymbols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Location> references(Symbol symbol) {
        return references(symbol, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Location> references(Document sourceDocument, LinePosition position) {
        return references(sourceDocument, position, true);
    }

    @Override
    public List<Location> references(Symbol symbol, boolean withDefinition) {
        Optional<Location> symbolLocation = symbol.getLocation();

        // Assumption is that the location will be null for regular type symbols
        if (symbolLocation.isEmpty()) {
            return Collections.emptyList();
        }

        BLangNode node = new NodeFinder(false)
                .lookupEnclosingContainer(this.bLangPackage, symbolLocation.get().lineRange());

        ReferenceFinder refFinder = new ReferenceFinder(withDefinition);
        return refFinder.findReferences(node, getInternalSymbol(symbol));
    }

    @Override
    public List<Location> references(Document sourceDocument, LinePosition position, boolean withDefinition) {
        BLangCompilationUnit compilationUnit = getCompilationUnit(sourceDocument);
        SymbolFinder symbolFinder = new SymbolFinder();
        BSymbol symbolAtCursor = symbolFinder.lookup(compilationUnit, position);

        if (symbolAtCursor == null) {
            return Collections.emptyList();
        }

        BLangNode node = new NodeFinder(false)
                .lookupEnclosingContainer(this.bLangPackage, symbolAtCursor.pos.lineRange());

        ReferenceFinder refFinder = new ReferenceFinder(withDefinition);
        return refFinder.findReferences(node, symbolAtCursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TypeSymbol> type(LineRange range) {
        BLangCompilationUnit compilationUnit = getCompilationUnit(range.filePath());
        NodeFinder nodeFinder = new NodeFinder(true);
        BLangNode node = nodeFinder.lookup(compilationUnit, range);

        if (node == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(typesFactory.getTypeDescriptor(node.getBType()));
    }

    @Override
    public Optional<TypeSymbol> typeOf(LineRange range) {
        BLangCompilationUnit compilationUnit = getCompilationUnit(range.filePath());
        NodeFinder nodeFinder = new NodeFinder(false);
        BLangNode node = nodeFinder.lookup(compilationUnit, range);

        if (!(node instanceof BLangExpression) && !isObjectConstructorExpr(node) && !isAnonFunctionExpr(node)) {
            return Optional.empty();
        }

        return Optional.ofNullable(typesFactory.getTypeDescriptor(node.getDeterminedType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TypeSymbol> type(Node node) {
        Optional<Location> nodeIdentifierLocation = node.apply(new SyntaxNodeToLocationMapper());

        if (nodeIdentifierLocation.isEmpty()) {
            return Optional.empty();
        }

        return type(node.location().lineRange());
    }

    @Override
    public Optional<TypeSymbol> typeOf(Node node) {
        Optional<Location> nodeIdentifierLocation = node.apply(new SyntaxNodeToLocationMapper());

        if (nodeIdentifierLocation.isEmpty()) {
            return Optional.empty();
        }

        return typeOf(node.location().lineRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> diagnostics(LineRange range) {
        List<Diagnostic> allDiagnostics = this.bLangPackage.getDiagnostics();
        List<Diagnostic> filteredDiagnostics = new ArrayList<>();

        for (Diagnostic diagnostic : allDiagnostics) {
            LineRange lineRange = diagnostic.location().lineRange();

            if (lineRange.filePath().equals(range.filePath()) && withinRange(lineRange, range)) {
                filteredDiagnostics.add(diagnostic);
            }
        }

        return filteredDiagnostics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> diagnostics() {
        return this.bLangPackage.getDiagnostics();
    }

    // Private helper methods for the public APIs above.

    private Optional<Symbol> lookupSymbol(BLangCompilationUnit compilationUnit, LinePosition position) {
        SymbolFinder symbolFinder = new SymbolFinder();
        BSymbol symbolAtCursor = symbolFinder.lookup(compilationUnit, position);

        if (symbolAtCursor == null || symbolAtCursor == symbolTable.notFoundSymbol) {
            return Optional.empty();
        }

        if (isTypeSymbol(symbolAtCursor) &&
                !(compilationUnit.getPackageID().equals(symbolAtCursor.pkgID)
                        && compilationUnit.getName().equals(symbolAtCursor.pos.lineRange().filePath())
                        && PositionUtil.withinBlock(position, symbolAtCursor.pos))) {
            return Optional.ofNullable(
                    typesFactory.getTypeDescriptor(symbolAtCursor.type, (BTypeSymbol) symbolAtCursor));
        }

        return Optional.ofNullable(symbolFactory.getBCompiledSymbol(symbolAtCursor,
                symbolAtCursor.getOriginalName().getValue()));
    }

    private boolean hasCursorPosPassedSymbolPos(BSymbol symbol, Location cursorPos) {
        if (symbol.origin != SOURCE) {
            return false;
        }

        if (symbol.owner.getKind() == SymbolKind.PACKAGE || Symbols.isFlagOn(symbol.flags, Flags.WORKER)) {
            return true;
        }

        if (!bLangPackage.packageID.equals(symbol.pkgID)) {
            return false;
        }

        // These checks whether the cursor position has passed the symbol position or not
        LinePosition cursorPosStartLine = cursorPos.lineRange().startLine();
        LinePosition symbolStartLine = symbol.pos.lineRange().startLine();

        if (cursorPosStartLine.line() < symbolStartLine.line()) {
            return false;
        }

        if (cursorPosStartLine.line() > symbolStartLine.line()) {
            return true;
        }

        return cursorPosStartLine.offset() > symbolStartLine.offset();
    }

    private boolean isImportedSymbol(BSymbol symbol) {
        return symbol.origin == COMPILED_SOURCE &&
                (Symbols.isFlagOn(symbol.flags, Flags.PUBLIC) || symbol.getKind() == SymbolKind.PACKAGE);
    }

    private BLangCompilationUnit getCompilationUnit(Document srcFile) {
        return getCompilationUnit(srcFile.name());
    }

    private BLangCompilationUnit getCompilationUnit(String srcFile) {
        List<BLangCompilationUnit> testSrcs = new ArrayList<>();
        for (BLangTestablePackage pkg : bLangPackage.testablePkgs) {
            testSrcs.addAll(pkg.compUnits);
        }

        Stream<BLangCompilationUnit> units = Stream.concat(bLangPackage.compUnits.stream(), testSrcs.stream());
        return units
                .filter(unit -> unit.name.equals(srcFile))
                .findFirst()
                .get();
    }

    private boolean isTypeSymbol(BSymbol symbol) {
        return symbol instanceof BTypeSymbol && !Symbols.isTagOn(symbol, PACKAGE)
                && !Symbols.isTagOn(symbol, ANNOTATION);
    }

    private BSymbol getInternalSymbol(Symbol symbol) {
        if (symbol.kind() == TYPE) {
            return ((AbstractTypeSymbol) symbol).getBType().tsymbol;
        }

        return ((BallerinaSymbol) symbol).getInternalSymbol();
    }

    private BPackageSymbol getModuleSymbol(BLangCompilationUnit compilationUnit) {
        return compilationUnit.getSourceKind() == REGULAR_SOURCE ? bLangPackage.symbol :
                bLangPackage.getTestablePkg().symbol;
    }

    private boolean withinRange(LineRange range, LineRange specifiedRange) {
        int startLine = range.startLine().line();
        int startOffset = range.startLine().offset();

        int specifiedStartLine = specifiedRange.startLine().line();
        int specifiedEndLine = specifiedRange.endLine().line();
        int specifiedStartOffset = specifiedRange.startLine().offset();
        int specifiedEndOffset = specifiedRange.endLine().offset();

        return startLine >= specifiedStartLine && startLine <= specifiedEndLine &&
                startOffset >= specifiedStartOffset && startOffset <= specifiedEndOffset;
    }

    private void addToCompiledSymbols(Set<Symbol> compiledSymbols, Scope.ScopeEntry scopeEntry, Location cursorPos,
                                      Name name, Set<DiagnosticState> states) {
        if (scopeEntry == null || scopeEntry.symbol == null || isFilteredVarSymbol(scopeEntry.symbol, states)) {
            return;
        }

        BSymbol symbol = scopeEntry.symbol;
        if ((hasCursorPosPassedSymbolPos(symbol, cursorPos) || isImportedSymbol(symbol))
                && !isServiceDeclSymbol(symbol)) {
            Symbol compiledSymbol;
            // TODO: Fix #31808 and remove this if-check
            if (symbol.getKind() == SymbolKind.PACKAGE) {
                compiledSymbol = symbolFactory.getBCompiledSymbol(symbol, name.getValue());
            } else {
                compiledSymbol = symbolFactory.getBCompiledSymbol(symbol, symbol.getOriginalName().getValue());
            }
            if (compiledSymbol == null || compiledSymbols.contains(compiledSymbol)) {
                return;
            }
            compiledSymbols.add(compiledSymbol);
        }
        addToCompiledSymbols(compiledSymbols, scopeEntry.next, cursorPos, name, states);
    }

    private boolean isServiceDeclSymbol(BSymbol symbol) {
        return symbol.kind == SymbolKind.SERVICE;
    }

    private boolean isFilteredVarSymbol(BSymbol symbol, Set<DiagnosticState> states) {
        return symbol instanceof BVarSymbol && !states.contains(((BVarSymbol) symbol).state);
    }

    private boolean isObjectConstructorExpr(BLangNode node) {
        return node instanceof BLangClassDefinition && ((BLangClassDefinition) node).flagSet.contains(Flag.OBJECT_CTOR);
    }

    private boolean isAnonFunctionExpr(BLangNode node) {
        return (node instanceof BLangFunction && ((BLangFunction) node).flagSet.contains(Flag.LAMBDA))
                || node instanceof BLangArrowFunction;
    }
}
