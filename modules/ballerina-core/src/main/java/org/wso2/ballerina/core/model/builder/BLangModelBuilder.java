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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.ParserException;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Position;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BackquoteExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.KeyValueExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValueType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code BLangModelBuilder} provides an high-level API to create Ballerina language object model.
 *
 * @since 1.0.0
 */
public class BLangModelBuilder {

    private String pkgName;

    private BallerinaFile.BFileBuilder bFileBuilder = new BallerinaFile.BFileBuilder();

    // Builds connectors and services.
    private CallableUnitGroupBuilder currentCUGroupBuilder;

    // Builds functions, actions and resources.
    private CallableUnitBuilder currentCUBuilder;

    private Stack<Annotation.AnnotationBuilder> annotationBuilderStack = new Stack<>();
    private Stack<BlockStmt.BlockStmtBuilder> blockStmtBuilderStack = new Stack<>();

    private Stack<IfElseStmt.IfElseStmtBuilder> ifElseStmtBuilderStack = new Stack<>();
    private Queue<BType> typeQueue = new LinkedList<>();
    private Stack<String> pkgNameStack = new Stack<>();
    private Stack<SymbolName> symbolNameStack = new Stack<>();
    private Stack<Expression> exprStack = new Stack<>();

    private Stack<KeyValueExpression> keyValueStack = new Stack<>();

    private static final Logger log = LoggerFactory.getLogger(BLangModelBuilder.class);


    // Holds ExpressionLists required for return statements, function/action invocations and connector declarations
    private Stack<List<Expression>> exprListStack = new Stack<>();

    private Stack<List<Annotation>> annotationListStack = new Stack<>();

    private Stack<List<KeyValueExpression>> mapInitKeyValueListStack = new Stack<>();

    public BallerinaFile build() {
        return bFileBuilder.build();
    }

    // Identifiers

    public void createSymbolName(String name) {
        if (pkgNameStack.isEmpty()) {
            symbolNameStack.push(new SymbolName(name));
        } else {
            symbolNameStack.push(new SymbolName(name, pkgNameStack.pop()));
        }
    }

    public void createSymbolName(String connectorName, String actionName) {
        SymbolName symbolName;
        if (pkgNameStack.isEmpty()) {
            symbolName = new SymbolName(actionName);
        } else {
            symbolName = new SymbolName(actionName, pkgNameStack.pop());
        }

        symbolName.setConnectorName(connectorName);
        symbolNameStack.push(symbolName);
    }

    // Packages and import packages

    public void createPackageName(String pkgName) {
        pkgNameStack.push(pkgName);
    }

    public void createPackageDcl() {
        pkgName = getPkgName();
        bFileBuilder.setPkgName(pkgName);
    }

    public void addImportPackage(String pkgName) {
        String pkgPath = getPkgName();

        if (pkgName != null) {
            bFileBuilder.addImportPackage(new ImportPackage(pkgPath, pkgName));
        } else {
            bFileBuilder.addImportPackage(new ImportPackage(pkgPath));
        }
    }

    // Annotations

    public void createInstanceCreaterExpr(String typeName) {
        InstanceCreationExpr expression = new InstanceCreationExpr(null);
        BType type = BTypes.getType(typeName);
        expression.setType(type);
        exprStack.push(expression);

    }

    public void startAnnotation() {
        annotationBuilderStack.push(new Annotation.AnnotationBuilder());
    }

    public void createAnnotationKeyValue(String key) {
        //        // Assuming the annotation value is a string literal
        //        String value = exprStack.pop().getBValueRef().getString();
        //
        //        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.peek();
        //        annotationBuilder.addKeyValuePair(new Identifier(key), value);

        log.warn("Key/Value pairs in annotations are not supported");
    }

