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

define(['require', 'jquery', 'd3', 'd3utils', 'backbone', 'diagram_core', 'lodash'], function (require, $, d3, D3Utils, Backbone, DiagramCore,  _) {

    // View to represent Diagram Preview.
    var DiagramPreview = Backbone.View.extend(
        /** @lends DiagramPreview.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class DiagramPreview Represents an outline view (a small preview) of a given DiagramView
             * @param {Object} options Rendering options for the view - Selector for container and Main DiagramView
             */
            initialize: function (options) {
                if (_.isUndefined(options.mainView)) {
                    throw "mainView is not set."
                }
                options.containerSelector = options.containerSelector || ".preview-container";
                this.$el = $(options.containerSelector);
                this.mainView = options.mainView;
                this.mainView.on("renderCompleted", this.render, this);
                this.width = options.width || "100%";
                this.height = options.height || "160px";
                this.fitToCanvasOpts = options.fitToCanvasOpts || {};
            },

            /**
             * Renders preview for the given diagram.
             *
             * @param {SVGSVGElement} [mainSVG] SVG element to be cloned for preview.
             */
            render: function (mainSVG) {
                if (!mainSVG) {
                    var mainSVG = this.mainView.d3svg.node();
                }

                // clone SVG for preview
                var previewSVG = $(mainSVG).clone();
                // get
                var limits = this.mainView.panAndZoom.limits;
                var padding = this.mainView.options.diagram.padding;
                // set viewbox to fit in all content
                d3.select(previewSVG.get((0)))
                    .attr("width", this.width)
                    .attr("height", this.height)
                    // get current limits and add a little offset to make red box visible at zoom out upper boundary
                    .attr("viewBox", (limits.x) + " " + (limits.y ) + " "
                    + ((limits.x2 - limits.x)) + " " + ((limits.y2 - limits.y)))
                    .attr("preserveAspectRatio", "xMinYMin meet");

                // unset current preview rendered on div
                this.$el.empty();

                // set new preview
                var previewContainer = $("<div></div>");
                previewContainer.attr("class", "diagram-preview");
                this.$el.append(previewContainer);
                previewContainer.append(previewSVG);

                // create controls
                var controlsContainer = $("<div></div>");
                controlsContainer.attr("class", "controls-container");
                this.$el.append(controlsContainer);

                var preview = this;

                var fitToCanvasControl = $("<span class='glyphicon glyphicon-fullscreen fit-to-area-btn' aria-hidden=true'></span>");
                controlsContainer.append(fitToCanvasControl);
                fitToCanvasControl.click(function (evt) {
                    preview.mainView.setViewBox(
                        preview.mainView.panAndZoom.limits.x,
                        preview.mainView.panAndZoom.limits.y,
                        preview.mainView.panAndZoom.limits.x2 - preview.mainView.panAndZoom.limits.x,
                        preview.mainView.panAndZoom.limits.y2 - preview.mainView.panAndZoom.limits.y);
                });

                // create zoom range controller
                var zoomRangeController = $("<div></div>");
                controlsContainer.append(zoomRangeController);
                zoomRangeController.attr("class", "zoom-slider");

                this.slider = zoomRangeController.slider({
                    min: 1,
                    max: 100,
                    step: 1,
                    slide: function (event, ui) {
                        var limitWidth = limits.x2 - limits.x;
                        var newWidth = limitWidth * ((100 - ui.value) / 100);
                        var newHeight = newWidth * (1 / preview.mainView.getCurrentAspectRatio());
                        var currentVB = preview.mainView.panAndZoom.getViewBox();
                        var newX = currentVB.x + ((currentVB.width - newWidth) / 2);
                        var newY = currentVB.y + ((currentVB.height - newHeight) / 2);
                        preview.mainView.setViewBox(newX, newY, newWidth, newHeight);
                    }
                });

                //init pan and zoom area marker box inside preview
                var previewWrapperGroup = d3.select(previewSVG.find("g").first()[0]);
                var panZoomMarkerRect = previewWrapperGroup.append('rect');

                var checkLimits = function (viewBox, limits) {
                    var limitsHeight, limitsWidth, reductionFactor, vb;
                    vb = $.extend({}, viewBox);
                    limitsWidth = Math.abs(limits.x2 - limits.x);
                    limitsHeight = Math.abs(limits.y2 - limits.y);
                    if (vb.width > limitsWidth) {
                        if (vb.height > limitsHeight) {
                            if (limitsWidth > limitsHeight) {
                                reductionFactor = limitsHeight / vb.height;
                                vb.height = limitsHeight;
                                vb.width = vb.width * reductionFactor;
                            } else {
                                reductionFactor = limitsWidth / vb.width;
                                vb.width = limitsWidth;
                                vb.height = vb.height * reductionFactor;
                            }
                        } else {
                            reductionFactor = limitsWidth / vb.width;
                            vb.width = limitsWidth;
                            vb.height = vb.height * reductionFactor;
                        }
                    } else if (vb.height > limitsHeight) {
                        reductionFactor = limitsHeight / vb.height;
                        vb.height = limitsHeight;
                        vb.width = vb.width * reductionFactor;
                    }
                    if (vb.x < limits.x) {
                        vb.x = limits.x;
                    }
                    if (vb.y < limits.y) {
                        vb.y = limits.y;
                    }
                    if (vb.x + vb.width > limits.x2) {
                        vb.x = limits.x2 - vb.width;
                    }
                    if (vb.y + vb.height > limits.y2) {
                        vb.y = limits.y2 - vb.height;
                    }
                    return vb;
                };
                var mainViewBox = preview.mainView.panAndZoom.getViewBox();
                panZoomMarkerRect
                    .attr("x", mainViewBox.x)
                    .attr("y", mainViewBox.y)
                    .attr("width", mainViewBox.width)
                    .attr("height", mainViewBox.height)
                    .attr("class", "pan-zoom-marker");

                var panZoomMarkerRectDrag = d3.drag()
                    .on("start", function () {

                    })
                    .on("drag", function () {
                        var evt = d3.event;
                        var newX = parseFloat(panZoomMarkerRect.attr("x")) + evt.dx;
                        var newY = parseFloat(panZoomMarkerRect.attr("y")) + evt.dy;
                        var oldW = parseFloat(panZoomMarkerRect.attr("width"));
                        var oldH = parseFloat(panZoomMarkerRect.attr("height"));

                        var approvedVB = checkLimits({x: newX, y: newY, width: oldW, height: oldH}, limits);
                        panZoomMarkerRect.attr("x", approvedVB.x);
                        panZoomMarkerRect.attr("y", approvedVB.y);
                        preview.mainView.setViewBox(approvedVB.x, approvedVB.y, oldW, oldH);
                    })
                    .on("end", function () {

                    });
                panZoomMarkerRect.call(panZoomMarkerRectDrag);

                if (this.mainView.isEmpty()) {
                    panZoomMarkerRect.attr("class", "pan-zoom-marker hidden");
                }

                this.mainView.on("viewBoxChange", function (newViewBox, animationTime) {
                    panZoomMarkerRect.attr("x", newViewBox.x)
                        .attr("y", newViewBox.y)
                        .attr("width", newViewBox.width)
                        .attr("height", newViewBox.height);

                    var sliderVal = 100 - ((newViewBox.width / ((limits.x2 - limits.x) - padding)) * 100);
                    this.slider.slider("option", "value", sliderVal);

                }, this);

                var resetZoomToDefaultControl = $("<span class='glyphicon glyphicon-screenshot reset-zoom-btn' aria-hidden=true'></span>");
                controlsContainer.append(resetZoomToDefaultControl);
                resetZoomToDefaultControl.click(function (evt) {
                    var defaultViewBox = preview.mainView.panAndZoom.initialViewBox;
                    preview.mainView.setViewBox(defaultViewBox.x, defaultViewBox.y,
                        defaultViewBox.width, defaultViewBox.height);
                });

            }

        });

    return DiagramPreview;
});