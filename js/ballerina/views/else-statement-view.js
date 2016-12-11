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
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});

            BallerinaStatementView.call(this, _.get(args, "parent"));
        };

        ElseStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ElseStatementView.prototype.constructor = ElseStatementView;

        ElseStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        ElseStatementView.prototype.canVisitElseStatement = function(){
            return true;
        };

        /**
         * Render the else statement
         */
        ElseStatementView.prototype.render = function () {
            var elseGroup = D3Utils.group(this._container);
            var ifBlockBottomY = parseInt(this.getParent().getIfBlockView().getStatementGroup().outerRect.attr('y')) +
                parseInt(this.getParent().getIfBlockView().getStatementGroup().outerRect.attr('height'));
            var x = parseInt(this.getParent().getIfBlockView().getStatementGroup().outerRect.attr('x'));
            var y = ifBlockBottomY;
            var point = new Point(x, y);
            var width = 120;
            var height = 60;
            var outer_rect = D3Utils.rect(x, y, 120, 60, 0, 0, elseGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(x, y, 40, 20, 0, 0, elseGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(x + 20, y + 10, 'Else', elseGroup).classed('statement-text', true);
            this._model.accept(this);

            elseGroup.outerRect = outer_rect;
            elseGroup.titleRect = title_rect;
            elseGroup.title_text = title_text;
            this.setStatementGroup(elseGroup);

            // Set the parent's(IfElseView) width, height, x, y
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

        return ElseStatementView;
    });