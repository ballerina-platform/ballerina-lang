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
    var SwitchMediator = {
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
        parameters: [
            {
                key: "description",
                value: "Description"
            }
        ],
        propertyPaneSchema: [
            {
                key: "description",
                text: "Description"
            }
        ],
        utils: {
            getMyPropertyPaneSchema : function () {
                return Processors.flowControllers.SwitchMediator.propertyPaneSchema;
            },
            getMyParameters: function (model) {
                return model.attributes.parameters;
            },
            saveMyProperties: function (model, inputs) {
                model.attributes.parameters = [
                    {
                        key: "description",
                        value: inputs.description.value
                    }
                ];
            },
            getMySubTree: function (model) {
                return new TreeNode("SwitchMediator", "SwitchMediator");
            }
        }
    };

    return SwitchMediator;

});