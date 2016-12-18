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
define(['lodash', 'log', './ballerina-statement-view', './../ast/arithmetic-expression', 'd3utils', 'd3'],
    function (_, log, BallerinaStatementView, ArithmeticExpression, D3Utils, d3) {

        /**
         * The view to represent a arithmetic expression which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ArithmeticExpression} args.model - The arithmetic expression model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ArithmeticExpressionView = function (args) {
            BallerinaStatementView.call(this, args);
            if (_.isNil(this._model) || !(this._model instanceof ArithmeticExpression)) {
                log.error("arithmetic expression undefined or is of different type." + this._model);
                throw "arithmetic expression undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for arithmetic expression is undefined." + this._container);
                throw "Container for arithmetic expression is undefined." + this._container;
            }
        };

        ArithmeticExpressionView.prototype = Object.create(BallerinaStatementView.prototype);
        ArithmeticExpressionView.prototype.constructor = ArithmeticExpressionView;

        ArithmeticExpressionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ArithmeticExpression) {
                this._model = model;
            } else {
                log.error("arithmetic expression undefined or is of different type." + model);
                throw "arithmetic expression undefined or is of different type." + model;
            }
        };

        ArithmeticExpressionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for arithmetic expression is undefined." + container);
                throw "Container for arithmetic expression is undefined." + container;
            }
        };

        ArithmeticExpressionView.prototype.getModel = function () {
            return this._model;
        };

        ArithmeticExpressionView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Renders the view for arithmetic expression.
         * @returns {group} - The SVG group which holds the elements of the arithmetic expression.
         */
        ArithmeticExpressionView.prototype.render = function () {
            var group = D3Utils.group(d3.select(this._container));
            group.attr("id","_" +this._model.id);
            var width = 120;
            var height = 30;
            var x = this.getXPosition();
            var y = this.getYPosition();
            var expressionRect = D3Utils.rect(x, y, 120, 30, 0, 0, group).classed('statement-rect', true);
            var text = this._model.getExpression();

            var expressionText = D3Utils.textElement(x + width/2, y + height/2, text, group).classed('statement-text', true);
            // Set x, y, height, width of the current view
            this.setWidth(width);
            this.setHeight(height);
            this.setXPosition(x);
            this.setYPosition(y);
            log.info("Rendering arithmetic expression view.");
            return group;
        };

        return ArithmeticExpressionView;
    });