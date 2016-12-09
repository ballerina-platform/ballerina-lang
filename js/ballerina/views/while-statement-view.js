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
define(['lodash', 'log', './ballerina-view', './../ast/while-statement', 'd3utils'],
    function (_, log, BallerinaView, WhileStatement, D3Utils) {

        /**
         * The view to represent a while statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WhileStatement} args.model - The while statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var WhileStatementView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});

            if (_.isNil(this._model) || !(this._model instanceof WhileStatement)) {
                log.error("While statement definition is undefined or is of different type." + this._model);
                throw "While statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for while statement is undefined." + this._container);
                throw "Container for while statement is undefined." + this._container;
            }

            BallerinaView.call(this);
        };

        WhileStatementView.prototype = Object.create(BallerinaView.prototype);
        WhileStatementView.prototype.constructor = WhileStatementView;

        WhileStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof WhileStatement) {
                this._model = model;
            } else {
                log.error("While statement definition is undefined or is of different type." + model);
                throw "While statement definition is undefined or is of different type." + model;
            }
        };

        WhileStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for while statement is undefined." + container);
                throw "Container for while statement is undefined." + container;
            }
        };

        WhileStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        WhileStatementView.prototype.getModel = function () {
            return this._model;
        };

        WhileStatementView.prototype.getContainer = function () {
            return this._container;
        };

        WhileStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        WhileStatementView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        return WhileStatementView;
    });