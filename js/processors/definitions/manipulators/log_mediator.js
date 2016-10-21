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
        colour : "#2980b9",
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
        parameters: [
            {
                key: "message",
                value: "Log message"
            },
            {
                key: "logLevel",
                value: "SIMPLE"
            },
            {
                key: "logCategory",
                value: "INFO"
            },
            {
                key: "description",
                value: "Description"
            }
        ],
        getSchema: function () {
            return {
                title: "Log Mediator",
                type: "object",
                properties: {
                    Message: {"type": "string"},
                    LogLevel: {
                        "type": "string",
                        "enum": [
                            "SIMPLE",
                            "CUSTOM",
                            "HEADERS",
                            "FULL"
                        ],
                        "default": "SIMPLE"
                    },
                    LogCategory: {
                        "type": "string",
                        "enum": [
                            "INFO",
                            "ERROR",
                            "WARN",
                            "FATAL",
                            "DEBUG",
                            "TRACE"
                        ],
                        "default": "INFO"
                    },
                    Description: {"type": "string"}
                }
            };
        },
        getEditableProperties: function (parameters) {
            var editableProperties = {};
            editableProperties.Message = parameters[0];
            editableProperties.LogLevel = parameters[1];
            editableProperties.LogCategory = parameters[2];
            editableProperties.Description = parameters[3];
            return editableProperties;
        },
        getMySubTree: function (model, parameters) {
            var log_configStart =  "log(level=\"" + parameters[1].value + "\"," + "status=\"" + parameters[0].value + "\"";
            return new TreeNode("LogMediator", "LogMediator", log_configStart, ");");
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    manipulators.LogMediator = logMediator;

    processors.manipulators = manipulators;

    return processors;

}(Processors || {}));
