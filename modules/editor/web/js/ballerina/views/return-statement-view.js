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
define(['lodash', 'log', './../ast/return-statement', './ballerina-statement-view', 'd3utils', 'd3'],
    function (_, log, ReturnStatement, BallerinaStatementView, D3Utils, d3) {

        /**
         * The view to represent a return statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ReturnStatement} args.model - The return statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ReturnStatementView = function (args) {
            BallerinaStatementView.call(this, args);
            if (_.isNil(this._model) || !(this._model instanceof ReturnStatement)) {
                log.error("Return statement definition is undefined or is of different type." + this._model);
                throw "Return statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for return statement is undefined." + this._container);
                throw "Container for return statement is undefined." + this._container;
            }

            // View options for height and width of the assignment statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
        };

        ReturnStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        ReturnStatementView.prototype.constructor = ReturnStatementView;

        ReturnStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ReturnStatement) {
                this._model = model;
            } else {
                log.error("Return statement definition is undefined or is of different type." + model);
                throw "Return statement definition is undefined or is of different type." + model;
            }
        };

        ReturnStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for return statement is undefined." + container);
                throw "Container for return statement is undefined." + container;
            }
        };

        ReturnStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ReturnStatementView.prototype.getModel = function () {
            return this._model;
        };

        ReturnStatementView.prototype.getContainer = function () {
            return this._container;
        };

        ReturnStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Rendering the view of the return statement.
         * @returns {Object} - The svg group which the return statement view resides in.
         */
        ReturnStatementView.prototype.render = function (diagramRenderingContext) {
            this.setDiagramRenderingContext(diagramRenderingContext);
            var returnStatementGroup = D3Utils.group(d3.select(this._container));
            returnStatementGroup.attr("id","_" +this._model.id);//added attribute 'id' starting with '_' to be compatible with HTML4
            var width = this.getBoundingBox().w();
            var height = this.getBoundingBox().h();

            var x = this.getBoundingBox().getLeft();
            var y = this.getBoundingBox().getTop();

            var expressionRect = D3Utils.rect(x, y, width, height, 0, 0, returnStatementGroup).classed('statement-rect', true);
            var text = this._model.getReturnExpression();
            text = ((text.length) > 11 ? (text.substring(0,11) + '...') : text);
            var expressionText = D3Utils.textElement(x + width/2, y + height/2, text, returnStatementGroup).classed('statement-text', true);
            returnStatementGroup.expression_rect = expressionRect;
            returnStatementGroup.expression_text = expressionText;
            this.setStatementGroup(returnStatementGroup);
            this.listenTo(this._model, 'update-property-text', this.updateStatementText);
            this._model.accept(this);

            // Creating property pane
            var editableProperty = {
                propertyType: "text",
                key: "Expression",
                model: this._model,
                getterMethod: this._model.getReturnExpression,
                setterMethod: this._model.setReturnExpression
            };
            this._createPropertyPane({
                model: this._model,
                statementGroup:returnStatementGroup,
                editableProperties: editableProperty
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                expressionRect.attr('y',  parseFloat(expressionRect.attr('y')) + dy);
                expressionText.attr('y',  parseFloat(expressionText.attr('y')) + dy);
            });
        };

        ReturnStatementView.prototype.updateStatementText = function (updatedText) {
            if (!_.isUndefined(updatedText) && updatedText !== '') {
                updatedText = ((updatedText.length) > 11 ? (updatedText.substring(0, 11) + '..') : updatedText);
                this.getStatementGroup().expression_text.node().textContent = updatedText;
            }
        };

        return ReturnStatementView;
    });