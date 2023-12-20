/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.desugar;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.Constants.INIT_FUNC_COUNT_PER_CLASS;
import static org.wso2.ballerinalang.compiler.util.Constants.INIT_METHOD_SPLIT_SIZE;
import static org.wso2.ballerinalang.compiler.util.Constants.MAX_LISTENER_COUNT_PER_METHOD;

/**
 * Split large init, start, stop BLangFunctions into several smaller functions.
 *
 * @since 2201.1.0
 */
public class LargeMethodSplitter {

    private static final String SPLIT_START_FUNCTIONS_CLASS_NAME = "$_start";
    private static final String SPLIT_STOP_FUNCTIONS_CLASS_NAME = "$_stop";
    private int initFuncIndex = 0;
    private int startFuncIndex = 0;
    private int stopFuncIndex = 0;

    private final Desugar desugar;
    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;

    private static final CompilerContext.Key<LargeMethodSplitter> LARGE_METHOD_SPLITTER_KEY =
            new CompilerContext.Key<>();

    public static LargeMethodSplitter getInstance(CompilerContext context) {
        LargeMethodSplitter largeMethodSplitter = context.get(LARGE_METHOD_SPLITTER_KEY);
        if (largeMethodSplitter == null) {
            largeMethodSplitter = new LargeMethodSplitter(context);
        }
        return largeMethodSplitter;
    }

    private LargeMethodSplitter(CompilerContext context) {
        context.put(LARGE_METHOD_SPLITTER_KEY, this);
        this.desugar = Desugar.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
    }

    void splitLargeFunctions(BLangPackage pkgNode, SymbolEnv env) {
        pkgNode.initFunction = splitInitFunction(pkgNode, env);
        pkgNode.startFunction = splitStartFunction(pkgNode, env);
        pkgNode.stopFunction = splitStopFunction(pkgNode, env);
    }

    private static BLangDiagnosticLocation getNewFuncPos(Location packageNodePos, String packageFileName,
                                                         int splitInitFuncClassCount) {
        LineRange lineRange = packageNodePos.lineRange();
        TextRange textRange = packageNodePos.textRange();
        LinePosition startLine = lineRange.startLine();
        LinePosition endLine = lineRange.endLine();
        return new BLangDiagnosticLocation(packageFileName + "$" + splitInitFuncClassCount, startLine.line(),
                endLine.line(), startLine.offset(), endLine.offset(), textRange.startOffset(), textRange.length());
    }

    /**
     * Split package init function into several smaller functions and put them into multiple classes
     * if the function count is very high.
     *
     * @param packageNode package node
     * @param env symbol environment
     * @return initial init function but trimmed in size
     */
    private BLangFunction splitInitFunction(BLangPackage packageNode, SymbolEnv env) {
        int splitInitFuncClassCount = 1;
        int splitFuncCount = 0;
        Location packageNodePos = packageNode.pos;
        String packageFileName = packageNodePos.lineRange().fileName();
        BLangDiagnosticLocation newFuncPos = getNewFuncPos(packageNodePos, packageFileName, splitInitFuncClassCount);
        splitInitFuncClassCount++;
        BLangBlockFunctionBody funcBody = (BLangBlockFunctionBody) packageNode.initFunction.body;
        BLangFunction initFunction = packageNode.initFunction;

        List<BLangFunction> generatedFunctions = new ArrayList<>();
        List<BLangStatement> stmts = new ArrayList<>(funcBody.stmts);
        funcBody.stmts.clear();
        BLangFunction newFunc = initFunction;
        BLangBlockFunctionBody newFuncBody = (BLangBlockFunctionBody) newFunc.body;

        // until we get to a varDef, stmts are independent, divide it based on methodSize
        int varDefIndex = 0;
        for (int i = 0; i < stmts.size(); i++) {
            BLangStatement statement = stmts.get(i);
            if (statement.getKind() == NodeKind.VARIABLE_DEF) {
                break;
            }
            varDefIndex++;
            if (i > 0 && (i % INIT_METHOD_SPLIT_SIZE == 0 || isAssignmentWithInitOrRecordLiteralExpr(statement))) {
                generatedFunctions.add(newFunc);
                newFunc = createIntermediateInitFunction(packageNode, env);
                splitFuncCount++;
                if (splitFuncCount % INIT_FUNC_COUNT_PER_CLASS == 0) {
                    newFuncPos = getNewFuncPos(packageNodePos, packageFileName, splitInitFuncClassCount);
                    splitInitFuncClassCount++;
                }
                newFunc.pos = newFuncPos;
                newFuncBody = (BLangBlockFunctionBody) newFunc.body;
                symTable.rootScope.define(names.fromIdNode(newFunc.name), newFunc.symbol);
            }
            newFuncBody.stmts.add(stmts.get(i));
        }

        newFuncBody.stmts.addAll(stmts.subList(varDefIndex, stmts.size()));
        generatedFunctions.add(newFunc);

        for (int j = 0; j < generatedFunctions.size() - 1; j++) {
            BLangFunction thisFunction = generatedFunctions.get(j);

            BLangCheckedExpr checkedExpr =
                    ASTBuilderUtil.createCheckExpr(initFunction.pos,
                            desugar.createInvocationNode(generatedFunctions.get(j + 1).name.value,
                                    new ArrayList<>(), symTable.errorOrNilType),
                            symTable.nilType);
            checkedExpr.equivalentErrorTypeList.add(symTable.errorType);

            BLangExpressionStmt expressionStmt = ASTBuilderUtil
                    .createExpressionStmt(thisFunction.pos, (BLangBlockFunctionBody) thisFunction.body);
            expressionStmt.expr = checkedExpr;
            expressionStmt.expr.pos = initFunction.pos;

            if (j > 0) { // skip init func
                thisFunction = desugar.rewrite(thisFunction, env);
                packageNode.functions.add(thisFunction);
                packageNode.topLevelNodes.add(thisFunction);
            }
        }

        rewriteLastSplitFunction(packageNode, env, generatedFunctions);
        initFuncIndex = 0;
        return generatedFunctions.get(0);
    }

