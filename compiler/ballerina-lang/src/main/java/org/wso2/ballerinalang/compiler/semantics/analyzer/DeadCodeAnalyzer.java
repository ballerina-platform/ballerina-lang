package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.expressions.*;
import org.wso2.ballerinalang.compiler.tree.statements.*;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.model.tree.NodeKind.FUNCTION;

public class DeadCodeAnalyzer extends SimpleBLangNodeAnalyzer<DeadCodeAnalyzer.AnalyzerData> {


    // --------------------------------------------------------- SAME AS CODEANALYZER --------------------------------------------------------- //
    private static final CompilerContext.Key<DeadCodeAnalyzer> DEAD_CODE_ANALYZER_KEY = new CompilerContext.Key<>();

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

    // 1st Method that gets called
    public BLangPackage analyze(BLangPackage pkgNode) {
        final DeadCodeAnalyzer.AnalyzerData data = new DeadCodeAnalyzer.AnalyzerData();
        visitNode(pkgNode, data);
        return pkgNode;
    }

    // 2nd Method that gets called
    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DEAD_CODE_ANALYZE)) {
            return;
        }
        analyzeTopLevelNodes(pkgNode, data);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visitNode(testablePackage, data));
    }

    // --------------------------------------------------------- MODIFICATIONS AND ADDITIONS --------------------------------------------------------- //

    /**
     * Iterates over the nodes of BLangPackage and populates the invocationMap
     * Populates the Lambda function HashMap
     * @param pkgNode Syntax Tree holding the nodes
     * @param data    Data used for identifying Dead Code
     */
    private void analyzeTopLevelNodes(BLangPackage pkgNode, DeadCodeAnalyzer.AnalyzerData data) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;

        // Registering all the top level functions inside the class
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == FUNCTION) {  // Filtering only the functions
                BLangFunction topLevelFunction = (BLangFunction) topLevelNode;
                if (isLambda(topLevelFunction)) {  // Checks whether the function is a lambda one
                    BInvokableSymbol functionPointer = (BInvokableSymbol) ((BLangSimpleVariable) ((topLevelFunction).parent).parent).symbol;
                    BInvokableSymbol function = topLevelFunction.symbol;
                    data.invocationData.declareFunction(functionPointer);
                    data.invocationData.lambdaPointers.put(functionPointer, function); // Add the Lambda functions and their var pointers to a HashMap for future use
                } else {                                                               // if the function is not a lambda one, it should be a class method
                    data.invocationData.declareFunction(topLevelFunction.symbol);
                    if (topLevelFunction.symbol.originalName.value.equals("main") || topLevelFunction.symbol.originalName.value.equals("init")) {
                        data.invocationData.usedFunctions.add(topLevelFunction.symbol);      // Setting the main and init functions as the starting points of used function chains
                    }
                }
            }
        }

        // Traverse through each node in TopLevelFunctions
        for (TopLevelNode topLevelNode : topLevelNodes) {
            visitNode((BLangNode) topLevelNode, data);
        }

        data.invocationData.findDeadFunctions();
        data.invocationData.printInvocationMap();

        pkgNode.completedPhases.add(CompilerPhase.DEAD_CODE_ANALYZE);
    }


    // Commented the body to stop it from causing infinite loops
    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
