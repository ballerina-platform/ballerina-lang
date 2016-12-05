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
define(['d3', 'ast/node'], function (d3, TreeNode) {

    //Define manipulator mediators
    var InvokeMediator = {
        id: "InvokeMediator",
        title: "Invoke",
        icon: "images/tool-icons/invoke.svg",
        colour : "#ffffff",
        type : "Action",
        editable : true,
        deletable: true,
        width: 130,
        height: 30,
        hasOutputConnection : true,
        dragCursorOffset : { left: 50, top: -5 },
        // For inout type message link value is 2
        messageLinkType : 2,
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/invoke.svg").mimeType("image/svg+xml").get(function(error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "100px").attr("height", "42px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }
            return cloneCallBack;
        },
        init: function (view) {
            if (!_.isUndefined(view.viewRoot)) {
                var center = view.model.get('center');
                var rectangle = view.viewRoot.draw.centeredRect(center, 20, 50, 0, 0, view.viewRoot, "#334455");
                rectangle.on('mouseover', function () {
                    defaultView.model.selectedNode = view.model;
                    rectangle.style("fill", "green").style("fill-opacity", 1)
                        .style("cursor", 'url(images/BlackHandwriting.cur), pointer');
                }).on('mouseout', function () {
                    if(_.isEqual(defaultView.model.selectedNode, view.model)){
                        defaultView.model.destinationLifeLine = null;
                        defaultView.model.destinationProcessor = view.model;
                        defaultView.model.selectedNode = null;
                    }
                    rectangle.style("fill", "#2c3e50").style("fill-opacity", 1);
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
                    defaultView.model.trigger("messageDrawStart", view.model, startPoint, newStartPointFn);
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
                        linkView.render("#" + defaultView.options.diagram.wrapperId, "messages");
                    }
                }
            }

        },
        parameters: [
            {
                key: "message",
                value: "Message"
            },
            {
                key: "description",
                value: "Description"
            }
        ],
        propertyPaneSchema: [
            {
                key: "message",
                text: "Message"
            },
            {
                key: "description",
                text: "Description"
            }
        ],
        utils: {
            getMyPropertyPaneSchema : function () {
                return Processors.manipulators.InvokeMediator.propertyPaneSchema;
            },
            getMyParameters: function (model) {
                return model.attributes.parameters;
            },
            saveMyProperties: function (model, inputs) {
                model.attributes.parameters = [
                    {
                        key: "condition",
                        value: inputs.message.value
                    },
                    {
                        key: "description",
                        value: inputs.description.value
                    }
                ];
            },
            getMySubTree: function (model) {
                var epTitle = undefined;
                var uri = undefined;

                var endpoint = model.get('outputConnector').get('message').get('destination').get('parent');
                uri = endpoint.get('parameters')[1].value;
                epTitle = endpoint.get('parameters')[0].value;
                definedConstants["HTTPEP"] = {name: epTitle, value: uri};
                return new TreeNode("InvokeMediator", "InvokeMediator", ("response = invoke(endpointKey=" + epTitle + ", messageKey=m)"), ";");
            },
            canConnectTo: function () {
                return ['EndPoint'];
            },
            createMyModel : function (model) {
                var position = createPoint(0, 0);
                var processor = model.createProcessor(
                    Processors.manipulators.InvokeMediator.title,
                    position,
                    Processors.manipulators.InvokeMediator.id,

                    {
                        type: Processors.manipulators.InvokeMediator.type,
                        initMethod: Processors.manipulators.InvokeMediator.init,
                        editable: Processors.manipulators.InvokeMediator.editable,
                        deletable: Processors.manipulators.InvokeMediator.deletable,
                        hasOutputConnection : Processors.manipulators.InvokeMediator.hasOutputConnection,
                        messageLinkType : Processors.manipulators.InvokeMediator.messageLinkType
                    },
                    {colour: Processors.manipulators.InvokeMediator.colour},
                    Processors.manipulators.InvokeMediator.parameters,
                    Processors.manipulators.InvokeMediator.utils
                );
                Processors.manipulators.InvokeMediator.init(model, processor);
                model.addChild(processor);
                return processor;
            }
        }
    };

    return InvokeMediator;

});
