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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLAttributeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
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
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import static org.ballerinalang.model.elements.PackageID.FUTURE;
import static org.ballerinalang.model.elements.PackageID.INT;
import static org.ballerinalang.model.elements.PackageID.MAP;
import static org.ballerinalang.model.elements.PackageID.OBJECT;
import static org.ballerinalang.model.elements.PackageID.QUERY;
import static org.ballerinalang.model.elements.PackageID.STREAM;
import static org.ballerinalang.model.elements.PackageID.STRING;
import static org.ballerinalang.model.elements.PackageID.TABLE;
import static org.ballerinalang.model.elements.PackageID.TYPEDESC;
import static org.ballerinalang.model.elements.PackageID.VALUE;
import static org.ballerinalang.model.elements.PackageID.XML;
import static org.ballerinalang.model.tree.NodeKind.IMPORT;
import static org.ballerinalang.util.diagnostic.DiagnosticCode.REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM;

/**
 * @since 0.94
 */
public class SymbolEnter extends BLangNodeVisitor {

    private static final CompilerContext.Key<SymbolEnter> SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    private final PackageLoader pkgLoader;
    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final BLangDiagnosticLogHelper dlog;
    private final Types types;
    private final SourceDirectory sourceDirectory;
    private List<TypeDefinition> unresolvedTypes;
    private HashSet<LocationData> unknownTypeRefs;
    private List<PackageID> importedPackages;
    private int typePrecedence;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private BLangAnonymousModelHelper anonymousModelHelper;

    private SymbolEnv env;

    private static final String DEPRECATION_ANNOTATION = "deprecated";

    public static SymbolEnter getInstance(CompilerContext context) {
        SymbolEnter symbolEnter = context.get(SYMBOL_ENTER_KEY);
        if (symbolEnter == null) {
            symbolEnter = new SymbolEnter(context);
        }

        return symbolEnter;
    }

    public SymbolEnter(CompilerContext context) {
        context.put(SYMBOL_ENTER_KEY, this);

        this.pkgLoader = PackageLoader.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.types = Types.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.sourceDirectory = context.get(SourceDirectory.class);
        this.importedPackages = new ArrayList<>();
        this.unknownTypeRefs = new HashSet<>();
    }

    public BLangPackage definePackage(BLangPackage pkgNode) {
        populatePackageNode(pkgNode);
        defineNode(pkgNode, this.symTable.pkgEnvMap.get(symTable.langAnnotationModuleSymbol));
        return pkgNode;
    }