    public void endAnnotation(String name, boolean valueAvailable) {
        Annotation.AnnotationBuilder annotationBuilder = annotationBuilderStack.pop();
        annotationBuilder.setName(new SymbolName(name));

        if (valueAvailable) {
            Expression expr = exprStack.pop();

            // Assuming the annotation value is a string literal
            if (expr instanceof BasicLiteral && expr.getType() == BTypes.STRING_TYPE) {
                String value = ((BasicLiteral) expr).getBValue().stringValue();
                annotationBuilder.setValue(value);
            } else {
                throw new RuntimeException("Annotations with key/value pars are not support at the moment");
            }
        }

        List<Annotation> annotationList = annotationListStack.peek();
        annotationList.add(annotationBuilder.build());
    }


    // Function parameters and types

    /**
     * Create a function parameter and a corresponding variable reference expression
     * <p/>
     * Set the even function to get the value from the function arguments with the correct index.
     * Store the reference in the symbol table.
     *
     * @param paramName name of the function parameter
     */
    public void createParam(String paramName) {
        //        paramIndex++;

        SymbolName paramNameId = new SymbolName(paramName);
        BType paramType = typeQueue.remove();
        Parameter param = new Parameter(paramType, paramNameId);

        if (currentCUBuilder != null) {
            // Add the parameter to callableUnitBuilder.
            currentCUBuilder.addParameter(param);
        } else {
            currentCUGroupBuilder.addParameter(param);
        }
    }

    public void createType(String typeName) {
        BType type = BTypes.getType(typeName);
        typeQueue.add(type);
    }

    public void createArrayType(String typeName) {
        BType type = BTypes.getArrayType(typeName);
        typeQueue.add(type);
    }

    public void registerConnectorType(String typeName) {
        //TODO: We might have to do this through a symbol table in the future
        BTypes.addConnectorType(typeName);
    }

    public void createReturnTypes() {
        while (!typeQueue.isEmpty()) {
            currentCUBuilder.addReturnType(typeQueue.remove());
        }
    }


    // Variable declarations, reference expressions

    public void createConstant(String constName) {
        SymbolName symbolName = new SymbolName(constName);
        BType type = typeQueue.remove();

        Const.ConstBuilder builder = new Const.ConstBuilder();
        builder.setType(type);
        builder.setSymbolName(symbolName);
        builder.setValueExpr(exprStack.pop());

        Const constant = builder.build();
        bFileBuilder.addConst(constant);
    }

    public void createVariableDcl(String varName) {
        // Create a variable declaration
        SymbolName localVarId = new SymbolName(varName);
        BType localVarType = typeQueue.remove();
        VariableDcl variableDcl = new VariableDcl(localVarType, localVarId);

        // Add this variable declaration to the current callable unit or callable unit group
        if (currentCUBuilder != null) {
            // This connector declaration should added to the relevant function/action or resource
            currentCUBuilder.addVariableDcl(variableDcl);
        } else {
            currentCUGroupBuilder.addVariableDcl(variableDcl);
        }

    }

    public void createConnectorDcl(String varName) {
        // Here we build the object model for the following line
        // ballerina.net.http:HTTPConnector nyseEP = new ballerina.net.http:HTTPConnector("http://..", 100);

        // Here we need to pop the symbolName stack twice as the connector name appears twice in the declaration.
        if (symbolNameStack.size() < 2) {
            IllegalStateException ex = new IllegalStateException("symbol stack size should be " +
                    "greater than or equal to two");
            throw new ParserException("Failed to parse connector declaration", ex);
        }

        symbolNameStack.pop();
        SymbolName cSymName = symbolNameStack.pop();
        List<Expression> exprList = exprListStack.pop();

        ConnectorDcl.ConnectorDclBuilder builder = new ConnectorDcl.ConnectorDclBuilder();
        builder.setConnectorName(cSymName);
        builder.setVarName(new SymbolName(varName));
        builder.setExprList(exprList);

        ConnectorDcl connectorDcl = builder.build();
        if (currentCUBuilder != null) {
            // This connector declaration should added to the relevant function/action or resource
            currentCUBuilder.addConnectorDcl(connectorDcl);
        } else {
            currentCUGroupBuilder.addConnectorDcl(connectorDcl);
        }
    }

