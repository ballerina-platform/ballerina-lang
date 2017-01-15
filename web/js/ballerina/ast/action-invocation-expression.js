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
        this._variableAccessor = _.get(args, 'accessor', '');
        this._actionName = _.get(args, 'actionName', '');
        this._actionPackageName = _.get(args, 'actionPackageName', '');
        this._actionConnectorName = _.get(args, 'actionConnectorName', '');
        this._actionInvocationReference = _.get(args, 'actionInvocationReference', '');
        this._connectorVariableReference = _.get(args, 'connectorVariableReference', '');
        this._path = _.get(args, 'path', '/');
        this._messageVariableReference = _.get(args, 'messageRef', 'm');
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

    ActionInvocationExpression.prototype.setActionName = function (actionName) {
        this._actionName = actionName;
    };
    ActionInvocationExpression.prototype.getActionName = function () {
        return this._actionName;
    };

    ActionInvocationExpression.prototype.setActionPackageName = function (actionPackageName) {
        this._actionPackageName = actionPackageName;
    };
    ActionInvocationExpression.prototype.getActionPackageName = function () {
        return this._actionPackageName;
    };

    ActionInvocationExpression.prototype.setActionConnectorName = function (actionConnectorName) {
        this._actionConnectorName = actionConnectorName;
    };
    ActionInvocationExpression.prototype.getActionConnectorName = function () {
        return this._actionConnectorName;
    };

    ActionInvocationExpression.prototype.setConnectorVariableReference = function (connectorVariableReference) {
        this._connectorVariableReference = connectorVariableReference;
    };
    ActionInvocationExpression.prototype.getConnectorVariableReference = function () {
        return this._connectorVariableReference;
    };

    ActionInvocationExpression.prototype.setPath = function (path) {
        this._path = path;
    };
    ActionInvocationExpression.prototype.getPath = function () {
        return this._path;
    };

    ActionInvocationExpression.prototype.setMessageVariableReference = function (messageVariableReference) {
        this._messageVariableReference = messageVariableReference;
    };

    ActionInvocationExpression.prototype.getMessageVariableReference = function () {
        return this._messageVariableReference;
    };

    /**
     * initialize ActionInvocationExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    ActionInvocationExpression.prototype.initFromJson = function (jsonNode) {
        this._actionName = jsonNode.action_name;
        this._actionPackageName = jsonNode.action_pkg_name;
        this._actionConnectorName = jsonNode.action_connector_name;
        this._connectorVariableReference = jsonNode.children[0].variable_reference_name;
        this._path = jsonNode.children[1].basic_literal_value;
        this._messageVariableReference = jsonNode.children[2].variable_reference_name;
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