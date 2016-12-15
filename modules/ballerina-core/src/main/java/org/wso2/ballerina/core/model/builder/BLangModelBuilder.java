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
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.Import;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
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
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.StringValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

// TODO Change the method names

/**
 * Builds ballerina language object model
 *
 * @since 1.0.0
 */
public class BLangModelBuilder {

    private Stack<CallableUnitGroupBuilder> cUnitGroupBuilderStack = new Stack<>();
    private Stack<CallableUnitBuilder> cUnitBuilderStack = new Stack<>();
    private Stack<Annotation.AnnotationBuilder> annotationBuilderStack = new Stack<>();
    private Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();
    private Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();

    private Queue<TypeC> typeQueue = new LinkedList<>();
    private BallerinaFile.BFileBuilder bFileBuilder = new BallerinaFile.BFileBuilder();
    private Stack<SymbolName> symbolNameStack = new Stack<>();
    private Stack<Expression> exprStack = new Stack<>();

    private Stack<List<Annotation>> annotationListStack = new Stack<>();

    public BallerinaFile build() {
        return bFileBuilder.build();
    }

    // Symbol table related instance variables;
//    private SymbolTable symbolTable = new SymbolTable(null);
//    private int paramIndex = -1;
//    private int localVarIndex = -1;

    // Identifiers

    public void createIdentifier(String varName) {
        symbolNameStack.push(new SymbolName(varName));
    }

    // Packages and import packages

    public void setPackageName(String pkgName) {
        bFileBuilder.setPkgName(pkgName);
    }

    public void addImportPackage(String pkgName) {
        bFileBuilder.addImportPackage(new Import(pkgName));
    }

    // Annotations

    public void startAnnotation() {
        annotationBuilderStack.push(new Annotation.AnnotationBuilder());
    }

    public void createAnnotationKeyValue(String key) {
//        // Assuming the annotation value is a string literal
//        String value = exprStack.pop().getBValueRef().getString();
//
//        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.peek();
//        annotationBuilder.addKeyValuePair(new Identifier(key), value);

        throw new RuntimeException("Key/Value pairs in annotations are not supported");
    }

    public void endAnnotation(String name, boolean valueAvailable) {
        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.pop();
        annotationBuilder.setName(new SymbolName(name));

        if (valueAvailable) {
            // Assuming the annotation value is a string literal
            annotationBuilder.setValue(exprStack.pop().getBValueRef().getString());
        }

        List<Annotation> annotationList = annotationListStack.peek();
        annotationList.add(annotationBuilder.build());
    }


    // Function parameters and return values

    /**
     * Create a function parameter and a corresponding variable reference expression
     * <p>
     * Set the even function to get the value from the function arguments with the correct index.
     * Store the reference in the symbol table.
     *
     * @param paramName name of the function parameter
     */
    public void createParam(String paramName) {
//        paramIndex++;

        SymbolName paramNameId = new SymbolName(paramName, SymbolName.SymType.VARIABLE);
        TypeC paramType = typeQueue.remove();
        Parameter param = new Parameter(paramType, paramNameId);

        // Add the parameter to callableUnitBuilder.
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.peek();
        callableUnitBuilder.addParameter(param);

        // Create variable reference expression and set the proper index to access the value
//        VariableRefExpr variableRefExpr = new VariableRefExpr(paramNameId);
//        variableRefExpr.setEvalFunction(VariableRefExpr.createGetParamValueFunc(paramIndex));

        // Store the variable reference in the symbol table
//        symbolTable.putVarRefExpr(paramNameId, variableRefExpr);
    }

    public void createType(String typeName) {
        TypeC type = TypeC.getTypeC(typeName);
        typeQueue.add(type);
    }

    public void createReturnTypes() {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.peek();
        while (!typeQueue.isEmpty()) {
            callableUnitBuilder.addReturnType(typeQueue.remove());
        }
    }

    // Variable declarations, reference expressions

    public void createVariableDcl(String varName) {
//        localVarIndex++;

        // Create a variable declaration
        SymbolName localVarId = new SymbolName(varName, SymbolName.SymType.VARIABLE);
        TypeC localVarType = typeQueue.remove();
        VariableDcl variableDcl = new VariableDcl(localVarType, localVarId);

        // Add this variable declaration to the current callable unit
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.peek();
        callableUnitBuilder.addVariableDcl(variableDcl);

        // Create variable reference expression and set the proper index to access the value
//        VariableRefExpr variableRefExpr = new VariableRefExpr(localVarId);
//        variableRefExpr.setEvalFunction(VariableRefExpr.createGetLocalValueFunc(localVarIndex));

        // Store the variable reference in the symbol table
//        symbolTable.putVarRefExpr(localVarId, variableRefExpr);
    }

    /**
     * Create variable reference expression
     * <p>
     * This method lookup the symbol table for a variable reference of a function parameter or of a local variable.
     */
    public void createVarRefExpr() {
        SymbolName symName = symbolNameStack.pop();
        symName.setSymType(SymbolName.SymType.VARIABLE);

        VariableRefExpr variableRefExpr = new VariableRefExpr(symName);
        exprStack.push(variableRefExpr);
    }

    // Expressions

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
                expr = new SubtractExpression(lExpr, rExpr);
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
        // Create a new symbol table for the callableUnit scope
//        addScopeToSymbolTable();

