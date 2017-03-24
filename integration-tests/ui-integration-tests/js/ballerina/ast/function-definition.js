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
define(['lodash', 'log', './node', './callable-definition', '../utils/common-utils'],
    function (_, log, ASTNode, CallableDefinition, CommonUtils) {

    /**
     * Constructor for FunctionDefinition
     * @param {Object} args - The arguments to create the FunctionDefinition
     * @param {string} [args.functionName=newFunction] - Function name
     * @param {boolean} [args.isPublic=false] - Public or not of the function
     * @param {string[]} [args.annotations] - Function annotations
     * @constructor
     */
    var FunctionDefinition = function (args) {
        this.id = autoGenerateId();
        CallableDefinition.call(this, 'Function');
        this._functionName = _.get(args, 'functionName');
        this._isPublic = _.get(args, "isPublic") || false;
        this._annotations = _.get(args, 'annotations', []);
        this.BallerinaASTFactory = this.getFactory();
    };

    FunctionDefinition.prototype = Object.create(CallableDefinition.prototype);
    FunctionDefinition.prototype.constructor = FunctionDefinition;

    // Auto generated Id for function definitions (for accordion views)
    function autoGenerateId(){
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    }

    FunctionDefinition.prototype.setFunctionName = function(name, options){
        if (!_.isNil(name) && ASTNode.isValidIdentifier(name)) {
            this.setAttribute('_functionName', name, options);
        } else {
            var errorString = "Invalid function name: " + name;
            log.error(errorString);
            throw errorString;
        }
    };

    FunctionDefinition.prototype.setIsPublic = function(isPublic, options){
        if(!_.isNil(isPublic)){
            this.setAttribute('_isPublic', isPublic, options);
        }
    };

    FunctionDefinition.prototype.getFunctionName = function () {
        return this._functionName;
    };

    FunctionDefinition.prototype.getArguments = function () {
        var functionArgs = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isArgument(child) && !self.BallerinaASTFactory.isReturnType(child)) {
                functionArgs.push(child);
            }
        });
        return functionArgs;
    };

    FunctionDefinition.prototype.getIsPublic = function () {
        return this._isPublic;
    };

    FunctionDefinition.prototype.getVariableDefinitionStatements = function () {
        var variableDefinitionStatements = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDefinitionStatement(child)) {
                variableDefinitionStatements.push(child);
            }
        });
        return variableDefinitionStatements;
    };

    /**
     * Adds new variable declaration.
     */
    FunctionDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
        // Get the index of the last variable declaration.
        var self = this;

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
     * Adds new variable declaration.
     */
    FunctionDefinition.prototype.removeVariableDeclaration = function (variableDeclarationIdentifier) {
        var self = this;
        // Removing the variable from the children.
        var variableDeclarationChild = _.find(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isVariableDeclaration(child)
                && child.getIdentifier() === variableDeclarationIdentifier;
        });
        this.removeChild(variableDeclarationChild);
    };

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    FunctionDefinition.prototype.getArgumentsAsString = function () {
        var argsAsString = "";
        var args = this.getArguments();
        _.forEach(args, function(argument, index){
            argsAsString += argument.type + " ";
            argsAsString += argument.identifier;
            if (args.length - 1 != index) {
                argsAsString += ", ";
            }
        });

        return argsAsString;
    };

    /**
     * Adds new argument to the function definition.
     * @param type - The type of the argument.
     * @param identifier - The identifier of the argument.
     */
    FunctionDefinition.prototype.addArgument = function(type, identifier) {
        //creating resource argument
        var newArgument = this.BallerinaASTFactory.createArgument();
        newArgument.setType(type);
        newArgument.setIdentifier(identifier);

        var self = this;

        // Get the index of the last resource argument declaration.
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isArgument(child);
        });

        this.addChild(newArgument, index + 1);
    };

    /**
     * Removes an argument from a function definition.
     * @param identifier - The identifier of the argument.
     * @return {Array} - The removed argument.
     */
    FunctionDefinition.prototype.removeArgument = function(identifier) {
        var self = this;
        _.remove(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isArgument(child) && child.getIdentifier() === identifier;
        });
    };

    //// Start of return type functions.

    /**
     * Gets the return type as a string separated by commas.
     * @return {string} - Return types.
     */
    FunctionDefinition.prototype.getReturnTypesAsString = function () {
        var returnTypes = [];
        _.forEach(this.getReturnTypes(), function (returnTypeChild) {
            returnTypes.push(returnTypeChild.getArgumentAsString())
        });

        return _.join(returnTypes, ", ");
    };

    /**
     * Gets the defined return types.
     * @return {Argument[]} - Array of args.
     */
    FunctionDefinition.prototype.getReturnTypes = function () {
        var returnTypeModel = this.getReturnTypeModel();
        return !_.isUndefined(returnTypeModel) ? this.getReturnTypeModel().getChildren().slice(0) : [];
    };

    /**
     * Adds a new argument to return type model.
     * @param {string} type - The type of the argument.
     * @param {string} identifier - The identifier of the argument.
     */
    FunctionDefinition.prototype.addReturnType = function (type, identifier) {
        var self = this;

        // Adding return type mode if it doesn't exists.
        if (_.isUndefined(this.getReturnTypeModel())) {
            this.addChild(this.BallerinaASTFactory.createReturnType());
        }

        // Check if there is already a return type with the same identifier.
        if (!_.isUndefined(identifier)) {
            _.forEach(this.getReturnTypeModel().getChildren(), function(child) {
                if (child.getIdentifier() === identifier) {
                    var errorString = "An argument with identifier '" + identifier + "' already exists.";
                    log.error(errorString);
                    throw errorString;
                }
            });
        }

        // Validating whether return type can be added based on identifiers of other return types.
        if (!_.isUndefined(identifier)) {
            if (!this.hasNamedReturnTypes() && this.hasReturnTypes()) {
                var errorStringWithoutIdentifiers = "Return types without identifiers already exists. Remove them to " +
                    "add return types with identifiers.";
                log.error(errorStringWithoutIdentifiers);
                throw errorStringWithoutIdentifiers;
            }
        } else {
            if (this.hasNamedReturnTypes() && this.hasReturnTypes()) {
                var errorStringWithIdentifiers = "Return types with identifiers already exists. Remove them to add " +
                    "return types without identifiers.";
                log.error(errorStringWithIdentifiers);
                throw errorStringWithIdentifiers;
            }
        }

        var argument = this.BallerinaASTFactory.createArgument({type: type, identifier: identifier});

        var existingReturnType = self.getReturnTypeModel();

        // Adding the new argument input position.
        if (!_.isNil(existingReturnType)) {
            existingReturnType.addChild(argument, existingReturnType.getChildren().length + 1);
        } else {
            var returnType = this.BallerinaASTFactory.createReturnType();
            returnType.addChild(argument, 0);
            this.addChild(returnType);
        }
    };

    FunctionDefinition.prototype.hasNamedReturnTypes = function () {
        if (_.isUndefined(this.getReturnTypeModel())) {
            return false;
        } else if (this.getReturnTypeModel().getChildren().length == 0) {
            //if there are no return types in the return type model
            return false;
        } else {
            //check if any of the return types have identifiers
            var indexWithoutIdentifiers = _.findIndex(this.getReturnTypeModel().getChildren(), function (child) {
                return _.isUndefined(child.getIdentifier());
            });

            if (indexWithoutIdentifiers !== -1) {
                return false;
            } else {
                return true;
            }
        }
    };

    FunctionDefinition.prototype.hasReturnTypes = function () {
        if (_.isUndefined(this.getReturnTypeModel())) {
            return false;
        } else {
            if (this.getReturnTypeModel().getChildren().length > 0) {
                return true;
            } else {
                return false;
            }
        }
    };

    /**
     * Removes return type argument from the return type model.
     * @param {string} modelID - The id of an {Argument} which resides in the return type model.
     */
    FunctionDefinition.prototype.removeReturnType = function (modelID) {
        var self = this;
        var argumentToRemove = undefined;

        // Find the argument to remove/delete.
        _.forEach(self.getReturnTypeModel().getChildren(), function (argument) {
            if (argument.getID() == modelID) {
                argumentToRemove = argument;
                // break
                return false;
            }
        });

        // Deleting the argument from the AST.
        if (!_.isUndefined(argumentToRemove)) {
            self.getReturnTypeModel().removeChild(argumentToRemove);
        } else {
            var exceptionString = "Could not find a return type with ID: " + modelID;
            log.error(exceptionString);
            throw exceptionString;
        }
    };

    /**
     * Gets the return type model. A function definition can have only one {ReturnType} model.
     * @return {ReturnType|undefined} - The return type model.
     */
    FunctionDefinition.prototype.getReturnTypeModel = function() {
        var self = this;
        var returnTypeModel = undefined;
        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isReturnType(child)) {
                returnTypeModel = child;
                return false; // break
            }
        });
        return returnTypeModel;
    };

    //// End of return type functions.

    FunctionDefinition.prototype.getConnectionDeclarations = function () {
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
     * Override the super call to addChild
     * @param child
     * @param index
     */
    FunctionDefinition.prototype.addChild = function (child, index) {
        if (this.BallerinaASTFactory.isWorkerDeclaration(child)) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
        }
    };

    FunctionDefinition.prototype.getWorkerDeclarations = function () {
        var workerDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    FunctionDefinition.prototype.canBeParentOf = function (node) {
        return this.BallerinaASTFactory.isConnectorDeclaration(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isWorkerDeclaration(node)
            || this.BallerinaASTFactory.isStatement(node);
    };

    /**
     * initialize FunctionDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.function_name] - Name of the function definition
     * @param {string} [jsonNode.annotations] - Annotations of the function definition
     * @param {boolean} [jsonNode.is_public_function] - Public or not of the function
     */
    FunctionDefinition.prototype.initFromJson = function (jsonNode) {
        this.setFunctionName(jsonNode.function_name, {doSilently: true});
        this.setIsPublic(jsonNode.is_public_function, {doSilently: true});
        this._annotations = jsonNode.annotations;

        var self = this;

        _.each(jsonNode.children, function (childNode) {
            var child = undefined;
            var childNodeTemp = undefined;
            //TODO : generalize this logic
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
     * @inheritDoc
     * @override
     */
    FunctionDefinition.prototype.generateUniqueIdentifiers = function () {
        CommonUtils.generateUniqueIdentifier({
            node: this,
            attributes: [{
                defaultValue: "newFunction",
                setter: this.setFunctionName,
                getter: this.getFunctionName,
                parents: [{
                    // ballerina-ast-node
                    node: this.parent,
                    getChildrenFunc: this.parent.getFunctionDefinitions,
                    getter: this.getFunctionName
                }]
            }]
        });
    };

    /**
     * Get the connector by name
     * @param {string} connectorName
     * @return {ConnectorDeclaration}
     */
    FunctionDefinition.prototype.getConnectorByName = function (connectorName) {
        var factory = this.getFactory();
        var connectorReference = _.find(this.getChildren(), function (child) {
            return (factory.isConnectorDeclaration(child) && (child.getConnectorVariable() === connectorName));
        });

        return connectorReference;
    };

    /**
     * Checks if the current method a main method.
     * @return {boolean} - true if main method, else false.
     */
    FunctionDefinition.prototype.isMainFunction = function () {
        return _.isEqual(this.getFunctionName(), "main")
            && _.isEqual(_.size(this.getArguments()), 1)
            && _.isEqual(this.getArguments()[0].getType().trim(), "string[]");
    };

    return FunctionDefinition;

});
