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
define(['require', 'lodash', 'log', 'property_pane_utils', './ballerina-statement-view', './../ast/if-else-statement', 'd3utils', 'd3', 'jquery', './../ast/if-statement', './../ast/if-else-statement'],
    function (require, _, log, PropertyPaneUtils, BallerinaStatementView, IfElseStatement, D3Utils, d3, $, IfStatement, IfElseStatement) {

        /**
         * The view to represent a If Else statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {IfElseStatement} args.model - The If Else statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent View (Resource, Worker, etc)
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var IfElseStatementView = function (args) {

            BallerinaStatementView.call(this, args);

            this._ifBlockView = undefined;
            this._elseIfViews = [];
            this._elseBlockView = undefined;
            this._totalHeight = 0;

            if (_.isNil(this._model) || !(this._model instanceof IfElseStatement)) {
                log.error("If Else statement definition is undefined or is of different type." + this._model);
                throw "If Else statement definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for If Else statement is undefined." + this._container);
                throw "Container for If Else statement is undefined." + this._container;
            }

            this.init();

        };

        IfElseStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        IfElseStatementView.prototype.constructor = IfElseStatementView;

        IfElseStatementView.prototype.canVisitStatement = function(){
            return true;
        };

        IfElseStatementView.prototype.init = function () {
            //Registering event listeners
            // this.listenTo(this._parentView, 'childViewAddedEvent', this.childViewAddedCallback);
        };

        /**
         * Visit If Statement
         * @param {IfStatement} statement
         */
        IfElseStatementView.prototype.visitIfStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._ifBlockView = statementView;
            this._diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            statementView.render(this._diagramRenderingContext);

            //adjust if-else statement's height
            // this._totalHeight = this._totalHeight + statementView.getBoundingBox().height;
            // this.setIfElseStatementHeight(this._totalHeight);
            this.trigger("childViewAddedEvent", statement);
        };

        /**
         * Visit If Else Statement
         * @param {IfElseStatement} statement
         */
        IfElseStatementView.prototype.visitElseIfStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            statementView.render(this._diagramRenderingContext);

            //adjust if-else statement's height
            this._totalHeight = this._totalHeight + statementView.getBoundingBox().height;
            this.setIfElseStatementHeight(this._totalHeight);
            this.trigger("childViewAddedEvent", statement);
        };

        /**
         * Visit Else Statement
         * @param {ElseStatement} statement
         */
        IfElseStatementView.prototype.visitElseStatement = function(statement){
            var StatementViewFactory = require('./statement-view-factory');
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this.getStatementGroup(), viewOptions: undefined, parent: this};
            var statementView = statementViewFactory.getStatementView(args);
            this._elseBlockView = statementView;
            this._diagramRenderingContext.getViewModelMap()[statement.id] = statementView;
            statementView.render(this._diagramRenderingContext);

            //adjust if-else statement's height
            this._totalHeight = this._totalHeight + statementView.getBoundingBox().height;
            this.setIfElseStatementHeight(this._totalHeight);
            this.trigger("childViewAddedEvent", statement);
        };

        /**
         * Render the svg group to draw the if and the else statements
         */
        IfElseStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var ifElseGroup = D3Utils.group(d3.select(this._container));
            this.setStatementGroup(ifElseGroup);
            this._model.accept(this);

            var editableProperties = [];
            _.forEach(this._model.getChildren(), function(child, index){
                var editableProperty = {};
                if (child instanceof IfStatement) {
                    editableProperty = {
                        propertyType: "text",
                        key: "If condition",
                        model: child,
                        getterMethod: child.getCondition,
                        setterMethod: child.setCondition
                    };

                    editableProperties.push(editableProperty);
                } else if(child instanceof IfElseStatement) {
                    editableProperty = {
                        propertyType: "text",
                        key: "Else If condition",
                        model: child,
                        getterMethod: child.getCondition,
                        setterMethod: child.setCondition
                    };

                    editableProperties.push(editableProperty);
                }
            });
            // Creating property pane
            this._createPropertyPane({
                statementGroup:ifElseGroup,
                editableProperties: editableProperties
            });
        };

        /**
         * Set the IfElseStatement model
         * @param {IfElseStatement} model
         */
        IfElseStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof IfElseStatement) {
                this._model = model;
            } else {
                log.error("If Else statement definition is undefined or is of different type." + model);
                throw "If Else statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the if else group
         * @param container
         */
        IfElseStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for If Else statement is undefined." + container);
                throw "Container for If Else statement is undefined." + container;
            }
        };

        IfElseStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };


        /**
         * setting the height for the bounding box
         * @param height
         */
        IfElseStatementView.prototype.setIfElseStatementHeight = function (height){
            // this.setBoundingBox(this.getBoundingBox().width, height, this.getBoundingBox().x, this.getBoundingBox().y);
        };

        /**
         * @returns {_model}
         */
        IfElseStatementView.prototype.getModel = function () {
            return this._model;
        };

        IfElseStatementView.prototype.getContainer = function () {
            return this._container;
        };

        IfElseStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        IfElseStatementView.prototype.setIfBlockView = function (ifBlockView) {
            this._ifBlockView = ifBlockView;
        };

        IfElseStatementView.prototype.setElseBlockView = function (elseBlockView) {
            this._elseBlockView = elseBlockView;
        };

        IfElseStatementView.prototype.getIfBlockView = function () {
            return this._ifBlockView;
        };

        IfElseStatementView.prototype.getElseBlockView = function () {
            return this._elseBlockView;
        };

        IfElseStatementView.prototype.getElseIfViewList = function () {
            return this._elseIfViews;
        };

        IfElseStatementView.prototype.getLastElseIf = function () {
            return this._elseIfViews[this._elseIfViews.length - 1];
        };

        IfElseStatementView.prototype.changeChildrenMetrics = function (childBoundingBox) {
            var currentX = this.getBoundingBox().x;
            var currentY = this.getBoundingBox().y;
            var currentWidth = this.getBoundingBox().width;
            var currentHeight = this.getBoundingBox().height;

            if (childBoundingBox.x <= this.getBoundingBox().x) {
                this.setBoundingBox(currentWidth + 20, currentHeight, currentX - 10, currentY);
            }
            this.trigger("changeStatementMetricsEvent", childBoundingBox);
        };


        return IfElseStatementView;
    });