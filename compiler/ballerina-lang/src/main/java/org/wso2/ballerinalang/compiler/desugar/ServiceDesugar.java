/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;

/**
 * Service De-sugar.
 *
 * @since 0.985.0
 */
public class ServiceDesugar {

    private static final CompilerContext.Key<ServiceDesugar> SERVICE_DESUGAR_KEY = new CompilerContext.Key<>();

    private static final String START_METHOD = "start";
    private static final String GRACEFUL_STOP = "gracefulStop";
    private static final String ATTACH_METHOD = "attach";
    private static final String LISTENER = "$LISTENER";

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private HttpFiltersDesugar httpFiltersDesugar;
    private TransactionDesugar transactionDesugar;

    private boolean transactionCoordinatorStarted;

    public static ServiceDesugar getInstance(CompilerContext context) {
        ServiceDesugar desugar = context.get(SERVICE_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new ServiceDesugar(context);
        }

        return desugar;
    }

    private ServiceDesugar(CompilerContext context) {
        context.put(SERVICE_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.httpFiltersDesugar = HttpFiltersDesugar.getInstance(context);
        this.transactionDesugar = TransactionDesugar.getInstance(context);
        this.transactionCoordinatorStarted = false;
    }

    void rewriteListeners(List<BLangSimpleVariable> variables, SymbolEnv env, BLangFunction startFunction,
                          BLangFunction stopFunction) {
        variables.stream().filter(varNode -> Symbols.isFlagOn(varNode.symbol.flags, Flags.LISTENER))
                .forEach(varNode -> rewriteListener(varNode, env, startFunction, stopFunction));
    }

    private void rewriteListener(BLangSimpleVariable variable, SymbolEnv env, BLangFunction startFunction,
                                 BLangFunction stopFunction) {
        rewriteListenerLifeCycleFunction(startFunction, variable, env, START_METHOD);
        rewriteListenerLifeCycleFunction(stopFunction, variable, env, GRACEFUL_STOP);
    }

    private void rewriteListenerLifeCycleFunction(BLangFunction lifeCycleFunction, BLangSimpleVariable variable,
            SymbolEnv env, String method) {
        // This method will generate and add following statement to give life cycle function.
        //
        //  _ = [check] var.start/stop();
        //

        final Location pos = variable.pos;

        // Find correct symbol.
        final Name functionName = names
                .fromString(Symbols.getAttachedFuncSymbolName(variable.type.tsymbol.name.value, method));
        BInvokableSymbol methodInvocationSymbol = (BInvokableSymbol) symResolver
                .lookupMemberSymbol(pos, variable.type.tsymbol.scope, env, functionName,
                        SymTag.INVOKABLE);

        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(pos, variable.symbol);

        // Create method invocation
        addMethodInvocation(pos, varRef, methodInvocationSymbol, Collections.emptyList(),
                (BLangBlockFunctionBody) lifeCycleFunction.body);
    }

    BLangBlockStmt rewriteServiceVariables(List<BLangService> services, SymbolEnv env) {
        BLangBlockStmt attachmentsBlock = (BLangBlockStmt) TreeBuilder.createBlockNode();
        services.forEach(service -> rewriteServiceVariable(service, env, attachmentsBlock));
        return attachmentsBlock;
    }

    void rewriteServiceVariable(BLangService service, SymbolEnv env, BLangBlockStmt attachments) {
        // service x [/abs/Path] on y { ... }
        //
        // after desugar :
        //      (init)                          ->      y.__attach(x, {});

        final Location pos = service.pos;

        ASTBuilderUtil.defineVariable(service.serviceVariable, env.enclPkg.symbol, names);
        env.enclPkg.globalVars.add(service.serviceVariable);

        int count = 0;
        for (BLangExpression attachExpr : service.attachedExprs) {
            //      if y is anonymous   ->      y = y(expr)
            BLangSimpleVarRef listenerVarRef;
            if (attachExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                listenerVarRef = (BLangSimpleVarRef) attachExpr;
            } else {
                // Define anonymous listener variable.
                BLangSimpleVariable listenerVar = ASTBuilderUtil
                        .createVariable(pos, LISTENER + service.name.value + UNDERSCORE + count++, attachExpr.type,
                                attachExpr,
                                null);
                ASTBuilderUtil.defineVariable(listenerVar, env.enclPkg.symbol, names);
                listenerVar.symbol.flags |= Flags.LISTENER;
                env.enclPkg.globalVars.add(listenerVar);
                listenerVarRef = ASTBuilderUtil.createVariableRef(pos, listenerVar.symbol);
            }

            //      (.<init>)              ->      y.__attach(x, {});
            // Find correct symbol.
            final Name functionName = names
                    .fromString(Symbols.getAttachedFuncSymbolName(attachExpr.type.tsymbol.name.value, ATTACH_METHOD));
            BInvokableSymbol methodRef = (BInvokableSymbol) symResolver
                    .lookupMemberSymbol(pos, listenerVarRef.type.tsymbol.scope, env,
                            functionName, SymTag.INVOKABLE);

            // Create method invocation
            List<BLangExpression> args = new ArrayList<>();
            args.add(ASTBuilderUtil.createVariableRef(pos, service.serviceVariable.symbol));

            if (service.getServiceNameLiteral() == null) {
                BLangListConstructorExpr.BLangArrayLiteral arrayLiteral =
                        ASTBuilderUtil.createEmptyArrayLiteral(service.getPosition(), symTable.arrayStringType);
                for (IdentifierNode path : service.getAbsolutePath()) {
                    var literal = ASTBuilderUtil.createLiteral(path.getPosition(), symTable.stringType,
                            path.getValue());
                    arrayLiteral.exprs.add(literal);
                }
                args.add(arrayLiteral);
            } else {
                args.add((BLangExpression) service.getServiceNameLiteral());
            }

            addMethodInvocation(pos, listenerVarRef, methodRef, args, attachments);
        }
    }

    private void addMethodInvocation(Location pos, BLangSimpleVarRef varRef, BInvokableSymbol methodRefSymbol,
                                     List<BLangExpression> args,
                                     BlockNode body) {
        // Create method invocation
        final BLangInvocation methodInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(pos, methodRefSymbol, args, symResolver);
        methodInvocation.expr = varRef;

        BLangCheckedExpr checkedExpr = ASTBuilderUtil.createCheckExpr(pos, methodInvocation, symTable.nilType);
        checkedExpr.equivalentErrorTypeList.add(symTable.errorType);

        BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, body);
        expressionStmt.expr = checkedExpr;
        expressionStmt.expr.pos = pos;
    }

