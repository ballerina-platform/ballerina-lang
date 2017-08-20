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
import ASTFactory from './ast-factory';
import FragmentUtils from '../utils/fragment-utils';
import EnableDefaultWSVisitor from './../visitors/source-gen/enable-default-ws-visitor';

/**
 * @class DefaultASTFactory
 * @lends DefaultASTFactory
 */
const DefaultASTFactory = {};

/**
 * creates ServiceDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} serviceDef
 */
DefaultASTFactory.createServiceDefinition = function (args) {
    const serviceDef = ASTFactory.createServiceDefinition(args);
    serviceDef.setProtocolPkgName('http');
    serviceDef.setProtocolPkgPath('ballerina.net.http');
    const resourceDef = DefaultASTFactory.createResourceDefinition(args);
    serviceDef.addChild(resourceDef, undefined, undefined, undefined, true);
    serviceDef.accept(new EnableDefaultWSVisitor());
    return serviceDef;
};

/**
 * Create the default reply statement.
 * @param args
 * @returns {ReplyStatement}
 */
DefaultASTFactory.createReplyStatement = function (args) {
    const replyStatement = ASTFactory.createReplyStatement(args);
    replyStatement.setReplyMessage('m');
    replyStatement.accept(new EnableDefaultWSVisitor());
    return replyStatement;
};

/**
 *  * create Fork Join
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} forkJoinStatement
 */
