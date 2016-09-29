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
        title: "Log Mediator",
        icon: "images/LogMediator.gif",
        colour : "#2e2eb8",
        type : "UnitProcessor",
        dragCursorOffset : { left: 45, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var svgRoot = view.createSVGForDraggable();
                var group = svgRoot.draw.group(svgRoot).attr("class", "log-mediator-tool");
                var rect = svgRoot.draw.basicRect(0, 0, 90, 30, 0, 0, group);
                var text = svgRoot.draw.centeredText(new GeoCore.Models.Point({'x': 45, 'y': 15}), "Log", group);
                return svgRoot.getDraggableRoot();
            }
            return cloneCallBack;
        },
        parameters: [
            {
                key: "level",
                label: "Log Level",
                required: true,
                value: {
                    type: "String",
                    values: [
                        {key: "full", label: "FULL"}, {key: "simple", label: "SIMPLE"},
                        {key: "headers", label: "HEADERS"}, {key: "custom", label: "CUSTOM"}
                    ]
                }

            },
            {
                key: "category",
                label: "Log Category",
                required: true,
                value: {
                    type: "String",
                    values: [
                        {key: "trace", label: "Trace"}, {key: "debug", label: "DEBUG"}, {key: "info", label: "INFO"}
                        , {key: "warn", label: "WARN"}, {key: "error", label: "ERROR"}, {key: "fatal", label: "FATAL"}
                    ]
                }
            },
            {
                key: "separator",
                label: "Log Separator",
                required: false,
                value: {
                    type: "String"
                }
            },
            {
                key: "description",
                label: "Log Description",
                required: false,
                value: {
                    type: "String"
                }
            },
            {
                key: "property",
                label: "Properties",
                required: false,
                value: {
                    type: "Array",
                    parameters: [
                        {
                            key: "name",
                            label: "Property Name",
                            required: true,
                            value: {
                                type: "String"
                            }
                        },
                        {
                            key: "value",
                            label: "Property Value",
                            required: false,
                            value: {
                                type: "String"
                            }
                        },
                        {
                            key: "expression",
                            label: "Property Expression",
                            required: false,
                            value: {
                                type: "String"
                            }
                        }
                    ]
                }
            }
        ],
        getMySubTree: function (model) {
            return new TreeNode("LogMediator", "LogMediator", "log(\"Test\"", ");");
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    manipulators.LogMediator = logMediator;

    processors.manipulators = manipulators;

    return processors;

}(Processors || {}));
