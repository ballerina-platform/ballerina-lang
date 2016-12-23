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

            // View options for height and width of the function invocation statement box.
            this._viewOptions.height = _.get(args, "viewOptions.height", 30);
            this._viewOptions.width = _.get(args, "viewOptions.width", 120);
            this.getBoundingBox().fromTopCenter(this._topCenter, this._viewOptions.width, this._viewOptions.height);
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

        FunctionInvocationStatementView.prototype.setDiagramRenderingContext = function(context){
            this._diagramRenderingContext = context;
        };

        /**
         * Renders the view for function invocation statement.
         * @returns {group} - The SVG group which holds the elements of the function invocation statement.
         */
        FunctionInvocationStatementView.prototype.render = function (renderingContext) {
            // TODO : Please revisit this method. Needs a refactor
            this.setDiagramRenderingContext(renderingContext);
            log.info("Rendering the Function Invocation Statement.");

            var funInvokeGroup = D3Utils.group(d3.select(this._container));
            funInvokeGroup.attr("id", "_" + this._model.id);//added attribute 'id' starting with '_' to be compatible with HTML4
            var width = this.getBoundingBox().w();
            var height = this.getBoundingBox().h();
            var x = this.getBoundingBox().getLeft();
            var y = this.getBoundingBox().getTop();

            var funInvokeRect = D3Utils.rect(x, y, width, height, 0, 0, funInvokeGroup).classed('statement-rect', true)
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
            // TODO : Please revisit these calculations.
            var funInvokeText = D3Utils.textElement(x + width / 2, y + height / 2, text, funInvokeGroup).classed('statement-text', true);

            funInvokeGroup.expression_rect = funInvokeRect;
            funInvokeGroup.expression_text = funInvokeText;
            this.setStatementGroup(funInvokeGroup);
            this.listenTo(this._model, 'update-property-text', this.updateStatementText);

            // Creating property pane
            var editableProperties = [
                {
                    propertyType: "text",
                    key: "PackageName",
                    model: this._model,
                    getterMethod: this._model.getPackageName,
                    setterMethod: this._model.setPackageName
                },
                {
                    propertyType: "text",
                    key: "FunctionName",
                    model: this._model,
                    getterMethod: this._model.getFunctionName,
                    setterMethod: this._model.setFunctionName
                },
                {
                    propertyType: "text",
                    key: "Params",
                    model: this._model,
                    getterMethod: this._model.getParams,
                    setterMethod: this._model.setParams
                }
            ];
            this._createPropertyPane({
                model: this._model,
                statementGroup: funInvokeGroup,
                editableProperties: editableProperties
            });

            this.getBoundingBox().on('top-edge-moved', function(dy){
                funInvokeRect.attr('y',  parseFloat(funInvokeRect.attr('y')) + dy);
                funInvokeText.attr('y',  parseFloat(funInvokeText.attr('y')) + dy);
            });
        };

        FunctionInvocationStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        FunctionInvocationStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        FunctionInvocationStatementView.prototype.updateStatementText = function (updatedText) {
            if (!_.isUndefined(updatedText) && updatedText !== '') {
                var text = this._model.getPackageName() + ':' + this._model.getFunctionName() + '(';
                var params = this._model.getParams();
                for (var id = 0; id < params.length; id++) {
                    if (id > 0) {
                        text += ',' + params[id];
                    } else {
                        text += params[id];
                    }
                }
                text += ')';
                this.getStatementGroup().expression_text.node().textContent = text;
            }
        };

        return FunctionInvocationStatementView;
    });