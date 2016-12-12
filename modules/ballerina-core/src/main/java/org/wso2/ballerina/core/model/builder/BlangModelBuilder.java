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
package org.wso2.ballerina.core.model.builder;

import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.Identifier;
import org.wso2.ballerina.core.model.Import;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubstractExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.StringValue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

// TODO Change the method names
/**
 * Builds ballerina language object model
 *
 * @since 1.0.0
 */
public class BlangModelBuilder {

    private Stack<CallableUnitGroupBuilder> cUnitGroupBuilderStack = new Stack<>();
    private Stack<CallableUnitBuilder> cUnitBuilderStack = new Stack<>();
    private Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();
    private Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();

    private Queue<Type> typeQueue = new LinkedList<>();
    private Stack<Identifier> identifierStack = new Stack<>();
    private Stack<Expression> exprStack = new Stack<>();

    private BallerinaFile.BFileBuilder bFileBuilder = new BallerinaFile.BFileBuilder();

    public BallerinaFile build() {
        return bFileBuilder.build();
    }

    public void setPackageName(String pkgName) {
        bFileBuilder.setPkgName(pkgName);
    }

    public void addImportPackage(String pkgName) {
        bFileBuilder.addImportPackage(new Import(pkgName));
    }

    // Function parameters and return values
    // -------------------------------------

    public void createParam(String paramName) {
        Parameter param = new Parameter(typeQueue.remove(), new Identifier(paramName));
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.peek();
        callableUnitBuilder.addParameter(param);
    }

    public void createType(String typeName) {
        Type type = TypeC.getType(typeName);
        typeQueue.add(type);
    }

    public void createReturnTypes() {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.peek();
        while (!typeQueue.isEmpty()) {
            callableUnitBuilder.addReturnType(typeQueue.remove());
        }
    }

    // Variable declarations, reference expressions
    // --------------------------------------------

    public void createVariableDcl(String varName) {
        // Create a variable declaration
        VariableDcl variableDcl = new VariableDcl(typeQueue.remove(), new Identifier(varName));

        // Add this variable declaration to the current callable unit
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.peek();
        callableUnitBuilder.addVariableDcl(variableDcl);
    }

    public void createVarIdentifier(String varName) {
        identifierStack.push(new Identifier(varName));
    }

    public void createVarRefExpr() {
        Identifier varName = identifierStack.pop();
        VariableRefExpr variableRefExpr = new VariableRefExpr(varName);
        exprStack.push(variableRefExpr);
    }

    // Expressions
    // -----------

    public void createBinaryExpr(String opStr) {
        Expression rExpr = exprStack.pop();
        Expression lExpr = exprStack.pop();
//        String opStr = ctx.getChild(1).getText();

        BinaryExpression expr;
        switch (opStr) {
            case "+":
                expr = new AddExpression(lExpr, rExpr);
                break;

            case "-":
                expr = new SubstractExpression(lExpr, rExpr);
                break;

            case "*":
                expr = new MultExpression(lExpr, rExpr);
                break;

            case "/":
                throw new RuntimeException("Unsupported operator: " + opStr);

            case "&&":
                expr = new AndExpression(lExpr, rExpr);
                break;

            case "||":
                expr = new OrExpression(lExpr, rExpr);
                break;

            case "==":
                expr = new EqualExpression(lExpr, rExpr);
                break;

            case "!=":
                expr = new NotEqualExpression(lExpr, rExpr);
                break;

            case ">=":
                expr = new GreaterEqualExpression(lExpr, rExpr);
                break;

            case ">":
                expr = new GreaterThanExpression(lExpr, rExpr);
                break;

            case "<":
                expr = new LessThanExpression(lExpr, rExpr);
                break;

            case "<=":
                expr = new LessEqualExpression(lExpr, rExpr);
                break;

            default:
                throw new RuntimeException("Unsupported operator: " + opStr);
        }

        exprStack.push(expr);
    }

    // Functions, Actions and Resources

