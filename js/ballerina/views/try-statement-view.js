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
define(['require', 'lodash', 'jquery', 'log', './ballerina-statement-view', './../ast/try-statement', 'd3utils', 'd3', './point'],
    function (require, _, $, log, BallerinaStatementView, TryStatement, D3Utils, d3, Point) {

        var TryStatementView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});
            this._viewGroup = undefined;

            BallerinaStatementView.call(this, _.get(args, "parent"));
        };

        TryStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        TryStatementView.prototype.constructor = TryStatementView;

        TryStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        TryStatementView.prototype.canVisitTryStatement = function(){
            return true;
        };

        TryStatementView.prototype.render = function () {
            var tryGroup = D3Utils.group(this._container);
            var x = 180;
            var y = 210;
            var point = new Point(x, y);
            var width = 120;
            var height = 60;
            var outer_rect = D3Utils.rect(x - width/2, y, 120, 60, 0, 0, tryGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(x - width/2, y, 40, 20, 0, 0, tryGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(x - width/2 + 20, y + 10, 'Try', tryGroup).classed('statement-text', true);
            this._model.accept(this);

            tryGroup.outerRect = outer_rect;
            tryGroup.titleRect = title_rect;
            tryGroup.titleText = title_text;
            this._viewGroup = tryGroup;
        };

        TryStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof TryStatement) {
                this._model = model;
            } else {
                log.error("Try Catch statement definition is undefined or is of different type." + model);
                throw "Try Catch statement definition is undefined or is of different type." + model;
            }
        };

        TryStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for Try Catch statement is undefined." + container);
                throw "Container for Try Catch statement is undefined." + container;
            }
        };

        TryStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        TryStatementView.prototype.getModel = function () {
            return this._model;
        };

        TryStatementView.prototype.getContainer = function () {
            return this._container;
        };

        TryStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        TryStatementView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        TryStatementView.prototype.getViewGroup = function () {
            return this._viewGroup;
        };

        return TryStatementView;
    });