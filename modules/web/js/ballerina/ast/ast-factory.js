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

/**
 * A module representing the factory for Ballerina AST
 */
import _ from 'lodash';
import ballerinaAstRoot from './ballerina-ast-root';
import serviceDefinition from './service-definition';
import functionDefinition from './function-definition';
import connectorDefinition from './connector-definition';
import resourceDefinition from './resource-definition';
import workerDeclaration from './worker-declaration';
import statement from './statements/statement';
import conditionalStatement from './statements/conditional-statement';
import connectorDeclaration from './connector-declaration';
import expression from './expressions/expression';
import ifElseStatement from './statements/if-else-statement';
import ifStatement from './statements/if-statement';
import elseStatement from './statements/else-statement';
import elseIfStatement from './statements/else-if-statement';
import tryCatchStatement from './statements/trycatch-statement';
import tryStatement from './statements/try-statement';
import catchStatement from './statements/catch-statement';
import finallyStatement from './statements/finally-statement';
import replyStatement from './statements/reply-statement';
import whileStatement from './statements/while-statement';
import returnStatement from './statements/return-statement';
import typeDefinition from './type-definition';
import typeElement from './type-element';
import variableDeclaration from './variable-declaration';
import packageDefinition from './package-definition';
import importDeclaration from './import-declaration';
import resourceParameter from './resource-parameter';
import assignmentStatement from './statements/assignment-statement';
import functionInvocationStatement from './statements/function-invocation-statement';
import functionInvocationExpression from './expressions/function-invocation-expression';
import SimpleVariableReferenceExpression from './expressions/simple-variable-reference-expression';
import actionInvocationStatement from './statements/action-invocation-statement';
import actionInvocationExpression from './expressions/action-invocation-expression';
import returnType from './return-type';
import typeName from './type-name';
import argument from './argument';
import backTickExpression from './expressions/back-tick-expression';
import basicLiteralExpression from './expressions/basic-literal-expression';
import lambdaFunctionExpression from './expressions/lambda-expression';
import nullLiteralExpression from './expressions/null-literal-expression';
import instanceCreationExpression from './expressions/instance-creation-expression';
import thenBody from './then-body';
import indexBasedVarRefExpression from './expressions/index-based-variable-reference-expression';
import keyValueExpression from './expressions/key-value-expression';
import binaryExpression from './expressions/binary-expression';
import unaryExpression from './expressions/unary-expression';
import connectorAction from './connector-action';
import structDefinition from './struct-definition';
import constantDefinition from './constant-definition';
import globalVariableDefinition from './global-variable-definition';
import variableDefinitionStatement from './statements/variable-definition-statement';
import workerInvocationStatement from './statements/worker-invocation-statement';
import referenceTypeInitExpression from './expressions/reference-type-init-expression';
import arrayInitExpression from './expressions/array-init-expression';
import workerReplyStatement from './statements/worker-reply-statement';
import structType from './struct-type';
import fieldBasedVarRefExpression from './expressions/field-based-variable-reference-expression';
import typeCastExpression from './expressions/type-cast-expression';
import typeConversionExpression from './expressions/type-conversion-expression';
import variableDefinition from './variable-definition';
import breakStatement from './statements/break-statement';
import throwStatement from './statements/throw-statement';
import commentStatement from './statements/comment-statement';
import annotationDefinition from './annotation-definition';
import annotationAttributeDefinition from './annotation-attribute-definition';
import parameterDefinition from './parameter-definition';
import argumentParameterDefinitionHolder from './argument-parameter-definition-holder';
import returnParameterDefinitionHolder from './return-parameter-definition-holder';
import AnnotationAttachment from './annotations/annotation-attachment';
import AnnotationAttribute from './annotations/annotation-attribute';
import AnnotationAttributeValue from './annotations/annotation-attribute-value';
import transformStatement from './statements/transform-statement';
import forkJoinStatement from './statements/fork-join-statement';
import timeoutStatement from './statements/timeout-statement';
import joinStatement from './statements/join-statement';
import transactionAbortedStatement from './statements/transaction-aborted-statement';
import transactionStatement from './statements/transaction-statement';
import abortedStatement from './statements/aborted-statement';
import abortStatement from './statements/abort-statement';
import committedStatement from './statements/committed-statement';
import continueStatement from './statements/continue-statement';
import connectorInitExpression from './expressions/connector-init-expression';
import simpleTypeName from './simple-type-name';
import VariableReferenceList from './variable-reference-list';
import BValue from './b-value';
import NamespaceDeclarationStatement from './statements/namespace-declaration-statement';
import NamespaceDeclaration from './namespace-declaration';
import XMLQNameExpression from './expressions/xml-qname-expression';
import XMLAttributeReferenceExpression from './expressions/xml-attribute-reference-expression';
import FailedStatement from './statements/failed-statement';
import RetryStatement from './statements/retry-statement';

/**
 * @class ASTFactory
 * @lends ASTFactory
 */
const ASTFactory = {};

/**
 * creates BallerinaASTRoot
 * @param args
 */
ASTFactory.createBallerinaAstRoot = function (args) {
    return new ballerinaAstRoot(args);
};

/**
 * creates SimpleTypeName
 * @param args
 */
ASTFactory.createSimpleTypeName = function (args) {
    return new simpleTypeName(args);
};

/**
 * creates ServiceDefinition
 * @param args
 * @param setDefaults - if this is set to true, default values will be set to the serviceDefinition
 */
ASTFactory.createServiceDefinition = function (args, setDefaults) {
    return new serviceDefinition(args);
};

/**
 * creates AnnotationDefinition.
 * @param args
 * */
ASTFactory.createAnnotationDefinition = function (args) {
    return new annotationDefinition(args);
};

/**
 * creates Annotation Attribute Definition.
 * @param args
 * */
ASTFactory.createAnnotationAttributeDefinition = function (args) {
    return new annotationAttributeDefinition(args);
};

/**
 * creates FunctionDefinition
 * @param args
 */
ASTFactory.createFunctionDefinition = function (args) {
    return new functionDefinition(args);
};

/**
 * creates ConnectorDefinition
 * @param args
 */
