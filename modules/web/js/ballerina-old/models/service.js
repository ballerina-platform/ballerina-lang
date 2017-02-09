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

define(['require','log', 'jquery', 'd3', 'backbone', 'lodash', 'diagram_core', 'ast/node', './life-line', './message-point'],

    function (require, log, $, d3, Backbone, _, DiagramCore, TreeNode, LifeLine, MessagePoint) {

    var Service = Backbone.Model.extend(
        /** @lends Service.prototype */
        {
            /**
             * @augments Backbone.Model
             * @constructs
             * @class Service represents the model for a ballerina Service.
             */
            initialize: function (attrs, options) {
                var DiagramElements = DiagramCore.Models.DiagramElements;
                var elements = new DiagramElements([], {diagram: this});
                var resources = new DiagramElements([], {diagram: this});
                var sources = new DiagramElements([], {diagram: this});
                var endPoints = new DiagramElements([], {diagram: this});
                var workers = new DiagramElements([], {diagram: this});
                this.diagramElements(elements);
                this.diagramResourceElements(resources);
                this.diagramSourceElements(sources);
                this.diagramEndpointElements(endPoints);
                this.diagramWorkerElements(workers);

                this.selectedNode = null;

                this.destinationLifeLine = null;
                this.deepestPointY = 100;
                this.sourceLifeLineY = 0;
                this.X = 0;
                this.highestLifeline = null;

                var resourceCounter = 0;
                var sourceCounter = 0;
                var endpointCounter = 0;
                var workerCounter = 0;
                this.resourceLifeLineCounter(resourceCounter);
                this.sourceLifeLineCounter(sourceCounter);
                this.endpointLifeLineCounter(endpointCounter);
                this.workerLifeLineCounter(workerCounter);
                this.CurrentDiagram();
            },

            modelName: "Diagram",

            selectedNode: null,

            lifeLineMap: {},

            destinationLifeLine: null,
            //getter/setter for currentDiagram object of tab
            CurrentDiagram: function (diagram) {
                if (_.isUndefined(diagram)) {
                    return this.get('CurrentDiagram');
                } else {
                    this.set('CurrentDiagram', diagram);
                }
            },
            //set current view as diagram view
            setCurrentView: function (view1) {
                view1.currentDiagramView(view1);
            },
            // setter/getter of resource element count
            resourceLifeLineCounter: function (rCounter) {
                if (_.isUndefined(rCounter)) {
                    return this.get('resourceLifeLineCounter');
                } else {
                    this.set('resourceLifeLineCounter', rCounter);
                }

            },
            // setter/getter of endpoint element count
            endpointLifeLineCounter: function (eCounter) {
                if (_.isUndefined(eCounter)) {
                    return this.get('endpointLifeLineCounter');
                } else {
                    this.set('endpointLifeLineCounter', eCounter);
                }

            },

            // setter/getter of endpoint element count
            sourceLifeLineCounter: function (sCounter) {
                if (_.isUndefined(sCounter)) {
                    return this.get('sourceLifelineCounter');
                } else {
                    this.set('sourceLifelineCounter', sCounter);
                }

            },

            // setter/getter of worker element count
            workerLifeLineCounter: function (wCounter) {
                if (_.isUndefined(wCounter)) {
                    return this.get('workerLifeLineCounter');
                } else {
                    this.set('workerLifeLineCounter', wCounter);
                }

            },

            addElement: function (element, opts) {
                //this.trigger("addElement", element, opts);

                if (_.has(element, 'attributes.type')) {
                    var type = _.get(element, 'attributes.type');

                    switch (type) {
                        case "Resource":
                            this.diagramResourceElements().add(element, opts);
                            break;
                        case "Source":
                            this.diagramSourceElements().add(element, opts);
                            break;
                        case "Worker":
                            this.diagramWorkerElements().add(element, opts);
                            break;
                        case "EndPoint":
                            this.diagramEndpointElements().add(element, opts);
                            break;
                        default:

                    }
                    this.lifeLineMap[element.attributes.centerPoint.attributes.x] = element;
                } else{
                    this.trigger("addElement", element, opts);
                }
            },

            getElement: function (id) {
                return this.diagramElements().get(id);
            },

            diagramElements: function (diaElements) {
                if (_.isUndefined(diaElements)) {
                    return this.get('diagramElements');
                } else {
                    this.set('diagramElements', diaElements);
                }
            },


            diagramResourceElements: function (diaElements) {
                if (_.isUndefined(diaElements)) {
                    return this.get('diagramResourceElements');
                } else {
                    this.set('diagramResourceElements', diaElements);
                }
            },

            diagramSourceElements: function (diaElements) {
                if (_.isUndefined(diaElements)) {
                    return this.get('diagramSourceElements');
                } else {
                    this.set('diagramSourceElements', diaElements);
                }
            },

            diagramWorkerElements: function (diaElements) {
                if (_.isUndefined(diaElements)) {
                    return this.get('diagramWorkerElements');
                } else {
                    this.set('diagramWorkerElements', diaElements);
                }
            },

            diagramEndpointElements: function (diaElements) {
                if (_.isUndefined(diaElements)) {
                    return this.get('diagramEndpointElements');
                } else {
                    this.set('diagramEndpointElements', diaElements);
                }
            },

            onLifelineClicked: function (x, y) {
                this.trigger("llClicked", x, y);
            },


            clickedLifeLine: undefined,

            positionTemp: undefined,

            dynamicMessage: undefined,

            test: undefined,

            createPoint: function (x, y) {
                return new DiagramCore.Models.Point({'x': x, 'y': y});
            },

            createLifeLine: function (title, center, colour, type, definition) {
                return new LifeLine({title: title, centerPoint: center, colour: colour,
                    type: type, definition: definition});
            },

            getNearestLifeLine: function (xPosition) {
                var distanceDiff = undefined;
                var nearestKey = undefined;
                for (var key in this.lifeLineMap) {
                    if (nearestKey === undefined) {
                        distanceDiff = Math.abs(xPosition - key);
                        nearestKey = key;
                    } else {
                        if (distanceDiff > Math.abs(xPosition - key)) {
                            distanceDiff = Math.abs(xPosition - key);
                            nearestKey = key;
                        }
                    }
                }

                return this.lifeLineMap[nearestKey];
            },

            parseTree: function() {

                var TreeRoot;

                var buildTree = function (resourceModel) {
                    // Until the message variabe concept introduce to the tooling we will be creating a message called response on behalf of the user
                    var rootNode = new TreeNode("Resource", "Resource", "resource passthrough (message m) {\nmessage response;", "}");
                    for (var itr = 0; itr < (resourceModel.get('children').models).length; itr++) {
                        var mediator = (resourceModel.get('children').models)[itr];

                        // Check whether the mediator is a message point from the resource to the source.
                        // If so handle it differently
                        if (mediator instanceof MessagePoint) {
                            // Check the message point is from resource to the source
                            if (mediator.get('message').get('destination').get('parent').get('title') === "Source") {
                                var node = new TreeNode("ResponseMsg", "ResponseMsg", "reply response", ";");
                                rootNode.getChildren().push(node);
                            }else if(mediator.get('message').get('destination').get('parent').get('cssClass') === "endpoint"){
                                //This section will handle "invoke" mediator transformation.
                                var endpoint = mediator.get('message').get('destination').get('parent').attributes.parameters[0].value;
                                var uri = mediator.get('message').get('destination').get('parent').attributes.parameters[1].value;
                                // When we define the properties, need to extract the endpoint from the property
                                definedConstants["HTTPEP"] = {name: endpoint, value: uri};

                                var invokeNode = new TreeNode("InvokeMediator", "InvokeMediator", ("response = invoke(endpointKey=" + endpoint + ", messageKey=m)"), ";");
                                rootNode.getChildren().push(invokeNode);
                            }
                        } else {
                            rootNode.getChildren().push((mediator.get('utils')).getMySubTree(mediator));
                        }
                    }
                    log.debug(rootNode);
                    return rootNode;
                };

                var finalSource = "";

                var includeConstants = function (resourceModel) {
                    // TODO: Need to handle this properly
                    // Defining the global constants
                    // for (var key in definedConstants) {
                    //     if (_.isEqual(key, "HTTPEP")) {
                    //         finalSource += "constant endpoint " + definedConstants[key].name + " = new HTTPEndPoint(\"" +
                    //             definedConstants[key].value + "\");\n";
                    //     }
                    // }

                    // For the moment we are injecting the API methods directly hardcoded here at the moment.
                    // After the properties view implementation those can be dynamically changed
                    finalSource += "\n" +
                        ((resourceModel.attributes.parameters[2].value==true) ? '@GET\n' : '') +
                        ((resourceModel.attributes.parameters[3].value==true) ? '@PUT\n' : '') +
                        ((resourceModel.attributes.parameters[4].value==true) ? '@POST\n' : '') +
                        '@Path ("' + resourceModel.attributes.parameters[1].value +'")\n'
                };

                var traverse = function (tree, finalSource) {

                    // Define the Resource methods and the context path (@GET, @POST, etc and @Path("/resourcePath")")

                    // Define the mediation logic
                    finalSource = finalSource + "" + tree.configStart;
                    var arr = tree.getChildren();
                    for (var a = 0; a < arr.length; a++) {
                        var node = arr[a];
                        finalSource = traverse(node, finalSource);
                    }
                    finalSource = finalSource + tree.configEnd;

                    return finalSource;
                };
                TreeRoot = buildTree(this.get('diagramResourceElements').models[0]);
                includeConstants(this.get('diagramResourceElements').models[0]);
                return traverse((TreeRoot), finalSource);
            },

            reloadDiagramArea: function () {
                this.resourceLifeLineCounter(0);
                this.endpointLifeLineCounter(0);
                this.workerLifeLineCounter(0);
                this.attributes.diagramResourceElements.models = [];
                this.attributes.diagramResourceElements.length = 0;
                this.attributes.diagramEndpointElements.models = [];
                this.attributes.diagramEndpointElements.length = 0;
            },

            defaults: {
            }

        });

    return Service;

});
