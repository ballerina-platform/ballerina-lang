package org.wso2.ballerinalang.compiler.desugar;


import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.IdentifierNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
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
    private BLangFunction originalFunction;

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
            pkgNode.getTestablePkg().functions.add(generateMockFunction(function));
        }

        System.out.println("[MockDesugar] (generateMockFunctions) Added all generated functions to pkgNode fn list");

    }

    // Generates and returns a BLangFunction
    private BLangFunction generateMockFunction(String functionName) {
        System.out.println("[MockDesugar] - (generateMockFunction) Generating for :" + functionName);

        // Get the orginal function from the pkgNode function list for reference
        this.originalFunction = getOriginalFunction(functionName);

        // Set the function name to $MOCK_<functionName>
        functionName = "$MOCK_" + functionName;

        // Create the Base function with the name
        BLangFunction generatedMock = ASTBuilderUtil.createFunction(bLangPackage.pos, functionName);

        // Required Params
        generatedMock.requiredParams = generateRequiredParams();

        // Return Type Node
        generatedMock.returnTypeNode = generateReturnTypeNode();

        // Body
        generatedMock.body = generateBody();

        // Invokable Symbol
        generatedMock.symbol = generateSymbol(functionName);

        // Type
        generatedMock.type = generateType();

        // ws / cloned env not included

        System.out.println("[MockDesugar] - (generateMockFunction) Function generated");
        return generatedMock;
    }

    // Looks through the blangPkg and returns the original function
    private BLangFunction getOriginalFunction(String functionName) {
        List<BLangFunction> functionList = bLangPackage.getFunctions();

        for (BLangFunction function : functionList) {
            if (function.getName().toString().equals(functionName)) {
                return function;
            }
        }

        return null;
    }

    private List<BLangSimpleVariable> generateRequiredParams() {
        List<BLangSimpleVariable> requiredParams = this.originalFunction.requiredParams;
        return requiredParams;
    }

    private BLangType generateReturnTypeNode() {
        BLangType returnTypeNode = this.originalFunction.returnTypeNode;
        return returnTypeNode;
    }

    private BInvokableSymbol generateSymbol(String functionName) {
        BInvokableSymbol symbol = new BInvokableSymbol(
                820,
                0,
                new Name(functionName),
                bLangPackage.packageID,
                generateSymbolInvokableType(),
                bLangPackage.symbol
        );

        return symbol;
    }

    private BInvokableType generateSymbolInvokableType() {
        BInvokableType bInvokableType = (BInvokableType) this.originalFunction.symbol.type;
        return bInvokableType;
    }

    private BType generateType() {
        BType type = ((BInvokableType) this.originalFunction.type).retType;
        return type;
    }

    // Try copying a body from the template function and see if it calls it properly.
    // This is the place where things go wrong
    private BLangFunctionBody generateBody() {
        BLangFunctionBody body = ASTBuilderUtil.createBlockFunctionBody(bLangPackage.pos, generateStatements());
        body.scope = bLangPackage.symbol.scope;
        return body;
    }

    private List<BLangStatement> generateStatements() {
        // List of statements
        List<BLangStatement> statements = new ArrayList<>();

        // BLangReturn Statement <retType> test:MockHandler(<MockFunctionObj>, [<args?>])
        BLangReturn blangReturn =
                ASTBuilderUtil.createReturnStmt(bLangPackage.pos, generateTypeConversionExpression());

        statements.add(blangReturn);

        return statements;
    }


    private BLangTypeConversionExpr generateTypeConversionExpression() {
        BLangExpression typeConversionExpr = null;

        BLangInvocation bLangInvocation = generateBLangInvocation();
        BType target = generateType(); // check this value

        // typeConversionExpr = ASTBuilderUtil.generateConversionExpr(bLangInvocation, target, symResolver); //check this



        BLangTypeConversionExpr typeConversionExpr1 = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        typeConversionExpr1.pos = bLangInvocation.pos;
        typeConversionExpr1.expr = bLangInvocation;
        typeConversionExpr1.type = target;
        typeConversionExpr1.targetType = target;

        return typeConversionExpr1;
    }

    // test:mockHandler(mock1, [a, b])
    private BLangInvocation generateBLangInvocation() {

        BInvokableSymbol invokableSymbol = getMockHandlerInvokableSymbol(); // Mock Handler
        List<BLangExpression> requiredArgs = generateInvocationRequiredArgs(); // Passed arguments

        BLangInvocation bLangInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(bLangPackage.pos, invokableSymbol, requiredArgs, symResolver);


        // Additional statements
        bLangInvocation.pkgAlias = (BLangIdentifier) createIdentifier("test"); //Should add missing pkg alias
        bLangInvocation.argExprs = requiredArgs;
        bLangInvocation.expectedType = bLangInvocation.type;

        return bLangInvocation;
    }

    // Looks for MockHandler Invokable symbol in the test package
    private BInvokableSymbol getMockHandlerInvokableSymbol() {
        BSymbol testPkg = bLangPackage.getTestablePkg().symbol.scope.lookup(new Name("test")).symbol; //Is this supposed to be BSymbol or BPackage something
        BInvokableSymbol mockHandlerSymbol = (BInvokableSymbol) testPkg.scope.lookup(new Name("mockHandler")).symbol;

        return mockHandlerSymbol;
    }

    private List<BLangExpression> generateInvocationRequiredArgs() {
        List<BLangExpression> requiredArgs = new ArrayList<>();

        // Reference for the Mock Function object
        BLangSimpleVarRef bLangSimpleVarRef = getMockFunctionReference();
        requiredArgs.add(bLangSimpleVarRef);

        // Reference for the arguments
        BLangListConstructorExpr argumentArray = generateMockHandlerArgs();
        requiredArgs.add(argumentArray);

        return requiredArgs;
    }

    // Returns reference to mock function
    private BLangSimpleVarRef getMockFunctionReference() {
        String mockObjName = bLangPackage.getTestablePkg().getMockFunctionNamesMap().get(originalFunction.getName().toString());
        BVarSymbol mockObjectSymbol = (BVarSymbol) bLangPackage.getTestablePkg().symbol.scope.lookup(new Name(mockObjName)).symbol;

        BLangSimpleVarRef bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, mockObjectSymbol);
        return bLangSimpleVarRef;
    }

    // Looks at the arguments passed and compiles a list
    private BLangListConstructorExpr generateMockHandlerArgs() {
        BLangListConstructorExpr argsList =
                ASTBuilderUtil.createEmptyArrayLiteral(bLangPackage.pos, symTable.arrayAnydataType);

        // THIS IS A PROBLEM
        // This cant be assigned to the BLangConstructorExpr
        // But when you debug, it shows that its a simple var ref
        // List of Simple var r
        List<BLangSimpleVarRef> argVariables =
                ASTBuilderUtil.createVariableRefList(bLangPackage.pos, originalFunction.requiredParams);

        // I dont know if this is legal
        for (BLangSimpleVarRef varRef : argVariables) {
            argsList.exprs.add((BLangExpression) varRef);
        }

        return argsList;
    }

    private static IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }
}