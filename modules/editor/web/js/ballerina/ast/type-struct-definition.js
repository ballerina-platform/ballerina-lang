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
         * Constructor for TypeStructDefinition
         * @param {Object} args - The arguments to create the TypeStructDefinition
         * @param {string} [args.typeStructName=newTypeStruct] - TypeStructDefinition name
         * @param {string[]} [args.annotations] - TypeStructDefinition annotations
         * @param {string} [args.schema] - Which type of schema object{struct,xml,json}
         * @param {string} [args.category] - Whether typeStructDefinition is of Source or Target
         * @param {string} [args.identifier] - identifier name
         * @param {string} [args.onConnectInstance] - callBackInstance of on connect of typeStructs
         * @param {string} [args.onDisconnectInstance] - callBackInstance of on disconnect of typeStructs
         * @constructor
         */
        var TypeStructDefinition = function (args) {
            this._typeStructName = _.get(args, 'typeStructName', 'newTypeStruct');
            this._annotations = _.get(args, 'annotations', []);
            this._schema = _.get(args, 'schema', {});
            this._category = _.get(args, 'category', 'default');
            this._identifier = _.get(args, 'identifier', 'default');
            this._onConnectInstance = _.get(args, 'onConnectInstance', {});
            this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
            this._selectedStructName = _.get(args, 'selectedStructName', 'default');
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
         * @returns {string}
         */
        TypeStructDefinition.prototype.getTypeStructName = function () {
           return this._typeStructName;
        };

        /**
         * set the AST node of the selected type struct
         * @param schema
         */
        TypeStructDefinition.prototype.setSchema = function (schema, options) {
            if (!_.isNil(schema)) {
                this.setAttribute('_schema', schema, options);
            } else {
                log.error('Invalid schema [' + schema + '] Provided');
                throw 'Invalid schema [' + schema + '] Provided';
            }

        };

        /**
         * returns whether the type struct is a source or a target
         * @returns {string}
         */
        TypeStructDefinition.prototype.getCategory = function () {
            return this._category;
        };

        /**
         * set whether chosen type struct is either source or target
         * @param category
         */
        TypeStructDefinition.prototype.setCategory = function (category, options) {
            if (!_.isNil(category)) {
                this.setAttribute('_category', category, options);
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
        TypeStructDefinition.prototype.setIdentifier = function (identifier, options) {
            if (!_.isNil(identifier)) {
                this.setAttribute('_identifier', identifier, options);
            } else {
                log.error('Invalid identifier [' + identifier + '] Provided');
                throw 'Invalid identifier [' + identifier + '] Provided';
            }
        };

        /**
         * returns the identifier of the type struct
         * @returns {string}
         */
        TypeStructDefinition.prototype.getIdentifier = function(){
            return this._identifier;
        };

        /**
         * returns the call back function to be called when a connection is drawn
         * @returns {object}
         */
        TypeStructDefinition.prototype.getOnConnectInstance = function () {
            return this._onConnectInstance;
        };

        /**
         * set the call back function for connecting a source and a target
         * @param onConnectInstance
         */
        TypeStructDefinition.prototype.setOnConnectInstance = function (onConnectInstance, options) {
            if (!_.isNil(onConnectInstance)) {
                this.setAttribute('_onConnectInstance', onConnectInstance, options);
            } else {
                log.error('Invalid onConnectInstance [' + onConnectInstance + '] Provided');
                throw 'Invalid onConnectInstance [' + onConnectInstance + '] Provided';
            }
        };

        /**
         * returns the call back function to be called when a connection is removed
         * @returns {object}
         */
        TypeStructDefinition.prototype.getOnDisconnectInstance = function () {
            return this._onDisconnectInstance;
        };

        /**
         * set the call back function for disconnecting a source and a target
         * @param onDisconnectInstance
         */
        TypeStructDefinition.prototype.setOnDisconnectInstance = function (onDisconnectInstance, options) {
            if (!_.isNil(onDisconnectInstance)) {
                this.setAttribute('_onDisconnectInstance', onDisconnectInstance, options);
            } else {
                log.error('Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided');
                throw 'Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided';
            }
        };

        /**
         * Preserves the previous selection of the type struct name in-order to remove upon a new selection
         * @param selectedStructName
         */
        TypeStructDefinition.prototype.setSelectedStructName = function (selectedStructName, options) {
            if (!_.isNil(selectedStructName)) {
                this.setAttribute('_selectedStructName', selectedStructName, options);
            } else {
                log.error('Invalid selectedStructName [' + selectedStructName + '] Provided');
                throw 'Invalid selectedStructName [' + selectedStructName + '] Provided';
            }
        };

        /**
         * returns the previously selected type struct name
         * @returns {string}
         */
        TypeStructDefinition.prototype.getSelectedStructName = function () {
            return this._selectedStructName;
        };

        return TypeStructDefinition;
    });
