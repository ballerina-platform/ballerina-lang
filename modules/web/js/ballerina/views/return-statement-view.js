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
define(['lodash', 'log', './../ast/return-statement', './simple-statement-view', 'd3utils', 'd3'],
    function (_, log, ReturnStatement, SimpleStatementView, D3Utils, d3) {

        /**
         * The view to represent a return statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ReturnStatement} args.model - The return statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class ReturnStatementView
         * @constructor
         * @extends SimpleStatementView
         */
        var ReturnStatementView = function (args) {
            SimpleStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof ReturnStatement)) {
                log.error("Return statement definition is undefined or is of different type." + this._model);
                throw "Return statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for return statement is undefined." + this._container);
                throw "Container for return statement is undefined." + this._container;
            }
        };

        ReturnStatementView.prototype = Object.create(SimpleStatementView.prototype);
        ReturnStatementView.prototype.constructor = ReturnStatementView;

        ReturnStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ReturnStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("Return statement definition is undefined or is of different type." + model);
                throw "Return statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Rendering the view of the return statement.
         * @returns {Object} - The svg group which the return statement view resides in.
         */
        ReturnStatementView.prototype.render = function (diagramRenderingContext) {
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, diagramRenderingContext);
            // Update model.
            var model = this.getModel();
            model.accept(this);
            // Setting display text.
            this.renderDisplayText(model.getReturnExpression());

            var statementGroup = this.getStatementGroup();
            // Creating property pane.
            var editableProperty = {
                propertyType: "text",
                key: "Expression",
                model: model,
                getterMethod: model.getExpression,
                setterMethod: model.setExpression
            };

            this._createDebugIndicator({
                statementGroup: statementGroup
            });

            this._createPropertyPane({
                                         model: model,
                                         statementGroup: statementGroup,
                                         editableProperties: editableProperty
                                     });
            this.listenTo(model, 'update-property-text', this.updateReturnExpression);

            return statementGroup;
        };

        ReturnStatementView.prototype.updateReturnExpression = function (newReturnExpression, propertyKey) {
            this._model.setExpression(newReturnExpression);
            var displayText = this._model.getReturnExpression();
            this.renderDisplayText(displayText);
        };

        return ReturnStatementView;
    });