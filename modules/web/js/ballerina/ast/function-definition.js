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
import log from 'log';
import ASTNode from './node';
import CallableDefinition from './callable-definition';
import CommonUtils from '../utils/common-utils';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';

/**
 * Constructor for FunctionDefinition
 * @param {Object} args - The arguments to create the FunctionDefinition
 * @param {string} [args.functionName=newFunction] - Function name
 * @param {boolean} [args.isPublic=false] - Public or not of the function
 * @param {string[]} [args.annotations] - Function annotations
 * @constructor
 */
class FunctionDefinition extends CallableDefinition {
    constructor(args) {
        super('FunctionDefinition');
        this.id = autoGenerateId();
        this._functionName = _.get(args, 'functionName');
        this._isPublic = _.get(args, 'isPublic') || false;
        this._annotations = _.get(args, 'annotations', []);
        this._isNative = _.get(args, 'isNative', false);
        this._isLambda = _.get(args, 'isLambda', false);
        this._hasReturnsKeyword = _.get(args, 'hasReturnsKeyword', false);
        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: ' ',
            2: ' ',
            3: '',
            4: ' ',
            5: '',
            6: ' ',
            7: '\n',
            8: '\n',
        };
    }

    setFunctionName(name, options) {
        if (this.isLambda() || (!_.isNil(name) && ASTNode.isValidIdentifier(name))) {
            this.setAttribute('_functionName', name, options);
        } else {
            const errorString = 'Invalid function name: ' + name;
            log.error(errorString);
            throw errorString;
        }
    }

    setIsPublic(isPublic, options) {
        if (!_.isNil(isPublic)) {
            this.setAttribute('_isPublic', isPublic, options);
        }
    }

    getFunctionName() {
        return this._functionName;
    }

    getArguments() {
        return this.getArgumentParameterDefinitionHolder().getChildren();
    }

    getIsPublic() {
        return this._isPublic;
    }

    getVariableDefinitionStatements() {
        return this.filterChildren(this.getFactory().isVariableDefinitionStatement).slice(0);
    }

    /**
     * Get the namespace declaration statements.
     * @return {ASTNode[]} namespace declaration statements
     * */
    getNamespaceDeclarationStatements() {
        return this.filterChildren(this.getFactory().isNamespaceDeclarationStatement).slice(0);
    }

    /**
     * Adds new variable declaration.
     */
    addVariableDeclaration(newVariableDeclaration) {
        // Get the index of the last variable declaration.
        let index = this.findLastIndexOfChild(this.getFactory().isVariableDeclaration);
        // index = -1 when there are not any variable declarations, hence get the index for connector
        // declarations.
        if (index == -1) {
            index = this.findLastIndexOfChild(this.getFactory().isConnectorDeclaration);
        }

        this.addChild(newVariableDeclaration, index + 1);
    }

    /**
     * Removes variable declaration.
     */
    removeVariableDeclaration(variableDeclarationIdentifier) {
        // Removing the variable from the children.
        this.removeChildByIdentifier = _.remove(this.getFactory().isVariableDeclaration, variableDeclarationIdentifier);
    }

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    getArgumentsAsString() {
        let argsString = '';
        this.getArguments().forEach((arg, index) => {
            if (index != 0) {
                argsString += ',';
                argsString += (arg.whiteSpace.useDefault ? ' ' : arg.getWSRegion(0));
            }
            argsString += arg.getParameterDefinitionAsString();
        });
        return argsString;
    }

    /**
     * Adds new argument to the function definition.
     * @param type - The type of the argument.
     * @param identifier - The identifier of the argument.
     */
    addArgument(type, identifier) {
        const newArgumentParamDef = this.getFactory().createParameterDefinition();
        newArgumentParamDef.setTypeName(type);
        newArgumentParamDef.setName(identifier);

        const argParamDefHolder = this.getArgumentParameterDefinitionHolder();
        const index = argParamDefHolder.getChildren().length;

        argParamDefHolder.addChild(newArgumentParamDef, index + 1);
    }

    /**
     * Removes an argument from a function definition.
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
            this.addChild(argParamDefHolder, undefined, true, true, false);
        }
        return argParamDefHolder;
    }

    // // Start of return type functions.

    /**
     * Gets the return type as a string separated by commas.
     * @return {string} - Return types separated by comma.
     */
    getReturnTypesAsString() {
        let returnTypesString = '';
        this.getReturnTypes().forEach((returnType, index) => {
            if (index !== 0) {
                returnTypesString += ',';
                returnTypesString += (returnType.whiteSpace.useDefault ? ' '
                    : returnType.getWSRegion(0));
            }
            returnTypesString += returnType.getParameterDefinitionAsString();
        });
        return returnTypesString;
    }

    /**
     * Gets the defined return types.
     * @return {ParameterDefinition[]} - Array of returns.
     */
    getReturnTypes() {
        return this.getReturnParameterDefinitionHolder().getChildren();
    }

    /**
     * Adds a new argument to return type model.
     * @param {string} type - The type of the argument.
     * @param {string} identifier - The identifier of the argument.
     */
    addReturnType(type, identifier) {
        const self = this;

        const returnParamDefHolder = this.getReturnParameterDefinitionHolder();

        // Check if there is already a return type with the same identifier.
        if (!_.isUndefined(identifier)) {
            const child = returnParamDefHolder.findChildByIdentifier(true, identifier);
            if (_.isUndefined(child)) {
                const errorString = "An return argument with identifier '" + identifier + "' already exists.";
                log.error(errorString);
                throw errorString;
            }
        }

        // Validating whether return type can be added based on identifiers of other return types.
        if (!_.isUndefined(identifier)) {
            if (!this.hasNamedReturnTypes() && this.hasReturnTypes()) {
                const errorStringWithoutIdentifiers = 'Return types without identifiers already exists. Remove them to ' +
                    'add return types with identifiers.';
                log.error(errorStringWithoutIdentifiers);
                throw errorStringWithoutIdentifiers;
            }
        } else if (this.hasNamedReturnTypes() && this.hasReturnTypes()) {
            const errorStringWithIdentifiers = 'Return types with identifiers already exists. Remove them to add ' +
                'return types without identifiers.';
            log.error(errorStringWithIdentifiers);
            throw errorStringWithIdentifiers;
        }

        const paramDef = this.getFactory().createParameterDefinition({ typeName: type, name: identifier });
        returnParamDefHolder.addChild(paramDef, 0);
    }

    hasNamedReturnTypes() {
        if (this.getReturnParameterDefinitionHolder().getChildren().length == 0) {
            // if there are no return types in the return type model
            return false;
        } else if (this.getReturnTypeModel().getChildren().length == 0) {
            // if there are no return types in the return type model
            return false;
        }
        // check if any of the return types have identifiers
        const indexWithoutIdentifiers = _.findIndex(this.getReturnParameterDefinitionHolder().getChildren(), (child) => {
            return _.isUndefined(child.getName());
        });

        if (indexWithoutIdentifiers !== -1) {
            return false;
        }
        return true;
    }

    hasReturnTypes() {
        if (this.getReturnParameterDefinitionHolder().getChildren().length > 0) {
            return true;
        }
        return false;
    }

    /**
     * Removes return type argument from the return type model.
     * @param {string} identifier - The identifier of a {ParameterDefinition} which resides in the return type model.
     */
    removeReturnType(modelID) {
        const removeChild = this.getReturnParameterDefinitionHolder().removeChildById(this.getFactory().isParameterDefinition, modelID);

        // Deleting the argument from the AST.
        if (!_.isUndefined(removeChild)) {
            const exceptionString = 'Could not find a return type with id : ' + modelID;
            log.error(exceptionString);
            throw exceptionString;
        }
    }

    getReturnParameterDefinitionHolder() {
        let returnParamDefHolder = this.findChild(this.getFactory().isReturnParameterDefinitionHolder);
        if (_.isUndefined(returnParamDefHolder)) {
            returnParamDefHolder = this.getFactory().createReturnParameterDefinitionHolder();
            this.addChild(returnParamDefHolder, undefined, true, true, false);
        }
        return returnParamDefHolder;
    }

    // // End of return type functions.

    getConnectionDeclarations() {
        const connectorDeclaration = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isConnectorDeclaration(child)) {
                connectorDeclaration.push(child);
            }
        });
        return _.sortBy(connectorDeclaration, [function (connectorDeclaration) {
            return connectorDeclaration.getConnectorVariable();
        }]);
    }

    /**
     * Override the super call to addChild
     * @param {object} child
     * @param {number} index
     * @param ignoreTreeModifiedEvent {boolean}
     * @param ignoreChildAddedEvent {boolean}
     */
    addChild(child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId) {
        if (BallerinaASTFactory.isWorkerDeclaration(child)) {
            Object.getPrototypeOf(this.constructor.prototype)
                .addChild.call(this, child, undefined, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        } else {
            const firstWorkerIndex = _.findIndex(this.getChildren(), (child) => {
                return BallerinaASTFactory.isWorkerDeclaration(child);
            });

            if (firstWorkerIndex > -1 && (_.isNil(index) || index < 0)) {
                index = firstWorkerIndex;
            }
            Object.getPrototypeOf(this.constructor.prototype)
                .addChild.call(this, child, index, ignoreTreeModifiedEvent, ignoreChildAddedEvent, generateId);
        }
    }

    getWorkerDeclarations() {
        const workerDeclarations = [];
        const self = this;

        _.forEach(this.getChildren(), (child) => {
            if (self.getFactory().isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
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

    isNative(isNative) {
        if (_.isNil(isNative)) {
            return this._isNative;
        }
        this._isNative = isNative;
    }

    isLambda(isLambda) {
        if (!_.isNil(isLambda)) {
            this._isLambda = isLambda;
        }
        return this._isLambda;
    }

    /**
     * Is 'returns' keyword used in source.
     *
     * @param hasReturnsKeyword {boolean?} is 'returns' keyword used in source.
     * @return {boolean} Is sss.
     */
    hasReturnsKeyword(hasReturnsKeyword) {
        if (!_.isNil(hasReturnsKeyword)) {
            this._hasReturnsKeyword = hasReturnsKeyword;
        }
        return this._hasReturnsKeyword;
    }

    /**
     * initialize FunctionDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.function_name] - Name of the function definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     * @param {boolean} [jsonNode.is_public_function] - Public or not of the function
     * @param {boolean} [jsonNode.has_returns_keyword] - is 'retruns' keyword used in source
     */
    initFromJson(jsonNode) {
        this.isLambda(jsonNode.is_lambda_function);
        this.hasReturnsKeyword(jsonNode.has_returns_keyword);
        this.setFunctionName(jsonNode.function_name, { doSilently: true });
        this.setIsPublic(jsonNode.is_public_function, { doSilently: true });
        this._annotations = jsonNode.annotations;
        this.isNative(jsonNode.is_native);

        const self = this;

        _.each(jsonNode.children, (childNode) => {
            const child = self.getFactory().createFromJson(childNode);
            self.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }

    /**
     * @inheritDoc
     * @override
     */
    generateUniqueIdentifiers() {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: 'Function',
                setter: this.setFunctionName,
                getter: this.getFunctionName,
                parents: [{
                    // ballerina-ast-node
                    node: this.parent,
                    getChildrenFunc: this.parent.getFunctionDefinitions,
                    getter: this.getFunctionName,
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
        const connectorReference = _.find(this.getChildren(), (child) => {
            let connectorVariableName;
            if (factory.isAssignmentStatement(child) && factory.isConnectorInitExpression(child.getChildren()[1])) {
                const variableReferenceList = [];

                _.forEach(child.getChildren()[0].getChildren(), (variableRef) => {
                    variableReferenceList.push(variableRef.getExpressionString());
                });

                connectorVariableName = (_.join(variableReferenceList, ',')).trim();
            } else if (factory.isConnectorDeclaration(child)) {
                connectorVariableName = child.getConnectorVariable();
            }

            return connectorVariableName === connectorName;
        });

        return connectorReference;
    }

    /**
     * Get all the connector references in the immediate scope
     * @return {ConnectorDeclaration[]} connectorReferences
     */
    getConnectorsInImmediateScope() {
        const factory = this.getFactory();
        return _.filter(this.getChildren(), (child) => {
            return factory.isConnectorDeclaration(child);
        });
    }

    /**
     * Checks if the current method a main method.
     * @return {boolean} - true if main method, else false.
     */
    isMainFunction() {
        return _.isEqual(this.getFunctionName(), 'main')
            && _.isEqual(_.size(this.getArguments()), 1)
            && _.isEqual(this.getArguments()[0].getType().trim(), 'string[]');
    }
}

// Auto generated Id for function definitions (for accordion views)
function autoGenerateId() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }

    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
}

export default FunctionDefinition;
