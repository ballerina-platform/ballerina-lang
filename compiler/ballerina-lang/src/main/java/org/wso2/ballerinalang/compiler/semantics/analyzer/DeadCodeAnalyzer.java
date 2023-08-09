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
import static org.ballerinalang.model.tree.NodeKind.LAMBDA;
import static org.ballerinalang.model.tree.NodeKind.INVOCATION;

public class DeadCodeAnalyzer extends SimpleBLangNodeAnalyzer<DeadCodeAnalyzer.AnalyzerData> {

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


    /**
     * 3rd method that gets called
     * Iterates over the nodes of BLangPackage using the visit functions
     *
     * @param pkgNode Syntax Tree holding the nodes
     * @param data    Data used for identifying Dead Code
     */
    private void analyzeTopLevelNodes(BLangPackage pkgNode, DeadCodeAnalyzer.AnalyzerData data) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;

        // Registering all the top level functions inside the class
        for (TopLevelNode topLevelNode : topLevelNodes) {
            if (topLevelNode.getKind() == FUNCTION) {
                BLangFunction topLevelFunction = (BLangFunction) topLevelNode;
                // Add the Lambda functions to a HashMap for future use
                if (topLevelFunction.symbol.origin.name().equals("VIRTUAL")) {
                    BInvokableSymbol functionPointer = (BInvokableSymbol) ((BLangSimpleVariable) ((topLevelFunction).parent).parent).symbol;
                    BInvokableSymbol function = topLevelFunction.symbol;
                    data.invocationGraph.declareFunction(functionPointer);
                    data.invocationGraph.lambdaPointers.put(functionPointer, function);
                } else {
                    data.invocationGraph.declareFunction(topLevelFunction.symbol);
                    // Setting the main and init functions as the starting points of used function chains
                    if (topLevelFunction.symbol.originalName.value.equals("main") || topLevelFunction.symbol.originalName.value.equals("init")) {
                        data.invocationGraph.usedFunctions.add(topLevelFunction.symbol);
                    }
                }
            }
        }

        // 2nd Iteration goes inside each topLevelNode to find Invocations
        for (TopLevelNode topLevelNode : topLevelNodes) {
//            if (!(((BLangFunction) topLevelNodes.get(i)).parent.getKind() == LAMBDA)) {
//            }
            visitNode((BLangNode) topLevelNode, data);
        }

        data.invocationGraph.findDeadFunctions();
        data.invocationGraph.printInvocationGraph();

        pkgNode.completedPhases.add(CompilerPhase.DEAD_CODE_ANALYZE);
    }


    // Commented the body to stop it from causing infinite loops
    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
//        SymbolEnv prevEnv = data.env;
//        BLangNode parent = data.parent;
//        node.parent = parent;
//        data.parent = node;
//        visitNode(node, data); // Overridden BLangFunction gets called from here
//        data.parent = parent;
//        data.env = prevEnv;
    }


    @Override
    public void visit(BLangFunction function, AnalyzerData data) {
        BInvokableSymbol parentFunction = function.symbol;
        if (function.symbol.origin.name().equals("VIRTUAL")) {
            parentFunction =(BInvokableSymbol) ((BLangSimpleVariable) ((function).parent).parent).symbol;
        }

        BLangBlockFunctionBody functionBody = (BLangBlockFunctionBody) function.body;
        for (BLangStatement stmt : functionBody.stmts) {
        data.currentParentFunction = parentFunction;
            visitNode(stmt, data);
        }
    }

//    @Override
//    public void visit(BLangLambdaFunction node, AnalyzerData data) {
//        data.invocationGraph.declareFunction(node.function.symbol);
//        BInvokableSymbol parent = data.currentParentFunction; // Saving the current parent in case of nested lambda functions
//        visitNode(node.function, data);
//        data.currentParentFunction = parent; // Restoring the current parent function
//    }

    @Override
    public void visit(BLangArrowFunction node, AnalyzerData data) {
        data.invocationGraph.declareFunction((BInvokableSymbol) ((BLangSimpleVariable) node.parent).symbol);

        visitNode(node.params, data);
        visitNode((BLangNode) node.functionName, data);
        visitNode(node.body, data);
    }

    @Override
    public void visit(BLangInvocation iExpression, AnalyzerData data) {
        data.invoCount++;
        BInvokableSymbol temp = (BInvokableSymbol) iExpression.symbol;
//        if (data.currentParentFunction.origin.name().equals("VIRTUAL")) {
////            data.invocationGraph.addFunctionInvocation(data.lambdaPointers.get(), temp);
//        } else {
//
//        data.invocationGraph.addFunctionInvocation(data.currentParentFunction, temp);
//        }

        data.invocationGraph.addFunctionInvocation(data.currentParentFunction, temp);

        visitNode(iExpression.expr, data);
        visitNode(iExpression.argExprs, data);
    }

    @Override
    public void visit(BLangSimpleVarRef node, AnalyzerData data) {
        analyzeNode(node, data);
        visitNode(node.pkgAlias, data);
        visitNode(node.variableName, data);
        if (node.symbol.getKind() == SymbolKind.FUNCTION) {
            data.invocationGraph.addFunctionInvocation(data.currentParentFunction,((BInvokableSymbol) node.symbol));
        }
    }