ASTFactory.createConnectorDefinition = function (args) {
    return new connectorDefinition(args);
};

/**
 * creates WorkerDeclaration
 * @param args
 */
ASTFactory.createWorkerDeclaration = function (args) {
    return new workerDeclaration(args);
};

/**
 * creates Statement
 * @param args
 */
ASTFactory.createStatement = function (args) {
    return new statement(args);
};

/**
 * creates TypeDefinition
 * @param args
 */
ASTFactory.createTypeDefinition = function (args) {
    return new typeDefinition(args);
};

/**
 * creates TypeElement
 * @param args
 */
ASTFactory.createTypeElement = function (args) {
    return new typeElement(args);
};

/**
 * creates structDefinition
 * @param {Object} args - object for structDefinition creation
 * @returns {StructDefinition}
 */
ASTFactory.createStructDefinition = function (args) {
    return new structDefinition(args);
};

/**
 * creates keyValueExpression
 * @param {Object} args - object for keyValueExpression creation
 * @returns {KeyValueExpression}
 */
ASTFactory.createKeyValueExpression = function (args) {
    return new keyValueExpression(args);
};

/**
 * creates ReferenceTypeInitExpression
 * @param {Object} args - object for ReferenceTypeInitExpression creation
 * @returns {ReferenceTypeInitExpression}
 */
ASTFactory.createReferenceTypeInitExpression = function (args) {
    return new referenceTypeInitExpression(args);
};

/**
 * creates ArrayInitExpression
 * @param {Object} args - object for ArrayInitExpression creation
 * @returns {ArrayInitExpression}
 */
ASTFactory.createArrayInitExpression = function (args) {
    return new arrayInitExpression(args);
};

/**
 * create VariableDeclaration
 * @param args - object for variableDeclaration creation
 * @returns {VariableDeclaration}
 */
ASTFactory.createVariableDeclaration = function (args) {
    return new variableDeclaration(args);
};

/**
 * create VariableDefinition
 * @param args
 */
ASTFactory.createVariableDefinition = function (args) {
    return new variableDefinition(args);
};

/**
 * create ConditionalStatement
 * @param args
 */
ASTFactory.createConditionalStatement = function (args) {
    return new conditionalStatement(args);
};

/**
 * create ConnectorDeclaration
 * @param args
 */
ASTFactory.createConnectorDeclaration = function (args) {
    return new connectorDeclaration(args);
};

/**
 * creates Expression
 * @param args
 */
ASTFactory.createExpression = function (args) {
    return new expression(args);
};

ASTFactory.createActionInvocationStatement = function (args) {
    return new actionInvocationStatement(args);
};

ASTFactory.createActionInvocationExpression = function (args) {
    return new actionInvocationExpression(args);
};

/**
 * creates a connector action
 * @param args
 * @return {ConnectorAction} Connector Action
 */
ASTFactory.createConnectorAction = function (args) {
    return new connectorAction(args);
};

/**
 * creates If-Else Statement
 * @param args
 */
ASTFactory.createIfElseStatement = function (args) {
    return new ifElseStatement(args);
};

/**
 * creates If Statement
 * @param args
 */
ASTFactory.createIfStatement = function (args) {
    return new ifStatement(args);
};

/**
 * creates Else-If Statement
 * @param args
 */
ASTFactory.createElseIfStatement = function (args) {
    return new elseIfStatement(args);
};

/**
 * creates Else Statement
 * @param args
 */
ASTFactory.createElseStatement = function (args) {
    return new elseStatement(args);
};

/**
 * creates TryCatchStatement
 * @param args
 */
ASTFactory.createTryCatchStatement = function (args) {
    return new tryCatchStatement(args);
};

/**
 * creates TryStatement
 * @param args
 */
ASTFactory.createTryStatement = function (args) {
    return new tryStatement(args);
};

/**
 * creates CatchStatement
 * @param args
 */
ASTFactory.createCatchStatement = function (args) {
    return new catchStatement(args);
};

/**
 * creates FinallyStatement
 * @param args
 */
ASTFactory.createFinallyStatement = function (args) {
    return new finallyStatement(args);
};

/**
 * creates Assignment
 * @param args
 */
ASTFactory.createAssignment = function (args) {
    return new assignment(args);
};

/**
 * creates AssignmentStatement
 * @param {Object} args
 * @returns {AssignmentStatement}
 */
ASTFactory.createAssignmentStatement = function (args) {
    return new assignmentStatement(args);
};

/**
 * creates AssignmentStatement
 * @param {Object} args
 * @returns {AssignmentStatement}
 */
ASTFactory.createTransformStatement = function (args) {
    return new transformStatement(args);
};

ASTFactory.createForkJoinStatement = function (args) {
    return new forkJoinStatement(args);
};

ASTFactory.createJoinStatement = function (args) {
    return new joinStatement(args);
};

ASTFactory.createTimeoutStatement = function (args) {
    return new timeoutStatement(args);
};

/**
 * Creates Variable Definition Statement
 * @param {Object} [args]
 * @returns {VariableDefinitionStatement}
 */
ASTFactory.createVariableDefinitionStatement = function (args) {
    return new variableDefinitionStatement(args);
};

/**
 * creates ReplyStatement
 * @param args
 */
ASTFactory.createReplyStatement = function (args) {
    return new replyStatement(args);
};

/**
 * creates FunctionInvocationStatement
 * @param args
 */
ASTFactory.createFunctionInvocationStatement = function (args) {
    return new functionInvocationStatement(args);
};

/**
 * creates FunctionInvocationExpression
 * @param {Object} args
 * @returns {FunctionInvocationExpression}
 */
ASTFactory.createFunctionInvocationExpression = function (args) {
    return new functionInvocationExpression(args);
};

/**
 * creates SimpleVariableReferenceExpression
 * @param {Object} args
 * @returns {SimpleVariableReferenceExpression}
 */
ASTFactory.createSimpleVariableReferenceExpression = function (args) {
    return new SimpleVariableReferenceExpression(args);
};

/**
 * creates ReturnStatement
 * @param args
 */
