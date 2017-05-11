/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import BallerinaASTFactory from './ballerina-ast-factory';

/**
 * @class DefaultBallerinaASTFactory
 * @lends DefaultBallerinaASTFactory
 */
var DefaultBallerinaASTFactory = {};

/**
 * creates ServiceDefinition
 * @param args
 */
DefaultBallerinaASTFactory.createServiceDefinition = function (args) {
    let serviceDef = BallerinaASTFactory.createServiceDefinition(args);

    // Creating the ServiceInfo annotation.
    let serviceInfoAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http.swagger',
        packageName: 'swagger',
        identifier: 'ServiceInfo'
    });
    let annotationEntryForInfoTitle = BallerinaASTFactory.createAnnotationEntry({leftValue: 'title', rightValue: '\"Sample Service\"'});
    let annotationEntryForInfoVersion = BallerinaASTFactory.createAnnotationEntry({leftValue: 'version', rightValue: '\"1.0.0\"'});
    serviceInfoAnnotation.addChild(annotationEntryForInfoTitle);
    serviceInfoAnnotation.addChild(annotationEntryForInfoVersion);
    serviceDef.addChild(serviceInfoAnnotation);

    // Creating the Swagger annotation
    let swaggerAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http.swagger',
        packageName: 'swagger',
        identifier: 'Swagger'
    });
    let annotationEntryForSwaggerVersion = BallerinaASTFactory.createAnnotationEntry({leftValue: 'version', rightValue: '\"2.0\"'});
    swaggerAnnotation.addChild(annotationEntryForSwaggerVersion);
    serviceDef.addChild(swaggerAnnotation);

    // Creating BasePath annotation.
    let annotationEntryForBasePath = BallerinaASTFactory.createAnnotationEntry({leftValue: 'value', rightValue: '\"/\"'});
    let basePathAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http',
        packageName: 'http',
        identifier: 'BasePath'
    });
    basePathAnnotation.addChild(annotationEntryForBasePath);
    serviceDef.addChild(basePathAnnotation);

    let resourceDef = DefaultBallerinaASTFactory.createResourceDefinition(args);
    serviceDef.addChild(resourceDef);
    return serviceDef;
};

/**
 * creates ResourceDefinition
 * @param args
 */
DefaultBallerinaASTFactory.createResourceDefinition = function (args) {
    let resourceDef = BallerinaASTFactory.createResourceDefinition(args);

    // Creating GET http method annotation.
    let getHttpMethodAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http',
        packageName: 'http',
        identifier: 'GET',
        uniqueIdentifier: 'httpMethod'
    });
    resourceDef.addChild(getHttpMethodAnnotation, 0);

    // Creating path annotation.
    let pathAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http',
        packageName: 'http',
        identifier: 'Path'
    });
    let annotationEntryForPathValue = BallerinaASTFactory.createAnnotationEntry({leftValue: 'value', rightValue: '\"/\"'});
    pathAnnotation.addChild(annotationEntryForPathValue);
    resourceDef.addChild(pathAnnotation, 1);

    let resourceArg = BallerinaASTFactory.createResourceParameter();
    resourceArg.setBType('message');
    resourceArg.setIdentifier('m');
    resourceDef.addChild(resourceArg);

    let responsesAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http.swagger',
        packageName: 'swagger',
        identifier: 'Responses'
    });

    // Creating the responses array entry
    let responsesAnnotationArray = BallerinaASTFactory.createAnnotationEntryArray();
    let responseAnnotationEntry = BallerinaASTFactory.createAnnotationEntry({rightValue: responsesAnnotationArray});
    responsesAnnotation.addChild(responseAnnotationEntry);

    // Creating default response
    let responseAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http.swagger',
        packageName: 'swagger',
        identifier: 'Response'
    });
    responsesAnnotationArray.addChild(BallerinaASTFactory.createAnnotationEntry({leftValue: '', rightValue: responseAnnotation}));
    let responsesDefaultAnnotationCodeEntryValue = BallerinaASTFactory.createAnnotationEntry({leftValue: 'code', rightValue: '\"default\"'});
    responseAnnotation.addChild(responsesDefaultAnnotationCodeEntryValue);
    let responsesDefaultAnnotationDescriptionEntryValue = BallerinaASTFactory.createAnnotationEntry({leftValue: 'description', rightValue: '\"Default Response\"'});
    responseAnnotation.addChild(responsesDefaultAnnotationDescriptionEntryValue);

    resourceDef.addChild(responsesAnnotation, 2);

    return resourceDef;
};

    /**
     * creates ConnectorDefinition
     * @param args
     */
    DefaultBallerinaASTFactory.createConnectorDefinition = function (args) {
        var connectorDef = BallerinaASTFactory.createConnectorDefinition(args);
        connectorDef.addArgument('message', 'm');
        return connectorDef;
    };

    /**
     * creates ConnectorAction
     * @param args
     */
    DefaultBallerinaASTFactory.createConnectorAction = function (args) {
        var actionDef = BallerinaASTFactory.createConnectorAction(args);
        actionDef.addArgument('message', 'm');
        return actionDef;
    };

/**
 * Creates a variable definition statement with default values.
 * @param {Object} [args] - Args for creating a variable definition statement.
 * @return {VariableDefinitionStatement} - New variable definition statement.
 *
 * @see {@link VariableDefinitionStatement}
 */
DefaultBallerinaASTFactory.createVariableDefinitionStatement = function (args) {
    var variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement(args);
    variableDefinitionStatement.setLeftExpression('int i');
    variableDefinitionStatement.setRightExpression('0');
    return variableDefinitionStatement;
};

