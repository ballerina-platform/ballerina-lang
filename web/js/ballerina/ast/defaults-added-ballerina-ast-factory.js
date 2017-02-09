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

    return DefaultsAddedBallerinaASTFactory;
});