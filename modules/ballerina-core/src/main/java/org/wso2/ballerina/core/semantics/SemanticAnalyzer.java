/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.semantics;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.SymTable;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.ResourceInvoker;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayAccessExpr;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryArithmeticExpression;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.BinaryLogicalExpression;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.ArrayType;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.util.LangModelUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code SemanticAnalyzer} analyzes semantic properties of a Ballerina program
 *
 * @since 1.0.0
 */
public class SemanticAnalyzer implements NodeVisitor {

    private int stackFrameOffset = -1;

    // TODO Check the possibility of passing this symbol table as constructor parameter to this class.
    // TODO Need to pass the global scope
    private SymTable symbolTable;

    private BallerinaFile bFile;

    // We need to keep a map of import packages.
    // This is useful when analyzing import functions, actions and types.
    private Map<String, ImportPackage> importPkgMap = new HashMap<>();

    public SemanticAnalyzer(BallerinaFile bFile) {
        this.bFile = bFile;
        symbolTable = new SymTable(bFile.getPackageScope());
    }

    @Override
    public void visit(BallerinaFile bFile) {
        for (ImportPackage importPkg : bFile.getImportPackages()) {
            importPkg.accept(this);
        }

        for (Service service : bFile.getServices()) {
            service.accept(this);
        }

        for (Function function : bFile.getFunctions().values()) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.accept(this);
        }
    }

    @Override
    public void visit(ImportPackage importPkg) {
        if (importPkgMap.containsKey(importPkg.getName())) {
            throw new RuntimeException("Duplicate import package declaration: " + importPkg.getPath());
        }

        importPkgMap.put(importPkg.getName(), importPkg);
    }

    @Override
    public void visit(Service service) {
        // Visit the set of resources in a service
        for (Resource resource : service.getResources()) {
            resource.accept(this);
        }
    }

    @Override
    public void visit(BallerinaConnector connector) {

    }

    @Override
    public void visit(Resource resource) {
        // Visit the contents within a resource
        // Open a new symbol scope
        openScope();

        // TODO create a Symbol for this function( with parameter and return types)

        Parameter[] parameters = resource.getParameters();
        for (Parameter parameter : parameters) {
            stackFrameOffset++;
            visit(parameter);
        }

        VariableDcl[] variableDcls = resource.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            stackFrameOffset++;
            visit(variableDcl);
        }

        ConnectorDcl[] connectorDcls = resource.getConnectorDcls();
        for (ConnectorDcl connectorDcl : connectorDcls) {
            stackFrameOffset++;
            visit(connectorDcl);
        }

        BlockStmt blockStmt = resource.getResourceBody();
        blockStmt.accept(this);

        int sizeOfStackFrame = stackFrameOffset + 1;
        resource.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        closeScope();

    }

    @Override
    public void visit(BallerinaFunction bFunction) {
        // Create a Symbol for this function( with parameter and return types)
        addFuncSymbol(bFunction);

        // Open a new symbol scope
        openScope();

        Parameter[] parameters = bFunction.getParameters();
        for (Parameter parameter : parameters) {
            stackFrameOffset++;
            visit(parameter);
        }

        VariableDcl[] variableDcls = bFunction.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            stackFrameOffset++;
            visit(variableDcl);
        }

        BlockStmt blockStmt = bFunction.getFunctionBody();
        blockStmt.accept(this);

        // Here we need to calculate size of the BValue array which will be created in the stack frame
        // Values in the stack frame are stored in the following order.
        // -- Parameter values --
        // -- Local var values --
        // -- Temp values      --
        // -- Return values    --
        // These temp values are results of intermediate expression evaluations.
        int sizeOfStackFrame = stackFrameOffset + 1;
        bFunction.setStackFrameSize(sizeOfStackFrame);

        // Close the symbol scope
        closeScope();
    }

    @Override
    public void visit(BallerinaAction action) {

    }

    @Override
    public void visit(Worker worker) {

    }

    @Override
    public void visit(Annotation annotation) {

    }

    @Override
    public void visit(Parameter parameter) {
        SymbolName symName = parameter.getName();

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null) {
            throw new SemanticException("Duplicate parameter name: " + symName.getName());
        }

        TypeC type = parameter.getTypeC();
        symbol = new Symbol(type, stackFrameOffset);

        symbolTable.insert(symName, symbol);

    }

    @Override
    public void visit(VariableDcl variableDcl) {
        SymbolName symName = variableDcl.getName();

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null) {
            throw new SemanticException("Duplicate variable declaration with name: " + symName.getName());
        }

        TypeC type = variableDcl.getTypeC();
        symbol = new Symbol(type, stackFrameOffset);

        symbolTable.insert(symName, symbol);
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {
        SymbolName symbolName = connectorDcl.getVarName();

        Symbol symbol = symbolTable.lookup(symbolName);
        if (symbol != null) {
            throw new SemanticException("Duplicate connector declaration with name: " + symbolName.getName());
        }

        symbol = new Symbol(TypeC.CONNECTOR_TYPE, stackFrameOffset);
        symbolTable.insert(symbolName, symbol);

        // Setting the connector name with the package name
        SymbolName connectorName = connectorDcl.getConnectorName();
        String pkgPath = getPackagePath(connectorName);
        connectorName = LangModelUtils.getConnectorSymName(connectorName.getName(), pkgPath);
        connectorDcl.setConnectorName(connectorName);

    }

    @Override
    public void visit(AssignStmt assignStmt) {
        Expression rExpr = assignStmt.getRExpr();
        if (rExpr instanceof ActionInvocationExpr) {
            assignStmt.setHaltExecution(true);

        }
        rExpr.accept(this);

        Expression lExpr = assignStmt.getLExpr();
        lExpr.accept(this);
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression expr = ifElseStmt.getCondition();
        expr.accept(this);

        if (expr.getType() != TypeC.BOOLEAN_TYPE) {
            throw new SemanticException("Incompatible types: expected a boolean expression");
        }

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);

            if (elseIfCondition.getType() != TypeC.BOOLEAN_TYPE) {
                throw new SemanticException("Incompatible types: expected a boolean expression");
            }

            Statement elseIfBody = elseIfBlock.getElseIfBody();
            elseIfBody.accept(this);
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression expr = whileStmt.getCondition();
        expr.accept(this);

        if (expr.getType() != TypeC.BOOLEAN_TYPE) {
            throw new SemanticException("Incompatible types: expected a boolean expression");
        }

        BlockStmt blockStmt = whileStmt.getBody();
        if (blockStmt.getStatements().length == 0) {
            // This can be optimized later to skip the while statement
            throw new SemanticException("No statements in the while loop");
        }

        blockStmt.accept(this);
    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        replyStmt.getReplyExpr().accept(this);
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        Expression[] exprs = returnStmt.getExprs();

        for (Expression expr : exprs) {
            expr.accept(this);
        }
    }

    // Expressions

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        visitExpr(instanceCreationExpr);

        // TODO here the type shouldn't be a value type

