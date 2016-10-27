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

    //Log mediator definition
    var logMediator = {
        id: "LogMediator",
        title: "Logger",
        icon: "images/tool-icons/log.svg",
        colour : "#ffffff",
        type : "UnitProcessor",
        dragCursorOffset : { left: 24, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/log_drag.svg").mimeType("image/svg+xml").get(function(error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "48px").attr("height", "108px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }
            return cloneCallBack;
        },
        utils : {
            propertyPaneSchema: [
                {
                    key: "messageRef",
                    text: "Message Reference"
                },
                {
                    key: "message",
                    text: "Log message"
                },
                {
                    key: "logLevel",
                    dropdown: "Log Level",
                    values: ["simple", "custom", "headers", "full"]
                },
                {
                    key: "logCategory",
                    dropdown: "Log Category",
                    values: ["info", "error", "warn", "fatal", "debug", "trace"]
                },
                {
                    key: "description",
                    text: "Description"
                }
            ],
            parameters: [
                {
                    key: "messageRef",
                    value: "m"
                },
                {
                    key: "message",
                    value: "Log message"
                },
                {
                    key: "logLevel",
                    value: "simple"
                },
                {
                    key: "logCategory",
                    value: "info"
                },
                {
                    key: "description",
                    value: "Description"
                }
            ],
            saveMyProperties: function (model, inputs) {
                var selectedLogLevel;
                var selectedLogCategory;
                if (inputs.logLevel.value !== "") {
                    selectedLogLevel = inputs.logLevel.value;
                } else {
                    selectedLogLevel = model.get("parameters").parameters[1].value;
                }
                if (inputs.logCategory.value !== "") {
                    selectedLogCategory = inputs.logCategory.value;
                } else {
                    selectedLogCategory = model.get("parameters").parameters[2].value;
                }
                model.get("utils").utils.parameters = [
                    {
                        key: "messageRef",
                        value: inputs.messageRef.value
                    },
                    {
                        key: "message",
                        value: inputs.message.value
                    },
                    {
                        key: "logLevel",
                        value: selectedLogLevel
                    },
                    {
                        key: "logCategory",
                        value: selectedLogCategory
                    },
                    {
                        key: "description",
                        value: inputs.description.value
                    }
                ];
            },
            getMySubTree: function (model) {
                var parameters = model.get('utils').utils.parameters;
                var log_configStart = "log(level=\"" + parameters[1].value + "\"," + "status=\"" + parameters[0].value + "\"";
                return new TreeNode("LogMediator", "LogMediator", log_configStart, ");");
            },
            outputs: false,
            getInputParams: function () {
                var inputParams = [];
                inputParams[0] = this.parameters[0];
                inputParams[1] = this.parameters[1];

                return inputParams;
            }
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    manipulators.LogMediator = logMediator;

    processors.manipulators = manipulators;

    return processors;

}(Processors || {}));
