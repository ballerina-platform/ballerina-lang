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
define(['require', 'lodash', 'jquery', 'log', './ballerina-statement-view', './../ast/else-statement', 'd3utils', 'd3', './point'],
    function (require, _, $, log, BallerinaStatementView, ElseStatement, D3Utils, d3, Point) {

        /**
         * The view to represent a Else statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {IfElseStatement} args.model - The Else statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent Statement View, which in this case the if-else statement
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ElseStatementView = function (args) {
            BallerinaStatementView.call(this, args);
        };

        ElseStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ElseStatementView.prototype.constructor = ElseStatementView;

        ElseStatementView.prototype.canVisitElseStatement = function(){
            return true;
        };

        /**
         * Render the else statement
         */
        ElseStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var elseGroup = D3Utils.group(this._container);
            var x = 0;
            var y = 0;

            // If we have more than one else if we get the position of the last else if
            var lastElseIf = this.getParent().getLastElseIf();
            if (!_.isUndefined(lastElseIf)) {
                x = parseInt(lastElseIf.getStatementGroup().outerRect.attr('x'));
                y = parseInt(lastElseIf.getStatementGroup().outerRect.attr('y')) +
                    parseInt(lastElseIf.getStatementGroup().outerRect.attr('height'));
            } else {
                y = parseInt(this.getParent().getIfBlockView().getStatementGroup().outerRect.attr('y')) +
                    parseInt(this.getParent().getIfBlockView().getStatementGroup().outerRect.attr('height'));
                x = parseInt(this.getParent().getIfBlockView().getStatementGroup().outerRect.attr('x'));
            }
            var point = new Point(x, y);
            var width = parseInt(this.getParent().getIfBlockView().getStatementGroup().outerRect.attr('width'));
            var height = 60;
            var outer_rect = D3Utils.rect(x, y, width, height, 0, 0, elseGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(x, y, 40, 20, 0, 0, elseGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(x + 20, y + 10, 'Else', elseGroup).classed('statement-text', true);

            elseGroup.outerRect = outer_rect;
            elseGroup.titleRect = title_rect;
            elseGroup.titleText = title_text;

            // Set the parent's(IfElseView) width, height, x, y
            this.getParent().setWidth(width);
            this.getParent().setHeight(this.getParent().getHeight() + height);
            this.getParent().setXPosition(x);
            this.getParent().setYPosition(y);

            // Set x, y, height, width of the current view
            this.setWidth(width);
            this.setHeight(height);
            this.setXPosition(x);
            this.setYPosition(y);
            this.setBoundingBox(width, height, x, y);
            this.setStatementGroup(elseGroup);
            this._model.accept(this);
        };

        /**
         * set the else statement model
         * @param {ElseStatement} model
         */
        ElseStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ElseStatement) {
                this._model = model;
            } else {
                log.error("Else statement definition is undefined or is of different type." + model);
                throw "Else statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the else statement
         * @param {svgGroup} container
         */
        ElseStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for Else statement is undefined." + container);
                throw "Container for Else statement is undefined." + container;
            }
        };

        ElseStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ElseStatementView.prototype.getModel = function () {
            return this._model;
        };

        ElseStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ElseStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        ElseStatementView.prototype.childVisitedCallback = function (child) {
            var childView = this._diagramRenderingContext.getViewModelMap()[child.id];
            var childMetrics = {
                width: childView.getWidth(),
                height: childView.getHeight(),
                x: childView.getXPosition(),
                y: childView.getYPosition()
            };
            this.getParent().changeChildrenMetrics(childMetrics);
        };

        ElseStatementView.prototype.changeMetricsCallback = function (baseMetrics) {
            var dw = 20;
            var dh = 20;
            var oldX = parseInt(baseMetrics.x);
            var oldHeight = parseInt(this.getHeight());
            var newHeight = baseMetrics.height + dh/2 + 30;
            var newWidth = baseMetrics.width + dw;
            var newX = oldX - dw/2;
            var newY = baseMetrics.y;

            this.getStatementGroup().outerRect.attr('width', newWidth);
            this.getStatementGroup().outerRect.attr('height', newHeight);
            this.getStatementGroup().outerRect.attr('x', newX);
            this.getStatementGroup().titleRect.attr('x', newX);
            this.getStatementGroup().titleText.attr('x', oldX + 20 - dw/2);
            this.setWidth(newWidth);
            this.setHeight(newHeight);
            this.getParent().setWidth(newWidth);
            this.getParent().setHeight(this.getParent().getHeight() + newHeight - oldHeight);
            this.getParent().setXPosition(newX);
            this.getParent().setWidth(newWidth);

            this.setBoundingBox(newWidth, newHeight, newX, newY);
        };

        return ElseStatementView;
    });