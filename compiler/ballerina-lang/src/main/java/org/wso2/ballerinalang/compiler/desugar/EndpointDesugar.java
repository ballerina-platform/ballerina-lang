/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.List;
import java.util.Objects;

/**
 * Desugar endpoints into executable entries.
 *
 * @since 0.965.0
 */
public class EndpointDesugar {

    private static final CompilerContext.Key<EndpointDesugar> ENDPOINT_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;

    public static EndpointDesugar getInstance(CompilerContext context) {
        EndpointDesugar endpointDesugar = context.get(ENDPOINT_DESUGAR_KEY);
        if (endpointDesugar == null) {
            endpointDesugar = new EndpointDesugar(context);
        }
        return endpointDesugar;
    }

    private EndpointDesugar(CompilerContext context) {
        context.put(ENDPOINT_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
    }

    void rewriteServiceBoundToEndpointInPkg(BLangPackage pkgNode, SymbolEnv pkgEnv) {
        pkgNode.services.forEach(service -> rewriteService(service, pkgEnv));
    }

    private void rewriteService(BLangService service, SymbolEnv pkgEnv) {
        if (service.attachedEndpoints.isEmpty()) {
            return;
        }
        final BSymbol enclosingSymbol = pkgEnv.enclPkg.symbol;
        final BSymbol varSymbol = pkgEnv.enclPkg.startFunction.symbol;
        final BLangBlockStmt startBlock = pkgEnv.enclPkg.startFunction.body;
        service.attachedEndpoints.forEach(endpointVarSymbol -> {
            final BLangBlockStmt generateCode = generateServiceRegistered(endpointVarSymbol, service, pkgEnv,
                    enclosingSymbol, varSymbol);
            ASTBuilderUtil.prependStatements(generateCode, startBlock);
        });
    }

    void defineGlobalEndpoint(BLangEndpoint ep, SymbolEnv env) {
        final BLangVariable epVariable = ASTBuilderUtil.createVariable(ep.pos, ep.name.value, ep.symbol.type);
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(ep.pos, env.enclPkg.symbol.scope, env,
                names.fromIdNode(ep.name), SymTag.VARIABLE);
        env.enclPkg.globalVars.add(epVariable);
    }

    void rewriteEndpoint(BLangEndpoint endpoint, SymbolEnv env) {
        final BSymbol enclosingSymbol, varSymbol;
        final BLangBlockStmt initBlock, startBlock;
        BLangBlockStmt stopBlock = null;
        if (env.enclInvokable != null) {
            // Function, Action, Resource. Code generate to its body directly.
            enclosingSymbol = varSymbol = env.enclInvokable.symbol;
            initBlock = startBlock = ((BLangInvokableNode) env.node).body;
        } else if (env.enclConnector != null) {
            enclosingSymbol = env.enclConnector.symbol;
            varSymbol = ((BLangConnector) env.node).initFunction.symbol;
            initBlock = startBlock = ((BLangConnector) env.node).initFunction.body;
        } else if (env.enclService != null) {
            enclosingSymbol = env.enclService.symbol;
            varSymbol = ((BLangService) env.node).initFunction.symbol;
            initBlock = startBlock = ((BLangService) env.node).initFunction.body;
        } else {
            // Pkg level endpoint.
            enclosingSymbol = env.enclPkg.symbol;
            varSymbol = ((BLangPackage) env.node).initFunction.symbol;
            initBlock = ((BLangPackage) env.node).initFunction.body;
            startBlock = ((BLangPackage) env.node).startFunction.body;
            stopBlock = ((BLangPackage) env.node).stopFunction.body;
        }

        final BLangBlockStmt generatedInitCode = generateEndpointInit(endpoint, env, enclosingSymbol, varSymbol);
        final BLangBlockStmt generatedStartCode = generateEndpointStartOrStop(endpoint, Names.EP_SPI_START, env,
                enclosingSymbol);
        final BLangBlockStmt generatedStopCode = generateEndpointStartOrStop(endpoint, Names.EP_SPI_STOP, env,
                enclosingSymbol);

        if (env.enclInvokable != null) {
            ASTBuilderUtil.prependStatements(generatedStartCode, startBlock);
            ASTBuilderUtil.prependStatements(generatedInitCode, initBlock);
            // TODO : Implement stop.
        } else if (env.enclConnector != null || env.enclService != null) {
            ASTBuilderUtil.appendStatements(generatedInitCode, initBlock);
            ASTBuilderUtil.appendStatements(generatedStartCode, startBlock);
            // TODO : Implement stop.
        } else {
            ASTBuilderUtil.appendStatements(generatedInitCode, initBlock);
            ASTBuilderUtil.appendStatements(generatedStartCode, startBlock);
            ASTBuilderUtil.appendStatements(generatedStopCode, Objects.requireNonNull(stopBlock));
        }
    }

    private BLangBlockStmt generateEndpointInit(BLangEndpoint endpoint,
                                                SymbolEnv env,
                                                BSymbol endpointParentSymbol,
                                                BSymbol varSymbolScope) {
        final String epName = endpoint.name.value;
        final DiagnosticPos pos = endpoint.pos;
        BLangBlockStmt temp = new BLangBlockStmt();

        // EPType ep_name = {};
        final BLangVariable epVariable = ASTBuilderUtil.createVariable(pos, epName, endpoint.symbol.type);
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(pos, endpointParentSymbol.scope, env,
                names.fromString(epName), SymTag.VARIABLE);
        final BLangRecordLiteral newExpr = ASTBuilderUtil.createEmptyRecordLiteral(pos, endpoint.symbol.type);
        if (env.enclInvokable != null) {
            // In callable unit, endpoint is same scope variable.
            final BLangVariableDef epNewStmt = ASTBuilderUtil.createVariableDefStmt(pos, temp);
            epNewStmt.var = epVariable;
            epNewStmt.var.expr = newExpr;
        } else {
            // This is an init function. ep variable is defined in outside.
            if (env.enclConnector != null || env.enclService != null) {
                // Add to endpoint variable to relevant location
                final BLangVariableDef epVarDef = ASTBuilderUtil.createVariableDef(pos);
                epVarDef.var = epVariable;
                if (env.enclConnector != null) {
                    env.enclConnector.varDefs.add(epVarDef);
                } else {
                    env.enclService.vars.add(epVarDef);
                }
            }
            final BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(pos, temp);
            assignmentStmt.varRefs.add(ASTBuilderUtil.createVariableRef(pos, epVariable.symbol));
            assignmentStmt.expr = newExpr;
        }

        // EPConfigType ep_nameConf = { ep-config-expr };
        final BLangVariableDef epConfigNewStmt = ASTBuilderUtil.createVariableDefStmt(pos, temp);
        epConfigNewStmt.var = ASTBuilderUtil.createVariable(pos, epName + "Conf",
                endpoint.configurationExpr.type);
        epConfigNewStmt.var.expr = endpoint.configurationExpr;
        ASTBuilderUtil.defineVariable(epConfigNewStmt.var, varSymbolScope, names);

        // String s = "epName";
        final BLangVariableDef epNameDefinStmt = ASTBuilderUtil.createVariableDefStmt(pos, temp);
        epNameDefinStmt.var = ASTBuilderUtil.createVariable(pos, epName + "epName", symTable.stringType);
        epNameDefinStmt.var.expr = ASTBuilderUtil.createLiteral(pos, symTable.stringType, epName);
        ASTBuilderUtil.defineVariable(epNameDefinStmt.var, varSymbolScope, names);

        List<BLangVariable> args = Lists.of(epVariable, epNameDefinStmt.var, epConfigNewStmt.var);
        final BStructSymbol.BAttachedFunction initFunction = ((BStructSymbol) endpoint.symbol.type.tsymbol)
                .attachedFuncs.stream().filter(f -> f.funcName.value.equals(Names.EP_SPI_INIT.value))
                .findAny().get();

        // epName.init( "epName", ep_nameConf );
        final BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, temp);
        expressionStmt.expr = ASTBuilderUtil.createInvocationExpr(pos, initFunction.symbol, args, symResolver);
        return temp;
    }

