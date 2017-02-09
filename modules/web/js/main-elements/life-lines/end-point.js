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

    //End point definition
    var EndPointLifeline = {
        id: "EndPoint",
        title: "End Point",
        icon: "images/tool-icons/dgm-lifeline.svg",
        class : "endpoint",
        shape: 'rect',
        editable : true,
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
        propertyPaneSchema: [
            {
                key: "title",
                text: "Title"
            },
            {
                key: "url",
                text: "URL"
            }
        ],
        parameters: [
            {
                key: "title",
                value: "End Point"
            },
            {
                key: "url",
                value: "https://"
            }
        ],
        textModel : "undefined",
        utils: {
            getMyPropertyPaneSchema : function () {
                return MainElements.lifelines.EndPointLifeline.propertyPaneSchema;
            },
            getMyParameters: function (model) {
                return [
                    {
                        key: "title",
                        value: model.attributes.title
                    },
                    {
                        key: "url",
                        value: model.attributes.parameters[1].value
                    }
                ];
            },
            saveMyProperties: function (model, inputs) {
                model.attributes.title = inputs.title.value;
                model.attributes.parameters = [
                    {
                        key: "title",
                        value: inputs.title.value
                    },
                    {
                        key: "url",
                        value: inputs.url.value
                    }
                ];
            },
            canConnectTo: function () {
                return ['Worker', 'Resource', 'ContainableProcessorElement'];
            },
            createMyModel: function (model, title, centerPoint, parameters) {
                var lifeline = createLifeLine(title, centerPoint, MainElements.lifelines.EndPointLifeline.class,
                    MainElements.lifelines.EndPointLifeline.utils,
                    parameters, MainElements.lifelines.EndPointLifeline.textModel, "Endpoint",
                    MainElements.lifelines.EndPointLifeline);
                var endpointLifeLineOptions = {
                    class: MainElements.lifelines.EndPointLifeline.class,
                    diagram: model
                };
                model.addElement(lifeline, endpointLifeLineOptions);
                var endpointCount = model.endpointLifeLineCounter();
                endpointCount++;
                model.endpointLifeLineCounter(endpointCount);
            },
            textModel : "undefined"
        }
    };

    return EndPointLifeline;

});
