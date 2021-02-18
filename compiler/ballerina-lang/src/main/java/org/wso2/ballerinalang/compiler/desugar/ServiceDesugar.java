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
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

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
    private final Types types;

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
        this.types = Types.getInstance(context);
    }

    void rewriteListeners(List<BLangVariable> variables, SymbolEnv env, BLangFunction startFunction,
                          BLangFunction stopFunction) {
        for (BLangVariable varNode : variables) {
            if (Symbols.isFlagOn(varNode.symbol.flags, Flags.LISTENER)) {
                rewriteListener(varNode, env, startFunction, stopFunction);
            }
        }
    }

    private void rewriteListener(BLangVariable variable, SymbolEnv env, BLangFunction startFunction,
                                 BLangFunction stopFunction) {
        rewriteListenerLifeCycleFunction(startFunction, variable, env, START_METHOD);
        rewriteListenerLifeCycleFunction(stopFunction, variable, env, GRACEFUL_STOP);
    }

    private void rewriteListenerLifeCycleFunction(BLangFunction lifeCycleFunction, BLangVariable variable,
            SymbolEnv env, String method) {
        // This method will generate and add following statement to give life cycle function.
        //
        //  _ = [check] var.start/stop();
        //

        final Location pos = variable.pos;

        // Find correct symbol.
        BTypeSymbol listenerTypeSymbol = getListenerType(variable.type).tsymbol;
        final Name functionName = names
                .fromString(Symbols.getAttachedFuncSymbolName(listenerTypeSymbol.name.value, method));
        BInvokableSymbol methodInvocationSymbol = (BInvokableSymbol) symResolver
                .lookupMemberSymbol(pos, listenerTypeSymbol.scope, env, functionName,
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
                        .createVariable(pos, generateServiceListenerVarName(service) + count, attachExpr.type,
                                attachExpr,
                                null);
                ASTBuilderUtil.defineVariable(listenerVar, env.enclPkg.symbol, names);
                listenerVar.symbol.flags |= Flags.LISTENER;
                env.enclPkg.globalVars.add(listenerVar);
                listenerVarRef = ASTBuilderUtil.createVariableRef(pos, listenerVar.symbol);
            }

            if (types.containsErrorType(listenerVarRef.type)) {
                BLangCheckedExpr listenerCheckExpr = ASTBuilderUtil.createCheckExpr(pos, listenerVarRef,
                        getListenerType(listenerVarRef.type));
                listenerCheckExpr.equivalentErrorTypeList.add(symTable.errorType);
                BLangSimpleVariable listenerWithoutErrors = ASTBuilderUtil.createVariable(pos,
                        generateServiceListenerVarName(service)  + "$CheckTemp" + count++,
                        getListenerTypeWithoutError(listenerVarRef.type),
                        listenerCheckExpr,
                        null);
                ASTBuilderUtil.defineVariable(listenerWithoutErrors, env.enclPkg.symbol, names);
                env.enclPkg.globalVars.add(listenerWithoutErrors);
                BLangSimpleVarRef checkedRef = ASTBuilderUtil.createVariableRef(pos, listenerWithoutErrors.symbol);
                listenerVarRef = checkedRef;
            }

            //      (.<init>)              ->      y.__attach(x, {});
            // Find correct symbol.
            BTypeSymbol listenerTypeSymbol = getListenerType(listenerVarRef.type).tsymbol;
            final Name functionName = names
                    .fromString(Symbols.getAttachedFuncSymbolName(listenerTypeSymbol.name.value, ATTACH_METHOD));
            BInvokableSymbol methodRef = (BInvokableSymbol) symResolver
                    .lookupMemberSymbol(pos, listenerTypeSymbol.scope, env, functionName, SymTag.INVOKABLE);

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

    private String generateServiceListenerVarName(BLangService service) {
        return LISTENER + service.name.value + UNDERSCORE;
    }

    private BType getListenerTypeWithoutError(BType type) {
        if (type.tag == TypeTags.UNION) {
            LinkedHashSet<BType> members = new LinkedHashSet<>();
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (types.isAssignable(memberType, symTable.errorType)) {
                    continue;
                }
                members.add(memberType);
            }
            return BUnionType.create(null, members);
        }
        return type;
    }

    private BType getListenerType(BType type) {
        if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (types.checkListenerCompatibility(memberType)) {
                    return memberType;
                }
            }
        }
        return type;
    }

    private void addMethodInvocation(Location pos, BLangSimpleVarRef varRef, BInvokableSymbol methodRefSymbol,
                                     List<BLangExpression> args,
                                     BlockNode body) {
        // Create method invocation
        final BLangInvocation methodInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(pos, methodRefSymbol, args, symResolver);

        // If listener contain errors we need to cast this to a listener type so that, attached function invocation
        // call is generated in BIRGen. Casting to the first listener type should be fine as actual method invocation
        // is based on the value rather than the type.
        BType listenerType = getListenerType(varRef.type);
        if (!types.isSameType(listenerType, varRef.type)) {
            BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
            castExpr.expr = varRef;
            castExpr.type = listenerType;
            castExpr.targetType = castExpr.type;
            methodInvocation.expr = castExpr;
        } else {
            methodInvocation.expr = varRef;
        }

        BLangCheckedExpr checkedExpr = ASTBuilderUtil.createCheckExpr(pos, methodInvocation, symTable.nilType);
        checkedExpr.equivalentErrorTypeList.add(symTable.errorType);

        BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, body);
        expressionStmt.expr = checkedExpr;
        expressionStmt.expr.pos = pos;
    }

    void engageCustomServiceDesugar(BLangService service, SymbolEnv env) {
        List<BType> expressionTypes = service.attachedExprs.stream().map(expression -> expression.type)
                .collect(Collectors.toList());
        service.serviceClass.functions.stream().filter(fun -> Symbols.isFlagOn(fun.symbol.flags, Flags.RESOURCE))
                .forEach(func -> engageCustomResourceDesugar(func, env, expressionTypes));
    }

    private void engageCustomResourceDesugar(BLangFunction functionNode, SymbolEnv env, List<BType> expressionTypes) {
        if (Symbols.isFlagOn(functionNode.symbol.flags, Flags.TRANSACTIONAL)) {
            BLangExpressionStmt stmt = new BLangExpressionStmt(transactionDesugar
                    .createBeginParticipantInvocation(functionNode.pos));
            ((BLangBlockFunctionBody) functionNode.body).stmts.add(0, stmt);
        }
        if (httpFiltersDesugar.isHttpPackage(expressionTypes)) {
            List<BLangStatement> statements = getFunctionBodyStatementList(functionNode);
            httpFiltersDesugar.addFilterStatements((BLangResourceFunction) functionNode, env, statements);
        }
    }

    private List<BLangStatement> getFunctionBodyStatementList(BLangFunction functionNode) {
        List<BLangStatement> statements = null;
        if (functionNode.body.getKind() == NodeKind.EXPR_FUNCTION_BODY) {
            BLangExprFunctionBody exprFunctionBody = (BLangExprFunctionBody) functionNode.body;
            BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(functionNode.getPosition());
            statements = blockStmt.stmts;

            BLangStatementExpression statementExpression = ASTBuilderUtil.createStatementExpression(
                            blockStmt,
                            exprFunctionBody.expr);
            exprFunctionBody.expr = statementExpression;
        } else {
            statements = ((BLangBlockFunctionBody) functionNode.body).stmts;
        }
        return statements;
    }
}
