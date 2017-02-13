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
define(['lodash', 'log', './ballerina-statement-view', './../ast/variable-definition-statement', 'd3utils', 'd3'],
    function (_, log, BallerinaStatementView, VariableDefinitionStatement, D3Utils, d3) {

        /**
         * The view to represent a assignment definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {VariableDefinitionStatement} args.model - The variable definition statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var VariableDefinitionStatementView = function (args) {
            BallerinaStatementView.call(this, args);

            if (_.isNil(this._container)) {
                log.error("Container for Variable Definition statement is undefined." + this._container);
                throw "Container for Variable Definition statement is undefined." + this._container;
            }

            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
        };

        VariableDefinitionStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        VariableDefinitionStatementView.prototype.constructor = VariableDefinitionStatementView;

        VariableDefinitionStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        VariableDefinitionStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof VariableDefinitionStatement) {
                this._model = model;
            } else {
                log.error("Variable Definition statement undefined or is of different type." + model);
                throw "Variable Definition statement undefined or is of different type." + model;
            }
        };

        VariableDefinitionStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for Variable Definition statement is undefined." + container);
                throw "Container for Variable Definition statement is undefined." + container;
            }
        };

        VariableDefinitionStatementView.prototype.getModel = function () {
            return this._model;
        };

        VariableDefinitionStatementView.prototype.getContainer = function () {
            return this._container;
        };

        /**
         * Renders the view for Variable Definition statement.
         * @returns {group} - The SVG group which holds the elements of the Variable Definition statement.
         */
        VariableDefinitionStatementView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);
            this.setStatementGroup(D3Utils.group(d3.select(this._container)));
            this.getStatementGroup().attr("id","_" +this._model.id);//added attribute 'id' starting with '_' to be compatible with HTML4
            var width = this.getBoundingBox().w();
            var height = this.getBoundingBox().h();

            var x = this.getBoundingBox().getLeft();
            var y = this.getBoundingBox().getTop();

            var expressionRect = D3Utils.rect(x, y, width, height, 0, 0, this.getStatementGroup()).classed('statement-rect', true);
            var assignmentText = this._model.getVariableDefinitionStatementString();
            assignmentText = ((assignmentText.length) > 11 ? (assignmentText.substring(0,11) + '...') : assignmentText);
            var expressionText = D3Utils.textElement(x + width/2, y + height/2, assignmentText, this.getStatementGroup()).classed('statement-text', true);
            this.getStatementGroup().expression_rect = expressionRect;
            this.getStatementGroup().expression_text = expressionText;
            this.setStatementGroup(this.getStatementGroup());
            this.listenTo(this._model, 'update-property-text', this.updateStatementText);
            this._model.accept(this);

            // Creating property pane
            var editableProperties = [];
            var editableProperty = {
                propertyType: "text",
                key: "VariableDefinition",
                model: this._model,
                getterMethod: this._model.getVariableDefinitionStatementString,
                setterMethod: this._model.setVariableDefinitionStatementString
            };
            editableProperties.push(editableProperty);
            this._createPropertyPane({
                model: this._model,
                statementGroup:this.getStatementGroup(),
                editableProperties: editableProperty
            });

            this._createDebugIndicator({
                model: this._model,
                statementGroup: this.getStatementGroup()
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                expressionRect.attr('y',  parseFloat(expressionRect.attr('y')) + dy);
                expressionText.attr('y',  parseFloat(expressionText.attr('y')) + dy);
            });
        };

        VariableDefinitionStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        VariableDefinitionStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        VariableDefinitionStatementView.prototype.updateStatementText = function (updatedText) {
            if (!_.isUndefined(updatedText) && updatedText !== '') {
                // Updating variable definition statement model.
                this.getModel().setVariableDefinitionStatementString(updatedText);

                updatedText = ((updatedText.length) > 11 ? (updatedText.substring(0, 11) + '..') : updatedText);
                this.getStatementGroup().expression_text.node().textContent = updatedText;
            }
        };

        return VariableDefinitionStatementView;
    });
