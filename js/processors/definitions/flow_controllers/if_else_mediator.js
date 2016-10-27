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
    var ifElseMediator = {
        id: "IfElseMediator",
        title: "If Else",
        icon: "images/tool-icons/tryblock.svg",
        colour : "#ffffff",
        type : "ComplexProcessor",
        containableElements: [{container:"ifContainer",children:[{title:"If"}]},{container:"elseContainer",children:[{title:"Else"}]}],
        dragCursorOffset : { left: 50, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/tryblock_drag.svg").mimeType("image/svg+xml").get(function(error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "100px").attr("height", "85px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }
            return cloneCallBack;
        },
        parameters: [
            {
                key: "condition",
                value: ""
            },
            {
                key: "description",
                value: "Description"
            }
        ],
        propertyPaneSchema: [
            {
                key: "condition",
                text: "Condition"
            },
            {
                key: "description",
                text: "Description"
            }
        ],
        saveMyProperties: function (model, inputs) {
            model.get("parameters").parameters = [
                {
                    key: "condition",
                    value: inputs.condition.value
                },
                {
                    key: "description",
                    value: inputs.description.value
                }
            ];
        },
        getMySubTree: function (model) {
            // Generate Subtree for the try block
            var tryBlock = model.get('containableProcessorElements').models[0];
            var parameters = model.get('parameters').parameters;
            var ifConfigStart = "if ( " + parameters[0].value + ") {";
            var tryBlockNode = new TreeNode("IfBlock", "IfBlock", ifConfigStart, "}");
            for (var itr = 0; itr < tryBlock.get('children').models.length; itr++) {
                var child = tryBlock.get('children').models[itr];
                if (child instanceof SequenceD.Models.MessagePoint && child.get('direction') == 'outbound') {
                    var endpoint = child.get('message').get('destination').get('parent').get('parameters')[0].value;
                    var uri = child.get('message').get('destination').get('parent').get('parameters')[1].value;
                    // When we define the properties, need to extract the endpoint from the property
                    definedConstants["HTTPEP"] = {name: endpoint, value: uri};
                    var l = new TreeNode("InvokeMediator", "InvokeMediator", ("response = invoke(endpointKey=" +
                    endpoint + ", messageKey=m)"), ";");
                    tryBlockNode.getChildren().push(l);
                } else {
                    tryBlockNode.getChildren().push(child.get('getMySubTree').getMySubTree(child));
                }
            }

            // Generate the Subtree for the catch block
            var catchBlock = model.get('containableProcessorElements').models[1];
            var catchBlockNode = new TreeNode("ElseBlock", "ElseBlock", "else{", "}");
            for (var itr = 0; itr < catchBlock.get('children').models.length; itr++) {
                var child = catchBlock.get('children').models[itr];
                if (child instanceof SequenceD.Models.MessagePoint && child.get('direction') == 'outbound') {
                    var endpoint = child.get('message').get('destination').get('parent').get('parameters')[0].value;
                    var uri = child.get('message').get('destination').get('parent').get('parameters')[1].value;
                    // When we define the properties, need to extract the endpoint from the property
                    definedConstants["HTTPEP"] = {name: endpoint, value: uri};
                    var l = new TreeNode("InvokeMediator", "InvokeMediator", ("response = invoke(endpointKey=" +
                    endpoint + ", messageKey=m)"), ";");
                    catchBlockNode.getChildren().push(l);
                } else {
                    catchBlockNode.getChildren().push(child.get('getMySubTree').getMySubTree(child));
                }
            }
            var tryCatchNode = new TreeNode("IfElseMediator", "IfElseMediator", "", "");
            tryCatchNode.getChildren().push(tryBlockNode);
            tryCatchNode.getChildren().push(catchBlockNode);

            return tryCatchNode;

        }
    };

    // Add defined mediators to manipulators
    // Mediator id should be exactly match to name defining here.(Eg : "LogMediator")
    flowControllers.IfElseMediator = ifElseMediator;

    processors.flowControllers = flowControllers;

    return processors;

}(Processors || {}));
