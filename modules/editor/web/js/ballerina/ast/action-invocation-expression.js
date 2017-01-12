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
define(['lodash', 'log', './action-invocation-statement'], function (_, log, ActionInvocationStatement) {

    /**
     * Class to represent a action invocation to ballerina.
     * @param args
     * @constructor
     */
    var ActionInvocationExpression = function (args) {
        ActionInvocationStatement.call(this, args);
        this._variableAccessor = _.get(args, 'accessor');
        this._actionName = _.get(args, 'actionName');
        this._actionPackageName = _.get(args, 'actionPackageName');
        this._actionConnectorName = _.get(args, 'actionConnectorName');
        this._actionInvocationReference = _.get(args, 'actionInvocationReference');
        this.type = "ActionInvocationExpression";
    };

    ActionInvocationExpression.prototype = Object.create(ActionInvocationStatement.prototype);
    ActionInvocationExpression.prototype.constructor = ActionInvocationExpression;

    ActionInvocationExpression.prototype.setVariableAccessor = function (accessor) {
        this._variableAccessor = accessor;
    };

    ActionInvocationExpression.prototype.getVariableAccessor = function () {
        return this._variableAccessor;
    };

    /**
     * initialize ActionInvocationExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.service_name] - Name of the service definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     */
    ActionInvocationExpression.prototype.initFromJson = function (jsonNode) {
        //TODO : Need to refactor the whole method
        this.setConnector(_.head(this.getInvocationConnector(_.head(jsonNode.children).variable_reference_name)));
        this.setAction("post");
        this.setVariableAccessor("response");
        this.setMessage("m");
        this.setPath("/");
    };

    ActionInvocationExpression.prototype.getInvocationConnector = function (variable_reference_name) {
        //TODO : Need to refactor the whole method
        var parent = this.getParent();
        var factory = this.getFactory();
        while (!factory.isBallerinaAstRoot(parent)) {
            if (factory.isResourceDefinition(parent) || factory.isFunctionDefinition(parent) || factory.isServiceDefinition(parent)) {
                break;
            }
            parent = parent.getParent();
        }
        var self = this;
        var connectorReference = _.filter(parent.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === variable_reference_name));
        });
        return connectorReference;
    };

    return ActionInvocationExpression;

});