    private boolean isAssignmentWithInitOrRecordLiteralExpr(BLangStatement statement) {
        if (statement.getKind() == NodeKind.ASSIGNMENT) {
            return desugar.isMappingOrObjectConstructorOrObjInit(((BLangAssignment) statement).getExpression());
        }
        return false;
    }

    /**
     * Split package start function into several smaller functions.
     *
     * @param packageNode package node
     * @param env symbol environment
     * @return initial start function but trimmed in size
     */
    private BLangFunction splitStartFunction(BLangPackage packageNode, SymbolEnv env) {
        BLangBlockFunctionBody funcBody = (BLangBlockFunctionBody) packageNode.startFunction.body;
        BLangFunction startFunction = packageNode.startFunction;

        List<BLangFunction> generatedFunctions = new ArrayList<>();
        List<BLangStatement> stmts = new ArrayList<>(funcBody.stmts);
        funcBody.stmts.clear();
        BLangFunction newFunc = startFunction;
        BLangBlockFunctionBody newFuncBody = (BLangBlockFunctionBody) newFunc.body;

        // last statement is the return statement
        // excluding that other statements are just filled into new intermediate start functions sequentially
        // each new function will have MAX_LISTENER_COUNT_PER_METHOD statements at most excluding the return statement
        for (int i = 0; i < stmts.size() - 1; i++) {
            if (i > 0 && (i % MAX_LISTENER_COUNT_PER_METHOD == 0)) {
                generatedFunctions.add(newFunc);
                newFunc = createIntermediateStartFunction(packageNode, env);
                newFuncBody = (BLangBlockFunctionBody) newFunc.body;
                symTable.rootScope.define(names.fromIdNode(newFunc.name), newFunc.symbol);
            }
            newFuncBody.stmts.add(stmts.get(i));
        }

        // original return statement is added to the start function created last
        newFuncBody.stmts.add(stmts.get(stmts.size() - 1));
        generatedFunctions.add(newFunc);

        // statement is added to each function except the last created function
        // this is to call the function created after it and to handle the return values of those two functions
        // so that each function will call the next function in a nested manner
        for (int j = 0; j < generatedFunctions.size() - 1; j++) {
            BLangFunction thisFunction = generatedFunctions.get(j);

            BLangCheckedExpr checkedExpr =
                    ASTBuilderUtil.createCheckExpr(startFunction.pos,
                            desugar.createInvocationNode(generatedFunctions.get(j + 1).name.value,
                                    new ArrayList<>(), symTable.errorOrNilType),
                            symTable.nilType);
            checkedExpr.equivalentErrorTypeList.add(symTable.errorType);

            BLangExpressionStmt expressionStmt = ASTBuilderUtil
                    .createExpressionStmt(thisFunction.pos, (BLangBlockFunctionBody) thisFunction.body);
            expressionStmt.expr = checkedExpr;
            expressionStmt.expr.pos = startFunction.pos;

            if (j > 0) { // skip start func
                thisFunction = desugar.rewrite(thisFunction, env);
                packageNode.functions.add(thisFunction);
                packageNode.topLevelNodes.add(thisFunction);
            }
        }

        // start function created last is also added to the function list
        rewriteLastSplitFunction(packageNode, env, generatedFunctions);
        startFuncIndex = 0;
        return generatedFunctions.get(0);
    }