DefaultASTFactory.createForkJoinStatement = function (args) {
    const forkJoinStatement = ASTFactory.createForkJoinStatement(args);
    const joinStatement = ASTFactory.createJoinStatement();
    const worker1Declaration = ASTFactory.createWorkerDeclaration();
    const worker2Declaration = ASTFactory.createWorkerDeclaration();
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
DefaultASTFactory.createResourceDefinition = function (args) {
    const resourceDef = ASTFactory.createResourceDefinition(args);

    // Creating GET http method annotation.
    const resourceConfigAnnotation = ASTFactory.createAnnotationAttachment({
        fullPackageName: 'ballerina.net.http',
        packageName: 'http',
        name: 'resourceConfig',
    });
    const httpMethodAttribute = ASTFactory.createAnnotationAttribute({
        key: 'methods',
    });

    const httpMethodsValue = ASTFactory.createAnnotationAttributeValue();
    const httpMethodsArray = ASTFactory.createAnnotationAttributeValue();
    const getHttpValue = ASTFactory.createBValue({
        stringValue: 'GET',
    });

    httpMethodsArray.addChild(getHttpValue);
    httpMethodsValue.addChild(httpMethodsArray);
    httpMethodAttribute.addChild(httpMethodsValue);
    resourceConfigAnnotation.addChild(httpMethodAttribute);
    resourceDef.addChild(resourceConfigAnnotation, 0);

    const parameterDef = ASTFactory.createParameterDefinition(args);
    parameterDef.setTypeName('message');
    parameterDef.setName('m');

    const argumentParameterDefinitionHolder = ASTFactory.createArgumentParameterDefinitionHolder();
    argumentParameterDefinitionHolder.addChild(parameterDef);
    resourceDef.addChild(argumentParameterDefinitionHolder);

    const replyStatement = DefaultASTFactory.createReplyStatement(args);
    resourceDef.addChild(replyStatement);

    return resourceDef;
};

/**
 * creates ConnectorDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} connectorDef
 */
DefaultASTFactory.createConnectorDefinition = function (args) {
    const connectorDef = ASTFactory.createConnectorDefinition(args);
    connectorDef.addArgument('message', 'm');
    const connectorActionDef = DefaultASTFactory.createConnectorAction();
    connectorActionDef.addReturnType('message', 'response');
    connectorDef.addChild(connectorActionDef, undefined, undefined, undefined, true);
    connectorDef.accept(new EnableDefaultWSVisitor());
    return connectorDef;
};

/**
 * creates ConnectorAction
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} actionDef
 */
DefaultASTFactory.createConnectorAction = function (args) {
    const actionDef = ASTFactory.createConnectorAction(args);
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
DefaultASTFactory.createVariableDefinitionStatement = function (args) {
    const variableDefinitionStatement = ASTFactory.createVariableDefinitionStatement(args);
    variableDefinitionStatement.setStatementFromString('int i = 0');
    variableDefinitionStatement.accept(new EnableDefaultWSVisitor());
    return variableDefinitionStatement;
};

/**
 * Create the particular assignment statement for the action invocation
 * @param {object} args - argument to be passed in to factory methods.
 * @returns {AssignmentStatement} assignmentStatement
 */
DefaultASTFactory.createAggregatedActionInvocationAssignmentStatement = function (args) {
    const actionDefinition = args.actionDefinition;
    let leftOperandExpression = '';
    actionDefinition.getReturnParams().forEach((param, index) => {
        if (index === 0) {
            leftOperandExpression = param.type + ' ' + (param.identifier
                ? param.identifier : param.type.substr(0, 1));
        }
    });

    const rightOperandExpression = 'clientConnector.' + args.action + '()';

    let statement;
    if (leftOperandExpression !== '') {
        statement = ASTFactory.createVariableDefinitionStatement();
        statement.setStatementFromString(`${leftOperandExpression} = ${rightOperandExpression}`, null);
    } else {
        statement = ASTFactory.createActionInvocationStatement();
        statement.setStatementFromString(rightOperandExpression);
    }
    statement.accept(new EnableDefaultWSVisitor());
    return statement;
};

/**
 * creates TryCatchStatement
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} tryCatchStatement
 */
DefaultASTFactory.createTryCatchStatement = function (args) {
    const tryCatchStatement = ASTFactory.createTryCatchStatement(args);
    tryCatchStatement.setStatementFromString('try{}catch(errors:Error err){}');
    tryCatchStatement.accept(new EnableDefaultWSVisitor());
    return tryCatchStatement;
};

/**
 * creates ThrowStatement
 * @param {Object} args - Arguments for creating a new throw statement.
 * @returns {ThrowStatement} throwStatement
 */
DefaultASTFactory.createThrowStatement = function (args) {
    const throwStatement = ASTFactory.createThrowStatement(args);
    throwStatement.setStatementFromString('throw e');
    throwStatement.accept(new EnableDefaultWSVisitor());
    return throwStatement;
};

/**
 * create an abort statement.
 * @param {object} args - arguments for creating a new throw statement.
 * @return {AbortStatement} AbortStatement
 * */
DefaultASTFactory.createAbortStatement = function (args) {
    return ASTFactory.createAbortStatement(args);
};

/**
 * create TransactionAborted Statement.
 * @param {object} args - argument for creating a new TransactionAborted statement.
 * @return {TransactionAbortedStatement} transactionAbortedStatement
 * */
DefaultASTFactory.createTransactionAbortedStatement = function (args) {
    const transactionAbortedStatement = ASTFactory.createTransactionAbortedStatement(args);
    transactionAbortedStatement.setStatementFromString('transaction {} failed {} aborted {} committed {}');
    transactionAbortedStatement.accept(new EnableDefaultWSVisitor());
    return transactionAbortedStatement;
};

/**
 * creates MainFunctionDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} functionDefinition
 */
DefaultASTFactory.createMainFunctionDefinition = function (args) {
    const functionDefinition = ASTFactory.createFunctionDefinition(args);
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
DefaultASTFactory.createAggregatedAssignmentStatement = function (args) {
    const fragment = FragmentUtils.createStatementFragment('a = b;');
    const parsedJson = FragmentUtils.parseFragment(fragment);
    if ((!_.has(parsedJson, 'error')
            || !_.has(parsedJson, 'syntax_errors'))
        && _.isEqual(parsedJson.type, 'assignment_statement')) {
        const node = ASTFactory.createFromJson(parsedJson);
        node.initFromJson(parsedJson);
        node.accept(new EnableDefaultWSVisitor());
        return node;
    }
    return ASTFactory.createAssignmentStatement(args);
};

DefaultASTFactory.createTransformAssignmentFunctionInvocationStatement = function (args) {
    const assignmentStmt = ASTFactory.createAssignmentStatement();
    const opts = {
        functionName: _.get(args, 'functionDef._name'),
        packageName: _.get(args, 'packageName'),
        fullPackageName: _.get(args, 'fullPackageName'),
    };
    const funcInvocationExpression = ASTFactory.createFunctionInvocationExpression(opts);
    if (!_.isNil(args) && _.has(args, 'functionDef')) {
        let functionInvokeString = '';
        if (!_.isNil(args.packageName)) {
            functionInvokeString += args.packageName + ':';
        }
        functionInvokeString += args.functionDef.getName() + '(';
        if (!_.isEmpty(args.functionDef.getParameters())) {
            const paramNames = [];
            args.functionDef.getParameters().forEach((param) => {
                paramNames.push(param.name);
                funcInvocationExpression.addChild(ASTFactory.createNullLiteralExpression());
            });
            functionInvokeString += _.join(paramNames, ', ');
        }
        functionInvokeString += ')';
        funcInvocationExpression.setExpressionFromString(functionInvokeString);

        // fragment parser does not have access to full package name. Hence, setting it here.
        funcInvocationExpression.setFullPackageName(_.get(args, 'fullPackageName'));

        let varRefListString = '';
        if (!_.isEmpty(args.functionDef.getReturnParams())) {
            const varRefNames = [];
            args.functionDef.getReturnParams().forEach((param, index) => {
                varRefNames.push(param.name || '_temp' + (index + 1));
            });
            if (varRefNames.length > 0) {
                varRefListString = _.join(varRefNames, ', ') + ' ';
                assignmentStmt.setIsDeclaredWithVar(true);
            }
        }

        const variableRefList = ASTFactory.createVariableReferenceList(args);
        variableRefList.setExpressionFromString(varRefListString);
        assignmentStmt.addChild(variableRefList, 0);
        assignmentStmt.addChild(funcInvocationExpression, 1);
    }
    return assignmentStmt;
};

DefaultASTFactory.createAssignmentFunctionInvocationStatement = function (args) {
    const assignmentStmt = ASTFactory.createAssignmentStatement();
    const opts = {
        functionName: _.get(args, 'functionDef._name'),
        packageName: _.get(args, 'packageName'),
        fullPackageName: _.get(args, 'fullPackageName'),
    };
    const funcInvocationExpression = ASTFactory.createFunctionInvocationExpression(opts);
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
                funcInvocationExpression.addChild(ASTFactory.createNullLiteralExpression());
            });
        }
        functionInvokeString += ')';
        funcInvocationExpression.setExpressionFromString(functionInvokeString);

        // fragment parser does not have access to full package name. Hence, setting it here.
        funcInvocationExpression.setFullPackageName(_.get(args, 'fullPackageName'));
        const variableRefList = ASTFactory.createVariableReferenceList(args);
        assignmentStmt.addChild(variableRefList, 0);
        assignmentStmt.addChild(funcInvocationExpression, 1);
    }
    return assignmentStmt;
};

