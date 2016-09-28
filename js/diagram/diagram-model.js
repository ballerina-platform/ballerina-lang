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
                this.destination(attrs['destination']['activation'], attrs['destination']['x'],
                                 attrs['destination']['y']);
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
                diagram.sourceLifeLineY = y;
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
                this.diagramElements(elements);
                this.selectedNode = null;
                this.destinationLifeLine = null;
                // TODO: won't be using this until the layout finalized
                this.deepestPointY = 100;
                this.sourceLifeLineY = 0;
                this.X = 0;
            },

            modelName: "Diagram",

            selectedNode: null,

            nameSpace: diagrams,

            lifeLineMap: {},

            destinationLifeLine: null,

            addElement: function (element, opts) {
                //this.trigger("addElement", element, opts);

                if (element instanceof SequenceD.Models.LifeLine) {
                    this.diagramElements().add(element, opts);
                    this.lifeLineMap[element.attributes.centerPoint.attributes.x] = element;
                } else {
                    this.trigger("addElement", element, opts);
                }
            },

            removeElement: function (element, opts) {
                var index = this.diagramElements().indexOf(element);
                //TODO need to implement this 
                //var elements = this.diagramElements();
                //delete elements[index];
                this.trigger("removeElement", element, opts);
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

            createLifeLine: function (title, center) {
                return new SequenceD.Models.LifeLine({title: title, centerPoint: center});
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

            getDefinitionSchema: function () {
                return {
                    "title": "Resource",
                    type: "object",
                    properties: {
                        Path: {"type": "string"},
                        Get: {"type": "boolean"},
                        Put: {"type": "boolean"},
                        Post: {"type": "boolean"}
                    }
                };
            },

            getDefinitionEditableProperties: function (point) {
                var editableProperties = {};
                editableProperties.Path = this.attributes.path;
                editableProperties.Get = this.attributes.get;
                editableProperties.Put = this.attributes.put;
                editableProperties.Post = this.attributes.post;
                return editableProperties;
            },

            defaults: {
                path: '',
                get: false,
                put: false,
                post: false
            }

        });

    models.DiagramElement = DiagramElement;
    models.DiagramElements = DiagramElements;
    models.Diagram = Diagram;
    models.Shape = Shape;
    models.Link = Link;
    models.Connection = Connection;
    models.ConnectionPoint = ConnectionPoint;
    diagrams.Models = models;

    return diagrams;
}(Diagrams || {}));
