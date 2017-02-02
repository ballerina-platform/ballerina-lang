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
define(['require', 'lodash', 'jquery', 'log', './ballerina-statement-view', './../ast/while-statement', 'd3utils', 'd3', 'ballerina/ast/ballerina-ast-factory'],
    function (require, _, $, log, BallerinaStatementView, WhileStatement, D3Utils, d3, BallerinaASTFactory) {

        /**
         * The view to represent a If statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {WhileStatement} args.model - The If statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent View, which in this case the parent model
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var WhileStatementView = function (args) {
            BallerinaStatementView.call(this, args);
            _.set(this._viewOptions, 'width', _.get(this._viewOptions, 'width', 140));
            _.set(this._viewOptions, 'height', _.get(this._viewOptions, 'height', 100));
            _.set(this._viewOptions, 'contentOffset', _.get(this._viewOptions, 'contentOffset', {top: 10, bottom: 10}));
            // Initialize the bounding box
            this.getBoundingBox().fromTopCenter(this.getTopCenter(),
                _.get(this._viewOptions, 'width'),  _.get(this._viewOptions, 'height'));
            this._statementContainer = undefined;
        };

        WhileStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        WhileStatementView.prototype.constructor = WhileStatementView;

        WhileStatementView.prototype.canVisitWhileStatement = function(){
            return true;
        };

        /**
         * Remove View callback
         * @param {ASTNode} parent - parent node
         * @param {ASTNode} child - child node
         */
        WhileStatementView.prototype.onBeforeModelRemove = function () {
            this._statementContainer.getBoundingBox().off('bottom-edge-moved');
            d3.select("#_" +this._model.id).remove();
            this.getBoundingBox().w(0).h(0);
        };

        /**
         * Override Child remove callback
         * @param {ASTNode} child - removed child
         */
        WhileStatementView.prototype.childRemovedCallback = function (child) {
            if (BallerinaASTFactory.isStatement(child)) {
                this.getStatementContainer().childStatementRemovedCallback(child);
            }
        };

        /**
         * Render the while statement
         */
        WhileStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var whileGroup = D3Utils.group(d3.select(this._container));
            whileGroup.attr("id","_" +this._model.id);
            var self = this;

            var title_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), this.getBoundingBox().w(), 25, 0, 0, whileGroup).classed('statement-title-rect', true);
            var outer_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), this.getBoundingBox().w(),
                this.getBoundingBox().h(), 0, 0, whileGroup).classed('background-empty-rect', true);
            var points = "" + this.getBoundingBox().x() + "," + (parseInt(this.getBoundingBox().y()) + 25) + " " +
                (parseInt(this.getBoundingBox().x()) + 35) + "," + (parseInt(this.getBoundingBox().y()) + 25) + " " +
                (parseInt(this.getBoundingBox().x()) + 45) + "," + this.getBoundingBox().y();
            var title_wrapper_polyline = D3Utils.polyline(points, whileGroup).classed('statement-title-polyline', true);
            var title_text = D3Utils.textElement(this.getBoundingBox().x() + 20, this.getBoundingBox().y() + 12, 'While', whileGroup).classed('statement-text', true);
            whileGroup.outerRect = outer_rect;
            whileGroup.titleRect = title_rect;
            whileGroup.titleText = title_text;
            whileGroup.title_wrapper_polyline = title_wrapper_polyline;
            this.setStatementGroup(whileGroup);

            this.getBoundingBox().on('moved', function(offset){
                outer_rect.attr("y", parseFloat(outer_rect.attr('y')) + offset.dy);
                outer_rect.attr("x", parseFloat(outer_rect.attr('x')) + offset.dx);
                title_rect.attr("y", parseFloat(title_rect.attr('y')) + offset.dy);
                title_rect.attr("x", parseFloat(title_rect.attr('x')) + offset.dx);
                title_text.attr("y", parseFloat(title_text.attr('y')) + offset.dy);
                title_text.attr("x", parseFloat(title_text.attr('x')) + offset.dx);
                var newPolylinePoints = "" + self.getBoundingBox().x() + "," + (parseInt(self.getBoundingBox().y()) + 25) + " " +
                    (parseInt(self.getBoundingBox().x()) + 35) + "," + (parseInt(self.getBoundingBox().y()) + 25) + " " +
                    (parseInt(self.getBoundingBox().x()) + 45) + "," + self.getBoundingBox().y();
                title_wrapper_polyline.attr("points", newPolylinePoints);
            });

            this.getBoundingBox().on('width-changed', function(dw){
                outer_rect.attr("x", parseFloat(outer_rect.attr('x')) - dw/2);
                outer_rect.attr("width", parseFloat(outer_rect.attr('width')) + dw);
                title_rect.attr("x", parseFloat(title_rect.attr('x')) - dw/2);
                title_text.attr("x", parseFloat(title_text.attr('x')) - dw/2);
                var newPolylinePoints = "" + self.getBoundingBox().x() + "," + (parseInt(self.getBoundingBox().y()) + 25) + " " +
                    (parseInt(self.getBoundingBox().x()) + 35) + "," + (parseInt(self.getBoundingBox().y()) + 25) + " " +
                    (parseInt(self.getBoundingBox().x()) + 45) + "," + self.getBoundingBox().y();
                title_wrapper_polyline.attr("points", newPolylinePoints);
            });

            // Creating property pane
            var editableProperty = {
                propertyType: "text",
                key: "Condition",
                model: this._model,
                getterMethod: this._model.getCondition,
                setterMethod: this._model.setCondition
            };
            this._createPropertyPane({
                model: this._model,
                statementGroup:whileGroup,
                editableProperties: editableProperty
            });

            this.getBoundingBox().on('height-changed', function(dh){
                outer_rect.attr("height", parseFloat(outer_rect.attr('height')) + dh);
            });

            this._rootGroup = whileGroup;
            this._statementContainerGroup = D3Utils.group(whileGroup);
            this.renderStatementContainer();
            this._model.accept(this);
            //Removing all the registered 'child-added' event listeners for this model.
            //This is needed because we are not unregistering registered event while the diagram element deletion.
            //Due to that, sometimes we are having two or more view elements listening to the 'child-added' event of same model.
            this._model.off('child-added');
            this._model.on('child-added', function(child){
                this.visit(child);
            }, this);
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        WhileStatementView.prototype.visit = function (statement) {
            var args = {model: statement, container: this._rootGroup.node(), viewOptions: {},
                toolPalette: this.toolPalette, messageManager: this.messageManager, parent: this};
            this._statementContainer.renderStatement(statement, args);
        };

        /**
         * Render statement container
         */
        WhileStatementView.prototype.renderStatementContainer = function(){
            var statementContainerOpts = {};
            _.set(statementContainerOpts, 'model', this._model);
            _.set(statementContainerOpts, 'topCenter', this.getTopCenter().clone().move(0, _.get(this._viewOptions, 'contentOffset.top')));
            var height = _.get(this._viewOptions, 'height') -
                _.get(this._viewOptions, 'contentOffset.top') - _.get(this._viewOptions, 'contentOffset.bottom');
            _.set(statementContainerOpts, 'bottomCenter', this.getTopCenter().clone().move(0, _.get(this._viewOptions, 'height')));
            _.set(statementContainerOpts, 'width', _.get(this._viewOptions, 'width'));
            _.set(statementContainerOpts, 'offset', {top: 40, bottom: 40});
            _.set(statementContainerOpts, 'parent', this);
            _.set(statementContainerOpts, 'container', this._statementContainerGroup.node());
            _.set(statementContainerOpts, 'toolPalette', this.toolPalette);
            var StatementContainer = require('./statement-container');
            this._statementContainer = new StatementContainer(statementContainerOpts);
            this.listenTo(this._statementContainer.getBoundingBox(), 'height-changed', function(dh){
                this.getBoundingBox().h(this.getBoundingBox().h() + dh);
            });
            this.getBoundingBox().on('top-edge-moved', function (dy) {
                this._statementContainer.isOnWholeContainerMove = true;
                this._statementContainer.getBoundingBox().y(this._statementContainer.getBoundingBox().y() + dy);
            }, this);

            this.listenTo(this._statementContainer.getBoundingBox(), 'width-changed', function(dw){
                this.getBoundingBox().w(this.getBoundingBox().w() + dw);
            });

            this._statementContainer.render(this._diagramRenderingContext);
        };

        /**
         * Set the while statement model
         * @param {WhileStatement} model
         */
        WhileStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof WhileStatement) {
                this._model = model;
            } else {
                log.error("While statement definition is undefined or is of different type." + model);
                throw "While statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the while statement
         * @param container
         */
        WhileStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for While statement is undefined." + container);
                throw "Container for While statement is undefined." + container;
            }
        };

        WhileStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        WhileStatementView.prototype.getModel = function () {
            return this._model;
        };

        WhileStatementView.prototype.getContainer = function () {
            return this._container;
        };

        WhileStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * Get the statement container
         * @return {StatementContainer} - statement container
         */
        WhileStatementView.prototype.getStatementContainer = function () {
            return this._statementContainer;
        };

        return WhileStatementView;
    });