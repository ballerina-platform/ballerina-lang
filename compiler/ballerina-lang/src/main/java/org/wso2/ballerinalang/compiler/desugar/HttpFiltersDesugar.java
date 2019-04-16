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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.programfile.InstructionCodes;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.Names.GEN_VAR_PREFIX;

/**
 * This class injects the code that invokes the http filters to the first lines of an http resource.
 * The code injected is as follows:
 * <blockquote><pre>
 *          http:FilterContext _$$_filterContext = new (serviceTypeDef, "serviceName", "resourceName");
 *          caller.conn.filterContext = _$$_filterContext;
 *          foreach _$$_filter in caller.config.filters {
 *              if(!_$$_filter.filterRequest(caller, req, _$$_filterContext)){
 *                  Done;
 *              }
 *          }
 * </pre></blockquote>
 * The second line in this code stores the _$$_filterContext reference to the http connector to be used when calling
 * the filterResponse method of the filters
 *
 * @since 0.974.1
 */
public class HttpFiltersDesugar {

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;

    private static final String HTTP_ENDPOINT_CONFIG = "config";
    private static final String HTTP_FILTERS_VAR = "filters";
    private static final String HTTP_FILTER_VAR = "filter";
    private static final String HTTP_FILTERCONTEXT_VAR = "filterContext";
    private static final String FILTER_REQUEST_FUNCTION = "filterRequest";

    private static final String ORG_NAME = "ballerina";
    private static final String PACKAGE_NAME = "http";
    private static final String CALLER_TYPE_NAME = "Caller";
    private static final String ORG_SEPARATOR = "/";

    private static final int ENDPOINT_PARAM_NUM = 0;
    private static final int REQUEST_PARAM_NUM = 1;
    private static final int FILTER_CONTEXT_FIELD_INDEX = 1;
    private static final int ENDPOINT_CONFIG_INDEX = 0;
    private static final int FILTERS_CONFIG_INDEX = 5;
    
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

    /**
     * Check if the resource is an http resource and apply filter.
     *
     * @param resourceNode The resource to apply filter on if it is http
     * @param env          the symbol environment
     */
    void addHttpFilterStatementsToResource(BLangFunction resourceNode, SymbolEnv env) {
        BLangSimpleVariable endpoint;
        if (resourceNode.requiredParams.size() >= 2) {
            endpoint = resourceNode.requiredParams.get(0);
            if (ORG_NAME.equals(endpoint.type.tsymbol.pkgID.orgName.value) && PACKAGE_NAME.equals(
                    endpoint.type.tsymbol.pkgID.name.value) && CALLER_TYPE_NAME.equals(
                    endpoint.type.tsymbol.name.value)) {
                addFilterStatements(resourceNode, env);
            }
        }
    }

    private void addFilterStatements(BLangFunction resourceNode, SymbolEnv env) {
        BLangSimpleVariable filterContextVar = addFilterContextCreation(resourceNode, env);
        addAssignmentAndForEach(resourceNode, filterContextVar);
    }

    /**
     * Adds code {@code http:FilterContext _$$_filterContext = new (serviceTypeDef, "serviceName", "resourceName");}.
     *
     * @param resourceNode The resource to add the FilterContext creation.
     * @param env          the symbol environment.
     */
    private BLangSimpleVariable addFilterContextCreation(BLangFunction resourceNode, SymbolEnv env) {
        BLangIdentifier pkgAlias = ASTBuilderUtil.createIdentifier(resourceNode.pos, getPackageAlias(env));
        BLangUserDefinedType filterContextUserDefinedType = new BLangUserDefinedType(
                pkgAlias, ASTBuilderUtil.createIdentifier(resourceNode.pos, "FilterContext"));
        BType filterContextType = symResolver.resolveTypeNode(filterContextUserDefinedType, env);
        String filterContextVarName = GEN_VAR_PREFIX.value + HTTP_FILTERCONTEXT_VAR;

        BLangSimpleVariable serviceSelf = (BLangSimpleVariable) resourceNode.getReceiver();
        BLangSimpleVarRef.BLangLocalVarRef serviceRef = new BLangSimpleVarRef.BLangLocalVarRef(serviceSelf.symbol);
        serviceRef.pos = resourceNode.pos;

        BLangLiteral serviceName = new BLangLiteral();
        serviceName.value = serviceSelf.type.tsymbol.name.value;
        serviceName.type = symTable.stringType;
        serviceName.pos = resourceNode.pos;

        BLangLiteral resourceName = new BLangLiteral();
        resourceName.value = resourceNode.name.value;
        resourceName.type = symTable.stringType;
        resourceName.pos = resourceNode.pos;

        BLangInvocation filterInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        filterInvocation.name = ASTBuilderUtil.createIdentifier(resourceNode.pos, "new");
        filterInvocation.symbol = ((BObjectTypeSymbol) filterContextType.tsymbol).initializerFunc.symbol;
        filterInvocation.pos = resourceNode.pos;
        filterInvocation.requiredArgs.add(serviceRef);
        filterInvocation.requiredArgs.add(serviceName);
        filterInvocation.requiredArgs.add(resourceName);
        filterInvocation.argExprs.add(serviceRef);
        filterInvocation.argExprs.add(serviceName);
        filterInvocation.argExprs.add(resourceName);
        filterInvocation.type = symTable.nilType;

        BLangTypeInit filterInitNode = (BLangTypeInit) TreeBuilder.createInitNode();
        filterInitNode.pos = resourceNode.pos;
        filterInitNode.type = filterContextType;
        filterInitNode.initInvocation = filterInvocation;
        filterInitNode.argsExpr.add(serviceRef);
        filterInitNode.argsExpr.add(serviceName);
        filterInitNode.argsExpr.add(resourceName);


        BLangSimpleVariable filterContextVar = ASTBuilderUtil.createVariable(
                resourceNode.pos, filterContextVarName, filterContextType, filterInitNode,
                new BVarSymbol(0, names.fromString(filterContextVarName), resourceNode.symbol.pkgID, filterContextType,
                               resourceNode.symbol));
        filterContextVar.typeNode = filterContextUserDefinedType;
        resourceNode.body.stmts.add(0, ASTBuilderUtil.createVariableDef(resourceNode.pos, filterContextVar));
        return filterContextVar;
    }

