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

    var UnitProcessorElement = Diagrams.Views.ShapeView.extend(
        /** @lends UnitProcessorElement.prototype */
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

                var unitProcessorElement = this.drawUnitProcessor(this.modelAttr('centerPoint'), this.modelAttr('title'), this.options);
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

                this.d3el = unitProcessorElement;
                this.el = unitProcessorElement.node();
                return unitProcessorElement;
            },

            drawUnitProcessor: function (center, title, prefs) {
                var d3Ref = this.getD3Ref();
                var group = d3Ref.draw.group()
                    .classed(prefs.class, true);
                var rect = d3Ref.draw.centeredRect(center, prefs.rect.width, prefs.rect.height, 3, 3, group)
                    .classed(prefs.rect.class, true);
                var text = d3Ref.draw.centeredText(center, title, group)
                    .classed(prefs.text.class, true);
                Object.getPrototypeOf(group).rect = rect;
                Object.getPrototypeOf(group).title = text;
                Object.getPrototypeOf(group).translate = function (dx, dy) {
                    this.attr("transform", function () {
                        return "translate(" + [dx, dy] + ")"
                    })
                };

                return group;
            }

        });

    views.UnitProcessorElement = UnitProcessorElement;
    return sequenced;

}(SequenceD || {}));
