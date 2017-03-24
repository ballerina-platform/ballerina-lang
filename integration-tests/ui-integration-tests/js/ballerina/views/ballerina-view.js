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
define(['lodash', 'log', 'jquery', 'd3', 'd3utils', './../visitors/ast-visitor', './bounding-box'],
    function (_, log, $, d3, D3Utils, ASTVisitor, BBox) {

        /**
         * An abstract class which consists functions of moving or resizing views.
         * @param {Object} args - Arguments for creating the view.
         * @param {ASTNode} args.model - Any ASTNode as the model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @param {ToolPalette} args.toolPalette - reference for tool palette
         * @param {DiagramRenderContext} args.diagramRenderingContext - Diagram rendering context for the view.
         * @constructor
         * @augments ASTVisitor
         */
        var BallerinaView = function (args) {
            ASTVisitor.call(this, args);
            this._parent = _.get(args, "parent");
            this.setModel(_.get(args, "model"));
            this._container = _.get(args, "container");
            this._viewOptions = _.get(args, "viewOptions", {});
            this._backendEndpointsOptions = _.get(args, "backendEndpointsOptions", {});
            this._boundingBox = new BBox();
            this.toolPalette = _.get(args, "toolPalette");
            this.messageManager =  _.get(args, "messageManager");
            this.diagramRenderingContext = _.get(args, "diagramRenderContext");
            this.id = uuid();
        };

        BallerinaView.prototype = Object.create(ASTVisitor.prototype);
        BallerinaView.prototype.constructor = BallerinaView;

        BallerinaView.prototype.setParent = function (parent) {
            this._parent = parent;
        };

        BallerinaView.prototype.getParent = function () {
            return this._parent;
        };

        BallerinaView.prototype.setModel = function (model) {
            this._model = model;
        };

        BallerinaView.prototype.getModel = function () {
            return this._model;
        };

        BallerinaView.prototype.setContainer = function (container) {
            this._container = container;
        };

        BallerinaView.prototype.getContainer = function () {
            return this._container;
        };

        BallerinaView.prototype.getBoundingBox = function () {
            return this._boundingBox;
        };

        BallerinaView.prototype.setToolPalette = function (toolPalette) {
            this.toolPalette = toolPalette;
        };

        BallerinaView.prototype.getToolPalette = function () {
            return this.toolPalette;
        };

        BallerinaView.prototype.setMessageManager = function (messageManager) {
            this.messageManager = messageManager;
        };

        BallerinaView.prototype.getMessageManager = function () {
            return this.messageManager;
        };

        BallerinaView.prototype.setDiagramRenderingContext = function (diagramRenderContext) {
            this.diagramRenderingContext = diagramRenderContext;
        };

        BallerinaView.prototype.getDiagramRenderingContext = function () {
            return this.diagramRenderingContext;
        };

        /**
         * Renders/Draws the view for a specific model(i.e {@link BallerinaView#_model}).
         * @abstract
         */
        BallerinaView.prototype.render = function() {
            throw "Method not implemented";
        };

        // Auto generated Id for service definitions (for accordion views)
        var uuid =  function (){
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }
            return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
                s4() + '-' + s4() + s4() + s4();
        };

        return BallerinaView;
    });