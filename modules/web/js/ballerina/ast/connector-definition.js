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
 * Constructor for ConnectorDefinition
 * @param {object} args - Constructor arguments
 * @constructor
 */
class ConnectorDefinition extends ASTNode {
    constructor(args) {
        super('ConnectorDefinition');
        this.connector_name = _.get(args, 'connector_name');
        this.annotations = _.get(args, 'annotations', []);
        this.arguments = _.get(args, 'arguments', []);
    }

    /**
     * Get the name of connector
     * @return {string} connector_name - Connector Name
     */
    getConnectorName() {
        return this.connector_name;
    }

    /**
     * Get the annotations
     * @return {string[]} annotations - Connector Annotations
     */
    getAnnotations() {
        return this.annotations;
    }

    /**
     * Get the connector arguments
     * @return {ParameterDefinition[]} arguments - Connector Arguments
     */
    getArguments() {
        return this.getArgumentParameterDefinitionHolder().getChildren();
    }

    /**
     * Adds new argument to the connector definition.
     * @param type - The type of the argument.
     * @param identifier - The identifier of the argument.
     */
    addArgument(type, identifier) {
        //creating argument
        let newArgumentParamDef = this.getFactory().createParameterDefinition();
        newArgumentParamDef.setTypeName(type);
        newArgumentParamDef.setName(identifier);

        let argParamDefHolder = this.getArgumentParameterDefinitionHolder();
        let index = argParamDefHolder.getChildren().length;

        argParamDefHolder.addChild(newArgumentParamDef, index + 1);
    }

    /**
     * Removes an argument from a connector definition.
     * @param identifier - The identifier of the argument.
     * @return {Array} - The removed argument.
     */
    removeArgument(identifier) {
        this.getArgumentParameterDefinitionHolder().removeChildByName(this.getFactory().isParameterDefinition, identifier);
    }

    getArgumentParameterDefinitionHolder() {
        let argParamDefHolder = this.findChild(this.getFactory().isArgumentParameterDefinitionHolder);
        if (_.isUndefined(argParamDefHolder)) {
            argParamDefHolder = this.getFactory().createArgumentParameterDefinitionHolder();
            this.addChild(argParamDefHolder);
        }
        return argParamDefHolder;
    }

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    getArgumentsAsString() {
        let argsStringArray = [];
        let args = this.getArguments();
        _.forEach(args, function (arg) {
            argsStringArray.push(arg.getParameterDefinitionAsString());
        });
        return _.join(argsStringArray, ', ');
    }


    /**
     * Set the Connector name
     * @param {string} name - Connector Name
     * @param {object} options - options for connector
     */
    setConnectorName(name, options) {
        if (!_.isNil(name) && ASTNode.isValidIdentifier(name)) {
            this.setAttribute('connector_name', name, options);
        } else {
            let errorString = "Invalid connector name: " + name;
            log.error(errorString);
            throw errorString;
        }
    }

