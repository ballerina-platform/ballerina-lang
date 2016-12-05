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
define(['log', 'lodash', 'event_channel', './../ast/service-definition', './../ast/function-definition', './../ast/connector-definition',
        './../ast/type-definition', './../ast/type-converter-definition', './../ast/constant-definition'],

    function(log, _, EventChannel, ServiceDefinition, FunctionDefinition, ConnectorDefinition, TypeDefinition,
                TypeConverterDefinition, ConstantDefinition){

    /**
     * @class Package
     * @augments EventChannel
     * @param args {Object} - args.name: name of the package
     * @constructor
     */
    var Package = function(args){
        this.setName(_.get(args, 'name', null));
        this.addServiceDefinitions(_.get(args, 'serviceDefinitions', []));
        this.addFunctionDefinitions(_.get(args, 'functionDefinitions', []));
        this.addConnectorDefinitions(_.get(args, 'connectorDefinitions', []));
        this.addTypeDefinitions(_.get(args, 'typeDefinitions', []));
        this.addTypeConverterDefinitions(_.get(args, 'typeConverterDefinitions', []));
        this.addConstantDefinitions(get(args, 'constantDefinitions', []));
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
        _.concat(this._constantDefinitions , constantDefinitions);
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
     * Add type converter defs
     * @param typeConverterDefinitions - can be an array of typeDefinitions or a single typeDefinition
     * @fires Package#type--converter-defs-added
     */
    Package.prototype.addTypeConverterDefinitions = function(typeConverterDefinitions){
        var err;
        if(!_.isArray(typeConverterDefinitions) && !(typeConverterDefinitions instanceof  TypeConverterDefinition)){
            err = "Adding type converter def failed. Not an instance of TypeConverterDefinition" + typeConverterDefinitions;
            log.error(err);
            throw err;
        }
        if(_.isArray(typeConverterDefinitions)){
            if(!_.isEmpty(typeConverterDefinitions)){
                _.each(typeConverterDefinitions, function(typeConverterDefinition){
                    if(!(typeConverterDefinition instanceof  TypeConverterDefinition)){
                        err = "Adding type converter def failed. Not an instance of TypeConverterDefinition" + typeConverterDefinition;
                        log.error(err);
                        throw err;
                    }
                });
            }
        }
        this._typeConverterDefinitions = this._typeConverterDefinitions || [];
        _.concat(this._typeConverterDefinitions , typeConverterDefinitions);
        /**
         * fired when new type converter defs are added to the package.
         * @event Package#type-converter-defs-added
         * @type {[TypeConverterDefinition]}
         */
        this.trigger("type-converter-defs-added", typeConverterDefinitions);
    };

    /**
     * Set type converter defs
     *
     * @param typeConverterDefs
     */
    Package.prototype.setTypeConverterDefinitions = function(typeConverterDefs){
        this._typeConverterDefinitions = null;
        this.addTypeConverterDefinitions(typeConverterDefs);
    };

    /**
     *
     * @returns {[TypeConverterDefinition]}
     */
    Package.prototype.getTypeConverterDefinitions = function() {
        return this._typeConverterDefinitions;
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
        _.concat(this._typeDefinitions , typeDefinitions);
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
     * Add connector defs
     * @param connectorDefinitions - can be an array of connectorDefinitions or a single connectorDefinition
     * @fires Package#connector-defs-added
     */
    Package.prototype.addConnectorDefinitions = function(connectorDefinitions){
        var err;
        if(!_.isArray(connectorDefinitions) && !(connectorDefinitions instanceof  ConnectorDefinition)){
            err = "Adding connector def failed. Not an instance of ConnectorDefinition" + connectorDefinitions;
            log.error(err);
            throw err;
        }
        if(_.isArray(connectorDefinitions)){
            if(!_.isEmpty(connectorDefinitions)){
                _.each(connectorDefinitions, function(connectorDefinition){
                    if(!(connectorDefinition instanceof  ConnectorDefinition)){
                        err = "Adding connector def failed. Not an instance of ConnectorDefinition" + connectorDefinition;
                        log.error(err);
                        throw err;
                    }
                });
            }
        }
        this._connectorDefinitions = this._connectorDefinitions || [];
        _.concat(this._connectorDefinitions , connectorDefinitions);
        /**
         * fired when new connector defs are added to the package.
         * @event Package#connector-defs-added
         * @type {[ConnectorDefinition]}
         */
        this.trigger("connector-defs-added", connectorDefinitions);
    };

    /**
     * Set connector defs
     *
     * @param connectorDefs
     */
    Package.prototype.setConnectorDefinitions = function(connectorDefs){
        this._connectorDefinitions = null;
        this.addConnectorDefinitions(connectorDefs);
    };

    /**
     *
     * @returns {[ConnectorDefinition]}
     */
    Package.prototype.getConnectorDefinitions = function() {
        return this._connectorDefinitions;
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
        _.concat(this._serviceDefinitions , serviceDefinitions);
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
        var err;
        if(!_.isArray(functionDefinitions) && !(functionDefinitions instanceof  FunctionDefinition)){
            err = "Adding function def failed. Not an instance of FunctionDefinition" + functionDefinitions;
            log.error(err);
            throw err;
        }
        if(_.isArray(functionDefinitions)){
            if(!_.isEmpty(functionDefinitions)){
                _.each(functionDefinitions, function(functionDefinition){
                    if(!(functionDefinition instanceof  FunctionDefinition)){
                        err = "Adding funciton def failed. Not an instance of FunctionDefinition" + functionDefinition;
                        log.error(err);
                        throw err;
                    }
                });
            }
        }
        this._functionDefinitions = this._functionDefinitions || [];
        _.concat(this._functionDefinitions , functionDefinitions);
        /**
         * fired when new function defs are added to the package.
         * @event Package#function-defs-added
         * @type {[FunctionDefinition]}
         */
        this.trigger("function-defs-added", functionDefinitions);
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

    return Package;
});