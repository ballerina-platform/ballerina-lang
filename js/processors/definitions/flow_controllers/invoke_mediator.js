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

    var flowControllers = processors.flowControllers || {};

    //Define manipulator mediators
    var invokeMediator = {
        id: "InvokeMediator",
        title: "Invoke Mediator",
        icon: "images/SwitchMediator.gif",
        colour : "#334455",
        type : "Custom",
        dragCursorOffset : { left: 10, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var svgRoot = view.createSVGForDraggable();
                var rect = svgRoot.draw.basicRect(0, 0, 20, 50, 0, 0).attr("fill-opacity", 1);
                return svgRoot.getDraggableRoot();
            }
            return cloneCallBack;
        },
        init: function (view) {
            if (!_.isUndefined(view.viewRoot)) {
                var center = view.model.get('center');
                var rectangle = view.viewRoot.draw.centeredRect(center, 20, 50, 0, 0, view.viewRoot, "#334455");
                rectangle.on('mouseover', function () {
                    diagram.selectedNode = view.model;
                    rectangle.style("fill", "green").style("fill-opacity", 1)
                        .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                }).on('mouseout', function () {
                    if(_.isEqual(diagram.selectedNode, view.model)){
                        diagram.destinationLifeLine = null;
                        diagram.destinationProcessor = view.model;
                        diagram.selectedNode = null;
                    }
                    rectangle.style("fill", "#334455").style("fill-opacity", 1);
                }).on('mousedown', function () {
                    d3.event.preventDefault();
                    d3.event.stopPropagation();
                    var startPoint = center.clone().move(0, -20);
                    var newStartPointFn = function (endX, endY) {
                        if (startPoint.x() > endX) {
                            return {x: startPoint.x() - 10, y: startPoint.y()};
                        } else {
                            return {x: startPoint.x() + 10, y: startPoint.y()};
                        }
                    };
                    diagram.trigger("messageDrawStart", view.model, startPoint, newStartPointFn);
                });
                Object.getPrototypeOf(view.viewRoot).rect = rectangle;

                function resyncPointCordinates(messagePoint){
                    if (_.isEqual(messagePoint.direction(), "inbound")) {
                        var sPX = messagePoint.message().source().x();
                        messagePoint.y(center.y() + 20);
                        messagePoint.message().source().forceY = true;
                        messagePoint.message().source().y(messagePoint.y());
                        view.modelAttr("children").add(messagePoint);
                    } else if (_.isEqual(messagePoint.direction(), "outbound")) {
                        var sPX = messagePoint.message().destination().x();
                        messagePoint.y(center.y() - 20);
                        var dest = messagePoint.message().destination();
                        if(dest.parent()){
                            dest.x(dest.parent().get("centerPoint").x());
                        }
                        dest.forceY = true;
                        dest.y(messagePoint.y());
                        view.modelAttr("children").add(messagePoint);
                    }
                    if (center.x() > sPX) {
                        messagePoint.x(center.x() - 10);
                    } else {
                        messagePoint.x(center.x() + 10);
                    }
                }

                //override addChild
                view.model.addChild = function (messageLinkPoint, opts) {
                    // Set the parent of the message link point
                    messageLinkPoint.parent(view.model);
                    resyncPointCordinates(messageLinkPoint);
                };

                for(var index = 0; index < view.modelAttr("children").length; index++){
                    var point = view.modelAttr("children").at(index);
                    resyncPointCordinates(point);
                    if(point && point instanceof SequenceD.Models.MessagePoint && _.isEqual(point.direction(), "outbound")) {
                        var linkView = new SequenceD.Views.MessageLink({
                            model: point.message(),
                            options: {class: "message"}
                        });
                        linkView.render("#diagramWrapper", "messages");
                    }
                }
            }

        },
        parameters: [],

        getMySubTree: function (model) {
            var messageLinks = model.get('children').models;
            var endpoint = undefined;
            messageLinks.forEach(function (child) {
                if (_.isEqual(child.get('direction'), "inbound")) {
                    endpoint = child.get('message').get('source').get('parent').get('title');
                    // When we define the properties, need to extract the endpoint from the property
                    definedConstants["HTTPEP"] = {name: endpoint, value: "https://www.google.lk"};
                } else {
                    endpoint = "anonymous";
                }
            });
            return new TreeNode("InvokeMediator", "InvokeMediator", ("invoke(" + endpoint + ")"), ";");
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    flowControllers.InvokeMediator = invokeMediator;

    processors.flowControllers = flowControllers;

    return processors;

}(Processors || {}));