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

define(['lodash', 'jquery', './ballerina-view', 'log','typeMapper'],
    function (_, $, BallerinaView, log,TypeMapper) {

        //todo add correct doc comments
        /**
         * The view to represent a worker declaration which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WorkerDeclaration} args.model - The worker declaration model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var TypeMapperFunctionDefinitionView = function (args) {

            BallerinaView.call(this, args);

            this._typeMapperFunctionRenderer = _.get(args, "renderer");
            this._onAttrConnectCallBack = _.get(args, "onAttrConnect");
            this._onAttrDisConnectCallBack = _.get(args, "onAttrDisConnect");

//            if (_.isNil(this._model) || !(this._model instanceof WorkerDeclaration)) {
//                log.error("Worker declaration definition undefined or is of different type." + this._model);
//                throw "Worker declaration definition undefined or is of different type." + this._model;
//            }
//
//            if (_.isNil(this._container)) {
//                log.error("Container for worker declaration is undefined." + this._container);
//                throw "Container for worker declaration is undefined." + this._container;
//            }

        };

        TypeMapperFunctionDefinitionView.prototype = Object.create(BallerinaView.prototype);
        TypeMapperFunctionDefinitionView.prototype.constructor = TypeMapperFunctionDefinitionView;

        TypeMapperFunctionDefinitionView.prototype.setModel = function (model) {
            this._model = model;
//            if (!_.isNil(model) && this._model instanceof WorkerDeclaration) {
//                this._model = model;
//            } else {
//                log.error("Worker declaration definition undefined or is of different type." + model);
//                throw "Worker declaration definition undefined or is of different type." + model;
//            }
        };

        TypeMapperFunctionDefinitionView.prototype.setContainer = function (container) {
            this._container = container;
//            if (!_.isNil(container)) {
//                this._container = container;
//            } else {
//                log.error("Container for worker declaration is undefined." + container);
//                throw "Container for worker declaration is undefined." + container;
//            }
        };

        TypeMapperFunctionDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        TypeMapperFunctionDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        TypeMapperFunctionDefinitionView.prototype.getTypeMapperFunctionRenderer = function () {
            return this._typeMapperFunctionRenderer;
        };

        //todo add correct doc comments
        /**
         * Rendering the view of the worker declaration.
         * @returns {Object} - The svg group which the worker declaration view resides in.
         */
        TypeMapperFunctionDefinitionView.prototype.render = function (diagramRenderingContext) {

            alert(999999999);
            var self = this;
            var typeMapperRenderer = new TypeMapper(this._onAttrConnectCallBack,this._onAttrDisConnectCallBack, self);
            this.getTypeMapperFunctionRenderer().addFunction(self.getFunctionSchema(self._model),self);
        };

        /**
         * return attributes list as a json object
         * @returns {Object} attributes array
         */
        TypeMapperFunctionDefinitionView.prototype.getFunctionSchema = function (model) {
            var schema = {};
            schema['name'] = model.getFunctionName();
            schema['returnType'] = "string";
            schema['parameters']= [];
            _.each(model.getChildren(), function(child) {
                var tempAttr = {};
                tempAttr['name'] = child.getVariableReferenceName();
                tempAttr['type'] = "string";
                schema['parameters'].push(tempAttr);
            });
            return schema;

        };

        return TypeMapperFunctionDefinitionView;
    });