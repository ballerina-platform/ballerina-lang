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
    var switchMediator = {
        id: "SwitchMediator",
        title: "Switch",
        icon: "images/tool-icons/switch.svg",
        colour : "#334455",
        type : "DynamicContainableProcessor",
        dragCursorOffset : { left: 40, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/switch_drag.svg").mimeType("image/svg+xml").get(function(error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "80px").attr("height", "65px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }
            return cloneCallBack;
        },
        utils: {
            parameters: [
                {
                    key: "description",
                    value: "Description"
                }
            ],
            getSchema: function () {
                return {
                    title: "Switch Mediator",
                    type: "object",
                    properties: {
                        Description: {"type": "string"}
                    }
                };
            },
            getEditableProperties: function (parameters) {
                var editableProperties = {};
                editableProperties.Description = parameters[0];
                return editableProperties;
            },
            getMySubTree: function (model) {
                return new TreeNode("SwitchMediator", "SwitchMediator");
            }
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    flowControllers.SwitchMediator = switchMediator;

    processors.flowControllers = flowControllers;

    return processors;

}(Processors || {}));