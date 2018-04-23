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
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttributeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConnectorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTransformerSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLAttributeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BEnumType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType.BStructField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangEnum.BLangEnumerator;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final PackageCache packageCache;
    private final Names names;
    private final SymbolResolver symResolver;
    private final BLangDiagnosticLog dlog;
    private final EndpointSPIAnalyzer endpointSPIAnalyzer;
    private final Types types;

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
        this.packageCache = PackageCache.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.endpointSPIAnalyzer = EndpointSPIAnalyzer.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
    }

    public BLangPackage definePackage(BLangPackage pkgNode) {
        populatePackageNode(pkgNode);
        defineNode(pkgNode, null);
        return pkgNode;
    }

    public void defineNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = prevEnv;
    }


    // Visitor methods

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DEFINE)) {
            return;
        }

        // Create PackageSymbol
        BPackageSymbol pkgSymbol = Symbols.createPackageSymbol(pkgNode.packageID, this.symTable);
        pkgNode.symbol = pkgSymbol;
        SymbolEnv builtinEnv = this.symTable.pkgEnvMap.get(symTable.builtInPackageSymbol);
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(pkgNode, pkgSymbol.scope, builtinEnv);
        this.symTable.pkgEnvMap.put(pkgSymbol, pkgEnv);

        createPackageInitFunctions(pkgNode);
        // visit the package node recursively and define all package level symbols.
        // And maintain a list of created package symbols.
        pkgNode.imports.forEach(importNode -> defineNode(importNode, pkgEnv));

        // Define struct nodes.
        pkgNode.enums.forEach(enumNode -> defineNode(enumNode, pkgEnv));

        // Define record nodes.
        pkgNode.records.forEach(typeNode -> defineNode(typeNode, pkgEnv));

        // Define struct nodes.
        pkgNode.structs.forEach(struct -> defineNode(struct, pkgEnv));

        // Define object nodes
        pkgNode.objects.forEach(object -> defineNode(object, pkgEnv));

        // Define connector nodes.
        pkgNode.connectors.forEach(con -> defineNode(con, pkgEnv));

        // Define connector params and type.
        defineConnectorParams(pkgNode.connectors, pkgEnv);

        // Define transformer nodes.
        pkgNode.transformers.forEach(tansformer -> defineNode(tansformer, pkgEnv));

        // Define service and resource nodes.
        pkgNode.services.forEach(service -> defineNode(service, pkgEnv));

        // Define type definitions.
        pkgNode.typeDefinitions.forEach(typeDefinition -> defineNode(typeDefinition, pkgEnv));

        // Define struct field nodes.
        defineRecordFields(pkgNode.records, pkgEnv);

        // Define struct field nodes.
        defineStructFields(pkgNode.structs, pkgEnv);

        // Define object field nodes.
        defineObjectFields(pkgNode.objects, pkgEnv);

        // Define connector action nodes.
        defineConnectorMembers(pkgNode.connectors, pkgEnv);

        // Define object functions
        defineObjectMembers(pkgNode.objects, pkgEnv);

        // Define function nodes.
        pkgNode.functions.forEach(func -> defineNode(func, pkgEnv));

        // Define transformer params
        defineTransformerMembers(pkgNode.transformers, pkgEnv);

        // Define service resource nodes.
        defineServiceMembers(pkgNode.services, pkgEnv);

        // Define annotation nodes.
        pkgNode.annotations.forEach(annot -> defineNode(annot, pkgEnv));

        resolveAnnotationAttributeTypes(pkgNode.annotations, pkgEnv);

        pkgNode.globalVars.forEach(var -> defineNode(var, pkgEnv));

        pkgNode.globalEndpoints.forEach(ep -> defineNode(ep, pkgEnv));

        definePackageInitFunctions(pkgNode, pkgEnv);
        pkgNode.completedPhases.add(CompilerPhase.DEFINE);
    }

    @Deprecated
    private void resolveAnnotationAttributeTypes(List<BLangAnnotation> annotations, SymbolEnv pkgEnv) {
        annotations.forEach(annotation -> {
            annotation.attributes.forEach(attribute -> {
                SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotation, annotation.symbol.scope, pkgEnv);
                BType actualType = this.symResolver.resolveTypeNode(attribute.typeNode, annotationEnv);
                attribute.symbol.type = actualType;
            });
        });
    }

    public void visit(BLangAnnotation annotationNode) {
        BSymbol annotationSymbol = Symbols.createAnnotationSymbol(Flags.asMask(annotationNode.flagSet), names.
                fromIdNode(annotationNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        annotationSymbol.type = new BAnnotationType((BAnnotationSymbol) annotationSymbol);
        annotationNode.attachmentPoints.forEach(point ->
                ((BAnnotationSymbol) annotationSymbol).attachmentPoints.add(point));
        annotationNode.symbol = annotationSymbol;
        defineSymbol(annotationNode.pos, annotationSymbol);
        SymbolEnv annotationEnv = SymbolEnv.createAnnotationEnv(annotationNode, annotationSymbol.scope, env);
        annotationNode.attributes.forEach(att -> this.defineNode(att, annotationEnv));
        if (annotationNode.typeNode != null) {
            BType structType = this.symResolver.resolveTypeNode(annotationNode.typeNode, annotationEnv);
            ((BAnnotationSymbol) annotationSymbol).attachedType = structType.tsymbol;
        }
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        BAnnotationAttributeSymbol annotationAttributeSymbol = Symbols.createAnnotationAttributeSymbol(names.
                        fromIdNode(annotationAttribute.name), env.enclPkg.symbol.pkgID,
                null, env.scope.owner);
        annotationAttributeSymbol.docTag = DocTag.FIELD;
        annotationAttributeSymbol.expr = annotationAttribute.expr;
        annotationAttribute.symbol = annotationAttributeSymbol;
        ((BAnnotationSymbol) env.scope.owner).attributes.add(annotationAttributeSymbol);
        defineSymbol(annotationAttribute.pos, annotationAttributeSymbol);
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
        if (importPkgNode.orgName.value == null || importPkgNode.orgName.value.isEmpty()) {
            // means it's in 'import <pkg-name>' style
            orgName = env.enclPkg.packageID.orgName;
        } else {
            // means it's in 'import <org-name>/<pkg-name>' style
            orgName = names.fromIdNode(importPkgNode.orgName);
        }
        List<Name> nameComps = importPkgNode.pkgNameComps.stream()
                .map(identifier -> names.fromIdNode(identifier))
                .collect(Collectors.toList());

        String version = names.fromIdNode(importPkgNode.version).getValue().replaceAll("[^\\d.]", "");
        PackageID pkgId = new PackageID(orgName, nameComps, new Name(version));
        if (pkgId.name.getValue().startsWith(Names.BUILTIN_PACKAGE.value)) {
            dlog.error(importPkgNode.pos, DiagnosticCode.PACKAGE_NOT_FOUND,
                    importPkgNode.getQualifiedPackageName());
            return;
        }

        BPackageSymbol pkgSymbol = pkgLoader.loadPackageSymbol(pkgId, env.enclPkg.packageRepository);
        if (pkgSymbol == null) {
            dlog.error(importPkgNode.pos, DiagnosticCode.PACKAGE_NOT_FOUND,
                    importPkgNode.getQualifiedPackageName());
            return;
        }

        populateInitFunctionInvocation(importPkgNode, pkgSymbol);

        // define the import package symbol in the current package scope
        importPkgNode.symbol = pkgSymbol;
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

    @Override
    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = Symbols.createStructSymbol(Flags.asMask(structNode.flagSet),
                names.fromIdNode(structNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        structNode.symbol = structSymbol;
        // Create struct type
        structNode.symbol.type = new BStructType((BTypeSymbol) structNode.symbol);
        defineSymbol(structNode.pos, structSymbol);
    }

    @Override
    public void visit(BLangObject objectNode) {
        BSymbol objectSymbol = Symbols.createObjectSymbol(Flags.asMask(objectNode.flagSet),
                names.fromIdNode(objectNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        objectNode.symbol = objectSymbol;
        // Create struct type
        objectNode.symbol.type = new BStructType((BTypeSymbol) objectNode.symbol);
        defineSymbol(objectNode.pos, objectSymbol);
    }

    @Override
    public void visit(BLangRecord record) {
        BSymbol structSymbol = Symbols.createRecordSymbol(Flags.asMask(record.flagSet),
                names.fromIdNode(record.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        record.symbol = structSymbol;
        // Create record type
        record.symbol.type = new BStructType((BTypeSymbol) record.symbol);
        defineSymbol(record.pos, structSymbol);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BSymbol typeDefSymbol = Symbols.createTypeDefinitionSymbol(Flags.asMask(typeDefinition.flagSet),
                names.fromIdNode(typeDefinition.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        typeDefinition.symbol = typeDefSymbol;

        HashSet<BLangExpression> valueSpace = new HashSet<>();
        for (BLangExpression literal : typeDefinition.valueSpace) {
            BType literalType = symTable.getTypeFromTag(((BLangLiteral) literal).typeTag);
            ((BLangLiteral) literal).type = literalType;
            valueSpace.add(literal);
        }
        BFiniteType finiteType = new BFiniteType((BTypeSymbol) typeDefSymbol, valueSpace);

        BType definedType = symTable.noType;
        if (typeDefinition.typeNode != null) {
            definedType = symResolver.resolveTypeNode(typeDefinition.typeNode, env);
        }

        if (definedType == symTable.noType) {
            typeDefinition.symbol.type = finiteType;
        } else if (definedType.tag == TypeTags.UNION) {
            if (!valueSpace.isEmpty()) {
                ((BUnionType) definedType).memberTypes.add(finiteType);
            }
            typeDefinition.symbol.type = definedType;
        } else {
            if (!valueSpace.isEmpty()) {
                Set<BType> memberTypes = new HashSet<>();
                memberTypes.add(definedType);
                memberTypes.add(finiteType);
                typeDefinition.symbol.type = new BUnionType(null, memberTypes,
                        memberTypes.contains(symTable.nilType));
            } else {
                typeDefinition.symbol.type = definedType;
            }
        }

        defineSymbol(typeDefinition.pos, typeDefSymbol);
    }

    @Override
    public void visit(BLangEnum enumNode) {
        BTypeSymbol enumSymbol = Symbols.createEnumSymbol(Flags.asMask(enumNode.flagSet),
                names.fromIdNode(enumNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        enumNode.symbol = enumSymbol;
        defineSymbol(enumNode.pos, enumSymbol);

        BEnumType enumType = new BEnumType(enumSymbol, null);
        enumSymbol.type = enumType;

        SymbolEnv enumEnv = SymbolEnv.createPkgLevelSymbolEnv(enumNode, enumSymbol.scope, this.env);
        for (int i = 0; i < enumNode.enumerators.size(); i++) {
            BLangEnumerator enumerator = enumNode.enumerators.get(i);
            BVarSymbol enumeratorSymbol = new BVarSymbol(Flags.PUBLIC,
                    names.fromIdNode(enumerator.name), enumSymbol.pkgID, enumType, enumSymbol);
            enumeratorSymbol.docTag = DocTag.FIELD;
            enumerator.symbol = enumeratorSymbol;

            if (symResolver.checkForUniqueSymbol(enumerator.pos, enumEnv, enumeratorSymbol, enumeratorSymbol.tag)) {
                enumEnv.scope.define(enumeratorSymbol.name, enumeratorSymbol);
            }
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        BInvokableSymbol workerSymbol = Symbols.createWorkerSymbol(Flags.asMask(workerNode.flagSet),
                names.fromIdNode(workerNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        workerNode.symbol = workerSymbol;
        defineSymbolWithCurrentEnvOwner(workerNode.pos, workerSymbol);
    }

    @Override
    public void visit(BLangConnector connectorNode) {
        BConnectorSymbol conSymbol = Symbols.createConnectorSymbol(Flags.asMask(connectorNode.flagSet),
                names.fromIdNode(connectorNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        connectorNode.symbol = conSymbol;
        defineSymbol(connectorNode.pos, conSymbol);
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, conSymbol.scope, env);
        connectorNode.endpoints.forEach(ep -> defineNode(ep, connectorEnv));
    }

    @Override
    public void visit(BLangService serviceNode) {
        BServiceSymbol serviceSymbol = Symbols.createServiceSymbol(Flags.asMask(serviceNode.flagSet),
                names.fromIdNode(serviceNode.name), env.enclPkg.symbol.pkgID, serviceNode.type, env.scope.owner);
        serviceNode.symbol = serviceSymbol;
        serviceNode.symbol.type = new BServiceType(serviceSymbol);
        defineSymbol(serviceNode.pos, serviceSymbol);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        boolean validAttachedFunc = validateFuncReceiver(funcNode);
        if (funcNode.attachedOuterFunction) {
            SymbolEnv objectEnv = SymbolEnv.createObjectEnv(null, funcNode.receiver.type.
                    tsymbol.scope, env);
            BSymbol funcSymbol = symResolver.lookupSymbol(objectEnv, getFuncSymbolName(funcNode), SymTag.FUNCTION);
            if (funcSymbol == symTable.notFoundSymbol) {
                dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_FUNCTION, funcNode.name,
                        funcNode.receiver.type.tsymbol.name);
                // This is only to keep the flow running so that at the end there will be proper semantic errors
                funcNode.symbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                        getFuncSymbolName(funcNode), env.enclPkg.symbol.pkgID, null, env.scope.owner, true);
                funcNode.symbol.scope = new Scope(funcNode.symbol);
            } else {
                funcNode.symbol = (BInvokableSymbol) funcSymbol;
                if (funcNode.symbol.bodyExist) {
                    dlog.error(funcNode.pos, DiagnosticCode.IMPLEMENTATION_ALREADY_EXIST, funcNode.name);
                }
                validateAttachedFunction(funcNode, funcNode.receiver.type.tsymbol.name);
            }
            //TODO check function parameters and return types
            SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);

            invokableEnv.scope = funcNode.symbol.scope;
            defineObjectAttachedInvokableSymbolParams(funcNode, invokableEnv);

            if (env.enclPkg.objAttachedFunctions.contains(funcNode.symbol)) {
                dlog.error(funcNode.pos, DiagnosticCode.IMPLEMENTATION_ALREADY_EXIST, funcNode.name);
                return;
            }

            env.enclPkg.objAttachedFunctions.add(funcNode.symbol);

            funcNode.receiver.symbol = funcNode.symbol.receiverSymbol;
            return;
        }
        BInvokableSymbol funcSymbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                getFuncSymbolName(funcNode), env.enclPkg.symbol.pkgID, null, env.scope.owner, funcNode.body != null);
        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, env);
        defineInvokableSymbol(funcNode, funcSymbol, invokableEnv);
        // Define function receiver if any.
        if (funcNode.receiver != null) {
            defineAttachedFunctions(funcNode, funcSymbol, invokableEnv, validAttachedFunc);
        }
    }

    private void validateAttachedFunction(BLangFunction funcNode, Name objName) {
        SymbolEnv invokableEnv = SymbolEnv.createDummyEnv(funcNode, env.scope, env);
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
            defineNode(funcNode.restParam, invokableEnv);
            paramTypes.add(funcNode.restParam.symbol.type);
        }

        BInvokableType sourceType = (BInvokableType) funcNode.symbol.type;
        int flags = Flags.asMask(funcNode.flagSet);
        if (((flags & Flags.NATIVE) != (funcNode.symbol.flags & Flags.NATIVE))
                || ((flags & Flags.PUBLIC) != (funcNode.symbol.flags & Flags.PUBLIC))) {
            dlog.error(funcNode.pos, DiagnosticCode.CANNOT_FIND_MATCHING_INTERFACE, funcNode.name, objName);
            return;
        }

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

    private boolean namesMissMatch(List<BLangVariable> lhs, List<BVarSymbol> rhs) {
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

    private boolean namesMissMatchDef(List<BLangVariableDef> lhs, List<BVarSymbol> rhs) {
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

    void visitObjectAttachedFunctionParam(BLangVariable variable, SymbolEnv invokableEnv) {
        // assign the type to var type node
        if (variable.type == null) {
            variable.type = symResolver.resolveTypeNode(variable.typeNode, env);
        }

        visitObjectAttachedFunctionParamSymbol(variable, invokableEnv);
    }

    void visitObjectAttachedFunctionParamSymbol(BLangVariable variable, SymbolEnv invokableEnv) {
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
        variable.symbol.defaultValue = literal.value;
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        validateTransformerMappingTypes(transformerNode);

        boolean safeConversion = transformerNode.retParams.size() == 1;
        Name name = getTransformerSymbolName(transformerNode);
        BTransformerSymbol transformerSymbol = Symbols.createTransformerSymbol(Flags.asMask(transformerNode.flagSet),
                name, env.enclPkg.symbol.pkgID, null, safeConversion, env.scope.owner);
        transformerNode.symbol = transformerSymbol;

        // If this is a default transformer, check whether this transformer conflicts with a built-in conversion
        if (transformerNode.name.value.isEmpty()) {
            BType targetType = transformerNode.retParams.get(0).type;
            BSymbol symbol = symResolver.resolveConversionOperator(transformerNode.source.type, targetType);
            if (symbol != symTable.notFoundSymbol) {
                dlog.error(transformerNode.pos, DiagnosticCode.TRANSFORMER_CONFLICTS_WITH_CONVERSION,
                        transformerNode.source.type, targetType);
                return;
            }
        }

        // Define the transformer
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerSymbol.scope, env);

        transformerNode.symbol = transformerSymbol;
        defineSymbol(transformerNode.pos, transformerSymbol);
        transformerEnv.scope = transformerSymbol.scope;

        // Define transformer source.
        defineNode(transformerNode.source, transformerEnv);
    }

    @Override
    public void visit(BLangAction actionNode) {
        BInvokableSymbol actionSymbol = Symbols
                .createActionSymbol(Flags.asMask(actionNode.flagSet), names.fromIdNode(actionNode.name),
                        env.enclPkg.symbol.pkgID, null, env.scope.owner);
        SymbolEnv invokableEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        defineInvokableSymbol(actionNode, actionSymbol, invokableEnv);
        actionNode.endpoints.forEach(ep -> defineNode(ep, invokableEnv));
    }

    @Override
    public void visit(BLangResource resourceNode) {
        BInvokableSymbol resourceSymbol = Symbols
                .createResourceSymbol(Flags.asMask(resourceNode.flagSet), names.fromIdNode(resourceNode.name),
                        env.enclPkg.symbol.pkgID, null, env.scope.owner);
        SymbolEnv invokableEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        if (!resourceNode.getParameters().isEmpty()
                && resourceNode.getParameters().get(0) != null
                && resourceNode.getParameters().get(0).typeNode == null) {
            // This is endpoint variable. Setting temporary type for now till we find actual type at semantic phase.
            resourceNode.getParameters().get(0).type = symTable.endpointType;
        }
        defineInvokableSymbol(resourceNode, resourceSymbol, invokableEnv);
    }

    @Override
    public void visit(BLangVariable varNode) {
        // this is a field variable defined for object init function
        if (varNode.isField) {
            Name varName = names.fromIdNode(varNode.name);
            BSymbol symbol = symResolver.resolveObjectField(varNode.pos, env, varName,
                    env.enclObject.symbol.type.tsymbol);

            if (symbol == symTable.notFoundSymbol) {
                dlog.error(varNode.pos, DiagnosticCode.UNDEFINED_OBJECT_FIELD, varName, env.enclObject.name);
            }
            varNode.type = symbol.type;
            varName = getFieldSymbolName(((BLangFunction) env.enclInvokable).receiver, varNode);
            BVarSymbol varSymbol = defineVarSymbol(varNode.pos, varNode.flagSet, varNode.type, varName, env);

            // This is to identify variable when passing parameters TODO any alternative?
            varSymbol.field = true;
            varSymbol.originalName = names.fromIdNode(varNode.name);

            env.enclObject.initFunction.initFunctionStmts.put(symbol,
                    (BLangStatement) createAssignmentStmt(varNode, varSymbol, symbol));
            varSymbol.docTag = varNode.docTag;
            varNode.symbol = varSymbol;
            return;
        }
        // assign the type to var type node
        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }

        Name varName = names.fromIdNode(varNode.name);
        if (varName == Names.EMPTY || varName == Names.IGNORE) {
            // This is a variable created for a return type
            // e.g. function foo() (int);
            return;
        }

        //Check annotations attached to the variable
        if (varNode.annAttachments.size() > 0) {
            if (hasAnnotation(varNode.annAttachments, Names.ANNOTATION_FINAL.getValue())) {
                varNode.flagSet.add(Flag.FINAL);
            }
            if (hasAnnotation(varNode.annAttachments, Names.ANNOTATION_READONLY.getValue())) {
                varNode.flagSet.add(Flag.READONLY);
            }
        }

        BVarSymbol varSymbol = defineVarSymbol(varNode.pos, varNode.flagSet,
                varNode.type, varName, env);
        varSymbol.docTag = varNode.docTag;
        varNode.symbol = varSymbol;
    }

    @Override
    public void visit(BLangEndpoint endpoint) {
        BType varType = symResolver.resolveTypeNode(endpoint.endpointTypeNode, env);
        Name varName = names.fromIdNode(endpoint.name);
        endpoint.type = varType;
        endpoint.symbol = defineEndpointVarSymbol(endpoint.pos, endpoint.flagSet, varType, varName, env);
        endpointSPIAnalyzer.resolveEndpointSymbol(endpoint);
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
            case STRUCT:
                pkgNode.structs.add((BLangStruct) node);
                break;
            case OBJECT:
                pkgNode.objects.add((BLangObject) node);
                break;
            case ENUM:
                pkgNode.enums.add((BLangEnum) node);
                break;
            case TYPE_DEFINITION:
                pkgNode.typeDefinitions.add((BLangTypeDefinition) node);
                break;
            case CONNECTOR:
                pkgNode.connectors.add((BLangConnector) node);
                break;
            case SERVICE:
                pkgNode.services.add((BLangService) node);
                break;
            case VARIABLE:
                pkgNode.globalVars.add((BLangVariable) node);
                // TODO There are two kinds of package level variables, constant and regular variables.
                break;
            case ANNOTATION:
                // TODO
                pkgNode.annotations.add((BLangAnnotation) node);
                break;
            case RECORD:
                pkgNode.records.add((BLangRecord) node);
                break;
            case XMLNS:
                pkgNode.xmlnsList.add((BLangXMLNS) node);
                break;
            case TRANSFORMER:
                pkgNode.transformers.add((BLangTransformer) node);
                break;
            case ENDPOINT:
                pkgNode.globalEndpoints.add((BLangEndpoint) node);
                break;
        }
    }

    private void defineRecordFields(List<BLangRecord> recordNodes, SymbolEnv pkgEnv) {
        recordNodes.forEach(record -> {
            // Create record type
            SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(record, record.symbol.scope, pkgEnv);
            BStructType structType = (BStructType) record.symbol.type;
            structType.fields = record.fields.stream()
                    .peek(field -> defineNode(field, structEnv))
                    .map(field -> new BStructField(names.fromIdNode(field.name), field.symbol, field.expr != null))
                    .collect(Collectors.toList());
        });

        // define init function
        recordNodes.forEach(record -> {
            SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(record, record.symbol.scope, pkgEnv);
            defineRecordInitFunction(record, structEnv);
        });
    }

    private void defineStructFields(List<BLangStruct> structNodes, SymbolEnv pkgEnv) {
        structNodes.forEach(struct -> {
            // Create struct type
            SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(struct, struct.symbol.scope, pkgEnv);
            BStructType structType = (BStructType) struct.symbol.type;
            structType.fields = struct.fields.stream()
                    .peek(field -> defineNode(field, structEnv))
                    .map(field -> new BStructField(names.fromIdNode(field.name), field.symbol, field.expr != null))
                    .collect(Collectors.toList());
        });

        // define init function
        structNodes.forEach(struct -> {
            SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(struct, struct.symbol.scope, pkgEnv);
            defineStructInitFunction(struct, structEnv);
        });
    }

    private void defineObjectFields(List<? extends BLangObject> objectNodes, SymbolEnv pkgEnv) {
        objectNodes.forEach(object -> {
            // Create object type
            SymbolEnv objectEnv = SymbolEnv.createObjectEnv(object, object.symbol.scope, pkgEnv);
            BStructType objectType = (BStructType) object.symbol.type;
            objectType.fields = object.fields.stream()
                    .peek(field -> defineNode(field, objectEnv))
                    .map(field -> new BStructField(names.fromIdNode(field.name), field.symbol, field.expr != null))
                    .collect(Collectors.toList());
        });
    }

    private void defineObjectMembers(List<? extends BLangObject> objects, SymbolEnv pkgEnv) {
        objects.forEach(obj -> {
            SymbolEnv objEnv = SymbolEnv.createObjectEnv(obj, obj.symbol.scope, pkgEnv);
            defineObjectInitFunction(obj, objEnv);
            obj.functions.forEach(f -> defineNode(f, objEnv));
        });
    }

    private void defineConnectorMembers(List<BLangConnector> connectors, SymbolEnv pkgEnv) {
        connectors.forEach(connector -> {
            SymbolEnv conEnv = SymbolEnv.createConnectorEnv(connector, connector.symbol.scope, pkgEnv);

            connector.varDefs.forEach(varDef -> defineNode(varDef.var, conEnv));
            defineConnectorInitFunction(connector, conEnv);
            connector.actions.stream()
                    .peek(action -> action.flagSet.add(Flag.PUBLIC))
                    .forEach(action -> defineNode(action, conEnv));
        });
    }

    private void defineConnectorParams(List<BLangConnector> connectors, SymbolEnv pkgEnv) {
        connectors.forEach(connector -> {
            SymbolEnv conEnv = SymbolEnv.createConnectorEnv(connector, connector.symbol.scope, pkgEnv);
            defineConnectorSymbolParams(connector, connector.symbol, conEnv);
        });
    }

    private void defineServiceMembers(List<BLangService> services, SymbolEnv pkgEnv) {
        services.forEach(service -> {
            SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(service, service.symbol.scope, pkgEnv);
            service.nsDeclarations.forEach(xmlns -> defineNode(xmlns, serviceEnv));
            service.vars.forEach(varDef -> defineNode(varDef.var, serviceEnv));
            defineServiceInitFunction(service, serviceEnv);
            service.resources.stream()
                    .peek(action -> action.flagSet.add(Flag.PUBLIC))
                    .forEach(resource -> defineNode(resource, serviceEnv));
        });
    }

    private void defineInvokableSymbol(BLangInvokableNode invokableNode, BInvokableSymbol funcSymbol,
                                       SymbolEnv invokableEnv) {
        invokableNode.symbol = funcSymbol;
        defineSymbol(invokableNode.pos, funcSymbol);
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
                                varSymbol.defaultValue = literal.value;
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

    private void defineConnectorSymbolParams(BLangConnector connectorNode, BConnectorSymbol symbol,
                                             SymbolEnv connectorEnv) {
    }

    private void defineSymbol(DiagnosticPos pos, BSymbol symbol) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol, symbol.tag)) {
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
        BVarSymbol varSymbol;

        if (varType.tag == TypeTags.INVOKABLE) {
            varSymbol = new BInvokableSymbol(SymTag.VARIABLE, Flags.asMask(flagSet), varName,
                    env.enclPkg.symbol.pkgID, varType, enclScope.owner);
        } else {
            varSymbol = new BVarSymbol(Flags.asMask(flagSet), varName,
                    env.enclPkg.symbol.pkgID, varType, enclScope.owner);
        }

        // Add it to the enclosing scope
        // Find duplicates in current scope only
        if (!symResolver.checkForUniqueSymbolInCurrentScope(pos, env, varSymbol, SymTag.VARIABLE_NAME)) {
            varSymbol.type = symTable.errType;
        }
        enclScope.define(varSymbol.name, varSymbol);
        return varSymbol;
    }

    public BEndpointVarSymbol defineEndpointVarSymbol(DiagnosticPos pos, Set<Flag> flagSet, BType varType,
                                                      Name varName, SymbolEnv env) {
        // Create variable symbol
        Scope enclScope = env.scope;
        BEndpointVarSymbol varSymbol = new BEndpointVarSymbol(Flags.asMask(flagSet), varName, env.enclPkg.symbol
                .pkgID, varType, enclScope.owner);
        Scope.ScopeEntry scopeEntry = enclScope.entries.get(names.fromString("$" + varName.value));
        if (scopeEntry != null && scopeEntry.symbol != null && scopeEntry.symbol instanceof BVarSymbol) {
            varSymbol.docTag = ((BVarSymbol) scopeEntry.symbol).docTag;
        }

        // Add it to the enclosing scope
        // Find duplicates
        if (!symResolver.checkForUniqueSymbol(pos, env, varSymbol, SymTag.VARIABLE_NAME)) {
            varSymbol.type = symTable.errType;
        }
        enclScope.define(varSymbol.name, varSymbol);
        return varSymbol;
    }

    private void defineConnectorInitFunction(BLangConnector connector, SymbolEnv conEnv) {
        BLangFunction initFunction = createInitFunction(connector.pos, connector.getName().getValue(),
                Names.INIT_FUNCTION_SUFFIX);
        //Add connector as a parameter to the init function
        BLangVariable param = (BLangVariable) TreeBuilder.createVariableNode();
        param.pos = connector.pos;
        param.setName(this.createIdentifier(Names.CONNECTOR.getValue()));
        BLangUserDefinedType connectorType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        connectorType.pos = connector.pos;
        connectorType.typeName = connector.name;
        connectorType.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        param.setTypeNode(connectorType);
        initFunction.addParameter(param);
        //Add connector level variables to the init function
        connector.varDefs.stream().filter(f -> f.var.expr != null)
                .forEachOrdered(v -> initFunction.body.addStatement(createAssignmentStmt(v.var)));

        addInitReturnStatement(initFunction.body);
        connector.initFunction = initFunction;

        BLangAction initAction = createNativeInitAction(connector.pos);
        connector.initAction = initAction;

        defineNode(connector.initFunction, conEnv);
        defineNode(connector.initAction, conEnv);
        connector.symbol.initFunctionSymbol = connector.initFunction.symbol;
    }

    private void defineObjectInitFunction(BLangObject object, SymbolEnv conEnv) {
        BLangFunction initFunction = object.initFunction;
        if (initFunction == null) {
            initFunction = createInitFunction(object.pos, "", Names.OBJECT_INIT_SUFFIX);
        }

        initFunction.attachedFunction = true;

        //Set cached receiver to the init function
        final BLangVariable receiver  = createReceiver(object.pos, object.name);
        initFunction.receiver = receiver;
        object.functions.forEach(f -> f.setReceiver(createReceiver(object.pos, object.name)));

        initFunction.flagSet.add(Flag.ATTACHED);

        object.initFunction = initFunction;

        defineNode(object.initFunction, conEnv);
    }

    private void defineStructInitFunction(BLangStruct struct, SymbolEnv conEnv) {
        if (struct.initFunction == null) {
            struct.initFunction = createInitFunction(struct.pos, struct.name.value, Names.INIT_FUNCTION_SUFFIX);
        }

        struct.initFunction.receiver = createReceiver(struct.pos, struct.name);
        struct.initFunction.attachedFunction = true;
        struct.initFunction.flagSet.add(Flag.ATTACHED);

        // Adding struct level variables to the init function is done at desugar phase

        defineNode(struct.initFunction, conEnv);
    }

    private void defineRecordInitFunction(BLangRecord record, SymbolEnv conEnv) {
        if (record.initFunction == null) {
            record.initFunction = createInitFunction(record.pos, record.name.value, Names.INIT_FUNCTION_SUFFIX);
        }

        record.initFunction.receiver = createReceiver(record.pos, record.name);
        record.initFunction.attachedFunction = true;
        record.initFunction.flagSet.add(Flag.ATTACHED);

        // Adding record level variables to the init function is done at desugar phase

        defineNode(record.initFunction, conEnv);
    }

    private void defineServiceInitFunction(BLangService service, SymbolEnv conEnv) {
        BLangFunction initFunction = createInitFunction(service.pos, service.getName().getValue(),
                Names.INIT_FUNCTION_SUFFIX);
        service.initFunction = initFunction;
        defineNode(service.initFunction, conEnv);
    }

    private void defineAttachedFunctions(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                         SymbolEnv invokableEnv, boolean isValidAttachedFunc) {
        BTypeSymbol typeSymbol = funcNode.receiver.type.tsymbol;

        // Check whether there exists a struct field with the same name as the function name.
        if (isValidAttachedFunc) {
            if (typeSymbol.tag == SymTag.STRUCT) {
                validateFunctionsAttachedToStructs(funcNode, funcSymbol, invokableEnv);
            } else if (typeSymbol.tag == SymTag.OBJECT) {
                validateFunctionsAttachedToObject(funcNode, funcSymbol, invokableEnv);
            } else if (typeSymbol.tag == SymTag.RECORD) {
                validateFunctionsAttachedToStructs(funcNode, funcSymbol, invokableEnv);
            }
        }

        defineNode(funcNode.receiver, invokableEnv);
        funcSymbol.receiverSymbol = funcNode.receiver.symbol;
    }

    private void validateFunctionsAttachedToStructs(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                                    SymbolEnv invokableEnv) {
        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        BStructSymbol structSymbol = (BStructSymbol) funcNode.receiver.type.tsymbol;
        BSymbol symbol = symResolver.lookupMemberSymbol(
                funcNode.receiver.pos, structSymbol.scope, invokableEnv,
                names.fromIdNode(funcNode.name), SymTag.VARIABLE);
        if (symbol != symTable.notFoundSymbol) {
            dlog.error(funcNode.pos, DiagnosticCode.STRUCT_FIELD_AND_FUNC_WITH_SAME_NAME,
                    funcNode.name.value, funcNode.receiver.type.toString());
            return;
        }

        BStructType structType = (BStructType) funcNode.receiver.type;
        BAttachedFunction attachedFunc = new BAttachedFunction(
                names.fromIdNode(funcNode.name), funcSymbol, funcType);
        structSymbol.attachedFuncs.add(attachedFunc);

        if (funcNode.name.value.equals(structType.tsymbol.name.value + Names.INIT_FUNCTION_SUFFIX.value)) {
            structSymbol.defaultsValuesInitFunc = attachedFunc;
            return;
        }

        // Check whether this attached function is a struct initializer.
        if (!structType.tsymbol.name.value.equals(funcNode.name.value)) {
            // Not a struct initializer.
            return;
        }

        if (!funcNode.requiredParams.isEmpty() || funcNode.returnTypeNode.type != symTable.nilType) {
            dlog.error(funcNode.pos, DiagnosticCode.INVALID_STRUCT_INITIALIZER_FUNCTION,
                    funcNode.name.value, funcNode.receiver.type.toString());
        }
        structSymbol.initializerFunc = attachedFunc;
    }

    private void validateFunctionsAttachedToObject(BLangFunction funcNode, BInvokableSymbol funcSymbol,
                                                    SymbolEnv invokableEnv) {

        BInvokableType funcType = (BInvokableType) funcSymbol.type;
        BStructSymbol objectSymbol = (BStructSymbol) funcNode.receiver.type.tsymbol;
        BSymbol symbol = symResolver.lookupMemberSymbol(funcNode.receiver.pos, objectSymbol.scope, invokableEnv,
                names.fromIdNode(funcNode.name), SymTag.VARIABLE);
        if (symbol != symTable.notFoundSymbol) {
            dlog.error(funcNode.pos, DiagnosticCode.STRUCT_FIELD_AND_FUNC_WITH_SAME_NAME,
                    funcNode.name.value, funcNode.receiver.type.toString());
            return;
        }

        BAttachedFunction attachedFunc = new BAttachedFunction(
                names.fromIdNode(funcNode.name), funcSymbol, funcType);
        objectSymbol.attachedFuncs.add(attachedFunc);

        // Check whether this attached function is a object initializer.
        if (!Names.OBJECT_INIT_SUFFIX.value.equals(funcNode.name.value)) {
            // Not a object initializer.
            return;
        }

        if (funcNode.returnTypeNode.type != symTable.nilType) {
            //TODO change message
            dlog.error(funcNode.pos, DiagnosticCode.INVALID_STRUCT_INITIALIZER_FUNCTION,
                    funcNode.name.value, funcNode.receiver.type.toString());
        }
        objectSymbol.initializerFunc = attachedFunc;
    }

    private StatementNode createAssignmentStmt(BLangVariable variable, BVarSymbol varSym, BSymbol fieldVar) {
        //Create LHS reference variable
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = variable.pos;
        varRef.variableName = (BLangIdentifier) createIdentifier(fieldVar.name.getValue());
        varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        //Create RHS variable reference
        BLangSimpleVarRef exprVar = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        exprVar.pos = variable.pos;
        exprVar.variableName = (BLangIdentifier) createIdentifier(varSym.name.getValue());
        exprVar.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        //Create assignment statement
        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = exprVar;
        assignmentStmt.pos = variable.pos;
        assignmentStmt.setVariable(varRef);
        return assignmentStmt;
    }

    private StatementNode createAssignmentStmt(BLangVariable variable) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder
                .createSimpleVariableReferenceNode();
        varRef.pos = variable.pos;
        varRef.variableName = variable.name;
        varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = variable.expr;
        assignmentStmt.pos = variable.pos;
        assignmentStmt.setVariable(varRef);
        return assignmentStmt;
    }

    private StatementNode createObjectAssignmentStmt(BLangVariable variable, BLangVariable receiver) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder
                .createSimpleVariableReferenceNode();
        varRef.pos = variable.pos;
        varRef.variableName = receiver.name;
        varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldBasedAccess.pos = variable.pos;
        fieldBasedAccess.field = variable.name;
        fieldBasedAccess.expr = varRef;
        fieldBasedAccess.fieldKind = FieldKind.SINGLE;
        fieldBasedAccess.safeNavigate = false;

        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = variable.expr;
        assignmentStmt.pos = variable.pos;
        assignmentStmt.setVariable(fieldBasedAccess);
        return assignmentStmt;
    }

    private BLangVariable createReceiver(DiagnosticPos pos, BLangIdentifier name) {
        BLangVariable receiver = (BLangVariable) TreeBuilder.createVariableNode();
        receiver.pos = pos;
        IdentifierNode identifier = createIdentifier(Names.SELF.getValue());
        receiver.setName(identifier);
        receiver.docTag = DocTag.RECEIVER;

        BLangUserDefinedType structTypeNode = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        structTypeNode.pkgAlias = new BLangIdentifier();
        structTypeNode.typeName = name;
        receiver.setTypeNode(structTypeNode);
        return receiver;
    }

    private BLangExpressionStmt createInitFuncInvocationStmt(BLangImportPackage importPackage,
                                                             BInvokableSymbol initFunctionSymbol) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = importPackage.pos;
        invocationNode.addWS(importPackage.getWS());
        BLangIdentifier funcName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        funcName.value = initFunctionSymbol.name.value;
        invocationNode.name = funcName;
        invocationNode.pkgAlias = importPackage.alias;

        BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = importPackage.pos;
        exprStmt.addWS(importPackage.getWS());
        exprStmt.expr = invocationNode;
        return exprStmt;
    }

    private void definePackageInitFunctions(BLangPackage pkgNode, SymbolEnv env) {
        BLangFunction initFunction = pkgNode.initFunction;
        // Add package level namespace declarations to the init function
        pkgNode.xmlnsList.forEach(xmlns -> {
            initFunction.body.addStatement(createNamespaceDeclrStatement(xmlns));
        });

        defineNode(pkgNode.initFunction, env);
        pkgNode.symbol.initFunctionSymbol = pkgNode.initFunction.symbol;

        addInitReturnStatement(pkgNode.startFunction.body);
        defineNode(pkgNode.startFunction, env);
        pkgNode.symbol.startFunctionSymbol = pkgNode.startFunction.symbol;

        addInitReturnStatement(pkgNode.stopFunction.body);
        defineNode(pkgNode.stopFunction, env);
        pkgNode.symbol.stopFunctionSymbol = pkgNode.stopFunction.symbol;
    }

    private void createPackageInitFunctions(BLangPackage pkgNode) {
        String alias = pkgNode.symbol.pkgID.bvmAlias();
        pkgNode.initFunction = createInitFunction(pkgNode.pos, alias,
                Names.INIT_FUNCTION_SUFFIX);
        pkgNode.startFunction = createInitFunction(pkgNode.pos, alias,
                Names.START_FUNCTION_SUFFIX);
        pkgNode.stopFunction = createInitFunction(pkgNode.pos, alias,
                Names.STOP_FUNCTION_SUFFIX);
    }

    private BLangFunction createInitFunction(DiagnosticPos pos, String name, Name sufix) {
        BLangFunction initFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        initFunction.setName(createIdentifier(name + sufix.getValue()));
        initFunction.flagSet = EnumSet.of(Flag.PUBLIC);
        initFunction.pos = pos;

        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = TypeKind.NIL;
        initFunction.returnTypeNode = typeNode;

        //Create body of the init function
        BLangBlockStmt body = (BLangBlockStmt) TreeBuilder.createBlockNode();
        body.pos = pos;
        initFunction.setBody(body);
        return initFunction;
    }

    private BLangAction createNativeInitAction(DiagnosticPos pos) {
        BLangAction initAction = (BLangAction) TreeBuilder.createActionNode();
        initAction.setName(createIdentifier(Names.INIT_ACTION_SUFFIX.getValue()));
        initAction.flagSet = EnumSet.of(Flag.NATIVE, Flag.PUBLIC);
        initAction.pos = pos;
        return initAction;
    }

    private IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    private void addInitReturnStatement(BLangBlockStmt bLangBlockStmt) {
        BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(bLangBlockStmt.pos, symTable.nilType);
        bLangBlockStmt.addStatement(returnStmt);
    }

    private BLangXMLNSStatement createNamespaceDeclrStatement(BLangXMLNS xmlns) {
        BLangXMLNSStatement xmlnsStmt = (BLangXMLNSStatement) TreeBuilder.createXMLNSDeclrStatementNode();
        xmlnsStmt.xmlnsDecl = xmlns;
        xmlnsStmt.pos = xmlns.pos;
        return xmlnsStmt;
    }

    private boolean validateFuncReceiver(BLangFunction funcNode) {
        if (funcNode.receiver == null) {
            return true;
        }

        BType varType = symResolver.resolveTypeNode(funcNode.receiver.typeNode, env);
        funcNode.receiver.type = varType;
        if (varType.tag == TypeTags.ERROR) {
            return true;
        }

        if (varType.tag != TypeTags.BOOLEAN
                && varType.tag != TypeTags.STRING
                && varType.tag != TypeTags.INT
                && varType.tag != TypeTags.FLOAT
                && varType.tag != TypeTags.BLOB
                && varType.tag != TypeTags.JSON
                && varType.tag != TypeTags.XML
                && varType.tag != TypeTags.MAP
                && varType.tag != TypeTags.TABLE
                && varType.tag != TypeTags.STREAM
                && varType.tag != TypeTags.FUTURE
                && varType.tag != TypeTags.STRUCT) {
            dlog.error(funcNode.receiver.pos, DiagnosticCode.FUNC_DEFINED_ON_NOT_SUPPORTED_TYPE,
                    funcNode.name.value, varType.toString());
            return false;
        }

        if (!this.env.enclPkg.symbol.pkgID.equals(varType.tsymbol.pkgID)) {
            dlog.error(funcNode.receiver.pos, DiagnosticCode.FUNC_DEFINED_ON_NON_LOCAL_TYPE,
                    funcNode.name.value, varType.toString());
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

    private Name getFieldSymbolName(BLangVariable receiver, BLangVariable variable) {
        return names.fromString(Symbols.getAttachedFuncSymbolName(
                receiver.type.tsymbol.name.value, variable.name.value));
    }

    private Name getTransformerSymbolName(BLangTransformer transformerNode) {
        if (transformerNode.name.value.isEmpty()) {
            return names.fromString(Names.TRANSFORMER.value + "<" + transformerNode.source.type + ","
                    + transformerNode.retParams.get(0).type + ">");
        }
        return names.fromIdNode(transformerNode.name);
    }

    private void populateInitFunctionInvocation(BLangImportPackage importPkgNode, BPackageSymbol pkgSymbol) {
        if (pkgSymbol.initFunctionsInvoked) {
            return;
        }

        ((BLangPackage) env.node).initFunction.body
                .addStatement(createInitFuncInvocationStmt(importPkgNode, pkgSymbol.initFunctionSymbol));
        ((BLangPackage) env.node).startFunction.body
                .addStatement(createInitFuncInvocationStmt(importPkgNode, pkgSymbol.startFunctionSymbol));
        ((BLangPackage) env.node).stopFunction.body
                .addStatement(createInitFuncInvocationStmt(importPkgNode, pkgSymbol.stopFunctionSymbol));
        pkgSymbol.initFunctionsInvoked = true;
    }

    private void validateTransformerMappingTypes(BLangTransformer transformerNode) {
        BType varType = symResolver.resolveTypeNode(transformerNode.source.typeNode, env);
        transformerNode.source.type = varType;

        symResolver.resolveTypeNode(transformerNode.returnTypeNode, env);
//        transformerNode.retParams.forEach(returnParams -> {
//            BType targetType = symResolver.resolveTypeNode(returnParams.typeNode, env);
//            returnParams.type = targetType;
//        });
    }

    private void defineTransformerMembers(List<BLangTransformer> transformers, SymbolEnv pkgEnv) {
        transformers.forEach(transformer -> {
            SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformer, transformer.symbol.scope, pkgEnv);
            defineInvokableSymbolParams(transformer, transformer.symbol, transformerEnv);
        });
    }
}
