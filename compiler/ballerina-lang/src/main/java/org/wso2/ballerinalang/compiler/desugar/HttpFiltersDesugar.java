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
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
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
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;
import static org.wso2.ballerinalang.compiler.util.Names.GEN_VAR_PREFIX;

/**
 * This class injects the code that invokes the http filters to the first lines of an http resource. The code injected
 * is as follows:
 * <blockquote><pre>
 *          http:FilterContext _$$_filterContext = new (serviceTypeDef, "serviceName", "resourceName");
 *          caller.conn.filterContext = _$$_filterContext;
 *          foreach _$$_filter in caller.config.filters {
 *              if(_$$_filter is http:FilterRequest  &amp;&amp; !_$$_filter.filterRequest(caller, req,
 *              _$$_filterContext)){
 *                  return;
 *              }
 *          }
 * </pre></blockquote>
 * The second line in this code stores the _$$_filterContext reference to the http connector to be used when calling the
 * filterResponse method of the filters
 *
 * @since 0.974.1
 */
public class HttpFiltersDesugar {

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;

    private static final String HTTP_ENDPOINT_CONFIG = "config";
    private static final String HTTP_FILTERS_VAR = "filters";
    private static final String HTTP_FILTER_VAR = "filter";
    private static final String HTTP_FILTERCONTEXT_VAR = "filterContext";
    private static final String FILTER_REQUEST_FUNCTION = "filterRequest";
    private static final String ANN_RESOURCE_CONFIG = "ResourceConfig";
    private static final String ANN_RESOURCE_ATTR_PATH = "path";
    private static final String ANN_RESOURCE_ATTR_WS_UPGRADE = "webSocketUpgrade";
    private static final String ANN_RESOURCE_ATTR_WS_UPGRADE_PATH = "upgradePath";
    private static final String ANN_RESOURCE_PARAM_ORDER_CONFIG = "ParamOrderConfig";
    private static final String ANN_RECORD_PARAM_ORDER_CONFIG = "HttpParamOrderConfig";
    private static final String ANN_FIELD_PATH_PARAM_ORDER = "pathParamOrder";

    private static final String ORG_NAME = "ballerina";
    private static final String PACKAGE_NAME = "http";
    private static final String CALLER_TYPE_NAME = "Caller";

