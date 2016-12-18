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
define(['lodash', 'log', 'd3', 'd3utils', 'jquery', './canvas', './point', './../ast/service-definition',
        './client-life-line', './resource-definition-view', 'ballerina/ast/ballerina-ast-factory', './axis', './connector-declaration-view'],
    function (_, log, d3, D3utils, $, Canvas, Point, ServiceDefinition, ClientLifeLine, ResourceDefinitionView, BallerinaASTFactory, Axis, ConnectorDeclarationView) {

        /**
         * The view to represent a service definition which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {ServiceDefinition} args.model - The service definition model.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var ServiceDefinitionView = function (args) {
            Canvas.call(this, args);

            this._connectorViewList =  _.get(args, "connectorViewList", []);
            this._viewOptions.LifeLineCenterGap = 180;
            this._resourceViewList = _.get(args, "resourceViewList", []);
            this._parentView = _.get(args, "parentView");
            this._viewOptions.offsetTop = _.get(args, "viewOptionsOffsetTop", 50);
            this._viewOptions.topBottomTotalGap = _.get(args, "viewOptionsTopBottomTotalGap", 100);
            //set initial height for the service container svg
            this._totalHeight = 170;
            _.set(this._viewOptions, 'client.center', new Point(100, 50));
            //set initial connector margin for the service
            this._lifelineMargin = new Axis(210, false);

            if (_.isNil(this._model) || !(this._model instanceof ServiceDefinition)) {
                log.error("Service definition is undefined or is of different type." + this._model);
                throw "Service definition is undefined or is of different type." + this._model;
            }

            if (_.isNil(this._container)) {
                log.error("Container for service definition is undefined." + this._container);
                throw "Container for service definition is undefined." + this._container;
            }
            this.init();
        };

        ServiceDefinitionView.prototype = Object.create(Canvas.prototype);
        ServiceDefinitionView.prototype.constructor = ServiceDefinitionView;
        // TODO move variable types into constant class
        var variableTypes = ['message','connection','string','int','exception'];

        ServiceDefinitionView.prototype.init = function(){
            //Registering event listeners
            this.listenTo(this._model, 'childVisitedEvent', this.childVisitedCallback);
            this.listenTo(this._parentView, 'childViewAddedEvent', this.childViewAddedCallback);
            this.listenTo(this._model, 'childRemovedEvent', this.childViewRemovedCallback);
        };

        ServiceDefinitionView.prototype.childVisitedCallback = function (child) {

        };

        ServiceDefinitionView.prototype.childViewAddedCallback = function (child) {
            if (BallerinaASTFactory.isServiceDefinition(child)) {
                if (child !== this._model) {
                    log.info("[Eventing] Service view added : ");
                }
            }
        };

        ServiceDefinitionView.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof ServiceDefinition) {
                this._model = model;
            } else {
                log.error("Service definition is undefined or is of different type." + model);
                throw "Service definition is undefined or is of different type." + model;
            }
        };

        ServiceDefinitionView.prototype.setContainer = function (container) {
            if (!_.isNil(container)) {
                this._container = container;
            } else {
                log.error("Container for service definition is undefined." + container);
                throw "Container for service definition is undefined." + container;
            }
        };

        ServiceDefinitionView.prototype.addToResourceViewList = function (view) {
            if (!_.isNil(view)) {
                //stop listening to current last resource view - if any
                if(!_.isEmpty(this._resourceViewList)){
                    this.stopListening(_.last(this._resourceViewList).getBoundingBox(), 'bottom-edge-moved');
                }
                this._resourceViewList.push(view);

                // set lifeline bottom point
                this._clientLifeLine.getBottomCenter().y(view.getBoundingBox().getBottom()
                    + this._clientLifeLine.getContentOffset().bottom);

                // listen to new last resource view
                this.listenTo(_.last(this._resourceViewList).getBoundingBox(), 'bottom-edge-moved',
                    this.onLastResourceBottomEdgeMoved);
            }
        };

        ServiceDefinitionView.prototype.onLastResourceBottomEdgeMoved = function (dy) {
            this._clientLifeLine.getBottomCenter().move(0, dy);
        };

         ServiceDefinitionView.prototype.setChildContainer = function (svg) {
            if (!_.isNil(svg)) {
                this._childContainer = svg;
            }
         };

        ServiceDefinitionView.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        ServiceDefinitionView.prototype.getModel = function () {
            return this._model;
        };

        ServiceDefinitionView.prototype.getContainer = function () {
            return this._container;
        };

        ServiceDefinitionView.prototype.getResourceViewList = function () {
            return this._resourceViewList;
        };

        ServiceDefinitionView.prototype.getChildContainer = function () {
            return this._rootGroup;
        };

        ServiceDefinitionView.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        ServiceDefinitionView.prototype.getClientTopCenter = function () {
            return this._clientLifeLine.getTopCenter();
        };

        /**
         * Rendering the view of the service definition.
         * @returns {Object} - The svg group which the service definition view resides in.
         */
        ServiceDefinitionView.prototype.render = function (diagramRenderingContext) {
            this.diagramRenderingContext = diagramRenderingContext;
            this.drawAccordionCanvas(this._container, this._viewOptions, this._model.id, this._model.type.toLowerCase(), this._model._serviceName);
            var divId = this._model.id;
            var currentContainer = $('#' + divId);
            this._container = currentContainer;

            // Creating client lifeline.

            var clientCenter = _.get(this._viewOptions, 'client.center');
            var lifeLineArgs = {};
            _.set(lifeLineArgs, 'container', this._rootGroup.node());
            _.set(lifeLineArgs, 'centerPoint', clientCenter);

            this._clientLifeLine = new ClientLifeLine(lifeLineArgs);
            this._clientLifeLine.render();
            this.getModel().accept(this);
            var self = this;
            this._model.on('child-added', function (child) {
                self.visit(child);
                self._model.trigger("childVisitedEvent", child);
            });

            var annotationButton = this._createAnnotationButton(this.getChildContainer().node());
             var variableButton = this._createVariableButton(this.getChildContainer().node());

            var variableProperties = {
                model: this._model,
                editableProperties: [{
                    propertyType: "text",
                    key: "Service Name",
                    model: this._model,
                    getterMethod: this._model.getServiceName,
                    setterMethod: this._model.setServiceName
                },
                    {
                        propertyType: "text",
                        key: "Base Path",
                        model: this._model,
                        getterMethod: this._model.getBasePath,
                        setterMethod: this._model.setBasePath
                    }],
                activatorElement: variableButton.node(),
                paneAppendElement: _.first($(this._container).children()),
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

        ServiceDefinitionView.prototype.renderVariables = function (variableDeclarationsList, variablePaneWrapper, serviceModel) {

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
                    $(removeBtn).click(serviceModel, function () {
                        var varList = serviceModel.getVariableDeclarations();
                        var varType = $($(this.parentNode.getElementsByTagName('label'))[0]).text();
                        var varIdentifier = $($(this.parentNode.getElementsByTagName('input'))[0]).val();
                        var index = self.checkExistingVariables(varList, varType, varIdentifier);

                        if (index != -1) {
                            serviceModel.data.getVariableDeclarations().splice(index, 1);
                        }

                        log.info($(variable).val());

                        if (variablePaneWrapper.children().length > 1) {
                            variablePaneWrapper.children()[variablePaneWrapper.children().length - 1].remove()
                        }
                        self.renderVariables(variableDeclarationsList, variablePaneWrapper, serviceModel);
                    });

                    // variable edit onclick
                    $(variable).click(serviceModel, function (serviceModel) {
                        log.info('Variable edit');
                        // ToDo : Render variables here
                    });
                }
            }
        };

        ServiceDefinitionView.prototype.checkExistingVariables = function (variableList, variableType, variableIdentifier) {
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

        ServiceDefinitionView.prototype.createVariablePane = function (args) {
            var activatorElement = _.get(args, "activatorElement");
            var serviceModel = _.get(args, "model");
            var paneElement = _.get(args, "paneAppendElement");
            var variableList = serviceModel.getVariableDeclarations();

            if (_.isNil(activatorElement)) {
                log.error("Unable to render property pane as the html element is undefined." + activatorElement);
                throw "Unable to render property pane as the html element is undefined." + activatorElement;
            }

            var variablePaneWrapper = $('<div id="variableSection" class="service-variable-pane"/>').appendTo($(paneElement));
            var variableForm = $('<form></form>').appendTo(variablePaneWrapper);
            var variableSelect = $("<select/>").appendTo(variableForm);
            var variableText = $("<input id='inputbox' placeholder='&nbsp;Variable Name'/>").appendTo(variableForm);
            for(var typeCount = 0;typeCount < variableTypes.length; typeCount ++){
                var selectOption = $("<option value="+ variableTypes[typeCount]+">"+variableTypes[typeCount] +
                    "</option>").appendTo($(variableSelect));
            }
            var addVariable = $("<button type='button'>+</button>").appendTo(variableForm);

            this.renderVariables(variableList,variablePaneWrapper,serviceModel);
            var self = this;

            $(addVariable).click(serviceModel, function (serviceModel) {

                // ToDo add variable name validation
                var variableList = serviceModel.data.getVariableDeclarations();

                //filtering empty variable identifier and existing variables
                if ($(variableText).val() != "" &&
                    self.checkExistingVariables(variableList, $(variableSelect).val(), $(variableText).val()) == -1) {

                    var variable = BallerinaASTFactory.createVariableDeclaration();

                    //pushing new variable declaration
                    variable.setType($(variableSelect).val());
                    variable.setIdentifier($(variableText).val());
                    serviceModel.data.getVariableDeclarations().push(variable);

                    //remove current variable list
                    if (variablePaneWrapper.children().length > 1) {
                        variablePaneWrapper.children()[variablePaneWrapper.children().length - 1].remove()
                    }
                    self.renderVariables(variableList, variablePaneWrapper, serviceModel);
                }
            });

            $(activatorElement).click(serviceModel, function (serviceModel) {
                if(paneElement.children[1].style.display== "none" || paneElement.children[1].style.display == "") {
                    paneElement.children[1].style.display = "inline";
                } else {
                    paneElement.children[1].style.display = "none";
                }
            });
        };

        ServiceDefinitionView.prototype.canVisitServiceDefinition = function (serviceDefinition) {
            return true;
        };

        ServiceDefinitionView.prototype.visitServiceDefinition = function (serviceDefinition) {

        };

        ServiceDefinitionView.prototype.canVisitResourceDefinition = function (resourceDefinition) {
            return false;
        };

        /**
         * Calls the render method for a resource definition.
         * @param {ResourceDefinition} resourceDefinition - The resource definition model.
         */
        ServiceDefinitionView.prototype.visitResourceDefinition = function (resourceDefinition) {
            log.info("Visiting resource definition");
            var resourceContainer = this.getChildContainer();
            // If more than 1 resource
            if (this.getResourceViewList().length > 0) {
                var prevView = this.getResourceViewList().pop(this.getResourceViewList().length - 1);
                var prevResourceHeight = prevView.getBoundingBox().h();
                var prevResourceY = prevView.getBoundingBox().y();
                var newY = prevResourceHeight + prevResourceY + prevView.getGapBetweenResources();
                var viewOpts = { topLeft: new Point( 50, newY)};
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition,container: resourceContainer,
                    toolPalette: this.toolPalette, messageManager: this.messageManager, viewOptions: viewOpts, parentView: this});
            }
            else{
                var resourceDefinitionView = new ResourceDefinitionView({model: resourceDefinition, container: resourceContainer,
                    toolPalette: this.toolPalette,messageManager: this.messageManager, parentView: this});
            }
            this.diagramRenderingContext.getViewModelMap()[resourceDefinition.id] = resourceDefinitionView;

            this.addToResourceViewList(resourceDefinitionView);

            this.listenTo(resourceDefinitionView, 'childConnectorViewAddedEvent', this.childConnectorViewAddedCallback);
            this.listenTo(resourceDefinitionView, 'defaultWorkerViewAddedEvent',this.defaultWorkerViewAddedCallback);
            resourceDefinitionView.render(this.diagramRenderingContext);

            //setting height of the service view
            var childView = this.diagramRenderingContext.getViewModelMap()[resourceDefinition.id];
            var staticHeights = childView.getGapBetweenResources();
            this._totalHeight = this._totalHeight + childView.getBoundingBox().h() + staticHeights;
            this.setServiceContainerHeight(this._totalHeight);

            //setting client lifeline's height. Value is calculated by reducing required amount of height from the total height of the service.
            // this.setClientLifelineHeight(this._totalHeight);

            this.trigger("childViewAddedEvent", resourceDefinition);
        };

        /**
         * callback function for connector view added event
         * @param connectorView
         */
        ServiceDefinitionView.prototype.childConnectorViewAddedCallback = function (connectorView) {
            this.updateLifelineMargin(connectorView);
        };

        /**
         * callback function for default worker view added event
         * @param defaultWorkerView
         */
        ServiceDefinitionView.prototype.defaultWorkerViewAddedCallback = function (defaultWorkerView) {
            this.updateLifelineMargin(defaultWorkerView);
        };

        /**
         * updates lifeline margin of this service
         * @param lifeLineView
         */
        ServiceDefinitionView.prototype.updateLifelineMargin = function (lifeLineView) {
            var centerX = lifeLineView.getBoundingBox().getTopCenterX();
            if (centerX > this.getLifelineMargin().getPosition()) {
                this.getLifelineMargin().setPosition(centerX);
            }
        };


        ServiceDefinitionView.prototype.canVisitConnectorDeclaration = function (connectorDeclaration) {
            return true;
        };

        /**
         * Calls the render method for a connector declaration.
         * @param connectorDeclaration
         */
        ServiceDefinitionView.prototype.visitConnectorDeclaration = function (connectorDeclaration) {
            var connectorContainer = this.getChildContainer().node(),
                connectorOpts = {
                    model: connectorDeclaration,
                    container: connectorContainer,
                    parentView: this,
                    lineHeight: this.getBoundingBox().h() - this._viewOptions.topBottomTotalGap
                },
                connectorDeclarationView,
                center;

            center = new Point(this.getLifelineMargin().getPosition(), this._viewOptions.offsetTop).move(this._viewOptions.LifeLineCenterGap, 0);
            _.set(connectorOpts, 'centerPoint', center);
            connectorDeclarationView = new ConnectorDeclarationView(connectorOpts);
            this.diagramRenderingContext.getViewModelMap()[connectorDeclaration.id] = connectorDeclarationView;
            this._connectorViewList.push(connectorDeclarationView);

            connectorDeclarationView.render();
            connectorDeclarationView.setParent(this);
            connectorDeclarationView.listenTo(this.getLifelineMargin(), 'moved', this.updateConnectorPositionCallback);
        };

        /**
         * updates connector position
         * @param dx
         */
        ServiceDefinitionView.prototype.updateConnectorPositionCallback = function (dx) {
            // "this" will be a connector instance
            this.position(dx, 0);
            this.getBoundingBox().move(dx, 0);
        };


        /**
         * Sets height of the client lifeline
         * @param height
         */
        ServiceDefinitionView.prototype.setClientLifelineHeight = function (height) {
            this._clientLifeLine.setHeight(height);
        };

        ServiceDefinitionView.prototype._createAnnotationButtonPane = function (annotationButton) {
            var paneWidth = 400;
            var paneHeadingHeight = annotationButton.attr("r") * 2;
            var paneContentHeight = 300;
            var wrapperDivClass = "service-annotation-wrapper";
            var headerWrapperDivClass = "service-annotation-header-wrapper";
            var contentWrapperDivClass = "service-annotation-content-wrapper";
            var headerTickDivClass = "service-annotation-header-tick";

            var data = [
                {
                    annotationType: "BasePath",
                    annotationValue: this._model.getBasePath(),
                    setterMethod: this._model.setBasePath
                },
                {
                    annotationType: "ServiceName",
                    annotationValue: this._model.getServiceName(),
                    setterMethod: this._model.setServiceName
                },
                {
                    annotationType: "Source:interface",
                    annotationValue: this._model.getSource().interface,
                    setterMethod: this._model.setSource
                }
            ];
            $(annotationButton.node()).click({model: this._model}, function (event) {

                if (_.isNil($(annotationButton.node()).data("showing")) || $(annotationButton.node()).data("showing") == "false") {
                    $(annotationButton.node()).data("showing", true);
                } else {
                    return;
                }

                var model = event.data.model;
                event.stopPropagation();

                var annotationButtonRectHoverEvent = $(annotationButton.node()).next().hover;
                $($(annotationButton.node()).next()).unbind("hover");

                annotationButton.style("opacity", 1);

                var divSvgWrapper = annotationButton.node().ownerSVGElement.parentElement;

                var paneStartingX = annotationButton.attr("cx") - paneWidth;
                var paneStartingY = annotationButton.attr("cy") - annotationButton.attr("r");

                // Heading background.
                var headingBackground = d3.select(annotationButton.node().parentElement).insert("rect", ":first-child")
                    .attr("x", paneStartingX)
                    .attr("y", paneStartingY)
                    .attr("width", paneWidth)
                    .attr("height", paneHeadingHeight)
                    .classed("service-annotation-header-pane", true);

                var annotationEditorWrapper = $("<div/>", {
                    class: wrapperDivClass
                }).width(paneWidth).offset({top: 72.325, left: 1174.33}).appendTo(divSvgWrapper);

                // Creating header content.
                var headerWrapper = $("<div/>", {
                    class: headerWrapperDivClass,
                    click: function (event) {
                        event.stopPropagation();
                    }
                }).height(paneHeadingHeight).appendTo(annotationEditorWrapper);

                var annotationTypeDropDown = $("<select />").appendTo(headerWrapper);
                _.forEach(data, function (annotation) {
                    $("<option />", {
                        value: annotation.annotationType,
                        text: annotation.annotationType
                    }).appendTo(annotationTypeDropDown);
                });

                var annotationValueInput = $("<input type='text' value='" + data[0].annotationValue + "'>").appendTo(headerWrapper);
                $(annotationTypeDropDown).change(function () {
                    var selectedAnnotationType = $(this).val();
                    var selectedAnnotation = _.filter(data, function (annotation) {
                        return annotation.annotationType == selectedAnnotationType;
                    });
                    annotationValueInput.val(_.first(selectedAnnotation).annotationValue);
                });

                // Add/Done button
                var tickSpan = $("<span/>", {
                    class: headerTickDivClass + " fw-stack fw-lg"
                }).appendTo(headerWrapper);

                var tickSquare = $("<i/>", {
                    class: "fw fw-square fw-stack-2x",
                    css: {
                        opacity: 0.4
                    }
                }).appendTo(tickSpan);

                var tickCheck = $("<i/>", {
                    class: "fw fw-check fw-stack-1x fw-inverse"
                }).appendTo(tickSpan);

                $(tickCheck).hover(
                    function () {
                        $(tickSquare).css("opacity", 1);
                    },
                    function () {
                        $(tickSquare).css("opacity", 0.4);
                    }
                );


                $(tickSpan).click(function () {
                    var selectedDropdownAnnotationType = $(annotationTypeDropDown).val();
                    var newAnnotationValue = $(annotationValueInput).val();
                    var annotationToUpdate = _.first(_.filter(data, function (annotation) {
                        return annotation.annotationType == selectedDropdownAnnotationType;
                    }));
                    annotationToUpdate.setterMethod.call(model, newAnnotationValue);
                });

                $(window).click(function () {
                    $(headingBackground.node()).remove();
                    $(annotationEditorWrapper).remove();
                    $(annotationButton.node()).data("showing", "false");

                    $(this).unbind("click");
                });

                // Creating content.
                var contentWrapper = $("<div/>", {
                    class: contentWrapperDivClass
                }).width(paneWidth).height(paneHeadingHeight).offset({
                    top: 72.325 + paneHeadingHeight,
                    left: 1174.33
                }).appendTo(annotationEditorWrapper);

                // using JSON.stringify pretty print capability:
                var editorJson = "{";
                _.forEach(data, function (annotation, index) {
                    editorJson += "\"" + annotation.annotationType + "\":\"" + annotation.annotationValue + "\"";
                    if (index != data.length - 1) {
                        editorJson += ",";
                    }
                });
                editorJson += "}";

                log.debug(editorJson);
                var str = JSON.stringify(JSON.parse(editorJson), undefined, 4);

                $("<textarea/>", {
                    val: str,
                    css: {
                        width: paneWidth,
                        "max-height": paneContentHeight
                    }
                }).appendTo(contentWrapper);

                // TODO : Correctly implement
                $(contentWrapper).remove();
            });
        };

        ServiceDefinitionView.prototype._createAnnotationButton = function (serviceContentSvg) {
            var svgDefinitions = d3.select(serviceContentSvg).append("defs");

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

            var outerBoxPadding = parseInt($(serviceContentSvg.parentElement).css("padding"), 10);
            // xPosition = Width of the outer div - padding of outer box - radius of the annotation button - 20(additional value).
            var xPosition = $(serviceContentSvg.parentElement.parentElement).prev().width() - outerBoxPadding - 18.675 - 20;
            // yPosition = (2 X radius of annotation button) + additional distance.
            var yPosition = 75;

            var annotationIconGroup = D3utils.group(d3.select(serviceContentSvg));

            var annotationIconBackgroundCircle = D3utils.circle(xPosition, yPosition, 18.675, annotationIconGroup)
                .classed("annotation-icon-background-circle", true);

            var annotationIconRect = D3utils.centeredRect(new Point(xPosition, yPosition), 18.67, 18.67, 0, 0, annotationIconGroup)
                .classed("annotation-icon-rect", true);

            // Positioning the icon when window is zoomed out or in.
            $(window).resize(function () {
                var outerBoxPadding = parseInt($(serviceContentSvg.parentElement).css("padding"), 10);

                // xPosition = Width of the outer div - padding of outer box - radius of the annotation button - 20(additional value).
                var xPosition = $(serviceContentSvg.parentElement.parentElement).prev().width() - outerBoxPadding - 18.675 - 20;

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

        ServiceDefinitionView.prototype._createVariableButton = function (serviceContentSvg) {
            var svgDefinitions = d3.select(serviceContentSvg).append("defs");

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

            var outerBoxPadding = parseInt($(serviceContentSvg.parentElement).css("padding"), 10);
            // xPosition = Width of the outer div - padding of outer box - radius of the annotation button - 20(additional value).
            var xPosition = $(serviceContentSvg.parentElement.parentElement).prev().width() - outerBoxPadding - 18.675 - 20;

            var variableIconGroup = D3utils.group(d3.select(serviceContentSvg));

            var variableIconBackgroundCircle = D3utils.circle(xPosition, 30, 18.675, variableIconGroup)
                .classed("variable-icon-background-circle", true);

            var variableIconRect = D3utils.centeredRect(new Point(xPosition, 30), 18.67, 18.67, 0, 0, variableIconGroup)
                .classed("variable-icon-rect", true);

            // Positioning the icon when window is zoomed out or in.
            $(window).resize(function () {
                var outerBoxPadding = parseInt($(serviceContentSvg.parentElement).css("padding"), 10);
                // xPosition = Width of the outer div - padding of outer box - radius of the annotation button - 20(additional value).
                var xPosition = $(serviceContentSvg.parentElement.parentElement).prev().width() - outerBoxPadding - 18.675 - 20;

                $(variableIconBackgroundCircle.node()).remove();
                $(variableIconRect.node()).remove();

                variableIconBackgroundCircle = D3utils.circle(xPosition, 30, 18.675, variableIconGroup)
                    .classed("variable-icon-background-circle", true);

                variableIconRect = D3utils.centeredRect(new Point(xPosition, 30), 18.67, 18.67, 0, 0, variableIconGroup)
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

        /**
         * set the lifeline margin
         * @param position
         */
        ServiceDefinitionView.prototype.setLifelineMargin = function (position) {
            this._lifelineMargin.setPosition(position);
        };

        /**
         * get the lifeline margin
         * @returns {Axis|*}
         */
        ServiceDefinitionView.prototype.getLifelineMargin = function () {
            return this._lifelineMargin;
        };

        return ServiceDefinitionView;
    });