//        Expression expr = instanceCreationExpr.getRExpr();
//        expr.accept(this);
    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        visitExpr(funcIExpr);

        Expression[] exprs = funcIExpr.getExprs();
        for (Expression expr : exprs) {
            expr.accept(this);
        }

        // Can we do this bit in the linker
        SymbolName symbolName = funcIExpr.getFunctionName();
        String pkgPath = getPackagePath(symbolName);

        TypeC[] paramTypes = new TypeC[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        symbolName = LangModelUtils.getSymNameWithParams(symbolName.getName(), pkgPath, paramTypes);
        funcIExpr.setFunctionName(symbolName);

        bFile.addFuncInvocationExpr(funcIExpr);

        // TODO store the types of each func argument expression
        // Implement semantic analysis for function invocations

        // Identify the package of the function to be invoked.


    }

    // TODO Duplicate code. fix me
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        visitExpr(actionIExpr);

        Expression[] exprs = actionIExpr.getExprs();
        for (Expression expr : exprs) {
            expr.accept(this);
        }

        // TODO Check whether first argument is of type Connector (with connector name). e.g. HttpConnector

        // Can we do this bit in the linker
        SymbolName symName = actionIExpr.getActionName();
        if (symName.getConnectorName() == null) {
            throw new SemanticException("Connector type is not associated with the action invocation");
        }

        String pkgPath = getPackagePath(symName);

        TypeC[] paramTypes = new TypeC[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        symName = LangModelUtils.getActionSymName(symName.getName(), symName.getConnectorName(),
                pkgPath, paramTypes);
        actionIExpr.setActionName(symName);

        bFile.addActionIExpr(actionIExpr);
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {

    }

    @Override
    public void visit(AddExpression addExpr) {
        TypeC arithmeticExprType = verifyBinaryArithmeticExprType(addExpr);

        // We need to find a better implementation than this.
        if (arithmeticExprType == TypeC.INT_TYPE) {
            addExpr.setEvalFunc(AddExpression.ADD_INT_FUNC);

        } else if (arithmeticExprType == TypeC.STRING_TYPE) {
            addExpr.setEvalFunc(AddExpression.ADD_STRING_FUNC);

        } else {
            throw new SemanticException("Add operation is not supported for type: " + arithmeticExprType);
        }
    }

    @Override
    public void visit(MultExpression multExpr) {
        TypeC binaryExprType = verifyBinaryArithmeticExprType(multExpr);
        if (binaryExprType == TypeC.INT_TYPE) {
            multExpr.setEvalFunc(MultExpression.MULT_INT_FUNC);
        } else {
            throw new SemanticException("Mult operation is not supported for type: " + binaryExprType);
        }
    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        TypeC binaryExprType = verifyBinaryArithmeticExprType(subtractExpr);
        if (binaryExprType == TypeC.INT_TYPE) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_INT_FUNC);
        } else {
            throw new SemanticException("Subtraction operation is not supported for type: " + binaryExprType);
        }
    }

    @Override
    public void visit(AndExpression andExpr) {
        visitBinaryLogicalExpr(andExpr);
        andExpr.setEvalFunc(AndExpression.AND_FUNC);
    }

    @Override
    public void visit(OrExpression orExpr) {
        visitBinaryLogicalExpr(orExpr);
        orExpr.setEvalFunc(OrExpression.OR_FUNC);
    }

    @Override
    public void visit(EqualExpression expr) {
        TypeC compareExprType = verifyBinaryCompareExprType(expr);

        if (compareExprType == TypeC.INT_TYPE) {
            expr.setEvalFunc(EqualExpression.EQUAL_INT_FUNC);
        } else if (compareExprType == TypeC.STRING_TYPE) {
            expr.setType(TypeC.BOOLEAN_TYPE);
            expr.setEvalFunc(EqualExpression.EQUAL_STRING_FUNC);
        } else {
            throw new SemanticException("Equals operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(notEqualExpr);

        if (compareExprType == TypeC.INT_TYPE) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_INT_FUNC);
        } else if (compareExprType == TypeC.STRING_TYPE) {
            notEqualExpr.setEvalFunc(NotEqualExpression.NOT_EQUAL_STRING_FUNC);
        } else {
            throw new SemanticException("NotEqual operation is not supported for type: " + compareExprType);
        }
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(greaterEqualExpr);

        if (compareExprType == TypeC.INT_TYPE) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_INT_FUNC);
        } else {
            throw new SemanticException("Greater than equal operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(greaterThanExpr);

        if (compareExprType == TypeC.INT_TYPE) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_INT_FUNC);
        } else {
            throw new SemanticException("Greater than operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(lessEqualExpr);
        if (compareExprType == TypeC.INT_TYPE) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_INT_FUNC);
        } else {
            throw new SemanticException("Less than equal operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(lessThanExpr);
        if (compareExprType == TypeC.INT_TYPE) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_INT_FUNC);
        } else {
            throw new SemanticException("Less than operation is not supported for type: " + compareExprType);
        }
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        SymbolName varName = variableRefExpr.getSymbolName();

        // Check whether this symName is declared
        Symbol symbol = getSymbol(varName);

        variableRefExpr.setType(symbol.getType());
        variableRefExpr.setOffset(symbol.getOffset());
    }

    @Override
    public void visit(ArrayAccessExpr arrayAccessExpr) {
        visitExpr(arrayAccessExpr);

        SymbolName arrayVarName = arrayAccessExpr.getSymbolName();

        // Check whether this symName is declared
        Symbol symbol = getSymbol(arrayVarName);

        // Type returned by the symbol should always be the ArrayType
        if (!(symbol.getType() instanceof ArrayType)) {
            throw new SemanticException("Attempt to index non-array variable: " + arrayVarName.getName());
        }

        arrayAccessExpr.setType(symbol.getType());

        Expression indexExpr = arrayAccessExpr.getRExpr();
        indexExpr.accept(this);

        if (indexExpr.getType() != TypeC.INT_TYPE) {
            throw new SemanticException("Array index should be of type int, not " + indexExpr.getType().toString() +
                    ". Array name: " + arrayVarName.getName());
        }
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
    }

    public void visit(ResourceInvoker resourceInvoker) {}


    // Private methods.

    private void openScope() {
        symbolTable.openScope();
    }

    private void closeScope() {
        stackFrameOffset = -1;
        symbolTable.closeScope();
    }

    private void visitBinaryExpr(BinaryExpression expr) {
        visitExpr(expr);

        expr.getRExpr().accept(this);
        expr.getLExpr().accept(this);
    }

    private void visitExpr(Expression expr) {
        stackFrameOffset++;
        expr.setOffset(stackFrameOffset);
    }

    private void addFuncSymbol(Function function) {
        SymbolName symbolName = LangModelUtils.getSymNameWithParams(function.getName(), function.getParameters());
        function.setSymbolName(symbolName);

        TypeC[] paramTypes = LangModelUtils.getTypesOfParams(function.getParameters());

        Symbol symbol = new Symbol(function, paramTypes, function.getReturnTypesC());

        if (symbolTable.lookup(symbolName) != null) {
            throw new SemanticException("Duplicate function definition: " + symbolName.getName());
        }
        symbolTable.insert(symbolName, symbol);
    }

    private TypeC verifyBinaryArithmeticExprType(BinaryArithmeticExpression binaryArithmeticExpr) {
        TypeC typeC = verifyBinaryExprType(binaryArithmeticExpr);
        binaryArithmeticExpr.setType(typeC);
        return typeC;
    }

    private TypeC verifyBinaryCompareExprType(BinaryExpression binaryExpression) {
        TypeC typeC = verifyBinaryExprType(binaryExpression);
        binaryExpression.setType(TypeC.BOOLEAN_TYPE);
        return typeC;
    }

    private TypeC verifyBinaryExprType(BinaryExpression binaryExpr) {
        visitBinaryExpr(binaryExpr);

        Expression rExpr = binaryExpr.getRExpr();
        Expression lExpr = binaryExpr.getLExpr();

        if (lExpr.getType() != rExpr.getType()) {
            throw new BallerinaException(
                    "Incompatible types in binary expression: " + lExpr.getType() + " vs "
                            + rExpr.getType());
        }

        return lExpr.getType();
    }

    private void visitBinaryLogicalExpr(BinaryLogicalExpression expr) {
        visitBinaryExpr(expr);

        Expression rExpr = expr.getRExpr();
        Expression lExpr = expr.getLExpr();

        if (lExpr.getType() == TypeC.BOOLEAN_TYPE && rExpr.getType() == TypeC.BOOLEAN_TYPE) {
            expr.setType(TypeC.BOOLEAN_TYPE);
        } else {
            throw new BallerinaException("Incompatible types used for '&&' operator");
        }
    }

    private String getPackagePath(SymbolName symbolName) {
        // Extract the package name from the function name.
        // Function name should be in one of the following formats
        //      1)  sayHello                        ->  No package name. must be a function in the same package.
        //      2)  hello:sayHello                  ->  Function is defined in the 'hello' package.  User must have
        //                                              added import declaration. 'import wso2.connector.hello'.
        //      3)  wso2.connector.hello:sayHello   ->  Function is defined in the wso2.connector.hello package.

        // First check whether there is a packaged name attached to the function.
        String pkgPath = null;
        String pkgName = symbolName.getPkgName();

        if (pkgName != null) {
            // A package name is specified. Check whether it is already listed as an imported package.
            ImportPackage importPkg = importPkgMap.get(pkgName);

            if (importPkg != null) {
                // Found the imported package of the pkgName.
                // Retrieve the package path
                pkgPath = importPkg.getPath();

            } else {
                // Package name is not listed in the imported packages.
                // User may have used the fully qualified package path.
                // If this package is not available, linker will throw an error.
                pkgPath = pkgName;
            }
        }
        return pkgPath;
    }

    private Symbol getSymbol(SymbolName symName) {
        // Check whether this symName is declared
        Symbol symbol = symbolTable.lookup(symName);

        if (symbol == null) {
            throw new SemanticException("Undeclared variable: " + symName.getName());
        }

        return symbol;
    }
}