ASTFactory.createReturnStatement = function (args) {
    return new returnStatement(args);
};

/**
 * creates FieldBasedVarRefExpression
 * @param args
 */
ASTFactory.createFieldBasedVarRefExpression = function (args) {
    return new fieldBasedVarRefExpression(args);
};

/**
 * creates TypeCastExpression
 * @param args
 */
ASTFactory.createTypeCastExpression = function (args) {
    return new typeCastExpression(args);
};

/**
 * creates TypeConversionExpression
 * @param args
 */
ASTFactory.createTypeConversionExpression = function (args) {
    return new typeConversionExpression(args);
};

/**
 * creates WorkerInvocationStatement
 * @param args
 */
ASTFactory.createWorkerInvocationStatement = function (args) {
    return new workerInvocationStatement(args);
};

/**
 * creates WorkerReplyStatement
 * @param args
 */
ASTFactory.createWorkerReplyStatement = function (args) {
    return new workerReplyStatement(args);
};

/**
 * creates WhileStatement
 * @param args
 */
ASTFactory.createWhileStatement = function (args) {
    return new whileStatement(args);
};

/**
 * creates BreakStatement
 */
ASTFactory.createBreakStatement = function () {
    return new breakStatement();
};

/**
 * creates ResourceDefinition
 * @param args
 */
ASTFactory.createResourceDefinition = function (args) {
    return new resourceDefinition(args);
};

/**
 * creates PackageDefinition
 * @param args
 * @returns {PackageDefinition}
 */
ASTFactory.createPackageDefinition = function (args) {
    return new packageDefinition(args);
};

/**
 * creates ImportDeclaration
 * @param args
 * @returns {ImportDeclaration}
 */
ASTFactory.createImportDeclaration = function (args) {
    return new importDeclaration(args);
};

/**
 * creates ResourceParameter
 * @param args
 * @returns {ResourceParameter}
 */
ASTFactory.createResourceParameter = function (args) {
    return new resourceParameter(args);
};

/**
 * creates StructType
 * @param args
 * @returns {StructType}
 */
ASTFactory.createStructType= function (args) {
    return new structType(args);
};

/**
 * creates Argument
 * @param {Object} [args] - The arguments to create the Argument
 * @param {string} [args.type] - Type of the argument
 * @param {string} [args.identifier] - Identifier of the argument
 * @returns {Argument}
 */
ASTFactory.createArgument = function (args) {
    return new argument(args);
};

/**
 * creates ReturnType
 * @param args
 * @returns {ReturnType}
 */
ASTFactory.createReturnType = function (args) {
    return new returnType(args);
};

/**
 * creates TypeName
 * @param args
 * @returns {TypeName}
 */
ASTFactory.createTypeName = function (args) {
    return new typeName(args);
};

/**
 * creates BackTickExpression
 * @param {Object} args
 * @returns {backTickExpression}
 */
ASTFactory.createBackTickExpression = function (args) {
    return new backTickExpression(args);
};

/**
 * creates BasicLiteralExpression
 * @param args
 * @returns {basicLiteralExpression}
 */
ASTFactory.createBasicLiteralExpression = function (args) {
    return new basicLiteralExpression(args);
};

/**
 * creates BasicLiteralExpression
 * @param args
 * @returns {LambdaExpression}
 */
ASTFactory.createLambdaExpression = function (args) {
    return new lambdaFunctionExpression(args);
};

/**
 * creates NullLiteralExpression
 * @param args
 * @returns {nullLiteralExpression}
 */
ASTFactory.createNullLiteralExpression = function (args) {
    return new nullLiteralExpression(args);
};

/**
 * creates VariableReferenceList
 * @param {Object} args
 * @returns {VariableReferenceList}
 */
ASTFactory.createVariableReferenceList = function (args) {
    return new VariableReferenceList(args);
};

/**
 * creates InstanceCreationExpression
 * @param {Object} args - Arguments for creating a new instance creation.
 * @param {Object} args.typeName - Type of the new instance creation.
 * @returns {InstanceCreationExpression} - New instance creation node.
 */
ASTFactory.createInstanceCreationExpression = function (args) {
    return new instanceCreationExpression(args);
};

/**
 * creates ThenBody
 * @param {Object} args - Arguments for creating a new instance creation.
 * @returns {ThenBody}
 */
ASTFactory.createThenBody = function (args) {
    return new thenBody(args);
};

/**
 * creates BinaryExpression
 * @param {Object} args - Arguments for creating a new instance creation.
 * @returns {BinaryExpression}
 */
ASTFactory.createBinaryExpression = function (args) {
    return new binaryExpression(args);
};

/**
 * Create Unary Expression
 * @param {Object} args - Arguments for the creating new expression creation
 * @return {UnaryExpression}
 * */
ASTFactory.createUnaryExpression = function (args) {
    return new unaryExpression(args);
};

/**
 * Create ConnectorInitExpression
 * @param {Object} args - Arguments for the creating new expression creation
 * @return {ConnectorInitExpression}
 * */
ASTFactory.createConnectorInitExpression = function (args) {
    return new connectorInitExpression(args);
};

/**
 * creates IndexBasedVarRefExpression
 * @param {Object} args - Arguments for creating a new instance creation.
 * @returns {IndexBasedVarRefExpression}
 */
ASTFactory.createIndexBasedVarRefExpression = function (args) {
    return new indexBasedVarRefExpression(args);
};

/**
 * creates ConstantDefinition
 * @param {Object} args - Arguments for creating a new constant definition.
 * @returns {ConstantDefinition}
 */
ASTFactory.createConstantDefinition = function (args) {
    return new constantDefinition(args);
};

/**
 * creates a GlobalVariableDefinition instance
 * @param {Object} args - Arguments for creating a new constant definition.
 * @returns {globalVariableDefinition}
 */
ASTFactory.createGlobalVariableDefinition = function (args) {
    return new globalVariableDefinition(args);
};

/**
 * creates ThrowStatement
 * @param {Object} args - Arguments for creating a new throw statement.
 * @returns {ThrowStatement}
 */
ASTFactory.createThrowStatement = function (args) {
    return new throwStatement(args);
};

