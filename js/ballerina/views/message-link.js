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
define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'diagram_core'], function (require, $, d3, Backbone, _, DiagramCore) {


    var createPoint = function (x, y) {
        return new DiagramCore.Models.Point({x: x, y: y});
    };

    var MessageLinkView = DiagramCore.Views.DiagramElementView.extend(
        /** @lends Processor.prototype */
        {
            /**
             * @augments ShapeView
             * @constructs
             * @class Processor Represents the view for processor components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                if (!_.has(options, 'serviceView')) {
                    throw "config parent [serviceView] is not provided.";
                }
                this.serviceView = _.get(options, 'serviceView');
                if (!this.serviceView.isPreviewMode()) {
                    if (!_.has(this.serviceView, 'toolPalette.dragDropManager')) {
                        throw "dragDropManager is not provided.";
                    }
                    this.dragDropManager = this.serviceView.toolPalette.dragDropManager;
                }

                options.canvas = this.serviceView.d3el;
                DiagramCore.Views.DiagramElementView.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID, status) {
                if (status == "messages") {
                    DiagramCore.Views.DiagramElementView.prototype.render.call(this, paperID);
                    var d3ref = this.getD3Ref();
                    var group = d3ref.draw.group();
                    var viewObj = this;
                    var optionsMenuGroup = group.append("g").attr("class", "option-menu option-menu-hide");
                    var destinationCenterPoint = this.model.destination().centerPoint();
                    var sourceCenterPoint = this.model.source().centerPoint();
                    var delXPosition = ((Math.round(sourceCenterPoint.get('x'))) +
                        Math.round(destinationCenterPoint.get('x'))) / 2;

                    var optionMenuWrapper = d3ref.draw.rect(Math.round(delXPosition) - 15,
                        Math.round(sourceCenterPoint.get('y')) + 10,
                        30,
                        30,
                        0,
                        0,
                        optionsMenuGroup, "#f8f8f3").attr("style", "stroke: #ede9dc; stroke-width: 1; opacity:0.5; cursor: pointer").on("mouseover", function () {
                        d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7; cursor: pointer");
                    }).on("mouseout", function () {
                        d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
                    });

                    var deleteOption = d3ref.draw.rect(Math.round(delXPosition) - 12,
                        Math.round(sourceCenterPoint.get('y')) + 13,
                        24,
                        24,
                        0,
                        0,
                        optionsMenuGroup, "url(#delIcon)").attr("style", "opacity:0.5; cursor: pointer").on("mouseover", function () {
                        d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 1; cursor: pointer");
                        optionMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: .7");
                    }).on("mouseout", function () {
                        d3.select(this).attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
                        optionMenuWrapper.attr("style", "stroke: #ede9dc; stroke-width: 1; opacity: 0.5; cursor: pointer");
                    });

                    // When we have unequal y coordinates in source and destination message points, we need to set them to a common value
                    // as we need a horizontal line always. Here we will use priority point and set that y value to both points.
                    if (!_.isUndefined(this.model.priority())) {
                        sourceCenterPoint.y(this.model.priority().centerPoint().y());
                        destinationCenterPoint.y(this.model.priority().centerPoint().y());
                    }

                    if (this.model.type() === 2 /** FIXME: use a constant **/) {
                        // Drawing an IN_OUT message.
                        var lineDestinationCenterPoint = createPoint(destinationCenterPoint.x(), Math.round(destinationCenterPoint.y()) - 5);
                        var lineSourceCenterPoint = createPoint(sourceCenterPoint.x(), Math.round(sourceCenterPoint.y()) - 5);
                        var line = d3ref.draw.lineFromPoints(lineSourceCenterPoint, lineDestinationCenterPoint, group)
                            .classed(this.options.class, true);

                        //Creating arrow head for line
                        var point1x = Math.round(lineDestinationCenterPoint.x()) - 9;
                        var point1y = Math.round(lineDestinationCenterPoint.y()) - 5;
                        var point2x = Math.round(point1x) + 8;
                        var point2y = Math.round(point1y) + 5;
                        var point3y = Math.round(point2y) + 5;

                        var point1 = [point1x, point1y];
                        var point2 = [point2x, point2y];
                        var point3 = [point1x, point3y];
                        var points = point1.join(',') + " " + point2.join(",") + " " + point3.join(",");
                        var arrowHead = d3ref.draw.arrowPolygon(points, group);
                        // End: creating arrow head for line

                        var line2DestinationCenterPoint = createPoint(destinationCenterPoint.x(), Math.round(destinationCenterPoint.y()) + 10);
                        var line2SourceCenterPoint = createPoint(sourceCenterPoint.x(), Math.round(sourceCenterPoint.y()) + 10);

                        var line2 = d3ref.draw.lineFromPoints(line2DestinationCenterPoint, line2SourceCenterPoint, group)
                            .classed(this.options.class, true);

                        //Creating arrow head for line2
                        var reply1x = Math.round(line2SourceCenterPoint.x()) + 9;
                        var reply1y = Math.round(line2SourceCenterPoint.y()) + 5;
                        var reply2x = Math.round(line2SourceCenterPoint.x()) - 9 + 10;
                        var reply2y = Math.round(line2SourceCenterPoint.y()) - 5 + 5;
                        var reply3y = Math.round(reply2y) - 5;

                        var replyPoint1 = [reply1x, reply1y];
                        var replyPoint2 = [reply2x, reply2y];
                        var replyPoint3 = [reply1x, reply3y];
                        var replyPoints = replyPoint1.join(',') + " " + replyPoint2.join(",") + " " + replyPoint3.join(",");
                        var arrowHead = d3ref.draw.arrowPolygon(replyPoints, group);
                        //End: creating arrow head for line2

                    } else {
                        // Drawing an OUT_ONLY message.
                        var line = d3ref.draw.lineFromPoints(sourceCenterPoint, destinationCenterPoint, group)
                            .classed(this.options.class, true);
                        // Check arrowhead side depending on direction for reply mediator
                        if (this.model.attributes.priority.attributes.direction == "inbound") {
                            var point1x = Math.round(destinationCenterPoint.x()) - 9;
                            var point1y = Math.round(destinationCenterPoint.y()) - 5;
                            var point2x = Math.round(point1x) + 8;
                            var point2y = Math.round(point1y) + 5;
                            var point3y = Math.round(point2y) + 5;

                        }
                        else {
                            var point1x = Math.round(destinationCenterPoint.x()) + 9;
                            var point1y = Math.round(destinationCenterPoint.y()) + 5;
                            var point2x = Math.round(destinationCenterPoint.x()) - 9 + 10;
                            var point2y = Math.round(destinationCenterPoint.y()) - 5 + 5;
                            var point3y = Math.round(point2y) - 5;

                        }
                        var point1 = [point1x, point1y];
                        var point2 = [point2x, point2y];
                        var point3 = [point1x, point3y];
                        var points = point1.join(',') + " " + point2.join(",") + " " + point3.join(",");
                        var arrowHead = d3ref.draw.arrowPolygon(points, group);

                    }

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

                        this.serviceView.render();
                    });

                    this.d3el = group;
                    this.el = group.node();
                    return this.d3el;
                }
            }

        });
    return MessageLinkView;
});

