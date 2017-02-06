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
define(['lodash', 'log', 'event_channel', './abstract-symbol-table-gen-visitor', './../../env/connector', './../../env/ballerina-env-factory'],
    function (_, log, EventChannel, AbstractSymbolTableGenVisitor, Connector, BallerinaEnvFactory) {

        var BallerinaASTRootVisitor = function (package, model) {
            AbstractSymbolTableGenVisitor.call(this, package);
            this._model = model;
            this.init();
        };

        BallerinaASTRootVisitor.prototype = Object.create(AbstractSymbolTableGenVisitor.prototype);
        BallerinaASTRootVisitor.prototype.constructor = BallerinaASTRootVisitor;

        /**
         * init function
         */
        BallerinaASTRootVisitor.prototype.init = function () {
            //Registering event listeners
            this._model.on('child-added', function (child) {
                this.visit(child);
            }, this);
        };

        BallerinaASTRootVisitor.prototype.canVisitBallerinaASTRoot = function (serviceDefinition) {
            return true;
        };

        BallerinaASTRootVisitor.prototype.beginVisitBallerinaASTRoot = function (serviceDefinition) {
            log.debug('Begin Visit BallerinaASTRoot');
        };

        BallerinaASTRootVisitor.prototype.visitBallerinaASTRoot = function (ballerinaASTRoot) {
            log.debug('Visit BallerinaASTRoot');
        };

        BallerinaASTRootVisitor.prototype.endVisitBallerinaASTRoot = function (serviceDefinition) {
            log.debug('End Visit BallerinaASTRoot');
        };

        BallerinaASTRootVisitor.prototype.visitFunctionDefinition = function (functionDefinition) {
            var functionDef = BallerinaEnvFactory.createFunction();
            functionDef.setName(functionDefinition.getFunctionName());
            functionDef.setTitle(functionDefinition.getFunctionName());
            functionDef.setId(functionDefinition.getFunctionName());
            this.getPackage().addFunctionDefinitions(functionDef);
        };

        BallerinaASTRootVisitor.prototype.visitStructDefinition = function (structDefinition) {
            this.getPackage().addStructDefinitions(structDefinition);
        };

        BallerinaASTRootVisitor.prototype.visitTypeMapperDefinition = function (typeMapperDefinition) {
            //todo need to refactored
//            var typeMapperDef = BallerinaEnvFactory.createTypeMapper();
//            typeMapperDef.setName(typeMapperDefinition.getTypeMapperName());
//            typeMapperDef.setTitle(typeMapperDefinition.getTypeMapperName());
//            typeMapperDef.setId(typeMapperDefinition.getTypeMapperName());
//            typeMapperDef.setSourceAndIdentifier(typeMapperDefinition.getSourceAndIdentifier());
//            typeMapperDef.setReturnType(typeMapperDefinition.getReturnType());
            this.getPackage().addTypeMapperDefinitions(typeMapperDefinition);
        };

        /**
         * visit connector definition
         * @param {Object} connectorDefinition - connector definition model
         */
        BallerinaASTRootVisitor.prototype.visitConnectorDefinition = function (connectorDefinition) {
            var connector = new Connector();
            connector.setName(connectorDefinition.getConnectorName());
            connector.setTitle(connectorDefinition.getConnectorName());
            this.getPackage().addConnectors(connector);
        };

        return BallerinaASTRootVisitor;
    });