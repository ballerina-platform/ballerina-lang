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
define(['lodash', 'log', './node'],
    function (_, log, ASTNode) {

    /**
     * Constructor for ResourceDefinition
     * @param {Object} args - The arguments to create the ServiceDefinition
     * @param {string} [args.resourceName=newResource] - Service name
     * @param {string[]} [args.annotations] - Resource annotations
     * @param {string} [args.annotations.Method] - Resource annotation for Method
     * @param {string} [args.annotations.Path] - Resource annotation for Path
     * @constructor
     */
    var ResourceDefinition = function (args) {
        this._resourceName = _.get(args, 'resourceName', 'newResource');
        this._annotations = _.get(args, 'annotations', []);

        // Adding available annotations and their default values.
        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Method";
            }))) {
            this._annotations.push({
                key: "Method",
                value: "GET"
            });
        }

        if (_.isNil(_.find(this._annotations, function (annotation) {
                return annotation.key == "Path";
            }))) {
            this._annotations.push({
                key: "Path",
                value: ""
            });
        }

        // TODO: All the types should be referred from the global constants
        ASTNode.call(this, 'Resource', 'resource {', '}');

        this.BallerinaASTFactory = this.getFactory();

        // Adding the default worker declaration.
        var defaultWorker = this.BallerinaASTFactory.createWorkerDeclaration({isDefaultWorker: true});
        this.addChild(defaultWorker);
    };

    ResourceDefinition.prototype = Object.create(ASTNode.prototype);
    ResourceDefinition.prototype.constructor = ResourceDefinition;

    ResourceDefinition.prototype.setResourceName = function (resourceName) {
        if (!_.isNil(resourceName)) {
            this._resourceName = resourceName;
        } else {
            log.error('Invalid Resource name [' + resourceName + '] Provided');
            throw 'Invalid Resource name [' + resourceName + '] Provided';
        }
    };

    ResourceDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    ResourceDefinition.prototype.getArguments = function () {
        var resourceArgs = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isResourceArgument(child)) {
                resourceArgs.push(child);
            }
        });
        return resourceArgs;
    };

    ResourceDefinition.prototype.getResourceName = function () {
        return this._resourceName;
    };

    ResourceDefinition.prototype.getAnnotations = function () {
        return this._annotations;
    };

    /**
     * Adds new variable declaration.
     */
    ResourceDefinition.prototype.addVariableDeclaration = function (newVariableDeclaration) {
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
     * Adds new variable declaration.
     */
    ResourceDefinition.prototype.removeVariableDeclaration = function (variableDeclaration) {
        this.removeChild(variableDeclaration);
    };

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    ResourceDefinition.prototype.getArgumentsAsString = function () {
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
     * Adds new argument to the resource definition.
     * @param type - The type of the argument.
     * @param identifier - The identifier of the argument.
     */
    ResourceDefinition.prototype.addArgument = function(type, identifier) {
        //creating resource argument
        var newResourceArgument = this.BallerinaASTFactory.createResourceArgument();
        newResourceArgument.setType(type);
        newResourceArgument.setIdentifier(identifier);

        var self = this;

        // Get the index of the last resource argument declaration.
        var index = _.findLastIndex(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isResourceArgument(child);
        });

        this.addChild(newResourceArgument, index + 1);
    };

    /**
     * Removes an argument from a resource definition.
     * @param identifier - The identifier of the argument.
     * @return {Array} - The removed argument.
     */
    ResourceDefinition.prototype.removeArgument = function(identifier) {
        var self = this;
        // Deleting the variable from the children.
        _.remove(this.getChildren(), function (child) {
            return self.BallerinaASTFactory.isResourceArgument(child) && child.getIdentifier() === identifier;
        });
    };

    ResourceDefinition.prototype.resourceParent = function (parent) {
        if (!_.isUndefined(parent)) {
            this.parent = parent;
        } else {
            return this.parent;
        }
    };

    /**
     * Override the super call to addChild
     * @param child
     * @param index
     */
    ResourceDefinition.prototype.addChild = function (child, index) {
        if (this.BallerinaASTFactory.isConnectorDeclaration(child)) {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, 0);
        } else {
            Object.getPrototypeOf(this.constructor.prototype).addChild.call(this, child, index);
        }
    };
    /**
     * Adding/Updating an annotation.
     * @param key - Annotation key
     * @param value - Value for the annotation.
     */
    ResourceDefinition.prototype.addAnnotation = function (key, value) {
        var existingAnnotation = _.find(this._annotations, function (annotation) {
            return annotation.key == key;
        });
        if (_.isNil(existingAnnotation)) {
            // If such annotation does not exists, then add a new one.
            this._annotations.push({
                key: key,
                value: value
            });
        } else {
            // Updating existing annotation.
            existingAnnotation.value = value;
        }
    };

    /**
     * Validates possible immediate child types.
     * @override
     * @param node
     * @return {boolean}
     */
    ResourceDefinition.prototype.canBeParentOf = function (node) {
        return this.BallerinaASTFactory.isConnectorDeclaration(node)
            || this.BallerinaASTFactory.isVariableDeclaration(node)
            || this.BallerinaASTFactory.isWorkerDeclaration(node)
            || this.BallerinaASTFactory.isStatement(node);
    };

    /**
     * initialize ResourceDefinition from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.resource_name] - Name of the resource definition
     * @param {string} [jsonNode.annotations] - Annotations of the resource definition
     */
    ResourceDefinition.prototype.initFromJson = function (jsonNode) {
        this._resourceName = jsonNode.resource_name;
        this._annotations = jsonNode.annotations;

        var self = this;

        _.each(jsonNode.children, function (childNode) {
            var child = self.BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
        });
    };

    return ResourceDefinition;
});
