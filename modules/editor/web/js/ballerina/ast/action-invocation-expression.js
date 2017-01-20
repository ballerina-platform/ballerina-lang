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
        this._actionName = _.get(args, 'action', '');
        this._actionPackageName = _.get(args, 'actionPackageName', '');
        this._actionConnectorName = _.get(args, 'actionConnectorName', '');
        this._actionInvocationReference = _.get(args, 'actionInvocationReference', '');
        this._connectorVariableReference = _.get(args, 'connectorVariableReference', '');
        this._path = _.get(args, 'path', '\"/\"');
        this._messageVariableReference = _.get(args, 'messageRef', 'm');
        this.type = "ActionInvocationExpression";
    };

    ActionInvocationExpression.prototype = Object.create(ActionInvocationStatement.prototype);
    ActionInvocationExpression.prototype.constructor = ActionInvocationExpression;

    /**
     * Set variable accessor
     * @param {string} accessor
     */
    ActionInvocationExpression.prototype.setVariableAccessor = function (accessor) {
        this._variableAccessor = accessor;
    };

    /**
     * Get the variable accessor
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getVariableAccessor = function () {
        return this._variableAccessor;
    };

    /**
     * Set action name
     * @param {string} actionName
     */
    ActionInvocationExpression.prototype.setActionName = function (actionName) {
        this._actionName = actionName;
    };
    /**
     * Get action name
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getActionName = function () {
        return this._actionName;
    };

    /**
     * Set Action package name
     * @param {string} actionPackageName
     */
    ActionInvocationExpression.prototype.setActionPackageName = function (actionPackageName) {
        this._actionPackageName = actionPackageName;
    };
    /**
     * Get Action Package Name
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getActionPackageName = function () {
        return this._actionPackageName;
    };

    /**
     * Set Action Connector name
     * @param {string} actionConnectorName
     */
    ActionInvocationExpression.prototype.setActionConnectorName = function (actionConnectorName) {
        this._actionConnectorName = actionConnectorName;
    };
    /**
     * Get action connector Name
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getActionConnectorName = function () {
        return this._actionConnectorName;
    };

    /**
     * set Connector variable reference
     * @param {string} connectorVariableReference
     */
    ActionInvocationExpression.prototype.setConnectorVariableReference = function (connectorVariableReference) {
        this._connectorVariableReference = connectorVariableReference;
    };
    /**
     * Get Connector variable reference
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getConnectorVariableReference = function () {
        return this._connectorVariableReference;
    };

    /**
     * Set Path
     * @param {string} path
     */
    ActionInvocationExpression.prototype.setPath = function (path) {
        this._path = path;
    };
    /**
     * Get Path
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getPath = function () {
        return this._path;
    };

    /**
     * Set Message Variable Reference
     * @param {string} messageVariableReference
     */
    ActionInvocationExpression.prototype.setMessageVariableReference = function (messageVariableReference) {
        this._messageVariableReference = messageVariableReference;
    };

    /**
     * Get Message variable reference
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getMessageVariableReference = function () {
        return this._messageVariableReference;
    };

    /**
     * initialize ActionInvocationExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    ActionInvocationExpression.prototype.initFromJson = function (jsonNode) {
        this.setConnector(_.head(this.getInvocationConnector(_.head(jsonNode.children).variable_reference_name)));
        this.setActionName(jsonNode.action_name);
        this.setActionPackageName(jsonNode.action_pkg_name);
        this.setActionConnectorName(jsonNode.action_connector_name);
        this.setConnectorVariableReference(jsonNode.children[0].variable_reference_name);

        var pathNode = jsonNode.children[1];
        //TODO : Need to remove this if/else ladder by delegating expression string calculation to child classes
        if (pathNode.type == "basic_literal_expression") {
            if(pathNode.basic_literal_type == "string") {
                // Adding double quotes if it is a string.
                this.setPath("\"" + pathNode.basic_literal_value + "\"");
            } else {
                this.setPath(pathNode.basic_literal_value);
            }
        } else if (pathNode.type == "variable_reference_expression") {
            this.setPath(pathNode.variable_reference_name);
        } else {
            var child = this.getFactory().createFromJson(pathNode);
            child.initFromJson(pathNode);
            this.setPath(child.getExpression());
        }

        this.setMessageVariableReference(jsonNode.children[2].variable_reference_name);
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