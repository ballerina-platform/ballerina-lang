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
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.DefaultValueLiteral;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.AttachPoints;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;

import static org.ballerinalang.model.tree.NodeKind.IMPORT;

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
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private List<BLangTypeDefinition> unresolvedTypes;
    private int typePrecedence;

    private SymbolEnv env;

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
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
    }

    public BLangPackage definePackage(BLangPackage pkgNode) {
        populatePackageNode(pkgNode);
        defineNode(pkgNode, this.symTable.pkgEnvMap.get(symTable.builtInPackageSymbol));
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

        pkgNode.symbol = pkgSymbol;
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(pkgNode, pkgSymbol.scope, this.env);
        this.symTable.pkgEnvMap.put(pkgSymbol, pkgEnv);

        defineConstructs(pkgNode, pkgEnv);
        pkgNode.getTestablePkgs().forEach(testablePackage -> defineTestablePackage(testablePackage, pkgEnv,
                                                                                   pkgNode.imports));
        pkgNode.completedPhases.add(CompilerPhase.DEFINE);
    }

    private void defineConstructs(BLangPackage pkgNode, SymbolEnv pkgEnv) {
        // visit the package node recursively and define all package level symbols.
        // And maintain a list of created package symbols.
        pkgNode.imports.forEach(importNode -> defineNode(importNode, pkgEnv));

        // Define type definitions.
        this.typePrecedence = 0;

        // First visit constants.
        pkgNode.constants.forEach(constant -> defineNode(constant, pkgEnv));

        // Visit type definitions.
        defineTypeNodes(pkgNode.typeDefinitions, pkgEnv);

        // Resolve type node of constants. This is done after visiting the type definitions because otherwise if the
        // constant's type node is a type, it wont get resolved.
        resolveConstantTypeNode(pkgNode.constants, pkgEnv);

        pkgNode.globalVars.forEach(var -> defineNode(var, pkgEnv));

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

        // Update globalVar for endpoints.
        pkgNode.globalVars.stream().filter(var -> var.symbol.type.tsymbol != null && Symbols
                .isFlagOn(var.symbol.type.tsymbol.flags, Flags.CLIENT)).map(varNode -> varNode.symbol)
                .forEach(varSymbol -> varSymbol.tag = SymTag.ENDPOINT);
    }

    public void visit(BLangAnnotation annotationNode) {
        BAnnotationSymbol annotationSymbol = Symbols.createAnnotationSymbol(Flags.asMask(annotationNode.flagSet),
                AttachPoints.asMask(annotationNode.attachPoints), names.fromIdNode(annotationNode.name),
                env.enclPkg.symbol.pkgID, null, env.scope.owner);
        annotationSymbol.markdownDocumentation =
                getMarkdownDocAttachment(annotationNode.markdownDocumentationAttachment);
        annotationSymbol.type = new BAnnotationType(annotationSymbol);
        annotationNode.symbol = annotationSymbol;
        defineSymbol(annotationNode.name.pos, annotationSymbol);
        SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotationNode, annotationSymbol.scope, env);
        if (annotationNode.typeNode != null) {
            BType recordType = this.symResolver.resolveTypeNode(annotationNode.typeNode, annotationEnv);
            annotationSymbol.attachedType = recordType.tsymbol;
            if (recordType != symTable.semanticError && recordType.tag != TypeTags.RECORD) {
                dlog.error(annotationNode.typeNode.pos, DiagnosticCode.ANNOTATION_REQUIRE_RECORD, recordType);
            }
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        Name pkgAlias = names.fromIdNode(importPkgNode.alias);
        if (symResolver.lookupSymbol(env, pkgAlias, SymTag.IMPORT) != symTable.notFoundSymbol) {
            dlog.error(importPkgNode.pos, DiagnosticCode.REDECLARED_SYMBOL, pkgAlias);
            return;
        }

        // TODO Clean this code up. Can we move the this to BLangPackageBuilder class
        // Create import package symbol
        Name orgName;
        Name version;
        PackageID enclPackageID = env.enclPkg.packageID;
        if (importPkgNode.orgName.value == null || importPkgNode.orgName.value.isEmpty()) {
            // means it's in 'import <pkg-name>' style
            orgName = enclPackageID.orgName;
            version = (Names.DEFAULT_VERSION.equals(enclPackageID.version)) ? new Name("") : enclPackageID.version;
        } else if (importPkgNode.orgName.value.equals(enclPackageID.orgName.value)) {
            // means it's in 'import <org-name>/<pkg-name>' style and <org-name> is used to import within same project
            orgName = names.fromIdNode(importPkgNode.orgName);
            // Here we set the version as empty due to the following cases:
            // 1) Suppose the import is from the same package, then the project version will be set later
            // 2) Suppose the import is from Ballerina Central or another project which has the same org, then the
            //    version is set when loading the import module
            version = new Name("");
        } else {
            // means it's in 'import <org-name>/<pkg-name>' style
            orgName = names.fromIdNode(importPkgNode.orgName);
            version = names.fromIdNode(importPkgNode.version);
        }
        List<Name> nameComps = importPkgNode.pkgNameComps.stream()
                .map(identifier -> names.fromIdNode(identifier))
                .collect(Collectors.toList());

        PackageID pkgId = new PackageID(orgName, nameComps, version);
        if (pkgId.name.getValue().startsWith(Names.BUILTIN_PACKAGE.value)) {
            dlog.error(importPkgNode.pos, DiagnosticCode.MODULE_NOT_FOUND,
                    importPkgNode.getQualifiedPackageName());
            return;
        }

        BPackageSymbol pkgSymbol = pkgLoader.loadPackageSymbol(pkgId, enclPackageID, this.env.enclPkg.repos);
        if (pkgSymbol == null) {
            dlog.error(importPkgNode.pos, DiagnosticCode.MODULE_NOT_FOUND,
                    importPkgNode.getQualifiedPackageName());
            return;
        }

        // define the import package symbol in the current package scope
        importPkgNode.symbol = pkgSymbol;
        ((BPackageSymbol) this.env.scope.owner).imports.add(pkgSymbol);
        this.env.scope.define(pkgAlias, pkgSymbol);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        String nsURI = (String) ((BLangLiteral) xmlnsNode.namespaceURI).value;

        if (xmlnsNode.prefix.value != null && nsURI.isEmpty()) {
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
        BSymbol foundSym = symResolver.lookupSymbol(env, xmlnsSymbol.name, SymTag.PACKAGE);
        if (foundSym != symTable.notFoundSymbol) {
            dlog.error(xmlnsNode.pos, DiagnosticCode.REDECLARED_SYMBOL, xmlnsSymbol.name);
            return;
        }

        // Define it in the enclosing scope. Here we check for the owner equality,
        // to support overriding of namespace declarations defined at package level.
        defineSymbol(xmlnsNode.pos, xmlnsSymbol);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        defineNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    private void defineTypeNodes(List<BLangTypeDefinition> typeDefs, SymbolEnv env) {
        if (typeDefs.size() == 0) {
            return;
        }
        this.unresolvedTypes = new ArrayList<>();
        for (BLangTypeDefinition typeDef : typeDefs) {
            defineNode(typeDef, env);
        }
        if (typeDefs.size() <= unresolvedTypes.size()) {
            // This situation can occur due to either a cyclic dependency or at least one of member types in type
            // definition node cannot be resolved. So we iterate through each node recursively looking for cyclic
            // dependencies or undefined types in type node.

            // We need to maintain a list to keep track of all encountered unresolved types. We need to keep track of
            // the location as well since the same unknown type can be specified in multiple places.
            LinkedList<LocationData> unknownTypes = new LinkedList<>();
            for (BLangTypeDefinition unresolvedType : unresolvedTypes) {
                // We need to keep track of all visited types to print cyclic dependency.
                LinkedList<String> references = new LinkedList<>();
                references.add(unresolvedType.name.value);
                checkErrors(unresolvedType, unresolvedType.typeNode, references, unknownTypes);
            }

            // Create and define dummy symbols and continue. This done to keep the remaining compiler
            // phases running, and to make the semantic validations happen properly.
            unresolvedTypes.forEach(type -> createDummyTypeDefSymbol(type, env));
            unresolvedTypes.forEach(type -> defineNode(type, env));
            return;
        }
        defineTypeNodes(unresolvedTypes, env);
    }

    private void checkErrors(BLangTypeDefinition unresolvedType, BLangType currentTypeNode, List<String> visitedNodes,
                             List<LocationData> encounteredUnknownTypes) {
        String unresolvedTypeNodeName = unresolvedType.name.value;

        // Check errors in the type definition.
        List<BLangType> memberTypeNodes;
        switch (currentTypeNode.getKind()) {
            case ARRAY_TYPE:
                checkErrors(unresolvedType, ((BLangArrayType) currentTypeNode).elemtype, visitedNodes,
                        encounteredUnknownTypes);
                break;
            case UNION_TYPE_NODE:
                // If the current type node is a union type node, we need to check all member nodes.
                memberTypeNodes = ((BLangUnionTypeNode) currentTypeNode).memberTypeNodes;
                // Recursively check all members.
                for (BLangType memberTypeNode : memberTypeNodes) {
                    checkErrors(unresolvedType, memberTypeNode, visitedNodes, encounteredUnknownTypes);
                }
                break;
            case TUPLE_TYPE_NODE:
                memberTypeNodes = ((BLangTupleTypeNode) currentTypeNode).memberTypeNodes;
                for (BLangType memberTypeNode : memberTypeNodes) {
                    checkErrors(unresolvedType, memberTypeNode, visitedNodes, encounteredUnknownTypes);
                }
                break;
            case CONSTRAINED_TYPE:
                checkErrors(unresolvedType, ((BLangConstrainedType) currentTypeNode).constraint, visitedNodes,
                        encounteredUnknownTypes);
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
                    visitedNodes.add(currentTypeNodeName);
                    dlog.error(unresolvedType.pos, DiagnosticCode.CYCLIC_TYPE_REFERENCE, visitedNodes);
                    // We need to remove the last occurrence since we use this list in a recursive call.
                    // Otherwise, unwanted types will get printed in the cyclic dependency error.
                    visitedNodes.remove(visitedNodes.lastIndexOf(currentTypeNodeName));
                } else if (visitedNodes.contains(currentTypeNodeName)) {
                    // Cyclic dependency detected. But in here, all the types in the list might not be necessary for the
                    // cyclic dependency error message.
                    //
                    // Eg - A -> B -> C -> B // Last B is what we are currently checking
                    //
                    // In such case, we create a new list with relevant type names.
                    List<String> dependencyList = new LinkedList<>();
                    for (int i = visitedNodes.indexOf(currentTypeNodeName); i < visitedNodes.size(); i++) {
                        dependencyList.add(visitedNodes.get(i));
                    }
                    // Add the `currentTypeNodeName` to complete the cycle.
                    dependencyList.add(currentTypeNodeName);
                    dlog.error(unresolvedType.pos, DiagnosticCode.CYCLIC_TYPE_REFERENCE, dependencyList);
                } else {
                    // Check whether the current type node is in the unresolved list. If it is in the list, we need to
                    // check it recursively.
                    List<BLangTypeDefinition> typeDefinitions = unresolvedTypes.stream()
                            .filter(typeDefinition -> typeDefinition.name.value.equals(currentTypeNodeName))
                            .collect(Collectors.toList());
                    if (typeDefinitions.isEmpty()) {
                        // If a type is declared, it should either get defined successfully or added to the unresolved
                        // types list. If a type is not in either one of them, that means it is an undefined type.
                        LocationData locationData = new LocationData(currentTypeNodeName, currentTypeNode.pos.sLine,
                                currentTypeNode.pos.sCol);
                        if (!encounteredUnknownTypes.contains(locationData)) {
                            dlog.error(currentTypeNode.pos, DiagnosticCode.UNKNOWN_TYPE, currentTypeNodeName);
                            encounteredUnknownTypes.add(locationData);
                        }
                    } else {
                        for (BLangTypeDefinition typeDefinition : typeDefinitions) {
                            String typeName = typeDefinition.name.value;
                            // Add the node name to the list.
                            visitedNodes.add(typeName);
                            // Recursively check for errors.
                            checkErrors(unresolvedType, typeDefinition.typeNode, visitedNodes, encounteredUnknownTypes);
                            // We need to remove the added type node here since we have finished checking errors.
                            visitedNodes.remove(visitedNodes.lastIndexOf(typeName));
                        }
                    }
                }
                break;
            case BUILT_IN_REF_TYPE:
                // Eg - `xml`. This is not needed to be checked because no types are available in the `xml`.
            case FINITE_TYPE_NODE:
            case FUNCTION_TYPE:
            case VALUE_TYPE:
            case RECORD_TYPE:
            case OBJECT_TYPE:
            case ERROR_TYPE:
                // Do nothing.
                break;
            default:
                throw new RuntimeException("unhandled type kind: " + currentTypeNode.getKind());
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BType definedType = symResolver.resolveTypeNode(typeDefinition.typeNode, env);
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
                    }
                    return;
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
        typeDefSymbol.flags |= Flags.asMask(typeDefinition.flagSet);

        typeDefinition.symbol = typeDefSymbol;
        defineSymbol(typeDefinition.name.pos, typeDefSymbol);
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
        defineSymbol(serviceNode.name.pos, serviceSymbol);

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
        if (funcNode.attachedOuterFunction) {
            if (funcNode.receiver.type.tsymbol.kind == SymbolKind.RECORD) {
                dlog.error(funcNode.pos, DiagnosticCode.CANNOT_ATTACH_FUNCTIONS_TO_RECORDS, funcNode.name,
                        funcNode.receiver.type.tsymbol.name);
                createDummyFunctionSymbol(funcNode);
                visitObjectAttachedFunction(funcNode);
                return;
            }

            BSymbol funcSymbol = symTable.notFoundSymbol;
            if (funcNode.receiver.type.tag == TypeTags.OBJECT) {
                SymbolEnv objectEnv = SymbolEnv.createObjectMethodsEnv(null, (BObjectTypeSymbol) funcNode.receiver.type.
                        tsymbol, env);
                funcSymbol = symResolver.lookupSymbol(objectEnv, getFuncSymbolName(funcNode), SymTag.FUNCTION);
            }

            if (funcSymbol == symTable.notFoundSymbol) {
                dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_FUNCTION, funcNode.name,
                        funcNode.receiver.type.tsymbol.name);
                createDummyFunctionSymbol(funcNode);
                visitObjectAttachedFunction(funcNode);
                return;
            }

            if (Symbols.isPublic(funcSymbol) ^ Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.PUBLIC)) {
                dlog.error(funcNode.pos, DiagnosticCode.INVALID_VISIBILITY_ON_INTERFACE_FUNCTION_IMPL, funcNode.name,
                        funcNode.receiver.type);
                createDummyFunctionSymbol(funcNode);
                visitObjectAttachedFunction(funcNode);
                return;
            }

            if (Symbols.isPrivate(funcSymbol) ^ Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.PRIVATE)) {
                dlog.error(funcNode.pos, DiagnosticCode.INVALID_VISIBILITY_ON_INTERFACE_FUNCTION_IMPL, funcNode.name,
                        funcNode.receiver.type);
                createDummyFunctionSymbol(funcNode);
                visitObjectAttachedFunction(funcNode);
                return;
            }

            funcNode.symbol = (BInvokableSymbol) funcSymbol;
            if (funcNode.symbol.bodyExist) {
                dlog.error(funcNode.pos, DiagnosticCode.IMPLEMENTATION_ALREADY_EXIST, funcNode.name);
            }
            if (remoteFlagSetOnNode && !Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
                dlog.error(funcNode.pos, DiagnosticCode.REMOTE_ON_NON_REMOTE_FUNCTION, funcNode.name.value);
            }
            if (!remoteFlagSetOnNode && Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
                dlog.error(funcNode.pos, DiagnosticCode.REMOTE_REQUIRED_ON_REMOTE_FUNCTION);
            }
            validateAttachedFunction(funcNode, funcNode.receiver.type.tsymbol.name);
            visitObjectAttachedFunction(funcNode);
            return;
        }

        if (!funcNode.attachedFunction && Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.PRIVATE)) {
            dlog.error(funcNode.pos, DiagnosticCode.PRIVATE_FUNCTION_VISIBILITY, funcNode.name);
        }

        if (funcNode.receiver == null && !funcNode.attachedFunction && remoteFlagSetOnNode) {
            dlog.error(funcNode.pos, DiagnosticCode.REMOTE_IN_NON_OBJECT_FUNCTION, funcNode.name.value);
        }
        BInvokableSymbol funcSymbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                getFuncSymbolName(funcNode), env.enclPkg.symbol.pkgID, null, env.scope.owner, funcNode.body != null);
        funcSymbol.markdownDocumentation = getMarkdownDocAttachment(funcNode.markdownDocumentationAttachment);
        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, env);
        defineInvokableSymbol(funcNode, funcSymbol, invokableEnv);
        // Define function receiver if any.
        if (funcNode.receiver != null) {
            defineAttachedFunctions(funcNode, funcSymbol, invokableEnv, validAttachedFunc);
        }
    }

    private void createDummyFunctionSymbol(BLangFunction funcNode) {
        // This is only to keep the flow running so that at the end there will be proper semantic errors
        funcNode.symbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                getFuncSymbolName(funcNode), env.enclPkg.symbol.pkgID, null, env.scope.owner, true);
        funcNode.symbol.scope = new Scope(funcNode.symbol);
        funcNode.symbol.type = new BInvokableType(new ArrayList<>(), symTable.noType, null);
    }

    private void visitObjectAttachedFunction(BLangFunction funcNode) {
        //TODO check function parameters and return types
        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);

        invokableEnv.scope = funcNode.symbol.scope;
        defineObjectAttachedInvokableSymbolParams(funcNode, invokableEnv);
        if (env.enclPkg.objAttachedFunctions.contains(funcNode.symbol)) {
            dlog.error(funcNode.pos, DiagnosticCode.IMPLEMENTATION_ALREADY_EXIST, funcNode.name);
            return;
        }

        if (!funcNode.objInitFunction) {
            env.enclPkg.objAttachedFunctions.add(funcNode.symbol);
        }
        funcNode.receiver.symbol = funcNode.symbol.receiverSymbol;
    }

    private void validateAttachedFunction(BLangFunction funcNode, Name objName) {
        SymbolEnv invokableEnv = SymbolEnv.createDummyEnv(funcNode, funcNode.symbol.scope, env);
        List<BType> paramTypes = funcNode.requiredParams.stream()
                .peek(varNode -> varNode.type = symResolver.resolveTypeNode(varNode.typeNode, invokableEnv))
                .map(varNode -> varNode.type)
                .collect(Collectors.toList());

        funcNode.defaultableParams.forEach(p -> paramTypes.add(symResolver
                .resolveTypeNode(p.var.typeNode, invokableEnv)));

        if (!funcNode.desugaredReturnType) {
            symResolver.resolveTypeNode(funcNode.returnTypeNode, invokableEnv);
        }

        if (funcNode.restParam != null) {
            if (!funcNode.symbol.restParam.name.equals(names.fromIdNode(funcNode.restParam.name))) {
                dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_INTERFACE, funcNode.name, objName);
                return;
            }
            BType restParamType = symResolver.resolveTypeNode(funcNode.restParam.typeNode, invokableEnv);
            paramTypes.add(restParamType);
        }

        BInvokableType sourceType = (BInvokableType) funcNode.symbol.type;
        //this was used earlier to one to one match object declaration with definitions for attached functions
        // keeping this commented as we may need uncomment this later.
        //        int flags = Flags.asMask(funcNode.flagSet);
        //        if (((flags & Flags.NATIVE) != (funcNode.symbol.flags & Flags.NATIVE))
        //                || ((flags & Flags.PUBLIC) != (funcNode.symbol.flags & Flags.PUBLIC))) {
        //            dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_INTERFACE, funcNode.name, objName);
        //            return;
        //        }

        if (typesMissMatch(paramTypes, sourceType.paramTypes)
                || namesMissMatch(funcNode.requiredParams, funcNode.symbol.params)
                || namesMissMatchDef(funcNode.defaultableParams, funcNode.symbol.defaultableParams)) {
            dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_INTERFACE, funcNode.name, objName);
            return;
        }

        if (funcNode.returnTypeNode.type == null && sourceType.retType == null) {
            return;
        } else if (funcNode.returnTypeNode.type == null || sourceType.retType == null) {
            dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_INTERFACE, funcNode.name, objName);
            return;
        }

        if (funcNode.returnTypeNode.type.tag != sourceType.retType.tag) {
            dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_INTERFACE, funcNode.name, objName);
            return;
        }
        //Reset symbol flags to remove interface flag
        funcNode.symbol.flags = funcNode.symbol.flags ^ Flags.INTERFACE;
    }

    private boolean typesMissMatch(List<BType> lhs, List<BType> rhs) {
        if (lhs.size() != rhs.size()) {
            return true;
        }

        for (int i = 0; i < lhs.size(); i++) {
            if (!types.isSameType(lhs.get(i), rhs.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean namesMissMatch(List<BLangSimpleVariable> lhs, List<BVarSymbol> rhs) {
        if (lhs.size() != rhs.size()) {
            return true;
        }

        for (int i = 0; i < lhs.size(); i++) {
            if (!rhs.get(i).name.equals(names.fromIdNode(lhs.get(i).name))) {
                return true;
            }
        }
        return false;
    }

    private boolean namesMissMatchDef(List<BLangSimpleVariableDef> lhs, List<BVarSymbol> rhs) {
        if (lhs.size() != rhs.size()) {
            return true;
        }

        for (int i = 0; i < lhs.size(); i++) {
            if (!rhs.get(i).name.equals(names.fromIdNode(lhs.get(i).var.name))) {
                return true;
            }
        }
        return false;
    }

    private void defineObjectAttachedInvokableSymbolParams(BLangInvokableNode invokableNode, SymbolEnv invokableEnv) {
        // visit required params of the function
        invokableNode.requiredParams.forEach(varNode -> {
            visitObjectAttachedFunctionParam(varNode, invokableEnv);
        });

        invokableNode.defaultableParams.forEach(varDefNode -> {
            visitObjectAttachedFunctionParam(varDefNode.var, invokableEnv);
        });

        if (invokableNode.returnTypeNode != null) {
            invokableNode.returnTypeNode.type = symResolver.resolveTypeNode(invokableNode.returnTypeNode, env);
        }

        if (invokableNode.restParam != null) {
            visitObjectAttachedFunctionParam(invokableNode.restParam, invokableEnv);
        }
    }

    void visitObjectAttachedFunctionParam(BLangSimpleVariable variable, SymbolEnv invokableEnv) {
        // assign the type to var type node
        if (variable.type == null) {
            variable.type = symResolver.resolveTypeNode(variable.typeNode, env);
        }

        visitObjectAttachedFunctionParamSymbol(variable, invokableEnv);
    }

    void visitObjectAttachedFunctionParamSymbol(BLangSimpleVariable variable, SymbolEnv invokableEnv) {
        BSymbol varSymbol = symResolver.lookupSymbol(invokableEnv, names.fromIdNode(variable.name),
                SymTag.VARIABLE);
        if (varSymbol == symTable.notFoundSymbol) {
            defineNode(variable, invokableEnv);
        } else {
            variable.symbol = (BVarSymbol) varSymbol;
        }
        if (variable.expr == null) {
            return;
        }
        if (variable.expr.getKind() != NodeKind.LITERAL) {
            this.dlog.error(variable.expr.pos, DiagnosticCode.INVALID_DEFAULT_PARAM_VALUE, variable.name);
            return;
        }
        BLangLiteral literal = (BLangLiteral) variable.expr;
        variable.symbol.defaultValue = new DefaultValueLiteral(literal.value, literal.typeTag);
    }

    @Override
    public void visit(BLangResource resourceNode) {
    }

    @Override
    public void visit(BLangConstant constant) {
        // Create a new constant symbol.
        Name name = names.fromIdNode(constant.name);
        PackageID pkgID = env.enclPkg.symbol.pkgID;

        BConstantSymbol constantSymbol = new BConstantSymbol(Flags.asMask(constant.flagSet), name, pkgID,
                symTable.semanticError, symTable.semanticError, env.scope.owner);

        // Update the symbol of the node.
        constant.symbol = constantSymbol;

        // Note - This is checked and error is logged in semantic analyzer.
        if (((BLangExpression) constant.value).getKind() != NodeKind.LITERAL) {
            if (symResolver.checkForUniqueSymbol(constant.pos, env, constantSymbol, SymTag.VARIABLE_NAME)) {
                env.scope.define(constantSymbol.name, constantSymbol);
            }
            return;
        }

        // Visit the associated type definition. This will set the type of the type definition.
        defineNode(constant.associatedTypeDefinition, env);

        // Get the type of the associated type definition and set it as the type of the symbol. This is needed to
        // resolve the types of any type definition which uses the constant in type node.
        constantSymbol.type = constant.associatedTypeDefinition.symbol.type;
        constantSymbol.literalValue = ((BLangLiteral) constant.value).value;
        constantSymbol.literalValueTypeTag = ((BLangLiteral) constant.value).typeTag;
        constantSymbol.markdownDocumentation = getMarkdownDocAttachment(constant.markdownDocumentationAttachment);

        // Note - constant.typeNode.type will be resolved in a `resolveConstantTypeNode()` later since at this
        // point we might not be able to resolve the type properly because type definitions in the package are not yet
        // visited.

        // Add the symbol to the enclosing scope.
        if (!symResolver.checkForUniqueSymbol(constant.pos, env, constantSymbol, SymTag.VARIABLE_NAME)) {
            return;
        }

        // Add the symbol to the enclosing scope.
        env.scope.define(constantSymbol.name, constantSymbol);
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

        BVarSymbol varSymbol = defineVarSymbol(varNode.pos, varNode.flagSet, varNode.type, varName, env);
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
                BLangInvokableNode enclInvokable = lambdaFunction.cachedEnv.enclInvokable;
                if (lambdaFunctions.hasNext() && enclInvokable != null && varSymbol.owner == enclInvokable.symbol) {
                    lambdaFunction.cachedEnv.scope.define(varSymbol.name, varSymbol);
                }
            }
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
        if (exprs.size() == 1 && exprs.get(0).getKind() == NodeKind.LITERAL) {
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


    // Private methods

    private void resolveConstantTypeNode(List<BLangConstant> constants, SymbolEnv env) {
        // Resolve the type node and update the type of the typeNode.
        for (BLangConstant constant : constants) {
            // Constant symbol type will be an error type if the RHS of the constant definition node is invalid.
            if (constant.symbol.type == symTable.semanticError) {
                continue;
            }

            if (constant.typeNode != null) {
                constant.symbol.literalValueType = symResolver.resolveTypeNode(constant.typeNode, env);
            } else {
                constant.symbol.literalValueType = symTable.getTypeFromTag(constant.symbol.literalValueTypeTag);
            }

            if (!isAllowedConstantType(constant.symbol)) {
                dlog.error(constant.typeNode.pos, DiagnosticCode.CANNOT_DEFINE_CONSTANT_WITH_TYPE, constant.typeNode);
            }
        }
    }

    private boolean isAllowedConstantType(BConstantSymbol symbol) {
        switch (symbol.literalValueType.tag) {
            case TypeTags.BOOLEAN:
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.NIL:
                return true;
        }
        return false;
    }

    private boolean hasAnnotation(List<BLangAnnotationAttachment> annotationAttachmentList, String expectedAnnotation) {
        return annotationAttachmentList.stream()
                .filter(annotation -> annotation.annotationName.value.equals(expectedAnnotation)).count() > 0;
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
        // Remove recurring imports from the testable package which appears in the enclosing bLangPackage
        pkgNode.getImports().removeIf(enclPkgImports::contains);
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
                if (!pkgNode.imports.contains(node)) {
                    pkgNode.imports.add((BLangImportPackage) node);
                }
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

            BType reasonType = Optional.ofNullable(errorTypeNode.reasonType)
                                        .map(bLangType -> symResolver.resolveTypeNode(bLangType, typeDefEnv))
                                        .orElse(symTable.stringType);
            BType detailType = Optional.ofNullable(errorTypeNode.detailType)
                                        .map(bLangType -> symResolver.resolveTypeNode(bLangType, typeDefEnv))
                                        .orElse(symTable.mapType);

            if (reasonType == symTable.stringType && detailType == symTable.mapType) {
                typeDef.symbol.type = symTable.errorType;
                continue;
            }

            BErrorType errorType = (BErrorType) typeDef.symbol.type;
            errorType.reasonType = reasonType;
            errorType.detailType = detailType;
        }
    }

    private void defineFields(List<BLangTypeDefinition> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangTypeDefinition typeDef : typeDefNodes) {
            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE ||
                    (typeDef.symbol.type.tag != TypeTags.OBJECT && typeDef.symbol.type.tag != TypeTags.RECORD)) {
                continue;
            }

            // Create typeDef type
            BStructureType structureType = (BStructureType) typeDef.symbol.type;
            BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeDef.typeNode;
            SymbolEnv typeDefEnv = SymbolEnv.createTypeEnv(structureTypeNode, typeDef.symbol.scope, pkgEnv);

            // Resolve and add the fields of the referenced types to this object.
            resolveReferencedFields(structureTypeNode, typeDefEnv);

            // Define all the fields
            structureType.fields =
                    Stream.concat(structureTypeNode.fields.stream(), structureTypeNode.referencedFields.stream())
                            .peek(field -> defineNode(field, typeDefEnv))
                            .filter(field -> field.symbol.type != symTable.semanticError) // filter out erroneous fields
                            .map(field -> new BField(names.fromIdNode(field.name), field.symbol))
                            .collect(Collectors.toList());

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
    }

    private void defineMembers(List<BLangTypeDefinition> typeDefNodes, SymbolEnv pkgEnv) {
        for (BLangTypeDefinition typeDef : typeDefNodes) {
            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                continue;
            }
            if (typeDef.symbol.kind == SymbolKind.OBJECT) {
                BLangObjectTypeNode objTypeNode = (BLangObjectTypeNode) typeDef.typeNode;
                SymbolEnv objMethodsEnv =
                        SymbolEnv.createObjectMethodsEnv(objTypeNode, (BObjectTypeSymbol) objTypeNode.symbol, pkgEnv);

                // Define the functions defined within the object
                defineObjectInitFunction(objTypeNode, objMethodsEnv);
                objTypeNode.functions.forEach(f -> {
                    f.setReceiver(ASTBuilderUtil.createReceiver(typeDef.pos, typeDef.symbol.type));
                    defineNode(f, objMethodsEnv);
                });

                // Add the attached functions of the referenced types to this object.
                // Here it is assumed that all the attached functions of the referred type are
                // resolved by the time we reach here. It is achieved by ordering the typeDefs
                // according to the precedence.
                for (BLangType typeRef : objTypeNode.typeRefs) {
                    if (typeRef.type.tsymbol.kind != SymbolKind.OBJECT) {
                        continue;
                    }

                    List<BAttachedFunction> functions = ((BObjectTypeSymbol) typeRef.type.tsymbol).attachedFuncs;
                    for (BAttachedFunction function : functions) {
                        defineReferencedFunction(typeDef, objMethodsEnv, typeRef, function);
                    }
                }
            } else if (typeDef.symbol.kind == SymbolKind.RECORD) {
                // Create typeDef type
                BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
                SymbolEnv typeDefEnv = SymbolEnv.createPkgLevelSymbolEnv(recordTypeNode, typeDef.symbol.scope, pkgEnv);
                defineRecordInitFunction(typeDef, typeDefEnv);
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
        List<BVarSymbol> paramSymbols =
                invokableNode.requiredParams.stream()
                        .peek(varNode -> defineNode(varNode, invokableEnv))
                        .map(varNode -> varNode.symbol)
                        .collect(Collectors.toList());

        List<BVarSymbol> namedParamSymbols =
                invokableNode.defaultableParams.stream()
                        .peek(varDefNode -> defineNode(varDefNode.var, invokableEnv))
                        .map(varDefNode -> {
                            BVarSymbol varSymbol = varDefNode.var.symbol;
                            if (varDefNode.var.expr.getKind() != NodeKind.LITERAL) {
                                this.dlog.error(varDefNode.var.expr.pos, DiagnosticCode.INVALID_DEFAULT_PARAM_VALUE,
                                        varDefNode.var.name);
                            } else {
                                BLangLiteral literal = (BLangLiteral) varDefNode.var.expr;
                                varSymbol.defaultValue = new DefaultValueLiteral(literal.value, literal.typeTag);
                            }
                            return varSymbol;
                        })
                        .collect(Collectors.toList());

        if (!invokableNode.desugaredReturnType) {
            symResolver.resolveTypeNode(invokableNode.returnTypeNode, invokableEnv);
        }
        invokableSymbol.params = paramSymbols;
        invokableSymbol.retType = invokableNode.returnTypeNode.type;
        invokableSymbol.defaultableParams = namedParamSymbols;

        // Create function type
        List<BType> paramTypes = paramSymbols.stream()
                .map(paramSym -> paramSym.type)
                .collect(Collectors.toList());
        namedParamSymbols.forEach(paramSymbol -> paramTypes.add(paramSymbol.type));

        if (invokableNode.restParam != null) {
            defineNode(invokableNode.restParam, invokableEnv);
            invokableSymbol.restParam = invokableNode.restParam.symbol;
            paramTypes.add(invokableSymbol.restParam.type);
        }
        invokableSymbol.type = new BInvokableType(paramTypes, invokableNode.returnTypeNode.type, null);
    }

    private void defineSymbol(DiagnosticPos pos, BSymbol symbol) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol, symbol.tag)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public void defineSymbol(DiagnosticPos pos, BSymbol symbol, SymbolEnv env) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol, symbol.tag)) {
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

    private void defineSymbolWithCurrentEnvOwner(DiagnosticPos pos, BSymbol symbol) {
        symbol.scope = new Scope(env.scope.owner);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol, symbol.tag)) {
            env.scope.define(symbol.name, symbol);
        }
    }

    public BVarSymbol defineVarSymbol(DiagnosticPos pos, Set<Flag> flagSet, BType varType, Name varName,
                                      SymbolEnv env) {
        // Create variable symbol
        Scope enclScope = env.scope;
        BVarSymbol varSymbol = createVarSymbol(flagSet, varType, varName, env);

        // Add it to the enclosing scope
        if (!symResolver.checkForUniqueSymbol(pos, env, varSymbol, SymTag.VARIABLE_NAME)) {
            varSymbol.type = symTable.semanticError;
        }
        enclScope.define(varSymbol.name, varSymbol);
        return varSymbol;
    }

    public BVarSymbol createVarSymbol(Set<Flag> flagSet, BType varType, Name varName, SymbolEnv env) {
        return createVarSymbol(Flags.asMask(flagSet), varType, varName, env);
    }

    public BVarSymbol createVarSymbol(int flags, BType varType, Name varName, SymbolEnv env) {
        BType safeType = types.getSafeType(varType, false);
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

    private void defineRecordInitFunction(BLangTypeDefinition typeDef, SymbolEnv conEnv) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
        recordTypeNode.initFunction = ASTBuilderUtil.createInitFunction(typeDef.pos, "", Names.INIT_FUNCTION_SUFFIX);

        recordTypeNode.initFunction.receiver = createReceiver(typeDef.pos, typeDef.name);
        recordTypeNode.initFunction.attachedFunction = true;
        recordTypeNode.initFunction.flagSet.add(Flag.ATTACHED);

        // Adding record level variables to the init function is done at desugar phase

        defineNode(recordTypeNode.initFunction, conEnv);
    }

    private void defineAttachedFunctions(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                         SymbolEnv invokableEnv, boolean isValidAttachedFunc) {
        BTypeSymbol typeSymbol = funcNode.receiver.type.tsymbol;

        // Check whether there exists a struct field with the same name as the function name.
        if (isValidAttachedFunc) {
            if (typeSymbol.tag == SymTag.OBJECT) {
                validateFunctionsAttachedToObject(funcNode, funcSymbol, invokableEnv);
            } else if (typeSymbol.tag == SymTag.RECORD) {
                validateFunctionsAttachedToRecords(funcNode, funcSymbol, invokableEnv);
            }
        }

        defineNode(funcNode.receiver, invokableEnv);
        funcSymbol.receiverSymbol = funcNode.receiver.symbol;
    }

    private void validateFunctionsAttachedToRecords(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                                    SymbolEnv invokableEnv) {
        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        BRecordTypeSymbol recordSymbol = (BRecordTypeSymbol) funcNode.receiver.type.tsymbol;

        recordSymbol.initializerFunc = new BAttachedFunction(
                names.fromIdNode(funcNode.name), funcSymbol, funcType);
    }

    private void validateFunctionsAttachedToObject(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                                   SymbolEnv invokableEnv) {

        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) funcNode.receiver.type.tsymbol;
        BSymbol symbol = symResolver.lookupMemberSymbol(funcNode.receiver.pos, objectSymbol.scope, invokableEnv,
                names.fromIdNode(funcNode.name), SymTag.VARIABLE);
        if (symbol != symTable.notFoundSymbol) {
            dlog.error(funcNode.pos, DiagnosticCode.OBJECT_FIELD_AND_FUNC_WITH_SAME_NAME,
                    funcNode.name.value, funcNode.receiver.type.toString());
            return;
        }

        BAttachedFunction attachedFunc = new BAttachedFunction(
                names.fromIdNode(funcNode.name), funcSymbol, funcType);

        validateRemoteFunctionAttachedToObject(funcNode, objectSymbol);
        validateResourceFunctionAttachedToObject(funcNode, objectSymbol);

        // Check whether this attached function is a object initializer.
        if (!funcNode.objInitFunction) {
            objectSymbol.attachedFuncs.add(attachedFunc);
            return;
        }

        if (funcNode.returnTypeNode.type != symTable.nilType) {
            dlog.error(funcNode.pos, DiagnosticCode.INVALID_OBJECT_CONSTRUCTOR,
                    funcNode.name.value, funcNode.receiver.type.toString());
        }
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
        if (!Symbols.isFlagOn(Flags.asMask(funcNode.flagSet), Flags.RESOURCE)) {
            return;
        }
        funcNode.symbol.flags |= Flags.RESOURCE;

        if (!Symbols.isFlagOn(objectSymbol.flags, Flags.SERVICE)) {
            this.dlog.error(funcNode.pos, DiagnosticCode.RESOURCE_FUNCTION_IN_NON_SERVICE_OBJECT);
        }
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

    private BLangSimpleVariable createReceiver(DiagnosticPos pos, BLangIdentifier name) {
        BLangSimpleVariable receiver = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        receiver.pos = pos;
        IdentifierNode identifier = createIdentifier(Names.SELF.getValue());
        receiver.setName(identifier);
        BLangUserDefinedType structTypeNode = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        structTypeNode.pkgAlias = new BLangIdentifier();
        structTypeNode.typeName = name;
        receiver.setTypeNode(structTypeNode);
        return receiver;
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

        if (funcNode.receiver.type.tag != TypeTags.BOOLEAN
                && funcNode.receiver.type.tag != TypeTags.STRING
                && funcNode.receiver.type.tag != TypeTags.INT
                && funcNode.receiver.type.tag != TypeTags.FLOAT
                && funcNode.receiver.type.tag != TypeTags.DECIMAL
                && funcNode.receiver.type.tag != TypeTags.JSON
                && funcNode.receiver.type.tag != TypeTags.XML
                && funcNode.receiver.type.tag != TypeTags.MAP
                && funcNode.receiver.type.tag != TypeTags.TABLE
                && funcNode.receiver.type.tag != TypeTags.STREAM
                && funcNode.receiver.type.tag != TypeTags.FUTURE
                && funcNode.receiver.type.tag != TypeTags.OBJECT
                && funcNode.receiver.type.tag != TypeTags.RECORD) {
            dlog.error(funcNode.receiver.pos, DiagnosticCode.FUNC_DEFINED_ON_NOT_SUPPORTED_TYPE,
                    funcNode.name.value, funcNode.receiver.type.toString());
            return false;
        }

        if (!this.env.enclPkg.symbol.pkgID.equals(funcNode.receiver.type.tsymbol.pkgID)) {
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

    private void createDummyTypeDefSymbol(BLangTypeDefinition typeDef, SymbolEnv env) {
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
        List<BSymbol> referencedTypes = new ArrayList<>();
        // Get the inherited fields from the type references
        structureTypeNode.referencedFields = structureTypeNode.typeRefs.stream().flatMap(typeRef -> {
            BType referredType = symResolver.resolveTypeNode(typeRef, typeDefEnv);
            if (referredType == symTable.semanticError) {
                return Stream.empty();
            }

            // Check for duplicate type references
            if (referencedTypes.contains(referredType.tsymbol)) {
                dlog.error(typeRef.pos, DiagnosticCode.REDECLARED_TYPE_REFERENCE, typeRef);
                return Stream.empty();
            }

            if (structureTypeNode.type.tag == TypeTags.OBJECT && (referredType.tag != TypeTags.OBJECT || !Symbols
                    .isFlagOn(referredType.tsymbol.flags, Flags.ABSTRACT))) {
                dlog.error(typeRef.pos, DiagnosticCode.INCOMPATIBLE_TYPE_REFERENCE, typeRef);
                return Stream.empty();
            }

            if (structureTypeNode.type.tag == TypeTags.RECORD && referredType.tag != TypeTags.RECORD) {
                dlog.error(typeRef.pos, DiagnosticCode.INCOMPATIBLE_RECORD_TYPE_REFERENCE, typeRef);
                return Stream.empty();
            }

            referencedTypes.add(referredType.tsymbol);

            // Here it is assumed that all the fields of the referenced types are resolved
            // by the time we reach here. It is achieved by ordering the typeDefs according
            // to the precedence.
            // Default values of fields are not inherited.
            return ((BStructureType) referredType).fields.stream().map(field -> {
                BLangSimpleVariable var = ASTBuilderUtil.createVariable(typeRef.pos, field.name.value, field.type);
                var.flagSet = field.symbol.getFlags();
                return var;
            });
        }).collect(Collectors.toList());
    }

    private void defineReferencedFunction(BLangTypeDefinition typeDef, SymbolEnv objEnv, BLangType typeRef,
                                          BAttachedFunction function) {
        Name funcName = names.fromString(
                Symbols.getAttachedFuncSymbolName(typeDef.symbol.name.value, function.funcName.value));
        BSymbol foundSymbol = symResolver.lookupSymbol(objEnv, funcName, SymTag.VARIABLE);
        if (foundSymbol != symTable.notFoundSymbol) {
            if (Symbols.isFlagOn(foundSymbol.flags, Flags.INTERFACE) &&
                    Symbols.isFlagOn(function.symbol.flags, Flags.INTERFACE)) {
                dlog.error(typeRef.pos, DiagnosticCode.REDECLARED_FUNCTION_FROM_TYPE_REFERENCE, function.funcName,
                        typeRef);
            }
            return;
        }

        if (Symbols.isPrivate(function.symbol)) {
            // we should not copy private functions.
            return;
        }

        // If not, define the function symbol within the object.
        // Take a copy of the symbol, with the new name, and the package ID same as the object type.
        BInvokableSymbol funcSymbol = ASTBuilderUtil.duplicateInvokableSymbol(function.symbol, typeDef.symbol, funcName,
                typeDef.symbol.pkgID);
        defineSymbol(typeRef.pos, funcSymbol, objEnv);

        // Create and define the parameters and receiver. This should be done after defining the function symbol.
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(null, funcSymbol.scope, objEnv);
        funcSymbol.params.forEach(param -> defineSymbol(typeRef.pos, param, funcEnv));
        funcSymbol.defaultableParams.forEach(param -> defineSymbol(typeRef.pos, param, funcEnv));
        if (funcSymbol.restParam != null) {
            defineSymbol(typeRef.pos, funcSymbol.restParam, funcEnv);
        }
        funcSymbol.receiverSymbol =
                defineVarSymbol(typeDef.pos, typeDef.flagSet, typeDef.symbol.type, Names.SELF, funcEnv);

        // Cache the function symbol.
        BAttachedFunction attachedFunc =
                new BAttachedFunction(function.funcName, funcSymbol, (BInvokableType) funcSymbol.type);
        ((BObjectTypeSymbol) typeDef.symbol).attachedFuncs.add(attachedFunc);
        ((BObjectTypeSymbol) typeDef.symbol).referencedFunctions.add(attachedFunc);
    }

    private void defineInitFunctionParam(BLangSimpleVariable varNode) {
        Name varName = names.fromIdNode(varNode.name);

        // Here it is assumed that initFunctions are always for objects.
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) env.enclType;
        BTypeSymbol objectTypeSumbol = objectTypeNode.type.tsymbol;
        BSymbol fieldSymbol = symResolver.resolveObjectField(varNode.pos, env, varName, objectTypeSumbol);

        if (fieldSymbol == symTable.notFoundSymbol) {
            dlog.error(varNode.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD, varName,
                    env.enclType.type.getKind().typeName(), env.enclType.type.tsymbol.name);
        }

        // Define a new symbol for the constructor param, with the same type as the object field.
        varNode.type = fieldSymbol.type;
        BVarSymbol paramSymbol;
        if (fieldSymbol.kind == SymbolKind.FUNCTION) {
            paramSymbol = ASTBuilderUtil.duplicateInvokableSymbol((BInvokableSymbol) fieldSymbol,
                    objectTypeNode.initFunction.symbol, fieldSymbol.name, objectTypeSumbol.pkgID);
        } else {
            paramSymbol = new BVarSymbol(Flags.asMask(varNode.flagSet), varName, env.enclPkg.symbol.pkgID, varNode.type,
                    env.scope.owner);
        }
        defineShadowedSymbol(varNode.pos, paramSymbol, env);

        // Create an assignment to the actual field.
        // i.e.: self.x = x
        objectTypeNode.initFunction.initFunctionStmts.put(fieldSymbol,
                (BLangStatement) createAssignmentStmt(varNode, paramSymbol, fieldSymbol));
        varNode.symbol = paramSymbol;
        return;
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
            if (!(o instanceof LocationData)) {
                return false;
            }
            LocationData data = (LocationData) o;
            return name.equals(data.name) && row == data.row && column == data.column;
        }
    }
}
