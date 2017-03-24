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
define(['lodash', 'log', 'event_channel', './abstract-symbol-table-gen-visitor'],
    function(_, log, EventChannel, AbstractSymbolTableGenVisitor) {

        /**
         * @param package
         * @constructor
         */
        var ConnectorDefinitionVisitor = function (package) {
            AbstractSymbolTableGenVisitor.call(this, package);
        };

        ConnectorDefinitionVisitor.prototype = Object.create(AbstractSymbolTableGenVisitor.prototype);
        ConnectorDefinitionVisitor.prototype.constructor = ConnectorDefinitionVisitor;

        ConnectorDefinitionVisitor.prototype.canVisitConnectorDefinition = function(connectorDefinition){
            return true;
        };

        ConnectorDefinitionVisitor.prototype.beginVisitConnectorDefinition = function(connectorDefinition){

        };

        ConnectorDefinitionVisitor.prototype.visitConnectorDefinition = function(connectorDefinition){

        };

        ConnectorDefinitionVisitor.prototype.endVisitConnectorDefinition = function(connectorDefinition){

        };

        ConnectorDefinitionVisitor.prototype.visitConnectorActionDefinition = function(connectorActionDefinition){
            log.debug('Visit ConnectorActionDefinition');
            this.getPackage().getConnectorDefinition(connectorActionDefinition.getConnector()).addConnectorActionDefinitions(connectorActionDefinition);
        };

        return ConnectorDefinitionVisitor;
    });