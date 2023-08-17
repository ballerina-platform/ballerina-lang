package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
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

    private final SymbolTable symTable;  // Do we need this?

    private BLangPackage pkgNodeCopy = null;    // Used for debugging purposes TODO remove this


    public static DeadCodeAnalyzer getInstance(CompilerContext context) {
        DeadCodeAnalyzer deadCodeAnalyzer = context.get(DEAD_CODE_ANALYZER_KEY);
        if (deadCodeAnalyzer == null) {
            deadCodeAnalyzer = new DeadCodeAnalyzer(context);
        }
        return deadCodeAnalyzer;
    }

    private DeadCodeAnalyzer(CompilerContext context) {
        context.put(DEAD_CODE_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
    }

    // 1st Method that gets called
    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNodeCopy = pkgNode;


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
                registerFunctions(topLevelFunction);
            }
            if (topLevelNode.getKind() == CLASS_DEFN) {
                for (BLangFunction classLevelFunction : ((BLangClassDefinition) topLevelNode).functions) {
                    registerFunctions(classLevelFunction);
                }
            }
        }

        // Traverse through each node in TopLevelFunctions
        for (TopLevelNode topLevelNode : topLevelNodes) {
            visitNode((BLangNode) topLevelNode, data);
        }

        data.invocationData.findDeadFunctionsGlobally();
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
            parentFunction = (BInvokableSymbol) ((BLangSimpleVariable) ((function).parent).parent).symbol;
        }

        // TODO remove null check
        if (!(function.body == null)) {         // body was null for certain functions pulled from ballerina central
            if (function.body.getKind() != NodeKind.EXTERN_FUNCTION_BODY) {        // handling remote functions
                BLangBlockFunctionBody functionBody = (BLangBlockFunctionBody) function.getBody();
                for (BLangStatement stmt : functionBody.stmts) {
                    data.currentParentFunction = parentFunction;                // Reassign the currentParentFunction in case it got changed inside a visit call
                    visitNode(stmt, data);
                }
            }
        }

    }

    // Arrow functions are also added to the current module
    @Override
    public void visit(BLangArrowFunction node, AnalyzerData data) {
        data.invocationData.addFunctionToModule((BInvokableSymbol) ((BLangSimpleVariable) node.parent).symbol);  // TODO Find a cleaner way
        visitNode(node.params, data);
        visitNode((BLangNode) node.functionName, data);
        visitNode(node.body, data);
    }

    // Adds the invocation to the current module
    @Override
    public void visit(BLangInvocation iExpression, AnalyzerData data) {
        if (iExpression.symbol != null) {
            BInvokableSymbol symbol = (BInvokableSymbol) iExpression.symbol;
            data.invocationData.addInvocation(data.currentParentFunction, symbol);
            visitNode(iExpression.expr, data);
            visitNode(iExpression.argExprs, data);
        }
    }

    // Used to track the invocations of Lambda function pointers.
    @Override
    public void visit(BLangSimpleVarRef node, AnalyzerData data) {
        analyzeNode(node, data);
        visitNode(node.pkgAlias, data);
        visitNode(node.variableName, data);
        // TODO Remove null check
        if (node.symbol != null) {          // Certain packages pulled from ballerina central had null symbols
            if (node.symbol.getKind() == SymbolKind.FUNCTION) {
                data.invocationData.addInvocation(data.currentParentFunction, ((BInvokableSymbol) node.symbol));
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
        data.invocationData.addFunctionToModule(functionPointer);
        data.invocationData.addLambdaPointer(functionPointer, function); // Add the Lambda functions and their var pointers to a HashMap for future use
    }

    private void addFunctionToMap(BLangFunction topLevelFunction) {
        data.invocationData.addFunctionToModule(topLevelFunction.symbol);
        if (topLevelFunction.symbol.originalName.value.equals("main") || topLevelFunction.symbol.originalName.value.equals("init")) {
            data.invocationData.getModule(topLevelFunction.symbol).usedFunctions.add(topLevelFunction.symbol);      // Setting the main and init functions as the starting points of used function chains
        }
    }

    private void registerFunctions(BLangFunction function) {
        if (isLambda(function)) {  // Checks whether the function is a lambda one
            addLambdaFunctionToMap(function);
        } else {    // if the function is not a lambda one, it should be a normal method
            addFunctionToMap(function);
        }
    }


    public static class AnalyzerData {      // TODO merge AnalyzerData and InvocationData
        BInvokableSymbol currentParentFunction;     // used to determine the parent of the invocation
        InvocationData invocationData = new InvocationData();
    }


    /**
     * keeps track of function invocations using a HashMap.
     * determines used functions and dead functions.
     * keeps track of lambda pointer variables and their functions.
     */
    public static class InvocationData {
        private final HashMap<PackageID, ModuleData> moduleMap;     // holds the data related to each module separately

        public InvocationData() {
            this.moduleMap = new HashMap<>();
        }

        // If the Module does not already exist, create a new one and add it
        public void addModule(PackageID packageID, ModuleData moduleData) {
            if (!moduleMap.containsKey(packageID)) {
                moduleMap.put(packageID, moduleData);
            }
        }

        // Add a function to the module as a parent function
        public void addFunctionToModule(BInvokableSymbol symbol) {
            getModule(symbol).declareFunction(symbol);
        }

        /**
         * Updates the invocation graph of the relevant Module
         * if the startFunction is already a "used" function, declare children "used" as well
         *
         * @param startFunctionSymbol Symbol of the parent function
         * @param endFunctionSymbol   Symbol of the invoked (callee) function
         */
        public void addInvocation(BInvokableSymbol startFunctionSymbol, BInvokableSymbol endFunctionSymbol) {
            getModule(startFunctionSymbol).addFunctionInvocation(startFunctionSymbol, endFunctionSymbol);
            if (isUsedFunction(startFunctionSymbol)) {
                registerChildrenAsUsed(endFunctionSymbol);
            }
        }

        /**
         * Iterates through the children chains and registers them as "used"
         *
         * @param symbol current parent function symbol
         */
        public void registerChildrenAsUsed(BInvokableSymbol symbol) {
            getModule(symbol).usedFunctions.add(symbol);
            if (getModule(symbol).invocationMap.containsKey(symbol)) {
                for (BInvokableSymbol child : getChildren(symbol)) {
                    registerChildrenAsUsed(child);
                }
            }
        }

        /**
         * returns the ModuleData object respective for the symbol
         * if the object does not exist, creates a new one
         *
         * @param symbol symbol to check
         * @return ModuleData object holding the given symbol
         */
        private ModuleData getModule(BInvokableSymbol symbol) {
            if (!moduleMap.containsKey(symbol.pkgID)) {
                addModule(symbol.pkgID, new ModuleData());
            }
            return moduleMap.get(symbol.pkgID);
        }

        private boolean isUsedFunction(BInvokableSymbol symbol) {
            return getModule(symbol).usedFunctions.contains(symbol);
        }

        private HashSet<BInvokableSymbol> getChildren(BInvokableSymbol parentSymbol) {
            return getModule(parentSymbol).invocationMap.get(parentSymbol);
        }

        // Register the lambda pointer variables and respective lambda function for ease of access
        public void addLambdaPointer(BInvokableSymbol functionPointer, BInvokableSymbol function) {
            getModule(functionPointer).lambdaPointers.put(functionPointer, function);
        }

        // Iterates through every module and detects dead functions
        public void findDeadFunctionsGlobally() {
            for (ModuleData module : moduleMap.values()) {
                module.findDeadFunctions();
            }
        }
    }

    /**
     * Represents a single Module for dead code detection
     */
    public static class ModuleData {
        HashMap<BInvokableSymbol, HashSet<BInvokableSymbol>> invocationMap;     // Key = Start Function // Value = End Function
        HashSet<BInvokableSymbol> privateDeadFunctions;
        HashSet<BInvokableSymbol> publicDeadFunctions;
        HashMap<BInvokableSymbol, BInvokableSymbol> lambdaPointers;   // Key = Function pointer variable // Value = Lambda Function
        HashSet<BInvokableSymbol> usedFunctions;

        public ModuleData() {
            this.privateDeadFunctions = new HashSet<>();
            this.publicDeadFunctions = new HashSet<>();
            this.usedFunctions = new HashSet<>();
            this.lambdaPointers = new HashMap<>();
            this.invocationMap = new HashMap<>();
        }

        /**
         * if the function is not already declared, it is added as a new key to the invocationMap hashMap
         *
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
         * @param endFunctionSymbol   Child function called by the invocation
         */
        public void addFunctionInvocation(BInvokableSymbol startFunctionSymbol, BInvokableSymbol endFunctionSymbol) {
            invocationMap.get(startFunctionSymbol).add(endFunctionSymbol);
        }

        /**
         * Iterates through the functions of the module and detects not "used" functions
         * adds them to the respective(public or private) HashSet
         * If the functions is a lambda function pointer, adds the origin lambda function to the dead code as well
         */
        public void findDeadFunctions() {
            for (BInvokableSymbol functionSymbol : invocationMap.keySet()) {
                if (!usedFunctions.contains(functionSymbol)) {
                    HashSet<BInvokableSymbol> deadFunctions = Symbols.isPublic(functionSymbol) ? publicDeadFunctions : privateDeadFunctions;
                    deadFunctions.add(functionSymbol);
                    if (lambdaPointers.containsKey(functionSymbol)) {     // Adding the lambda function along with the function pointer variable
                        deadFunctions.add(lambdaPointers.get(functionSymbol));
                    }
                } else {
                    privateDeadFunctions.remove(functionSymbol);
                    publicDeadFunctions.remove(functionSymbol);
                }
            }
        }
    }
}
