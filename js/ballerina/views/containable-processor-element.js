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
define(['lodash', 'diagram_core'], function ( _, DiagramCore) {

    var ContainableProcessorElementView = DiagramCore.Views.ShapeView.extend(
        /** @lends ContainableProcessorElement.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class LifeLineView Represents the view for lifeline components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                DiagramCore.Views.ShapeView.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID, centerPoint) {
                var thisModel = this.model;
                DiagramCore.Views.ShapeView.prototype.render.call(this, paperID);

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
                var middleRect = d3Ref.draw.centeredBasicRect( new DiagramCore.Models.Point({'x': center.x(), 'y':  center.y()+100}), 150, height, 0, 0);
                middleRect.on("mousedown", function () {
                    var m = d3.mouse(this);
                    prefs.diagram.trigger("messageDrawStart", viewObj.model,  new DiagramCore.Models.Point({'x': center.x(), 'y': m[1]}));
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
                var drawMessageRect = d3Ref.draw.centeredBasicRect(new DiagramCore.Models.Point({'x': center.x(), 'y': center.y()+100}), (prefs.middleRect.width * 0.4), height, 0, 0, d3Ref)
                    .on("mousedown", function () {
                        d3.event.preventDefault();
                        d3.event.stopPropagation();
                        var m = d3.mouse(this);

                        prefs.diagram.clickedLifeLine = viewObj.model;
                        prefs.diagram.trigger("messageDrawStart", viewObj.model,  new DiagramCore.Models.Point({'x': center.x(), 'y': m[1]}));

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
                    var ProcessorView = require('app/ballerina-diagram/views/processor'); //TODO: fix this
                    var processorView = new ProcessorView({model: processor});
                    //TODO : Please remove this if else with a proper implementation
                    if(processor.type == "messagePoint"){
                        yValue = yValue-20;
                    }
                    var processorCenterPoint =  new DiagramCore.Models.Point({'x': xValue, 'y': yValue});

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
                                viewObj.model.get('parent').attributes.parameters,
                                viewObj.model.attributes.parent.get("utils").getMyPropertyPaneSchema());
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
                    });

                    //group.remove();
                    //
                    //return newGroup;
                }

                return group;
            }

        });

    return ContainableProcessorElementView;
});

