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
        './default-worker', './point', './connector-declaration-view',
        './statement-view-factory', 'ballerina/ast/ballerina-ast-factory', './expression-view-factory','./message', './statement-container'],
    function (_, log, d3, $, D3utils, BallerinaView, ResourceDefinition,
              DefaultWorkerView, Point, ConnectorDeclarationView, StatementViewFactory, BallerinaASTFactory, ExpressionViewFactory,
                MessageView, StatementContainer) {

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

            // Center point of the default worker
            this._viewOptions.defaultWorker = _.get(args, "viewOptions.defaultWorker", {});
            this._viewOptions.defaultWorker.offsetTop = _.get(args, "viewOptions.defaultWorker.offsetTop", 50);
            this._viewOptions.defaultWorker.center = _.get(args, "viewOptions.defaultWorker.centerPoint",
                            this._viewOptions.topLeft.clone().move(160, 60));

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
            this._viewOptions.deleteIconWidth = _.get(args, "viewOptions.deleteIconWidth", 1005);

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
        var variableTypes = ['message', 'connection', 'string', 'int', 'exception'];

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
            var messageStart = this._parentView.getClientTopCenter().clone();
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
            this._statementContainer.renderStatement(statement, args);
        };

        ResourceDefinitionView.prototype.visitExpression = function (statement) {
            //var expressionViewFactory = new ExpressionViewFactory();
            //var args = {model: statement, container: this._contentGroup.node(), viewOptions: undefined, parent:this};
            //var expressionView = expressionViewFactory.getExpressionView(args);
            //
            //// TODO: we need to keep this value as a configurable value and read from constants
            //var statementsGap = 40;
            //var expressionWidth = 120;
            //if (this._statementExpressionViewList.length > 0) {
            //    var lastStatement = this._statementExpressionViewList[this._statementExpressionViewList.length - 1];
            //    expressionView.setXPosition(lastStatement.getXPosition());
            //    expressionView.setYPosition(lastStatement.getYPosition() + lastStatement.getHeight() + statementsGap);
            //} else {
            //    var x = this._defaultWorker.getMidPoint() - parseInt(expressionWidth/2);
            //    expressionView.setXPosition(x);
            //    expressionView.setYPosition(y + statementsGap);
            //}
            //this._statementExpressionViewList.push(expressionView);
            //expressionView.render();
        };

        /**
         * Rendering the view for resource definition.
         * @returns {group} The svg group which contains the elements of the resource definition view.
         */
        ResourceDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.diagramRenderingContext = diagramRenderingContext;
            // Render resource view
            var svgContainer = $(this._container)[0];

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

            // Resource related definitions: resourceIcon,collapseIcon
            var def = resourceGroup.append("defs");
            var pattern = def.append("pattern").attr("id", "toggleIcon").attr("width", "100%").attr("height", "100");
            var image = pattern.append("image").attr("xlink:href", "images/down.svg").attr("x", "0")
                .attr("y", "5").attr("width", "14").attr("height", "14");
            var pattern2 = def.append("pattern").attr("id", "resourceIcon").attr("width", "100%").attr("height", "100");
            var image2 = pattern2.append("image").attr("xlink:href", "images/dmg-resource.svg")
                .attr("x", "5").attr("y", "5").attr("width", "14").attr("height", "14");
            var pattern3 = def.append("pattern").attr("id", "deleteIcon").attr("width", "100%").attr("height", "100");
            var image3 = pattern3.append("image").attr("xlink:href", "images/delete.svg")
                .attr("x", "0").attr("y", "5").attr("width", "14").attr("height", "14");

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

            //Resource  heading collapse icon
            var headingCollapseIcon = D3utils.rect(this._viewOptions.collapseIconWidth, headingStart.y(),
                this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("headingCollapseIcon", true);

            //Resource  heading delete icon
            var headingDeleteIcon = D3utils.rect(this._viewOptions.deleteIconWidth, headingStart.y(),
                this._viewOptions.heading.icon.width,
                this._viewOptions.heading.icon.height, 0, 0, headerGroup).classed("headingDeleteIcon", true);


            // Create rect for the http method text
            var httpMethodRect = D3utils.rect(headingStart.x() + this._viewOptions.heading.icon.width, headingStart.y()
                + 0.5, this._viewOptions.heading.icon.width + 25,
                this._viewOptions.heading.icon.height - 1, 0, 0, headerGroup).classed("httpMethodRect", true);

            // Set HTTP Method
            var httpMethodText = D3utils.textElement(headingStart.x()
                + this._viewOptions.heading.icon.width + 5, headingStart.y() + 4, this._model.getResourceMethod(),
                headerGroup).classed("httpMethodText", true);
            httpMethodText.attr('dominant-baseline', "text-before-edge");

            // Setting resource path prefix
            var resourcePathPrefix = D3utils.textElement(headingStart.x() +
                this._viewOptions.heading.icon.width + 55, headingStart.y() + 4, "Path: ",
                headerGroup).classed("resourcePathPrefix", true);
            resourcePathPrefix.attr('dominant-baseline', "text-before-edge");

            var resourcePath = D3utils.textElement(headingStart.x() +
                this._viewOptions.heading.icon.width + 90, headingStart.y() + 4,
                this._model.getResourcePath(), headerGroup);
            resourcePath.attr('dominant-baseline', "text-before-edge");

            // Container for resource body
            var contentGroup = D3utils.group(resourceGroup);
            contentGroup.attr('id', "contentGroup");

            this._contentGroup = contentGroup;

            var contentRect = D3utils.rect(contentStart.x(), contentStart.y(),
                this._viewOptions.contentWidth, this._viewOptions.contentHeight, 0, 0,
                contentGroup).classed("resource-content", true);

            this._contentRect = contentRect;
            contentRect.attr("fill", "#fff");

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

            // On click of delete icon
            headingDeleteIcon.on("click", function () {
                log.info("Clicked delete button");
                var child = self._model;
                var parent = child.parent;
                parent.removeChild(child);
            });

            this.getBoundingBox().on("bottom-edge-moved", function(dy){
                this._contentRect.attr('height', parseFloat(this._contentRect.attr('height')) + dy);
            }, this);

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
            var self = this;
            this._model.on('child-added', function(child){
                self.visit(child);
            });

            var annotationButton = this._createAnnotationButton(this.getChildContainer().node());
            var variableButton = this._createVariableButton(this.getChildContainer().node());

            var variableProperties = {
                model: this._model,
                editableProperties: [{
                    propertyType: "text",
                    key: "Resource Name",
                    model: this._model,
                    getterMethod: this._model.getResourceName,
                    setterMethod: this._model.setResourceName
                },
                    {
                        propertyType: "text",
                        key: "Resource Path",
                        model: this._model,
                        getterMethod: this._model.getResourcePath,
                        setterMethod: this._model.setResourcePath
                    }],
                activatorElement: variableButton.node(),
                paneAppendElement: $(".panel-body").children(),
                viewOptions: {
                    position: {
                        x: parseFloat(variableButton.attr("cx")),
                        y: parseFloat(variableButton.attr("cy")) + parseFloat(variableButton.attr("r"))
                    }
                }
            };
            this.createVariablePane(variableProperties);
            this._createAnnotationButtonPane(annotationButton);
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
                    this.getBoundingBox().h(this.getBoundingBox().h() + dy);
            });
            this._statementContainer.render(this.diagramRenderingContext);
        };

        ResourceDefinitionView.prototype.renderVariables = function (variableDeclarationsList, variablePaneWrapper, resourceModel) {

            if (variableDeclarationsList.length > 0) {
                var variableSetWrapper = $('<div/>').appendTo($(variablePaneWrapper));
                var variableTable = $('<table/>').appendTo(variableSetWrapper);

                for (var variableCount = 0; variableCount < variableDeclarationsList.length; variableCount++) {
                    var currentRaw;
                    if (variableCount % 3 == 0) {
                        currentRaw = $('<tr/>').appendTo(variableTable);
                    }
                    var labelClass = "";
                    if (variableDeclarationsList[variableCount].getType() === "message") {
                        labelClass = "variable-type-message";
                    } else if (variableDeclarationsList[variableCount].getType() === "connection") {
                        labelClass = "variable-type-connection";
                    } else if (variableDeclarationsList[variableCount].getType() === "string") {
                        labelClass = "variable-type-string";
                    } else if (variableDeclarationsList[variableCount].getType() === "int") {
                        labelClass = "variable-type-int";
                    } else {
                        labelClass = "variable-type-exception";
                    }
                    var currentCell = $('<td/>').appendTo(currentRaw);
                    var variable = $("<label for=" + variableDeclarationsList[variableCount].getIdentifier() + " class=" + labelClass + ">" +
                        variableDeclarationsList[variableCount].getType() + "</label>" + "<input readonly maxlength='7' size='7' value=" +
                        variableDeclarationsList[variableCount].getIdentifier() + " class=" + labelClass + ">").appendTo(currentCell);
                    var removeBtn = $('<button class="variable-list">x</button>').appendTo(currentCell);

                    // variable delete onclick
                    var self = this;
                    $(removeBtn).click(resourceModel, function () {
                        var varList = resourceModel.getVariables();
                        var varType = $($(this.parentNode.getElementsByTagName('label'))[0]).text();
                        var varIdentifier = $($(this.parentNode.getElementsByTagName('input'))[0]).val();
                        var index = self.checkExistingVariables(varList, varType, varIdentifier);

                        if (index != -1) {
                            resourceModel.data.getVariables().splice(index, 1);
                        }

                        log.info($(variable).val());

                        if (variablePaneWrapper.children().length > 1) {
                            variablePaneWrapper.children()[variablePaneWrapper.children().length - 1].remove()
                        }
                        self.renderVariables(variableDeclarationsList, variablePaneWrapper, resourceModel);
                    });

                    // variable edit onclick
                    $(variable).click(resourceModel, function (resourceModel) {
                        log.info('Variable edit');
                        // ToDo : Render variables here
                    });
                }
            }
        };

        ResourceDefinitionView.prototype.checkExistingVariables = function (variableList, variableType, variableIdentifier) {
            var index = -1;
            for (var varIndex = 0; varIndex < variableList.length; varIndex++) {
                if (variableList[varIndex].getType() == variableType &&
                    variableList[varIndex].getIdentifier() == variableIdentifier) {
                    index = varIndex;
                    break;
                }
            }
            return index;
        };

        ResourceDefinitionView.prototype._createAnnotationButtonPane = function (annotationButton) {
            var paneWidth = 400;
            var paneHeadingHeight = annotationButton.attr("r") * 2;
            var mainActionWrapper = "main-action-wrapper service-annotation-main-action-wrapper";
            var actionContentWrapper = "svg-action-content-wrapper";
            var actionContentWrapperHeading = "action-content-wrapper-heading service-annotation-wrapper-heading";
            var actionContentDropdownWrapper = "action-content-dropdown-wrapper input-group-btn";
            var actionIconWrapper = "action-icon-wrapper";
            var actionContentWrapperBody = "svg-action-content-wrapper-body service-annotation-details-wrapper";
            var annotationDetailWrapper = "service-annotation-detail-wrapper";
            var annotationDetailCellWrapper = "service-annotation-detail-cell-wrapper";

            // Annotation data of the service.
            var data = [
                {
                    annotationType: "ResourcePath",
                    annotationValue: this._model.getResourcePath(),
                    setterMethod: this._model.setResourcePath
                },
                {
                    annotationType: "ResourceName",
                    annotationValue: this._model.getResourceName(),
                    setterMethod: this._model.setResourceName
                },
                {
                    annotationType: "Resource:Action",
                    annotationValue: ""/*this._model.getSource().interface*/,
                    setterMethod: ""
                }
            ];

            // Showing annotation pane when annotation button is clicked.
            $(annotationButton.node()).click({model: this._model}, function (event) {

                // Show the annotation pane only if annotation pane is closed.
                if (_.isNil($(annotationButton.node()).data("showing"))
                    || $(annotationButton.node()).data("showing") == "false") {
                    $(annotationButton.node()).data("showing", true);
                } else {
                    return;
                }

                var model = event.data.model;

                // Stopping event propagation to element behind.
                event.stopPropagation();

                // Adding darkness to the annotation button.
                var annotationButtonClass = $(annotationButton.node()).attr("class");
                $(annotationButton.node()).removeAttr("class");

                var divSvgWrapper = annotationButton.node().ownerSVGElement.parentElement;

                // Getting the start location for drawing the background.
                var paneStartingX = annotationButton.attr("cx") - paneWidth;
                var paneStartingY = annotationButton.attr("cy") - annotationButton.attr("r");

                // Heading background.
                var headingBackground = d3.select(annotationButton.node().parentElement).insert("rect", ":first-child")
                    .attr("x", paneStartingX)
                    .attr("y", paneStartingY)
                    .attr("width", paneWidth)
                    .attr("height", paneHeadingHeight)
                    .classed("svg-action-content-wrapper-heading", true);

                // Padding value needs to be taken into considering as that starting point of the SVG and its wrapper is
                // not the same. Difference is the padding.
                var paddingOfDivSvgWrapper = parseInt($(divSvgWrapper).css("padding"), 10);

                var annotationEditorWrapper = $("<div/>", {
                    class: mainActionWrapper
                }).width(paneWidth)
                    .offset({top: paneStartingY + paddingOfDivSvgWrapper, left: paneStartingX + paddingOfDivSvgWrapper})
                    .appendTo(divSvgWrapper);

                var annotationActionContentWrapper = $("<div/>", {
                    class: actionContentWrapper
                }).appendTo(annotationEditorWrapper);

                // Creating header content.
                var headerWrapper = $("<div/>", {
                    class: actionContentWrapperHeading
                }).appendTo(annotationActionContentWrapper);

                // Creating a wrapper for the annotation type dropwdown.
                var annotationTypeDropDownWrapper = $("<div/>", {
                    class: actionContentDropdownWrapper
                }).appendTo(headerWrapper);

                // Creating dropdown button element.
                var dropdownClickable = $("<button/>", {
                    class: "btn btn-default dropdown-toggle",
                    text : "Annotation Type"
                }).appendTo(annotationTypeDropDownWrapper);

                // Adding bootstrap attributes to the above button element.
                dropdownClickable.attr("data-toggle", "dropdown")
                    .attr("aria-haspopup", true)
                    .attr("aria-expanded", false);

                var dropdownClickableIcon = $("<i class='icon-caret fw fw-down icon-caret'></i>");

                dropdownClickableIcon.appendTo(dropdownClickable);

                // Creating <ul> tag to add dropdown elements.
                var dropdownElementsWrapper = $("<ul/>", {
                    class: "dropdown-menu"
                }).appendTo(annotationTypeDropDownWrapper);

                // Text input for editing the value of an annotation.
                var annotationValueInput = $("<input/>", {
                    type: "text"
                }).appendTo(headerWrapper);

                // Wrapper for the add and check icon.
                var addIconWrapper = $("<div/>", {
                    class: actionIconWrapper
                }).appendTo(headerWrapper);

                var addButton = $("<span class='fw-stack fw-lg'>" +
                    "<i class='fw fw-square fw-stack-2x'></i>" +
                    "<i class='fw fw-add fw-stack-1x fw-inverse'></i>" +
                    "</span>").appendTo(addIconWrapper);

                // Adding a value to a new annotation.
                $(addButton).click(function() {
                    var annotationValue = annotationValueInput.val();
                    var annotationType = dropdownClickable.text();
                    var annotation = _.first(_.filter(data, function(annotation){
                        return annotation.annotationType == annotationType;
                    }));

                    annotation.annotationValue = annotationValue;

                    // Recreating the annotation details view.
                    createCurrentAnnotationView(data, annotationsContentWrapper);

                    // Re-add elements to dropdown.
                    addAnnotationsToDropdown();
                });

                // Add elements to dropdown.
                addAnnotationsToDropdown();

                // Creating the content editing div.
                var annotationsContentWrapper = $("<div/>", {
                    class: actionContentWrapperBody
                }).appendTo(annotationActionContentWrapper);

                // Creating the annotation details view.
                createCurrentAnnotationView(data, annotationsContentWrapper);

                // If an item in the dropdown(the button and the li elements) is clicked, we nee to allow propagation as
                // the dropdown effect is handle through bootstrap.
                annotationEditorWrapper.click({
                    dropDown: dropdownClickable,
                    dropDownList: dropdownElementsWrapper
                }, function (event) {
                    if (!(event.target == event.data.dropDown.get(0) ||
                        (!_.isNil(event.target.parentElement) &&
                        event.data.dropDownList.get(0) == event.target.parentElement.parentElement))) {
                        event.stopPropagation();
                    }
                });

                // Closing the pop-up. But we should not close the pop-up when clicked on an dropdown element(the button
                // and the li elements) as it is handled through bootstrap.
                $(window).click({dropDownList: dropdownElementsWrapper}, function (event) {
                    if (!(!_.isNil(event.target.parentElement) &&
                        event.data.dropDownList.get(0) == event.target.parentElement.parentElement)) {
                        $(headingBackground.node().remove());
                        annotationEditorWrapper.remove();
                        $(annotationButton.node()).data("showing", "false");
                        $(annotationButton.node()).attr("class", annotationButtonClass);

                        $(event.currentTarget).unbind("click");
                    }
                });

                /**
                 * Adds annotation with values to the dropdown.
                 */
                function addAnnotationsToDropdown() {
                    dropdownElementsWrapper.empty();

                    // Adding dropdown elements.
                    _.forEach(data, function (annotation) {
                        if (_.isEmpty(annotation.annotationValue)) {
                            var dropDownItem = $("<li><a href='#'>" + annotation.annotationType + "</a></li>")
                                .appendTo(dropdownElementsWrapper);

                            // Creating click event when an dropdown value is select.
                            $(dropDownItem).click(function () {
                                var selectedAnnotationType = $(this).text();

                                // Setting the select text value to the dropdown clickable.
                                dropdownClickable.text(selectedAnnotationType);
                                // Appending the dropdown arrow to the button.
                                dropdownClickableIcon.appendTo(dropdownClickable);

                                // Showing the annotation value.
                                var selectedAnnotation = _.filter(data, function (annotation) {
                                    return annotation.annotationType == selectedAnnotationType;
                                });
                                annotationValueInput.val(_.first(selectedAnnotation).annotationValue);
                            });
                        }
                    });
                }

                /**
                 * Creates the annotation detail wrapper and its events.
                 * @param annotationData - The annotation data.
                 * @param wrapper - The wrapper element which these details should be appended to.
                 */
                function createCurrentAnnotationView(annotationData, wrapper) {
                    wrapper.empty();

                    // Creating annotation info
                    _.forEach(annotationData, function (annotation) {
                        if (!_.isEmpty(annotation.annotationValue)) {

                            var annotationWrapper = $("<div/>", {
                                class: annotationDetailWrapper
                            }).appendTo(wrapper);

                            // Creating a wrapper for the annotation type.
                            var annotationTypeWrapper = $("<div/>", {
                                text: annotation.annotationType,
                                class: annotationDetailCellWrapper
                            }).appendTo(annotationWrapper);

                            // Creating a wrapper for the annotation value.
                            var annotationValueWrapper = $("<div/>", {
                                text: ": " + annotation.annotationValue,
                                class: annotationDetailCellWrapper
                            }).appendTo(annotationWrapper);

                            var deleteIcon = $("<i class='fw fw-cancel service-annotation-detail-cell-delete-icon'></i>");

                            deleteIcon.appendTo(annotationValueWrapper);

                            // When an annotation detail is clicked.
                            annotationWrapper.click({
                                clickedAnnotationValueWrapper: annotationValueWrapper,
                                annotation: annotation
                            }, function (event) {
                                var clickedAnnotationValueWrapper = event.data.clickedAnnotationValueWrapper;
                                var annotation = event.data.annotation;
                                // Empty the content inside the annotation value wrapper.
                                clickedAnnotationValueWrapper.empty();

                                // Changing the background
                                annotationWrapper.css("background-color", "#f5f5f5");

                                // Creating the text area for the value of the annotation.
                                var annotationValueTextArea = $("<textarea/>", {
                                    text: annotation.annotationValue,
                                    class: "form-control"
                                }).appendTo(clickedAnnotationValueWrapper);

                                var newDeleteIcon = deleteIcon.clone();

                                // Fixing the delete icon.
                                newDeleteIcon.appendTo(clickedAnnotationValueWrapper);

                                // Adding in-line display block to override the hovering css.
                                newDeleteIcon.css("display", "block");

                                // Resetting of other annotations wrapper which has been used for editing.
                                annotationWrapper.siblings().each(function () {

                                    // Removing the textareas of other annotations and use simple text.
                                    var annotationValueDiv = $(this).children().eq(1);
                                    if (annotationValueDiv.find("textarea").length > 0) {
                                        // Reverting the background color of other annotation editors.
                                        $(this).removeAttr("style");

                                        var annotationVal = ": " + annotationValueDiv.find("textarea").val();
                                        annotationValueDiv.empty().text(annotationVal);

                                        var delIcon = deleteIcon.clone();

                                        delIcon.appendTo(annotationValueDiv);
                                        delIcon.removeAttr("style");
                                    }
                                });
                            });
                        }

                    });
                }
            });
        };

        ResourceDefinitionView.prototype.createVariablePane = function (args) {
            var activatorElement = _.get(args, "activatorElement");
            var resourceModel = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");
            var variableList = resourceModel.getVariables();

            if (_.isNil(activatorElement)) {
                log.error("Unable to render property pane as the html element is undefined." + activatorElement);
                throw "Unable to render property pane as the html element is undefined." + activatorElement;
            }

            var variablePaneWrapper = $('<div id="' + resourceModel.id + '" class="resource-variable-pane"/>').appendTo($(paneElement));
            var variableForm = $('<form></form>').appendTo(variablePaneWrapper);
            var variableSelect = $("<select/>").appendTo(variableForm);
            var variableText = $("<input placeholder='&nbsp;Variable Name'/>").appendTo(variableForm);
            for(var typeCount = 0;typeCount < variableTypes.length; typeCount ++){
                var selectOption = $("<option value="+ variableTypes[typeCount]+">"+variableTypes[typeCount] +
                    "</option>").appendTo($(variableSelect));
            }
            var addVariable = $("<button type='button'>+</button>").appendTo(variableForm);

            this.renderVariables(variableList,variablePaneWrapper,resourceModel);
            var self = this;

            $(addVariable).click(resourceModel, function (resourceModel) {

                // ToDo add variable name validation
                var variableList = resourceModel.data.getVariables();

                //filtering empty variable identifier and existing variables
                if ($(variableText).val() != "" &&
                    self.checkExistingVariables(variableList, $(variableSelect).val(), $(variableText).val()) == -1) {

                    var variable = BallerinaASTFactory.createVariableDeclaration();

                    //pushing new variable declaration
                    variable.setType($(variableSelect).val());
                    variable.setIdentifier($(variableText).val());
                    resourceModel.data.getVariables().push(variable);

                    //remove current variable list
                    if (variablePaneWrapper.children().length > 1) {
                        variablePaneWrapper.children()[variablePaneWrapper.children().length - 1].remove()
                    }
                    self.renderVariables(variableList, variablePaneWrapper, resourceModel);
                }
            });

            $(activatorElement).click(resourceModel, function (resourceModel) {
                for(var iterator = 0;iterator < paneElement.children().length; iterator++) {
                    console.log(paneElement.children()[iterator]);
                    if(paneElement.children()[iterator].className == "resource-variable-pane") {
                        if(paneElement.children()[iterator].id == resourceModel.data.id) {
                            $(paneElement.children()[iterator]).css('top', $(this).position().top);
                            $(paneElement.children()[iterator]).css('left', $(this).position().left - 340);
                            console.log("Panel element Display Status#############################" + paneElement.children()[iterator].style.display);
                            if(paneElement.children()[iterator].style.display== "none" || paneElement.children()[iterator].style.display == "") {
                                paneElement.children()[iterator].style.display = "inline";
                            } else {
                                paneElement.children()[iterator].style.display = "none";
                            }
                            break;
                        }
                    }
                }
            });
        };

        ResourceDefinitionView.prototype._createAnnotationButton = function (resourceContentSvg) {
            var svgDefinitions = d3.select(resourceContentSvg).select("#contentGroup").append("defs");

            var annotationButtonPattern = svgDefinitions.append("pattern")
                .attr("id", "annotationIcon")
                .attr("width", "100%")
                .attr("height", "100%");

            annotationButtonPattern.append("image")
                .attr("xlink:href", "images/annotation.svg")
                .attr("x", 0)
                .attr("y", 0)
                .attr("width", 18.67)
                .attr("height", 18.67);

            // xPosition = Width of the outer div - padding of outer box - radius of the annotation button - 20(additional value).
            //var xPosition = $(resourceContentSvg.parentElement.parentElement).prev().width() - outerBoxPadding - 18.675 - 40;
            var xPosition = d3.selectAll(resourceContentSvg.childNodes).select('.headingCollapseIcon').attr("x") - 25;
            // yPosition = (2 X radius of annotation button) + additional distance.
            //var yPosition = 75;
            var yPosition = parseInt(d3.selectAll(resourceContentSvg.childNodes).select('.resource-content').attr("y")) + 85;

            var annotationIconGroup = D3utils.group(d3.select(resourceContentSvg).select("#contentGroup"));

            var annotationIconBackgroundCircle = D3utils.circle(xPosition, yPosition, 18.675, annotationIconGroup)
                .classed("annotation-icon-background-circle", true);

            var annotationIconRect = D3utils.centeredRect(new Point(xPosition, yPosition), 18.67, 18.67, 0, 0, annotationIconGroup)
                .classed("annotation-icon-rect", true);

            // Positioning the icon when window is zoomed out or in.
            $(window).resize(function () {
                $(annotationIconBackgroundCircle.node()).remove();
                $(annotationIconRect.node()).remove();

                annotationIconBackgroundCircle = D3utils.circle(xPosition, yPosition, 18.675, annotationIconGroup)
                    .classed("annotation-icon-background-circle", true);

                annotationIconRect = D3utils.centeredRect(new Point(xPosition, yPosition), 18.67, 18.67, 0, 0, annotationIconGroup)
                    .classed("annotation-icon-rect", true);
            });

            // Get the hover effect of the circle on the icon hover.
            $(annotationIconRect.node()).hover(
                function () {
                    annotationIconBackgroundCircle.style("opacity", 1);
                },
                function () {
                    $(annotationIconBackgroundCircle.node()).removeAttr("style");
                }
            );

            $(annotationIconRect.node()).click(function (event) {
                $(annotationIconBackgroundCircle.node()).trigger("click");
                event.stopPropagation();
            });

            return annotationIconBackgroundCircle;
        };

        ResourceDefinitionView.prototype._createVariableButton = function (resourceContentSvg) {
            var svgDefinitions = d3.select(resourceContentSvg).select("#contentGroup").append("defs");

            var variableButtonPattern = svgDefinitions.append("pattern")
                .attr("id", "variableIcon")
                .attr("width", "100%")
                .attr("height", "100%");

            variableButtonPattern.append("image")
                .attr("xlink:href", "images/variable.svg")
                .attr("x", 0)
                .attr("y", 0)
                .attr("width", 18.67)
                .attr("height", 18.67);

            // xPosition = Width of the outer div - padding of outer box - radius of the annotation button - 20(additional value).
            //var xPosition = $(resourceContentSvg).width() - outerBoxPadding - 18.675 - 40;
            var xPosition = d3.selectAll(resourceContentSvg.childNodes).select('.headingCollapseIcon').attr("x") - 25;
            var yPosition = parseInt(d3.selectAll(resourceContentSvg.childNodes).select('.resource-content').attr("y")) + 40;

            var variableIconGroup = D3utils.group(d3.select(resourceContentSvg).select("#contentGroup"));

            var variableIconBackgroundCircle = D3utils.circle(xPosition, yPosition, 18.675, variableIconGroup)
                .classed("variable-icon-background-circle", true);

            var variableIconRect = D3utils.centeredRect(new Point(xPosition, yPosition), 18.67, 18.67, 0, 0, variableIconGroup)
                .classed("variable-icon-rect", true);

            // Positioning the icon when window is zoomed out or in.
            $(window).resize(function () {
                $(variableIconBackgroundCircle.node()).remove();
                $(variableIconRect.node()).remove();

                variableIconBackgroundCircle = D3utils.circle(xPosition, yPosition, 18.675, variableIconGroup)
                    .classed("variable-icon-background-circle", true);

                variableIconRect = D3utils.centeredRect(new Point(xPosition, yPosition), 18.67, 18.67, 0, 0, variableIconGroup)
                    .classed("variable-icon-rect", true);
            });

            // Get the hover effect of the circle on the icon hover.
            $(variableIconRect.node()).hover(
                function () {
                    variableIconBackgroundCircle.style("opacity", 1);
                },
                function () {
                    $(variableIconBackgroundCircle.node()).removeAttr("style");
                }
            );

            $(variableIconRect.node()).click(function () {
                $(variableIconBackgroundCircle.node()).trigger("click");
            });

            return variableIconBackgroundCircle;
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
                // TODO : Please add a proper logic for line height calculation in following line.
                connectorOpts = {model: connectorDeclaration, container: connectorContainer, parentView: this, lineHeight:this.getBoundingBox().h() - 95},
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

            this.getBoundingBox().on("bottom-edge-moved", function (dy) {
                this.getBottomCenter().move(0, dy);
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
            return 10;
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

