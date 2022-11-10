/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OrderedNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClientDeclarationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BWorkerSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLAttributeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangClientDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangClientDeclarationStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.XMLConstants;

import static org.ballerinalang.model.elements.PackageID.ARRAY;
import static org.ballerinalang.model.elements.PackageID.BOOLEAN;
import static org.ballerinalang.model.elements.PackageID.DECIMAL;
import static org.ballerinalang.model.elements.PackageID.ERROR;
import static org.ballerinalang.model.elements.PackageID.FLOAT;
import static org.ballerinalang.model.elements.PackageID.FUNCTION;
import static org.ballerinalang.model.elements.PackageID.FUTURE;
import static org.ballerinalang.model.elements.PackageID.INT;
import static org.ballerinalang.model.elements.PackageID.MAP;
import static org.ballerinalang.model.elements.PackageID.OBJECT;
import static org.ballerinalang.model.elements.PackageID.QUERY;
import static org.ballerinalang.model.elements.PackageID.REGEXP;
import static org.ballerinalang.model.elements.PackageID.STREAM;
import static org.ballerinalang.model.elements.PackageID.STRING;
import static org.ballerinalang.model.elements.PackageID.TABLE;
import static org.ballerinalang.model.elements.PackageID.TRANSACTION;
import static org.ballerinalang.model.elements.PackageID.TYPEDESC;
import static org.ballerinalang.model.elements.PackageID.VALUE;
import static org.ballerinalang.model.elements.PackageID.XML;
import static org.ballerinalang.model.symbols.SymbolOrigin.BUILTIN;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.ballerinalang.model.tree.NodeKind.IMPORT;
import static org.ballerinalang.model.tree.NodeKind.RECORD_TYPE;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.DEFAULTABLE_PARAM_DEFINED_AFTER_INCLUDED_RECORD_PARAM;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.EXPECTED_RECORD_TYPE_AS_INCLUDED_PARAMETER;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.REDECLARED_SYMBOL;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.REQUIRED_PARAM_DEFINED_AFTER_INCLUDED_RECORD_PARAM;
import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;
import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * @since 0.94
 */
public class SymbolEnter extends BLangNodeVisitor {

    private static final CompilerContext.Key<SymbolEnter> SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private final SourceDirectory sourceDirectory;
    private List<BLangNode> unresolvedTypes;
    private Set<BLangNode> unresolvedRecordDueToFields;
    private boolean resolveRecordsUnresolvedDueToFields;
    private List<BLangClassDefinition> unresolvedClasses;
    private HashSet<LocationData> unknownTypeRefs;
    private List<PackageID> importedPackages;
    private int typePrecedence;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;
    private PackageCache packageCache;
    private List<BLangNode> intersectionTypes;
    private Map<BType, BLangTypeDefinition> typeToTypeDef;

    private SymbolEnv env;
    private final boolean projectAPIInitiatedCompilation;

    private static final String DEPRECATION_ANNOTATION = "deprecated";
    private static final String ANONYMOUS_RECORD_NAME = "anonymous-record";

    public static SymbolEnter getInstance(CompilerContext context) {
        SymbolEnter symbolEnter = context.get(SYMBOL_ENTER_KEY);
        if (symbolEnter == null) {
            symbolEnter = new SymbolEnter(context);
        }

        return symbolEnter;
    }

    public SymbolEnter(CompilerContext context) {
        context.put(SYMBOL_ENTER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.sourceDirectory = context.get(SourceDirectory.class);
        this.importedPackages = new ArrayList<>();
        this.unknownTypeRefs = new HashSet<>();
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
        this.intersectionTypes = new ArrayList<>();

        CompilerOptions options = CompilerOptions.getInstance(context);
        projectAPIInitiatedCompilation = Boolean.parseBoolean(
                options.get(CompilerOptionName.PROJECT_API_INITIATED_COMPILATION));
    }

    private void cleanup() {
        unknownTypeRefs.clear();
    }

    public BLangPackage definePackage(BLangPackage pkgNode) {
        dlog.setCurrentPackageId(pkgNode.packageID);
        populatePackageNode(pkgNode);
        defineNode(pkgNode, this.symTable.pkgEnvMap.get(symTable.langAnnotationModuleSymbol));
        return pkgNode;
    }

    public void defineClassDefinition(BLangClassDefinition classNode, SymbolEnv env) {
//        if (classNode.symbol != null) {
//            return;
//        }
//        defineNode(classNode, env);
        if (classNode.definitionCompleted) {
            return;
        }
        populateDistinctTypeIdsFromIncludedTypeReferences(classNode);
        defineFieldsOfClassDef(classNode, env);
        defineReferencedFieldsOfClassDef(classNode, env);
        defineFunctionsOfClassDef(env, classNode);
        setReadOnlynessOfClassDef(classNode, env);
        defineReadOnlyIncludedFieldsAndMethods(classNode, env);
        classNode.definitionCompleted = true;
    }

    public void defineNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = prevEnv;
    }

    public BLangPackage defineTestablePackage(BLangTestablePackage pkgNode, SymbolEnv env) {
        populatePackageNode(pkgNode);
        defineNode(pkgNode, env);
        return pkgNode;
    }

