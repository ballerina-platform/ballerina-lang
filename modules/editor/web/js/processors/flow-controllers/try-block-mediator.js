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
    var TryBlockMediator = {
        id: "TryBlockMediator",
        title: "Try Block",
        icon: "images/tool-icons/dgm-try-catch.svg",
        colour : "#ffffff",
        type : "ComplexProcessor",
        containableElements: [{container:"tryContainer",children:[{title:"Try"}]},{container:"catchContainer",children:[{title:"Catch"}]}],
        dragCursorOffset : { left: 50, top: -5 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/dgm-try-catch.svg").mimeType("image/svg+xml").get(function(error, xml) {
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
                key: "exception",
                value: "Exception"
            },
            {
                key: "description",
                value: "Description"
            }
        ],
        propertyPaneSchema: [
            {
                key: "exception",
                text: "Exception"
            },
            {
                key: "description",
                text: "Description"
            }
        ],
        utils: {
            getMyPropertyPaneSchema : function () {
                return Processors.flowControllers.TryBlockMediator.propertyPaneSchema;
            },
            getMyParameters: function (model) {
                return model.attributes.parameters;
            },
            saveMyProperties: function (model, inputs) {
                model.attributes.parameters = [
                    {
                        key: "exception",
                        value: inputs.exception.value
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
                var tryBlockNode = new TreeNode("TryBlock", "TryBlock", "try{", "}");
                for (var itr = 0; itr < tryBlock.get('children').models.length; itr++) {
                    var child = tryBlock.get('children').models[itr];
                    tryBlockNode.getChildren().push(child.get('utils').getMySubTree(child));
                }

                // Generate the Subtree for the catch block
                var catchBlock = model.get('containableProcessorElements').models[1];
                var catchBlockNode = new TreeNode("CatchBlock", "CatchBlock", "catch(Exception e){", "}");
                for (var itr = 0; itr < catchBlock.get('children').models.length; itr++) {
                    var child = catchBlock.get('children').models[itr];
                    catchBlockNode.getChildren().push(child.get('utils').getMySubTree(child));
                }
                var tryCatchNode = new TreeNode("TryCatchMediator", "TryCatchMediator", "", "");
                tryCatchNode.getChildren().push(tryBlockNode);
                tryCatchNode.getChildren().push(catchBlockNode);

                return tryCatchNode;
            }
        }
    };

   return TryBlockMediator;
});
