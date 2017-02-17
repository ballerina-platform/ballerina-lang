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
define(['lodash', 'log', './simple-statement-view', './../ast/function-invocation', 'd3utils', 'd3'],
    function (_, log, SimpleStatementView, FunctionInvocationStatement, D3Utils, d3) {

        /**
         * The view to represent a function invocation which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {FunctionInvocation} args.model - The function invocation statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class FunctionInvocationStatementView
         * @constructor
         * @extends SimpleStatementView
         */
        var FunctionInvocationStatementView = function (args) {
            SimpleStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof FunctionInvocationStatement)) {
                log.error("function invocation statement undefined or is of different type." + this._model);
                throw "function invocation statement undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for function invocation statement is undefined." + this._container);
                throw "Container for function invocation statement is undefined." + this._container;
            }
        };

        FunctionInvocationStatementView.prototype = Object.create(SimpleStatementView.prototype);
        FunctionInvocationStatementView.prototype.constructor = FunctionInvocationStatementView;

        FunctionInvocationStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof FunctionInvocationStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("function invocation statement undefined or is of different type." + model);
                throw "function invocation statement undefined or is of different type." + model;
            }
        };

        /**
         * Renders the view for function invocation statement.
         * @returns {group} - The SVG group which holds the elements of the function invocation statement.
         */
        FunctionInvocationStatementView.prototype.render = function (renderingContext) {
            log.debug("Rendering Function Invocation Statement started.");
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, renderingContext);
            var model = this.getModel().getChildren()[0];
            // Setting display text.
            this.renderDisplayText(model.getFunctionalExpression());
            // Setting group ID.
            var statementGroup = this.getStatementGroup();

            // Creating property pane.
            var editableProperty = {
                propertyType: "text",
                key: "Function",
                model: model,
                getterMethod: model.getFunctionalExpression,
                setterMethod: model.setFunctionalExpression
            };
            this._createPropertyPane({
                model: model,
                statementGroup: statementGroup,
                editableProperties: editableProperty
            });
            this.listenTo(model, 'update-property-text', this.updateFunctionalExpression);

            this._createDebugIndicator({
                statementGroup: statementGroup
            });

            log.debug("Rendering Function Invocation Statement finished.");
            return statementGroup;
        };

        FunctionInvocationStatementView.prototype.updateFunctionalExpression = function (newExpression, propertyKey) {
            this._model.getChildren()[0].setFunctionalExpression(newExpression); // Set property value.
            var displayText = this._model.getChildren()[0].getFunctionalExpression();
            this.renderDisplayText(displayText);// Set display text.
        };

        return FunctionInvocationStatementView;
    });