/**
 * creates typeMapperDefinition with default statement
 * @param {Object} args - object for typeMapperDefinition creation
 * @returns {TypeMapperDefinition}
 */
DefaultBallerinaASTFactory.createTypeMapperDefinition = function (args) {
    var typeMapperDefinition = BallerinaASTFactory.createTypeMapperDefinition(args);
    var blockStatement = BallerinaASTFactory.createBlockStatement(args);
    var returnStatement = BallerinaASTFactory.createReturnStatement(args);
    var variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement(args);
    var rightOperandExpression = BallerinaASTFactory.createRightOperandExpression(args);
    var referenceTypeInitiExpression = BallerinaASTFactory.createReferenceTypeInitExpression(args);

    rightOperandExpression.addChild(referenceTypeInitiExpression);

    var returnStatementVariableReferenceExpression = BallerinaASTFactory.createVariableReferenceExpression(args);
    returnStatement.addChild(returnStatementVariableReferenceExpression);

    variableDefinitionStatement.addChild(rightOperandExpression);

    blockStatement.addChild(variableDefinitionStatement);
    blockStatement.addChild(returnStatement);
    typeMapperDefinition.addChild(blockStatement);
    return typeMapperDefinition;
};


/**
 * Create the action invocation statement for action invocation
 * @param args
 * @returns {ActionInvocationStatement}
 */
DefaultBallerinaASTFactory.createAggregatedActionInvocationStatement = function (args) {
    var actionInStmt = BallerinaASTFactory.createActionInvocationStatement(args);
    var actionInExp = BallerinaASTFactory.createActionInvocationExpression(args);
    actionInStmt.addChild(actionInExp);
    return actionInStmt;
};

/**
 * Create the particular assignment statement for the action invocation
 * @param args
 * @returns {AssignmentStatement}
 */
DefaultBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement = function (args) {
    var assignmentStmt = BallerinaASTFactory.createAssignmentStatement(args);
    var leftOp = BallerinaASTFactory.createLeftOperandExpression(args);
    var rightOp = BallerinaASTFactory.createRightOperandExpression(args);
    var actionInExp = BallerinaASTFactory.createActionInvocationExpression(args);
    rightOp.addChild(actionInExp);
    rightOp.setRightOperandExpressionString(actionInExp.getExpression());
    assignmentStmt.addChild(leftOp);
    assignmentStmt.addChild(rightOp);
    return assignmentStmt;
};

/**
 * creates TryCatchStatement
 * @param args
 */
DefaultBallerinaASTFactory.createTryCatchStatement = function (args) {
    var tryCatchStatement = BallerinaASTFactory.createTryCatchStatement(args);
    var tryStatement = BallerinaASTFactory.createTryStatement(args);
    tryCatchStatement.addChild(tryStatement);
    var catchStatement = BallerinaASTFactory.createCatchStatement(args);
    tryCatchStatement.addChild(catchStatement);
    return tryCatchStatement;
};

/**
 * creates ThrowStatement
 * @param {Object} args - Arguments for creating a new throw statement.
 * @returns {ThrowStatement}
 */
DefaultBallerinaASTFactory.createThrowStatement = function (args) {
    var throwStatement = BallerinaASTFactory.createThrowStatement(args);
    throwStatement.addChild(BallerinaASTFactory.createVariableReferenceExpression({variableName:"e"}));
    return throwStatement;
};

/**
 * creates MainFunctionDefinition
 * @param args
 */
DefaultBallerinaASTFactory.createMainFunctionDefinition = function (args) {
    var functionDefinition = BallerinaASTFactory.createFunctionDefinition(args);
    functionDefinition.setFunctionName('main');
    functionDefinition.addArgument('string[]', 'args');
    return functionDefinition;
};

/* Create the particular assignment statement for the function invocation
 * @param args
 * @returns {AssignmentStatement}
 */
DefaultBallerinaASTFactory.createAggregatedFunctionInvocationExpression = function (args) {
    var assignmentStmt = BallerinaASTFactory.createAssignmentStatement(args);
    var leftOp = BallerinaASTFactory.createLeftOperandExpression(args);
    var rightOp = BallerinaASTFactory.createRightOperandExpression(args);
    var functionInExp = BallerinaASTFactory.createFunctionInvocationExpression(args);
    rightOp.addChild(functionInExp);
    rightOp.setRightOperandExpressionString(functionInExp.getExpression());
    assignmentStmt.addChild(leftOp);
    assignmentStmt.addChild(rightOp);
    return assignmentStmt;
};

/**
 * creates Aggregated AssignmentStatement
 * @param {Object} args
 * @returns {AssignmentStatement}
 */
DefaultBallerinaASTFactory.createAggregatedAssignmentStatement = function (args) {
    var assignmentStmt = BallerinaASTFactory.createAssignmentStatement(args);
    var leftOperand = BallerinaASTFactory.createLeftOperandExpression(args);
    leftOperand.setLeftOperandExpressionString('a');
    var rightOperand = BallerinaASTFactory.createRightOperandExpression(args);
    rightOperand.setRightOperandExpressionString('b');
    assignmentStmt.addChild(leftOperand);
    assignmentStmt.addChild(rightOperand);
    return assignmentStmt;
};

/**
 * creates FunctionInvocationStatement
 * @param args
 * @returns {FunctionInvocation}
 */
DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement = function (args) {
    var funcInvocationStatement = BallerinaASTFactory.createFunctionInvocationStatement(args);
    var funcInvocationExpression = BallerinaASTFactory.createFunctionInvocationExpression(args);
    funcInvocationStatement.addChild(funcInvocationExpression);
    return funcInvocationStatement;
};

export default DefaultBallerinaASTFactory;
