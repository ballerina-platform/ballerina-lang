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

import org.wso2.ballerina.core.interpreter.SymTable;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryArithmeticExpression;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
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
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.util.SymbolUtils;

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

    public SemanticAnalyzer(BallerinaFile bFile) {
        this.bFile = bFile;
        symbolTable = new SymTable(bFile.getPackageScope());
    }

    @Override
    public void visit(BallerinaFile bFile) {
        for (Service service : bFile.getServices()) {
            service.accept(this);
        }

        for (Function function : bFile.getFunctions().values()) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.accept(this);
        }
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
            throw new InvalidSemanticException("Duplicate parameter name: " + symName.getName());
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
            throw new InvalidSemanticException("Duplicate variable declaration with name: " + symName.getName());
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
            throw new InvalidSemanticException("Duplicate connector declaration with name: " + symbolName.getName());
        }

        symbol = new Symbol(TypeC.CONNECTOR_TYPE, stackFrameOffset);
        symbolTable.insert(symbolName, symbol);

    }

    @Override
    public void visit(AssignStmt assignStmt) {
        Expression rExpr = assignStmt.getRExpr();
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
            throw new InvalidSemanticException("Incompatible types: expected a boolean expression");
        }

        Statement thenBody = ifElseStmt.getThenBody();
        thenBody.accept(this);

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);

            if (elseIfCondition.getType() != TypeC.BOOLEAN_TYPE) {
                throw new InvalidSemanticException("Incompatible types: expected a boolean expression");
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
            throw new InvalidSemanticException("Incompatible types: expected a boolean expression");
        }

        BlockStmt blockStmt = whileStmt.getBody();
        if (blockStmt.getStatements().length == 0) {
            // This can be optimized later to skip the while statement
            throw new InvalidSemanticException("No statements in the while loop");
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
    public void visit(BasicLiteral basicLiteral) {
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
            throw new InvalidSemanticException("Add operation is not supported for type: " + arithmeticExprType);
        }
    }

    @Override
    public void visit(AndExpression andExpr) {
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
            throw new InvalidSemanticException("Equals operation is not supported for type: "
                    + compareExprType);
        }
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

        TypeC[] paramTypes = new TypeC[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        symbolName = SymbolUtils.generateSymbolName(symbolName.getName(), paramTypes);
        funcIExpr.setFunctionName(symbolName);

        bFile.addFuncInvocationExpr(funcIExpr);

        // TODO store the types of each func argument expression
    }

    // TODO Duplicate code. fix me
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        visitExpr(actionIExpr);

        Expression[] exprs = actionIExpr.getExprs();
        for (Expression expr : exprs) {
            expr.accept(this);
        }

        // Can we do this bit in the linker
        SymbolName symbolName = actionIExpr.getActionName();

        TypeC[] paramTypes = new TypeC[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            paramTypes[i] = exprs[i].getType();
        }

        symbolName = SymbolUtils.generateSymbolName(symbolName.getName(), paramTypes);
        actionIExpr.setActionName(symbolName);

        bFile.addActionIExpr(actionIExpr);
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(greaterEqualExpr);

        if (compareExprType == TypeC.INT_TYPE) {
            greaterEqualExpr.setEvalFunc(GreaterEqualExpression.GREATER_EQUAL_INT_FUNC);
        } else {
            throw new InvalidSemanticException("Greater than equal operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(greaterThanExpr);

        if (compareExprType == TypeC.INT_TYPE) {
            greaterThanExpr.setEvalFunc(GreaterThanExpression.GREATER_THAN_INT_FUNC);
        } else {
            throw new InvalidSemanticException("Greater than operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(lessEqualExpr);
        if (compareExprType == TypeC.INT_TYPE) {
            lessEqualExpr.setEvalFunc(LessEqualExpression.LESS_EQUAL_INT_FUNC);
        } else {
            throw new InvalidSemanticException("Less than equal operation is not supported for type: "
                    + compareExprType);
        }
    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {
        TypeC compareExprType = verifyBinaryCompareExprType(lessThanExpr);
        if (compareExprType == TypeC.INT_TYPE) {
            lessThanExpr.setEvalFunc(LessThanExpression.LESS_THAN_INT_FUNC);
        } else {
            throw new InvalidSemanticException("Less than operation is not supported for type: " + compareExprType);
        }
    }

    @Override
    public void visit(MultExpression multExpr) {
        TypeC binaryExprType = verifyBinaryArithmeticExprType(multExpr);
        if (binaryExprType == TypeC.INT_TYPE) {
            multExpr.setEvalFunc(MultExpression.MULT_INT_FUNC);
        } else {
            throw new InvalidSemanticException("Mult operation is not supported for type: " + binaryExprType);
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
            throw new InvalidSemanticException("NotEqual operation is not supported for type: " + compareExprType);
        }
    }

    @Override
    public void visit(OrExpression orExpr) {

    }

    @Override
    public void visit(SubtractExpression subtractExpr) {
        TypeC binaryExprType = verifyBinaryArithmeticExprType(subtractExpr);
        if (binaryExprType == TypeC.INT_TYPE) {
            subtractExpr.setEvalFunc(SubtractExpression.SUB_INT_FUNC);
        } else {
            throw new InvalidSemanticException("Subtraction operation is not supported for type: " + binaryExprType);
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpr) {

    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        SymbolName symName = variableRefExpr.getSymbolName();
        // Check whether this symName is declared

        Symbol symbol = symbolTable.lookup(symName);
        // Then set the type correctly..
        if (symbol == null) {
            throw new InvalidSemanticException("Undeclared variable: " + symName.getName());
        }

        //        symName.setSymbol(symbol);
        variableRefExpr.setType(symbol.getType());
        variableRefExpr.setOffset(symbol.getOffset());
    }

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
        SymbolName symbolName = SymbolUtils.generateSymbolName(function.getName(), function.getParameters());
        function.setSymbolName(symbolName);

        TypeC[] paramTypes = SymbolUtils.getTypesOfParams(function.getParameters());

        Symbol symbol = new Symbol(function, paramTypes, function.getReturnTypesC());
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
            throw new RuntimeException(
                    "Incompatible types in binary arithmetic  expression: " + lExpr.getType() + " vs "
                            + rExpr.getType());
        }
        return lExpr.getType();

    }


}
