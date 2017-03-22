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
import _ from 'lodash';
import log from 'log';
import d3 from 'd3';
import $ from 'jquery';
import D3utils from 'd3utils';
import BallerinaView from './ballerina-view';
import ConnectorAction from './../ast/connector-action';
import DefaultWorkerView from './default-worker';
import Point from './point';
import ConnectorDeclarationView from './connector-declaration-view';
import StatementViewFactory from './statement-view-factory';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import MessageView from './message';
import StatementContainer from './statement-container';
import VariableDeclaration from './../ast/variable-declaration';
import AnnotationView from './annotation-view';
import ArgumentsView from './function-arguments-view';
import ReturnTypesPaneView from './return-types-pane-view';
import Axis from './axis';
import WorkerDeclarationView from './worker-declaration-view';

/**
 * The view to represent a connector action which is an AST visitor.
 * @param {Object} args - Arguments for creating the view.
 * @param {ConnectorAction} args.model - The connector action model.
 * @param {Object} args.container - The HTML container to which the view should be added to.
 * @param {Object} [args.viewOptions={}] - Configuration values for the view.
 * @constructor
 */
class ConnectorActionView extends BallerinaView {
    constructor(args) {
        super(args);
        this._connectorWorkerViewList = [];
        this._defaultWorker = undefined;
        this._statementExpressionViewList = [];
        // TODO: Instead of using the parentView use the parent. Fix this from BallerinaView.js and bellow
        this._parentView = _.get(args, "parentView");

        if (_.isNil(this._model) || !(this._model instanceof ConnectorAction)) {
            log.error("Connector Action is undefined or is of different type." + this._model);
            throw "Connector Action is undefined or is of different type." + this._model;
        }

        if (_.isNil(this._container)) {
            log.error("Container for connector action is undefined." + this._container);
            throw "Container for connector action is undefined." + this._container;
        }

        // Center point of the connector action view
        this._viewOptions.topLeft = _.get(args, "viewOptions.topLeft", new Point(50, 100));
        this._viewOptions.startActionOffSet = _.get(args, "viewOptions.startActionOffSet", 60);

        // Center point of the default worker
        this._viewOptions.defaultWorker = _.get(args, "viewOptions.defaultWorker", {});
        this._viewOptions.defaultWorker.offsetTop = _.get(args, "viewOptions.defaultWorker.offsetTop", 50);
        this._viewOptions.defaultWorker.center = _.get(args, "viewOptions.defaultWorker.centerPoint",
            this._viewOptions.topLeft.clone().move(100, 150));

        // View options for height and width of the heading box.
        this._viewOptions.heading = _.get(args, "viewOptions.heading", {});
        this._viewOptions.heading.height = _.get(args, "viewOptions.heading.height", 25);
        this._viewOptions.heading.width = _.get(args, "viewOptions.heading.width", this._container.node().ownerSVGElement.parentElement.offsetWidth - 100);

        // View options for height and width of the connector action icon in the heading box.
        this._viewOptions.heading.icon = _.get(args, "viewOptions.heading.icon", {});
        this._viewOptions.heading.icon.height = _.get(args, "viewOptions.heading.icon.height", 25);
        this._viewOptions.heading.icon.width = _.get(args, "viewOptions.heading.icon.width", 25);

        this._viewOptions.contentCollapsed = _.get(args, "viewOptions.contentCollapsed", false);
        this._viewOptions.contentWidth = _.get(args, "viewOptions.contentWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 100);
        this._viewOptions.contentHeight = _.get(args, "viewOptions.contentHeight", 470);
        this._viewOptions.collapseIconWidth = _.get(args, "viewOptions.collaspeIconWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 95);
        this._viewOptions.deleteIconWidth = _.get(args, "viewOptions.deleteIconWidth", this._container.node().ownerSVGElement.parentElement.offsetWidth - 125);

        this._viewOptions.heading.minWidth = 700;
        this._viewOptions.contentMinWidth = 700;

        this._viewOptions.totalHeightGap = 50;
        this._viewOptions.LifeLineCenterGap = 120;
        this._viewOptions.defua = 180;
        this._viewOptions.hoverClass = _.get(args, "viewOptions.cssClass.hover_svg", 'design-view-hover-svg');

        //setting initial height for connector action container
        this._totalHeight = 230;
        this._headerIconGroup = undefined;
        // initialize bounding box
        this.getBoundingBox().fromTopLeft(this._viewOptions.topLeft, this._viewOptions.heading.width, this._viewOptions.heading.height
            + this._viewOptions.contentHeight);

        //set initial worker margin for the action
        this._workerLifelineMargin = new Axis(0, false);
        // Set the initial height control margin
        this._horizontalMargin = new Axis(0, true);

        this._connectorActionGroup = undefined;
        this._offsetLastStatementGap = 100;
        this.init();
    }

    /**
     * Init the event listeners
     */
    init() {
        this.listenTo(this._model, 'child-removed', this.childRemovedCallback, this);
        this._model.on('before-remove', this.onBeforeModelRemove, this);
    }

    onBeforeModelRemove() {
        d3.select("#_" +this._model.id).remove();
        $(this._nameDiv).remove();
        this.getBoundingBox().move(0, -this.getBoundingBox().h() - 25);
    }

    /**
     * Child remove callback
     * @param {ASTNode} child - removed child
     */
    childRemovedCallback(child) {
        var self = this;
        if (BallerinaASTFactory.isStatement(child)) {
            this.getStatementContainer().childStatementRemovedCallback(child);
        } else if (BallerinaASTFactory.isConnectorDeclaration(child)) {
            var connectorViews = this.getConnectorViews();
            var nextConnector = undefined;
            var previousConnector = undefined;
            var connectorViewIndex = _.findIndex(connectorViews, function (view) {
                return view.getModel().id === child.id;
            });

            var currentConnector = connectorViews[connectorViewIndex];

            if (connectorViewIndex === 0) {
                // Deleted the first connector/ only connector
                currentConnector.stopListening(this.getWorkerLifeLineMargin());
                if (!_.isNil(connectorViews[connectorViewIndex + 1])) {
                    nextConnector = connectorViews[connectorViewIndex + 1];
                    nextConnector.stopListening(currentConnector.getBoundingBox());
                    nextConnector.listenTo(this.getWorkerLifeLineMargin(), 'moved', function (offset) {
                        nextConnector.getBoundingBox().move(offset, 0);
                    });
                }
            } else if (connectorViewIndex === connectorViews.length - 1) {
                // Deleted the last connector
                currentConnector.stopListening(connectorViews[connectorViewIndex].getBoundingBox());
            } else {
                // Deleted an intermediate Connector
                previousConnector = connectorViews[connectorViewIndex - 1];
                nextConnector = connectorViews[connectorViewIndex + 1];
                currentConnector.stopListening(previousConnector.getBoundingBox());
                nextConnector.stopListening(currentConnector.getBoundingBox());
                nextConnector.listenTo(previousConnector.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveActionLevelConnector(nextConnector, offset);
                });
            }
            var connectorViewOriginalIndex = _.findIndex(this.getConnectorWorkerViewList(), function (view) {
                return view.getModel().id === child.id;
            });
            this.getConnectorWorkerViewList()[connectorViewOriginalIndex] = null;
            this.getConnectorWorkerViewList().splice(connectorViewOriginalIndex, 1);
        } else if (BallerinaASTFactory.isWorkerDeclaration(child)) {
            var workerViews = this.getWorkerViews();
            var nextWorker = undefined;
            var previousWorker = undefined;
            var workerViewIndex = _.findIndex(workerViews, function (view) {
                return view.getModel().id === child.id;
            });

            var currentWorker = workerViews[workerViewIndex];

            if (workerViewIndex === 0) {
                // Deleted the first worker/ only worker
                nextWorker = workerViews[workerViewIndex + 1];
                // Since we deleted the first worker, now the worker lifeline margin listen to the default worker
                this.getWorkerLifeLineMargin().stopListening(currentWorker.getBoundingBox());
                this.getWorkerLifeLineMargin().listenTo(this.getDefaultWorker().getBoundingBox(),
                    'right-edge-moved', function (offset) {
                        self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + offset);
                    });
            } else if (workerViewIndex === workerViews.length - 1) {
                // Deleted the last worker
                this.getWorkerLifeLineMargin().stopListening(currentWorker.getBoundingBox());
                previousWorker = workerViews[workerViewIndex - 1];
                if (!_.isNil(previousWorker)) {
                    this.getWorkerLifeLineMargin().listenTo(previousWorker.getBoundingBox(), 'right-edge-moved', function (offset) {
                        self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + offset);
                    });
                }
            } else {
                // Deleted an intermediate Worker
                previousWorker = workerViews[workerViewIndex - 1];
                nextWorker = workerViews[workerViewIndex + 1];
                currentWorker.stopListening(previousWorker.getBoundingBox());
                nextWorker.stopListening(currentWorker.getBoundingBox());
                nextWorker.listenTo(previousWorker.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveActionLevelConnector(nextWorker, offset);
                });
            }
            var workerViewOriginalIndex = _.findIndex(this.getConnectorWorkerViewList(), function (view) {
                return view.getModel().id === child.id;
            });
            this.getConnectorWorkerViewList()[workerViewOriginalIndex] = null;
            this.getConnectorWorkerViewList().splice(workerViewOriginalIndex, 1);
        }

        // Remove the connector/ worker from the diagram rendering context
        delete this.diagramRenderingContext.getViewModelMap()[child.id];
    }

    /**
     * Get the child container
     * @return {svg} svg container
     */
    getChildContainer() {
        return this._connectorActionGroup;
    }

    canVisitConnectorAction(connectorAction) {
        return true;
    }

    /**
     * Set the connctor acton view's corresponding model
     * @param model
     */
    setModel(model) {
        if (!_.isNil(model) && model instanceof ConnectorAction) {
            this._model = model;
        } else {
            log.error("Connector Action is undefined or is of different type." + model);
            throw "Connector Action is definition undefined or is of different type." + model;
        }
    }

    /**
     * Set the container for the connector action view
     * @param {object} container
     */
    setContainer(container) {
        if (!_.isNil(container)) {
            this._container = container;
        } else {
            log.error("Container for connector action is undefined." + this._container);
            throw "Container for connector action is undefined." + this._container;
        }
    }

    /**
     * set the view Options
     * @param {object} viewOptions
     */
    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    /**
     * Get the model for the connector action view
     * @return {ConnectorAction} _model
     */
    getModel() {
        return this._model;
    }

    /**
     * Get the container
     * @return {object} _container
     */
    getContainer() {
        return this._container;
    }

    /**
     * Get the View Options
     * @return {Object} _viewOptions
     */
    getViewOptions() {
        return this._viewOptions;
    }

    /**
     * @param {BallerinaStatementView} statement
     */
    visitStatement(statement) {
        var args = {model: statement, container: this._contentGroup.node(), viewOptions: {},
            toolPalette: this.toolPalette, messageManager: this.messageManager, parent: this};
        this._statementContainer.renderStatement(statement, args);
    }

    /**
     * Rendering the view for connector action.
     * @returns {group} The svg group which contains the elements of the connector action view.
     */
    render(diagramRenderingContext) {
        this.diagramRenderingContext = diagramRenderingContext;
        var svgContainer = $(this._container)[0];
        var self = this;

        var headingStart = new Point(this._viewOptions.topLeft.x(), this._viewOptions.topLeft.y());
        var contentStart = new Point(this._viewOptions.topLeft.x(),
            this._viewOptions.topLeft.y() + this._viewOptions.heading.height);
        //Main container for a connector action
        var connectorActionGroup = D3utils.group(svgContainer);
        this._connectorActionGroup = connectorActionGroup;
        connectorActionGroup.attr("id", "_" +this._model.id);
        connectorActionGroup.attr("width", this._viewOptions.heading.width)
            .attr("height", this._viewOptions.heading.height + this._viewOptions.contentHeight);
        connectorActionGroup.attr("x", headingStart.x()).attr("y", contentStart.y());

        // Creating SVG definitions group for icons.
        var def = connectorActionGroup.append("defs");
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

        // Creating connector action icon for SVG definitions.
        var connectorActionIconSVGPattern = def.append("pattern").attr("id", "connectorActionIcon").attr("width", "100%")
            .attr("height", "100%");
        // Connector action icon
        connectorActionIconSVGPattern.append("image").attr("xlink:href", "images/action.svg").attr("x", 5)
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

        // Connector action header container
        var headerGroup = D3utils.group(connectorActionGroup);
        headerGroup.attr("id", "headerGroup");

        var headingRect = D3utils.rect(headingStart.x(), headingStart.y(),
            this._viewOptions.heading.width, this._viewOptions.heading.height,
            0, 0, headerGroup).classed("headingRect", true);
        this._headingRect = headingRect;

        var headingIconsGroup = D3utils.group(headerGroup);
        headingIconsGroup.attr("transform", "translate(0,0)");
        this._headerIconGroup = headingIconsGroup;

        // Drawing connector action icon
        var headingRectIconHolder = D3utils.rect(headingStart.x(),
            headingStart.y(), this._viewOptions.heading.icon.width,
            this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("connectorActionHeadingIconHolder",true);

        var actionHeadingRectIcon = D3utils.rect(headingStart.x(), headingStart.y(), this._viewOptions.heading.icon.width,
            this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("actionHeadingRectIcon", true);

        var xEndOfHeadingRect = parseFloat(headingRect.attr("x")) + parseFloat(headingRect.attr("width")) ;
        var yForIcons = parseFloat(headingRect.attr("y")) + (((this._viewOptions.heading.icon.height) / 2) - (14 / 2));

        // Creating wrapper for collpase icon.
        var headingCollapseIconWrapper = D3utils.rect(
            xEndOfHeadingRect - this._viewOptions.heading.icon.width, headingStart.y() + 1,
            this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 1, 0, 0, headingIconsGroup)
            .classed("heading-icon-wrapper hoverable heading-icon-collpase-wrapper", true);

        var xForCollpaseIcon = xEndOfHeadingRect - this._viewOptions.heading.icon.width + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

        // Creating connector action heading collapse icon.
        var headingCollapseIcon = D3utils.rect(xForCollpaseIcon, yForIcons,
            iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).attr("title", "Collapse Pane")
            .classed("headingExpandIcon", true).attr("style", "opacity: 0.4");

        // Creating separator for collapse icon.
        D3utils.line(xEndOfHeadingRect - this._viewOptions.heading.icon.width, parseFloat(headingRect.attr("y")) + 5,
            xEndOfHeadingRect - this._viewOptions.heading.icon.width,
            parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
            .classed("operations-separator", true);

        // Creating separator for delete icon.
        D3utils.line(xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width),
            parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width),
            parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
            .classed("operations-separator", true);

        // Creating separator for annotation icon.
        D3utils.line(xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width),
            parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width),
            parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
            .classed("operations-separator", true);

        // Creating separator for annotation icon.
        D3utils.line(xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width),
            parseFloat(headingRect.attr("y")) + 5, xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width),
            parseFloat(headingRect.attr("y")) + parseFloat(headingRect.attr("height")) - 5, headingIconsGroup)
            .classed("operations-separator", true);

        // Creating wrapper for delete icon.
        var headingDeleteIconWrapper = D3utils.rect(
            xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
            this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
            .classed("heading-icon-wrapper heading-icon-delete-wrapper", true);

        var xForDeleteIcon = xEndOfHeadingRect - (2 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

        // Connector Action heading delete icon
        // TODO: removed the tooltip temporarily
        var headingDeleteIcon = D3utils.rect(xForDeleteIcon, yForIcons,
            iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup)
            .classed("headingDeleteIcon", true).attr("style", "opacity: 0.4");

        // Creating wrapper for annotation icon.
        var headingAnnotationIconWrapper = D3utils.rect(
            xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
            this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
            .classed("heading-icon-wrapper heading-icon-annotation-wrapper", true);

        var xForAnnotationIcon = xEndOfHeadingRect - (3 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

        // Connector Action heading annotation icon
        var headingAnnotationIcon = D3utils.rect(xForAnnotationIcon, yForIcons,
            iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).attr("title", "Annotations")
            .classed("headingAnnotationBlackIcon", true).attr("style", "opacity: 0.4");

        // Creating wrapper for arguments icon.
        var headingArgumentsIconWrapper = D3utils.rect(
            xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
            this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
            .classed("heading-icon-wrapper heading-icon-arguments-wrapper", true);

        var xForArgumentsIcon = xEndOfHeadingRect - (5 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

        // Connector Action heading arguments icon.
        var headingArgumentsIcon = D3utils.rect(xForArgumentsIcon, yForIcons,
            iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).attr("title", "Arguments")
            .classed("headingArgumentsBlackIcon", true).attr("style", "opacity: 0.4");

        // Creating wrapper for Return Types icon.
        var headingReturnTypesIconWrapper = D3utils.rect(
            xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width), headingStart.y() + 1,
            this._viewOptions.heading.icon.width - 1, this._viewOptions.heading.icon.height - 2, 0, 0, headingIconsGroup)
            .classed("heading-icon-wrapper heading-icon-return-type-wrapper", true);

        var xForReturnTypesIcon = xEndOfHeadingRect - (4 * this._viewOptions.heading.icon.width) + (((this._viewOptions.heading.icon.width) / 2) - (14 / 2));

        // Connector Action heading Return Types icon.
        var headingReturnTypesIcon = D3utils.rect(xForReturnTypesIcon, yForIcons,
            iconSizeSideLength, iconSizeSideLength, 0, 0, headingIconsGroup).attr("title", "Return Types")
            .classed("headingReturnTypeBlackIcon", true).attr("style", "opacity: 0.4");

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

        //initialize all svg related tooltips
        $('svg rect').tooltip({'container': 'body'});

        // UI changes when the return Types button is clicked.
        $(headingReturnTypesIcon.node()).click(function () {
            if ($(this).data("showing") === "true") {
                $(this).data("showing", "false");
                headingReturnTypesIcon.classed("headingReturnTypeBlackIcon", true);
                headingReturnTypesIcon.classed("headingReturnTypeIcon", false);
                headingReturnTypesIconWrapper.classed("heading-icon-return-type-wrapper-clicked", false);
            } else {
                $(this).data("showing", "true");
                headingReturnTypesIcon.classed("headingReturnTypeBlackIcon", false);
                headingReturnTypesIcon.classed("headingReturnTypeIcon", true);
                headingReturnTypesIconWrapper.classed("heading-icon-return-type-wrapper-clicked", true);
            }
        });

        // Add the connector action name editable html area
        var svgWrappingHtml = this.getChildContainer().node().ownerSVGElement.parentElement;
        this._nameDiv = $("<div></div>");
        this._nameDiv.css('left', (parseInt(headingStart.x()) + 30) + "px");
        this._nameDiv.css('top', parseInt(headingStart.y()) + "px");
        this._nameDiv.css('width',"100px");
        this._nameDiv.css('height',"25px");
        this._nameDiv.addClass("name-container-div");
        var nameSpan = $("<span></span>");
        nameSpan.text(self._model.getActionName());
        nameSpan.addClass("name-span");
        nameSpan.attr("contenteditable", "true");
        nameSpan.attr("spellcheck", "false");
        nameSpan.focus();
        nameSpan.blur();
        this._nameDiv.append(nameSpan);
        $(svgWrappingHtml).append(this._nameDiv);
        // Container for connector action body
        var contentGroup = D3utils.group(connectorActionGroup);
        contentGroup.attr('id', "contentGroup");

        nameSpan.on("change paste keyup", function (e) {
            self._model.setActionName($(this).text());
        }).on("keydown", function (e) {
            // Check whether the Enter key has been pressed. If so return false. Won't type the character
            if (e.keyCode === 13) {
                return false;
            }
        });

        this._contentGroup = contentGroup;

        var contentRect = D3utils.rect(contentStart.x(), contentStart.y(),
            this._viewOptions.contentWidth, this._viewOptions.contentHeight, 0, 0,
            contentGroup).classed("connector-action-content", true);

        this._contentRect = contentRect;
        contentRect.attr("fill", "#fff");

        var onExpandCollapse = function () {
            var connectorActionBBox = self.getBoundingBox();
            var visibility = contentGroup.node().getAttribute("display");
            if (visibility == "none") {
                contentGroup.attr("display", "inline");
                // connector action content is expanded. Hence expand connector action BBox
                connectorActionBBox.h(connectorActionBBox.h() + self._minizedHeight);

                // Changing icon if the collapse.
                headingCollapseIcon.classed("headingExpandIcon", true);
                headingCollapseIcon.classed("headingCollapsedIcon", false);
            }
            else {
                contentGroup.attr("display", "none");
                // connector action content is folded. Hence decrease connector action BBox height and keep the minimized size
                self._minizedHeight =  parseFloat(contentRect.attr('height'));
                connectorActionBBox.h(connectorActionBBox.h() - self._minizedHeight);

                // Changing icon if the collapse.
                headingCollapseIcon.classed("headingExpandIcon", false);
                headingCollapseIcon.classed("headingCollapsedIcon", true);
            }
        };

        // On click of collapse icon hide/show connector action body
        headingCollapseIcon.on("click", onExpandCollapse);
        headingRect.on("click", onExpandCollapse);

        // On click of delete icon
        headingDeleteIcon.on("click", function () {
            log.debug("Clicked delete button");
            self._model.remove();
        });

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
            },
            view: this
        };

        this._annotationView = AnnotationView.createAnnotationPane(annotationProperties);

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
            },
            view: this
        };

        this._argumentsView = ArgumentsView.createArgumentsPane(argumentsProperties, diagramRenderingContext);

        // Creating return type pane.
        var returnTypeProperties = {
            model: this._model,
            activatorElement: headingReturnTypesIcon.node(),
            paneAppendElement: this.getChildContainer().node().ownerSVGElement.parentElement,
            viewOptions: {
                position: new Point(parseFloat(this.getChildContainer().attr("x")) + parseFloat(this.getChildContainer().attr("width")) - 1,
                    this.getChildContainer().attr("y"))
            },
            view: this
        };

        this._returnTypePaneView = new ReturnTypesPaneView(returnTypeProperties);
        this._returnTypePaneView.createReturnTypePane(diagramRenderingContext);

        this.getBoundingBox().on("height-changed", function(dh){
            var newHeight = parseFloat(this._contentRect.attr('height')) + dh;
            this._contentRect.attr('height', (newHeight < 0 ? 0 : newHeight));
        }, this);

        this.getBoundingBox().on("right-edge-moved", function(dw){
            var transformX = this._headerIconGroup.node().transform.baseVal.consolidate().matrix.e;
            var transformY = this._headerIconGroup.node().transform.baseVal.consolidate().matrix.f;
            this._headerIconGroup.node().transform.baseVal.getItem(0).setTranslate(transformX + dw, transformY);
            this._contentRect.attr('width', parseFloat(this._contentRect.attr('width')) + dw);
            this._headingRect.attr('width', parseFloat(this._headingRect.attr('width')) + dw);

            // repositioning annotation editor view
            this._annotationView.move({dx: dw});
            // repositioning argument editor view
            this._argumentsView.move({dx: dw});
            // repositioning return type pane view
            this._returnTypePaneView.move({dx: dw});

            // If the bounding box of the connector action go over the svg's current width
            if (this.getBoundingBox().getRight() > this._parentView.getSVG().width()) {
                this._parentView.setSVGWidth(this.getBoundingBox().getRight() + 60);
            }
        }, this);

        if (_.isUndefined(this._defaultWorker)) {
            var defaultWorkerOpts = {};
            _.set(defaultWorkerOpts, 'container', contentGroup.node());
            _.set(defaultWorkerOpts, 'centerPoint', this._viewOptions.defaultWorker.center.clone().move(150, 0));
            _.set(defaultWorkerOpts, 'title', 'ActionWorker');
            this._defaultWorker = new DefaultWorkerView(defaultWorkerOpts);
        }
        this._defaultWorker.render();

        // Set the workerLifeLineMargin to the end of the default worker
        this.getWorkerLifeLineMargin().setPosition(this._defaultWorker.getBoundingBox().getRight());
        this.getHorizontalMargin().setPosition(this._defaultWorker.getBoundingBox().getBottom());

        this.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
            self._defaultWorker.getBottomCenter().y(self._defaultWorker.getBottomCenter().y() + dy);
            var newDropZoneHeight = self._defaultWorker.getBottomCenter().y() - self._defaultWorker.getTopCenter().y();
            self.getStatementContainer().getBoundingBox().h(newDropZoneHeight, true);
            self.getStatementContainer().changeDropZoneHeight(newDropZoneHeight);
            self.getBoundingBox().h(this.getBoundingBox().h() + dy);
        });

        this.trigger("defaultWorkerViewAddedEvent", this._defaultWorker);
        this.initActionLevelDropTarget();
        this.renderStatementContainer();

        // If the statement container moved, we move the default worker accordingly
        this.listenTo(this.getStatementContainer().getBoundingBox(), 'width-changed', function (dw) {
            self.getDefaultWorker().getBoundingBox().w(self.getDefaultWorker().getBoundingBox().w() + dw);
            self.getDefaultWorker().move(dw/2, 0);
        });

        this.getWorkerLifeLineMargin().listenTo(this._defaultWorker.getBoundingBox(), 'right-edge-moved', function (dx) {
            self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + dx);
        });

        log.debug("Rendering Connector Action View");
        this.getModel().accept(this);
        //Removing all the registered 'child-added' event listeners for this model.
        //This is needed because we are not unregistering registered event while the diagram element deletion.
        //Due to that, sometimes we are having two or more view elements listening to the 'child-added' event of same model.
        this._model.off('child-added');
        this._model.on('child-added', function(child){
            self.visit(child);
            // Show/Hide scrolls.
            self._showHideScrolls(self.getChildContainer().node().ownerSVGElement.parentElement, self.getChildContainer().node().ownerSVGElement);
        });

        var operationButtons = [headingAnnotationIcon.node(), headingArgumentsIcon.node(),
            headingReturnTypesIcon.node()];

        // Closing the shown pane when another operation button is clicked.
        _.forEach(operationButtons, function (button) {
            $(button).click(function(event){
                event.stopPropagation();
            });

            $(button).click(function () {
                _.forEach(operationButtons, function (buttonToClick) {
                    if (button !== buttonToClick && $(buttonToClick).data("showing-pane") == "true") {
                        $(buttonToClick).click();
                    }
                });
            });
        });

        this.getBoundingBox().on("moved", function(offset){
            var currentTransform = this._connectorActionGroup.attr("transform");
            this._connectorActionGroup.attr("transform", (!_.isNil(currentTransform) ? currentTransform : "") +
                " translate(" + offset.dx + ", " + offset.dy + ")");

            // Reposition the connector action name container
            var newDivPositionVertical = parseInt(this._nameDiv.css("top")) + offset.dy;
            this._nameDiv.css("top", newDivPositionVertical + "px");
        }, this);
    }

    /**
     * Shows and hide the custom scrolls depending on the amount scrolled.
     * @param {Element} container - The container of the SVG. i.e the parent of the SVG.
     * @param {Element} svgElement - The SVG element.
     */
    _showHideScrolls(container, svgElement) {
        // Creating scroll panes.
        var leftScroll = $(container).find(".service-left-scroll").get(0);
        var rightScroll = $(container).find(".service-right-scroll").get(0);

        // Setting heights of the scrolls.
        $(leftScroll).height($(container).height());
        $(rightScroll).height($(container).height());

        // Positioning the arrows of the scrolls.
        $(leftScroll).find("i").css("padding-top", ($(container).height() / 2) - (parseInt($(leftScroll).find("i").css("font-size"), 10) / 2) + "px");
        $(rightScroll).find("i").css("padding-top", ($(container).height() / 2) - (parseInt($(rightScroll).find("i").css("font-size"), 10) / 2) + "px");

        // Showing/Hiding scrolls.
        if (Math.abs($(container).width() - $(svgElement).width()) < 5) {
            // If the svg width is less than or equal to the container, then no need to show the arrows.
            $(leftScroll).hide();
            $(rightScroll).hide();
        } else {
            // If the svg width is greater than the width of the container...
            if ($(container).scrollLeft() == 0) {
                // When scrollLeft is 0, means that it is already scrolled to the left corner.
                $(rightScroll).show();
                $(leftScroll).hide();
            } else if ($(container).scrollLeft() == parseInt($(svgElement).width(), 10) -
                parseInt($(container).width(), 10)) {
                // When scrolled all the way to the right.
                $(leftScroll).show();
                $(rightScroll).hide();
            } else {
                // When scrolled to the middle.
                $(leftScroll).show();
                $(rightScroll).show();
            }
        }
    }

    /**
     * Render statement container
     */
    renderStatementContainer() {
        var statementContainerOpts = {};
        var self = this;
        _.set(statementContainerOpts, 'model', this._model);
        _.set(statementContainerOpts, 'topCenter', this._defaultWorker.getTopCenter());
        _.set(statementContainerOpts, 'bottomCenter', this._defaultWorker.getBottomCenter());
        _.set(statementContainerOpts, 'width', this._defaultWorker.width());
        _.set(statementContainerOpts, 'container', this._defaultWorker.getContentArea().node());
        _.set(statementContainerOpts, 'toolPalette', this.toolPalette);
        _.set(statementContainerOpts, 'offset', {top: 40, bottom: 40});
        this._statementContainer = new StatementContainer(statementContainerOpts);

        this.listenTo(this.getStatementContainer(), 'statement-container-height-adjusted', function (dh) {
            var childrenLength = self.getStatementContainer().getManagedStatements().length;
            var deltaMove = self.getDeltaMove(self.getDeepestChild(self, dh), dh, childrenLength);
            self.getHorizontalMargin().setPosition(self.getHorizontalMargin().getPosition() + deltaMove);
        });
        this._statementContainer.render(this.diagramRenderingContext);
    }

    initActionLevelDropTarget() {
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
                    // IMPORTANT: override connector action node's default validation logic
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
            //TODO : Remove this and set currentScope/connector action properly
            self.diagramRenderingContext.currentConnectorAction = self;

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
    }

    getConnectorWorkerViewList() {
        return this._connectorWorkerViewList;
    }

    /**
     * @inheritDoc
     * @returns {_defaultWorker}
     */
    getDefaultWorker() {
        return this._defaultWorker;
    }

    canVisitConnectorAction(connectorAction) {
        return true;
    }

    canVisitConnectorDeclaration(connectorDeclaration) {
        return true;
    }

    /**
     * Calls the render method for a connector declaration.
     * @param {ConnectorDeclaration} connectorDeclaration - The connector declaration model.
     */
    visitConnectorDeclaration(connectorDeclaration) {
        var lastLifeLine = this.getLastLifeLine();
        var lastConnectorLifeLine = this.getConnectorWorkerViewList()[this.getLastConnectorLifeLineIndex()];
        var self = this;
        var centerPoint = new Point(lastLifeLine.getBoundingBox().getRight(), lastLifeLine.getTopCenter().y());
        centerPoint.move(this._viewOptions.LifeLineCenterGap, 0);
        var connectorContainer = this._contentGroup.node();
        var connectorDeclarationView = new ConnectorDeclarationView({
                model: connectorDeclaration,
                container: connectorContainer,
                parentView: this,
                messageManager: this.messageManager,
                lineHeight: this._defaultWorker.getTopCenter().absDistInYFrom(this._defaultWorker.getBottomCenter()),
                centerPoint: centerPoint
        });
        this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;

        connectorDeclarationView._rootGroup.attr('id', '_' +connectorDeclarationView._model.id);
        connectorDeclarationView.render();

        // Creating property pane
        var editableProperty = {
            propertyType: "text",
            key: "ConnectorDeclaration",
            model: connectorDeclarationView._model,
            getterMethod: connectorDeclarationView._model.getConnectorExpression,
            setterMethod: connectorDeclarationView._model.setConnectorExpression,
            getDisplayTitle: connectorDeclarationView._model.getConnectorVariable
        };
        connectorDeclarationView.createPropertyPane({
            model: connectorDeclarationView._model,
            lifeLineGroup:connectorDeclarationView._rootGroup,
            editableProperties: editableProperty
        });

        if (_.isNil(lastConnectorLifeLine)) {
            // This is the first connector we are adding
            connectorDeclarationView.listenTo(this.getWorkerLifeLineMargin(), 'moved', function (offset) {
                self.moveActionLevelConnector(this, offset);
            });
        } else {
            // There are already added connectors
            // Previously added connector stop listening to bounding box move.
            // Based on this event we increase the service container width
            lastConnectorLifeLine.stopListening(lastConnectorLifeLine.getBoundingBox(), 'right-edge-moved');
            connectorDeclarationView.listenTo(lastConnectorLifeLine.getBoundingBox(), 'right-edge-moved', function (offset) {
                self.moveActionLevelConnector(this, offset);
            });
        }

        /* If the adding connector (connectorDeclarationView) goes out of this action definition's view,
         then we need to expand this action definition's view. */
        if (connectorDeclarationView.getBoundingBox().getRight() > this.getBoundingBox().getRight()) {
            this._parentView.getLifeLineMargin().setPosition(this._parentView.getLifeLineMargin().getPosition()
                + this._viewOptions.LifeLineCenterGap);
            this.setContentMinWidth(connectorDeclarationView.getBoundingBox().getRight());
            this.setHeadingMinWidth(connectorDeclarationView.getBoundingBox().getRight());
        }

        var connectorBBox = connectorDeclarationView.getBoundingBox();
        connectorDeclarationView.listenTo(connectorBBox, 'right-edge-moved', function (offset) {
            if (connectorBBox.getRight() > self.getBoundingBox().getRight()) {
                self._parentView.getLifeLineMargin().setPosition(self._parentView.getLifeLineMargin().getPosition() + self._viewOptions.LifeLineCenterGap);
                self.setContentMinWidth(connectorBBox.getRight());
                self.setHeadingMinWidth(connectorBBox.getRight());
            }
        }, connectorDeclarationView);

        connectorDeclarationView.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
            connectorDeclarationView.getBoundingBox().h(connectorDeclarationView.getBoundingBox().h() + dy);
        });

        this._connectorWorkerViewList.push(connectorDeclarationView);
        this.trigger("childConnectorViewAddedEvent", connectorDeclarationView);
    }

    /**
     * Visit the worker declaration
     * @param {WorkerDeclaration} workerDeclaration
     */
    visitWorkerDeclaration(workerDeclaration) {
        var self = this;
        // If the default worker, we skip
        if (!workerDeclaration.isDefaultWorker()) {
            var container = this._contentGroup.node();
            var lastWorkerIndex = this.getLastWorkerLifeLineIndex();
            var lastWorker = this.getConnectorWorkerViewList()[lastWorkerIndex];
            var newWorkerPosition = lastWorkerIndex === -1 ? 0 : lastWorkerIndex + 1;
            var centerPoint = undefined;
            if (newWorkerPosition === 0) {
                centerPoint = new Point(this._defaultWorker.getBoundingBox().getRight(),
                    this._defaultWorker.getTopCenter().y());
                centerPoint.move(this._viewOptions.LifeLineCenterGap, 0);
            } else {
                centerPoint = new Point(this._connectorWorkerViewList[lastWorkerIndex].getBoundingBox().getRight(),
                    this._connectorWorkerViewList[lastWorkerIndex].getTopCenter().y());
                centerPoint.move(this._viewOptions.LifeLineCenterGap, 0);
            }
            var lineHeight = this.getDefaultWorker().getBottomCenter().y() - centerPoint.y();
            var workerDeclarationOptions = {
                model: workerDeclaration,
                container: container,
                centerPoint: centerPoint,
                toolPalette: this.toolPalette,
                messageManager: this.messageManager,
                diagramRenderContext: this.getDiagramRenderingContext(),
                line: {height: lineHeight},
                title: workerDeclaration.getWorkerName()
            };
            var workerDeclarationView = new WorkerDeclarationView(workerDeclarationOptions);
            workerDeclarationView.setParent(this);
            workerDeclarationView.render();

            // Creating Expression Editor
            var editableProperty = {
                propertyType: "text",
                key: "WorkerDeclaration",
                model: workerDeclarationView._model,
                getterMethod: workerDeclarationView._model.getWorkerDeclarationStatement,
                setterMethod: workerDeclarationView._model.setWorkerDeclarationStatement,
                getDisplayTitle: workerDeclarationView._model.getWorkerName
            };
            workerDeclarationView.createPropertyPane({
                model: workerDeclarationView._model,
                lifeLineGroup:workerDeclarationView._rootGroup,
                editableProperties: editableProperty
            });

            var statementContainer = workerDeclarationView.renderStatementContainer({offset: {top: 40, bottom: 50}});
            this.diagramRenderingContext.getViewModelMap()[workerDeclaration.id] = workerDeclarationView;

            workerDeclarationView.listenTo(this.getHorizontalMargin(), 'moved', function (dy) {
                workerDeclarationView.getBottomCenter().y(workerDeclarationView.getBottomCenter().y() + dy);
                var newDropZoneHeight = workerDeclarationView.getBottomCenter().y() - workerDeclarationView.getTopCenter().y();
                workerDeclarationView.getStatementContainer().getBoundingBox().h(newDropZoneHeight, true);
                workerDeclarationView.getStatementContainer().changeDropZoneHeight(newDropZoneHeight);
            });

            workerDeclarationView.listenTo(statementContainer, 'statement-container-height-adjusted', function (dh) {
                var childrenLength = workerDeclarationView.getStatementContainer().getManagedStatements().length;
                var deltaMove = self.getDeltaMove(self.getDeepestChild(self, dh), dh, childrenLength);
                self.getHorizontalMargin().setPosition(self.getHorizontalMargin().getPosition() + deltaMove);
            });

            // Set the workerLifeLineMargin to the right edge of the newly added worker
            this.getWorkerLifeLineMargin().setPosition(workerDeclarationView.getBoundingBox().getRight());

            if (newWorkerPosition > 0) {
                // There are already added workers
                this.getWorkerLifeLineMargin().stopListening(this.getConnectorWorkerViewList()[lastWorkerIndex].getBoundingBox());
                workerDeclarationView.listenTo(lastWorker.getBoundingBox(), 'right-edge-moved', function (offset) {
                    self.moveActionLevelWorker(workerDeclarationView, offset);
                });
            } else {
                this.getWorkerLifeLineMargin().stopListening(this._defaultWorker.getBoundingBox());
            }
            this.getWorkerLifeLineMargin().listenTo(workerDeclarationView.getBoundingBox(), 'right-edge-moved', function (offset) {
                self.getWorkerLifeLineMargin().setPosition(self.getWorkerLifeLineMargin().getPosition() + offset);
            });

            if (lastWorkerIndex === this.getConnectorWorkerViewList().length -1 &&
                workerDeclarationView.getBoundingBox().getRight() > this.getBoundingBox().getRight()) {
                // Worker is added as the last element for the ConnectorWorkerViewList.
                // Only Connectors are there at the moment
                this._parentView.getLifeLineMargin().setPosition(this._parentView.getLifeLineMargin().getPosition() + this._viewOptions.LifeLineCenterGap);
                this.setContentMinWidth(workerDeclarationView.getBoundingBox().getRight());
                this.setHeadingMinWidth(workerDeclarationView.getBoundingBox().getRight());
            }

            if (_.isEqual(newWorkerPosition, 0)) {
                workerDeclarationView.listenTo(this.getDefaultWorker().getBoundingBox(), 'right-edge-moved', function (dx) {
                    workerDeclarationView.getBoundingBox().move(dx, 0);
                });
            }

            statementContainer.listenTo(workerDeclarationView.getBoundingBox(), 'left-edge-moved', function (dx) {
                statementContainer.getBoundingBox().move(dx, 0);
            });

            this._connectorWorkerViewList.splice(newWorkerPosition, 0, workerDeclarationView);
        }
    }

    /**
     * setting connector action container height and setting the height for the bounding box
     * @param height
     */
    setConnectorActionContainerHeight(height) {
        this._connectorActionGroup.attr("height", height);
        this._contentRect.attr("height", height);
        this._defaultWorker.setHeight(height - this._viewOptions.totalHeightGap);
        this.getBoundingBox().h(height);
    }

    getLastLifeLine() {
        if(this.getConnectorWorkerViewList().length > 0 ){
            return _.last(this.getConnectorWorkerViewList());
        }
        else{
            return this.getDefaultWorker();
        }
    }

    /**
     * get the Statement View List of the the connector action
     * @returns [_statementExpressionViewList] {Array}
     */
    getStatementExpressionViewList() {
        return this._statementExpressionViewList;
    }

    /**
     * Y distance from one connector action's end point to next connector action's start point
     * @returns {number}
     */
    getGapBetweenConnectorActions() {
        return 25;
    }

    /**
     * Y distance from one statement's end point to next statement's start point
     * @returns {number}
     */
    getGapBetweenStatements() {
        return 10;
    }

    /**
     * Height of the action's heading
     * @returns {number}
     */
    getConnectorActionHeadingHeight() {
        return this._viewOptions.heading.height;
    }

    /**
     * Minimum width of the content area
     * @returns {number} Minimum content width
     */
    getContentMinWidth() {
        return this._viewOptions.contentMinWidth;
    }

    /**
     * Set Minimum width of the content area
     * @param {number} minWidth - Minimum width
     */
    setContentMinWidth(minWidth) {
        this._viewOptions.contentMinWidth = minWidth;
    }

    /**
     * Set Minimum width of the heading
     * @param {number} minWidth - Minimum width
     */
    setHeadingMinWidth(minWidth) {
        this._viewOptions.heading.minWidth = minWidth;
    }

    /**
     * Minimum width of the heading
     * @returns {number} Minimum Heading Width
     */
    getHeadingMinWidth() {
        return this._viewOptions.heading.minWidth;
    }

    /**
     * Shrink or Expand the Connector action view
     * @param {number} dw - delta width
     * @returns {boolean} - Shrink or expanded
     */
    ShrinkOrExpand(dw) {
        if (this.getBoundingBox().w() + dw > this._viewOptions.contentMinWidth) {
            this.getBoundingBox().w(this.getBoundingBox().w() + dw);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return statement container
     * @return {StatementContainerView}
     */
    getStatementContainer() {
        return this._statementContainer;
    }

    /**
     * Move the action level connector
     * @param {BallerinaView} connectorView - Connector View
     * @param {number} offset - Move offset
     */
    moveActionLevelConnector(connectorView, offset) {
        connectorView.getBoundingBox().move(offset, 0);
    }

    /**
     * Move the action level worker
     * @param {BallerinaView} workerView - Worker View
     * @param {number} offset - Move offset
     */
    moveActionLevelWorker(workerView, offset) {
        workerView.getBoundingBox().move(offset, 0);
    }

    /**
     * Get the worker lifeline margin
     * @return {Axis}
     */
    getWorkerLifeLineMargin() {
        return this._workerLifelineMargin;
    }

    /**
     * Set the worker lifeline margin
     * @param {Axis} workerLifeLineMargin
     */
    setWorkerLifeLineMargin(workerLifeLineMargin) {
        this._workerLifelineMargin = workerLifeLineMargin;
    }

    /**
     * Set the horizontal margin
     * @param {Axis} horizontalMargin - horizontal margin
     */
    setHorizontalMargin(horizontalMargin) {
        this._horizontalMargin = horizontalMargin;
    }

    /**
     * Get the horizontal margin
     * @return {Axis|*}
     */
    getHorizontalMargin() {
        return this._horizontalMargin;
    }

    getDeepestChild(currentWorker, dy) {
        var self = this;
        var lastChildArr = [];

        this.getConnectorWorkerViewList().forEach(function (worker) {
            if (worker instanceof WorkerDeclarationView) {
                // if (worker.getModel().id === currentWorker.getModel().id && dy < 0) {
                //     // TODO: Refactor logic
                //     // Child we are removing, have not removed from the view list yet
                //     lastChildArr.push(worker.getStatementContainer().getManagedStatements()[worker.getStatementContainer().getManagedStatements() - 2]);
                // } else {
                //     lastChildArr.push(_.last(worker.getStatementContainer().getManagedStatements()));
                // }
                // TODO: We need to rewrite this logic due to the current limitation of the element remove in statement container
                lastChildArr.push(_.last(worker.getStatementContainer().getManagedStatements()));
            }
        });

        var sortedLastChildArr = _.sortBy(lastChildArr, function (child) {
            var stmtView = _.isNil(child) ? undefined : self.getDiagramRenderingContext().getViewOfModel(child);
            return _.isNil(stmtView) ? -1 : stmtView.getBoundingBox().getBottom();
        });

        var deepestChildStatement = _.last(sortedLastChildArr);
        var defaultWorkerLastChild = _.last(this.getStatementContainer().getManagedStatements());

        if (!_.isNil(deepestChildStatement) && _.isNil(defaultWorkerLastChild)) {
            return deepestChildStatement;
        } else if (!_.isNil(deepestChildStatement) && !_.isNil(defaultWorkerLastChild)) {
            if (this.getDiagramRenderingContext().getViewOfModel(deepestChildStatement).getBoundingBox().getBottom()
                > this.getDiagramRenderingContext().getViewOfModel(defaultWorkerLastChild).getBoundingBox().getBottom()) {
                return deepestChildStatement;
            } else {
                return defaultWorkerLastChild;
            }
        } else {
            return defaultWorkerLastChild;
        }
    }

    getDeltaMove(deepestChild, dy, childrenLength) {
        var deltaMove = 0;
        if (dy < 0) {
            if (_.isNil(deepestChild) || childrenLength === 1) {
                deltaMove = dy;
            } else {
                deltaMove = -(this.getHorizontalMargin().getPosition() -
                this.getDiagramRenderingContext().getViewOfModel(deepestChild).getBoundingBox().getBottom() - this._offsetLastStatementGap);
            }
        } else {
            deltaMove = dy;
        }
        return deltaMove;
    }

    getLastWorkerLifeLineIndex() {
        var index = _.findLastIndex(this.getConnectorWorkerViewList(), function (lifeLine) {
            return lifeLine instanceof WorkerDeclarationView;
        });
        return index;
    }

    getLastConnectorLifeLineIndex() {
        var index = _.findLastIndex(this.getConnectorWorkerViewList(), function (lifeLine) {
            return lifeLine instanceof ConnectorDeclarationView;
        });
        return index;
    }

    getWorkerViews() {
        var workers = _.filter(this.getConnectorWorkerViewList(), function (view) {
            return BallerinaASTFactory.isWorkerDeclaration(view.getModel());
        });
        return workers;
    }

    getConnectorViews() {
        var connectors = _.filter(this.getConnectorWorkerViewList(), function (view) {
            return BallerinaASTFactory.isConnectorDeclaration(view.getModel());
        });
        return connectors;
    }
}

// TODO move variable types into constant class
var variableTypes = ['message', 'boolean', 'string', 'int', 'float', 'long', 'double', 'json', 'xml'];

export default ConnectorActionView;
    

