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
            this.BallerinaASTFactory = this.getFactory();
        };

        TypeStructDefinition.prototype = Object.create(ASTNode.prototype);
        TypeStructDefinition.prototype.constructor = TypeStructDefinition;

        TypeStructDefinition.prototype.setTypeStructName = function (typeStructName) {
            if (!_.isNil(typeStructName)) {
                this._typeStructName = typeStructName;
            }
        };

        TypeStructDefinition.prototype.getTypeStructName = function () {
           return this._typeStructName;
        };

        TypeStructDefinition.prototype.setSchema = function (schema) {
            if (!_.isNil(schema)) {
                this._schema = schema;
            }
        };

        TypeStructDefinition.prototype.setCategory = function (category) {
            if (!_.isNil(category)) {
                this._category = category;
            }
        };

        TypeStructDefinition.prototype.getSchemaPropertyObj = function () {
            return this._schema.getAttributesArray();
        };

        TypeStructDefinition.prototype.getCategory = function () {
            return this._category;
        };

        TypeStructDefinition.prototype.setIdentifier = function (identifier) {
            if(!_.isUndefined(identifier)){
                this._identifier = identifier;
            }
        };

        TypeStructDefinition.prototype.getIdentifier = function(){
            return this._identifier;
        };

        TypeStructDefinition.prototype.getDataMapperInstance = function () {
            return this._dataMapperInstance;
        };

        TypeStructDefinition.prototype.setDataMapperInstance = function (dataMapperInstance) {
            if(!_.isUndefined(dataMapperInstance)){
                this._dataMapperInstance = dataMapperInstance;
            }
        };

        TypeStructDefinition.prototype.getOnConnectInstance = function () {
            return this._onConnectInstance;
        };

        TypeStructDefinition.prototype.setOnConnectInstance = function (onConnectInstance) {
            if(!_.isUndefined(onConnectInstance)){
                this._onConnectInstance = onConnectInstance;
            }
        };

        TypeStructDefinition.prototype.getOnDisconnectInstance = function () {
            return this._onDisconnectInstance;
        };

        TypeStructDefinition.prototype.setOnDisconnectInstance = function (onDisconnectInstance) {
            if(!_.isUndefined(onDisconnectInstance)){
                this._onDisconnectInstance = onDisconnectInstance;
            }
        };

        return TypeStructDefinition;
    });
