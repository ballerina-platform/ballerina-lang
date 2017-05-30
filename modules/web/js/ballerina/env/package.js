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
import log from 'log';
import _ from 'lodash';
import EventChannel from 'event_channel';
import ServiceDefinition from './../ast/service-definition';
import FunctionDefinition from './../ast/function-definition';
import TypeDefinition from './../ast/type-definition';
import TypeMapperDefinition from './../ast/type-mapper-definition';
import ConstantDefinition from './../ast/constant-definition';
import StructDefinition from './../ast/struct-definition';
import AnnotationDefinition from './../ast/annotation-definition';
import BallerinaEnvFactory from './ballerina-env-factory';

/**
 * @class Package
 * @augments EventChannel
 * @param args {Object} - args.name: name of the package
 * @constructor
 */
class Package extends EventChannel {
    constructor(args) {
        super();
        this.setName(_.get(args, 'name', ''));
        this.addServiceDefinitions(_.get(args, 'serviceDefinitions', []));
        this.addFunctionDefinitions(_.get(args, 'functionDefinitions', []));
        this.addStructDefinitions(_.get(args, 'structDefinitions', []));
        this._connectorDefinitions = _.get(args, 'connectors', []);
        this.addTypeDefinitions(_.get(args, 'typeDefinitions', []));
        this.addTypeMapperDefinitions(_.get(args, 'typeMapperDefinitions', []));
        this.addConstantDefinitions(_.get(args, 'constantDefinitions', []));
        this.addAnnotationDefinitions(_.get(args, 'annotationDefinitions', []));
    }

    setName(name) {
        if(!_.isNil(name) && _.isString(name)){
            this._name = name;
        } else {
            log.error("Invalid value for package name: ", name);
        }
    }

    getName() {
        return this._name;
    }

    /**
     * Add constant defs
     * @param constantDefinitions - can be an array of constantDefinitions or a single constantDefinition
     * @fires Package#constant-defs-added
     */
    addConstantDefinitions(constantDefinitions) {
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
    }

    /**
     * Set constant defs
     *
     * @param constantDefs
     */
    setConstantDefinitions(constantDefs) {
        this._constantDefinitions = null;
        this.addConstantDefinitions(constantDefs);
    }

    /**
     *
     * @returns {[ConstantDefinition]}
     */
    getConstantDefinitions() {
        return this._constantDefinitions;
    }

    /**
     * Add type mapper defs
     * @param typeMapperDefinitions - can be an array of typeDefinitions or a single typeDefinition
     * @fires Package#type--mapper-defs-added
     */
    addTypeMapperDefinitions(typeMapperDefinitions) {
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
    }

    /**
     * Set type mapper defs
     *
     * @param typeMapperDefs
     */
    setTypeMapperDefinitions(typeMapperDefs) {
        this._typeMapperDefinitions = null;
        this.addTypeMapperDefinitions(typeMapperDefs);
    }

    /**
     *
     * @returns {[TypeMapperDefinition]}
     */
    getTypeMapperDefinitions() {
        return this._typeMapperDefinitions;
    }

    /**
     * Add type defs
     * @param typeDefinitions - can be an array of typeDefinitions or a single typeDefinition
     * @fires Package#type-defs-added
     */
    addTypeDefinitions(typeDefinitions) {
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
    }

    /**
     * Set type defs
     *
     * @param typeDefs
     */
    setTypeDefinitions(typeDefs) {
        this._typeDefinitions = null;
        this.addTypeDefinitions(typeDefs);
    }

    /**
     *
     * @returns {[TypeDefinition]}
     */
    getTypeDefinitions() {
        return this._typeDefinitions;
    }

