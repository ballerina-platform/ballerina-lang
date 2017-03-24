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

    /**
     * @param connectionDeclarations
     * @param variableDeclarations
     * @param workerDeclarations
     * @param statements
     * @param configStart
     * @param configEnd
     * @constructor
     */
    var CallableDefinition = function (type) {
        this.connectionDeclarations = [];
        this.variableDeclarations = [];
        this.workerDeclarations = [];
        this.statements = [];

        ASTNode.call(this, type);
    };

    CallableDefinition .prototype = Object.create(ASTNode.prototype);
    CallableDefinition .prototype.constructor = CallableDefinition ;

    CallableDefinition.prototype.setConnectionDeclarations = function (connectionDeclarations, options) {
        if (!_.isNil(connectionDeclarations)) {
            this.setAttribute('connectionDeclarations', connectionDeclarations, options);
        }
    };

    CallableDefinition.prototype.setVariableDeclarations = function (variableDeclarations, options) {
        if (!_.isNil(variableDeclarations)) {
            this.setAttribute('variableDeclarations', variableDeclarations, options);
        }
    };

    CallableDefinition.prototype.setWorkerDeclarations = function (workerDeclarations, options) {
        if (!_.isNil(workerDeclarations)) {
            this.setAttribute('workerDeclarations', workerDeclarations, options);
        }
    };

    CallableDefinition.prototype.setStatements = function (statements, options) {
        if (!_.isNil(statements)) {
            this.setAttribute('statements', statements, options);
        }
    };

    CallableDefinition.prototype.getConnectionDeclarations = function () {
        return this.connectionDeclarations;
    };

    CallableDefinition.prototype.getVariableDeclarations = function () {
        return this.variableDeclarations;
    };

    CallableDefinition.prototype.getWorkerDeclarations = function () {
       return this.workerDeclarations;
    };

    CallableDefinition.prototype.getStatements = function () {
        return this.statements;
    };

    return CallableDefinition;

});
