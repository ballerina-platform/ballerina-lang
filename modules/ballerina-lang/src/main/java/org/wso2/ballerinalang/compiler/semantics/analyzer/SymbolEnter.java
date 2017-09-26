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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType.BStructField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NodeUtils;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.model.tree.NodeKind.IMPORT;

/**
 * @since 0.94
 */
public class SymbolEnter extends BLangNodeVisitor {

    private static final CompilerContext.Key<SymbolEnter> SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    private PackageLoader pkgLoader;
    private SymbolTable symTable;
    private Names names;
    private SymbolResolver symResolver;

    private BLangPackage rootPkgNode;

    private SymbolEnv env;
    public Map<BPackageSymbol, SymbolEnv> packageEnvs = new HashMap<>();

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

        this.rootPkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.rootPkgNode.symbol = symTable.rootPkgSymbol;
    }

    public BPackageSymbol definePackage(BLangPackage pkgNode) {
        populatePackageNode(pkgNode);

        defineNode(pkgNode, null);
        return pkgNode.symbol;
    }


    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.phase != CompilerPhase.DEFINE) {
            return;
        }
        // Create PackageSymbol.
        BPackageSymbol pSymbol = createPackageSymbol(pkgNode);
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(pkgNode, pSymbol.scope);
        packageEnvs.put(pSymbol, pkgEnv);

        // visit the package node recursively and define all package level symbols.
        // And maintain a list of created package symbols.
        pkgNode.imports.forEach(importNode -> defineNode(importNode, pkgEnv));

        // Define struct nodes.
        pkgNode.structs.forEach(struct -> defineNode(struct, pkgEnv));

        // Define connector nodes.
        pkgNode.connectors.forEach(con -> defineNode(con, pkgEnv));

        // Define connector action nodes.
        defineActions(pkgNode.connectors, pkgEnv);

        // Define function nodes.
        pkgNode.functions.forEach(func -> defineNode(func, pkgEnv));

        // Define service and resource nodes.
        defineServices(pkgNode.services, pkgEnv);

        // Define struct field nodes.
        defineStructFields(pkgNode.structs, pkgEnv);

        pkgNode.globalVars.forEach(var -> defineNode(var, pkgEnv));

        definePackageInitFunction(pkgNode, pkgEnv);
        pkgNode.phase = CompilerPhase.TYPE_CHECK;
    }

    public void visit(BLangImportPackage importPkgNode) {
        BLangPackage pkgNode = pkgLoader.loadPackage(importPkgNode.pkgNameComps, importPkgNode.version);
        // Create import package symbol
        BPackageSymbol pkgSymbol = pkgNode.symbol;
        importPkgNode.symbol = pkgSymbol;
        this.env.scope.define(names.fromIdNode(importPkgNode.alias), pkgSymbol);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = Symbols.createStructSymbol(Flags.asMask(structNode.flagSet),
                names.fromIdNode(structNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        structNode.symbol = structSymbol;
        defineSymbol(structNode.pos, structSymbol);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        BInvokableSymbol workerSymbol = Symbols.createWorkerSymbol(Flags.asMask(workerNode.flagSet),
                names.fromIdNode(workerNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        workerNode.symbol = workerSymbol;
        defineSymbolWithCurrentEnvOwner(workerNode.pos, workerSymbol);
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol conSymbol = Symbols.createConnectorSymbol(Flags.asMask(connectorNode.flagSet),
                names.fromIdNode(connectorNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        connectorNode.symbol = conSymbol;
        defineConnectorInitFunction(connectorNode);
        defineSymbol(connectorNode.pos, conSymbol);
    }

    public void visit(BLangConnector connectorNode) {
        BTypeSymbol conSymbol = Symbols.createConnectorSymbol(Flags.asMask(connectorNode.flagSet),
                names.fromIdNode(connectorNode.name), null, env.scope.owner);
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, conSymbol.scope, env);
        defineConnectorSymbol(connectorNode, conSymbol, connectorEnv);
        conSymbol.pkgName = env.scope.owner.name;
        defineConnectorInitFunction(connectorNode);
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = Symbols.createServiceSymbol(Flags.asMask(serviceNode.flagSet),
                names.fromIdNode(serviceNode.name), env.enclPkg.symbol.pkgID, null, env.scope.owner);
        serviceNode.symbol = serviceSymbol;
        defineServiceInitFunction(serviceNode);
        defineSymbol(serviceNode.pos, serviceSymbol);
    }

    public void visit(BLangFunction funcNode) {
        BInvokableSymbol funcSymbol = Symbols
                .createFunctionSymbol(Flags.asMask(funcNode.flagSet), names.fromIdNode(funcNode.name),
                        env.enclPkg.symbol.pkgID, null, env.scope.owner);
        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, env);
        defineInvokableSymbol(funcNode, funcSymbol, invokableEnv);
    }

    public void visit(BLangAction actionNode) {
        BInvokableSymbol actionSymbol = Symbols
                .createActionSymbol(Flags.asMask(actionNode.flagSet), names.fromIdNode(actionNode.name),
                        env.enclPkg.symbol.pkgID, null, env.scope.owner);
        SymbolEnv invokableEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        defineInvokableSymbol(actionNode, actionSymbol, invokableEnv);
    }

    public void visit(BLangAction actionNode) {
        BInvokableSymbol actionSymbol = Symbols
                .createActionSymbol(Flags.asMask(actionNode.flagSet), names.fromIdNode(actionNode.name), null,
                        env.scope.owner);
        BLangVariable param = (BLangVariable) TreeBuilder.createVariableNode();
        param.pos = env.node.pos;
        param.setName(this.createIdentifier(Names.CONNECTOR.getValue()));
        BLangUserDefinedType connectorType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        connectorType.pos = env.node.pos;
        connectorType.typeName = ((BLangConnector) env.node).name;
        param.setTypeNode(connectorType);
        List<BLangVariable> params = new ArrayList<>();
        params.add(param);
        params.addAll(actionNode.params);
        actionNode.params = params;
        SymbolEnv invokableEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        defineInvokableSymbol(actionNode, actionSymbol, invokableEnv);
    }

    public void visit(BLangResource resourceNode) {
        BInvokableSymbol resourceSymbol = Symbols
                .createResourceSymbol(Flags.asMask(resourceNode.flagSet), names.fromIdNode(resourceNode.name),
                        env.enclPkg.symbol.pkgID, null, env.scope.owner);
        SymbolEnv invokableEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        defineInvokableSymbol(resourceNode, resourceSymbol, invokableEnv);
    }

    public void visit(BLangVariable varNode) {
        // assign the type to var type node
        BType varType = symResolver.resolveTypeNode(varNode.typeNode, env);
        varNode.type = varType;

        Name varName = names.fromIdNode(varNode.name);
        if (varName == Names.EMPTY) {
            // This is a variable created for a return type
            // e.g. function foo() (int);
            return;
        }

        varNode.symbol = defineVarSymbol(varNode.pos, varNode.flagSet, varType, varName, env);
    }


    // Private methods

    private BPackageSymbol createPackageSymbol(BLangPackage pkgNode) {
        BPackageSymbol pSymbol;
        if (pkgNode.pkgDecl == null) {
            pSymbol = new BPackageSymbol(PackageID.DEFAULT, symTable.rootPkgSymbol);
        } else {
            PackageID pkgID = NodeUtils.getPackageID(names, pkgNode.pkgDecl.pkgNameComps, pkgNode.pkgDecl.version);
            pSymbol = new BPackageSymbol(pkgID, symTable.rootPkgSymbol);
        }
        pkgNode.symbol = pSymbol;
        pSymbol.scope = new Scope(pSymbol);
        return pSymbol;
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
        // TODO Check whether package in 'compUnit' is equal to the package in 'pkgNode'

        // TODO If the pkgID is null, then assign an unnamed package/default package.
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
            case PACKAGE_DECLARATION:
                // TODO verify the rules..
                pkgNode.pkgDecl = (BLangPackageDeclaration) node;
                break;
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
                break;
            case XMLNS:
                // TODO
                break;
        }
    }

    private void defineStructFields(List<BLangStruct> structNodes, SymbolEnv pkgEnv) {
        structNodes.forEach(struct -> {
            // Create struct type
            BStructType structType = new BStructType((BTypeSymbol) struct.symbol, new ArrayList<>());
            struct.symbol.type = structType;

            SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(struct, struct.symbol.scope, pkgEnv);
            structType.fields = struct.fields.stream()
                    .peek(field -> defineNode(field, structEnv))
                    .map(field -> new BStructField(names.fromIdNode(field.name), field.type))
                    .collect(Collectors.toList());
        });
    }

    private void defineServices(List<BLangService> serviceNodes, SymbolEnv pkgEnv) {
        serviceNodes.forEach(service -> {
            defineNode(service, pkgEnv);
            SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(service, service.symbol.scope, pkgEnv);
            service.resources.forEach(resource -> defineNode(resource, serviceEnv));
        });
    }

    private void defineActions(List<BLangConnector> connectors, SymbolEnv pkgEnv) {
        connectors.forEach(connector -> {
            SymbolEnv conEnv = SymbolEnv.createConnectorEnv(connector, connector.symbol.scope, pkgEnv);
            connector.actions.forEach(action -> defineNode(action, conEnv));
        });
    }

    private void defineInvokableSymbol(BLangInvokableNode invokableNode, BInvokableSymbol funcSymbol,
                                       SymbolEnv invokableEnv) {
        invokableNode.symbol = funcSymbol;
        defineSymbol(invokableNode.pos, funcSymbol);
        invokableEnv.scope = funcSymbol.scope;
        defineInvokableSymbolParams(invokableNode, funcSymbol, invokableEnv);
    }

    private void defineInvokableSymbolParams(BLangInvokableNode invokableNode, BInvokableSymbol symbol,
                                             SymbolEnv invokableEnv) {
        List<BVarSymbol> paramSymbols =
                invokableNode.params.stream()
                        .peek(varNode -> defineNode(varNode, invokableEnv))
                        .map(varNode -> varNode.symbol)
                        .collect(Collectors.toList());

        List<BVarSymbol> retParamSymbols =
                invokableNode.retParams.stream()
                        .peek(varNode -> defineNode(varNode, invokableEnv))
                        .filter(varNode -> varNode.symbol != null)
                        .map(varNode -> varNode.symbol)
                        .collect(Collectors.toList());

        symbol.params = paramSymbols;
        symbol.retParams = retParamSymbols;

        // Create function type
        List<BType> paramTypes = paramSymbols.stream()
                .map(paramSym -> paramSym.type)
                .collect(Collectors.toList());
        List<BType> retTypes = invokableNode.retParams.stream()
                .map(varNode -> varNode.typeNode.type)
                .collect(Collectors.toList());

        symbol.type = new BInvokableType(paramTypes, retTypes, null);
    }

    private void defineConnectorSymbol(BLangConnector connectorNode, BTypeSymbol connectorSymbol,
                                       SymbolEnv connectorEnv) {
        connectorNode.symbol = connectorSymbol;
        defineSymbol(connectorNode.pos, connectorSymbol);
        connectorEnv.scope = connectorSymbol.scope;
        defineConnectorSymbolParams(connectorNode, connectorSymbol, connectorEnv);
    }

    private void defineConnectorSymbolParams(BLangConnector connectorNode, BTypeSymbol symbol,
                                             SymbolEnv connectorEnv) {
        List<BVarSymbol> paramSymbols =
                connectorNode.params.stream()
                        .peek(varNode -> defineNode(varNode, connectorEnv))
                        .map(varNode -> varNode.symbol)
                        .collect(Collectors.toList());

        symbol.params = paramSymbols;

        // Create function type
        List<BType> paramTypes = paramSymbols.stream()
                .map(paramSym -> paramSym.type)
                .collect(Collectors.toList());

        symbol.type = new BConnectorType(paramTypes, symbol);
    }

    private void defineSymbol(DiagnosticPos pos, BSymbol symbol) {
        symbol.scope = new Scope(symbol);
        if (symResolver.checkForUniqueSymbol(pos, env, symbol)) {
            env.scope.define(symbol.name, symbol);
        }
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
        BVarSymbol varSymbol = new BVarSymbol(Flags.asMask(flagSet), varName,
                env.enclPkg.symbol.pkgID, varType, enclScope.owner);

        // Add it to the enclosing scope
        // Find duplicates
        if (symResolver.checkForUniqueSymbol(pos, env, varSymbol)) {
            enclScope.define(varSymbol.name, varSymbol);
        }
        return varSymbol;
    }

    public void defineNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        node.accept(this);
        this.env = prevEnv;
    }

    private void defineConnectorInitFunction(BLangConnector connector) {
        BLangFunction initFunction = createInitFunction(connector.pos, connector.getName().getValue());
        //Add connector as a parameter to the init function
        BLangVariable param = (BLangVariable) TreeBuilder.createVariableNode();
        param.pos = connector.pos;
        param.setName(this.createIdentifier(Names.CONNECTOR.getValue()));
        BLangUserDefinedType connectorType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        connectorType.pos = connector.pos;
        connectorType.typeName = connector.name;
        param.setTypeNode(connectorType);
        initFunction.addParameter(param);
        //Add connector level variables to the init function
        for (BLangVariableDef variableDef : connector.getVariableDefs()) {
            initFunction.body.addStatement(variableDef);
        }
        addInitReturnStatement(initFunction.body);
        connector.initFunction = initFunction;
        defineNode(connector.initFunction, env);
        connector.symbol.initFunction = initFunction.symbol;
    }

    private void defineServiceInitFunction(BLangService service) {
        BLangFunction initFunction = createInitFunction(service.pos, service.getName().getValue());
        //Add service level variables to the init function
        for (BLangVariableDef variableDef : service.getVariables()) {
            initFunction.body.addStatement(variableDef);
        }
        addInitReturnStatement(initFunction.body);
        service.initFunction = initFunction;
        defineNode(service.initFunction, env);
    }

    private void definePackageInitFunction(BLangPackage pkgNode, SymbolEnv env) {
        BLangFunction initFunction = createInitFunction(pkgNode.pos, pkgNode.symbol.getName().getValue());
        //Add global variables to the init function
        for (BLangVariable variable : pkgNode.getGlobalVariables()) {
            initFunction.body.addStatement(createVariableDefStatement(variable.pos, variable));
        }
        addInitReturnStatement(initFunction.body);
        pkgNode.initFunction = initFunction;
        defineNode(pkgNode.initFunction, env);
    }

    private BLangFunction createInitFunction(DiagnosticPos pos, String name) {
        BLangFunction initFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        initFunction.setName(createIdentifier(name + Names.INIT_FUNCTION_SUFFIX.getValue()));
        initFunction.pos = pos;
        //Create body of the init function
        BLangBlockStmt body = (BLangBlockStmt) TreeBuilder.createBlockNode();
        body.pos = pos;
        initFunction.setBody(body);
        return initFunction;
    }

    private BLangVariableDef createVariableDefStatement(DiagnosticPos pos, BLangVariable variable) {
        BLangVariableDef variableDef = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    private IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    private void addInitReturnStatement(BLangBlockStmt bLangBlockStmt) {
        //Add return statement to the init function
        BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = bLangBlockStmt.pos;
        bLangBlockStmt.addStatement(returnStmt);
    }
}
