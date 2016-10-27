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

            drawProcessor: function (paperID, center, title, prefs) {
                var d3Ref = this.getD3Ref();
                var group = d3Ref.draw.group();
                var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");
                var viewObj = this;

                if (this.model.model.type === "UnitProcessor") {

                    var height = 0;
                    if (this.model.get('utils').utils.outputs) {
                        height = this.model.getHeight();
                    } else {
                        height = this.model.getHeight() - 20;
                    }
                    var width = this.model.getWidth();

                    var orderedElements = [];

                    var optionMenuWrapper = d3Ref.draw.rect((center.x() + 10 + width/2),
                        (center.y() - height/2),
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
                        (center.y() + 3 - height/2),
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
                        (center.y() + 31 - height/2),
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

                    var processorTitleRect = d3Ref.draw.rect((center.x() - this.model.getWidth()/2),
                        (center.y() - height/2),
                        this.model.getWidth(),
                        20,
                        0,
                        0,
                        group,
                        this.modelAttr('viewAttributes').colour
                    );

                    var rectBottomXXX = d3Ref.draw.centeredRect(center,
                        this.model.getWidth(),
                        height,//prefs.rect.height,
                        0,
                        0,
                        group, //element.viewAttributes.colour
                        this.modelAttr('viewAttributes').colour
                    );
                    var mediatorText = d3Ref.draw.textElement(center.x(),
                        (center.y() + 15 - height/2),
                        title,
                        group)
                        .classed("mediator-title", true);
                    var inputText = d3Ref.draw.textElement(center.x() + 20 - this.model.getWidth()/2,
                        (center.y() + 35 - height/2),
                        this.generateInputOutputString(this.model.get('utils').utils.getInputParams()),
                        group)
                        .classed("input-output-text", true);
                    var inputTri = d3Ref.draw.inputTriangle(center.x() + 5 - this.model.getWidth()/2,
                        (center.y() + 30 - height/2),
                        group);

                    //this.generateInputOutputString(this.model.get('utils').utils.getInputParams());

                    if (this.model.get('utils').utils.outputs) {
                        var outputText = d3Ref.draw.textElement(center.x() + 20 - this.model.getWidth()/2,
                            (center.y() + 58 - this.model.getHeight()/2),
                            this.generateInputOutputString(this.model.get('utils').utils.getOutputParams()),
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

                        orderedElements = [rectBottomXXX,
                            processorTitleRect,
                            mediatorText,
                            inputText,
                            outputText,
                            inputTri,
                            outputTri,
                            dashedSeparator,
                            optionsMenuGroup
                        ];
                    } else {
                        orderedElements = [rectBottomXXX,
                            processorTitleRect,
                            mediatorText,
                            inputText,
                            inputTri,
                            optionsMenuGroup
                        ];
                    }

                    group.rect = rectBottomXXX;
                    group.title = mediatorText;

                    group.rect = rectBottomXXX;
                    group.title = mediatorText;

                    var newGroup = d3Ref.draw.regroup(orderedElements);
                    group.remove();

                    // On click of the mediator show/hide the options menu
                    processorTitleRect.on("click", function () {
                        if (optionsMenuGroup.classed("option-menu-hide")) {
                            optionsMenuGroup.classed("option-menu-hide", false);
                            optionsMenuGroup.classed("option-menu-show", true);

                            if (diagram.selectedOptionsGroup) {
                                diagram.selectedOptionsGroup.classed("option-menu-hide", true);
                                diagram.selectedOptionsGroup.classed("option-menu-show", false);
                            }
                            if (diagram.propertyWindow) {
                                diagram.propertyWindow = false;
                                defaultView.enableDragZoomOptions();
                                $('#property-pane-svg').empty();
                            }
                            diagram.selectedOptionsGroup = optionsMenuGroup;

                        } else {
                            optionsMenuGroup.classed("option-menu-hide", true);
                            optionsMenuGroup.classed("option-menu-show", false);
                            diagram.propertyWindow = false;
                            defaultView.enableDragZoomOptions();
                            defaultView.render();
                        }
                    });

                    // On click of the edit icon will show the properties to to edit
                    editOption.on("click", function () {
                        if (diagram.propertyWindow) {
                            diagram.propertyWindow = false;
                            defaultView.enableDragZoomOptions();
                            defaultView.render();

                        } else {
                            var options = {
                                x: parseFloat(this.getAttribute("x")) + 6,
                                y: parseFloat(this.getAttribute("y")) + 21
                            };

                            defaultView.selectedNode = viewObj.model;
                            defaultView.drawPropertiesPane(d3Ref, options,
                                viewObj.model.get('utils').utils.parameters,
                                getPropertyPaneSchema(viewObj.model));
                        }
                    });

                    // On click of the delete icon will delete the processor
                    deleteOption.on("click", function () {
                        // Get the parent of the model and delete it from the parent
                        var parentModelChildren = viewObj.model.get("parent").get("children").models;
                        for (var itr = 0; itr < parentModelChildren.length; itr ++) {
                            if (parentModelChildren[itr].cid === viewObj.model.cid) {

                                parentModelChildren.splice(itr, 1);
                                defaultView.render();
                                break;
                            }
                        }

                        if (propertyPane) {
                            propertyPane.destroy();
                        }
                    });

                    var getPropertyPaneSchema = function (model) {
                        return model.get('utils').utils.propertyPaneSchema;
                    };

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
                    var group = d3ref.draw.group();
                    var viewObj = this;
                    var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");
                    var delXPosition = ((Math.round(this.model.source().centerPoint().get('x'))) +
                        Math.round(this.model.destination().centerPoint().get('x')))/2;

                    var optionMenuWrapper = d3ref.draw.rect(Math.round(delXPosition) - 15,
                        Math.round(this.model.source().centerPoint().get('y')) + 10,
                        30,
                        30,
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

                    var deleteOption = d3ref.draw.rect(Math.round(delXPosition) - 12,
                        Math.round(this.model.source().centerPoint().get('y')) + 13,
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

                    var line = d3ref.draw.lineFromPoints(this.model.source().centerPoint(), this.model.destination().centerPoint(), group)
                        .classed(this.options.class, true);

                    // TODO : If we are drawing an arrow from pipeline to an endpoint, we need a reverse arrow as well.
                    // But this needs to be fixed as we need to support OUT_ONLY messages.
                    if (this.model.destination().get('parent').get('cssClass') === "endpoint") {
                        var lineDestinationCenterPoint = createPoint(this.model.destination().centerPoint().x(), Math.round(this.model.destination().centerPoint().y()) + 20);
                        var lineSourceCenterPoint = createPoint(this.model.source().centerPoint().x(), Math.round(this.model.source().centerPoint().y()) + 20);

                        var line2 = d3ref.draw.lineFromPoints(lineDestinationCenterPoint, lineSourceCenterPoint, group)
                            .classed(this.options.class, true);
                    }

                    //this.model.source().on("connectingPointChanged", this.sourceMoved, this);
                    //this.model.destination().on("connectingPointChanged", this.destinationMoved, this);

                    line.on("click", function () {
                        if (optionsMenuGroup.classed("option-menu-hide")) {
                            optionsMenuGroup.classed("option-menu-hide", false);
                            optionsMenuGroup.classed("option-menu-show", true);
                        } else {
                            optionsMenuGroup.classed("option-menu-hide", true);
                            optionsMenuGroup.classed("option-menu-show", false);
                        }
                    });

                    deleteOption.on("click", function () {
                        var model = viewObj.model;

                        var sourceLifeLineChildren = model.get('source').get('parent').get('children').models;
                        var destLifeLineChildren = model.get('destination').get('parent').get('children').models;

                        sourceLifeLineChildren.forEach(function (child) {
                            if (child.cid == model.get('sourcePoint').cid) {
                                sourceLifeLineChildren.splice(sourceLifeLineChildren.indexOf(child), 1);
                            }
                        });

                        destLifeLineChildren.forEach(function (child) {
                            if (child.cid == model.get('destinationPoint').cid) {
                                destLifeLineChildren.splice(destLifeLineChildren.indexOf(child), 1);
                            }
                        });

                        defaultView.render();
                    });

                    this.d3el = group;
                    this.el = group.node();
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
                             //TODO: for event synchronize
                            //get text model of processor. Add this (lifeline?) as the parent in it and make hasParent:true

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
                                messagePoint.y(yValue);
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
                        if ((messagePoint instanceof SequenceD.Models.MessagePoint)) {
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
                var mm = this.model.attributes.textModel;
                if(this.model.attributes.textModel.dynamicRectWidth() === undefined){
                    this.model.attributes.textModel.dynamicRectWidth(130);
                }

                var rect = d3Ref.draw.genericCenteredRect(center, prefs.rect.width + 30, prefs.rect.height, 0, 0, group,'',this.model.attributes.textModel)
                    .classed(prefs.rect.class, true).classed("genericR",true);

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

                var rectBottom = d3Ref.draw.genericCenteredRect(createPoint(center.get('x'), center.get('y') + prefs.line.height), prefs.rect.width + 30, prefs.rect.height, 0, 0, group,'',this.model.attributes.textModel)
                    .classed(prefs.rect.class, true).classed("genericR",true);

                var line = d3Ref.draw.verticalLine(createPoint(center.get('x'), center.get('y') + prefs.rect.height / 2), prefs.line.height - prefs.rect.height, group)
                    .classed(prefs.line.class, true);
                var text = d3Ref.draw.genericCenteredText(center, title, group,this.model.attributes.textModel)
                    .classed(prefs.text.class, true).classed("genericT",true);
                var textBottom = d3Ref.draw.genericCenteredText(createPoint(center.get('x'), center.get('y') + prefs.line.height), title, group,this.model.attributes.textModel)
                    .classed(prefs.text.class, true).classed("genericT",true);


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

                var optionMenuStartX = center.x() + 2 + (prefs.rect.width + 30)/2;
                var optionMenuStartY = center.y() - prefs.rect.height/2;
                var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");

                var optionMenuWrapper = d3Ref.draw.rect(optionMenuStartX + 8,
                    optionMenuStartY,
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

                var deleteOption = d3Ref.draw.rect(optionMenuStartX + 11,
                    optionMenuStartY + 3,
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

                var editOption = d3Ref.draw.rect(optionMenuStartX + 11,
                    optionMenuStartY + 32,
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

                var viewObj = this;
                middleRect.on('mouseover', function () {
                    //setting current tab view based diagram model
                     diagram = defaultView.model;
                    diagram.selectedNode = viewObj.model;
                    d3.select(this).style("fill", "green").style("fill-opacity", 0.1);
                    // Update event manager with current active element type for validation
                    eventManager.isActivated(diagram.selectedNode.attributes.title);
                }).on('mouseout', function () {
                    diagram.destinationLifeLine = diagram.selectedNode;
                    diagram.selectedNode = null;
                    d3.select(this).style("fill-opacity", 0.01);
                    // Update event manager with out of focus on active element
                    eventManager.isActivated("none");
                }).on('mouseup', function (data) {

                });

                drawMessageRect.on('mouseover', function () {
                    //setting current tab view based diagram model
                    diagram = defaultView.model;
                    diagram.selectedNode = viewObj.model;
                    d3.select(this).style("fill", "black").style("fill-opacity", 0.2)
                        .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                    // Update event manager with current active element type for validation
                    eventManager.isActivated(diagram.selectedNode.attributes.title);
                }).on('mouseout', function () {
                    d3.select(this).style("fill-opacity", 0.0);
                    // Update event manager with out of focus on active element
                    eventManager.isActivated("none");
                }).on('mouseup', function (data) {
                });

                rect.on("click", (function () {
                    defaultView.model.selectedNode = viewObj.model;
                    if (optionsMenuGroup.classed("option-menu-hide")) {
                        optionsMenuGroup.classed("option-menu-hide", false);
                        optionsMenuGroup.classed("option-menu-show", true);
                        
                        if (diagram.selectedOptionsGroup) {
                            diagram.selectedOptionsGroup.classed("option-menu-hide", true);
                            diagram.selectedOptionsGroup.classed("option-menu-show", false);
                        }
                        if (diagram.propertyWindow) {
                            diagram.propertyWindow = false;
                            defaultView.enableDragZoomOptions();
                            $('#property-pane-svg').empty();
                        }
                        diagram.selectedOptionsGroup = optionsMenuGroup;
                        
                    } else {
                        optionsMenuGroup.classed("option-menu-hide", true);
                        optionsMenuGroup.classed("option-menu-show", false);
                        diagram.propertyWindow = false;
                        defaultView.enableDragZoomOptions();
                        diagram.selectedOptionsGroup = null;
                        defaultView.render();
                    }
                }));

                editOption.on("click", function () {

                    if (diagram.propertyWindow) {
                        diagram.propertyWindow = false;
                        defaultView.enableDragZoomOptions();
                        defaultView.render();
                        
                    } else {
                        diagram.selectedMainElementText = {
                            top: viewObj.d3el.svgTitle,
                            bottom: viewObj.d3el.svgTitleBottom
                        };
                        
                        var options = {
                            x: parseFloat(this.getAttribute("x")) + 6,
                            y: parseFloat(this.getAttribute("y")) + 21
                        };
                        
                        defaultView.selectedNode = viewObj.model;
                        var parameters;
                        
                        if (viewObj.model.attributes.cssClass === "resource") {
                            parameters = [
                                {
                                    key: "title",
                                    value: viewObj.model.get('utils').utils.parameters[0].value
                                },
                                {
                                    key: "path",
                                    value: viewObj.model.get('utils').utils.parameters[1].value
                                },
                                {
                                    key: "get",
                                    value: viewObj.model.get('utils').utils.parameters[2].value
                                },
                                {
                                    key: "put",
                                    value: viewObj.model.get('utils').utils.parameters[3].value
                                },
                                {
                                    key: "post",
                                    value: viewObj.model.get('utils').utils.parameters[4].value
                                }
                            ];
                            
                        } else if (viewObj.model.attributes.cssClass === "endpoint") {
                            parameters = [
                                {
                                    key: "title",
                                    value: viewObj.model.get('utils').utils.parameters[0].value
                                },
                                {
                                    key: "url",
                                    value: viewObj.model.get('utils').utils.parameters[1].value
                                }
                            ];
                            
                        } else if (viewObj.model.attributes.cssClass === "source") {
                            parameters = [
                                {
                                    key: "title",
                                    value: viewObj.title
                                }
                            ]
                        }

                        var propertySchema;
                        if (viewObj.model.attributes.cssClass === "endpoint") {
                            propertySchema = MainElements.lifelines.EndPointLifeline.utils.propertyPaneSchema;

                        } else if (viewObj.model.attributes.cssClass === "resource") {
                            propertySchema = MainElements.lifelines.ResourceLifeline.utils.propertyPaneSchema;

                        } else if (viewObj.model.attributes.cssClass === "source") {
                            propertySchema = MainElements.lifelines.SourceLifeline.utils.propertyPaneSchema;
                        }

                        defaultView.drawPropertiesPane(d3Ref, options, parameters, propertySchema);
                    }
                });

                deleteOption.on("click", function () {
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
                    if (propertyPane) {
                        propertyPane.destroy();
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

            drawUnitProcessor: function (center, title, prefs) {

                var d3Ref = this.getD3Ref();
                var group = d3Ref.draw.group()
                    .classed(prefs.class, true);
                var viewObj = this;
                //var deleteIconGroup = undefined;
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
                    //TODO : Please remove this if else with a proper implementation
                    if(processor.type == "messagePoint"){
                        yValue = yValue-20;
                    }
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

                if (viewObj.model.get("title") === "Try" || viewObj.model.get("title") === "If") {
                    var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");
                    var optionMenuStartX = center.x() + 80;
                    var optionMenuStartY = center.y() - prefs.rect.height/2;

                    var optionMenuWrapper = d3Ref.draw.rect(optionMenuStartX + 8,
                        optionMenuStartY,
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

                    var deleteOption = d3Ref.draw.rect(optionMenuStartX + 11,
                        optionMenuStartY + 3,
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

                    var editOption = d3Ref.draw.rect(optionMenuStartX + 11,
                        optionMenuStartY + 32,
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

                    // On click of the mediator show/hide the delete icon
                    rectBottomXXX.containerRect.on("click", function () {
                        defaultView.model.selectedNode = viewObj.model;
                        
                        if (optionsMenuGroup.classed("option-menu-hide")) {
                            optionsMenuGroup.classed("option-menu-hide", false);
                            optionsMenuGroup.classed("option-menu-show", true);
                            
                            if (diagram.selectedOptionsGroup) {
                                diagram.selectedOptionsGroup.classed("option-menu-hide", true);
                                diagram.selectedOptionsGroup.classed("option-menu-show", false);
                            }
                            if (diagram.propertyWindow) {
                                diagram.propertyWindow = false;
                                defaultView.enableDragZoomOptions();
                                $('#property-pane-svg').empty();
                            }
                            diagram.selectedOptionsGroup = optionsMenuGroup;
                            
                        } else {
                            optionsMenuGroup.classed("option-menu-hide", true);
                            optionsMenuGroup.classed("option-menu-show", false);
                            if (diagram.propertyWindow) {
                                diagram.propertyWindow = false;
                                defaultView.enableDragZoomOptions();
                                defaultView.render();
                            }
                            diagram.selectedOptionsGroup = null;
                        }
                    });

                    var getPropertyPaneSchema = function (model) {
                        return model.get('utils').utils.propertyPaneSchema;
                    };

                    editOption.on("click", function () {
                        if (diagram.propertyWindow) {
                            diagram.propertyWindow = false;
                            defaultView.enableDragZoomOptions();
                            defaultView.render();
                            
                        } else {
                            var options = {
                                x: parseFloat(this.getAttribute("x")) + 6,
                                y: parseFloat(this.getAttribute("y")) + 21
                            };
                            
                            defaultView.selectedNode = viewObj.model.attributes.parent;
                            defaultView.drawPropertiesPane(d3Ref, options,
                                                           viewObj.model.get('parent').get('utils').utils.parameters,
                                                           getPropertyPaneSchema(viewObj.model.attributes.parent));
                        }
                    });

                    deleteOption.on("click", function () {
                         //Get the parent of the model and delete it from the parent
                        var parentModelChildren = viewObj.model.get("parent").get("parent").get("children").models;
                        for (var itr = 0; itr < parentModelChildren.length; itr ++) {
                            if (parentModelChildren[itr].cid === viewObj.model.get("parent").cid) {
                                parentModelChildren.splice(itr, 1);
                                defaultView.render();
                                break;
                            }
                        }
                        if (propertyPane) {
                            propertyPane.destroy();
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
