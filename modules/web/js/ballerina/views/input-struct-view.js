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
define(['lodash', 'log','./ballerina-view','./../ast/resource-parameter', 'typeMapper'],
    function (_, log, BallerinaView,ResourceParameter, TypeMapperRenderer) {

        var InputStructView = function (args) {
            BallerinaView.call(this, args);
            this._parentView = _.get(args, "parentView");
            this._onConnectInstance = _.get(args, 'onConnectInstance', {});
            this._onDisconnectInstance = _.get(args, 'onDisconnectInstance', {});
            this._sourceInfo = _.get(args, 'sourceInfo', {});

            if (_.isNil(this.getModel()) || !(this._model instanceof ResourceParameter)) {
                log.error("Resource parameter is undefined or is of different type." + this.getModel());
                throw "Resource parameter is undefined or is of different type." + this.getModel();
            }

        };

        InputStructView.prototype = Object.create(BallerinaView.prototype);
        InputStructView.prototype.constructor = InputStructView;

        InputStructView.prototype.canVisitInputStructView = function (inputStructView) {
            return true;
        };

        /**
         * Rendering the view of the Resource parameter.
         * @param {Object} diagramRenderingContext - the object which is carrying data required for rendering
         */
        InputStructView.prototype.render = function (diagramRenderingContext, mapper) {
            var self = this;
            this._diagramRenderingContext = diagramRenderingContext;
            var typeStructName = this.getModel().getStructType();
            var typeStructSchema = this.getSourceInfo().sourceStruct;

            if(!mapper) {
                mapper = new TypeMapperRenderer(self.getOnConnectInstance(), self.getOnDisconnectInstance(), this._parentView);
                this._parentView._typeMapper = mapper;
            }

            mapper.removeStruct(typeStructName);
            mapper.addSourceStruct(typeStructSchema.getAttributesArray(),this.getModel());


    //        var struct = this.getModel().getSchemaPropertyObj();
    //        var category = this.getModel().getCategory();
    //        var selectedStructName = this.getModel().getSelectedStructName();
    //        if(!mapper) {
    //            mapper = new TypeMapperRenderer(this.getModel().getOnConnectInstance(), this.getModel().getOnDisconnectInstance(), this._parentView);
    //            this._parentView._typeMapper = mapper;
    //        }
    //        mapper.removeStruct(selectedStructName);
    //        if (category == "SOURCE"){
    //            mapper.addSourceStruct(struct,this.getModel());
    //        } else{
    //            mapper.addTargetStruct(struct,this.getModel());
    //        }
        };

        /**
         * returns the call back function to be called when a connection is drawn
         * @returns {object}
         */
        InputStructView.prototype.getOnConnectInstance = function () {
            return this._onConnectInstance;
        };

        /**
         * set the call back function for connecting a source and a target
         * @param onConnectInstance
         */
        InputStructView.prototype.setOnConnectInstance = function (onConnectInstance) {
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
        InputStructView.prototype.getOnDisconnectInstance = function () {
            return this._onDisconnectInstance;
        };

        /**
         * set the call back function for disconnecting a source and a target
         * @param onDisconnectInstance
         */
        InputStructView.prototype.setOnDisconnectInstance = function (onDisconnectInstance) {
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
        InputStructView.prototype.getSourceInfo = function () {
            return this._sourceInfo;
        };

        return InputStructView;
});