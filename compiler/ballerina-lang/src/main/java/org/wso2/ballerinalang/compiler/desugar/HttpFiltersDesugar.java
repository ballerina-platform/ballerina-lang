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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * This class injects the code that is required for declarative authentication, to the first lines of an given resource.
 * The code injected will be one of the followings:
 * <blockquote><pre>
 *     authenticateResource(self, "resourceMethod", ["resourcePath", "resourcePath"])
 *     authenticateResource(self)
 * </pre></blockquote>
 * `authenticateResource` is expected to panic with a `distinct error` that the relevant listener knows and the listener
 * handles that error specifically.
 *
 * @since 0.974.1
 */
public class HttpFiltersDesugar {

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;

    private static final String ORG_NAME = "ballerina";
    private static final String HTTP_PACKAGE_NAME = "http";
    private static final String WEBSOCKET_PACKAGE_NAME = "websocket";
    private static final String AUTHENTICATE_RESOURCE = "authenticateResource";

    private static final CompilerContext.Key<HttpFiltersDesugar> HTTP_FILTERS_DESUGAR_KEY =
            new CompilerContext.Key<>();

    public static HttpFiltersDesugar getInstance(CompilerContext context) {
        HttpFiltersDesugar desugar = context.get(HTTP_FILTERS_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new HttpFiltersDesugar(context);
        }

        return desugar;
    }

