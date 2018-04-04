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
import ServiceDefinition from 'plugins/ballerina/model/tree/service-node';
import BallerinaEnvFactory from './ballerina-env-factory';

/**
 * @class Package
 * @param args {Object} - args.name: name of the package
 * @constructor
 */
class Package {
    constructor(args) {
        this.setName(_.get(args, 'name', ''));
        this.addServiceDefinitions(_.get(args, 'serviceDefinitions', []));
        this.addFunctionDefinitions(_.get(args, 'functionDefinitions', []));
        this.addStructDefinitions(_.get(args, 'structDefinitions', []));
        this.addEnums(_.get(args, 'enums', []));
        this._connectorDefinitions = _.get(args, 'connectors', []);
        this.addTypeDefinitions(_.get(args, 'typeDefinitions', []));
        this.addConstantDefinitions(_.get(args, 'constantDefinitions', []));
        this.addAnnotationDefinitions(_.get(args, 'annotationDefinitions', []));
        this.addEndpoints(_.get(args, 'endpoints',[]));
        this.addObjects(_.get(args,'objects',[]));
    }

    /**
     * Add objects to the package.
     *
     * @param objects objects to be added to the package
     */
    addObjects(objects) {
        this._objects = this._objects || [];
        this._objects = _.concat(this._objects, objects);
    }

    /**
     * Get objects available in the package.
     *
     * @returns {objects} objects available in the package
     */
    getObjects() {
        return this._objects;
    }

    /**
     * Set objects to the package.
     *
     * @param objects objects to be added to the package
     */
    setObjects(objects) {
        this._objects = null;
        this.addObjects(objects);
    }

    /**
     * Add the endpoints to the package.
     *
     * @param endpoints - endpoints to be added.
     */
    addEndpoints(endpoints) {
        this._endpoints = this._endpoints || [];
        this._endpoints = _.concat(this._endpoints, endpoints);
    }

    /**
     * Get the endpoints available for the package.
     *
     * @returns {endpoints} endpoints of the package
     */
    getEndpoints() {
        return this._endpoints;
    }

    /**
     * Set endpoint to the package.
     *
     * @param endpoints endpoints to be added to the package
     */
    setEndpoints(endpoints) {
        this._endpoints = null;
        this.addEndpoints(endpoints);
    }