    public void defineNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = prevEnv;
    }

    public BLangPackage defineTestablePackage(BLangTestablePackage pkgNode, SymbolEnv env,
                                              List<BLangImportPackage> enclPkgImports) {
        populatePackageNode(pkgNode, enclPkgImports);
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
            pkgSymbol = Symbols.createPackageSymbol(pkgNode.packageID, this.symTable, Flags.asMask(pkgNode.flagSet));
        } else {
            pkgSymbol = Symbols.createPackageSymbol(pkgNode.packageID, this.symTable);
        }
        if (PackageID.isLangLibPackageID(pkgSymbol.pkgID)) {
            populateLangLibInSymTable(pkgSymbol);
        }

        pkgNode.symbol = pkgSymbol;
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(pkgNode, pkgSymbol.scope, this.env);
        this.symTable.pkgEnvMap.put(pkgSymbol, pkgEnv);

        // Add the current package node's ID to the imported package list. This is used to identify cyclic module
        // imports.
        importedPackages.add(pkgNode.packageID);

        defineConstructs(pkgNode, pkgEnv);
        pkgNode.getTestablePkgs().forEach(testablePackage -> defineTestablePackage(testablePackage, pkgEnv,
                                                                                   pkgNode.imports));
        pkgNode.completedPhases.add(CompilerPhase.DEFINE);

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
                        dlog.error(unresolvedPkg.pos, DiagnosticCode.REDECLARED_IMPORT_MODULE,
                                unresolvedPkg.getQualifiedPackageName());
                    } else {
                        dlog.error(unresolvedPkg.pos, DiagnosticCode.REDECLARED_SYMBOL, unresolvedPkgAlias);
                    }
                    continue;
                }

                unresolvedPkg.symbol = pkgSymbol;
                // and define it in the current package scope
                BPackageSymbol symbol = duplicatePackagSymbol(pkgSymbol);
                symbol.compUnit = names.fromIdNode(unresolvedPkg.compUnit);
                symbol.scope = pkgSymbol.scope;
                unresolvedPkg.symbol = symbol;
                pkgEnv.scope.define(unresolvedPkgAlias, symbol);
            }
        }

        // Define type definitions.
        this.typePrecedence = 0;

        // Treat constants and type definitions in the same manner, since constants can be used as
        // types. Also, there can be references between constant and type definitions in both ways.
        // Thus visit them according to the precedence.
        List<TypeDefinition> typDefs = new ArrayList<>();
        pkgNode.constants.forEach(constant -> typDefs.add(constant));
        pkgNode.typeDefinitions.forEach(typDef -> typDefs.add(typDef));
        defineTypeNodes(typDefs, pkgEnv);

        for (BLangSimpleVariable variable : pkgNode.globalVars) {
            if (variable.expr != null && variable.expr.getKind() == NodeKind.LAMBDA && variable.isDeclaredWithVar) {
                resolveAndSetFunctionTypeFromRHSLambda(variable, pkgEnv);
            }
        }

        // Enabled logging errors after type def visit.
        // TODO: Do this in a cleaner way
        pkgEnv.logErrors = true;

        // Sort type definitions with precedence, before defining their members.
        pkgNode.typeDefinitions.sort(Comparator.comparing(t -> t.precedence));

        // Define error details
        defineErrorDetails(pkgNode.typeDefinitions, pkgEnv);

        // Define type def fields (if any)
        defineFields(pkgNode.typeDefinitions, pkgEnv);

        // Define type def members (if any)
        defineMembers(pkgNode.typeDefinitions, pkgEnv);

        // Define service and resource nodes.
        pkgNode.services.forEach(service -> defineNode(service, pkgEnv));

        // Define function nodes.
        pkgNode.functions.forEach(func -> defineNode(func, pkgEnv));

        // Define annotation nodes.
        pkgNode.annotations.forEach(annot -> defineNode(annot, pkgEnv));

        pkgNode.globalVars.forEach(var -> defineNode(var, pkgEnv));

        // Update globalVar for endpoints.
        pkgNode.globalVars.stream().filter(var -> var.symbol.type.tsymbol != null && Symbols
                .isFlagOn(var.symbol.type.tsymbol.flags, Flags.CLIENT)).map(varNode -> varNode.symbol)
                .forEach(varSymbol -> varSymbol.tag = SymTag.ENDPOINT);
    }

    public void visit(BLangAnnotation annotationNode) {
        BAnnotationSymbol annotationSymbol = Symbols.createAnnotationSymbol(Flags.asMask(annotationNode.flagSet),
                                                                            annotationNode.getAttachPoints(),
                                                                            names.fromIdNode(annotationNode.name),
                                                                            env.enclPkg.symbol.pkgID, null,
                                                                            env.scope.owner);
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
            annotationSymbol.attachedType = type.tsymbol;
            if (!isValidAnnotationType(type)) {
                dlog.error(annotTypeNode.pos, DiagnosticCode.ANNOTATION_INVALID_TYPE, type);
            }

            if (annotationNode.flagSet.contains(Flag.CONSTANT) && !type.isAnydata()) {
                dlog.error(annotTypeNode.pos, DiagnosticCode.ANNOTATION_INVALID_CONST_TYPE, type);
            }
        }

        if (!annotationNode.flagSet.contains(Flag.CONSTANT) &&
                annotationNode.getAttachPoints().stream().anyMatch(attachPoint -> attachPoint.source)) {
            dlog.error(annotationNode.pos, DiagnosticCode.ANNOTATION_REQUIRES_CONST);
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
                    dlog.error(importPkgNode.pos, DiagnosticCode.REDECLARED_IMPORT_MODULE,
                            importPkgNode.getQualifiedPackageName());
                } else {
                    dlog.error(importPkgNode.pos, DiagnosticCode.REDECLARED_SYMBOL, pkgAlias);
                }
                return;
            }
        }

        // TODO Clean this code up. Can we move the this to BLangPackageBuilder class
        // Create import package symbol
        Name orgName;
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
                String pkgName = importPkgNode.getPackageName().stream()
                        .map(id -> id.value)
                        .collect(Collectors.joining("."));
                if (this.sourceDirectory.getSourcePackageNames().contains(pkgName)
                        && orgName.value.equals(enclPackageID.orgName.value)) {
                    version = enclPackageID.version;
                } else {
                    version = Names.EMPTY;
                }
            }
        } else {
            orgName = enclPackageID.orgName;
            version = (Names.DEFAULT_VERSION.equals(enclPackageID.version)) ? Names.EMPTY : enclPackageID.version;
        }

        List<Name> nameComps = importPkgNode.pkgNameComps.stream()
                .map(identifier -> names.fromIdNode(identifier))
                .collect(Collectors.toList());

        PackageID pkgId = new PackageID(orgName, nameComps, version);

        // Built-in Annotation module is not allowed to import.
        if (pkgId.equals(PackageID.ANNOTATIONS) || pkgId.equals(PackageID.INTERNAL) || pkgId.equals(PackageID.QUERY)) {
            // Only peer lang.* modules able to see these two modules.
            // Spec allows to annotation model to be imported, but implementation not support this.
            if (!(enclPackageID.orgName.equals(Names.BALLERINA_ORG)
                    && enclPackageID.name.value.startsWith(Names.LANG.value))) {
                dlog.error(importPkgNode.pos, DiagnosticCode.MODULE_NOT_FOUND,
                        importPkgNode.getQualifiedPackageName());
                return;
            }
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
            dlog.error(importPkgNode.pos, DiagnosticCode.CYCLIC_MODULE_IMPORTS_DETECTED, stringBuilder.toString());
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
            dlog.error(importPkgNode.pos, DiagnosticCode.CYCLIC_MODULE_IMPORTS_DETECTED, stringBuilder.toString());
            return;
        }

        BPackageSymbol pkgSymbol = pkgLoader.loadPackageSymbol(pkgId, enclPackageID, this.env.enclPkg.repos);
        if (pkgSymbol == null) {
            dlog.error(importPkgNode.pos, DiagnosticCode.MODULE_NOT_FOUND,
                    importPkgNode.getQualifiedPackageName());
            return;
        }

        List<BPackageSymbol> imports = ((BPackageSymbol) this.env.scope.owner).imports;
        if (!imports.contains(pkgSymbol)) {
            imports.add(pkgSymbol);
        }

        // get a copy of the package symbol, add compilation unit info to it,
        // and define it in the current package scope
        BPackageSymbol symbol = duplicatePackagSymbol(pkgSymbol);
        symbol.compUnit = names.fromIdNode(importPkgNode.compUnit);
        symbol.scope = pkgSymbol.scope;
        importPkgNode.symbol = symbol;
        this.env.scope.define(pkgAlias, symbol);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        String nsURI = (String) ((BLangLiteral) xmlnsNode.namespaceURI).value;

        if (!nullOrEmpty(xmlnsNode.prefix.value) && nsURI.isEmpty()) {
            dlog.error(xmlnsNode.pos, DiagnosticCode.INVALID_NAMESPACE_DECLARATION, xmlnsNode.prefix);
        }

        // set the prefix of the default namespace
        if (xmlnsNode.prefix.value == null) {
            xmlnsNode.prefix.value = XMLConstants.DEFAULT_NS_PREFIX;
        }

        BXMLNSSymbol xmlnsSymbol = Symbols.createXMLNSSymbol(names.fromIdNode(xmlnsNode.prefix), nsURI,
                env.enclPkg.symbol.pkgID, env.scope.owner);
        xmlnsNode.symbol = xmlnsSymbol;

        // First check for package-imports with the same alias.
        // Here we do not check for owner equality, since package import is always at the package
        // level, but the namespace declaration can be at any level.
        BSymbol foundSym = symResolver.lookupSymbolInPrefixSpace(env, xmlnsSymbol.name);
        if ((foundSym.tag & SymTag.PACKAGE) != SymTag.PACKAGE) {
            foundSym = symTable.notFoundSymbol;
        }
        if (foundSym != symTable.notFoundSymbol) {
            dlog.error(xmlnsNode.pos, DiagnosticCode.REDECLARED_SYMBOL, xmlnsSymbol.name);
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

    private void defineTypeNodes(List<? extends TypeDefinition> typeDefs, SymbolEnv env) {
        if (typeDefs.size() == 0) {
            return;
        }

        this.unresolvedTypes = new ArrayList<>();
        for (TypeDefinition typeDef : typeDefs) {
            defineNode((BLangNode) typeDef, env);
        }

        if (typeDefs.size() <= unresolvedTypes.size()) {
            // This situation can occur due to either a cyclic dependency or at least one of member types in type
            // definition node cannot be resolved. So we iterate through each node recursively looking for cyclic
            // dependencies or undefined types in type node.


            for (TypeDefinition unresolvedType : unresolvedTypes) {
                // We need to keep track of all visited types to print cyclic dependency.
                Stack<String> references = new Stack<>();
                references.push(unresolvedType.getName().getValue());
                checkErrors(unresolvedType, (BLangType) unresolvedType.getTypeNode(), references);
            }

            unresolvedTypes.forEach(type -> defineNode((BLangNode) type, env));
            return;
        }
        defineTypeNodes(unresolvedTypes, env);
    }

    private void checkErrors(TypeDefinition unresolvedType, BLangType currentTypeNode, Stack<String> visitedNodes) {
        String unresolvedTypeNodeName = unresolvedType.getName().getValue();

        // Check errors in the type definition.
        List<BLangType> memberTypeNodes;
        switch (currentTypeNode.getKind()) {
            case ARRAY_TYPE:
                checkErrors(unresolvedType, ((BLangArrayType) currentTypeNode).elemtype, visitedNodes);
                break;
            case UNION_TYPE_NODE:
                // If the current type node is a union type node, we need to check all member nodes.
                memberTypeNodes = ((BLangUnionTypeNode) currentTypeNode).memberTypeNodes;
                // Recursively check all members.
                for (BLangType memberTypeNode : memberTypeNodes) {
                    checkErrors(unresolvedType, memberTypeNode, visitedNodes);
                }
                break;
            case TUPLE_TYPE_NODE:
                memberTypeNodes = ((BLangTupleTypeNode) currentTypeNode).memberTypeNodes;
                for (BLangType memberTypeNode : memberTypeNodes) {
                    checkErrors(unresolvedType, memberTypeNode, visitedNodes);
                }
                break;
            case CONSTRAINED_TYPE:
                checkErrors(unresolvedType, ((BLangConstrainedType) currentTypeNode).constraint, visitedNodes);
                break;
            case USER_DEFINED_TYPE:
                String currentTypeNodeName = ((BLangUserDefinedType) currentTypeNode).typeName.value;
                // Skip all types defined as anonymous types.
                if (currentTypeNodeName.startsWith("$")) {
                    return;
                }

                if (unresolvedTypeNodeName.equals(currentTypeNodeName)) {
                    // Cyclic dependency detected. We need to add the `unresolvedTypeNodeName` or the
                    // `memberTypeNodeName` to the end of the list to complete the cyclic dependency when
                    // printing the error.
                    visitedNodes.push(currentTypeNodeName);
                    dlog.error((DiagnosticPos) unresolvedType.getPosition(), DiagnosticCode.CYCLIC_TYPE_REFERENCE,
                            visitedNodes);
                    // We need to remove the last occurrence since we use this list in a recursive call.
                    // Otherwise, unwanted types will get printed in the cyclic dependency error.
                    visitedNodes.pop();
                } else if (visitedNodes.contains(currentTypeNodeName)) {
                    // Cyclic dependency detected. But in here, all the types in the list might not be necessary for the
                    // cyclic dependency error message.
                    //
                    // Eg - A -> B -> C -> B // Last B is what we are currently checking
                    //
                    // In such case, we create a new list with relevant type names.
                    int i = visitedNodes.indexOf(currentTypeNodeName);
                    List<String> dependencyList = new ArrayList<>(visitedNodes.size() - i);
                    for (; i < visitedNodes.size(); i++) {
                        dependencyList.add(visitedNodes.get(i));
                    }
                    // Add the `currentTypeNodeName` to complete the cycle.
                    dependencyList.add(currentTypeNodeName);
                    dlog.error((DiagnosticPos) unresolvedType.getPosition(), DiagnosticCode.CYCLIC_TYPE_REFERENCE,
                            dependencyList);
                } else {
                    // Check whether the current type node is in the unresolved list. If it is in the list, we need to
                    // check it recursively.
                    List<TypeDefinition> typeDefinitions = unresolvedTypes.stream()
                            .filter(typeDefinition -> typeDefinition.getName().getValue().equals(currentTypeNodeName))
                            .collect(Collectors.toList());
                    if (typeDefinitions.isEmpty()) {
                        // If a type is declared, it should either get defined successfully or added to the unresolved
                        // types list. If a type is not in either one of them, that means it is an undefined type.
                        LocationData locationData = new LocationData(currentTypeNodeName, currentTypeNode.pos.sLine,
                                currentTypeNode.pos.sCol);
                        if (unknownTypeRefs.add(locationData)) {
                            dlog.error(currentTypeNode.pos, DiagnosticCode.UNKNOWN_TYPE, currentTypeNodeName);
                        }
                    } else {
                        for (TypeDefinition typeDefinition : typeDefinitions) {
                            String typeName = typeDefinition.getName().getValue();
                            // Add the node name to the list.
                            visitedNodes.push(typeName);
                            // Recursively check for errors.
                            checkErrors(unresolvedType, (BLangType) typeDefinition.getTypeNode(), visitedNodes);
                            // We need to remove the added type node here since we have finished checking errors.
                            visitedNodes.pop();
                        }
                    }
                }
                break;
            case BUILT_IN_REF_TYPE:
                // Eg - `xml`. This is not needed to be checked because no types are available in the `xml`.
            case FINITE_TYPE_NODE:
            case FUNCTION_TYPE:
            case VALUE_TYPE:
            case ERROR_TYPE:
                // Do nothing.
                break;
            case RECORD_TYPE:
                for (TypeNode typeNode : ((BLangRecordTypeNode) currentTypeNode).getTypeReferences()) {
                    checkErrors(unresolvedType, (BLangType) typeNode, visitedNodes);
                }
                break;
            case OBJECT_TYPE:
                for (TypeNode typeNode : ((BLangObjectTypeNode) currentTypeNode).getTypeReferences()) {
                    checkErrors(unresolvedType, (BLangType) typeNode, visitedNodes);
                }
                break;
            default:
                throw new RuntimeException("unhandled type kind: " + currentTypeNode.getKind());
        }
    }

    public boolean isUnknownTypeRef(BLangUserDefinedType bLangUserDefinedType) {
        LocationData locationData = new LocationData(bLangUserDefinedType.typeName.value,
                                                     bLangUserDefinedType.pos.sLine, bLangUserDefinedType.pos.sCol);
        return unknownTypeRefs.contains(locationData);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BType definedType = symResolver.resolveTypeNode(typeDefinition.typeNode, env);
        if (definedType == symTable.semanticError) {
            // TODO : Fix this properly. issue #21242
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
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE ||
                typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDefinition.typeNode;
            // For each referenced type, check whether the types are already resolved.
            // If not, then that type should get a higher precedence.
            for (BLangType typeRef : structureTypeNode.typeRefs) {
                BType referencedType = symResolver.resolveTypeNode(typeRef, env);
                if (referencedType == symTable.noType) {
                    if (!this.unresolvedTypes.contains(typeDefinition)) {
                        this.unresolvedTypes.add(typeDefinition);
                        return;
                    }
                }
            }
        }

        if (typeDefinition.typeNode.getKind() == NodeKind.FUNCTION_TYPE && definedType.tsymbol == null) {
            definedType.tsymbol = Symbols.createTypeSymbol(SymTag.FUNCTION_TYPE, Flags.asMask(typeDefinition.flagSet),
                                                           Names.EMPTY, env.enclPkg.symbol.pkgID, definedType,
                                                           env.scope.owner);
        }

        typeDefinition.precedence = this.typePrecedence++;
        BTypeSymbol typeDefSymbol;
        if (definedType.tsymbol.name != Names.EMPTY) {
            typeDefSymbol = definedType.tsymbol.createLabelSymbol();
        } else {
            typeDefSymbol = definedType.tsymbol;
        }
        typeDefSymbol.markdownDocumentation = getMarkdownDocAttachment(typeDefinition.markdownDocumentationAttachment);
        typeDefSymbol.name = names.fromIdNode(typeDefinition.getName());
        typeDefSymbol.pkgID = env.enclPkg.packageID;

        boolean distinctFlagPresent = isDistinctFlagPresent(typeDefinition);

        // todo: need to handle intersections
        if (distinctFlagPresent) {
            if (definedType.getKind() == TypeKind.ERROR) {
                BErrorType distinctType = getDistinctErrorType(typeDefinition, (BErrorType) definedType, typeDefSymbol);
                typeDefinition.typeNode.type = distinctType;
                definedType = distinctType;
            } else if (definedType.getKind() == TypeKind.UNION) {
                validateUnionForDistinctType((BUnionType) definedType, typeDefinition.pos);
            }
        }

        typeDefSymbol.flags |= Flags.asMask(typeDefinition.flagSet);
        // Reset public flag when set on a non public type.
        typeDefSymbol.flags &= getPublicFlagResetingMask(typeDefinition.flagSet, typeDefinition.typeNode);
        if (isDeprecated(typeDefinition.annAttachments)) {
            typeDefSymbol.flags |= Flags.DEPRECATED;
        }

        if (typeDefinition.annAttachments.stream()
                .anyMatch(attachment -> attachment.annotationName.value.equals(Names.ANNOTATION_TYPE_PARAM.value))) {
            // TODO : Clean this. Not a nice way to handle this.
            //  TypeParam is built-in annotation, and limited only within lang.* modules.
            if (PackageID.isLangLibPackageID(this.env.enclPkg.packageID)) {
                typeDefSymbol.type = typeParamAnalyzer.createTypeParam(typeDefSymbol.type, typeDefSymbol.name);
                typeDefSymbol.flags |= Flags.TYPE_PARAM;
                if (typeDefinition.typeNode.getKind() == NodeKind.ERROR_TYPE) {
                    typeDefSymbol.isLabel = false;
                }
            } else {
                dlog.error(typeDefinition.pos, DiagnosticCode.TYPE_PARAM_OUTSIDE_LANG_MODULE);
            }
        }
        definedType.flags |= typeDefSymbol.flags;

        typeDefinition.symbol = typeDefSymbol;
        boolean isLanglibModule = PackageID.isLangLibPackageID(this.env.enclPkg.packageID);
        if (isLanglibModule) {
            handleLangLibTypes(typeDefinition);
            return;
        }

        defineSymbol(typeDefinition.name.pos, typeDefSymbol);

        if (typeDefinition.typeNode.type.tag == TypeTags.ERROR) {
            // constructors are only defined for named types.
            defineErrorConstructorSymbol(typeDefinition.name.pos, typeDefSymbol);
        }
    }

    private void validateUnionForDistinctType(BUnionType definedType, DiagnosticPos pos) {
        Set<BType> memberTypes = definedType.getMemberTypes();
        TypeKind firstTypeKind = null;
        for (BType type : memberTypes) {
            TypeKind typeKind = type.getKind();
            if (firstTypeKind == null && (typeKind == TypeKind.ERROR || typeKind == TypeKind.OBJECT)) {
                firstTypeKind = typeKind;

            }
            if (typeKind != firstTypeKind) {
                dlog.error(pos, DiagnosticCode.DISTINCT_TYPING_ONLY_SUPPORT_OBJECTS_AND_ERRORS);
            }
        }
    }

    private BErrorType getDistinctErrorType(BLangTypeDefinition typeDefinition, BErrorType definedType,
                                            BTypeSymbol typeDefSymbol) {
        BErrorType definedErrorType = definedType;
        // Create a new type for distinct type definition such as `type FooErr distinct BarErr;`
        // `typeDefSymbol` is different to `definedErrorType.tsymbol` in a type definition statement that use
        // already defined type as the base type.
        if (definedErrorType.tsymbol != typeDefSymbol) {
            BErrorType bErrorType = new BErrorType(typeDefSymbol);
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
                ? anonymousModelHelper.getNextDistinctErrorId(env.enclPkg.packageID)
                : typeDefinition.getName().value;

        return BTypeIdSet.from(env.enclPkg.packageID, name, isPublicType, secondary);
    }

    private boolean isDistinctFlagPresent(BLangTypeDefinition typeDefinition) {
        boolean distinctFlagPresent = false;
        if (typeDefinition.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
            distinctFlagPresent = ((BLangUserDefinedType) typeDefinition.typeNode).flagSet.contains(Flag.DISTINCT);
        } else if (typeDefinition.typeNode.getKind() == NodeKind.ERROR_TYPE) {
            distinctFlagPresent = ((BLangErrorType) typeDefinition.typeNode).flagSet.contains(Flag.DISTINCT);
        }
        return distinctFlagPresent;
    }

    private void handleLangLibTypes(BLangTypeDefinition typeDefinition) {

        // As per spec 2020R3 built-in types are limited only within lang.* modules.
        for (BLangAnnotationAttachment attachment : typeDefinition.annAttachments) {
            if (attachment.annotationName.value.equals(Names.ANNOTATION_TYPE_PARAM.value)) {
                BTypeSymbol typeDefSymbol = typeDefinition.symbol;
                typeDefSymbol.type = typeParamAnalyzer.createTypeParam(typeDefSymbol.type, typeDefSymbol.name);
                typeDefSymbol.flags |= Flags.TYPE_PARAM;
                break;
            } else if (attachment.annotationName.value.equals(Names.ANNOTATION_BUILTIN_SUBTYPE.value)) {
                // Type is pre-defined in symbol Table.
                BType type = symTable.getLangLibSubType(typeDefinition.name.value);
                typeDefinition.symbol = type.tsymbol;
                typeDefinition.type = type;
                typeDefinition.typeNode.type = type;
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
    private int getPublicFlagResetingMask(Set<Flag> flagSet, BLangType typeNode) {
        boolean isAnonType =
                typeNode instanceof BLangStructureTypeNode && ((BLangStructureTypeNode) typeNode).isAnonymous;
        if (flagSet.contains(Flag.PUBLIC) || isAnonType) {
            return Integer.MAX_VALUE;
        } else {
            return ~Flags.PUBLIC;
        }
    }

    private void defineErrorConstructorSymbol(DiagnosticPos pos, BTypeSymbol typeDefSymbol) {
        BErrorType errorType = (BErrorType) typeDefSymbol.type;
        BConstructorSymbol symbol = new BConstructorSymbol(typeDefSymbol.flags, typeDefSymbol.name,
                typeDefSymbol.pkgID, errorType, typeDefSymbol.owner);
        symbol.kind = SymbolKind.ERROR_CONSTRUCTOR;
        symbol.scope = new Scope(symbol);
        symbol.retType = errorType;
        if (symResolver.checkForUniqueSymbol(pos, env, symbol)) {
            env.scope.define(symbol.name, symbol);
        }

        ((BErrorTypeSymbol) typeDefSymbol).ctorSymbol = symbol;
    }

    @Override
    public void visit(BLangWorker workerNode) {
        BInvokableSymbol workerSymbol = Symbols.createWorkerSymbol(Flags.asMask(workerNode.flagSet),
                names.fromIdNode(workerNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        workerSymbol.markdownDocumentation = getMarkdownDocAttachment(workerNode.markdownDocumentationAttachment);
        workerNode.symbol = workerSymbol;
        defineSymbolWithCurrentEnvOwner(workerNode.pos, workerSymbol);
    }

    @Override
    public void visit(BLangService serviceNode) {
        BServiceSymbol serviceSymbol = Symbols.createServiceSymbol(Flags.asMask(serviceNode.flagSet),
                names.fromIdNode(serviceNode.name), env.enclPkg.symbol.pkgID, serviceNode.type, env.scope.owner);
        serviceSymbol.markdownDocumentation = getMarkdownDocAttachment(serviceNode.markdownDocumentationAttachment);

        BType serviceObjectType = serviceNode.serviceTypeDefinition.symbol.type;
        serviceNode.symbol = serviceSymbol;
        serviceNode.symbol.type = new BServiceType(serviceObjectType.tsymbol);
        serviceSymbol.scope = new Scope(serviceSymbol);

        // Caching values future validation.
        if (serviceNode.serviceTypeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode;
            objectTypeNode.functions.stream().filter(func -> func.flagSet.contains(Flag.RESOURCE))
                    .forEach(func -> serviceNode.resourceFunctions.add(func));
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        boolean validAttachedFunc = validateFuncReceiver(funcNode);
        boolean remoteFlagSetOnNode = Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.REMOTE);

        if (!funcNode.attachedFunction && Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.PRIVATE)) {
            dlog.error(funcNode.pos, DiagnosticCode.PRIVATE_FUNCTION_VISIBILITY, funcNode.name);
        }

        if (funcNode.receiver == null && !funcNode.attachedFunction && remoteFlagSetOnNode) {
            dlog.error(funcNode.pos, DiagnosticCode.REMOTE_IN_NON_OBJECT_FUNCTION, funcNode.name.value);
        }

        if (PackageID.isLangLibPackageID(env.enclPkg.symbol.pkgID)) {
            funcNode.flagSet.add(Flag.LANG_LIB);
        }

        BInvokableSymbol funcSymbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                                                                   getFuncSymbolName(funcNode),
                                                                   env.enclPkg.symbol.pkgID, null, env.scope.owner,
                                                                   funcNode.hasBody());
        funcSymbol.source = funcNode.pos.src.cUnitName;
        funcSymbol.markdownDocumentation = getMarkdownDocAttachment(funcNode.markdownDocumentationAttachment);
        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, env);
        defineInvokableSymbol(funcNode, funcSymbol, invokableEnv);

        if (isDeprecated(funcNode.annAttachments)) {
            funcSymbol.flags |= Flags.DEPRECATED;
        }
        // Define function receiver if any.
        if (funcNode.receiver != null) {
            defineAttachedFunctions(funcNode, funcSymbol, invokableEnv, validAttachedFunc);
        }
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
    public void visit(BLangResource resourceNode) {
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
                if (types.isValidLiteral((BLangLiteral) constant.expr, staticType)) {
                    // A literal type constant is defined with correct type.
                    // Update the type of the finiteType node to the static type.
                    // This is done to make the type inferring work.
                    // eg: const decimal d = 5.0;
                    BLangFiniteTypeNode finiteType = (BLangFiniteTypeNode) constant.associatedTypeDefinition.typeNode;
                    BLangExpression valueSpaceExpr = finiteType.valueSpace.iterator().next();
                    valueSpaceExpr.type = staticType;
                    defineNode(constant.associatedTypeDefinition, env);

                    constantSymbol.type = constant.associatedTypeDefinition.symbol.type;
                    constantSymbol.literalType = staticType;
                } else {
                    // A literal type constant is defined with some incorrect type. Set the original
                    // types and continue the flow and let it fail at semantic analyzer.
                    defineNode(constant.associatedTypeDefinition, env);
                    constantSymbol.type = staticType;
                    constantSymbol.literalType = constant.expr.type;
                }
            } else {
                // A literal type constant is defined without the type.
                // Then the type of the symbol is the finite type.
                defineNode(constant.associatedTypeDefinition, env);
                constantSymbol.type = constant.associatedTypeDefinition.symbol.type;
                constantSymbol.literalType = constant.expr.type;
            }
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
        return new BConstantSymbol(Flags.asMask(constant.flagSet), name, pkgID,
                symTable.semanticError, symTable.noType, env.scope.owner);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        // assign the type to var type node
        if (varNode.type == null) {
            if (varNode.typeNode != null) {
                varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
            } else {
                varNode.type = symTable.noType;
            }
        }

        Name varName = names.fromIdNode(varNode.name);
        if (varName == Names.EMPTY || varName == Names.IGNORE) {
            // This is a variable created for a return type
            // e.g. function foo() (int);
            return;
        }

        BVarSymbol varSymbol = defineVarSymbol(varNode.name.pos, varNode.flagSet, varNode.type, varName, env);
        if (isDeprecated(varNode.annAttachments)) {
            varSymbol.flags |= Flags.DEPRECATED;
        }
        varSymbol.markdownDocumentation = getMarkdownDocAttachment(varNode.markdownDocumentationAttachment);
        varNode.symbol = varSymbol;
        if (varNode.symbol.type.tsymbol != null && Symbols.isFlagOn(varNode.symbol.type.tsymbol.flags, Flags.CLIENT)) {
            varSymbol.tag = SymTag.ENDPOINT;
        }

        if (varSymbol.type.tag == TypeTags.FUTURE && ((BFutureType) varSymbol.type).workerDerivative) {
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

        if (varSymbol.type.tag == TypeTags.INVOKABLE) {
            BInvokableSymbol symbol = (BInvokableSymbol) varSymbol;
            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) symbol.type.tsymbol;
            symbol.params = tsymbol.params;
            symbol.restParam = tsymbol.restParam;
            symbol.retType = tsymbol.returnType;
        }

        if (varSymbol.type.tag == TypeTags.NEVER && ((env.scope.owner.tag & SymTag.RECORD) != SymTag.RECORD)) {
            // check if the variable is defined as a 'never' type (except inside a record type)
            // if so, log an error
            dlog.error(varNode.pos, DiagnosticCode.NEVER_TYPED_VAR_DEF_NOT_ALLOWED, varSymbol.name);
        }
    }

    @Override
    public void visit(BLangTupleVariable varNode) {
        // assign the type to var type node
        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }
    }

    @Override
    public void visit(BLangRecordVariable varNode) {
        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }
    }

    @Override
    public void visit(BLangErrorVariable varNode) {
        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }
    }

    @Override
    public void visit(BLangEndpoint endpoint) {
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
                    env.enclPkg.symbol.pkgID, env.scope.owner);
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
        BXMLNSSymbol xmlnsSymbol =
                new BXMLNSSymbol(names.fromString(symbolName), nsURI, env.enclPkg.symbol.pkgID, env.scope.owner);
        if (symResolver.checkForUniqueMemberSymbol(bLangXMLAttribute.pos, env, xmlnsSymbol)) {
            env.scope.define(xmlnsSymbol.name, xmlnsSymbol);
            bLangXMLAttribute.symbol = xmlnsSymbol;
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);
        defineRecordTypeNode(recordTypeNode, typeDefEnv);
    }

    private void defineRecordTypeNode(BLangRecordTypeNode recordTypeNode, SymbolEnv env) {
        BRecordType recordType = (BRecordType) recordTypeNode.symbol.type;
        recordTypeNode.type = recordType;

        // Define all the fields
        resolveFields(recordType, recordTypeNode, env);

        recordType.sealed = recordTypeNode.sealed;
        if (recordTypeNode.sealed && recordTypeNode.restFieldType != null) {
            dlog.error(recordTypeNode.restFieldType.pos, DiagnosticCode.REST_FIELD_NOT_ALLOWED_IN_SEALED_RECORDS);
            return;
        }

        if (recordTypeNode.restFieldType == null) {
            if (recordTypeNode.sealed) {
                recordType.restFieldType = symTable.noType;
                return;
            }
            recordType.restFieldType = symTable.anydataType;
            return;
        }

        recordType.restFieldType = symResolver.resolveTypeNode(recordTypeNode.restFieldType, env);
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
        if (langLib.equals(FUTURE)) {
            symTable.langFutureModuleSymbol = packageSymbol;
            return;
        }
        if (langLib.equals(INT)) {
            symTable.langIntModuleSymbol = packageSymbol;
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
    }

    private boolean isValidAnnotationType(BType type) {
        if (type == symTable.semanticError) {
            return false;
        }

        switch (type.tag) {
            case TypeTags.RECORD:
//                BRecordType recordType = (BRecordType) type;
//                return recordType.fields.stream().allMatch(field -> types.isAnydata(field.type)) &&
//                        (recordType.sealed || types.isAnydata(recordType.restFieldType));
            case TypeTags.MAP:
//                return types.isAnydata(((BMapType) type).constraint);
                return true;
            case TypeTags.ARRAY:
                BType elementType = ((BArrayType) type).eType;
                return (elementType.tag == TypeTags.MAP || elementType.tag == TypeTags.RECORD) &&
                        isValidAnnotationType(elementType);
        }

        return types.isAssignable(type, symTable.trueType);
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
     * Visit each compilation unit (.bal file) and add each top-level node in the compilation unit to the
     * testable package node.
     *
     * @param pkgNode current package node
     * @param enclPkgImports imports of the enclosed package
     */
    private void populatePackageNode(BLangTestablePackage pkgNode, List<BLangImportPackage> enclPkgImports) {
        populatePackageNode(pkgNode);
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
                pkgNode.globalVars.add((BLangSimpleVariable) node);
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
        }
    }

    private void defineErrorDetails(List<BLangTypeDefinition> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangTypeDefinition typeDef : typeDefNodes) {
            if (typeDef.typeNode.getKind() != NodeKind.ERROR_TYPE) {
                continue;
            }

            BLangErrorType errorTypeNode = (BLangErrorType) typeDef.typeNode;
            SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(errorTypeNode, typeDef.symbol.scope, pkgEnv);

            BType detailType = Optional.ofNullable(errorTypeNode.detailType)
                                        .map(bLangType -> symResolver.resolveTypeNode(bLangType, typeDefEnv))
                                        .orElse(symTable.detailType);

            BErrorType errorType = (BErrorType) typeDef.symbol.type;
            errorType.detailType = detailType;
        }
    }

    private void defineFields(List<BLangTypeDefinition> typeDefNodes, SymbolEnv pkgEnv) {
        int originalSize = typeDefNodes.size();

        for (int i = 0; i < originalSize; i++) {
            BLangTypeDefinition typeDef = typeDefNodes.get(i);
            NodeKind nodeKind = typeDef.typeNode.getKind();
            if (nodeKind != NodeKind.OBJECT_TYPE && nodeKind != NodeKind.RECORD_TYPE) {
                continue;
            }

            // Create typeDef type
            BStructureType structureType = (BStructureType) typeDef.symbol.type;
            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDef.typeNode;
            SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(structureTypeNode, typeDef.symbol.scope, pkgEnv);

            // Define all the fields
            resolveFields(structureType, structureTypeNode, typeDefEnv);

            if (typeDef.symbol.kind != SymbolKind.RECORD) {
                continue;
            }

            BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) structureTypeNode;
            BRecordType recordType = (BRecordType) structureType;
            recordType.sealed = recordTypeNode.sealed;
            if (recordTypeNode.sealed && recordTypeNode.restFieldType != null) {
                dlog.error(recordTypeNode.restFieldType.pos, DiagnosticCode.REST_FIELD_NOT_ALLOWED_IN_SEALED_RECORDS);
                continue;
            }

            if (recordTypeNode.restFieldType == null) {
                if (recordTypeNode.sealed) {
                    recordType.restFieldType = symTable.noType;
                    continue;
                }
                recordType.restFieldType = symTable.anydataType;
                continue;
            }

            recordType.restFieldType = symResolver.resolveTypeNode(recordTypeNode.restFieldType, typeDefEnv);
        }

        // Any newly added typedefs are due to `T & readonly` typed fields. Once the fields are set for all
        // type-definitions we can revisit the newly added type-definitions and define the fields for them.
        int newSize = typeDefNodes.size();

        for (int i = originalSize; i < newSize; i++) {
            ImmutableTypeCloner.defineUndefinedImmutableRecordFields(typeDefNodes.get(i), types, pkgEnv, symTable,
                                                                     anonymousModelHelper, names);
        }
    }

    private void resolveFields(BStructureType structureType, BLangStructureTypeNode structureTypeNode,
                               SymbolEnv typeDefEnv) {
        structureType.fields = structureTypeNode.fields.stream()
                .peek(field -> defineNode(field, typeDefEnv))
                .filter(field -> field.symbol.type != symTable.semanticError) // filter out erroneous fields
                .map(field -> new BField(names.fromIdNode(field.name), field.pos, field.symbol))
                .collect(getFieldCollector());

        // Resolve and add the fields of the referenced types to this object.
        resolveReferencedFields(structureTypeNode, typeDefEnv);

        for (BLangSimpleVariable field : structureTypeNode.referencedFields) {
            defineNode(field, typeDefEnv);
            if (field.symbol.type == symTable.semanticError) {
                continue;
            }
            structureType.fields.put(field.name.value, new BField(names.fromIdNode(field.name), field.pos,
                                                                  field.symbol));
        }
    }

    private void defineMembers(List<BLangTypeDefinition> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangTypeDefinition typeDef : typeDefNodes) {
            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                continue;
            }
            if (typeDef.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                BLangObjectTypeNode objTypeNode = (BLangObjectTypeNode) typeDef.typeNode;
                SymbolEnv objMethodsEnv =
                        SymbolEnv.createObjectMethodsEnv(objTypeNode, (BObjectTypeSymbol) objTypeNode.symbol, pkgEnv);

                // Define the functions defined within the object
                defineObjectInitFunction(objTypeNode, objMethodsEnv);
                objTypeNode.functions.forEach(f -> {
                    f.setReceiver(ASTBuilderUtil.createReceiver(typeDef.pos, typeDef.symbol.type));
                    defineNode(f, objMethodsEnv);
                });

                Set<String> includedFunctionNames = new HashSet<>();
                // Add the attached functions of the referenced types to this object.
                // Here it is assumed that all the attached functions of the referred type are
                // resolved by the time we reach here. It is achieved by ordering the typeDefs
                // according to the precedence.
                for (BLangType typeRef : objTypeNode.typeRefs) {
                    if (typeRef.type.tsymbol == null || typeRef.type.tsymbol.kind != SymbolKind.OBJECT) {
                        continue;
                    }

                    List<BAttachedFunction> functions = ((BObjectTypeSymbol) typeRef.type.tsymbol).attachedFuncs;
                    for (BAttachedFunction function : functions) {
                        defineReferencedFunction(typeDef, objMethodsEnv, typeRef, function, includedFunctionNames);
                    }
                }
            }
        }
    }

    private void defineInvokableSymbol(BLangInvokableNode invokableNode, BInvokableSymbol funcSymbol,
                                       SymbolEnv invokableEnv) {
        invokableNode.symbol = funcSymbol;
        defineSymbol(invokableNode.name.pos, funcSymbol);
        invokableEnv.scope = funcSymbol.scope;
        defineInvokableSymbolParams(invokableNode, funcSymbol, invokableEnv);
    }

    private void defineInvokableSymbolParams(BLangInvokableNode invokableNode, BInvokableSymbol invokableSymbol,
                                             SymbolEnv invokableEnv) {
        boolean foundDefaultableParam = false;
        List<BVarSymbol> paramSymbols = new ArrayList<>();
        invokableNode.clonedEnv = invokableEnv.shallowClone();
        for (BLangSimpleVariable varNode : invokableNode.requiredParams) {
            defineNode(varNode, invokableEnv);
            if (varNode.expr != null) {
                foundDefaultableParam = true;
            }
            if (varNode.expr == null && foundDefaultableParam) {
                dlog.error(varNode.pos, REQUIRED_PARAM_DEFINED_AFTER_DEFAULTABLE_PARAM);
            }
            BVarSymbol symbol = varNode.symbol;
            if (varNode.flagSet.contains(Flag.PUBLIC)) {
                symbol.flags |= Flags.PUBLIC;
            }
            if (varNode.expr != null) {
                symbol.flags |= Flags.OPTIONAL;
                symbol.defaultableParam = true;
            }
            paramSymbols.add(symbol);
        }

        if (!invokableNode.desugaredReturnType) {
            symResolver.resolveTypeNode(invokableNode.returnTypeNode, invokableEnv);
        }
        invokableSymbol.params = paramSymbols;
        invokableSymbol.retType = invokableNode.returnTypeNode.type;

        // Create function type
        List<BType> paramTypes = paramSymbols.stream()
                .map(paramSym -> paramSym.type)
                .collect(Collectors.toList());

        BInvokableTypeSymbol functionTypeSymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE,
                invokableSymbol.flags,
                env.enclPkg.symbol.pkgID,
                invokableSymbol.type, env.scope.owner);
        functionTypeSymbol.params = invokableSymbol.params;
        functionTypeSymbol.returnType = invokableSymbol.retType;

        BType restType = null;
        if (invokableNode.restParam != null) {
            defineNode(invokableNode.restParam, invokableEnv);
            invokableSymbol.restParam = invokableNode.restParam.symbol;
            functionTypeSymbol.restParam = invokableSymbol.restParam;
            restType = invokableSymbol.restParam.type;
        }
        invokableSymbol.type = new BInvokableType(paramTypes, restType, invokableNode.returnTypeNode.type, null);
        invokableSymbol.type.tsymbol = functionTypeSymbol;
    }

    private void defineSymbol(DiagnosticPos pos, BSymbol symbol) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public void defineSymbol(DiagnosticPos pos, BSymbol symbol, SymbolEnv env) {
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
    public void defineShadowedSymbol(DiagnosticPos pos, BSymbol symbol, SymbolEnv env) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbolInCurrentScope(pos, env, symbol, symbol.tag)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public void defineTypeNarrowedSymbol(DiagnosticPos pos, SymbolEnv targetEnv, BVarSymbol symbol, BType type) {
        if (symbol.owner.tag == SymTag.PACKAGE) {
            // Avoid defining shadowed symbol for global vars, since the type is not narrowed.
            return;
        }

        BVarSymbol varSymbol = createVarSymbol(symbol.flags, type, symbol.name, targetEnv);
        if (type.tag == TypeTags.INVOKABLE && type.tsymbol != null) {
            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) type.tsymbol;
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) varSymbol;
            invokableSymbol.params = tsymbol.params;
            invokableSymbol.restParam = tsymbol.restParam;
            invokableSymbol.retType = tsymbol.returnType;
            invokableSymbol.flags = tsymbol.flags;
        }
        varSymbol.owner = symbol.owner;
        varSymbol.originalSymbol = symbol;
        defineShadowedSymbol(pos, varSymbol, targetEnv);
    }

    private void defineSymbolWithCurrentEnvOwner(DiagnosticPos pos, BSymbol symbol) {
        symbol.scope = new Scope(env.scope.owner);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public BVarSymbol defineVarSymbol(DiagnosticPos pos, Set<Flag> flagSet, BType varType, Name varName,
                                      SymbolEnv env) {
        // Create variable symbol
        Scope enclScope = env.scope;
        BVarSymbol varSymbol = createVarSymbol(flagSet, varType, varName, env);

        // Add it to the enclosing scope
        if (!symResolver.checkForUniqueSymbol(pos, env, varSymbol)) {
            varSymbol.type = symTable.semanticError;
        }
        enclScope.define(varSymbol.name, varSymbol);
        return varSymbol;
    }

    public void defineExistingVarSymbolInEnv(BVarSymbol varSymbol, SymbolEnv env) {
        if (!symResolver.checkForUniqueSymbol(env, varSymbol)) {
            varSymbol.type = symTable.semanticError;
        }
        env.scope.define(varSymbol.name, varSymbol);
    }

    public BVarSymbol createVarSymbol(Set<Flag> flagSet, BType varType, Name varName, SymbolEnv env) {
        return createVarSymbol(Flags.asMask(flagSet), varType, varName, env);
    }

    public BVarSymbol createVarSymbol(int flags, BType varType, Name varName, SymbolEnv env) {
        BType safeType = types.getSafeType(varType, true, false);
        BVarSymbol varSymbol;
        if (safeType.tag == TypeTags.INVOKABLE) {
            varSymbol = new BInvokableSymbol(SymTag.VARIABLE, flags, varName, env.enclPkg.symbol.pkgID, varType,
                                             env.scope.owner);
            varSymbol.kind = SymbolKind.FUNCTION;
        } else {
            varSymbol = new BVarSymbol(flags, varName, env.enclPkg.symbol.pkgID, varType, env.scope.owner);
            if (varType.tsymbol != null && Symbols.isFlagOn(varType.tsymbol.flags, Flags.CLIENT)) {
                varSymbol.tag = SymTag.ENDPOINT;
            }
        }
        return varSymbol;
    }

    private void defineObjectInitFunction(BLangObjectTypeNode object, SymbolEnv conEnv) {
        BLangFunction initFunction = object.initFunction;
        if (initFunction == null) {
            return;
        }

        //Set cached receiver to the init function
        initFunction.receiver = ASTBuilderUtil.createReceiver(object.pos, object.type);

        initFunction.attachedFunction = true;
        initFunction.flagSet.add(Flag.ATTACHED);
        defineNode(initFunction, conEnv);
    }

    private void defineAttachedFunctions(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                         SymbolEnv invokableEnv, boolean isValidAttachedFunc) {
        BTypeSymbol typeSymbol = funcNode.receiver.type.tsymbol;

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
        BRecordTypeSymbol recordSymbol = (BRecordTypeSymbol) funcNode.receiver.type.tsymbol;

        recordSymbol.initializerFunc = new BAttachedFunction(
                names.fromIdNode(funcNode.name), funcSymbol, funcType);
    }

    private void validateFunctionsAttachedToObject(BLangFunction funcNode, BInvokableSymbol funcSymbol) {

        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) funcNode.receiver.type.tsymbol;
        BAttachedFunction attachedFunc = new BAttachedFunction(
                names.fromIdNode(funcNode.name), funcSymbol, funcType);

        validateRemoteFunctionAttachedToObject(funcNode, objectSymbol);
        validateResourceFunctionAttachedToObject(funcNode, objectSymbol);

        // Check whether this attached function is a object initializer.
        if (!funcNode.objInitFunction) {
            objectSymbol.attachedFuncs.add(attachedFunc);
            return;
        }

        types.validateErrorOrNilReturn(funcNode, DiagnosticCode.INVALID_OBJECT_CONSTRUCTOR);
        objectSymbol.initializerFunc = attachedFunc;
    }

    private void validateRemoteFunctionAttachedToObject(BLangFunction funcNode, BObjectTypeSymbol objectSymbol) {
        if (!Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.REMOTE)) {
            return;
        }
        funcNode.symbol.flags |= Flags.REMOTE;

        if (!Symbols.isFlagOn(objectSymbol.flags, Flags.CLIENT)) {
            this.dlog.error(funcNode.pos, DiagnosticCode.REMOTE_FUNCTION_IN_NON_CLIENT_OBJECT);
        }
    }

    private void validateResourceFunctionAttachedToObject(BLangFunction funcNode, BObjectTypeSymbol objectSymbol) {
        if (Symbols.isFlagOn(objectSymbol.flags, Flags.SERVICE)
                && (Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.PUBLIC)
                || Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.PRIVATE))) {
            this.dlog.error(funcNode.pos, DiagnosticCode.SERVICE_FUNCTION_INVALID_MODIFIER);
        }
        if (!Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.RESOURCE)) {
            return;
        }
        funcNode.symbol.flags |= Flags.RESOURCE;

        if (!Symbols.isFlagOn(objectSymbol.flags, Flags.SERVICE)) {
            this.dlog.error(funcNode.pos, DiagnosticCode.RESOURCE_FUNCTION_IN_NON_SERVICE_OBJECT);
        }

        types.validateErrorOrNilReturn(funcNode, DiagnosticCode.RESOURCE_FUNCTION_INVALID_RETURN_TYPE);
    }

    private StatementNode createAssignmentStmt(BLangSimpleVariable variable, BVarSymbol varSym, BSymbol fieldVar) {
        //Create LHS reference variable
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = variable.pos;
        varRef.variableName = (BLangIdentifier) createIdentifier(fieldVar.name.getValue());
        varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        varRef.symbol = fieldVar;
        varRef.type = fieldVar.type;

        //Create RHS variable reference
        BLangSimpleVarRef exprVar = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        exprVar.pos = variable.pos;
        exprVar.variableName = (BLangIdentifier) createIdentifier(varSym.name.getValue());
        exprVar.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        exprVar.symbol = varSym;
        exprVar.type = varSym.type;

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

        if (funcNode.receiver.type == null) {
            funcNode.receiver.type = symResolver.resolveTypeNode(funcNode.receiver.typeNode, env);
        }
        if (funcNode.receiver.type.tag == TypeTags.SEMANTIC_ERROR) {
            return true;
        }

        if (funcNode.receiver.type.tag == TypeTags.OBJECT
                && !this.env.enclPkg.symbol.pkgID.equals(funcNode.receiver.type.tsymbol.pkgID)) {
            dlog.error(funcNode.receiver.pos, DiagnosticCode.FUNC_DEFINED_ON_NON_LOCAL_TYPE,
                    funcNode.name.value, funcNode.receiver.type.toString());
            return false;
        }
        return true;
    }

    private Name getFuncSymbolName(BLangFunction funcNode) {
        if (funcNode.receiver != null) {
            return names.fromString(Symbols.getAttachedFuncSymbolName(
                    funcNode.receiver.type.tsymbol.name.value, funcNode.name.value));
        }
        return names.fromIdNode(funcNode.name);
    }

    private Name getFieldSymbolName(BLangSimpleVariable receiver, BLangSimpleVariable variable) {
        return names.fromString(Symbols.getAttachedFuncSymbolName(
                receiver.type.tsymbol.name.value, variable.name.value));
    }

    private MarkdownDocAttachment getMarkdownDocAttachment(BLangMarkdownDocumentation docNode) {
        if (docNode == null) {
            return new MarkdownDocAttachment();
        }
        MarkdownDocAttachment docAttachment = new MarkdownDocAttachment();
        docAttachment.description = docNode.getDocumentation();

        docNode.getParameters().forEach(p ->
                docAttachment.parameters.add(new MarkdownDocAttachment.Parameter(p.parameterName.value,
                        p.getParameterDocumentation())));

        docAttachment.returnValueDescription = docNode.getReturnParameterDocumentation();
        return docAttachment;
    }

    private void createDummyTypeDefSymbol(TypeDefinition typeDefNode, SymbolEnv env) {
        if (typeDefNode.getKind() == NodeKind.CONSTANT) {
            return;
        }

        BLangTypeDefinition typeDef = (BLangTypeDefinition) typeDefNode;
        // This is only to keep the flow running so that at the end there will be proper semantic errors
        typeDef.symbol = Symbols.createTypeSymbol(SymTag.TYPE_DEF, Flags.asMask(typeDef.flagSet),
                names.fromIdNode(typeDef.name), env.enclPkg.symbol.pkgID, typeDef.typeNode.type, env.scope.owner);
        typeDef.symbol.scope = env.scope;

        // Todo - Add more kinds.
        switch (typeDef.typeNode.type.tag) {
            case TypeTags.RECORD:
            case TypeTags.OBJECT:
                typeDef.symbol.kind = ((BLangStructureTypeNode) typeDef.typeNode).symbol.kind;
                ((BLangStructureTypeNode) typeDef.typeNode).symbol.scope = env.scope;
                break;
        }

        defineSymbol(typeDef.pos, typeDef.symbol, env);
    }

    private void resolveReferencedFields(BLangStructureTypeNode structureTypeNode, SymbolEnv typeDefEnv) {
        Set<BSymbol> referencedTypes = new HashSet<>();
        List<BLangType> invalidTypeRefs = new ArrayList<>();
        // Get the inherited fields from the type references

        Map<String, BLangSimpleVariable> fieldNames = new HashMap<>();
        for (BLangSimpleVariable fieldVariable : structureTypeNode.fields) {
            fieldNames.put(fieldVariable.name.value, fieldVariable);
        }

        structureTypeNode.referencedFields = structureTypeNode.typeRefs.stream().flatMap(typeRef -> {
            BType referredType = symResolver.resolveTypeNode(typeRef, typeDefEnv);
            if (referredType == symTable.semanticError) {
                return Stream.empty();
            }

            // Check for duplicate type references
            if (!referencedTypes.add(referredType.tsymbol)) {
                dlog.error(typeRef.pos, DiagnosticCode.REDECLARED_TYPE_REFERENCE, typeRef);
                return Stream.empty();
            }

            if (structureTypeNode.type.tag == TypeTags.OBJECT) {
                if (referredType.tag != TypeTags.OBJECT) {
                    dlog.error(typeRef.pos, DiagnosticCode.INCOMPATIBLE_TYPE_REFERENCE, typeRef);
                    invalidTypeRefs.add(typeRef);
                    return Stream.empty();
                }

                BObjectType objectType = (BObjectType) referredType;
                if (structureTypeNode.type.tsymbol.owner != referredType.tsymbol.owner) {
                    for (BField field : objectType.fields.values()) {
                        if (!Symbols.isPublic(field.symbol)) {
                            dlog.error(typeRef.pos, DiagnosticCode.INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS,
                                       typeRef);
                            invalidTypeRefs.add(typeRef);
                            return Stream.empty();
                        }
                    }

                    for (BAttachedFunction func : ((BObjectTypeSymbol) objectType.tsymbol).attachedFuncs) {
                        if (!Symbols.isPublic(func.symbol)) {
                            dlog.error(typeRef.pos, DiagnosticCode.INCOMPATIBLE_TYPE_REFERENCE_NON_PUBLIC_MEMBERS,
                                       typeRef);
                            invalidTypeRefs.add(typeRef);
                            return Stream.empty();
                        }
                    }
                }
            }

            if (structureTypeNode.type.tag == TypeTags.RECORD && referredType.tag != TypeTags.RECORD) {
                dlog.error(typeRef.pos, DiagnosticCode.INCOMPATIBLE_RECORD_TYPE_REFERENCE, typeRef);
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
                    return !types.isAssignable(existingVariable.type, f.type);
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

    private void defineReferencedFunction(BLangTypeDefinition typeDef, SymbolEnv objEnv, BLangType typeRef,
                                          BAttachedFunction referencedFunc, Set<String> includedFunctionNames) {
        String referencedFuncName = referencedFunc.funcName.value;
        Name funcName = names.fromString(
                Symbols.getAttachedFuncSymbolName(typeDef.symbol.name.value, referencedFuncName));
        BSymbol matchingObjFuncSym = symResolver.lookupSymbolInMainSpace(objEnv, funcName);

        if (matchingObjFuncSym != symTable.notFoundSymbol) {
            if (!includedFunctionNames.add(referencedFuncName)) {
                dlog.error(typeRef.pos, DiagnosticCode.REDECLARED_SYMBOL, referencedFuncName);
                return;
            }

            if (Symbols.isFunctionDeclaration(matchingObjFuncSym) && Symbols.isFunctionDeclaration(
                    referencedFunc.symbol)) {
                Optional<BLangFunction> matchingFunc = ((BLangObjectTypeNode) typeDef.typeNode)
                        .functions.stream().filter(fn -> fn.symbol == matchingObjFuncSym).findFirst();
                DiagnosticPos pos = matchingFunc.isPresent() ? matchingFunc.get().pos : typeRef.pos;
                dlog.error(pos, DiagnosticCode.REDECLARED_FUNCTION_FROM_TYPE_REFERENCE,
                           referencedFunc.funcName, typeRef);
            }

            if (!hasSameFunctionSignature((BInvokableSymbol) matchingObjFuncSym, referencedFunc.symbol)) {
                Optional<BLangFunction> matchingFunc = ((BLangObjectTypeNode) typeDef.typeNode)
                        .functions.stream().filter(fn -> fn.symbol == matchingObjFuncSym).findFirst();
                DiagnosticPos pos = matchingFunc.isPresent() ? matchingFunc.get().pos : typeRef.pos;
                dlog.error(pos, DiagnosticCode.REFERRED_FUNCTION_SIGNATURE_MISMATCH,
                           getCompleteFunctionSignature(referencedFunc.symbol),
                           getCompleteFunctionSignature((BInvokableSymbol) matchingObjFuncSym));
            }
            return;
        }

        if (Symbols.isPrivate(referencedFunc.symbol)) {
            // we should not copy private functions.
            return;
        }

        // If not, define the function symbol within the object.
        // Take a copy of the symbol, with the new name, and the package ID same as the object type.
        BInvokableSymbol funcSymbol = ASTBuilderUtil.duplicateFunctionDeclarationSymbol(referencedFunc.symbol,
                                                                                        typeDef.symbol, funcName,
                                                                                        typeDef.symbol.pkgID);
        defineSymbol(typeRef.pos, funcSymbol, objEnv);

        // Create and define the parameters and receiver. This should be done after defining the function symbol.
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(null, funcSymbol.scope, objEnv);
        funcSymbol.params.forEach(param -> defineSymbol(typeRef.pos, param, funcEnv));
        if (funcSymbol.restParam != null) {
            defineSymbol(typeRef.pos, funcSymbol.restParam, funcEnv);
        }
        funcSymbol.receiverSymbol =
                defineVarSymbol(typeDef.pos, typeDef.flagSet, typeDef.symbol.type, Names.SELF, funcEnv);

        // Cache the function symbol.
        BAttachedFunction attachedFunc =
                new BAttachedFunction(referencedFunc.funcName, funcSymbol, (BInvokableType) funcSymbol.type);
        ((BObjectTypeSymbol) typeDef.symbol).attachedFuncs.add(attachedFunc);
        ((BObjectTypeSymbol) typeDef.symbol).referencedFunctions.add(attachedFunc);
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

    private boolean hasSameVisibilityModifier(int flags1, int flags2) {
        int xorOfFlags = flags1 ^ flags2;
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

    private BPackageSymbol duplicatePackagSymbol(BPackageSymbol originalSymbol) {
        BPackageSymbol copy = new BPackageSymbol(originalSymbol.pkgID, originalSymbol.owner, originalSymbol.flags);
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
        return copy;
    }

    private boolean isSameImport(BLangImportPackage importPkgNode, BPackageSymbol importSymbol) {
        if (!importPkgNode.orgName.value.equals(importSymbol.pkgID.orgName.value)) {
            return false;
        }

        BLangIdentifier pkgName = importPkgNode.pkgNameComps.get(importPkgNode.pkgNameComps.size() - 1);
        return pkgName.value.equals(importSymbol.pkgID.name.value);
    }

    private void resolveAndSetFunctionTypeFromRHSLambda(BLangSimpleVariable variable, SymbolEnv env) {
        BLangFunction function = ((BLangLambdaFunction) variable.expr).function;
        variable.type = symResolver.createInvokableType(function.getParameters(),
                function.restParam, function.returnTypeNode, Flags.asMask(variable.flagSet), env);
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
