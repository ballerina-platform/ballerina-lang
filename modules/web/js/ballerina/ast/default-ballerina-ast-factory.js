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
import _ from 'lodash';
import BallerinaASTFactory from './ballerina-ast-factory';
import FragmentUtils from '../utils/fragment-utils';
import EnableDefaultWSVisitor from './../visitors/source-gen/enable-default-ws-visitor';

/**
 * @class DefaultBallerinaASTFactory
 * @lends DefaultBallerinaASTFactory
 */
const DefaultBallerinaASTFactory = {};

/**
 * creates ServiceDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} serviceDef
 */
DefaultBallerinaASTFactory.createServiceDefinition = function (args) {
    const serviceDef = BallerinaASTFactory.createServiceDefinition(args);
    const resourceDef = DefaultBallerinaASTFactory.createResourceDefinition(args);
    serviceDef.addChild(resourceDef, undefined, undefined, undefined, true);
    serviceDef.accept(new EnableDefaultWSVisitor());
    return serviceDef;
};

/**
 * Create the default reply statement.
 * @param args
 * @returns {ReplyStatement}
 */
DefaultBallerinaASTFactory.createReplyStatement = function (args) {
    const replyStatement = BallerinaASTFactory.createReplyStatement(args);
    replyStatement.setReplyMessage('m');
    replyStatement.accept(new EnableDefaultWSVisitor());
    return replyStatement;
};

/**
 *  * create Fork Join
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} forkJoinStatement
 */
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
    forkJoinStatement.accept(new EnableDefaultWSVisitor());
    return forkJoinStatement;
};

/**
 * creates ResourceDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} resourceDef
 */
DefaultBallerinaASTFactory.createResourceDefinition = function (args) {
    const resourceDef = BallerinaASTFactory.createResourceDefinition(args);

    // Creating GET http method annotation.
    const getHttpMethodAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http',
        packageName: 'http',
        identifier: 'GET',
        uniqueIdentifier: 'httpMethod',
    });
    resourceDef.addChild(getHttpMethodAnnotation, 0);

    const parameterDef = BallerinaASTFactory.createParameterDefinition(args);
    parameterDef.setTypeName('message');
    parameterDef.setName('m');

    const argumentParameterDefinitionHolder = BallerinaASTFactory.createArgumentParameterDefinitionHolder();
    argumentParameterDefinitionHolder.addChild(parameterDef);
    resourceDef.addChild(argumentParameterDefinitionHolder);

    const replyStatement = DefaultBallerinaASTFactory.createReplyStatement(args);
    resourceDef.addChild(replyStatement);

    const responsesAnnotation = BallerinaASTFactory.createAnnotation({
        fullPackageName: 'ballerina.net.http.swagger',
        packageName: 'swagger',
        identifier: 'Responses',
    });

    // Creating the responses array entry
    const responsesAnnotationArray = BallerinaASTFactory.createAnnotationEntryArray();
    const responseAnnotationEntry = BallerinaASTFactory.createAnnotationEntry({
        rightValue: responsesAnnotationArray,
    });
    responsesAnnotation.addChild(responseAnnotationEntry);
    responsesAnnotation.accept(new EnableDefaultWSVisitor());
    return resourceDef;
};

/**
 * creates ConnectorDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} connectorDef
 */
DefaultBallerinaASTFactory.createConnectorDefinition = function (args) {
    const connectorDef = BallerinaASTFactory.createConnectorDefinition(args);
    connectorDef.addArgument('message', 'm');
    const connectorActionDef = DefaultBallerinaASTFactory.createConnectorAction();
    connectorDef.addChild(connectorActionDef, undefined, undefined, undefined, true);
    connectorDef.accept(new EnableDefaultWSVisitor());
    return connectorDef;
};

/**
 * creates ConnectorAction
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} actionDef
 */
DefaultBallerinaASTFactory.createConnectorAction = function (args) {
    const actionDef = BallerinaASTFactory.createConnectorAction(args);
    actionDef.addArgument('message', 'm');
    actionDef.accept(new EnableDefaultWSVisitor());
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
    const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement(args);
    variableDefinitionStatement.setStatementFromString('int i = 0');
    variableDefinitionStatement.accept(new EnableDefaultWSVisitor());
    return variableDefinitionStatement;
};

