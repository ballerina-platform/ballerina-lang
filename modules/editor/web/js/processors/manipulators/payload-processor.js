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

    // Payload Processor Definition
    var PayloadProcessor = {
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
        propertyPaneSchema: [
            {
                key: "contentType",
                text: "Content Type"
            },
            {
                key: "messageReference",
                text: "Message Reference"
            },
            {
                key: "payload",
                text: "Payload"
            }
        ],
        utils: {
            getMyPropertyPaneSchema : function () {
                return Processors.manipulators.PayloadProcessor.propertyPaneSchema;
            },
            getMyParameters: function (model) {
                return model.attributes.parameters;
            },
            saveMyProperties: function (model, inputs) {
                model.attributes.parameters = [
                    {
                        key: "contentType",
                        value: inputs.contentType.value
                    },
                    {
                        key: "messageReference",
                        value: inputs.messageReference.value
                    },
                    {
                        key: "payload",
                        value: inputs.payload.value
                    }
                ];
            },
            getMySubTree: function (model) {
                var parameters = model.attributes.parameters;
                var payloadConfigStart = parameters[0].value + ".setPayload(messageRef = " + parameters[1].value + ", payload = " + parameters[2].value;
                return new TreeNode("PayloadProcessor", "PayloadProcessor", payloadConfigStart, ");");
            }
        }
    };

   return PayloadProcessor;
});
