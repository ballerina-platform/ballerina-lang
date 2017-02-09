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
define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'diagram_core',
        './containable-processor-element'],

    function (require, $, d3, Backbone, _, DiagramCore, ContainableProcessorElementView) {

    var createPoint = function (x, y) {
        return new DiagramCore.Models.Point({x: x, y: y});
    };

    var ProcessorView = DiagramCore.Views.ShapeView.extend(
    /** @lends ProcessorView.prototype */
    {
        /**
         * @augments ShapeView
         * @constructs
         * @class Processor Represents the view for processor components in Sequence Diagrams.
         * @param {Object} options Rendering options for the view
         */
        initialize: function (options) {
            DiagramCore.Views.ShapeView.prototype.initialize.call(this, options);
            _.extend(this, _.pick(options, ["center"]));

            if(!_.has(options, 'serviceView')){
                throw "config parent [serviceView] is not provided.";
            }

            this.serviceView = _.get(options, 'serviceView');

            if(!this.serviceView.isPreviewMode()) {
                if (!_.has(this.serviceView, 'toolPalette.dragDropManager')) {
                    throw "dragDropManager is not provided.";
                }
                this.dragDropManager = this.serviceView.toolPalette.dragDropManager;
            }


            this.options = options;
        },

        verticalDrag: function () {
            return false;
        },

        render: function () {
            var processor = this.drawProcessor(this.center, this.modelAttr('title'), this.options);
            var viewObj = this;
            var drag = d3.drag()
                .on("start", function () {
                    viewObj.dragStart(d3.event);
                })
                .on("drag", function () {
                    viewObj.dragMove(d3.event);
                })
                .on("end", function () {
                    viewObj.dragStop();
                });

            this.d3el = processor;
            this.el = processor.node();
            return processor;
        },
        generateInputOutputString: function (params) {
            var line = "";
            for (var x = 0; x < params.length; x++) {
                line += params[x].value;
                if (x < params.length - 1) {
                    line += ", ";
                }
            }

            if (line.length > 20) {
                line = line.substring(0, 15) + " ...";
            }
            return line;
        },

        generateInputOutputString: function (params) {
            var line = "";
            for (var x = 0; x < params.length; x++) {
                line += params[x].value;
                if (x < params.length - 1) {
                    line += ", ";
                }
            }

            if (line.length > 20) {
                line = line.substring(0, 15) + " ...";
            }
            return line;
        },

        addEditableAndDeletable: function(d3Ref, optionsMenuGroup, processorTitleRect,
                                          center, height, width, viewObj) {

            var serviceView = this.serviceView;

            var optionMenuWrapper = d3Ref.draw.rect((center.x() + 10 + width/2), center.y(),
                30,
                58,
                0,
                0,
                optionsMenuGroup, "#f8f8f3").
            attr("style", "stroke: #ede9dc; stroke-width: 1; opacity:0.5; cursor: pointer").
            on("mouseover", function () {
                d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7; cursor: pointer");
            }).
            on("mouseout", function () {
                d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
            });

            var deleteOption = d3Ref.draw.rect((center.x() + 13 + width/2),
                (center.y() + 3),
                24,
                24,
                0,
                0,
                optionsMenuGroup, "url(#delIcon)").
            attr("style", "opacity:0.5; cursor: pointer").
            on("mouseover", function () {
                d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 1; cursor: pointer");
                optionMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7");
            }).
            on("mouseout", function () {
                d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
                optionMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
            });

            var editOption = d3Ref.draw.rect((center.x() + 13 + width/2),
                (center.y() + 31),
                24,
                24,
                0,
                0,
                optionsMenuGroup, "url(#editIcon)").
            attr("style", "opacity:0.5; cursor: pointer").
            on("mouseover", function () {
                d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 1; cursor: pointer");
                optionMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7; cursor: pointer");
            }).
            on("mouseout", function () {
                d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
                optionMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
            });

            // On click of the mediator show/hide the options menu
            processorTitleRect.on("click", function () {
                if (optionsMenuGroup.classed("option-menu-hide")) {
                    optionsMenuGroup.classed("option-menu-hide", false);
                    optionsMenuGroup.classed("option-menu-show", true);

                    if (serviceView.model.selectedOptionsGroup &&
                        (serviceView.model.selectedOptionsGroup !== optionsMenuGroup)) {
                        serviceView.model.selectedOptionsGroup.classed("option-menu-hide", true);
                        serviceView.model.selectedOptionsGroup.classed("option-menu-show", false);
                    }
                    if (serviceView.model.propertyWindow) {
                        serviceView.model.propertyWindow = false;
                        serviceView.model.enableDragZoomOptions();
                        $('#property-pane-svg').empty();
                    }
                    serviceView.model.selectedOptionsGroup = optionsMenuGroup;

                } else {
                    optionsMenuGroup.classed("option-menu-hide", true);
                    optionsMenuGroup.classed("option-menu-show", false);
                    serviceView.model.propertyWindow = false;
                    serviceView.enableDragZoomOptions();
                    serviceView.render();
                }
                d3.event.preventDefault();
                d3.event.stopPropagation();
            });

            // On click of the edit icon will show the properties to to edit
            editOption.on("click", function () {
                if (serviceView.model.propertyWindow) {
                    serviceView.model.propertyWindow = false;
                    serviceView.enableDragZoomOptions();
                    serviceView.render();

                } else {
                    var options = {
                        x: parseFloat(this.getAttribute("x")) + 6,
                        y: parseFloat(this.getAttribute("y")) + 21
                    };

                    serviceView.selectedNode = viewObj.model;
                    serviceView.drawPropertiesPane(d3Ref, options,
                        viewObj.model.get('utils').getMyParameters(
                            viewObj.model),
                        viewObj.model.get('utils').getMyPropertyPaneSchema(
                            viewObj.model));
                }
                d3.event.preventDefault();
                d3.event.stopPropagation();
            });

            deleteOption.on("click", function () {
                // Get the parent of the model and delete it from the parent
                var parentModel = viewObj.model.get("parent");
                var parentModelChildren = parentModel.get("children").models;
                var highestHeight = viewObj.model.get("serviceView").highestLifeline.get("height");
                for (var itr = 0; itr < parentModelChildren.length; itr ++) {
                    if (parentModelChildren[itr].cid === viewObj.model.cid) {
                        if(!_.isUndefined(viewObj.model.inputConnector()) || !_.isUndefined(viewObj.model.outputConnector())) {
                            var inboundId, messagePoints;
                            if(!_.isUndefined(viewObj.model.inputConnector())) {
                                inboundId = viewObj.model.inputConnector().message().destination().cid;
                                messagePoints = serviceView.model.get("diagramSourceElements").models[0].get("children").models;
                                for (var i = 0; i < messagePoints.length; i++) {
                                    if (messagePoints[i].cid === inboundId) {
                                        messagePoints.splice(i, 1);
                                    }
                                }
                            }
                            else if (!_.isUndefined(viewObj.model.outputConnector())) {
                                inboundId = viewObj.model.outputConnector().message().destination().cid;
                                var endpoints = serviceView.model.get("diagramEndpointElements").models;
                                for(var i = 0; i < endpoints.length; i++) {
                                    messagePoints = endpoints[i].get("children").models;
                                    for (var j = 0; j < messagePoints.length; j++) {
                                        if (messagePoints[j].cid === inboundId) {
                                            messagePoints.splice(j, 1);
                                        }
                                    }
                                }
                            }
                        }
                        //reset parent height
                        var currentElementHeight = parentModelChildren[itr].getHeight();
                        parentModel.setHeight(parentModel.getHeight() - currentElementHeight);
                        var parentElement = parentModel;
                        //todo chnage this to get first lifeline type parent instead of Resource
                        while(!(parentElement.get("type") === "Resource")){
                            parentElement = parentElement.get("parent")
                        }
                        // save current life-line height
                        var lifelineHeight = parentElement.getHeight();
                        //removing current processor
                        parentModelChildren.splice(itr, 1);
                        if(lifelineHeight + currentElementHeight >= highestHeight){
                            viewObj.model.get("serviceView").highestLifeline.setHeight(highestHeight - currentElementHeight);
                        }
                        serviceView.render();
                        break;
                    }
                }
            });

            var getPropertyPaneSchema = function (model) {
                return ;
            };

        },

        drawProcessor: function (center, title, prefs) {
            var d3Ref = this.getD3Ref();
            var group = d3Ref.append('g');
            var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");
            var viewObj = this;
            var serviceView = this.serviceView;

            if (this.model.model.type === "UnitProcessor") {

                var height = 0;
                var titleRectHeight = 20;
                if (this.model.get('utils').outputs) {
                    height = this.model.getHeight() + 20;
                } else {
                    height = this.model.getHeight();
                }
                var width = this.model.getWidth();

                var rectBottomXXX = d3Ref.draw.centeredRect(center,
                    this.model.getWidth(),
                    height,
                    0,
                    0,
                    group,
                    this.modelAttr('viewAttributes').colour
                );

                var processorTitleRect = d3Ref.draw.rect((center.x() - this.model.getWidth()/2), center.y(),
                    this.model.getWidth(),
                    titleRectHeight,
                    0,
                    0,
                    group,
                    this.modelAttr('viewAttributes').colour
                );

                this.addEditableAndDeletable(d3Ref, optionsMenuGroup, processorTitleRect,
                    center, height, width, viewObj);

                var mediatorText = d3Ref.draw.textElement(center.x(),
                    (center.y() + titleRectHeight/2),
                    title,
                    group)
                    .classed("mediator-title", true);

                var inputText = d3Ref.draw.textElement(center.x() + 20 - this.model.getWidth()/2,
                    (center.y() + 35),
                    this.generateInputOutputString(this.model.get('utils').getInputParams(this.model)),
                    group)
                    .classed("input-output-text", true);

                var inputTri = d3Ref.draw.inputTriangle(center.x() + 5 - this.model.getWidth()/2,
                    (center.y() + 30),
                    group);

                //this.generateInputOutputString(this.model.get('utils').utils.getInputParams());

                if (this.model.get('utils').outputs) {
                    var outputText = d3Ref.draw.textElement(center.x() + 20 - this.model.getWidth()/2,
                        (center.y() + 58 - this.model.getHeight()/2),
                        this.generateInputOutputString(this.model.get('utils').getOutputParams()),
                        group)
                        .classed("input-output-text", true);
                    var outputTri = d3Ref.draw.outputTriangle(center.x() + 5 - this.model.getWidth()/2,
                        (center.y() + 53 - this.model.getHeight()/2),
                        group);
                    var dashedSeparator =d3Ref.draw.dashedLine(
                        center.x() - this.model.getWidth()/2,
                        center.y() + 10,
                        center.x() + this.model.getWidth()/2,
                        center.y() + 10,
                        "black",
                        group
                    );
                }

                group.rect = rectBottomXXX;
                group.title = mediatorText;
                //this.renderViewForElement(element, opts);
            } else if (this.model.model.type === "DynamicContainableProcessor") {

                var rectBottomXXX = d3Ref.draw.rectWithTitle(center,
                    60,
                    prefs.rect.height,
                    150,
                    200,
                    0,
                    0,
                    group,
                    this.modelAttr('viewAttributes').colour,
                    this.modelAttr('title')
                );
                var middleRect = d3Ref.draw.centeredBasicRect(createPoint(center.x(),
                    center.y()+75), 150, 200 - prefs.rect.height, 0, 0, group);
                middleRect.on("mousedown", function () {
                    var m = d3.mouse(this);
                    this.mouseDown(prefs, center.x(), m[1]);
                }).on('mouseover', function () {
                    viewObj.serviceView.model.selectedNode = viewObj.model;
                    d3.select(this).style("fill", "green").style("fill-opacity", 0.1);
                }).on('mouseout', function () {
                    viewObj.serviceView.model.destinationLifeLine = viewObj.serviceView.model.selectedNode;
                    viewObj.serviceView.model.selectedNode = null;
                    d3.select(this).style("fill-opacity", 0.01);
                }).on('mouseup', function (data) {
                });
                group.rect = rectBottomXXX;
                group.middleRect = middleRect;

                var centerPoint = center;
                var xValue = centerPoint.x();
                var yValue = centerPoint.y();
                //lifeLine.call(drag);
                yValue += 60;
                for (var id in this.modelAttr("children").models) {
                    var processor = this.modelAttr("children").models[id];
                    var processorViewOptions = {
                        model: processor,
                        center: new DiagramCore.Models.Point({x: xValue, y: yValue}),
                        canvas: this.canvas,
                        serviceView: this.serviceView,
                        application: this.application
                    };
                    var processorView = new ProcessorView(processorViewOptions);
                    var processorCenterPoint = createPoint(xValue, yValue);
                    processorView.render("#", processorCenterPoint, "processors");
                    processor.setY(yValue);
                    yValue += processor.getHeight()+ 30;
                }

            } else if (this.model.model.type === "ComplexProcessor") {

                var containableProcessorElementViewArray = [];
                var centerPoint = center;
                var xValue = centerPoint.x();
                var yValue = centerPoint.y();

                var totalHeight=0;
                var maximumWidth = 150;
                var deltaDistance = 0;
                var destinationX = 0;

                for (var id in this.modelAttr("containableProcessorElements").models) {

                    var containableProcessorElement = this.modelAttr("containableProcessorElements").models[id];
                    var opts = {
                        serviceView: this.serviceView,
                        canvas:this.serviceView.d3el,
                        application: this.application,
                        model: containableProcessorElement
                    };
                    var containableProcessorElementView = new ContainableProcessorElementView(opts);
                    var processorCenterPoint = createPoint(xValue, yValue);
                    var elemView = containableProcessorElementView.render("#" + viewObj.serviceView.options.diagram.wrapperId, processorCenterPoint);
                    containableProcessorElement.setY(yValue);
                    containableProcessorElement.setX(xValue);
                    containableProcessorElementViewArray.push(elemView);
                    yValue = yValue+containableProcessorElement.getHeight();
                    totalHeight+=containableProcessorElement.getHeight();

                    var childElementsInContainableProcessorElement = containableProcessorElement.get("children").models;
                    var sourceX = 0;
                    if(childElementsInContainableProcessorElement.length != 0) {
                        for (var child in childElementsInContainableProcessorElement) {
                            if (childElementsInContainableProcessorElement[child].get("outputConnector") != null) {
                                if(childElementsInContainableProcessorElement[child].get("outputConnector").get("message") != null) {
                                    if(childElementsInContainableProcessorElement[child].get("outputConnector").get("message").get("destination") != null) {
                                        var destinationXVal = childElementsInContainableProcessorElement[child].get("outputConnector").get("message").get("destination").get("centerPoint").get("x");
                                        sourceX = childElementsInContainableProcessorElement[child].get("outputConnector").get("message").get("source").get("centerPoint").get("x");
                                        if (destinationX < destinationXVal) {
                                            destinationX = destinationXVal;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (destinationX != 0 && sourceX != 0) {
                        deltaDistance = (destinationX - sourceX) + 30;
                    }

                    if(maximumWidth < containableProcessorElement.getWidth()){
                        maximumWidth = containableProcessorElement.getWidth();
                    }
                }

                var newWidth = maximumWidth + deltaDistance;

                var arrayLength = containableProcessorElementViewArray.length;
                for (var i = 0; i < arrayLength; i++) {

                    var middleRect = containableProcessorElementViewArray[i].middleRect;
                    var rect = containableProcessorElementViewArray[i].rect;
                    var titleRect = containableProcessorElementViewArray[i].titleRect;
                    var text = containableProcessorElementViewArray[i].text;

                    var initWidth = middleRect.attr("width");
                    middleRect.attr("width", newWidth);
                    rect.attr("width", newWidth);

                    var deviation = (maximumWidth - initWidth)/2;

                    middleRect.attr("x", parseInt(middleRect.attr("x")) - deviation);
                    rect.attr("x", parseInt(rect.attr("x")) - deviation);
                    titleRect.attr("x", parseInt(titleRect.attr("x")) - deviation);
                    text.attr("x", parseInt(text.attr("x")) - deviation);

                }

                this.model.setHeight(totalHeight);
                this.model.setWidth(maximumWidth);
            } else if(this.model.model.type === "CustomProcessor") {
                if(!_.isUndefined(this.model.get('utils').init)){
                    this.viewRoot = group;
                    this.model.set('centerPoint', center);
                    this.model.get('utils').init(this, d3Ref);
                }
            } else if (this.model.model.type === 'Action') {
                var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");
                var height = 0;
                height = this.model.getHeight();
                var width = this.model.getWidth();
                var x = (center.x() - this.model.getWidth()/2);
                var y = center.y();
                var rectHeight = 30;
                var rectWidth = this.model.getWidth();

                var processorTitleRect = d3Ref.draw.rect(x,
                    y,
                    rectWidth,
                    rectHeight,
                    0,
                    0,
                    group,
                    this.modelAttr('viewAttributes').colour
                );

                // We need connection staring point only for the processors which have outbound connections.
                if(!_.isUndefined(this.model.model.hasOutputConnection) && this.model.model.hasOutputConnection) {
                    var circle = d3Ref.draw.circle(x + rectWidth, y + rectHeight / 2, rectHeight / 3, group, "#2c3e50");

                    circle.on("mousedown", function () {
                        d3.event.preventDefault();
                        d3.event.stopPropagation();
                        var m = d3.mouse(this);
                        viewObj.serviceView.model.clickedLifeLine = viewObj.model;
                        viewObj.serviceView.model.trigger("messageDrawStart", viewObj.model,
                            new DiagramCore.Models.Point({x: (x + rectWidth), y: m[1]}));
                    }).on('mouseover', function () {
                        viewObj.serviceView.model.selectedNode = viewObj.model;
                        d3.select(this).style("fill", "red").style("fill-opacity", 0.6)
                            .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                        // Update event manager with current active element type for validation
                        // viewObj.dragDropManager.isActivated(viewObj.serviceView.model.selectedNode.attributes.title);
                    }).on('mouseout', function () {
                        //diagram.destinationLifeLine = diagram.selectedNode;
                        viewObj.serviceView.model.selectedNode = null;
                        d3.select(this).style("fill-opacity", 0.01);
                        // Update event manager with out of focus on active element
                        // viewObj.dragDropManager.isActivated("none");
                    }).on('mouseup', function (data) {

                    });
                }

                var mediatorText = d3Ref.draw.textElement(center.x(),
                    (center.y() + height/2),
                    title,
                    group)
                    .classed("mediator-title", true);

                //We will add edit and delete buttons if we have set editable and deletable to true in the processor definition.
                if(!_.isUndefined(this.model.model.editable) && !_.isUndefined(this.model.model.deletable)
                    && this.model.model.editable && this.model.model.deletable) {
                    this.addEditableAndDeletable(d3Ref, optionsMenuGroup, processorTitleRect, center, height, width, viewObj);
                }

                group.rect = rectBottomXXX;
                group.title = mediatorText;
                group.circle = circle;

                var inputMessagePoint = this.model.inputConnector();
                if(!_.isUndefined(inputMessagePoint)){
                    inputMessagePoint.x(center.x() - width/2);
                    inputMessagePoint.y(center.y() + height/2);
                }

                var outputMessagePoint = this.model.outputConnector();
                if(!_.isUndefined(outputMessagePoint)){
                    outputMessagePoint.x(center.x() + width/2);
                    outputMessagePoint.y(center.y() + height/2);
                }
            }

            /*var rect = d3Ref.draw.centeredRect(center, prefs.rect.width, prefs.rect.height, 3, 3, group)
             .classed(prefs.rect.class, true);
             var text = d3Ref.draw.centeredText(center, title, group)
             .classed(prefs.text.class, true);*/

            Object.getPrototypeOf(group).translate = function (dx, dy) {
                this.attr("transform", function () {
                    return "translate(" + [dx, dy] + ")"
                })
            };

            return group;
        }

    });

    return ProcessorView;
});

