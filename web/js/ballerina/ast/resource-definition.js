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
define(['lodash', 'log', './node', './worker-declaration', './connector-declaration'],
    function (_, log, ASTNode, WorkerDeclaration, ConnectorDeclaration) {

    var ResourceDefinition = function (args) {
        this._path = _.get(args, 'path', '/');
        this._connectionDeclarations = _.get(args, 'connectorDefinitions', []);
        this._variableDeclarations = _.get(args, 'variableDeclarations', []);
        this._workerDeclarations = _.get(args, 'workerDeclarations', []);
        this._statements = _.get(args, 'statements', []);
        this._arguments = _.get(args, 'resourceArguments', []);
        this._resourceName = _.get(args, 'resourceName', 'Resource');
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

        // Adding the default worker declaration.
        var defaultWorker = new WorkerDeclaration({isDefaultWorker: true});
        this._workerDeclarations.push(defaultWorker);
    };

    ResourceDefinition.prototype = Object.create(ASTNode.prototype);
    ResourceDefinition.prototype.constructor = ResourceDefinition;

    ResourceDefinition.prototype.setConnections = function (connections) {
        if (!_.isNil(connections)) {
            this._connectionDeclarations = connections;
        }
    };
    ResourceDefinition.prototype.setVariableDeclarations = function (variables) {
        if (!_.isNil(variables)) {
            this._variableDeclarations = variables;
        }
    };
    ResourceDefinition.prototype.setWorkers = function (workers) {
        if (!_.isNil(workers)) {
            this._workerDeclarations = workers;
        }
    };

    ResourceDefinition.prototype.setStatements = function (statements) {
        if (!_.isNil(statements)) {
            this._statements = statements;
        }
    };

    ResourceDefinition.prototype.setArguments = function (resourceArgs) {
        if (!_.isNil(resourceArgs)) {
            this._arguments = resourceArgs;
        }
    };

    ResourceDefinition.prototype.setResourceName = function (resourceName) {
        if (!_.isNil(resourceName)) {
            this._resourceName = resourceName;
        } else {
            log.error('Invalid Resource name [' + resourceName + '] Provided');
            throw 'Invalid Resource name [' + resourceName + '] Provided';
        }
    };

    ResourceDefinition.prototype.getStatements = function () {
        return this._statements;
    };

    ResourceDefinition.prototype.getWorkers = function () {
        return this._workerDeclarations;
    };

    ResourceDefinition.prototype.getVariableDeclarations = function () {
        return this._variableDeclarations;
    };

    ResourceDefinition.prototype.getConnections = function () {
        return this._connectionDeclarations;
    };

    ResourceDefinition.prototype.getArguments = function () {
        return this._arguments;
    };

    ResourceDefinition.prototype.getResourceName = function () {
        return this._resourceName;
    };

    ResourceDefinition.prototype.getAnnotations = function () {
        return this._annotations;
    };

    /**
     * Returns the list of arguments as a string separated by commas.
     * @return {string} - Arguments as string.
     */
    ResourceDefinition.prototype.getArgumentsAsString = function () {
        var argsAsString = "";
        var args = this._arguments;
        _.forEach(this._arguments, function(argument, index){
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
        this._arguments.push({
            type: type,
            identifier: identifier
        })
    };

    /**
     * Removes an argument from a resource definition.
     * @param identifier - The identifier of the argument.
     * @return {Array} - The removed argument.
     */
    ResourceDefinition.prototype.removeArgument = function(identifier) {
        return  _.remove(this._arguments, function(functionArg) {
            return functionArg.identifier === identifier;
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
        if (child instanceof ConnectorDeclaration) {
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
        var BallerinaASTFactory = this.getFactory();
        return BallerinaASTFactory.isConnectorDeclaration(node)
            || BallerinaASTFactory.isVariableDeclaration(node)
            || BallerinaASTFactory.isWorkerDeclaration(node)
            || BallerinaASTFactory.isStatement(node);
    };

    /**
     * initialize from json
     * @param jsonNode
     */
    ResourceDefinition.prototype.initFromJson = function (jsonNode) {
        //TODO : add annotations
        this._resourceName = jsonNode.resource_name;

        var self = this;
        var BallerinaASTFactory = this.getFactory();

        _.each(jsonNode.children, function (childNode) {
            var child = BallerinaASTFactory.createFromJson(childNode);
            self.addChild(child);
        });
    };

    return ResourceDefinition;
});