/**
 * creates FunctionInvocationStatement
 * @param {object} args - argument to be passed in to factory methods.
 * @returns {FunctionInvocationStatement} funcInvocationStatement
 */
DefaultASTFactory.createAggregatedFunctionInvocationStatement = function (args) {
    const funcInvocationStatement = ASTFactory.createFunctionInvocationStatement();
    const opts = {
        functionName: _.get(args, 'functionDef._name'),
        packageName: _.get(args, 'packageName'),
        fullPackageName: _.get(args, 'fullPackageName'),
    };
    const funcInvocationExpression = ASTFactory.createFunctionInvocationExpression(opts);
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
            const variableDefinitionStatement = ASTFactory.createVariableDefinitionStatement();
            let leftOperandExpression = '';
            args.functionDef.getReturnParams().forEach((returnParam, index) => {
                if (index === 0) {
                    leftOperandExpression = returnParam.type + ' ' + (returnParam.identifier
                        ? returnParam.identifier : returnParam.type.substr(0, 1));
                }
            });
            const expression = leftOperandExpression + ' = ' + functionInvokeString;
            variableDefinitionStatement.setStatementFromString(expression);
            variableDefinitionStatement.getRightExpression().setFullPackageName(_.get(args, 'fullPackageName'));
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
DefaultASTFactory.createWorkerInvocationStatement = function (args) {
    const workerInvocationStatement = ASTFactory.createWorkerInvocationStatement(args);
    workerInvocationStatement.setStatementFromString('m -> workerName');
    workerInvocationStatement.accept(new EnableDefaultWSVisitor());
    return workerInvocationStatement;
};

/**
 * creates workerReplyStatement
 * @param {object} args - argument to be passed in to factory methods.
 * @returns {WorkerReplyStatement} workerReplyStatement
 */
DefaultASTFactory.createWorkerReplyStatement = function (args) {
    const workerReplyStatement = ASTFactory.createWorkerReplyStatement(args);
    workerReplyStatement.setStatementFromString('m <- workerName');
    workerReplyStatement.accept(new EnableDefaultWSVisitor());
    return workerReplyStatement;
};

/**
 * create AnnotationDefinition
 * @param {object} args - argument to be passed in to factory methods.
 * @return {AnnotationDefinition} annotationDefinition
 * */
DefaultASTFactory.createAnnotationDefinition = function (args) {
    const annotationDefinition = ASTFactory.createAnnotationDefinition(args);
    const enableDefaultWSVisitor = new EnableDefaultWSVisitor();
    annotationDefinition.accept(enableDefaultWSVisitor);
    return annotationDefinition;
};

/**
 * create connector declaration
 * @param {object} args - argument to be passed in to factory methods.
 * @return {ASTNode} connectorDeclaration
 * */
DefaultASTFactory.createConnectorDeclaration = function (args) {
    const packageName = args.pkgName;
    const declarationStatement = (packageName !== 'Current Package' ? args.pkgName + ':' : '') + args.connectorName
        + ' endpoint = create ' + (packageName !== 'Current Package' ? args.pkgName + ':' : '')
        + args.connectorName + '()';
    const connectorDeclaration = ASTFactory.createConnectorDeclaration();
    connectorDeclaration.setStatementFromString(declarationStatement);
    connectorDeclaration.setFullPackageName(args.fullPackageName);
    connectorDeclaration.accept(new EnableDefaultWSVisitor());
    return connectorDeclaration;
};

/**
 * create namespace declaration statement.
 * @param {object} args - argument for creating namespace declaration statement.
 * @return {ASTNode} namespaceDeclarationStatement
 * */
DefaultASTFactory.createNamespaceDeclarationStatement = function (args) {
    const namespaceDeclarationStatement = ASTFactory.createNamespaceDeclarationStatement(args);
    namespaceDeclarationStatement.setStatementFromString('xmlns "http://sample.org/test" as ns');
    namespaceDeclarationStatement.accept(new EnableDefaultWSVisitor());
    return namespaceDeclarationStatement;
};

DefaultASTFactory.createIgnoreErrorVariableReference = function (args) {
    const variableReferenceExpression = ASTFactory.createSimpleVariableReferenceExpression(args);
    variableReferenceExpression.setExpressionFromString('_');
    variableReferenceExpression.accept(new EnableDefaultWSVisitor());
    return variableReferenceExpression;
};

export default DefaultASTFactory;
