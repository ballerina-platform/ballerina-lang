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
define(['lodash', './ballerina-ast-root', './service-definition', './function-definition', './connector-definition',
        './resource-definition', './worker-declaration', './statement', './conditional-statement', './connector-declaration',
        './expression', './if-else-statement', './if-statement', './else-statement', './else-if-statement', './trycatch-statement',
        './try-statement', './catch-statement', './reply-statement', './while-statement', './return-statement',
        './type-mapper-definition', './type-definition', './type-element', './variable-declaration',
        './package-definition', './import-declaration', './resource-parameter', './assignment', './assignment-statement',
        './function-invocation', './function-invocation-expression', './variable-reference-expression',
        './action-invocation-statement', './arithmetic-expression', './logical-expression', './action-invocation-expression',
        './return-type', './type-name', './argument', './back-quote-expression', './basic-literal-expression',
        './left-operand-expression', './right-operand-expression', './instance-creation-expression', './then-body',
        './if-condition', './array-map-access-expression', './key-value-expression',
        './binary-expression', './unary-expression','./connector-action', './struct-definition', './constant-definition',
        './variable-definition-statement','./type-casting-expression', './worker-invoke',
        './reference-type-init-expression', './array-init-expression', './worker-receive','./struct-type','./struct-field-access-expression',
        './block-statement','./type-cast-expression','./variable-definition', './break-statement', './throw-statement', './comment-statement'],
    function (_, ballerinaAstRoot, serviceDefinition, functionDefinition, connectorDefinition, resourceDefinition,
              workerDeclaration, statement, conditionalStatement, connectorDeclaration, expression, ifElseStatement,
              ifStatement, elseStatement, elseIfStatement, tryCatchStatement, tryStatement, catchStatement, replyStatement,
              whileStatement, returnStatement, typeMapperDefinition, typeDefinition, typeElement, variableDeclaration,
              packageDefinition, importDeclaration, resourceParameter, assignment, assignmentStatement, functionInvocation,
              functionInvocationExpression, variableReferenceExpression, actionInvocationStatement, arithmeticExpression,
              logicalExpression, actionInvocationExpression, returnType, typeName, argument, backQuoteExpression,
              basicLiteralExpression, leftOperandExpression, rightOperandExpression, instanceCreationExpression,
              thenBody, ifCondition, arrayMapAccessExpression, keyValueExpression, binaryExpression,
              unaryExpression, connectorAction, structDefinition, constantDefinition, variableDefinitionStatement,
              typeCastingExpression, workerInvoke, referenceTypeInitExpression, arrayInitExpression, workerReceive,structType,
              structFieldAccessExpression,blockStatement,typeCastExpression,variableDefinition, breakStatement, throwStatement, commentStatement) {



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
         * creates MainFunctionDefinition
         * @param args
         */
        BallerinaASTFactory.createMainFunctionDefinition = function (args) {
            var functionDefinition = BallerinaASTFactory.createFunctionDefinition(args);
            functionDefinition.setFunctionName("main");
            functionDefinition.addArgument("string[]", "args");
            return functionDefinition;
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
         * creates structDefinition
         * @param {Object} args - object for structDefinition creation
         * @returns {StructDefinition}
         */
        BallerinaASTFactory.createStructDefinition = function (args) {
            return new structDefinition(args);
        };

        /**
         * creates keyValueExpression
         * @param {Object} args - object for keyValueExpression creation
         * @returns {KeyValueExpression}
         */
        BallerinaASTFactory.createKeyValueExpression = function (args) {
            return new keyValueExpression(args);
        };

        /**
         * creates TypeCastingExpression
         * @param {Object} args - object for TypeCastingExpression creation
         * @returns {TypeCastingExpression}
         */
        BallerinaASTFactory.createTypeCastingExpression = function (args) {
            return new typeCastingExpression(args);
        };

        /**
         * creates ReferenceTypeInitExpression
         * @param {Object} args - object for TypeCastingExpression creation
         * @returns {ReferenceTypeInitExpression}
         */
        BallerinaASTFactory.createReferenceTypeInitExpression = function (args) {
            return new referenceTypeInitExpression(args);
        };

        /**
         * creates ArrayInitExpression
         * @param {Object} args - object for TypeCastingExpression creation
         * @returns {ArrayInitExpression}
         */
        BallerinaASTFactory.createArrayInitExpression = function (args) {
            return new arrayInitExpression(args);
        };

        /**
         * creates typeMapperDefinition
         * @param {Object} args - object for typeMapperDefinition creation
         * @returns {TypeMapperDefinition}
         */
        BallerinaASTFactory.createTypeMapperDefinition = function (args) {
            return new typeMapperDefinition(args);
        };

        /**
         * create VariableDeclaration
         * @param args - object for variableDeclaration creation
         * @returns {VariableDeclaration}
         */
        BallerinaASTFactory.createVariableDeclaration = function (args) {
            return new variableDeclaration(args);
        };

        /**
         * create VariableDefinition
         * @param args
         */
        BallerinaASTFactory.createVariableDefinition = function (args) {
            return new variableDefinition(args);
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
         * creates a connector action
         * @param args
         * @return {ConnectorAction} Connector Action
         */
        BallerinaASTFactory.createConnectorAction = function(args) {
            return new connectorAction(args);
        };

        /* Create the particular assignment statement for the function invocation
         * @param args
         * @returns {AssignmentStatement}
         */
        BallerinaASTFactory.createAggregatedFunctionInvocationExpression = function(args) {
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
         * creates If-Else Statement
         * @param args
         */
        BallerinaASTFactory.createIfElseStatement = function (args) {
            return new ifElseStatement(args);
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
         * creates Assignment
         * @param args
         */
        BallerinaASTFactory.createAssignment = function (args) {
            return new assignment(args);
        };

        /**
         * creates AssignmentStatement
         * @param {Object} args
         * @returns {AssignmentStatement}
         */
        BallerinaASTFactory.createAssignmentStatement = function (args) {
            return new assignmentStatement(args);
        };

        /**
         * Creates Variable Definition Statement
         * @param {Object} [args]
         * @returns {VariableDefinitionStatement}
         */
        BallerinaASTFactory.createVariableDefinitionStatement = function (args) {
            return new variableDefinitionStatement(args);
        };

        /**
         * creates Aggregated AssignmentStatement
         * @param {Object} args
         * @returns {AssignmentStatement}
         */
        BallerinaASTFactory.createAggregatedAssignmentStatement = function (args) {
            var assignmentStmt = BallerinaASTFactory.createAssignmentStatement(args);
            var leftOperand = BallerinaASTFactory.createLeftOperandExpression(args);
            leftOperand.setLeftOperandExpressionString("a");
            var rightOperand = BallerinaASTFactory.createRightOperandExpression(args);
            rightOperand.setRightOperandExpressionString("b");
            assignmentStmt.addChild(leftOperand);
            assignmentStmt.addChild(rightOperand);
            return assignmentStmt;
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
         * creates FunctionInvocationStatement
         * @param args
         * @returns {FunctionInvocation}
         */
        BallerinaASTFactory.createAggregatedFunctionInvocationStatement = function (args) {
            var funcInvocationStatement = new functionInvocation(args);
            var funcInvocationExpression = BallerinaASTFactory.createFunctionInvocationExpression(args);
            funcInvocationStatement.addChild(funcInvocationExpression);
            return funcInvocationStatement;
        };

        /**
         * creates FunctionInvocationExpression
         * @param {Object} args
         * @returns {FunctionInvocationExpression}
         */
        BallerinaASTFactory.createFunctionInvocationExpression = function (args) {
            return new functionInvocationExpression(args);
        };

        /**
         * creates VariableReferenceExpression
         * @param {Object} args
         * @returns {VariableReferenceExpression}
         */
        BallerinaASTFactory.createVariableReferenceExpression = function (args) {
            return new variableReferenceExpression(args);
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
         * creates BlockStatement
         * @param args
         */
        BallerinaASTFactory.createBlockStatement = function (args) {
            return new blockStatement(args);
        };

        /**
         * creates ReturnStatement
         * @param args
         */
        BallerinaASTFactory.createReturnStatement = function (args) {
            return new returnStatement(args);
        };

         /**
         * creates StructFieldAccessExpression
         * @param args
         */
        BallerinaASTFactory.createStructFieldAccessExpression = function (args) {
            return new structFieldAccessExpression(args);
        };

        /**
         * creates TypeCastExpression
         * @param args
         */
        BallerinaASTFactory.createTypeCastExpression = function (args) {
            return new typeCastExpression(args);
        };

        /**
         * creates WorkerInvokeStatement
         * @param args
         */
        BallerinaASTFactory.createWorkerInvokeStatement = function (args) {
            return new workerInvoke(args);
        };

        /**
         * creates WorkerReceiveStatement
         * @param args
         */
        BallerinaASTFactory.createWorkerReceiveStatement = function (args) {
            return new workerReceive(args);
        };

        /**
         * creates WhileStatement
         * @param args
         */
        BallerinaASTFactory.createWhileStatement = function (args) {
            return new whileStatement(args);
        };

        /**
         * creates BreakStatement
         */
        BallerinaASTFactory.createBreakStatement = function () {
            return new breakStatement();
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
         * creates ResourceParameter
         * @param args
         * @returns {ResourceParameter}
         */
        BallerinaASTFactory.createResourceParameter = function (args) {
            return new resourceParameter(args);
        };

        /**
         * creates StructType
         * @param args
         * @returns {StructType}
         */
        BallerinaASTFactory.createStructType = function (args) {
            return new structType(args);
        };

        /**
         * creates Argument
         * @param {Object} [args] - The arguments to create the Argument
         * @param {string} [args.type] - Type of the argument
         * @param {string} [args.identifier] - Identifier of the argument
         * @returns {Argument}
         */
        BallerinaASTFactory.createArgument = function (args) {
            return new argument(args);
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
         * creates BackQuoteExpression
         * @param {Object} args
         * @returns {backQuoteExpression}
         */
        BallerinaASTFactory.createBackQuoteExpression = function (args) {
            return new backQuoteExpression(args);
        };

        /**
         * creates BasicLiteralExpression
         * @param args
         * @returns {basicLiteralExpression}
         */
        BallerinaASTFactory.createBasicLiteralExpression = function (args) {
            return new basicLiteralExpression(args);
        };

        /**
         * creates LeftOperandExpression
         * @param {Object} args
         * @returns {LeftOperandExpression}
         */
        BallerinaASTFactory.createLeftOperandExpression = function (args) {
            return new leftOperandExpression(args);
        };

        /**
         * creates RightOperandExpression
         * @param {Object} args
         * @returns {RightOperandExpression}
         */
        BallerinaASTFactory.createRightOperandExpression = function (args) {
            return new rightOperandExpression(args);
        };

        /**
         * creates InstanceCreationExpression
         * @param {Object} args - Arguments for creating a new instance creation.
         * @param {Object} args.typeName - Type of the new instance creation.
         * @returns {InstanceCreationExpression} - New instance creation node.
         */
        BallerinaASTFactory.createInstanceCreationExpression = function (args) {
            return new instanceCreationExpression(args);
        };

        /**
         * creates ThenBody
         * @param {Object} args - Arguments for creating a new instance creation.
         * @returns {ThenBody}
         */
        BallerinaASTFactory.createThenBody = function (args) {
            return new thenBody(args);
        };

        /**
         * creates IfCondition
         * @param {Object} args - Arguments for creating a new instance creation.
         * @returns {IfCondition}
         */
        BallerinaASTFactory.createIfCondition = function (args) {
            return new ifCondition(args);
        };

        /**
         * creates BinaryExpression
         * @param {Object} args - Arguments for creating a new instance creation.
         * @returns {BinaryExpression}
         */
        BallerinaASTFactory.createBinaryExpression = function (args) {
            return new binaryExpression(args);
        };

        /**
         * Create Unary Expression
         * @param {Object} args - Arguments for the creating new expression creation
         * @return {UnaryExpression}
         * */
        BallerinaASTFactory.createUnaryExpression = function (args) {
            return new unaryExpression(args);
        };


        /**
         * creates ArrayMapAccessExpression
         * @param {Object} args - Arguments for creating a new instance creation.
         * @returns {ArrayMapAccessExpression}
         */
        BallerinaASTFactory.createArrayMapAccessExpression = function (args) {
            return new arrayMapAccessExpression(args);
        };

        /**
         * creates ConstantDefinition
         * @param {Object} args - Arguments for creating a new constant definition.
         * @returns {ConstantDefinition}
         */
        BallerinaASTFactory.createConstantDefinition = function (args) {
            return new constantDefinition(args);
        };

        /**
         * creates ThrowStatement
         * @param {Object} args - Arguments for creating a new throw statement.
         * @returns {ThrowStatement}
         */
        BallerinaASTFactory.createThrowStatement = function (args) {
          return new throwStatement(args);
        };

        /**
         * crates CommentStatement
         * @param {Object} args - Arguments for creating a new comment statement.
         * @returns {CommentStatement}
         */
        BallerinaASTFactory.createCommentStatement = function (args) {
          return new commentStatement(args);
        };

        /**
         * instanceof check for BallerinaAstRoot
         * @param {Object} child
         * @returns {boolean}
         */
        BallerinaASTFactory.isBallerinaAstRoot = function (child) {
            return child instanceof ballerinaAstRoot;
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
         * instanceof check for WorkerInvoke
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isWorkerInvokeStatement = function (child) {
            return child instanceof workerInvoke;
        };

        /**
         * instanceof check for WorkerReceive
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isWorkerReceiveStatement = function (child) {
            return child instanceof workerReceive;
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
         * instanceof check for Block Statement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isBlockStatement = function (child) {
            return child instanceof blockStatement;
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
         * instanceof check for StructDefinition
         * @param {ASTNode} child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isStructDefinition = function (child) {
            return child instanceof structDefinition;
        };

        /**
         * instanceof check for TypeMapperDefinition
         * @param {ASTNode} child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTypeMapperDefinition = function (child) {
            return child instanceof typeMapperDefinition;
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
         * is ReferenceTypeInitExpression
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isReferenceTypeInitiExpression = function (child) {
            return child instanceof referenceTypeInitExpression;
        };



        /**
         * is StructType
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isStructType = function (child) {
            return child instanceof structType;
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
         * instanceof check for StructFieldAccessExpression
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isStructFieldAccessExpression = function (child) {
            return child instanceof structFieldAccessExpression;
        };

        /**
         * instanceof check for LeftOperandExpression
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isLeftOperandExpression = function (child) {
            return child instanceof leftOperandExpression;
        };

        /**
         * instanceof check for TypeCastExpression
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isTypeCastExpression = function (child) {
            return child instanceof typeCastExpression;
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
         * instanceof check for ResourceParameter.
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isResourceParameter = function (child) {
            return child instanceof resourceParameter;
        };

        /**
         * instanceof check for ActionInvocationStatement
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isActionInvocationStatement = function(child){
            return child instanceof actionInvocationStatement;
        };

        /**
         * instanceof check for ActionInvocationExpression
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isActionInvocationExpression = function(child){
            return child instanceof actionInvocationExpression;
        };

        /**
         * instanceof check for Argument
         * @param child - Object for instanceof check
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isArgument = function (child) {
            return child instanceof argument;
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

        /**
         * instanceof check for BackQuoteExpression
         * @param child
         * @returns {boolean}
         */
        BallerinaASTFactory.isBackQuoteExpression = function (child) {
            return child instanceof backQuoteExpression;
        };

        /**
         * instanceof check for Assignment
         * @param child
         * @returns {boolean}
         */
        BallerinaASTFactory.isAssignment = function (child) {
            return child instanceof assignment;
        };

        /**
         * instanceof check for Assignment Statement
         * @param child
         * @returns {boolean}
         */
        BallerinaASTFactory.isAssignmentStatement = function (child) {
            return child instanceof assignmentStatement;
        };

        /**
         * instanceof check for BasicLiteralExpression
         * @param child
         * @returns {boolean}
         */
        BallerinaASTFactory.isBasicLiteralExpression = function (child) {
            return child instanceof basicLiteralExpression;
        };

        /**
         * instanceof check for VariableReferenceExpression
         * @param child
         * @returns {boolean}
         */
        BallerinaASTFactory.isVariableReferenceExpression = function (child) {
            return child instanceof variableReferenceExpression;
        };

        /**
         * instanceof check for VariableDefinition
         * @param child
         * @returns {boolean}
         */
        BallerinaASTFactory.isVariableDefinition = function (child) {
            return child instanceof variableDefinition;
        };

        /**
         * instanceof check for RightOperandExpression
         * @param child
         * @returns {boolean}
         */
        BallerinaASTFactory.isRightOperandExpression = function (child) {
            return child instanceof rightOperandExpression;
        };

        /**
         * instanceof check for InstanceCreationExpression
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - True if node is an instance creation, else false.
         */
        BallerinaASTFactory.isInstanceCreationExpression = function (child) {
            return child instanceof instanceCreationExpression;
        };

        /**
         * instanceof check for ThenBody
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isThenBody = function (child) {
            return child instanceof thenBody;
        };

        /**
         * instanceof check for IfCondition
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isIfCondition = function (child) {
            return child instanceof ifCondition;
        };

        /**
         * instanceof check for binaryExpression
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isBinaryExpression = function (child) {
            return child instanceof binaryExpression;
        };

        /**
         * instanceof check for arrayMapAccessExpression
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isArrayMapAccessExpression = function (child) {
            return child instanceof arrayMapAccessExpression;
        };


        /**
         * instanceof check for functionInvocationExpression
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isFunctionInvocationExpression = function (child) {
            return child instanceof functionInvocationExpression;
        };

        /**
         * instanceof check for functionInvocationStatement
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isFunctionInvocationStatement = function (child) {
            return child instanceof functionInvocation;
        };

        /**
         * instanceof check for connectorAction
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isConnectorAction = function (child) {
            return child instanceof connectorAction;
        };

        /**
         * instanceof check for constantDefinition
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isConstantDefinition = function (child) {
            return child instanceof constantDefinition;
        };

        /**
         * instanceof check for variableDefinitionStatement
         * @param {ASTNode} child - The ast node.
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isVariableDefinitionStatement = function (child) {
            return child instanceof variableDefinitionStatement;
        };

        /**
         * instanceof check for throwStatement
         * @param {ASTNode} child - The ast node
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isThrowStatement = function (child) {
            return child instanceof throwStatement;
        };

        /**
         * instanceof check for commentStatement
         * @param {ASTNode} child - The ast node
         * @returns {boolean} - true if same type, else false
         */
        BallerinaASTFactory.isCommentStatement = function (child) {
          return child instanceof commentStatement;
        };

        BallerinaASTFactory.createFromJson = function (jsonNode) {
            var node;
            var nodeType = jsonNode.type;

            if (_.isUndefined(jsonNode.type)) {
                var statement = jsonNode.statement;
                node = BallerinaASTFactory.createAssignment();
            } else {
                switch (nodeType) {
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
                    case 'variable_definition':
                        node = BallerinaASTFactory.createVariableDefinition();
                        break;
                    case 'variable_definition_statement':
                        node = BallerinaASTFactory.createVariableDefinitionStatement();
                        break;
                    case 'argument_declaration':
                        node = BallerinaASTFactory.createResourceParameter();
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
                    case 'return_argument':
                        node = BallerinaASTFactory.createArgument();
                        break;
                    case 'type_name':
                        node = BallerinaASTFactory.createTypeName();
                        break;
                    case 'function_invocation_statement':
                        node = BallerinaASTFactory.createFunctionInvocationStatement();
                        break;
                    case 'function_invocation_expression':
                        node = BallerinaASTFactory.createFunctionInvocationExpression();
                        break;
                    case 'variable_reference_name':
                        node = BallerinaASTFactory.createVariableReferenceExpression();
                        break;
                    case 'variable_reference_expression':
                        node = BallerinaASTFactory.createVariableReferenceExpression();
                        break;
                    case 'action_invocation_expression':
                        node = BallerinaASTFactory.createActionInvocationExpression();
                        break;
                    case 'assignment_statement':
                        node = BallerinaASTFactory.createAssignmentStatement();
                        break;
                    case 'back_quote_expression':
                        node = BallerinaASTFactory.createBackQuoteExpression();
                        break;
                    case 'while_statement' :
                        node = BallerinaASTFactory.createWhileStatement();
                        break;
                    case 'break_statement' :
                        node = BallerinaASTFactory.createBreakStatement();
                        break;
                    case 'basic_literal_expression' :
                        node = BallerinaASTFactory.createBasicLiteralExpression();
                        break;
                    case 'left_operand_expression':
                        node = BallerinaASTFactory.createLeftOperandExpression();
                        break;
                    case 'right_operand_expression':
                        node = BallerinaASTFactory.createRightOperandExpression();
                        break;
                    case 'if_else_statement' :
                        node = BallerinaASTFactory.createIfElseStatement();
                        break;
                    case 'instance_creation_expression':
                        node = BallerinaASTFactory.createInstanceCreationExpression();
                        break;
                    case 'then_body':
                        node = BallerinaASTFactory.createThenBody();
                        break;
                    case 'if_condition':
                        node = BallerinaASTFactory.createIfCondition();
                        break;
                    case 'equal_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "=="});
                        break;
                    case 'greater_than_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : ">"});
                        break;
                    case 'add_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "+"});
                        break;
                    case 'multiplication_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "*"});
                        break;
                    case 'division_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "/"});
                        break;
                    case 'mod_expression' :
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "%"});
                        break;
                    case 'and_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "&&"});
                        break;
                    case 'subtract_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "-"});
                        break;
                    case 'or_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "||"});
                        break;
                    case 'greater_equal_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : ">="});
                        break;
                    case 'less_than_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "<"});
                        break;
                    case 'less_equal_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "<="});
                        break;
                    case 'not_equal_expression':
                        node = BallerinaASTFactory.createBinaryExpression({"operator" : "!="});
                        break;
                    case 'unary_expression':
                        node = BallerinaASTFactory.createUnaryExpression({"operator" : jsonNode.operator});
                        break;
                    case 'array_map_access_expression':
                        node = BallerinaASTFactory.createArrayMapAccessExpression();
                        break;
                    case 'connector':
                        node = BallerinaASTFactory.createConnectorDefinition();
                        break;
                    case 'action':
                        node = BallerinaASTFactory.createConnectorAction();
                        break;
                    case 'constant_definition':
                        node = BallerinaASTFactory.createConstantDefinition();
                        break;
                    case 'struct_definition':
                        node = BallerinaASTFactory.createStructDefinition();
                        break;
                    case 'key_value_expression':
                        node = BallerinaASTFactory.createKeyValueExpression();
                        break;
                    case 'type_casting_expression':
                        node = BallerinaASTFactory.createTypeCastExpression();
                        break;
                    case 'type_mapper_definition':
                        node = BallerinaASTFactory.createTypeMapperDefinition();
                        break;
                    case 'struct_field_access_expression':
                        node = BallerinaASTFactory.createStructFieldAccessExpression();
                        break;
                    case 'block_statement':
                        node = BallerinaASTFactory.createBlockStatement();
                        break;
                    case 'reference_type_init_expression':
                        node = BallerinaASTFactory.createReferenceTypeInitExpression();
                        break;
                    case 'array_init_expression':
                        node = BallerinaASTFactory.createArrayInitExpression();
                        break;
                    case 'action_invocation_statement':
                        node = BallerinaASTFactory.createActionInvocationStatement();
                        break;
                    case 'worker':
                        node = BallerinaASTFactory.createWorkerDeclaration();
                        break;
                    case 'worker_invocation_statement':
                        node = BallerinaASTFactory.createWorkerInvokeStatement();
                        break;
                    case 'worker_reply_statement':
                        node = BallerinaASTFactory.createWorkerReceiveStatement();
                        break;
                    case 'try_catch_statement':
                        node = BallerinaASTFactory.createTryCatchStatement();
                        break;
                    case 'try_block':
                        node = BallerinaASTFactory.createTryStatement();
                        break;
                    case 'catch_block':
                        node = BallerinaASTFactory.createCatchStatement();
                        break;
                    case 'throw_statement':
                        node = BallerinaASTFactory.createThrowStatement();
                        break;
                    case 'comment_statement':
                        node = BallerinaASTFactory.createCommentStatement();
                        break;
                    default:
                        throw "Unknown node definition for " + jsonNode.type;
                }
            }
            node.setLineNumber(jsonNode.line_number, {doSilently: true});
            return node;
        };

        return BallerinaASTFactory;

    });
