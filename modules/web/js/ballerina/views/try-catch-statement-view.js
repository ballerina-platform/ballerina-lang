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
define(['require', 'lodash', 'log', './compound-statement-view', './../ast/trycatch-statement', './../ast/catch-statement', './point'],
    function (require, _, log, CompoundStatementView, TryCatchStatement, CatchStatement, Point) {

        /**
         * The view to represent a Try Catch statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {TryCatchStatement} args.model - The Try Catch statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent View (Resource, Worker, etc)
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class TryCatchStatementView
         * @constructor
         * @extends CompoundStatementView
         */
        var TryCatchStatementView = function (args) {
            CompoundStatementView.call(this, args);

            this._tryBlockView = undefined;
            this._catchBlockView = undefined;
            this.getModel()._isChildOfWorker = args.isChildOfWorker;

            if (_.isNil(this._model) || !(this._model instanceof TryCatchStatement)) {
                log.error("Try Catch statement definition is undefined or is of different type." + this._model);
                throw "Try Catch statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for Try Catch statement is undefined." + this._container);
                throw "Container for Try Catch statement is undefined." + this._container;
            }
        };

        TryCatchStatementView.prototype = Object.create(CompoundStatementView.prototype);
        TryCatchStatementView.prototype.constructor = TryCatchStatementView;

        TryCatchStatementView.prototype.canVisitTryCatchStatement = function(){
            return true;
        };

        /**
         * Visit Try Statement
         * @param {TryStatement} statement
         */
        TryCatchStatementView.prototype.visitTryStatement = function(statement){
            this._tryBlockView = this.visitChildStatement(statement);
        };

        /**
         * Visit Catch Statement
         * @param {CatchStatement} statement
         */
        TryCatchStatementView.prototype.visitCatchStatement = function(statement){
            this._catchBlockView = this.visitChildStatement(statement);
        };

        TryCatchStatementView.prototype.render = function (diagramRenderingContext) {
            // Calling super render.
            (this.__proto__.__proto__).render.call(this, diagramRenderingContext);

            // Creating property pane
            var model = this.getModel();
            var editableProperty = {};
            _.forEach(model.getChildren(), function (childStatement, index) {
                if (childStatement instanceof CatchStatement) {
                    editableProperty = {
                        propertyType: "text",
                        key: "Catch parameter",
                        model: childStatement,
                        getterMethod: childStatement.getParameter
                    };
                    return false;
                }
            });
            this._createPropertyPane({
                                         model: model,
                                         statementGroup: this.getStatementGroup(),
                                         editableProperties: editableProperty
                                     });
            this._createDebugIndicator({
                                           statementGroup: this.getStatementGroup()
                                       });
        };

        /**
         * Set the TryCatchStatement model
         * @param {TryCatchStatement} model
         */
        TryCatchStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof TryCatchStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("Try Catch statement definition is undefined or is of different type." + model);
                throw "Try Catch statement definition is undefined or is of different type." + model;
            }
        };

        TryCatchStatementView.prototype.getTryBlockView = function () {
            return this._tryBlockView;
        };

        TryCatchStatementView.prototype.getCatchBlockView = function () {
            return this._catchBlockView;
        };

        TryCatchStatementView.prototype.onBeforeModelRemove = function () {
            this.removeAllChildStatements();
        };

        TryCatchStatementView.prototype._getEditPaneLocation = function () {
            var statementBoundingBox = this.getCatchBlockView().getBoundingBox();
            return new Point((statementBoundingBox.x()), (statementBoundingBox.y() - 1));
        };

        return TryCatchStatementView;
    });