    // Visitor methods

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DEFINE)) {
            return;
        }

        // Create PackageSymbol
        BPackageSymbol pkgSymbol;
        if (Symbols.isFlagOn(Flags.asMask(pkgNode.flagSet), Flags.TESTABLE)) {
            pkgSymbol = Symbols.createPackageSymbol(pkgNode.packageID, this.symTable, Flags.asMask(pkgNode.flagSet),
                                                    SOURCE);
        } else {
            pkgSymbol = Symbols.createPackageSymbol(pkgNode.packageID, this.symTable, SOURCE);
        }
        if (PackageID.isLangLibPackageID(pkgSymbol.pkgID)) {
            populateLangLibInSymTable(pkgSymbol);
        }

        if (pkgNode.moduleContextDataHolder != null) {
            pkgSymbol.exported = pkgNode.moduleContextDataHolder.isExported();
            pkgSymbol.descriptor = pkgNode.moduleContextDataHolder.descriptor();
        }

        pkgNode.symbol = pkgSymbol;
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(pkgNode, pkgSymbol.scope, this.env);
        this.symTable.pkgEnvMap.put(pkgSymbol, pkgEnv);
        this.symTable.immutableTypeMaps.remove(Types.getPackageIdString(pkgSymbol.pkgID));

        // Add the current package node's ID to the imported package list. This is used to identify cyclic module
        // imports.
        importedPackages.add(pkgNode.packageID);

        defineConstructs(pkgNode, pkgEnv);
        pkgNode.getTestablePkgs().forEach(testablePackage -> defineTestablePackage(testablePackage, pkgEnv));
        pkgNode.completedPhases.add(CompilerPhase.DEFINE);

        // cleanup to avoid caching on compile context
        cleanup();

        // After we have visited a package node, we need to remove it from the imports list.
        importedPackages.remove(pkgNode.packageID);
    }

    private void defineConstructs(BLangPackage pkgNode, SymbolEnv pkgEnv) {
        // visit the package node recursively and define all package level symbols.
        // And maintain a list of created package symbols.
        Map<String, ImportResolveHolder> importPkgHolder = new HashMap<>();
        pkgNode.imports.forEach(importNode -> {
            String qualifiedName = importNode.getQualifiedPackageName();
            if (importPkgHolder.containsKey(qualifiedName)) {
                importPkgHolder.get(qualifiedName).unresolved.add(importNode);
                return;
            }
            defineNode(importNode, pkgEnv);
            if (importNode.symbol != null) {
                importPkgHolder.put(qualifiedName, new ImportResolveHolder(importNode));
            }
        });

        for (ImportResolveHolder importHolder : importPkgHolder.values()) {
            BPackageSymbol pkgSymbol = importHolder.resolved.symbol; // get a copy of the package symbol, add
            // compilation unit info to it,

            for (BLangImportPackage unresolvedPkg : importHolder.unresolved) {
                BPackageSymbol importSymbol = importHolder.resolved.symbol;
                Name resolvedPkgAlias = names.fromIdNode(importHolder.resolved.alias);
                Name unresolvedPkgAlias = names.fromIdNode(unresolvedPkg.alias);

                // check if its the same import or has the same alias.
                if (!Names.IGNORE.equals(unresolvedPkgAlias) && unresolvedPkgAlias.equals(resolvedPkgAlias)
                    && importSymbol.compUnit.equals(names.fromIdNode(unresolvedPkg.compUnit))) {
                    if (isSameImport(unresolvedPkg, importSymbol)) {
                        dlog.error(unresolvedPkg.pos, DiagnosticErrorCode.REDECLARED_IMPORT_MODULE,
                                unresolvedPkg.getQualifiedPackageName());
                    } else {
                        dlog.error(unresolvedPkg.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, unresolvedPkgAlias);
                    }
                    continue;
                }

                unresolvedPkg.symbol = pkgSymbol;
                // and define it in the current package scope
                BPackageSymbol symbol = dupPackageSymbolAndSetCompUnit(pkgSymbol,
                        names.fromIdNode(unresolvedPkg.compUnit));
                symbol.scope = pkgSymbol.scope;
                unresolvedPkg.symbol = symbol;
                pkgEnv.scope.define(unresolvedPkgAlias, symbol);
            }
        }
        if (!PackageID.ANNOTATIONS.equals(pkgNode.packageID)) {
            initPredeclaredModules(symTable.predeclaredModules, pkgNode.compUnits, pkgEnv);
        }

        for (BLangClientDeclaration clientDeclaration : pkgNode.clientDeclarations) {
            defineNode(clientDeclaration, pkgEnv);
        }

        // Define type definitions.
        this.typePrecedence = 0;

        // Treat constants and type definitions in the same manner, since constants can be used as
        // types. Also, there can be references between constant and type definitions in both ways.
        // Thus visit them according to the precedence.
        List<BLangNode> typeAndClassDefs = new ArrayList<>();
        pkgNode.constants.forEach(constant -> typeAndClassDefs.add(constant));
        pkgNode.typeDefinitions.forEach(typDef -> typeAndClassDefs.add(typDef));
        List<BLangClassDefinition> classDefinitions = getClassDefinitions(pkgNode.topLevelNodes);
        classDefinitions.forEach(classDefn -> typeAndClassDefs.add(classDefn));
        defineTypeNodes(typeAndClassDefs, pkgEnv);

        // Enabled logging errors after type def visit.
        // TODO: Do this in a cleaner way
        pkgEnv.logErrors = true;

        // Sort type definitions with precedence, before defining their members.
        pkgNode.typeDefinitions.sort(getTypePrecedenceComparator());
        typeAndClassDefs.sort(getTypePrecedenceComparator());

        // Add distinct type information
        defineDistinctClassAndObjectDefinitions(typeAndClassDefs);

        // Define type def fields (if any)
        defineFields(typeAndClassDefs, pkgEnv);
        populateTypeToTypeDefMap(typeAndClassDefs);
        defineDependentFields(typeAndClassDefs, pkgEnv);

        // Calculate error intersections types.
        defineIntersectionTypes(pkgEnv);

        // Define error details.
        defineErrorDetails(pkgNode.typeDefinitions, pkgEnv);

        // Define type def members (if any)
        defineFunctions(typeAndClassDefs, pkgEnv);

        // Intersection type nodes need to look at the member fields of a structure too.
        // Once all the fields and members of other types are set revisit intersection type definitions to validate
        // them and set the fields and members of the relevant immutable type.
        validateIntersectionTypeDefinitions(pkgNode.typeDefinitions, pkgNode.packageID);
        defineUndefinedReadOnlyTypes(pkgNode.typeDefinitions, typeAndClassDefs, pkgEnv);

        // Define service and resource nodes.
        pkgNode.services.forEach(service -> defineNode(service, pkgEnv));

        // Define function nodes.
        for (BLangFunction bLangFunction : pkgNode.functions) {
            // Define the lambda functions when visit lambda exprs because the lambda function is an expr.
            if (!bLangFunction.flagSet.contains(Flag.LAMBDA)) {
                defineNode(bLangFunction, pkgEnv);
            }
        }

        // Define annotation nodes.
        pkgNode.annotations.forEach(annot -> defineNode(annot, pkgEnv));

        for (BLangVariable variable : pkgNode.globalVars) {
            BLangExpression expr = variable.expr;
            if (expr != null && expr.getKind() == NodeKind.LAMBDA) {
                defineNode(((BLangLambdaFunction) expr).function, pkgEnv);
                if (variable.isDeclaredWithVar) {
                    setTypeFromLambdaExpr(variable);
                }
            }
            defineNode(variable, pkgEnv);
        }

        // Update globalVar for endpoints.
        for (BLangVariable var : pkgNode.globalVars) {
            if (var.getKind() == NodeKind.VARIABLE) {
                BVarSymbol varSymbol = var.symbol;
                if (varSymbol != null) {
                    BTypeSymbol tSymbol = varSymbol.type.tsymbol;
                    if (tSymbol != null && Symbols.isFlagOn(tSymbol.flags, Flags.CLIENT)) {
                        varSymbol.tag = SymTag.ENDPOINT;
                    }
                }
            }
        }
    }

    private void populateTypeToTypeDefMap(List<BLangNode> typeDefNodes) {
        typeToTypeDef = new HashMap<>(typeDefNodes.size());
        for (BLangNode typeDef : typeDefNodes) {
            if (typeDef.getKind() == NodeKind.TYPE_DEFINITION) {
                BLangTypeDefinition typeDefNode = (BLangTypeDefinition) typeDef;
                BType type = typeDefNode.typeNode.getBType();
                typeToTypeDef.put(type, typeDefNode);
            }
        }
    }

    private void defineDependentFields(List<BLangNode> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangNode typeDef : typeDefNodes) {
            if (typeDef.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDefinition = (BLangClassDefinition) typeDef;
                if (isObjectCtor(classDefinition)) {
                    continue;
                }
                defineReferencedFieldsOfClassDef(classDefinition, pkgEnv);
            } else if (typeDef.getKind() == NodeKind.TYPE_DEFINITION) {
                defineReferencedFieldsOfRecordTypeDef((BLangTypeDefinition) typeDef);
            }
        }
    }

    private void defineReferencedFieldsOfClassDef(BLangClassDefinition classDefinition, SymbolEnv pkgEnv) {
        SymbolEnv typeDefEnv = classDefinition.typeDefEnv;
        BObjectTypeSymbol tSymbol = (BObjectTypeSymbol) classDefinition.symbol;
        BObjectType objType = (BObjectType) tSymbol.type;

        defineReferencedClassFields(classDefinition, typeDefEnv, objType, false);
    }

    private void defineIntersectionTypes(SymbolEnv env) {
        for (BLangNode typeDescriptor : this.intersectionTypes) {
            defineNode(typeDescriptor, env);
        }
        this.intersectionTypes.clear();
    }

    private void defineErrorType(Location pos, BErrorType errorType, SymbolEnv env) {
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(env.enclPkg.symbol);
        BTypeSymbol errorTSymbol = errorType.tsymbol;
        errorTSymbol.scope = new Scope(errorTSymbol);

        if (symResolver.checkForUniqueSymbol(pos, pkgEnv, errorTSymbol)) {
            pkgEnv.scope.define(errorTSymbol.name, errorTSymbol);
        }

        SymbolEnv prevEnv = this.env;
        this.env = pkgEnv;
        this.env = prevEnv;
    }

    private boolean isObjectCtor(BLangNode node) {
        if (node.getKind() == NodeKind.CLASS_DEFN) {
            BLangClassDefinition classDefinition = (BLangClassDefinition) node;
            return isObjectCtor(classDefinition);
        }
        return false;
    }

    private boolean isObjectCtor(BLangClassDefinition classDefinition) {
        if (!classDefinition.isObjectContructorDecl && classDefinition.isServiceDecl) {
            return false;
        }
        if (classDefinition.flagSet.contains(Flag.OBJECT_CTOR)) {
            return true;
        }
        return false;
    }

    private void defineDistinctClassAndObjectDefinitions(List<BLangNode> typDefs) {
        for (BLangNode node : typDefs) {
            if (node.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDefinition = (BLangClassDefinition) node;
                if (isObjectCtor(classDefinition)) {
                    continue;
                }
                populateDistinctTypeIdsFromIncludedTypeReferences((BLangClassDefinition) node);
            } else if (node.getKind() == NodeKind.TYPE_DEFINITION) {
                populateDistinctTypeIdsFromIncludedTypeReferences((BLangTypeDefinition) node);
            }
        }
    }

    private void populateDistinctTypeIdsFromIncludedTypeReferences(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.typeNode.getKind() == NodeKind.INTERSECTION_TYPE_NODE) {
            if (typeDefinition.typeNode.getBType() == null) {
                return;
            }

            BType definingType = types.getTypeWithEffectiveIntersectionTypes(typeDefinition.typeNode.getBType());
            definingType = Types.getReferredType(definingType);
            if (definingType.tag != TypeTags.OBJECT) {
                return;
            }
            BObjectType definigObjType = (BObjectType) definingType;

            BLangIntersectionTypeNode typeNode = (BLangIntersectionTypeNode) typeDefinition.typeNode;
            for (BLangType constituentTypeNode : typeNode.getConstituentTypeNodes()) {
                BType constituentType = Types.getReferredType(constituentTypeNode.getBType());
                if (constituentType.tag != TypeTags.OBJECT) {
                    continue;
                }
                definigObjType.typeIdSet.add(((BObjectType) constituentType).typeIdSet);
            }
        } else if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDefinition.typeNode;
            BTypeIdSet typeIdSet = ((BObjectType) objectTypeNode.getBType()).typeIdSet;

            for (BLangType typeRef : objectTypeNode.typeRefs) {
                BType type = types.getTypeWithEffectiveIntersectionTypes(typeRef.getBType());
                type = Types.getReferredType(type);
                if (type.tag != TypeTags.OBJECT) {
                    continue;
                }
                BObjectType refType = (BObjectType) type;
                typeIdSet.add(refType.typeIdSet);
            }
        }
    }

    private void populateDistinctTypeIdsFromIncludedTypeReferences(BLangClassDefinition typeDef) {
        BLangClassDefinition classDefinition = typeDef;
        BTypeIdSet typeIdSet = ((BObjectType) classDefinition.getBType()).typeIdSet;

        for (BLangType typeRef : classDefinition.typeRefs) {
            BType type = types.getTypeWithEffectiveIntersectionTypes(typeRef.getBType());
            type = Types.getReferredType(type);
            if (type.tag != TypeTags.OBJECT) {
                continue;
            }
            BObjectType refType = (BObjectType) type;
            typeIdSet.add(refType.typeIdSet);
        }
    }

    private Comparator<BLangNode> getTypePrecedenceComparator() {
        return (l, r) -> {
            if (l instanceof OrderedNode && r instanceof OrderedNode) {
                return ((OrderedNode) l).getPrecedence() - ((OrderedNode) r).getPrecedence();
            }
            return 0;
        };
    }

    private void defineFunctionsOfClassDef(SymbolEnv pkgEnv, BLangClassDefinition classDefinition) {
        validateInclusionsForNonPrivateMembers(classDefinition.typeRefs);
        BObjectType objectType = (BObjectType) classDefinition.symbol.type;

        if (objectType.mutableType != null) {
            // If this is an object type definition defined for an immutable type.
            // We skip defining methods here since they would either be defined already, or would be defined
            // later.
            return;
        }

        SymbolEnv objMethodsEnv =
                SymbolEnv.createClassMethodsEnv(classDefinition, (BObjectTypeSymbol) classDefinition.symbol, pkgEnv);
        if (classDefinition.isObjectContructorDecl) {
            classDefinition.oceEnvData.objMethodsEnv = objMethodsEnv;
        }

        // Define the functions defined within the object
        defineClassInitFunction(classDefinition, objMethodsEnv);
        classDefinition.functions.forEach(f -> {
            f.flagSet.add(Flag.FINAL); // Method can't be changed
            f.setReceiver(ASTBuilderUtil.createReceiver(classDefinition.pos, objectType));
            defineNode(f, objMethodsEnv);
        });

        defineIncludedMethods(classDefinition, objMethodsEnv, false);
    }

    private void defineIncludedMethods(BLangClassDefinition classDefinition, SymbolEnv objMethodsEnv,
                                       boolean defineReadOnlyInclusionsOnly) {
        Set<String> includedFunctionNames = new HashSet<>();

        if (defineReadOnlyInclusionsOnly) {
            for (BAttachedFunction function :
                    ((BObjectTypeSymbol) classDefinition.getBType().tsymbol).referencedFunctions) {
                includedFunctionNames.add(function.funcName.value);
            }
        }

        // Add the attached functions of the referenced types to this object.
        // Here it is assumed that all the attached functions of the referred type are
        // resolved by the time we reach here. It is achieved by ordering the typeDefs
        // according to the precedence.
        for (BLangType typeRef : classDefinition.typeRefs) {
            BType type = Types.getReferredType(typeRef.getBType());
            if (type == null || type == symTable.semanticError) {
                return;
            }

            if (type.tag == TypeTags.INTERSECTION) {
                if (!defineReadOnlyInclusionsOnly) {
                    // Will be defined once all the readonly type's methods are defined.
                    continue;
                }

                type = ((BIntersectionType) type).effectiveType;
            } else {
                if (defineReadOnlyInclusionsOnly) {
                    if (!isImmutable((BObjectType) type)) {
                        continue;
                    }
                } else if (isImmutable((BObjectType) type)) {
                    continue;
                }
            }

            List<BAttachedFunction> functions = ((BObjectTypeSymbol) type.tsymbol).attachedFuncs;
            for (BAttachedFunction function : functions) {
                defineReferencedFunction(classDefinition.pos, classDefinition.flagSet, objMethodsEnv,
                        typeRef, function, includedFunctionNames, classDefinition.symbol, classDefinition.functions,
                        classDefinition.internal);
            }
        }
    }

    private void defineReferencedClassFields(BLangClassDefinition classDefinition, SymbolEnv typeDefEnv,
                                             BObjectType objType, boolean defineReadOnlyInclusionsOnly) {
        if (classDefinition.typeRefs.isEmpty()) {
            return;
        }
        Set<BSymbol> referencedTypes = new HashSet<>(classDefinition.typeRefs.size());
        List<BLangType> invalidTypeRefs = new ArrayList<>(classDefinition.typeRefs.size());

        Map<String, BLangSimpleVariable> fieldNames = new HashMap<>(classDefinition.fields.size());
        for (BLangSimpleVariable fieldVariable : classDefinition.fields) {
            fieldNames.put(fieldVariable.name.value, fieldVariable);
        }

        // Get the inherited fields from the type references
        List<BLangSimpleVariable> referencedFields = new ArrayList<>();

        for (BLangType typeRef : classDefinition.typeRefs) {
            BType referredType = Types.getReferredType(symResolver.resolveTypeNode(typeRef, typeDefEnv));
            if (referredType == symTable.semanticError) {
                continue;
            }

            int tag = Types.getReferredType(classDefinition.getBType()).tag;
            if (tag == TypeTags.OBJECT) {
                if (isInvalidIncludedTypeInClass(referredType)) {
                    if (!defineReadOnlyInclusionsOnly) {
                        dlog.error(typeRef.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_REFERENCE, typeRef);
                    }
                    invalidTypeRefs.add(typeRef);
                    continue;
                }

                BObjectType objectType = null;

                if (referredType.tag == TypeTags.INTERSECTION) {
                    if (!defineReadOnlyInclusionsOnly) {
                        // Will be defined once all the readonly type's fields are defined.
                        continue;
                    }
                } else {
                    objectType = (BObjectType) referredType;

                    if (defineReadOnlyInclusionsOnly) {
                        if (!isImmutable(objectType)) {
                            continue;
                        }
                    } else if (isImmutable(objectType)) {
                        continue;
                    }
                }
            } else if (defineReadOnlyInclusionsOnly) {
                continue;
            }

            // Check for duplicate type references
            if (!referencedTypes.add(referredType.tsymbol)) {
                dlog.error(typeRef.pos, DiagnosticErrorCode.REDECLARED_TYPE_REFERENCE, typeRef);
                continue;
            }

            BType effectiveIncludedType = referredType;

            if (tag == TypeTags.OBJECT) {
                BObjectType objectType;
                referredType = Types.getReferredType(referredType);

                if (referredType.tag == TypeTags.INTERSECTION) {
                    effectiveIncludedType = objectType = (BObjectType) ((BIntersectionType) referredType).effectiveType;
                } else {
                    objectType = (BObjectType) referredType;
                }

                if (!classDefinition.symbol.pkgID.equals(referredType.tsymbol.pkgID)) {
                    boolean errored = false;
                    for (BField field : objectType.fields.values()) {
                        if (!Symbols.isPublic(field.symbol)) {
                            dlog.error(typeRef.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS,
                                       typeRef);
                            invalidTypeRefs.add(typeRef);
                            errored = true;
                            break;
                        }
                    }

                    if (errored) {
                        continue;
                    }

                    for (BAttachedFunction func : ((BObjectTypeSymbol) objectType.tsymbol).attachedFuncs) {
                        if (!Symbols.isPublic(func.symbol)) {
                            dlog.error(typeRef.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS,
                                       typeRef);
                            invalidTypeRefs.add(typeRef);
                            errored = true;
                            break;
                        }
                    }

                    if (errored) {
                        continue;
                    }
                }
            }

            // Here it is assumed that all the fields of the referenced types are resolved
            // by the time we reach here. It is achieved by ordering the typeDefs according
            // to the precedence.
            // Default values of fields are not inherited.
            for (BField field : ((BStructureType) effectiveIncludedType).fields.values()) {
                if (fieldNames.containsKey(field.name.value)) {
                    BLangSimpleVariable existingVariable = fieldNames.get(field.name.value);
                    if ((existingVariable.flagSet.contains(Flag.PUBLIC) !=
                            Symbols.isFlagOn(field.symbol.flags, Flags.PUBLIC)) ||
                            (existingVariable.flagSet.contains(Flag.PRIVATE) !=
                                    Symbols.isFlagOn(field.symbol.flags, Flags.PRIVATE))) {
                        dlog.error(existingVariable.pos,
                                DiagnosticErrorCode.MISMATCHED_VISIBILITY_QUALIFIERS_IN_OBJECT_FIELD,
                                existingVariable.name.value);
                    }
                    continue;
                }

                BLangSimpleVariable var = ASTBuilderUtil.createVariable(typeRef.pos, field.name.value, field.type);
                var.flagSet = field.symbol.getFlags();
                referencedFields.add(var);
            }
        }
        classDefinition.typeRefs.removeAll(invalidTypeRefs);

        for (BLangSimpleVariable field : referencedFields) {
            defineNode(field, typeDefEnv);
            if (field.symbol.type == symTable.semanticError) {
                continue;
            }
            objType.fields.put(field.name.value, new BField(names.fromIdNode(field.name), field.pos, field.symbol));
        }

        classDefinition.referencedFields.addAll(referencedFields);
    }

    private List<BLangClassDefinition> getClassDefinitions(List<TopLevelNode> topLevelNodes) {
        List<BLangClassDefinition> classDefinitions = new ArrayList<>();
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == NodeKind.CLASS_DEFN) {
                classDefinitions.add((BLangClassDefinition) topLevelNode);
            }
        }
        return classDefinitions;
    }

    @Override
    public void visit(BLangObjectConstructorExpression objectCtorExpression) {
        visit(objectCtorExpression.classNode);
        objectCtorExpression.setBType(objectCtorExpression.classNode.getBType());
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        EnumSet<Flag> flags = EnumSet.copyOf(classDefinition.flagSet);
        boolean isPublicType = flags.contains(Flag.PUBLIC);
        Name className = names.fromIdNode(classDefinition.name);
        Name classOrigName = names.originalNameFromIdNode(classDefinition.name);

        BClassSymbol tSymbol = Symbols.createClassSymbol(Flags.asMask(flags), className, env.enclPkg.symbol.pkgID, null,
                                                         env.scope.owner, classDefinition.name.pos,
                                                         getOrigin(className, flags), classDefinition.isServiceDecl);
        tSymbol.originalName = classOrigName;
        tSymbol.scope = new Scope(tSymbol);
        tSymbol.markdownDocumentation = getMarkdownDocAttachment(classDefinition.markdownDocumentationAttachment);


        long typeFlags = 0;

        if (flags.contains(Flag.READONLY)) {
            typeFlags |= Flags.READONLY;
        }

        if (flags.contains(Flag.ISOLATED)) {
            typeFlags |= Flags.ISOLATED;
        }

        if (flags.contains(Flag.SERVICE)) {
            typeFlags |= Flags.SERVICE;
        }

        if (flags.contains(Flag.OBJECT_CTOR)) {
            typeFlags |= Flags.OBJECT_CTOR;
        }

        BObjectType objectType = new BObjectType(tSymbol, typeFlags);
        if (classDefinition.isObjectContructorDecl || flags.contains(Flag.OBJECT_CTOR)) {
            classDefinition.oceEnvData.objectType = objectType;
            objectType.classDef = classDefinition;
        }

        if (flags.contains(Flag.DISTINCT)) {
            objectType.typeIdSet = BTypeIdSet.from(env.enclPkg.symbol.pkgID, classDefinition.name.value, isPublicType);
        }

        if (flags.contains(Flag.CLIENT)) {
            objectType.flags |= Flags.CLIENT;
        }

        tSymbol.type = objectType;
        classDefinition.setBType(objectType);
        classDefinition.setDeterminedType(objectType);
        classDefinition.symbol = tSymbol;

        if (isDeprecated(classDefinition.annAttachments)) {
            tSymbol.flags |= Flags.DEPRECATED;
        }

        // For each referenced type, check whether the types are already resolved.
        // If not, then that type should get a higher precedence.
        for (BLangType typeRef : classDefinition.typeRefs) {
            BType referencedType = symResolver.resolveTypeNode(typeRef, env);
            if (referencedType == symTable.noType && !this.unresolvedTypes.contains(classDefinition)) {
                this.unresolvedTypes.add(classDefinition);
                return;
            }
            objectType.typeInclusions.add(referencedType);
        }

        classDefinition.setPrecedence(this.typePrecedence++);
        if (symResolver.checkForUniqueSymbol(classDefinition.pos, env, tSymbol)) {
            env.scope.define(tSymbol.name, tSymbol);
        }
        // TODO : check
        // env.scope.define(tSymbol.name, tSymbol);
    }

    public void visit(BLangAnnotation annotationNode) {
        Name annotName = names.fromIdNode(annotationNode.name);
        Name annotOrigName = names.originalNameFromIdNode(annotationNode.name);
        BAnnotationSymbol annotationSymbol = Symbols.createAnnotationSymbol(Flags.asMask(annotationNode.flagSet),
                                                                            annotationNode.getAttachPoints(),
                                                                            annotName, annotOrigName,
                                                                            env.enclPkg.symbol.pkgID, null,
                                                                            env.scope.owner, annotationNode.name.pos,
                                                                            getOrigin(annotName));
        annotationSymbol.markdownDocumentation =
                getMarkdownDocAttachment(annotationNode.markdownDocumentationAttachment);
        if (isDeprecated(annotationNode.annAttachments)) {
            annotationSymbol.flags |= Flags.DEPRECATED;
        }
        annotationSymbol.type = new BAnnotationType(annotationSymbol);
        annotationNode.symbol = annotationSymbol;
        defineSymbol(annotationNode.name.pos, annotationSymbol);
        SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotationNode, annotationSymbol.scope, env);
        BLangType annotTypeNode = annotationNode.typeNode;
        if (annotTypeNode != null) {
            BType type = this.symResolver.resolveTypeNode(annotTypeNode, annotationEnv);
            annotationSymbol.attachedType = type;
            if (!isValidAnnotationType(type)) {
                dlog.error(annotTypeNode.pos, DiagnosticErrorCode.ANNOTATION_INVALID_TYPE, type);
            }

//            if (annotationNode.flagSet.contains(Flag.CONSTANT) && !type.isAnydata()) {
//                dlog.error(annotTypeNode.pos, DiagnosticErrorCode.ANNOTATION_INVALID_CONST_TYPE, type);
//            }
        }

        if (!annotationNode.flagSet.contains(Flag.CONSTANT) &&
                annotationNode.getAttachPoints().stream().anyMatch(attachPoint -> attachPoint.source)) {
            dlog.error(annotationNode.pos, DiagnosticErrorCode.ANNOTATION_REQUIRES_CONST);
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        Name pkgAlias = names.fromIdNode(importPkgNode.alias);
        if (!Names.IGNORE.equals(pkgAlias)) {
            BSymbol importSymbol =
                    symResolver.resolvePrefixSymbol(env, pkgAlias, names.fromIdNode(importPkgNode.compUnit));
            if (importSymbol != symTable.notFoundSymbol) {
                if (isSameImport(importPkgNode, (BPackageSymbol) importSymbol)) {
                    dlog.error(importPkgNode.pos, DiagnosticErrorCode.REDECLARED_IMPORT_MODULE,
                            importPkgNode.getQualifiedPackageName());
                } else {
                    dlog.error(importPkgNode.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, pkgAlias);
                }
                return;
            }
        }

        // TODO Clean this code up. Can we move the this to BLangPackageBuilder class
        // Create import package symbol
        Name orgName;
        Name pkgName = null;
        Name version;
        PackageID enclPackageID = env.enclPkg.packageID;
        // The pattern of the import statement is 'import [org-name /] module-name [version sem-ver]'
        // Three cases should be considered here.
        // 1. import org-name/module-name version
        // 2. import org-name/module-name
        //      2a. same project
        //      2b. different project
        // 3. import module-name
        if (!isNullOrEmpty(importPkgNode.orgName.value)) {
            orgName = names.fromIdNode(importPkgNode.orgName);
            if (!isNullOrEmpty(importPkgNode.version.value)) {
                version = names.fromIdNode(importPkgNode.version);
            } else {
                // TODO We are removing the version in the import declaration anyway
                if (projectAPIInitiatedCompilation) {
                    version = Names.EMPTY;
                } else {
                    String pkgNameComps = importPkgNode.getPackageName().stream()
                            .map(id -> id.value)
                            .collect(Collectors.joining("."));
                    if (this.sourceDirectory.getSourcePackageNames().contains(pkgNameComps)
                            && orgName.value.equals(enclPackageID.orgName.value)) {
                        version = enclPackageID.version;
                    } else {
                        version = Names.EMPTY;
                    }
                }
            }
        } else {
            orgName = enclPackageID.orgName;
            pkgName = enclPackageID.pkgName;
            version = (Names.DEFAULT_VERSION.equals(enclPackageID.version)) ? Names.EMPTY : enclPackageID.version;
        }

        List<Name> nameComps = importPkgNode.pkgNameComps.stream()
                .map(identifier -> names.fromIdNode(identifier))
                .collect(Collectors.toList());
        Name moduleName = new Name(nameComps.stream().map(Name::getValue).collect(Collectors.joining(".")));

        if (pkgName == null) {
            pkgName = moduleName;
        }

        PackageID pkgId = new PackageID(orgName, pkgName, moduleName, version, null);

        // Un-exported modules not inside current package is not allowed to import.
        BPackageSymbol bPackageSymbol = this.packageCache.getSymbol(pkgId);
        if (bPackageSymbol != null && this.env.enclPkg.moduleContextDataHolder != null) {
            boolean isCurrentPackageModuleImport =
                this.env.enclPkg.moduleContextDataHolder.descriptor().org() == bPackageSymbol.descriptor.org()
                    && this.env.enclPkg.moduleContextDataHolder.descriptor().packageName() ==
                        bPackageSymbol.descriptor.packageName();
            if (!isCurrentPackageModuleImport && !bPackageSymbol.exported) {
                dlog.error(importPkgNode.pos, DiagnosticErrorCode.MODULE_NOT_FOUND,
                           bPackageSymbol.toString() + " is not exported");
                           return;
            }
        }

        // Built-in Annotation module is not allowed to import.
        if (pkgId.equals(PackageID.ANNOTATIONS) || pkgId.equals(PackageID.INTERNAL) || pkgId.equals(PackageID.QUERY)) {
            // Only peer lang.* modules able to see these two modules.
            // Spec allows to annotation model to be imported, but implementation not support this.
            if (!(enclPackageID.orgName.equals(Names.BALLERINA_ORG)
                    && enclPackageID.name.value.startsWith(Names.LANG.value))) {
                dlog.error(importPkgNode.pos, DiagnosticErrorCode.MODULE_NOT_FOUND,
                        importPkgNode.getQualifiedPackageName());
                return;
            }
        }

        if (symResolver.isModuleGeneratedForClientDeclaration(enclPackageID, pkgId)) {
            dlog.error(importPkgNode.pos, DiagnosticErrorCode.CANNOT_IMPORT_MODULE_GENERATED_FOR_CLIENT_DECL);
            return;
        }

        // Detect cyclic module dependencies. This will not detect cycles which starts with the entry package because
        // entry package has a version. So we check import cycles which starts with the entry package in next step.
        if (importedPackages.contains(pkgId)) {
            int index = importedPackages.indexOf(pkgId);
            // Generate the import cycle.
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = index; i < importedPackages.size(); i++) {
                stringBuilder.append(importedPackages.get(i).toString()).append(" -> ");
            }
            // Append the current package to complete the cycle.
            stringBuilder.append(pkgId);
            dlog.error(importPkgNode.pos, DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED, stringBuilder.toString());
            return;
        }

        boolean samePkg = false;
        // Get the entry package.
        PackageID entryPackage = importedPackages.get(0);
        if (entryPackage.isUnnamed == pkgId.isUnnamed) {
            samePkg = (!entryPackage.isUnnamed) || (entryPackage.sourceFileName.equals(pkgId.sourceFileName));
        }
        // Check whether the package which we have encountered is the same as the entry package. We don't need to
        // check the version here because we cannot import two different versions of the same package at the moment.
        if (samePkg && entryPackage.orgName.equals(pkgId.orgName) && entryPackage.name.equals(pkgId.name)) {
            StringBuilder stringBuilder = new StringBuilder();
            String entryPackageString = importedPackages.get(0).toString();
            // We need to remove the package.
            int packageIndex = entryPackageString.indexOf(":");
            if (packageIndex != -1) {
                entryPackageString = entryPackageString.substring(0, packageIndex);
            }
            // Generate the import cycle.
            stringBuilder.append(entryPackageString).append(" -> ");
            for (int i = 1; i < importedPackages.size(); i++) {
                stringBuilder.append(importedPackages.get(i).toString()).append(" -> ");
            }
            stringBuilder.append(pkgId);
            dlog.error(importPkgNode.pos, DiagnosticErrorCode.CYCLIC_MODULE_IMPORTS_DETECTED, stringBuilder.toString());
            return;
        }

        BPackageSymbol pkgSymbol = packageCache.getSymbol(pkgId);

        if (pkgSymbol == null) {
            dlog.error(importPkgNode.pos, DiagnosticErrorCode.MODULE_NOT_FOUND,
                    importPkgNode.getQualifiedPackageName());
            return;
        }

        List<BPackageSymbol> imports = ((BPackageSymbol) this.env.scope.owner).imports;
        if (!imports.contains(pkgSymbol)) {
            imports.add(pkgSymbol);
        }

        // get a copy of the package symbol, add compilation unit info to it,
        // and define it in the current package scope
        BPackageSymbol symbol = dupPackageSymbolAndSetCompUnit(pkgSymbol, names.fromIdNode(importPkgNode.compUnit));
        if (!Names.IGNORE.equals(pkgAlias)) {
            symbol.importPrefix = pkgAlias;
        }
        symbol.scope = pkgSymbol.scope;
        importPkgNode.symbol = symbol;
        this.env.scope.define(pkgAlias, symbol);
    }

    public void initPredeclaredModules(Map<Name, BPackageSymbol> predeclaredModules,
                                       List<BLangCompilationUnit> compUnits, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        for (Map.Entry<Name, BPackageSymbol> predeclaredModuleEntry : predeclaredModules.entrySet()) {
            Name alias = predeclaredModuleEntry.getKey();
            BPackageSymbol packageSymbol = predeclaredModuleEntry.getValue();
            int index = 0;
            ScopeEntry entry = this.env.scope.lookup(alias);
            if (entry == NOT_FOUND_ENTRY && !compUnits.isEmpty()) {
                this.env.scope.define(alias, dupPackageSymbolAndSetCompUnit(packageSymbol,
                        new Name(compUnits.get(index++).name)));
                entry = this.env.scope.lookup(alias);
            }
            for (int i = index; i < compUnits.size(); i++) {
                boolean isUndefinedModule = true;
                String compUnitName = compUnits.get(i).name;
                if (((BPackageSymbol) entry.symbol).compUnit.value.equals(compUnitName)) {
                    isUndefinedModule = false;
                }
                while (entry.next != NOT_FOUND_ENTRY) {
                    if (((BPackageSymbol) entry.next.symbol).compUnit.value.equals(compUnitName)) {
                        isUndefinedModule = false;
                        break;
                    }
                    entry = entry.next;
                }
                if (isUndefinedModule) {
                    entry.next = new ScopeEntry(dupPackageSymbolAndSetCompUnit(packageSymbol,
                            new Name(compUnitName)), NOT_FOUND_ENTRY);
                }
            }
        }
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        String nsURI;
        if (xmlnsNode.namespaceURI.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) xmlnsNode.namespaceURI;
            if (missingNodesHelper.isMissingNode(varRef.variableName.value)) {
                nsURI = "";
            } else {
                // TODO: handle const-ref (#24911)
                nsURI = "";
            }
        } else {
            nsURI = (String) ((BLangLiteral) xmlnsNode.namespaceURI).value;
            if (!nullOrEmpty(xmlnsNode.prefix.value) && nsURI.isEmpty()) {
                dlog.error(xmlnsNode.pos, DiagnosticErrorCode.INVALID_NAMESPACE_DECLARATION, xmlnsNode.prefix);
            }
        }

        // set the prefix of the default namespace
        if (xmlnsNode.prefix.value == null) {
            xmlnsNode.prefix.value = XMLConstants.DEFAULT_NS_PREFIX;
        }

        Name prefix = names.fromIdNode(xmlnsNode.prefix);
        Location nsSymbolPos = prefix.value.isEmpty() ? xmlnsNode.pos : xmlnsNode.prefix.pos;
        BXMLNSSymbol xmlnsSymbol = Symbols.createXMLNSSymbol(prefix, nsURI, env.enclPkg.symbol.pkgID, env.scope.owner,
                                                             nsSymbolPos, getOrigin(prefix));
        xmlnsNode.symbol = xmlnsSymbol;

        // First check for package-imports with the same alias.
        // Here we do not check for owner equality, since package import is always at the package
        // level, but the namespace declaration can be at any level.
        BSymbol foundSym = symResolver.lookupSymbolInPrefixSpace(env, xmlnsSymbol.name);
        if ((foundSym.tag & SymTag.PACKAGE) != SymTag.PACKAGE) {
            foundSym = symTable.notFoundSymbol;
        }
        if (foundSym != symTable.notFoundSymbol) {
            dlog.error(xmlnsNode.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, xmlnsSymbol.name);
            return;
        }

        // Define it in the enclosing scope. Here we check for the owner equality,
        // to support overriding of namespace declarations defined at package level.
        defineSymbol(xmlnsNode.prefix.pos, xmlnsSymbol);
    }

    private boolean nullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        defineNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    @Override
    public void visit(BLangClientDeclaration clientDeclaration) {
        BLangIdentifier prefix = clientDeclaration.prefix;

        if (!symTable.clientDeclarations.containsKey(this.env.enclPkg.packageID) ||
                !symTable.clientDeclarations.get(this.env.enclPkg.packageID)
                        .containsKey(clientDeclaration.pos.lineRange().filePath())) {
            dlog.error(clientDeclaration.pos, DiagnosticErrorCode.NO_MODULE_GENERATED_FOR_CLIENT_DECL);
        } else {
            Map<LineRange, Optional<PackageID>> lineRangeMap =
                    symTable.clientDeclarations.get(this.env.enclPkg.packageID)
                    .get(clientDeclaration.pos.lineRange().filePath());
            if (!lineRangeMap.containsKey(prefix.pos.lineRange())) {
                dlog.error(clientDeclaration.pos, DiagnosticErrorCode.NO_MODULE_GENERATED_FOR_CLIENT_DECL);
            }
        }

        Name prefixName = names.fromIdNode(prefix);

        BClientDeclarationSymbol clientDeclarationSymbol =
                Symbols.createClientDeclarationSymbol(prefixName, (String) clientDeclaration.uri.value,
                                                      env.enclPkg.symbol.pkgID, env.scope.owner, prefix.pos,
                                                      getOrigin(prefixName));
        clientDeclaration.symbol = clientDeclarationSymbol;

        // Check for module imports with the same prefix separately since the owner for a module is not the current
        // module.
        BSymbol foundSym = symResolver.lookupSymbolInPrefixSpace(env, clientDeclarationSymbol.name);
        if ((foundSym.tag & SymTag.PACKAGE) != SymTag.PACKAGE) {
            foundSym = symTable.notFoundSymbol;
        }

        if (foundSym != symTable.notFoundSymbol) {
            dlog.error(prefix.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, clientDeclarationSymbol.name);
            return;
        }

        // Define it in the enclosing scope. This will check for the owner equality.
        defineSymbol(prefix.pos, clientDeclarationSymbol);
    }

    @Override
    public void visit(BLangClientDeclarationStatement clientDeclarationStatement) {
        defineNode(clientDeclarationStatement.clientDeclaration, env);
    }

    private void defineTypeNodes(List<BLangNode> typeDefs, SymbolEnv env) {
        if (typeDefs.isEmpty()) {
            return;
        }

        this.unresolvedTypes = new ArrayList<>(typeDefs.size());
        this.unresolvedRecordDueToFields = new HashSet<>(typeDefs.size());
        this.resolveRecordsUnresolvedDueToFields = false;
        for (BLangNode typeDef : typeDefs) {
            if (isErrorIntersectionTypeCreatingNewType(typeDef, env)) {
                populateUndefinedErrorIntersection((BLangTypeDefinition) typeDef, env);
                continue;
            }
//            if (isObjectCtor(typeDef)) {
//                continue;
//            }

            defineNode(typeDef, env);
        }

        if (typeDefs.size() <= unresolvedTypes.size()) {

            this.resolveRecordsUnresolvedDueToFields = true;
            unresolvedTypes.removeAll(unresolvedRecordDueToFields);
            for (BLangNode unresolvedType : unresolvedRecordDueToFields) {
                defineNode(unresolvedType, env);
            }
            this.resolveRecordsUnresolvedDueToFields = false;

            // This situation can occur due to either a cyclic dependency or at least one of member types in type
            // definition node cannot be resolved. So we iterate through each node recursively looking for cyclic
            // dependencies or undefined types in type node.

            for (BLangNode unresolvedType : unresolvedTypes) {
                Stack<String> references = new Stack<>();
                NodeKind unresolvedKind = unresolvedType.getKind();
                if (unresolvedKind == NodeKind.TYPE_DEFINITION || unresolvedKind == NodeKind.CONSTANT) {
                    TypeDefinition def = (TypeDefinition) unresolvedType;
                    // We need to keep track of all visited types to print cyclic dependency.
                    references.push(def.getName().getValue());
                    checkErrors(env, unresolvedType, (BLangNode) def.getTypeNode(), references, false);
                } else if (unresolvedType.getKind() == NodeKind.CLASS_DEFN) {
                    BLangClassDefinition classDefinition = (BLangClassDefinition) unresolvedType;
                    references.push(classDefinition.getName().getValue());
                    checkErrors(env, unresolvedType, classDefinition, references, true);
                }
            }
            defineAllUnresolvedCyclicTypesInScope(env);

            Set<String> alreadyDefinedTypeDefNames = new HashSet<>();
            int unresolvedTypeCount = unresolvedTypes.size();
            for (int i = 0; i < unresolvedTypeCount; i++) {
                for (BLangNode node : this.unresolvedTypes) {
                    String name = getTypeOrClassName(node);
                    boolean symbolNotFound = false;
                    boolean isTypeOrClassDefinition =
                            node.getKind() == NodeKind.TYPE_DEFINITION || node.getKind() == NodeKind.CLASS_DEFN;
                    // Skip the type resolving in the first iteration (i == 0)
                    // as we want to define the type before trying to resolve it.
                    if (isTypeOrClassDefinition && i != 0) { // Do not skip the first iteration
                        BSymbol bSymbol = symResolver.lookupSymbolInMainSpace(env, names.fromString(name));
                        symbolNotFound = (bSymbol == symTable.notFoundSymbol);
                    }

                    boolean notFoundInList = alreadyDefinedTypeDefNames.add(name);

                    // Prevent defining already defined type names.
                    if (notFoundInList || symbolNotFound) {
                        defineNode(node, env);
                    }
                }
            }
            return;
        }
        defineTypeNodes(unresolvedTypes, env);
    }

    private void populateUndefinedErrorIntersection(BLangTypeDefinition typeDef, SymbolEnv env) {
        long flags = 0;
        if (typeDef.flagSet.contains(Flag.PUBLIC)) {
            flags = Flags.PUBLIC;
        }

        BErrorType intersectionErrorType = types.createErrorType(null, flags, env);
        intersectionErrorType.tsymbol.name = names.fromString(typeDef.name.value);
        defineErrorType(typeDef.pos, intersectionErrorType, env);

        this.intersectionTypes.add(typeDef);
    }

    private boolean isErrorIntersectionTypeCreatingNewType(BLangNode typeDef, SymbolEnv env) {
        boolean isIntersectionType = typeDef.getKind() == NodeKind.TYPE_DEFINITION
                && ((BLangTypeDefinition) typeDef).typeNode.getKind() == NodeKind.INTERSECTION_TYPE_NODE;
        if (!isIntersectionType) {
            return false;
        }

        BLangIntersectionTypeNode intersectionTypeNode =
                (BLangIntersectionTypeNode) ((BLangTypeDefinition) typeDef).typeNode;

        Set<BType> errorTypes = new HashSet<>();

        for (BLangType type : intersectionTypeNode.constituentTypeNodes) {
            BType bType = symResolver.resolveTypeNode(type, env);
            if (Types.getReferredType(bType).tag == TypeTags.ERROR) {
                errorTypes.add(bType);
            }
        }
        return errorTypes.size() > 1;
    }

    private void checkErrors(SymbolEnv env, BLangNode unresolvedType, BLangNode currentTypeOrClassNode,
                             Stack<String> visitedNodes,
                             boolean fromStructuredType) {
        // Check errors in the type definition.
        List<BLangType> memberTypeNodes;
        switch (currentTypeOrClassNode.getKind()) {
            case ARRAY_TYPE:
                checkErrors(env, unresolvedType, ((BLangArrayType) currentTypeOrClassNode).elemtype, visitedNodes,
                        true);
                break;
            case UNION_TYPE_NODE:
                // If the current type node is a union type node, we need to check all member nodes.
                memberTypeNodes = ((BLangUnionTypeNode) currentTypeOrClassNode).memberTypeNodes;
                // Recursively check all members.
                for (BLangType memberTypeNode : memberTypeNodes) {
                    checkErrors(env, unresolvedType, memberTypeNode, visitedNodes, fromStructuredType);
                }
                break;
            case INTERSECTION_TYPE_NODE:
                memberTypeNodes = ((BLangIntersectionTypeNode) currentTypeOrClassNode).constituentTypeNodes;
                for (BLangType memberTypeNode : memberTypeNodes) {
                    checkErrors(env, unresolvedType, memberTypeNode, visitedNodes, fromStructuredType);
                }
                break;
            case TUPLE_TYPE_NODE:
                BLangTupleTypeNode tupleNode = (BLangTupleTypeNode) currentTypeOrClassNode;
                memberTypeNodes = tupleNode.memberTypeNodes;
                for (BLangType memberTypeNode : memberTypeNodes) {
                    checkErrors(env, unresolvedType, memberTypeNode, visitedNodes, true);
                }
                if (tupleNode.restParamType != null) {
                    checkErrors(env, unresolvedType, tupleNode.restParamType, visitedNodes, true);
                }
                break;
            case CONSTRAINED_TYPE:
                checkErrors(env, unresolvedType, ((BLangConstrainedType) currentTypeOrClassNode).constraint,
                        visitedNodes,
                        true);
                break;
            case TABLE_TYPE:
                checkErrors(env, unresolvedType, ((BLangTableTypeNode) currentTypeOrClassNode).constraint, visitedNodes,
                        true);
                break;
            case STREAM_TYPE:
                checkErrors(env, unresolvedType, ((BLangStreamType) currentTypeOrClassNode).constraint, visitedNodes,
                        true);
                BLangType completionType = ((BLangStreamType) currentTypeOrClassNode).error;
                if (completionType != null) {
                    checkErrors(env, unresolvedType, completionType, visitedNodes, true);
                }
                break;
            case USER_DEFINED_TYPE:
                checkErrorsOfUserDefinedType(env, unresolvedType, (BLangUserDefinedType) currentTypeOrClassNode,
                        visitedNodes, fromStructuredType);
                break;
            case BUILT_IN_REF_TYPE:
                // Eg - `xml`. This is not needed to be checked because no types are available in the `xml`.
            case FINITE_TYPE_NODE:
            case VALUE_TYPE:
            case ERROR_TYPE:
                // Do nothing.
                break;
            case FUNCTION_TYPE:
                BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) currentTypeOrClassNode;
                functionTypeNode.params.forEach(p -> checkErrors(env, unresolvedType, p.typeNode, visitedNodes,
                        fromStructuredType));
                if (functionTypeNode.restParam != null) {
                    checkErrors(env, unresolvedType, functionTypeNode.restParam.typeNode, visitedNodes,
                            fromStructuredType);
                }
                if (functionTypeNode.returnTypeNode != null) {
                    checkErrors(env, unresolvedType, functionTypeNode.returnTypeNode, visitedNodes, fromStructuredType);
                }
                break;
            case RECORD_TYPE:
                for (TypeNode typeNode : ((BLangRecordTypeNode) currentTypeOrClassNode).getTypeReferences()) {
                    checkErrors(env, unresolvedType, (BLangType) typeNode, visitedNodes, true);
                }
                break;
            case OBJECT_TYPE:
                for (TypeNode typeNode : ((BLangObjectTypeNode) currentTypeOrClassNode).getTypeReferences()) {
                    checkErrors(env, unresolvedType, (BLangType) typeNode, visitedNodes, true);
                }
                break;
            case CLASS_DEFN:
                for (TypeNode typeNode : ((BLangClassDefinition) currentTypeOrClassNode).typeRefs) {
                    checkErrors(env, unresolvedType, (BLangType) typeNode, visitedNodes, true);
                }
                break;
            default:
                throw new RuntimeException("unhandled type kind: " + currentTypeOrClassNode.getKind());
        }
    }

    private boolean isTypeConstructorAvailable(NodeKind unresolvedType) {
        switch (unresolvedType) {
            case OBJECT_TYPE:
            case RECORD_TYPE:
            case CONSTRAINED_TYPE:
            case ARRAY_TYPE:
            case TUPLE_TYPE_NODE:
            case TABLE_TYPE:
            case ERROR_TYPE:
            case FUNCTION_TYPE:
            case STREAM_TYPE:
                return true;
            default:
                return false;
        }
    }

    private void checkErrorsOfUserDefinedType(SymbolEnv env, BLangNode unresolvedType,
                                              BLangUserDefinedType currentTypeOrClassNode,
                                              Stack<String> visitedNodes, boolean fromStructuredType) {
        String currentTypeNodeName = currentTypeOrClassNode.typeName.value;
        // Skip all types defined as anonymous types.
        if (currentTypeNodeName.startsWith("$")) {
            return;
        }
        String unresolvedTypeNodeName = getTypeOrClassName(unresolvedType);
        boolean sameTypeNode = unresolvedTypeNodeName.equals(currentTypeNodeName);
        boolean isVisited = visitedNodes.contains(currentTypeNodeName);
        boolean typeDef = unresolvedType.getKind() == NodeKind.TYPE_DEFINITION;

        if (sameTypeNode || isVisited) {
            if (typeDef) {
                BLangTypeDefinition typeDefinition = (BLangTypeDefinition) unresolvedType;
                NodeKind unresolvedTypeNodeKind = typeDefinition.getTypeNode().getKind();
                if (fromStructuredType && (unresolvedTypeNodeKind == NodeKind.UNION_TYPE_NODE
                        || unresolvedTypeNodeKind == NodeKind.TUPLE_TYPE_NODE)) {
                    // Type definitions with tuples and unions are allowed to have cyclic references atm
                    typeDefinition.hasCyclicReference = true;
                    return;
                }
                // Recursive types (A -> B -> C -> B) are valid provided they go through a type constructor
                if (unresolvedTypeNodeKind != NodeKind.OBJECT_TYPE && isTypeConstructorAvailable(unresolvedTypeNodeKind)
                    && !sameTypeNode) {
                    return;
                }
            }
            if (isVisited) {
                // Invalid dependency detected. But in here, all the types in the list might not
                // be necessary for the cyclic dependency error message.
                //
                // Eg - A -> B -> C -> B // Last B is what we are currently checking
                //
                // In such case, we create a new list with relevant type names.
                int i = visitedNodes.indexOf(currentTypeNodeName);
                List<String> dependencyList = new ArrayList<>(visitedNodes.size() - i);
                for (; i < visitedNodes.size(); i++) {
                    dependencyList.add(visitedNodes.get(i));
                }
                if (!sameTypeNode && dependencyList.size() == 1
                        && dependencyList.get(0).equals(currentTypeNodeName)) {
                    // Check to support valid scenarios such as the following
                    // type A int\A[];
                    // type B A;
                    // @typeparam type B A;
                    return;
                }
                // Add the `currentTypeNodeName` to complete the cycle.
                dependencyList.add(currentTypeNodeName);
                dlog.error(unresolvedType.getPosition(), DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE, dependencyList);
            } else {
                visitedNodes.push(currentTypeNodeName);
                dlog.error(unresolvedType.getPosition(), DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE, visitedNodes);
                visitedNodes.remove(currentTypeNodeName);
            }
        } else {
            // Check whether the current type node is in the unresolved list. If it is in the list, we need to
            // check it recursively.
            List<BLangNode> typeDefinitions = unresolvedTypes.stream()
                    .filter(node -> getTypeOrClassName(node).equals(currentTypeNodeName)).collect(Collectors.toList());

            if (typeDefinitions.isEmpty()) {
                BType referredType = symResolver.resolveTypeNode(currentTypeOrClassNode, env);
                // We are referring a fully or partially defined type from another cyclic type
                if (referredType != symTable.noType) {
                    return;
                }

                // If a type is declared, it should either get defined successfully or added to the unresolved
                // types list. If a type is not in either one of them, that means it is an undefined type.
                LocationData locationData = new LocationData(
                        currentTypeNodeName, currentTypeOrClassNode.pos.lineRange().startLine().line(),
                        currentTypeOrClassNode.pos.lineRange().startLine().offset());
                if (unknownTypeRefs.add(locationData)) {
                    dlog.error(currentTypeOrClassNode.pos, DiagnosticErrorCode.UNKNOWN_TYPE, currentTypeNodeName);
                }
            } else {
                for (BLangNode typeDefinition : typeDefinitions) {
                    if (typeDefinition.getKind() == NodeKind.TYPE_DEFINITION) {
                        BLangTypeDefinition langTypeDefinition = (BLangTypeDefinition) typeDefinition;
                        String typeName = langTypeDefinition.getName().getValue();
                        // Add the node name to the list.
                        visitedNodes.push(typeName);
                        // Recursively check for errors.
                        checkErrors(env, unresolvedType, langTypeDefinition.getTypeNode(), visitedNodes,
                                fromStructuredType);
                        // We need to remove the added type node here since we have finished checking errors.
                        visitedNodes.pop();
                    } else {
                        BLangClassDefinition classDefinition = (BLangClassDefinition) typeDefinition;
                        visitedNodes.push(classDefinition.getName().getValue());
                        checkErrors(env, unresolvedType, classDefinition, visitedNodes, fromStructuredType);
                        visitedNodes.pop();
                    }
                }
            }
        }
    }

    private String getTypeOrClassName(BLangNode node) {
        if (node.getKind() == NodeKind.TYPE_DEFINITION || node.getKind() == NodeKind.CONSTANT) {
            return ((TypeDefinition) node).getName().getValue();
        } else  {
            return ((BLangClassDefinition) node).getName().getValue();
        }
    }

    public boolean isUnknownTypeRef(BLangUserDefinedType bLangUserDefinedType) {
        var startLine = bLangUserDefinedType.pos.lineRange().startLine();
        LocationData locationData = new LocationData(bLangUserDefinedType.typeName.value, startLine.line(),
                startLine.offset());
        return unknownTypeRefs.contains(locationData);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BType definedType;
        if (typeDefinition.hasCyclicReference) {
            definedType = getCyclicDefinedType(typeDefinition, env);
        } else {
            definedType = symResolver.resolveTypeNode(typeDefinition.typeNode, env);
        }

        if (definedType == symTable.semanticError) {
            // TODO : Fix this properly. issue #21242

            invalidateAlreadyDefinedErrorType(typeDefinition);
            return;
        }
        if (definedType == symTable.noType) {
            // This is to prevent concurrent modification exception.
            if (!this.unresolvedTypes.contains(typeDefinition)) {
                this.unresolvedTypes.add(typeDefinition);
            }
            return;
        }

        // Check for any circular type references
        boolean hasTypeInclusions = false;
        NodeKind typeNodeKind = typeDefinition.typeNode.getKind();
        if (typeNodeKind == NodeKind.OBJECT_TYPE || typeNodeKind == NodeKind.RECORD_TYPE) {
            if (definedType.tsymbol.scope == null) {
                definedType.tsymbol.scope = new Scope(definedType.tsymbol);
            }
            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDefinition.typeNode;
            // For each referenced type, check whether the types are already resolved.
            // If not, then that type should get a higher precedence.
            for (BLangType typeRef : structureTypeNode.typeRefs) {
                hasTypeInclusions = true;
                BType referencedType = symResolver.resolveTypeNode(typeRef, env);
                if (referencedType == symTable.noType) {
                    if (!this.unresolvedTypes.contains(typeDefinition)) {
                        this.unresolvedTypes.add(typeDefinition);
                        return;
                    }
                }
            }
        }

        // check for unresolved fields. This record may be referencing another record
        if (hasTypeInclusions && !this.resolveRecordsUnresolvedDueToFields && typeNodeKind == NodeKind.RECORD_TYPE) {
            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDefinition.typeNode;
            for (BLangSimpleVariable variable : structureTypeNode.fields) {
                if (variable.typeNode.getKind() == NodeKind.FUNCTION_TYPE) {
                    continue;
                }
                Scope scope = new Scope(structureTypeNode.symbol);
                structureTypeNode.symbol.scope = scope;
                SymbolEnv typeEnv = SymbolEnv.createTypeEnv(structureTypeNode, scope, env);
                BType referencedType = symResolver.resolveTypeNode(variable.typeNode, typeEnv);
                if (referencedType == symTable.noType) {
                    if (this.unresolvedRecordDueToFields.add(typeDefinition) &&
                            !this.unresolvedTypes.contains(typeDefinition)) {
                        this.unresolvedTypes.add(typeDefinition);
                        return;
                    }
                }
            }
        }

        if (typeDefinition.flagSet.contains(Flag.ENUM)) {
            definedType.tsymbol = createEnumSymbol(typeDefinition, definedType);
        }

        typeDefinition.setPrecedence(this.typePrecedence++);

        BSymbol typeDefSymbol = Symbols.createTypeDefinitionSymbol(Flags.asMask(typeDefinition.flagSet),
                names.fromIdNode(typeDefinition.name), env.enclPkg.packageID, definedType, env.scope.owner,
                typeDefinition.name.pos, getOrigin(typeDefinition.name.value));
        typeDefSymbol.markdownDocumentation = getMarkdownDocAttachment(typeDefinition.markdownDocumentationAttachment);
        BTypeSymbol typeSymbol = new BTypeSymbol(SymTag.TYPE_REF, typeDefSymbol.flags, typeDefSymbol.name,
                typeDefSymbol.pkgID, typeDefSymbol.type, typeDefSymbol.owner, typeDefSymbol.pos, typeDefSymbol.origin);
        typeSymbol.markdownDocumentation = typeDefSymbol.markdownDocumentation;
        ((BTypeDefinitionSymbol) typeDefSymbol).referenceType = new BTypeReferenceType(definedType, typeSymbol,
                typeDefSymbol.type.flags);

        boolean isLabel = true;
        //todo remove after type ref introduced to runtime
        if (definedType.tsymbol.name == Names.EMPTY) {
            isLabel = false;
            definedType.tsymbol.name = names.fromIdNode(typeDefinition.name);
            definedType.tsymbol.originalName = names.fromIdNode(typeDefinition.name);
            definedType.tsymbol.flags |= typeDefSymbol.flags;

            definedType.tsymbol.markdownDocumentation = typeDefSymbol.markdownDocumentation;
            definedType.tsymbol.pkgID = env.enclPkg.packageID;
            if (definedType.tsymbol.tag == SymTag.ERROR) {
                definedType.tsymbol.owner = env.scope.owner;
            }
        }

        if ((((definedType.tsymbol.kind == SymbolKind.OBJECT
                && !Symbols.isFlagOn(definedType.tsymbol.flags, Flags.CLASS))
                || definedType.tsymbol.kind == SymbolKind.RECORD))
                && ((BStructureTypeSymbol) definedType.tsymbol).typeDefinitionSymbol == null) {
            ((BStructureTypeSymbol) definedType.tsymbol).typeDefinitionSymbol = (BTypeDefinitionSymbol) typeDefSymbol;
        }

        if (typeDefinition.flagSet.contains(Flag.ENUM)) {
            typeDefSymbol = definedType.tsymbol;
            typeDefSymbol.pos = typeDefinition.name.pos;
        }

        boolean isErrorIntersection = isErrorIntersection(definedType);
        if (isErrorIntersection) {
            populateSymbolNameOfErrorIntersection(definedType, typeDefinition.name.value);
            populateAllReadyDefinedErrorIntersection(definedType, typeDefinition, env);
        }

        BType referenceConstraintType = Types.getReferredType(definedType);
        boolean isIntersectionType = referenceConstraintType.tag == TypeTags.INTERSECTION && !isLabel;

        BType effectiveDefinedType = isIntersectionType ? ((BIntersectionType) referenceConstraintType).effectiveType :
                referenceConstraintType;

        boolean isIntersectionTypeWithNonNullEffectiveTypeSymbol =
                isIntersectionType && effectiveDefinedType.tsymbol != null;

        if (isIntersectionTypeWithNonNullEffectiveTypeSymbol) {
            BTypeSymbol effectiveTypeSymbol = effectiveDefinedType.tsymbol;
            effectiveTypeSymbol.name = typeDefSymbol.name;
            effectiveTypeSymbol.pkgID = typeDefSymbol.pkgID;
        }

        handleDistinctDefinition(typeDefinition, typeDefSymbol, definedType, referenceConstraintType);

        typeDefSymbol.flags |= Flags.asMask(typeDefinition.flagSet);
        // Reset public flag when set on a non public type.
        typeDefSymbol.flags &= getPublicFlagResetingMask(typeDefinition.flagSet, typeDefinition.typeNode);
        if (isDeprecated(typeDefinition.annAttachments)) {
            typeDefSymbol.flags |= Flags.DEPRECATED;
        }

        // Reset origin for anonymous types
        if (Symbols.isFlagOn(typeDefSymbol.flags, Flags.ANONYMOUS)) {
            typeDefSymbol.origin = VIRTUAL;
        }

        if (typeDefinition.annAttachments.stream()
                .anyMatch(attachment -> attachment.annotationName.value.equals(Names.ANNOTATION_TYPE_PARAM.value))) {
            // TODO : Clean this. Not a nice way to handle this.
            //  TypeParam is built-in annotation, and limited only within lang.* modules.
            if (PackageID.isLangLibPackageID(this.env.enclPkg.packageID)) {
                typeDefSymbol.type = typeParamAnalyzer.createTypeParam(typeDefSymbol.type, typeDefSymbol.name);
                typeDefSymbol.flags |= Flags.TYPE_PARAM;
            } else {
                dlog.error(typeDefinition.pos, DiagnosticErrorCode.TYPE_PARAM_OUTSIDE_LANG_MODULE);
            }
        }
        definedType.flags |= typeDefSymbol.flags;

        if (isIntersectionTypeWithNonNullEffectiveTypeSymbol) {
            BTypeSymbol effectiveTypeSymbol = effectiveDefinedType.tsymbol;
            effectiveTypeSymbol.flags |= definedType.tsymbol.flags;
            effectiveTypeSymbol.origin = VIRTUAL;
            effectiveDefinedType.flags |= definedType.flags;
        }

        typeDefinition.symbol = typeDefSymbol;

        if (typeDefinition.hasCyclicReference) {
            // Workaround for https://github.com/ballerina-platform/ballerina-lang/issues/29742
            typeDefinition.getBType().tsymbol = definedType.tsymbol;
        } else {
            boolean isLanglibModule = PackageID.isLangLibPackageID(this.env.enclPkg.packageID);
            if (isLanglibModule) {
                handleLangLibTypes(typeDefinition);
                return;
            }
            // We may have already defined error intersection
            if (!isErrorIntersection || lookupTypeSymbol(env, typeDefinition.name) == symTable.notFoundSymbol) {
                defineSymbol(typeDefinition.name.pos, typeDefSymbol);
            }
        }
    }

    private void handleDistinctDefinition(BLangTypeDefinition typeDefinition, BSymbol typeDefSymbol,
                                          BType definedType, BType referenceConstraintType) {
        BType distinctType = definedType;
        if (isDistinctFlagPresent(typeDefinition)) {
            if (referenceConstraintType.getKind() == TypeKind.ERROR) {
                distinctType = getDistinctErrorType(typeDefinition, (BErrorType) referenceConstraintType,
                        typeDefSymbol);
                typeDefinition.typeNode.setBType(distinctType);
            } else if (referenceConstraintType.tag == TypeTags.INTERSECTION &&
                    ((BIntersectionType) referenceConstraintType).effectiveType.getKind() == TypeKind.ERROR) {
                boolean distinctFlagPresentInTypeDef = typeDefinition.typeNode.flagSet.contains(Flag.DISTINCT);

                BTypeIdSet typeIdSet = BTypeIdSet.emptySet();
                int numberOfDistinctConstituentTypes = 0;
                BLangIntersectionTypeNode intersectionTypeNode = (BLangIntersectionTypeNode) typeDefinition.typeNode;
                for (BLangType constituentType : intersectionTypeNode.constituentTypeNodes) {
                    BType type = constituentType.getBType();

                    if (type.getKind() == TypeKind.ERROR) {
                        if (constituentType.flagSet.contains(Flag.DISTINCT)) {
                            numberOfDistinctConstituentTypes++;
                            typeIdSet.addSecondarySet(((BErrorType) type).typeIdSet.getAll());
                        } else {
                            typeIdSet.add(((BErrorType) type).typeIdSet);
                        }
                    }
                }

                BErrorType effectiveType = (BErrorType) ((BIntersectionType) referenceConstraintType).effectiveType;

                // if the distinct keyword is part of a distinct-type-descriptor that is the
                // only distinct-type-descriptor occurring within a module-type-defn,
                // then the local id is the name of the type defined by the module-type-defn.
                if (numberOfDistinctConstituentTypes == 1
                        || (numberOfDistinctConstituentTypes == 0 && distinctFlagPresentInTypeDef)) {
                    BTypeIdSet typeIdSetForDefinedType = BTypeIdSet.from(
                            env.enclPkg.packageID,
                            typeDefinition.name.value,
                            typeDefinition.flagSet.contains(Flag.PUBLIC),
                            typeIdSet);
                    effectiveType.typeIdSet.add(typeIdSetForDefinedType);
                } else {
                    for (BLangType constituentType : intersectionTypeNode.constituentTypeNodes) {
                        if (constituentType.getBType().getKind() != TypeKind.ERROR) {
                            continue;
                        }
                        if (constituentType.flagSet.contains(Flag.DISTINCT)) {
                            typeIdSet.add(BTypeIdSet.from(env.enclPkg.packageID,
                                    anonymousModelHelper.getNextAnonymousTypeId(env.enclPkg.packageID), true));
                        }
                    }
                    effectiveType.typeIdSet.add(typeIdSet);
                }

            } else if (referenceConstraintType.getKind() == TypeKind.OBJECT) {
                distinctType = getDistinctObjectType(typeDefinition, (BObjectType) referenceConstraintType,
                        referenceConstraintType.tsymbol);
                typeDefinition.typeNode.setBType(distinctType);
            }

            //setting the newly created distinct type as the referred type of the definition
            if (((BTypeDefinitionSymbol) typeDefSymbol).referenceType != null) {
                ((BTypeDefinitionSymbol) typeDefSymbol).referenceType.referredType = distinctType;
            }
            definedType.flags |= Flags.DISTINCT;
        }
    }

    private void invalidateAlreadyDefinedErrorType(BLangTypeDefinition typeDefinition) {
        // We need to invalidate the already defined type as we don't have a way to undefine it.
        BSymbol alreadyDefinedTypeSymbol = lookupTypeSymbol(env, typeDefinition.name);
        if (alreadyDefinedTypeSymbol.type.tag == TypeTags.ERROR) {
            alreadyDefinedTypeSymbol.type = symTable.errorType;
        }
    }

    private void populateErrorTypeIds(BErrorType effectiveType, BLangIntersectionTypeNode typeNode, String name,
                                      boolean distinctFlagPresentInTypeDef) {
        BTypeIdSet typeIdSet = BTypeIdSet.emptySet();
        int numberOfDistinctConstituentTypes = 0;

        for (BLangType constituentType : typeNode.constituentTypeNodes) {
            BType resolvedTypeNode = symResolver.resolveTypeNode(constituentType, env);
            BType type = Types.getReferredType(resolvedTypeNode);

            if (type.getKind() == TypeKind.ERROR) {
                if (constituentType.flagSet.contains(Flag.DISTINCT)) {
                    numberOfDistinctConstituentTypes++;
                    typeIdSet.addSecondarySet(((BErrorType) type).typeIdSet.getAll());
                } else {
                    typeIdSet.add(((BErrorType) type).typeIdSet);
                }
            }
        }

        // if the distinct keyword is part of a distinct-type-descriptor that is the
        // only distinct-type-descriptor occurring within a module-type-defn,
        // then the local id is the name of the type defined by the module-type-defn.
        if (numberOfDistinctConstituentTypes == 1
                || (numberOfDistinctConstituentTypes == 0 && distinctFlagPresentInTypeDef)) {
            effectiveType.typeIdSet = BTypeIdSet.from(env.enclPkg.packageID, name, true, typeIdSet);
        } else {
            for (BLangType constituentType : typeNode.constituentTypeNodes) {
                if (constituentType.flagSet.contains(Flag.DISTINCT)) {
                    typeIdSet.add(BTypeIdSet.from(env.enclPkg.packageID,
                                    anonymousModelHelper.getNextAnonymousTypeId(env.enclPkg.packageID), true));
                }
            }
            effectiveType.typeIdSet = typeIdSet;
        }
    }

    private void populateAllReadyDefinedErrorIntersection(BType definedType, BLangTypeDefinition typeDefinition,
                                                          SymbolEnv env) {

        BSymbol bSymbol = lookupTypeSymbol(env, typeDefinition.name);
        BErrorType alreadyDefinedErrorType = (BErrorType) bSymbol.type;

        boolean distinctFlagPresent = typeDefinition.typeNode.flagSet.contains(Flag.DISTINCT);

        BIntersectionType intersectionType = (BIntersectionType) definedType;
        BErrorType errorType = (BErrorType) intersectionType.effectiveType;
        populateErrorTypeIds(errorType, (BLangIntersectionTypeNode) typeDefinition.typeNode,
                typeDefinition.name.value, distinctFlagPresent);

        alreadyDefinedErrorType.typeIdSet = errorType.typeIdSet;
        alreadyDefinedErrorType.detailType = errorType.detailType;
        alreadyDefinedErrorType.flags = errorType.flags;
        alreadyDefinedErrorType.name = errorType.name;
        intersectionType.effectiveType = alreadyDefinedErrorType;

        if (!errorType.typeIdSet.isEmpty()) {
            definedType.flags |= Flags.DISTINCT;
        }
    }

    private BSymbol lookupTypeSymbol(SymbolEnv env, BLangIdentifier name) {
        return symResolver.lookupSymbolInMainSpace(env, names.fromString(name.value));
    }

    private void populateSymbolNameOfErrorIntersection(BType definedType, String typeDefName) {
        BErrorType effectiveErrorType = (BErrorType) ((BIntersectionType) definedType).effectiveType;
        effectiveErrorType.tsymbol.name = names.fromString(typeDefName);
    }

    private boolean isErrorIntersection(BType definedType) {
        BType type = Types.getReferredType(definedType);
        if (type.tag == TypeTags.INTERSECTION) {
            BIntersectionType intersectionType = (BIntersectionType) type;
            return intersectionType.effectiveType.tag == TypeTags.ERROR;
        }

        return false;
    }

    private BEnumSymbol createEnumSymbol(BLangTypeDefinition typeDefinition, BType definedType) {
        List<BConstantSymbol> enumMembers = new ArrayList<>();

        List<BLangType> members = ((BLangUnionTypeNode) typeDefinition.typeNode).memberTypeNodes;
        for (BLangType member : members) {
            enumMembers.add((BConstantSymbol) ((BLangUserDefinedType) member).symbol);
        }

        BEnumSymbol enumSymbol = new BEnumSymbol(enumMembers, Flags.asMask(typeDefinition.flagSet),
                names.fromIdNode(typeDefinition.name), names.fromIdNode(typeDefinition.name),
                env.enclPkg.symbol.pkgID, definedType, env.scope.owner,
                typeDefinition.pos, SOURCE);

        enumSymbol.name = names.fromIdNode(typeDefinition.name);
        enumSymbol.originalName = names.fromIdNode(typeDefinition.name);
        enumSymbol.flags |= Flags.asMask(typeDefinition.flagSet);

        enumSymbol.markdownDocumentation = getMarkdownDocAttachment(typeDefinition.markdownDocumentationAttachment);
        enumSymbol.pkgID = env.enclPkg.packageID;
        return enumSymbol;
    }

    private BObjectType getDistinctObjectType(BLangTypeDefinition typeDefinition, BObjectType definedType,
                                              BTypeSymbol typeDefSymbol) {
        BTypeSymbol tSymbol = typeDefSymbol.kind == SymbolKind.TYPE_DEF ? typeDefSymbol.type.tsymbol : typeDefSymbol;
        BObjectType definedObjType = definedType;
        // Create a new type for distinct type definition such as `type FooErr distinct BarErr;`
        // `typeDefSymbol` is different to `definedObjType.tsymbol` in a type definition statement that use
        // already defined type as the base type.
        if (definedObjType.tsymbol != tSymbol) {
            BObjectType objType = new BObjectType(tSymbol);
            tSymbol.type = objType;
            definedObjType = objType;
        }
        boolean isPublicType = typeDefinition.flagSet.contains(Flag.PUBLIC);
        definedObjType.typeIdSet = calculateTypeIdSet(typeDefinition, isPublicType, definedType.typeIdSet);
        return definedObjType;
    }

    private void defineTypeInMainScope(BTypeSymbol typeDefSymbol, BLangTypeDefinition typeDef, SymbolEnv env) {
        if (PackageID.isLangLibPackageID(env.enclPkg.packageID)) {
            typeDefSymbol.origin = BUILTIN;
            handleLangLibTypes(typeDef);
        } else {
            defineSymbol(typeDef.name.pos, typeDefSymbol, env);
        }
    }

    private BType defineSymbolForCyclicTypeDefinition(BLangTypeDefinition typeDef, SymbolEnv env) {
        Name newTypeDefName = names.fromIdNode(typeDef.name);
        BTypeSymbol typeDefSymbol;
        BType newTypeNode;

        switch (typeDef.typeNode.getKind()) {
            case TUPLE_TYPE_NODE:
                newTypeNode = new BTupleType(null, new ArrayList<>(), true);
                typeDefSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(typeDef.flagSet),
                        newTypeDefName, env.enclPkg.symbol.pkgID, newTypeNode, env.scope.owner,
                        typeDef.name.pos, SOURCE);
                break;
            default:
                newTypeNode = BUnionType.create(null, new LinkedHashSet<>(), true);
                typeDefSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(typeDef.flagSet),
                        newTypeDefName, env.enclPkg.symbol.pkgID, newTypeNode, env.scope.owner,
                        typeDef.name.pos, SOURCE);
        }
        typeDef.symbol = typeDefSymbol;
        defineTypeInMainScope(typeDefSymbol, typeDef, env);
        newTypeNode.tsymbol = typeDefSymbol;
        newTypeNode.flags |= typeDefSymbol.flags;
        return newTypeNode;
    }

    private BType getCyclicDefinedType(BLangTypeDefinition typeDef, SymbolEnv env) {
        // Get cyclic type reference from main scope
        BSymbol foundSym = symResolver.lookupSymbolInMainSpace(env, names.fromIdNode(typeDef.name));
        BType newTypeNode = foundSym.type;

        // Resolver only manages to resolve members as they are defined as user defined types.
        // Since we defined the symbols, the user defined types get resolved.
        // Since we are calling this API we don't have to call
        // `markParameterizedType(unionType, memberTypes);` again for resolved members
        BType resolvedTypeNodes = symResolver.resolveTypeNode(typeDef.typeNode, env);

        if (resolvedTypeNodes == symTable.noType) {
            return symTable.semanticError;
        }

        switch (resolvedTypeNodes.tag) {
            case TypeTags.TUPLE:
                BTupleType definedTupleType = (BTupleType) resolvedTypeNodes;
                for (BType member : definedTupleType.getTupleTypes()) {
                    if (!((BTupleType) newTypeNode).addMembers(member)) {
                        return constructDependencyListError(typeDef, member);
                    }
                }
                if (!((BTupleType) newTypeNode).addRestType(definedTupleType.restType)) {
                    return constructDependencyListError(typeDef, definedTupleType.restType);
                }
                break;
            default:
                BUnionType definedUnionType = (BUnionType) resolvedTypeNodes;
                for (BType member : definedUnionType.getMemberTypes()) {
                    ((BUnionType) newTypeNode).add(member);
                }
                break;
        }
        typeDef.typeNode.setBType(newTypeNode);
        typeDef.typeNode.getBType().tsymbol.type = newTypeNode;
        typeDef.symbol.type = newTypeNode;
        typeDef.setBType(newTypeNode);
        return newTypeNode;
    }

    private void defineAllUnresolvedCyclicTypesInScope(SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        for (BLangNode unresolvedNode : unresolvedTypes) {
            if (unresolvedNode.getKind() == NodeKind.TYPE_DEFINITION &&
                    ((BLangTypeDefinition) unresolvedNode).hasCyclicReference) {
                defineSymbolForCyclicTypeDefinition((BLangTypeDefinition) unresolvedNode, env);
            }
        }
        this.env = prevEnv;
    }

    private BType constructDependencyListError(BLangTypeDefinition typeDef, BType member) {
        List<String> dependencyList = new ArrayList<>();
        dependencyList.add(getTypeOrClassName(typeDef));
        dependencyList.add(member.tsymbol.name.value);
        dlog.error(typeDef.getPosition(), DiagnosticErrorCode.CYCLIC_TYPE_REFERENCE, dependencyList);
        return symTable.semanticError;
    }

    private BErrorType getDistinctErrorType(BLangTypeDefinition typeDefinition, BErrorType definedType,
                                            BSymbol typeDefSymbol) {
        BErrorType definedErrorType = definedType;
        // Create a new type for distinct type definition such as `type FooErr distinct BarErr;`
        // `typeDefSymbol` is different to `definedErrorType.tsymbol` in a type definition statement that use
        // already defined type as the base type.
        if (definedErrorType.tsymbol != typeDefSymbol) {
            BTypeSymbol typeSymbol = new BTypeSymbol(SymTag.TYPE_DEF, typeDefSymbol.flags, typeDefSymbol.name,
                    typeDefSymbol.pkgID, null, typeDefSymbol.owner, typeDefSymbol.pos, typeDefSymbol.origin);
            BErrorType bErrorType = new BErrorType(typeSymbol);
            typeSymbol.type = bErrorType;
            bErrorType.detailType = definedErrorType.detailType;
            typeDefSymbol.type = bErrorType;
            definedErrorType = bErrorType;
        }
        boolean isPublicType = typeDefinition.flagSet.contains(Flag.PUBLIC);
        definedErrorType.typeIdSet = calculateTypeIdSet(typeDefinition, isPublicType, definedType.typeIdSet);
        return definedErrorType;
    }

    private BTypeIdSet calculateTypeIdSet(BLangTypeDefinition typeDefinition, boolean isPublicType,
                                          BTypeIdSet secondary) {
        String name = typeDefinition.flagSet.contains(Flag.ANONYMOUS)
                ? anonymousModelHelper.getNextAnonymousTypeId(env.enclPkg.packageID)
                : typeDefinition.getName().value;

        return BTypeIdSet.from(env.enclPkg.packageID, name, isPublicType, secondary);
    }

    private boolean isDistinctFlagPresent(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.typeNode.flagSet.contains(Flag.DISTINCT)) {
            return true;
        }

        return false;
    }

    private void handleLangLibTypes(BLangTypeDefinition typeDefinition) {

        // As per spec 2020R3 built-in types are limited only within lang.* modules.
        for (BLangAnnotationAttachment attachment : typeDefinition.annAttachments) {
            if (attachment.annotationName.value.equals(Names.ANNOTATION_TYPE_PARAM.value)) {
                BSymbol typeDefSymbol = typeDefinition.symbol;
                typeDefSymbol.type = typeParamAnalyzer.createTypeParam(typeDefSymbol.type, typeDefSymbol.name);
                typeDefSymbol.flags |= Flags.TYPE_PARAM;
                break;
            } else if (attachment.annotationName.value.equals(Names.ANNOTATION_BUILTIN_SUBTYPE.value)) {
                // Type is pre-defined in symbol Table.
                BType type = symTable.getLangLibSubType(typeDefinition.name.value);
                typeDefinition.symbol.type = type;
                typeDefinition.symbol.flags |= type.tsymbol.flags;
                ((BTypeDefinitionSymbol) typeDefinition.symbol).referenceType.tsymbol.flags |= type.tsymbol.flags;
                ((BTypeDefinitionSymbol) typeDefinition.symbol).referenceType.referredType = type;
                typeDefinition.setBType(type);
                typeDefinition.typeNode.setBType(type);
                typeDefinition.isBuiltinTypeDef = true;
                break;
            }
            throw new IllegalStateException("Not supported annotation attachment at:" + attachment.pos);
        }
        defineSymbol(typeDefinition.name.pos, typeDefinition.symbol);
    }

    // If this type is defined to a public type or this is a anonymous type, return int with all bits set to 1,
    // so that we can bitwise and it with any flag and the original flag will not change.
    // If the type is not a public type then return a mask where public flag is set to zero and all others are set
    // to 1 so that we can perform bitwise and operation to remove the public flag from given flag.
    private long getPublicFlagResetingMask(Set<Flag> flagSet, BLangType typeNode) {
        boolean isAnonType =
                typeNode instanceof BLangStructureTypeNode && ((BLangStructureTypeNode) typeNode).isAnonymous;
        if (flagSet.contains(Flag.PUBLIC) || isAnonType) {
            return Long.MAX_VALUE;
        } else {
            return ~Flags.PUBLIC;
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        defineNode(serviceNode.serviceVariable, env);

        Name generatedServiceName = names.fromString("service$" + serviceNode.serviceClass.symbol.name.value);
        BType type = serviceNode.serviceClass.typeRefs.isEmpty() ? null : serviceNode.serviceClass.typeRefs.get(0)
                .getBType();
        BServiceSymbol serviceSymbol = new BServiceSymbol((BClassSymbol) serviceNode.serviceClass.symbol,
                                                          Flags.asMask(serviceNode.flagSet), generatedServiceName,
                                                          env.enclPkg.symbol.pkgID, type, env.enclPkg.symbol,
                                                          serviceNode.pos, SOURCE);
        serviceNode.symbol = serviceSymbol;

        if (!serviceNode.absoluteResourcePath.isEmpty()) {
            if ("/".equals(serviceNode.absoluteResourcePath.get(0).getValue())) {
                serviceSymbol.setAbsResourcePath(Collections.emptyList());
            } else {
                List<String> list = new ArrayList<>(serviceNode.absoluteResourcePath.size());
                for (IdentifierNode identifierNode : serviceNode.absoluteResourcePath) {
                    list.add(identifierNode.getValue());
                }
                serviceSymbol.setAbsResourcePath(list);
            }
        }

        if (serviceNode.serviceNameLiteral != null) {
            serviceSymbol.setAttachPointStringLiteral(serviceNode.serviceNameLiteral.value.toString());
        }

        env.scope.define(serviceSymbol.name, serviceSymbol);
    }

    @Override
    public void visit(BLangResourceFunction funcNode) {
        boolean validAttachedFunc = validateFuncReceiver(funcNode);

        if (PackageID.isLangLibPackageID(env.enclPkg.symbol.pkgID)) {
            funcNode.flagSet.add(Flag.LANG_LIB);
        }

        BInvokableSymbol funcSymbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                getFuncSymbolName(funcNode), getFuncSymbolOriginalName(funcNode),
                env.enclPkg.symbol.pkgID, null, env.scope.owner,
                funcNode.hasBody(), funcNode.name.pos, SOURCE);
        funcSymbol.source = funcNode.pos.lineRange().filePath();
        funcSymbol.markdownDocumentation = getMarkdownDocAttachment(funcNode.markdownDocumentationAttachment);
        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, env);
        defineInvokableSymbol(funcNode, funcSymbol, invokableEnv);
        funcNode.setBType(funcSymbol.type);

        if (isDeprecated(funcNode.annAttachments)) {
            funcSymbol.flags |= Flags.DEPRECATED;
        }
        // Define function receiver if any.
        if (funcNode.receiver != null) {
            defineAttachedFunctions(funcNode, funcSymbol, invokableEnv, validAttachedFunc);
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        boolean validAttachedFunc = validateFuncReceiver(funcNode);
        boolean remoteFlagSetOnNode = Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.REMOTE);

        if (!funcNode.attachedFunction && Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.PRIVATE)) {
            dlog.error(funcNode.pos, DiagnosticErrorCode.PRIVATE_FUNCTION_VISIBILITY, funcNode.name);
        }

        if (funcNode.receiver == null && !funcNode.attachedFunction && remoteFlagSetOnNode) {
            dlog.error(funcNode.pos, DiagnosticErrorCode.REMOTE_IN_NON_OBJECT_FUNCTION, funcNode.name.value);
        }

        if (PackageID.isLangLibPackageID(env.enclPkg.symbol.pkgID)) {
            funcNode.flagSet.add(Flag.LANG_LIB);
        }

        Location symbolPos = funcNode.flagSet.contains(Flag.LAMBDA) ?
                                                        symTable.builtinPos : funcNode.name.pos;
        BInvokableSymbol funcSymbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                                                                   getFuncSymbolName(funcNode),
                                                                   getFuncSymbolOriginalName(funcNode),
                                                                   env.enclPkg.symbol.pkgID, null, env.scope.owner,
                                                                   funcNode.hasBody(), symbolPos,
                                                                   getOrigin(funcNode.name.value));
        funcSymbol.source = funcNode.pos.lineRange().filePath();
        funcSymbol.markdownDocumentation = getMarkdownDocAttachment(funcNode.markdownDocumentationAttachment);
        SymbolEnv invokableEnv;
        NodeKind previousNodeKind = env.node.getKind();
        if (previousNodeKind == NodeKind.CLASS_DEFN) {
            invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope,
                    fieldsRemovedEnv(env, ((BLangClassDefinition) env.node).fields));
        } else if (previousNodeKind == NodeKind.OBJECT_TYPE) {
            invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope,
                    fieldsRemovedEnv(env, ((BLangObjectTypeNode) env.node).fields));
        } else {
            invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, env);
        }
        defineInvokableSymbol(funcNode, funcSymbol, invokableEnv);
        funcNode.setBType(funcSymbol.type);

        // Reset origin if it's the generated function node for a lambda
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.LAMBDA)) {
            funcSymbol.origin = VIRTUAL;
        }

        if (isDeprecated(funcNode.annAttachments)) {
            funcSymbol.flags |= Flags.DEPRECATED;
        }
        // Define function receiver if any.
        if (funcNode.receiver != null) {
            defineAttachedFunctions(funcNode, funcSymbol, invokableEnv, validAttachedFunc);
        }
    }

    private SymbolEnv fieldsRemovedEnv(SymbolEnv currentEnv, List<BLangSimpleVariable> fields) {
        if (fields.isEmpty()) {
            return currentEnv;
        }
        Scope currentScope = currentEnv.scope;
        Scope newScope = new Scope(currentScope.owner);
        newScope.entries.putAll(currentScope.entries);
        Map<Name, ScopeEntry> entries = newScope.entries;
        for (BLangSimpleVariable field : fields) {
            entries.remove(Names.fromString(field.name.value));
        }
        SymbolEnv newEnv = new SymbolEnv(currentEnv.node, newScope);
        currentEnv.copyTo(newEnv, currentEnv.enclEnv);
        return newEnv;
    }

    private boolean isDeprecated(List<BLangAnnotationAttachment> annAttachments) {
        for (BLangAnnotationAttachment annotationAttachment : annAttachments) {
            if (annotationAttachment.annotationName.getValue().equals(DEPRECATION_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(BLangConstant constant) {
        BType staticType;
        if (constant.typeNode != null) {
            staticType = symResolver.resolveTypeNode(constant.typeNode, env);
            if (staticType == symTable.noType) {
                constant.symbol = getConstantSymbol(constant);
                // This is to prevent concurrent modification exception.
                if (!this.unresolvedTypes.contains(constant)) {
                    this.unresolvedTypes.add(constant);
                }
                return;
            }
        } else {
            staticType = symTable.semanticError;
        }
        BConstantSymbol constantSymbol = getConstantSymbol(constant);
        constant.symbol = constantSymbol;

        NodeKind nodeKind = constant.expr.getKind();
        if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
            if (constant.typeNode != null) {
                BType referredType = Types.getReferredType(staticType);
                if (types.isValidLiteral((BLangLiteral) constant.expr, referredType)) {
                    // A literal type constant is defined with correct type.
                    // Update the type of the finiteType node to the static type.
                    // This is done to make the type inferring work.
                    // eg: const decimal d = 5.0;
                    BLangFiniteTypeNode finiteType = (BLangFiniteTypeNode) constant.associatedTypeDefinition.typeNode;
                    BLangExpression valueSpaceExpr = finiteType.valueSpace.iterator().next();
                    valueSpaceExpr.setBType(referredType);
                    defineNode(constant.associatedTypeDefinition, env);

                    constantSymbol.type = constant.associatedTypeDefinition.symbol.type;
                    constantSymbol.literalType = referredType;
                } else {
                    // A literal type constant is defined with some incorrect type. Set the original
                    // types and continue the flow and let it fail at semantic analyzer.
                    defineNode(constant.associatedTypeDefinition, env);
                    constantSymbol.type = staticType;
                    constantSymbol.literalType = constant.expr.getBType();
                }
            } else {
                // A literal type constant is defined without the type.
                // Then the type of the symbol is the finite type.
                defineNode(constant.associatedTypeDefinition, env);
                constantSymbol.type = constant.associatedTypeDefinition.symbol.type;
                constantSymbol.literalType = constant.expr.getBType();
            }
            if (constantSymbol.type.tag != TypeTags.TYPEREFDESC) {
                constantSymbol.type.tsymbol.flags |= constant.associatedTypeDefinition.symbol.flags;
            }

        } else if (nodeKind == NodeKind.UNARY_EXPR && constant.typeNode == null &&
                types.isLiteralInUnaryAllowed((BLangUnaryExpr) constant.expr)) {
            // When unary type constants with `-` or `+` operators are defined without the type,
            // then the type of the symbol will be handled similar to handling a literal type constant
            // defined without the type.

            BLangUnaryExpr unaryConstant = (BLangUnaryExpr) constant.expr;
            // Replace unary expression in AssociatedTypeDefinition of constant with numeric literal
            BLangNumericLiteral literal = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
            Types.setValueOfNumericLiteral(literal, unaryConstant);
            literal.isConstant = true;
            literal.setBType(unaryConstant.expr.getBType());
            ((BLangFiniteTypeNode) constant.getAssociatedTypeDefinition().getTypeNode()).valueSpace.set(0, literal);

            defineNode(constant.associatedTypeDefinition, env);
            constantSymbol.type = constant.associatedTypeDefinition.symbol.type;
            constantSymbol.literalType = unaryConstant.expr.getBType();
        } else if (constant.typeNode != null) {
            constantSymbol.type = constantSymbol.literalType = staticType;
        }
        constantSymbol.markdownDocumentation = getMarkdownDocAttachment(constant.markdownDocumentationAttachment);
        if (isDeprecated(constant.annAttachments)) {
            constantSymbol.flags |= Flags.DEPRECATED;
        }
        // Add the symbol to the enclosing scope.
        if (!symResolver.checkForUniqueSymbol(constant.name.pos, env, constantSymbol)) {
            return;
        }

        if (constant.symbol.name == Names.IGNORE) {
            // Avoid symbol definition for constants with name '_'
            return;
        }
        // Add the symbol to the enclosing scope.
        env.scope.define(constantSymbol.name, constantSymbol);
    }

    private BConstantSymbol getConstantSymbol(BLangConstant constant) {
        // Create a new constant symbol.
        Name name = names.fromIdNode(constant.name);
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        return new BConstantSymbol(Flags.asMask(constant.flagSet), name, names.originalNameFromIdNode(constant.name),
                                   pkgID, symTable.semanticError, symTable.noType, env.scope.owner,
                                   constant.name.pos, getOrigin(name));
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        // assign the type to var type node
        if (varNode.getBType() == null) {
            if (varNode.typeNode != null) {
                varNode.setBType(symResolver.resolveTypeNode(varNode.typeNode, env));
            } else {
                varNode.setBType(symTable.noType);
            }
        }

        Name varName = names.fromIdNode(varNode.name);
        Name varOrigName = names.originalNameFromIdNode(varNode.name);
        if (varName == Names.IGNORE || varNode.symbol != null) {
            return;
        }

        BVarSymbol varSymbol = defineVarSymbol(varNode.name.pos, varNode.flagSet, varNode.getBType(), varName,
                                               varOrigName, env, varNode.internal);
        if (isDeprecated(varNode.annAttachments)) {
            varSymbol.flags |= Flags.DEPRECATED;
        }

        // Skip setting the state if there's a diagnostic already (e.g., redeclared symbol)
        if (varSymbol.type == symTable.semanticError && varSymbol.state == DiagnosticState.VALID) {
            varSymbol.state = DiagnosticState.UNKNOWN_TYPE;
        }

        varSymbol.markdownDocumentation = getMarkdownDocAttachment(varNode.markdownDocumentationAttachment);
        varNode.symbol = varSymbol;
        if (varNode.symbol.type.tsymbol != null && Symbols.isFlagOn(varNode.symbol.type.tsymbol.flags, Flags.CLIENT)) {
            varSymbol.tag = SymTag.ENDPOINT;
        }

        if (Types.getReferredType(varSymbol.type).tag == TypeTags.FUTURE
                && ((BFutureType) Types.getReferredType(varSymbol.type)).workerDerivative) {
            Iterator<BLangLambdaFunction> lambdaFunctions = env.enclPkg.lambdaFunctions.iterator();
            while (lambdaFunctions.hasNext()) {
                BLangLambdaFunction lambdaFunction = lambdaFunctions.next();
                // let's inject future symbol to all the lambdas
                // last lambda needs to be skipped to avoid self reference
                // lambda's form others functions also need to be skiped
                BLangInvokableNode enclInvokable = lambdaFunction.capturedClosureEnv.enclInvokable;
                if (lambdaFunctions.hasNext() && enclInvokable != null && varSymbol.owner == enclInvokable.symbol) {
                    lambdaFunction.capturedClosureEnv.scope.define(varSymbol.name, varSymbol);
                }
            }
        }

        if (Types.getReferredType(varSymbol.type).tag == TypeTags.INVOKABLE) {
            BInvokableSymbol symbol = (BInvokableSymbol) varSymbol;
            BTypeSymbol typeSymbol = Types.getReferredType(varSymbol.type).tsymbol;
            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) typeSymbol;
            symbol.params = tsymbol.params == null ? null : new ArrayList<>(tsymbol.params);
            symbol.restParam = tsymbol.restParam;
            symbol.retType = tsymbol.returnType;
        }

        if ((env.scope.owner.tag & SymTag.RECORD) != SymTag.RECORD && !varNode.flagSet.contains(Flag.NEVER_ALLOWED) &&
                types.isNeverTypeOrStructureTypeWithARequiredNeverMember(varSymbol.type)) {
            // check if the variable is defined as a 'never' type or equivalent to 'never'
            // (except inside a record type or iterative use (followed by in) in typed binding pattern)
            // if so, log an error
            if (varNode.flagSet.contains(Flag.REQUIRED_PARAM) || varNode.flagSet.contains(Flag.DEFAULTABLE_PARAM)) {
                dlog.error(varNode.pos, DiagnosticErrorCode.NEVER_TYPE_NOT_ALLOWED_FOR_REQUIRED_DEFAULTABLE_PARAMS);
            } else {
                if ((env.scope.owner.tag & SymTag.OBJECT) == SymTag.OBJECT) {
                    dlog.error(varNode.pos, DiagnosticErrorCode.NEVER_TYPED_OBJECT_FIELD_NOT_ALLOWED);
                } else {
                    dlog.error(varNode.pos, DiagnosticErrorCode.NEVER_TYPED_VAR_DEF_NOT_ALLOWED);
                }
            }
        }
    }

    @Override
    public void visit(BLangTupleVariable varNode) {
        if (varNode.isDeclaredWithVar) {
            varNode.symbol =
                    defineVarSymbol(varNode.pos, varNode.flagSet, symTable.noType,
                                    names.fromString(anonymousModelHelper.getNextTupleVarKey(env.enclPkg.packageID)),
                                    env, true);
            // Symbol enter with type other
            List<BLangVariable> memberVariables = new ArrayList<>(varNode.memberVariables);
            if (varNode.restVariable != null) {
                memberVariables.add(varNode.restVariable);
            }
            for (int i = 0; i < memberVariables.size(); i++) {
                BLangVariable memberVar = memberVariables.get(i);
                memberVar.isDeclaredWithVar = true;
                defineNode(memberVar, env);
            }
            return;
        }
        if (varNode.getBType() == null) {
            varNode.setBType(symResolver.resolveTypeNode(varNode.typeNode, env));
        }
        // To support variable forward referencing we need to symbol enter each tuple member with type at SymbolEnter.
        if (!(checkTypeAndVarCountConsistency(varNode, env))) {
            varNode.setBType(symTable.semanticError);
            return;
        }
    }

    boolean checkTypeAndVarCountConsistency(BLangTupleVariable var, SymbolEnv env) {
        if (var.symbol == null) {
            Name varName = names.fromString(anonymousModelHelper.getNextTupleVarKey(env.enclPkg.packageID));
            var.symbol = defineVarSymbol(var.pos, var.flagSet, var.getBType(), varName, env, true);
        }
        
        return checkTypeAndVarCountConsistency(var, null, env);
    }

    boolean checkTypeAndVarCountConsistency(BLangTupleVariable varNode, BTupleType tupleTypeNode,
                                                    SymbolEnv env) {
        if (tupleTypeNode == null) {
        /*
          This switch block will resolve the tuple type of the tuple variable.
          For example consider the following - [int, string]|[boolean, float] [a, b] = foo();
          Since the varNode type is a union, the types of 'a' and 'b' will be resolved as follows:
          Type of 'a' will be (int | boolean) while the type of 'b' will be (string | float).
          Consider anydata (a, b) = foo();
          Here, the type of 'a'and type of 'b' will be both anydata.
         */
            BType bType = varNode.getBType();
            BType referredType = Types.getReferredType(bType);
            switch (referredType.tag) {
                case TypeTags.UNION:
                    Set<BType> unionType = types.expandAndGetMemberTypesRecursive(referredType);
                    List<BType> possibleTypes = new ArrayList<>();
                    for (BType type : unionType) {
                        if (!(TypeTags.TUPLE == type.tag &&
                                checkMemVarCountMatchWithMemTypeCount(varNode, (BTupleType) type)) &&
                        TypeTags.ANY != type.tag && TypeTags.ANYDATA != type.tag &&
                                (TypeTags.ARRAY != type.tag || ((BArrayType) type).state == BArrayState.OPEN)) {
                            continue;
                        }
                        possibleTypes.add(type);
                    }
                    if (possibleTypes.isEmpty()) {
                        // handle var count mismatch in foreach declared with `var`
                        if (varNode.isDeclaredWithVar) {
                            dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_LIST_BINDING_PATTERN);
                            return false;
                        }
                        dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_LIST_BINDING_PATTERN_DECL, bType);
                        return false;
                    }

                    if (possibleTypes.size() > 1) {
                        List<BType> memberTupleTypes = new ArrayList<>();
                        for (int i = 0; i < varNode.memberVariables.size(); i++) {
                            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
                            for (BType possibleType : possibleTypes) {
                                if (possibleType.tag == TypeTags.TUPLE) {
                                    memberTypes.add(((BTupleType) possibleType).tupleTypes.get(i));
                                } else if (possibleType.tag == TypeTags.ARRAY) {
                                    memberTypes.add(((BArrayType) possibleType).eType);
                                } else {
                                    memberTupleTypes.add(referredType);
                                }
                            }

                            if (memberTypes.size() > 1) {
                                memberTupleTypes.add(BUnionType.create(null, memberTypes));
                            } else {
                                memberTupleTypes.addAll(memberTypes);
                            }
                        }
                        tupleTypeNode = new BTupleType(memberTupleTypes);
                        tupleTypeNode.restType = getPossibleRestTypeForUnion(varNode, possibleTypes);
                        break;
                    }

                    if (possibleTypes.get(0).tag == TypeTags.TUPLE) {
                        tupleTypeNode = (BTupleType) possibleTypes.get(0);
                        tupleTypeNode.restType = getPossibleRestTypeForUnion(varNode, possibleTypes);
                        break;
                    }

                    List<BType> memberTypes = new ArrayList<>();
                    for (int i = 0; i < varNode.memberVariables.size(); i++) {
                        memberTypes.add(possibleTypes.get(0));
                    }
                    tupleTypeNode = new BTupleType(memberTypes);
                    tupleTypeNode.restType = getPossibleRestTypeForUnion(varNode, possibleTypes);
                    break;
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                    List<BType> memberTupleTypes = new ArrayList<>();
                    for (int i = 0; i < varNode.memberVariables.size(); i++) {
                        memberTupleTypes.add(referredType);
                    }
                    tupleTypeNode = new BTupleType(memberTupleTypes);
                    if (varNode.restVariable != null) {
                        tupleTypeNode.restType = referredType;
                    }
                    break;
                case TypeTags.TUPLE:
                    tupleTypeNode = (BTupleType) referredType;
                    break;
                case TypeTags.ARRAY:
                    List<BType> tupleTypes = new ArrayList<>();
                    BArrayType arrayType = (BArrayType) referredType;
                    tupleTypeNode = new BTupleType(tupleTypes);
                    BType eType = arrayType.eType;
                    for (int i = 0; i < arrayType.size; i++) {
                        tupleTypes.add(eType);
                    }
                    if (varNode.restVariable != null) {
                        tupleTypeNode.restType = eType;
                    }
                    break;
                default:
                    dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_LIST_BINDING_PATTERN_DECL, bType);
                    return false;
            }
        }

        if (!checkMemVarCountMatchWithMemTypeCount(varNode, tupleTypeNode)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_LIST_BINDING_PATTERN);
            return false;
        }

        int ignoredCount = 0;
        int i = 0;
        BType type;
        for (BLangVariable var : varNode.memberVariables) {
            type = tupleTypeNode.tupleTypes.get(i);
            i++;
            if (var.getKind() == NodeKind.VARIABLE) {
                // '_' is allowed in tuple variables. Not allowed if all variables are named as '_'
                BLangSimpleVariable simpleVar = (BLangSimpleVariable) var;
                Name varName = names.fromIdNode(simpleVar.name);
                if (varName == Names.IGNORE) {
                    ignoredCount++;
                    simpleVar.setBType(symTable.anyType);
                    if (!types.isAssignable(type, symTable.anyType)) {
                        dlog.error(varNode.pos, DiagnosticErrorCode.WILD_CARD_BINDING_PATTERN_ONLY_SUPPORTS_TYPE_ANY);
                    }
                    continue;
                }
            }
            defineMemberNode(var, env, type);
        }

        if (varNode.restVariable != null) {
            int tupleNodeMemCount = tupleTypeNode.tupleTypes.size();
            int varNodeMemCount = varNode.memberVariables.size();
            BType restType = tupleTypeNode.restType;
            List<BType> memberTypes = new ArrayList<>();
            if (varNodeMemCount < tupleNodeMemCount) {
                for (int j = varNodeMemCount; j < tupleNodeMemCount; j++) {
                    memberTypes.add(tupleTypeNode.tupleTypes.get(j));
                }
            }
            if (!memberTypes.isEmpty()) {
                BTupleType restTupleType = new BTupleType(memberTypes);
                restTupleType.restType = restType;
                type = restTupleType;
            } else {
                type = restType != null ? new BArrayType(restType) : null;
            }
            defineMemberNode(varNode.restVariable, env, type);
        }

        if (!varNode.memberVariables.isEmpty() && ignoredCount == varNode.memberVariables.size()
                && varNode.restVariable == null) {
            dlog.error(varNode.pos, DiagnosticErrorCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
            return false;
        }
        return true;
    }

    private BType getPossibleRestTypeForUnion(BLangTupleVariable varNode, List<BType> possibleTypes) {
        if (varNode.restVariable == null) {
            return null;
        }
        LinkedHashSet<BType> memberRestTypes = new LinkedHashSet<>();
        for (BType possibleType : possibleTypes) {
            if (possibleType.tag == TypeTags.TUPLE) {
                BTupleType tupleType = (BTupleType) possibleType;
                for (int j = varNode.memberVariables.size(); j < tupleType.tupleTypes.size();
                     j++) {
                    memberRestTypes.add(tupleType.tupleTypes.get(j));
                }
                if (tupleType.restType != null) {
                    memberRestTypes.add(tupleType.restType);
                }
            } else if (possibleType.tag == TypeTags.ARRAY) {
                memberRestTypes.add(((BArrayType) possibleType).eType);
            } else {
                memberRestTypes.add(possibleType);
            }
        }
        if (!memberRestTypes.isEmpty()) {
            return memberRestTypes.size() > 1 ? BUnionType.create(null, memberRestTypes) :
                    memberRestTypes.iterator().next();
        } else {
            return varNode.getBType();
        }
    }

    private boolean checkMemVarCountMatchWithMemTypeCount(BLangTupleVariable varNode, BTupleType tupleTypeNode) {
        int memberVarsSize = varNode.memberVariables.size();
        BLangVariable restVariable = varNode.restVariable;
        int tupleTypesSize = tupleTypeNode.tupleTypes.size();
        if (memberVarsSize > tupleTypesSize) {
            return false;
        }
        return restVariable != null ||
                (tupleTypesSize == memberVarsSize && tupleTypeNode.restType == null);
    }

    @Override
    public void visit(BLangRecordVariable recordVar) {
        if (recordVar.isDeclaredWithVar) {
            recordVar.symbol =
                    defineVarSymbol(recordVar.pos, recordVar.flagSet, symTable.noType,
                                    names.fromString(anonymousModelHelper.getNextRecordVarKey(env.enclPkg.packageID)),
                                    env, true);
            // Symbol enter each member with type other.
            for (BLangRecordVariable.BLangRecordVariableKeyValue variable : recordVar.variableList) {
                BLangVariable value = variable.getValue();
                value.isDeclaredWithVar = true;
                defineNode(value, env);
            }

            BLangSimpleVariable restParam = (BLangSimpleVariable) recordVar.restParam;
            if (restParam != null) {
                restParam.isDeclaredWithVar = true;
                defineNode(restParam, env);
            }
            return;
        }

        if (recordVar.getBType() == null) {
            recordVar.setBType(symResolver.resolveTypeNode(recordVar.typeNode, env));
        }
        // To support variable forward referencing we need to symbol enter each record member with type at SymbolEnter.
        if (!(symbolEnterAndValidateRecordVariable(recordVar, env))) {
            recordVar.setBType(symTable.semanticError);
            return;
        }
    }

    boolean symbolEnterAndValidateRecordVariable(BLangRecordVariable var, SymbolEnv env) {
        if (var.symbol == null) {
            Name varName = names.fromString(anonymousModelHelper.getNextRecordVarKey(env.enclPkg.packageID));
            var.symbol = defineVarSymbol(var.pos, var.flagSet, var.getBType(), varName, env, true);
        }

        return validateRecordVariable(var, env);
    }

    boolean validateRecordVariable(BLangRecordVariable recordVar, SymbolEnv env) {
        BType recordType = Types.getReferredType(recordVar.getBType());
        BRecordType recordVarType;
        /*
          This switch block will resolve the record type of the record variable.
          For example consider the following -
          type Foo record {int a, boolean b};
          type Bar record {string a, float b};
          Foo|Bar {a, b} = foo();
          Since the varNode type is a union, the types of 'a' and 'b' will be resolved as follows:
          Type of 'a' will be a union of the types of field 'a' in both Foo and Bar.
          i.e. type of 'a' is (int | string) and type of 'b' is (boolean | float).
          Consider anydata {a, b} = foo();
          Here, the type of 'a'and type of 'b' will be both anydata.
         */
        switch (recordType.tag) {
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) recordType;
                Set<BType> bTypes = types.expandAndGetMemberTypesRecursive(unionType);
                List<BType> possibleTypes = bTypes.stream()
                        .filter(rec -> doesRecordContainKeys(rec, recordVar.variableList, recordVar.restParam != null))
                        .collect(Collectors.toList());

                if (possibleTypes.isEmpty()) {
                    dlog.error(recordVar.pos, DiagnosticErrorCode.INVALID_RECORD_BINDING_PATTERN, recordType);
                    return false;
                }

                if (possibleTypes.size() > 1) {
                    recordVarType = populatePossibleFields(recordVar, possibleTypes, env);
                    break;
                }

                if (possibleTypes.get(0).tag == TypeTags.RECORD) {
                    recordVarType = (BRecordType) possibleTypes.get(0);
                    break;
                }

                if (possibleTypes.get(0).tag == TypeTags.MAP) {
                    recordVarType = createSameTypedFieldsRecordType(recordVar,
                            ((BMapType) possibleTypes.get(0)).constraint, env);
                    break;
                }

                recordVarType = createSameTypedFieldsRecordType(recordVar, possibleTypes.get(0), env);
                break;
            case TypeTags.RECORD:
                recordVarType = (BRecordType) recordType;
                break;
            case TypeTags.MAP:
                recordVarType = createSameTypedFieldsRecordType(recordVar,
                                                                ((BMapType) recordType).constraint, env);
                break;
            default:
                dlog.error(recordVar.pos, DiagnosticErrorCode.INVALID_RECORD_BINDING_PATTERN, recordType);
                return false;
        }

        return defineVariableList(recordVar, recordVarType, env);
    }

    private BRecordType populatePossibleFields(BLangRecordVariable recordVar, List<BType> possibleTypes,
                                               SymbolEnv env) {
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.ANONYMOUS,
                names.fromString(ANONYMOUS_RECORD_NAME),
                env.enclPkg.symbol.pkgID, null,
                env.scope.owner, recordVar.pos, SOURCE);
        BRecordType recordVarType = (BRecordType) symTable.recordType;

        List<String> mappedFields = recordVar.variableList.stream().map(varKeyValue -> varKeyValue.getKey().value)
                .collect(Collectors.toList());
        LinkedHashMap<String, BField> fields = populateAndGetPossibleFieldsForRecVar(recordVar.pos, possibleTypes,
                mappedFields, recordSymbol, env);

        if (recordVar.restParam != null) {
            recordVarType.restFieldType = createRestFieldFromPossibleTypes(recordVar.pos, env, possibleTypes,
                    fields, recordSymbol);
        }
        recordVarType.tsymbol = recordSymbol;
        recordVarType.fields = fields;
        recordSymbol.type = recordVarType;
        return recordVarType;
    }

    private BType createRestFieldFromPossibleTypes(Location pos, SymbolEnv env, List<BType> possibleTypes,
                                                   LinkedHashMap<String, BField> boundedFields, BSymbol recordSymbol) {
        LinkedHashSet<BType> restFieldMemberTypes = new LinkedHashSet<>();
        List<LinkedHashMap<String, BField>> possibleRecordFieldMapList = new ArrayList<>();

        for (BType possibleType : possibleTypes) {
            if (possibleType.tag == TypeTags.RECORD) {
                BRecordType recordType = (BRecordType) possibleType;
                possibleRecordFieldMapList.add(recordType.fields);
                restFieldMemberTypes.add(recordType.restFieldType);
            } else if (possibleType.tag == TypeTags.MAP) {
                restFieldMemberTypes.add(((BMapType) possibleType).constraint);
            } else {
                restFieldMemberTypes.add(possibleType);
            }
        }

        BType restFieldType = restFieldMemberTypes.size() > 1 ?
                BUnionType.create(null, restFieldMemberTypes) :
                restFieldMemberTypes.iterator().next();

        if (!possibleRecordFieldMapList.isEmpty()) {
            List<String> intersectionFields = getIntersectionFields(possibleRecordFieldMapList);
            LinkedHashMap<String, BField> unmappedMembers = populateAndGetPossibleFieldsForRecVar(pos,
                    possibleTypes, intersectionFields, recordSymbol, env);

            LinkedHashMap<String, BField> optionalFields = new LinkedHashMap<>() {{
                possibleRecordFieldMapList.forEach(map -> putAll(map));
            }};

            intersectionFields.forEach(optionalFields::remove);
            boundedFields.keySet().forEach(unmappedMembers::remove);

            for (BField field : optionalFields.values()) {
                field.symbol.flags = setSymbolAsOptional(field.symbol.flags);
            }
            unmappedMembers.putAll(optionalFields);

            BRecordType restRecord = new BRecordType(null);
            restRecord.fields = unmappedMembers;
            restRecord.restFieldType = restFieldType;
            restFieldType = restRecord;
        }

        return restFieldType;
    }

    private List<String> getIntersectionFields(List<LinkedHashMap<String, BField>> fieldList) {
        LinkedHashMap<String, BField> intersectionMap = fieldList.get(0);
        HashSet<String> intersectionSet = new HashSet<>(intersectionMap.keySet());

        for (int i = 1; i < fieldList.size(); i++) {
            LinkedHashMap<String, BField> map = fieldList.get(i);
            HashSet<String> set = new HashSet<>(map.keySet());
            intersectionSet.retainAll(set);
        }

        return new ArrayList<>(intersectionSet);
    }

    /**
     * This method will resolve field types based on a list of possible types.
     * When a record variable has multiple possible assignable types, each field will be a union of the relevant
     * possible types field type.
     *
     * @param pos line number information of the source file
     * @param possibleTypes list of possible types
     * @param fieldNames fields types to be resolved
     * @param recordSymbol symbol of the record type to be used in creating fields
     * @param env environment to define the symbol
     * @return the list of fields
     */
    private LinkedHashMap<String, BField> populateAndGetPossibleFieldsForRecVar(Location pos, List<BType> possibleTypes,
                                                                                List<String> fieldNames,
                                                                                BSymbol recordSymbol, SymbolEnv env) {
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        for (String fieldName : fieldNames) {
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
            for (BType possibleType : possibleTypes) {
                if (possibleType.tag == TypeTags.RECORD) {
                    BRecordType possibleRecordType = (BRecordType) possibleType;

                    if (possibleRecordType.fields.containsKey(fieldName)) {
                        BField field = possibleRecordType.fields.get(fieldName);
                        if (Symbols.isOptional(field.symbol)) {
                            memberTypes.add(symTable.nilType);
                        }
                        memberTypes.add(field.type);
                    } else {
                        memberTypes.add(possibleRecordType.restFieldType);
                        memberTypes.add(symTable.nilType);
                    }

                    continue;
                }

                if (possibleType.tag == TypeTags.MAP) {
                    BMapType possibleMapType = (BMapType) possibleType;
                    memberTypes.add(possibleMapType.constraint);
                    continue;
                }
                memberTypes.add(possibleType); // possible type is any or anydata}
            }

            BType fieldType = memberTypes.size() > 1 ?
                    BUnionType.create(null, memberTypes) : memberTypes.iterator().next();
            BField field = new BField(names.fromString(fieldName), pos,
                    new BVarSymbol(0, names.fromString(fieldName), env.enclPkg.symbol.pkgID,
                            fieldType, recordSymbol, pos, SOURCE));
            fields.put(field.name.value, field);
        }
        return fields;
    }

    private BRecordType createSameTypedFieldsRecordType(BLangRecordVariable recordVar, BType fieldTypes,
                                                        SymbolEnv env) {
        BType fieldType;
        if (fieldTypes.isNullable()) {
            fieldType = fieldTypes;
        } else {
            fieldType = BUnionType.create(null, fieldTypes, symTable.nilType);
        }

        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.ANONYMOUS,
                names.fromString(ANONYMOUS_RECORD_NAME),
                env.enclPkg.symbol.pkgID, null, env.scope.owner,
                recordVar.pos, SOURCE);
        //TODO check below field position
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        for (BLangRecordVariable.BLangRecordVariableKeyValue bLangRecordVariableKeyValue : recordVar.variableList) {
            Name fieldName = names.fromIdNode(bLangRecordVariableKeyValue.key);
            BField bField = new BField(fieldName, recordVar.pos,
                    new BVarSymbol(0, fieldName, names.originalNameFromIdNode(bLangRecordVariableKeyValue.key),
                                   env.enclPkg.symbol.pkgID, fieldType, recordSymbol, recordVar.pos, SOURCE));
            fields.put(fieldName.getValue(), bField);
        }

        BRecordType recordVarType = (BRecordType) symTable.recordType;
        recordVarType.fields = fields;
        recordSymbol.type = recordVarType;
        recordVarType.tsymbol = recordSymbol;

        // Since this is for record variables, we consider its record type as an open record type.
        recordVarType.sealed = false;
        recordVarType.restFieldType = fieldTypes; // TODO: 7/26/19 Check if this should be `fieldType`

        return recordVarType;
    }

    private boolean defineVariableList(BLangRecordVariable recordVar, BRecordType recordVarType, SymbolEnv env) {
        LinkedHashMap<String, BField> recordVarTypeFields = recordVarType.fields;

        boolean validRecord = true;
        int ignoredCount = 0;
        for (BLangRecordVariable.BLangRecordVariableKeyValue variable : recordVar.variableList) {
            // Infer the type of each variable in recordVariable from the given record type
            // so that symbol enter is done recursively
            BLangVariable value = variable.getValue();
            String key = variable.key.value;
            if (value.getKind() == NodeKind.VARIABLE) {
                // '_' is allowed in record variables. Not allowed if all variables are named as '_'
                BLangSimpleVariable simpleVar = (BLangSimpleVariable) value;
                Name varName = names.fromIdNode(simpleVar.name);
                if (varName == Names.IGNORE) {
                    ignoredCount++;
                    simpleVar.setBType(symTable.anyType);
                    if (!recordVarTypeFields.containsKey(key)) {
                        continue;
                    }
                    if (!types.isAssignable(recordVarTypeFields.get(key).type,
                            symTable.anyType)) {
                        dlog.error(variable.valueBindingPattern.pos,
                                DiagnosticErrorCode.WILD_CARD_BINDING_PATTERN_ONLY_SUPPORTS_TYPE_ANY);
                    }
                    continue;
                }
            }

            if (recordVar.getBType().tag == TypeTags.MAP) {
                validRecord = false;
                dlog.error(variable.key.pos, DiagnosticErrorCode.INVALID_FIELD_BINDING_PATTERN_WITH_NON_REQUIRED_FIELD);
            }

            BField field = recordVarTypeFields.get(key);
            if (field == null) {
                validRecord = false;
                if (recordVarType.sealed) {
                    dlog.error(recordVar.pos, DiagnosticErrorCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN,
                               key, recordVar.getBType());
                } else {
                    dlog.error(variable.key.pos,
                            DiagnosticErrorCode.INVALID_FIELD_BINDING_PATTERN_WITH_NON_REQUIRED_FIELD);
                }
            } else {
                BType fieldType;
                if (Symbols.isOptional(field.symbol)) {
                    fieldType = types.addNilForNillableAccessType(field.type);
                } else {
                    fieldType = field.type;
                }
                defineMemberNode(value, env, fieldType);
            }
        }

        if (!recordVar.variableList.isEmpty() && ignoredCount == recordVar.variableList.size()
                && recordVar.restParam == null) {
            dlog.error(recordVar.pos, DiagnosticErrorCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
            return false;
        }

        if (recordVar.restParam != null) {
            BType restType = getRestParamType(recordVarType);
            List<String> varList = recordVar.variableList.stream().map(t -> t.getKey().value)
                    .collect(Collectors.toList());
            BRecordType restConstraint = createRecordTypeForRestField(recordVar.restParam.getPosition(), env,
                    recordVarType, varList, restType);
            defineMemberNode(recordVar.restParam, env, restConstraint);
        }

        return validRecord;
    }

    private boolean doesRecordContainKeys(BType varType,
                                          List<BLangRecordVariable.BLangRecordVariableKeyValue> variableList,
                                          boolean hasRestParam) {
        if (varType.tag == TypeTags.MAP || varType.tag == TypeTags.ANY || varType.tag == TypeTags.ANYDATA) {
            return true;
        }
        if (varType.tag != TypeTags.RECORD) {
            return false;
        }
        BRecordType recordVarType = (BRecordType) varType;
        Map<String, BField> recordVarTypeFields = recordVarType.fields;

        for (BLangRecordVariable.BLangRecordVariableKeyValue var : variableList) {
            if (!recordVarTypeFields.containsKey(var.key.value) && recordVarType.sealed) {
                return false;
            }
        }

        if (!hasRestParam) {
            return true;
        }

        return !recordVarType.sealed;
    }

    public BRecordTypeSymbol createAnonRecordSymbol(SymbolEnv env, Location pos) {
        EnumSet<Flag> flags = EnumSet.of(Flag.PUBLIC, Flag.ANONYMOUS);
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(flags), Names.EMPTY,
                env.enclPkg.packageID, null, env.scope.owner, pos, VIRTUAL);
        recordSymbol.name = names.fromString(anonymousModelHelper.getNextAnonymousTypeKey(env.enclPkg.packageID));
        recordSymbol.scope = new Scope(recordSymbol);
        return recordSymbol;
    }

    BType getRestParamType(BRecordType recordType)  {
        BType memberType;
        if (recordType.restFieldType != null) {
            memberType = recordType.restFieldType;
        } else if (hasErrorTypedField(recordType)) {
            memberType = hasOnlyPureTypedFields(recordType) ? symTable.pureType :
                    BUnionType.create(null, symTable.anyType, symTable.errorType);
        } else {
            memberType = hasOnlyAnyDataTypedFields(recordType) ? symTable.anydataType : symTable.anyType;
        }
        if (memberType.tag == TypeTags.RECORD) {
            memberType = getRestParamType((BRecordType) memberType);
        }
        return memberType;
    }

    public BType getRestMatchPatternConstraintType(BRecordType recordType,
                                           Map<String, BField> remainingFields,
                                           BType restVarSymbolMapType) {
        LinkedHashSet<BType> constraintTypes = new LinkedHashSet<>();
        for (BField field : remainingFields.values()) {
            constraintTypes.add(field.type);
        }

        if (!recordType.sealed) {
            BType restFieldType = recordType.restFieldType;
            if (!this.types.isNeverTypeOrStructureTypeWithARequiredNeverMember(restFieldType)) {
                constraintTypes.add(restFieldType);
            }
        }

        BType restConstraintType;
        if (constraintTypes.isEmpty()) {
            restConstraintType = symTable.neverType;
        } else if (constraintTypes.size() == 1) {
            restConstraintType = constraintTypes.iterator().next();
        } else {
            restConstraintType = BUnionType.create(null, constraintTypes);
        }
        return restVarSymbolMapType.tag == TypeTags.NONE ?
                restConstraintType : this.types.mergeTypes(restVarSymbolMapType, restConstraintType);
    }

    BRecordType createRecordTypeForRestField(Location pos, SymbolEnv env, BRecordType recordType,
                                       List<String> variableList,
                                       BType restConstraint) {
        BRecordTypeSymbol recordSymbol = createAnonRecordSymbol(env, pos);
        BRecordType recordVarType = new BRecordType(recordSymbol);
        LinkedHashMap<String, BField> unMappedFields = new LinkedHashMap<>() {{
            putAll(recordType.fields);
            if (recordType.restFieldType.tag == TypeTags.RECORD) {
                putAll(((BRecordType) recordType.restFieldType).fields);
            }
        }};

        if (recordType.sealed && (restConstraint.tag == TypeTags.NONE)) {
            setRestRecordFields(pos, env, unMappedFields, variableList, null, recordVarType);
        } else {
            setRestRecordFields(pos, env, unMappedFields, variableList, restConstraint, recordVarType);
        }

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(recordVarType,
                env.enclPkg.packageID, symTable, pos);
        recordTypeNode.initFunction =
                TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordVarType, recordSymbol, recordTypeNode, env);

        return recordVarType;
    }

    void setRestRecordFields(Location pos, SymbolEnv env,
                             LinkedHashMap<String, BField> unMappedFields,
                             List<String> variableList, BType restConstraint,
                             BRecordType targetRestRecType) {
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        LinkedHashMap<String, BField> markAsOptional = new LinkedHashMap<>();

        if (!targetRestRecType.fields.isEmpty()) {
            fields.putAll(targetRestRecType.fields);
            List<String> intersectionFields = getIntersectionFields(Arrays.asList(targetRestRecType.fields,
                    unMappedFields));
            markAsOptional.putAll(unMappedFields);
            markAsOptional.putAll(targetRestRecType.fields);
            intersectionFields.forEach(markAsOptional::remove);
        }

        //adds a never-typed optional field for the mapped bindings
        for (String fieldName : variableList) {
            unMappedFields.remove(fieldName);
            BField newField = new BField(names.fromString(fieldName), pos,
                    new BVarSymbol(Flags.OPTIONAL, names.fromString(fieldName), env.enclPkg.symbol.pkgID,
                            symTable.neverType, targetRestRecType.tsymbol, pos, VIRTUAL));
            fields.put(fieldName, newField);
        }

        for (BField field : unMappedFields.values()) {
            if (fields.containsKey(field.name.value)) {
                BField targetField = fields.get(field.getName().value);
                targetField.type = types.mergeTypes(targetField.type, field.type);
            } else {
                BField newField = new BField(field.name, pos,
                        new BVarSymbol(field.symbol.flags, field.name, env.enclPkg.symbol.pkgID,
                                field.type, targetRestRecType.tsymbol, pos, VIRTUAL));
                fields.put(field.name.value, newField);
                targetRestRecType.tsymbol.scope.define(newField.name, newField.symbol);
            }
        }

        //marks field as optional if the field is not common for all union members
        for (BField optionalField : markAsOptional.values()) {
            optionalField.symbol.flags = setSymbolAsOptional(optionalField.symbol.flags);
        }

        targetRestRecType.fields = fields;
        if (restConstraint == null) {
            targetRestRecType.restFieldType = new BNoType(TypeTags.NONE);
            targetRestRecType.sealed = true;
        } else {
            targetRestRecType.restFieldType = restConstraint;
        }
    }

    private long setSymbolAsOptional(long existingFlags) {
        Set<Flag> unmaskedFlags = Flags.unMask(existingFlags);
        unmaskedFlags.remove(Flag.REQUIRED);
        unmaskedFlags.add(Flag.OPTIONAL);
        return Flags.asMask(unmaskedFlags);
    }

    private boolean hasOnlyAnyDataTypedFields(BRecordType recordType) {
        IsAnydataUniqueVisitor isAnydataUniqueVisitor = new IsAnydataUniqueVisitor();
        return isAnydataUniqueVisitor.visit(recordType);
    }

    private boolean hasOnlyPureTypedFields(BRecordType recordType) {
        IsPureTypeUniqueVisitor isPureTypeUniqueVisitor = new IsPureTypeUniqueVisitor();
        for (BField field : recordType.fields.values()) {
            BType fieldType = field.type;
            if (!isPureTypeUniqueVisitor.visit(fieldType)) {
                return false;
            }
            isPureTypeUniqueVisitor.reset();
        }
        return recordType.sealed || isPureTypeUniqueVisitor.visit(recordType);
    }

    private boolean hasErrorTypedField(BRecordType recordType) {
        for (BField field : recordType.fields.values()) {
            BType type = field.type;
            if (hasErrorType(type)) {
                return true;
            }
        }
        return hasErrorType(recordType.restFieldType);
    }

    private boolean hasErrorType(BType type) {
        if (type.tag != TypeTags.UNION) {
            return type.tag == TypeTags.ERROR;
        }

        return ((BUnionType) type).getMemberTypes().stream().anyMatch(this::hasErrorType);
    }

    @Override
    public void visit(BLangErrorVariable errorVar) {
        if (errorVar.isDeclaredWithVar) {
            errorVar.symbol =
                    defineVarSymbol(errorVar.pos, errorVar.flagSet, symTable.noType,
                                    names.fromString(anonymousModelHelper.getNextErrorVarKey(env.enclPkg.packageID)),
                                    env, true);

            // Symbol enter each member with type other.
            BLangSimpleVariable errorMsg = errorVar.message;
            if (errorMsg != null) {
                errorMsg.isDeclaredWithVar = true;
                defineNode(errorMsg, env);
            }

            BLangVariable cause = errorVar.cause;
            if (cause != null) {
                cause.isDeclaredWithVar = true;
                defineNode(cause, env);
            }

            for (BLangErrorVariable.BLangErrorDetailEntry detailEntry: errorVar.detail) {
                BLangVariable value = detailEntry.getValue();
                value.isDeclaredWithVar = true;
                defineNode(value, env);
            }

            BLangSimpleVariable restDetail = errorVar.restDetail;
            if (restDetail != null) {
                restDetail.isDeclaredWithVar = true;
                defineNode(restDetail, env);
            }

            return;
        }

        if (errorVar.getBType() == null) {
            errorVar.setBType(symResolver.resolveTypeNode(errorVar.typeNode, env));
        }
        // To support variable forward referencing we need to symbol enter each variable inside error variable
        // with type at SymbolEnter.
        if (!symbolEnterAndValidateErrorVariable(errorVar, env)) {
            errorVar.setBType(symTable.semanticError);
            return;
        }
    }

    boolean symbolEnterAndValidateErrorVariable(BLangErrorVariable var, SymbolEnv env) {
        if (var.symbol == null) {
            Name varName = names.fromString(anonymousModelHelper.getNextErrorVarKey(env.enclPkg.packageID));
            var.symbol = defineVarSymbol(var.pos, var.flagSet, var.getBType(), varName, env, true);
        }

        return validateErrorVariable(var, env);
    }

    boolean validateErrorVariable(BLangErrorVariable errorVariable, SymbolEnv env) {
        BType varType = Types.getReferredType(errorVariable.getBType());
        BErrorType errorType;
        switch (varType.tag) {
            case TypeTags.UNION:
                BUnionType unionType = ((BUnionType) varType);
                List<BErrorType> possibleTypes = types.getAllTypes(unionType, true).stream()
                        .filter(type -> TypeTags.ERROR == type.tag)
                        .map(BErrorType.class::cast)
                        .collect(Collectors.toList());

                if (possibleTypes.isEmpty()) {
                    dlog.error(errorVariable.pos, DiagnosticErrorCode.INVALID_ERROR_BINDING_PATTERN, varType);
                    return false;
                }

                if (possibleTypes.size() > 1) {
                    LinkedHashSet<BType> detailType = new LinkedHashSet<>();
                    for (BErrorType possibleErrType : possibleTypes) {
                        detailType.add(possibleErrType.detailType);
                    }
                    BType errorDetailType = detailType.size() > 1
                            ? BUnionType.create(null, detailType)
                            : detailType.iterator().next();
                    errorType = new BErrorType(null, errorDetailType);
                } else {
                    errorType = possibleTypes.get(0);
                }
                break;
            case TypeTags.ERROR:
                errorType = (BErrorType) varType;
                break;
            case TypeTags.SEMANTIC_ERROR:
                // we assume that we have already given an error
                return false;
            default:
                dlog.error(errorVariable.pos, DiagnosticErrorCode.INVALID_ERROR_BINDING_PATTERN,
                        varType);
                return false;
        }
        errorVariable.setBType(errorType);

        if (!errorVariable.isInMatchStmt) {
            BLangSimpleVariable errorMsg = errorVariable.message;
            if (errorMsg != null) {
                defineMemberNode(errorVariable.message, env, symTable.stringType);
            }

            BLangVariable cause = errorVariable.cause;
            if (cause != null) {
                defineMemberNode(errorVariable.cause, env, symTable.errorOrNilType);
            }
        }

        if (errorVariable.detail == null || (errorVariable.detail.isEmpty()
                && !isRestDetailBindingAvailable(errorVariable))) {
            return validateErrorMessageMatchPatternSyntax(errorVariable, env);
        }

        BType detailType = Types.getReferredType(errorType.detailType);
        if (detailType.getKind() == TypeKind.RECORD || detailType.getKind() == TypeKind.MAP) {
            return validateErrorVariable(errorVariable, errorType, env);
        } else if (detailType.getKind() == TypeKind.UNION) {
            BErrorTypeSymbol errorTypeSymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.ERROR,
                    env.enclPkg.packageID, symTable.errorType,
                    env.scope.owner, errorVariable.pos, SOURCE);
            // TODO: detail type need to be a union representing all details of members of `errorType`
            errorVariable.setBType(new BErrorType(errorTypeSymbol, symTable.detailType));
            return validateErrorVariable(errorVariable, env);
        }

        if (isRestDetailBindingAvailable(errorVariable)) {
            defineMemberNode(errorVariable.restDetail, env, symTable.detailType);
        }
        return true;
    }

    private boolean validateErrorVariable(BLangErrorVariable errorVariable, BErrorType errorType, SymbolEnv env) {
        BLangSimpleVariable errorMsg = errorVariable.message;
        if (errorMsg != null && errorMsg.symbol == null) {
            defineMemberNode(errorMsg, env, symTable.stringType);
        }

        BRecordType recordType = getDetailAsARecordType(errorType);
        LinkedHashMap<String, BField> detailFields = recordType.fields;
        Set<String> matchedDetailFields = new HashSet<>();
        for (BLangErrorVariable.BLangErrorDetailEntry errorDetailEntry : errorVariable.detail) {
            String entryName = errorDetailEntry.key.getValue();
            matchedDetailFields.add(entryName);
            BField entryField = detailFields.get(entryName);

            BLangVariable boundVar = errorDetailEntry.valueBindingPattern;
            if (entryField != null) {
                if ((entryField.symbol.flags & Flags.OPTIONAL) == Flags.OPTIONAL) {
                    boundVar.setBType(BUnionType.create(null, entryField.type, symTable.nilType));
                } else {
                    boundVar.setBType(entryField.type);
                }
                errorDetailEntry.keySymbol = entryField.symbol;
            } else {
                if (recordType.sealed) {
                    dlog.error(errorVariable.pos, DiagnosticErrorCode.INVALID_ERROR_BINDING_PATTERN,
                               errorVariable.getBType());
                    boundVar.setBType(symTable.semanticError);
                    return false;
                } else {
                    boundVar.setBType(BUnionType.create(null, recordType.restFieldType, symTable.nilType));
                }
            }

            boolean isIgnoredVar = boundVar.getKind() == NodeKind.VARIABLE
                    && ((BLangSimpleVariable) boundVar).name.value.equals(Names.IGNORE.value);
            if (!isIgnoredVar) {
                defineMemberNode(boundVar, env, boundVar.getBType());
            }
        }

        if (isRestDetailBindingAvailable(errorVariable)) {
            // Type of rest pattern is a map type where constraint type is,
            // union of keys whose values are not matched in error binding/match pattern.
            BTypeSymbol typeSymbol = createTypeSymbol(SymTag.TYPE, env);
            BType constraint = getRestMapConstraintType(detailFields, matchedDetailFields, recordType);
            BMapType restType = new BMapType(TypeTags.MAP, constraint, typeSymbol);
            typeSymbol.type = restType;
            errorVariable.restDetail.setBType(restType);
            defineMemberNode(errorVariable.restDetail, env, restType);
        }
        return true;
    }

    BRecordType getDetailAsARecordType(BErrorType errorType) {
        BType detailType = Types.getReferredType(errorType.detailType);
        if (detailType.getKind() == TypeKind.RECORD) {
            return (BRecordType) detailType;
        }
        BRecordType detailRecord = new BRecordType(null);
        BMapType detailMap = (BMapType) detailType;
        detailRecord.sealed = false;
        detailRecord.restFieldType = detailMap.constraint;
        return detailRecord;
    }

    private BType getRestMapConstraintType(Map<String, BField> errorDetailFields, Set<String> matchedDetailFields,
                                           BRecordType recordType) {
        BUnionType restUnionType = BUnionType.create(null);
        if (!recordType.sealed) {
            if (recordType.restFieldType.tag == TypeTags.UNION) {
                BUnionType restFieldUnion = (BUnionType) recordType.restFieldType;
                // This is to update type name for users to read easily the cyclic unions
                if (restFieldUnion.isCyclic && errorDetailFields.entrySet().isEmpty()) {
                    restUnionType.isCyclic = true;
                    restUnionType.tsymbol = restFieldUnion.tsymbol;
                }
            } else {
                restUnionType.add(recordType.restFieldType);
            }
        }
        for (Map.Entry<String, BField> entry : errorDetailFields.entrySet()) {
            if (!matchedDetailFields.contains(entry.getKey())) {
                BType type = entry.getValue().getType();
                if (!types.isAssignable(type, restUnionType)) {
                    restUnionType.add(type);
                }
            }
        }

        Set<BType> memberTypes = restUnionType.getMemberTypes();
        if (memberTypes.size() == 1) {
            return memberTypes.iterator().next();
        }

        return restUnionType;
    }

    private boolean validateErrorMessageMatchPatternSyntax(BLangErrorVariable errorVariable, SymbolEnv env) {
        if (errorVariable.isInMatchStmt
                && !errorVariable.reasonVarPrefixAvailable
                && errorVariable.reasonMatchConst == null
                && isReasonSpecified(errorVariable)) {

            BSymbol reasonConst = symResolver.lookupSymbolInMainSpace(env.enclEnv,
                    names.fromString(errorVariable.message.name.value));
            if ((reasonConst.tag & SymTag.CONSTANT) != SymTag.CONSTANT) {
                dlog.error(errorVariable.message.pos, DiagnosticErrorCode.INVALID_ERROR_REASON_BINDING_PATTERN,
                        errorVariable.message.name);
            } else {
                dlog.error(errorVariable.message.pos, DiagnosticErrorCode.UNSUPPORTED_ERROR_REASON_CONST_MATCH);
            }
            return false;
        }
        return true;
    }

    private boolean isReasonSpecified(BLangErrorVariable errorVariable) {
        return !isIgnoredOrEmpty(errorVariable.message);
    }

    boolean isIgnoredOrEmpty(BLangSimpleVariable varNode) {
        return varNode.name.value.equals(Names.IGNORE.value) || varNode.name.value.equals("");
    }

    private boolean isRestDetailBindingAvailable(BLangErrorVariable errorVariable) {
        return errorVariable.restDetail != null &&
                !errorVariable.restDetail.name.value.equals(Names.IGNORE.value);
    }

    private BTypeSymbol createTypeSymbol(int type, SymbolEnv env) {
        return new BTypeSymbol(type, Flags.PUBLIC, Names.EMPTY, env.enclPkg.packageID,
                null, env.scope.owner, symTable.builtinPos, VIRTUAL);
    }

    private void defineMemberNode(BLangVariable memberVar, SymbolEnv env, BType type) {
        memberVar.setBType(type);
        // Module level variables declared with `var` already defined
        if ((env.scope.owner.tag & SymTag.PACKAGE) == SymTag.PACKAGE && memberVar.isDeclaredWithVar) {
            memberVar.symbol.type = type;
            memberVar.isDeclaredWithVar = false;
            // Need to assign resolved type for member variables inside complex variable declared with `var`
            if (memberVar.getKind() == NodeKind.VARIABLE) {
                return;
            }
        }
        defineNode(memberVar, env);
    }

    public void visit(BLangXMLAttribute bLangXMLAttribute) {
        if (!(bLangXMLAttribute.name.getKind() == NodeKind.XML_QNAME)) {
            return;
        }

        BLangXMLQName qname = (BLangXMLQName) bLangXMLAttribute.name;

        // If the attribute is not an in-line namespace declaration, check for duplicate attributes.
        // If no duplicates, then define this attribute symbol.
        if (!bLangXMLAttribute.isNamespaceDeclr) {
            BXMLAttributeSymbol attrSymbol = new BXMLAttributeSymbol(qname.localname.value, qname.namespaceURI,
                                                                     env.enclPkg.symbol.pkgID, env.scope.owner,
                                                                     bLangXMLAttribute.pos, SOURCE);

            if (missingNodesHelper.isMissingNode(qname.localname.value)
                    || (qname.namespaceURI != null && missingNodesHelper.isMissingNode(qname.namespaceURI))) {
                attrSymbol.origin = VIRTUAL;
            }
            if (symResolver.checkForUniqueMemberSymbol(bLangXMLAttribute.pos, env, attrSymbol)) {
                env.scope.define(attrSymbol.name, attrSymbol);
                bLangXMLAttribute.symbol = attrSymbol;
            }
            return;
        }

        List<BLangExpression> exprs = bLangXMLAttribute.value.textFragments;
        String nsURI = null;

        // We reach here if the attribute is an in-line namesapce declaration.
        // Get the namespace URI, only if it is statically defined. Then define the namespace symbol.
        // This namespace URI is later used by the attributes, when they lookup for duplicate attributes.
        // TODO: find a better way to get the statically defined URI.
        NodeKind nodeKind = exprs.get(0).getKind();
        if (exprs.size() == 1 && (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL)) {
            nsURI = (String) ((BLangLiteral) exprs.get(0)).value;
        }

        String symbolName = qname.localname.value;
        if (symbolName.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            symbolName = XMLConstants.DEFAULT_NS_PREFIX;
        }

        Name prefix = names.fromString(symbolName);
        BXMLNSSymbol xmlnsSymbol = new BXMLNSSymbol(prefix, nsURI, env.enclPkg.symbol.pkgID, env.scope.owner,
                                                    qname.localname.pos, getOrigin(prefix));

        if (symResolver.checkForUniqueMemberSymbol(bLangXMLAttribute.pos, env, xmlnsSymbol)) {
            env.scope.define(xmlnsSymbol.name, xmlnsSymbol);
            bLangXMLAttribute.symbol = xmlnsSymbol;
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        recordTypeNode.typeDefEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);
        defineRecordTypeNode(recordTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        for (BLangType type : unionTypeNode.memberTypeNodes) {
            defineNode(type, env);
        }
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        for (BLangType type : intersectionTypeNode.constituentTypeNodes) {
            defineNode(type, env);
        }
    }

    private void defineRecordTypeNode(BLangRecordTypeNode recordTypeNode) {
        BRecordType recordType = (BRecordType) recordTypeNode.symbol.type;
        recordTypeNode.setBType(recordType);

        // Define all the fields
        resolveFields(recordType, recordTypeNode);
        resolveFieldsIncluded(recordType, recordTypeNode);

        recordType.sealed = recordTypeNode.sealed;
        if (recordTypeNode.sealed && recordTypeNode.restFieldType != null) {
            dlog.error(recordTypeNode.restFieldType.pos, DiagnosticErrorCode.REST_FIELD_NOT_ALLOWED_IN_CLOSED_RECORDS);
            return;
        }

        List<BType> fieldTypes = new ArrayList<>(recordType.fields.size());
        for (BField field : recordType.fields.values()) {
            BType type = field.type;
            fieldTypes.add(type);
        }

        if (recordTypeNode.restFieldType == null) {
            symResolver.markParameterizedType(recordType, fieldTypes);
            if (recordTypeNode.sealed) {
                recordType.restFieldType = symTable.noType;
                return;
            }
            recordType.restFieldType = symTable.anydataType;
            return;
        }

        recordType.restFieldType = symResolver.resolveTypeNode(recordTypeNode.restFieldType, env);
        fieldTypes.add(recordType.restFieldType);
        symResolver.markParameterizedType(recordType, fieldTypes);
    }

    private Collector<BField, ?, LinkedHashMap<String, BField>> getFieldCollector() {
        BinaryOperator<BField> mergeFunc = (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
        return Collectors.toMap(field -> field.name.value, Function.identity(), mergeFunc, LinkedHashMap::new);
    }

    // Private methods

    private void populateLangLibInSymTable(BPackageSymbol packageSymbol) {

        PackageID langLib = packageSymbol.pkgID;
        if (langLib.equals(ARRAY)) {
            symTable.langArrayModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(DECIMAL)) {
            symTable.langDecimalModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(ERROR)) {
            symTable.langErrorModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(FLOAT)) {
            symTable.langFloatModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(FUNCTION)) {
            symTable.langFunctionModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(FUTURE)) {
            symTable.langFutureModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(INT)) {
            symTable.langIntModuleSymbol = packageSymbol;
            symTable.updateIntSubtypeOwners();
            return;
        }
        if (langLib.equals(MAP)) {
            symTable.langMapModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(OBJECT)) {
            symTable.langObjectModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(STREAM)) {
            symTable.langStreamModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(STRING)) {
            symTable.langStringModuleSymbol = packageSymbol;
            symTable.updateStringSubtypeOwners();
            return;
        }
        if (langLib.equals(TABLE)) {
            symTable.langTableModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(TYPEDESC)) {
            symTable.langTypedescModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(VALUE)) {
            symTable.langValueModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(XML)) {
            symTable.langXmlModuleSymbol = packageSymbol;
            symTable.updateXMLSubtypeOwners();
            return;
        }
        if (langLib.equals(BOOLEAN)) {
            symTable.langBooleanModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(QUERY)) {
            symTable.langQueryModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(TRANSACTION)) {
            symTable.langTransactionModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(REGEXP)) {
            symTable.langRegexpModuleSymbol = packageSymbol;
            return;
        }
    }

    public boolean isValidAnnotationType(BType type) {
        if (type == symTable.semanticError) {
            return false;
        }

        switch (type.tag) {
            case TypeTags.MAP:
                BType constraintType = ((BMapType) type).constraint;
                return isCloneableTypeTypeSkippingObjectType(constraintType);
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) type;
                for (BField field : recordType.fields.values()) {
                    if (!isCloneableTypeTypeSkippingObjectType(field.type)) {
                        return false;
                    }
                }

                BType recordRestType = recordType.restFieldType;
                if (recordRestType == null || recordRestType == symTable.noType) {
                    return true;
                }

                return isCloneableTypeTypeSkippingObjectType(recordRestType);
            case TypeTags.ARRAY:
                BType elementType = Types.getReferredType(((BArrayType) type).eType);
                if ((elementType.tag == TypeTags.MAP) || (elementType.tag == TypeTags.RECORD)) {
                    return isValidAnnotationType(elementType);
                }
                return false;
            case TypeTags.TYPEREFDESC:
                return isValidAnnotationType(((BTypeReferenceType) type).referredType);
        }

        return types.isAssignable(type, symTable.trueType);
    }

    private boolean isCloneableTypeTypeSkippingObjectType(BType type) {
        return isCloneableTypeSkippingObjectTypeHelper(type, new HashSet<>());
    }

    private boolean isCloneableTypeSkippingObjectTypeHelper(BType type, Set<BType> unresolvedTypes) {
        if (type == symTable.semanticError) {
            return false;
        }

        if (!unresolvedTypes.add(type)) {
            return true;
        }

        switch (type.tag) {
            case TypeTags.OBJECT:
            case TypeTags.ANYDATA:
                return true;
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) type;
                for (BField field : recordType.fields.values()) {
                    if (!isCloneableTypeSkippingObjectTypeHelper(field.type, unresolvedTypes)) {
                        return false;
                    }
                }
                BType recordRestType = recordType.restFieldType;
                if (recordRestType == null || recordRestType == symTable.noType) {
                    return true;
                }
                return isCloneableTypeSkippingObjectTypeHelper(recordRestType, unresolvedTypes);
            case TypeTags.MAP:
                BType constraintType = ((BMapType) type).constraint;
                return isCloneableTypeSkippingObjectTypeHelper(constraintType, unresolvedTypes);
            case TypeTags.UNION:
                for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                    if (!isCloneableTypeSkippingObjectTypeHelper(memberType, unresolvedTypes)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                for (BType tupMemType : tupleType.getTupleTypes()) {
                    if (!isCloneableTypeSkippingObjectTypeHelper(tupMemType, unresolvedTypes)) {
                        return false;
                    }
                }
                BType tupRestType = tupleType.restType;
                if (tupRestType == null) {
                    return true;
                }
                return isCloneableTypeSkippingObjectTypeHelper(tupRestType, unresolvedTypes);
            case TypeTags.TABLE:
                return isCloneableTypeSkippingObjectTypeHelper(((BTableType) type).constraint, unresolvedTypes);
            case TypeTags.ARRAY:
                return isCloneableTypeSkippingObjectTypeHelper(((BArrayType) type).getElementType(),
                        unresolvedTypes);
            case TypeTags.TYPEREFDESC:
                return isCloneableTypeSkippingObjectTypeHelper(Types.getReferredType(type), unresolvedTypes);
        }

        return types.isAssignable(type, symTable.cloneableType);
    }


    /**
     * Visit each compilation unit (.bal file) and add each top-level node
     * in the compilation unit to the package node.
     *
     * @param pkgNode current package node
     */
    private void populatePackageNode(BLangPackage pkgNode) {
        List<BLangCompilationUnit> compUnits = pkgNode.getCompilationUnits();
        compUnits.forEach(compUnit -> populateCompilationUnit(pkgNode, compUnit));
    }

    /**
     * Visit each top-level node and add it to the package node.
     *
     * @param pkgNode  current package node
     * @param compUnit current compilation unit
     */
    private void populateCompilationUnit(BLangPackage pkgNode, BLangCompilationUnit compUnit) {
        compUnit.getTopLevelNodes().forEach(node -> addTopLevelNode(pkgNode, node));
    }

    private void addTopLevelNode(BLangPackage pkgNode, TopLevelNode node) {
        NodeKind kind = node.getKind();

        // Here we keep all the top-level nodes of a compilation unit (aka file) in exact same
        // order as they appear in the compilation unit. This list contains all the top-level
        // nodes of all the compilation units grouped by the compilation unit.
        // This allows other compiler phases to visit top-level nodes in the exact same order
        // as they appear in compilation units. This is required for error reporting.
        if (kind != NodeKind.PACKAGE_DECLARATION && kind != IMPORT) {
            pkgNode.topLevelNodes.add(node);
        }

        switch (kind) {
            case IMPORT:
                // TODO Verify the rules..
                // TODO Check whether the same package alias (if any) has been used for the same import
                // TODO The version of an import package can be specified only once for a package
                pkgNode.imports.add((BLangImportPackage) node);
                break;
            case FUNCTION:
                pkgNode.functions.add((BLangFunction) node);
                break;
            case TYPE_DEFINITION:
                pkgNode.typeDefinitions.add((BLangTypeDefinition) node);
                break;
            case SERVICE:
                pkgNode.services.add((BLangService) node);
                break;
            case VARIABLE:
            case TUPLE_VARIABLE:
            case RECORD_VARIABLE:
            case ERROR_VARIABLE:
                pkgNode.globalVars.add((BLangVariable) node);
                // TODO There are two kinds of package level variables, constant and regular variables.
                break;
            case ANNOTATION:
                // TODO
                pkgNode.annotations.add((BLangAnnotation) node);
                break;
            case XMLNS:
                pkgNode.xmlnsList.add((BLangXMLNS) node);
                break;
            case CONSTANT:
                pkgNode.constants.add((BLangConstant) node);
                break;
            case CLASS_DEFN:
                pkgNode.classDefinitions.add((BLangClassDefinition) node);
                break;
            case CLIENT_DECL:
                pkgNode.clientDeclarations.add((BLangClientDeclaration) node);
        }
    }

    private void defineErrorDetails(List<BLangTypeDefinition> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangTypeDefinition typeDef : typeDefNodes) {
            BLangType typeNode = typeDef.typeNode;
            if (typeNode.getKind() == NodeKind.ERROR_TYPE) {
                SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(typeNode, typeDef.symbol.scope, pkgEnv);
                BLangErrorType errorTypeNode = (BLangErrorType) typeNode;
                BType typeDefType = typeDef.symbol.type;
                ((BErrorType) typeDefType).detailType = getDetailType(typeDefEnv, errorTypeNode);
            } else if (typeNode.getBType() != null && typeNode.getBType().tag == TypeTags.ERROR) {
                SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(typeNode, typeDef.symbol.scope, pkgEnv);
                BType detailType = ((BErrorType) typeNode.getBType()).detailType;
                if (detailType == symTable.noType) {
                    BType resolvedType = symResolver.resolveTypeNode(typeNode, typeDefEnv);
                    BErrorType type = (BErrorType) Types.getReferredType(resolvedType);
                    ((BErrorType) Types.getReferredType(typeDef.symbol.type))
                            .detailType = type.detailType;
                }
            }
        }
    }

    private BType getDetailType(SymbolEnv typeDefEnv, BLangErrorType errorTypeNode) {
        BLangType detailType = errorTypeNode.detailType;
        if (detailType != null && detailType.getKind() == NodeKind.CONSTRAINED_TYPE) {
            BLangType constraint = ((BLangConstrainedType) detailType).constraint;
            if (constraint.getKind() == NodeKind.USER_DEFINED_TYPE) {
                BLangUserDefinedType userDefinedType = (BLangUserDefinedType) constraint;
                if (userDefinedType.typeName.value.equals(TypeDefBuilderHelper.INTERSECTED_ERROR_DETAIL)) {
                    errorTypeNode.detailType = null;
                    return ((BErrorType) errorTypeNode.getBType()).detailType;
                }
            }
        }

        return Optional.ofNullable(errorTypeNode.detailType)
                .map(bLangType -> symResolver.resolveTypeNode(bLangType, typeDefEnv))
                .orElse(symTable.detailType);
    }

    private void defineFields(List<BLangNode> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangNode typeDef : typeDefNodes) {
            if (typeDef.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDefinition = (BLangClassDefinition) typeDef;
                if (isObjectCtor(classDefinition)) {
                    continue;
                }
                defineFieldsOfClassDef(classDefinition, pkgEnv);
            } else if (typeDef.getKind() == NodeKind.TYPE_DEFINITION) {
                defineFields((BLangTypeDefinition) typeDef, pkgEnv);
            }
        }
    }

    public void defineFieldsOfClassDef(BLangClassDefinition classDefinition, SymbolEnv env) {
        SymbolEnv typeDefEnv = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, env);
        BObjectTypeSymbol tSymbol = (BObjectTypeSymbol) classDefinition.symbol;
        BObjectType objType = (BObjectType) tSymbol.type;

        if (classDefinition.isObjectContructorDecl) {
            classDefinition.oceEnvData.fieldEnv = typeDefEnv;
        }

        classDefinition.typeDefEnv = typeDefEnv;

        for (BLangSimpleVariable field : classDefinition.fields) {
            defineNode(field, typeDefEnv);
            if (field.expr != null) {
                field.symbol.isDefaultable = true;
            }
            // Unless skipped, this causes issues in negative cases such as duplicate fields.
            if (field.symbol.type == symTable.semanticError) {
                continue;
            }
            objType.fields.put(field.name.value, new BField(names.fromIdNode(field.name), field.pos, field.symbol));
        }
    }

    private void defineFields(BLangTypeDefinition typeDef, SymbolEnv pkgEnv) {
        NodeKind nodeKind = typeDef.typeNode.getKind();
        if (nodeKind != NodeKind.RECORD_TYPE) {
            defineNode(typeDef.typeNode, pkgEnv);
            return;
        }

        // TODO : Following logic should move to visitor of BLangRecordType. Fix with issue-31317
        // Create typeDef type
        BStructureType structureType = (BStructureType) typeDef.symbol.type;
        BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDef.typeNode;

        if (typeDef.symbol.kind == SymbolKind.TYPE_DEF && structureType.tsymbol.kind == SymbolKind.RECORD) {
            BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) structureTypeNode;
            BRecordType recordType = (BRecordType) structureType;
            // update this before resolving fields type checker to work properly for field resolution
            recordType.sealed = recordTypeNode.sealed;
        }

        Scope recordScope = structureType.tsymbol.scope;
        SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(structureTypeNode, recordScope, pkgEnv);
        structureTypeNode.typeDefEnv = typeDefEnv;

        resolveFields(structureType, structureTypeNode);
        if (structureTypeNode.typeRefs.isEmpty()) {
            defineReferencedFieldsOfRecordTypeDef(typeDef); // update rest type
        }
    }

    private void resolveFields(BStructureType structureType, BLangStructureTypeNode structureTypeNode) {
        SymbolEnv typeDefEnv = structureTypeNode.typeDefEnv;
        structureType.fields = structureTypeNode.fields.stream()
                .peek((BLangSimpleVariable field) -> defineNode(field, typeDefEnv))
                .filter(field -> field.symbol.type != symTable.semanticError) // filter out erroneous fields
                .map((BLangSimpleVariable field) -> {
                    field.symbol.isDefaultable = field.expr != null;
                    return new BField(names.fromIdNode(field.name), field.pos, field.symbol);
                })
                .collect(getFieldCollector());
    }

    private void defineReferencedFieldsOfRecordTypeDef(BLangTypeDefinition typeDef) {
        if (typeDef.referencedFieldsDefined == true) {
            return;
        }
        NodeKind nodeKind = typeDef.typeNode.getKind();
        if (nodeKind != NodeKind.OBJECT_TYPE && nodeKind != NodeKind.RECORD_TYPE) {
            return;
        }
        resolveReferencedFields(typeDef);
        resolveRestField(typeDef);
        typeDef.referencedFieldsDefined = true;
    }

    private void resolveRestField(BLangTypeDefinition typeDef) {
        BStructureType structureType = (BStructureType) typeDef.symbol.type;
        BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDef.typeNode;
        if (typeDef.symbol.kind == SymbolKind.TYPE_DEF && structureType.tsymbol.kind != SymbolKind.RECORD) {
            return;
        }

        SymbolEnv typeDefEnv = structureTypeNode.typeDefEnv;
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) structureTypeNode;
        BRecordType recordType = (BRecordType) structureType;
        recordType.sealed = recordTypeNode.sealed;
        if (recordTypeNode.sealed && recordTypeNode.restFieldType != null) {
            dlog.error(recordTypeNode.restFieldType.pos, DiagnosticErrorCode.REST_FIELD_NOT_ALLOWED_IN_CLOSED_RECORDS);
            return;
        }

        if (recordTypeNode.restFieldType != null) {
            recordType.restFieldType = symResolver.resolveTypeNode(recordTypeNode.restFieldType, typeDefEnv);
            return;
        }

        if (!recordTypeNode.sealed) {
            recordType.restFieldType = symTable.anydataType;
            return;
        }

        // analyze restFieldType for open records
        for (BLangType typeRef : recordTypeNode.typeRefs) {
            BType refType = Types.getReferredType(typeRef.getBType());
            if (refType.tag != TypeTags.RECORD) {
                continue;
            }
            BType restFieldType = ((BRecordType) refType).restFieldType;
            if (restFieldType == symTable.noType) {
                continue;
            }
            if (recordType.restFieldType != null && !types.isSameType(recordType.restFieldType, restFieldType)) {
                recordType.restFieldType = symTable.noType;
                dlog.error(recordTypeNode.pos,
                        DiagnosticErrorCode.
                        CANNOT_USE_TYPE_INCLUSION_WITH_MORE_THAN_ONE_OPEN_RECORD_WITH_DIFFERENT_REST_DESCRIPTOR_TYPES);
                return;
            }
            recordType.restFieldType = restFieldType;
            recordType.sealed = false;
        }

        if (recordType.restFieldType != null) {
            return;
        }
        recordType.restFieldType = symTable.noType;
    }

    private void resolveReferencedFields(BLangTypeDefinition typeDef) {
        BStructureType structureType = (BStructureType) typeDef.symbol.type;
        BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDef.typeNode;
        resolveFieldsIncluded(structureType, structureTypeNode);
    }

    private void resolveFieldsIncluded(BStructureType structureType, BLangStructureTypeNode structureTypeNode) {
        resolveIncludedFields(structureTypeNode);
        populateResolvedTypeRefs(structureType, structureTypeNode);
        defineReferencedFields(structureType, structureTypeNode);
    }

    private void populateResolvedTypeRefs(BStructureType structureType, BLangStructureTypeNode structureTypeNode) {
        // collect resolved type refs from structural type
        List<BLangType> typeRefs = structureTypeNode.typeRefs;
        structureType.typeInclusions = new ArrayList<>(typeRefs.size());
        for (BLangType tRef : typeRefs) {
            structureType.typeInclusions.add(tRef.getBType());
        }
    }

    private void defineReferencedFields(BStructureType structureType, BLangStructureTypeNode structureTypeNode) {
        SymbolEnv typeDefEnv = structureTypeNode.typeDefEnv;
        for (BLangSimpleVariable field : structureTypeNode.includedFields) {
            defineNode(field, typeDefEnv);
            if (field.symbol.type == symTable.semanticError) {
                continue;
            }
            structureType.fields.put(field.name.value, new BField(names.fromIdNode(field.name), field.pos,
                    field.symbol));
        }
    }

    private void defineFunctions(List<BLangNode> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangNode node : typeDefNodes) {
            if (node.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDef = (BLangClassDefinition) node;
                if (isObjectCtor(classDef)) {
                    continue;
                }
                defineFunctionsOfClassDef(pkgEnv, classDef);
            } else if (node.getKind() == NodeKind.TYPE_DEFINITION) {
                defineFunctionsOfObjectTypeDef(pkgEnv, (BLangTypeDefinition) node);
            }
        }
    }

    private void validateInclusionsForNonPrivateMembers(List<BLangType> inclusions) {
        for (BLangType inclusion : inclusions) {
            BType type = Types.getReferredType(inclusion.getBType());

            if (type.tag != TypeTags.OBJECT) {
                continue;
            }

            BObjectType objectType = (BObjectType) type;

            boolean hasPrivateMember = false;

            for (BField field : objectType.fields.values()) {
                if (Symbols.isFlagOn(field.symbol.flags, Flags.PRIVATE)) {
                    hasPrivateMember = true;
                    break;
                }
            }

            if (!hasPrivateMember) {
                for (BAttachedFunction method : ((BObjectTypeSymbol) type.tsymbol).attachedFuncs) {
                    if (Symbols.isFlagOn(method.symbol.flags, Flags.PRIVATE)) {
                        hasPrivateMember = true;
                        break;
                    }
                }
            }

            if (hasPrivateMember) {
                dlog.error(inclusion.pos, DiagnosticErrorCode.INVALID_INCLUSION_OF_OBJECT_WITH_PRIVATE_MEMBERS);
            }
        }
    }

    private void defineFunctionsOfObjectTypeDef(SymbolEnv pkgEnv, BLangTypeDefinition node) {
        BLangTypeDefinition typeDefinition = node;
        BLangType typeNode = typeDefinition.typeNode;

        if (typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            validateInclusionsForNonPrivateMembers(((BLangObjectTypeNode) typeNode).typeRefs);
        }

        BLangTypeDefinition typeDef = node;
        if (typeDef.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            BObjectType objectType = (BObjectType) typeDef.symbol.type;

            if (objectType.mutableType != null) {
                // If this is an object type definition defined for an immutable type.
                // We skip defining methods here since they would either be defined already, or would be defined
                // later.
                return;
            }

            BLangObjectTypeNode objTypeNode = (BLangObjectTypeNode) typeDef.typeNode;
            SymbolEnv objMethodsEnv =
                    SymbolEnv.createObjectMethodsEnv(objTypeNode, (BObjectTypeSymbol) objTypeNode.symbol, pkgEnv);

            // Define the functions defined within the object
            defineObjectInitFunction(objTypeNode, objMethodsEnv);
            objTypeNode.functions.forEach(f -> {
                f.flagSet.add(Flag.FINAL); // Method's can't change once defined.
                f.setReceiver(ASTBuilderUtil.createReceiver(typeDef.pos, objectType));
                defineNode(f, objMethodsEnv);
            });

            Set<String> includedFunctionNames = new HashSet<>();
            // Add the attached functions of the referenced types to this object.
            // Here it is assumed that all the attached functions of the referred type are
            // resolved by the time we reach here. It is achieved by ordering the typeDefs
            // according to the precedence.
            for (BLangType typeRef : objTypeNode.typeRefs) {
                if (typeRef.getBType().tsymbol == null) {
                    continue;
                }
                BTypeSymbol objectSymbol = Types.getReferredType(typeRef.getBType()).tsymbol;
                if (objectSymbol.kind != SymbolKind.OBJECT) {
                    continue;
                }

                List<BAttachedFunction> functions = ((BObjectTypeSymbol) objectSymbol).attachedFuncs;
                for (BAttachedFunction function : functions) {
                    defineReferencedFunction(typeDef.pos, typeDef.flagSet, objMethodsEnv,
                            typeRef, function, includedFunctionNames, typeDef.symbol,
                            ((BLangObjectTypeNode) typeDef.typeNode).functions, node.internal);
                }
            }
        }
    }

    private void validateIntersectionTypeDefinitions(List<BLangTypeDefinition> typeDefNodes, PackageID packageID) {
        Set<BType> loggedTypes = new HashSet<>();

        for (BLangTypeDefinition typeDefNode : typeDefNodes) {
            BLangType typeNode = typeDefNode.typeNode;
            NodeKind kind = typeNode.getKind();
            if (kind == NodeKind.INTERSECTION_TYPE_NODE) {
                BType currentType = Types.getReferredType(typeNode.getBType());

                if (currentType.tag != TypeTags.INTERSECTION) {
                    continue;
                }

                BIntersectionType intersectionType = (BIntersectionType) currentType;

                BType effectiveType = intersectionType.effectiveType;
                if (!loggedTypes.add(effectiveType)) {
                    continue;
                }

                boolean hasNonReadOnlyElement = false;
                for (BType constituentType : intersectionType.getConstituentTypes()) {
                    if (Types.getReferredType(constituentType) == symTable.readonlyType) {
                        continue;
                    }
                    // If constituent type is error, we have already validated error intersections.
                    if (!types.isSelectivelyImmutableType(constituentType, true, packageID)
                            && Types.getReferredType(constituentType).tag != TypeTags.ERROR) {

                        hasNonReadOnlyElement = true;
                        break;
                    }
                }

                if (hasNonReadOnlyElement) {
                    dlog.error(typeDefNode.typeNode.pos, DiagnosticErrorCode.INVALID_INTERSECTION_TYPE, typeNode);
                    typeNode.setBType(symTable.semanticError);
                }

                continue;
            }

            BStructureType immutableType;
            BStructureType mutableType;

            if (kind == NodeKind.OBJECT_TYPE) {
                BObjectType currentType = (BObjectType) typeNode.getBType();
                mutableType = currentType.mutableType;
                if (mutableType == null) {
                    continue;
                }
                immutableType = currentType;
            } else if (kind == NodeKind.RECORD_TYPE) {
                BRecordType currentType = (BRecordType) typeNode.getBType();
                mutableType = currentType.mutableType;
                if (mutableType == null) {
                    continue;
                }
                immutableType = currentType;
            } else {
                continue;
            }

            if (!loggedTypes.add(immutableType)) {
                continue;
            }

            if (!types.isSelectivelyImmutableType(mutableType, true, packageID)) {
                dlog.error(typeDefNode.typeNode.pos, DiagnosticErrorCode.INVALID_INTERSECTION_TYPE, typeDefNode.name);
                typeNode.setBType(symTable.semanticError);
            }
        }
    }

    private void defineUndefinedReadOnlyTypes(List<BLangTypeDefinition> typeDefNodes, List<BLangNode> typeDefs,
                                              SymbolEnv pkgEnv) {
        // Any newly added typedefs are due to `T & readonly` typed fields. Once the fields are set for all
        // type-definitions we can revisit the newly added type-definitions and define the fields and members for them.
        populateImmutableTypeFieldsAndMembers(typeDefNodes, pkgEnv);

        // If all the fields of a structure are readonly or final, mark the structure type itself as readonly.
        // If the type is a `readonly object` validate if all fields are compatible.
        validateFieldsAndSetReadOnlyType(typeDefs, pkgEnv);

        defineReadOnlyInclusions(typeDefs, pkgEnv);
    }

    private void populateImmutableTypeFieldsAndMembers(List<BLangTypeDefinition> typeDefNodes, SymbolEnv pkgEnv) {
        int size = typeDefNodes.size();
        for (int i = 0; i < size; i++) {
            BLangTypeDefinition typeDef = typeDefNodes.get(i);
            NodeKind nodeKind = typeDef.typeNode.getKind();
            if (nodeKind == NodeKind.OBJECT_TYPE) {
                if (((BObjectType) typeDef.symbol.type).mutableType == null) {
                    continue;
                }
            } else if (nodeKind == NodeKind.RECORD_TYPE) {
                if (((BRecordType) typeDef.symbol.type).mutableType == null) {
                    continue;
                }
            } else {
                continue;
            }

            SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(typeDef.typeNode, typeDef.symbol.scope, pkgEnv);
            ImmutableTypeCloner.defineUndefinedImmutableFields(typeDef, types, typeDefEnv, symTable,
                                                               anonymousModelHelper, names);

            if (nodeKind != NodeKind.OBJECT_TYPE) {
                continue;
            }

            BObjectType immutableObjectType = (BObjectType) typeDef.symbol.type;
            BObjectType mutableObjectType = immutableObjectType.mutableType;

            ImmutableTypeCloner.defineObjectFunctions((BObjectTypeSymbol) immutableObjectType.tsymbol,
                                                      (BObjectTypeSymbol) mutableObjectType.tsymbol, names, symTable);
        }
    }

    private void validateFieldsAndSetReadOnlyType(List<BLangNode> typeDefNodes, SymbolEnv pkgEnv) {
        int origSize = typeDefNodes.size();
        for (int i = 0; i < origSize; i++) {
            BLangNode typeDefOrClass = typeDefNodes.get(i);
            if (typeDefOrClass.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDefinition = (BLangClassDefinition) typeDefOrClass;
                if (isObjectCtor(classDefinition)) {
                    continue;
                }
                setReadOnlynessOfClassDef(classDefinition, pkgEnv);
                continue;
            } else if (typeDefOrClass.getKind() != NodeKind.TYPE_DEFINITION) {
                continue;
            }

            BLangTypeDefinition typeDef = (BLangTypeDefinition) typeDefOrClass;
            BLangType typeNode = typeDef.typeNode;
            NodeKind nodeKind = typeNode.getKind();
            if (nodeKind != NodeKind.OBJECT_TYPE && nodeKind != NodeKind.RECORD_TYPE) {
                continue;
            }

            BSymbol symbol = typeDef.symbol;
            BStructureType structureType = (BStructureType) Types.getReferredType(symbol.type);

            if (Symbols.isFlagOn(structureType.flags, Flags.READONLY)) {
                if (structureType.tag != TypeTags.OBJECT) {
                    continue;
                }

                BObjectType objectType = (BObjectType) structureType;
                if (objectType.mutableType != null) {
                    // This is an object defined due to `T & readonly` like usage, thus validation has been done
                    // already.
                    continue;
                }

                Location pos = typeDef.pos;
                // We reach here for `readonly object`s.
                // We now validate if it is a valid `readonly object` - i.e., all the fields are compatible readonly
                // types.
                if (!types.isSelectivelyImmutableType(objectType, new HashSet<>(), pkgEnv.enclPkg.packageID)) {
                    dlog.error(pos, DiagnosticErrorCode.INVALID_READONLY_OBJECT_TYPE, objectType);
                    return;
                }

                SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(typeNode, symbol.scope, pkgEnv);
                for (BField field : objectType.fields.values()) {
                    BType type = field.type;

                    Set<Flag> flagSet;
                    if (typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                        flagSet = ((BLangObjectTypeNode) typeNode).flagSet;
                    } else if (typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                        flagSet = ((BLangUserDefinedType) typeNode).flagSet;
                    } else {
                        flagSet = new HashSet<>();
                    }

                    if (!types.isInherentlyImmutableType(type)) {
                        field.type = field.symbol.type = ImmutableTypeCloner.getImmutableIntersectionType(
                                pos, types, type, typeDefEnv, symTable,
                                anonymousModelHelper, names, flagSet);

                    }

                    field.symbol.flags |= Flags.READONLY;
                }
                continue;
            }

            if (nodeKind != NodeKind.RECORD_TYPE) {
                continue;
            }

            BRecordType recordType = (BRecordType) structureType;
            if (!recordType.sealed && recordType.restFieldType.tag != TypeTags.NEVER) {
                continue;
            }

            boolean allImmutableFields = true;

            Collection<BField> fields = structureType.fields.values();

            for (BField field : fields) {
                if (!Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
                    allImmutableFields = false;
                    break;
                }
            }

            if (allImmutableFields) {
                structureType.tsymbol.flags |= Flags.READONLY;
                structureType.flags |= Flags.READONLY;
            }
        }
    }

    private void defineReadOnlyInclusions(List<BLangNode> typeDefs, SymbolEnv pkgEnv) {
        for (BLangNode typeDef : typeDefs) {
            if (typeDef.getKind() != NodeKind.CLASS_DEFN) {
                continue;
            }
            BLangClassDefinition classDefinition = (BLangClassDefinition) typeDef;
            if (isObjectCtor(classDefinition)) {
                continue;
            }
            defineReadOnlyIncludedFieldsAndMethods(classDefinition, pkgEnv);
            classDefinition.definitionCompleted = true;
        }
    }

    public void defineReadOnlyIncludedFieldsAndMethods(BLangClassDefinition classDefinition, SymbolEnv pkgEnv) {
        SymbolEnv typeDefEnv = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, pkgEnv);
        BObjectType objType = (BObjectType) classDefinition.symbol.type;
        defineReferencedClassFields(classDefinition, typeDefEnv, objType, true);

        SymbolEnv objMethodsEnv = SymbolEnv.createClassMethodsEnv(classDefinition,
                (BObjectTypeSymbol) classDefinition.symbol,
                pkgEnv);
        defineIncludedMethods(classDefinition, objMethodsEnv, true);
    }

    private void setReadOnlynessOfClassDef(BLangClassDefinition classDef, SymbolEnv pkgEnv) {
        BObjectType objectType = (BObjectType) classDef.getBType();
        Location pos = classDef.pos;

        if (Symbols.isFlagOn(classDef.getBType().flags, Flags.READONLY)) {
            if (!types.isSelectivelyImmutableType(objectType, new HashSet<>(), pkgEnv.enclPkg.packageID)) {
                dlog.error(pos, DiagnosticErrorCode.INVALID_READONLY_OBJECT_TYPE, objectType);
                return;
            }

            ImmutableTypeCloner.markFieldsAsImmutable(classDef, pkgEnv, objectType, types, anonymousModelHelper,
                                                      symTable, names, pos);
        } else if (classDef.isObjectContructorDecl) {
            Collection<BField> fields = objectType.fields.values();
            if (fields.isEmpty()) {
                return;
            }

            for (BField field : fields) {
                if (!Symbols.isFlagOn(field.symbol.flags, Flags.FINAL) ||
                        !Symbols.isFlagOn(field.type.flags, Flags.READONLY)) {
                    return;
                }
            }

            classDef.getBType().tsymbol.flags |= Flags.READONLY;
            classDef.getBType().flags |= Flags.READONLY;
        }
    }

    private void defineInvokableSymbol(BLangInvokableNode invokableNode, BInvokableSymbol funcSymbol,
                                       SymbolEnv invokableEnv) {
        invokableNode.symbol = funcSymbol;
        defineSymbol(invokableNode.name.pos, funcSymbol);
        invokableEnv.scope = funcSymbol.scope;
        defineInvokableSymbolParams(invokableNode, funcSymbol, invokableEnv);

        if (Symbols.isFlagOn(funcSymbol.type.tsymbol.flags, Flags.ISOLATED)) {
            funcSymbol.type.flags |= Flags.ISOLATED;
        }

        if (Symbols.isFlagOn(funcSymbol.type.tsymbol.flags, Flags.TRANSACTIONAL)) {
            funcSymbol.type.flags |= Flags.TRANSACTIONAL;
        }
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
    }

    @Override
    public void visit(BLangErrorType errorType) {
        if (errorType.detailType != null) {
            defineNode(errorType.detailType, env);
        }
    }

    @Override
    public void visit(BLangValueType valueType) {
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefTypeNode) {
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        defineNode(arrayType.elemtype, env);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        defineNode(constrainedType.type, env);
        defineNode(constrainedType.constraint, env);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        defineNode(streamType.constraint, env);
        defineNode(streamType.type, env);
        if (streamType.error != null) {
            defineNode(streamType.error, env);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        for (BLangType memType : tupleTypeNode.memberTypeNodes) {
            defineNode(memType, env);
        }
        if (tupleTypeNode.restParamType != null) {
            defineNode(tupleTypeNode.restParamType, env);
        }
    }

    @Override
    public void visit(BLangTableTypeNode tableTypeNode) {
        defineNode(tableTypeNode.constraint, env);
        defineNode(tableTypeNode.type, env);
        if (tableTypeNode.tableKeyTypeConstraint != null) {
            defineNode(tableTypeNode.tableKeyTypeConstraint, env);
        }
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint keyTypeConstraint) {
        defineNode(keyTypeConstraint.keyType, env);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        objectTypeNode.typeDefEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);
        resolveFields((BObjectType) objectTypeNode.symbol.type, objectTypeNode);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        SymbolEnv typeDefEnv =
                            SymbolEnv.createTypeEnv(functionTypeNode, functionTypeNode.getBType().tsymbol.scope, env);
        defineInvokableTypeNode(functionTypeNode, Flags.asMask(functionTypeNode.flagSet), typeDefEnv);
    }

    private List<BVarSymbol> defineParameters(List<BLangSimpleVariable> params, SymbolEnv typeDefEnv) {
        boolean foundDefaultableParam = false;
        boolean foundIncludedRecordParam = false;
        List<BVarSymbol> paramSymbols = new ArrayList<>();
        Set<String> requiredParamNames = new HashSet<>();
        for (BLangSimpleVariable varNode : params) {
            boolean isDefaultableParam = varNode.expr != null;
            boolean isIncludedRecordParam = varNode.flagSet.contains(Flag.INCLUDED);
            defineNode(varNode, typeDefEnv);
            if (isDefaultableParam) {
                foundDefaultableParam = true;
            } else if (isIncludedRecordParam) {
                foundIncludedRecordParam = true;
            }

            if (isDefaultableParam) {
                if (foundIncludedRecordParam) {
                    dlog.error(varNode.pos, DEFAULTABLE_PARAM_DEFINED_AFTER_INCLUDED_RECORD_PARAM);
                }
            } else if (!isIncludedRecordParam) {
                if (foundDefaultableParam) {
                    dlog.error(varNode.pos, REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM);
                } else if (foundIncludedRecordParam) {
                    dlog.error(varNode.pos, REQUIRED_PARAM_DEFINED_AFTER_INCLUDED_RECORD_PARAM);
                }
            }
            BVarSymbol symbol = varNode.symbol;
            if (varNode.expr != null) {
                symbol.flags |= Flags.OPTIONAL;
                symbol.isDefaultable = true;

                if (varNode.expr.getKind() == NodeKind.INFER_TYPEDESC_EXPR) {
                    symbol.flags |= Flags.INFER;
                }
            }
            if (varNode.flagSet.contains(Flag.INCLUDED)) {
                BType varNodeType = Types.getReferredType(varNode.getBType());
                if (varNodeType.getKind() == TypeKind.RECORD) {
                    symbol.flags |= Flags.INCLUDED;
                    LinkedHashMap<String, BField> fields = ((BRecordType) varNodeType).fields;
                    for (String fieldName : fields.keySet()) {
                        BField field = fields.get(fieldName);
                        if (field.symbol.type.tag != TypeTags.NEVER) {
                            if (!requiredParamNames.add(fieldName)) {
                                dlog.error(varNode.pos, REDECLARED_SYMBOL, fieldName);
                            }
                        }
                    }
                } else {
                    dlog.error(varNode.typeNode.pos, EXPECTED_RECORD_TYPE_AS_INCLUDED_PARAMETER);
                }
            } else {
                requiredParamNames.add(symbol.name.value);
            }
            paramSymbols.add(symbol);
        }
        return paramSymbols;
    }

    public void defineInvokableTypeNode(BLangFunctionTypeNode functionTypeNode, long flags, SymbolEnv env) {
        BInvokableTypeSymbol invokableTypeSymbol = (BInvokableTypeSymbol) functionTypeNode.getBType().tsymbol;
        List<BVarSymbol> paramSymbols = defineParameters(functionTypeNode.params, env);
        invokableTypeSymbol.params = paramSymbols;

        BType retType = null;
        BLangType retTypeNode = functionTypeNode.returnTypeNode;
        if (retTypeNode != null) {
            symResolver.resolveTypeNode(retTypeNode, env);
            invokableTypeSymbol.returnType = retTypeNode.getBType();
            retType = retTypeNode.getBType();
        }

        BType restType = null;
        BLangVariable restParam = functionTypeNode.restParam;
        if (restParam != null) {
            defineNode(restParam, env);
            invokableTypeSymbol.restParam = restParam.symbol;
            restType = restParam.getBType();
        }
        List<BType> paramTypes = new ArrayList<>();
        for (BVarSymbol paramSym : paramSymbols) {
            BType type = paramSym.type;
            paramTypes.add(type);
        }
        BInvokableType bInvokableType = (BInvokableType) invokableTypeSymbol.type;
        bInvokableType.paramTypes = paramTypes;
        bInvokableType.retType = retType;
        bInvokableType.restType = restType;
        bInvokableType.flags = flags;
        functionTypeNode.setBType(bInvokableType);

        List<BType> allConstituentTypes = new ArrayList<>(paramTypes);
        allConstituentTypes.add(restType);
        allConstituentTypes.add(retType);
        symResolver.markParameterizedType(bInvokableType, allConstituentTypes);
    }

    void defineInvokableSymbolParams(BLangInvokableNode invokableNode, BInvokableSymbol invokableSymbol,
                                             SymbolEnv invokableEnv) {
        invokableNode.clonedEnv = invokableEnv.shallowClone();
        List<BVarSymbol> paramSymbols = defineParameters(invokableNode.requiredParams, invokableEnv);
        if (!invokableNode.desugaredReturnType) {
            symResolver.resolveTypeNode(invokableNode.returnTypeNode, invokableEnv);
        }
        invokableSymbol.params = paramSymbols;
        BType retType = invokableNode.returnTypeNode.getBType();
        invokableSymbol.retType = retType;

        symResolver.validateInferTypedescParams(invokableNode.pos, invokableNode.getParameters(), retType);

        // Create function type
        List<BType> paramTypes = paramSymbols.stream()
                .map(paramSym -> paramSym.type)
                .collect(Collectors.toList());

        BInvokableTypeSymbol functionTypeSymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE,
                                                                                    invokableSymbol.flags,
                                                                                    invokableEnv.enclPkg.symbol.pkgID,
                                                                                    invokableSymbol.type,
                                                                                    invokableEnv.scope.owner,
                                                                                    invokableNode.pos, SOURCE);
        functionTypeSymbol.params = invokableSymbol.params == null ? null : new ArrayList<>(invokableSymbol.params);
        functionTypeSymbol.returnType = invokableSymbol.retType;

        BType restType = null;
        if (invokableNode.restParam != null) {
            defineNode(invokableNode.restParam, invokableEnv);
            invokableSymbol.restParam = invokableNode.restParam.symbol;
            functionTypeSymbol.restParam = invokableSymbol.restParam;
            restType = invokableSymbol.restParam.type;
        }
        invokableSymbol.type = new BInvokableType(paramTypes, restType, retType, null);
        invokableSymbol.type.tsymbol = functionTypeSymbol;
        invokableSymbol.type.tsymbol.type = invokableSymbol.type;
    }

    private void defineSymbol(Location pos, BSymbol symbol) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public void defineSymbol(Location pos, BSymbol symbol, SymbolEnv env) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    /**
     * Define a symbol that is unique only for the current scope.
     *
     * @param pos Line number information of the source file
     * @param symbol Symbol to be defines
     * @param env Environment to define the symbol
     */
    public void defineShadowedSymbol(Location pos, BSymbol symbol, SymbolEnv env) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbolInCurrentScope(pos, env, symbol, symbol.tag)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public void defineTypeNarrowedSymbol(Location location, SymbolEnv targetEnv, BVarSymbol symbol,
                                         BType type, boolean isInternal) {
        if (symbol.owner.tag == SymTag.PACKAGE) {
            // Avoid defining shadowed symbol for global vars, since the type is not narrowed.
            return;
        }

        BVarSymbol varSymbol = createVarSymbol(symbol.flags, type, symbol.name, targetEnv, symbol.pos, isInternal);
        if (type.tag == TypeTags.INVOKABLE && type.tsymbol != null) {
            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) type.tsymbol;
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) varSymbol;
            invokableSymbol.params = tsymbol.params == null ? null : new ArrayList(tsymbol.params);
            invokableSymbol.restParam = tsymbol.restParam;
            invokableSymbol.retType = tsymbol.returnType;
            invokableSymbol.flags = tsymbol.flags;
        }
        varSymbol.originalName = symbol.getOriginalName();
        varSymbol.owner = symbol.owner;
        varSymbol.originalSymbol = symbol;
        defineShadowedSymbol(location, varSymbol, targetEnv);
    }

    private void defineSymbolWithCurrentEnvOwner(Location pos, BSymbol symbol) {
        symbol.scope = new Scope(env.scope.owner);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public BVarSymbol defineVarSymbol(Location pos, Set<Flag> flagSet, BType varType, Name varName,
                                      SymbolEnv env, boolean isInternal) {
        return defineVarSymbol(pos, flagSet, varType, varName, varName, env, isInternal);
    }

    public BVarSymbol defineVarSymbol(Location pos, Set<Flag> flagSet, BType varType, Name varName, Name origName,
                                      SymbolEnv env, boolean isInternal) {
        // Create variable symbol
        Scope enclScope = env.scope;
        BVarSymbol varSymbol = createVarSymbol(flagSet, varType, varName, env, pos, isInternal);
        if (varSymbol.name == Names.EMPTY) {
            return varSymbol;
        }

        boolean isMemberOfFunc = (flagSet.contains(Flag.REQUIRED_PARAM) || flagSet.contains(Flag.DEFAULTABLE_PARAM) ||
                flagSet.contains(Flag.REST_PARAM) || flagSet.contains(Flag.INCLUDED));
        boolean considerAsMemberSymbol;
        if (isMemberOfFunc) {
            considerAsMemberSymbol = env.enclEnv.enclInvokable == null;
        } else {
            considerAsMemberSymbol = flagSet.contains(Flag.FIELD);
        }
        varSymbol.originalName = origName;
        if (considerAsMemberSymbol && !symResolver.checkForUniqueMemberSymbol(pos, env, varSymbol) ||
                !considerAsMemberSymbol && !symResolver.checkForUniqueSymbol(pos, env, varSymbol)) {
            varSymbol.type = symTable.semanticError;
            varSymbol.state = DiagnosticState.REDECLARED;
        }

        enclScope.define(varSymbol.name, varSymbol);
        return varSymbol;
    }

    public void defineExistingVarSymbolInEnv(BVarSymbol varSymbol, SymbolEnv env) {
        if (!symResolver.checkForUniqueSymbol(env, varSymbol)) {
            varSymbol.type = symTable.semanticError;
            varSymbol.state = DiagnosticState.REDECLARED;
        }
        env.scope.define(varSymbol.name, varSymbol);
    }

    public BVarSymbol createVarSymbol(Set<Flag> flagSet, BType varType, Name varName, SymbolEnv env,
                                      Location pos, boolean isInternal) {
        return createVarSymbol(Flags.asMask(flagSet), varType, varName, env, pos, isInternal);
    }

    public BVarSymbol createVarSymbol(long flags, BType type, Name varName, SymbolEnv env,
                                      Location location, boolean isInternal) {
        BVarSymbol varSymbol;
        BType varType = Types.getReferredType(type);
        if (varType.tag == TypeTags.INVOKABLE) {
            varSymbol = new BInvokableSymbol(SymTag.VARIABLE, flags, varName, env.enclPkg.symbol.pkgID, type,
                                             env.scope.owner, location, isInternal ? VIRTUAL : getOrigin(varName));
            varSymbol.kind = SymbolKind.FUNCTION;
            BInvokableTypeSymbol invokableTypeSymbol = (BInvokableTypeSymbol) varType.tsymbol;
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) varSymbol;
            invokableSymbol.params = invokableTypeSymbol.params;
            invokableSymbol.restParam = invokableTypeSymbol.restParam;
            invokableSymbol.retType = invokableTypeSymbol.returnType;
            if (varName.value.startsWith(WORKER_LAMBDA_VAR_PREFIX)) {
                varSymbol.flags |= Flags.WORKER;
            }
        } else if (Symbols.isFlagOn(flags, Flags.WORKER)) {
            varSymbol = new BWorkerSymbol(flags, varName, env.enclPkg.symbol.pkgID, type, env.scope.owner, location,
                                          isInternal ? VIRTUAL : getOrigin(varName));
            resolveAssociatedWorkerFunc((BWorkerSymbol) varSymbol, env);
        } else {
            varSymbol = new BVarSymbol(flags, varName, env.enclPkg.symbol.pkgID, type, env.scope.owner, location,
                                       isInternal ? VIRTUAL : getOrigin(varName));
            if (varType.tsymbol != null && Symbols.isFlagOn(varType.tsymbol.flags, Flags.CLIENT)) {
                varSymbol.tag = SymTag.ENDPOINT;
            }
        }
        return varSymbol;
    }

    private void resolveAssociatedWorkerFunc(BWorkerSymbol worker, SymbolEnv env) {
        LineRange workerVarPos = worker.pos.lineRange();

        for (BLangLambdaFunction lambdaFn : env.enclPkg.lambdaFunctions) {
            LineRange workerBodyPos = lambdaFn.function.pos.lineRange();
            Location targetRangePos = env.node.pos;

            // TODO: targetRangePos is null, because we create a block stmt to group the statement after a If block
            //  without en else block. Its pos is not set. Setting the pos requires the exact positions of start
            //  and end after the if block. When the pos is set we can remove this check
            if (targetRangePos == null) {
                targetRangePos = env.enclInvokable.pos;
            }

            if (worker.name.value.equals(lambdaFn.function.defaultWorkerName.value)
                    && withinRange(workerVarPos, targetRangePos.lineRange())
                    && withinRange(workerBodyPos, targetRangePos.lineRange())) {
                worker.setAssociatedFuncSymbol(lambdaFn.function.symbol);
                return;
            }
        }

        throw new IllegalStateException(
                "Matching function node not found for worker: " + worker.name.value + " at " + worker.pos);
    }

    private void defineObjectInitFunction(BLangObjectTypeNode object, SymbolEnv conEnv) {
        BLangFunction initFunction = object.initFunction;
        if (initFunction == null) {
            return;
        }

        //Set cached receiver to the init function
        initFunction.receiver = ASTBuilderUtil.createReceiver(object.pos, object.getBType());

        initFunction.attachedFunction = true;
        initFunction.flagSet.add(Flag.ATTACHED);
        defineNode(initFunction, conEnv);
    }

    private void defineClassInitFunction(BLangClassDefinition classDefinition, SymbolEnv conEnv) {
        BLangFunction initFunction = classDefinition.initFunction;
        if (initFunction == null) {
            return;
        }

        //Set cached receiver to the init function
        initFunction.receiver = ASTBuilderUtil.createReceiver(classDefinition.pos, classDefinition.getBType());

        initFunction.attachedFunction = true;
        initFunction.flagSet.add(Flag.ATTACHED);
        defineNode(initFunction, conEnv);
    }

    private void defineAttachedFunctions(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                         SymbolEnv invokableEnv, boolean isValidAttachedFunc) {
        BTypeSymbol typeSymbol = funcNode.receiver.getBType().tsymbol;

        // Check whether there exists a struct field with the same name as the function name.
        if (isValidAttachedFunc) {
            if (typeSymbol.tag == SymTag.OBJECT) {
                validateFunctionsAttachedToObject(funcNode, funcSymbol);
            } else if (typeSymbol.tag == SymTag.RECORD) {
                validateFunctionsAttachedToRecords(funcNode, funcSymbol);
            }
        }

        defineNode(funcNode.receiver, invokableEnv);
        funcSymbol.receiverSymbol = funcNode.receiver.symbol;
    }

    private void validateFunctionsAttachedToRecords(BLangFunction funcNode, BInvokableSymbol funcSymbol) {
        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        BRecordTypeSymbol recordSymbol = (BRecordTypeSymbol) funcNode.receiver.getBType().tsymbol;

        recordSymbol.initializerFunc = new BAttachedFunction(
                names.fromIdNode(funcNode.name), funcSymbol, funcType, funcNode.pos);
    }

    private void validateFunctionsAttachedToObject(BLangFunction funcNode, BInvokableSymbol funcSymbol) {

        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) funcNode.receiver.getBType().tsymbol;
        BAttachedFunction attachedFunc;
        if (funcNode.getKind() == NodeKind.RESOURCE_FUNC) {
            attachedFunc = createResourceFunction(funcNode, funcSymbol, funcType);
        } else {
            attachedFunc = new BAttachedFunction(names.fromIdNode(funcNode.name), funcSymbol, funcType, funcNode.pos);
        }

        validateRemoteFunctionAttachedToObject(funcNode, objectSymbol);
        validateResourceFunctionAttachedToObject(funcNode, objectSymbol);

        // Check whether this attached function is a object initializer.
        if (!funcNode.objInitFunction) {
            objectSymbol.attachedFuncs.add(attachedFunc);
            return;
        }

        types.validateErrorOrNilReturn(funcNode, DiagnosticErrorCode.INVALID_OBJECT_CONSTRUCTOR);
        objectSymbol.initializerFunc = attachedFunc;
    }

    private BAttachedFunction createResourceFunction(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                                     BInvokableType funcType) {
        BLangResourceFunction resourceFunction = (BLangResourceFunction) funcNode;
        Name accessor = names.fromIdNode(resourceFunction.methodName);
        List<Name> resourcePath = resourceFunction.resourcePath.stream()
                .map(names::fromIdNode)
                .collect(Collectors.toList());

        List<BVarSymbol> pathParamSymbols = resourceFunction.pathParams.stream()
                .map(p -> {
                    p.symbol.kind = SymbolKind.PATH_PARAMETER;
                    return p.symbol;
                })
                .collect(Collectors.toList());

        BVarSymbol restPathParamSym = null;
        if (resourceFunction.restPathParam != null) {
            restPathParamSym = resourceFunction.restPathParam.symbol;
            restPathParamSym.kind = SymbolKind.PATH_REST_PARAMETER;
        }

        symResolver.resolveTypeNode(resourceFunction.resourcePathType, env);
        return new BResourceFunction(names.fromIdNode(funcNode.name), funcSymbol, funcType, resourcePath,
                                     accessor, pathParamSymbols, restPathParamSym, 
                (BTupleType) resourceFunction.resourcePathType.getBType(), funcNode.pos);
    }

    private void validateRemoteFunctionAttachedToObject(BLangFunction funcNode, BObjectTypeSymbol objectSymbol) {
        if (!Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.REMOTE)) {
            return;
        }
        funcNode.symbol.flags |= Flags.REMOTE;
        funcNode.symbol.flags |= Flags.PUBLIC;

        if (!isNetworkQualified(objectSymbol)) {
            this.dlog.error(funcNode.pos, DiagnosticErrorCode.REMOTE_FUNCTION_IN_NON_NETWORK_OBJECT);
        }
    }

    private boolean isNetworkQualified(BObjectTypeSymbol objectSymbol) {
        return Symbols.isFlagOn(objectSymbol.flags, Flags.CLIENT)
                || Symbols.isFlagOn(objectSymbol.flags, Flags.SERVICE);
    }

    private void validateResourceFunctionAttachedToObject(BLangFunction funcNode, BObjectTypeSymbol objectSymbol) {
        if (!Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.RESOURCE)) {
            return;
        }
        funcNode.symbol.flags |= Flags.RESOURCE;
        funcNode.symbol.flags |= Flags.PUBLIC;

        if (!isNetworkQualified(objectSymbol)) {
            this.dlog.error(funcNode.pos, 
                    DiagnosticErrorCode.RESOURCE_METHODS_ARE_ONLY_ALLOWED_IN_SERVICE_OR_CLIENT_OBJECTS);
        }
    }

    private StatementNode createAssignmentStmt(BLangSimpleVariable variable, BVarSymbol varSym, BSymbol fieldVar) {
        //Create LHS reference variable
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = variable.pos;
        varRef.variableName = (BLangIdentifier) createIdentifier(fieldVar.name.getValue());
        varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        varRef.symbol = fieldVar;
        varRef.setBType(fieldVar.type);

        //Create RHS variable reference
        BLangSimpleVarRef exprVar = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        exprVar.pos = variable.pos;
        exprVar.variableName = (BLangIdentifier) createIdentifier(varSym.name.getValue());
        exprVar.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        exprVar.symbol = varSym;
        exprVar.setBType(varSym.type);

        //Create assignment statement
        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = exprVar;
        assignmentStmt.pos = variable.pos;
        assignmentStmt.setVariable(varRef);
        return assignmentStmt;
    }

    private IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    private boolean validateFuncReceiver(BLangFunction funcNode) {
        if (funcNode.receiver == null) {
            return true;
        }

        if (funcNode.receiver.getBType() == null) {
            funcNode.receiver.setBType(symResolver.resolveTypeNode(funcNode.receiver.typeNode, env));
        }
        if (funcNode.receiver.getBType().tag == TypeTags.SEMANTIC_ERROR) {
            return true;
        }

        BType receiverType = funcNode.receiver.getBType();
        BType referredReceiverType = Types.getReferredType(receiverType);
        if (referredReceiverType.tag == TypeTags.OBJECT
                && !this.env.enclPkg.symbol.pkgID.equals(receiverType.tsymbol.pkgID)) {
            dlog.error(funcNode.receiver.pos, DiagnosticErrorCode.FUNC_DEFINED_ON_NON_LOCAL_TYPE,
                       funcNode.name.value, receiverType.toString());
            return false;
        }
        return true;
    }

    private Name getFuncSymbolName(BLangFunction funcNode) {
        if (funcNode.receiver != null) {
            return names.fromString(Symbols.getAttachedFuncSymbolName(
                    funcNode.receiver.getBType().tsymbol.name.value, funcNode.name.value));
        }
        return names.fromIdNode(funcNode.name);
    }

    private Name getFuncSymbolOriginalName(BLangFunction funcNode) {
        return names.originalNameFromIdNode(funcNode.name);
    }

    private Name getFieldSymbolName(BLangSimpleVariable receiver, BLangSimpleVariable variable) {
        return names.fromString(Symbols.getAttachedFuncSymbolName(
                receiver.getBType().tsymbol.name.value, variable.name.value));
    }

    private MarkdownDocAttachment getMarkdownDocAttachment(BLangMarkdownDocumentation docNode) {
        if (docNode == null) {
            return new MarkdownDocAttachment(0);
        }
        MarkdownDocAttachment docAttachment = new MarkdownDocAttachment(docNode.getParameters().size());
        docAttachment.description = docNode.getDocumentation();

        for (BLangMarkdownParameterDocumentation p : docNode.getParameters()) {
            docAttachment.parameters.add(new MarkdownDocAttachment.Parameter(p.parameterName.value,
                                                                             p.getParameterDocumentation()));
        }

        docAttachment.returnValueDescription = docNode.getReturnParameterDocumentation();
        BLangMarkDownDeprecationDocumentation deprecatedDocs = docNode.getDeprecationDocumentation();

        if (deprecatedDocs == null) {
            return docAttachment;
        }

        docAttachment.deprecatedDocumentation = deprecatedDocs.getDocumentation();

        BLangMarkDownDeprecatedParametersDocumentation deprecatedParamsDocs =
                docNode.getDeprecatedParametersDocumentation();

        if (deprecatedParamsDocs == null) {
            return docAttachment;
        }

        for (BLangMarkdownParameterDocumentation param : deprecatedParamsDocs.getParameters()) {
            docAttachment.deprecatedParams.add(
                    new MarkdownDocAttachment.Parameter(param.parameterName.value, param.getParameterDocumentation()));
        }

        return docAttachment;
    }

    private void resolveIncludedFields(BLangStructureTypeNode structureTypeNode) {
        SymbolEnv typeDefEnv = structureTypeNode.typeDefEnv;
        List<BLangType> typeRefs = structureTypeNode.typeRefs;
        int typeRefSize = typeRefs.size();
        Set<BSymbol> referencedTypes = new HashSet<>(typeRefSize); // provide size to prevent rehashing
        List<BLangType> invalidTypeRefs = new ArrayList<>(typeRefSize); // provide size to prevent dynamic growing
        // Get the inherited fields from the type references

        Map<String, BLangSimpleVariable> fieldNames = new HashMap<>(structureTypeNode.fields.size());
        for (BLangSimpleVariable fieldVariable : structureTypeNode.fields) {
            fieldNames.put(fieldVariable.name.value, fieldVariable);
        }

        for (BLangType typeRef : typeRefs) {
            BType referredType = symResolver.resolveTypeNode(typeRef, typeDefEnv);
            referredType = Types.getReferredType(referredType);
            if (referredType == symTable.semanticError) {
                continue;
            }
            if (referredType.tag != TypeTags.RECORD) {
                continue;
            }
            var fields = ((BStructureType) referredType).fields.values();
            for (BField field : fields) {
                BType type = field.type;
                BLangTypeDefinition typeDefinition = typeToTypeDef.get(type);
                if (typeDefinition != null && typeDefinition.typeNode != null &&
                        typeDefinition.typeNode.getKind() == RECORD_TYPE) {
                    defineReferencedFieldsOfRecordTypeDef(typeDefinition);
                }
            }
        }

        structureTypeNode.includedFields = typeRefs.stream().flatMap(typeRef -> {
            BType referredType = symResolver.resolveTypeNode(typeRef, typeDefEnv);
            referredType = Types.getReferredType(referredType);
            if (referredType == symTable.semanticError) {
                return Stream.empty();
            }

            // Check for duplicate type references
            if (!referencedTypes.add(referredType.tsymbol)) {
                dlog.error(typeRef.pos, DiagnosticErrorCode.REDECLARED_TYPE_REFERENCE, typeRef);
                return Stream.empty();
            }

            int referredTypeTag = referredType.tag;
            if (Types.getReferredType(structureTypeNode.getBType()).tag == TypeTags.OBJECT) {
                if (referredTypeTag != TypeTags.OBJECT) {
                    DiagnosticErrorCode errorCode = DiagnosticErrorCode.INCOMPATIBLE_TYPE_REFERENCE;

                    if (referredTypeTag == TypeTags.INTERSECTION &&
                            isReadOnlyAndObjectIntersection((BIntersectionType) referredType)) {
                        errorCode = DiagnosticErrorCode.INVALID_READ_ONLY_TYPEDESC_INCLUSION_IN_OBJECT_TYPEDESC;
                    }

                    dlog.error(typeRef.pos, errorCode, typeRef);
                    invalidTypeRefs.add(typeRef);
                    return Stream.empty();
                }

                BObjectType objectType = (BObjectType) referredType;
                if (!structureTypeNode.symbol.pkgID.equals(referredType.tsymbol.pkgID)) {
                    for (BField field : objectType.fields.values()) {
                        if (!Symbols.isPublic(field.symbol)) {
                            dlog.error(typeRef.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS,
                                       typeRef);
                            invalidTypeRefs.add(typeRef);
                            return Stream.empty();
                        }
                    }

                    for (BAttachedFunction func : ((BObjectTypeSymbol) objectType.tsymbol).attachedFuncs) {
                        if (!Symbols.isPublic(func.symbol)) {
                            dlog.error(typeRef.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS,
                                       typeRef);
                            invalidTypeRefs.add(typeRef);
                            return Stream.empty();
                        }
                    }
                }
            }

            if (structureTypeNode.getBType().tag == TypeTags.RECORD && referredTypeTag != TypeTags.RECORD) {
                dlog.error(typeRef.pos, DiagnosticErrorCode.INCOMPATIBLE_RECORD_TYPE_REFERENCE, typeRef);
                invalidTypeRefs.add(typeRef);
                return Stream.empty();
            }

            // Here it is assumed that all the fields of the referenced types are resolved
            // by the time we reach here. It is achieved by ordering the typeDefs according
            // to the precedence.
            // Default values of fields are not inherited.
            return ((BStructureType) referredType).fields.values().stream().filter(f -> {
                if (fieldNames.containsKey(f.name.value)) {
                    BLangSimpleVariable existingVariable = fieldNames.get(f.name.value);
                    if (existingVariable.flagSet.contains(Flag.PUBLIC) !=
                            Symbols.isFlagOn(f.symbol.flags, Flags.PUBLIC)) {
                        dlog.error(existingVariable.pos,
                                DiagnosticErrorCode.MISMATCHED_VISIBILITY_QUALIFIERS_IN_OBJECT_FIELD,
                                existingVariable.name.value);
                    }
                    return false;
                }
                return true;
            }).map(field -> {
                BLangSimpleVariable var = ASTBuilderUtil.createVariable(typeRef.pos, field.name.value, field.type);
                var.flagSet = field.symbol.getFlags();
                return var;
            });
        }).collect(Collectors.toList());
        structureTypeNode.typeRefs.removeAll(invalidTypeRefs);
    }

    private void defineReferencedFunction(Location location, Set<Flag> flagSet, SymbolEnv objEnv,
                                          BLangType typeRef, BAttachedFunction referencedFunc,
                                          Set<String> includedFunctionNames, BSymbol typeDefSymbol,
                                          List<BLangFunction> declaredFunctions, boolean isInternal) {
        typeDefSymbol = typeDefSymbol.tag == SymTag.TYPE_DEF ? typeDefSymbol.type.tsymbol : typeDefSymbol;
        String referencedFuncName = referencedFunc.funcName.value;
        Name funcName = names.fromString(
                Symbols.getAttachedFuncSymbolName(typeDefSymbol.name.value, referencedFuncName));
        BSymbol matchingObjFuncSym = symResolver.lookupSymbolInMainSpace(objEnv, funcName);

        BInvokableSymbol referencedFuncSymbol = referencedFunc.symbol;
        if (matchingObjFuncSym != symTable.notFoundSymbol) {
            if (!includedFunctionNames.add(referencedFuncName)) {
                dlog.error(typeRef.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, referencedFuncName);
                return;
            }

            if (!hasSameFunctionSignature((BInvokableSymbol) matchingObjFuncSym, referencedFuncSymbol)) {
                BLangFunction matchingFunc = findFunctionBySymbol(declaredFunctions, matchingObjFuncSym);
                Location methodPos = matchingFunc != null ? matchingFunc.pos : typeRef.pos;
                dlog.error(methodPos, DiagnosticErrorCode.REFERRED_FUNCTION_SIGNATURE_MISMATCH,
                           getCompleteFunctionSignature(referencedFuncSymbol),
                           getCompleteFunctionSignature((BInvokableSymbol) matchingObjFuncSym));
            }

            if (Symbols.isFunctionDeclaration(matchingObjFuncSym) && Symbols.isFunctionDeclaration(
                    referencedFuncSymbol) && !types.isAssignable(matchingObjFuncSym.type, referencedFunc.type)) {
                BLangFunction matchingFunc = findFunctionBySymbol(declaredFunctions, matchingObjFuncSym);
                Location methodPos = matchingFunc != null ? matchingFunc.pos : typeRef.pos;
                dlog.error(methodPos, DiagnosticErrorCode.REDECLARED_FUNCTION_FROM_TYPE_REFERENCE,
                        referencedFunc.funcName, typeRef);
            }
            return;
        }

        if (Symbols.isPrivate(referencedFuncSymbol)) {
            // we should not copy private functions
            return;
        }

        // If not, define the function symbol within the object.
        // Take a copy of the symbol, with the new name, and the package ID same as the object type.
        BInvokableSymbol funcSymbol = ASTBuilderUtil.duplicateFunctionDeclarationSymbol(referencedFuncSymbol,
                typeDefSymbol, funcName, typeDefSymbol.pkgID, typeRef.pos, getOrigin(funcName));
        defineSymbol(typeRef.pos, funcSymbol, objEnv);

        // Create and define the parameters and receiver. This should be done after defining the function symbol.
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(null, funcSymbol.scope, objEnv);
        funcSymbol.params.forEach(param -> defineSymbol(typeRef.pos, param, funcEnv));
        if (funcSymbol.restParam != null) {
            defineSymbol(typeRef.pos, funcSymbol.restParam, funcEnv);
        }
        funcSymbol.receiverSymbol =
                defineVarSymbol(location, flagSet, typeDefSymbol.type, Names.SELF, funcEnv, isInternal);

        // Cache the function symbol.
        BAttachedFunction attachedFunc;
        if (referencedFunc instanceof BResourceFunction) {
            BResourceFunction resourceFunction = (BResourceFunction) referencedFunc;
            attachedFunc = new BResourceFunction(referencedFunc.funcName,
                    funcSymbol, (BInvokableType) funcSymbol.type, resourceFunction.resourcePath,
                    resourceFunction.accessor, resourceFunction.pathParams, resourceFunction.restPathParam,
                    resourceFunction.resourcePathType, referencedFunc.pos);
        } else {
            attachedFunc = new BAttachedFunction(referencedFunc.funcName, funcSymbol, (BInvokableType) funcSymbol.type,
                    referencedFunc.pos);
        }

        ((BObjectTypeSymbol) typeDefSymbol).attachedFuncs.add(attachedFunc);
        ((BObjectTypeSymbol) typeDefSymbol).referencedFunctions.add(attachedFunc);
    }

    private BLangFunction findFunctionBySymbol(List<BLangFunction> declaredFunctions, BSymbol symbol) {
        for (BLangFunction fn : declaredFunctions) {
            if (fn.symbol == symbol) {
                return fn;
            }
        }
        return null;
    }

    private boolean hasSameFunctionSignature(BInvokableSymbol attachedFuncSym, BInvokableSymbol referencedFuncSym) {
        if (!hasSameVisibilityModifier(referencedFuncSym.flags, attachedFuncSym.flags)) {
            return false;
        }

        if (!types.isAssignable(attachedFuncSym.type, referencedFuncSym.type)) {
            return false;
        }

        List<BVarSymbol> params = referencedFuncSym.params;
        for (int i = 0; i < params.size(); i++) {
            BVarSymbol referencedFuncParam = params.get(i);
            BVarSymbol attachedFuncParam = attachedFuncSym.params.get(i);
            if (!referencedFuncParam.name.value.equals(attachedFuncParam.name.value) ||
                    !hasSameVisibilityModifier(referencedFuncParam.flags, attachedFuncParam.flags)) {
                return false;
            }
        }

        if (referencedFuncSym.restParam != null && attachedFuncSym.restParam != null) {
            return referencedFuncSym.restParam.name.value.equals(attachedFuncSym.restParam.name.value);
        }

        return referencedFuncSym.restParam == null && attachedFuncSym.restParam == null;
    }

    private boolean hasSameVisibilityModifier(long flags1, long flags2) {
        var xorOfFlags = flags1 ^ flags2;
        return ((xorOfFlags & Flags.PUBLIC) != Flags.PUBLIC) && ((xorOfFlags & Flags.PRIVATE) != Flags.PRIVATE);
    }

    private String getCompleteFunctionSignature(BInvokableSymbol funcSymbol) {
        StringBuilder signatureBuilder = new StringBuilder();
        StringJoiner paramListBuilder = new StringJoiner(", ", "(", ")");

        String visibilityModifier = "";
        if (Symbols.isPublic(funcSymbol)) {
            visibilityModifier = "public ";
        } else if (Symbols.isPrivate(funcSymbol)) {
            visibilityModifier = "private ";
        }

        signatureBuilder.append(visibilityModifier).append("function ")
                .append(funcSymbol.name.value.split("\\.")[1]);

        funcSymbol.params.forEach(param -> paramListBuilder.add(
                (Symbols.isPublic(param) ? "public " : "") + param.type.toString() + " " + param.name.value));

        if (funcSymbol.restParam != null) {
            paramListBuilder.add(((BArrayType) funcSymbol.restParam.type).eType.toString() + "... " +
                                         funcSymbol.restParam.name.value);
        }

        signatureBuilder.append(paramListBuilder.toString());

        if (funcSymbol.retType != symTable.nilType) {
            signatureBuilder.append(" returns ").append(funcSymbol.retType.toString());
        }

        return signatureBuilder.toString();
    }

    private BPackageSymbol dupPackageSymbolAndSetCompUnit(BPackageSymbol originalSymbol, Name compUnit) {
        BPackageSymbol copy = new BPackageSymbol(originalSymbol.pkgID, originalSymbol.owner, originalSymbol.flags,
                                                 originalSymbol.pos, originalSymbol.origin);
        copy.initFunctionSymbol = originalSymbol.initFunctionSymbol;
        copy.startFunctionSymbol = originalSymbol.startFunctionSymbol;
        copy.stopFunctionSymbol = originalSymbol.stopFunctionSymbol;
        copy.testInitFunctionSymbol = originalSymbol.testInitFunctionSymbol;
        copy.testStartFunctionSymbol = originalSymbol.testStartFunctionSymbol;
        copy.testStopFunctionSymbol = originalSymbol.testStopFunctionSymbol;
        copy.packageFile = originalSymbol.packageFile;
        copy.compiledPackage = originalSymbol.compiledPackage;
        copy.entryPointExists = originalSymbol.entryPointExists;
        copy.scope = originalSymbol.scope;
        copy.owner = originalSymbol.owner;
        copy.compUnit = compUnit;
        copy.importPrefix = originalSymbol.importPrefix;
        return copy;
    }

    private boolean isSameImport(BLangImportPackage importPkgNode, BPackageSymbol importSymbol) {
        if (!importPkgNode.orgName.value.equals(importSymbol.pkgID.orgName.value)) {
            return false;
        }

        BLangIdentifier pkgName = importPkgNode.pkgNameComps.get(importPkgNode.pkgNameComps.size() - 1);
        return pkgName.value.equals(importSymbol.pkgID.name.value);
    }

    private void setTypeFromLambdaExpr(BLangVariable variable) {
        BLangFunction function = ((BLangLambdaFunction) variable.expr).function;
        BInvokableType invokableType = (BInvokableType) function.symbol.type;
        if (function.flagSet.contains(Flag.ISOLATED)) {
            invokableType.flags |= Flags.ISOLATED;
            invokableType.tsymbol.flags |= Flags.ISOLATED;
        }

        if (function.flagSet.contains(Flag.TRANSACTIONAL)) {
            invokableType.flags |= Flags.TRANSACTIONAL;
            invokableType.tsymbol.flags |= Flags.TRANSACTIONAL;
        }

        variable.setBType(invokableType);
    }

    private SymbolOrigin getOrigin(Name name, Set<Flag> flags) {
        if ((flags.contains(Flag.ANONYMOUS) && (flags.contains(Flag.SERVICE) || flags.contains(Flag.CLASS)))
                || missingNodesHelper.isMissingNode(name)) {
            return VIRTUAL;
        }
        return SOURCE;
    }

    private SymbolOrigin getOrigin(Name name) {
        return getOrigin(name.value);
    }

    private SymbolOrigin getOrigin(String name) {
        if (missingNodesHelper.isMissingNode(name)) {
            return VIRTUAL;
        }
        return SOURCE;
    }

    private boolean isInvalidIncludedTypeInClass(BType includedType) {
        int tag = includedType.tag;

        if (tag == TypeTags.OBJECT) {
            return false;
        }

        if (tag != TypeTags.INTERSECTION) {
            return true;
        }

        for (BType constituentType : ((BIntersectionType) includedType).getConstituentTypes()) {
            int constituentTypeTag = Types.getReferredType(constituentType).tag;

            if (constituentTypeTag != TypeTags.OBJECT && constituentTypeTag != TypeTags.READONLY) {
                return true;
            }
        }
        return false;
    }

    private boolean isImmutable(BObjectType objectType) {
        if (Symbols.isFlagOn(objectType.flags, Flags.READONLY)) {
            return true;
        }

        Collection<BField> fields = objectType.fields.values();
        if (fields.isEmpty()) {
            return false;
        }

        for (BField field : fields) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.FINAL) ||
                    !Symbols.isFlagOn(field.type.flags, Flags.READONLY)) {
                return false;
            }
        }

        return true;
    }

    private boolean isReadOnlyAndObjectIntersection(BIntersectionType referredType) {
        BType effectiveType = referredType.effectiveType;

        if (effectiveType.tag != TypeTags.OBJECT || !Symbols.isFlagOn(effectiveType.flags, Flags.READONLY)) {
            return false;
        }

        for (BType constituentType : referredType.getConstituentTypes()) {
            if (constituentType.tag == TypeTags.READONLY) {
                return true;
            }
        }
        return false;
    }

    private boolean withinRange(LineRange srcRange, LineRange targetRange) {
        int startLine = srcRange.startLine().line();
        int startOffset = srcRange.startLine().offset();
        int endLine = srcRange.endLine().line();
        int endOffset = srcRange.endLine().offset();

        int enclStartLine = targetRange.startLine().line();
        int enclEndLine = targetRange.endLine().line();
        int enclStartOffset = targetRange.startLine().offset();
        int enclEndOffset = targetRange.endLine().offset();

        return targetRange.filePath().equals(srcRange.filePath())
                && (startLine == enclStartLine && startOffset >= enclStartOffset || startLine > enclStartLine)
                && (endLine == enclEndLine && endOffset <= enclEndOffset || endLine < enclEndLine);
    }

    /**
     * Holds imports that are resolved and unresolved.
     */
    public static class ImportResolveHolder {
        public BLangImportPackage resolved;
        public List<BLangImportPackage> unresolved;

        public ImportResolveHolder() {
            this.unresolved = new ArrayList<>();
        }

        public ImportResolveHolder(BLangImportPackage resolved) {
            this.resolved = resolved;
            this.unresolved = new ArrayList<>();
        }
    }

    /**
     * Used to store location data for encountered unknown types in `checkErrors` method.
     *
     * @since 0.985.0
     */
    class LocationData {

        private String name;
        private int row;
        private int column;

        LocationData(String name, int row, int column) {
            this.name = name;
            this.row = row;
            this.column = column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LocationData that = (LocationData) o;
            return row == that.row &&
                    column == that.column &&
                    name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, row, column);
        }
    }
}
