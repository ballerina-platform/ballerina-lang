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
define(['require', 'lodash', 'jquery', 'log', './ballerina-statement-view', './../ast/else-if-statement', 'd3utils', 'd3', './point'],
    function (require, _, $, log, BallerinaStatementView, ElseIfStatement, D3Utils, d3, Point) {

        /**
         * The view to represent a Else-If statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ElseIfStatement} args.model - The Else-If statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent Statement View, which in this case the if-else statement
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ElseIfStatementView = function (args) {
            BallerinaStatementView.call(this, args);
            this.init();
        };

        ElseIfStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ElseIfStatementView.prototype.constructor = ElseIfStatementView;

        ElseIfStatementView.prototype.canVisitElseIfStatement = function(){
            return true;
        };

        ElseIfStatementView.prototype.init = function () {
            this.listenTo(this.getParent(), 'parent-bbox-modified', this.parentViewModifiedCallback);
        };

        /**
         * Render the else-if statement
         */
        ElseIfStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var elseIfGroup = D3Utils.group(this._container);
            elseIfGroup.attr("id","_" +this._model.id);

            // Default width and height of the else if statement.
            // TODO: Read these from the constants
            // This initial height is read from the viewOptions. Because the else statement's initial width depends on
            // the parent view's(IfElseStatementView) width
            var width = this.getViewOptions().width;
            var height = 60;
            // Initialize the bounding box
            this.getBoundingBox().fromTopCenter(this.getTopCenter(), width, height);
            var outer_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), width, height, 0, 0, elseIfGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), 40, 20, 0, 0, elseIfGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(this.getBoundingBox().x() + 20, this.getBoundingBox().y() + 10, 'ElseIf', elseIfGroup).classed('statement-text', true);
            elseIfGroup.outerRect = outer_rect;
            elseIfGroup.titleRect = title_rect;
            elseIfGroup.titleText = title_text;
            this.setStatementGroup(elseIfGroup);
            this._model.accept(this);
            this.trigger('sub-component-rendered', this.getBoundingBox());
        };

        /**
         * Set the else-if statement model
         * @param {ElseIfStatement} model
         */
        ElseIfStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof IfStatement) {
                this._model = model;
            } else {
                log.error("Else If statement definition is undefined or is of different type." + model);
                throw "Else If statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the if statement
         * @param container
         */
        ElseIfStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for Else If statement is undefined." + container);
                throw "Container for Else If statement is undefined." + container;
            }
        };

        ElseIfStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ElseIfStatementView.prototype.getModel = function () {
            return this._model;
        };

        ElseIfStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ElseIfStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        ElseIfStatementView.prototype.parentViewModifiedCallback = function (parentBBox) {
            if (this.getBoundingBox().w() < parentBBox.w()) {
                var newWidth = parentBBox.w();
                this.getStatementGroup().outerRect.attr('width', newWidth);
                this.getStatementGroup().outerRect.attr('x', parentBBox.x());
                this.getStatementGroup().titleRect.attr('x', parentBBox.x());
                this.getStatementGroup().titleText.attr('x', parentBBox.x() + 20);
            }
        };

        return ElseIfStatementView;
    });