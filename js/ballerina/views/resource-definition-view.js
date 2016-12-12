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
        './point', './life-line', './action-processor-view', './connector-declaration-view', './statement-view-factory', 'ballerina/ast/ballerina-ast-factory'],
    function (_, log, d3, $, D3utils, BallerinaView, ResourceDefinition,
              Point, LifeLine,ActionProcessor,ConnectorDeclarationView, StatementViewFactory, Ballerina) {

        /**
         * The view to represent a resource definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ResourceDefinition} args.model - The resource definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ResourceDefinitionView = function (args) {
            this._model = _.get(args, 'model');
            this._container = _.get(args, 'container');
            this._viewOptions = _.get(args, 'viewOptions', {});
            this._connectorViewList =  [];
            this._defaultResourceLifeLine = undefined;
            this._statementViewList = [];
            this._defaultActionProcessor = undefined;
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
            this._viewOptions.centerPoint = _.get(args, "viewOptions.centerPoint", {});
            this._viewOptions.centerPoint.x = _.get(args, "viewOptions.centerPoint.x", 50);
            this._viewOptions.centerPoint.y = _.get(args, "viewOptions.centerPoint.y", 100);

            // View options for height and width of the heading box.
            this._viewOptions.heading = _.get(args, "viewOptions.heading", {});
            this._viewOptions.heading.height = _.get(args, "viewOptions.heading.height", 25);
            this._viewOptions.heading.width = _.get(args, "viewOptions.heading.width", 1000);

            // View options for height and width of the resource icon in the heading box.
            this._viewOptions.heading.icon = _.get(args, "viewOptions.heading.icon", {});
            this._viewOptions.heading.icon.height = _.get(args, "viewOptions.heading.icon.height", 25);
            this._viewOptions.heading.icon.width = _.get(args, "viewOptions.heading.icon.width", 25);

            this._viewOptions.contentCollapsed = _.get(args, "viewOptions.contentCollapsed", false);
            this._viewOptions.contentWidth = _.get(args, "viewOptions.contentWidth", 1000);
            this._viewOptions.contentHeight = _.get(args, "viewOptions.contentHeight", 360);
            this._viewOptions.collapseIconWidth = _.get(args, "viewOptions.collaspeIconWidth", 1025);


            BallerinaView.call(this);
            this.init();
        };

        ResourceDefinitionView.prototype = Object.create(BallerinaView.prototype);
        ResourceDefinitionView.prototype.constructor = ResourceDefinitionView;

        ResourceDefinitionView.prototype.init = function(){
            //Registering event listeners
            this.listenTo(this._model, 'childVisitedEvent', this.childVisitedCallback);
            this.listenTo(this._parentView, 'childViewAddedEvent', this.childViewAddedCallback);
        };

        ResourceDefinitionView.prototype.canVisitResourceDefinition = function (resourceDefinition) {
            return true;
        };

        ResourceDefinitionView.prototype.childVisitedCallback = function (child) {
            this.trigger("childViewAddedEvent", child);
        };

        ResourceDefinitionView.prototype.childViewAddedCallback = function (child) {
            var ballerinaASTFactory = new Ballerina();
            if(ballerinaASTFactory.isResourceDefinition(child)){
                if(child !== this._model){
                    log.info("[Eventing] Resource view added : ");
                };
            }
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

        ResourceDefinitionView.prototype.setBoundingBox = function (width, height, x, y) {
            if (!_.isNil(width) || !_.isNil(height) || !_.isNil(x) || !_.isNil(y))
                this._boundingBox = {"width": width, "height": height, "x": x, "y": y};
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

        ResourceDefinitionView.prototype.getBoundingBox = function () {
            return this._boundingBox;
        };

        /**
         * @param {BallerinaStatementView} statement
         */
        ResourceDefinitionView.prototype.visitStatement = function (statement) {
            var statementViewFactory = new StatementViewFactory();
            var args = {model: statement, container: this._container, viewOptions: undefined, parent:this};
            var statementView = statementViewFactory.getStatementView(args);

            //TODO: Setting get action invocation's reference connector view
            if(statementViewFactory.isGetActionStatement(statement)){
                _.each(this.getConnectorViewList(), function (view) {
                    if(_.isMatch(statement.getConnector(),view.getModel())) {
                        statementView.setParent(this);
                        statementView.setConnectorView(view);
                        return;
                    }
                });
            }

            // TODO: we need to keep this value as a configurable value and read from constants
            var statementsGap = 40;
            var statementsWidth = 120;
            if (this._statementViewList.length > 0) {
                var lastStatement = this._statementViewList[this._statementViewList.length - 1];
                statementView.setXPosition(lastStatement.getXPosition());
                statementView.setYPosition(lastStatement.getYPosition() + lastStatement.getHeight() + statementsGap);
            } else {
                var x = parseInt(this._defaultResourceLifeLine.getMiddleLine().attr('x1')) - parseInt(statementsWidth/2);
                // First statement is drawn wrt to the position of the default action processor
                var y = this._defaultActionProcessor.getHeight() + this._defaultActionProcessor.getYPosition();
                statementView.setXPosition(x);
                statementView.setYPosition(y + statementsGap);
            }
            this._statementViewList.push(statementView);
            statementView.render();
        };

        /**
         * Rendering the view for resource definition.
         * @returns {group} The svg group which contains the elements of the resource definition view.
         */
        ResourceDefinitionView.prototype.render = function () {
            // Render resource view
            var svgContainer = $(this._container)[0];

            var headingStart = new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y);
            var contentStart = new Point(this._viewOptions.centerPoint.x, this._viewOptions.centerPoint.y + this._viewOptions.heading.height);
            //Main container for a resource
            var resourceGroup = D3utils.group(d3.select(svgContainer));
            resourceGroup.attr("id", "resourceGroup");
            resourceGroup.attr("width", this._viewOptions.heading.width).attr("height", this._viewOptions.heading.height + this._viewOptions.contentHeight);
            resourceGroup.attr("x", headingStart.x()).attr("y", contentStart.y());

            this.setBoundingBox(this._viewOptions.heading.width, this._viewOptions.heading.height + this._viewOptions.contentHeight, headingStart.x(), contentStart.y());

            // Resource related definitions: resourceIcon,collapseIcon
            var def = resourceGroup.append("defs");
            var pattern = def.append("pattern").attr("id", "toggleIcon").attr("width", "100%").attr("height", "100");
            var image = pattern.append("image").attr("xlink:href", "images/down.svg").attr("x", "0").attr("y", "5").attr("width", "14").attr("height", "14");
            var pattern2 = def.append("pattern").attr("id", "resourceIcon").attr("width", "100%").attr("height", "100");
            var image2 = pattern2.append("image").attr("xlink:href", "images/dgm-resource.svg").attr("x", "5").attr("y", "5").attr("width", "14").attr("height", "14");

            // Resource header container
            var headerGroup = D3utils.group(resourceGroup);
            headerGroup.attr("id", "headerGroup");

            var headingRect = D3utils.rect(headingStart.x(), headingStart.y(), this._viewOptions.heading.width, this._viewOptions.heading.height, 0, 0, headerGroup).classed("headingRect", true);

            // Drawing resource icon
            var headingRectIcon = D3utils.rect(headingStart.x(), headingStart.y(), this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("headingRectIcon", true);

            //Resource  heading collapse icon
            var headingCollapseIcon = D3utils.rect(this._viewOptions.collapseIconWidth, headingStart.y(), this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("headingCollapseIcon", true);

            // Create rect for the http method text
            var httpMethodRect = D3utils.rect(headingStart.x() + this._viewOptions.heading.icon.width, headingStart.y() + 0.5, this._viewOptions.heading.icon.width + 25,
                this._viewOptions.heading.icon.height - 1, 0, 0, headerGroup).classed("httpMethodRect", true);

            // Set HTTP Method
            var httpMethodText = D3utils.textElement(headingStart.x() + this._viewOptions.heading.icon.width + 5, headingStart.y() + 4, this._model.getResourceMethod(), headerGroup).classed("httpMethodText", true);
            httpMethodText.attr('dominant-baseline', "text-before-edge");

            // Setting resource path prefix
            var resourcePathPrefix = D3utils.textElement(headingStart.x() + this._viewOptions.heading.icon.width + 55, headingStart.y() + 4, "Path: ", headerGroup).classed("resourcePathPrefix", true);
            resourcePathPrefix.attr('dominant-baseline', "text-before-edge");

            var resourcePath = D3utils.textElement(headingStart.x() + this._viewOptions.heading.icon.width + 90, headingStart.y() + 4, this._model.getResourcePath(), headerGroup);
            resourcePath.attr('dominant-baseline', "text-before-edge");

            // Container for resource body
            var contentGroup = D3utils.group(resourceGroup);
            contentGroup.attr('id', "contentGroup");

            var contentRect = D3utils.rect(contentStart.x(), contentStart.y(), this._viewOptions.contentWidth, this._viewOptions.contentHeight, 0, 0, contentGroup).classed("resource-content", true);

            // On click of collapse icon hide/show resource body
            headingCollapseIcon.on("click", function () {
                //TODO: trigger event when collapsed/opened
                var visibility = contentGroup.node().getAttribute("display");
                if (visibility == "none") {
                    contentGroup.attr("display", "inline");
                }
                else {
                    contentGroup.attr("display", "none");
                    log.debug("Resource collapsed");
                }

            });


            // Drawing default worker
            var defaultWorkerOptions = {
                editable: true,
                centerPoint: {
                    x: contentStart.x() + 130,
                    y: contentStart.y() + 25
                },
                class: "lifeline",
                polygon: {
                    shape: "rect",
                    width: 120,
                    height: 30,
                    roundX: 0,
                    roundY: 0,
                    class: "lifeline-polygon"
                },
                droppableRect: {
                    width: 100,
                    height: 300,
                    roundX: 0,
                    roundY: 0,
                    class: "lifeline-droppableRect"
                },
                line: {
                    height: 280,
                    class: "lifeline-line"
                },
                text: {
                    value: "Resource Worker",
                    class: "lifeline-text"
                },
                child: {
                    value: true
                }
            };

            if (_.isUndefined(this._defaultResourceLifeLine)) {
                this._defaultResourceLifeLine = new LifeLine(contentGroup, defaultWorkerOptions);
            }
            this._defaultResourceLifeLine.render();
           //Drawing processor for resource worker
            var processorCenterPointX = contentStart.x() + 130;
            var processorCenterPointY = contentStart.y() + 75;
            var processorWidth = 120;
            var processorHeight = 30;
            var sourcePointX = processorCenterPointX - (processorWidth/2) -30;
            var sourcePointY = processorCenterPointY;
            var destinationPointX =  processorCenterPointX - (processorWidth/2);
            var  arrowX = destinationPointX - 5;
            var arrowY = processorCenterPointY;

            var processorViewOpts = {
                parent: contentGroup,
                processorWidth: processorWidth,
                processorHeight: processorHeight,
                centerPoint: {
                    x: processorCenterPointX,
                    y: processorCenterPointY
                },
                sourcePoint: {
                    x: sourcePointX,
                    y: sourcePointY
                },
                destinationPoint: {
                    x: destinationPointX,
                    y: sourcePointY
                },
                action: "Start",
                inArrow: true,
                arrowX: arrowX,
                arrowY: arrowY

            };
            var defaultProcessor = new ActionProcessor(processorViewOpts);
            this._defaultActionProcessor = defaultProcessor;
            defaultProcessor.render();

            log.debug("Rendering Resource View");
            this.getModel().accept(this);
        };

        ResourceDefinitionView.prototype.addConnectorViewList = function(view){
            if (!_.isNil(view)) {
                this._connectorViewList.push(view);
            }
        };

        ResourceDefinitionView.prototype.getConnectorViewList = function(){
            return this._connectorViewList;
        };
        /**
         * @inheritDoc
         * @returns {_defaultResourceWorker}
         */
        ResourceDefinitionView.prototype.getDefaultResourceLifeLine = function () {
            return this._defaultResourceLifeLine;
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
            /**
             * @inheritDoc
             */
            log.info("Visiting connector declaration of resource");
            var connectorContainer = this._container.getElementById("contentGroup");
            // If more than 1 connector declaration
            if(this.getConnectorViewList().length > 0 ){
                var prevView = this.getConnectorViewList().pop(this.getConnectorViewList().length-1);
                var newCenterPointX = prevView._viewOptions.connectorCenterPointX+ 180;
                var newCenterPointY = prevView._viewOptions.connectorCenterPointY;
                var viewOpts = {connectorCenterPointX:newCenterPointX, connectorCenterPointY: newCenterPointY};
                var connectorDeclarationView =new ConnectorDeclarationView({model: connectorDeclaration,container: connectorContainer, viewOptions: viewOpts});
            }
            else{
                var defaultResourceLifeline = this.getDefaultResourceLifeLine();
                var resourceViewOpts = defaultResourceLifeline._viewOptions;
                var connectorCenterPointX = resourceViewOpts.centerPoint.x + 180;
                var connectorCenterPointY = resourceViewOpts.centerPoint.y;
                var connectorViewOpts = {connectorCenterPointX: connectorCenterPointX,connectorCenterPointY: connectorCenterPointY};
                var connectorDeclarationView = new ConnectorDeclarationView({model: connectorDeclaration,container: connectorContainer, viewOptions: connectorViewOpts} );
            }

            connectorDeclarationView.render();
            connectorDeclarationView.setParent(this);
            this.addConnectorViewList(connectorDeclarationView);

        };
        ResourceDefinitionView.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ResourceDefinitionView.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ResourceDefinitionView.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ResourceDefinitionView.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ResourceDefinitionView.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ResourceDefinitionView.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ResourceDefinitionView.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        ResourceDefinitionView.prototype.getYPosition = function () {
            // TODO : Implement
        };

        /**
         * get the Statement View List of the the resource
         * @returns [_statementViewList] {Array}
         */
        ResourceDefinitionView.prototype.getStatementViewList = function () {
            return this._statementViewList;
        };

        return ResourceDefinitionView;

    });
