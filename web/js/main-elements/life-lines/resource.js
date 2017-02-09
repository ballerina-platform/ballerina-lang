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
define(['d3'], function (d3) {

    var ResourceLifeline = {
        id: "Resource",
        title: "Pipe Line",
        icon: "images/tool-icons/lifeline.svg",
        class : "resource",
        shape: 'rect',
        editable: true,
        deletable: true,
        dragCursorOffset : { left: 50, top: 50 },
        createCloneCallback : function(view){
            function cloneCallBack() {
                var div = view.createContainerForDraggable();
                d3.xml("images/tool-icons/lifeline.svg").mimeType("image/svg+xml").get(function(error, xml) {
                    if (error) throw error;
                    var svg = xml.getElementsByTagName("svg")[0];
                    d3.select(svg).attr("width", "100px").attr("height", "100px");
                    d3.select(svg).attr("width", "100px");
                    div.node().appendChild(svg);
                });
                return div.node();
            }
            return cloneCallBack;
        },
        parameters: [
            {
                key: "title",
                value: "Resource"
            },
            {
                key: "path",
                value: ""
            },
            {
                key: "get",
                value: false
            },
            {
                key: "put",
                value: false
            },
            {
                key: "post",
                value: false
            }
        ],
        propertyPaneSchema: [
            {
                key: "title",
                text: "Title"
            },
            {
                key: "path",
                text: "Path"
            },
            {
                key: "get",
                checkbox: "GET"
            },
            {
                key: "put",
                checkbox: "PUT"
            },
            {
                key: "post",
                checkbox: "POST"
            }
        ],
        textModel : "undefined",
        utils: {
            getMyPropertyPaneSchema : function () {
                return MainElements.lifelines.ResourceLifeline.propertyPaneSchema;
            },
            getMyParameters: function (model) {
                return model.attributes.parameters;
            },
            saveMyProperties: function (model, inputs) {
                model.attributes.title = inputs.title.value;
                model.attributes.parameters = [
                    {
                        key: "title",
                        value: inputs.title.value
                    },
                    {
                        key: "path",
                        value: inputs.path.value
                    },
                    {
                        key: "get",
                        value: inputs.get.checked
                    },
                    {
                        key: "put",
                        value: inputs.put.checked
                    },
                    {
                        key: "post",
                        value: inputs.post.checked
                    }
                ];
            },
            canConnectTo: function () {
                return ['Worker', 'Source', 'ContainableProcessorElement', 'EndPoint'];
            },
            createMyModel: function (model) {
                //add the resource model
                var resourceCenterPoint = createPoint(380, 50);
                var resourceType = "Resource";
                var resourceLifeLineDef = MainElements.lifelines.ResourceLifeline;

                var resourceLifeline = createLifeLine(resourceType, resourceCenterPoint, resourceLifeLineDef.class,
                    resourceLifeLineDef.utils,
                    resourceLifeLineDef.parameters, resourceLifeLineDef.textModel,
                    resourceType, resourceLifeLineDef);
                var resourceLifeLineOptions = {
                    class: MainElements.lifelines.ResourceLifeline.class,
                    diagram: model
                };
                model.addElement(resourceLifeline, resourceLifeLineOptions);

                //increment resource counter
                var resourceCount = model.resourceLifeLineCounter();
                resourceCount++;
                model.resourceLifeLineCounter(resourceCount);
                return resourceLifeline;
            },
            createMyStartProcessorModel : function (resourceLifeline) {
                var position = new GeoCore.Models.Point({x: 0, y: 0});
                var processor = new SequenceD.Models.ProcessorFactory(Processors.actions.startAction.title,
                    position,
                    Processors.actions.startAction.type,
                    {
                        type: Processors.actions.startAction.type,
                        initMethod: Processors.actions.startAction.init
                    },
                    {
                        colour: Processors.actions.startAction.colour
                    },
                    Processors.actions.startAction.parameters,
                    Processors.actions.startAction.utils,
                    Processors.actions.startAction.textModel,
                    Processors.actions.startAction.width,
                    Processors.actions.startAction.height);

                resourceLifeline.addChild(processor);
                return processor;
            }
        }
    };

    return ResourceLifeline;
});