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

/**
 * SequenceD-Views Module extension.
 *
 * Definition of Backbone Views required for Sequence Diagrams.
 */
var SequenceD = (function (sequenced) {
    var views = sequenced.Views = sequenced.Views || {};
    var toolPaletteWidth = 240;
    var imageHeight = 20;

    var Processor = Diagrams.Views.ShapeView.extend(
        /** @lends Processor.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class Processor Represents the view for processor components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.ShapeView.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID, centerPoint, status) {
                if (status == "processors") {
                    Diagrams.Views.ShapeView.prototype.render.call(this, paperID);
                    var processor = this.drawProcessor(paperID, centerPoint, this.modelAttr('title'), this.options);
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
                }
            },

            updateProcessorProperties: function () {
                diagram.selectedNode = this.model;

                //get processor parameters
                var parameters = [];
                var processorParameters = diagram.selectedNode.parameters.parameters;
                processorParameters.forEach(function (parameter, index) {
                    parameters[index] = parameter.value;
                });

                //get processor definition
                var processorDefinition;
                var type = this.model.type;
                if (type === "LogMediator") {
                    processorDefinition = Processors.manipulators.LogMediator;
                } else if (type === "PayLoadFactoryMediator") {
                    processorDefinition = Processors.manipulators.PayLoadFactoryMediator;
                } else if (type === "InvokeMediator") {
                    processorDefinition = Processors.flowControllers.InvokeMediator;
                }

                ppView.loadPropertyPane(this, processorDefinition, parameters);
            },

            drawProcessor: function (paperID, center, title, prefs) {
                var d3Ref = this.getD3Ref();
                var group = d3Ref.draw.group();
                var propertiesIconGroup = group.append("g").attr("class", "properties-icon circle-hide");
                var deleteIconGroup = group.append("g").attr("class", "close-icon circle-hide");
                var viewObj = this;

                if (this.model.model.type === "UnitProcessor") {
                    var height = this.model.getHeight();
                    var width = this.model.getWidth();
                    var path = "M " + (center.x() + width/2 - 3) + "," + (center.y() - height/2 - 3) + " L " + (center.x() + width/2 + 3) + "," +
                        (center.y() - height/2 + 3) + " M " + (center.x() + width/2 + 3) + "," + (center.y() - height/2 - 3) + " L " +
                        (center.x() + width/2 - 3) + "," + (center.y() - height/2 + 3);

                    var closeCircle = d3Ref.draw.circle((center.x() + width/2), (center.y() - height/2),
                        7, deleteIconGroup).
                        attr("fill", "#95a5a6").
                        attr("style", "stroke: black; stroke-width: 2; opacity:0.8");
                    var propertiesCircle = d3Ref.draw.circle((center.x() + width / 2 - 14), (center.y() - height / 2),
                                                             7, propertiesIconGroup)
                        .attr("fill", "#95a5a6")
                        .attr("style", "stroke: black; stroke-width: 2; opacity:0.8");

                    deleteIconGroup.append("path")
                        .attr("d", path)
                        .attr("style", "stroke: black;fill: transparent; stroke-linecap:round; stroke-width: 1.5;");

                    var rectBottomXXX = d3Ref.draw.centeredRect(center,
                        this.model.getWidth(),
                        this.model.getHeight(),//prefs.rect.height,
                        0,
                        0,
                        group, //element.viewAttributes.colour
                        this.modelAttr('viewAttributes').colour
                    );
                    var mediatorText = d3Ref.draw.centeredText(center,
                        title,
                        group)
                        .classed(prefs.text.class, true);
                    group.rect = rectBottomXXX;
                    group.title = mediatorText;

                    var orderedElements = [rectBottomXXX, mediatorText, deleteIconGroup, propertiesIconGroup];

                    var newGroup = d3Ref.draw.regroup(orderedElements);
                    group.remove();

                    // On click of the mediator show/hide the delete icon
                    rectBottomXXX.on("click", function () {
                        if (deleteIconGroup.classed("circle-hide")) {
                            if (diagram.propertyWindow) {
                                $('#property-pane-svg').empty();
                                diagram.propertyWindow = false;
                            }
                            deleteIconGroup.classed("circle-hide", false);
                            deleteIconGroup.classed("circle-show", true);
                            propertiesIconGroup.classed("circle-hide", false);
                            propertiesIconGroup.classed("circle-show", true);
                            diagram.selectedNode = viewObj.model;
                        } else {
                            deleteIconGroup.classed("circle-hide", true);
                            deleteIconGroup.classed("circle-show", false);
                            propertiesIconGroup.classed("circle-hide", true);
                            propertiesIconGroup.classed("circle-show", false);
                            defaultView.render();
                        }

                        diagram.currentDeleteIconGroup = deleteIconGroup;
                        diagram.currentPropertyIconGroup = propertiesIconGroup;
                        viewObj.updateProcessorProperties();
                    });

                    deleteIconGroup.on("click", function () {
                        // Get the parent of the model and delete it from the parent
                        var parentModelChildren = viewObj.model.get("parent").get("children").models;
                        for (var itr = 0; itr < parentModelChildren.length; itr ++) {
                            if (parentModelChildren[itr].cid === viewObj.model.cid) {

                                parentModelChildren.splice(itr, 1);
                                defaultView.render();
                                break;
                            }
                        }
                    });

                    var getPropertyPaneSchema = function (type) {
                        if (type === "LogMediator") {
                            return Processors.manipulators.LogMediator.propertyPaneSchema;
                        }
                    };

                    propertiesIconGroup.on("click", function () {
                        if (diagram.propertyWindow) {
                            diagram.propertyWindow = false;
                            diagram.previousDeleteIconGroup.classed("circle-hide", true);
                            diagram.previousDeleteIconGroup.classed("circle-show", false);
                            diagram.previousPropertyIconGroup.classed("circle-hide", true);
                            diagram.previousPropertyIconGroup.classed("circle-show", false);
                            defaultView.render();
                        } else {
                            diagram.propertyWindow = true;
                            var options = {
                                x: parseInt(jQuery(event.target).attr("cx")) - 5,
                                y: parseInt(jQuery(event.target).attr("cy")) + 3
                            };
                            defaultView.selectedNode = viewObj.model;
                            defaultView.drawPropertiesPane(d3Ref, options,
                                                           viewObj.model.attributes.parameters.parameters,
                                                           getPropertyPaneSchema(viewObj.model.type));
                        }
                    });

                    group.rect = rectBottomXXX;
                    group.title = mediatorText;
                    //this.renderViewForElement(element, opts);
                } else if (this.model.model.type === "DynamicContainableProcessor") {

                    console.log("Processor added");
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
                    console.log("started");
                    var middleRect = d3Ref.draw.centeredBasicRect(createPoint(center.x(),
                        center.y()+75), 150, 200 - prefs.rect.height, 0, 0, group);
                    middleRect.on("mousedown", function () {
                        var m = d3.mouse(this);
                        this.mouseDown(prefs, center.x(), m[1]);
                    }).on('mouseover', function () {
                        console.log("middle rect detected");
                        diagram.selectedNode = viewObj.model;
                        d3.select(this).style("fill", "green").style("fill-opacity", 0.1);
                    }).on('mouseout', function () {
                        diagram.destinationLifeLine = diagram.selectedNode;
                        diagram.selectedNode = null;
                        d3.select(this).style("fill-opacity", 0.01);
                    }).on('mouseup', function (data) {
                    });
                    console.log(middleRect);
                    group.rect = rectBottomXXX;
                    group.middleRect = middleRect;

                    var centerPoint = center;
                    var xValue = centerPoint.x();
                    var yValue = centerPoint.y();
                    //lifeLine.call(drag);
                    yValue += 60;
                    for (var id in this.modelAttr("children").models) {
                        var processor = this.modelAttr("children").models[id];
                        var processorView = new SequenceD.Views.Processor({model: processor, options: lifeLineOptions});
                        var processorCenterPoint = createPoint(xValue, yValue);
                        processorView.render("#" + defaultView.options.diagram.wrapperId, processorCenterPoint, "processors");
                        processor.setY(yValue);
                        yValue += processor.getHeight()+ 30;
                    }


                } else if (this.model.model.type === "ComplexProcessor") {

                    console.log("Processor added");

                    var containableProcessorElementViewArray = [];


                    var centerPoint = center;
                    var xValue = centerPoint.x();
                    var yValue = centerPoint.y();

                    var totalHeight=0;
                    var maximumWidth = 150;

                    for (var id in this.modelAttr("containableProcessorElements").models) {

                        var containableProcessorElement = this.modelAttr("containableProcessorElements").models[id];
                        var containableProcessorElementView = new SequenceD.Views.ContainableProcessorElement({model: containableProcessorElement, options: lifeLineOptions});
                        var processorCenterPoint = createPoint(xValue, yValue);
                        var elemView = containableProcessorElementView.render("#" + defaultView.options.diagram.wrapperId, processorCenterPoint);
                        containableProcessorElementViewArray.push(elemView);
                        yValue = yValue+containableProcessorElement.getHeight();
                        totalHeight+=containableProcessorElement.getHeight();
                        //yValue += 60;
                        //var processor = this.modelAttr("children").models[id];
                        //var processorView = new SequenceD.Views.Processor({model: processor, options:
                        // lifeLineOptions}); var processorCenterPoint = createPoint(xValue, yValue);
                        // processorView.render("#diagramWrapper", processorCenterPoint); processor.setY(yValue);

                        if(maximumWidth < containableProcessorElement.getWidth()){
                            maximumWidth = containableProcessorElement.getWidth();
                        }
                    }


                    var arrayLength = containableProcessorElementViewArray.length;
                    for (var i = 0; i < arrayLength; i++) {

                        var middleRect = containableProcessorElementViewArray[i].middleRect;
                        var rect = containableProcessorElementViewArray[i].rect;
                        var titleRect = containableProcessorElementViewArray[i].titleRect;
                        var text = containableProcessorElementViewArray[i].text;

                        var initWidth = middleRect.attr("width");
                        middleRect.attr("width", maximumWidth);
                        rect.attr("width", maximumWidth);

                        var deviation = (maximumWidth - initWidth)/2;

                        middleRect.attr("x", parseInt(middleRect.attr("x")) - deviation);
                        rect.attr("x", parseInt(rect.attr("x")) - deviation);
                        titleRect.attr("x", parseInt(titleRect.attr("x")) - deviation);
                        text.attr("x", parseInt(text.attr("x")) - deviation);

                    }

                    this.model.setHeight(totalHeight);
                    this.model.setWidth(maximumWidth);
                } else if(this.model.model.type === "Custom") {
                    if(!_.isUndefined(this.model.model.initMethod)){
                        this.viewRoot = group;
                        this.model.set('center', center);
                        this.model.model.initMethod(this);
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

    var MessageLink = Diagrams.Views.DiagramElementView.extend(
        /** @lends Processor.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class Processor Represents the view for processor components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.DiagramElementView.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID, status) {
                if (status == "messages") {
                    Diagrams.Views.DiagramElementView.prototype.render.call(this, paperID);
                    var d3ref = this.getD3Ref();

                    var line = d3ref.draw.lineFromPoints(this.model.source().centerPoint(), this.model.destination().centerPoint())
                        .classed(this.options.class, true);

                    //this.model.source().on("connectingPointChanged", this.sourceMoved, this);
                    //this.model.destination().on("connectingPointChanged", this.destinationMoved, this);

                    this.d3el = line;
                    this.el = line.node();
                    return this.d3el;
                }
            }

        });

    var LifeLineView = Diagrams.Views.ShapeView.extend(
        /** @lends LifeLineView.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class LifeLineView Represents the view for lifeline components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.ShapeView.prototype.initialize.call(this, options);
                this.listenTo(this.model, 'change:title', this.renderTitle);
            },

            handleDropEvent: function (event, ui) {
            },

            verticalDrag: function () {
                return false;
            },
            horizontalDrag: function () {
                return false;
            },

            renderTitle: function () {
                if (this.d3el) {
                    this.d3el.svgTitle.text(this.model.attributes.title);
                    this.d3el.svgTitleBottom.text(this.model.attributes.title);
                    if (propertyPane && defaultView.model.selectedNode) {
                        var lifeLineDefinition;
                        if (defaultView.model.selectedNode.attributes.cssClass === "resource") {
                            lifeLineDefinition = MainElements.lifelines.ResourceLifeline;
                        } else if (defaultView.model.selectedNode.attributes.cssClass === "endpoint") {
                            lifeLineDefinition = MainElements.lifelines.EndPointLifeline;
                        }

                        if (propertyPane.schema.title === lifeLineDefinition.getSchema().title) {
                            propertyPane.setValue(lifeLineDefinition.getEditableProperties());
                        }
                    }
                }
                if (!propertyPane.getValue().Title) {
                    propertyPane = ppView.createPropertyPane(lifeLineDefinition.getSchema(),
                              lifeLineDefinition.getEditableProperties(defaultView.model.selectedNode.get('title')),
                              defaultView.model.selectedNode);
                }
            },

            render: function (paperID, status, colour) {
                if (status == "processors") {
                    Diagrams.Views.ShapeView.prototype.render.call(this, paperID);
                    thisModel = this.model;
                    var centerPoint = this.modelAttr('centerPoint');
                    var lifeLine = this.drawLifeLine(centerPoint, this.modelAttr('title'), this.options, colour);
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
                    var xValue = centerPoint.x();
                    var yValue = centerPoint.y();

                    lifeLine.call(drag);
                    yValue += 60;

                    var initialHeight = parseInt(lifeLine.line.attr("y2")) - parseInt(lifeLine.line.attr("y1")) ;
                    var totalIncrementedHeight = 0;

                    for (var id in this.modelAttr("children").models) {

                        if (this.modelAttr("children").models[id] instanceof SequenceD.Models.Processor) {
                            var processor = this.modelAttr("children").models[id];
                            var processorView = new SequenceD.Views.Processor({
                                model: processor,
                                options: lifeLineOptions
                            });

                            var processorCenterPoint = createPoint(xValue, yValue);
                            processorView.render("#" + defaultView.options.diagram.wrapperId, processorCenterPoint, "processors");
                            processor.setY(yValue);
                            yValue += processor.getHeight()+ 30;
                            totalIncrementedHeight = totalIncrementedHeight + processor.getHeight()+ 30;
                        } else {
                            var messagePoint = this.modelAttr("children").models[id];
                            var linkCenterPoint = createPoint(xValue, yValue);
                            //link.source.setY()
                            if (messagePoint.direction() == "outbound") {
                                if(!_.isUndefined(messagePoint.forceY) && _.isEqual(messagePoint.forceY, true)){
                                    yValue = messagePoint.y();
                                }
                                messagePoint.x(xValue);
                            } else {
                                if(!_.isUndefined(messagePoint.forceY) && _.isEqual(messagePoint.forceY, true)){
                                    yValue = messagePoint.y();
                                }
                                var sourceY = messagePoint.message().source().y();
                                if (yValue < sourceY) {
                                    messagePoint.y(sourceY);
                                } else {
                                    messagePoint.y(yValue);
                                    messagePoint.message().source().y(yValue);
                                }
                                messagePoint.x(xValue);
                            }
                            yValue += 60;
                            totalIncrementedHeight = totalIncrementedHeight + 40;
                        }
                    }

                    var totalHeight = totalIncrementedHeight + initialHeight;
                    if (!_.isUndefined(diagram.highestLifeline) && diagram.highestLifeline !== null && diagram.highestLifeline.getHeight() > totalHeight) {
                        totalHeight = diagram.highestLifeline.getHeight();
                    }
                    this.model.setHeight(totalHeight);
                    this.adjustHeight(lifeLine, totalHeight - initialHeight);

                    if (diagram.highestLifeline == undefined || diagram.highestLifeline.getHeight() < this.model.getHeight()) {
                        diagram.highestLifeline = this.model;
                        defaultView.render();
                        return false;
                    }

                    //this.model.on("addChildProcessor", this.onAddChildProcessor, this);

                    this.d3el = lifeLine;
                    this.el = lifeLine.node();
                    return lifeLine;
                } else if (status == "messages") {
                    for (var id in this.modelAttr("children").models) {
                        var messagePoint = this.modelAttr("children").models[id];
                        if (messagePoint instanceof SequenceD.Models.MessagePoint) {
                            var linkView = new SequenceD.Views.MessageLink({
                                model: messagePoint.message(),
                                options: {class: "message"}
                            });
                            linkView.render("#" + defaultView.options.diagram.wrapperId, "messages");
                        }
                    }
                }
            },

            adjustHeight: function (lifeLine, difference) {
                lifeLine.rectBottom.attr("y", parseInt(lifeLine.rectBottom.attr("y")) + difference);
                lifeLine.line.attr("y2", parseInt(lifeLine.line.attr("y2")) + difference);
                lifeLine.textBottom.attr("y", parseInt(lifeLine.textBottom.attr("y")) + difference);
                lifeLine.drawMessageRect.attr("height", parseInt(lifeLine.drawMessageRect.attr("height")) + difference);
                lifeLine.middleRect.attr("height", parseInt(lifeLine.middleRect.attr("height")) + difference);

            },

            onAddChildProcessor: function (element, opts) {

                if (element instanceof SequenceD.Models.Processor) {

                    if (element.model.type === "UnitProcessor") {

                        var d3Ref = this.getD3Ref();
                        console.log("Processor added");
                        var rectBottomXXX = d3Ref.draw.centeredRect(
                            createPoint(defaultView.model.selectedNode.get('centerPoint').get('x'),
                                element.get('centerPoint').get('y')),
                            this.prefs.rect.width,
                            this.prefs.rect.height,
                            0,
                            0,
                            this.group, element.viewAttributes.colour
                        );
                        rectBottomXXX.on('click',this.updateProcessorProperties, this);
                        var mediatorText = d3Ref.draw.centeredText(
                            createPoint(defaultView.model.selectedNode.get('centerPoint').get('x'),
                                element.get('centerPoint').get('y')),
                            element.get('title'),
                            this.group)
                            .classed(this.prefs.text.class, true);
                        //this.renderViewForElement(element, opts);
                    } else if (element.model.type === "DynamicContainableProcessor") {
                        var d3Ref = this.getD3Ref();
                        console.log("Processor added");
                        var rectBottomXXX = d3Ref.draw.rectWithTitle(
                            createPoint(defaultView.model.selectedNode.get('centerPoint').get('x'),
                                element.get('centerPoint').get('y')),
                            60,
                            this.prefs.rect.height,
                            150,
                            200,
                            0,
                            0,
                            this.group, element.viewAttributes.colour,
                            element.attributes.title
                        );

                    } else if (element.model.type === "ComplexProcessor") {
                        var d3Ref = this.getD3Ref();
                        console.log("Processor added");
                        var rectBottomXXX = d3Ref.draw.rectWithTitle(
                            createPoint(defaultView.model.selectedNode.get('centerPoint').get('x'),
                                element.get('centerPoint').get('y')),
                            60,
                            this.prefs.rect.height,
                            150,
                            200,
                                0,
                            0,
                            this.group, element.viewAttributes.colour,
                            element.attributes.title
                        );

                    }

                } else if (element instanceof SequenceD.Models.Message) {
                    console.log("Message Link added !!!")
                    if (opts.direction == 'inbound') {
                        defaultView.model.addElement(element, opts);
                    }
                }

            },

            drawLifeLine: function (center, title, prefs, colour) {
                var d3Ref = this.getD3Ref();
                this.diagram = prefs.diagram;
                var viewObj = this;
                var group = d3Ref.draw.group()
                    .classed(this.model.viewAttributes.class, true);
                this.group = group;
                this.prefs = prefs;
                this.center = center;
                this.title = title;

                var rect = d3Ref.draw.centeredRect(center, prefs.rect.width + 30, prefs.rect.height, 0, 0, group)
                    .classed(prefs.rect.class, true);

                var middleRect = d3Ref.draw.centeredBasicRect(createPoint(center.get('x'), center.get('y') + prefs.rect.height / 2 + prefs.line.height / 2), prefs.middleRect.width, prefs.middleRect.height, 0, 0, group)
                    .classed(prefs.middleRect.class, true);

                var drawMessageRect = d3Ref.draw.centeredBasicRect(createPoint(center.get('x'), center.get('y') + prefs.rect.height / 2 + prefs.line.height / 2), (prefs.middleRect.width * 0.4), prefs.middleRect.height, 0, 0, group)
                    .on("mousedown", function () {
                        d3.event.preventDefault();
                        d3.event.stopPropagation();
                        var m = d3.mouse(this);
                        prefs.diagram.clickedLifeLine = viewObj.model;
                        prefs.diagram.trigger("messageDrawStart", viewObj.model,  new GeoCore.Models.Point({'x': center.x(), 'y': m[1]}));

                    });

                var rectBottom = d3Ref.draw.centeredRect(createPoint(center.get('x'), center.get('y') + prefs.line.height), prefs.rect.width + 30, prefs.rect.height, 0, 0, group)
                    .classed(prefs.rect.class, true);

                var line = d3Ref.draw.verticalLine(createPoint(center.get('x'), center.get('y') + prefs.rect.height / 2), prefs.line.height - prefs.rect.height, group)
                    .classed(prefs.line.class, true);
                var text = d3Ref.draw.centeredText(center, title, group)
                    .classed(prefs.text.class, true);
                var textBottom = d3Ref.draw.centeredText(createPoint(center.get('x'), center.get('y') + prefs.line.height), title, group)
                    .classed(prefs.text.class, true);
                group.rect = rect;
                group.rectBottom = rectBottom;
                group.line = line;
                group.middleRect = middleRect;
                group.drawMessageRect = drawMessageRect;
                group.textBottom = textBottom;
                group.svgTitle = text;
                group.svgTitleBottom = textBottom;
                //Object.getPrototypeOf(group).title = text;
                //Object.getPrototypeOf(group).titleBottom = textBottom;
                group.translate = function (dx, dy) {
                    this.attr("transform", function () {
                        return "translate(" + [dx, dy] + ")"
                    })
                };

                var circleCenterX = center.x() + (prefs.rect.width + 30)/2;
                var circleCenterY = center.y() - prefs.rect.height/2;
                var deleteIconGroup = group.append("g")
                    .attr("class", "close-icon circle-hide");
                var propertiesIconGroup = group.append("g").attr("class", "properties-icon circle-hide");
                path = "M " + (circleCenterX - 3) + "," + (circleCenterY - 3) + " L " + (circleCenterX + 3) + "," +
                    (circleCenterY + 3) + " M " + (circleCenterX + 3) + "," + (circleCenterY - 3) + " L " +
                    (circleCenterX - 3) + "," + (circleCenterY + 3);
                var closeCircle = d3Ref.draw.circle(circleCenterX, circleCenterY, 7, deleteIconGroup).
                    attr("fill", "#95a5a6").
                    attr("style", "stroke: black; stroke-width: 2; opacity:0.8");
                deleteIconGroup.append("path")
                    .attr("d", path)
                    .attr("style", "stroke: black;fill: transparent; stroke-linecap:round; stroke-width: 1.5;");

                var propertiesCircle = d3Ref.draw.circle(circleCenterX - 14, circleCenterY, 7, propertiesIconGroup)
                    .attr("fill", "#95a5a6")
                    .attr("style", "stroke: black; stroke-width: 2; opacity:0.8");

                var viewObj = this;
                middleRect.on('mouseover', function () {
                    //setting current tab view based diagram model
                     diagram = defaultView.model;
                    diagram.selectedNode = viewObj.model;
                    d3.select(this).style("fill", "green").style("fill-opacity", 0.1);
                }).on('mouseout', function () {
                    diagram.destinationLifeLine = diagram.selectedNode;
                    diagram.selectedNode = null;
                    d3.select(this).style("fill-opacity", 0.01);
                }).on('mouseup', function (data) {
                });

                drawMessageRect.on('mouseover', function () {
                    //setting current tab view based diagram model
                    diagram = defaultView.model;
                    diagram.selectedNode = viewObj.model;
                    d3.select(this).style("fill", "black").style("fill-opacity", 0.2)
                        .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                }).on('mouseout', function () {
                    d3.select(this).style("fill-opacity", 0.0);
                }).on('mouseup', function (data) {
                });

                function updatePropertyPane() {
                    var lifeLineDefinition;
                    if (defaultView.model.selectedNode.attributes.cssClass === "resource") {
                        lifeLineDefinition = MainElements.lifelines.ResourceLifeline;
                    } else if (defaultView.model.selectedNode.attributes.cssClass === "endpoint") {
                        lifeLineDefinition = MainElements.lifelines.EndPointLifeline;
                    }
                    propertyPane = ppView.createPropertyPane(lifeLineDefinition.getSchema(), 
                                lifeLineDefinition.getEditableProperties(defaultView.model.selectedNode.get('title')),
                                defaultView.model.selectedNode);
                }

                rect.on("click", (function () {
                    if (deleteIconGroup.classed("circle-hide")) {
                        deleteIconGroup.classed("circle-hide", false);
                        deleteIconGroup.classed("circle-show", true);
                        propertiesIconGroup.classed("circle-hide", false);
                        propertiesIconGroup.classed("circle-show", true);
                    } else {
                        deleteIconGroup.classed("circle-hide", true);
                        deleteIconGroup.classed("circle-show", false);
                        propertiesIconGroup.classed("circle-hide", true);
                        propertiesIconGroup.classed("circle-show", false);
                        if (diagram.propertyWindow) {
                            $('#property-pane-svg').empty();
                            diagram.propertyWindow = false;
                        }
                    }
                    defaultView.model.selectedNode = viewObj.model;
                    diagram.currentDeleteIconGroup = deleteIconGroup;
                    diagram.currentPropertyIconGroup = propertiesIconGroup;

                    if (selected) {
                        if (this == selected) {
                            if (propertyPane) {
                                propertyPane.destroy();
                            }
                            selected = '';
                        } else {
                            if (diagram.previousDeleteIconGroup) {
                                diagram.previousDeleteIconGroup.classed("circle-hide", true);
                                diagram.previousDeleteIconGroup.classed("circle-show", false);
                                diagram.previousPropertyIconGroup.classed("circle-hide", true);
                                diagram.previousPropertyIconGroup.classed("circle-show", false);
                                if (diagram.propertyWindow) {
                                    $('#property-pane-svg').empty();
                                    diagram.propertyWindow = false;
                                }
                            }
                            updatePropertyPane();
                            selected = this;
                        }
                    } else {
                        defaultView.model.selected = false;
                        updatePropertyPane();
                        selected = this;
                    }
                    diagram.previousDeleteIconGroup = diagram.currentDeleteIconGroup;
                    diagram.previousPropertyIconGroup = diagram.currentPropertyIconGroup;
                    diagram.currentPropertyIconGroup = null;
                }));

                deleteIconGroup.on("click", function () {
                    //Get the parent of the model and delete it from the parent
                    if (~viewObj.model.get("title").indexOf("Resource")) {
                        var resourceElements = defaultView.model.get("diagramResourceElements").models;
                        for (var itr = 0; itr < resourceElements.length; itr ++) {
                            if (resourceElements[itr].cid === viewObj.model.cid) {
                                resourceElements.splice(itr, 1);
                                var currentResources = defaultView.model.resourceLifeLineCounter();
                                defaultView.model.resourceLifeLineCounter(currentResources - 1);
                                defaultView.model.get("diagramResourceElements").length -= 1;
                                defaultView.render();
                                break;
                            }
                        }
                    } else {
                        var endpointElements = defaultView.model.get("diagramEndpointElements").models;
                        for (var itr = 0; itr < endpointElements.length; itr ++) {
                            if (endpointElements[itr].cid === viewObj.model.cid) {
                                endpointElements.splice(itr, 1);
                                var currentEndpoints = defaultView.model.endpointLifeLineCounter();
                                defaultView.model.endpointLifeLineCounter(currentEndpoints - 1);
                                defaultView.model.get("diagramEndpointElements").length -= 1;
                                defaultView.render();
                                break;
                            }
                        }
                    }

                });

                propertiesIconGroup.on("click", function () {
                    diagram.selectedMainElementText = {
                        top: viewObj.d3el.svgTitle,
                        bottom: viewObj.d3el.svgTitleBottom
                    };
                    if (diagram.propertyWindow) {
                        diagram.propertyWindow = false;
                        defaultView.render();
                    } else {
                        var options = {
                            x: viewObj.model.attributes.centerPoint.attributes.x + 47,
                            y: viewObj.model.attributes.centerPoint.attributes.y - 12
                        };
                        defaultView.selectedNode = viewObj.model;
                        var parameters;
                        if (viewObj.model.attributes.cssClass === "resource") {
                            parameters = [
                                {
                                    key: "title",
                                    value: viewObj.title
                                },
                                {
                                    key: "path",
                                    value: viewObj.model.attributes.parameters[0].value
                                },
                                {
                                    key: "get",
                                    value: viewObj.model.attributes.parameters[1].value
                                },
                                {
                                    key: "put",
                                    value: viewObj.model.attributes.parameters[2].value
                                },
                                {
                                    key: "post",
                                    value: viewObj.model.attributes.parameters[3].value
                                }
                            ];
                        } else if (viewObj.model.attributes.cssClass === "endpoint") {
                            parameters = [
                                {
                                    key: "title",
                                    value: viewObj.title
                                },
                                {
                                    key: "url",
                                    value: viewObj.model.attributes.parameters[0].value
                                }
                            ];
                        }

                        var propertySchema;
                        if (viewObj.model.attributes.cssClass === "endpoint") {
                            propertySchema = MainElements.lifelines.EndPointLifeline.propertyPaneSchema;

                        } else if (viewObj.model.attributes.cssClass === "resource") {
                            propertySchema = MainElements.lifelines.ResourceLifeline.propertyPaneSchema;

                        }

                        defaultView.drawPropertiesPane(d3Ref, options, parameters, propertySchema);
                    }
                });
                return group;
            }

        });

    var MessageView = Diagrams.Views.LinkView.extend(
        /** @lends MessageView.prototype */
        {
            /**
             * @augments LinkView
             * @constructs
             * @class MessageView Represents the view for message components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.LinkView.prototype.initialize.call(this, options);
            },

            render: function (paperID) {
                // call super
                Diagrams.Views.LinkView.prototype.render.call(this, paperID);
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

                this.d3el.call(drag);
                return this.d3el;
            }
        });

    var ActivationView = Diagrams.Views.ConnectionPointView.extend(
        /** @lends ConnectionPointView.prototype */
        {
            /**
             * @augments LinkView
             * @constructs
             * @class ActivationView Represents the view for activations in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.ConnectionPointView.prototype.initialize.call(this, options);
            },

            render: function (paperID) {
                // call super
                Diagrams.Views.ConnectionPointView.prototype.render.call(this, paperID);

            },

            getNextAvailableConnectionPoint: function (connecion, x, y) {
                var nextYCoordinate = defaultView.model.deepestPointY + 50;
                var nextXCoordinate = this.model.owner().get('centerPoint').x();

                // TODO: Until the layout finalize we will be drawing the message without offsetting dynamically
                //if (_.isEqual(connecion.type(), "incoming")) {
                //    lifeLineOptions.diagram.deepestPointY = nextYCoordinate;
                //}
                return new GeoCore.Models.Point({'x': nextXCoordinate, 'y': defaultView.model.sourceLifeLineY});
            }
        });

    var FixedSizedMediatorView = Diagrams.Views.ShapeView.extend(
        /** @lends FixedSizedMediatorView.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class LifeLineView Represents the view for lifeline components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.ShapeView.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID) {
                Diagrams.Views.ShapeView.prototype.render.call(this, paperID);

                var lifeLine = this.drawFixedSizedMediator(this.modelAttr('centerPoint'), this.modelAttr('title'), this.options);
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

                //lifeLine.call(drag);

                this.d3el = lifeLine;
                this.el = lifeLine.node();
                return lifeLine;
            },

            drawFixedSizedMediator: function (center, title, prefs) {
                var d3Ref = this.getD3Ref();
                var group = d3Ref.draw.group()
                    .classed(prefs.class, true);
                var rect = d3Ref.draw.centeredRect(center, prefs.rect.width, prefs.rect.height, 0, 0, group)
                    .classed(prefs.rect.class, true);
                //var rectBottom = d3Ref.draw.centeredRect(createPoint(center.get('x'), center.get('y') +
                // prefs.line.height), prefs.rect.width, prefs.rect.height, 3, 3, group) .classed(prefs.rect.class,
                // true); var line = d3Ref.draw.verticalLine(createPoint(center.get('x'), center.get('y')+
                // prefs.rect.height/2), prefs.line.height-prefs.rect.height, group) .classed(prefs.line.class, true);
                var text = d3Ref.draw.centeredText(center, title, group)
                    .classed(prefs.text.class, true);
                //var textBottom = d3Ref.draw.centeredText(createPoint(center.get('x'), center.get('y') +
                // prefs.line.height), title, group) .classed(prefs.text.class, true);
                group.rect = rect;
                //Object.getPrototypeOf(group).rectBottom = rectBottom;
                //Object.getPrototypeOf(group).line = line;
                group.title = text;
                //Object.getPrototypeOf(group).titleBottom = textBottom;
                group.translate = function (dx, dy) {
                    this.attr("transform", function () {
                        return "translate(" + [dx, dy] + ")"
                    })
                };

                return group;
            }

        });



    var ContainableProcessorElement = Diagrams.Views.ShapeView.extend(
        /** @lends ContainableProcessorElement.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class LifeLineView Represents the view for lifeline components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.ShapeView.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID, centerPoint) {
                var thisModel = this.model;
                Diagrams.Views.ShapeView.prototype.render.call(this, paperID);

                var unitProcessorElement = this.drawUnitProcessor(centerPoint, this.modelAttr('title'), this.options);
                var viewObj = this;

                this.d3el = unitProcessorElement;
                this.el = unitProcessorElement.node();
                return unitProcessorElement;
            },

            updateProcessorProperties: function () {

                diagram.selectedNode = this.model;

                //get processor parameters
                var parameters = [];

                var processorParameters = diagram.selectedNode.attributes.parent.parameters.parameters;
                processorParameters.forEach(function (parameter, index) {
                    parameters[index] = parameter.value;
                });

                //get processor definition
                var processorDefinition;
                var type = diagram.selectedNode.attributes.parent.type;
                if (type === "TryBlockMediator") {
                    processorDefinition = Processors.flowControllers.TryBlockMediator;
                } else if (type === "SwitchMediator") {
                    processorDefinition = Processors.flowControllers.SwitchMediator;
                }

                ppView.loadPropertyPane(this, processorDefinition, parameters);
            },

            drawUnitProcessor: function (center, title, prefs) {

                var d3Ref = this.getD3Ref();
                var group = d3Ref.draw.group()
                    .classed(prefs.class, true);
                var viewObj = this;
                var deleteIconGroup = undefined;
                var path = undefined;
                var height = prefs.rect.height;
                var width = prefs.rect.width;


                var rectBottomXXX = d3Ref.draw.rectWithTitle(
                    center,
                    60,
                    prefs.rect.height,
                    150,
                    200,
                    0,
                    0,
                    d3Ref,
                    this.modelAttr('viewAttributes').colour,
                    this.modelAttr('title')
                );
                console.log("started");
                var height = (200 - prefs.rect.height);
                var middleRect = d3Ref.draw.centeredBasicRect(createPoint(center.x(),
                    center.y()+100), 150, height, 0, 0);
                middleRect.on("mousedown", function () {
                    var m = d3.mouse(this);
                    prefs.diagram.trigger("messageDrawStart", viewObj.model,  new GeoCore.Models.Point({'x': center.x(), 'y': m[1]}));
                }).on('mouseover', function () {
                    defaultView.model.selectedNode = viewObj.model;
                    d3.select(this).style("fill", "green").style("fill-opacity", 0.1);
                }).on('mouseout', function () {
                    defaultView.model.destinationLifeLine = defaultView.model.selectedNode;
                    defaultView.model.selectedNode = null;
                    d3.select(this).style("fill-opacity", 0.01);
                }).on('mouseup', function (data) {
                });
                console.log(middleRect);

                var drawMessageRect = d3Ref.draw.centeredBasicRect(createPoint(center.x(),
                    center.y()+100), (prefs.middleRect.width * 0.4), height, 0, 0, d3Ref)
                    .on("mousedown", function () {
                        d3.event.preventDefault();
                        d3.event.stopPropagation();
                        var m = d3.mouse(this);

                        prefs.diagram.clickedLifeLine = viewObj.model;
                        prefs.diagram.trigger("messageDrawStart", viewObj.model,  new GeoCore.Models.Point({'x': center.x(), 'y': m[1]}));

                    }).on('mouseover', function () {
                        defaultView.model.selectedNode = viewObj.model;
                        d3.select(this).style("fill", "black").style("fill-opacity", 0.2)
                            .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                    }).on('mouseout', function () {
                        d3.select(this).style("fill-opacity", 0.0);
                    });

                group.middleRect = middleRect;
                group.drawMessageRect = drawMessageRect;
                group.rect = rectBottomXXX.containerRect;
                group.titleRect = rectBottomXXX.titleRect;
                group.text = rectBottomXXX.text;

                var centerPoint = center;
                var xValue = centerPoint.x();
                var yValue = centerPoint.y();
                //lifeLine.call(drag);

                var totalHeight = 60;
                var totalWidth = 150;
                this.model.setHeight(30);

                var initWidth =rectBottomXXX.containerRect.attr("width");

                yValue += 60;
                for (var id in this.modelAttr("children").models) {
                    var processor = this.modelAttr("children").models[id];
                    var processorView = new SequenceD.Views.Processor({model: processor, options: lifeLineOptions});
                    var processorCenterPoint = createPoint(xValue, yValue);
                    processorView.render("#" + defaultView.options.diagram.wrapperId, processorCenterPoint, "processors");
                    processor.setY(yValue);
                    totalHeight = totalHeight + this.model.getHeight() + processor.getHeight();
                    yValue += processor.getHeight()+ 30;

                    if (this.model.widestChild == null || this.model.widestChild.getWidth() < processor.getWidth()) {
                        this.model.widestChild = processor;
                    }
                }

                if (this.model.widestChild != null) {
                    totalWidth = this.model.widestChild.getWidth() + 30;
                }

                var deviation = (totalWidth - initWidth)/2;

                rectBottomXXX.containerRect.attr("height", totalHeight);
                rectBottomXXX.containerRect.attr("width", totalWidth);
                rectBottomXXX.containerRect.attr("x", parseInt(rectBottomXXX.containerRect.attr("x")) - deviation);
                rectBottomXXX.titleRect.attr("x", parseInt(rectBottomXXX.titleRect.attr("x")) - deviation);
                rectBottomXXX.text.attr("x", parseInt(rectBottomXXX.text.attr("x")) - deviation);
                this.model.setHeight(totalHeight);
                this.model.setWidth(totalWidth);
                middleRect.attr("height", totalHeight-30);
                middleRect.attr("width", totalWidth);
                middleRect.attr("x", parseInt(middleRect.attr("x")) - deviation);
                drawMessageRect.attr("height", totalHeight-30);

                if (viewObj.model.get("title") === "Try") {
                    var circleCenterX = center.x() + 75;
                    var circleCenterY = center.y() - prefs.rect.height/2;
                    deleteIconGroup = group.append("g")
                        .attr("class", "close-icon circle-hide");
                    path = "M " + (circleCenterX - 3) + "," + (circleCenterY - 3) + " L " + (circleCenterX + 3) + "," +
                        (circleCenterY + 3) + " M " + (circleCenterX + 3) + "," + (circleCenterY - 3) + " L " +
                        (circleCenterX - 3) + "," + (circleCenterY + 3);
                    var closeCircle = d3Ref.draw.circle(circleCenterX, circleCenterY, 7, deleteIconGroup).
                        attr("fill", "#95a5a6").
                        attr("style", "stroke: black; stroke-width: 2; opacity:0.8");
                    deleteIconGroup.append("path")
                        .attr("d", path)
                        .attr("style", "stroke: black;fill: transparent; stroke-linecap:round; stroke-width: 1.5;");

                    // On click of the mediator show/hide the delete icon
                    rectBottomXXX.containerRect.on("click", function () {
                        if (deleteIconGroup.classed("circle-hide")) {
                            deleteIconGroup.classed("circle-hide", false);
                            deleteIconGroup.classed("circle-show", true);
                        } else {
                            deleteIconGroup.classed("circle-hide", true);
                            deleteIconGroup.classed("circle-show", false);
                        }
                        diagram.currentDeleteIconGroup = deleteIconGroup;
                        diagram.selectedNode = viewObj.model;
                        viewObj.updateProcessorProperties();
                    });

                    deleteIconGroup.on("click", function () {
                         //Get the parent of the model and delete it from the parent
                        var parentModelChildren = viewObj.model.get("parent").get("parent").get("children").models;
                        for (var itr = 0; itr < parentModelChildren.length; itr ++) {
                            if (parentModelChildren[itr].cid === viewObj.model.get("parent").cid) {
                                parentModelChildren.splice(itr, 1);
                                defaultView.render();
                                break;
                            }
                        }
                    });

                    //group.remove();
                    //
                    //return newGroup;
                }

                return group;
            }

        });

    views.MessageView = MessageView;
    views.ActivationView = ActivationView;
    views.LifeLineView = LifeLineView;
    views.Processor = Processor;
    views.MessageLink = MessageLink;
    views.ContainableProcessorElement = ContainableProcessorElement;

    return sequenced;

}(SequenceD || {}));
