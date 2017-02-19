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
define(['require', 'lodash', 'log', 'property_pane_utils', './compound-statement-view', './../ast/if-else-statement', './../ast/if-statement'],
    function (require, _, log, PropertyPaneUtils, CompoundStatementView, IfElseStatement, IfStatement) {

        /**
         * The view to represent a If Else statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {IfElseStatement} args.model - The If Else statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent View (Resource, Worker, etc)
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @class IfElseStatementView
         * @constructor
         * @extends CompoundStatementView
         */
        var IfElseStatementView = function (args) {
            CompoundStatementView.call(this, args);

            this._ifBlockView = undefined;
            this._elseIfViews = [];
            this._elseBlockView = undefined;
            this._totalHeight = 0;
            this.getModel()._isChildOfWorker = args.isChildOfWorker;

            if (_.isNil(this._model) || !(this._model instanceof IfElseStatement)) {
                log.error("If Else statement definition is undefined or is of different type." + this._model);
                throw "If Else statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for If Else statement is undefined." + this._container);
                throw "Container for If Else statement is undefined." + this._container;
            }
        };

        IfElseStatementView.prototype = Object.create(CompoundStatementView.prototype);
        IfElseStatementView.prototype.constructor = IfElseStatementView;

        IfElseStatementView.prototype.canVisitIfElseStatement = function(){
            return true;
        };

        /**
         * Visit If Statement
         * @param {IfStatement} statement
         */
        IfElseStatementView.prototype.visitIfStatement = function(statement){
            this._ifBlockView = this.visitChildStatement(statement);
        };

        /**
         * Visit Else If Statement
         * @param {ElseIfStatement} statement
         */
        IfElseStatementView.prototype.visitElseIfStatement = function(statement){
            var childStatementView = this.visitChildStatement(statement);
            this._elseIfViews.push(childStatementView);
        };

        /**
         * Visit Else Statement
         * @param {ElseStatement} statement
         */
        IfElseStatementView.prototype.visitElseStatement = function(statement){
            this._elseBlockView = this.visitChildStatement(statement);
        };

        /**
         * Render the svg group to draw the if and the else statements
         */
        IfElseStatementView.prototype.render = function (diagramRenderingContext) {
            // Calling super render.
            (this.__proto__.__proto__).render.call(this, diagramRenderingContext);

            var editableProperty = {};
            _.forEach(this.getModel().getChildren(), function(child, index){
                if (child instanceof IfStatement) {
                    editableProperty = {
                        propertyType: "text",
                        key: "If condition",
                        model: child,
                        getterMethod: child.getCondition,
                        setterMethod: child.setCondition
                    };
                } else if(child instanceof IfElseStatement) {
                    editableProperty = {
                        propertyType: "text",
                        key: "Else If condition",
                        model: child,
                        getterMethod: child.getCondition,
                        setterMethod: child.setCondition
                    };
                }
            });
            // Creating property pane
            this._createPropertyPane({
                model: this.getModel(),
                statementGroup: this.getStatementGroup(),
                editableProperties: editableProperty
            });

            this._createDebugIndicator({
                statementGroup: this.getStatementGroup()
            });
        };

        /**
         * Set the IfElseStatement model
         * @param {IfElseStatement} model
         */
        IfElseStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof IfElseStatement) {
                (this.__proto__.__proto__).setModel(model);
            } else {
                log.error("If Else statement definition is undefined or is of different type." + model);
                throw "If Else statement definition is undefined or is of different type." + model;
            }
        };

        IfElseStatementView.prototype.getIfBlockView = function () {
            return this._ifBlockView;
        };

        IfElseStatementView.prototype.getElseBlockView = function () {
            return this._elseBlockView;
        };

        IfElseStatementView.prototype.getElseIfViews = function () {
            return this._elseIfViews;
        };

        IfElseStatementView.prototype.getLastElseIf = function () {
            return this._elseIfViews[this._elseIfViews.length - 1];
        };

        IfElseStatementView.prototype.onBeforeModelRemove = function () {
            this.removeAllChildStatements();
        };

        return IfElseStatementView;
    });