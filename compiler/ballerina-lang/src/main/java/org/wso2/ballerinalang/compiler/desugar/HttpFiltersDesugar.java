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
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
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
 *              if(!_$$_filter.filterRequest()){
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

    private static final String HTTP_CONNECTION_VAR = "conn";
    private static final String HTTP_ENDPOINT_CONFIG = "config";
    private static final String HTTP_FILTERS_VAR = "filters";
    private static final String HTTP_FILTER_VAR = "filter";
    private static final String HTTP_FILTERCONTEXT_VAR = "filterContext";
    private static final String FILTER_REQUEST_FUNCTION = "filterRequest";

    private static final String ORG_NAME = "ballerina";
    private static final String PACKAGE_NAME = "http";
    private static final String ENDPOINT_TYPE_NAME = "Listener";
    private static final String ORG_SEPARATOR = "/";

    private static final int ENDPOINT_PARAM_NUM = 0;
    private static final int REQUEST_PARAM_NUM = 1;
    private static final int CONNECTOR_FIELD_INDEX = 3;
    private static final int FILTER_CONTEXT_FIELD_INDEX = 1;
    private static final int ENDPOINT_CONFIG_INDEX = 4;
    private static final int FILTERS_CONFIG_INDEX = 6;
    
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
    void invokeFilters(BLangResource resourceNode, SymbolEnv env) {
        BLangSimpleVariable endpoint;
        if (resourceNode.requiredParams.size() >= 2) {
            endpoint = resourceNode.requiredParams.get(0);
            if (ORG_NAME.equals(endpoint.type.tsymbol.pkgID.orgName.value) && PACKAGE_NAME.equals(
                    endpoint.type.tsymbol.pkgID.name.value) && ENDPOINT_TYPE_NAME.equals(
                    endpoint.type.tsymbol.name.value)) {
                addFilterStatements(resourceNode, env);
            }
        }
    }

    private void addFilterStatements(BLangResource resourceNode, SymbolEnv env) {
        BLangSimpleVariable filterContextVar = addFilterContextCreation(resourceNode, env);
        addAssignmentAndForEach(resourceNode, filterContextVar);
    }

    /**
     * Adds code {@code http:FilterContext _$$_filterContext = new (serviceTypeDef, "serviceName", "resourceName");}.
     *
     * @param resourceNode The resource to add the FilterContext creation.
     * @param env          the symbol environment.
     */
    private BLangSimpleVariable addFilterContextCreation(BLangResource resourceNode, SymbolEnv env) {
        BLangIdentifier pkgAlias = ASTBuilderUtil.createIdentifier(resourceNode.pos, getPackageAlias(env));
        BLangUserDefinedType filterContextUserDefinedType = new BLangUserDefinedType(
                pkgAlias, ASTBuilderUtil.createIdentifier(resourceNode.pos, "FilterContext"));
        BType filterContextType = symResolver.resolveTypeNode(filterContextUserDefinedType, env);
        String filterContextVarName = GEN_VAR_PREFIX.value + HTTP_FILTERCONTEXT_VAR;

        BLangSimpleVarRef serviceRef = new BLangSimpleVarRef();
        serviceRef.variableName = ((BLangService) resourceNode.parent).name;
        serviceRef.type = symTable.typeDesc;
        serviceRef.pos = resourceNode.pos;
        serviceRef.symbol = ((BLangService) resourceNode.parent).symbol;

        BLangLiteral serviceName = new BLangLiteral();
        serviceName.value = ((BLangService) resourceNode.parent).name.value;
        serviceName.type = symTable.stringType;
        serviceName.pos = resourceNode.pos;

        BLangLiteral resourceName = new BLangLiteral();
        resourceName.value = resourceNode.name.value;
        resourceName.type = symTable.stringType;
        resourceName.pos = resourceNode.pos;

        BLangInvocation filterInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        filterInvocation.symbol = ((BObjectTypeSymbol) filterContextType.tsymbol).initializerFunc.symbol;
        filterInvocation.pos = resourceNode.pos;
        filterInvocation.requiredArgs.add(serviceRef);
        filterInvocation.requiredArgs.add(serviceName);
        filterInvocation.requiredArgs.add(resourceName);
        filterInvocation.argExprs.add(serviceRef);
        filterInvocation.argExprs.add(serviceName);
        filterInvocation.argExprs.add(resourceName);
        filterInvocation.type = symTable.nilType;

        BLangTypeInit filterInitNode = (BLangTypeInit) TreeBuilder.createObjectInitNode();
        filterInitNode.pos = resourceNode.pos;
        filterInitNode.type = filterContextType;
        filterInitNode.objectInitInvocation = filterInvocation;
        filterInitNode.userDefinedType = filterContextUserDefinedType;
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
     *      if(!_$$_filter.filterRequest()){
     *          Done;
     *      }
     *  }
     * </pre></blockquote>
     */
    private void addAssignmentAndForEach(BLangResource resourceNode, BLangSimpleVariable filterContextVar) {
        //Assignment statement START
        BLangSimpleVarRef callerRef = new BLangSimpleVarRef();
        BLangSimpleVariable endpointVar = resourceNode.requiredParams.get(ENDPOINT_PARAM_NUM);
        callerRef.variableName = endpointVar.name;
        callerRef.type = endpointVar.type;
        callerRef.pos = resourceNode.pos;
        callerRef.symbol = endpointVar.symbol;

        BField connVal = ((BLangService) resourceNode.parent).endpointType.fields.get(CONNECTOR_FIELD_INDEX);
        BLangFieldBasedAccess connField = ASTBuilderUtil.createFieldAccessExpr(callerRef, ASTBuilderUtil
                .createIdentifier(resourceNode.pos, HTTP_CONNECTION_VAR));
        connField.type = connVal.type;
        connField.symbol = connVal.symbol;
        connField.pos = resourceNode.pos;

        BField filterContextVal = ((BObjectType) connVal.type).fields.get(FILTER_CONTEXT_FIELD_INDEX);
        BLangFieldBasedAccess filterContextField = ASTBuilderUtil.createFieldAccessExpr(connField, ASTBuilderUtil
                .createIdentifier(resourceNode.pos, HTTP_FILTERCONTEXT_VAR));
        filterContextField.type = filterContextVal.type;
        filterContextField.symbol = filterContextVal.symbol;
        filterContextField.pos = resourceNode.pos;

        BLangSimpleVarRef filterContextRef = new BLangSimpleVarRef();
        filterContextRef.variableName = filterContextVar.name;
        filterContextRef.type = filterContextVar.type;
        filterContextRef.pos = resourceNode.pos;
        filterContextRef.symbol = filterContextVar.symbol;

        BLangAssignment filterContextAssignment = ASTBuilderUtil.createAssignmentStmt(resourceNode.pos,
                                                                                      filterContextField,
                                                                                      filterContextRef, false);

        resourceNode.body.stmts.add(1, filterContextAssignment);
        //Assignment statement END

        //forEach statement START
        BField configVal = ((BLangService) resourceNode.parent).endpointType.fields.get(ENDPOINT_CONFIG_INDEX);
        BField filtersVal = ((BRecordType) configVal.type).fields.get(FILTERS_CONFIG_INDEX);
        BType filtersType = filtersVal.type;
        BType filterType = ((BArrayType) filtersType).eType;

        BLangFieldBasedAccess configField = ASTBuilderUtil.createFieldAccessExpr(callerRef, ASTBuilderUtil
                .createIdentifier(resourceNode.pos, HTTP_ENDPOINT_CONFIG));
        configField.type = configVal.type;
        configField.symbol = configVal.symbol;
        configField.pos = resourceNode.pos;

        BLangFieldBasedAccess filtersField = ASTBuilderUtil.createFieldAccessExpr(configField, ASTBuilderUtil
                .createIdentifier(resourceNode.pos, HTTP_FILTERS_VAR));
        filtersField.type = filtersType;
        filtersField.symbol = filtersVal.symbol;
        filtersField.pos = resourceNode.pos;


        BLangSimpleVarRef filterRef = new BLangSimpleVarRef();
        String filterVarName = GEN_VAR_PREFIX + HTTP_FILTER_VAR;
        filterRef.variableName = ASTBuilderUtil.createIdentifier(resourceNode.pos, filterVarName);
        filterRef.type = filterType;
        filterRef.pos = resourceNode.pos;
        filterRef.symbol = new BVarSymbol(0, new Name(filterVarName), resourceNode.symbol.pkgID, filterType,
                                          resourceNode.symbol);

        BLangDone doneNode = (BLangDone) TreeBuilder.createDoneNode();
        doneNode.pos = resourceNode.pos;

        BLangBlockStmt doneStatement = ASTBuilderUtil.createBlockStmt(resourceNode.pos,
                                                                      createSingletonArrayList(doneNode));

        BLangSimpleVarRef requestRef = new BLangSimpleVarRef();
        BLangSimpleVariable requestVar = resourceNode.requiredParams.get(REQUEST_PARAM_NUM);
        requestRef.variableName = requestVar.name;
        requestRef.type = requestVar.type;
        requestRef.pos = requestVar.pos;
        requestRef.symbol = requestVar.symbol;

        BLangInvocation filterRequestInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        filterRequestInvocation.symbol = getFilterRequestFuncSymbol(filterType);
        filterRequestInvocation.pos = resourceNode.pos;
        filterRequestInvocation.requiredArgs.add(callerRef);
        filterRequestInvocation.requiredArgs.add(requestRef);
        filterRequestInvocation.requiredArgs.add(filterContextRef);
        filterRequestInvocation.type = symTable.booleanType;
        filterRequestInvocation.expr = filterRef;

        BLangUnaryExpr unaryExpr = ASTBuilderUtil.createUnaryExpr(
                resourceNode.pos, filterRequestInvocation, symTable.booleanType, OperatorKind.NOT,
                new BOperatorSymbol(names.fromString(OperatorKind.NOT.value()), symTable.rootPkgSymbol.pkgID,
                                    new BInvokableType(createSingletonArrayList(symTable.booleanType),
                                                       symTable.booleanType, null), symTable.rootPkgSymbol,
                                    InstructionCodes.BNOT));

        BLangBracedOrTupleExpr ifBraceExpr = (BLangBracedOrTupleExpr) TreeBuilder.createBracedOrTupleExpression();
        ifBraceExpr.expressions.add(unaryExpr);
        ifBraceExpr.isBracedExpr = true;
        ifBraceExpr.pos = resourceNode.pos;
        ifBraceExpr.type = symTable.booleanType;

        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = resourceNode.pos;
        ifNode.body = doneStatement;
        ifNode.expr = ifBraceExpr;

        BLangBlockStmt ifStatement = ASTBuilderUtil.createBlockStmt(resourceNode.pos,
                                                                    createSingletonArrayList(ifNode));

        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = resourceNode.pos;
        foreach.body = ifStatement;
        foreach.collection = filtersField;
        foreach.varRefs.add(filterRef);
        foreach.varTypes = createSingletonArrayList(filterType);

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
