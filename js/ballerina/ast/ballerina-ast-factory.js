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
        './worker-declaration', './statement', './conditional-statement', './connection-declaration', './expression',
        './if-statement', './reply-statement', './return-statement', './type-converter-definition', './type-definition',
        './type-element', './variable-declaration', './package-definition', './import-declaration', './resource-arg'],
    function (ballerinaAstRoot, serviceDefinition, functionDefinition, connectorDefinition, resourceDefinition,
              workerDeclaration, statement, conditionalStatement, connectionDeclaration, expression,
              ifStatement, replyStatement, returnStatement, typeConverterDefinition, typeDefinition,
              typeElement, variableDeclaration, packageDefinition, importDeclaration, resourceArgument) {

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
         * create ConnectionDeclaration
         * @param args
         */
        BallerinaASTFactory.prototype.createConnectionDeclaration = function (args) {
            return new connectionDeclaration(args);
        };

        /**
         * creates Expression
         * @param args
         */
        BallerinaASTFactory.prototype.createExpression = function (args) {
            return new expression(args);
        };

        /**
         * creates IfStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createIfStatement = function (args) {
            return new ifStatement(args);
        };

        /**
         * creates ReplyStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createReplyStatement = function (args) {
            return new replyStatement(args);
        };

        /**
         * creates ReturnStatement
         * @param args
         */
        BallerinaASTFactory.prototype.createReturnStatement = function (args) {
            return new returnStatement(args);
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

        return BallerinaASTFactory;

    });