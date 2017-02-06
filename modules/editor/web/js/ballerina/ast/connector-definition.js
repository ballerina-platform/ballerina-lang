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
define(['lodash', './node', 'log'], function(_, ASTNode, log){

    /**
     * Constructor for ConnectorDefinition
     * @param {object} args - Constructor arguments
     * @constructor
     */
    var ConnectorDefinition = function(args) {
        ASTNode.call(this, "ConnectorDefinition");
        this.BallerinaASTFactory = this.getFactory();
        this.connector_name = _.get(args, 'connector_name', 'newConnector');
        this.annotations = _.get(args, 'annotations', []);
        this.arguments = _.get(args, 'arguments', []);
    };

    ConnectorDefinition.prototype = Object.create(ASTNode.prototype);
    ConnectorDefinition.prototype.constructor = ConnectorDefinition;

    /**
     * Get the name of connector
     * @return {string} connector_name - Connector Name
     */
    ConnectorDefinition.prototype.getConnectorName = function () {
        return this.connector_name
    };

    /**
     * Get the annotations
     * @return {string[]} annotations - Connector Annotations
     */
    ConnectorDefinition.prototype.getAnnotations = function () {
        return this.annotations
    };

    /**
     * Get the connector Arguments
     * @return {Object[]} arguments - Connector Arguments
     */
    ConnectorDefinition.prototype.getArguments = function () {
        var argumentsList = [];
        var self = this;
        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isArgument(child)) {
                argumentsList.push(child);
            }
        });
        return argumentsList;
    };

    /**
     * Adds new argument to the connector definition.
     * @param type - The type of the argument.
     * @param identifier - The identifier of the argument.
     */
    ConnectorDefinition.prototype.addArgument = function(type, identifier) {
        //creating argument
        var newArgument = this.BallerinaASTFactory.createArgument();
        newArgument.setType(type);
        newArgument.setIdentifier(identifier);

        var self = this;

        // Get the index of the last argument declaration.
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isArgument(child);
        });

        this.addChild(newArgument, index + 1);
    };

    /**
     * Set the Connector name
     * @param {string} name - Connector Name
     */
    ConnectorDefinition.prototype.setConnectorName = function (name, options) {
        if (!_.isNil(name) && ASTNode.isValidIdentifier(name)) {
            this.setAttribute('connector_name', name, options);
        } else {
            var errorString = "Invalid connector name: " + name;
            log.error(errorString);
            throw errorString;
        }
    };

    /**
     * Set the connector annotations
     * @param {string[]} annotations - Connector Annotations
     */
    ConnectorDefinition.prototype.setAnnotations = function (annotations, options) {
        if (!_.isNil(annotations)) {
            this.setAttribute('annotations', annotations, options);
        } else {
            log.warn('Trying to set a null or undefined array to annotations');
        }
    };

    /**
     * Set the Connector Arguments
     * @param {object[]} arguments - Connector Arguments
     */
    ConnectorDefinition.prototype.setArguments = function (arguments, options) {
        if (!_.isNil(arguments)) {
            this.setAttribute('arguments', arguments, options);
        } else {
            log.warn('Trying to set a null or undefined array to arguments');
        }
    };

    /**
     * Get the variable declarations
     * @return {VariableDeclaration[]}
     */
    ConnectorDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    /**
     * Adds new variable declaration.
     */
    ConnectorDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
        var self = this;

        // Get the index of the last variable declaration.
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child);
        });

        // index = -1 when there are not any variable declarations, hence get the index for connector
        // declarations.
        if (index == -1) {
            index = _.findLastIndex(this.getChildren(), function (child) {
                return self.BallerinaASTFactory.isConnectorDeclaration(child);
            });
        }

        this.addChild(newVariableDeclaration, index + 1);
    };

    /**
     * Remove a variable declaration.
     */
    ConnectorDefinition.prototype.removeVariableDeclaration = function (variableDeclaration) {
        this.removeChild(variableDeclaration)
    };

    /**
     * initialize ConnectorDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.connector_name] - Name of the service definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     */
    ConnectorDefinition.prototype.initFromJson = function (jsonNode) {
        var self = this;
        this.setConnectorName(jsonNode.connector_name, {doSilently: true});

        // Populate the annotations array
        for (var itr = 0; itr < this.getAnnotations().length; itr ++) {
            var key = this.getAnnotations()[itr].key;
            for (var itrInner = 0; itrInner < jsonNode.annotations.length; itrInner ++) {
                if (jsonNode.annotations[itrInner].annotation_name === key) {
                    this._annotations[itr].value = jsonNode.annotations[itrInner].annotation_value;
                }
            }
        }
        _.each(jsonNode.children, function (childNode) {
            var child = self.BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    ConnectorDefinition.prototype.canBeParentOf = function (node) {
        return this.BallerinaASTFactory.isConnectorAction(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isConnectorDeclaration(node);
    };

    return ConnectorDefinition;
});
