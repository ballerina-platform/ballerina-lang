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

define(['lodash','d3', 'jquery', './ballerina-view', './../ast/connector-declaration', 'log', 'd3utils','./life-line'],
    function (_, d3, $, BallerinaView, ConnectorDeclaration, log, D3utils, LifeLine) {

        /**
         * The view to represent a connection declaration which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ConnectionDeclaration} args.model - The connection declaration model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ConnectorDeclarationView = function (args) {
            this._totalHeightGap = 50;
            this._parentView = _.get(args, "parentView");
            LifeLine.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ConnectorDeclaration)) {
                log.error("Connection declaration is undefined or is of different type." + this._model);
                throw "Connection declaration is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for connection declaration is undefined." + this._container);
                throw "Container for connection declaration is undefined." + this._container;
            }

            this.init();
        };

        ConnectorDeclarationView.prototype = Object.create(LifeLine.prototype);
        ConnectorDeclarationView.prototype.constructor = ConnectorDeclaration;

        ConnectorDeclarationView.prototype.init = function () {
            this.listenTo(this._parentView, 'resourceHeightChangedEvent', this.resourceHeightChangedCallback);
        };

        /**
         * Callback function for resource's height changed event
         * @param height
         */
        ConnectorDeclarationView.prototype.resourceHeightChangedCallback = function (height) {
            this.setHeight(height - this._totalHeightGap);
        };

        ConnectorDeclarationView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ConnectorDeclaration) {
                this._model = model;
            } else {
                log.error("Connection declaration is undefined or is of different type." + model);
                throw "Connection declaration is undefined or is of different type." + model;
            }
        };

        ConnectorDeclarationView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for connection declaration is undefined." + container);
                throw "Container for connection declaration is undefined." + container;
            }
        };

        ConnectorDeclarationView.prototype.getModel = function () {
            return this._model;
        };

        ConnectorDeclarationView.prototype.getContainer = function () {
            return this._container;
        };

        return ConnectorDeclarationView;
    });