    /**
     * Split package stop function into several smaller functions.
     *
     * @param packageNode package node
     * @param env symbol environment
     * @return initial stop function but trimmed in size
     */
    private BLangFunction splitStopFunction(BLangPackage packageNode, SymbolEnv env) {
        BLangBlockFunctionBody funcBody = (BLangBlockFunctionBody) packageNode.stopFunction.body;
        BLangFunction stopFunction = packageNode.stopFunction;

        List<BLangFunction> generatedFunctions = new ArrayList<>();
        List<BLangStatement> stmts = new ArrayList<>(funcBody.stmts);
        funcBody.stmts.clear();
        BLangFunction newFunc = stopFunction;
        BLangBlockFunctionBody newFuncBody = (BLangBlockFunctionBody) newFunc.body;

        for (int i = 0; i < stmts.size() - 1; i++) {
            if (i > 0 && (i % MAX_LISTENER_COUNT_PER_METHOD == 0)) {
                generatedFunctions.add(newFunc);
                newFunc = createIntermediateStopFunction(packageNode, env);
                newFuncBody = (BLangBlockFunctionBody) newFunc.body;
                symTable.rootScope.define(names.fromIdNode(newFunc.name), newFunc.symbol);
            }
            newFuncBody.stmts.add(stmts.get(i));
        }

        newFuncBody.stmts.add(stmts.get(stmts.size() - 1));
        generatedFunctions.add(newFunc);

        // For the stop function, splitting is done the same as the start function except here.
        // Here, it is only required to call the next created stop function
        // because the return value is () and not ()|error.
        for (int j = 0; j < generatedFunctions.size() - 1; j++) {
            BLangFunction thisFunction = generatedFunctions.get(j);

            BInvokableSymbol invokableSymbol = (BInvokableSymbol) symTable.rootScope.lookup(
                    new Name(generatedFunctions.get(j + 1).name.value)).symbol;
            BLangInvocation invocationExpr = ASTBuilderUtil.createInvocationExpr(stopFunction.pos, invokableSymbol,
                    new ArrayList<>(), symResolver);

            BLangExpressionStmt expressionStmt = ASTBuilderUtil
                    .createExpressionStmt(thisFunction.pos, (BLangBlockFunctionBody) thisFunction.body);
            expressionStmt.expr = invocationExpr;
            expressionStmt.expr.pos = stopFunction.pos;

            if (j > 0) { // skip start func
                thisFunction = desugar.rewrite(thisFunction, env);
                packageNode.functions.add(thisFunction);
                packageNode.topLevelNodes.add(thisFunction);
            }
        }

        rewriteLastSplitFunction(packageNode, env, generatedFunctions);
        stopFuncIndex = 0;
        return generatedFunctions.get(0);
    }

    private void rewriteLastSplitFunction(BLangPackage packageNode, SymbolEnv env,
                                          List<BLangFunction> generatedFunctions) {
        if (generatedFunctions.size() > 1) {
            // add last func
            BLangFunction lastFunc = generatedFunctions.get(generatedFunctions.size() - 1);
            lastFunc = desugar.rewrite(lastFunc, env);
            packageNode.functions.add(lastFunc);
            packageNode.topLevelNodes.add(lastFunc);
        }
    }

    /**
     * Create an intermediate package init function.
     *
     * @param pkgNode package node
     * @param env     symbol environment of package
     * @return created intermediate init function
     */
    private BLangFunction createIntermediateInitFunction(BLangPackage pkgNode, SymbolEnv env) {
        String alias = pkgNode.symbol.pkgID.toString();
        BLangFunction initFunction = ASTBuilderUtil
                .createInitFunctionWithErrorOrNilReturn(pkgNode.pos, alias,
                        new Name(Names.INIT_FUNCTION_SUFFIX.value
                                + this.initFuncIndex++), symTable);
        // Create invokable symbol for init function
        desugar.createInvokableSymbol(initFunction, env);
        return initFunction;
    }

    /**
     * Create an intermediate package start function.
     *
     * @param pkgNode package node
     * @param env     symbol environment of package
     * @return created intermediate start function
     */
    private BLangFunction createIntermediateStartFunction(BLangPackage pkgNode, SymbolEnv env) {
        String alias = pkgNode.symbol.pkgID.toString();
        BLangFunction startFunction = ASTBuilderUtil
                .createInitFunctionWithErrorOrNilReturn(new BLangDiagnosticLocation(SPLIT_START_FUNCTIONS_CLASS_NAME,
                                0, 0, 0, 0, 0, 0),
                        alias, new Name(Names.START_FUNCTION_SUFFIX.value + this.startFuncIndex++), symTable);
        // Create invokable symbol for start function
        desugar.createInvokableSymbol(startFunction, env);
        return startFunction;
    }

    /**
     * Create an intermediate package stop function.
     *
     * @param pkgNode package node
     * @param env     symbol environment of package
     * @return created intermediate stop function
     */
    private BLangFunction createIntermediateStopFunction(BLangPackage pkgNode, SymbolEnv env) {
        String alias = pkgNode.symbol.pkgID.toString();
        BLangFunction stopFunction = ASTBuilderUtil
                .createInitFunctionWithNilReturn(new BLangDiagnosticLocation(SPLIT_STOP_FUNCTIONS_CLASS_NAME,
                                0, 0, 0, 0, 0, 0),
                        alias, new Name(Names.STOP_FUNCTION_SUFFIX.value + this.stopFuncIndex++));
        // Create invokable symbol for stop function
        desugar.createInvokableSymbol(stopFunction, env);
        return stopFunction;
    }
}