/**
 * creates CommentStatement
 * @param {Object} args - Arguments for creating a new comment statement.
 * @returns {CommentStatement}
 */
ASTFactory.createCommentStatement = function (args) {
    return new commentStatement(args);
};

/**
 * creates TransformStatement
 * @param {Object} args - Arguments for creating a new transform statement.
 * @returns {TransformStatement}
 */
ASTFactory.createTransformStatement = function (args) {
    return new transformStatement(args);
};

/**
 * Creates retry statement
 * @param {Object} args - argument for the creating retry statement.
 * @return {RetryStatement} retry statement.
 * */
ASTFactory.createRetryStatement = function(args){
    return new RetryStatement(args);
};

/**
 * creates {@link AnnotationAttachment}
 * @param  {Object} args arguments for creating a new annotation.
 * @return {AnnotationAttachment}      new annotation object.
 */
ASTFactory.createAnnotationAttachment = function (args) {
    return new AnnotationAttachment(args);
};

/**
 * creates {@link AnnotationAttribute}
 * @param  {Object} args arguments for creating a new annotation attribute.
 * @return {AnnotationAttribute} new annotation attribute object.
 */
ASTFactory.createAnnotationAttribute = function (args) {
    return new AnnotationAttribute(args);
};

/**
 * Creates a {@link AnnotationAttributeValue}
 * @param {Object} args Arguments for creating a new annotation attribute value.
 * @return {AnnotationAttributeValue}
 */
ASTFactory.createAnnotationAttributeValue = (args) => {
    return new AnnotationAttributeValue(args);
};

/**
 * creates ParameterDefinition
 * @param {Object} args - Arguments for creating a new parameter definition.
 * @returns {ParameterDefinition}
 */
ASTFactory.createParameterDefinition = function (args) {
    return new parameterDefinition(args);
};

ASTFactory.createArgumentParameterDefinitionHolder = function (args) {
    return new argumentParameterDefinitionHolder();
};

ASTFactory.createReturnParameterDefinitionHolder = function (args) {
    return new returnParameterDefinitionHolder();
};

/**
 * Create {@link TransactionAbortedStatement}
 * @param {object} args - Argument to create the transaction aborted statement.
 * @return {TransactionAbortedStatement} new TransactionAborted Statement.
 * */
ASTFactory.createTransactionAbortedStatement = function (args) {
    return new transactionAbortedStatement(args);
};

/**
 * Create {@link TransactionStatement}
 * @param {object} args - Argument to create the transaction statement.
 * @return {TransactionStatement} new Transaction Statement.
 * */
ASTFactory.createTransactionStatement = function (args) {
    return new transactionStatement();
};

/**
 * Create {@link FailedStatement}
 * @param {object} args - Argument to create the failed statement.
 * @return {FailedStatement} new Failed Statement.
 * */
ASTFactory.createFailedStatement = function (args) {
    return new FailedStatement();
};

/**
 * Create {@link AbortedStatement}
 * @param {object} args - Argument to create the Aborted statement.
 * @return {AbortedStatement} new Aborted Statement.
 * */
ASTFactory.createAbortedStatement = function (args) {
    return new abortedStatement();
};

/**
 * Create {@link CommittedStatement}
 * @param {object} args - Arguments to create the committed statement.
 * @return {CommittedStatement} new Committed Statement.
 * */
ASTFactory.createCommittedStatement = function (args) {
    return new committedStatement();
};

/**
 * Create {@link }
 * @param {object} args - Arguments to create the continue statement.
 * @return {} new Committed Statement.
 * */
ASTFactory.createContinueStatement = function (args) {
    return new continueStatement();
};

/**
 * Create {@link AbortStatement}
 * @param {object} args - Arguments to create the Abort Statement.
 * @return {AbortStatement} new Abort Statement.
 * */
ASTFactory.createAbortStatement = function (args) {
    return new abortStatement();
};

/**
 * Creates new {@link BValue}.
 * @param {Object} Arguments for creating a bvalue object.
 * @return {Bvalue} new Bvalue
 */
ASTFactory.createBValue = (args) => {
    return new BValue(args);
};

/**
 * Create new {@link NamespaceDeclarationStatement}
 * @param {object} args - arguments for creating a namespace declaration object.
 * @return {NamespaceDeclarationStatement} new NamespaceDeclarationStatement.
 * */
ASTFactory.createNamespaceDeclarationStatement = (args) => {
    return new NamespaceDeclarationStatement(args);
};

/**
 * Create new {@link NamespaceDeclaration}
 * @param {object} args - arguments for creating a namespace declaration.
 * @return {NamespaceDeclaration} new NamespaceDeclaration.
 * */
ASTFactory.createNamespaceDeclaration = (args) => {
    return new NamespaceDeclaration(args);
};

/**
 * Create new {@link XMLQNameExpression}
 * @param {object} args - arguments for the creating a XML QName expression.
 * @return {XMLQNameExpression} new XMLQNameExpression.
 * */
ASTFactory.createXMLQNameExpression = (args) => {
    return new XMLQNameExpression(args);
};

/**
 * Create new {@link XMLAttributeReferenceExpression}
 * @return {XMLAttributeReferenceExpression} new XMLAttributeReferenceExpression.
 * */
ASTFactory.createXMLAttributeReferenceExpression = () => {
    return new XMLAttributeReferenceExpression();
};

/**
 * instanceof check for BallerinaAstRoot
 * @param {Object} child
 * @returns {boolean}
 */
ASTFactory.isBallerinaAstRoot = function (child) {
    return child instanceof ballerinaAstRoot;
};

/**
 * instanceof check for SimpleTypeName
 * @param {Object} child
 * @returns {boolean}
 */
ASTFactory.isSimpleTypeName = function (child) {
    return child instanceof simpleTypeName;
};

