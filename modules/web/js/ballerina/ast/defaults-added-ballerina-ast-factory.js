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

define(['lodash', './ballerina-ast-factory'], function (_, BallerinaASTFactory) {

    /**
     * @class DefaultsAddedBallerinaASTFactory
     * @lends DefaultsAddedBallerinaASTFactory
     */
    var DefaultsAddedBallerinaASTFactory = {};

    /**
     * creates ServiceDefinition
     * @param args
     */
    DefaultsAddedBallerinaASTFactory.createServiceDefinition = function (args) {
        var serviceDef = BallerinaASTFactory.createServiceDefinition(args);
        var resourceDef = DefaultsAddedBallerinaASTFactory.createResourceDefinition(args);
        serviceDef.addChild(resourceDef);
        return serviceDef;
    };

    /**
     * creates ResourceDefinition
     * @param args
     */
    DefaultsAddedBallerinaASTFactory.createResourceDefinition = function (args) {
        var resourceDef = BallerinaASTFactory.createResourceDefinition(args);
        var resourceArg = BallerinaASTFactory.createResourceParameter();
        resourceArg.setType("message");
        resourceArg.setIdentifier("m");
        resourceDef.addChild(resourceArg);
        return resourceDef;
    };

    /**
     * creates ConnectorDefinition
     * @param args
     */
    DefaultsAddedBallerinaASTFactory.createConnectorDefinition = function (args) {
        var connectorDef = BallerinaASTFactory.createConnectorDefinition(args);
        var connectorArg = BallerinaASTFactory.createResourceParameter();
        connectorArg.setType("message");
        connectorArg.setIdentifier("m");
        connectorDef.addChild(connectorArg);
        return connectorDef;
    };

    /**
     * creates ConnectorAction
     * @param args
     */
    DefaultsAddedBallerinaASTFactory.createConnectorAction = function (args) {
        var actionDef = BallerinaASTFactory.createConnectorAction(args);
        var actionArg = BallerinaASTFactory.createResourceParameter();
        actionArg.setType("message");
        actionArg.setIdentifier("m");
        actionDef.addChild(actionArg);
        return actionDef;
    };

    /**
     * Creates a variable definition statement with default values.
     * @param {Object} [args] - Args for creating a variable definition statement.
     * @return {VariableDefinitionStatement} - New variable definition statement.
     *
     * @see {@link VariableDefinitionStatement}
     */
    DefaultsAddedBallerinaASTFactory.createVariableDefinitionStatement = function (args) {
        var variableDefinitionStatement = BallerinaASTFactory.createVariableDefinitionStatement(args);
        variableDefinitionStatement.setLeftExpression("int i");
        variableDefinitionStatement.setRightExpression("0");
        return variableDefinitionStatement;
    };

    /**
     * creates typeMapperDefinition with default statement
     * @param {Object} args - object for typeMapperDefinition creation
     * @returns {TypeMapperDefinition}
     */
    DefaultsAddedBallerinaASTFactory.createTypeMapperDefinition = function (args) {
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
    DefaultsAddedBallerinaASTFactory.createAggregatedActionInvocationStatement = function(args) {
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
    DefaultsAddedBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement = function(args) {
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
    DefaultsAddedBallerinaASTFactory.createTryCatchStatement = function (args) {
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
    DefaultsAddedBallerinaASTFactory.createThrowStatement = function (args) {
        var throwStatement = BallerinaASTFactory.createThrowStatement(args);
        throwStatement.addChild(BallerinaASTFactory.createVariableReferenceExpression({variableReferenceName:"e"}));
        return throwStatement;
    };

    return DefaultsAddedBallerinaASTFactory;
});