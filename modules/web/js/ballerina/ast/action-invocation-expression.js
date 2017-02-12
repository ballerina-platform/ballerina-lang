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
define(['lodash', 'log', './statement'], function (_, log, Statement) {

    /**
     * Class to represent a action invocation to ballerina.
     * @param args
     * @constructor
     */
    var ActionInvocationExpression = function (args) {
        // TODO: Implement the expression visitor and extend from Expression
        Statement.call(this, "ActionInvocationExpression");
        this._variableAccessor = _.get(args, 'accessor', 'var1');
        this._actionName = _.get(args, 'action', '');
        this._actionPackageName = _.get(args, 'actionPackageName', '');
        this._actionConnectorName = _.get(args, 'actionConnectorName', '');
        this._connectorVariableReference = _.get(args, 'connectorVariableReference', undefined);
        this._arguments = _.get(args, "arguments", []);
        this._connector = _.get(args, 'connector');
        //create the default expression for action invocation
        this.setExpression(this.generateExpression());
        this.type = "ActionInvocationExpression";
    };

    ActionInvocationExpression.prototype = Object.create(Statement.prototype);
    ActionInvocationExpression.prototype.constructor = ActionInvocationExpression;

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

    /**
     * set Connector variable reference
     * @param {string} connectorVariableReference
     */
    ActionInvocationExpression.prototype.setConnectorVariableReference = function (connectorVariableReference, options) {
        if(_.isUndefined(this._connectorVariableReference)){
            //update the expression when adding connector variable
            this.setExpression(insertConnectorVariableToExpression(true, connectorVariableReference, this.getExpression()), options);
        } else {
            // update the expression when modifying connector variable
            this.setExpression(insertConnectorVariableToExpression(false, connectorVariableReference, this.getExpression()), options);
        }
        this.setAttribute('_connectorVariableReference', connectorVariableReference, options);
    };
    /**
     * Get Connector variable reference
     * @returns {string}
     */
    ActionInvocationExpression.prototype.getConnectorVariableReference = function () {
        return this._connectorVariableReference;
    };

    ActionInvocationExpression.prototype.getConnector = function(){
        return this._connector;
    };

    ActionInvocationExpression.prototype.setConnector = function(connector, options){
        if(!_.isNil(connector)){
            this.setAttribute("_connector", connector, options);
        }
    };

    /**
     * initialize ActionInvocationExpression from json object
     * @param {Object} jsonNode to initialize from
     */
    ActionInvocationExpression.prototype.initFromJson = function (jsonNode) {
        var action_invocation_expression = jsonNode.children[1];
        if(!_.isUndefined(action_invocation_expression.children) && !_.isUndefined(action_invocation_expression.children[0])) {
            var connector = this.getInvocationConnector(action_invocation_expression.children[0].variable_reference_name);
            this.setConnector(connector, {doSilently: true});
        }
        this.setActionName(action_invocation_expression.action_name, {doSilently: true});
        this.setActionPackageName(action_invocation_expression.action_pkg_name, {doSilently: true});
        this.setActionConnectorName(action_invocation_expression.action_connector_name, {doSilently: true});
        if(!_.isUndefined(jsonNode.children[0])) {
            this.setVariableAccessor(jsonNode.children[0], {doSilently: true});
        }else{
            this._isActionInvocationStatement = true;
        }
        if(!_.isUndefined(this.getConnector())) {
            //if connector is available, arguments needs to be added from second in children
            var argStartIndex = 1;
            this.setConnectorVariableReference(action_invocation_expression.children[0].variable_reference_name, {doSilently: true});
        } else {
            var argStartIndex = 0;
        }

        var self = this;
        
        _.each(_.slice(action_invocation_expression.children, argStartIndex), function (argNode) {
            var arg = self.getFactory().createFromJson(argNode);
            arg.initFromJson(argNode);
            self.addArgument(arg);
        });

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

        var self = this;

        for (var itr = 0; itr < arguments.length; itr++) {
            argsString += arguments[itr].getExpression();

            if (itr !== arguments.length - 1) {
                argsString += ' , ';
            }
        }

        if (!_.isUndefined(this.getConnectorVariableReference())) {
            _.isEmpty(argsString)
                ? (argsString = this.getConnectorVariableReference())
                : (argsString = this.getConnectorVariableReference() + ' , ' + argsString);
        }

        if (!_.isNil(this.getActionPackageName()) && !_.isEmpty(this.getActionPackageName().trim())) {
            if(this._isActionInvocationStatement){
                return this.getActionPackageName() + ':' + this.getActionConnectorName() + '.' + this.getActionName() +
                    '(' + argsString +  ')';
            }else{
                return this.getVariableAccessor() + " = " + this.getActionPackageName() + ':' + this.getActionConnectorName() + '.' + this.getActionName() +
                    '(' + argsString +  ')';
            }

        } else {
            if(this._isActionInvocationStatement){
                return this.getActionConnectorName() + '.' + this.getActionName() +
                    '(' + argsString +  ')';
            }else{
                return this.getVariableAccessor() + " = " + this.getActionConnectorName() + '.' + this.getActionName() +
                    '(' + argsString +  ')';
            }
        }
    };


    function insertConnectorVariableToExpression (isNewConnectorVariable, connectorVariable, expression) {
        if (isNewConnectorVariable) {
            var indexStartArgs = _.indexOf(expression, '(') + 1;
            var indexEndArgs = _.indexOf(expression, ')');
            if (indexStartArgs == indexEndArgs) {
                //if there are no arguments
                var newExpression = [expression.slice(0, indexStartArgs), connectorVariable, expression.slice(indexStartArgs)].join('');
            } else {
                var newExpression = [expression.slice(0, indexStartArgs), connectorVariable, ' , ', expression.slice(indexStartArgs)].join('');
            }
        } else {
            var indexStartConnectorVar = _.indexOf(expression, '(') + 1;
            var indexEndConnectorVar = _.indexOf(expression, ',');
            var newExpression = [expression.slice(0, indexStartConnectorVar), connectorVariable, expression.slice(indexEndConnectorVar)].join('');
        }
        return newExpression;
    }

    // TODO: Implement the expression visitor and extend from Expression
    ActionInvocationExpression.prototype.setExpression = function (expression, options) {
        if(!_.isUndefined(expression)){
            this.setAttribute('_expression', expression, options);
        }
    };

    ActionInvocationExpression.prototype.getExpression = function () {
        return this._expression;
    };

    ActionInvocationExpression.prototype.setVariableAccessor = function (accessorChild, options) {
        this.setAttribute('_variableAccessor',
            (!_.isNil(accessorChild.variable_def_options) ? (accessorChild.variable_def_options.type_name + " ") : "")
            + accessorChild.variable_reference_name, options);
    };

    ActionInvocationExpression.prototype.getVariableAccessor = function (accessor) {
        return this._variableAccessor;
    };

    return ActionInvocationExpression;

});