        cUnitBuilderStack.push(new CallableUnitBuilder());
        annotationListStack.push(new ArrayList<>());
    }

    public void createFunction(String name, boolean isPublic) {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.pop();
        callableUnitBuilder.setName(new SymbolName(name, SymbolName.SymType.CALLABLE_UNIT));
        callableUnitBuilder.setPublic(isPublic);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(callableUnitBuilder::addAnnotation);

        BallerinaFunction function = callableUnitBuilder.buildFunction();
        bFileBuilder.addFunction(function);

        // Remove the callable unit scope from the symbol table;
//        removeScopeFromSymbolTable();
    }

    public void createResource(String name) {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.pop();
        callableUnitBuilder.setName(new SymbolName(name, SymbolName.SymType.CALLABLE_UNIT));

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(callableUnitBuilder::addAnnotation);

        Resource resource = callableUnitBuilder.buildResource();
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.peek();
        callableUnitGroupBuilder.addResource(resource);

        // Remove the callable unit scope from the symbol table;
//        removeScopeFromSymbolTable();
    }

    public void createAction(String name) {
        CallableUnitBuilder callableUnitBuilder = cUnitBuilderStack.pop();
        callableUnitBuilder.setName(new SymbolName(name, SymbolName.SymType.CALLABLE_UNIT));

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(callableUnitBuilder::addAnnotation);

        Action action = callableUnitBuilder.buildAction();
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.peek();
        callableUnitGroupBuilder.addAction(action);

        // Remove the callable unit scope from the symbol table;
//        removeScopeFromSymbolTable();
    }

    // Services and Connectors

    public void startCallableUnitGroup() {
        // Create a new symbol table for the callableUnitGroup scope
//        addScopeToSymbolTable();

        cUnitGroupBuilderStack.push(new CallableUnitGroupBuilder());
        annotationListStack.push(new ArrayList<>());
    }

    public void createService(String name) {
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.pop();
        callableUnitGroupBuilder.setName(new SymbolName(name, SymbolName.SymType.CALLABLE_UNIT_GROUP));

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(callableUnitGroupBuilder::addAnnotation);

        Service service = callableUnitGroupBuilder.buildService();
        bFileBuilder.addService(service);

        // Remove the callable unit group scope from the symbol table;
//        removeScopeFromSymbolTable();
    }

    public void createConnector(String name) {
        CallableUnitGroupBuilder callableUnitGroupBuilder = cUnitGroupBuilderStack.pop();
        callableUnitGroupBuilder.setName(new SymbolName(name, SymbolName.SymType.CALLABLE_UNIT_GROUP));

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(callableUnitGroupBuilder::addAnnotation);

        Connector connector = callableUnitGroupBuilder.buildConnector();
        bFileBuilder.addConnector(connector);

        // Remove the callable unit group scope from the symbol table;
//        removeScopeFromSymbolTable();
    }

    // Statements

    public void createAssignmentExpr() {
        SymbolName symName = symbolNameStack.pop();
        symName.setSymType(SymbolName.SymType.VARIABLE);

        VariableRefExpr lExpr = new VariableRefExpr(symName);
        Expression rExpr = exprStack.pop();
        AssignStmt assignStmt = new AssignStmt(lExpr, rExpr);

        addToBlockStmt(assignStmt);
    }

    public void createReturnStmt() {
        ReturnStmt.ReturnStmtBuilder returnStmtBuilder = new ReturnStmt.ReturnStmtBuilder();

        if (!exprStack.isEmpty()) {
            addInReverseOrder(returnStmtBuilder);
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

    public void createIntegerLiteral(String value) {
        BValue bValue = new IntValue(Integer.parseInt(value));
        createLiteral(new BValueRef(bValue), TypeC.INT_TYPE);
    }

    public void createFloatLiteral(String value) {
        BValue bValue = new FloatValue(Float.parseFloat(value));
        createLiteral(new BValueRef(bValue), TypeC.FLOAT_TYPE);
    }

    public void createStringLiteral(String value) {
        BValue bValue = new StringValue(value);
        createLiteral(new BValueRef(bValue), TypeC.STRING_TYPE);
    }

    public void createBooleanLiteral(String value) {
        BValue bValue = new BooleanValue(Boolean.parseBoolean(value));
        createLiteral(new BValueRef(bValue), TypeC.BOOLEAN_TYPE);
    }

    public void createNullLiteral(String value) {
        throw new RuntimeException("Null values are not yet supported in Ballerina");
    }

    // Private methods

    private void addToBlockStmt(Statement stmt) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.peek();
        blockStmtBuilder.addStmt(stmt);
    }

    private void addInReverseOrder(ReturnStmt.ReturnStmtBuilder builder) {
        Expression expr = exprStack.pop();
        if (exprStack.isEmpty()) {
            builder.addExpression(expr);
        } else {
            addInReverseOrder(builder);
            builder.addExpression(expr);
        }
    }

    private void createLiteral(BValueRef bValueRef, TypeC type) {
        BasicLiteral basicLiteral = new BasicLiteral(bValueRef);
        basicLiteral.setType(type);
        exprStack.push(basicLiteral);
    }

//    private void addScopeToSymbolTable() {
//        SymbolTable scopeTable = new SymbolTable(this.symbolTable);
//        this.symbolTable = scopeTable;
//    }

//    private void removeScopeFromSymbolTable() {
//        this.symbolTable = symbolTable.getParent();
//        localVarIndex = -1;
//        paramIndex = -1;
//    }
}