    private static final String ORG_SEPARATOR = "/";
    private static final int ENDPOINT_PARAM_NUM = 0;
    private static final int REQUEST_PARAM_NUM = 1;

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
        this.types = Types.getInstance(context);
    }

    /**
     * Check if the resource is an http resource and apply filter.
     *
     * @param resourceNode The resource to apply filter on if it is http
     * @param env          the symbol environment
     */
    void addHttpFilterStatementsToResource(BLangFunction resourceNode, SymbolEnv env) {
        if (isHttpResource(resourceNode)) {
            addFilterStatements(resourceNode, env);
        }
    }

    private boolean isHttpResource(BLangFunction resourceNode) {
        if (resourceNode.requiredParams.size() < 2) {
            return false;
        }
        BTypeSymbol tsymbol = resourceNode.requiredParams.get(0).type.tsymbol;
        return ORG_NAME.equals(tsymbol.pkgID.orgName.value) && PACKAGE_NAME.equals(tsymbol.pkgID.name.value) &&
                CALLER_TYPE_NAME.equals(tsymbol.name.value);
    }

    private void addFilterStatements(BLangFunction resourceNode, SymbolEnv env) {
        BLangSimpleVariable filterContextVar = addFilterContextCreation(resourceNode, env);
        addAssignmentAndForEach(resourceNode, filterContextVar, env);
    }

    /**
     * Adds code {@code http:FilterContext _$$_filterContext = new (serviceTypeDef, "serviceName", "resourceName");}.
     *
     * @param resourceNode The resource to add the FilterContext creation.
     * @param env          the symbol environment.
     */
    private BLangSimpleVariable addFilterContextCreation(BLangFunction resourceNode, SymbolEnv env) {
        BLangIdentifier pkgAlias =
                ASTBuilderUtil.createIdentifier(resourceNode.pos, getPackageAlias(env, resourceNode));
        BLangUserDefinedType filterContextUserDefinedType = new BLangUserDefinedType(
                pkgAlias, ASTBuilderUtil.createIdentifier(resourceNode.pos, "FilterContext"));
        filterContextUserDefinedType.pos = resourceNode.pos;
        BType filterContextType = symResolver.resolveTypeNode(filterContextUserDefinedType, env);
        String filterContextVarName = GEN_VAR_PREFIX.value + HTTP_FILTERCONTEXT_VAR;

        BLangSimpleVariable serviceSelf = (BLangSimpleVariable) resourceNode.getReceiver();
        BLangSimpleVarRef.BLangLocalVarRef serviceRef = new BLangSimpleVarRef.BLangLocalVarRef(serviceSelf.symbol);
        serviceRef.type = serviceSelf.type;
        serviceRef.pos = resourceNode.pos;

        BLangLiteral serviceName = new BLangLiteral();
        serviceName.value = getServiceName(serviceSelf.type.tsymbol.name.value);
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
                               resourceNode.symbol, resourceNode.pos, VIRTUAL));
        filterContextVar.typeNode = filterContextUserDefinedType;
        addStatementToResourceBody(resourceNode.body,
                                   ASTBuilderUtil.createVariableDef(resourceNode.pos, filterContextVar), 0);
        return filterContextVar;
    }

    private void addStatementToResourceBody(BLangFunctionBody body, BLangStatement stmt, int index) {
        NodeKind bodyKind = body.getKind();

        if (bodyKind == NodeKind.BLOCK_FUNCTION_BODY) {
            ((BLangBlockFunctionBody) body).stmts.add(index, stmt);
        } else if (bodyKind == NodeKind.EXPR_FUNCTION_BODY) {
            BLangExprFunctionBody exprFnBody = (BLangExprFunctionBody) body;
            exprFnBody.expr = desugarExprFuncBody(exprFnBody.expr, stmt);
        } else {
            throw new IllegalStateException("Invalid resource method body type: " + bodyKind.toString());
        }
    }

    private BLangStatementExpression desugarExprFuncBody(BLangExpression expr, BLangStatement stmt) {
        if (expr.getKind() == NodeKind.STATEMENT_EXPRESSION) {
            BLangStatementExpression stmtExpr = (BLangStatementExpression) expr;
            ((BLangBlockStmt) stmtExpr.stmt).addStatement(stmt);
            return stmtExpr;
        }
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(expr.pos);
        blockStmt.addStatement(stmt);
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(blockStmt, expr);
        stmtExpr.type = expr.type;
        return stmtExpr;
    }

    private String getServiceName(String serviceTypeName) {
        int serviceIndex = serviceTypeName.lastIndexOf("$$service$");
        return serviceTypeName.substring(0, serviceIndex);
    }

    /**
     * Get the alias name of the http import.
     *
     * @param env the symbol environment.
     * @return the alias name.
     */
    private String getPackageAlias(SymbolEnv env, BLangNode node) {
        String compUnitName = node.pos.getSource().getCompilationUnitName();
        for (BLangImportPackage importStmt : env.enclPkg.imports) {
            if (!ORG_NAME.equals(importStmt.symbol.pkgID.orgName.value) ||
                    !PACKAGE_NAME.equals(importStmt.symbol.pkgID.name.value)) {
                continue;
            }

            if (importStmt.compUnit.value.equals(compUnitName)) {
                return importStmt.alias.value;
            }

        }

        return PACKAGE_NAME;
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
    private void addAssignmentAndForEach(BLangFunction resourceNode, BLangSimpleVariable filterContextVar,
                                         SymbolEnv env) {
        //Assignment statement START
        BLangSimpleVariable endpointVar = resourceNode.requiredParams.get(ENDPOINT_PARAM_NUM);
        BLangSimpleVarRef.BLangLocalVarRef callerRef = new BLangSimpleVarRef.BLangLocalVarRef(endpointVar.symbol);
        callerRef.type = endpointVar.type;
        callerRef.pos = resourceNode.pos;

        BLangLiteral filterContextName = new BLangLiteral();
        filterContextName.value = HTTP_FILTERCONTEXT_VAR;
        filterContextName.type = symTable.stringType;
        filterContextName.pos = resourceNode.pos;

        BField filterContextVal = ((BObjectType) endpointVar.type).fields.get(HTTP_FILTERCONTEXT_VAR);
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

        addStatementToResourceBody(resourceNode.body, filterContextAssignment, 1);
        //Assignment statement END

        //forEach statement START
        BField configVal = ((BObjectType) endpointVar.type).fields.get(HTTP_ENDPOINT_CONFIG);
        BField filtersVal = ((BRecordType) configVal.type).fields.get(HTTP_FILTERS_VAR);
        BType filtersType = filtersVal.type;
        BUnionType filterUnionType = (BUnionType) ((BArrayType) filtersType).eType;
        BLangIdentifier pkgAlias =
                ASTBuilderUtil.createIdentifier(resourceNode.pos, getPackageAlias(env, resourceNode));
        BLangUserDefinedType filterUserDefinedType = new BLangUserDefinedType(
                pkgAlias, ASTBuilderUtil.createIdentifier(resourceNode.pos, "RequestFilter"));
        filterUserDefinedType.pos = resourceNode.pos;
        BObjectType filterType = (BObjectType) symResolver.resolveTypeNode(filterUserDefinedType, env);

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

        BLangSimpleVarRef unionFilterRef = ASTBuilderUtil.createVariableRef(
                resourceNode.pos, new BVarSymbol(0, new Name(filterVarName), resourceNode.symbol.pkgID, filterUnionType,
                                                 resourceNode.symbol, resourceNode.pos, VIRTUAL));
        unionFilterRef.variableName = ASTBuilderUtil.createIdentifier(resourceNode.pos, filterVarName);
        unionFilterRef.type = filterType;
        unionFilterRef.pos = resourceNode.pos;

        // Create a new variable definition. This is needed for the foreach node.
        BLangSimpleVariable variable = ASTBuilderUtil.createVariable(resourceNode.pos, filterVarName, filterType,
                                                                     null, (BVarSymbol) unionFilterRef.symbol);
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
        // lhsExpr
        BLangTypeTestExpr filterTypeCheckExpr = ASTBuilderUtil.createTypeTestExpr(resourceNode.pos, unionFilterRef,
                                                                                  filterUserDefinedType);

        // rhsExpr
        BLangSimpleVariable requestVar = resourceNode.requiredParams.get(REQUEST_PARAM_NUM);
        BLangSimpleVarRef.BLangLocalVarRef requestRef = new BLangSimpleVarRef.BLangLocalVarRef(requestVar.symbol);
        requestRef.variableName = requestVar.name;
        requestRef.type = requestVar.type;
        requestRef.pos = requestVar.pos;

        List<BLangExpression> requiredArgs = new ArrayList<>();
        requiredArgs.add(callerRef);
        requiredArgs.add(requestRef);
        requiredArgs.add(filterContextRef);

        BLangInvocation filterRequestInvocation = ASTBuilderUtil.createInvocationExprForMethod(
                resourceNode.pos, getFilterRequestFuncSymbol(filterType), requiredArgs, symResolver);
        filterRequestInvocation.expr = unionFilterRef;
        filterRequestInvocation.type = symTable.booleanType;
        filterRequestInvocation.async = false;

        BInvokableType type = new BInvokableType(createSingletonArrayList(symTable.booleanType), symTable.booleanType,
                                                 null);
        BOperatorSymbol notOperatorSymbol = new BOperatorSymbol(
                names.fromString(OperatorKind.NOT.value()), symTable.rootPkgSymbol.pkgID, type, symTable.rootPkgSymbol,
                symTable.builtinPos, VIRTUAL);
        BLangUnaryExpr unaryExpr = ASTBuilderUtil.createUnaryExpr(
                resourceNode.pos, filterRequestInvocation, symTable.booleanType, OperatorKind.NOT, notOperatorSymbol);
        BOperatorSymbol andOperatorSymbol = new BOperatorSymbol(
                names.fromString(OperatorKind.AND.value()), symTable.rootPkgSymbol.pkgID, type, symTable.rootPkgSymbol,
                symTable.builtinPos, VIRTUAL);
        BLangBinaryExpr binaryExpr = ASTBuilderUtil.createBinaryExpr(
                resourceNode.pos, filterTypeCheckExpr, unaryExpr, symTable.booleanType, OperatorKind.AND,
                andOperatorSymbol);
        BLangGroupExpr groupExpr = new BLangGroupExpr();
        groupExpr.expression = binaryExpr;
        groupExpr.typedescType = symTable.booleanType;

        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = resourceNode.pos;
        ifNode.body = doneStatement;
        ifNode.expr = groupExpr;
        ifNode.statementLink = new BLangStatement.BLangStatementLink();
        ifNode.statementLink.statement = ifNode;

        BLangBlockStmt ifStatement = ASTBuilderUtil.createBlockStmt(resourceNode.pos,
                                                                    createSingletonArrayList(ifNode));

        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = resourceNode.pos;
        foreach.body = ifStatement;
        foreach.collection = filtersField;
        foreach.isDeclaredWithVar = false;
        this.types.setForeachTypedBindingPatternType(foreach);
        foreach.variableDefinitionNode = variableDefinition;

        addStatementToResourceBody(resourceNode.body, foreach, 2);
        //forEach statement END
    }

    private <E> List<E> createSingletonArrayList(E val) {
        List<E> list = new ArrayList<>();
        list.add(val);
        return list;
    }

    private BInvokableSymbol getFilterRequestFuncSymbol(BType filterType) {
        return ((BObjectTypeSymbol) filterType.tsymbol).attachedFuncs.stream().filter(func -> {
            return FILTER_REQUEST_FUNCTION.equals(func.funcName.value);
        }).findFirst().get().symbol;
    }

    /**
     * Check if the resource is an http resource and inject ParamOrderConfig annotation.
     *
     * @param resourceNode The resource to inject annotation
     * @param env          the symbol environment
     */
    void addCustomAnnotationToResource(BLangFunction resourceNode, SymbolEnv env) {
        if (isHttpResource(resourceNode)) {
            addOrderParamConfig(resourceNode, env);
        }
    }

    private void addOrderParamConfig(BLangFunction resourceNode, SymbolEnv env) {
        List<RecordLiteralNode.RecordField> annotationValues = null;
        for (BLangAnnotationAttachment annotationAttachment : resourceNode.getAnnotationAttachments()) {
            if (ANN_RESOURCE_CONFIG.equals(annotationAttachment.getAnnotationName().getValue()) &&
                    annotationAttachment.getExpression() != null) {
                annotationValues = ((BLangRecordLiteral) annotationAttachment.getExpression()).fields;
                break;
            }
        }
        if (annotationValues == null) {
            return;
        }
        for (RecordLiteralNode.RecordField field : annotationValues) {
            BLangExpression expression = field.isKeyValueField() ?
                    ((BLangRecordLiteral.BLangRecordKeyValueField) field).valueExpr :
                    (BLangRecordLiteral.BLangRecordVarNameField) field;

            switch (getAnnotationFieldKey(field)) {
                case ANN_RESOURCE_ATTR_WS_UPGRADE:
                    for (RecordLiteralNode.RecordField upgradeField : ((BLangRecordLiteral) expression).getFields()) {
                        if (getAnnotationFieldKey(upgradeField).equals(ANN_RESOURCE_ATTR_WS_UPGRADE_PATH)) {
                            addParamOrderConfigAnnotation(resourceNode, upgradeField.isKeyValueField() ?
                                    ((BLangRecordLiteral.BLangRecordKeyValueField) upgradeField).valueExpr :
                                    (BLangRecordLiteral.BLangRecordVarNameField) upgradeField, env);
                            break;
                        }
                    }
                    break;
                case ANN_RESOURCE_ATTR_PATH:
                    addParamOrderConfigAnnotation(resourceNode, expression, env);
                    break;
            }
        }
    }

    private static String getAnnotationFieldKey(RecordLiteralNode.RecordField field) {
        if (!field.isKeyValueField()) {
            return ((BLangSimpleVarRef) field).variableName.getValue();
        }

        return ((BLangSimpleVarRef) (((BLangRecordLiteral.BLangRecordKeyValueField) field).key).expr)
                .variableName.getValue();
    }

    private static boolean checkForPathParam(List<BLangSimpleVariable> parameters, BLangExpression value) {
        return parameters.size() > 2 && value.toString().contains("{") && value.toString().contains("}");
    }

    private void addParamOrderConfigAnnotation(BLangFunction resourceNode, BLangExpression value, SymbolEnv env) {
        if (!checkForPathParam(resourceNode.getParameters(), value)) {
            return;
        }
        DiagnosticPos pos = resourceNode.pos;
        BLangAnnotationAttachment annoAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        resourceNode.addAnnotationAttachment(annoAttachment);
        BSymbol annSymbol = lookupAnnotationSpaceSymbolInPackage(symResolver, resourceNode.pos, env, names.fromString
                (PACKAGE_NAME), names.fromString(ANN_RESOURCE_PARAM_ORDER_CONFIG));

        if (annSymbol == symTable.notFoundSymbol) {
            return;
        }
        if (annSymbol instanceof BAnnotationSymbol) {
            annoAttachment.annotationSymbol = (BAnnotationSymbol) annSymbol;
        }
        annoAttachment.annotationName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        annoAttachment.annotationName.value = ANN_RESOURCE_PARAM_ORDER_CONFIG;
        annoAttachment.pos = pos;
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        annoAttachment.expr = literalNode;
        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setValue(PACKAGE_NAME);
        annoAttachment.pkgAlias = pkgAlias;
        annoAttachment.attachPoints.add(AttachPoint.Point.RESOURCE);
        literalNode.pos = pos;
        BStructureTypeSymbol bStructSymbol;
        BSymbol annTypeSymbol = lookupMainSpaceSymbolInPackage(symResolver, resourceNode.pos, env, names.fromString
                (PACKAGE_NAME), names.fromString(ANN_RECORD_PARAM_ORDER_CONFIG));
        if (annTypeSymbol == symTable.notFoundSymbol) {
            return;
        }
        if (annTypeSymbol instanceof BStructureTypeSymbol) {
            bStructSymbol = (BStructureTypeSymbol) annTypeSymbol;
            literalNode.type = bStructSymbol.type;
        }

        // Create pathParamOrder record literal
        BLangRecordLiteral.BLangRecordKeyValueField pathParamOrderKeyValue =
                (BLangRecordLiteral.BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
        literalNode.fields.add(pathParamOrderKeyValue);

        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = ANN_FIELD_PATH_PARAM_ORDER;
        keyLiteral.type = symTable.stringType;

        BLangRecordLiteral paramOrderLiteralNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        paramOrderLiteralNode.type = new BMapType(TypeTags.MAP, symTable.intType, symTable.mapType.tsymbol);

        pathParamOrderKeyValue.key = new BLangRecordLiteral.BLangRecordKey(keyLiteral);
        pathParamOrderKeyValue.valueExpr = paramOrderLiteralNode;

        getParamMapper(resourceNode.getParameters(), value.toString()).forEach((paramName, paramIndex) -> {
            // Create a new literal for the key.
            BLangLiteral paramKeyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            paramKeyLiteral.value = paramName;
            paramKeyLiteral.type = symTable.stringType;

            // Create a new literal for the value.
            BLangLiteral paramValueLiteral = new BLangLiteral();
            paramValueLiteral.value = Long.valueOf(paramIndex);
            paramValueLiteral.type = symTable.intType;

            // Create a new key-value.
            BLangRecordLiteral.BLangRecordKeyValueField pathParamOrderEntry =
                    new BLangRecordLiteral.BLangRecordKeyValueField();
            pathParamOrderEntry.key = new BLangRecordLiteral.BLangRecordKey(paramKeyLiteral);
            pathParamOrderEntry.valueExpr = paramValueLiteral;
            paramOrderLiteralNode.fields.add(pathParamOrderEntry);
        });
    }

    private HashMap<String, Integer> getParamMapper(List<BLangSimpleVariable> parameters, String path) {
        HashMap<String, Integer> mapper = new HashMap<>();
        int startIndex = 0;
        int pointerIndex = 0;
        while (pointerIndex < path.length()) {
            char ch = path.charAt(pointerIndex);
            switch (ch) {
                case '{':
                    startIndex = pointerIndex + 1;
                    break;
                case '}':
                    String token = path.substring(startIndex, pointerIndex);
                    IntStream.range(2, parameters.size()).filter(i -> parameters.get(i).getName().getValue()
                            .equals(token)).findFirst().ifPresent(i -> mapper.put(token, i));
                    break;
            }
            pointerIndex++;
        }
        return mapper;
    }

    private BSymbol lookupMainSpaceSymbolInPackage(SymbolResolver symResolver, DiagnosticPos pos, SymbolEnv env,
                                                   Name pkgAlias, Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return symResolver.lookupSymbolInMainSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol = symResolver.resolvePkgSymbol(pos, env, pkgAlias);
        if (pkgSymbol == symTable.notFoundSymbol) {
            return pkgSymbol;
        }

        // 3) Look up the package scope without considering the access modifier.
        Scope.ScopeEntry entry = pkgSymbol.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.MAIN) == SymTag.MAIN) {
                return entry.symbol;
            }
            entry = entry.next;
        }
        return symTable.notFoundSymbol;
    }

    private BSymbol lookupAnnotationSpaceSymbolInPackage(SymbolResolver symResolver, DiagnosticPos pos, SymbolEnv env,
                                                   Name pkgAlias, Name name) {
        // 1) Look up the current package if the package alias is empty.
        if (pkgAlias == Names.EMPTY) {
            return symResolver.lookupSymbolInAnnotationSpace(env, name);
        }

        // 2) Retrieve the package symbol first
        BSymbol pkgSymbol = symResolver.resolvePkgSymbol(pos, env, pkgAlias);
        if (pkgSymbol == symTable.notFoundSymbol) {
            return pkgSymbol;
        }

        // 3) Look up the package scope without considering the access modifier.
        Scope.ScopeEntry entry = pkgSymbol.scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.ANNOTATION) == SymTag.ANNOTATION) {
                return entry.symbol;
            }
            entry = entry.next;
        }
        return symTable.notFoundSymbol;
    }
}