    /**
     * Get the alias name of the http import.
     *
     * @param env the symbol environment.
     * @return the alias name.
     */
    private String getPackageAlias(SymbolEnv env) {
        return env.enclPkg.imports.stream()
                .filter(imports -> imports.symbol.pkgID.toString().equals(ORG_NAME + ORG_SEPARATOR + PACKAGE_NAME))
                .map(importPackage -> importPackage.alias.value).findFirst().orElse(PACKAGE_NAME);
    }

    /**
     * Adds the following code.
     * <blockquote><pre>
     *  caller.conn.filterContext = _$$_filterContext;
     *  foreach _$$_filter in caller.config.filters {
     *      if(!_$$_filter.filterRequest(caller, req, _$$_filterContext)){
     *          Done;
     *      }
     *  }
     * </pre></blockquote>
     */
    private void addAssignmentAndForEach(BLangFunction resourceNode, BLangSimpleVariable filterContextVar) {
        //Assignment statement START
        BLangSimpleVariable endpointVar = resourceNode.requiredParams.get(ENDPOINT_PARAM_NUM);
        BLangSimpleVarRef.BLangLocalVarRef callerRef = new BLangSimpleVarRef.BLangLocalVarRef(endpointVar.symbol);
        callerRef.type = endpointVar.type;
        callerRef.pos = resourceNode.pos;

        BLangLiteral filterContextName = new BLangLiteral();
        filterContextName.value = HTTP_FILTERCONTEXT_VAR;
        filterContextName.type = symTable.stringType;
        filterContextName.pos = resourceNode.pos;

        BField filterContextVal = ((BObjectType) endpointVar.type).fields.get(FILTER_CONTEXT_FIELD_INDEX);
        BLangIndexBasedAccess.BLangStructFieldAccessExpr filterContextField =
                new BLangIndexBasedAccess.BLangStructFieldAccessExpr(
                        resourceNode.pos, callerRef, filterContextName, filterContextVal.symbol, false);
        filterContextField.type = filterContextVal.type;

        BLangSimpleVarRef.BLangLocalVarRef filterContextRef = new BLangSimpleVarRef.BLangLocalVarRef(
                filterContextVar.symbol);
        filterContextRef.variableName = filterContextVar.name;
        filterContextRef.type = filterContextVar.type;
        filterContextRef.pos = resourceNode.pos;

        BLangAssignment filterContextAssignment = ASTBuilderUtil.createAssignmentStmt(
                resourceNode.pos, filterContextField, filterContextRef, false);

        resourceNode.body.stmts.add(1, filterContextAssignment);
        //Assignment statement END

        //forEach statement START
        BField configVal = ((BObjectType) endpointVar.type).fields.get(ENDPOINT_CONFIG_INDEX);
        BField filtersVal = ((BRecordType) configVal.type).fields.get(FILTERS_CONFIG_INDEX);
        BType filtersType = filtersVal.type;
        BType filterType = ((BArrayType) filtersType).eType;

        BLangLiteral configName = new BLangLiteral();
        configName.value = HTTP_ENDPOINT_CONFIG;
        configName.type = symTable.stringType;
        configName.pos = resourceNode.pos;

        BLangIndexBasedAccess.BLangStructFieldAccessExpr configField =
                new BLangIndexBasedAccess.BLangStructFieldAccessExpr(
                        resourceNode.pos, callerRef, configName, configVal.symbol, false);
        configField.type = configVal.type;

        BLangLiteral filtersName = new BLangLiteral();
        filtersName.value = HTTP_FILTERS_VAR;
        filtersName.type = symTable.stringType;
        filtersName.pos = resourceNode.pos;

        BLangIndexBasedAccess.BLangStructFieldAccessExpr filtersField =
                new BLangIndexBasedAccess.BLangStructFieldAccessExpr(
                        resourceNode.pos, configField, filtersName, filtersVal.symbol, true);
        filtersField.type = filtersType;

        String filterVarName = GEN_VAR_PREFIX + HTTP_FILTER_VAR;

        BLangSimpleVarRef.BLangLocalVarRef filterRef = new BLangSimpleVarRef.BLangLocalVarRef(
                new BVarSymbol(0, new Name(filterVarName), resourceNode.symbol.pkgID, filterType, resourceNode.symbol));
        filterRef.variableName = ASTBuilderUtil.createIdentifier(resourceNode.pos, filterVarName);
        filterRef.type = filterType;
        filterRef.pos = resourceNode.pos;

        // Create a new variable definition. This is needed for the foreach node.
        BLangSimpleVariable variable = ASTBuilderUtil.createVariable(resourceNode.pos, filterVarName, filterType,
                null, (BVarSymbol) filterRef.symbol);
        BLangSimpleVariableDef variableDefinition = ASTBuilderUtil.createVariableDef(resourceNode.pos, variable);

        BLangLiteral returnName = new BLangLiteral();
        returnName.value = Names.NIL_VALUE;
        returnName.type = symTable.nilType;
        returnName.pos = resourceNode.pos;

        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.expr = returnName;
        returnNode.statementLink = new BLangStatement.BLangStatementLink();
        returnNode.statementLink.statement = returnNode;
        returnNode.pos = resourceNode.pos;

        BLangBlockStmt doneStatement = ASTBuilderUtil.createBlockStmt(resourceNode.pos,
                                                                      createSingletonArrayList(returnNode));

        BLangSimpleVariable requestVar = resourceNode.requiredParams.get(REQUEST_PARAM_NUM);
        BLangSimpleVarRef.BLangLocalVarRef requestRef = new BLangSimpleVarRef.BLangLocalVarRef(requestVar.symbol);
        requestRef.variableName = requestVar.name;
        requestRef.type = requestVar.type;
        requestRef.pos = requestVar.pos;

        List<BLangExpression> requiredArgs = new ArrayList<>();
        requiredArgs.add(filterRef);
        requiredArgs.add(callerRef);
        requiredArgs.add(requestRef);
        requiredArgs.add(filterContextRef);

        BLangInvocation.BLangAttachedFunctionInvocation filterRequestInvocation =
                new BLangInvocation.BLangAttachedFunctionInvocation(
                        resourceNode.pos, requiredArgs, new ArrayList<>(), new ArrayList<>(),
                        getFilterRequestFuncSymbol(filterType), symTable.booleanType, filterRef, false);
        filterRequestInvocation.desugared = true;

        BInvokableType type = new BInvokableType(createSingletonArrayList(symTable.booleanType), symTable.booleanType,
                null);
        BOperatorSymbol operatorSymbol = new BOperatorSymbol(names.fromString(OperatorKind.NOT.value()),
                symTable.rootPkgSymbol.pkgID, type, symTable.rootPkgSymbol, InstructionCodes.BNOT);
        BLangUnaryExpr unaryExpr = ASTBuilderUtil.createUnaryExpr(resourceNode.pos, filterRequestInvocation,
                symTable.booleanType, OperatorKind.NOT, operatorSymbol);

        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = resourceNode.pos;
        ifNode.body = doneStatement;
        ifNode.expr = unaryExpr;
        ifNode.statementLink = new BLangStatement.BLangStatementLink();
        ifNode.statementLink.statement = ifNode;

        BLangBlockStmt ifStatement = ASTBuilderUtil.createBlockStmt(resourceNode.pos,
                                                                    createSingletonArrayList(ifNode));

        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = resourceNode.pos;
        foreach.body = ifStatement;
        foreach.collection = filtersField;
        foreach.isDeclaredWithVar = false;
        foreach.varType = filterType;
        BMapType mapType = new BMapType(TypeTags.RECORD, filterType, symTable.mapType.tsymbol);
        foreach.resultType = mapType;
        foreach.nillableResultType = BUnionType.create(null, mapType, symTable.nilType);

        foreach.variableDefinitionNode = variableDefinition;

        resourceNode.body.stmts.add(2, foreach);
        //forEach statement END
    }

    private <E> List<E> createSingletonArrayList(E val) {
        List<E> list = new ArrayList<>();
        list.add(val);
        return list;
    }

    private BSymbol getFilterRequestFuncSymbol(BType filterType) {
        return ((BObjectTypeSymbol) filterType.tsymbol).attachedFuncs.stream().filter(func -> {
            return FILTER_REQUEST_FUNCTION.equals(func.funcName.value);
        }).findFirst().get().symbol;
    }
}
