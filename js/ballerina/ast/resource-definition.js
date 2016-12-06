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
define(['lodash', './node'], function (_, ASTNode) {

    var ResourceDefinition = function (args) {
        this._path = _.get(args, 'path');
        this._method = _.get(args, 'method');
        this.connectionDeclarations =_.get(args, 'connectorDefinitions', []);
        this.variableDeclarations =_.get(args, 'variableDeclarations', []);
        this.workerDeclarations = _.get(args, 'workerDeclarations', []);
        this.statements = _.get(args, 'statements', []);
        this.resourceArguments = _.get(args, 'resourceArguments', []);

        ASTNode.call(this);
    };

    ResourceDefinition.prototype = Object.create(ASTNode.prototype);
    ResourceDefinition.prototype.constructor = ResourceDefinition;


    ResourceDefinition.prototype.setResourcePath = function (path) {
        if (!_.isNil(path)) {
            this._path = path;
        }
    };

    ResourceDefinition.prototype.setResourceMethod = function (method) {
        if (!_.isNil(method)) {
            this._method = method;
        }
    };

    ResourceDefinition.prototype.setConnections = function (connections) {
        if (!_.isNil(connections)) {
            this.connectionDeclarations = connections;
        }
    };
    ResourceDefinition.prototype.setVariables = function (variables) {
        if (!_.isNil(variables)) {
            this.variableDeclarations = variables;
        }
    };
    ResourceDefinition.prototype.setWorkers = function (workers) {
        if (!_.isNil(workers)) {
            this.workerDeclarations = workers;
        }
    };

    ResourceDefinition.prototype.setStatements = function (statements) {
        if (!_.isNil(statements)) {
            this.statements = statements;
        }
    };

    ResourceDefinition.prototype.setResourceArguments = function (resourceArgs) {
        if (!_.isNil(resourceArgs)) {
            this.resourceArguments = resourceArgs;
        }
    };

    ResourceDefinition.prototype.getStatements = function () {
        return this.statements;
    };

    ResourceDefinition.prototype.getWorkers = function () {
        return this.workerDeclarations;
    };

    ResourceDefinition.prototype.getVariables = function () {
        return this.variableDeclarations;
    };

    ResourceDefinition.prototype.getConnections = function () {
        return this.connectionDeclarations;
    };

    ResourceDefinition.prototype.getResourceMethod = function () {
        return this._method;
    };

    ResourceDefinition.prototype.getResourcePath = function () {
        return this._path;
    };

    ResourceDefinition.prototype.getResourceArguments = function () {
        return this.resourceArguments;
    };

    ResourceDefinition.prototype.resourceParent = function (parent) {
        if (!_.isUndefined(parent)) {
            this.parent = parent;
        } else {
            return this.parent;
        }
    };

    return ResourceDefinition;
});
