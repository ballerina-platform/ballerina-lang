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

var Processors = (function (processors) {

    var manipulators = processors.manipulators || {};

    //Payload Factory mediator definition
    var forkProcessor = {
        id: "ForkProcessor",
        title: "Fork",
        icon: "images/tool-icons/datamapper.svg",
        colour : "#27ae60",
        type : "CustomProcessor",
        dragCursorOffset : { left: 50, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/datamapper_drag.svg").mimeType("image/svg+xml").get(function(error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "100px").attr("height", "140px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }
            return cloneCallBack;
        },
        utils: {
            getMySubTree: function (model) {
                return new TreeNode("forkProcessor", "forkProcessor", "forkProcessor {", "}");
            },

            canConnectTo: function () {
                return ['Worker', 'EndPoint'];
            },

            init: function (view, d3Ref) {
                if (!_.isUndefined(view.viewRoot)) {
                    var center = view.model.get('centerPoint');
                    var processorW = 24;
                    var processorH = 80;
                    var defaultForks = 2;
                    var forkCircleOffset = 30;
                    view.model.setHeight(processorH);
                    view.model.setWidth(processorW);

                    var forkRect = d3Ref.draw.centeredRect(center, processorW, processorH, 0, 0,
                        view.viewRoot, "#FFFFFF");
                    var triStartX = center.x() - processorW/2;
                    var triStartY = center.y() - processorH/2;
                    var points = "" + triStartX + "," + triStartY + " " + (triStartX + 5) + "," + (triStartY + 5) + " " + triStartX + "," + (triStartY + 10);

                    // If there are forks children already, then change the y coordinates
                    if(view.model.get('children').models.length > 0) {
                        for (var x = 0; x < view.model.get('children').models.length; x ++) {
                            view.model.get('children').models[x].setY(triStartY + forkCircleOffset*(x + 1));
                        }
                    }
                    var forkPolyLine =  view.viewRoot.append("polyline")
                        .attr("points", points);

                    for (var x = 0; x < defaultForks; x ++) {
                        var circle = this.drawForkStarts(center.x(), triStartY + forkCircleOffset*(x + 1), 5, view, d3Ref);
                    }

                }
            },

            drawForkStarts: function (x, y, r, view, d3Ref) {
                var circle = d3Ref.draw.circle(x, y, r, view.viewRoot);
                var startPoint = createPoint(x, y);
                circle.on('mousedown', function () {
                    d3.event.preventDefault();
                    d3.event.stopPropagation();
                    defaultView.model.trigger("messageDrawStart", view.model, startPoint);
                });

                return circle;
            }
        }

    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    manipulators.ForkProcessor = forkProcessor;

    processors.manipulators = manipulators;

    return processors;

}(Processors || {}));
