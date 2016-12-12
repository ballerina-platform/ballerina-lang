/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['./ballerina-ast-root', './service-definition', './function-definition', './connector-definition', './resource-definition',
        './worker-declaration', './statement', './conditional-statement', './connector-declaration', './expression',
        './if-else-statement', './if-statement', './else-statement', './trycatch-statement', './try-statement', './catch-statement', './reply-statement', './while-statement', './return-statement', './type-converter-definition', './type-definition',
        './type-element', './variable-declaration', './package-definition', './import-declaration', './resource-arg', './assignment', './function-invocation','./action-invocation-statement','./get-action-statement'],
    function (ballerinaAstRoot, serviceDefinition, functionDefinition, connectorDefinition, resourceDefinition,
              workerDeclaration, statement, conditionalStatement, connectorDeclaration, expression,
              ifElseStatement, ifStatement, elseStatement, tryCatchStatement, tryStatement, catchStatement, replyStatement, whileStatement, returnStatement, typeConverterDefinition, typeDefinition,
              typeElement, variableDeclaration, packageDefinition, importDeclaration, resourceArgument, assignmentStatement, functionInvocation, actionInvocationStatement,getActionStatement) {


        /**
         * Constructs BallerinaASTFactory
         * @constructor
         */
        var BallerinaASTFactory = function () {

        };

        BallerinaASTFactory.prototype.constructor = BallerinaASTFactory;

        /**
         * creates BallerinaASTRoot
         * @param args
         */
        BallerinaASTFactory.prototype.createBallerinaAstRoot = function (args) {
            return new ballerinaAstRoot(args);
        };

        /**
         * creates ServiceDefinition
         * @param args
         */
        BallerinaASTFactory.prototype.createServiceDefinition = function (args) {
            return new serviceDefinition(args);
        };

        /**
         * creates FunctionDefinition
         * @param args
         */
        BallerinaASTFactory.prototype.createFunctionDefinition = function (args) {
            return new functionDefinition(args);
        };

        /**
         * creates ConnectorDefinition
         * @param args
         */
        BallerinaASTFactory.prototype.createConnectorDefinition = function (args) {
            return new connectorDefinition(args);
        };

        /**
         * creates WorkerDeclaration
         * @param args
         */
        BallerinaASTFactory.prototype.createWorkerDeclaration = function (args) {
            return new workerDeclaration(args);
        };

        /**
         * creates Statement
         * @param args
         */
        BallerinaASTFactory.prototype.createStatement = function (args) {
            return new statement(args);
        };

        /**
         * creates TypeConverterDefinition
         * @param args
         */
        BallerinaASTFactory.prototype.createTypeConverterDefinition = function (args) {
            return new typeConverterDefinition(args);
        };

        /**
         * creates TypeDefinition
         * @param args
         */
        BallerinaASTFactory.prototype.createTypeDefinition = function (args) {
            return new typeDefinition(args);
        };

        /**
         * creates TypeElement
         * @param args
         */
        BallerinaASTFactory.prototype.createTypeElement = function (args) {
            return new typeElement(args);
        };

        /**
         * create VariableDeclaration
         * @param args
         */
        BallerinaASTFactory.prototype.createVariableDeclaration = function (args) {
            return new variableDeclaration(args);
        };
        /**
         * create ConditionalStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createConditionalStatement = function (args) {
            return new conditionalStatement(args);
        };

        /**
         * create ConnectorDeclaration
         * @param args
         */
        BallerinaASTFactory.prototype.createConnectorDeclaration = function (args) {
            return new connectorDeclaration(args);
        };

        /**
         * creates Expression
         * @param args
         */
        BallerinaASTFactory.prototype.createExpression = function (args) {
            return new expression(args);
        };

        BallerinaASTFactory.prototype.createGetActionStatement = function(args){
            return new getActionStatement(args);
        }

        /**
         * creates If-Else Statement
         * @param args
         */
        BallerinaASTFactory.prototype.createIfElseStatement = function (args) {
            return new ifElseStatement(args);
        };

        /**
         * creates If Statement
         * @param args
         */
        BallerinaASTFactory.prototype.createIfStatement = function (args) {
            return new ifStatement(args);
        };

        /**
         * creates Else Statement
         * @param args
         */
        BallerinaASTFactory.prototype.createElseStatement = function (args) {
            return new elseStatement(args);
        };

        /**
         * creates TryCatchStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createTryCatchStatement = function (args) {
            return new tryCatchStatement(args);
        };

        /**
         * creates TryStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createTryStatement = function (args) {
            return new tryStatement(args);
        };

        /**
         * creates CatchStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createCatchStatement = function (args) {
            return new catchStatement(args);
        };

        /**
         * creates AssignmentStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createAssignmentStatement = function (args) {
            return new assignmentStatement(args);
        };

        /**
         * creates ReplyStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createReplyStatement = function (args) {
            return new replyStatement(args);
        };

        /**
         * creates FunctionInvocationStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createFunctionInvocationStatement = function (args) {
            return new functionInvocation(args);
        };

        /**
         * creates ReturnStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createReturnStatement = function (args) {
            return new returnStatement(args);
        };

        /**
        * creates WhileStatement
        * @param args
        */
        BallerinaASTFactory.prototype.createWhileStatement = function (args) {
            return new whileStatement(args);
        };

        /**
         * creates ResourceDefinition
         * @param args
         */
        BallerinaASTFactory.prototype.createResourceDefinition = function (args) {
            return new resourceDefinition(args);
        };

        /**
         * creates PackageDefinition
         * @param args
         * @returns {PackageDefinition}
         */
        BallerinaASTFactory.prototype.createPackageDefinition = function (args) {
            return new packageDefinition(args);
        };

        /**
         * creates ImportDeclaration
         * @param args
         * @returns {ImportDeclaration}
         */
        BallerinaASTFactory.prototype.createImportDeclaration = function (args) {
            return new importDeclaration(args);
        };

        /**
         * creates ResourceArgument
         * @param args
         * @returns {ResourceArgument}
         */
        BallerinaASTFactory.prototype.createResourceArgument = function (args) {
            return new resourceArgument(args);
        };

        /**
         * instanceof check for ServiceDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isServiceDefinition = function (child) {
            return child instanceof serviceDefinition;
        };

        /**
         * instanceof check for FunctionDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isFunctionDefinition = function (child) {
            return child instanceof functionDefinition;
        };

        /**
         * instanceof check for ConnectorDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isConnectorDefinition = function (child) {
            return child instanceof connectorDefinition;
        };

        /**
         * instanceof check for WorkerDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isWorkerDeclaration = function (child) {
            return child instanceof workerDeclaration;
        };

        /**
         * instanceof check for Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isStatement = function (child) {
            return child instanceof statement;
        };

        /**
         * instanceof check for TypeConverterDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isTypeConverterDefinition = function (child) {
            return child instanceof typeConverterDefinition;
        };

        /**
         * instanceof check for TypeDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isTypeDefinition = function (child) {
            return child instanceof typeDefinition;
        };

        /**
         * instanceof check for TypeElement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isTypeElement = function (child) {
            return child instanceof typeElement;
        };

        /**
         * is VariableDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isVariableDeclaration = function (child) {
            return child instanceof variableDeclaration;
        };
        /**
         * is ConditionalStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isConditionalStatement = function (child) {
            return child instanceof conditionalStatement;
        };

        /**
         * is ConnectionDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isConnectionDeclaration = function (child) {
            return child instanceof connectionDeclaration;
        };

        /**
         * instanceof check for Expression
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isExpression = function (child) {
            return child instanceof expression;
        };

        /**
         * instanceof check for If-Else Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isIfElseStatement = function (child) {
            return child instanceof ifElseStatement;
        };

        /**
         * instanceof check for If Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isIfStatement = function (child) {
            return child instanceof ifStatement;
        };

        /**
         * instanceof check for Else Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isElseStatement = function (child) {
            return child instanceof elseStatement;
        };

        /**
         * instanceof check for TryCatchStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isTryCatchStatement = function (child) {
            return child instanceof tryCatchStatement;
        };

        /**
         * instanceof check for TryStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isTryStatement = function (child) {
            return child instanceof tryStatement;
        };

        /**
         * instanceof check for CatchStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isCatchStatement = function (child) {
            return child instanceof catchStatement;
        };

        /**
         * instanceof check for ReplyStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isReplyStatement = function (child) {
            return child instanceof replyStatement;
        };

        /**
         * instanceof check for ReturnStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isReturnStatement = function (child) {
            return child instanceof returnStatement;
        };

        /**
         * instanceof check for ResourceDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isResourceDefinition = function (child) {
            return child instanceof resourceDefinition;
        };

        /**
         * instanceof check for PackageDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isPackageDefinition = function (child) {
            return child instanceof packageDefinition;
        };

        /**
         * instanceof check for ImportDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isImportDeclaration = function (child) {
            return child instanceof importDeclaration;
        };

        /**
         * instanceof check for ResourceArgument
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.prototype.isResourceArgument = function (child) {
            return child instanceof resourceArgument;
        };
        
        return BallerinaASTFactory;

    });