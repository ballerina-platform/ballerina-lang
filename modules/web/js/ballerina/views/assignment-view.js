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
define(['lodash', 'log', './simple-statement-view', './../ast/assignment', 'd3utils', 'd3'],
    function (_, log, SimpleStatementView, AssignmentStatement, D3Utils, d3) {

        /**
         * The view to represent a assignment definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {Assignment} args.model - The assignment statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class AssignmentStatementView
         * @constructor
         * @extends SimpleStatementView
         */
        var AssignmentStatementView = function (args) {
            SimpleStatementView.call(this, args);

            if (_.isNil(this._container)) {
                log.error("Container for Assignment statement is undefined." + this._container);
                throw "Container for Assignment statement is undefined." + this._container;
            }
        };

        AssignmentStatementView.prototype = Object.create(SimpleStatementView.prototype);
        AssignmentStatementView.prototype.constructor = AssignmentStatementView;

        AssignmentStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        AssignmentStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof AssignmentStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("Assignment statement undefined or is of different type." + model);
                throw "Assignment statement undefined or is of different type." + model;
            }
        };

        /**
         * Renders the view for assignment statement.
         * @returns {group} - The SVG group which holds the elements of the assignment statement.
         */
        AssignmentStatementView.prototype.render = function (diagramRenderingContext) {
            // Calling super class's render function.
            (this.__proto__.__proto__).render.call(this, diagramRenderingContext);
            // Update model.
            var model = this.getModel();
            model.accept(this);
            // Setting display text.
            this.renderDisplayText(model.getStatementString());

            var statementGroup = this.getStatementGroup();
            // Creating property pane.
            var editableProperty = {
                propertyType: "text",
                key: "Assignment",
                model: model,
                getterMethod: model.getStatementString,
                setterMethod: model.setStatementString
            };
            this._createPropertyPane({
                                         model: model,
                                         statementGroup: statementGroup,
                                         editableProperties: editableProperty
                                     });
            this.listenTo(model, 'update-property-text', this.updateStatementText);

            this._createDebugIndicator({
                statementGroup: statementGroup
            });

            return statementGroup;
        };

        AssignmentStatementView.prototype.updateStatementText = function (newStatementText, propertyKey) {
            this._model.setStatementString(newStatementText);
            var displayText = this._model.getStatementString();
            this.renderDisplayText(displayText);
        };

        return AssignmentStatementView;
    });
