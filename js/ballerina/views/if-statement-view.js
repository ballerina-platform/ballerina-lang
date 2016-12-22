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
define(['require', 'lodash', 'jquery', 'log', './ballerina-statement-view', './../ast/if-statement', 'd3utils', 'd3'],
    function (require, _, $, log, BallerinaStatementView, IfStatement, D3Utils, d3) {

        /**
         * The view to represent a If statement which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {IfStatement} args.model - The If statement model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} args.parent - Parent Statement View, which in this case the if-else statement
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var IfStatementView = function (args) {
            BallerinaStatementView.call(this, args);
            _.set(this._viewOptions, 'width', _.get(this._viewOptions, 'width', 140));
            _.set(this._viewOptions, 'height', _.get(this._viewOptions, 'height', 100));
            _.set(this._viewOptions, 'contentOffset', _.get(this._viewOptions, 'contentOffset', {top: 10, bottom: 10}));
            // Initialize the bounding box
            this.getBoundingBox().fromTopCenter(this.getTopCenter(),
                _.get(this._viewOptions, 'width'),  _.get(this._viewOptions, 'height'));
            this.init();
        };

        IfStatementView.prototype = Object.create(BallerinaStatementView.prototype);
        IfStatementView.prototype.constructor = IfStatementView;

        IfStatementView.prototype.init = function () {
        };

        IfStatementView.prototype.canVisitIfStatement = function(){
            return true;
        };

        /**
         * Render the if statement
         */
        IfStatementView.prototype.render = function (diagramRenderingContext) {
            this._diagramRenderingContext = diagramRenderingContext;
            var ifGroup = D3Utils.group(this._container);
            ifGroup.attr("id","_" +this._model.id);

            var outer_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), this.getBoundingBox().w(),
                this.getBoundingBox().h(), 0, 0, ifGroup).classed('statement-rect', true);
            var title_rect = D3Utils.rect(this.getBoundingBox().x(), this.getBoundingBox().y(), 40, 20, 0, 0, ifGroup).classed('statement-rect', true);
            var title_text = D3Utils.textElement(this.getBoundingBox().x() + 20, this.getBoundingBox().y() + 10, 'If', ifGroup).classed('statement-text', true);
            ifGroup.outerRect = outer_rect;
            ifGroup.titleRect = title_rect;
            ifGroup.titleText = title_text;
            this.setStatementGroup(ifGroup);

            this.getBoundingBox().on('moved', function(offset){
                outer_rect.attr("y", parseFloat(outer_rect.attr('y')) + offset.dy);
                outer_rect.attr("x", parseFloat(outer_rect.attr('x')) + offset.dx);
                title_rect.attr("y", parseFloat(title_rect.attr('y')) + offset.dy);
                title_rect.attr("x", parseFloat(title_rect.attr('x')) + offset.dx);
                title_text.attr("y", parseFloat(title_text.attr('y')) + offset.dy);
                title_text.attr("x", parseFloat(title_text.attr('x')) + offset.dx);
            });

            this.getBoundingBox().on('width-changed', function(dw){
                outer_rect.attr("x", parseFloat(outer_rect.attr('x')) - dw/2);
                outer_rect.attr("width", parseFloat(outer_rect.attr('width')) + dw);
                title_rect.attr("x", parseFloat(title_rect.attr('x')) - dw/2);
                title_text.attr("x", parseFloat(title_text.attr('x')) - dw/2);

            });

            this.getBoundingBox().on('height-changed', function(dh){
                outer_rect.attr("height", parseFloat(outer_rect.attr('height')) + dh);
            });

            this._rootGroup = ifGroup;
            this._statementContainerGroup = D3Utils.group(ifGroup);
            this.renderStatementContainer();
            this._model.accept(this);
            this._model.on('child-added', function(child){
                this.visit(child);
            }, this);
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        IfStatementView.prototype.visit = function (statement) {
            var args = {model: statement, container: this._rootGroup.node(), viewOptions: {},
                toolPalette: this.toolPalette, messageManager: this.messageManager, parent: this};
            this._statementContainer.renderStatement(statement, args);
        };

        /**
         * Render statement container
         */
        IfStatementView.prototype.renderStatementContainer = function(){
            var statementContainerOpts = {};
            _.set(statementContainerOpts, 'model', this._model);
            _.set(statementContainerOpts, 'topCenter', this.getTopCenter().clone().move(0, _.get(this._viewOptions, 'contentOffset.top')));
            var height = _.get(this._viewOptions, 'height') -
                _.get(this._viewOptions, 'contentOffset.top') - _.get(this._viewOptions, 'contentOffset.bottom');
            _.set(statementContainerOpts, 'bottomCenter', this.getTopCenter().clone().move(0, height));
            _.set(statementContainerOpts, 'width', 120);
            _.set(statementContainerOpts, 'offset', {top: 40, bottom: 40});
            _.set(statementContainerOpts, 'parent', this);
            _.set(statementContainerOpts, 'container', this._statementContainerGroup.node());
            _.set(statementContainerOpts, 'toolPalette', this.toolPalette);
            var StatementContainer = require('./statement-container');
            this._statementContainer = new StatementContainer(statementContainerOpts);
            this.listenTo(this._statementContainer.getBoundingBox(), 'height-changed', function(dh){
                if(this.getBoundingBox().getBottom() < this._statementContainer.getBoundingBox().getBottom()){
                    this.getBoundingBox().h(this.getBoundingBox().h() + dh);
                }
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
         * Set the if statement model
         * @param {IfStatement} model
         */
        IfStatementView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof IfStatement) {
                this._model = model;
            } else {
                log.error("If Else statement definition is undefined or is of different type." + model);
                throw "If Else statement definition is undefined or is of different type." + model;
            }
        };

        /**
         * Set the container to draw the if statement
         * @param container
         */
        IfStatementView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for If Else statement is undefined." + container);
                throw "Container for If Else statement is undefined." + container;
            }
        };

        IfStatementView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        IfStatementView.prototype.getModel = function () {
            return this._model;
        };

        IfStatementView.prototype.getContainer = function () {
            return this._container;
        };

        IfStatementView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        return IfStatementView;
    });