    private BLangBlockStmt generateEndpointStartOrStop(BLangEndpoint endpoint,
                                                       Name functionName,
                                                       SymbolEnv env,
                                                       BSymbol endpointParentSymbol) {
        final DiagnosticPos pos = endpoint.pos;
        final String epName = endpoint.name.value;
        BLangBlockStmt temp = new BLangBlockStmt();
        final BStructSymbol.BAttachedFunction startFunction = ((BStructSymbol) endpoint.symbol.type.tsymbol)
                .attachedFuncs.stream().filter(f -> f.funcName.value.equals(functionName.value))
                .findAny().get();

        final BLangVariable epVariable = ASTBuilderUtil.createVariable(pos, epName, endpoint.symbol.type);
        final Name name = names.fromIdNode(endpoint.name);
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(pos, endpointParentSymbol.scope, env, name,
                SymTag.VARIABLE);
        List<BLangVariable> args = Lists.of(epVariable);
        final BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, temp);
        expressionStmt.expr = ASTBuilderUtil.createInvocationExpr(pos, startFunction.symbol, args, symResolver);
        return temp;
    }

    private BLangBlockStmt generateServiceRegistered(BEndpointVarSymbol endpoint,
                                                     BLangService service,
                                                     SymbolEnv env,
                                                     BSymbol endpointParentSymbol,
                                                     BSymbol varSymbolScope) {
        final DiagnosticPos pos = service.pos;
        final String epName = endpoint.name.value;
        BLangBlockStmt temp = new BLangBlockStmt();

        final BStructSymbol.BAttachedFunction startFunction = ((BStructSymbol) endpoint.type.tsymbol)
                .attachedFuncs.stream()
                .filter(f -> f.funcName.value.equals(Names.EP_SPI_REGISTER.value))
                .findAny().get();

        final BLangVariable epVariable = ASTBuilderUtil.createVariable(pos, epName, endpoint.type);
        final Name name = endpoint.name;
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(pos, endpointParentSymbol.scope, env, name,
                SymTag.VARIABLE);

        final BLangVariableDef serviceTypeDef = ASTBuilderUtil.createVariableDefStmt(pos, temp);
        serviceTypeDef.var = ASTBuilderUtil.createVariable(pos, service.name + "type", symTable.typeType);
        ASTBuilderUtil.defineVariable(serviceTypeDef.var, varSymbolScope, names);

        final BLangUnaryExpr typeOfExpr = ASTBuilderUtil.createUnaryExpr(pos);
        serviceTypeDef.var.expr = typeOfExpr;
        typeOfExpr.operator = OperatorKind.TYPEOF;
        typeOfExpr.expr = getTypeAccessExpression(pos, service.symbol.type);
        typeOfExpr.type = symTable.typeType;

        List<BLangVariable> args = Lists.of(epVariable, serviceTypeDef.var);
        final BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, temp);
        expressionStmt.expr = ASTBuilderUtil.createInvocationExpr(pos, startFunction.symbol, args, symResolver);
        return temp;
    }

    private BLangTypeofExpr getTypeAccessExpression(DiagnosticPos pos,
                                                    BType serviceType) {
        BLangTypeofExpr typeAccessExpr = (BLangTypeofExpr) TreeBuilder.createTypeAccessNode();
        typeAccessExpr.pos = pos;
        typeAccessExpr.resolvedType = serviceType;
        typeAccessExpr.type = symTable.typeType;
        return typeAccessExpr;
    }
}