    /**
     * Override the super call to addChild
     * @param child
     * @param index
     */
    addChild(child, index) {
        if (this.getFactory().isConnectorDeclaration(child)) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
        }
    }

    /**
     * Set the connector annotations
     * @param {string[]} annotations - Connector Annotations
     * @param {object} options - options for annotation
     */
    setAnnotations(annotations, options) {
        if (!_.isNil(annotations)) {
            this.setAttribute('annotations', annotations, options);
        } else {
            log.warn('Trying to set a null or undefined array to annotations');
        }
    }

    /**
     * Set the Connector Arguments
     * @param {object[]} args - Connector Arguments
     * @param {object} options - options for arguments
     */
    setArguments(args, options) {
        if (!_.isNil(args)) {
            this.setAttribute('arguments', args, options);
        } else {
            log.warn('Trying to set a null or undefined array to arguments');
        }
    }

    /**
     * Gets the variable definition statements of the connector definition.
     * @return {VariableDefinitionStatement[]}
     */
    getVariableDefinitionStatements() {
        let variableDefinitionStatements = [];
        let self = this;

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
            let errorStringOfEmptyIdentifier = "A variable definition requires an identifier.";
            log.error(errorStringOfEmptyIdentifier);
            throw errorStringOfEmptyIdentifier;
        }

        // Check if already variable definition statement exists with same identifier.
        let identifierAlreadyExists = _.findIndex(this.getVariableDefinitionStatements(),
                function (variableDefinitionStatement) {
                    return _.isEqual(variableDefinitionStatement.getIdentifier(), identifier);
                }) !== -1;

        // If variable definition statement with the same identifier exists, then throw an error. Else create the new
        // variable definition statement.
        if (identifierAlreadyExists) {
            let errorString = "A variable definition with identifier '" + identifier + "' already exists.";
            log.error(errorString);
            throw errorString;
        } else {
            // Creating new constant definition.
            let newVariableDefinitionStatement = this.getFactory().createVariableDefinitionStatement();
            newVariableDefinitionStatement.setLeftExpression(bType + " " + identifier);
            if (!_.isNil(assignedValue) && !_.isEmpty(assignedValue)) {
                newVariableDefinitionStatement.setRightExpression(assignedValue);
            }

            let self = this;

            // Get the index of the last variable definition statement.
            let index = _.findLastIndex(this.getChildren(), function (child) {
                return self.getFactory().isVariableDefinitionStatement(child);
            });

            if (index === -1) {
                index = _.findLastIndex(this.getChildren(), function (child) {
                    return self.getFactory().isConnectorDeclaration(child);
                });
            }

            this.addChild(newVariableDefinitionStatement, index + 1);
        }
    }

    /**
     * Removes an existing variable definition statement.
     * @param {string} modelID - The model ID of variable definition statement.
     */
    removeVariableDefinitionStatement(modelID) {
        let self = this;
        // Deleting the variable definition statement from the children.
        let variableDefinitionStatementToRemove = _.find(this.getChildren(), function (child) {
            return self.getFactory().isVariableDefinitionStatement(child) && _.isEqual(child.id, modelID);
        });

        this.removeChild(variableDefinitionStatementToRemove);
    }

    getConnectionDeclarations() {
        let connectorDeclaration = [];
        let self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConnectorDeclaration(child)) {
                connectorDeclaration.push(child);
            }
        });

        return _.sortBy(connectorDeclaration, [function (connectorDeclaration) {
            return connectorDeclaration.getConnectorVariable();
        }]);
    }

    getConnectorActionDefinitions() {
        let connectorActionDefinitions = [];
        let self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isConnectorAction(child)) {
                connectorActionDefinitions.push(child);
            }
        });
        return connectorActionDefinitions;
    }

    /**
     * initialize ConnectorDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.connector_name] - Name of the service definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     */
    initFromJson(jsonNode) {
        let self = this;
        this.setConnectorName(jsonNode.connector_name, {doSilently: true});

        _.each(jsonNode.children, function (childNode) {
            let child = undefined;
            let childNodeTemp = undefined;
            if (childNode.type === "variable_definition_statement" && !_.isNil(childNode.children[1]) && childNode.children[1].type === 'connector_init_expr') {
                child = self.getFactory().createConnectorDeclaration();
                childNodeTemp = childNode;
            } else {
                child = self.getFactory().createFromJson(childNode);
                childNodeTemp = childNode;
            }
            self.addChild(child);
            child.initFromJson(childNodeTemp);
        });
    }

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    canBeParentOf(node) {
        return this.getFactory().isConnectorAction(node)
            || this.getFactory().isVariableDeclaration(node)
            || this.getFactory().isConnectorDeclaration(node);
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: 'Connector',
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
    }

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    getConnectorByName(connectorName) {
        let factory = this.getFactory();
        let connectorReference = _.find(this.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return connectorReference;
    }

    /**
     * Get all the connector references in the immediate scope
     * @return {ConnectorDeclaration[]} connectorReferences
     */
    getConnectorsInImmediateScope() {
        let factory = this.getFactory();
        let connectorReferences = _.filter(this.getChildren(), function (child) {
            return factory.isConnectorDeclaration(child);
        });

        return connectorReferences;
    }
}

export default ConnectorDefinition;