    /**
     * Create variable reference expression
     * <p/>
     * There are three types of variables references as per the grammar file.
     * 1) Simple variable references. a, b, index etc
     * 2) Map or array access a[1], m["key"]
     * 3) Struct field access  Person.name
     */
    public void createVarRefExpr(String varName) {
        SymbolName symName = new SymbolName(varName);

        VariableRefExpr variableRefExpr = new VariableRefExpr(symName);
        exprStack.push(variableRefExpr);
    }

    public void createMapArrayVarRefExpr(String varName) {
        SymbolName symName = new SymbolName(varName);
        Expression indexExpr = exprStack.pop();
        VariableRefExpr arrayVarRefExpr = new VariableRefExpr(symName);

        ArrayMapAccessExpr.ArrayMapAccessExprBuilder builder = new ArrayMapAccessExpr.ArrayMapAccessExprBuilder();
        builder.setVarName(symName);
        builder.setIndexExpr(indexExpr);
        builder.setArrayMapVarRefExpr(arrayVarRefExpr);

        ArrayMapAccessExpr accessExpr = builder.build();
        exprStack.push(accessExpr);
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
                throw new ParserException("Unsupported operator: " + opStr);

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
                throw new ParserException("Unsupported operator: " + opStr);
        }

        exprStack.push(expr);
    }

    public void createBackquoteExpr(String stringContent) {
        String templateStr = getValueWithinBackquote(stringContent);

        BackquoteExpr.BackquoteExprBuilder builder = new BackquoteExpr.BackquoteExprBuilder();
        builder.setTemplateStr(templateStr);

        exprStack.push(builder.build());
    }

    public void startExprList() {
        exprListStack.push(new ArrayList<>());
    }

    public void endExprList(int exprCount) {
        List<Expression> exprList = exprListStack.peek();
        addExprToList(exprList, exprCount);
    }

    public void createFunctionInvocationExpr(Position invokedLocation) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setExpressionList(exprListStack.pop());
        cIExprBuilder.setName(symbolNameStack.pop());

        FunctionInvocationExpr invocationExpr = cIExprBuilder.buildFuncInvocExpr();
        invocationExpr.setInvokedLocation(invokedLocation);
        exprStack.push(invocationExpr);
    }

    public void createActionInvocationExpr(Position invokedLocation) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setExpressionList(exprListStack.pop());
        cIExprBuilder.setName(symbolNameStack.pop());

        ActionInvocationExpr invocationExpr = cIExprBuilder.buildActionInvocExpr();
        invocationExpr.setInvokedLocation(invokedLocation);
        exprStack.push(invocationExpr);
    }

    public void createArrayInitExpr() {
        ArrayInitExpr.ArrayInitExprBuilder builder = new ArrayInitExpr.ArrayInitExprBuilder();

        if (!exprListStack.isEmpty()) {
            List<Expression> argList = exprListStack.pop();
            builder.setArgList(argList);
        }

        ArrayInitExpr arrayInitExpr = builder.build();
        exprStack.push(arrayInitExpr);
    }

    public void createMapInitExpr() {
        MapInitExpr.MapInitExprBuilder builder = new MapInitExpr.MapInitExprBuilder();

        if (!mapInitKeyValueListStack.isEmpty()) {
            List<KeyValueExpression> argList = mapInitKeyValueListStack.pop();
            builder.setArgList(argList);
        }

        MapInitExpr mapInitExpr = builder.build();
        exprStack.push(mapInitExpr);
    }

    public void startMapInitKeyValue() {
        mapInitKeyValueListStack.push(new ArrayList<>());
    }

    public void endMapInitKeyValue(int exprCount) {
        List<KeyValueExpression> keyValueList = mapInitKeyValueListStack.peek();
        addKeyValueToList(keyValueList, exprCount);
    }

    public void createMapInitKeyValue(String key) {
        if (!exprStack.isEmpty()) {
            Expression currentExpression = exprStack.pop();
            keyValueStack.push(new KeyValueExpression(key, currentExpression));
        } else {
            keyValueStack.push(new KeyValueExpression(key, null));
        }


    }

    // Functions, Actions and Resources

    public void startCallableUnitBody() {
        blockStmtBuilderStack.push(new BlockStmt.BlockStmtBuilder());
    }

    public void endCallableUnitBody() {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.pop();
        BlockStmt blockStmt = blockStmtBuilder.build();

        currentCUBuilder.setBody(blockStmt);
    }

    public void startCallableUnit() {
        currentCUBuilder = new CallableUnitBuilder();
        annotationListStack.push(new ArrayList<>());
    }

    public void createFunction(String name, boolean isPublic, Position location, int position) {
        currentCUBuilder.setName(new SymbolName(name, pkgName));
        currentCUBuilder.setPublic(isPublic);
        currentCUBuilder.setPosition(location);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaFunction function = currentCUBuilder.buildFunction();
        function.setRelativePosition(position);
        bFileBuilder.addFunction(function);

        currentCUBuilder = null;
    }

    public void createResource(String name, Position location) {
        currentCUBuilder.setName(new SymbolName(name, pkgName));
        currentCUBuilder.setPosition(location);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUBuilder::addAnnotation);

        Resource resource = currentCUBuilder.buildResource();
        currentCUGroupBuilder.addResource(resource);

        currentCUBuilder = null;
    }

    public void createAction(String name, Position location) {
        currentCUBuilder.setName(new SymbolName(name, pkgName));
//        currentCUBuilder.setPosition(location);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUBuilder::addAnnotation);

        BallerinaAction action = currentCUBuilder.buildAction();
        currentCUGroupBuilder.addAction(action);

        currentCUBuilder = null;
    }

    // Services and Connectors

    public void startCallableUnitGroup() {
        currentCUGroupBuilder = new CallableUnitGroupBuilder();
        annotationListStack.push(new ArrayList<>());
    }

    public void createService(String name, Position location, int position) {
        currentCUGroupBuilder.setName(new SymbolName(name, pkgName));
        currentCUGroupBuilder.setLocation(location);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        Service service = currentCUGroupBuilder.buildService();
        service.setRelativePosition(position);
        bFileBuilder.addService(service);

        currentCUGroupBuilder = null;
    }

    public void createConnector(String name, Position location, int position) {
        currentCUGroupBuilder.setName(new SymbolName(name, pkgName));
        currentCUGroupBuilder.setLocation(location);

        List<Annotation> annotationList = annotationListStack.pop();
        // TODO Improve this implementation
        annotationList.forEach(currentCUGroupBuilder::addAnnotation);

        BallerinaConnector connector = currentCUGroupBuilder.buildConnector();
        connector.setRelativePosition(position);
        bFileBuilder.addConnector(connector);

        currentCUGroupBuilder = null;
    }

    // Statements

    public void createAssignmentExpr() {
        Expression rExpr = exprStack.pop();
        Expression lExpr = exprStack.pop();

        AssignStmt assignStmt = new AssignStmt(lExpr, rExpr);
        addToBlockStmt(assignStmt);
    }

    public void createReturnStmt() {
        ReturnStmt.ReturnStmtBuilder returnStmtBuilder = new ReturnStmt.ReturnStmtBuilder();

        // Get the expression list from the expression list stack
        if (!exprListStack.isEmpty()) {
            // Return statement with empty expression list.
            // Just a return statement
            returnStmtBuilder.setExpressionList(exprListStack.pop());
        }

        ReturnStmt returnStmt = returnStmtBuilder.build();
        addToBlockStmt(returnStmt);
    }

    public void createReplyStmt() {
        ReplyStmt.ReplyStmtBuilder replyStmtBuilder = new ReplyStmt.ReplyStmtBuilder();
        replyStmtBuilder.setExpression(exprStack.pop());
        ReplyStmt replyStmt = replyStmtBuilder.build();
        addToBlockStmt(replyStmt);
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

    public void createFunctionInvocationStmt(Position invokedLocation) {
        CallableUnitInvocationExprBuilder cIExprBuilder = new CallableUnitInvocationExprBuilder();
        cIExprBuilder.setExpressionList(exprListStack.pop());
        cIExprBuilder.setName(symbolNameStack.pop());

        FunctionInvocationExpr invocationExpr = cIExprBuilder.buildFuncInvocExpr();
        invocationExpr.setInvokedLocation(invokedLocation);

        FunctionInvocationStmt.FunctionInvokeStmtBuilder stmtBuilder =
                new FunctionInvocationStmt.FunctionInvokeStmtBuilder();
        stmtBuilder.setFunctionInvocationExpr(invocationExpr);
        FunctionInvocationStmt functionInvocationStmt = stmtBuilder.build();

        blockStmtBuilderStack.peek().addStmt(functionInvocationStmt);
    }

    // Literal Values

    public void createIntegerLiteral(String value) {
        BValueType bValue = new BInteger(Integer.parseInt(value));
        createLiteral(bValue, BTypes.INT_TYPE);
    }

    public void createFloatLiteral(String value) {
        BValueType bValue = new BFloat(Float.parseFloat(value));
        createLiteral(bValue, BTypes.FLOAT_TYPE);
    }

    public void createStringLiteral(String value) {
        BValueType bValue = new BString(value);
        createLiteral(bValue, BTypes.STRING_TYPE);
    }

    public void createBooleanLiteral(String value) {
        BValueType bValue = new BBoolean(Boolean.parseBoolean(value));
        createLiteral(bValue, BTypes.BOOLEAN_TYPE);
    }

    public void createNullLiteral(String value) {
        throw new RuntimeException("Null values are not yet supported in Ballerina");
    }

    // Private methods

    private void addToBlockStmt(Statement stmt) {
        BlockStmt.BlockStmtBuilder blockStmtBuilder = blockStmtBuilderStack.peek();
        blockStmtBuilder.addStmt(stmt);
    }

    private void createLiteral(BValueType bValueType, BType type) {
        BasicLiteral basicLiteral = new BasicLiteral(bValueType);
        basicLiteral.setType(type);
        exprStack.push(basicLiteral);
    }

    /**
     * @param exprList List<Expression>
     * @param n        number of expression to be added the given list
     */
    private void addExprToList(List<Expression> exprList, int n) {

        if (exprStack.isEmpty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
        }

        if (n == 1) {
            Expression expr = exprStack.pop();
            exprList.add(expr);
        } else {
            Expression expr = exprStack.pop();
            addExprToList(exprList, n - 1);
            exprList.add(expr);
        }
    }

    /**
     * @param keyValueDataHolderList List<KeyValueDataHolder>
     * @param n        number of expression to be added the given list
     */
    private void addKeyValueToList(List<KeyValueExpression> keyValueDataHolderList, int n) {

        if (keyValueStack.isEmpty()) {
            throw new IllegalStateException("KeyValue stack cannot be empty in processing a KeyValueList");
        }

        if (n == 1) {
            KeyValueExpression keyValue = keyValueStack.pop();
            keyValueDataHolderList.add(keyValue);
        } else {
            KeyValueExpression keyValue = keyValueStack.pop();
            addKeyValueToList(keyValueDataHolderList, n - 1);
            keyValueDataHolderList.add(keyValue);
        }
    }

    private String getPkgName() {
        if (pkgNameStack.isEmpty()) {
            throw new IllegalStateException("Package name stack is empty");
        }

        return pkgNameStack.pop();
    }

    /**
     * return value within double quotes
     *
     * @param inputString string with double quotes
     * @return value
     */
    private static String getValueWithinBackquote(String inputString) {
        Pattern p = Pattern.compile("`([^`]*)`");
        Matcher m = p.matcher(inputString);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

}
