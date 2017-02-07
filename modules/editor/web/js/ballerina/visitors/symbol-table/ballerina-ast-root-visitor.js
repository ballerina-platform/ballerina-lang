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
define(['lodash', 'log', 'event_channel', './abstract-symbol-table-gen-visitor', './../../env/connector', './../../env/ballerina-env-factory', './../../ast/ballerina-ast-factory'],
    function (_, log, EventChannel, AbstractSymbolTableGenVisitor, Connector, BallerinaEnvFactory, BallerinaASTFactory) {

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
            this._model.on('child-removed', function (child) {
                if (BallerinaASTFactory.isFunctionDefinition(child)) {
                    this.removeFunctionDefinition(child);
                }
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

        /**
         * visit function definition
         * @param {Object} functionDefinition - function definition model
         */
        BallerinaASTRootVisitor.prototype.visitFunctionDefinition = function (functionDefinition) {
            var functionDef = BallerinaEnvFactory.createFunction();
            functionDef.setName(functionDefinition.getFunctionName());
            functionDef.setId(functionDefinition.getFunctionName());
            this.getPackage().addFunctionDefinitions(functionDef);

            var self = this;
            functionDefinition.on('tree-modified', function (modifiedData) {
                var attributeName = modifiedData.data.attributeName;
                var newValue = modifiedData.data.newValue;
                var oldValue = modifiedData.data.oldValue;
                if (_.isEqual(attributeName, '_functionName')) {
                    self.updateFunctionDefinition(oldValue, newValue);
                }
            });
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
            var connector = BallerinaEnvFactory.createConnector();
            connector.setName(connectorDefinition.getConnectorName());
            this.getPackage().addConnectors(connector);

            _.each(connectorDefinition.getChildren(), function (child) {
                if (BallerinaASTFactory.isConnectorAction(child)) {
                    var connectorAction = BallerinaEnvFactory.createConnectorAction();
                    connectorAction.setName(child.getActionName());
                    connector.addAction(connectorAction);
                }
            });
            connectorDefinition.on('child-added', function (child) {
                if (BallerinaASTFactory.isConnectorAction(child)) {
                    var connectorAction = BallerinaEnvFactory.createConnectorAction();
                    connectorAction.setName(child.getActionName());
                    connector.addAction(connectorAction);
                }
            }, this);
        };

        /**
         * remove given function definition from the package object
         * @param {Object} functionDef - function definition to be removed
         */
        BallerinaASTRootVisitor.prototype.removeFunctionDefinition = function (functionDef) {
            this.getPackage().removeFunctionDefinition(functionDef);
        };

        /**
         * updates function definition with new value
         * @param {Object} oldValue - old value
         * @param {Object} newValue - new value
         */
        BallerinaASTRootVisitor.prototype.updateFunctionDefinition = function (oldValue, newValue) {
            var functionDefinition = this.getPackage().getFunctionDefinitionByName(oldValue);
            functionDefinition.setName(newValue);
            functionDefinition.setId(newValue);
        };

        return BallerinaASTRootVisitor;
    });