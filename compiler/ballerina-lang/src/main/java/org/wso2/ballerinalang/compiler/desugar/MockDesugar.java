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
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Class to generate Mock Functions.
 *
 * @since 2.0.0
 */
public class MockDesugar {

    private static final CompilerContext.Key<MockDesugar> MOCK_DESUGAR_KEY = new CompilerContext.Key<>();
    private static final String MOCK_ANNOTATION_DELIMITER = "#";
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private BLangPackage bLangPackage;
    private BLangFunction originalFunction;
    private BInvokableSymbol importFunction;
    private String mockFnObjectName;

    private MockDesugar(CompilerContext context) {
        context.put(MOCK_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
    }

    public static MockDesugar getInstance(CompilerContext context) {
        MockDesugar desugar = context.get(MOCK_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new MockDesugar(context);
        }
        return desugar;
    }

    private static IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    /**
     * Generates all the Mock Functions and adds them to the BLangPackage's TestablePkg function list.
     *
     * @param pkgNode BLangPackage
     */
    public void generateMockFunctions(BLangPackage pkgNode) {
        // Set the BLangPackage
        this.bLangPackage = pkgNode;

        // Get the Mock Function map from the pkgNode
        Map<String, String> mockFunctionMap = pkgNode.getTestablePkg().getMockFunctionNamesMap();

        // Get the set of functions to generate
        Set<String> mockFunctionSet = mockFunctionMap.keySet();

        for (String function : mockFunctionSet) {
            if (!function.contains("~")) {
                pkgNode.getTestablePkg().functions.add(generateMockFunction(function));
            }
        }
    }

    /**
     * Generates a BLangFunction based on the function name provided.
     *
     * @param functionName Name of the function to generate the mock
     * @return Generated BLangFunction
     */
    private BLangFunction generateMockFunction(String functionName) {
        // Set the current mock object
        this.mockFnObjectName = this.bLangPackage.getTestablePkg().getMockFunctionNamesMap().get(functionName);

        // Reset both import and original functions
        this.importFunction = null;
        this.originalFunction = null;

        // Identify if function is part of current package or import package
        if (functionName.contains(this.bLangPackage.packageID.toString())) {
            // Simply extract the name only
            functionName = functionName.substring(functionName.indexOf(MOCK_ANNOTATION_DELIMITER) + 1);
            this.originalFunction = getOriginalFunction(functionName);
        } else {
            // Extract the name and the package details
            String packageName = functionName.substring(functionName.indexOf('/') + 1, functionName.indexOf(':'));
            functionName = functionName.substring(functionName.indexOf(MOCK_ANNOTATION_DELIMITER) + 1);
            this.importFunction = getImportFunction(functionName, packageName);
        }

        // Set the function name to $MOCK_<functionName>
        functionName = "$MOCK_" + functionName;

        // Create the Base function with the name
        BLangFunction generatedMock = ASTBuilderUtil.createFunction(bLangPackage.pos, functionName);

        if (this.originalFunction != null || this.importFunction != null) {
            generatedMock.requiredParams = generateRequiredParams();        // Required Params
            generatedMock.restParam = generateRestParam();                  // Rest Param
            generatedMock.returnTypeNode = generateReturnTypeNode();        // Return Type Node
            generatedMock.body = generateBody();                            // Body
            generatedMock.type = generateSymbolInvokableType();             // Invokable Type
            generatedMock.symbol = generateSymbol(functionName);            // Invokable Symbol
        } else {
            throw new IllegalStateException("Mock Function and Function to Mock cannot be null");
        }

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

    private BInvokableSymbol getImportFunction(String functionName, String packageName) {
        BInvokableSymbol bInvokableSymbol =
                getInvokableSymbol(functionName, packageName, this.bLangPackage.getImports());

        if (bInvokableSymbol == null) {
            bInvokableSymbol =
                    getInvokableSymbol(functionName, packageName, this.bLangPackage.getTestablePkg().getImports());
        }

        return bInvokableSymbol;
    }

    private BInvokableSymbol getInvokableSymbol(String functionName, String packageName,
                                                List<BLangImportPackage> importList) {
        // Loop through each BLangImportPackage
        for (BLangImportPackage importPkg : importList) {
            // If the import package name matches
            if (importPkg.alias.getValue().equals(packageName)) {
                // return the function name defined in the package entry map
                return (BInvokableSymbol) importPkg.symbol.scope.entries.get(new Name(functionName)).symbol;
            }
        }

        return null;
    }

    private List<BLangSimpleVariable> generateRequiredParams() {
        List<BLangSimpleVariable> requiredParams;

        if (this.originalFunction != null) {
            requiredParams = this.originalFunction.requiredParams;
        } else {
            requiredParams = generateImportRequiredParams();
        }

        return requiredParams;
    }

    private List<BLangSimpleVariable> generateImportRequiredParams() {
        List<BLangSimpleVariable> bLangSimpleVariables = new ArrayList<>();

        // Iterate through the params and generate the BLangSimpleVariable
        for (BVarSymbol bVarSymbol : this.importFunction.params) {
            BLangSimpleVariable bLangSimpleVariable =
                    ASTBuilderUtil.createVariable(bLangPackage.pos, bVarSymbol.name.getValue(),
                                                  bVarSymbol.type, null, bVarSymbol);
            bLangSimpleVariables.add(bLangSimpleVariable);
        }

        return bLangSimpleVariables;
    }

    private BLangSimpleVariable generateRestParam() {
        BLangSimpleVariable bLangSimpleVariable = null;

        if (this.importFunction != null) {
            if (this.importFunction.restParam != null) {
                bLangSimpleVariable =
                        ASTBuilderUtil.createVariable(bLangPackage.pos, this.importFunction.restParam.name.getValue(),
                                                      this.importFunction.restParam.type, null,
                                                      this.importFunction.restParam);
                bLangSimpleVariable.typeNode = ASTBuilderUtil.createTypeNode(this.importFunction.restParam.type);
            }
        } else {
            bLangSimpleVariable = this.originalFunction.restParam;
        }

        return bLangSimpleVariable;
    }

    private BLangType generateReturnTypeNode() {
        BLangType returnTypeNode;

        if (this.originalFunction == null) {
            returnTypeNode = generateImportReturnTypeNode();
        } else {
            returnTypeNode = this.originalFunction.returnTypeNode;
        }

        return returnTypeNode;
    }

    private BLangType generateImportReturnTypeNode() {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = this.bLangPackage.pos;
        typeNode.typeKind = this.importFunction.retType.getKind();
        typeNode.type = this.importFunction.retType;

        return typeNode;
    }

    private BInvokableSymbol generateSymbol(String functionName) {
        BInvokableSymbol symbol = new BInvokableSymbol(
                820,
                0,
                new Name(functionName),
                bLangPackage.packageID,
                generateSymbolInvokableType(),
                bLangPackage.symbol,
                symTable.builtinPos,
                VIRTUAL
        );

        return symbol;
    }

    private BInvokableType generateSymbolInvokableType() {
        BInvokableType bInvokableType;

        if (this.originalFunction == null && this.importFunction != null) {
            bInvokableType = (BInvokableType) this.importFunction.type;
        } else {
            bInvokableType = (BInvokableType) this.originalFunction.symbol.type;
        }
        return bInvokableType;
    }

    private BLangFunctionBody generateBody() {
        BLangFunctionBody body = ASTBuilderUtil.createBlockFunctionBody(bLangPackage.pos, generateStatements());
        body.scope = bLangPackage.symbol.scope;
        return body;
    }

    private List<BLangStatement> generateStatements() {
        // The following synthesizes the equivalent of : `<MockFunctionObj>.functionToMock = (functionToMock);`
        String functionToMockVal = (this.originalFunction == null) ?
                this.importFunction.name.toString() : this.originalFunction.name.toString();
        BLangAssignment bLangAssignment1 = ASTBuilderUtil.createAssignmentStmt(
                bLangPackage.pos,
                generateFieldBasedAccess("functionToMock"),
                generateRHSExpr(functionToMockVal));

        // The following synthesizes the equivalent of :
        // `<MockFunctionObj>.functionToMockPackage = `(functionToMockPackage);`
        String functionToMockPackageVal = (this.originalFunction == null) ?
                this.importFunction.pkgID.toString()
                        + "/" + getFunctionSource(this.importFunction.source) :
                this.originalFunction.symbol.pkgID.toString()
                        + "/" + getFunctionSource(this.originalFunction.symbol.source);
        BLangAssignment bLangAssignment2 = ASTBuilderUtil.createAssignmentStmt(
                bLangPackage.pos,
                generateFieldBasedAccess("functionToMockPackage"),
                generateRHSExpr(functionToMockPackageVal));

        // The following synthesizes the equivalent of :
        // `BLangReturn Statement <retType> test:MockHandler(<MockFunctionObj>, [<args?>])`
        BLangReturn blangReturn =
                ASTBuilderUtil.createReturnStmt(bLangPackage.pos, generateTypeConversionExpression());

        List<BLangStatement> statements = new ArrayList<>();

        statements.add(bLangAssignment1);
        statements.add(bLangAssignment2);
        statements.add(blangReturn);

        return statements;
    }

    private String getFunctionSource(String source) {
        return source.replaceAll(".bal", "");
    }

    // This function synthesizes the Ballerina equivalent of : `<mockobj>.functionToMock =`
    private BLangFieldBasedAccess generateFieldBasedAccess(String fieldName) {
        BLangVariableReference expr = getMockFunctionReference();
        BLangIdentifier field = ASTBuilderUtil.createIdentifier(bLangPackage.pos, fieldName);

        BLangFieldBasedAccess bLangFieldBasedAccess = ASTBuilderUtil.createFieldAccessExpr(expr, field);

        bLangFieldBasedAccess.fieldKind = FieldKind.SINGLE;
        bLangFieldBasedAccess.originalType = symTable.stringType;
        bLangFieldBasedAccess.lhsVar = true;
        bLangFieldBasedAccess.expectedType = symTable.stringType;
        bLangFieldBasedAccess.type = symTable.stringType;

        return bLangFieldBasedAccess;
    }

    // This function synthesizes the Ballerina equivalent of : `= (functionToMock)`
    private BLangLiteral generateRHSExpr(String val) {
        BType type = symTable.stringType;
        BLangLiteral bLangLiteral = ASTBuilderUtil.createLiteral(bLangPackage.pos, type, val);
        bLangLiteral.expectedType = type;

        return bLangLiteral;
    }

    // This function synthesizes the Ballerina equivalent of : `Return: <type> MockHandler(<mockFnObj>, args)`
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

    // This function synthesizes the Ballerina equivalent of : `test:MockHandler(<mockFnObj>, args)`
    private BLangInvocation generateBLangInvocation() {
        BInvokableSymbol invokableSymbol = getMockHandlerInvokableSymbol();
        List<BLangExpression> argsExprs = generateInvocationArgsExprs();
        List<BLangExpression> requiredArgs = generateInvocationRequiredArgs();
        List<BLangExpression> restArgs = generateInvocationRestArgs();

        BLangInvocation bLangInvocation = ASTBuilderUtil.createInvocationExprForMethod(
                bLangPackage.pos, invokableSymbol, requiredArgs, symResolver);
        bLangInvocation.pkgAlias = (BLangIdentifier) createIdentifier("test");
        bLangInvocation.argExprs = argsExprs;
        bLangInvocation.expectedType = bLangInvocation.type;
        bLangInvocation.restArgs = restArgs;

        return bLangInvocation;
    }

    // This function synthesizes the Ballerina equivalent of : `MockHandler()`
    private BInvokableSymbol getMockHandlerInvokableSymbol() {
        BSymbol testPkg = bLangPackage.getTestablePkg().symbol.scope.lookup(new Name("test")).symbol;

        BInvokableSymbol mockHandlerSymbol =
                (BInvokableSymbol) testPkg.scope.lookup(new Name("mockHandler")).symbol;

        return mockHandlerSymbol;
    }

    // This function synthesizes the Ballerina equivalent of : `(<mockFnObj>, args)`
    private List<BLangExpression> generateInvocationArgsExprs() {
        List<BLangExpression> argExprs = new ArrayList<>();

        BLangSimpleVarRef bLangSimpleVarRef = getMockFunctionReference();
        argExprs.add(bLangSimpleVarRef);

        if (this.originalFunction != null) {
            for (BLangSimpleVariable var : this.originalFunction.requiredParams) {
                bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, var.symbol);
                argExprs.add(bLangSimpleVarRef);
            }

            if (this.originalFunction.restParam != null) {
                bLangSimpleVarRef =
                        ASTBuilderUtil.createVariableRef(bLangPackage.pos, this.originalFunction.restParam.symbol);
                argExprs.add(bLangSimpleVarRef);
            }
        } else {
            if (!this.importFunction.params.isEmpty()) {
                for (BVarSymbol bVarSymbol : this.importFunction.params) {
                    bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, bVarSymbol);
                    argExprs.add(bLangSimpleVarRef);
                }
            }

            if (this.importFunction.restParam != null) {
                bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, this.importFunction.restParam);
                argExprs.add(bLangSimpleVarRef);
            }
        }

