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

        /**
         * The view to represent a Try statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {IfStatement} args.model - The If statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent Statement View, which in this case the try-catch statement
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var TryStatementView = function (args) {
            BallerinaStatementView.call(this, args);
        };

        TryStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        TryStatementView.prototype.constructor = TryStatementView;

        TryStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        TryStatementView.prototype.canVisitTryStatement = function(){
            return true;
        };

        /**
         * Render the try statement
         */
        TryStatementView.prototype.render = function () {
            var tryGroup = D3Utils.group(this._container);
            tryGroup.attr("id","_" +this._model.id);
            var x = this.getParent().getXPosition();
            var y = this.getParent().getYPosition();
            var width = 120;
            var height = 60;
            var outer_rect = D3Utils.rect(x, y, 120, 60, 0, 0, tryGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(x, y, 40, 20, 0, 0, tryGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(x + 20, y + 10, 'Try', tryGroup).classed('statement-text', true);

            // Set the parent's(TryCatchView) width, height, x, y
            this.getParent().setWidth(width);
            this.getParent().setHeight(height);
            this.getParent().setXPosition(x);
            this.getParent().setYPosition(y);

            // Set x, y, height, width of the current view
            this.setWidth(width);
            this.setHeight(height);
            this.setXPosition(x);
            this.setYPosition(y);

            tryGroup.outerRect = outer_rect;
            tryGroup.titleRect = title_rect;
            tryGroup.titleText = title_text;
            this.setStatementGroup(tryGroup);
            this._model.accept(this);
        };

        /**
         * Set the try statement model
         * @param {TryStatement} model
         */
        TryStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof TryStatement) {
                this._model = model;
            } else {
                log.error("If statement definition is undefined or is of different type." + model);
                throw "If statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the try statement
         * @param container
         */
        TryStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for If statement is undefined." + container);
                throw "Container for If statement is undefined." + container;
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

        return TryStatementView;
    });