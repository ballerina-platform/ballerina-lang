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
        init: function (view) {
            if (!_.isUndefined(view.viewRoot)) {
                var center = view.model.get('center');
                var rectangle = view.viewRoot.draw.centeredRect(center, 20, 50, 1, 1, view.viewRoot, "#334455");
                rectangle.on('mouseover', function () {
                    diagram.selectedNode = view.model;
                    rectangle.style("fill", "green").style("fill-opacity", 1)
                        .style("cursor", 'url(http://www.rw-designer.com/cursor-extern.php?id=93354), pointer');
                }).on('mouseout', function () {
                    if(_.isEqual(diagram.selectedNode, view.model)){
                        diagram.destinationLifeLine = null;
                        diagram.destinationProcessor = view.model;
                        diagram.selectedNode = null;
                    }
                    rectangle.style("fill", "#334455").style("fill-opacity", 1);
                }).on('mousedown', function () {
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

                //override addChild
                view.model.addChild = function (messageLinkPoint, opts) {

                    if (_.isEqual(messageLinkPoint.direction(), "inbound")) {
                        // Set the parent of the message link point
                        messageLinkPoint.parent(diagram.clickedLifeLine);
                        var sPX = messageLinkPoint.message().source().x();
                        messageLinkPoint.y(center.y() + 20);
                        messageLinkPoint.message().source().forceY = true;
                        messageLinkPoint.message().source().y(messageLinkPoint.y());
                        view.modelAttr("children").add(messageLinkPoint);
                    } else if (_.isEqual(messageLinkPoint.direction(), "outbound")) {
                        // Set the parent of the message link point
                        messageLinkPoint.parent(view.model);
                        var sPX = messageLinkPoint.message().destination().x();
                        messageLinkPoint.y(center.y() - 20);
                        messageLinkPoint.message().destination().y(messageLinkPoint.y());
                        view.modelAttr("children").add(messageLinkPoint);
                    }
                    if (center.x() > sPX) {
                        messageLinkPoint.x(center.x() - 10);
                    } else {
                        messageLinkPoint.x(center.x() + 10);
                    }
                };

                for(var index = 0; index < view.modelAttr("children").length; index++){
                    var point = view.modelAttr("children").at(index);
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
                    endpoint = child.get('parent').get('title');
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