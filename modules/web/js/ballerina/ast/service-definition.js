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
define(['lodash', './node', 'log', '../utils/common-utils'],
    function (_, ASTNode, log, CommonUtils) {

    /**
     * Constructor for ServiceDefinition
     * @param {Object} args - The arguments to create the ServiceDefinition
     * @param {string} [args.serviceName=newService] - Service name
     * @param {string[]} [args.annotations] - Service annotations
     * @param {string} [args.annotations.BasePath] - Service annotation for BasePath
     * @constructor
     */
    var ServiceDefinition = function (args) {
        this._serviceName = _.get(args, 'serviceName');
        this._annotations = _.get(args, 'annotations', []);

        // Adding available annotations and their default values.
        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "http:BasePath";
            }))) {
            this._annotations.push({
                key: "http:BasePath",
                value: ""
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Source:interface";
            }))) {
            this._annotations.push({
                key: "Source:interface",
                value: ""
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Service:description";
            }))) {
            this._annotations.push({
                key: "Service:description",
                value: ""
            });
        }

        // TODO: All the types should be referred from the global constants
        ASTNode.call(this, 'Service');
        this.BallerinaASTFactory = this.getFactory();
    };

    ServiceDefinition.prototype = Object.create(ASTNode.prototype);
    ServiceDefinition.prototype.constructor = ServiceDefinition;

    ServiceDefinition.prototype.setServiceName = function (serviceName, options) {
        if (!_.isNil(serviceName) && ASTNode.isValidIdentifier(serviceName)) {
            this.setAttribute('_serviceName', serviceName, options);
        } else {
            var errorString = "Invalid name for the service name: " + serviceName;
            log.error(errorString);
            throw errorString;
        }
    };

    ServiceDefinition.prototype.getServiceName = function () {
        return this._serviceName;
    };

    ServiceDefinition.prototype.getAnnotations = function () {
        return this._annotations;
    };


    ServiceDefinition.prototype.getConnectionDeclarations = function () {
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

    /**
     * Gets the variable definition statements of the service.
     * @return {VariableDefinitionStatement[]}
     */
    ServiceDefinition.prototype.getVariableDefinitionStatements = function () {
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
    ServiceDefinition.prototype.addVariableDefinitionStatement = function (bType, identifier, assignedValue) {

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

            this.addChild(newVariableDefinitionStatement, index + 1);
        }
    };

    /**
     * Removes an existing variable definition statement.
     * @param {string} modelID - The model ID of variable definition statement.
     */
    ServiceDefinition.prototype.removeVariableDefinitionStatement = function (modelID) {
        var self = this;
        // Deleting the variable definition statement from the children.
        var variableDefinitionStatementToRemove = _.find(this.getChildren(), function (child) {
            return self.getFactory().isVariableDefinitionStatement(child) && _.isEqual(child.id, modelID);
        });

        this.removeChild(variableDefinitionStatementToRemove);
    };

    /**
     * Adding/Updating an annotation.
     * @param key - Annotation key
     * @param value - Value for the annotation.
     */
    ServiceDefinition.prototype.addAnnotation = function (key, value) {
        if (!_.isNil(key) && !_.isNil(value)) {
            var options = {
              predicate: {key: key}
            }
            this.pushToArrayAttribute('_annotations', {key: key, value: value}, options);
        } else {
            var errorString = "Cannot add annotation @" + key + "(\"" + value + "\").";
            log.error(errorString);
            throw errorString;
        }
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    ServiceDefinition.prototype.canBeParentOf = function (node) {
        return this.BallerinaASTFactory.isResourceDefinition(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isConnectorDeclaration(node);
    };

    /**
     * initialize ServiceDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} jsonNode.service_name - Name of the service definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     */
    ServiceDefinition.prototype.initFromJson = function (jsonNode) {
        var self = this;
        this.setServiceName(jsonNode.service_name, {doSilently: true});

        // Populate the annotations array
        for (var itr = 0; itr < this._annotations.length; itr ++) {
            var key = this._annotations[itr].key;
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
     * Override the super call to addChild
     * @param {ASTNode} child
     * @param {number} index
     */
    ServiceDefinition.prototype.addChild = function (child, index) {
        var self = this;
        var newIndex = index;
        // Always the connector declarations should be the first children
        if (this.BallerinaASTFactory.isConnectorDeclaration(child)) {
            newIndex = _.findLastIndex(this.getChildren(), function (node) {
                return self.BallerinaASTFactory.isConnectorDeclaration(node);
            });

            if (newIndex === -1) {
                newIndex = _.findLastIndex(this.getChildren(), function (child) {
                    return self.getFactory().isVariableDefinitionStatement(child);
                });
            }
            newIndex = newIndex + 1;
        }
        if (newIndex === -1) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, newIndex);
        }
    };

    //// Start of resource definitions functions

    ServiceDefinition.prototype.getResourceDefinitions = function () {
        var resourceDefinitions = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isResourceDefinition(child)) {
                resourceDefinitions.push(child);
            }
        });
        return resourceDefinitions;
    };

    //// End of resource definitions functions

    /**
     * @inheritDoc
     * @override
     */
    ServiceDefinition.prototype.generateUniqueIdentifiers = function () {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: "newService",
                setter: this.setServiceName,
                getter: this.getServiceName,
                parents: [{
                    // ballerina-ast-root
                    node: this.parent,
                    getChildrenFunc: this.parent.getServiceDefinitions,
                    getter: this.getServiceName
                }]
            }]
        });
    };

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    ServiceDefinition.prototype.getConnectorByName = function (connectorName) {
        var factory = this.getFactory();
        var connectorReference = _.find(this.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return connectorReference;
    };

    return ServiceDefinition;

});
