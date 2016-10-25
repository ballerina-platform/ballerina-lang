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
    var payloadFactoryMediator = {
        id: "PayLoadFactoryMediator",
        title: "Data Mapper",
        icon: "images/tool-icons/datamapper.svg",
        colour : "#27ae60",
        type : "UnitProcessor",
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
        parameters: [
            {
                key: "configurationFile",
                value: "Configuration file"
            },
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
                key: "configurationFile",
                text: "Configuration File"
            },
            {
                key: "message",
                text: "Message"
            },
            {
                key: "description",
                text: "Description"
            }
        ],
        saveMyProperties: function (model, inputs) {
            model.get("parameters").parameters = [
                {
                    key: "configurationFile",
                    value: inputs.configurationFile.value
                },
                {
                    key: "message",
                    value: inputs.message.value
                },
                {
                    key: "description",
                    value: inputs.description.value
                }
            ];
        },
        getMySubTree: function (model) {
            return new TreeNode("payloadFactoryMediator", "payloadFactoryMediator", "payloadFactory {", "}");
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    manipulators.PayLoadFactoryMediator = payloadFactoryMediator;

    processors.manipulators = manipulators;

    return processors;

}(Processors || {}));