    public void startCallableUnitBody() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endCallableUnitBody() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();

        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.peek();
        callableUnitBuilder.setBody(blockStmt);
    }

    public void startCallableUnit() {
        cUnitBuilderStack.push(new CallableUnitBuilder());
    }

    public void createFunction(String name, boolean isPublic) {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.pop();
        callableUnitBuilder.setName(new Identifier(name));
        callableUnitBuilder.setPublic(isPublic);

        BallerinaFunction function = callableUnitBuilder.buildFunction();
        bFileBuilder.addFunction(function);
    }

    public void createResource(String name) {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.pop();
        callableUnitBuilder.setName(new Identifier(name));

        Resource resource = callableUnitBuilder.buildResource();
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.peek();
        callableUnitGroupBuilder.addResource(resource);
    }

    public void createAction(String name) {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.pop();
        callableUnitBuilder.setName(new Identifier(name));

        Action action = callableUnitBuilder.buildAction();
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.peek();
        callableUnitGroupBuilder.addAction(action);
    }

    // Services and Connectors

    public void startCallableUnitGroup() {
        cUnitGroupBuilderStack.push(new CallableUnitGroupBuilder());
    }

    public void createService(String name) {
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.pop();
        callableUnitGroupBuilder.setName(new Identifier(name));

        Service service = callableUnitGroupBuilder.buildService();
        bFileBuilder.addService(service);
    }

    public void createConnector(String name) {
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.pop();
        callableUnitGroupBuilder.setName(new Identifier(name));

        Connector connector = callableUnitGroupBuilder.buildConnector();
        bFileBuilder.addConnector(connector);
    }

    // Statements
    // ----------

    public void createAssignmentExpr() {
        VariableRefExpr lExpr = new VariableRefExpr(identifierStack.pop());
        Expression rExpr = exprStack.pop();
        AssignStmt assignStmt = new AssignStmt(lExpr, rExpr);

        addToBlockStmt(assignStmt);
    }

    public void createReturnStmt() {
        ReturnStmt.ReturnStmtBuilder returnStmtBuilder = new ReturnStmt.ReturnStmtBuilder();
        while (!exprStack.isEmpty()) {
            returnStmtBuilder.addExpression(exprStack.pop());
        }

        ReturnStmt returnStmt = returnStmtBuilder.build();
        addToBlockStmt(returnStmt);
    }

    public void startWhileStmt() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endWhileStmt() {
        // Create a while statement builder
        WhileStmt.WhileStmtBuilder whileStmtBuilder = new WhileStmt.WhileStmtBuilder();

        // Get the expression at the top of the expression stack and set it as the while condition
        whileStmtBuilder.setCondition(exprStack.pop());

        // Get the statement block at the top of the block statement stack and set as the while body.
        whileStmtBuilder.setWhileBody(blockStmtBuilderStack.pop().build());

        // Add the while statement to the statement block which is at the top of the stack.
        blockStmtBuilderStack.peek().addStmt(whileStmtBuilder.build());
    }

    public void startIfElseStmt() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
        ifElseStmtBuilderStack.push(new IfElseStmt.IfElseStmtBuilder());
    }

    public void startElseIfClause() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endElseIfClause() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        ifElseStmtBuilder.addElseIfBlock(exprStack.pop(), blockStmtBuilder.build());
    }

    public void startElseClause() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endElseClause() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.peek();
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        ifElseStmtBuilder.setElseBody(blockStmtBuilder.build());
    }

    public void endIfElseStmt() {
        IfElseStmt.IfElseStmtBuilder ifElseStmtBuilder = ifElseStmtBuilderStack.pop();
        ifElseStmtBuilder.setIfCondition(exprStack.pop());

        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        ifElseStmtBuilder.setThenBody(blockStmtBuilder.build());

        IfElseStmt ifElseStmt = ifElseStmtBuilder.build();
        addToBlockStmt(ifElseStmt);
    }

    // Literal Values
    // --------------

    public void createIntegerLiteral(String value) {
        BValue bValue = new IntValue(Integer.parseInt(value));
        exprStack.push(new BasicLiteral(new BValueRef(bValue)));
    }

    public void createFloatLiteral(String value) {
        BValue bValue = new FloatValue(Float.parseFloat(value));
        exprStack.push(new BasicLiteral(new BValueRef(bValue)));
    }

    public void createStringLiteral(String value) {
        BValue bValue = new StringValue(value);
        exprStack.push(new BasicLiteral(new BValueRef(bValue)));
    }

    public void createBooleanLiteral(String value) {
        BValue bValue = new BooleanValue(Boolean.parseBoolean(value));
        exprStack.push(new BasicLiteral(new BValueRef(bValue)));
    }

    public void createNullLiteral(String value) {
        throw new RuntimeException("Null values are not yet supported in Ballerina");
    }

    // Private methods
    // ---------------

    private void addToBlockStmt(Statement stmt) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.peek();
        blockStmtBuilder.addStmt(stmt);
    }
}