    setName(name) {
        if (!_.isNil(name) && _.isString(name)) {
            this._name = name;
        } else {
            log.error('Invalid value for package name: ', name);
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
        this._constantDefinitions = this._constantDefinitions || [];
        this._constantDefinitions = _.concat(this._constantDefinitions, constantDefinitions);
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
     * Add type defs
     * @param typeDefinitions - can be an array of typeDefinitions or a single typeDefinition
     * @fires Package#type-defs-added
     */
    addTypeDefinitions(typeDefinitions) {
        this._typeDefinitions = this._typeDefinitions || [];
        this._typeDefinitions = _.concat(this._typeDefinitions, typeDefinitions);
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
     * Set connectors
     *
     * @param connectorDefs
     */
    setConnectors(connectorDefs) {
        this._connectorDefinitions = null;
        this.addConnectors(connectorDefs);
    }

    /**
     * Add connectors
     * @param connectors - can be an array of connectors or a single connector
     * @fires Package#connector-defs-added
     */
    addConnectors(connectors) {
        let err;
        if (!_.isArray(connectors) && !(BallerinaEnvFactory.isConnector(connectors))) {
            err = 'Adding connector failed. Not an instance of connector ' + connectors;
            log.error(err);
            throw err;
        }
        this._connectorDefinitions = this._connectorDefinitions || [];
        this._connectorDefinitions = _.concat(this._connectorDefinitions, connectors);
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
        return _.find(this.getConnectors(), (connector) => {
            return _.isEqual(connector.getName(), connectorName);
        });
    }

    /**
     * returns connector definition
     * @param {string} annotationDefinitionName - name of the connector to be retrieved
     */
    getAnnotationDefinitionByName(annotationDefinitionName) {
        return _.find(this.getAnnotationDefinitions(), (annotationDefinition) => {
            return _.isEqual(annotationDefinition.getName(), annotationDefinitionName);
        });
    }

    /**
     * Add annotation defs
     * @param annotationDefinitions - can be an array of annotationDefs or a single annotationDef
     */
    addAnnotationDefinitions(annotationDefinitions) {
        let err;
        if (!_.isArray(annotationDefinitions) && !(BallerinaEnvFactory.isAnnotationDefinition(annotationDefinitions))) {
            err = 'Adding annotation def failed. Not an instance of AnnotationDefinition' + annotationDefinitions;
            log.error(err);
            throw err;
        }
        this._annotationDefinitions = this._annotationDefinitions || [];
        this._annotationDefinitions = _.concat(this._annotationDefinitions, annotationDefinitions);
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
        let err;
        if (!_.isArray(serviceDefinitions) && !(serviceDefinitions instanceof ServiceDefinition)) {
            err = 'Adding service def failed. Not an instance of ServiceDefinition' + serviceDefinitions;
            log.error(err);
            throw err;
        }
        if (_.isArray(serviceDefinitions)) {
            if (!_.isEmpty(serviceDefinitions)) {
                _.each(serviceDefinitions, (serviceDefinition) => {
                    if (!(serviceDefinition instanceof ServiceDefinition)) {
                        err = 'Adding service def failed. Not an instance of ServiceDefinition' + serviceDefinition;
                        log.error(err);
                        throw err;
                    }
                });
            }
        }
        this._serviceDefinitions = this._serviceDefinitions || [];
        this._serviceDefinitions = _.concat(this._serviceDefinitions, serviceDefinitions);
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
        let err;
        if (!_.isArray(functionDefinitions) && !(BallerinaEnvFactory.isFunction(functionDefinitions))) {
            err = 'Adding function def failed. Not an instance of FunctionDefinition' + functionDefinitions;
            log.error(err);
            throw err;
        }
        functionDefinitions.kind = 'Function';
        this._functionDefinitions = this._functionDefinitions || [];
        this._functionDefinitions = _.concat(this._functionDefinitions, functionDefinitions);
    }

    /**
     * remove function definition
     * @param functionDefinition - function definition to be removed
     */
    removeFunctionDefinition(functionDefinition) {
        _.remove(this._functionDefinitions, (functionDefinitionItem) => {
            // TODO Need to check param types along with function name to support overloaded functions
            return _.isEqual(functionDefinitionItem.getName(), functionDefinition.getFunctionName());
        });
    }

    /**
     * remove connector definition
     * @param connectorDefinition - connector definition to be removed
     */
    removeConnectorDefinition(connectorDefinition) {
        const connector = _.filter(this._connectorDefinitions, (connectorDefinitionItem) => {
            // TODO Need to check param types along with function name to support overloaded functions
            return _.isEqual(connectorDefinitionItem.getName(), connectorDefinition.getConnectorName());
        });
        // removing child connector actions
        connector[0].removeAllActions(connectorDefinition);
        _.remove(this._connectorDefinitions, connector[0]);
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
        return _.find(this._functionDefinitions, (functionDefinition) => {
            return _.isEqual(functionDefinition.getName(), functionName);
        });
    }

    /**
     * Add struct definition(s) to the package.
     * @param {StructDefinition[]|StructDefinition} structDefinitions - The struct definition(s).
     */
    addStructDefinitions(structDefinitions) {
        this._structDefinitions = this._structDefinitions || [];
        let err;
        if (!_.isArray(structDefinitions) && !(BallerinaEnvFactory.isStruct(structDefinitions))) {
            err = 'Adding struct def failed. Not an instance of StructDefinition' + structDefinitions;
            log.error(err);
            throw err;
        }
        this._structDefinitions = this._structDefinitions || [];
        this._structDefinitions = _.concat(this._structDefinitions, structDefinitions);
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

  /**
   * Add enums(s) to the package.
   * @param {Enum[]|Enum} enums- The enum(s).
   */
    addEnums(enums) {
        this._enums = this._enums || [];
        let err;
        if (!_.isArray(enums) && !(BallerinaEnvFactory.isEnum(enums))) {
            err = 'Adding enum failed. Not an instance of Enum' + enums;
            log.error(err);
            throw err;
        }
        this._enums = this._enums || [];
        this._enums = _.concat(this._enums, enums);
    }

  /**
   * Set enums
   *
   * @param {Enums[]} enums
   */
    setEnums(enums) {
        this._enums = null;
        this.addEnums(enums);
    }

  /**
   * Gets the enums in the package.
   * @return {Enums[]} - The enums
   */
    getEnums() {
        return this._enums;
    }

    initFromJson(jsonNode) {
        this.setName(jsonNode.name);

        _.each(jsonNode.connectors, (connectorNode) => {
            const connector = BallerinaEnvFactory.createConnector();
            connector.initFromJson(connectorNode);
            this.addConnectors(connector);
        });

        _.each(jsonNode.functions, (functionNode) => {
            const functionDef = BallerinaEnvFactory.createFunction();
            functionDef.initFromJson(functionNode);
            functionDef.setFullPackageName(this.getName());
            this.addFunctionDefinitions(functionDef);
        });

        _.each(jsonNode.structs, (structNode) => {
            const structDef = BallerinaEnvFactory.createStruct();
            structDef.initFromJson(structNode);
            structDef.setFullPackageName(this.getName());
            this.addStructDefinitions(structDef);
        });

        _.each(jsonNode.enums, (enumNode) => {
            const enumDef = BallerinaEnvFactory.createEnum();
            enumDef.initFromJson(enumNode);
            enumDef.setFullPackageName(this.getName());
            this.addEnums(enumDef);
        });
        _.each(jsonNode.annotations, (annotationNode) => {
            const annotationDef = BallerinaEnvFactory.createAnnotationDefinition();
            annotationDef.initFromJson(annotationNode);
            this.addAnnotationDefinitions(annotationDef);
        });

        _.each(jsonNode.endpoints, (endpointNode) => {
            const endpointDef = BallerinaEnvFactory.createEndpoint();
            endpointDef.initFromJson(endpointNode);
            this.addEndpoints(endpointDef);
        });

        _.each(jsonNode.objects, (objectNode) => {
            const objectDef = BallerinaEnvFactory.createObject();
            objectDef.initFromJson(objectNode);
            this.addObjects(objectDef);
        });
    }

    /**
     * Get the list of types in the current package
     * @return {Array}
     */
    getTypesInPackage() {
        const types = [];
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