/**
 * instanceof check for ServiceDefinition
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isServiceDefinition = function (child) {
    return child instanceof serviceDefinition;
};

/**
 * instanceof check for FunctionDefinition
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isFunctionDefinition = function (child) {
    return child instanceof functionDefinition;
};

/**
 * instanceof check for ConnectorDefinition
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isConnectorDefinition = function (child) {
    return child instanceof connectorDefinition;
};

/**
 * instanceof check for WorkerDeclaration
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isWorkerDeclaration = function (child) {
    return child instanceof workerDeclaration;
};

/**
 * instanceof check for WorkerInvocationStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isWorkerInvocationStatement = function (child) {
    return child instanceof workerInvocationStatement;
};

/**
 * instanceof check for JoinStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isJoinStatement = function (child) {
    return child instanceof joinStatement;
};

/**
 * instanceof check for ForkJoinStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isForkJoinStatement = function (child) {
    return child instanceof forkJoinStatement;
};

/**
 * instanceof check for ForkTimeoutStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTimeoutStatement = function (child) {
    return child instanceof timeoutStatement;
};

/**
 * instanceof check for WorkerReplyStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isWorkerReplyStatement = function (child) {
    return child instanceof workerReplyStatement;
};

/**
 * instanceof check for Statement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isStatement = function (child) {
    return child instanceof statement;
};

/**
 * instanceof check for while Statement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isWhileStatement = function (child) {
    return child instanceof whileStatement;
};

/**
 * instanceof check for break Statement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isBreakStatement = function (child) {
    return child instanceof breakStatement;
};

/**
 * instanceof check for TypeConverterDefinition
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTypeConverterDefinition = function (child) {
    return child instanceof typeConverterDefinition;
};

/**
 * instanceof check for TypeDefinition
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTypeDefinition = function (child) {
    return child instanceof typeDefinition;
};

/**
 * instanceof check for TypeElement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTypeElement = function (child) {
    return child instanceof typeElement;
};

/**
 * instanceof check for StructDefinition
 * @param {ASTNode} child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isStructDefinition = function (child) {
    return child instanceof structDefinition;
};

/**
 * is VariableDeclaration
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isVariableDeclaration = function (child) {
    return child instanceof variableDeclaration;
};

/**
 * is ReferenceTypeInitExpression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isReferenceTypeInitiExpression = function (child) {
    return child instanceof referenceTypeInitExpression;
};


/**
 * is StructType
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isStructType = function (child) {
    return child instanceof structType;
};

/**
 * is ConditionalStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isConditionalStatement = function (child) {
    return child instanceof conditionalStatement;
};

/**
 * is ConnectorDeclaration
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isConnectorDeclaration = function (child) {
    return child instanceof connectorDeclaration;
};

/**
 * instanceof check for Expression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isExpression = function (child) {
    return child instanceof expression;
};

/**
 * instanceof check for KeyValueExpression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isKeyValueExpression= function (child) {
    return child instanceof keyValueExpression;
};

/**
 * instanceof check for ReferenceTypeInitExpression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isReferenceTypeInitExpression = function (child) {
    return child instanceof referenceTypeInitExpression;
};

/**
 * instanceof check for FieldBasedVarRefExpression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isFieldBasedVarRefExpression = function (child) {
    return child instanceof fieldBasedVarRefExpression;
};

/**
 * instanceof check for VariableReferenceList
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isVariableReferenceList = function (child) {
    return child instanceof VariableReferenceList;
};

/**
 * instanceof check for TypeCastExpression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTypeCastExpression = function (child) {
    return child instanceof typeCastExpression;
};

/**
 * instanceof check for TypeConversionExpression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTypeConversionExpression = function (child) {
    return child instanceof typeConversionExpression;
};

/**
 * instanceof check for If-Else Statement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isIfElseStatement = function (child) {
    return child instanceof ifElseStatement;
};

/**
 * instanceof check for If Statement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isIfStatement = function (child) {
    return child instanceof ifStatement;
};

/**
 * instanceof check for Else Statement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isElseStatement = function (child) {
    return child instanceof elseStatement;
};

/**
 * instanceof check for ElseIf Statement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isElseIfStatement = function (child) {
    return child instanceof elseIfStatement;
};

/**
 * instanceof check for TryCatchStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTryCatchStatement = function (child) {
    return child instanceof tryCatchStatement;
};

/**
 * instanceof check for TryStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTryStatement = function (child) {
    return child instanceof tryStatement;
};

/**
 * instanceof check for CatchStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isCatchStatement = function (child) {
    return child instanceof catchStatement;
};

/**
 * instanceof check for FinallyStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isFinallyStatement = function (child) {
    return child instanceof finallyStatement;
};

/**
 * instanceof check for ReplyStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isReplyStatement = function (child) {
    return child instanceof replyStatement;
};

/**
 * instanceof check for ReturnStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isReturnStatement = function (child) {
    return child instanceof returnStatement;
};

/**
 * instanceof check for ResourceDefinition
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isResourceDefinition = function (child) {
    return child instanceof resourceDefinition;
};

/**
 * instanceof check for PackageDefinition
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isPackageDefinition = function (child) {
    return child instanceof packageDefinition;
};

/**
 * instanceof check for ImportDeclaration
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isImportDeclaration = function (child) {
    return child instanceof importDeclaration;
};

/**
 * instanceof check for ResourceParameter.
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isResourceParameter = function (child) {
    return child instanceof resourceParameter;
};

/**
 * instanceof check for ActionInvocationStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isActionInvocationStatement = function (child) {
    return child instanceof actionInvocationStatement;
};

/**
 * instanceof check for TransformStatement
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTransformStatement = function (child) {
    return child instanceof transformStatement;
};

/**
 * instanceof check for ActionInvocationExpression
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isActionInvocationExpression = function (child) {
    return child instanceof actionInvocationExpression;
};

/**
 * instanceof check for Argument
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isArgument = function (child) {
    return child instanceof argument;
};

/**
 * instanceof check for ReturnType
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isReturnType = function (child) {
    return child instanceof returnType;
};

/**
 * instanceof check for retry.
 * @param {ASTNode} child - object for instanceof check
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isRetry = function(child){
    return child instanceof RetryStatement;
};

/**
 * instanceof check for TypeName
 * @param child - Object for instanceof check
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isTypeName = function (child) {
    return child instanceof typeName;
};

/**
 * instanceof check for BackTickExpression
 * @param child
 * @returns {boolean}
 */
