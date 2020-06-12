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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

public class MockDesugar {

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private final Desugar desugar;
    private final SymbolEnter symbolEnter;

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

    public void generateMockFunctions(BLangPackage pkgNode) {
        System.out.println("[MockDesugar] (generateMockFunctions) Generating Mock Functions");

        // Create the Base BLangFunction
        BLangFunction generatedMock = ASTBuilderUtil.createFunction(pkgNode.pos, "$$$MOCK_intAdd");


        // Create Mock Invokable Symbol "$$$MOCK
        generatedMock.symbol = createMockInvokableSymbol(pkgNode);

        generatedMock.body = createMockBody(pkgNode);


        System.out.println("[MockDesugar] (generateMockFunctions) Adding generated mock functions to package node");
        pkgNode.addFunction(generatedMock);
    }



    // Creates the Invokable Symbol for the Mock function
    private BInvokableSymbol createMockInvokableSymbol(BLangPackage pkgNode) {
        // PARAM TYPES
        List<BType> paramTypes = new ArrayList<>();

        // ballerina/test:0.0.0:MockFunction
        Symbol testPkg = pkgNode.symbol.scope.lookup(new Name("test")).symbol;
        Symbol mockFunctionSymbol = ((BSymbol) testPkg).scope.lookup(new Name("MockFunction")).symbol;
        BType mockObjectType = ((BSymbol) mockFunctionSymbol).type;
        paramTypes.add(0, mockObjectType);

        // anydata
        BType anydataType = symTable.anydataType;
        paramTypes.add(1, anydataType);



        // TYPE SYMBOL
        BTypeSymbol typeSymbol = new BInvokableTypeSymbol(67108892, 0, pkgNode.packageID, symTable.anyType, pkgNode.symbol);
        List<BVarSymbol> params = new ArrayList<>();

        // mockFunction
        BVarSymbol mockFunction = symbolEnter.createVarSymbol(64, ((BSymbol) mockFunctionSymbol).type, new Name("mockFunction"), symTable.pkgEnvMap.get(pkgNode.symbol));
        params.add(mockFunction);
        // args
        BVarSymbol args = symbolEnter.createVarSymbol(8256, ((BSymbol) mockFunctionSymbol).type, new Name("args"), symTable.pkgEnvMap.get(pkgNode.symbol));
        params.add(args);
        ((BInvokableTypeSymbol) typeSymbol).params = params;


        // Generate Invokable Symbol
        BInvokableSymbol bInvokableSymbol = new BInvokableSymbol(
                820,
                0,
                new Name("$$$MOCK_intAdd"),
                pkgNode.packageID,
                new BInvokableType(paramTypes, symTable.anyType, typeSymbol),
                pkgNode.symbol
        );

        return bInvokableSymbol;
    }

    // Creates the Body for the Mock function
    private BLangFunctionBody createMockBody(BLangPackage pkgNode) {
        Symbol testPkg = pkgNode.symbol.scope.lookup(new Name("test")).symbol;
        Symbol mockFunctionSymbol = ((BSymbol) testPkg).scope.lookup(new Name("MockFunction")).symbol;


        // List of statements
        List<BLangStatement> statements = new ArrayList<>();

        // Invokable Symbol
        BInvokableSymbol invokableSymbol = createHandlerInvokableSymbol(pkgNode);

        // BLangInvocation -> requiredArgs
        List<BLangSimpleVariable> requiredArgs = new ArrayList<>();
        BLangSimpleVariable bLangSimpleVariable = ASTBuilderUtil.createVariable(pkgNode.pos, "mockFunction", ((BSymbol) mockFunctionSymbol).type);
        requiredArgs.add(bLangSimpleVariable);

        // BlangReturn -> BLangInvocation
        BLangInvocation invocation = ASTBuilderUtil.createInvocationExpr(pkgNode.pos, invokableSymbol, requiredArgs, symResolver);

        // statements -> BlangReturn
        BLangReturn bLangReturn = ASTBuilderUtil.createReturnStmt(pkgNode.pos, invocation);
        statements.add(bLangReturn);

        BLangFunctionBody mockBody = ASTBuilderUtil.createBlockFunctionBody(pkgNode.pos, statements);
        return mockBody;
    }


    private BInvokableSymbol createHandlerInvokableSymbol(BLangPackage pkgNode) {

        // BInvokableSymbol -> paramType
        List<BType> paramTypes = new ArrayList<>();
        Symbol testPkg = pkgNode.symbol.scope.lookup(new Name("test")).symbol;
        Symbol mockFunctionSymbol = ((BSymbol) testPkg).scope.lookup(new Name("MockFunction")).symbol;
        BType mockObjectType = ((BSymbol) mockFunctionSymbol).type;
        paramTypes.add(0, mockObjectType);

        // BInvokableSymbol -> retType
        BType retType = symTable.anyType;

        // BInvokableSymbol -> Tsymbol
        BTypeSymbol typeSymbol = new BInvokableTypeSymbol(67108892, 3, pkgNode.packageID, symTable.anyType, pkgNode.symbol);
        List<BVarSymbol> params = new ArrayList<>();

        // mockFunction
        BVarSymbol mockFunction = symbolEnter.createVarSymbol(64, ((BSymbol) mockFunctionSymbol).type, new Name("mockFunction"), symTable.pkgEnvMap.get(pkgNode.symbol));
        params.add(mockFunction);

        ((BInvokableTypeSymbol) typeSymbol).params = params;


        BInvokableType bInvokableType = new BInvokableType(paramTypes, retType, typeSymbol);

        BInvokableSymbol invokableSymbol = new BInvokableSymbol(
                820,
                3,
                new Name("mockHandler"),
                pkgNode.packageID,
                bInvokableType,
                pkgNode.symbol);

        return invokableSymbol;
    }

}