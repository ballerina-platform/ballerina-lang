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

define(['lodash', 'jquery', './canvas', './../ast/connector-declaration', 'log', 'd3utils'],
    function (_, $, Canvas, ConnectionDeclaration, log, D3Utils) {

        /**
         * The view to represent a connection declaration which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ConnectionDeclaration} args.model - The connection declaration model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ConnectionDeclarationView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});

            if (_.isNil(this._model) || !(this._model instanceof ConnectionDeclaration)) {
                log.error("Connection declaration is undefined or is of different type." + this._model);
                throw "Connection declaration is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for connection declaration is undefined." + this._container);
                throw "Container for connection declaration is undefined." + this._container;
            }

            Canvas.call(this);
        };

        ConnectionDeclarationView.prototype = Object.create(Canvas.prototype);
        ConnectionDeclarationView.prototype.constructor = ConnectionDeclaration;

        ConnectionDeclarationView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ConnectionDeclaration) {
                this._model = model;
            } else {
                log.error("Connection declaration is undefined or is of different type." + model);
                throw "Connection declaration is undefined or is of different type." + model;
            }
        };

        ConnectionDeclarationView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for connection declaration is undefined." + container);
                throw "Container for connection declaration is undefined." + container;
            }
        };

        ConnectionDeclarationView.prototype.getModel = function () {
            return this._model;
        };

        ConnectionDeclarationView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Rendering the view for connection declaration.
         * @returns {group} The svg group which contains the elements of the connection declaration view.
         */
        ConnectionDeclarationView.prototype.render = function () {
            var group = D3Utils.group(this._container);
            // TODO : Draw the view of the connection view and add it to the above svg group.
            log.info("Rendering the Worker Declaration");
            return group;
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ConnectionDeclarationView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        return ConnectionDeclarationView;
    });