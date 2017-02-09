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
define(['lodash', 'diagram_core', 'd3'], function ( _, DiagramCore, d3) {

    var MessageView = DiagramCore.Views.LinkView.extend(
    /** @lends MessageView.prototype */
    {
        /**
         * @augments LinkView
         * @constructs
         * @class MessageView Represents the view for message components in Sequence Diagrams.
         * @param {Object} options Rendering options for the view
         */
        initialize: function (options) {
            DiagramCore.Views.LinkView.prototype.initialize.call(this, options);
        },

        render: function (paperID) {
            // call super
            DiagramCore.Views.LinkView.prototype.render.call(this, paperID);
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

    return MessageView;
});

