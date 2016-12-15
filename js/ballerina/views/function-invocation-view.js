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
define(['lodash', 'log', './ballerina-statement-view', './../ast/function-invocation', 'd3utils', 'd3'],
    function (_, log, BallerinaStatementView, FunctionInvocationStatement, D3Utils, d3) {

        /**
         * The view to represent a function invocation which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {FunctionInvocation} args.model - The function invocation statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var FunctionInvocationStatementView = function (args) {

            BallerinaStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof FunctionInvocationStatement)) {
                log.error("function invocation statement undefined or is of different type." + this._model);
                throw "function invocation statement undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for function invocation statement is undefined." + this._container);
                throw "Container for function invocation statement is undefined." + this._container;
            }

        };

        FunctionInvocationStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        FunctionInvocationStatementView.prototype.constructor = FunctionInvocationStatementView;

        FunctionInvocationStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof FunctionInvocationStatement) {
                this._model = model;
            } else {
                log.error("function invocation statement undefined or is of different type." + model);
                throw "function invocation statement undefined or is of different type." + model;
            }
        };

        FunctionInvocationStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for function invocation statement is undefined." + container);
                throw "Container for function invocation statement is undefined." + container;
            }
        };

        FunctionInvocationStatementView.prototype.getModel = function () {
            return this._model;
        };

        FunctionInvocationStatementView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Renders the view for function invocation statement.
         * @returns {group} - The SVG group which holds the elements of the function invocation statement.
         */
        FunctionInvocationStatementView.prototype.render = function () {
            var group = D3Utils.group(d3.select(this._container));
            var width = 120;
            var height = 30;
            var x = this.getXPosition();
            var y = this.getYPosition();
            var invocationRect = D3Utils.rect(x, y, 120, 30, 0, 0, group).classed('statement-rect', true);
            var text = this._model.getPackageName() + ':' +this._model.getFunctionName() + '(';
            var params = this._model.getParams();
            for (var id = 0; id < params.length; id ++) {
                if (id > 0) {
                    text += ',' + params[id];
                } else {
                    text += params[id];
                }
            }
            text += ')';

            var expressionText = D3Utils.textElement(x + width/2, y + height/2, text, group).classed('statement-text', true);
            // Set x, y, height, width of the current view
            this.setWidth(width);
            this.setHeight(height);
            this.setXPosition(x);
            this.setYPosition(y);
            log.info("Rendering function invocation statement view.");
            this.getModel().accept(this);
            return group;
        };

        return FunctionInvocationStatementView;
    });