/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash', 'log', './simple-statement-view', './../ast/break-statement'],
    function (_, log, SimpleStatementView, BreakStatement) {

        /**
         * The view to represent a break definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {Assignment} args.model - The assignment statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class BreakStatementView
         * @constructor
         * @extends SimpleStatementView
         */
        var BreakStatementView = function (args) {
            SimpleStatementView.call(this, args);

            if (_.isNil(this._container)) {
                log.error("Container for Break statement is undefined." + this._container);
                throw "Container for Break statement is undefined." + this._container;
            }
        };

        BreakStatementView.prototype = Object.create(SimpleStatementView.prototype);
        BreakStatementView.prototype.constructor = BreakStatementView;

        BreakStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        BreakStatementView.prototype.canVisitBreakStatement = function(){
            return true;
        };

        BreakStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof BreakStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("Break statement undefined or is of different type." + model);
                throw "Break statement undefined or is of different type." + model;
            }
        };

        /**
         * Renders the view for break statement.
         * @returns {group} - The SVG group which holds the elements of the break statement.
         */
        BreakStatementView.prototype.render = function (diagramRenderingContext) {
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, diagramRenderingContext);
            // Update model.
            var model = this.getModel();
            model.accept(this);
            // Setting display text.
            this.renderDisplayText(model.getStatement());

            var statementGroup = this.getStatementGroup();

            this._createPropertyPane({
                model: model,
                statementGroup: statementGroup,
                editableProperties: {}
            });

            this._createDebugIndicator({
                statementGroup: statementGroup
            });

            return statementGroup;
        };

        return BreakStatementView;
    });
