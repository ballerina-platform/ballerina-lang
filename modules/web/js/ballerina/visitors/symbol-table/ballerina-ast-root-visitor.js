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
                } else if (BallerinaASTFactory.isConnectorDefinition(child)) {
                    this.removeConnectorDefinition(child);
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

            // Adding arguments
            var args = [];
            _.forEach(functionDefinition.getArguments(), function (argument) {
                args.push({
                    name: argument.getIdentifier(),
                    type: argument.getType()
                })
            });
            functionDef.setParameters(args);

            // Adding return types
            var returnTypes = [];
            _.forEach(functionDefinition.getReturnTypes(), function (returnType) {
                // Return type contains an Argument child.
                returnTypes.push({
                    name: returnType.getIdentifier(),
                    type: returnType.getType()
                })
            });
            functionDef.setReturnParams(returnTypes);

            this.getPackage().addFunctionDefinitions(functionDef);

            var self = this;
            functionDefinition.on('tree-modified', function (modifiedData) {
                var attributeName = modifiedData.data.attributeName;
                var oldValue = modifiedData.data.oldValue;
                var newValue = modifiedData.data.newValue;
                if (BallerinaASTFactory.isFunctionDefinition(modifiedData.origin)) {
                    if (!_.isEqual(attributeName, '_functionName')) {
                        newValue = undefined;
                    }
                    self.updateFunctionDefinition(modifiedData.origin, oldValue, newValue, modifiedData.data.child);
                } else if (BallerinaASTFactory.isReturnType(modifiedData.origin)){
                    self.updateFunctionDefinition(modifiedData.origin.getParent(), oldValue, newValue, modifiedData.origin);
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
            connector.setId(connectorDefinition.getConnectorName());
            this.getPackage().addConnectors(connector);

            var self = this;
            connectorDefinition.on('tree-modified', function (modifiedData) {
                var attributeName = modifiedData.data.attributeName;
                var newValue = modifiedData.data.newValue;
                var oldValue = modifiedData.data.oldValue;
                if (BallerinaASTFactory.isConnectorDefinition(modifiedData.origin)) {
                    self.updateConnectorDefinition(connector, modifiedData);
                }
            });

            //TODO : move this to the visit method
            _.each(connectorDefinition.getChildren(), function (child) {
                if (BallerinaASTFactory.isConnectorAction(child)) {
                    var connectorAction = BallerinaEnvFactory.createConnectorAction();
                    connectorAction.initFromASTModel(child);
                    connector.addAction(connectorAction);

                    child.on('tree-modified', function (modifiedData) {
                        var attributeName = modifiedData.data.attributeName;
                        var newValue = modifiedData.data.newValue;
                        var oldValue = modifiedData.data.oldValue;
                        if (BallerinaASTFactory.isConnectorAction(modifiedData.origin) && _.isEqual(attributeName, 'action_name')) {
                            self.updateConnectorActionDefinition(child.getParent().getConnectorName(), oldValue, newValue);
                        }
                    });

                } else if (BallerinaASTFactory.isResourceParameter(child)){
                    connector.addParam(child);
                }
            });
            connectorDefinition.on('child-added', function (child) {
                if (BallerinaASTFactory.isConnectorAction(child)) {
                    var connectorAction = BallerinaEnvFactory.createConnectorAction();
                    connectorAction.initFromASTModel(child);
                    connector.addAction(connectorAction);

                    child.on('tree-modified', function (modifiedData) {
                        var attributeName = modifiedData.data.attributeName;
                        var newValue = modifiedData.data.newValue;
                        var oldValue = modifiedData.data.oldValue;
                        if (BallerinaASTFactory.isConnectorAction(modifiedData.origin) && _.isEqual(attributeName, 'action_name')) {
                            self.updateConnectorActionDefinition(child.getParent().getConnectorName(), oldValue, newValue);
                        }
                    });
                }
            }, this);

            connectorDefinition.on('child-removed', function (child) {
                if (BallerinaASTFactory.isConnectorAction(child)) {
                    self.removeConnectorActionDefinition(connectorDefinition, child);
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
         * remove given connector definition from the package object
         * @param {Object} connectorDef - connector definition to be removed
         */
        BallerinaASTRootVisitor.prototype.removeConnectorDefinition = function (connectorDef) {
            this.getPackage().removeConnectorDefinition(connectorDef);
        };

        /**
         * remove given connector action definition from the package object
         * @param {Object} connectorDef - connector definition
         * @param connectorActionDef - connector action definition to be removed
         */
        BallerinaASTRootVisitor.prototype.removeConnectorActionDefinition = function (connectorDef, connectorActionDef) {
            this.getPackage().getConnectorByName(connectorDef.getConnectorName()).removeAction(connectorActionDef);
        };

        /**
         * updates function definition with new value
         * @param {Object} functionDefinition - function name
         * @param {Object} newValue - new value
         * @param {Object} child - child which is getting modified
         */
        BallerinaASTRootVisitor.prototype.updateFunctionDefinition = function (functionDefinition, oldValue, newValue, child) {
            var funcName = functionDefinition.getFunctionName();
            var functionDef = this.getPackage().getFunctionDefinitionByName(funcName);
            if (newValue) {
                functionDef = this.getPackage().getFunctionDefinitionByName(oldValue);
                functionDef.setName(newValue);
                functionDef.setId(newValue);
            }
            if (child) {
                if (BallerinaASTFactory.isReturnType(child)) {
                    var returnTypes = [];
                    _.forEach(functionDefinition.getReturnTypes(), function (returnType) {
                        returnTypes.push({
                            name: returnType.getIdentifier(),
                            type: returnType.getType()
                        })
                    });
                    functionDef.setReturnParams(returnTypes);
                } else if (BallerinaASTFactory.isArgument(child)) {
                    var args = [];
                    _.forEach(functionDefinition.getArguments(), function (argument) {
                        args.push({
                            name: argument.getIdentifier(),
                            type: argument.getType()
                        })
                    });
                    functionDef.setParameters(args);
                }
            }
        };

        /**
         * updates connector definition with new value
         * @param {Object} oldValue - old value
         * @param {Object} newValue - new value
         */
        BallerinaASTRootVisitor.prototype.updateConnectorDefinition = function (connectorDefinition, modifiedData) {
            if (modifiedData.type === 'child-added') {
                // child_added sends different format of data
                if (BallerinaASTFactory.isArgument(modifiedData.data.child)) {
                    connectorDefinition.addParam(modifiedData.data.child);
                    return;
                }
            }

            var attributeName = modifiedData.data.attributeName;
            var newValue = modifiedData.data.newValue;
            var oldValue = modifiedData.data.oldValue;
            switch (attributeName) {
                case 'connector_name':
                    connectorDefinition.setName(newValue);
                    connectorDefinition.setId(newValue);
                    break;

            }
        };

        /**
         * updates connector definition with new value
         * @param {Object} connector - connector for the action
         * @param {Object} oldValue - old value
         * @param {Object} newValue - new value
         */
        BallerinaASTRootVisitor.prototype.updateConnectorActionDefinition = function (connector, oldValue, newValue) {
            var connectorActionDefinition = this.getPackage().getConnectorByName(connector).getActionByName(oldValue);
            connectorActionDefinition.setName(newValue);
            connectorActionDefinition.setId(newValue);
        };

        return BallerinaASTRootVisitor;
    });
