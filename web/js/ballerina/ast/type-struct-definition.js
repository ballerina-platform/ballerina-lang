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
define(['lodash', 'require', 'log', './node'],
    function (_, require, log, ASTNode) {

        /**
         * Constructor for ResourceDefinition
         * @param {Object} args - The arguments to create the ServiceDefinition
         * @param {string} [args.resourceName=newResource] - Service name
         * @param {string[]} [args.annotations] - Resource annotations
         * @param {string} [args.annotations.Method] - Resource annotation for Method
         * @param {string} [args.annotations.Path] - Resource annotation for Path
         * @constructor
         */
        var TypeStructDefinition = function (args) {
            this._typeStructName = _.get(args, 'typeStructName', 'newStruct');
            this._annotations = _.get(args, 'annotations', []);
            this._schema;
            this._category;
            this._identifier;
            this._dataMapperInstance;
            this._onConnectInstance;
            this._onDisconnectInstance;
            this._selectedStructName;
        };

        TypeStructDefinition.prototype = Object.create(ASTNode.prototype);
        TypeStructDefinition.prototype.constructor = TypeStructDefinition;

        /**
         * Set the name of the type
         * @param typeStructName
         */
        TypeStructDefinition.prototype.setTypeStructName = function (typeStructName) {
            if (!_.isNil(typeStructName)) {
                this.setAttribute('_typeStructName', typeStructName);
            } else {
                log.error('Invalid Type Struct Name [' + typeStructName + '] Provided');
                throw 'Invalid Type Struct Name [' + typeStructName + '] Provided';
            }
        };

        /**
         * returns the name of the type
         * @returns {*}
         */
        TypeStructDefinition.prototype.getTypeStructName = function () {
           return this._typeStructName;
        };

        /**
         * set the AST node of the selected type struct
         * @param schema
         */
        TypeStructDefinition.prototype.setSchema = function (schema) {
            if (!_.isNil(schema)) {
                this.setAttribute('_schema', schema);
            } else {
                log.error('Invalid schema [' + schema + '] Provided');
                throw 'Invalid schema [' + schema + '] Provided';
            }

        };

        /**
         * returns whether the type struct is a source or a target
         * @returns {*}
         */
        TypeStructDefinition.prototype.getCategory = function () {
            return this._category;
        };

        /**
         * set whether chosen type struct is either source or target
         * @param category
         */
        TypeStructDefinition.prototype.setCategory = function (category) {
            if (!_.isNil(category)) {
                this.setAttribute('_category', category);
            } else {
                log.error('Invalid category [' + category + '] Provided');
                throw 'Invalid category [' + category + '] Provided';
            }
        };

        /**
         * returns the attributes array of the chosen type struct
         * @returns {Object}
         */
        TypeStructDefinition.prototype.getSchemaPropertyObj = function () {
            return this._schema.getAttributesArray();
        };

        /**
         * set the identifier of the chosen type struct (ex: People p;)
         * @param identifier
         */
        TypeStructDefinition.prototype.setIdentifier = function (identifier) {
            if (!_.isNil(identifier)) {
                this.setAttribute('_identifier', identifier);
            } else {
                log.error('Invalid identifier [' + identifier + '] Provided');
                throw 'Invalid identifier [' + identifier + '] Provided';
            }
        };

        /**
         * returns the identifier of the type struct
         * @returns {*}
         */
        TypeStructDefinition.prototype.getIdentifier = function(){
            return this._identifier;
        };

        /**
         * returns the call back function to be called when a connection is drawn
         * @returns {*}
         */
        TypeStructDefinition.prototype.getOnConnectInstance = function () {
            return this._onConnectInstance;
        };

        /**
         * set the call back function for connecting a source and a target
         * @param onConnectInstance
         */
        TypeStructDefinition.prototype.setOnConnectInstance = function (onConnectInstance) {
            if (!_.isNil(onConnectInstance)) {
                this.setAttribute('_onConnectInstance', onConnectInstance);
            } else {
                log.error('Invalid onConnectInstance [' + onConnectInstance + '] Provided');
                throw 'Invalid onConnectInstance [' + onConnectInstance + '] Provided';
            }
        };

        /**
         * returns the call back function to be called when a connection is removed
         * @returns {*}
         */
        TypeStructDefinition.prototype.getOnDisconnectInstance = function () {
            return this._onDisconnectInstance;
        };

        /**
         * set the call back function for disconnecting a source and a target
         * @param onDisconnectInstance
         */
        TypeStructDefinition.prototype.setOnDisconnectInstance = function (onDisconnectInstance) {
            if (!_.isNil(onDisconnectInstance)) {
                this.setAttribute('_onDisconnectInstance', onDisconnectInstance);
            } else {
                log.error('Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided');
                throw 'Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided';
            }
        };

        /**
         * Preserves the previous selection of the type struct name in-order to remove upon a new selection
         * @param selectedStructName
         */
        TypeStructDefinition.prototype.setSelectedStructName = function (selectedStructName) {
            if (!_.isNil(selectedStructName)) {
                this.setAttribute('_selectedStructName', selectedStructName);
            } else {
                log.error('Invalid selectedStructName [' + selectedStructName + '] Provided');
                throw 'Invalid selectedStructName [' + selectedStructName + '] Provided';
            }
        };

        /**
         * returns the previously selected type struct name
         * @returns {*}
         */
        TypeStructDefinition.prototype.getSelectedStructName = function () {
            return this._selectedStructName;
        };

        return TypeStructDefinition;
    });
