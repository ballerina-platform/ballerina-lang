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
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Connector;
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
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
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
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.TypeC;

/**
 *
 */
public class SemanticAnalyzer implements NodeVisitor {

    private int localVarOffset = -1;
    private int exprValOffset = -1;

    // TODO Check the possibility of passing this symbol table as constructor parameter to this class.
    // TODO Need to pass the global scope
    private SymTable symbolTable = new SymTable();

    @Override
    public void visit(BallerinaFile file) {

        for (Service service : file.getServices()) {
            service.visit(this);
        }

        for (Function function : file.getFunctions().values()) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.visit(this);
        }
    }

    @Override
    public void visit(Service service) {
        ;
    }

    @Override
    public void visit(Connector connector) {

    }

    @Override
    public void visit(Resource resource) {

    }

    @Override
    public void visit(BallerinaFunction bFunction) {
        // Open a new symbol scope
        openScope();

        // TODO create a Symbol for this function( with parameter and return types)

        Parameter[] parameters = bFunction.getParameters();
        for (Parameter parameter : parameters) {
            localVarOffset++;
            visit(parameter);
        }

        VariableDcl[] variableDcls = bFunction.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            localVarOffset++;
            visit(variableDcl);
        }


        BlockStmt blockStmt = bFunction.getFunctionBody();
        blockStmt.visit(this);

        // Close the symbol scope
        closeScope();
    }

    @Override
    public void visit(Action action) {

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
            throw new RuntimeException("Duplicate parameter name: " + symName.getName());
        }

        TypeC type = parameter.getTypeC();
        symbol = new Symbol(type, localVarOffset);
        symName.setSymbol(symbol);

        symbolTable.insert(symName, symbol);

    }

    @Override
    public void visit(VariableDcl variableDcl) {
        SymbolName symName = variableDcl.getName();

        Symbol symbol = symbolTable.lookup(symName);
        if (symbol != null) {
            throw new RuntimeException("Duplicate variable declaration with name: " + symName.getName());
        }

        TypeC type = variableDcl.getTypeC();
        symbol = new Symbol(type, localVarOffset);
        symName.setSymbol(symbol);

        symbolTable.insert(symName, symbol);
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {

    }

    @Override
    public void visit(AssignStmt assignStmt) {
        Expression rExpr = assignStmt.getRExpr();
        rExpr.visit(this);

        Expression lExpr = assignStmt.getLExpr();
        lExpr.visit(this);
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        for (Statement stmt : blockStmt.getStatements()) {
            stmt.visit(this);
        }
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {

    }

    @Override
    public void visit(ReplyStmt replyStmt) {

    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        Expression[] exprs = returnStmt.getExprs();

        for (Expression expr : exprs) {
            expr.visit(this);
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {

    }

    @Override
    public void visit(AddExpression addExpr) {
        exprValOffset++;
        addExpr.setOffset(exprValOffset);

        Expression rExpr = addExpr.getRExpr();
        rExpr.visit(this);

        Expression lExpr = addExpr.getLExpr();
        lExpr.visit(this);

        if (lExpr.getType() != rExpr.getType()) {
            throw new RuntimeException("Incompatible types in the add expression: " + lExpr.getType()
                    + " vs " + rExpr.getType());
        }

        // We need to find a better implementation than this.
        if (lExpr.getType() == TypeC.INT_TYPE) {
            addExpr.setType(TypeC.INT_TYPE);
            addExpr.setEvalFunc(AddExpression.ADD_INT_FUNC_NEW);

//        }
//        else if (lExpr.getType() == TypeC.LONG_TYPE) {
//            addExpr.setType(TypeC.LONG_TYPE);
//            addExpr.setEvalFunc(AddExpression.ADD_LONG_FUNC);
//
//        } else if (lExpr.getType() == TypeC.FLOAT_TYPE) {
//            addExpr.setType(TypeC.FLOAT_TYPE);
//            addExpr.setEvalFunc(AddExpression.ADD_FLOAT_FUNC);
//
//        } else if (lExpr.getType() == TypeC.DOUBLE_TYPE) {
//            addExpr.setType(TypeC.DOUBLE_TYPE);
//            addExpr.setEvalFunc(AddExpression.ADD_DOUBLE_FUNC);
//
//        } else if (lExpr.getType() == TypeC.STRING_TYPE) {
//            addExpr.setType(TypeC.STRING_TYPE);
//            addExpr.setEvalFunc(AddExpression.ADD_STRING_FUNC);

        } else {
            throw new RuntimeException("Add operation is not supported for type: " + lExpr.getType());
        }
    }

    @Override
    public void visit(AndExpression andExpr) {
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {

    }

    @Override
    public void visit(EqualExpression equalExpr) {

    }

    @Override
    public void visit(FunctionInvocationExpr functionInvocationExpr) {

    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpr) {

    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpr) {

    }

    @Override
    public void visit(LessEqualExpression lessEqualExpr) {

    }

    @Override
    public void visit(LessThanExpression lessThanExpr) {

    }

    @Override
    public void visit(MultExpression multExpr) {

    }

    @Override
    public void visit(NotEqualExpression notEqualExpr) {

    }

    @Override
    public void visit(OrExpression orExpr) {

    }

    @Override
    public void visit(SubtractExpression subtractExpr) {

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
            throw new RuntimeException("Undeclared variable: " + symName.getName());
        }

        symName.setSymbol(symbol);
        variableRefExpr.setType(symbol.getType());
        variableRefExpr.setOffset(symbol.getOffset());
//        variableRefExpr.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(symbol.getOffset()));
    }

    private void openScope() {
        localVarOffset = -1;
        exprValOffset = -1;
        symbolTable.openScope();
    }

    private void closeScope() {
        localVarOffset = -1;
        exprValOffset = -1;
        symbolTable.closeScope();
    }
}