ASTFactory.isBackTickExpression = function (child) {
    return child instanceof backTickExpression;
};

/**
 * instanceof check for Assignment Statement
 * @param child
 * @returns {boolean}
 */
ASTFactory.isAssignmentStatement = function (child) {
    return child instanceof assignmentStatement;
};

/**
 * instanceof check for Assignment Statement
 * @param child
 * @returns {boolean}
 */
ASTFactory.isTransformStatement = function (child) {
    return child instanceof transformStatement;
};


/**
 * instanceof check for BasicLiteralExpression
 * @param child
 * @returns {boolean}
 */
ASTFactory.isBasicLiteralExpression = function (child) {
    return child instanceof basicLiteralExpression;
};

/**
 * instanceof check for LambdaExpression
 * @param child
 * @returns {boolean}
 */
ASTFactory.isLambdaExpression = function (child) {
    return child instanceof lambdaFunctionExpression;
};

/**
 * instanceof check for NullLiteralExpression
 * @param child
 * @returns {boolean}
 */
ASTFactory.isNullLiteralExpression = function (child) {
    return child instanceof nullLiteralExpression;
};

/**
 * instanceof check for SimpleVariableReferenceExpression
 * @param node
 * @returns {boolean}
 */
ASTFactory.isSimpleVariableReferenceExpression = function (node) {
    return node instanceof SimpleVariableReferenceExpression;
};

/**
 * instanceof check for VariableDefinition
 * @param child
 * @returns {boolean}
 */
ASTFactory.isVariableDefinition = function (child) {
    return child instanceof variableDefinition;
};

/**
 * instanceof check for InstanceCreationExpression
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - True if node is an instance creation, else false.
 */
ASTFactory.isInstanceCreationExpression = function (child) {
    return child instanceof instanceCreationExpression;
};

/**
 * instanceof check for ThenBody
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isThenBody = function (child) {
    return child instanceof thenBody;
};

/**
 * instanceof check for binaryExpression
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isBinaryExpression = function (child) {
    return child instanceof binaryExpression;
};

/**
 * instanceof check for ConnectorInitExpression
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isConnectorInitExpression = function (child) {
    return child instanceof connectorInitExpression;
};

/**
 * instanceof check for IndexBasedVarRefExpression
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isIndexBasedVarRefExpression = function (child) {
    return child instanceof indexBasedVarRefExpression;
};

/**
 * instanceof check for functionInvocationExpression
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isFunctionInvocationExpression = function (child) {
    return child instanceof functionInvocationExpression;
};

/**
 * instanceof check for functionInvocationStatement
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isFunctionInvocationStatement = function (child) {
    return child instanceof functionInvocationStatement;
};

/**
 * instanceof check for connectorAction
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isConnectorAction = function (child) {
    return child instanceof connectorAction;
};

/**
 * instanceof check for constantDefinition
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isConstantDefinition = function (child) {
    return child instanceof constantDefinition;
};

/**
 * instanceof check for globalVariableDefinition
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isGlobalVariableDefinition = function (child) {
    return child instanceof globalVariableDefinition;
};

/**
 * instanceof check for Annotation definition.
 * @param {ASTNode} child - The ast node.
 * @return {boolean} - true if same type, else false
 * */
ASTFactory.isAnnotationDefinition = function (child) {
    return child instanceof annotationDefinition;
};

/**
 * instanceof check for Annotation Attribute Definition.
 * @param {ASTNode} child - The ast node.
 * @return {boolean} - true if same type, else false
 * */
ASTFactory.isAnnotationAttributeDefinition = function (child) {
    return child instanceof annotationAttributeDefinition;
};

/**
 * instanceof check for variableDefinitionStatement
 * @param {ASTNode} child - The ast node.
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isVariableDefinitionStatement = function (child) {
    return child instanceof variableDefinitionStatement;
};

/**
 * instanceof check for throwStatement
 * @param {ASTNode} child - The ast node
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isThrowStatement = function (child) {
    return child instanceof throwStatement;
};

/**
 * instanceof check for commentStatement
 * @param {ASTNode} child - The ast node
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isCommentStatement = function (child) {
    return child instanceof commentStatement;
};

/**
 * instanceof check for annotation attachment ast node.
 * @param  {ASTNode} child The ast node
 * @return {Boolean} true if same type, else false
 */
ASTFactory.isAnnotationAttachment = (child) => {
    return child instanceof AnnotationAttachment;
};

/**
 * instanceof check for annotation attribute ast node.
 * @param  {ASTNode} child The ast node
 * @return {Boolean} true if same type, else false
 */
ASTFactory.isAnnotationAttribute = (child) => {
    return child instanceof AnnotationAttribute;
};

/**
 * instanceof check for AnnotationAttributeValue ast node.
 * @param  {ASTNode}  child The ast node
 * @return {Boolean}  true if same type, else false
 */
ASTFactory.isAnnotationAttributeValue = function (child) {
    return child instanceof AnnotationAttributeValue;
};

/**
 * instanceof check for ParameterDefinition
 * @param {ASTNode} child - The ast node
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isParameterDefinition = function (child) {
    return child instanceof parameterDefinition;
};

/**
 * instanceof check for ArgumentParameterDefinitionHolder
 * @param {ASTNode} child - The ast node
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isArgumentParameterDefinitionHolder = function (child) {
    return child instanceof argumentParameterDefinitionHolder;
};

/**
 * instanceof check for ReturnParameterDefinitionHolder
 * @param {ASTNode} child - The ast node
 * @returns {boolean} - true if same type, else false
 */
ASTFactory.isReturnParameterDefinitionHolder = function (child) {
    return child instanceof returnParameterDefinitionHolder;
};

