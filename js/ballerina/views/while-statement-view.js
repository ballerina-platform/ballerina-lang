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
define(['lodash', 'log', './ballerina-statement-view', './../ast/while-statement', 'd3utils', 'd3'],
    function (_, log, BallerinaStatementView, WhileStatement, D3Utils, d3) {

        /**
         * The view to represent a while statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WhileStatement} args.model - The while statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var WhileStatementView = function (args) {
            BallerinaStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof WhileStatement)) {
                log.error("While statement definition is undefined or is of different type." + this._model);
                throw "While statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for while statement is undefined." + this._container);
                throw "Container for while statement is undefined." + this._container;
            }

        };

        WhileStatementView.prototype = Object.create(BallerinaStatementView.prototype);
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
         * Render the svg group to draw the if and the else statements
         */
        WhileStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var whileGroup = D3Utils.group(d3.select(this._container));
            var x = this.getXPosition();
            var y = this.getYPosition();
            var width = 120;
            var height = 60;
            var outer_rect = D3Utils.rect(x, y, 120, 60, 0, 0, whileGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(x, y, 40, 20, 0, 0, whileGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(x + 20, y + 10, 'While', whileGroup).classed('statement-text', true);

            // Set x, y, height, width of the current view
            this.setWidth(width);
            this.setHeight(height);
            this.setXPosition(x);
            this.setYPosition(y);
            this.setBoundingBox(width, height, x, y);

            whileGroup.outerRect = outer_rect;
            whileGroup.titleRect = title_rect;
            whileGroup.titleText = title_text;
            this.setStatementGroup(whileGroup);
            this._model.accept(this);
        };

        return WhileStatementView;
    });