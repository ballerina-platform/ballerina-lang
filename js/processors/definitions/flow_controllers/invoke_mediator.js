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

                    var sPX = messageLinkPoint.message.source().get('centerPoint').x();
                    if (center.x() > sPX) {
                        messageLinkPoint.get('centerPoint').x(center.x() - 10);
                    } else {
                        messageLinkPoint.get('centerPoint').x(center.x() + 10);
                    }
                    if (_.isEqual(messageLinkPoint.direction, "inbound")) {
                        messageLinkPoint.get('centerPoint').y(center.x() + 20);
                        view.modelAttr("children").add(messageLinkPoint, {at: 0});
                    } else if (_.isEqual(messageLinkPoint.direction, "outbound")) {
                        messageLinkPoint.get('centerPoint').y(center.x() - 20);
                        view.modelAttr("children").add(messageLinkPoint, {at: 1});
                    }
                };

                // render messages
                var inboundPoint = view.modelAttr("children").at(0);
                if(inboundPoint && inboundPoint instanceof SequenceD.Models.MessagePoint) {
                    var linkView = new SequenceD.Views.MessageLink({
                        model: inboundPoint.message,
                        options: {class: "message"}
                    });
                    linkView.render("#diagramWrapper", "messages");
                }
                var outboundPoint = view.modelAttr("children").at(1);
                if(outboundPoint && outboundPoint instanceof SequenceD.Models.MessagePoint) {
                    var linkView = new SequenceD.Views.MessageLink({
                        model: outboundPoint.message,
                        options: {class: "message"}
                    });
                    linkView.render("#diagramWrapper", "messages");
                }
            }

        },
        parameters: []
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    flowControllers.InvokeMediator = invokeMediator;

    processors.flowControllers = flowControllers;

    return processors;

}(Processors || {}));