/**
 * instanceof check for the TransactionAbortedStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isTransactionAbortedStatement = function (child) {
    return child instanceof transactionAbortedStatement;
};

/**
 * instanceof check for the TransactionStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isTransactionStatement = function (child) {
    return child instanceof transactionStatement;
};

/**
 * instanceof check for the FailedStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isFailedStatement = function (child) {
    return child instanceof FailedStatement;
};

/**
 * instanceof check for the AbortedStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isAbortedStatement = function (child) {
    return child instanceof abortedStatement;
};

/**
 * instanceof check for the AbortStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isAbortStatement = function (child) {
    return child instanceof abortStatement;
};

/**
 * instanceof check for the CommittedStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isCommittedStatement = function (child) {
    return child instanceof committedStatement;
};

/**
 * instanceof check for the ContinueStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isContinueStatement = function (child) {
    return child instanceof continueStatement;
};

/**
 * instanceof check for the BValue.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 */
ASTFactory.isBValue = (child) => {
    return child instanceof BValue;
};

/**
 * instanceof check for the NamespaceDeclarationStatement.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if same type, else false.
 * */
ASTFactory.isNamespaceDeclarationStatement = (child) => {
    return child instanceof NamespaceDeclarationStatement;
};

/**
 * instanceof check for the NamespaceDeclaration.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if the same type, else false.
 * */
ASTFactory.isNamespaceDeclaration = (child) => {
    return child instanceof NamespaceDeclaration;
};

/**
 * instanceof check for the XMLQNameExpression.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if the same type, else false.
 * */
ASTFactory.isXMLQNameExpression = (child) => {
    return child instanceof XMLQNameExpression;
};

/**
 * instanceof check for the XMLAttributeReferenceExpression.
 * @param {ASTNode} child - the ast node.
 * @return {boolean} - true if the same typ, else false
 * */
ASTFactory.isXMLAttributeReferenceExpression = (child) => {
    return child instanceof XMLAttributeReferenceExpression;
};

