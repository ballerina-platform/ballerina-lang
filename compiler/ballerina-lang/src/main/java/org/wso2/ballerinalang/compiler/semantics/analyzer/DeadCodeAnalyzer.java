package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.*;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.model.tree.NodeKind.FUNCTION;

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
        System.out.println("visit BLangPackage method running");
        if (pkgNode.completedPhases.contains(CompilerPhase.DEAD_CODE_ANALYZE)) {
            return;
        }
        analyzeTopLevelNodes(pkgNode, data);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visitNode(testablePackage, data));
    }


    /**
     * 3rd method that gets called
     * Iterates over the nodes of BLangPackage using the visit functions
     * @param pkgNode Syntax Tree holding the nodes
     * @param data Data used for identifying Dead Code
     */
    private void analyzeTopLevelNodes(BLangPackage pkgNode, DeadCodeAnalyzer.AnalyzerData data) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;

        // Registering all the top level functions inside the class
        for (TopLevelNode topLevelNode:topLevelNodes) {
            if (topLevelNode.getKind() == FUNCTION) {
                BLangFunction topLevelFunction = (BLangFunction) topLevelNode;
                data.invocationGraph.declareFunction(topLevelFunction.symbol);
            }
        }

        // 2nd Iteration goes inside each topLevelNode to find Invocations
        for (int i = 0; i < topLevelNodes.size(); i++) {
            analyzeNode((BLangNode) topLevelNodes.get(i), data);
        }

        data.invocationGraph.printInvocationGraph();

        pkgNode.completedPhases.add(CompilerPhase.DEAD_CODE_ANALYZE);
    }

    // 4th method that gets called
    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        SymbolEnv prevEnv = data.env;
        BLangNode parent = data.parent;
        node.parent = parent;
        data.parent = node;
        visitNode(node, data); // Overridden BLangFunction gets called from here
        data.parent = parent;
        data.env = prevEnv;
    }

    //5th method that gets called
    @Override
    public void visit(BLangFunction function, AnalyzerData data) {
        data.functionCount++;
        data.currentParentFunction = function.symbol;
        System.out.println("BLangFunction got called....");
        BLangBlockFunctionBody functionBody = (BLangBlockFunctionBody) function.body;
        List<BLangStatement> functionBodyStmts = functionBody.stmts;
        for (BLangStatement stmt:functionBodyStmts) {
            analyzeNode(stmt,data);
        }
    }

    @Override
    public void visit(BLangInvocation iExpression, AnalyzerData data) {
        data.invoCount++;
        BInvokableSymbol temp = (BInvokableSymbol) iExpression.symbol;
        data.invocationGraph.addFunctionInvocation(data.currentParentFunction,temp);
        System.out.println("Invocation found");
    }

    @Override
    public void visit(BLangSimpleVariableDef varNode, AnalyzerData data) {
        // TODO if a variable calls a function...
    }

    @Override
    public void visit(BLangIf stmt,AnalyzerData data) {
        analyzeNode(stmt.body,data);
    }

    @Override
    public void visit(BLangDo stmt, AnalyzerData data) {
        analyzeNode(stmt.body,data);
    }

    @Override
    public void visit(BLangWhile stmt, AnalyzerData data) {
        analyzeNode(stmt.body,data);
    }

    @Override
    public void visit(BLangForeach stmt, AnalyzerData data) {
        analyzeNode(stmt.body,data);
    }

    @Override
    public void visit(BLangBlockStmt blocStmt, AnalyzerData data) {
        List<BLangStatement> blockStmts = blocStmt.stmts;
        for (BLangStatement stmt: blockStmts) {
            analyzeNode(stmt,data);
        }
    }

    @Override
    public void visit(BLangExpressionStmt stmt, AnalyzerData data) {
        analyzeNode(stmt.expr,data);
    }

    @Override
    public void visit(BLangCheckedExpr stmt, AnalyzerData data) {
        analyzeNode(stmt.expr,data);
    }


    public static class AnalyzerData {
        int noOfDeadCodeInstances;
        SymbolEnv env;
        BLangNode parent;
        int functionCount;
        int invoCount;

        BInvokableSymbol currentParentFunction;

        InvocationGraph invocationGraph = new InvocationGraph();
    }

    public static class InvocationGraph{
        HashMap<BInvokableSymbol,HashSet<BInvokableSymbol>> invocationGraph;

        public InvocationGraph() {
            this.invocationGraph = new HashMap<>();
        }

        public void declareFunction(BInvokableSymbol functionSymbol) {
            if (!invocationGraph.containsKey(functionSymbol)) {
                invocationGraph.put(functionSymbol,new HashSet<>());
            }
        }

        // Only registers invocations of already known functions
        public void addFunctionInvocation(BInvokableSymbol startFunctionSymbol,BInvokableSymbol endFunctionSymbol) {
            if (invocationGraph.containsKey(endFunctionSymbol)) {
                invocationGraph.get(startFunctionSymbol).add(endFunctionSymbol);
            }
        }

        public void printInvocationGraph() {
            for (BInvokableSymbol startFunction : invocationGraph.keySet()) {
                System.out.print(startFunction.name.value + "==>");
                for (BInvokableSymbol endFunction: invocationGraph.get(startFunction)) {
                    System.out.print(endFunction.name.value +"  ");
                }
                System.out.println();
            }
        }
    }
}
