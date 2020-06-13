package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.Symbol;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockDesugar {

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private final Desugar desugar;
    private final SymbolEnter symbolEnter;
    private BLangPackage bLangPackage;

    private static final CompilerContext.Key<MockDesugar> MOCK_DESUGAR_KEY = new CompilerContext.Key<>();

    public static MockDesugar getInstance(CompilerContext context) {
        MockDesugar desugar = context.get(MOCK_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new MockDesugar(context);
        }
        return desugar;
    }

    private MockDesugar(CompilerContext context) {
        context.put(MOCK_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
    }

    // Looks for all the mock functions and generates the equivalent desugar
    public void generateMockFunctions(BLangPackage pkgNode) {
        System.out.println("[MockDesugar] (generateMockFunctions) Generating Mock Functions");

        // Set the BLangPackage
        this.bLangPackage = pkgNode;

        // Get the Mock Function map from the pkgNode
        Map<String, String> mockFunctionMap = pkgNode.getTestablePkg().getMockFunctionNamesMap();

        // Get the set of functions to generate
        Set<String> mockFunctionSet = mockFunctionMap.keySet();

        for (String function: mockFunctionSet) {
            System.out.println("[MockDesugar] (generateMockFunctions) Generating mock function for : " + function);
            System.out.println("[MockDesugar] (generateMockFunctions) Adding generated mock functions to package node");
            pkgNode.addFunction(generateMockFunction(function));
        }

        System.out.println("[MockDesugar] (generateMockFunctions) Added all generated functions to pkgNode fn list");

    }


    // Generates and returns a BLangFunction
    private BLangFunction generateMockFunction(String functionName) {

        // Get the orginal function from the pkgNode function list for reference
        BLangFunction originalFunction = getOriginalFunction(functionName);

        // Set the function name to $MOCK_<functionName>
        functionName = "$MOCK_" + functionName;

        // Create the Base function with the name
        BLangFunction generatedMock = ASTBuilderUtil.createFunction(bLangPackage.pos, functionName);

        // Required Params
        generatedMock.requiredParams = generateRequiredParams(originalFunction);

        // Return Type Node
        generatedMock.returnTypeNode = generateReturnTypeNode(originalFunction);

        // Body
        generatedMock.body = generateBody();

        // Invokable Symbol
        generatedMock.symbol = generateSymbol(functionName, originalFunction);

        // Type
        generatedMock.type = generateType(originalFunction);

        // ws / cloned env

        return generatedMock;
    }

    // Looks through the blangPkg and returns the original function
    private BLangFunction getOriginalFunction(String functionName) {
        List<BLangFunction> functionList = bLangPackage.getFunctions();

        for (BLangFunction function : functionList) {
            if (function.getName().toString().equals(functionName)) {
                return function;
            } // No need for else since we wouldnt even reach this point if there wasnt a function. It has to work
        }

        return null; // This shouldnt hit. If it does, something has gone very very wrong
    }

    // Checks the orignal functions 'requiredParam' and copies it
    // TODO : If this doesnt work, then simply expand the list and add the params individually
    //  OR simply null the unwanted stuff and see if that works
    //  OR .. just do it from scratch :/
    private List<BLangSimpleVariable> generateRequiredParams(BLangFunction originalFunction) {
        List<BLangSimpleVariable> requiredParams = null;

        // Required params will be the same as the original function
        // Extract the List from 'originalFunction'
        requiredParams = originalFunction.requiredParams;

        return requiredParams;
    }

    private BLangType generateReturnTypeNode(BLangFunction originalFunction) {
        BLangType returnTypeNode = null;

        // Return Type Node will be the same as orginal function
        // Extract returnTypeNode from 'originalFunction'

        returnTypeNode = originalFunction.returnTypeNode;

        return returnTypeNode;
    }


    // Try copying a body from the template function and see if it calls it properly.
    // This is the place where things go wrong
    private BLangFunctionBody generateBody() {
        BLangFunctionBody body = null;

        // THIS IS NOT THE FINAL IMPLEMENTATION
        // WRITTEN TO TEST IF THE BODY WILL ACTUALLY CALL THE MOCKHANDLER FUNCTION

        body = ((BLangFunction)((java.util.ArrayList)((BLangTestablePackage)((java.util.ArrayList)((BLangPackage)((MockDesugar)this).bLangPackage).testablePkgs).get(0)).functions).get(0)).body;

        return body;
    }


    // Invokable Symbol needs to be generated manually
    private BInvokableSymbol generateSymbol(String functionName, BLangFunction originalFunction) {
        BInvokableSymbol symbol = null;
        BInvokableType bInvokableType = generateSymbolInvokableType(originalFunction);

        symbol = new BInvokableSymbol(
                820,
                0,
                new Name(functionName),
                bLangPackage.packageID,
                bInvokableType,
                bLangPackage.symbol
        );


        return symbol;
    }

    private BInvokableType generateSymbolInvokableType(BLangFunction orginalFunction) {
        BInvokableType bInvokableType = null;


        bInvokableType = (BInvokableType) orginalFunction.symbol.type;
        // Incase yuou have to generate the function
//        List<BType> paramType;
//        BType retType = symTable.intType;
//        BTypeSymbol tsymbol;
//
//        bInvokableType = new BInvokableType(paramType, retType, tsymbol);

        return bInvokableType;
    }


    private BType generateType(BLangFunction originalFunction) {
        BType type = null;

        type = originalFunction.type;

        return type;
    }



//    // Creates the Invokable Symbol for the Mock function
//    private BInvokableSymbol createMockInvokableSymbol(BLangPackage pkgNode) {
//        // PARAM TYPES
//        List<BType> paramTypes = new ArrayList<>();
//
//        // ballerina/test:0.0.0:MockFunction
//        Symbol testPkg = pkgNode.symbol.scope.lookup(new Name("test")).symbol;
//
//        Symbol mockFunctionSymbol = ((BSymbol) testPkg).scope.lookup(new Name("MockFunction")).symbol;
//        BType mockObjectType = ((BSymbol) mockFunctionSymbol).type;
//        paramTypes.add(0, mockObjectType);
//
//        // anydata
//        BType anydataType = symTable.anydataType;
//        paramTypes.add(1, anydataType);
//
//
//
//        // TYPE SYMBOL
//        BTypeSymbol typeSymbol = new BInvokableTypeSymbol(67108892, 0, pkgNode.packageID, symTable.anyType, pkgNode.symbol);
//        List<BVarSymbol> params = new ArrayList<>();
//
//        // mockFunction
//        BVarSymbol mockFunction = symbolEnter.createVarSymbol(64, ((BSymbol) mockFunctionSymbol).type, new Name("mockFunction"), symTable.pkgEnvMap.get(pkgNode.symbol));
//        params.add(mockFunction);
//        // args
//        BVarSymbol args = symbolEnter.createVarSymbol(8256, ((BSymbol) mockFunctionSymbol).type, new Name("args"), symTable.pkgEnvMap.get(pkgNode.symbol));
//        params.add(args);
//        ((BInvokableTypeSymbol) typeSymbol).params = params;
//
//
//        // Generate Invokable Symbol
//        BInvokableSymbol bInvokableSymbol = new BInvokableSymbol(
//                820,
//                0,
//                new Name("$$$MOCK_intAdd"),
//                pkgNode.packageID,
//                new BInvokableType(paramTypes, symTable.anyType, typeSymbol),
//                pkgNode.symbol
//        );
//
//        return bInvokableSymbol;
//    }
//
//    // Creates the Body for the Mock function
//    private BLangFunctionBody createMockBody(BLangPackage pkgNode) {
//        Symbol testPkg = pkgNode.symbol.scope.lookup(new Name("test")).symbol;
//        Symbol mockFunctionSymbol = ((BSymbol) testPkg).scope.lookup(new Name("MockFunction")).symbol;
//
//
//        // List of statements
//        List<BLangStatement> statements = new ArrayList<>();
//
//        // Invokable Symbol
//        BInvokableSymbol invokableSymbol = createHandlerInvokableSymbol(pkgNode);
//
//        // BLangInvocation -> requiredArgs
//        List<BLangSimpleVariable> requiredArgs = new ArrayList<>();
//        BLangSimpleVariable bLangSimpleVariable = ASTBuilderUtil.createVariable(pkgNode.pos, "mockFunction", ((BSymbol) mockFunctionSymbol).type);
//        requiredArgs.add(bLangSimpleVariable);
//
//        // BlangReturn -> BLangInvocation
//        BLangInvocation invocation = ASTBuilderUtil.createInvocationExpr(pkgNode.pos, invokableSymbol, requiredArgs, symResolver);
//
//        // statements -> BlangReturn
//        BLangReturn bLangReturn = ASTBuilderUtil.createReturnStmt(pkgNode.pos, invocation);
//        statements.add(bLangReturn);
//
//        BLangFunctionBody mockBody = ASTBuilderUtil.createBlockFunctionBody(pkgNode.pos, statements);
//        return mockBody;
//    }
//
//
//    private BInvokableSymbol createHandlerInvokableSymbol(BLangPackage pkgNode) {
//
//        // BInvokableSymbol -> paramType
//        List<BType> paramTypes = new ArrayList<>();
//        Symbol testPkg = pkgNode.symbol.scope.lookup(new Name("test")).symbol;
//        Symbol mockFunctionSymbol = ((BSymbol) testPkg).scope.lookup(new Name("MockFunction")).symbol;
//        BType mockObjectType = ((BSymbol) mockFunctionSymbol).type;
//        paramTypes.add(0, mockObjectType);
//
//        // BInvokableSymbol -> retType
//        BType retType = symTable.anyType;
//
//        // BInvokableSymbol -> Tsymbol
//        BTypeSymbol typeSymbol = new BInvokableTypeSymbol(67108892, 3, pkgNode.packageID, symTable.anyType, pkgNode.symbol);
//        List<BVarSymbol> params = new ArrayList<>();
//
//        // mockFunction
//        BVarSymbol mockFunction = symbolEnter.createVarSymbol(64, ((BSymbol) mockFunctionSymbol).type, new Name("mockFunction"), symTable.pkgEnvMap.get(pkgNode.symbol));
//        params.add(mockFunction);
//
//        ((BInvokableTypeSymbol) typeSymbol).params = params;
//
//
//        BInvokableType bInvokableType = new BInvokableType(paramTypes, retType, typeSymbol);
//
//        BInvokableSymbol invokableSymbol = new BInvokableSymbol(
//                820,
//                3,
//                new Name("mockHandler"),
//                pkgNode.packageID,
//                bInvokableType,
//                pkgNode.symbol);
//
//        return invokableSymbol;
//    }

}