//        SymbolEnv prevEnv = data.env;
//        BLangNode parent = data.parent;
//        node.parent = parent;
//        data.parent = node;
//        visitNode(node, data);
//        data.parent = parent;
//        data.env = prevEnv;
    }


    // keeps track of the immediate parent function of each stmt using parentFunction variable.
    // parentFunction variable is used to determine the invocation location of the functions.
    // Inside nested Lambda functions this is useful.
    @Override
    public void visit(BLangFunction function, AnalyzerData data) {
        BInvokableSymbol parentFunction = function.symbol;
        if (isLambda(function)) {       // if the function is a lambda one, method of getting parent is different
            parentFunction =(BInvokableSymbol) ((BLangSimpleVariable) ((function).parent).parent).symbol;
        }

        BLangBlockFunctionBody functionBody = (BLangBlockFunctionBody) function.body;
        for (BLangStatement stmt : functionBody.stmts) {
        data.currentParentFunction = parentFunction;                // Reassign the currentParentFunction in case it got changed inside a visit call
            visitNode(stmt, data);
        }
    }


    // Arrow functions are also added to the invocationMap.
    // Have to optimise this because arrow functions cant have children(other invocations)
    @Override
    public void visit(BLangArrowFunction node, AnalyzerData data) {
        data.invocationData.declareFunction((BInvokableSymbol) ((BLangSimpleVariable) node.parent).symbol);
        visitNode(node.params, data);
        visitNode((BLangNode) node.functionName, data);
        visitNode(node.body, data);
    }

    // Adds the invocation to the invocationMap
    @Override
    public void visit(BLangInvocation iExpression, AnalyzerData data) {
        BInvokableSymbol symbol = (BInvokableSymbol) iExpression.symbol;
        data.invocationData.addFunctionInvocation(data.currentParentFunction, symbol);
        visitNode(iExpression.expr, data);
        visitNode(iExpression.argExprs, data);
    }

    // Used to track the invocations of Lambda function pointers.
    @Override
    public void visit(BLangSimpleVarRef node, AnalyzerData data) {
        analyzeNode(node, data);
        visitNode(node.pkgAlias, data);
        visitNode(node.variableName, data);
        if (node.symbol.getKind() == SymbolKind.FUNCTION) {
            data.invocationData.addFunctionInvocation(data.currentParentFunction,((BInvokableSymbol) node.symbol));
        }
    }

    /**
     * Checks whether a given function is a lambda function
     * TODO implement a better way to determine it
     * @param function given function
     * @return true if function is lambda, false otherwise
     */
    private boolean isLambda(BLangFunction function) {
        return function.symbol.origin.name().equals("VIRTUAL");
    }


    public static class AnalyzerData {
        BInvokableSymbol currentParentFunction;     // used to determine the parent of the invocation
        InvocationData invocationData = new InvocationData();
    }


    /**
     * keeps track of function invocations using a HashMap.
     * determines used functions and dead functions.
     * keeps track of lambda pointer variables and their functions.
     */
    public static class InvocationData {
        private HashMap<BInvokableSymbol, HashSet<BInvokableSymbol>> invocationMap;     // Key = Start Function // Value = End Function
        private HashSet<BInvokableSymbol> usedFunctions;
        private HashSet<BInvokableSymbol> deadFunctions;
        private HashMap<BInvokableSymbol, BInvokableSymbol> lambdaPointers = new HashMap<>();   // Key = Function pointer variable // Value = Lambda Function


        public InvocationData() {
            this.invocationMap = new HashMap<>();
            this.usedFunctions = new HashSet<>();
            this.deadFunctions = new HashSet<>();
        }

        /**
         * if the function is not already declared, it is added as a new key to the invocationMap hashMap
         * @param functionSymbol Function to be declared
         */
        public void declareFunction(BInvokableSymbol functionSymbol) {
            if (!invocationMap.containsKey(functionSymbol)) {
                invocationMap.put(functionSymbol, new HashSet<>());
            }
        }


        /**
         * Updates the invocationMap values with new invocations.
         * If the parent function is already a used function, child is also added to the "usedFunctions" HashSet
         *
         * @param startFunctionSymbol Parent function calling the invocation
         * @param endFunctionSymbol Child function called by the invocation
         */
        public void addFunctionInvocation(BInvokableSymbol startFunctionSymbol, BInvokableSymbol endFunctionSymbol) {
            invocationMap.get(startFunctionSymbol).add(endFunctionSymbol);
            if (usedFunctions.contains(startFunctionSymbol)) {
                usedFunctions.add(endFunctionSymbol);
                declareChildrenUsable(endFunctionSymbol);      // In case parent function gets checked after its child functions
            }
        }


        /**
         * Declares all the nested invocations recursively of the parent function as used
         * @param parentFunction Parent function that is already used
         */
        private void declareChildrenUsable(BInvokableSymbol parentFunction) {
            if (invocationMap.containsKey(parentFunction)) {
                for (BInvokableSymbol childFunction : invocationMap.get(parentFunction)) {
                    declareChildrenUsable(childFunction);
                }
            }
            usedFunctions.add(parentFunction);
        }

        /**
         * If an initialized function is not inside the usedFunctions HashSet, it should be a dead function.
         * This method declares both function pointer variables and actual lambda functions if the pointer variable isn't invoked.
         */
        public void findDeadFunctions() {
            for (BInvokableSymbol function : invocationMap.keySet()) {
                if (!usedFunctions.contains(function)) {
                    deadFunctions.add(function);
                    // Adding the lambda function along with the function pointer variable
                    if (lambdaPointers.containsKey(function)) {
                        deadFunctions.add(lambdaPointers.get(function));
                    }
                }
            }
        }

        /**
         * Prints the invocation map along with used functions and dead functions
         */
        public void printInvocationMap() {

            System.out.println("---------- Invocation Graph ----------");
            System.out.println(invocationMap);

            System.out.println("Used Functions : " + usedFunctions);
            System.out.println("Dead Functions : " + deadFunctions);

        }
    }
}