        return argExprs;
    }

    // This function synthesizes the Ballerina equivalent of : `(<mockFnObj>`
    private BLangSimpleVarRef getMockFunctionReference() {
        BVarSymbol mockObjectSymbol =
                (BVarSymbol) bLangPackage.getTestablePkg().symbol.scope.lookup(new Name(this.mockFnObjectName)).symbol;
        BLangSimpleVarRef bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, mockObjectSymbol);
        return bLangSimpleVarRef;
    }

    private List<BLangExpression> generateInvocationRequiredArgs() {
        List<BLangExpression> requiredArgs = new ArrayList<>();

        BLangSimpleVarRef bLangSimpleVarRef = getMockFunctionReference();
        requiredArgs.add(bLangSimpleVarRef);

        if (this.originalFunction != null) {
            for (BLangSimpleVariable var : this.originalFunction.requiredParams) {
                bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, var.symbol);
                requiredArgs.add(bLangSimpleVarRef);
            }
        } else {
            if (!this.importFunction.params.isEmpty()) {
                for (BVarSymbol bVarSymbol : this.importFunction.params) {
                    bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, bVarSymbol);
                    requiredArgs.add(bLangSimpleVarRef);
                }
            }
        }

        return requiredArgs;
    }

    private List<BLangExpression> generateInvocationRestArgs() {
        List<BLangExpression> restArgs = new ArrayList<>();
        BLangSimpleVarRef bLangSimpleVarRef = null;

        if (this.originalFunction != null) {
            if (this.originalFunction.requiredParams != null) {
                for (BLangSimpleVariable var : this.originalFunction.requiredParams) {
                    bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, var.symbol);
                    restArgs.add(bLangSimpleVarRef);
                }
            }

            if (this.originalFunction.restParam != null) {
                bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos,
                                                                     this.originalFunction.restParam.symbol);
                restArgs.add(bLangSimpleVarRef);
            }

        } else {
            if (!this.importFunction.params.isEmpty()) {

                for (BVarSymbol bVarSymbol : this.importFunction.params) {
                    bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, bVarSymbol);
                    restArgs.add(bLangSimpleVarRef);
                }
            }

            if (this.importFunction.restParam != null) {
                bLangSimpleVarRef = ASTBuilderUtil.createVariableRef(bLangPackage.pos, this.importFunction.restParam);

                restArgs.add(bLangSimpleVarRef);
            }
        }

        return restArgs;
    }

    private BType generateType() {
        BType type;

        if (originalFunction == null) {
            type = this.importFunction.retType;
        } else {
            type = ((BInvokableType) this.originalFunction.type).retType;
        }

        return type;
    }
}
