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
define(['lodash', 'd3', 'diagram_core', 'app/ballerina-old/models/life-line'], function ( _, d3, DiagramCore, LifeLine) {

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
                if(!_.has(options, 'serviceView')){
                    throw "config parent [ServiceView] is not provided.";
                }
                this.serviceView = _.get(options, 'serviceView');

                if(!_.has(options, 'application')){
                    throw "config parent [application] is not provided.";
                }
                this.application = _.get(options, 'application');
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID, centerPoint) {
                var model = this.model;
                DiagramCore.Views.ShapeView.prototype.render.call(this, paperID);

                var unitProcessorElement = this.drawUnitProcessor(centerPoint,
                    this.modelAttr('title'), this.options, model);
                var viewObj = this;

                this.d3el = unitProcessorElement;
                this.el = unitProcessorElement.node();
                return unitProcessorElement;
            },

            drawUnitProcessor: function (center, title, prefs, model) {

                var d3Ref = this.getD3Ref();
                var group = d3Ref.draw.group();
                var viewObj = this;
                //var deleteIconGroup = undefined;
                var path = undefined;
                var height = model.getHeight();
                var width = model.getWidth();
                var modelHeight = model.getHeight();


                var rect = d3Ref.draw.rectWithTitle(
                    center,
                    60,
                    30,
                    150,
                    model.getHeight(),
                    0,
                    0,
                    d3Ref,
                    this.modelAttr('viewAttributes').colour,
                    this.modelAttr('title')
                );

                rect.middleRect.on('mouseover', function () {
                    viewObj.serviceView.model.selectedNode = viewObj.model;
                    d3.select(this).style("fill", "green").style("fill-opacity", 0.1);
                }).on('mouseout', function () {
                    viewObj.serviceView.model.destinationLifeLine = viewObj.serviceView.model.selectedNode;
                    viewObj.serviceView.model.selectedNode = null;
                    d3.select(this).style("fill-opacity", 0.01);
                }).on('mouseup', function (data) {
                });

                group.middleRect = rect.middleRect;
                group.rect = rect.containerRect;
                group.titleRect = rect.titleRect;
                group.text = rect.text;

                var centerPoint = center;
                var xValue = centerPoint.x();
                var yValue = centerPoint.y();

                var totalHeight = 60;
                var totalWidth = 150;
                this.model.setHeight(30);

                var initWidth =rect.containerRect.attr("width");

                yValue += 60;
                for (var id in this.modelAttr("children").models) {
                    var processor = this.modelAttr("children").models[id];
                    var ProcessorView = require('app/ballerina-old/views/processor'); //TODO: fix this
                    var processorViewOptions = {
                        model: processor,
                        center: new DiagramCore.Models.Point({x: xValue, y: yValue}),
                        canvas: this.canvas,
                        serviceView: this.serviceView,
                        application: this.application
                    };
                    var processorView = new ProcessorView(processorViewOptions);
                    //TODO : Please remove this if else with a proper implementation
                    if(processor.type == "messagePoint"){
                        yValue = yValue-20;
                    }
                    var processorCenterPoint =  new DiagramCore.Models.Point({'x': xValue, 'y': yValue});

                    //TODO: Remove the wrapper ID param from the render method
                    processorView.render("#", processorCenterPoint, "processors");
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
                var newX = parseInt(rect.containerRect.attr("x")) - deviation;

                rect.containerRect.attr("height", totalHeight);
                rect.containerRect.attr("width", totalWidth);
                rect.containerRect.attr("x", newX);
                rect.titleRect.attr("x", parseInt(rect.titleRect.attr("x")) - deviation);
                rect.text.attr("x", parseInt(rect.text.attr("x")) - deviation);
                this.model.setHeight(totalHeight);
                this.model.get("parent").setHeight(this.model.get("parent").getHeight() + totalHeight - modelHeight);
                this.model.setWidth(totalWidth);
                this.model.setX(newX);
                rect.middleRect.attr("height", totalHeight-30);
                rect.middleRect.attr("width", totalWidth);
                rect.middleRect.attr("x", parseInt(rect.middleRect.attr("x")) - deviation);

                if (viewObj.model.get("title") === "Try" || viewObj.model.get("title") === "If") {
                    var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");
                    var optionMenuStartX = center.x() + 80;
                    var optionMenuStartY = center.y() - model.getHeight()/2;

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
                    rect.containerRect.on("click", function () {
                        viewObj.serviceView.model.selectedNode = viewObj.model;

                        if (optionsMenuGroup.classed("option-menu-hide")) {
                            optionsMenuGroup.classed("option-menu-hide", false);
                            optionsMenuGroup.classed("option-menu-show", true);

                            if (viewObj.serviceView.model.selectedOptionsGroup) {
                                viewObj.serviceView.model.selectedOptionsGroup.classed("option-menu-hide", true);
                                viewObj.serviceView.model.selectedOptionsGroup.classed("option-menu-show", false);
                            }
                            if (viewObj.serviceView.model.propertyWindow) {
                                viewObj.serviceView.model.propertyWindow = false;
                                viewObj.serviceView.enableDragZoomOptions();
                                $('#property-pane-svg').empty();
                            }
                            viewObj.serviceView.model.selectedOptionsGroup = optionsMenuGroup;

                        } else {
                            optionsMenuGroup.classed("option-menu-hide", true);
                            optionsMenuGroup.classed("option-menu-show", false);
                            if (viewObj.serviceView.model.propertyWindow) {
                                viewObj.serviceView.model.propertyWindow = false;
                                viewObj.serviceView.enableDragZoomOptions();
                                viewObj.serviceView.render();
                            }
                            viewObj.serviceView.model.selectedOptionsGroup = null;
                        }
                    });

                    editOption.on("click", function () {
                        if (viewObj.serviceView.model.propertyWindow) {
                            viewObj.serviceView.model.propertyWindow = false;
                            viewObj.serviceView.enableDragZoomOptions();
                            viewObj.serviceView.render();

                        } else {
                            var options = {
                                x: parseFloat(this.getAttribute("x")) + 6,
                                y: parseFloat(this.getAttribute("y")) + 21
                            };

                            viewObj.serviceView.model.selectedNode = viewObj.model.attributes.parent;
                            viewObj.serviceView.drawPropertiesPane(d3Ref, options,
                                viewObj.model.get('parent').attributes.parameters,
                                viewObj.model.attributes.parent.get("utils").getMyPropertyPaneSchema());
                        }
                    });

                    deleteOption.on("click", function () {
                    //Get the parent of the model and delete it from the parent
                    var parentModel = viewObj.model.get("parent").get("parent");
                    var parentModelChildren = parentModel.get("children").models;
                    //Get diagram highest height
                    var highestHeight = viewObj.model.get("parent").get("serviceView").highestLifeline.get("height");
                    for (var itr = 0; itr < parentModelChildren.length; itr ++) {
                        if (parentModelChildren[itr].cid === viewObj.model.get("parent").cid) {
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
                            parentModelChildren.splice(itr, 1);
                            // adjust life-line height
                            parentElement.setHeight(lifelineHeight - currentElementHeight);
                            // if the current life-line is the tallest life-line we adjust it's height
                            if(lifelineHeight + currentElementHeight >= highestHeight){
                                viewObj.model.get("parent").get("serviceView").highestLifeline.setHeight(highestHeight - currentElementHeight);
                            }
                            viewObj.serviceView.render();
                            break;
                        }
                    }
                 });
                }

                return group;
            }

        });

    return ContainableProcessorElementView;
});

