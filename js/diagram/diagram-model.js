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

var Diagrams = (function (diagrams) {
    var models = diagrams.models || {};

    var DiagramElement = Backbone.Model.extend(
        /** @lends DiagramElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Element represents the model for elements in a diagram.
             */
            initialize: function (attrs, options) {
            },

            parent: function (parent) {
                if (_.isUndefined(parent)) {
                    return this.get("parent");
                } else {
                    this.set("parent", parent);
                }
            },

            modelName: "DiagramElement",

            nameSpace: diagrams,

            idAttribute: this.cid,

            defaults: {}
        });

    var Shape = DiagramElement.extend(
        /** @lends Link.prototype */
        {
            /**
             * @augments DiagramElement
             * @constructs
             * @class Link represents the model for a link between two elements in a diagrams.
             */
            initialize: function (attrs, options) {
                DiagramElement.prototype.initialize.call(this, attrs, options);
                var connectionPoints = new Backbone.Collection([], {model: ConnectionPoint, owner: this});
                this.connectionPoints = connectionPoints;
            },

            modelName: "Shape",

            nameSpace: diagrams,

            addConnectionPoint: function (cntPoints) {
                if (_.isArray(cntPoints)) {
                    cntPoints.forEach(function (cntPoint) {
                        this.addConnectionPoint(cntPoint);
                    });
                }
                this.connectionPoints.add(cntPoints);
                this.trigger("connectionPointAdded", cntPoints);
            },

            defaults: {}
        });

    var Link = DiagramElement.extend(
        /** @lends Link.prototype */
        {
            /**
             * @augments DiagramElement
             * @constructs
             * @class Link represents the model for a link between two elements in a diagrams.
             */
            initialize: function (attrs, options) {
                DiagramElement.prototype.initialize.call(this, attrs, options);
                this.source(attrs['source']['activation'], attrs['source']['x'], attrs['source']['y']);
                this.destination(attrs['destination']['activation'], attrs['destination']['x'], attrs['destination']['y']);
            },

            modelName: "Link",

            nameSpace: diagrams,

            defaults: {},

            /**
             * Gets or sets source connectionPoint for the link.
             * @param {ConnectionPoint} [connectionPoint] Source connectionPoint
             */
            source: function (connectionPoint, x, y) {
                if (connectionPoint === undefined) {
                    return this.get('source');
                }
                defaultView.model.sourceLifeLineY = y;
                var connection = connectionPoint.connectLink(this, {type: 'outgoing', x: x, y: y});
                if (this.makeParallel()) {
                    connection.point().y(this.destination().point().y());
                }
                this.set('source', connection);
            },
            /**
             * Gets or sets destination connectionPoint for the link.
             * @param {ConnectionPoint} [connectionPoint] Destination connectionPoint
             */
            destination: function (connectionPoint, x, y) {
                if (connectionPoint === undefined) {
                    return this.get('destination');
                }
                var connection = connectionPoint.connectLink(this, {type: 'incoming', x: x, y: y});
                if (this.makeParallel()) {
                    connection.point().y(this.source().point().y());
                }
                this.set('destination', connection);
            },
            makeParallel: function () {
                return true;
            }
        });

    var Connection = DiagramElement.extend(
        /** @lends Connection.prototype */
        {
            /**
             * @augments DiagramElement
             * @constructs
             * @class Connection represents a connection made to a connection point owned by an element in a diagrams.
             */
            initialize: function (attrs, options) {
                DiagramElement.prototype.initialize.call(this, attrs, options);
                this.connectionPoint().on("elementMoved", this.onConnectionPointMoved, this);
            },

            modelName: "Connection",

            nameSpace: diagrams,

            onConnectionPointMoved: function (event) {
                this.point().move(event.dx, event.dy);
                this.trigger("connectingPointChanged", this.point());
            },

            /**
             * Get or set connection type of the connection.
             * @param {String} [type] incoming or outgoing
             * @returns {String, void}
             */
            type: function (type) {
                if (_.isUndefined(type)) {
                    return this.get('type');
                }
                this.set('type', type);
            },

            /**
             * Gets or sets the point on paper in which this connection point
             * belongs
             * @param {Point} [point]
             * @returns {Point|void}
             */
            point: function (point) {
                if (_.isUndefined(point)) {
                    return this.get('point');
                }
                this.set('point', point)
            },

            connectionPoint: function () {
                return this.get('connectionPoint');
            },

            /**
             * Checks whether this connection is an incoming connection
             * @returns {boolean}
             */
            isIncomingConnection: function () {
                if (_.isEqual(this.type(), 'incoming')) {
                    return true;
                }
                return false;
            }

        });

    var ConnectionPoint = DiagramElement.extend(
        /** @lends ConnectionPoint.prototype */
        {
            /**
             * @augments DiagramElement
             * @constructs
             * @class ConnectionPoint represents a connection point owned by an element in a diagrams.
             */
            initialize: function (attrs, options) {
                DiagramElement.prototype.initialize.call(this, attrs, options);
                this.connections = new Backbone.Collection([], {model: Connection, connectionPoint: this});
                this.owner().on("elementMoved", this.onOwnerMoved, this);
            },

            modelName: "ConnectionPoint",

            nameSpace: diagrams,

            connectLink: function (link, options) {
                var connection = new Connection({type: options.type, link: link, connectionPoint: this});
                this.connections.add(connection);
                this.trigger("connectionMade", connection, options.x, options.y);
                return connection;
            },

            onOwnerMoved: function (event) {
                this.trigger("elementMoved", event);
            },

            /**
             * Sets or gets the instance of shape which owns this connection point
             * @param {Shape} [shape]
             * @returns {Shape|void}
             */
            owner: function (shape) {
                if (_.isUndefined(shape)) {
                    return this.get('owner');
                }
                this.set('owner', shape);
            }

        });

    var DiagramElements = Backbone.Collection.extend(
        /** @lends DiagramElements.prototype */
        {
            /**
             * @augments Backbone.Collection
             * @constructs
             * @class DiagramElements represents the collection for elements in a diagram.
             */
            initialize: function (models, options) {
            },

            modelName: "DiagramElements",

            nameSpace: diagrams,

            model: DiagramElement

        });

// Model for Tab based resources
    var Tab = Backbone.Model.extend({
        modelName: "Tab",
        nameSpace: diagrams,


        initialize: function (attrs, options) {
            //store diagram object of a given tab
            var diagramSet = new DiagramsList([]);
            this.diagramForTab(diagramSet);
            this.viewObj = {};

        },
        // mark already visited tabs
        setSelectedTab: function () {
            this.attributes.createdTab = true;
        },
        getSelectedTab: function () {
            return this.createdTab;
        },
        setDiagramViewForTab: function (view) {
            this.viewObj = view;
        },

        // diagram collection getters/setters
        diagramForTab: function (element) {
            if (_.isUndefined(element)) {
                return this.get('diagramForTab');
            } else {
                this.set('diagramForTab', element);
            }
        },
        // on new tab creation add the diagram object to list
        addDiagramForTab: function (element) {

            this.diagramForTab().add(element);
        },
        //get the diagram object from the collection
        getDiagramOfTab: function (id) {
            return this.diagramForTab().get(id);
        },

        preview: function(preview){
            if(_.isUndefined(preview)){
                return this.get("preview");
            }
            this.set("preview", preview);
        },

        defaults: {
            resourceId: "id-not-set",
            resourceTitle: "",
            hrefId: "id-not-set",
            createdTab: false,
        }
    });
    var Diagram = Backbone.Model.extend(
        /** @lends Diagram.prototype */
        {
            /**
             * @augments Backbone.Model
             * @constructs
             * @class Diagram represents the model for aa diagrams.
             */
            initialize: function (attrs, options) {

                var elements = new DiagramElements([], {diagram: this});
                var resources = new DiagramElements([], {diagram: this});
                var sources = new DiagramElements([], {diagram: this});
                var endPoints = new DiagramElements([], {diagram: this});
                this.diagramElements(elements);
                this.diagramResourceElements(resources);
                this.diagramSourceElements(sources);
                this.diagramEndpointElements(endPoints);
                this.selectedNode = null;
                this.destinationLifeLine = null;
                // TODO: won't be using this until the layout finalized
                this.deepestPointY = 100;
                this.sourceLifeLineY = 0;
                this.X = 0;
                this.highestLifeline = null;
                var resourceCounter = 0;
                var sourceCounter = 0;
                var endpointCounter = 0;
                this.resourceLifeLineCounter(resourceCounter);
                this.sourceLifeLineCounter(sourceCounter);
                this.endpointLifeLineCounter(endpointCounter);
                this.CurrentDiagram();
            },

            modelName: "Diagram",

            selectedNode: null,

            nameSpace: diagrams,

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
            //create a diagram view  for new tab
            createDiagramView: function (diagram, opts) {
                var view = new Diagrams.Views.DiagramView({model: diagram, options: opts});
                view.render();
                return view;

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

            addElement: function (element, opts) {
                //this.trigger("addElement", element, opts);

                if (element instanceof SequenceD.Models.LifeLine) {
                    if(element.attributes.title.startsWith("Resource")) {
                        this.diagramResourceElements().add(element, opts);
                    } else if (element.attributes.title.startsWith("Source")) {
                        this.diagramSourceElements().add(element, opts);
                    } else {
                        this.diagramEndpointElements().add(element, opts);
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
                return new GeoCore.Models.Point({'x': x, 'y': y});
            },

            createLifeLine: function (title, center, colour) {
                return new SequenceD.Models.LifeLine({title: title, centerPoint: center, colour: colour});
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
                        if (mediator instanceof SequenceD.Models.MessagePoint) {
                            // Check the message point is from resource to the source
                            if (mediator.get('message').get('destination').get('parent').get('title') === "Source") {
                                var node = new TreeNode("ResponseMsg", "ResponseMsg", "reply response", ";");
                                rootNode.getChildren().push(node);
                            }
                        } else {
                            rootNode.getChildren().push((mediator.get('getMySubTree')).getMySubTree(mediator));
                        }
                    }
                    console.log(rootNode);
                    return rootNode;
                };

                var finalSource = "";

                var includeConstants = function (resourceModel) {
                    // TODO: Need to handle this properly
                    // Defining the global constants
                    for (var key in definedConstants) {
                        if (_.isEqual(key, "HTTPEP")) {
                            finalSource += "constant endpoint " + definedConstants[key].name + " = new HTTPEndPoint(\"" +
                                definedConstants[key].value + "\");\n";
                        }
                    }

                    // For the moment we are injecting the API methods directly hardcoded here at the moment.
                    // After the properties view implementation those can be dynamically changed
                    finalSource += "\n" +
                        ((resourceModel.get('parameters')[2].value==true) ? '@GET\n' : '') +
                        ((resourceModel.get('parameters')[3].value==true) ? '@PUT\n' : '') +
                        ((resourceModel.get('parameters')[4].value==true) ? '@POST\n' : '') +
                        '@Path ("' + resourceModel.get('parameters')[1].value +'")\n'
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
                TreeRoot = buildTree(defaultView.model.get('diagramResourceElements').models[0]);
                includeConstants(defaultView.model.get('diagramResourceElements').models[0]);
                return traverse((TreeRoot), finalSource);
            },

            reloadDiagramArea: function () {
                defaultView.model.resourceLifeLineCounter(0);
                defaultView.model.endpointLifeLineCounter(0);
                if (diagramD3el) {
                    diagramD3el.remove();
                    for (var element in diagramViewElements) {
                        diagramViewElements[element].remove();
                    }
                }
                defaultView.model.attributes.diagramResourceElements.models = [];
                defaultView.model.attributes.diagramResourceElements.length = 0;
                defaultView.model.attributes.diagramEndpointElements.models = [];
                defaultView.model.attributes.diagramEndpointElements.length = 0;
            },

            defaults: {
                path: '',
                get: false,
                put: false,
                post: false
            }

        });
    var DiagramsList = Backbone.Collection.extend(
        /** @lends DiagramElements.prototype */
        {
            /**
             * @augments Backbone.Collection
             * @constructs
             * @class DiagramElements represents the collection of diagrams
             */
            initialize: function (models, options) {
            },

            modelName: "DiagramsList",

            nameSpace: diagrams,

            model: Diagram

        });
    var EventManager = Backbone.Model.extend(
        /** @lends Eventmanager.prototype */
        {
            idAttribute: this.cid,
            modelName: "EventManager",
            /**
             * @augments Backbone.Model
             * @constructs
             * @class Handles validations of the diagram
             */
            initialize: function (attrs, options) {
                this.draggedElement();
                this.isActivated();
                this.currentType();
                this.invalid = false;
            },
            //keep the current dragged element type
            currentType: function (type) {
                if (_.isUndefined(type)) {
                    return this.get('currentType');
                } else {
                    this.set('currentType', type);
                }
            },
            draggedElement: function (element) {
                if (_.isUndefined(element)) {
                    return this.get('draggedElement');
                } else {
                    this.set('draggedElement', element);
                }
            },
            // check the current activated element's valid drops
            isActivated: function (activatedType) {
                if (activatedType != null) {
                    if (this.currentType() != "Resource" && this.currentType() != "EndPoint" && this.currentType() != "Source") {
                        // validation for active endpoints
                        if (activatedType.includes("EndPoint") || activatedType.includes("Source")) {
                            this.invalid = true;
                        }
                        else {
                            this.invalid = false;
                        }
                    }
                }
            }

        });
    models.EventManager = EventManager;
    models.DiagramElement = DiagramElement;
    models.DiagramElements = DiagramElements;
    models.Diagram = Diagram;
    models.Shape = Shape;
    models.Link = Link;
    models.Connection = Connection;
    models.ConnectionPoint = ConnectionPoint;
    models.Tab = Tab;
    models.DiagramsList = DiagramsList;
    diagrams.Models = models;


    return diagrams;
}(Diagrams || {}));
