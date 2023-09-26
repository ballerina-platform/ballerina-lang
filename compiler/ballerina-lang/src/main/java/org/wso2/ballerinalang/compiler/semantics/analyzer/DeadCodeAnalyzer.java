/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 *  KIND, either express or implied.  See the License for the
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.model.tree.NodeKind.CLASS_DEFN;
import static org.ballerinalang.model.tree.NodeKind.FUNCTION;

/**
 * Analyze function invocation chains and detect unused functions
 *
 */
public class DeadCodeAnalyzer extends SimpleBLangNodeAnalyzer<DeadCodeAnalyzer.AnalyzerData> {


    private static final CompilerContext.Key<DeadCodeAnalyzer> DEAD_CODE_ANALYZER_KEY = new CompilerContext.Key<>();
    final DeadCodeAnalyzer.AnalyzerData data = new DeadCodeAnalyzer.AnalyzerData();

    public static DeadCodeAnalyzer getInstance(CompilerContext context) {
        DeadCodeAnalyzer deadCodeAnalyzer = context.get(DEAD_CODE_ANALYZER_KEY);
        if (deadCodeAnalyzer == null) {
            deadCodeAnalyzer = new DeadCodeAnalyzer(context);
        }
        return deadCodeAnalyzer;
    }

    private DeadCodeAnalyzer(CompilerContext context) {
        context.put(DEAD_CODE_ANALYZER_KEY, this);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        visitNode(pkgNode, data);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DEAD_CODE_ANALYZE)) {
            return;
        }
        analyzeTopLevelNodes(pkgNode, data);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visitNode(testablePackage, data));
    }


    /**
     * Iterates over the nodes of BLangPackage and populates the invocationMap
     *
     * @param pkgNode Syntax Tree holding the nodes
     * @param data    Data used for identifying Dead Code
     */
    private void analyzeTopLevelNodes(BLangPackage pkgNode, DeadCodeAnalyzer.AnalyzerData data) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;

        // Registering all the top level functions inside the module
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == FUNCTION) {  // Filtering only the functions
                BLangFunction topLevelFunction = (BLangFunction) topLevelNode;
                if (isLambda(topLevelFunction)) {  // Checks whether the function is a lambda one
                    addLambdaFunctionToMap(topLevelFunction);
                } else {    // if the function is not a lambda one, it should be a normal method
                    addFunctionToMap(topLevelFunction);
                }
            }
            if (topLevelNode.getKind() == CLASS_DEFN) {
                for (BLangFunction classLevelFunction : ((BLangClassDefinition) topLevelNode).functions) {
                    addFunctionToMap(classLevelFunction);
                }
                if (((BLangClassDefinition) topLevelNode).initFunction != null)
                    addFunctionToMap(((BLangClassDefinition) topLevelNode).initFunction);
            }
        }

        // Traverse through each node in TopLevelFunctions
        for (TopLevelNode topLevelNode : topLevelNodes) {
            visitNode((BLangNode) topLevelNode, data);
        }

        pkgNode.completedPhases.add(CompilerPhase.DEAD_CODE_ANALYZE);
    }


    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        // Empty body to stop it from causing infinite loops
    }


    // keeps track of the immediate parent function of each stmt using parentFunction variable.
    // parentFunction variable is used to determine the invocation location of the functions.
    // Inside nested Lambda functions this is useful.
    @Override
    public void visit(BLangFunction function, AnalyzerData data) {
        // if the function is a lambda one, method of getting parent is different
        BInvokableSymbol parentFunctionSymbol = isLambda(function) ? (BInvokableSymbol) ((BLangSimpleVariable) ((function).parent).parent).symbol : function.symbol;

        // body is null for abstract functions
        if (!function.getFlags().contains(Flag.INTERFACE)) {
            return;
        }

        if (function.body.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {        // handling remote functions
            for (BLangStatement stmt : ((BLangBlockFunctionBody) function.getBody()).getStatements()) {
                data.currentParentFunction = parentFunctionSymbol;      // Reassign the currentParentFunction in case it got changed inside a visit call (Lambda functions might change it)
                visitNode(stmt, data);
            }
        }

    }

    public void visit(BLangArrowFunction node, AnalyzerData data) {
        if (!(node.parent.getKind() == NodeKind.SIMPLE_VARIABLE_REF)) {
            return;
        } else if (!(node.parent.getBType().getKind() == TypeKind.TYPEREFDESC)) {
            return;
        }
        data.addParentFunctionToPkgSymbol((BInvokableSymbol) ((BLangSimpleVariable) node.parent).symbol);  // TODO Find a cleaner way

        visitNode(node.params, data);
        visitNode((BLangNode) node.functionName, data);
        visitNode(node.body, data);
    }

    // Registers an invocation
    // Updates the usedStates, usedFunctions, deadFunctions and invocationMaps accordingly
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

    // Used to track the invocations of Lambda and arrow function pointers
    @Override
    public void visit(BLangSimpleVarRef node, AnalyzerData data) {
        if (node.symbol != null) {
            return;
        } else if (node.symbol.getKind() != SymbolKind.FUNCTION) {
            return;
        }

        data.addInvocation(data.currentParentFunction, ((BInvokableSymbol) node.symbol));
    }

    /**
     * Checks whether a given function is a lambda function
     * TODO implement a better way to determine it
     *
     * @param function given function
     * @return true if function is lambda, false otherwise
     */
    private boolean isLambda(BLangFunction function) {
        return function.symbol.getFlags().contains(Flag.LAMBDA) && function.parent.parent.getKind() == NodeKind.VARIABLE;
    }

    private void addLambdaFunctionToMap(BLangFunction topLevelFunction) {
        BInvokableSymbol functionPointer = (BInvokableSymbol) ((BLangSimpleVariable) ((topLevelFunction).parent).parent).symbol;
        BInvokableSymbol function = topLevelFunction.symbol;

        // Add the Lambda functions and their var pointers to a HashMap for future use
        data.lambdaPointers.put(functionPointer, function);
        // initializing the lambda function as dead
        function.usedState = UsedState.UNUSED;
        data.getPackageSymbol(function).deadFunctions.add(function);
        // Only add the function pointer to the map for readability
        data.addParentFunctionToPkgSymbol(functionPointer);
    }

    private void addFunctionToMap(BLangFunction topLevelFunction) {
        data.addParentFunctionToPkgSymbol(topLevelFunction.symbol);
        if (topLevelFunction.symbol.originalName.value.equals("main") || topLevelFunction.symbol.originalName.value.equals("init")) {
            data.addToUsedListInPkgSym(topLevelFunction.symbol);        // Setting the main and init functions as the starting points of used function chains
            topLevelFunction.symbol.usedState = UsedState.USED;
        }
    }


    /**
     * keeps track of function invocations using a HashMap.
     * determines used functions and dead functions.
     * keeps track of lambda pointer variables and their functions.
     */
    protected static class AnalyzerData {
        BInvokableSymbol currentParentFunction;     // used to determine the parent of the current invocation
        private HashMap<BInvokableSymbol, BInvokableSymbol> lambdaPointers;   // Key = Function pointer variable // Value = Lambda Function


        private AnalyzerData() {
            this.lambdaPointers = new HashMap<>();
        }

        /**
         * Initializes a function as a parent function
         * Replaces "UNEXPLORED" used state as "UNUSED"
         *
         * @param parentFunctionSymbol Symbol of the parent function
         */
        private void addParentFunctionToPkgSymbol(BInvokableSymbol parentFunctionSymbol) {
            if (parentFunctionSymbol.usedState == UsedState.UNEXPOLORED) parentFunctionSymbol.usedState = UsedState.UNUSED;
            getPackageSymbol(parentFunctionSymbol).invocationMap.putIfAbsent(parentFunctionSymbol, new HashSet<>());    // Adding to the pkgSymbol
            getPackageSymbol(parentFunctionSymbol).deadFunctions.add(parentFunctionSymbol);     // Pre declare as dead
        }

        /**
         * Updates the invocationMap of the relevant pkgSymbol
         * if the startFunction is already a "USED" function, declare children "USED" as well
         *
         * @param startFunctionSymbol Symbol of the parent function
         * @param endFunctionSymbol   Symbol of the invoked (callee) function
         */
        private void addInvocation(BInvokableSymbol startFunctionSymbol, BInvokableSymbol endFunctionSymbol) {
            startFunctionSymbol.childrenFunctions.add(endFunctionSymbol);     // Adding children to the start(parent) BInvokableSymbol

            if (endFunctionSymbol.usedState == UsedState.UNEXPOLORED) endFunctionSymbol.usedState = UsedState.UNUSED;       // Update the initial state
            updatePkgSymbolInvocationMap(startFunctionSymbol, endFunctionSymbol);

            // Update the children chains if the parent is "USED"
            if (startFunctionSymbol.isUsed()) {
                markSelfAndChildrenAsUsed(endFunctionSymbol);
            }
        }

        /**
         * Updates the given symbol and its descendants as "USED"
         */
        private void markSelfAndChildrenAsUsed(BInvokableSymbol parentSymbol) {
            parentSymbol.usedState = UsedState.USED;
            // Populating the "used" graph for PackageSymbol
            addToUsedListInPkgSym(parentSymbol);
            if (isLambdaFunctionPointer(parentSymbol)) {    // updating the origin lambda function if the parentSymbol is a function pointer
                addToUsedListInPkgSym(lambdaPointers.get(parentSymbol));
            }

            for (BInvokableSymbol child : parentSymbol.childrenFunctions) {
                if (child.usedState == UsedState.UNUSED) {         // If a given child is used, its descendants should also be used
                    child.usedState = UsedState.USED;
                    markSelfAndChildrenAsUsed(child);
                }
            }
        }

        /**
         * Removes the given function from deadFunctions and add to usedFunctions
         */
        private void addToUsedListInPkgSym(BInvokableSymbol symbol) {
            getPackageSymbol(symbol).deadFunctions.remove(symbol);
            getPackageSymbol(symbol).usedFunctions.add(symbol);
        }

        /**
         * Updates the invocationMap of respective pkgSymbol
         */
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
