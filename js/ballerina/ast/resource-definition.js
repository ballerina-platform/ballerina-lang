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

    var ResourceDefinition = function (path, method, connections, variables, workers, statements, resourceArgs) {
        this._path = path;
        this._method = method;
        this.connectionDeclarations = connections || [];
        this.variableDeclarations = variables || [];
        this.workerDeclarations = workers || [];
        this.statements = statements || [];
        this.resourceArguments = resourceArgs || [];

    };

    ResourceDefinition.prototype = Object.create(ASTNode.prototype);
    ResourceDefinition.prototype.constructor = ResourceDefinition;


    ResourceDefinition.prototype.setResourcePath = function (path) {
        if (!_.isEmpty(path)) {
            this._path = path;
        }
    };

    ResourceDefinition.prototype.getResourcePath = function () {
        return this._path;
    };

    ResourceDefinition.prototype.setResourceMethod = function (method) {
        if (!_.isEmpty(method)) {
            this._method = method;
        }
    };

    ResourceDefinition.prototype.getResourceMethod = function () {
        return this._method;
    };

    ResourceDefinition.prototype.setConnections = function (connections) {
        if (!_.isEmpty(connections)) {
            this.connectionDeclarations = connections;
        }
    };

    ResourceDefinition.prototype.getConnections = function () {
        return this.connectionDeclarations;
    };
    ResourceDefinition.prototype.setVariables = function (variables) {
        if (!_.isEmpty(variables)) {
            this.variableDeclarations = variables;
        }
    };

    ResourceDefinition.prototype.getVariables = function () {
        return this.variableDeclarations;
    };
    ResourceDefinition.prototype.setWorkers = function (workers) {
        if (!_.isEmpty(workers)) {
            this.workerDeclarations = workers;
        }
    };

    ResourceDefinition.prototype.getWorkers = function () {
        return this.workerDeclarations;
    };
    ResourceDefinition.prototype.setStatements = function (statements) {
        if (!_.isEmpty(statements)) {
            this.statements = statements;
        }
    };

    ResourceDefinition.prototype.getStatements = function () {
        return this.statements;
    };
    ResourceDefinition.prototype.setResourceArguments = function (resourceArgs) {
        if (!_.isEmpty(resourceArgs)) {
            this.resourceArguments = resourceArgs;
        }
    };

    ResourceDefinition.prototype.getResourceArguments = function () {
        return this.resourceArguments;
    };

return ResourceDefinition;
});