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
        title: "Switch Mediator",
        icon: "images/SwitchMediator.gif",
        colour : "#334455",
        type : "DynamicContainableProcessor",
        dragCursorOffset : { left: 45, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var svgRoot = view.createSVGForDraggable();
                var group = svgRoot.draw.group(svgRoot).attr("class", "switch-mediator-tool");
                var rect = svgRoot.draw.basicRect(0, 0, 90, 30, 0, 0, group);
                var text = svgRoot.draw.centeredText(new GeoCore.Models.Point({'x': 45, 'y': 15}), "Switch", group);
                return svgRoot.getDraggableRoot();
            }
            return cloneCallBack;
        },
        parameters: [],
        getMySubTree: function (model) {
            return new TreeNode("SwitchMediator", "SwitchMediator");
        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    flowControllers.SwitchMediator = switchMediator;

    processors.flowControllers = flowControllers;

    return processors;

}(Processors || {}));