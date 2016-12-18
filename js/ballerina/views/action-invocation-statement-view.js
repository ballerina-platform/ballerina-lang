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
define(['lodash', 'd3','log', './ballerina-statement-view', './../ast/action-invocation-statement','./point', 'd3utils'],
    function (_, d3, log, BallerinaStatementView, ActionInvocationStatement, Point, D3Utils) {

        var ActionInvocationStatement = function (args) {
            BallerinaStatementView.call(this, args);
            this._connectorView = {};

            if (_.isNil(this._model) || !(this._model.getFactory().isActionInvocationStatement(this._model))) {
                log.error("Action invocation statement definition is undefined or is of different type." + this._model);
                throw "Action invocation statement is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for action statement is undefined." + this._container);
                throw "Container for action statement is undefined." + this._container;
            }

        };

        ActionInvocationStatement.prototype = Object.create(BallerinaStatementView.prototype);
        ActionInvocationStatement.prototype.constructor = ActionInvocationStatement;

        ActionInvocationStatement.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ActionInvocationStatement) {
                this._model = model;
            } else {
                log.error("Action statement definition is undefined or is of different type." + model);
                throw "Action statement definition is undefined or is of different type." + model;
            }
        };

        ActionInvocationStatement.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for action statement is undefined." + container);
                throw "Container for action statement is undefined." + container;
            }
        };

        ActionInvocationStatement.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ActionInvocationStatement.prototype.getModel = function () {
            return this._model;
        };

        ActionInvocationStatement.prototype.getContainer = function () {
            return this._container;
        };

        ActionInvocationStatement.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        ActionInvocationStatement.prototype.setConnectorView = function(view){
            this._connectorView = view;
        };
        ActionInvocationStatement.prototype.getConnectorView = function(){
            return this._connectorView;
        };

        /**
         * Rendering the view for get-Action statement.
         * @returns {group} The svg group which contains the elements of the action statement view.
         */
        ActionInvocationStatement.prototype.render = function () {
            var actionStatementGroup = D3Utils.group(d3.select(this._container));
            actionStatementGroup.attr("id","_" +this._model.id);
            log.info("Rendering the Get Action Statement.");

        };

        return ActionInvocationStatement;
    });