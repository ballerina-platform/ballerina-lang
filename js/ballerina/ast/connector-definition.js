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
define(['lodash', './node'], function(_, ASTNode){

    var ConnectorDefinition = function() {
        this.id = autoGenerateId();
        this.connectionDeclarations =  [];
        this.variableDeclarations =  [];
        this.actionDefinitions =  [];

        ASTNode.call(this);
    };

    ConnectorDefinition.prototype = Object.create(ASTNode.prototype);
    ConnectorDefinition.prototype.constructor = ConnectorDefinition;

    // Auto generated Id for service definitions (for accordion views)
    function autoGenerateId(){
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    };

    ConnectorDefinition.prototype.setConnectionDeclarations = function (connectionDeclarations) {
        if (!_.isNil(connectionDeclarations)) {
            this.connectionDeclarations = connections;
        }
    };

    ConnectorDefinition.prototype.getConnectionDeclaration = function () {
            return this.connectionDeclarations;
    };

    ConnectorDefinition.prototype.setVariableDeclarations = function (variableDeclarations) {
        if (!_.isNil(variableDeclarations)) {
            this.variableDeclarations = variableDeclarations;
        }
    };

    ConnectorDefinition.prototype.getVariableDeclarations = function () {
            return this.variableDeclarations;
    };

    ConnectorDefinition.prototype.setActionDefinitions = function (actionDefinitions) {
        if (!_.isNil(actionDefinitions)) {
            this.actionDefinitions = actionDefinitions;
        }
    };

    ConnectorDefinition.prototype.getActionDefinitions = function () {
            return this.actionDefinitions;
    };

    ConnectorDefinition.prototype.accept = function (visitor) {
        visitor.visitConnectionDeclaration(this);


    };

    return ConnectorDefinition;
});