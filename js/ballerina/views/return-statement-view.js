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
define(['lodash', 'log', './ballerina-view', './../ast/return-statement', 'd3utils'],
    function (_, log, BallerinaView, ReturnStatement, D3Utils) {

        /**
         * The view to represent a return statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ReturnStatement} args.model - The return statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ReturnStatementView = function (args) {
            BallerinaView.call(this, args);
            if (_.isNil(this._model) || !(this._model instanceof ReturnStatement)) {
                log.error("Return statement definition is undefined or is of different type." + this._model);
                throw "Return statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for return statement is undefined." + this._container);
                throw "Container for return statement is undefined." + this._container;
            }
        };

        ReturnStatementView.prototype = Object.create(BallerinaView.prototype);
        ReturnStatementView.prototype.constructor = ReturnStatementView;

        ReturnStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ReturnStatement) {
                this._model = model;
            } else {
                log.error("Return statement definition is undefined or is of different type." + model);
                throw "Return statement definition is undefined or is of different type." + model;
            }
        };

        ReturnStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for return statement is undefined." + container);
                throw "Container for return statement is undefined." + container;
            }
        };

        ReturnStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ReturnStatementView.prototype.getModel = function () {
            return this._model;
        };

        ReturnStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ReturnStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view of the return statement.
         * @returns {Object} - The svg group which the return statement view resides in.
         */
        ReturnStatementView.prototype.render = function () {
            var group = D3Utils.group(this._container);
            // var rect = D3Utils.draw.rect(10, 10, 100, 100, 0, 0, group, "#FFFFFF");
            log.info("Rendering the Return Statement.");
            // group.rect = rect;
            return group;
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ReturnStatementView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        return ReturnStatementView;
    });