ASTFactory.createFromJson = function (jsonNode) {
    let node;
    const nodeType = jsonNode.type;

    switch (nodeType) {
        case 'simple_type_name':
            node = ASTFactory.createSimpleTypeName();
            break;
        case 'package':
            node = ASTFactory.createPackageDefinition();
            break;
        case 'import':
            node = ASTFactory.createImportDeclaration();
            break;
        case 'annotation_attachment':
            node = ASTFactory.createAnnotationAttachment();
            break;
        case 'annotation_attribute':
            node = ASTFactory.createAnnotationAttribute();
            break;
        case 'annotation_attribute_value':
            node = ASTFactory.createAnnotationAttributeValue();
            break;
        case 'service_definition':
            node = ASTFactory.createServiceDefinition();
            break;
        case 'annotation_definition':
            node = ASTFactory.createAnnotationDefinition();
            break;
        case 'function_definition':
            node = ASTFactory.createFunctionDefinition();
            break;
        case 'connector_definition':
            node = ASTFactory.createConnectorDefinition();
            break;
        case 'type_definition':
            node = ASTFactory.createTypeDefinition();
            break;
        case 'resource_definition':
            node = ASTFactory.createResourceDefinition();
            break;
        case 'connector_declaration':
            node = ASTFactory.createConnectorDeclaration();
            break;
        case 'variable_definition':
            node = ASTFactory.createVariableDefinition();
            break;
        case 'variable_definition_statement':
            node = ASTFactory.createVariableDefinitionStatement();
            break;
        case 'argument_declaration':
            node = ASTFactory.createResourceParameter();
            break;
        case 'reply_statement':
            node = ASTFactory.createReplyStatement();
            break;
        case 'return_statement':
            node = ASTFactory.createReturnStatement();
            break;
        case 'return_type':
            node = ASTFactory.createReturnType();
            break;
        case 'return_argument':
            node = ASTFactory.createArgument();
            break;
        case 'type_name':
            node = ASTFactory.createTypeName();
            break;
        case 'function_invocation_statement':
            node = ASTFactory.createFunctionInvocationStatement();
            break;
        case 'function_invocation_expression':
            node = ASTFactory.createFunctionInvocationExpression();
            break;
        case 'simple_variable_reference_expression':
            node = ASTFactory.createSimpleVariableReferenceExpression();
            break;
        case 'action_invocation_expression':
            node = ASTFactory.createActionInvocationExpression();
            break;
        case 'assignment_statement':
            node = ASTFactory.createAssignmentStatement();
            break;
        case 'back_tick_expression':
            node = ASTFactory.createBackTickExpression();
            break;
        case 'while_statement' :
            node = ASTFactory.createWhileStatement();
            break;
        case 'break_statement' :
            node = ASTFactory.createBreakStatement();
            break;
        case 'basic_literal_expression' :
            node = ASTFactory.createBasicLiteralExpression();
            break;
        case 'lambda_function_expression' :
            node = ASTFactory.createLambdaExpression();
            break;
        case 'null_literal_expression' :
            node = ASTFactory.createNullLiteralExpression();
            break;
        case 'variable_reference_list':
            node = ASTFactory.createVariableReferenceList();
            break;
        case 'if_else_statement' :
            node = ASTFactory.createIfElseStatement();
            break;
        case 'if_statement' :
            node = ASTFactory.createIfStatement();
            break;
        case 'else_if_statement' :
            node = ASTFactory.createElseIfStatement();
            break;
        case 'else_statement' :
            node = ASTFactory.createElseStatement();
            break;
        case 'instance_creation_expression':
            node = ASTFactory.createInstanceCreationExpression();
            break;
        case 'then_body':
            node = ASTFactory.createThenBody();
            break;
        case 'equal_expression':
            node = ASTFactory.createBinaryExpression({ operator: '==' });
            break;
        case 'greater_than_expression':
            node = ASTFactory.createBinaryExpression({ operator: '>' });
            break;
        case 'add_expression':
            node = ASTFactory.createBinaryExpression({ operator: '+' });
            break;
        case 'multiplication_expression':
            node = ASTFactory.createBinaryExpression({ operator: '*' });
            break;
        case 'division_expression':
            node = ASTFactory.createBinaryExpression({ operator: '/' });
            break;
        case 'mod_expression' :
            node = ASTFactory.createBinaryExpression({ operator: '%' });
            break;
        case 'and_expression':
            node = ASTFactory.createBinaryExpression({ operator: '&&' });
            break;
        case 'subtract_expression':
            node = ASTFactory.createBinaryExpression({ operator: '-' });
            break;
        case 'or_expression':
            node = ASTFactory.createBinaryExpression({ operator: '||' });
            break;
        case 'greater_equal_expression':
            node = ASTFactory.createBinaryExpression({ operator: '>=' });
            break;
        case 'less_than_expression':
            node = ASTFactory.createBinaryExpression({ operator: '<' });
            break;
        case 'less_equal_expression':
            node = ASTFactory.createBinaryExpression({ operator: '<=' });
            break;
        case 'not_equal_expression':
            node = ASTFactory.createBinaryExpression({ operator: '!=' });
            break;
        case 'unary_expression':
            node = ASTFactory.createUnaryExpression({ operator: jsonNode.operator });
            break;
        case 'connector_init_expr':
            node = ASTFactory.createConnectorInitExpression();
            break;
        case 'index_based_variable_reference_expression':
            node = ASTFactory.createIndexBasedVarRefExpression();
            break;
        case 'connector':
            node = ASTFactory.createConnectorDefinition();
            break;
        case 'action_definition':
            node = ASTFactory.createConnectorAction();
            break;
        case 'constant_definition':
            node = ASTFactory.createConstantDefinition();
            break;
        case 'global_variable_definition':
            node = ASTFactory.createGlobalVariableDefinition();
            break;
        case 'struct_definition':
            node = ASTFactory.createStructDefinition();
            break;
        case 'key_value_expression':
            node = ASTFactory.createKeyValueExpression();
            break;
        case 'type_cast_expression':
            node = ASTFactory.createTypeCastExpression();
            break;
        case 'type_conversion_expression':
            node = ASTFactory.createTypeConversionExpression();
            break;
        case 'field_based_variable_reference_expression':
            node = ASTFactory.createFieldBasedVarRefExpression();
            break;
        case 'reference_type_init_expression':
            node = ASTFactory.createReferenceTypeInitExpression();
            break;
        case 'array_init_expression':
            node = ASTFactory.createArrayInitExpression();
            break;
        case 'action_invocation_statement':
            node = ASTFactory.createActionInvocationStatement();
            break;
        case 'worker':
            node = ASTFactory.createWorkerDeclaration();
            break;
        case 'worker_invocation_statement':
            node = ASTFactory.createWorkerInvocationStatement();
            break;
        case 'worker_reply_statement':
            node = ASTFactory.createWorkerReplyStatement();
            break;
        case 'try_catch_statement':
            node = ASTFactory.createTryCatchStatement();
            break;
        case 'try_block':
            node = ASTFactory.createTryStatement();
            break;
        case 'catch_block':
            node = ASTFactory.createCatchStatement();
            break;
        case 'finally_block':
            node = ASTFactory.createFinallyStatement();
            break;
        case 'throw_statement':
            node = ASTFactory.createThrowStatement();
            break;
        case 'comment_statement':
            node = ASTFactory.createCommentStatement();
            break;
        case 'annotation_attribute_definition':
            node = ASTFactory.createAnnotationAttributeDefinition();
            break;
        case 'parameter_definition':
            node = ASTFactory.createParameterDefinition();
            break;
        case 'argument_parameter_definitions':
            node = ASTFactory.createArgumentParameterDefinitionHolder();
            break;
        case 'return_parameter_definitions':
            node = ASTFactory.createReturnParameterDefinitionHolder();
            break;
        case 'transform_statement':
            node = ASTFactory.createTransformStatement();
            break;
        case 'fork_join_statement':
            node = ASTFactory.createForkJoinStatement();
            break;
        case 'join_statement':
            node = ASTFactory.createJoinStatement();
            break;
        case 'timeout_statement':
            node = ASTFactory.createTimeoutStatement();
            break;
        case 'transaction_aborted_statement':
            node = ASTFactory.createTransactionAbortedStatement();
            break;
        case 'transaction_statement':
            node = ASTFactory.createTransactionStatement();
            break;
        case 'aborted_statement':
            node = ASTFactory.createAbortedStatement();
            break;
        case 'abort_statement':
            node = ASTFactory.createAbortStatement();
            break;
        case 'committed_statement':
            node = ASTFactory.createCommittedStatement();
            break;
        case 'continue_statement':
            node = ASTFactory.createContinueStatement();
            break;
        case 'bvalue':
            node = ASTFactory.createBValue();
            break;
        case 'namespace_declaration_statement':
            node = ASTFactory.createNamespaceDeclarationStatement();
            break;
        case 'namespace_declaration':
            node = ASTFactory.createNamespaceDeclaration();
            break;
        case 'xml_qname_expression':
            node = ASTFactory.createXMLQNameExpression();
            break;
        case 'xml_attribute_ref_expression':
            node = ASTFactory.createXMLAttributeReferenceExpression();
            break;
        case 'failed_statement':
            node = ASTFactory.createFailedStatement();
            break;
        case 'retry_statement':
            node = ASTFactory.createRetryStatement();
            break;
        default:
            throw new Error('Unknown node definition for ' + jsonNode.type);
    }
    // handle special case of connector declaration
    if (jsonNode.type === 'variable_definition_statement' &&
                !_.isNil(jsonNode.children[1]) && jsonNode.children[1].type === 'connector_init_expr') {
        node = ASTFactory.createConnectorDeclaration();
    } 

    node.setLineNumber(jsonNode.line_number, { doSilently: true });

    if (!_.isNil(jsonNode.position_info)) {
        const { start_line, start_offset, stop_line, stop_offset } = jsonNode.position_info;
        const position = {
            startLine: start_line,
            startOffset: start_offset,
            stopLine: stop_line,
            stopOffset: stop_offset
        };
        node.setPosition(position, { doSilently: true });
    }
    if (jsonNode.is_identifier_literal) {
        node.setIsIdentifierLiteral(jsonNode.is_identifier_literal, { doSilently: true });
    }

    if (!_.isNil(jsonNode.whitespace_descriptor)) {
        node.setWhiteSpaceDescriptor(jsonNode.whitespace_descriptor);
    }
    node.whiteSpace.useDefault = false;
    return node;
};

export default ASTFactory;