    /**
     * Add connectors
     * @param connectors - can be an array of connectors or a single connector
     * @fires Package#connector-defs-added
     */
    addConnectors(connectors) {
        var self = this;
        var err;
        if(!_.isArray(connectors) && !(BallerinaEnvFactory.isConnector(connectors))){
            err = "Adding connector failed. Not an instance of connector " + connectors;
            log.error(err);
            throw err;
        }
        if(_.isArray(connectors)){
            if(!_.isEmpty(connectors)){
                _.each(connectors, function(connector){
                    if(!BallerinaEnvFactory.isConnector(connector)){
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
    }

    /**
     * Get all connectors
     * @returns {[Connector]}
     */
    getConnectors() {
        return this._connectorDefinitions;
    }

    /**
     * returns connector definition
     * @param {string} connectorName - name of the connector to be retrieved
     */
    getConnectorByName(connectorName) {
        return _.find(this.getConnectors(), function (connector) {
            return _.isEqual(connector.getName(),connectorName);
        });
    }

    /**
     * Add annotation defs
     * @param annotationDefinitions - can be an array of annotationDefs or a single annotationDef
     */
    addAnnotationDefinitions(annotationDefinitions){
        var err;
        if(!_.isArray(annotationDefinitions) && !(BallerinaEnvFactory.isAnnotation(annotationDefinitions))){
            err = "Adding annotation def failed. Not an instance of AnnotationDefinition" + annotationDefinitions;
            log.error(err);
            throw err;
        }
        if(_.isArray(annotationDefinitions)){
            if(!_.isEmpty(annotationDefinitions)){
                _.each(annotationDefinitions, function(annotationDefinition){
                    if(!(annotationDefinition instanceof  AnnotationDefinition)){
                        err = "Adding annotation def failed. Not an instance of AnnotationDefinitions" + annotationDefinition;
                        log.error(err);
                        throw err;
                    }
                });
            }
        }
        this._annotationDefinitions = this._annotationDefinitions || [];
        this._annotationDefinitions = _.concat(this._annotationDefinitions , annotationDefinitions);
        /**
         * fired when new annotation defs are added to the package.
         * @event Package#annotation-defs-added
         * @type {[AnnotationDefinition]}
         */
        this.trigger("annotation-defs-added", annotationDefinitions);
    }

    /**
     * Set annotation defs
     *
     * @param annotationDefs
     */
    setAnnotationDefinitions(annotationDefs) {
        this._annotationDefinitions = null;
        this.addAnnotationDefinitions(annotationDefs);
    }

    /**
     *
     * @returns {[AnnotationDefinition]}
     */
    getAnnotationDefinitions() {
        return this._annotationDefinitions;
    }

    /**
     * Add service defs
     * @param serviceDefinitions - can be an array of serviceDefs or a single serviceDef
     */
    addServiceDefinitions(serviceDefinitions) {
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
    }

    /**
     * Set service defs
     *
     * @param serviceDefs
     */
    setServiceDefinitions(serviceDefs) {
        this._serviceDefinitions = null;
        this.addServiceDefinitions(serviceDefs);
    }

    /**
     *
     * @returns {[ServiceDefinition]}
     */
    getServiceDefinitions() {
        return this._serviceDefinitions;
    }

    /**
     * Add function defs
     * @param functionDefinitions - can be an array of functionDefinitions or a single functionDefinition
     */
    addFunctionDefinitions(functionDefinitions) {
        var self = this;
        var err;
        if(!_.isArray(functionDefinitions) && !(BallerinaEnvFactory.isFunction(functionDefinitions))){
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
    }

    /**
     * remove function definition
     * @param functionDefinition - function definition to be removed
     */
    removeFunctionDefinition(functionDefinition) {
        _.remove(this._functionDefinitions, function (functionDefinitionItem) {
            //TODO Need to check param types along with function name to support overloaded functions
            return _.isEqual(functionDefinitionItem.getName(), functionDefinition.getFunctionName());
        });
        this.trigger("function-def-removed", functionDefinition);
    }

    /**
     * remove connector definition
     * @param connectorDefinition - connector definition to be removed
     */
    removeConnectorDefinition(connectorDefinition) {
        var connector  = _.filter(this._connectorDefinitions, function (connectorDefinitionItem) {
            //TODO Need to check param types along with function name to support overloaded functions
            return _.isEqual(connectorDefinitionItem.getName(), connectorDefinition.getConnectorName());
        });
        // removing child connector actions
        connector[0].removeAllActions(connectorDefinition);
        _.remove(this._connectorDefinitions, connector[0]);
        this.trigger("connector-def-removed", connectorDefinition);
    }

    /**
     * Set function defs
     *
     * @param functionDefs
     */
    setFunctionDefinitions(functionDefs) {
        this._functionDefinitions = null;
        this.addFunctionDefinitions(functionDefs);
    }

    /**
     *
     * @returns {[FunctionDefinition]}
     */
    getFunctionDefinitions() {
        return this._functionDefinitions;
    }

    /**
     * returns function definition
     * @param {string} functionName - name of the function to be retrieved
     */
    getFunctionDefinitionByName(functionName) {
        return _.find(this._functionDefinitions, function (functionDefinition) {
            return _.isEqual(functionDefinition.getName(),functionName);
        });
    }

    /**
     * Add struct definition(s) to the package.
     * @param {StructDefinition[]|StructDefinition} structDefinitions - The struct definition(s).
     */
    addStructDefinitions(structDefinitions) {
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
    }

    /**
     * Set struct definitions.
     *
     * @param {StructDefinition[]} structDefinitions
     */
    setStructDefinitions(structDefinitions) {
        this._structDefinitions = null;
        this.addStructDefinitions(structDefinitions);
    }

    /**
     * Gets the struct definitions in the package.
     * @return {StructDefinition[]} - The struct definitions.
     */
    getStructDefinitions() {
        return this._structDefinitions;
    }

    initFromJson(jsonNode) {
        var self = this;
        this.setName(jsonNode.name);

        _.each(jsonNode.connectors, function (connectorNode) {
            var connector = BallerinaEnvFactory.createConnector();
            connector.initFromJson(connectorNode);
            self.addConnectors(connector);
        });

        _.each(jsonNode.functions, function(functionNode){
            var functionDef = BallerinaEnvFactory.createFunction();
            functionDef.initFromJson(functionNode);
            self.addFunctionDefinitions(functionDef);
        });

        _.each(jsonNode.annotations, function(annotationNode){
            var annotationDef = BallerinaEnvFactory.createAnnotation();
            annotationDef.initFromJson(annotationNode);
            self.addAnnotationDefinitions(annotationDef);
        });

    }

    /**
     * Get the list of types in the current package
     * @return {Array}
     */
    getTypesInPackage() {
        var types = [];
        _.forEach(this._connectorDefinitions, (connectorDef) => {
            types.push(connectorDef.getName());
        });
        _.forEach(this.getStructDefinitions(), (structDef) => {
            types.push(structDef.getStructName());
        });

        return types;
    }
}

export default Package;
