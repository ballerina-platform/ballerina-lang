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
define(['lodash', 'log', 'd3', 'jquery', 'd3utils', './ballerina-view', './../ast/resource-definition',
        './default-worker', './point', './connector-declaration-view', './statement-view-factory',
        'ballerina/ast/ballerina-ast-factory', './expression-view-factory','./message', './statement-container',
        './../ast/variable-declaration', './variables-view', './client-life-line', './annotation-view',
        './arguments-view'],
    function (_, log, d3, $, D3utils, BallerinaView, ResourceDefinition,
              DefaultWorkerView, Point, ConnectorDeclarationView, StatementViewFactory,
              BallerinaASTFactory, ExpressionViewFactory, MessageView, StatementContainer,
              VariableDeclaration, VariablesView, ClientLifeLine, AnnotationView,
              ArgumentsView) {

        /**
         * The view to represent a resource definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ResourceDefinition} args.model - The resource definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ResourceDefinitionView = function (args) {

            BallerinaView.call(this, args);

            this._connectorViewList =  [];
            this._defaultWorker = undefined;
            this._statementExpressionViewList = [];
            // TODO: Instead of using the parentView use the parent. Fix this from BallerinaView.js and bellow
            this._parentView = _.get(args, "parentView");

            if (_.isNil(this._model) || !(this._model instanceof ResourceDefinition)) {
                log.error("Resource definition is undefined or is of different type." + this._model);
                throw "Resource statement is definition undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for resource definition is undefined." + this._container);
                throw "Container for resource definition is undefined." + this._container;
            }

            // Center point of the resource
            this._viewOptions.topLeft = _.get(args, "viewOptions.topLeft", new Point(50, 100));
            this._viewOptions.startActionOffSet = _.get(args, "viewOptions.startActionOffSet", 60);

            // center point for the client lifeline
            this._viewOptions.client = _.get(args, "viewOptions.client", {});
            this._viewOptions.client.center = _.get(args, "viewOptions.client.centerPoint",
                this._viewOptions.topLeft.clone().move(100, 150));

            // Center point of the default worker
            this._viewOptions.defaultWorker = _.get(args, "viewOptions.defaultWorker", {});
            this._viewOptions.defaultWorker.offsetTop = _.get(args, "viewOptions.defaultWorker.offsetTop", 50);
            this._viewOptions.defaultWorker.center = _.get(args, "viewOptions.defaultWorker.centerPoint",
                            this._viewOptions.topLeft.clone().move(260, 150));

            // View options for height and width of the heading box.
            this._viewOptions.heading = _.get(args, "viewOptions.heading", {});
            this._viewOptions.heading.height = _.get(args, "viewOptions.heading.height", 25);
            this._viewOptions.heading.width = _.get(args, "viewOptions.heading.width", this._container.node().ownerSVGElement.parentElement.offsetWidth - 100);

            // View options for height and width of the resource icon in the heading box.
            this._viewOptions.heading.icon = _.get(args, "viewOptions.heading.icon", {});
            this._viewOptions.heading.icon.height = _.get(args, "viewOptions.heading.icon.height", 25);
            this._viewOptions.heading.icon.width = _.get(args, "viewOptions.heading.icon.width", 25);

            this._viewOptions.contentCollapsed = _.get(args, "viewOptions.contentCollapsed", false);
            this._viewOptions.contentWidth = _.get(args, "viewOptions.contentWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 100);
            this._viewOptions.contentHeight = _.get(args, "viewOptions.contentHeight", 470);
            this._viewOptions.collapseIconWidth = _.get(args, "viewOptions.collaspeIconWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 95);
            this._viewOptions.deleteIconWidth = _.get(args, "viewOptions.deleteIconWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 125);

            this._viewOptions.startAction = _.get(args, "viewOptions.startAction", {
                width: 120,
                height: 30,
                title: 'start',
                cssClass: 'start-action'
            });

            this._viewOptions.totalHeightGap = 50;
            this._viewOptions.LifeLineCenterGap = 180;
            this._viewOptions.defua = 180;
            this._viewOptions.hoverClass = _.get(args, "viewOptions.cssClass.hover_svg", 'design-view-hover-svg');

            this._variableButton = undefined;
            this._variablePane = undefined;

            //setting initial height for resource container
            this._totalHeight = 230;
            // initialize bounding box
            this.getBoundingBox().fromTopLeft(this._viewOptions.topLeft, this._viewOptions.heading.width, this._viewOptions.heading.height
                + this._viewOptions.contentHeight);
            this.init();
        };

        ResourceDefinitionView.prototype = Object.create(BallerinaView.prototype);
        ResourceDefinitionView.prototype.constructor = ResourceDefinitionView;
        // TODO move variable types into constant class
        var variableTypes = ['message', 'boolean', 'string', 'int', 'float', 'long', 'double', 'json', 'xml'];

        ResourceDefinitionView.prototype.init = function(){
            //Registering event listeners
            this.listenTo(this._model, 'childVisitedEvent', this.childVisitedCallback);
            this.listenTo(this._parentView, 'childViewAddedEvent', this.childViewAddedCallback);
            this.listenTo(this._model, 'childRemovedEvent', this.childViewRemovedCallback);
        };

        ResourceDefinitionView.prototype.getChildContainer = function () {
            return this._resourceGroup;
        };

        ResourceDefinitionView.prototype.canVisitResourceDefinition = function (resourceDefinition) {
            return true;
        };

        /**
         * callback function for childVisited event
         * @param child
         */
        ResourceDefinitionView.prototype.childVisitedCallback = function (child) {
            this.trigger("childViewAddedEvent", child);
        };

        ResourceDefinitionView.prototype.childViewAddedCallback = function (child) {
            if(BallerinaASTFactory.isResourceDefinition(child)){
                if(child !== this._model){
                    log.info("[Eventing] Resource view added : ");
                }
            }
        };

        ResourceDefinitionView.prototype.childRemovedCallback = function (child) {
            log.info("[Eventing] Child element removed. ");
            (d3.select(this._container)).selectAll('#_' +child.id).remove();
        };

        ResourceDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ResourceDefinition) {
                this._model = model;
            } else {
                log.error("Resource definition is undefined or is of different type." + model);
                throw "Resource statement is definition undefined or is of different type." + model;
            }
        };

        ResourceDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for resource definition is undefined." + this._container);
                throw "Container for resource definition is undefined." + this._container;
            }
        };

        ResourceDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        /**
         * Render Start Action
         */
        ResourceDefinitionView.prototype.renderStartAction = function () {

            var prefs = this._viewOptions.startAction;
            var group = D3utils.group(this._contentGroup).classed(prefs.cssClass, true);
            var center = this._viewOptions.defaultWorker.center.clone()
                            .move(0, _.get(this._viewOptions, "startActionOffSet"));

            var rect = D3utils.centeredRect(center, prefs.width, prefs.height, 0, 0, group);
            var text = D3utils.centeredText(center, prefs.title, group);
            var messageStart = this._clientLifeLine.getTopCenter().clone();
            messageStart.y(center.y());
            var messageEnd = messageStart.clone();
            messageEnd.x(center.x() - prefs.width/2);
            var messageView = new MessageView({container: group.node(), start: messageStart, end: messageEnd});
            messageView.render();

            this._startActionGroup = group;
        };

        ResourceDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        ResourceDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        ResourceDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        ResourceDefinitionView.prototype.visitStatement = function (statement) {
            var args = {model: statement, container: this._contentGroup.node(), viewOptions: {},
                toolPalette: this.toolPalette, messageManager: this.messageManager, parent: this};

            // pass some additional params for reply statement view
            if(this._model.getFactory().isReplyStatement(statement)){
                var distFromClientToDefaultWorker = this._clientLifeLine.getTopCenter()
                    .absDistInXFrom(this._defaultWorker.getTopCenter());
                _.set(args, 'viewOptions.distanceToClient', distFromClientToDefaultWorker);
            }

            this._statementContainer.renderStatement(statement, args);
        };

        ResourceDefinitionView.prototype.visitExpression = function (expression) {
           /* var expressionViewFactory = new ExpressionViewFactory();
            var args = {model: expression, container: this._contentGroup.node(), viewOptions: undefined, parent:this};
            var expressionView = expressionViewFactory.getExpressionView(args);

             //TODO: we need to keep this value as a configurable value and read from constants
            var statementsGap = 40;
            var expressionWidth = 120;
            if (this._statementExpressionViewList.length > 0) {
                var lastStatement = this._statementExpressionViewList[this._statementExpressionViewList.length - 1];
                expressionView.setXPosition(lastStatement.getXPosition());
                expressionView.setYPosition(lastStatement.getYPosition() + lastStatement.getHeight() + statementsGap);
            } else {
               var x = this._defaultWorker.getMidPoint() - parseInt(expressionWidth/2);
                expressionView.setXPosition(x);
                expressionView.setYPosition(y + statementsGap);
            }
            this._statementExpressionViewList.push(expressionView);
            expressionView.render();*/
        };

        /**
         * Rendering the view for resource definition.
         * @returns {group} The svg group which contains the elements of the resource definition view.
         */
        ResourceDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.diagramRenderingContext = diagramRenderingContext;
            // Render resource view
            var svgContainer = $(this._container)[0];
            var self = this;

            var headingStart = new Point(this._viewOptions.topLeft.x(), this._viewOptions.topLeft.y());
            var contentStart = new Point(this._viewOptions.topLeft.x(),
                this._viewOptions.topLeft.y() + this._viewOptions.heading.height);
            //Main container for a resource
            var resourceGroup = D3utils.group(svgContainer);
            this._resourceGroup = resourceGroup;
            resourceGroup.attr("id", "_" +this._model.id);
            resourceGroup.attr("width", this._viewOptions.heading.width)
                .attr("height", this._viewOptions.heading.height + this._viewOptions.contentHeight);
            resourceGroup.attr("x", headingStart.x()).attr("y", contentStart.y());

            // Creating SVG definitions group for icons.
            var def = resourceGroup.append("defs");
            var iconSizeSideLength = 14;

            // Creating collapsed icon for SVG definitions.
            var collapsedIconSVGPattern = def.append("pattern").attr("id", "collapsedIcon").attr("width", "100%")
                .attr("height", "100%");
            collapsedIconSVGPattern.append("image").attr("xlink:href", "images/down.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var expandIconSVGPattern = def.append("pattern").attr("id", "expandIcon").attr("width", "100%")
                .attr("height", "100%");
            expandIconSVGPattern.append("image").attr("xlink:href", "images/up.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating resource icon for SVG definitions.
            var resourceIconSVGPattern = def.append("pattern").attr("id", "resourceIcon").attr("width", "100%")
                .attr("height", "100%");
            resourceIconSVGPattern.append("image").attr("xlink:href", "images/resource.svg").attr("x", 5)
                .attr("y", 5).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating delete icon for SVG definitions.
            var deleteIconSVGPattern = def.append("pattern").attr("id", "deleteIcon").attr("width", "100%")
                .attr("height", "100%");
            deleteIconSVGPattern.append("image").attr("xlink:href", "images/delete.svg").attr("x", 0).attr("y", 0)
                .attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var deleteRedIconSVGPattern = def.append("pattern").attr("id", "deleteRedIcon").attr("width", "100%")
                .attr("height", "100%");
            deleteRedIconSVGPattern.append("image").attr("xlink:href", "images/delete-red.svg").attr("x", 0).attr("y", 0)
                .attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating annotations icon for SVG definitions.
            var annotationIconSVGPattern = def.append("pattern").attr("id", "annotationIcon").attr("width", "100%")
                .attr("height", "100%");
            annotationIconSVGPattern.append("image").attr("xlink:href", "images/annotation.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var annotationBlackIconSVGPattern = def.append("pattern").attr("id", "annotationBlackIcon").attr("width", "100%")
                .attr("height", "100%");
            annotationBlackIconSVGPattern.append("image").attr("xlink:href", "images/annotation-black.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating arguments icon for SVG definitions.
            var argumentsIconSVGPattern = def.append("pattern").attr("id", "argumentsIcon").attr("width", "100%")
                .attr("height", "100%");
            argumentsIconSVGPattern.append("image").attr("xlink:href", "images/import.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var argumentsBlackIconSVGPattern = def.append("pattern").attr("id", "argumentsBlackIcon").attr("width", "100%")
                .attr("height", "100%");
            argumentsBlackIconSVGPattern.append("image").attr("xlink:href", "images/import-black.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Creating return type icon for SVG definitions.
            var returnTypeIconSVGPattern = def.append("pattern").attr("id", "returnTypeIcon").attr("width", "100%")
                .attr("height", "100%");
            returnTypeIconSVGPattern.append("image").attr("xlink:href", "images/export.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            var returnTypeBlackIconSVGPattern = def.append("pattern").attr("id", "returnTypeBlackIcon").attr("width", "100%")
                .attr("height", "100%");
            returnTypeBlackIconSVGPattern.append("image").attr("xlink:href", "images/export-black.svg").attr("x", 0)
                .attr("y", 0).attr("width", iconSizeSideLength).attr("height", iconSizeSideLength);

            // Resource header container
            var headerGroup = D3utils.group(resourceGroup);
            headerGroup.attr("id", "headerGroup");

            var headingRect = D3utils.rect(headingStart.x(), headingStart.y(),
                this._viewOptions.heading.width, this._viewOptions.heading.height,
                0, 0, headerGroup).classed("headingRect", true);
            this._headingRect = headingRect;

            // Drawing resource icon
            var headingRectIconHolder = D3utils.rect(headingStart.x(),
                headingStart.y(), this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("resourceHeadingIconHolder",true);

            var headingRectIcon = D3utils.rect(headingStart.x(), headingStart.y(), this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("headingRectIcon", true);

            var xEndOfHeadingRect = parseFloat(headingRect.attr("x")) + parseFloat(headingRect.attr("width")) ;
            var yForIcons = parseFloat(headingRect.attr("y")) + (((this._viewOptions.heading.icon.height) / 2) - (14 / 2));

            // Creating wrapper for collpase icon.
            var headingCollapseIconWrapper = D3utils.rect(
                xEndOfHeadingRect - this._viewOptions.heading.icon.width, headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 1, 0, 0, headerGroup)
                .classed("heading-icon-wrapper hoverable heading-icon-collpase-wrapper", true);

            var xForCollpaseIcon = xEndOfHeadingRect - this._viewOptions.heading.icon.width + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Creating resource heading collapse icon.
            var headingCollapseIcon = D3utils.rect(xForCollpaseIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headerGroup)
                .classed("headingCollapsedIcon", true);

            // Creating separator for collapse icon.
            D3utils.line(xEndOfHeadingRect - this._viewOptions.heading.icon.width, parseFloat(headingRect.attr("y")) + 5,
                xEndOfHeadingRect - this._viewOptions.heading.icon.width,
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headerGroup)
                .classed("operations-separator", true);

            // Creating separator for delete icon.
            D3utils.line(xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headerGroup)
                .classed("operations-separator", true);

            // Creating separator for annotation icon.
            D3utils.line(xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headerGroup)
                .classed("operations-separator", true);

            // Creating separator for annotation icon.
            D3utils.line(xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width),
                parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headerGroup)
                .classed("operations-separator", true);

            // Creating separator for annotation icon.
            // D3utils.line(xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width),
            //     parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width),
            //     parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headerGroup)
            //     .classed("operations-separator", true);

            // Creating wrapper for delete icon.
            var headingDeleteIconWrapper = D3utils.rect(
                xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headerGroup)
                .classed("heading-icon-wrapper heading-icon-delete-wrapper", true);

            var xForDeleteIcon = xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Resource heading delete icon
            var headingDeleteIcon = D3utils.rect(xForDeleteIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headerGroup).classed("headingDeleteIcon", true);

            // Creating wrapper for annotation icon.
            var headingAnnotationIconWrapper = D3utils.rect(
                xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headerGroup)
                .classed("heading-icon-wrapper heading-icon-annotation-wrapper", true);

            var xForAnnotationIcon = xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Resource heading annotation icon
            var headingAnnotationIcon = D3utils.rect(xForAnnotationIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headerGroup).classed("headingAnnotationBlackIcon", true);

            // Creating wrapper for arguments icon.
            var headingArgumentsIconWrapper = D3utils.rect(
                xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
                this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headerGroup)
                .classed("heading-icon-wrapper heading-icon-arguments-wrapper", true);

            var xForArgumentsIcon = xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

            // Resource heading arguments icon.
            var headingArgumentsIcon = D3utils.rect(xForArgumentsIcon, yForIcons,
                iconSizeSideLength, iconSizeSideLength, 0, 0, headerGroup).classed("headingArgumentsBlackIcon", true);

            // Creating wrapper for return type icon.
            // var headingReturnTypeIconWrapper = D3utils.rect(
            //     xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
            //     this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headerGroup)
            //     .classed("heading-icon-wrapper heading-icon-return-type-wrapper", true);
            //
            // var xForReturnTypeIcon = xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));
            //
            // // Resource heading return type icon.
            // var headingReturnTypeIcon = D3utils.rect(xForReturnTypeIcon, yForIcons,
            //     iconSizeSideLength, iconSizeSideLength, 0, 0, headerGroup).classed("headingReturnTypeBlackIcon", true);

            // UI changes when the annotation button is clicked.
            $(headingAnnotationIcon.node()).click(function () {
                if ($(this).data("showing") === "true") {
                    $(this).data("showing", "false");
                    headingAnnotationIcon.classed("headingAnnotationBlackIcon", true);
                    headingAnnotationIcon.classed("headingAnnotationIcon", false);
                    headingAnnotationIconWrapper.classed("heading-icon-annotation-wrapper-clicked", false);
                } else {
                    $(this).data("showing", "true");
                    headingAnnotationIcon.classed("headingAnnotationBlackIcon", false);
                    headingAnnotationIcon.classed("headingAnnotationIcon", true);
                    headingAnnotationIconWrapper.classed("heading-icon-annotation-wrapper-clicked", true);
                }
            });

            // UI changes when the arguments button is clicked.
            $(headingArgumentsIcon.node()).click(function () {
                if ($(this).data("showing") === "true") {
                    $(this).data("showing", "false");
                    headingArgumentsIcon.classed("headingArgumentsBlackIcon", true);
                    headingArgumentsIcon.classed("headingArgumentsIcon", false);
                    headingArgumentsIconWrapper.classed("heading-icon-arguments-wrapper-clicked", false);
                } else {
                    $(this).data("showing", "true");
                    headingArgumentsIcon.classed("headingArgumentsBlackIcon", false);
                    headingArgumentsIcon.classed("headingArgumentsIcon", true);
                    headingArgumentsIconWrapper.classed("heading-icon-arguments-wrapper-clicked", true);
                }
            });

            // UI changes when the return type button is clicked.
            // $(headingReturnTypeIcon.node()).click(function () {
            //     if ($(this).data("showing") === "true") {
            //         $(this).data("showing", "false");
            //         headingReturnTypeIcon.classed("headingReturnTypeBlackIcon", true);
            //         headingReturnTypeIcon.classed("headingReturnTypeIcon", false);
            //         headingReturnTypeIconWrapper.classed("heading-icon-return-type-wrapper-clicked", false);
            //     } else {
            //         $(this).data("showing", "true");
            //         headingReturnTypeIcon.classed("headingReturnTypeBlackIcon", false);
            //         headingReturnTypeIcon.classed("headingReturnTypeIcon", true);
            //         headingReturnTypeIconWrapper.classed("heading-icon-return-type-wrapper-clicked", true);
            //     }
            // });

            // Add the resource name editable html area
            var svgWrappingHtml = this.getChildContainer().node().ownerSVGElement.parentElement;
            var nameDiv = $("<div></div>");
            nameDiv.css('left', (parseInt(headingStart.x()) + 30) + "px");
            nameDiv.css('top', parseInt(headingStart.y()) + "px");
            nameDiv.css('width',"100px");
            nameDiv.css('height',"25px");
            nameDiv.addClass("name-container-div");
            var nameSpan = $("<span></span>");
            nameSpan.text(self._model.getResourceName());
            nameSpan.addClass("name-span");
            nameSpan.attr("contenteditable", "true");
            nameSpan.attr("spellcheck", "false");
            nameSpan.focus();
            nameSpan.blur();
            nameDiv.append(nameSpan);
            $(svgWrappingHtml).append(nameDiv);
            // Container for resource body
            var contentGroup = D3utils.group(resourceGroup);
            contentGroup.attr('id', "contentGroup");

            nameSpan.on("change paste keyup", function () {
                self._model.setResourceName($(this).text());
            });

            this._contentGroup = contentGroup;

            var contentRect = D3utils.rect(contentStart.x(), contentStart.y(),
                this._viewOptions.contentWidth, this._viewOptions.contentHeight, 0, 0,
                contentGroup).classed("resource-content", true);

            this._contentRect = contentRect;
            contentRect.attr("fill", "#fff");

            var onExpandCollapse = function () {
                var resourceBBox = self.getBoundingBox();
                var visibility = contentGroup.node().getAttribute("display");
                if (visibility == "none") {
                    contentGroup.attr("display", "inline");
                    // resource content is expanded. Hence expand resource BBox
                    resourceBBox.h(resourceBBox.h() + self._minizedHeight);

                    // show the variable button and variable pane
                    self._variableButton.show();
                    self._variablePane.show();

                    // Changing icon if the collapse.
                    headingCollapseIcon.classed("headingExpandIcon", false);
                    headingCollapseIcon.classed("headingCollapsedIcon", true);
                }
                else {
                    contentGroup.attr("display", "none");
                    // resource content is folded. Hence decrease resource BBox height and keep the minimized size
                    self._minizedHeight =  parseFloat(contentRect.attr('height'));
                    resourceBBox.h(resourceBBox.h() - self._minizedHeight);

                    // hide the variable button and variable pane
                    self._variableButton.hide();
                    self._variablePane.hide();

                    // Changing icon if the collapse.
                    headingCollapseIcon.classed("headingExpandIcon", true);
                    headingCollapseIcon.classed("headingCollapsedIcon", false);
                }
            };

            // On click of collapse icon hide/show resource body
            headingCollapseIcon.on("click", onExpandCollapse);
            headingRect.on("click", onExpandCollapse);

            // On click of delete icon
            headingDeleteIcon.on("click", function () {
                log.info("Clicked delete button");
                var child = self._model;
                var parent = child.parent;
                parent.removeChild(child);
            });

            this.getBoundingBox().on("height-changed", function(dh){
                this._contentRect.attr('height', parseFloat(this._contentRect.attr('height')) + dh);
            }, this);

            // render client life line
            // Creating client lifeline.
            var clientCenter = _.get(this._viewOptions, 'client.center');
            var lifeLineArgs = {};
            _.set(lifeLineArgs, 'container', this._contentGroup.node());
            _.set(lifeLineArgs, 'centerPoint', clientCenter);

            this._clientLifeLine = new ClientLifeLine(lifeLineArgs);
            this._clientLifeLine.render();

            if (_.isUndefined(this._defaultWorker)) {
                var defaultWorkerOpts = {};
                _.set(defaultWorkerOpts, 'container', contentGroup.node());
                _.set(defaultWorkerOpts, 'centerPoint', this._viewOptions.defaultWorker.center);
                this._defaultWorker = new DefaultWorkerView(defaultWorkerOpts);
            }
            this._defaultWorker.render();
            this.trigger("defaultWorkerViewAddedEvent", this._defaultWorker);

            this.initResourceLevelDropTarget();
            this.renderStartAction();
            this.renderStatementContainer();
            log.debug("Rendering Resource View");
            this.getModel().accept(this);
            //Removing all the registered 'child-added' event listeners for this model.
            //This is needed because we are not unregistering registered event while the diagram element deletion.
            //Due to that, sometimes we are having two or more view elements listening to the 'child-added' event of same model.
            this._model.off('child-added');
            this._model.on('child-added', function(child){
                self.visit(child);
            });

            this._variableButton = VariablesView.createVariableButton(this.getChildContainer().node(),
                parseInt(this.getChildContainer().attr("x")) + 4, parseInt(this.getChildContainer().attr("y")) + 7);

            var variableProperties = {
                model: this._model,
                activatorElement: this._variableButton,
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        x: parseInt(this.getChildContainer().attr("x")) + 17,
                        y: parseInt(this.getChildContainer().attr("y")) + 6
                    },
                    width: parseInt(this.getChildContainer().node().getBBox().width) - 36
                }
            };

            this._variablePane = VariablesView.createVariablePane(variableProperties);

            var annotationProperties = {
                model: this._model,
                activatorElement: headingAnnotationIcon.node(),
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        // "-1" to remove the svg stroke line
                        left: parseFloat(this.getChildContainer().attr("x")) + parseFloat(this.getChildContainer().attr("width")) - 1,
                        top: this.getChildContainer().attr("y")
                    }
                }
            };

            AnnotationView.createAnnotationPane(annotationProperties);

            var argumentsProperties = {
                model: this._model,
                activatorElement: headingArgumentsIcon.node(),
                paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
                viewOptions: {
                    position: {
                        // "-1" to remove the svg stroke line
                        left: parseFloat(this.getChildContainer().attr("x")) + parseFloat(this.getChildContainer().attr("width")) - 1,
                        top: this.getChildContainer().attr("y")
                    }
                }
            };

            ArgumentsView.createArgumentsPane(argumentsProperties);

            this.getBoundingBox().on("moved", function(offset){
                var currentTransform = this._resourceGroup.attr("transform");
               this._resourceGroup.attr("transform", (!_.isNil(currentTransform) ? currentTransform : "") +
                   " translate(" + offset.dx + ", " + offset.dy + ")");

                // Reposition the resource name container
                var newDivPositionVertical = parseInt(nameDiv.css("top")) + offset.dy;
                nameDiv.css("top", newDivPositionVertical + "px");

                // Reposition Variable button
                var newVButtonPositionVertical = parseInt($(self._variableButton).css("top")) + offset.dy;
                $(self._variableButton).css("top", newVButtonPositionVertical + "px");

                // Reposition variable pane
                var newVPanePositionVertical = parseInt($(self._variablePane).css("top")) + offset.dy;
                $(self._variablePane).css("top", newVPanePositionVertical + "px");
            }, this);
        };

        /**
         * Render statement container
         */
        ResourceDefinitionView.prototype.renderStatementContainer = function(){
            var statementContainerOpts = {};
            _.set(statementContainerOpts, 'model', this._model);
            _.set(statementContainerOpts, 'topCenter', this._defaultWorker.getTopCenter());
            _.set(statementContainerOpts, 'bottomCenter', this._defaultWorker.getBottomCenter());
            _.set(statementContainerOpts, 'width', this._defaultWorker.width());
            _.set(statementContainerOpts, 'container', this._defaultWorker.getContentArea().node());
            _.set(statementContainerOpts, 'toolPalette', this.toolPalette);
            this._statementContainer = new StatementContainer(statementContainerOpts);
            this.listenTo(this._statementContainer.getBoundingBox(), 'bottom-edge-moved', function(dy){
                    this._defaultWorker.getBottomCenter().y(this._statementContainer.getBoundingBox().getBottom());
                    this._clientLifeLine.getBottomCenter().y(this._statementContainer.getBoundingBox().getBottom());
                    this.getBoundingBox().h(this.getBoundingBox().h() + dy);
            });
            this._statementContainer.render(this.diagramRenderingContext);
        };

        ResourceDefinitionView.prototype.initResourceLevelDropTarget = function(){
            var self = this,
                hoverClass = this._viewOptions.hoverClass;
            var mouseOverHandler = function() {
                //if someone is dragging a tool from tool-palette
                if(self.toolPalette.dragDropManager.isOnDrag()){

                    if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)){
                        return;
                    }

                    // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                    // tool view will use this to provide feedback on impossible drop zones
                    self.toolPalette.dragDropManager.setActivatedDropTarget(self._model, function(nodeBeingDragged){
                        var nodeFactory = self._model.getFactory();
                        // IMPORTANT: override resource definition node's default validation logic
                        // This drop zone is for worker and connector declarations only.
                        // Statements should only be allowed on top of default worker's drop zone.
                        return nodeFactory.isConnectorDeclaration(nodeBeingDragged)
                            || nodeFactory.isWorkerDeclaration(nodeBeingDragged);
                    });

                    // indicate drop area
                    self._contentRect.classed(hoverClass, true);
                    self._headingRect.classed(hoverClass, true);

                    // reset ui feed back on drop target change
                    self.toolPalette.dragDropManager.once("drop-target-changed", function(){
                        self._contentRect.classed(hoverClass, false);
                        self._headingRect.classed(hoverClass, false);
                    });
                }
                d3.event.stopPropagation();
                //TODO : Remove this and set currentScope/resource properly
                self.diagramRenderingContext.currentResource = self;

            };

            var mouseOutHandler = function() {
                // reset ui feed back on hover out
                if(self.toolPalette.dragDropManager.isOnDrag()){
                    if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self._model)){
                        self._contentRect.classed('design-view-hover-svg', false);
                        self._headingRect.classed('design-view-hover-svg', false);
                    }
                }
                d3.event.stopPropagation();

            };
            this._contentRect.on("mouseover", mouseOverHandler);
            this._headingRect.on("mouseover", mouseOverHandler);
            this._contentRect.on("mouseout", mouseOutHandler);
            this._headingRect.on("mouseout", mouseOutHandler);
        };

        ResourceDefinitionView.prototype.getConnectorViewList = function(){
            return this._connectorViewList;
        };
        /**
         * @inheritDoc
         * @returns {_defaultResourceWorker}
         */
        ResourceDefinitionView.prototype.getDefaultWorker = function () {
            return this._defaultWorker;
        };

        ResourceDefinitionView.prototype.canVisitResourceDefinition = function (resourceDefinition) {
            return true;
        };

        ResourceDefinitionView.prototype.visitResourceDefinition = function (resourceDefinition) {

        };

        ResourceDefinitionView.prototype.canVisitConnectorDeclaration = function (connectorDeclaration) {
            return true;
        };

        /**
         * Calls the render method for a connector declaration.
         * @param {ConnectorDeclaration} connectorDeclaration - The connector declaration model.
         */
        ResourceDefinitionView.prototype.visitConnectorDeclaration = function (connectorDeclaration) {
            var connectorContainer = this._contentGroup.node(),
                height = this._clientLifeLine.getTopCenter().absDistInYFrom(this._clientLifeLine.getBottomCenter()),
                connectorOpts = {model: connectorDeclaration, container: connectorContainer,
                    parentView: this, messageManager: this.messageManager, lineHeight: height},
                connectorDeclarationView,
                center;

            var lastLifeLine = this.getLastLifeLine();
                center = lastLifeLine.getTopCenter().clone().move(this._viewOptions.LifeLineCenterGap, 0);

            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;
            this._connectorViewList.push(connectorDeclarationView);

            connectorDeclarationView._rootGroup.attr('id', '_' +connectorDeclarationView._model.id);

            connectorDeclarationView.render();

            // Creating property pane
            var editableProperties = [
                {
                    propertyType: "text",
                    key: "Name",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getConnectorName,
                    setterMethod: connectorDeclarationView._model.setConnectorName
                },
                {
                    propertyType: "text",
                    key: "Uri",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getUri,
                    setterMethod: connectorDeclarationView._model.setUri
                },
                {
                    propertyType: "text",
                    key: "Timeout",
                    model: connectorDeclarationView._model,
                    getterMethod: connectorDeclarationView._model.getTimeout,
                    setterMethod: connectorDeclarationView._model.setTimeout
                }
            ];
            connectorDeclarationView.createPropertyPane({
                model: connectorDeclarationView._model,
                lifeLineGroup:connectorDeclarationView._rootGroup,
                editableProperties: editableProperties
            });

            connectorDeclarationView.setParent(this);

            this.getBoundingBox().on("height-changed", function (dh) {
                this.getBoundingBox().h( this.getBoundingBox().h() + dh);
            }, connectorDeclarationView);

            this.trigger("childConnectorViewAddedEvent", connectorDeclarationView);
        };

        /**
         * setting resource container height and setting the height for the bounding box
         * @param height
         */
        ResourceDefinitionView.prototype.setResourceContainerHeight = function (height){
            this._resourceGroup.attr("height", height);
            this._contentRect.attr("height", height);
            this._defaultWorker.setHeight(height - this._viewOptions.totalHeightGap);
            this.getBoundingBox().h(height);
        };

        ResourceDefinitionView.prototype.getLastLifeLine = function () {
            if(this.getConnectorViewList().length > 0 ){
                return _.last(this.getConnectorViewList());
            }
            else{
                return this.getDefaultWorker();
            }
        };


        /**
         * get the Statement View List of the the resource
         * @returns [_statementExpressionViewList] {Array}
         */
        ResourceDefinitionView.prototype.getStatementExpressionViewList = function () {
            return this._statementExpressionViewList;
        };

        /**
         * Y distance from one resource's end point to next resource's start point
         * @returns {number}
         */
        ResourceDefinitionView.prototype.getGapBetweenResources = function () {
            return 25;
        };

        /**
         * Y distance from one statement's end point to next statement's start point
         * @returns {number}
         */
        ResourceDefinitionView.prototype.getGapBetweenStatements = function () {
            return 10;
        };

        /**
         * Height of the resource's heading
         * @returns {number}
         */
        ResourceDefinitionView.prototype.getResourceHeadingHeight = function () {
            return this._viewOptions.heading.height;
        };

        return ResourceDefinitionView;


    });

