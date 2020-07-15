/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to generate Mock Functions.
 *
 * @since 2.0.0
 */
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

    public void generateMockFunctions(BLangPackage pkgNode) {
        // Set the BLangPackage
        this.bLangPackage = pkgNode;

        // Get the Mock Function map from the pkgNode
        Map<String, String> mockFunctionMap = pkgNode.getTestablePkg().getMockFunctionNamesMap();

        // Get the set of functions to generate
        Set<String> mockFunctionSet = mockFunctionMap.keySet();

        for (String function: mockFunctionSet) {
            if (!function.contains("#")) { // Check added to skip desugaring for previous implementation mock functions
                pkgNode.getTestablePkg().functions.add(generateMockFunction(function));
            }
        }
    }

    private BLangFunction generateMockFunction(String functionName) {
        // Get the orginal function from the pkgNode function list for reference
        this.originalFunction = getOriginalFunction(functionName);

        // Set the function name to $MOCK_<functionName>
        functionName = "$MOCK_" + functionName;

        // Create the Base function with the name
        BLangFunction generatedMock = ASTBuilderUtil.createFunction(bLangPackage.pos, functionName);

        generatedMock.requiredParams = generateRequiredParams();        // Required Params
        generatedMock.returnTypeNode = generateReturnTypeNode();        // Return Type Node
        generatedMock.body = generateBody();                            // Body
        generatedMock.symbol = generateSymbol(functionName);            // Invokable Symbol
        generatedMock.type = generateType();                            // Type
        return generatedMock;
    }

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

    private BLangFunctionBody generateBody() {
        BLangFunctionBody body = ASTBuilderUtil.createBlockFunctionBody(bLangPackage.pos, generateStatements());
        body.scope = bLangPackage.symbol.scope;
        return body;
    }

    private List<BLangStatement> generateStatements() {
        // <MockFunctionObj>.functionToMock = (functionToMock);
        BLangAssignment bLangAssignment =
                ASTBuilderUtil.createAssignmentStmt(bLangPackage.pos, generateFieldBasedAccess(), generateRHSExpr());
        // BLangReturn Statement <retType> test:MockHandler(<MockFunctionObj>, [<args?>])
        BLangReturn blangReturn =
                ASTBuilderUtil.createReturnStmt(bLangPackage.pos, generateTypeConversionExpression());

        List<BLangStatement> statements = new ArrayList<>();

        statements.add(bLangAssignment);
        statements.add(blangReturn);

        return statements;
    }

    // <mockobj>.functionToMock =
    private BLangFieldBasedAccess generateFieldBasedAccess() {
        BLangVariableReference expr = getMockFunctionReference();
        BLangIdentifier field = ASTBuilderUtil.createIdentifier(bLangPackage.pos, "functionToMock");

        BLangFieldBasedAccess bLangFieldBasedAccess = ASTBuilderUtil.createFieldAccessExpr(expr, field);

        bLangFieldBasedAccess.fieldKind = FieldKind.SINGLE;
        bLangFieldBasedAccess.originalType = symTable.stringType;
        bLangFieldBasedAccess.lhsVar = true;
        bLangFieldBasedAccess.expectedType = symTable.stringType;
        bLangFieldBasedAccess.type = symTable.stringType;

        return bLangFieldBasedAccess;
    }

    // = (functionToMock)
    private BLangLiteral generateRHSExpr() {
        BType type = symTable.stringType;
        Object value = this.originalFunction.name.value;

        BLangLiteral bLangLiteral = ASTBuilderUtil.createLiteral(bLangPackage.pos, type, value);
        bLangLiteral.expectedType = type;

        return bLangLiteral;
    }

    private BLangTypeConversionExpr generateTypeConversionExpression() {
        BLangInvocation bLangInvocation = generateBLangInvocation();
        BType target = generateType();

        BLangTypeConversionExpr typeConversionExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        typeConversionExpr.pos = bLangInvocation.pos;
        typeConversionExpr.expr = bLangInvocation;
        typeConversionExpr.type = target;
        typeConversionExpr.targetType = target;

        return typeConversionExpr;
    }

    private BLangInvocation generateBLangInvocation() {
        BInvokableSymbol invokableSymbol = getMockHandlerInvokableSymbol();
        List<BLangExpression> argsExprs = generateInvocationRequiredArgs();

        BLangInvocation bLangInvocation =
                ASTBuilderUtil.createInvocationExprForMethod(bLangPackage.pos, invokableSymbol, argsExprs, symResolver);
        bLangInvocation.pkgAlias = (BLangIdentifier) createIdentifier("test");
        bLangInvocation.argExprs = argsExprs;
        bLangInvocation.expectedType = bLangInvocation.type;

        return bLangInvocation;
    }

    private BInvokableSymbol getMockHandlerInvokableSymbol() {
        BSymbol testPkg = bLangPackage.getTestablePkg().symbol.scope.lookup(new Name("test")).symbol;

        BInvokableSymbol mockHandlerSymbol =
                (BInvokableSymbol) testPkg.scope.lookup(new Name("mockHandler")).symbol;

        return mockHandlerSymbol;
    }

    private List<BLangExpression> generateInvocationRequiredArgs() {
        List<BLangExpression> requiredArgs = new ArrayList<>();

        BLangSimpleVarRef bLangSimpleVarRef = getMockFunctionReference();
        requiredArgs.add(bLangSimpleVarRef);

        BLangListConstructorExpr argumentArray = generateMockHandlerArgs();
        requiredArgs.add(argumentArray);

        return requiredArgs;
    }

    private BLangSimpleVarRef getMockFunctionReference() {
        String mockObjName =
                bLangPackage.getTestablePkg().getMockFunctionNamesMap().get(originalFunction.getName().toString());
        BVarSymbol mockObjectSymbol =
                (BVarSymbol) bLangPackage.getTestablePkg().symbol.scope.lookup(new Name(mockObjName)).symbol;

        BLangSimpleVarRef bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, mockObjectSymbol);

        return bLangSimpleVarRef;
    }

    private BLangListConstructorExpr generateMockHandlerArgs() {
        BLangListConstructorExpr argsList =
                ASTBuilderUtil.createEmptyArrayLiteral(bLangPackage.pos, symTable.arrayAnydataType);
        List<BLangSimpleVarRef> argVariables =
                ASTBuilderUtil.createVariableRefList(bLangPackage.pos, originalFunction.requiredParams);

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
