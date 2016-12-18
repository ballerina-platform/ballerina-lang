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
define(['require', 'lodash', 'jquery', 'log', './ballerina-statement-view', './../ast/if-statement', 'd3utils', 'd3', './point'],
    function (require, _, $, log, BallerinaStatementView, IfStatement, D3Utils, d3, Point) {

        /**
         * The view to represent a If statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {IfStatement} args.model - The If statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent Statement View, which in this case the if-else statement
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var IfStatementView = function (args) {
            BallerinaStatementView.call(this, args);
            this.init();
        };

        IfStatementView.prototype.init = function () {
            this.listenTo(this.getParent(), 'parent-bbox-modified', this.modifyView);
        };

        IfStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        IfStatementView.prototype.constructor = IfStatementView;

        IfStatementView.prototype.canVisitIfStatement = function(){
            return true;
        };

        /**
         * Render the if statement
         */
        IfStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var ifGroup = D3Utils.group(this._container);
            ifGroup.attr("id","_" +this._model.id);
            // Default width and height of the if statement.
            // TODO: Read these from the constants
            var width = 120;
            var height = 60;
            // Initialize the bounding box
            this.getBoundingBox().fromTopCenter(this.getTopCenter(), width, height);
            var outer_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), width, height, 0, 0, ifGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), 40, 20, 0, 0, ifGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(this.getBoundingBox().x() + 20, this.getBoundingBox().y() + 10, 'If', ifGroup).classed('statement-text', true);
            ifGroup.outerRect = outer_rect;
            ifGroup.titleRect = title_rect;
            ifGroup.titleText = title_text;
            this.setStatementGroup(ifGroup);
            this._model.accept(this);
            this.trigger('sub-component-rendered', this.getBoundingBox());
        };

        /**
         * Set the if statement model
         * @param {IfStatement} model
         */
        IfStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof IfStatement) {
                this._model = model;
            } else {
                log.error("If Else statement definition is undefined or is of different type." + model);
                throw "If Else statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the if statement
         * @param container
         */
        IfStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for If Else statement is undefined." + container);
                throw "Container for If Else statement is undefined." + container;
            }
        };

        IfStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        IfStatementView.prototype.getModel = function () {
            return this._model;
        };

        IfStatementView.prototype.getContainer = function () {
            return this._container;
        };

        IfStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        // IfStatementView.prototype.childVisitedCallback = function (child) {
        //     var childView = this._diagramRenderingContext.getViewModelMap()[child.id];
        //     var childBoundingBox = childView.getBoundingBox();
        //     this.getParent().changeChildrenMetrics(childBoundingBox);
        // };

        // IfStatementView.prototype.changeMetricsCallback = function (childBoundingBox) {
        //
        //     var parentBoundingBox = this.getParent().getBoundingBox();
        //
        //     this.getStatementGroup().outerRect.attr('width', parentBoundingBox.width);
        //     this.getStatementGroup().outerRect.attr('height', childBoundingBox.height + 40);
        //     this.getStatementGroup().outerRect.attr('x', parentBoundingBox.x);
        //     this.getStatementGroup().titleRect.attr('x', parentBoundingBox.x);
        //     this.getStatementGroup().titleText.attr('x', parentBoundingBox.x + 20);
        //
        //     this.setBoundingBox(parentBoundingBox.width, parentBoundingBox.height, parentBoundingBox.x, parentBoundingBox.y);
        // };

        IfStatementView.prototype.modifyView = function (parentBBox) {

        };

        return IfStatementView;
    });