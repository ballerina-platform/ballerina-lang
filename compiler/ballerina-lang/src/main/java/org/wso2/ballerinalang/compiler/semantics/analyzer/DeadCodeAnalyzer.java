package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.projects.ProjectKind;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.*;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.expressions.*;
import org.wso2.ballerinalang.compiler.tree.statements.*;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.ballerinalang.model.tree.NodeKind.CLASS_DEFN;
import static org.ballerinalang.model.tree.NodeKind.FUNCTION;

public class DeadCodeAnalyzer extends SimpleBLangNodeAnalyzer<DeadCodeAnalyzer.AnalyzerData> {


    // --------------------------------------------------------- SAME AS CODEANALYZER --------------------------------------------------------- //
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

    // --------------------------------------------------------- MODIFICATIONS AND ADDITIONS --------------------------------------------------------- //

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

        if (!function.getFlags().contains(Flag.INTERFACE)) {         // body is null for abstract functions
            if (function.body.getKind() != NodeKind.EXTERN_FUNCTION_BODY && function.getBody() instanceof BLangBlockFunctionBody functionBody) {        // handling remote functions
                for (BLangStatement stmt : functionBody.stmts) {
                    data.currentParentFunction = parentFunctionSymbol;      // Reassign the currentParentFunction in case it got changed inside a visit call (Lambda functions might change it)
                    visitNode(stmt, data);
                }
            }
        }
    }

    // Top level nodes does not contain Arrow functions
    // Because of this they have to be manually added as parent functions
    public void visit(BLangArrowFunction node, AnalyzerData data) {
        if (node.parent instanceof BLangSimpleVariable && !node.parent.getBType().getKind().equals(TypeKind.TYPEREFDESC)) {
            data.addParentFunctionToPkgSymbol((BInvokableSymbol) ((BLangSimpleVariable) node.parent).symbol);  // TODO Find a cleaner way
        }
        visitNode(node.params, data);
        visitNode((BLangNode) node.functionName, data);
        visitNode(node.body, data);
    }

    // Registers an invocation
    // Updates the usedFunctions, deadFunctions and invocationMaps accordingly
    @Override
    public void visit(BLangInvocation node, AnalyzerData data) {
        if (node.symbol != null) {
            BInvokableSymbol symbol = (BInvokableSymbol) node.symbol;
            data.addInvocation(data.currentParentFunction, symbol);
            visitNode(node.expr, data);
            visitNode(node.argExprs, data);
        }
    }

    // Used to track the invocations of Lambda and arrow function pointers
    @Override
    public void visit(BLangSimpleVarRef node, AnalyzerData data) {
        // TODO Remove null check
        if (node.symbol != null) {          // Certain packages pulled from ballerina central had null symbols
            if (node.symbol.getKind() == SymbolKind.FUNCTION) {
                data.addInvocation(data.currentParentFunction, ((BInvokableSymbol) node.symbol));
            }
        }
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

    private boolean isMainPkgNode(BLangPackage pkgNode) {
        if (pkgNode.moduleContextDataHolder != null) {
            if (pkgNode.moduleContextDataHolder.descriptor().name().moduleNamePart() == null && pkgNode.moduleContextDataHolder.projectKind() == ProjectKind.BUILD_PROJECT) {
                return true;
            }
        }
        return false;
    }


    /**
     * keeps track of function invocations using a HashMap.
     * determines used functions and dead functions.
     * keeps track of lambda pointer variables and their functions.
     */
    public static class AnalyzerData {
        BInvokableSymbol currentParentFunction;     // used to determine the parent of the current invocation
        private HashMap<BInvokableSymbol, BInvokableSymbol> lambdaPointers;   // Key = Function pointer variable // Value = Lambda Function


        public AnalyzerData() {
            this.lambdaPointers = new HashMap<>();
        }

        // Add a function to the module as a parent function
        public void addParentFunctionToPkgSymbol(BInvokableSymbol symbol) {
            if (symbol.usedState == UsedState.UNEXPOLORED) symbol.usedState = UsedState.UNUSED;
            getPackageSymbol(symbol).invocationMap.putIfAbsent(symbol, new HashSet<>());    // Adding to the pkgSymbol
            getPackageSymbol(symbol).deadFunctions.add(symbol);     // Pre declare as dead
        }

        /**
         * Updates the invocation graph of the relevant Module
         * if the startFunction is already a "USED" function, declare children "USED" as well
         *
         * @param startFunctionSymbol Symbol of the parent function
         * @param endFunctionSymbol   Symbol of the invoked (callee) function
         */
        public void addInvocation(BInvokableSymbol startFunctionSymbol, BInvokableSymbol endFunctionSymbol) {
            startFunctionSymbol.childrenFunctions.add(endFunctionSymbol);     // Adding children to the start(parent) BInvokableSymbol

            if (endFunctionSymbol.usedState == UsedState.UNEXPOLORED) endFunctionSymbol.usedState = UsedState.UNUSED;       // Update the initial state
            updatePkgSymbolInvocationMap(startFunctionSymbol, endFunctionSymbol);
            if (isUsedFunction(startFunctionSymbol)) {
                markSelfAndChildrenAsUsed(endFunctionSymbol);
            }
        }

//        /**
//         * Iterates through the children chains and registers them as "used"
//         *
//         * @param parentSymbol current parent function symbol
//         */
//        public void registerChildrenAsUsed(BInvokableSymbol parentSymbol) {
//            getModule(parentSymbol).usedFunctions.add(parentSymbol);
//            if (getModule(parentSymbol).lambdaPointers.containsKey(parentSymbol)) {         // if the function is a lambda pointer, related lambda function will also be used
//                getModule(parentSymbol).usedFunctions.add(getModule(parentSymbol).lambdaPointers.get(parentSymbol));
//            }
//            if (getModule(parentSymbol).invocationMap.containsKey(parentSymbol)) {
//                for (BInvokableSymbol child : getChildren(parentSymbol)) {
//                    if (!getModule(child).usedFunctions.contains(child)) {          // In case of recursion (Parent having itself as a child)
//                        registerChildrenAsUsed(child);
//                    }
//                }
//            }
//        }

        public void markSelfAndChildrenAsUsed(BInvokableSymbol parentSymbol) {
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

        public void addToUsedListInPkgSym(BInvokableSymbol symbol) {
            getPackageSymbol(symbol).deadFunctions.remove(symbol);
            getPackageSymbol(symbol).usedFunctions.add(symbol);
        }

        public void updatePkgSymbolInvocationMap(BInvokableSymbol startSymbol, BInvokableSymbol endSymbol) {
            getPackageSymbol(startSymbol).invocationMap.putIfAbsent(startSymbol, new HashSet<>());
            getPackageSymbol(startSymbol).invocationMap.get(startSymbol).add(endSymbol);
        }

        public boolean isLambdaFunctionPointer(BInvokableSymbol symbol) {
            return lambdaPointers.containsKey(symbol);
        }

        public BPackageSymbol getPackageSymbol(BSymbol symbol) {
            if (symbol.owner.getKind().equals(SymbolKind.PACKAGE)) {    //Module level functions
                return (BPackageSymbol) symbol.owner;
            } else {
                return getPackageSymbol(symbol.owner);
            }
        }

        private boolean isUsedFunction(BInvokableSymbol symbol) {
            return symbol.usedState == UsedState.USED;
        }
    }
}
