/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.UsedState;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.SimpleBLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.model.tree.NodeKind.CLASS_DEFN;
import static org.ballerinalang.model.tree.NodeKind.FUNCTION;

/**
 * Analyze function invocation chains and detect unused functions.
 */
public class DeadCodeAnalyzer extends SimpleBLangNodeAnalyzer<DeadCodeAnalyzer.AnalyzerData> {

    private static final CompilerContext.Key<DeadCodeAnalyzer> DEAD_CODE_ANALYZER_KEY = new CompilerContext.Key<>();
    private static final String MAIN_FUNCTION_NAME = "main";
    private static final String INIT_FUNCTION_NAME = "init";
    final DeadCodeAnalyzer.AnalyzerData data = new DeadCodeAnalyzer.AnalyzerData();

    private DeadCodeAnalyzer(CompilerContext context) {
        context.put(DEAD_CODE_ANALYZER_KEY, this);
    }

    public static DeadCodeAnalyzer getInstance(CompilerContext context) {
        DeadCodeAnalyzer deadCodeAnalyzer = context.get(DEAD_CODE_ANALYZER_KEY);
        if (deadCodeAnalyzer == null) {
            deadCodeAnalyzer = new DeadCodeAnalyzer(context);
        }
        return deadCodeAnalyzer;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        visitNode(pkgNode, data);
        return pkgNode;
    }

    private void analyzeTopLevelNodes(BLangPackage pkgNode, DeadCodeAnalyzer.AnalyzerData data) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;

        // Identifying all available functions and registering lambda function pointers
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == FUNCTION) {
                registerTopLevelFunction(topLevelNode);
            }
            if (topLevelNode.getKind() == CLASS_DEFN) {
                registerClassLevelFunctions(topLevelNode);
            }
        }

        // Analyzing the bodies
        for (TopLevelNode topLevelNode : topLevelNodes) {
            visitNode((BLangNode) topLevelNode, data);
        }

        pkgNode.completedPhases.add(CompilerPhase.DEAD_CODE_ANALYZE);
    }

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        // Empty body to stop it from causing infinite loops
    }

    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DEAD_CODE_ANALYZE)) {
            return;
        }
        data.markAsUsed(pkgNode.initFunction.symbol);
        data.markAsUsed(pkgNode.startFunction.symbol);
        data.markAsUsed(pkgNode.stopFunction.symbol);

        analyzeTopLevelNodes(pkgNode, data);
    }

    @Override
    public void visit(BLangFunction function, AnalyzerData data) {
        // body is null for abstract functions
        if (function.getFlags().contains(Flag.INTERFACE)) {
            return;
        }
        // Handling remote functions
        if (function.body.getKind() != NodeKind.BLOCK_FUNCTION_BODY) {
            return; // todo handle these separately
        }

        BInvokableSymbol parentFunctionSymbol = function.originalFuncSymbol;
        // if the parent function is a lambda, it's function pointer is taken for ease of debugging
        if (isLambda(function)) {
            parentFunctionSymbol = (BInvokableSymbol) ((BLangSimpleVariable) ((function).parent).parent).symbol;
        }

        for (BLangStatement stmt : ((BLangBlockFunctionBody) function.getBody()).getStatements()) {
            // Reassign the currentParentFunction in case it got changed inside a visitor call
            // Traversing Lambda functions change it in certain cases
            data.currentParentFunction = parentFunctionSymbol;
            visitNode(stmt, data);
        }
    }

    @Override
    public void visit(BLangArrowFunction node, AnalyzerData data) {
        if (node.parent.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        } else if (node.parent.getBType().getKind() == TypeKind.TYPEREFDESC) {
            return;
        }
        data.addParentFunctionToPkgSymbol(
                (BInvokableSymbol) ((BLangSimpleVariable) node.parent).symbol);  // TODO Find a cleaner way

        visitNode(node.params, data);
        visitNode((BLangNode) node.functionName, data);
        visitNode(node.body, data);
    }

    @Override
    public void visit(BLangInvocation node, AnalyzerData data) {
        if (node.symbol == null) {
            return;
        }
        BInvokableSymbol symbol = (BInvokableSymbol) node.symbol;
        data.addInvocation(data.currentParentFunction, symbol);
        visitNode(node.expr, data);
        visitNode(node.argExprs, data);
    }

    @Override
    public void visit(BLangTypeInit typeInit, AnalyzerData data) {
//        if (typeInit.getKind() == NodeKind.TYPE_INIT_EXPR) {
//            BClassSymbol classSymbol = (BClassSymbol) ((BLangUserDefinedType) typeInit.getType()).symbol;
////            BClassSymbol classSymbol = (BClassSymbol) typeInit.type.tsymbol;
//            classSymbol.usedState = UsedState.USED;
//            data.addInvocation(data.currentParentFunction, classSymbol.generatedInitializerFunc.symbol);
//        }

        super.visit(typeInit, data);
    }

