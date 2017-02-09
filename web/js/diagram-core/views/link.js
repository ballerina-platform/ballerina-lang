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
define(['lodash', './element'], function (_, DiagramElementView) {

    var LinkView = DiagramElementView.extend(
    /** @lends LinkView.prototype */
    {
        /**
         * @augments DiagramElementView.View
         * @constructs
         * @class LinkView Represents the view for links in a diagram.
         * @param {Object} options Rendering options for the view
         */
        initialize: function (options) {
            this.options = options || {};
            DiagramElementView.prototype.initialize.call(this, options);
        },
        // disable horizontal drag for links
        horizontalDrag: function () {
            return false;
        },

        sourceMoved: function (event) {
            this.d3el.attr('x1', this.model.source().point().x());
            this.d3el.attr('y1', this.model.source().point().y());
        },

        destinationMoved: function (event) {
            this.d3el.attr('x2', this.model.destination().point().x());
            this.d3el.attr('y2', this.model.destination().point().y());
        },

        render: function () {
            var line = this.getD3Ref().draw.lineFromPoints(this.model.source().point(), this.model.destination().point())
                .classed(this.options.class, true);

            this.model.source().on("connectingPointChanged", this.sourceMoved, this);
            this.model.destination().on("connectingPointChanged", this.destinationMoved, this);

            this.d3el = line;
            this.el = line.node();
            return line;
        }

    });

    return LinkView;
});

