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
define(['require', 'lodash', 'jquery', 'log', './ballerina-statement-view', './../ast/catch-statement', 'd3utils', 'd3', './point'],
    function (require, _, $, log, BallerinaStatementView, CatchStatement, D3Utils, d3, Point) {

        /**
         * The view to represent a Catch statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {TryCatchStatement} args.model - The Try Catch statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent Statement View, which in this case the try-catch statement
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var CatchStatementView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});

            BallerinaStatementView.call(this, _.get(args, "parent"));
        };

        CatchStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        CatchStatementView.prototype.constructor = CatchStatementView;

        CatchStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        CatchStatementView.prototype.canVisitCatchStatement = function(){
            return true;
        };

        /**
         * Render the catch statement
         */
        CatchStatementView.prototype.render = function () {
            var catchGroup = D3Utils.group(this._container);
            var tryBlockBottomY = parseInt(this.getParent().getTryBlockView().getStatementGroup().outerRect.attr('y')) +
                parseInt(this.getParent().getTryBlockView().getStatementGroup().outerRect.attr('height'));
            var x = parseInt(this.getParent().getTryBlockView().getStatementGroup().outerRect.attr('x'));
            var y = tryBlockBottomY;
            var point = new Point(x, y);
            var width = 120;
            var height = 60;
            var outer_rect = D3Utils.rect(x, y, 120, 60, 0, 0, catchGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(x, y, 40, 20, 0, 0, catchGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(x + 20, y + 10, 'Catch', catchGroup).classed('statement-text', true);
            this._model.accept(this);

            catchGroup.outerRect = outer_rect;
            catchGroup.titleRect = title_rect;
            catchGroup.title_text = title_text;
            this.setStatementGroup(catchGroup);

            // Set the parent's(TryCatchView) width, height, x, y
            this.getParent().setWidth(width);
            this.getParent().setHeight(this.getParent().getHeight() + height);

            // Set x, y, height, width of the current view
            this.setWidth(width);
            this.setHeight(height);
            this.setXPosition(x);
            this.setYPosition(y);
            this._model.accept(this);
        };

        /**
         * set the catch statement model
         * @param {CatchStatement} model
         */
        CatchStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof CatchStatement) {
                this._model = model;
            } else {
                log.error("Try Catch statement definition is undefined or is of different type." + model);
                throw "Try Catch statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the catch statement
         * @param {svgGroup} container
         */
        CatchStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for Try Catch statement is undefined." + container);
                throw "Container for Try Catch statement is undefined." + container;
            }
        };

        CatchStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        CatchStatementView.prototype.getModel = function () {
            return this._model;
        };

        CatchStatementView.prototype.getContainer = function () {
            return this._container;
        };

        CatchStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        return CatchStatementView;
    });