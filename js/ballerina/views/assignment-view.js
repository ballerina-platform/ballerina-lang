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
define(['lodash', 'log', './ballerina-statement-view', './../ast/assignment', 'd3utils', 'd3'],
    function (_, log, BallerinaStatementView, AssignmentStatement, D3Utils, d3) {

        /**
         * The view to represent a assignment definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {Assignment} args.model - The assignment statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var AssignmentStatementView = function (args) {
            BallerinaStatementView.call(this, args);

            if (_.isNil(this._model) || !(this._model instanceof AssignmentStatement)) {
                log.error("Assignment statement undefined or is of different type." + this._model);
                throw "Assignment statement undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for Assignment statement is undefined." + this._container);
                throw "Container for Assignment statement is undefined." + this._container;
            }

            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
        };

        AssignmentStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        AssignmentStatementView.prototype.constructor = AssignmentStatementView;

        AssignmentStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        AssignmentStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof AssignmentStatement) {
                this._model = model;
            } else {
                log.error("Assignment statement undefined or is of different type." + model);
                throw "Assignment statement undefined or is of different type." + model;
            }
        };

        AssignmentStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for assignment statement is undefined." + container);
                throw "Container for assignment statement is undefined." + container;
            }
        };

        AssignmentStatementView.prototype.getModel = function () {
            return this._model;
        };

        AssignmentStatementView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Renders the view for assignment statement.
         * @returns {group} - The SVG group which holds the elements of the assignment statement.
         */
        AssignmentStatementView.prototype.render = function () {
            var assignmentStatementGroup = D3Utils.group(d3.select(this._container));
            assignmentStatementGroup.attr("id","_" +this._model.id);//added attribute 'id' starting with '_' to be compatible with HTML4
            var width = this.getBoundingBox().w();
            var height = this.getBoundingBox().h();

            var x = this.getBoundingBox().getLeft();
            var y = this.getBoundingBox().getTop();

            var expressionRect = D3Utils.rect(x, y, width, height, 0, 0, assignmentStatementGroup).classed('statement-rect', true);
            var assignmentText = this._model.getVariableAccessor() + ' = ' +this._model.getExpression();
            var expressionText = D3Utils.textElement(x + width/2, y + height/2, assignmentText, assignmentStatementGroup).classed('statement-text', true);
            assignmentStatementGroup.expression_rect = expressionRect;
            assignmentStatementGroup.expression_text = expressionText;
            this.setStatementGroup(assignmentStatementGroup);
            this.listenTo(this._model, 'update-statement-text', this.updateStatementText);
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
                statementGroup:assignmentStatementGroup,
                editableProperties: editableProperties
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                expressionRect.attr('y',  parseFloat(expressionRect.attr('y')) + dy);
                expressionText.attr('y',  parseFloat(expressionText.attr('y')) + dy);
            });
        };

        AssignmentStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        AssignmentStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        AssignmentStatementView.prototype.updateStatementText = function (updatedText) {
            if (!_.isUndefined(updatedText) && updatedText !== '') {
                this.getStatementGroup().expression_text.node().textContent = updatedText;
            }
        };

        return AssignmentStatementView;
    });