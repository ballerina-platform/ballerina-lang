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
define(['lodash', './node', 'log', '../utils/common-utils'], function(_, ASTNode, log, CommonUtils){

    /**
     * Constructor for ConnectorDefinition
     * @param {object} args - Constructor arguments
     * @constructor
     */
    var ConnectorDefinition = function(args) {
        ASTNode.call(this, "ConnectorDefinition");
        this.BallerinaASTFactory = this.getFactory();
        this.connector_name = _.get(args, 'connector_name');
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
     * Removes an argument from a connector definition.
     * @param identifier - The identifier of the argument.
     * @return {Array} - The removed argument.
     */
    ConnectorDefinition.prototype.removeArgument = function(identifier) {
        var self = this;
        _.remove(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isArgument(child) && child.getIdentifier() === identifier;
        });
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
     * Override the super call to addChild
     * @param child
     * @param index
     */
    ConnectorDefinition.prototype.addChild = function (child, index) {
        if (this.BallerinaASTFactory.isConnectorDeclaration(child)) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
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
     * Gets the variable definition statements of the connector definition.
     * @return {VariableDefinitionStatement[]}
     */
    ConnectorDefinition.prototype.getVariableDefinitionStatements = function () {
        var variableDefinitionStatements = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isVariableDefinitionStatement(child)) {
                variableDefinitionStatements.push(child);
            }
        });
        return variableDefinitionStatements;
    };

    /**
     * Adds new variable definition statement.
     * @param {string} bType - The ballerina type of the variable definition statement.
     * @param {string} identifier - The identifier of the variable definition statement.
     * @param {string} assignedValue - The right hand expression.
     */
    ConnectorDefinition.prototype.addVariableDefinitionStatement = function (bType, identifier, assignedValue) {

        // Check is identifier is not null or empty.
        if (_.isNil(identifier) || _.isEmpty(identifier)) {
            var errorStringOfEmptyIdentifier = "A variable definition requires an identifier.";
            log.error(errorStringOfEmptyIdentifier);
            throw errorStringOfEmptyIdentifier;
        }

        // Check if already variable definition statement exists with same identifier.
        var identifierAlreadyExists = _.findIndex(this.getVariableDefinitionStatements(),
                function (variableDefinitionStatement) {
                    return _.isEqual(variableDefinitionStatement.getIdentifier(), identifier);
                }) !== -1;

        // If variable definition statement with the same identifier exists, then throw an error. Else create the new
        // variable definition statement.
        if (identifierAlreadyExists) {
            var errorString = "A variable definition with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new constant definition.
            var newVariableDefinitionStatement = this.getFactory().createVariableDefinitionStatement();
            newVariableDefinitionStatement.setLeftExpression(bType + " " + identifier);
            if (!_.isNil(assignedValue) && !_.isEmpty(assignedValue)) {
                newVariableDefinitionStatement.setRightExpression(assignedValue);
            }

            var self = this;

            // Get the index of the last variable definition statement.
            var index = _.findLastIndex(this.getChildren(), function (child) {
                return self.getFactory().isVariableDefinitionStatement(child);
            });

            if (index == -1) {
                index = _.findLastIndex(this.getChildren(), function (child) {
                    return self.getFactory().isConnectorDeclaration(child);
                })
            }

            this.addChild(newVariableDefinitionStatement, index + 1);
        }
    };

    /**
     * Removes an existing variable definition statement.
     * @param {string} modelID - The model ID of variable definition statement.
     */
    ConnectorDefinition.prototype.removeVariableDefinitionStatement = function (modelID) {
        var self = this;
        // Deleting the variable definition statement from the children.
        var variableDefinitionStatementToRemove = _.find(this.getChildren(), function (child) {
            return self.getFactory().isVariableDefinitionStatement(child) && _.isEqual(child.id, modelID);
        });

        this.removeChild(variableDefinitionStatementToRemove);
    };

    ConnectorDefinition.prototype.getConnectionDeclarations = function () {
        var connectorDeclaration = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConnectorDeclaration(child)) {
                connectorDeclaration.push(child);
            }
        });

        return _.sortBy(connectorDeclaration, [function (connectorDeclaration) {
            return connectorDeclaration.getConnectorVariable();
        }]);
    };

    ConnectorDefinition.prototype.getConnectorActionDefinitions = function () {
        var connectorActionDefinitions = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConnectorAction(child)) {
                connectorActionDefinitions.push(child);
            }
        });
        return connectorActionDefinitions;
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
            var child = undefined;
            var childNodeTemp = undefined;
            if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = self.BallerinaASTFactory.createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = self.BallerinaASTFactory.createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
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

    /**
     * @inheritDoc
     * @override
     */
    ConnectorDefinition.prototype.generateUniqueIdentifiers = function () {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: "newConnector",
                setter: this.setConnectorName,
                getter: this.getConnectorName,
                parents: [{
                    // ballerina-ast-node
                    node: this.parent,
                    getChildrenFunc: this.parent.getConnectorDefinitions,
                    getter: this.getConnectorName
                }]
            }]
        });
    };

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    ConnectorDefinition.prototype.getConnectorByName = function (connectorName) {
        var factory = this.getFactory();
        var connectorReference = _.find(this.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return connectorReference;
    };

    return ConnectorDefinition;
});
