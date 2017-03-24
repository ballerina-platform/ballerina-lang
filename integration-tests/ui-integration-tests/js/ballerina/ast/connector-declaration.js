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
define(['lodash', './node', '../utils/common-utils'], function(_, ASTNode, CommonUtils){

    /**
     * Defines a connector declaration AST node.
     * Default source will be as follows : http:HTTPConnector ep = new http:HTTPConnector("http://localhost:9090");
     * @param {Object} options - arguments for a connector.
     * @param {string} [options.connectorName="HTTPConnector"] - The name of the connector.
     * @param {string} [options.connectorVariable="ep"] - Variable identifier for the connector.
     * @param {string} [options.connectorPackageName="http"] - Package name of the connector.
     * @param {number} [options.timeout=""] - Timeout value.
     * @param {string} [options.uri="http://localhost:9090"] - The endpoint url.
     * @constructor
     * @augments ASTNode
     */
    var ConnectorDeclaration = function(options) {
        ASTNode.call(this, "ConnectorDeclaration");
        this._connectorName = _.get(options, "connectorName", "ClientConnector");
        this._connectorVariable = _.get(options, "connectorVariable");
        this._connectorType = _.get(options, "connectorType", "ConnectorDeclaration");
        this._connectorPkgName = _.get(options, "connectorPackageName", "http");
        this._timeout = _.get(options, "timeout", "");
        this._params = _.get(options, "params", '\"http://localhost:9090\"');
        this._arguments = _.get(options, "arguments", []);
        this._connectorActionsReference = _.get(options, "connectorActionsReference", []);
        this._fullPackageName = _.get(options, 'fullPackageName', '');
    };

    ConnectorDeclaration.prototype = Object.create(ASTNode.prototype);
    ConnectorDeclaration.prototype.constructor = ConnectorDeclaration;

    ConnectorDeclaration.prototype.canBeConnectorOf = function (action) {
        var BallerinaASTFactory = this.getFactory();
    };
    ConnectorDeclaration.prototype.setConnectorName = function (name, options) {
        this.setAttribute('_connectorName', name, options);
    };
    ConnectorDeclaration.prototype.addConnectorActionReference = function (object) {
        this._connectorActionsReference.push(object);
    };
    ConnectorDeclaration.prototype.removeConnectorActionReference = function (id) {
        _.pullAllBy(this._connectorActionsReference, [{ 'id': id }], 'id');
    };
    ConnectorDeclaration.prototype.setConnectorVariable = function (connectorVariable, options) {
        this.setAttribute('_connectorVariable', connectorVariable, options);
    };

    ConnectorDeclaration.prototype.setConnectorType = function (type, options) {
        this.setAttribute('_connectorType', type, options);
    };
    ConnectorDeclaration.prototype.setConnectorPkgName = function (pkgName, options) {
        this.setAttribute('_connectorPkgName', pkgName, options);
    };
    ConnectorDeclaration.prototype.setFullPackageName = function(fullPkgName, options){
        this.setAttribute('_fullPackageName', fullPkgName, options);
    };
    /**
     * Set parameters for the connector
     *
     * @param {string} params String with comma separable values
     * @param {object} options
     * */
    ConnectorDeclaration.prototype.setParams = function (params, options) {
        this.setAttribute("_params", params, options);
    };

    /**
     * Set connector details from the given expression.
     *
     * @param {string} expression Expression entered by user.
     * @param {object} options
     * */
    ConnectorDeclaration.prototype.setConnectorExpression = function (expression, options) {
        if (!_.isNil(expression) && expression !== "") {
            var firstAssignmentIndex = expression.indexOf("=");
            var leftSide = expression.slice(0, firstAssignmentIndex);
            var rightSide = expression.slice((firstAssignmentIndex + 1), expression.length);

            if (leftSide) {
                leftSide = leftSide.trim();
                this.setAttribute("_connectorPkgName", leftSide.includes(":") ?
                    leftSide.split(":", 1)[0]
                    : "");

                this.setAttribute("_connectorName", leftSide.includes(":") ?
                    leftSide.split(":", 2)[1].split(" ", 1)[0]
                    : (leftSide.indexOf(" ") !== (leftSide.length - 1) ? leftSide.split(" ", 1)[0] : ""));

                this.setAttribute("_connectorVariable", leftSide.includes(":") ?
                    leftSide.split(":", 2)[1].split(" ", 2)[1]
                    : (leftSide.indexOf(" ") !== (leftSide.length - 1) ? leftSide.split(" ", 2)[1] : ""));
            }
            if (rightSide) {
                rightSide = rightSide.trim();
                this.setAttribute("_params", rightSide.includes("(") ?
                    rightSide.split("(", 2)[1].slice(0, (rightSide.split("(", 2)[1].length - 1))
                    : "", options);
            }
        }
    };
    ConnectorDeclaration.prototype.setTimeout = function (timeout, options) {
        this.setAttribute("_timeout", timeout, options);
    };

    ConnectorDeclaration.prototype.setArguments = function (argument) {
        this._arguments.push(argument);
    };

    ConnectorDeclaration.prototype.getParams = function(){
        return this._params;
    };

    ConnectorDeclaration.prototype.getArguments = function () {
        return this._arguments;
    };

    ConnectorDeclaration.prototype.getConnectorName = function () {
        return this._connectorName;
    };
    
    ConnectorDeclaration.prototype.getConnectorActionsReference = function () {
        return this._connectorActionsReference;
    };

    ConnectorDeclaration.prototype.getConnectorVariable = function () {
        return this._connectorVariable;
    };

    ConnectorDeclaration.prototype.getConnectorType = function () {
        return this._connectorType;
    };
    
    ConnectorDeclaration.prototype.getConnectorPkgName = function () {
        return this._connectorPkgName;
    };

    ConnectorDeclaration.prototype.getFullPackageName = function () {
        return this._fullPackageName;
    };
    /**
     * This will return connector expression
     *
     * @return {string} expression
     * */
    ConnectorDeclaration.prototype.getConnectorExpression = function () {
        var self = this;
        return generateExpression(self);
    };
    ConnectorDeclaration.prototype.getTimeout = function () {
        return this._timeout;
    };

    /**
     * initialize ConnectorDeclaration from json object
     * @param {Object} jsonNode to initialize from
     */
    ConnectorDeclaration.prototype.initFromJson = function (jsonNode) {
        if(jsonNode.children[1].connector_name) {
            var connectorName = jsonNode.children[1].connector_name.split(":");
            if (connectorName.length > 1) {
                this.setConnectorName(connectorName[1], {doSilently: true});
                this.setConnectorPkgName(connectorName[0], {doSilently: true});
            }
            else {
                //if no package name is available. i.e. : connector definition is in the same package
                this.setConnectorName(jsonNode.children[1].connector_name, {doSilently: true});
                this.setConnectorPkgName(undefined, {doSilently: true});
            }
        }
        this.setConnectorVariable(jsonNode.children[0].variable_reference_name, {doSilently: true});
        this.setConnectorType('ConnectorDeclaration', {doSilently: true});
        var self = this;
        self._arguments = [];
        if (!_.isUndefined(jsonNode.children[1].arguments)) {
            _.each(jsonNode.children[1].arguments, function (argNode) {
                var arg = self.getFactory().createFromJson(argNode);
                arg.initFromJson(argNode);
                self.setArguments(arg);
            });
        }
        generateParamString(self);
    };

    /**
     * Generate Param String
     *
     * @param {object} self Connector declaration
     * */
    var generateParamString = function (self) {
        self._params = "";
        for (var i = 0; i < self._arguments.length; i++) {
            self._params += self._arguments[i].getExpression();
            if (i !== (self._arguments.length - 1)) {
                self._params += ",";
            }
        }
    };

    /**
     * Generate Expression to Show on the edit textbox.
     *
     * @param {object} self Connector declaration
     * @return {string} expression
     * */
    var generateExpression = function (self) {
        var expression = "";
        if (!shouldSkipPackageName(self._connectorPkgName)) {
            expression += self._connectorPkgName + ":";
        }

        if (!_.isUndefined(self._connectorName) && !_.isNil(self._connectorName)) {
            expression += self._connectorName + " ";
        }

        if (!_.isUndefined(self._connectorVariable) && !_.isNil(self._connectorVariable)) {
            expression += self._connectorVariable + " = ";
        }

        expression += "create ";

        if (!shouldSkipPackageName(self._connectorPkgName)) {
            expression += self._connectorPkgName + ":";
        }

        if (!_.isUndefined(self._connectorName) && !_.isNil(self._connectorName)) {
            expression += self._connectorName;
        }

        expression += "(";
        if (!_.isUndefined(self._params) && !_.isNil(self._params)) {
            expression += self._params;
        }
        expression += ")";
        return expression;
    };

    var shouldSkipPackageName = function(packageName) {
        return _.isUndefined(packageName) || _.isNil(packageName) ||
            _.isEqual(packageName, 'Current Package') || _.isEqual(packageName, '')
    }

    /**
     * @inheritDoc
     * @override
     */
    ConnectorDeclaration.prototype.generateUniqueIdentifiers = function () {
        var self = this;
        var uniqueIDGenObject = {
            node: this,
            attributes: [{
                defaultValue: "endpoint",
                setter: this.setConnectorVariable,
                getter: this.getConnectorVariable,
                parents: [{
                    // resource/service definition.
                    node: this.parent,
                    getChildrenFunc: this.parent.getConnectionDeclarations,
                    getter: this.getConnectorVariable
                }]
            }]
        };

        if (this.getFactory().isResourceDefinition(this.parent)) {
            uniqueIDGenObject.attributes[0].parents.push({
                // service definition
                node: this.parent.parent,
                getChildrenFunc: this.parent.getConnectionDeclarations,
                getter: this.getConnectorVariable
            })
        } else if (this.getFactory().isServiceDefinition(this.parent)) {
            var resourceDefinitions = this.parent.getResourceDefinitions();
            _.forEach(resourceDefinitions, function (resourceDefinition) {
                uniqueIDGenObject.attributes[0].parents.push({
                    // resource definition
                    node: resourceDefinition,
                    getChildrenFunc: self.parent.getConnectionDeclarations,
                    getter: self.getConnectorVariable
                })
            });
        } else if (this.getFactory().isConnectorAction(this.parent)) {
            uniqueIDGenObject.attributes[0].parents.push({
                // connector definition
                node: this.parent.parent,
                getChildrenFunc: this.parent.getConnectionDeclarations,
                getter: this.getConnectorVariable
            })
        } else if (this.getFactory().isConnectorDefinition(this.parent)) {
            var connectorActions = this.parent.getConnectorActionDefinitions();
            _.forEach(connectorActions, function (connectionAction) {
                uniqueIDGenObject.attributes[0].parents.push({
                    // connector action definition
                    node: connectionAction,
                    getChildrenFunc: self.parent.getConnectionDeclarations,
                    getter: self.getConnectorVariable
                })
            });
        }

        CommonUtils.generateUniqueIdentifier(uniqueIDGenObject);
    };

    return ConnectorDeclaration;
});