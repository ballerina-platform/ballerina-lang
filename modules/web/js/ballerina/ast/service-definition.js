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
import log from 'log';
import ASTNode from './node';
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
        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: '\n',
            3: '\n',
        };
    }

    setServiceName(serviceName, options) {
        if (!_.isNil(serviceName) && ASTNode.isValidIdentifier(serviceName)) {
            this.setAttribute('_serviceName', serviceName, options);
        } else {
            const errorString = 'Invalid name for the service name: ' + serviceName;
            log.error(errorString);
            throw errorString;
        }
    }

    getServiceName() {
        return this._serviceName;
    }

    getConnectionDeclarations() {
        const connectorDeclaration = [];
        const self = this;
        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isConnectorDeclaration(child)) {
                connectorDeclaration.push(child);
            }
        });

        return _.sortBy(connectorDeclaration, [function (connectorDec) {
            return connectorDec.getConnectorVariable();
        }]);
    }

    /**
     * Gets the variable definition statements of the service.
     * @return {VariableDefinitionStatement[]}
     */
    getVariableDefinitionStatements() {
        const variableDefinitionStatements = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
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
            const errorStringOfEmptyIdentifier = 'A variable definition requires an identifier.';
            log.error(errorStringOfEmptyIdentifier);
            throw errorStringOfEmptyIdentifier;
        }

        // Check if already variable definition statement exists with same identifier.
        const identifierAlreadyExists = _.findIndex(this.getVariableDefinitionStatements(),
                   variableDefinitionStatement =>
                       _.isEqual(variableDefinitionStatement.getIdentifier(), identifier)) !== -1;

        // If variable definition statement with the same identifier exists, then throw an error. Else create the new
        // variable definition statement.
        if (identifierAlreadyExists) {
            const errorString = "A variable definition with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new constant definition.
            const newVariableDefinitionStatement = this.getFactory().createVariableDefinitionStatement();
            let stmtString = bType + ' ' + identifier;
            if (!_.isNil(assignedValue) && !_.isEmpty(assignedValue)) {
                stmtString += ' = ' + assignedValue;
            }
            newVariableDefinitionStatement.setStatementFromString(stmtString);

            // Get the index of the last variable definition statement.
            const index = _.findLastIndex(this.getChildren(), child =>
                                    this.getFactory().isVariableDefinitionStatement(child));

            this.addChild(newVariableDefinitionStatement, index + 1);
        }
    }

    addVariableDefinitionFromString(variableDefString) {
        if(!variableDefString){
            return;
        }

        const varDefStatement = this.getFactory().createVariableDefinitionStatement();
        varDefStatement.setStatementFromString(variableDefString, ({isValid, response}) => {
            if(!isValid) {
                return;
            }

            // Get the index of the last variable definition statement.
            const index = _.findLastIndex(this.getChildren(), child =>
                                    this.getFactory().isVariableDefinitionStatement(child));

            this.addChild(varDefStatement, index + 1);
        });
    }

    /**
     * Removes an existing variable definition statement.
     * @param {string} modelID - The model ID of variable definition statement.
     */
    removeVariableDefinitionStatement(modelID) {
        const self = this;
        // Deleting the variable definition statement from the children.
        const variableDefinitionStatementToRemove = _.find(this.getChildren(), child =>
                    self.getFactory().isVariableDefinitionStatement(child) && _.isEqual(child.id, modelID));

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
        const self = this;
        this.setServiceName(jsonNode.service_name, { doSilently: true });

        _.each(jsonNode.children, (childNode) => {
            let child;
            let childNodeTemp;
            if (childNode.type === 'variable_definition_statement' &&
                      !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
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
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId) {
        let newIndex = index;
        // Always the connector declarations should be the first children
        if (this.BallerinaASTFactory.isConnectorDeclaration(child)) {
            newIndex = _.findLastIndex(this.getChildren(), node =>
                             this.BallerinaASTFactory.isConnectorDeclaration(node));

            if (newIndex === -1) {
                newIndex = _.findLastIndex(this.getChildren(), childIndex =>
                                  this.getFactory().isVariableDefinitionStatement(childIndex));
            }
            newIndex += 1;
        }
        if (newIndex === -1) {
            Object.getPrototypeOf(this.constructor.prototype)
              .addChild.call(this, child, 0, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        } else {
            Object.getPrototypeOf(this.constructor.prototype)
              .addChild.call(this, child, newIndex, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        }
    }

    // // Start of resource definitions functions

    getResourceDefinitions() {
        const resourceDefinitions = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isResourceDefinition(child)) {
                resourceDefinitions.push(child);
            }
        });
        return resourceDefinitions;
    }

    // // End of resource definitions functions

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
                    getter: this.getServiceName,
                }],
            }],
        });
    }

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    getConnectorByName(connectorName) {
        const factory = this.getFactory();
        const connectorReference = _.find(this.getChildren(), child =>
                (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName)));

        return connectorReference;
    }

    /**
     * Get all the connector references in the immediate scope
     * @return {ConnectorDeclaration[]} connectorReferences
     */
    getConnectorsInImmediateScope() {
        const factory = this.getFactory();
        const connectorReferences = _.filter(this.getChildren(), (child) => {
            return factory.isConnectorDeclaration(child);
        });

        return connectorReferences;
    }
}

export default ServiceDefinition;
