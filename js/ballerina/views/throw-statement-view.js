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
define(['lodash', 'log', './ballerina-view', './../ast/throw-statement', 'd3utils'],
    function (_, log, BallerinaView, ThrowStatement, D3Utils) {

        /**
         * The view to represent a throw statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ThrowStatement} args.model - The throw statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ThrowStatementView = function (args) {
            BallerinaView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ThrowStatement)) {
                log.error("Throw statement definition is undefined or is of different type." + this._model);
                throw "Throw statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for throw statement is undefined." + this._container);
                throw "Container for throw statement is undefined." + this._container;
            }

        };

        ThrowStatementView.prototype = Object.create(BallerinaView.prototype);
        ThrowStatementView.prototype.constructor = ThrowStatementView;

        ThrowStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ThrowStatement) {
                this._model = model;
            } else {
                log.error("Throw statement definition is undefined or is of different type." + model);
                throw "Throw statement definition is undefined or is of different type." + model;
            }
        };

        ThrowStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for throw statement is undefined." + container);
                throw "Container for throw statement is undefined." + container;
            }
        };

        ThrowStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ThrowStatementView.prototype.getModel = function () {
            return this._model;
        };

        ThrowStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ThrowStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view of the throw statement.
         * @returns {Object} - The svg group which the throw statement view resides in.
         */
        ThrowStatementView.prototype.render = function () {
            var group = D3Utilsgroup(this._container);
            log.info("Rendering the throw Statement.");
            return group;
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ThrowStatementView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        return ThrowStatementView;
    });