//    @Override
//    public void visit(BLangClassDefinition classDefNode, AnalyzerData data) {
//        classDefNode.
//    }

    private void registerTopLevelFunction(TopLevelNode topLevelNode) {
        BLangFunction topLevelFunction = (BLangFunction) topLevelNode;
        if (isLambda(topLevelFunction)) {
            addLambdaFunctionToMap(topLevelFunction);
        } else {
            addFunctionToMap(topLevelFunction);
        }
    }

    private void registerClassLevelFunctions(TopLevelNode classDef) {
        BLangClassDefinition classDefNode = (BLangClassDefinition) classDef;
        for (BLangFunction classLevelFunction : classDefNode.functions) {
            addFunctionToMap(classLevelFunction);
        }
        if (classDefNode.initFunction != null) {
            addFunctionToMap(classDefNode.initFunction);
        }
    }

    private boolean isLambda(BLangFunction function) {
        return function.symbol.getFlags().contains(Flag.LAMBDA);
//               && function.parent.parent.getKind() == NodeKind.VARIABLE;
        // TODO check whether we need the above condition
    }

    private void addLambdaFunctionToMap(BLangFunction topLevelFunction) {
        BInvokableSymbol functionPointer =
                (BInvokableSymbol) ((BLangSimpleVariable) ((topLevelFunction).parent).parent).symbol;
        BInvokableSymbol invokableSymbol = topLevelFunction.originalFuncSymbol;

        // Add the Lambda functions and their var pointers to a HashMap for future use
        data.lambdaPointers.put(functionPointer, invokableSymbol);
        invokableSymbol.usedState = UsedState.UNUSED;
        data.getPackageSymbol(invokableSymbol).deadFunctions.add(invokableSymbol);
        // Only add the function pointer to the map for readability
        data.addParentFunctionToPkgSymbol(functionPointer);
    }

    private void addFunctionToMap(BLangFunction function) {
        BInvokableSymbol invokableSymbol = function.originalFuncSymbol;

        data.addParentFunctionToPkgSymbol(invokableSymbol);

        // "main" and "init" functions are the roots of USED function chains
        if (invokableSymbol.name.value.equals(MAIN_FUNCTION_NAME) ||
                invokableSymbol.name.value.equals(INIT_FUNCTION_NAME)) {
            data.markAsUsed(invokableSymbol);
        }
        // Resource functions should be USED by default
        if (function.flagSet.contains(Flag.RESOURCE)) {
            data.markAsUsed(invokableSymbol);
        }
    }

    /**
     * keeps track of function invocations using a HashMap. determines used functions and dead functions. keeps track of
     * lambda pointer variables and their functions.
     */
    protected static class AnalyzerData {

        // The key of the following HashMap is function pointer variable symbol
        // The value is lambda function symbol
        private final HashMap<BInvokableSymbol, BInvokableSymbol> lambdaPointers;
        // This attribute is used to determine the parent of the current invocation. We need it for lambda functions
        BInvokableSymbol currentParentFunction;

        private AnalyzerData() {
            this.lambdaPointers = new HashMap<>();
        }

        private void addParentFunctionToPkgSymbol(BInvokableSymbol parentFunctionSymbol) {
            if (parentFunctionSymbol.usedState == UsedState.UNEXPOLORED) {
                parentFunctionSymbol.usedState = UsedState.UNUSED;
            }
            getPackageSymbol(parentFunctionSymbol).invocationMap.putIfAbsent(parentFunctionSymbol, new HashSet<>());
            getPackageSymbol(parentFunctionSymbol).deadFunctions.add(parentFunctionSymbol);
        }

        private void addInvocation(BInvokableSymbol startFunctionSymbol, BInvokableSymbol endFunctionSymbol) {
            startFunctionSymbol.childrenFunctions.add(endFunctionSymbol);
            if (endFunctionSymbol.usedState == UsedState.UNEXPOLORED){
                endFunctionSymbol.usedState = UsedState.UNUSED;   // TODO do we need this?
            }
            updatePkgSymbolInvocationMap(startFunctionSymbol, endFunctionSymbol);

            // Update the children chains if the parent is "USED"
            if (startFunctionSymbol.isUsed()) {
                markSelfAndChildrenAsUsed(endFunctionSymbol);
            }
        }

        private void markSelfAndChildrenAsUsed(BInvokableSymbol parentSymbol) {
            markAsUsed(parentSymbol);

            // updating the origin lambda function if the parentSymbol is a function pointer
            if (isLambdaFunctionPointer(parentSymbol)) {
                markAsUsed(lambdaPointers.get(parentSymbol));
            }

            // Recursively mark all the children as used
            for (BInvokableSymbol child : parentSymbol.childrenFunctions) {
                if (child.usedState == UsedState.UNUSED) {
                    child.usedState = UsedState.USED;
                    markSelfAndChildrenAsUsed(child);
                }
            }
        }

        private void markAsUsed(BInvokableSymbol symbol) {
            symbol.usedState = UsedState.USED;
            getPackageSymbol(symbol).deadFunctions.remove(symbol);
            getPackageSymbol(symbol).usedFunctions.add(symbol);
        }

        private void updatePkgSymbolInvocationMap(BInvokableSymbol startSymbol, BInvokableSymbol endSymbol) {
            getPackageSymbol(startSymbol).invocationMap.putIfAbsent(startSymbol, new HashSet<>());
            getPackageSymbol(startSymbol).invocationMap.get(startSymbol).add(endSymbol);
        }

        private boolean isLambdaFunctionPointer(BInvokableSymbol symbol) {
            return lambdaPointers.containsKey(symbol);
        }

        private BPackageSymbol getPackageSymbol(BSymbol symbol) {
            if (symbol.owner.getKind().equals(SymbolKind.PACKAGE)) {    //Module level functions
                return (BPackageSymbol) symbol.owner;
            } else {
                return getPackageSymbol(symbol.owner);
            }
        }
    }
}