    void engageCustomServiceDesugar(BLangService service, SymbolEnv env) {
        service.serviceClass.functions.stream().filter(fun -> Symbols.isFlagOn(fun.symbol.flags, Flags.RESOURCE))
                .forEach(func -> engageCustomResourceDesugar(func, env));
    }

    private void engageCustomResourceDesugar(BLangFunction functionNode, SymbolEnv env) {
        if (Symbols.isFlagOn(functionNode.symbol.flags, Flags.TRANSACTIONAL)) {
            BLangExpressionStmt stmt = new BLangExpressionStmt(transactionDesugar
                    .createBeginParticipantInvocation(functionNode.pos));
            if (!transactionCoordinatorStarted) {
                setTransactionCoordinatorStarted(true);
                BLangExpressionStmt trxCoordnStmt = new BLangExpressionStmt(transactionDesugar.
                        createStartTransactionCoordinatorInvocation(functionNode.pos));
                ((BLangBlockFunctionBody) functionNode.body).stmts.add(0, trxCoordnStmt);
                ((BLangBlockFunctionBody) functionNode.body).stmts.add(1, stmt);
            } else {
                ((BLangBlockFunctionBody) functionNode.body).stmts.add(0, stmt);
            }
        }
//        httpFiltersDesugar.addHttpFilterStatementsToResource(functionNode, env);
//        httpFiltersDesugar.addCustomAnnotationToResource(functionNode, env);
    }

    boolean getTransactionCoordinatorStarted() {
        return this.transactionCoordinatorStarted;
    }

    void setTransactionCoordinatorStarted(boolean started) {
        this.transactionCoordinatorStarted = started;
    }
}
