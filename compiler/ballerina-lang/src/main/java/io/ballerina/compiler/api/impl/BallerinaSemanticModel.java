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
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeReferenceTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
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

import static io.ballerina.compiler.api.symbols.SymbolKind.CLASS_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.OBJECT_FIELD;
import static io.ballerina.compiler.api.symbols.SymbolKind.RECORD_FIELD;
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
     * */
    @Override
    public Types types() {
        return BallerinaTypes.getInstance(this.compilerContext);
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
        SymbolEnv symbolEnv = envResolver.lookUp(compilationUnit, position);
        Map<Name, List<Scope.ScopeEntry>> scopeSymbols = symbolResolver.getAllVisibleInScopeSymbols(symbolEnv);

        Location cursorPos = new BLangDiagnosticLocation(compilationUnit.name,
                                                         position.line(), position.line(),
                                                         position.offset(), position.offset());

        Set<DiagnosticState> statesSet = new HashSet<>(Arrays.asList(states));
        Set<Symbol> compiledSymbols = new HashSet<>();
        for (Map.Entry<Name, List<Scope.ScopeEntry>> entry : scopeSymbols.entrySet()) {
            Name name = entry.getKey();
            List<Scope.ScopeEntry> scopeEntries = entry.getValue();
            for (Scope.ScopeEntry scopeEntry : scopeEntries) {
                addToCompiledSymbols(compiledSymbols, scopeEntry, cursorPos, name, symbolEnv, statesSet,
                        compilationUnit.getName());
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
        BSymbol symbolAtCursor = findSymbolAtCursorPosition(sourceDocument, position);
        if (symbolAtCursor == null) {
            return Collections.emptyList();
        }
        Location symbolLocation = symbolAtCursor.getPosition();
        BLangNode node = new NodeFinder(false).lookupEnclosingContainer(this.bLangPackage, symbolLocation.lineRange());
        return getReferences(symbolAtCursor, node, true);
    }

    @Override
    public List<Location> references(Symbol symbol, boolean withDefinition) {
        BSymbol symbolAtCursor = getInternalSymbol(symbol);
        Optional<Location> symbolLocation = symbol.getLocation();
        if (symbolLocation.isEmpty()) {
            return Collections.emptyList();
        }
        BLangNode node = new NodeFinder(false)
                .lookupEnclosingContainer(this.bLangPackage, symbolLocation.get().lineRange());

        return getReferences(symbolAtCursor, node, withDefinition);
    }

    @Override
    public List<Location> references(Document sourceDocument, LinePosition position, boolean withDefinition) {
        BSymbol symbolAtCursor = findSymbolAtCursorPosition(sourceDocument, position);
        if (symbolAtCursor == null) {
            return Collections.emptyList();
        }
        Location symbolLocation = symbolAtCursor.getPosition();
        BLangNode node = new NodeFinder(false)
                .lookupEnclosingContainer(this.bLangPackage, symbolLocation.lineRange());

        return getReferences(symbolAtCursor, node, withDefinition);
    }

    @Override
    public List<Location> references(Symbol symbol, Document targetDocument, boolean withDefinition) {
        BSymbol symbolAtCursor = getInternalSymbol(symbol);
        Optional<Location> symbolLocation = symbol.getLocation();
        if (symbolLocation.isEmpty()) {
            return Collections.emptyList();
        }
        BLangNode node = new NodeFinder(false)
                .lookupEnclosingContainer(getCompilationUnit(targetDocument), symbolLocation.get().lineRange());

        return getReferences(symbolAtCursor, node, withDefinition);
    }

    @Override
    public List<Location> references(Document sourceDocument,
                                     Document targetDocument,
                                     LinePosition position,
                                     boolean withDefinition) {

        BSymbol symbolAtCursor = findSymbolAtCursorPosition(sourceDocument, position);
        if (symbolAtCursor == null) {
            return Collections.emptyList();
        }
        Location symbolLocation = symbolAtCursor.getPosition();
        BLangNode node = new NodeFinder(false)
                .lookupEnclosingContainer(getCompilationUnit(targetDocument), symbolLocation.lineRange());

        return getReferences(symbolAtCursor, node, withDefinition);
    }

    private BSymbol findSymbolAtCursorPosition(Document sourceDocument, LinePosition linePosition) {
        BLangCompilationUnit sourceCompilationUnit = getCompilationUnit(sourceDocument);
        SymbolFinder symbolFinder = new SymbolFinder();
        return symbolFinder.lookup(sourceCompilationUnit, linePosition);
    }

    private List<Location> getReferences(BSymbol symbol, BLangNode node, boolean withDefinition) {
        ReferenceFinder refFinder = new ReferenceFinder(withDefinition);
        return refFinder.findReferences(node, symbol);
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

        if (!isNonNamedArgExprNode(node) && !isObjectConstructorExpr(node) && !isAnonFunctionExpr(node)) {
            return Optional.empty();
        }

        // The determined type of invocation with a start action is set to be future. Therefore, the constraint type
        // is obtained if the given range is pointed to the invocation function.
        BType determinedType = getDeterminedType(node, range);

        return Optional.ofNullable(typesFactory.getTypeDescriptor(determinedType));
    }

    private BType getDeterminedType(BLangNode node, LineRange range) {
        if (node.getKind() == NodeKind.INVOCATION && node.getDeterminedType().getKind() == TypeKind.FUTURE) {
            BLangInvocation invocationNode = (BLangInvocation) node;
            if (invocationNode.isAsync()
                    && PositionUtil.withinBlock(range.startLine(), invocationNode.getName().getPosition())) {

                return ((BFutureType) node.getDeterminedType()).getConstraint();
            }
        }

        return node.getDeterminedType();
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

            if (lineRange.filePath().equals(range.filePath()) && PositionUtil.withinRange(lineRange, range)) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TypeSymbol> expectedType(Document sourceDocument, LinePosition linePosition) {
        Optional<TypeSymbol> typeSymbol = null;
        BLangCompilationUnit compilationUnit = getCompilationUnit(sourceDocument);
        SyntaxTree syntaxTree = sourceDocument.syntaxTree();
        Node node = findInnerMostNode(linePosition, syntaxTree);
        ExpectedTypeFinder expectedTypeFinder = new ExpectedTypeFinder(this, compilationUnit,
                this.compilerContext, linePosition, sourceDocument);
        while (node != null) {
            try {
                typeSymbol = node.apply(expectedTypeFinder);
            } catch (IllegalStateException e) {
                break;
            }
            // To handle the cases related to ExternalTreeNodeList.
            if (typeSymbol != null && typeSymbol.isPresent()) {
                break;
            }
            node = node.parent();
        }

        return typeSymbol == null ? Optional.empty() : typeSymbol;
    }

    // Private helper methods for the public APIs above.

    private Optional<Symbol> lookupSymbol(BLangCompilationUnit compilationUnit, LinePosition position) {
        SymbolFinder symbolFinder = new SymbolFinder();
        BSymbol symbolAtCursor = symbolFinder.lookup(compilationUnit, position);

        if (symbolAtCursor == null || symbolAtCursor == symbolTable.notFoundSymbol) {
            return Optional.empty();
        }

        if (symbolAtCursor.kind == SymbolKind.TYPE_DEF
                && isCursorNotAtDefinition(compilationUnit, symbolAtCursor, position)) {
            return Optional.ofNullable(
                    typesFactory.getTypeDescriptor(((BTypeDefinitionSymbol) symbolAtCursor).referenceType));
        }

        if (isTypeSymbol(symbolAtCursor) &&
                (isInlineSingletonType(symbolAtCursor) || isInlineErrorType(symbolAtCursor)
                        || isCursorNotAtDefinition(compilationUnit, symbolAtCursor, position))) {
            return Optional.ofNullable(
                    typesFactory.getTypeDescriptor(symbolAtCursor.type, symbolAtCursor));
        }

        return Optional.ofNullable(symbolFactory.getBCompiledSymbol(symbolAtCursor,
                                                                    symbolAtCursor.getOriginalName().getValue()));
    }

    private boolean hasCursorPosPassedSymbolPos(BSymbol symbol, Location cursorPos) {
        if (symbol.origin != SOURCE) {
            return false;
        }

        if (symbol.owner.getKind() == SymbolKind.OBJECT
                || symbol.owner.getKind() == SymbolKind.PACKAGE
                || Symbols.isFlagOn(symbol.flags, Flags.WORKER)) {
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

    private boolean isCursorNotAtDefinition(BLangCompilationUnit compilationUnit, BSymbol symbolAtCursor,
                                            LinePosition cursorPos) {
        return !(compilationUnit.getPackageID().equals(symbolAtCursor.pkgID)
                && compilationUnit.getName().equals(symbolAtCursor.pos.lineRange().filePath())
                && PositionUtil.withinBlock(cursorPos, symbolAtCursor.pos));
    }

    private boolean isInlineSingletonType(BSymbol symbol) {
        // !(symbol.kind == SymbolKind.TYPE_DEF) is checked to exclude type defs
        return !(symbol.kind == SymbolKind.TYPE_DEF) && symbol.type.tag == TypeTags.FINITE &&
                ((BFiniteType) symbol.type).getValueSpace().size() == 1;
    }

    private boolean isInlineErrorType(BSymbol symbol) {
        return symbol.type.tag == TypeTags.ERROR && Symbols.isFlagOn(symbol.type.flags, Flags.ANONYMOUS);
    }

    private boolean isTypeSymbol(BSymbol tSymbol) {
        if (tSymbol.kind == SymbolKind.TYPE_DEF) {
            return true;
        }
        return tSymbol instanceof BTypeSymbol && !Symbols.isTagOn(tSymbol, PACKAGE)
                && !Symbols.isTagOn(tSymbol, ANNOTATION);
    }

    private BSymbol getInternalSymbol(Symbol symbol) {
        if (symbol.kind() == TYPE) {
            AbstractTypeSymbol abstractTypeSymbol = (AbstractTypeSymbol) symbol;
            if (abstractTypeSymbol.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                return ((BallerinaSymbol) ((BallerinaTypeReferenceTypeSymbol) symbol).definition()).getInternalSymbol();
            }

            return (abstractTypeSymbol).getBType().tsymbol;
        }

        return ((BallerinaSymbol) symbol).getInternalSymbol();
    }

    private BPackageSymbol getModuleSymbol(BLangCompilationUnit compilationUnit) {
        return compilationUnit.getSourceKind() == REGULAR_SOURCE ? bLangPackage.symbol :
                bLangPackage.getTestablePkg().symbol;
    }

    private void addToCompiledSymbols(Set<Symbol> compiledSymbols, Scope.ScopeEntry scopeEntry, Location cursorPos,
                                      Name name, SymbolEnv symbolEnv, Set<DiagnosticState> states,
                                      String compUnitName) {
        if (scopeEntry == null || scopeEntry.symbol == null || isFilteredVarSymbol(scopeEntry.symbol, states)) {
            return;
        }

        BSymbol symbolEnvScopeOwner = symbolEnv.scope.owner;
        BSymbol symbol = scopeEntry.symbol;

        if (isIgnorableSelfSymbol(name, symbol, symbolEnvScopeOwner)) {
            return;
        }

        // Checks
        // 1. if the enclosed node is within a worker declaration body and the encountered symbol is the same
        // worker that is being declared.
        // 2. if the cursor within a class-field declaration and the particular symbol is a class-method.
        if (isWithinCurrentWorker(symbolEnvScopeOwner.flags, symbolEnv.enclEnv, symbol) ||
                isCursorWithinClassFieldDecl(symbolEnvScopeOwner.flags) && isClassMemberMethod(symbol))  {
            return;
        }

        if ((hasCursorPosPassedSymbolPos(symbol, cursorPos) || isImportedSymbol(symbol))
                && !isServiceDeclSymbol(symbol) && !isResourceFunction(symbol)
                && (symbol.getKind() != SymbolKind.PACKAGE || isPackageImportedOnTheCompUnit(symbol, compUnitName))) {
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

            if (isFieldSymbol(compiledSymbol)) {
                BSymbol scopeEntryOwner = scopeEntry.symbol.owner;
                // If the current scope entry symbol is a child symbol of the enclosing node, and if the compiled
                // symbol is a field symbol, it can be determined that the cursor is within the field context.
                if (symbolEnvScopeOwner.getName().equals(scopeEntryOwner.getName())
                        && symbolEnvScopeOwner.pkgID.equals(scopeEntryOwner.pkgID)
                        && symbolEnvScopeOwner.getPosition().equals(scopeEntryOwner.getPosition())) {
                    return;
                }
            }

            compiledSymbols.add(compiledSymbol);
        }
        addToCompiledSymbols(compiledSymbols, scopeEntry.next, cursorPos, name, symbolEnv, states, compUnitName);
    }

    private boolean isWithinCurrentWorker(long symbolEnvScopeOwnerFlags, SymbolEnv enclEnv, BSymbol symbol) {

        if (Symbols.isFlagOn(symbolEnvScopeOwnerFlags, Flags.WORKER)
                && Symbols.isFlagOn(symbol.flags, Flags.WORKER)
                && enclEnv != null
                && enclEnv.node.getKind() == NodeKind.FUNCTION) {

            BLangIdentifier defaultWorkerName = ((BLangFunction) enclEnv.node).defaultWorkerName;

            return defaultWorkerName.getValue().equals(symbol.getName().getValue())
                    && defaultWorkerName.getPosition().equals(symbol.getPosition());
        }

        return false;
    }

    private boolean isCursorWithinClassFieldDecl(long symbolEnvScopeOwnerFlags) {
        return Symbols.isFlagOn(symbolEnvScopeOwnerFlags, Flags.CLASS);
    }

    private boolean isClassMemberMethod(BSymbol symbol) {
        return symbol.getKind() == SymbolKind.FUNCTION && Symbols.isFlagOn(symbol.flags, Flags.ATTACHED)
                && Symbols.isFlagOn(symbol.owner.flags, Flags.CLASS);
    }

    private boolean isFieldSymbol(Symbol symbol) {
        return symbol.kind() == CLASS_FIELD || symbol.kind() == OBJECT_FIELD || symbol.kind() == RECORD_FIELD;
    }

    private boolean isServiceDeclSymbol(BSymbol symbol) {
        return symbol.kind == SymbolKind.SERVICE;
    }

    private boolean isResourceFunction(BSymbol symbol) {
        return Symbols.isFlagOn(symbol.flags, Flags.RESOURCE);
    }

    private boolean isIgnorableSelfSymbol(Name name, BSymbol symbol, BSymbol symbolEnvScopeOwner) {
        return name.value.equals("self") && !symbol.owner.owner.equals(symbolEnvScopeOwner.owner);
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

    private boolean isNonNamedArgExprNode(BLangNode node) {
        return node instanceof BLangExpression && !(node instanceof BLangNamedArgsExpression);
    }

    private boolean isPackageImportedOnTheCompUnit(BSymbol symbol, String compUnit) {
        return symbol.getKind() == SymbolKind.PACKAGE && ((BPackageSymbol) symbol).compUnit.getValue().equals(compUnit);
    }

    private static NonTerminalNode findInnerMostNode(LinePosition linePosition, SyntaxTree syntaxTree) {
        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(linePosition);
        int end = textDocument.textPositionFrom(linePosition);
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, end - start), true);
    }

}
