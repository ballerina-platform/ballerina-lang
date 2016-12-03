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
        './if-statement', './package', './reply-statement', './return-statement', './type-converter-definition', './type-definition',
        './type-element', './variable-declaration'],
    function (ballerinaAstRoot, serviceDefinition, functionDefinition, connectorDefinition,
              workerDeclaration, statement, conditionalStatement, connectionDeclaration, expression,
              ifStatement, package, replyStatement, returnStatement, typeConverterDefinition, typeDefinition,
              typeElement, variableDeclaration) {

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
        var createFunctionDefinition = function (args) {
            return new functionDefinition(args);
        }

        /**
         * creates ConnectorDefinition
         * @param args
         */
        var createConnectorDefinition = function (args) {
            return new connectorDefinition(args);
        }

        /**
         * creates WorkerDeclaration
         * @param args
         */
        var createWorkerDeclaration = function (args) {
            return new workerDeclaration(args);
        }

        /**
         * creates Statement
         * @param args
         */
        var createStatement = function (args) {
            return new statement(args);
        }

        /**
         * creates TypeConverterDefinition
         * @param args
         */
        var createTypeConverterDefinition = function (args) {
            return new typeConverterDefinition(args);
        }

        /**
         * creates TypeDefinition
         * @param args
         */
        var createTypeDefinition = function (args) {
            return new typeDefinition(args);
        }

        /**
         * creates TypeElement
         * @param args
         */
        var createTypeElement = function (args) {
            return new typeElement(args);
        }

        /**
         * create VariableDeclaration
         * @param args
         */
        var createVariableDeclaration = function (args) {
            return new variableDeclaration(args);
        }
        /**
         * create ConditionalStatement
         * @param args
         */
        var createConditionalStatement = function (args) {
            return new conditionalStatement(args);
        }

        /**
         * create ConnectionDeclaration
         * @param args
         */
        var createConnectionDeclaration = function (args) {
            return new connectionDeclaration(args);
        }

        /**
         * creates Expression
         * @param args
         */
        var createExpression = function (args) {
            return new expression(args);
        }

        /**
         * creates IfStatement
         * @param args
         */
        var createIfStatement = function (args) {
            return new ifStatement(args);
        }

        /**
         * creates Package
         * @param args
         */
        var createPackage = function (args) {
            return new package(args);
        }

        /**
         * creates ReplyStatement
         * @param args
         */
        var createReplyStatement = function (args) {
            return new replyStatement(args);
        }

        /**
         * creates ReturnStatement
         * @param args
         */
        var createReturnStatement = function (args) {
            return new returnStatement(args);
        }

        return BallerinaASTFactory;

    });