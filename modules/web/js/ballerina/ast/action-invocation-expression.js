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
define(['lodash', 'log', './expression'], function (_, log, Expression) {

    /**
     * Class to represent a action invocation to ballerina.
     * @param args
     * @constructor
     */
    var ActionInvocationExpression = function (args) {
        Expression.call(this, "ActionInvocationExpression");
        this._actionName = _.get(args, 'action', undefined);
        this._actionPackageName = _.get(args, 'actionPackageName', undefined);
        this._actionConnectorName = _.get(args, 'actionConnectorName', undefined);
        this._arguments = _.get(args, "arguments", []);
        this._connector = _.get(args, 'connector');
        this._fullPackageName = _.get(args, 'fullPackageName', undefined);
        //create the default expression for action invocation
        this.setExpression(this.generateExpression());
        this.type = "ActionInvocationExpression";
    };

    ActionInvocationExpression.prototype = Object.create(Expression.prototype);
    ActionInvocationExpression.prototype.constructor = ActionInvocationExpression;

    /**
     * Set the full package name
     * @param {String} fullPkgName full package name
     * @param {Object} options
     * */
    ActionInvocationExpression.prototype.setFullPackageName = function (fullPkgName, options){
        this.setAttribute('_fullPackageName', fullPkgName, options);
    };

    /**
     * Get the full package name
     * @return {String} full package name
     * */
    ActionInvocationExpression.prototype.getFullPackageName = function (){
        return this._fullPackageName;
    };

    /**
     * Set action name
     * @param {string} actionName
     */
    ActionInvocationExpression.prototype.setActionName = function (actionName, options) {
        this.setAttribute('_actionName', actionName, options);
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
    ActionInvocationExpression.prototype.setActionPackageName = function (actionPackageName, options) {
        this.setAttribute('_actionPackageName', actionPackageName, options);
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
    ActionInvocationExpression.prototype.setActionConnectorName = function (actionConnectorName, options) {
        this.setAttribute('_actionConnectorName', actionConnectorName, options);
    };
    /**
     * Get action connector Name
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getActionConnectorName = function () {
        return this._actionConnectorName;
    };

    ActionInvocationExpression.prototype.getConnector = function(){
        return this._connector;
    };

    ActionInvocationExpression.prototype.setConnector = function(connector, options){
        if(!_.isNil(connector)){
            this.setAttribute("_connector", connector, options);
            this.setExpression(this.generateExpression());
        }
    };

    /**
     * initialize ActionInvocationExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    ActionInvocationExpression.prototype.initFromJson = function (jsonNode) {
        this.setActionName(jsonNode.action_name, {doSilently: true});
        this.setActionPackageName(jsonNode.action_pkg_name, {doSilently: true});
        this.setActionConnectorName(jsonNode.action_connector_name, {doSilently: true});

        if (jsonNode.children.length > 0) {
            var connectorExp = this.getFactory().createFromJson(jsonNode.children[0]);
            connectorExp.initFromJson(jsonNode.children[0]);
            var connector = this.getInvocationConnector(connectorExp.getExpression());
            this.setConnector(connector, {doSilently: true});

            var self = this;

            //get params apart from first param which is the connector variable
            _.each(_.slice(jsonNode.children, 1), function (argNode) {
                var arg = self.getFactory().createFromJson(argNode);
                arg.initFromJson(argNode);
                self.addArgument(arg);
            });
        }
        this.setExpression(this.generateExpression(), {doSilently: true});
    };

    ActionInvocationExpression.prototype.addArgument = function (argument){
        this._arguments.push(argument);
    };

    ActionInvocationExpression.prototype.getArguments = function (){
        return this._arguments;
    };

    ActionInvocationExpression.prototype.getInvocationConnector = function (connectorVariable) {
        var parent = this.getParent();
        var factory = this.getFactory();

        // Iteratively we find the most atomic parent node which can hold a connector
        // ATM those are [FunctionDefinition, ResourceDefinition, ConnectorAction]
        while (!factory.isBallerinaAstRoot(parent)) {
            if (factory.isResourceDefinition(parent) || factory.isFunctionDefinition(parent)
                || factory.isConnectorAction(parent)) {
                break;
            }
            parent = parent.getParent();
        }

        var connector = parent.getConnectorByName(connectorVariable);
        return connector;
    };

    /**
     * Get the action invocation statement
     * @return {string} action invocation statement
     */
    ActionInvocationExpression.prototype.generateExpression = function () {
        var argsString = "";
        var arguments = this.getArguments();

        for (var itr = 0; itr < arguments.length; itr++) {

            // TODO: we need to refactor this along with the action invocation argument types as well
            if (this.getFactory().isExpression(arguments[itr])) {
                argsString += arguments[itr].getExpression();
            } else if (this.getFactory().isResourceParameter(arguments[itr])) {
                argsString += arguments[itr].getParameterAsString();
            }

            if (itr !== arguments.length - 1) {
                argsString += ' , ';
            }
        }

        if (!_.isUndefined(this.getConnector()) && !_.isNil(this.getConnector())) {
            if (!_.isEmpty(argsString)) {
                argsString = this.getConnector().getConnectorVariable() + " , " + argsString;
            } else {
                argsString = this.getConnector().getConnectorVariable();
            }
        }

        var expression = this.getActionConnectorName() + '.' + this.getActionName() + '(' + argsString +  ')';
        if(!_.isUndefined(this.getActionPackageName()) && !_.isNil(this.getActionPackageName())
                && !_.isEqual(this.getActionPackageName(), 'Current Package')){
            expression = this.getActionPackageName() + ":" + expression;
        }
        return expression;
    };

    ActionInvocationExpression.prototype.setExpression = function (expression, options) {
        if(!_.isUndefined(expression)){
            this.setAttribute('_expression', expression, options);
        }
    };

    ActionInvocationExpression.prototype.getExpression = function () {
        return this._expression;
    };

    return ActionInvocationExpression;

});