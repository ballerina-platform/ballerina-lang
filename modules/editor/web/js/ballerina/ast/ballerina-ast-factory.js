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
        './if-else-statement', './if-statement', './else-statement', './else-if-statement', './trycatch-statement', './try-statement',
        './catch-statement', './reply-statement', './while-statement', './return-statement',
        './type-converter-definition', './type-definition', './type-element', './variable-declaration',
        './package-definition', './import-declaration', './resource-arg', './assignment', './function-invocation',
        './action-invocation-statement', './arithmetic-expression', './logical-expression', './action-invocation-expression',
        './return-type', './type-name'],
    function (ballerinaAstRoot, serviceDefinition, functionDefinition, connectorDefinition, resourceDefinition,
              workerDeclaration, statement, conditionalStatement, connectorDeclaration, expression,
              ifElseStatement, ifStatement, elseStatement, elseIfStatement, tryCatchStatement, tryStatement, catchStatement, replyStatement,
              whileStatement, returnStatement, typeConverterDefinition, typeDefinition, typeElement, variableDeclaration,
              packageDefinition, importDeclaration, resourceArgument, assignmentStatement, functionInvocation,
              actionInvocationStatement, arithmeticExpression, logicalExpression, actionInvocationExpression, returnType,
              typeName) {


        /**
         * @class BallerinaASTFactory
         * @lends BallerinaASTFactory
         */
        var BallerinaASTFactory = {};

        /**
         * creates BallerinaASTRoot
         * @param args
         */
        BallerinaASTFactory.createBallerinaAstRoot = function (args) {
            return new ballerinaAstRoot(args);
        };

        /**
         * creates ServiceDefinition
         * @param args
         * @param setDefaults - if this is set to true, default values will be set to the serviceDefinition
         */
        BallerinaASTFactory.createServiceDefinition = function (args, setDefaults) {
            var serviceDef = new serviceDefinition(args);
            if(setDefaults){
                serviceDef.addChild(BallerinaASTFactory.createResourceDefinition(args));
            }
            return serviceDef;
        };

        /**
         * creates FunctionDefinition
         * @param args
         */
        BallerinaASTFactory.createFunctionDefinition = function (args) {
            return new functionDefinition(args);
        };

        /**
         * creates ConnectorDefinition
         * @param args
         */
        BallerinaASTFactory.createConnectorDefinition = function (args) {
            return new connectorDefinition(args);
        };

        /**
         * creates WorkerDeclaration
         * @param args
         */
        BallerinaASTFactory.createWorkerDeclaration = function (args) {
            return new workerDeclaration(args);
        };

        /**
         * creates Statement
         * @param args
         */
        BallerinaASTFactory.createStatement = function (args) {
            return new statement(args);
        };

        /**
         * creates TypeConverterDefinition
         * @param args
         */
        BallerinaASTFactory.createTypeConverterDefinition = function (args) {
            return new typeConverterDefinition(args);
        };

        /**
         * creates TypeDefinition
         * @param args
         */
        BallerinaASTFactory.createTypeDefinition = function (args) {
            return new typeDefinition(args);
        };

        /**
         * creates TypeElement
         * @param args
         */
        BallerinaASTFactory.createTypeElement = function (args) {
            return new typeElement(args);
        };

        /**
         * create VariableDeclaration
         * @param args
         */
        BallerinaASTFactory.createVariableDeclaration = function (args) {
            return new variableDeclaration(args);
        };
        /**
         * create ConditionalStatement
         * @param args
         */
        BallerinaASTFactory.createConditionalStatement = function (args) {
            return new conditionalStatement(args);
        };

        /**
         * create ConnectorDeclaration
         * @param args
         */
        BallerinaASTFactory.createConnectorDeclaration = function (args) {
            return new connectorDeclaration(args);
        };

        /**
         * creates Expression
         * @param args
         */
        BallerinaASTFactory.createExpression = function (args) {
            return new expression(args);
        };

        BallerinaASTFactory.createActionInvocationStatement = function(args) {
            return new actionInvocationStatement(args);
        };

        BallerinaASTFactory.createActionInvocationExpression = function(args) {
            return new actionInvocationExpression(args);
        };

        /**
         * creates If-Else Statement
         * @param args
         */
        BallerinaASTFactory.createIfElseStatement = function (args) {
            var ifElse = new ifElseStatement(args);
            // TODO: Else statement should add through a button click. By default If else consists an if statement only
            var elseStmt = ifElse.createElseStatement(args);
            return ifElse;
        };

        /**
         * creates TryCatchStatement
         * @param args
         */
        BallerinaASTFactory.createTryCatchStatement = function (args) {
            return new tryCatchStatement(args);
        };

        /**
         * creates TryStatement
         * @param args
         */
        BallerinaASTFactory.createTryStatement = function (args) {
            return new tryStatement(args);
        };

        /**
         * creates CatchStatement
         * @param args
         */
        BallerinaASTFactory.createCatchStatement = function (args) {
            return new catchStatement(args);
        };

        /**
         * creates AssignmentStatement
         * @param args
         */
        BallerinaASTFactory.createAssignmentStatement = function (args) {
            return new assignmentStatement(args);
        };

        /**
         * creates ReplyStatement
         * @param args
         */
        BallerinaASTFactory.createReplyStatement = function (args) {
            return new replyStatement(args);
        };

        /**
         * creates FunctionInvocationStatement
         * @param args
         */
        BallerinaASTFactory.createFunctionInvocationStatement = function (args) {
            return new functionInvocation(args);
        };

        /**
         * creates ArithmeticExpression
         * @param args
         */
        BallerinaASTFactory.createArithmeticExpression = function (args) {
            return new arithmeticExpression(args);
        };

        /**
         * creates LogicalExpression
         * @param args
         */
        BallerinaASTFactory.createLogicalExpression = function (args) {
            return new logicalExpression(args);
        };

        /**
         * creates ReturnStatement
         * @param args
         */
        BallerinaASTFactory.createReturnStatement = function (args) {
            return new returnStatement(args);
        };

        /**
        * creates WhileStatement
        * @param args
        */
        BallerinaASTFactory.createWhileStatement = function (args) {
            return new whileStatement(args);
        };

        /**
         * creates ResourceDefinition
         * @param args
         */
        BallerinaASTFactory.createResourceDefinition = function (args) {
            return new resourceDefinition(args);
        };

        /**
         * creates PackageDefinition
         * @param args
         * @returns {PackageDefinition}
         */
        BallerinaASTFactory.createPackageDefinition = function (args) {
            return new packageDefinition(args);
        };

        /**
         * creates ImportDeclaration
         * @param args
         * @returns {ImportDeclaration}
         */
        BallerinaASTFactory.createImportDeclaration = function (args) {
            return new importDeclaration(args);
        };

        /**
         * creates ResourceArgument
         * @param args
         * @returns {ResourceArgument}
         */
        BallerinaASTFactory.createResourceArgument = function (args) {
            return new resourceArgument(args);
        };

        /**
         * creates ReturnType
         * @param args
         * @returns {ReturnType}
         */
        BallerinaASTFactory.createReturnType = function (args) {
            return new returnType(args);
        };

        /**
         * creates TypeName
         * @param args
         * @returns {TypeName}
         */
        BallerinaASTFactory.createTypeName = function (args) {
            return new typeName(args);
        };

        /**
         * instanceof check for ServiceDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isServiceDefinition = function (child) {
            return child instanceof serviceDefinition;
        };

        /**
         * instanceof check for FunctionDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isFunctionDefinition = function (child) {
            return child instanceof functionDefinition;
        };

        /**
         * instanceof check for ConnectorDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isConnectorDefinition = function (child) {
            return child instanceof connectorDefinition;
        };

        /**
         * instanceof check for WorkerDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isWorkerDeclaration = function (child) {
            return child instanceof workerDeclaration;
        };

        /**
         * instanceof check for Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isStatement = function (child) {
            return child instanceof statement;
        };

        /**
         * instanceof check for TypeConverterDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTypeConverterDefinition = function (child) {
            return child instanceof typeConverterDefinition;
        };

        /**
         * instanceof check for TypeDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTypeDefinition = function (child) {
            return child instanceof typeDefinition;
        };

        /**
         * instanceof check for TypeElement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTypeElement = function (child) {
            return child instanceof typeElement;
        };

        /**
         * is VariableDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isVariableDeclaration = function (child) {
            return child instanceof variableDeclaration;
        };
        /**
         * is ConditionalStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isConditionalStatement = function (child) {
            return child instanceof conditionalStatement;
        };

        /**
         * is ConnectorDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isConnectorDeclaration = function (child) {
            return child instanceof connectorDeclaration;
        };

        /**
         * instanceof check for Expression
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isExpression = function (child) {
            return child instanceof expression;
        };

        /**
         * instanceof check for If-Else Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isIfElseStatement = function (child) {
            return child instanceof ifElseStatement;
        };

        /**
         * instanceof check for If Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isIfStatement = function (child) {
            return child instanceof ifStatement;
        };

        /**
         * instanceof check for Else Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isElseStatement = function (child) {
            return child instanceof elseStatement;
        };

        /**
         * instanceof check for TryCatchStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTryCatchStatement = function (child) {
            return child instanceof tryCatchStatement;
        };

        /**
         * instanceof check for TryStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTryStatement = function (child) {
            return child instanceof tryStatement;
        };

        /**
         * instanceof check for CatchStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isCatchStatement = function (child) {
            return child instanceof catchStatement;
        };

        /**
         * instanceof check for ReplyStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isReplyStatement = function (child) {
            return child instanceof replyStatement;
        };

        /**
         * instanceof check for ReturnStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isReturnStatement = function (child) {
            return child instanceof returnStatement;
        };

        /**
         * instanceof check for ResourceDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isResourceDefinition = function (child) {
            return child instanceof resourceDefinition;
        };

        /**
         * instanceof check for PackageDefinition
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isPackageDefinition = function (child) {
            return child instanceof packageDefinition;
        };

        /**
         * instanceof check for ImportDeclaration
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isImportDeclaration = function (child) {
            return child instanceof importDeclaration;
        };

        /**
         * instanceof check for ResourceArgument
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isResourceArgument = function (child) {
            return child instanceof resourceArgument;
        };

        /**
         * instanceof check for ActionInvocationStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isActionInvocationStatement = function(statement){
            if (statement instanceof actionInvocationStatement){
                return true;
            }
        };

        /**
         * instanceof check for ReturnType
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isReturnType = function (child) {
            return child instanceof returnType;
        };

        /**
         * instanceof check for TypeName
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTypeName = function (child) {
            return child instanceof typeName;
        };

        BallerinaASTFactory.createFromJson = function (jsonNode) {
            var node;
            switch (jsonNode.type) {
                case 'package':
                    node = BallerinaASTFactory.createPackageDefinition();
                    break;
                case 'import':
                    node = BallerinaASTFactory.createImportDeclaration();
                    break;
                case 'service_definition':
                    node = BallerinaASTFactory.createServiceDefinition();
                    break;
                case 'function_definition':
                    node = BallerinaASTFactory.createFunctionDefinition();
                    break;
                case 'connector_definition':
                    node = BallerinaASTFactory.createConnectorDefinition();
                    break;
                case 'type_definition':
                    node = BallerinaASTFactory.createTypeDefinition();
                    break;
                case 'resource_definition':
                    node = BallerinaASTFactory.createResourceDefinition();
                    break;
                case 'connector_declaration':
                    node = BallerinaASTFactory.createConnectorDeclaration();
                    break;
                case 'variable_declaration':
                    node = BallerinaASTFactory.createVariableDeclaration();
                    break;
                case 'argument_declaration':
                    node = BallerinaASTFactory.createResourceArgument();
                    break;
                case 'reply_statement':
                    node = BallerinaASTFactory.createReplyStatement();
                    break;
                case 'return_statement':
                    node = BallerinaASTFactory.createReturnStatement();
                    break;
                case 'return_type':
                    node = BallerinaASTFactory.createReturnType();
                    break;
                case 'type_name':
                    node = BallerinaASTFactory.createTypeName();
                    break;
                default:
                    throw "Unknown node definition for " + jsonNode.type;
            }

            node.initFromJson(jsonNode);
            return node;
        };

        return BallerinaASTFactory;

    });