/**
 * Create the particular assignment statement for the action invocation
 * @param {object} args - argument to be passed in to factory methods.
 * @returns {AssignmentStatement} assignmentStatement
 */
DefaultBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement = function (args) {
    const actionDefinition = args.actionDefinition;
    let leftOperandExpression = '';
    actionDefinition.getReturnParams().forEach((param, index) => {
        if (index === 0) {
            leftOperandExpression = param.type + ' ' + (param.identifier
                    ? param.identifier : param.type.substr(0, 1));
        }
    });

    const rightOperandExpression = args.actionPackageName + ':' +
        args.actionConnectorName + '.' + args.action + '()';

    const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement();
    variableDefinitionStatement.setStatementFromString(leftOperandExpression + ' = ' + rightOperandExpression);
    variableDefinitionStatement.accept(new EnableDefaultWSVisitor());
    return variableDefinitionStatement;
};

/**
 * creates TryCatchStatement
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} tryCatchStatement
 */
DefaultBallerinaASTFactory.createTryCatchStatement = function (args) {
    const tryCatchStatement = BallerinaASTFactory.createTryCatchStatement(args);
    tryCatchStatement.setStatementFromString('try{}catch(exception e){}');
    tryCatchStatement.accept(new EnableDefaultWSVisitor());
    return tryCatchStatement;
};

/**
 * creates ThrowStatement
 * @param {Object} args - Arguments for creating a new throw statement.
 * @returns {ThrowStatement} throwStatement
 */
DefaultBallerinaASTFactory.createThrowStatement = function (args) {
    const throwStatement = BallerinaASTFactory.createThrowStatement(args);
    throwStatement.setStatementFromString('throw e');
    throwStatement.accept(new EnableDefaultWSVisitor());
    return throwStatement;
};

/**
 * create an abort statement.
 * @param {object} args - arguments for creating a new throw statement.
 * @return {AbortStatement} AbortStatement
 * */
DefaultBallerinaASTFactory.createAbortStatement = function (args) {
    return BallerinaASTFactory.createAbortStatement(args);
};

/**
 * create TransactionAborted Statement.
 * @param {object} args - argument for creating a new TransactionAborted statement.
 * @return {TransactionAbortedStatement} transactionAbortedStatement
 * */
DefaultBallerinaASTFactory.createTransactionAbortedStatement = function (args) {
    const transactionAbortedStatement = BallerinaASTFactory.createTransactionAbortedStatement(args);
    transactionAbortedStatement.setStatementFromString('transaction {} aborted {} committed {}');
    transactionAbortedStatement.accept(new EnableDefaultWSVisitor());
    return transactionAbortedStatement;
};

/**
 * creates MainFunctionDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} functionDefinition
 */
DefaultBallerinaASTFactory.createMainFunctionDefinition = function (args) {
    const functionDefinition = BallerinaASTFactory.createFunctionDefinition(args);
    functionDefinition.setFunctionName('main');
    functionDefinition.addArgument('string[]', 'args');
    functionDefinition.accept(new EnableDefaultWSVisitor());
    return functionDefinition;
};

/**
 * creates Aggregated AssignmentStatement
 * @param {Object} args argument to be passed in to factory methods.
 * @returns {AssignmentStatement} AssignmentStatement
 */
DefaultBallerinaASTFactory.createAggregatedAssignmentStatement = function (args) {
    const fragment = FragmentUtils.createStatementFragment('a = b;');
    const parsedJson = FragmentUtils.parseFragment(fragment);
    if ((!_.has(parsedJson, 'error')
        || !_.has(parsedJson, 'syntax_errors'))
        && _.isEqual(parsedJson.type, 'assignment_statement')) {
        const node = BallerinaASTFactory.createFromJson(parsedJson);
        node.initFromJson(parsedJson);
        node.accept(new EnableDefaultWSVisitor());
        return node;
    }
    return BallerinaASTFactory.createAssignmentStatement(args);
};

/**
 * creates FunctionInvocationStatement
 * @param {object} args - argument to be passed in to factory methods.
 * @returns {FunctionInvocationStatement} funcInvocationStatement
 */
DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement = function (args) {
    const funcInvocationStatement = BallerinaASTFactory.createFunctionInvocationStatement();
    const opts = {
        functionName: _.get(args, 'functionDef._name'),
        packageName: _.get(args, 'packageName'),
        fullPackageName: _.get(args, 'fullPackageName'),
    };
    const funcInvocationExpression = BallerinaASTFactory.createFunctionInvocationExpression(opts);
    if (!_.isNil(args) && _.has(args, 'functionDef')) {
        let functionInvokeString = '';
        if (!_.isNil(args.packageName)) {
            functionInvokeString += args.packageName + ':';
        }
        functionInvokeString += args.functionDef.getName() + '(';
        if (!_.isEmpty(args.functionDef.getParameters())) {
            args.functionDef.getParameters().forEach((param, index) => {
                if (index !== 0) {
                    functionInvokeString += ', ';
                }
                functionInvokeString += param.name;
            });
        }
        functionInvokeString += ')';
        funcInvocationExpression.setExpressionFromString(functionInvokeString);

        // fragment parser does not have access to full package name. Hence, setting it here.
        funcInvocationExpression.setFullPackageName(_.get(args, 'fullPackageName'));

        if (!_.isEmpty(args.functionDef.getReturnParams())) {
            // FIXME : Do a better solution to this by refactoring transform addChild and canDrop
            const variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement();
            let leftOperandExpression = '';
            args.functionDef.getReturnParams().forEach((returnParam, index) => {
                if (index === 0) {
                    leftOperandExpression = returnParam.type + ' ' + (returnParam.identifier
                            ? returnParam.identifier : returnParam.type.substr(0, 1));
                }
            });
            const expression = leftOperandExpression + ' = ' + functionInvokeString;
            variableDefinitionStatement.setStatementFromString(expression);
            variableDefinitionStatement.accept(new EnableDefaultWSVisitor());
            return variableDefinitionStatement;
        }
    }
    funcInvocationStatement.addChild(funcInvocationExpression);
    funcInvocationStatement.accept(new EnableDefaultWSVisitor());
    return funcInvocationStatement;
};

/**
 * creates WorkerInvocationStatement
 * @param {object} args - argument to be passed in to factory methods.
 * @returns {WorkerInvocationStatement} workerInvocationStatement
 */
DefaultBallerinaASTFactory.createWorkerInvocationStatement = function (args) {
    const workerInvocationStatement = BallerinaASTFactory.createWorkerInvocationStatement(args);
    workerInvocationStatement.setStatementFromString('m -> workerName');
    workerInvocationStatement.accept(new EnableDefaultWSVisitor());
    return workerInvocationStatement;
};

/**
 * creates workerReplyStatement
 * @param {object} args - argument to be passed in to factory methods.
 * @returns {WorkerReplyStatement} workerReplyStatement
 */
DefaultBallerinaASTFactory.createWorkerReplyStatement = function (args) {
    const workerReplyStatement = BallerinaASTFactory.createWorkerReplyStatement(args);
    workerReplyStatement.setStatementFromString('m <- workerName');
    workerReplyStatement.accept(new EnableDefaultWSVisitor());
    return workerReplyStatement;
};

/**
 * create AnnotationDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {AnnotationDefinition} annotationDefinition
 * */
DefaultBallerinaASTFactory.createAnnotationDefinition = function (args) {
    const annotationDefinition = BallerinaASTFactory.createAnnotationDefinition(args);
    const enableDefaultWSVisitor = new EnableDefaultWSVisitor();
    annotationDefinition.accept(enableDefaultWSVisitor);
    return annotationDefinition;
};

/**
 * create connector declaration
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} connectorDeclaration
 * */
DefaultBallerinaASTFactory.createConnectorDeclaration = function (args) {
    const packageName = args.pkgName;
    const declarationStatement = (packageName !== 'Current Package' ? args.pkgName + ':' : '') + args.connectorName
        + ' endpoint = create ' + (packageName !== 'Current Package' ? args.pkgName + ':' : '')
        + args.connectorName + '()';
    const connectorDeclaration = BallerinaASTFactory.createConnectorDeclaration();
    connectorDeclaration.setStatementFromString(declarationStatement);
    connectorDeclaration.setFullPackageName(args.fullPackageName);
    connectorDeclaration.accept(new EnableDefaultWSVisitor());
    return connectorDeclaration;
};

export default DefaultBallerinaASTFactory;
