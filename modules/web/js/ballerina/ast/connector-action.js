/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import BallerinaASTFactory from './../ast/ballerina-ast-factory';

/**
 * Constructor for ConnectorAction
 * @param {object} args - Constructor arguments
 * @constructor
 */
class ConnectorAction extends ASTNode {
    constructor(args) {
        super('ConnectorAction');
        this.action_name = _.get(args, 'action_name');
        this.arguments = _.get(args, 'arguments', []);
    }

    /**
     * Get the name of action
     * @return {string} action_name - Action Name
     */
    getActionName() {
        return this.action_name;
    }

    /**
     * Get the action Arguments
     * @return {Object[]} arguments - Action Arguments
     */
    getArguments() {
        return this.getArgumentParameterDefinitionHolder().getChildren();
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
     * Set the Action name
     * @param {string} name - Action Name
     * @param {object} options - attribute options
     */
    setActionName(name, options) {
        this.setAttribute('action_name', name, options);
    }

    /**
     * Set the Action name
     * This is an alias to setActionName
     * Many other node types' setters for name takes pattern `set<Node Type>Name`
     * Node type of connector actions is ConnectorAction
     * This method helps keep that formality so we can take advantage of it
     * @param {string} name - Action Name
     * @param {object} options - attribute options
     */
    setConnectorActionName(name, options) {
        this.setActionName(name, options);
    }

    /**
     * Get the variable Declarations
     * @return {VariableDeclaration[]} variableDeclarations
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
     * Remove variable declaration.
     */
    removeVariableDeclaration(variableDeclarationIdentifier) {
        let self = this;
        // Removing the variable from the children.
        let variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return self.getFactory().isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    }

    /**
     * Adds new variable declaration.
     */
    addVariableDeclaration(newVariableDeclaration) {
        let self = this;
        // Get the index of the last variable declaration.
        let index = _.findLastIndex(this.getChildren(), function (child) {
            return self.getFactory().isVariableDeclaration(child);
        });

        // index = -1 when there are not any variable declarations, hence get the index for connector
        // declarations.
        if (index === -1) {
            index = _.findLastIndex(this.getChildren(), function (child) {
                return self.getFactory().isConnectorDeclaration(child);
            });
        }

        this.addChild(newVariableDeclaration, index + 1);
    }

    //// Start of return type functions.

    /**
     * Gets the return type as a string separated by commas.
     * @return {string} - Return types.
     */
    getReturnTypesAsString() {
        let returnTypes = [];
        _.forEach(this.getReturnTypes(), function (returnTypeChild) {
            returnTypes.push(returnTypeChild.getParameterDefinitionAsString());
        });

        return _.join(returnTypes, " , ");
    }

    /**
     * Gets the defined return types.
     * @return {ParameterDefinition[]} - Array of return arguments.
     */
    getReturnTypes() {
        return this.getReturnParameterDefinitionHolder().getChildren();
    }

    getReturnParameterDefinitionHolder() {
        let returnParamDefHolder = this.findChild(this.getFactory().isReturnParameterDefinitionHolder);
        if (_.isUndefined(returnParamDefHolder)) {
            returnParamDefHolder = this.getFactory().createReturnParameterDefinitionHolder();
            this.addChild(returnParamDefHolder);
        }
        return returnParamDefHolder;
    }

    /**
     * Adds a new argument to return type model.
     * @param {string} type - The type of the argument.
     * @param {string} identifier - The identifier of the argument.
     */
    addReturnType(type, identifier) {
        let returnParamDefHolder = this.getReturnParameterDefinitionHolder();

        // Adding return type mode if it doesn't exists.
        if (_.isUndefined(this.getReturnTypeModel())) {
            this.addChild(this.getFactory().createReturnType());
        }

        // Check if there is already a return type with the same identifier.
        if (!_.isUndefined(identifier)) {
            let child = returnParamDefHolder.findChildByIdentifier(true, identifier);
            if (_.isUndefined(child)) {
                let errorString = "An return argument with identifier '" + identifier + "' already exists.";
                log.error(errorString);
                throw errorString;
            }
        }

        // Validating whether return type can be added based on identifiers of other return types.
        if (!_.isUndefined(identifier)) {
            if (!this.hasNamedReturnTypes() && this.hasReturnTypes()) {
                let errorStringWithoutIdentifiers = "Return types without identifiers already exists. Remove them to " +
                    "add return types with identifiers.";
                log.error(errorStringWithoutIdentifiers);
                throw errorStringWithoutIdentifiers;
            }
        } else {
            if (this.hasNamedReturnTypes() && this.hasReturnTypes()) {
                let errorStringWithIdentifiers = "Return types with identifiers already exists. Remove them to add " +
                    "return types without identifiers.";
                log.error(errorStringWithIdentifiers);
                throw errorStringWithIdentifiers;
            }
        }

        let paramDef = this.getFactory().createParameterDefinition({typeName: type, name: identifier});
        returnParamDefHolder.addChild(paramDef, 0);
    }

    hasNamedReturnTypes() {
        if (this.getReturnParameterDefinitionHolder().getChildren().length === 0) {
            //if there are no return types in the return type model
            return false;
        } else {
            //check if any of the return types have identifiers
            let indexWithoutIdentifiers = _.findIndex(this.getReturnParameterDefinitionHolder().getChildren(), function (child) {
                return _.isUndefined(child.getName());
            });

            return indexWithoutIdentifiers === -1;
        }
    }

    hasReturnTypes() {
        return this.getReturnParameterDefinitionHolder().getChildren().length > 0;
    }

    /**
     * Removes return type argument from the return type model.
     * @param {string} modelID - The id of an {Argument} which resides in the return type model.
     */
    removeReturnType(modelID) {
        let removeChild = this.getReturnParameterDefinitionHolder().removeChildById(this.getFactory().isParameterDefinition, modelID);

        // Deleting the argument from the AST.
        if (_.isUndefined(removeChild)) {
            let exceptionString = "Could not find a return type with id : " + modelID;
            log.error(exceptionString);
            throw exceptionString;
        }
    }

    //// End of return type functions.

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    getArgumentsAsString() {
        let argsAsString = "";
        let args = this.getArguments();
        _.forEach(args, function (argument, index) {
            argsAsString += argument.getTypeName() + " ";
            argsAsString += argument.getName();
            if (args.length - 1 !== index) {
                argsAsString += " , ";
            }
        });
        return argsAsString;
    }

    /**
     * Adds new argument to the connector action.
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
     * Removes an argument from a function definition.
     * @param identifier - The identifier of the argument.
     * @return {Array} - The removed argument.
     */
    removeArgument(identifier) {
        let argParamDefHolder = this.getArgumentParameterDefinitionHolder();
        argParamDefHolder.removeChildByName(this.getFactory().isParameterDefinition, identifier);
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

    getWorkerDeclarations() {
        let workerDeclarations = [];
        let self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
    }

    /**
     * initialize ConnectorAction from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.resource_name] - Name of the resource definition
     */
    initFromJson(jsonNode) {
        let self = this;
        this.setActionName(jsonNode.action_name, {doSilently: true});

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
        return this.getFactory().isConnectorDeclaration(node)
            || this.getFactory().isVariableDeclaration(node)
            || this.getFactory().isWorkerDeclaration(node)
            || this.getFactory().isStatement(node);
    }

    /**
     * Override the addChild method for ordering the child elements as
     * [Statements, Workers, Connectors]
     * @param {ASTNode} child
     * @param {number|undefined} index
     */
    addChild(child, index) {
        if (BallerinaASTFactory.isWorkerDeclaration(child)) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child);
        } else {
            const firstWorkerIndex = _.findIndex(this.getChildren(), function (child) {
                return BallerinaASTFactory.isWorkerDeclaration(child);
            });

            if (firstWorkerIndex > -1 && _.isNil(index)) {
                index = firstWorkerIndex;
            }
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
        }
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: 'Action',
                setter: this.setActionName,
                getter: this.getActionName,
                parents: [{
                    // ballerina-ast-node
                    node: this.parent,
                    getChildrenFunc: this.parent.getConnectorActionDefinitions,
                    getter: this.getActionName
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
        let self = this;
        let connectorReference = _.find(this.getChildren(), function (child) {
            return (self.getFactory().isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return !_.isNil(connectorReference) ? connectorReference : this.getParent().getConnectorByName(connectorName);
    }

    /**
     * Get all the connector references in the immediate scope
     * @return {Array} connectorReferences
     */
    getConnectorsInImmediateScope() {
        let factory = this.getFactory();
        let connectorReferences = _.filter(this.getChildren(), function (child) {
            return factory.isConnectorDeclaration(child);
        });

        return !_.isEmpty(connectorReferences) ? connectorReferences : this.getParent().getConnectorsInImmediateScope();
    }
}

export default ConnectorAction;
