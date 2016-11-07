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
define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'diagram', 'geometry'], function (require, $, d3, Backbone, _, Diagrams, GeoCore) {


    var MessageLinkView = Diagrams.Views.DiagramElementView.extend(
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
    return MessageLinkView;
});

