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
import FragmentUtils from '../utils/fragment-utils';
import _ from 'lodash';

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
    let resourceDef = DefaultBallerinaASTFactory.createResourceDefinition(args);
    serviceDef.addChild(resourceDef);
    return serviceDef;
};

DefaultBallerinaASTFactory.createForkJoinStatement = function (args) {
    const forkJoinStatement = BallerinaASTFactory.createForkJoinStatement(args);
    const joinStatement = BallerinaASTFactory.createJoinStatement();
    const worker1Declaration = BallerinaASTFactory.createWorkerDeclaration();
    const worker2Declaration = BallerinaASTFactory.createWorkerDeclaration();
    worker1Declaration.setWorkerName('forkWorker1');
    worker2Declaration.setWorkerName('forkWorker2');
    forkJoinStatement.addChild(joinStatement);
    forkJoinStatement.addChild(worker1Declaration);
    forkJoinStatement.addChild(worker2Declaration);
    return forkJoinStatement;
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

    let parameterDef = BallerinaASTFactory.createParameterDefinition(args);
    parameterDef.setTypeName('message');
    parameterDef.setName('m');

    let argumentParameterDefinitionHolder = BallerinaASTFactory.createArgumentParameterDefinitionHolder();
    argumentParameterDefinitionHolder.addChild(parameterDef);
    resourceDef.addChild(argumentParameterDefinitionHolder);

    let responsesAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http.swagger',
        packageName: 'swagger',
        identifier: 'Responses'
    });

    // Creating the responses array entry
    let responsesAnnotationArray = BallerinaASTFactory.createAnnotationEntryArray();
    let responseAnnotationEntry = BallerinaASTFactory.createAnnotationEntry({rightValue: responsesAnnotationArray});
    responsesAnnotation.addChild(responseAnnotationEntry);
    return resourceDef;
};

/**
 * creates ConnectorDefinition
 * @param args
 */
DefaultBallerinaASTFactory.createConnectorDefinition = function (args) {
    let connectorDef = BallerinaASTFactory.createConnectorDefinition(args);
    connectorDef.addArgument('message', 'm');
    return connectorDef;
};

/**
 * creates ConnectorAction
 * @param args
 */
DefaultBallerinaASTFactory.createConnectorAction = function (args) {
    let actionDef = BallerinaASTFactory.createConnectorAction(args);
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
    let variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement(args);
    variableDefinitionStatement.setStatementFromString('int i = 0')
    return variableDefinitionStatement;
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
    var assignmentStatementString = 'm = ' + args.actionPackageName + ':' +
        args.actionConnectorName + '.' + args.action + '()';
    var assignmentStatement = BallerinaASTFactory.createAssignmentStatement();
    assignmentStatement.setStatementFromString(assignmentStatementString);
    return assignmentStatement;
};

/**
 * creates TryCatchStatement
 * @param args
 */
DefaultBallerinaASTFactory.createTryCatchStatement = function (args) {
    var tryCatchStatement = BallerinaASTFactory.createTryCatchStatement(args);
    tryCatchStatement.setStatementFromString('try{}catch(exception e){}');
    return tryCatchStatement;
};

/**
 * creates ThrowStatement
 * @param {Object} args - Arguments for creating a new throw statement.
 * @returns {ThrowStatement}
 */
DefaultBallerinaASTFactory.createThrowStatement = function (args) {
    var throwStatement = BallerinaASTFactory.createThrowStatement(args);
    throwStatement.setStatementFromString('throw e');
    return throwStatement;
};

/**
 * create an abort statement.
 * @param {object} args - arguments for creating a new throw statement.
 * @return {AbortStatement}
 * */
DefaultBallerinaASTFactory.createAbortStatement = function (args) {
    return BallerinaASTFactory.createAbortStatement(args);
};

/**
 * create TransactionAborted Statement.
 * @param {object} args - argument for creating a new TransactionAborted statement.
 * @return {TransactionAbortedStatement}
 * */
DefaultBallerinaASTFactory.createTransactionAbortedStatement = function (args) {
    var transactionAbortedStatement = BallerinaASTFactory.createTransactionAbortedStatement(args);
    transactionAbortedStatement.setStatementFromString('transaction {} aborted {}');
    return transactionAbortedStatement;
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

/**Create the particular assignment statement for the function invocation
 * @param args
 * @returns {AssignmentStatement}
 */
DefaultBallerinaASTFactory.createAggregatedFunctionInvocationExpression = function (args) {
    var assignmentStmt = BallerinaASTFactory.createAssignmentStatement(args);
    var leftOp = BallerinaASTFactory.createLeftOperandExpression(args);
    var rightOp = BallerinaASTFactory.createRightOperandExpression(args);
    var functionInExp = BallerinaASTFactory.createFunctionInvocationExpression(args);
    rightOp.addChild(functionInExp);
    rightOp.setExpressionFromString(functionInExp.getExpression());
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
    let fragment = FragmentUtils.createStatementFragment('a = b;');
    let parsedJson = FragmentUtils.parseFragment(fragment);
    if ((!_.has(parsedJson, 'error')
           || !_.has(parsedJson, 'syntax_errors'))
           && _.isEqual(parsedJson.type, 'assignment_statement')) {
        let node = BallerinaASTFactory.createFromJson(parsedJson);
        node.initFromJson(parsedJson);
        node.whiteSpace.useDefault = true;
        return node;
    }
    return BallerinaASTFactory.createAssignmentStatement();
};

/**
 * creates FunctionInvocationStatement
 * @param args
 * @returns {FunctionInvocationStatement}
 */
DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement = function (args) {
    var funcInvocationStatement = BallerinaASTFactory.createFunctionInvocationStatement(args);
    var funcInvocationExpression = BallerinaASTFactory.createFunctionInvocationExpression(args);
    funcInvocationStatement.addChild(funcInvocationExpression);
    return funcInvocationStatement;
};

/**
 * creates WorkerInvocationStatement
 * @param args
 * @returns {WorkerInvocationStatement}
 */
DefaultBallerinaASTFactory.createWorkerInvocationStatement = function (args) {
    var workerInvocationStatement = BallerinaASTFactory.createWorkerInvocationStatement();
    workerInvocationStatement.setStatementFromString('m -> workerName');
    return workerInvocationStatement;
};

/**
 * creates workerReplyStatement
 * @param args
 * @returns WorkerReplyStatement}
 */
DefaultBallerinaASTFactory.createWorkerReplyStatement = function (args) {
    var workerReplyStatement = BallerinaASTFactory.createWorkerReplyStatement();
    workerReplyStatement.setStatementFromString('m <- workerName');
    return workerReplyStatement;
};

export default DefaultBallerinaASTFactory;
