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
define(['lodash', 'log','./ballerina-view','./../ast/return-type', 'typeMapper','constants'],
    function (_, log, BallerinaView,ReturnType, TypeMapperRenderer,Constants) {

        var OutputStructView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");
            this._onConnectInstance = _.get(args, 'onConnectInstance', {});
            this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
            this._targetInfo = _.get(args, 'targetInfo', {});

            if (_.isNil(this.getModel()) || !(this._model instanceof ReturnType)) {
                log.error("Return type is undefined or is of different type." + this.getModel());
                throw "Return type is undefined or is of different type." + this.getModel();
            }

        };

        OutputStructView.prototype = Object.create(BallerinaView.prototype);
        OutputStructView.prototype.constructor = OutputStructView;

        OutputStructView.prototype.canVisitInputStructView = function (outputStructView) {
            return true;
        };

        /**
         * Rendering the view of the Return type.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        OutputStructView.prototype.render = function (diagramRenderingContext, mapper) {
            var self = this;
            this._diagramRenderingContext = diagramRenderingContext;

            var typeStructSchema = this.getTargetInfo().targetStruct;
            var previousSelection = this.getTargetInfo()[TYPE_MAPPER_COMBOBOX_PREVIOUS_SELECTION];

            if(!mapper) {
                mapper = new TypeMapperRenderer(self.getOnConnectInstance(), self.getOnDisconnectInstance(), this._parentView);
                this._parentView._typeMapper = mapper;
            }

            if(previousSelection != undefined && previousSelection != TYPE_MAPPER_COMBOBOX_DEFAULT_SELECTION){
                mapper.removeStruct(previousSelection);
            }
            mapper.addTargetStruct(typeStructSchema.getAttributesArray(),this.getModel());
        };

        /**
         * returns the call back function to be called when a connection is drawn
         * @returns {object}
         */
        OutputStructView.prototype.getOnConnectInstance = function () {
            return this._onConnectInstance;
        };

        /**
         * set the call back function for connecting a source and a target
         * @param onConnectInstance
         */
        OutputStructView.prototype.setOnConnectInstance = function (onConnectInstance) {
            if (!_.isNil(onConnectInstance)) {
                this._onConnectInstance = onConnectInstance;
            } else {
                log.error('Invalid onConnectInstance [' + onConnectInstance + '] Provided');
                throw 'Invalid onConnectInstance [' + onConnectInstance + '] Provided';
            }
        };

        /**
         * returns the call back function to be called when a connection is removed
         * @returns {object}
         */
        OutputStructView.prototype.getOnDisconnectInstance = function () {
            return this._onDisconnectInstance;
        };

        /**
         * set the call back function for disconnecting a source and a target
         * @param onDisconnectInstance
         */
        OutputStructView.prototype.setOnDisconnectInstance = function (onDisconnectInstance) {
            if (!_.isNil(onDisconnectInstance)) {
                this._onDisconnectInstance = onDisconnectInstance;
            } else {
                log.error('Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided');
                throw 'Invalid onDisconnectInstance [' + onDisconnectInstance + '] Provided';
            }
        };

        /**
         * returns the source info
         * @returns {object}
         */
        OutputStructView.prototype.getTargetInfo = function () {
            return this._targetInfo;
        };

        return OutputStructView;
});