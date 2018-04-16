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
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.EndpointSPIAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
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
    private final EndpointSPIAnalyzer endpointSPIAnalyzer;

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
        this.endpointSPIAnalyzer = EndpointSPIAnalyzer.getInstance(context);
    }

    void rewriteAllEndpointsInPkg(BLangPackage pkgNode, SymbolEnv env) {
        pkgNode.globalEndpoints.forEach(ep -> this.rewriteEndpoint(ep, env));

        pkgNode.functions.forEach(function -> {
            SymbolEnv fucEnv = SymbolEnv.createFunctionEnv(function, function.symbol.scope, env);
            function.endpoints.forEach(endpoint -> rewriteEndpoint(endpoint, fucEnv));
        });

        pkgNode.services.forEach(serviceNode -> {
            SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.symbol.scope, env);
            serviceNode.endpoints.forEach(endpoint -> rewriteEndpoint(endpoint, serviceEnv));

            serviceNode.resources.forEach(resourceNode -> {
                SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceNode.symbol.scope,
                        serviceEnv);
                resourceNode.endpoints.forEach(endpoint -> rewriteEndpoint(endpoint, resourceEnv));
            });
        });
    }

    void rewriteAnonymousEndpointsInPkg(BLangPackage pkgNode, SymbolEnv pkgEnv) {
        pkgNode.services.forEach(service -> rewriteAnonymousEndpointInService(service, pkgEnv));
    }

    void rewriteAnonymousEndpointInService(BLangService service, SymbolEnv pkgEnv) {
        if (service.anonymousEndpointBind != null) {
            defineGlobalAnonymousEndpoint(service, pkgEnv);
        }
    }

    void rewriteServiceBoundToEndpointInPkg(BLangPackage pkgNode, SymbolEnv pkgEnv) {
        pkgNode.services.forEach(service -> rewriteService(service, pkgEnv));
    }

    private void rewriteService(BLangService service, SymbolEnv pkgEnv) {
        final BServiceSymbol serviceSymbol = (BServiceSymbol) service.symbol;
        if (serviceSymbol.boundEndpoints.isEmpty()) {
            return;
        }
        final BSymbol enclosingSymbol = pkgEnv.enclPkg.symbol;
        final BSymbol varSymbol = pkgEnv.enclPkg.startFunction.symbol;
        final BLangBlockStmt startBlock = pkgEnv.enclPkg.startFunction.body;
        serviceSymbol.boundEndpoints.forEach(endpointVarSymbol -> {
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

    void defineGlobalAnonymousEndpoint(BLangService service, SymbolEnv env) {
        BLangEndpoint ep = (BLangEndpoint) TreeBuilder.createEndpointNode();
        final BLangRecordLiteral anonymousEndpointBind = service.anonymousEndpointBind;
        ep.pos = anonymousEndpointBind.pos;
        ep.configurationExpr = anonymousEndpointBind;
        ep.name = ASTBuilderUtil.createIdentifier(anonymousEndpointBind.pos, service.name.value + "$Endpoint");
        ep.type = service.endpointType;

        BEndpointVarSymbol varSymbol = new BEndpointVarSymbol(0, names.fromIdNode(ep.name),
                env.enclPkg.symbol.pkgID, service.endpointType, env.enclPkg.symbol);
        if (service.endpointType.tsymbol.kind == SymbolKind.OBJECT
                || service.endpointType.tsymbol.kind == SymbolKind.RECORD) {
            endpointSPIAnalyzer.populateEndpointSymbol((BStructSymbol) service.endpointType.tsymbol, varSymbol);
        }
        ep.symbol = varSymbol;
        env.enclPkg.symbol.scope.define(varSymbol.name, varSymbol);

        final BLangVariable epVariable = ASTBuilderUtil.createVariable(ep.pos, ep.name.value, ep.symbol.type);
        epVariable.symbol = varSymbol;
        env.enclPkg.globalEndpoints.add(ep);
        service.boundEndpoints.add(ASTBuilderUtil.createVariableRef(anonymousEndpointBind.pos, varSymbol));
        ((BServiceSymbol) service.symbol).boundEndpoints.add(varSymbol);
    }

    void rewriteEndpoint(BLangEndpoint endpoint, SymbolEnv env) {
        final BSymbol encSymbol, varSymbol;
        final BLangBlockStmt initBlock, startBlock;
        BLangBlockStmt stopBlock = null;
        if (env.enclInvokable != null) {
            // Function, Action, Resource. Code generate to its body directly.
            encSymbol = varSymbol = env.enclInvokable.symbol;
            initBlock = startBlock = ((BLangInvokableNode) env.node).body;
        } else if (env.enclService != null) {
            encSymbol = env.enclService.symbol;
            varSymbol = ((BLangService) env.node).initFunction.symbol;
            initBlock = startBlock = ((BLangService) env.node).initFunction.body;
        } else {
            // Pkg level endpoint.
            encSymbol = env.enclPkg.symbol;
            varSymbol = ((BLangPackage) env.node).initFunction.symbol;
            initBlock = ((BLangPackage) env.node).initFunction.body;
            startBlock = ((BLangPackage) env.node).startFunction.body;
            stopBlock = ((BLangPackage) env.node).stopFunction.body;
        }

        BLangBlockStmt genInit, genInitCall, genStartCall, genStopCall;
        genInit = generateEndpointInit(endpoint, env, encSymbol);
        genInitCall = generateEndpointInitFunctionCall(endpoint, env, encSymbol, varSymbol);
        genStartCall = generateEndpointStartOrStop(endpoint, endpoint.symbol.startFunction, env, encSymbol);
        genStopCall = generateEndpointStartOrStop(endpoint, endpoint.symbol.stopFunction, env, encSymbol);

        if (env.enclInvokable != null) {
            ASTBuilderUtil.prependStatements(genStartCall, startBlock);
            ASTBuilderUtil.prependStatements(genInitCall, initBlock);
            // TODO : Implement stop.
            ASTBuilderUtil.prependStatements(genInit, initBlock);
        } else if (env.enclService != null) {
            ASTBuilderUtil.appendStatements(genInit, initBlock);
            ASTBuilderUtil.appendStatements(genInitCall, initBlock);
            ASTBuilderUtil.appendStatements(genStartCall, startBlock);
            // TODO : Implement stop.
        } else {
            ASTBuilderUtil.appendStatements(genInit, initBlock);
            ASTBuilderUtil.appendStatements(genInitCall, initBlock);
            ASTBuilderUtil.appendStatements(genStartCall, startBlock);
            ASTBuilderUtil.appendStatements(genStopCall, Objects.requireNonNull(stopBlock));
        }
    }

    private BLangBlockStmt generateEndpointInit(BLangEndpoint endpoint,
                                                SymbolEnv env,
                                                BSymbol encSymbol) {
        final String epName = endpoint.name.value;
        final DiagnosticPos pos = endpoint.pos;
        BLangBlockStmt temp = new BLangBlockStmt();

        final BLangVariable epVariable = ASTBuilderUtil.createVariable(pos, epName, endpoint.symbol.type);
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(pos, encSymbol.scope, env,
                names.fromString(epName), SymTag.VARIABLE);

        final BLangExpression newExpr;
        if (endpoint.configurationExpr != null
                && endpoint.configurationExpr.getKind() != NodeKind.RECORD_LITERAL_EXPR) {
            // Handle Endpoint Assignment.
            newExpr = endpoint.configurationExpr;
        } else if (endpoint.configurationExpr != null
                && endpoint.configurationExpr.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
            // Handle Endpoint initialization.
            newExpr = ASTBuilderUtil.createEmptyRecordLiteral(pos, endpoint.symbol.type);
        } else {
            newExpr = null;
        }

        // EPType ep_name = {};
        if (env.enclInvokable != null) {
            // In callable unit, endpoint is same scope variable.
            final BLangVariableDef epNewStmt = ASTBuilderUtil.createVariableDefStmt(pos, temp);
            epNewStmt.var = epVariable;
            epNewStmt.var.expr = newExpr;
        } else {
            // This is an init function. ep variable is defined in outside.
            if (env.enclService != null) {
                // Add to endpoint variable to relevant location
                final BLangVariableDef epVarDef = ASTBuilderUtil.createVariableDef(pos);
                epVarDef.var = epVariable;
                env.enclService.vars.add(epVarDef);
            }
            final BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(pos, temp);
            assignmentStmt.varRef = ASTBuilderUtil.createVariableRef(pos, epVariable.symbol);
            assignmentStmt.expr = newExpr;
        }
        return temp;
    }

    private BLangBlockStmt generateEndpointInitFunctionCall(BLangEndpoint endpoint,
                                                            SymbolEnv env,
                                                            BSymbol encSymbol,
                                                            BSymbol varEncSymbol) {
        BLangBlockStmt temp = new BLangBlockStmt();
        if (endpoint.configurationExpr == null
                || endpoint.configurationExpr.getKind() != NodeKind.RECORD_LITERAL_EXPR) {
            return temp;
        }
        final String epName = endpoint.name.value;
        final DiagnosticPos pos = endpoint.pos;
        final BLangVariable epVariable = ASTBuilderUtil.createVariable(pos, epName, endpoint.symbol.type);
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(pos, encSymbol.scope, env,
                names.fromString(epName), SymTag.VARIABLE);

        // EPConfigType ep_nameConf = { ep-config-expr };
        final BLangVariableDef epConfigNewStmt = ASTBuilderUtil.createVariableDefStmt(pos, temp);
        epConfigNewStmt.var = ASTBuilderUtil.createVariable(pos, epName + "Conf",
                endpoint.configurationExpr.type);
        epConfigNewStmt.var.expr = endpoint.configurationExpr;
        ASTBuilderUtil.defineVariable(epConfigNewStmt.var, varEncSymbol, names);
        List<BLangVariable> args = Lists.of(epConfigNewStmt.var);
        if (endpoint.symbol.initFunction != null && endpoint.symbol.initFunction.params.size() == 2) {
            // Endpoint is already desugared. Fix this correctly.
            args.add(0, epVariable);
        }
        // epName.init(ep_nameConf);
        final BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, temp);
        final BLangInvocation iExpr = ASTBuilderUtil.createInvocationExpr(pos, endpoint.symbol.initFunction, args,
                symResolver);
        if (endpoint.symbol.initFunction != null && endpoint.symbol.initFunction.params.size() != 2) {
            iExpr.expr = ASTBuilderUtil.createVariableRef(epVariable.pos, epVariable.symbol);
        }
        expressionStmt.expr = iExpr;
        return temp;
    }

    private BLangBlockStmt generateEndpointStartOrStop(BLangEndpoint endpoint,
                                                       BInvokableSymbol funSymbol,
                                                       SymbolEnv env,
                                                       BSymbol encSymbol) {
        BLangBlockStmt temp = new BLangBlockStmt();
        if (funSymbol == null || endpoint.configurationExpr == null
                || endpoint.configurationExpr.getKind() != NodeKind.RECORD_LITERAL_EXPR) {
            // Start or Stop should be called only
            //  1: If start function is present.
            //  2: If endpoint in initialized
            return temp;
        }
        final DiagnosticPos pos = endpoint.pos;
        final String epName = endpoint.name.value;

        final BLangVariable epVariable = ASTBuilderUtil.createVariable(pos, epName, endpoint.symbol.type);
        final Name name = names.fromIdNode(endpoint.name);
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(pos, encSymbol.scope, env, name,
                SymTag.VARIABLE);
        List<BLangVariable> args = new ArrayList<>();
        if (funSymbol.params.size() == 1) {
            // Endpoint is already desugared. Fix this correctly.
            args.add(0, epVariable);
        }
        final BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, temp);
        final BLangInvocation iExpr = ASTBuilderUtil.createInvocationExpr(pos, funSymbol, args, symResolver);
        if (funSymbol.params.size() != 1) {
            iExpr.expr = ASTBuilderUtil.createVariableRef(epVariable.pos, epVariable.symbol);
        }
        expressionStmt.expr = iExpr;
        return temp;
    }

    private BLangBlockStmt generateServiceRegistered(BEndpointVarSymbol endpoint,
                                                     BLangService service,
                                                     SymbolEnv env,
                                                     BSymbol encSymbol,
                                                     BSymbol varEncSymbol) {
        final DiagnosticPos pos = service.pos;
        final String epName = endpoint.name.value;
        BLangBlockStmt temp = new BLangBlockStmt();

        final BLangVariable epVariable = ASTBuilderUtil.createVariable(pos, epName, endpoint.type);
        final Name name = endpoint.name;
        epVariable.symbol = (BVarSymbol) symResolver.lookupMemberSymbol(pos, encSymbol.scope, env, name,
                SymTag.VARIABLE);

        final BLangVariableDef serviceTypeDef = ASTBuilderUtil.createVariableDefStmt(pos, temp);
        serviceTypeDef.var = ASTBuilderUtil.createVariable(pos, service.name + "type", symTable.typeDesc);
        ASTBuilderUtil.defineVariable(serviceTypeDef.var, varEncSymbol, names);

        serviceTypeDef.var.expr = getTypeAccessExpression(pos, service.symbol.type);

        List<BLangVariable> args = Lists.of(serviceTypeDef.var);
        if (endpoint.registerFunction != null && endpoint.registerFunction.params.size() == 2) {
            // Endpoint is already desugared. Fix this correctly.
            args.add(0, epVariable);
        }
        final BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, temp);
        final BLangInvocation iExpr = ASTBuilderUtil.createInvocationExpr(pos, endpoint.registerFunction, args,
                symResolver);
        if (endpoint.registerFunction != null && endpoint.registerFunction.params.size() != 2) {
            iExpr.expr = ASTBuilderUtil.createVariableRef(epVariable.pos, epVariable.symbol);
        }
        expressionStmt.expr = iExpr;
        return temp;
    }

    private BLangTypedescExpr getTypeAccessExpression(DiagnosticPos pos,
                                                      BType serviceType) {
        BLangTypedescExpr typeAccessExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
        typeAccessExpr.pos = pos;
        typeAccessExpr.resolvedType = serviceType;
        typeAccessExpr.type = symTable.typeDesc;
        return typeAccessExpr;
    }
}
