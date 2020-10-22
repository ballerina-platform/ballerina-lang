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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.BlockNode;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.ballerina.runtime.util.BLangConstants.UNDERSCORE;

/**
 * Service De-sugar.
 *
 * @since 0.985.0
 */
public class ServiceDesugar {

    private static final CompilerContext.Key<ServiceDesugar> SERVICE_DESUGAR_KEY = new CompilerContext.Key<>();

    private static final String START_METHOD = "__start";
    private static final String GRACEFUL_STOP = "__gracefulStop";
    private static final String ATTACH_METHOD = "__attach";
    private static final String LISTENER = "$LISTENER";

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private HttpFiltersDesugar httpFiltersDesugar;

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
        //  _ = [check] var.__start/__stop();
        //

        final DiagnosticPos pos = variable.pos;

        // Find correct symbol.
        final Name functionName = names
                .fromString(Symbols.getAttachedFuncSymbolName(variable.type.tsymbol.name.value, method));
        BInvokableSymbol methodInvocationSymbol = (BInvokableSymbol) symResolver
                .lookupMemberSymbol(pos, variable.type.tsymbol.scope, env, functionName,
                        SymTag.INVOKABLE);

        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(pos, variable.symbol);

        // Create method invocation
        addMethodInvocation(pos, varRef, methodInvocationSymbol, Collections.emptyList(), Collections.emptyList(),
                            (BLangBlockFunctionBody) lifeCycleFunction.body);
    }

    BLangBlockStmt rewriteServiceVariables(List<BLangService> services, SymbolEnv env) {
        BLangBlockStmt attachmentsBlock = (BLangBlockStmt) TreeBuilder.createBlockNode();
        services.forEach(service -> rewriteServiceVariable(service, env, attachmentsBlock));
        return attachmentsBlock;
    }

    void rewriteServiceVariable(BLangService service, SymbolEnv env, BLangBlockStmt attachments) {
        // service x on y { ... }
        //
        // after desugar :
        //      if y is anonymous (globalVar)   ->      y = y(expr)
        //      (init)                          ->      y.__attach(x, {});

        if (service.isAnonymousService()) {
            return;
        }
        final DiagnosticPos pos = service.pos;

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
            args.add(ASTBuilderUtil.createVariableRef(pos, service.variableNode.symbol));

            BLangLiteral serviceName = ASTBuilderUtil.createLiteral(pos, symTable.stringType, service.name.value);
            List<BLangNamedArgsExpression> namedArgs = Collections.singletonList(
                    ASTBuilderUtil.createNamedArg("name", serviceName));

            addMethodInvocation(pos, listenerVarRef, methodRef, args, namedArgs, attachments);
        }
    }

    private void addMethodInvocation(DiagnosticPos pos, BLangSimpleVarRef varRef, BInvokableSymbol methodRefSymbol,
                                     List<BLangExpression> args, List<BLangNamedArgsExpression> namedArgs,
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
        httpFiltersDesugar.addHttpFilterStatementsToResource(functionNode, env);
        httpFiltersDesugar.addCustomAnnotationToResource(functionNode, env);
    }
}