//    @Override
//    public void visit(BLangSimpleVariableDef varNode, AnalyzerData data) {
//        analyzeNode(varNode.var.expr,data);
//    }
//
//    @Override
//    public void visit(BLangSimpleVariable varNode, AnalyzerData data) {
//        analyzeNode(varNode.expr,data);
//    }
//
//     @Override
//    public void visit(BLangLiteral varNode, AnalyzerData data) {
//        // TODO Should we keep it empty?
//    }
//
//    @Override
//    public void visit(BLangIf stmt,AnalyzerData data) {
//        analyzeNode(stmt.expr,data);
//        analyzeNode(stmt.body,data);
//        analyzeNode(stmt.elseStmt,data);
//    }
//
//    @Override
//    public void visit(BLangDo stmt, AnalyzerData data) {
//        analyzeNode(stmt.body,data);
//    }
//
//    @Override
//    public void visit(BLangWhile stmt, AnalyzerData data) {
//        analyzeNode(stmt.body,data);
//        analyzeNode(stmt.expr,data);
//    }
//
//    @Override
//    public void visit(BLangForeach stmt, AnalyzerData data) {
//        analyzeNode(stmt.body,data);
//        // TODO add function check for expr
//    }
//
//    @Override
//    public void visit(BLangBlockStmt blocStmt, AnalyzerData data) {
//        List<BLangStatement> blockStmts = blocStmt.stmts;
//        for (BLangStatement stmt: blockStmts) {
//            analyzeNode(stmt,data);
//        }
//    }
//
//    @Override
//    public void visit(BLangExpressionStmt stmt, AnalyzerData data) {
//        analyzeNode(stmt.expr,data);
//    }
//
//    @Override
//    public void visit(BLangCheckedExpr stmt, AnalyzerData data) {
//        analyzeNode(stmt.expr,data);
//    }
//
//    @Override
//    public void visit(BLangAssignment stmt, AnalyzerData data) {
//        analyzeNode(stmt.expr,data);
//    }
//
//    @Override
//    public void visit(BLangReturn stmt, AnalyzerData data) {
//        analyzeNode(stmt.expr,data);
//    }
//    @Override
//    public void visit(BLangBinaryExpr expr, AnalyzerData data) {
//        // TODO If the right side is a method call, analyse rhsExpr
//        if (expr.lhsExpr.getKind() == INVOCATION) {
//            analyzeNode(expr.lhsExpr,data);
//        }
//        if (expr.rhsExpr.getKind() == INVOCATION) {
//            analyzeNode(expr.rhsExpr,data);
//        }
//    }
//
//    @Override
//    public void visit(BLangErrorConstructorExpr expr, AnalyzerData data) {
//        // TODO Nothing??
//    }
//
//    @Override
//    public void visit(BLangGroupExpr expr, AnalyzerData data) {
//        analyzeNode(expr.expression,data);
//    }

//    @Override
//    public void visit(BLangUnaryExpr expr, AnalyzerData data) {
//        analyzeNode(expr.expr,data);
//    }
//    @Override
//    public void visit(BLangTypeTestExpr expr, AnalyzerData data) {
//        analyzeNode(expr.expr,data);
//    }
//    @Override
//    public void visit(BLangSimpleVarRef expr, AnalyzerData data) {
////        analyzeNode(expr.expr,data);
//    }

    public static class AnalyzerData {
        //        SymbolEnv env;
//        BLangNode parent;
        int invoCount;
        BInvokableSymbol currentParentFunction;
        InvocationGraph invocationGraph = new InvocationGraph();
    }

    public static class InvocationGraph {
        HashMap<BInvokableSymbol, HashSet<BInvokableSymbol>> invocationGraph;
        HashSet<BInvokableSymbol> usedFunctions;
        HashSet<BInvokableSymbol> deadFunctions;
        HashMap<BInvokableSymbol, BInvokableSymbol> lambdaPointers = new HashMap<>();


        public InvocationGraph() {
            this.invocationGraph = new HashMap<>();
            this.usedFunctions = new HashSet<>();
            this.deadFunctions = new HashSet<>();
        }

        public void declareFunction(BInvokableSymbol functionSymbol) {
            if (!invocationGraph.containsKey(functionSymbol)) {
                invocationGraph.put(functionSymbol, new HashSet<>());
            }
        }

        // Only registers invocations of already known functions
        public void addFunctionInvocation(BInvokableSymbol startFunctionSymbol, BInvokableSymbol endFunctionSymbol) {
//            if (invocationGraph.containsKey(endFunctionSymbol)) {
//                invocationGraph.get(startFunctionSymbol).add(endFunctionSymbol);
//
//                // Determining used functions (Not Dead functions)
//                if (usedFunctions.contains(startFunctionSymbol)) {
//                    usedFunctions.add(endFunctionSymbol);
//                }
//            }
//            declareFunction(endFunctionSymbol);
            invocationGraph.get(startFunctionSymbol).add(endFunctionSymbol);
            if (usedFunctions.contains(startFunctionSymbol)) {
                usedFunctions.add(endFunctionSymbol);
                declareChildrenUsable(endFunctionSymbol);      // In case parent function gets checked after its child functions
            }
        }

        public void declareChildrenUsable(BInvokableSymbol parentFunction) {
            if (invocationGraph.containsKey(parentFunction)) {
                for (BInvokableSymbol childFunction : invocationGraph.get(parentFunction)) {
                    declareChildrenUsable(childFunction);
                }
            }
            usedFunctions.add(parentFunction);
        }

        public void findDeadFunctions() {
            for (BInvokableSymbol function : invocationGraph.keySet()) {
                if (!usedFunctions.contains(function)) {
                    deadFunctions.add(function);

                    // Adding the lambda function along with the function pointer variable
                    if (lambdaPointers.containsKey(function)) {
                        deadFunctions.add(lambdaPointers.get(function));
                    }
                }
            }
        }

        public void printInvocationGraph() {

            System.out.println("---------- Invocation Graph ----------");
            System.out.println(invocationGraph);

            System.out.println("Used Functions : " + usedFunctions);
            System.out.println("Dead Functions : " + deadFunctions);

        }
    }
}
