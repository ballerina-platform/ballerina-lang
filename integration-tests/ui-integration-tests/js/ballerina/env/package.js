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
define(['log', 'lodash', 'require', 'event_channel', './../ast/service-definition', './../ast/function-definition',
        './../ast/type-definition', './../ast/type-mapper-definition', './../ast/constant-definition',
        './../ast/struct-definition'],
    function(log, _, require, EventChannel, ServiceDefinition, FunctionDefinition,
             TypeDefinition, TypeMapperDefinition, ConstantDefinition,
             StructDefinition){

        /**
         * @class Package
         * @augments EventChannel
         * @param args {Object} - args.name: name of the package
         * @constructor
         */
        var Package = function(args){
            this.setName(_.get(args, 'name', ''));
            this.addServiceDefinitions(_.get(args, 'serviceDefinitions', []));
            this.addFunctionDefinitions(_.get(args, 'functionDefinitions', []));
            this.addStructDefinitions(_.get(args, 'structDefinitions', []));
            this._connectorDefinitions = _.get(args, 'connectors', []);
            this.addTypeDefinitions(_.get(args, 'typeDefinitions', []));
            this.addTypeMapperDefinitions(_.get(args, 'typeMapperDefinitions', []));
            this.addConstantDefinitions(_.get(args, 'constantDefinitions', []));
            this.BallerinaEnvFactory = require('./ballerina-env-factory');
        };

        Package.prototype = Object.create(EventChannel.prototype);
        Package.prototype.constructor = Package;

        Package.prototype.setName = function(name){
            if(!_.isNil(name) && _.isString(name)){
                this._name = name;
            } else {
                log.error("Invalid value for package name: ", name);
            }
        };

        Package.prototype.getName = function(){
            return this._name;
        };

        /**
         * Add constant defs
         * @param constantDefinitions - can be an array of constantDefinitions or a single constantDefinition
         * @fires Package#constant-defs-added
         */
        Package.prototype.addConstantDefinitions = function(constantDefinitions){
            var err;
            if(!_.isArray(constantDefinitions) && !(constantDefinitions instanceof  ConstantDefinition)){
                err = "Adding constant def failed. Not an instance of ConstantDefinition" + constantDefinitions;
                log.error(err);
                throw err;
            }
            if(_.isArray(constantDefinitions)){
                if(!_.isEmpty(constantDefinitions)){
                    _.each(constantDefinitions, function(constantDefinition){
                        if(!(constantDefinition instanceof  ConstantDefinition)){
                            err = "Adding constant def failed. Not an instance of ConstantDefinition" + constantDefinition;
                            log.error(err);
                            throw err;
                        }
                    });
                }
            }
            this._constantDefinitions = this._constantDefinitions || [];
            this._constantDefinitions = _.concat(this._constantDefinitions , constantDefinitions);
            /**
             * fired when new constant defs are added to the package.
             * @event Package#constant-defs-added
             * @type {[ConstantDefinition]}
             */
            this.trigger("constant-defs-added", constantDefinitions);
        };

        /**
         * Set constant defs
         *
         * @param constantDefs
         */
        Package.prototype.setConstantDefinitions = function(constantDefs){
            this._constantDefinitions = null;
            this.addConstantDefinitions(constantDefs);
        };

        /**
         *
         * @returns {[ConstantDefinition]}
         */
        Package.prototype.getConstantDefinitions = function() {
            return this._constantDefinitions;
        };

        /**
         * Add type mapper defs
         * @param typeMapperDefinitions - can be an array of typeDefinitions or a single typeDefinition
         * @fires Package#type--mapper-defs-added
         */
        Package.prototype.addTypeMapperDefinitions = function(typeMapperDefinitions){
            var err;
            var self = this;
            if(!_.isArray(typeMapperDefinitions) && !(typeMapperDefinitions instanceof  TypeMapperDefinition)){
                err = "Adding type mapper def failed. Not an instance of TypeMapperDefinition" + typeMapperDefinitions;
                log.error(err);
                throw err;
            }
            if(_.isArray(typeMapperDefinitions)){
                if(!_.isEmpty(typeMapperDefinitions)){
                    _.each(typeMapperDefinitions, function(typeMapperDefinition){
                        if(!(typeMapperDefinition instanceof  TypeMapperDefinition)){
                            err = "Adding type mapper def failed. Not an instance of TypeMapperDefinition" + typeMapperDefinition;
                            log.error(err);
                            throw err;
                        }
                    });
                }
            }
            this._typeMapperDefinitions = this._typeMapperDefinitions || [];
            this._typeMapperDefinitions = _.concat(this._typeMapperDefinitions , typeMapperDefinitions);
            /**
             * fired when new type mapper defs are added to the package.
             * @event Package#type-mapper-defs-added
             * @type {[TypeMapperDefinition]}
             */
            this.trigger("type-mapper-defs-added", typeMapperDefinitions);
        };

        /**
         * Set type mapper defs
         *
         * @param typeMapperDefs
         */
        Package.prototype.setTypeMapperDefinitions = function(typeMapperDefs){
            this._typeMapperDefinitions = null;
            this.addTypeMapperDefinitions(typeMapperDefs);
        };

        /**
         *
         * @returns {[TypeMapperDefinition]}
         */
        Package.prototype.getTypeMapperDefinitions = function() {
            return this._typeMapperDefinitions;
        };

        /**
         * Add type defs
         * @param typeDefinitions - can be an array of typeDefinitions or a single typeDefinition
         * @fires Package#type-defs-added
         */
        Package.prototype.addTypeDefinitions = function(typeDefinitions){
            var err;
            if(!_.isArray(typeDefinitions) && !(typeDefinitions instanceof  TypeDefinition)){
                err = "Adding type def failed. Not an instance of TypeDefinition" + typeDefinitions;
                log.error(err);
                throw err;
            }
            if(_.isArray(typeDefinitions)){
                if(!_.isEmpty(typeDefinitions)){
                    _.each(typeDefinitions, function(typeDefinition){
                        if(!(typeDefinition instanceof  TypeDefinition)){
                            err = "Adding type def failed. Not an instance of TypeDefinition" + typeDefinition;
                            log.error(err);
                            throw err;
                        }
                    });
                }
            }
            this._typeDefinitions = this._typeDefinitions || [];
            this._typeDefinitions = _.concat(this._typeDefinitions , typeDefinitions);
            /**
             * fired when new type defs are added to the package.
             * @event Package#type-defs-added
             * @type {[TypeDefinition]}
             */
            this.trigger("type-defs-added", typeDefinitions);
        };

        /**
         * Set type defs
         *
         * @param typeDefs
         */
        Package.prototype.setTypeDefinitions = function(typeDefs){
            this._typeDefinitions = null;
            this.addTypeDefinitions(typeDefs);
        };

        /**
         *
         * @returns {[TypeDefinition]}
         */
        Package.prototype.getTypeDefinitions = function() {
            return this._typeDefinitions;
        };

        /**
         * Add connectors
         * @param connectors - can be an array of connectors or a single connector
         * @fires Package#connector-defs-added
         */
        Package.prototype.addConnectors = function(connectors){
            var self = this;
            var err;
            if(!_.isArray(connectors) && !(self.BallerinaEnvFactory.isConnector(connectors))){
                err = "Adding connector failed. Not an instance of connector " + connectors;
                log.error(err);
                throw err;
            }
            if(_.isArray(connectors)){
                if(!_.isEmpty(connectors)){
                    _.each(connectors, function(connector){
                        if(!self.BallerinaEnvFactory.isConnector(connector)){
                            err = "Adding connector failed. Not an instance of connector" + connector;
                            log.error(err);
                            throw err;
                        }
                    });
                }
            }
            this._connectorDefinitions = _.concat(this._connectorDefinitions , connectors);
            /**
             * fired when new connectors are added to the package.
             * @event Package#connector-defs-added
             * @type {[Connector]}
             */
            this.trigger("connector-defs-added", connectors);
        };

        /**
         * Get all connectors
         * @returns {[Connector]}
         */
        Package.prototype.getConnectors = function() {
            return this._connectorDefinitions;
        };

        /**
         * returns function definition
         * @param {string} functionName - name of the function to be retrieved
         */
        Package.prototype.getConnectorByName = function (connectorName) {
            return _.find(this.getConnectors(), function (connector) {
                return _.isEqual(connector.getName(),connectorName);
            });
        };

        /**
         * Add service defs
         * @param serviceDefinitions - can be an array of serviceDefs or a single serviceDef
         */
        Package.prototype.addServiceDefinitions = function(serviceDefinitions){
            var err;
            if(!_.isArray(serviceDefinitions) && !(serviceDefinitions instanceof  ServiceDefinition)){
                err = "Adding service def failed. Not an instance of ServiceDefinition" + serviceDefinitions;
                log.error(err);
                throw err;
            }
            if(_.isArray(serviceDefinitions)){
                if(!_.isEmpty(serviceDefinitions)){
                    _.each(serviceDefinitions, function(serviceDefinition){
                        if(!(serviceDefinition instanceof  ServiceDefinition)){
                            err = "Adding service def failed. Not an instance of ServiceDefinition" + serviceDefinition;
                            log.error(err);
                            throw err;
                        }
                    });
                }
            }
            this._serviceDefinitions = this._serviceDefinitions || [];
            this._serviceDefinitions = _.concat(this._serviceDefinitions , serviceDefinitions);
            /**
             * fired when new service defs are added to the package.
             * @event Package#service-defs-added
             * @type {[ServiceDefinition]}
             */
            this.trigger("service-defs-added", serviceDefinitions);
        };

        /**
         * Set service defs
         *
         * @param serviceDefs
         */
        Package.prototype.setServiceDefinitions = function(serviceDefs){
            this._serviceDefinitions = null;
            this.addServiceDefinitions(serviceDefs);
        };

        /**
         *
         * @returns {[ServiceDefinition]}
         */
        Package.prototype.getServiceDefinitions = function() {
            return this._serviceDefinitions;
        };


        /**
         * Add function defs
         * @param functionDefinitions - can be an array of functionDefinitions or a single functionDefinition
         */
        Package.prototype.addFunctionDefinitions = function(functionDefinitions){
            var self = this;
            var err;
            if(!_.isArray(functionDefinitions) && !(self.BallerinaEnvFactory.isFunction(functionDefinitions))){
                err = "Adding function def failed. Not an instance of FunctionDefinition" + functionDefinitions;
                log.error(err);
                throw err;
            }
            if(_.isArray(functionDefinitions)){
                if(!_.isEmpty(functionDefinitions)){
                    _.each(functionDefinitions, function(functionDefinition){
                        if(!(functionDefinition instanceof  FunctionDefinition)){
                            err = "Adding function def failed. Not an instance of FunctionDefinition" + functionDefinition;
                            log.error(err);
                            throw err;
                        }
                    });
                }
            }
            this._functionDefinitions = this._functionDefinitions || [];
            this._functionDefinitions = _.concat(this._functionDefinitions , functionDefinitions);
            /**
             * fired when new function defs are added to the package.
             * @event Package#function-defs-added
             * @type {[FunctionDefinition]}
             */
            this.trigger("function-defs-added", functionDefinitions);
        };

        /**
         * remove function definition
         * @param functionDefinition - function definition to be removed
         */
        Package.prototype.removeFunctionDefinition = function (functionDefinition) {
            _.remove(this._functionDefinitions, function (functionDefinitionItem) {
                //TODO Need to check param types along with function name to support overloaded functions
                return _.isEqual(functionDefinitionItem.getName(), functionDefinition.getFunctionName());
            });
            this.trigger("function-def-removed", functionDefinition);
        };

        /**
         * remove connector definition
         * @param connectorDefinition - connector definition to be removed
         */
        Package.prototype.removeConnectorDefinition  = function (connectorDefinition) {
            var connector  = _.filter(this._connectorDefinitions, function (connectorDefinitionItem) {
                //TODO Need to check param types along with function name to support overloaded functions
                return _.isEqual(connectorDefinitionItem.getName(), connectorDefinition.getConnectorName());
            });
            // removing child connector actions
            connector[0].removeAllActions(connectorDefinition);
            _.remove(this._connectorDefinitions, connector[0]);
            this.trigger("connector-def-removed", connectorDefinition);
        };

        /**
         * Set function defs
         *
         * @param functionDefs
         */
        Package.prototype.setFunctionDefinitions = function(functionDefs){
            this._functionDefinitions = null;
            this.addFunctionDefinitions(functionDefs);
        };

        /**
         *
         * @returns {[FunctionDefinition]}
         */
        Package.prototype.getFunctionDefinitions = function() {
            return this._functionDefinitions;
        };

        /**
         * returns function definition
         * @param {string} functionName - name of the function to be retrieved
         */
        Package.prototype.getFunctionDefinitionByName = function (functionName) {
            return _.find(this._functionDefinitions, function (functionDefinition) {
                return _.isEqual(functionDefinition.getName(),functionName);
            });
        };

        /**
         * Add struct definition(s) to the package.
         * @param {StructDefinition[]|StructDefinition} structDefinitions - The struct definition(s).
         */
        Package.prototype.addStructDefinitions = function(structDefinitions){
            var err;
            if(!_.isArray(structDefinitions) && !(structDefinitions instanceof StructDefinition)){
                err = "Adding struct def failed. Not an instance of StructDefinition: " + structDefinitions;
                log.error(err);
                throw err;
            }
            if(_.isArray(structDefinitions)){
                if(!_.isEmpty(structDefinitions)){
                    _.each(structDefinitions, function(structDefinition){
                        if(!(structDefinition instanceof  StructDefinition)){
                            err = "Adding struct def failed. Not an instance of StructDefinition: " + structDefinition;
                            log.error(err);
                            throw err;
                        }
                    });
                }
            }
            this._structDefinitions = this._structDefinitions || [];
            // Join all struct definitions to one array(concat). Reversing the struct definitions so that the last
            // modified elements comes first(reverse). Then remove the duplicates using struct name(uniqBy).
            // Then reverse it back(reverse).
            this._structDefinitions = _.reverse(
                _.uniqBy(
                    _.reverse(
                        _.concat(this._structDefinitions, structDefinitions)), function (structDefinition) {
                        return structDefinition.getStructName();
                    })
            );
            /**
             * Fired when new struct defs are added to the package.
             * @event Package#struct-defs-added
             * @type {FunctionDefinition}
             */
            this.trigger("struct-defs-added", structDefinitions);
        };

        /**
         * Set struct definitions.
         *
         * @param {StructDefinition[]} structDefinitions
         */
        Package.prototype.setStructDefinitions = function(structDefinitions){
            this._structDefinitions = null;
            this.addStructDefinitions(structDefinitions);
        };

        /**
         * Gets the struct definitions in the package.
         * @return {StructDefinition[]} - The struct definitions.
         */
        Package.prototype.getStructDefinitions = function() {
            return this._structDefinitions;
        };

        Package.prototype.initFromJson = function(jsonNode) {
            var self = this;
            this.setName(jsonNode.name);

            _.each(jsonNode.connectors, function (connectorNode) {
                var connector = self.BallerinaEnvFactory.createConnector();
                connector.initFromJson(connectorNode);
                self.addConnectors(connector);
            });

            _.each(jsonNode.functions, function(functionNode){
                var functionDef = self.BallerinaEnvFactory.createFunction();
                functionDef.initFromJson(functionNode);
                self.addFunctionDefinitions(functionDef);
            });

        };

        return Package;
    });