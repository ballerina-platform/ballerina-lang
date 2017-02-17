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
define(['lodash', 'log', './ballerina-statement-view', './../ast/logical-expression', 'd3utils', 'd3'],
    function (_, log, BallerinaStatementView, LogicalExpression, D3Utils, d3) {

        /**
         * The view to represent a logical expression which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {LogicalExpression} args.model - The logical expression model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var LogicalExpressionView = function (args) {

            BallerinaStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof LogicalExpression)) {
                log.error("logical expression undefined or is of different type." + this._model);
                throw "logical expression undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for logical expression is undefined." + this._container);
                throw "Container for logical expression is undefined." + this._container;
            }
            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
        };

        LogicalExpressionView.prototype = Object.create(BallerinaStatementView.prototype);
        LogicalExpressionView.prototype.constructor = LogicalExpressionView;

        LogicalExpressionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof LogicalExpression) {
                this._model = model;
            } else {
                log.error("logical expression undefined or is of different type." + model);
                throw "logical expression undefined or is of different type." + model;
            }
        };

        LogicalExpressionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for logical expression is undefined." + container);
                throw "Container for logical expression is undefined." + container;
            }
        };

        LogicalExpressionView.prototype.getModel = function () {
            return this._model;
        };

        LogicalExpressionView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Renders the view for logical expression.
         * @returns {group} - The SVG group which holds the elements of the logical expression.
         */
        LogicalExpressionView.prototype.render = function () {
            var group = D3Utils.group(d3.select(this._container));
            var width = 120;
            var height = 30;
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
            var x = this.getBoundingBox().x();
            var y = this.getBoundingBox().y();
            var expressionRect = D3Utils.rect(x, y, 120, 30, 0, 0, group).classed('statement-rect', true);
            var text = this._model.getExpression();
            var expressionText = D3Utils.textElement(x + width/2, y + height/2, text, group).classed('statement-text', true);
            log.debug("Rendering logical expression view.");
            group.expression_rect = expressionRect;
            group.expression_text = expressionText;
            this.setStatementGroup(group);
            this.listenTo(this._model, 'update-property-text', this.updateStatementText);
            this._model.accept(this);

            // Creating property pane
            var editableProperties = [];
            var editableProperty = {
                propertyType: "text",
                key: "Expression",
                model: this._model,
                getterMethod: this._model.getExpression,
                setterMethod: this._model.setExpression
            };
            editableProperties.push(editableProperty);
            this._createPropertyPane({
                model: this._model,
                statementGroup:group,
                editableProperties: editableProperties
            });

            this._createDebugIndicator({
                statementGroup: group
            });

            return group;
        };

        LogicalExpressionView.prototype.updateStatementText = function (updatedText) {
            if (!_.isUndefined(updatedText) && updatedText !== '') {
                this.getStatementGroup().expression_text.node().textContent = updatedText;
            }
        };

        return LogicalExpressionView;
    });