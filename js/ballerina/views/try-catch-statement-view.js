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
define(['require', 'lodash', 'log', './ballerina-statement-view', './../ast/trycatch-statement', 'd3utils', 'd3'],
    function (require, _, log, BallerinaStatementView, TryCatchStatement, D3Utils, d3) {

        /**
         * The view to represent a Try Catch statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {TryCatchStatement} args.model - The Try Catch statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent View (Resource, Worker, etc)
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var TryCatchStatementView = function (args) {
            this._model = _.get(args, "model");
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});
            this._tryBlockView = undefined;
            this._catchBlockView = undefined;

            if (_.isNil(this._model) || !(this._model instanceof TryCatchStatement)) {
                log.error("Try Catch statement definition is undefined or is of different type." + this._model);
                throw "Try Catch statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for Try Catch statement is undefined." + this._container);
                throw "Container for Try Catch statement is undefined." + this._container;
            }

            BallerinaStatementView.call(this);
        };

        TryCatchStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        TryCatchStatementView.prototype.constructor = TryCatchStatementView;

        TryCatchStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        /**
         * Visit Try Statement
         * @param {TryStatement} statement
         */
        TryCatchStatementView.prototype.visitTryStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._tryBlockView = statementView;
            statementView.render();
        };

        /**
         * Visit Catch Statement
         * @param {CatchStatement} statement
         */
        TryCatchStatementView.prototype.visitCatchStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._catchBlockView = statementView;
            statementView.render();
        };

        /**
         * Render the svg group to draw the try and the catch statements
         */
        TryCatchStatementView.prototype.render = function () {
            var tryCatchGroup = D3Utils.group(d3.select(this._container));
            this.setStatementGroup(tryCatchGroup);
            this._model.accept(this);
        };

        /**
         * Set the TryCatchStatement model
         * @param {TryCatchStatement} model
         */
        TryCatchStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof TryCatchStatement) {
                this._model = model;
            } else {
                log.error("Try Catch statement definition is undefined or is of different type." + model);
                throw "Try Catch statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the try catch group
         * @param container
         */
        TryCatchStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for Try Catch statement is undefined." + container);
                throw "Container for Try Catch statement is undefined." + container;
            }
        };

        TryCatchStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        /**
         * @returns {_model}
         */
        TryCatchStatementView.prototype.getModel = function () {
            return this._model;
        };

        TryCatchStatementView.prototype.getContainer = function () {
            return this._container;
        };

        TryCatchStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        TryCatchStatementView.prototype.setTryBlockView = function (tryBlockView) {
            this._tryBlockView = tryBlockView;
        };

        TryCatchStatementView.prototype.setTryBlockView = function (catchBlockView) {
            this._catchBlockView = catchBlockView;
        };

        TryCatchStatementView.prototype.getTryBlockView = function () {
            return this._tryBlockView;
        };

        TryCatchStatementView.prototype.getCatchBlockView = function () {
            return this._catchBlockView;
        };

        return TryCatchStatementView;
    });