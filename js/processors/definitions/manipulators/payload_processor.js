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

    // Payload Processor Definition
    var payloadProcessor = {
        id: "PayloadProcessor",
        title: "Payload",
        icon: "images/tool-icons/payload.svg",
        colour : "#2980b9",
        type : "UnitProcessor",
        dragCursorOffset : { left: 24, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/payload_drag.svg").mimeType("image/svg+xml").get(function(error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "48px").attr("height", "108px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }
            return cloneCallBack;
        },
        parameters: [
            {
                key: "contentType",
                value: "Content Type"
            },
            {
                key: "messageReference",
                value: "Message Reference"
            },
            {
                key: "payload",
                value: "Payload"
            }
        ],
        getSchema: function () {
            return {
                title: "Payload Processor",
                type: "object",
                properties: {
                    ContentType: {"type": "string"},
                    MessageRef: {"type": "string"},
                    Payload: {"type": "string"}
                }
            };
        },
        getEditableProperties: function (parameters) {
            var editableProperties = {};
            editableProperties.ContentType = parameters[0];
            editableProperties.MessageRef = parameters[1];
            editableProperties.Payload = parameters[2];
            return editableProperties;
        },
        getMySubTree: function (model) {
            return new TreeNode("PayloadProcessor", "PayloadProcessor", "JSON.setPayload(messageRef = response, payload = {\"error\":\"backend failed\"});");
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    manipulators.PayloadProcessor = payloadProcessor;

    processors.manipulators = manipulators;

    return processors;

}(Processors || {}));
