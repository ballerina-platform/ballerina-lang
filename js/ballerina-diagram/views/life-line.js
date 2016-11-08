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
define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'diagram_core', 'ballerina_models', './processor', './message-link',

], function (require, $, d3, Backbone, _, DiagramCore, BallerinaModels, ProcessorView, MessageLinkView) {

    var createPoint = function (x, y) {
        return new DiagramCore.Models.Point({x: x, y: y});
    };

    var LifeLineView = DiagramCore.Views.ShapeView.extend(
        /** @lends LifeLineView.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class LifeLineView Represents the view for lifeline components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (opts) {
                // set default options for the life-lines
                var lifeLineOptions = opts.options || {};
                lifeLineOptions.class = opts.options.class || "lifeline";
                // Lifeline rectangle options
                lifeLineOptions.rect = opts.options.rect || {};
                lifeLineOptions.rect.width = opts.options.rect.width || 100;
                lifeLineOptions.rect.height = opts.options.rect.height || 30;
                lifeLineOptions.rect.roundX = opts.options.rect.roundX || 20;
                lifeLineOptions.rect.roundY = opts.options.rect.roundY || 20;
                lifeLineOptions.rect.class = opts.options.rect.class || "lifeline-rect";
                // Lifeline middle-rect options
                lifeLineOptions.middleRect = opts.options.middleRect || {};
                lifeLineOptions.middleRect.width = opts.options.middleRect || 100;
                lifeLineOptions.middleRect.height = opts.options.middleRect || 300;
                lifeLineOptions.middleRect.roundX = opts.options.middleRect || 1;
                lifeLineOptions.middleRect.roundY = opts.options.middleRect || 1;
                lifeLineOptions.middleRect.class = opts.options.middleRect || "lifeline-middleRect";
                // Lifeline options
                lifeLineOptions.line = opts.options.line || {};
                lifeLineOptions.line.height = opts.options.line.height || 300;
                lifeLineOptions.line.class = opts.options.line.class || "lifeline-line";
                // Lifeline text options
                lifeLineOptions.text = opts.options.text || {};
                lifeLineOptions.text.class = opts.options.text.class || "lifeline-title";

                DiagramCore.Views.ShapeView.prototype.initialize.call(this, opts);
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
                }
            },

            leftUpperCorner: function () {
                var centerPoint = this.modelAttr('centerPoint');
                return {x: centerPoint.attributes.x - 65, y: centerPoint.attributes.y - 15}
            },

            rightLowerCorner: function () {
                var centerPoint = this.modelAttr('centerPoint');
                return {
                    x: centerPoint.attributes.x + 65,
                    y: centerPoint.attributes.y + 15 + this.options.middleRect.height + this.options.rect.height
                }
            },

            render: function () {
                var centerPoint = this.modelAttr('centerPoint');
                var lifeLine = this.drawLifeLine(centerPoint, this.modelAttr('title'), this.options);
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

                lifeLine.call(drag);
                this.d3el = lifeLine;
                this.el = lifeLine.node();
            },

            renderProcessors: function () {
                var centerPoint = this.modelAttr('centerPoint');
                var xValue = centerPoint.x();
                var yValue = centerPoint.y();
                yValue += 60;

                var initialHeight = parseInt(this.d3el.line.attr("y2")) - parseInt(this.d3el.line.attr("y1"));
                var totalIncrementedHeight = 0;

                for (var id in this.modelAttr("children").models) {

                    if (this.modelAttr("children").models[id] instanceof BallerinaModels.Processor) {
                        var processor = this.modelAttr("children").models[id];
                        var processorView = new ProcessorView({
                            model: processor,
                            center: new DiagramCore.Models.Point({x: xValue, y: yValue}),
                            canvas: this.canvas
                        });
                        processorView.render();
                        processor.setY(yValue);
                        yValue += processor.getHeight() + 30;
                        totalIncrementedHeight = totalIncrementedHeight + processor.getHeight() + 30;
                    } else {
                        var messagePoint = this.modelAttr("children").models[id];
                        if (messagePoint.direction() == "outbound") {
                            if (!_.isUndefined(messagePoint.forceY) && _.isEqual(messagePoint.forceY, true)) {
                                yValue = messagePoint.y();
                            }
                            messagePoint.y(yValue);
                            messagePoint.x(xValue);
                        } else {
                            if (!_.isUndefined(messagePoint.forceY) && _.isEqual(messagePoint.forceY, true)) {
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
                this.adjustHeight(this.d3el, totalHeight - initialHeight);

                if (diagram.highestLifeline == undefined || diagram.highestLifeline.getHeight() < this.model.getHeight()) {
                    diagram.highestLifeline = this.model;
                    defaultView.render();
                    return false;
                }
                return this.d3el;
            },

            renderMessages: function () {
                for (var id in this.modelAttr("children").models) {
                    var messagePoint = this.modelAttr("children").models[id];
                    if ((messagePoint instanceof BallerinaModels.MessagePoint)) {
                        var linkView = new MessageLinkView({
                            model: messagePoint.message(),
                            options: {class: "message"}
                        });
                        linkView.render("#" + defaultView.options.diagram.wrapperId, "messages");
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

                var textModel = this.model.attributes.textModel;
                if (textModel.dynamicRectWidth() === undefined) {
                    textModel.dynamicRectWidth(130);
                }

                var rect = d3Ref.draw.genericCenteredRect(center, prefs.rect.width + 30, prefs.rect.height, 0, 0, group, '', textModel)
                    .classed(prefs.rect.class, true).classed("genericR", true);

                var middleRect = d3Ref.draw.centeredBasicRect(createPoint(center.get('x'), center.get('y') + prefs.rect.height / 2 + prefs.line.height / 2), prefs.middleRect.width, prefs.middleRect.height, 0, 0, group)
                    .classed(prefs.middleRect.class, true);

                var drawMessageRect = d3Ref.draw.centeredBasicRect(createPoint(center.get('x'), center.get('y') + prefs.rect.height / 2 + prefs.line.height / 2), (prefs.middleRect.width * 0.4), prefs.middleRect.height, 0, 0, group)
                    .on("mousedown", function () {
                        d3.event.preventDefault();
                        d3.event.stopPropagation();
                        var m = d3.mouse(this);
                        prefs.diagram.clickedLifeLine = viewObj.model;
                        prefs.diagram.trigger("messageDrawStart", viewObj.model, new DiagramCore.Models.Point({
                            'x': center.x(),
                            'y': m[1]
                        }));

                    });

                var rectBottom = d3Ref.draw.genericCenteredRect(createPoint(center.get('x'), center.get('y') + prefs.line.height), prefs.rect.width + 30, prefs.rect.height, 0, 0, group, '', textModel)
                    .classed(prefs.rect.class, true).classed("genericR", true);

                var line = d3Ref.draw.verticalLine(createPoint(center.get('x'), center.get('y') + prefs.rect.height / 2), prefs.line.height - prefs.rect.height, group)
                    .classed(prefs.line.class, true);
                var text = d3Ref.draw.genericCenteredText(center, title, group, textModel)
                    .classed(prefs.text.class, true).classed("genericT", true);
                var textBottom = d3Ref.draw.genericCenteredText(createPoint(center.get('x'), center.get('y') + prefs.line.height), title, group, textModel)
                    .classed(prefs.text.class, true).classed("genericT", true);


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

                var optionMenuStartX = center.x() + 2 + (prefs.rect.width + 30) / 2;
                var optionMenuStartY = center.y() - prefs.rect.height / 2;
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

                        defaultView.drawPropertiesPane(d3Ref, options,
                            viewObj.model.get("utils").getMyParameters(viewObj.model),
                            viewObj.model.get('utils').getMyPropertyPaneSchema(
                                viewObj.model));
                    }
                });

                deleteOption.on("click", function () {
                    //Get the parent of the model and delete it from the parent
                    if (~viewObj.model.get("title").indexOf("Resource")) {
                        var resourceElements = defaultView.model.get("diagramResourceElements").models;
                        for (var itr = 0; itr < resourceElements.length; itr++) {
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
                        for (var itr = 0; itr < endpointElements.length; itr++) {
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

                return group;
            }

        });

    return LifeLineView;
});

