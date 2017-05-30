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
import _ from 'lodash';
import ASTNode from './node';
import log from 'log';
import CommonUtils from '../utils/common-utils';

/**
 * Constructor for ServiceDefinition
 * @param {Object} args - The arguments to create the ServiceDefinition
 * @param {string} [args.serviceName=newService] - Service name
 * @constructor
 */
class ServiceDefinition extends ASTNode {
    constructor(args) {
        super('ServiceDefinition');
        this._serviceName = _.get(args, 'serviceName');

        // TODO: All the types should be referred from the global constants
        this.BallerinaASTFactory = this.getFactory();
    }

    setServiceName(serviceName, options) {
        if (!_.isNil(serviceName) && ASTNode.isValidIdentifier(serviceName)) {
            this.setAttribute('_serviceName', serviceName, options);
        } else {
            var errorString = "Invalid name for the service name: " + serviceName;
            log.error(errorString);
            throw errorString;
        }
    }

    getServiceName() {
        return this._serviceName;
    }

    getConnectionDeclarations() {
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
    }

    /**
     * Gets the variable definition statements of the service.
     * @return {VariableDefinitionStatement[]}
     */
    getVariableDefinitionStatements() {
        var variableDefinitionStatements = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isVariableDefinitionStatement(child)) {
                variableDefinitionStatements.push(child);
            }
        });
        return variableDefinitionStatements;
    }

    /**
     * Adds new variable definition statement.
     * @param {string} bType - The ballerina type of the variable definition statement.
     * @param {string} identifier - The identifier of the variable definition statement.
     * @param {string} assignedValue - The right hand expression.
     */
    addVariableDefinitionStatement(bType, identifier, assignedValue) {

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
    }

    /**
     * Removes an existing variable definition statement.
     * @param {string} modelID - The model ID of variable definition statement.
     */
    removeVariableDefinitionStatement(modelID) {
        var self = this;
        // Deleting the variable definition statement from the children.
        var variableDefinitionStatementToRemove = _.find(this.getChildren(), function (child) {
            return self.getFactory().isVariableDefinitionStatement(child) && _.isEqual(child.id, modelID);
        });

        this.removeChild(variableDefinitionStatementToRemove);
    }

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        return this.BallerinaASTFactory.isResourceDefinition(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isConnectorDeclaration(node);
    }

    /**
     * initialize ServiceDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} jsonNode.service_name - Name of the service definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     */
    initFromJson(jsonNode) {
        var self = this;
        this.setServiceName(jsonNode.service_name, {doSilently: true});

        _.each(jsonNode.children, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            if (childNode.type === 'variable_definition_statement' && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = self.BallerinaASTFactory.createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = self.BallerinaASTFactory.createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    }

    /**
     * Override the super call to addChild
     * @param {ASTNode} child
     * @param {number} index
     * @param ignoreTreeModifiedEvent {boolean}
     * @param ignoreChildAddedEvent {boolean}
     */
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent) {
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
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0, ignoreTreeModifiedEvent,
                ignoreChildAddedEvent);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, newIndex,
                ignoreTreeModifiedEvent, ignoreChildAddedEvent);
        }
    }

    //// Start of resource definitions functions

    getResourceDefinitions() {
        var resourceDefinitions = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isResourceDefinition(child)) {
                resourceDefinitions.push(child);
            }
        });
        return resourceDefinitions;
    }

    //// End of resource definitions functions

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: 'Service',
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
    }

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    getConnectorByName(connectorName) {
        var factory = this.getFactory();
        var connectorReference = _.find(this.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return connectorReference;
    }

    /**
     * Get all the connector references in the immediate scope
     * @return {ConnectorDeclaration[]} connectorReferences
     */
    getConnectorsInImmediateScope() {
        var factory = this.getFactory();
        var connectorReferences = _.filter(this.getChildren(), function (child) {
            return factory.isConnectorDeclaration(child);
        });

        return connectorReferences;
    }
}

export default ServiceDefinition;