    private HttpFiltersDesugar(CompilerContext context) {
        context.put(HTTP_FILTERS_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
    }

    void desugarFunction(BLangFunction functionNode, SymbolEnv env, List<BType> expressionTypes) {
        if (isValidPackage(expressionTypes, HTTP_PACKAGE_NAME)) {
            addHttpAuthDesugarFunctionInvocation(functionNode, env);
        } else if (isValidPackage(expressionTypes, WEBSOCKET_PACKAGE_NAME)) {
            addWebSocketAuthDesugarFunctionInvocation(functionNode, env);
        }
    }

    boolean isValidPackage(List<BType> expressionTypes, String packageName) {
        for (BType expressionType : expressionTypes) {
            if (expressionType.tag == TypeTags.UNION) {
                for (BType memberType : ((BUnionType) expressionType).getMemberTypes()) {
                    if (memberType.tag == TypeTags.OBJECT && isValidPackage((BObjectType) memberType, packageName)) {
                        return true;
                    }
                }
            } else if (expressionType.tag == TypeTags.OBJECT &&
                    isValidPackage((BObjectType) expressionType, packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidPackage(BObjectType type, String packageName) {
        return type.tsymbol.pkgID.orgName.value.equals(ORG_NAME) && type.tsymbol.pkgID.name.value.equals(packageName);
    }

    void addHttpAuthDesugarFunctionInvocation(BLangFunction functionNode, SymbolEnv env) {
        BPackageSymbol packageSymbol = getPackageSymbol(env, HTTP_PACKAGE_NAME);
        if (packageSymbol == null) {
            // Couldn't find http package in imports list or symbols list.
            return;
        }
        // Expected method type:
        // `function authenticateResource(service object {} serviceRef, string methodName, string[] resourcePath)`
        // The function is expected to panic with a distinct error when fail to authenticate.
        // HTTP listener will handle this error.
        BSymbol methodSym = symResolver.lookupMethodInModule(packageSymbol, names.fromString(AUTHENTICATE_RESOURCE),
                                                             env);
        if (methodSym == symTable.notFoundSymbol || !(methodSym instanceof BInvokableSymbol)) {
            return;
        }
        BInvokableSymbol invocationSymbol = (BInvokableSymbol) methodSym;
        BLangResourceFunction resourceNode = (BLangResourceFunction) functionNode;
        Location pos = resourceNode.getPosition();

        // Create method invocation.
        BLangSimpleVarRef selfRef = ASTBuilderUtil.createVariableRef(
                pos, resourceNode.symbol.receiverSymbol);

        BLangLiteral methodNameLiteral = ASTBuilderUtil.createLiteral(
                pos, symTable.stringType, resourceNode.methodName.value);

        ArrayList<BLangExpression> pathLiterals = new ArrayList<>();
        for (BLangIdentifier path : resourceNode.resourcePath) {
            pathLiterals.add(ASTBuilderUtil.createLiteral(pos, symTable.stringType, path.value));
        }
        BLangListConstructorExpr.BLangArrayLiteral resourcePathLiteral = ASTBuilderUtil.createEmptyArrayLiteral(
                pos, (BArrayType) symTable.stringArrayType);
        resourcePathLiteral.exprs = pathLiterals;

        ArrayList<BLangExpression> args = new ArrayList<>();
        args.add(selfRef);
        args.add(methodNameLiteral);
        args.add(resourcePathLiteral);

        BLangInvocation invocationExpr =
                ASTBuilderUtil.createInvocationExprForMethod(pos, invocationSymbol, args, symResolver);
        BLangSimpleVariableDef result = ASTBuilderUtil.createVariableDef(pos,
                 ASTBuilderUtil.createVariable(pos, "$temp$http$desugar$result",
                                               symTable.anyType, invocationExpr, null));
        List<BLangStatement> statements = getFunctionBodyStatementList(functionNode);
        statements.add(0, result);

        BVarSymbol resultSymbol = new BVarSymbol(0, names.fromIdNode(result.var.name), env.enclPkg.packageID,
                                                 result.var.type, resourceNode.symbol, pos, VIRTUAL);
        resourceNode.symbol.scope.define(resultSymbol.name, resultSymbol);
        result.var.symbol = resultSymbol;
    }

    void addWebSocketAuthDesugarFunctionInvocation(BLangFunction functionNode, SymbolEnv env) {
        BPackageSymbol packageSymbol = getPackageSymbol(env, "websocket");
        if (packageSymbol == null) {
            // Couldn't find websocket package in imports list or symbols list.
            return;
        }
        // Expected method type:
        // `function authenticateResource(service object {} serviceRef)`
        // The function is expected to panic with a distinct error when fail to authenticate.
        // WebSocket listener will handle this error.
        BSymbol methodSym = symResolver.lookupMethodInModule(packageSymbol, names.fromString(AUTHENTICATE_RESOURCE),
                                                             env);
        if (methodSym == symTable.notFoundSymbol || !(methodSym instanceof BInvokableSymbol)) {
            return;
        }
        BInvokableSymbol invocationSymbol = (BInvokableSymbol) methodSym;
        BLangResourceFunction resourceNode = (BLangResourceFunction) functionNode;

        Location pos = resourceNode.getPosition();

        // Create method invocation.
        BLangSimpleVarRef selfRef = ASTBuilderUtil.createVariableRef(
                pos, resourceNode.symbol.receiverSymbol);

        ArrayList<BLangExpression> args = new ArrayList<>();
        args.add(selfRef);

        BLangInvocation invocationExpr =
                ASTBuilderUtil.createInvocationExprForMethod(pos, invocationSymbol, args, symResolver);
        BLangSimpleVariableDef result = ASTBuilderUtil.createVariableDef(pos,
                ASTBuilderUtil.createVariable(pos, "$temp$websocket$desugar$result",
                                              symTable.anyType, invocationExpr, null));
        List<BLangStatement> statements = getFunctionBodyStatementList(functionNode);
        statements.add(0, result);

        BVarSymbol resultSymbol = new BVarSymbol(0, names.fromIdNode(result.var.name), env.enclPkg.packageID,
                                                 result.var.type, resourceNode.symbol, pos, VIRTUAL);
        resourceNode.symbol.scope.define(resultSymbol.name, resultSymbol);
        result.var.symbol = resultSymbol;
    }

    private BPackageSymbol getPackageSymbol(SymbolEnv env, String packageName) {
        // This resolves the package symbol when the code have an import relevant to the particular service
        for (BLangImportPackage pkg : env.enclPkg.imports) {
            if (pkg.symbol.pkgID.orgName.value.equals(ORG_NAME) && pkg.symbol.pkgID.name.value.equals(packageName)) {
                return pkg.symbol;
            }
        }
        // This resolves the package symbol when the code is at a submodule of the module which have the service
        // definition. In that case there is no any import relevant to the particular service.
        while (env.enclEnv != null) {
            if (env.enclEnv.scope.owner.pkgID.orgName.value.equals(ORG_NAME) &&
                    env.enclEnv.scope.owner.pkgID.name.value.equals(packageName)) {
                return env.enclEnv.enclPkg.symbol;
            }
            env = env.enclEnv;
        }
        return null;
    }

    private List<BLangStatement> getFunctionBodyStatementList(BLangFunction functionNode) {
        List<BLangStatement> statements;
        if (functionNode.body.getKind() == NodeKind.EXPR_FUNCTION_BODY) {
            BLangExprFunctionBody exprFunctionBody = (BLangExprFunctionBody) functionNode.body;
            BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(functionNode.getPosition());
            statements = blockStmt.stmts;
            exprFunctionBody.expr = ASTBuilderUtil.createStatementExpression(blockStmt, exprFunctionBody.expr);
        } else {
            statements = ((BLangBlockFunctionBody) functionNode.body).stmts;
        }
        return statements;
    }
}
