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

var SequenceD = (function (sequenced) {
    var models = sequenced.Models || {};

    var Processor = Diagrams.Models.Shape.extend(
        /** @lends Processor.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Processor represents the model for processors in diagram.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.Shape.prototype.initialize.call(this, attrs, options);
                this.type = attrs.type;
                this.model = attrs.model;
                this.viewAttributes = attrs.viewAttributes;
                this.parameters = attrs.parameters;

                var children = new Children([], {diagram: this});
                this.children(children);
            },

            modelName: "Processor",

            nameSpace: sequenced,

            idAttribute: this.cid,

            parameters: {},

            viewAttributes: {},

            children: function (children) {
                if (_.isUndefined(children)) {
                    return this.get('children');
                } else {
                    this.set('children', children);
                }
            },

            createProcessor: function (title, center, type, model, viewAttributes, parameters) {
                return new SequenceD.Models.Processor({
                    title: title,
                    centerPoint: center,
                    type: type,
                    model: model,
                    viewAttributes: viewAttributes,
                    parameters: parameters
                });
            },

            addChild: function (element, opts) {
                //this.children().add(element, opts);

                var position = this.calculateIndex(element, element.get('centerPoint').get('y'));
                var index = position.index;
                this.children().add(element, {at: index});

                //this.trigger("addChild", element, opts);
                //this.trigger("addChildProcessor", element, opts);
            },


            calculateIndex: function (element, y) {
                var previousChild;
                var count = 1;
                var position = {};
                this.children().each(function (child) {
                    if (!_.isEqual(element, child)) {
                        if (child.get('centerPoint').get('y') > y) {
                            previousChild = child;
                            return false;
                        }
                        count = count + 1;
                    }
                });
                if (_.isUndefined(previousChild)) {
                    if (this.children().size() == 0) {
                        position.index = 0;
                    } else {
                        position.index = this.children().indexOf(element);
                    }
                } else {
                    position.index = this.children().indexOf(previousChild);
                }
                return position;
            },

            setY: function (y) {
                this.get('centerPoint').set('y', y);
            },

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "Processor"
            }
        });

    var FixedSizedMediator = Diagrams.Models.Shape.extend(
        /** @lends DiagramElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Element represents the model for elements in a diagram.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.Shape.prototype.initialize.call(this, attrs, options);
            },

            modelName: "FixedSizedMediator",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "Mediator"
            }
        });


    var FixedSizedMediators = Backbone.Collection.extend(
        /** @lends DiagramElements.prototype */
        {
            /**
             * @augments Backbone.Collection
             * @constructs
             * @class DiagramElements represents the collection for elements in a diagram.
             */
            initialize: function (models, options) {
            },

            modelName: "FixedSizedMediators",

            nameSpace: sequenced,

            model: FixedSizedMediator

        });

    var Child = Diagrams.Models.DiagramElement.extend(
        /** @lends DiagramElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Element represents the model for elements in a diagram.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.DiagramElement.prototype.initialize.call(this, attrs, options);
            },

            modelName: "Child",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "Child"
            }
        });


    var Children = Backbone.Collection.extend(
        /** @lends DiagramElements.prototype */
        {
            /**
             * @augments Backbone.Collection
             * @constructs
             * @class DiagramElements represents the collection for elements in a diagram.
             */
            initialize: function (models, options) {
            },

            modelName: "Children",

            nameSpace: sequenced,

            model: Child

        });


    var LifeLine = Diagrams.Models.Shape.extend(
        /** @lends LifeLine.prototype */
        {
            /**
             * @augments Element
             * @constructs
             * @class LifeLine Represents the model for a LifeLine in Sequence Diagrams.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.Shape.prototype.initialize.call(this, attrs, options);

                var elements = new FixedSizedMediators([], {diagram: this});

                var fixedSizedMediators = new FixedSizedMediators([], {diagram: this});
                this.fixedSizedMediators(fixedSizedMediators);

                var children = new Children([], {diagram: this});
                this.children(children);

                this.viewAttributes = {
                    leftUpperConer: {x: 0, y: 0},
                    rightLowerConer: {x: 0, y: 0}
                };

            },

            modelName: "LifeLine",

            nameSpace: sequenced,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "Lifeline"
            },

            getSchema: function () {
                var schema = {
                    "title": "Lifeline",
                    type: "object",
                    properties: {
                        Title: { "type": "string" }
                    }
                };
                return schema;
            },

            getEditableProperties: function (point) {
                var editableProperties = {};
                editableProperties.Title = this.attributes.title;
                //editableProperties.Uid = 123;
                //here add properties you want to see in property panel, but those need to be defined in above getSchema() method
                return editableProperties;
            },
            getPropertyPane: function (point) {
                var pane = new JSONEditor(document.getElementById("propertyPane"), {
                    schema: this.getSchema(),
                    no_additional_properties: true,
                    disable_properties: true,
                    disable_edit_json: true
                });
                var thisLifeline = this;
                pane.setValue(this.getEditableProperties());
                pane.watch('root.Title', function () {
                    $("#save-image").css({opacity: 1});
                    //thisLifeline.set('title', pane.getValue().Title); //commented as this results recursive call and updated to theolder value.
                });

                return pane;
            },
            leftUpperConer: function (point) {
                if (_.isUndefined(point)) {
                    return this.viewAttributes.leftUpperConer;
                } else {
                    this.viewAttributes.leftUpperConer = point;
                }
            },

            rightLowerConer: function (point) {
                if (_.isUndefined(point)) {
                    return this.viewAttributes.rightLowerConer;
                } else {
                    this.viewAttributes.rightLowerConer = point;
                }
            },

            createActivation: function (opts) {
                var activation = new SequenceD.Models.Activation({owner: this}, opts);
                this.addConnectionPoint(activation);
                return activation;
            },

            createPoint: function (x, y) {
                return new GeoCore.Models.Point({'x': x, 'y': y});
            },

            createLifeLine: function (title, center) {
                return new SequenceD.Models.LifeLine({title: title, centerPoint: center});
            },

            createFixedSizedMediator: function (title, center) {
                return new SequenceD.Models.FixedSizedMediator({title: title, centerPoint: center});
            },

            createProcessor: function (title, center, type, model, viewAttributes, parameters) {
                return new SequenceD.Models.Processor({
                    title: title,
                    centerPoint: center,
                    type: type,
                    model: model,
                    viewAttributes: viewAttributes,
                    parameters: parameters
                });
            },

            addFixedSizedMediator: function (element, opts) {
                this.fixedSizedMediators().add(element, opts);
                this.trigger("addFixedSizedMediator", element, opts);

            },

            addChild: function (element, opts) {
                //this.children().add(element, opts);

                if (element instanceof SequenceD.Models.Processor) {
                    var position = this.calculateIndex(element, element.get('centerPoint').get('y'));
                    var index = position.index;
                    this.children().add(element, {at: index});
                } else if (!_.isUndefined(opts)) {
                    //diagram.addElement(element, opts);
                    var position = this.calculateIndex(element, element.centerPoint.get('y'));
                    var index = position.index;
                    this.children().add(element, {at: index});
                }

                //this.trigger("addChild", element, opts);
                //this.trigger("addChildProcessor", element, opts);
            },


            calculateIndex: function (element, y) {
                var previousChild;
                var count = 1;
                var position = {};
                this.children().each(function (child) {
                    if (!_.isEqual(element, child)) {
                        if (child.get('centerPoint').get('y') > y) {
                            previousChild = child;
                            return false;
                        }
                        count = count + 1;
                    }
                });
                if (_.isUndefined(previousChild)) {
                    if (this.children().size() == 0) {
                        position.index = 0;
                    } else {
                        position.index = this.children().indexOf(element);
                    }
                } else {
                    position.index = this.children().indexOf(previousChild);
                }
                return position;
            },

            fixedSizedMediators: function (fixedSizedMediators) {
                if (_.isUndefined(fixedSizedMediators)) {
                    return this.get('fixedSizedMediators');
                    //console.log("fixedSizedMediators is undefined");
                } else {
                    this.set('fixedSizedMediators', fixedSizedMediators);
                }
            },

            children: function (children) {
                if (_.isUndefined(children)) {
                    return this.get('children');
                } else {
                    this.set('children', children);
                }
            }


        });

    var Activation = Diagrams.Models.ConnectionPoint.extend(
        /** @lends Activation.prototype */
        {
            /**
             * @augments ConnectionPoint
             * @constructs
             * @class Activation Represents the model for an activation in Sequence Diagrams.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.ConnectionPoint.prototype.initialize.call(this, attrs, options);
                this.owner().addConnectionPoint(this);
            },

            modelName: "Activation",

            nameSpace: sequenced

        });

    var Message = Diagrams.Models.Link.extend(
        /** @lends Message.prototype */
        {
            /**
             * @augments Link
             * @constructs
             * @class Message Represents the model for a Message in Sequence Diagrams.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.Link.prototype.initialize.call(this, attrs, options);
            },

            modelName: "Message",

            nameSpace: sequenced,

            defaults: {},

            source: function (ConnectionPoint, x, y) {
                return Diagrams.Models.Link.prototype.source.call(this, ConnectionPoint, x, y);
            },

            destination: function (ConnectionPoint, x, y) {
                return Diagrams.Models.Link.prototype.destination.call(this, ConnectionPoint, x, y);
            },

            makeParallel: function () {
                return false;
            }
        });

    var MessageLink = Diagrams.Models.DiagramElement.extend(
        /** @lends MessageLink.prototype */
        {
            /**
             * @augments Link
             * @constructs
             * @class MessageLink Represents the model for a Message in Sequence Diagrams.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.DiagramElement.prototype.initialize.call(this, attrs, options);
                this.sourcePoint = attrs.source;
                this.destinationPoint = attrs.destination;
            },

            modelName: "MessageLink",

            nameSpace: sequenced,

            defaults: {},

            source: function (messagePoint) {
                if (messagePoint) {
                    this.sourcePoint = messagePoint;
                } else {
                    return this.sourcePoint;
                }
            },

            destination: function (messagePoint) {
                if (messagePoint) {
                    this.destinationPoint = messagePoint;
                } else {
                    return this.destinationPoint;
                }
            }

        });

    var MessagePoint = Diagrams.Models.DiagramElement.extend(
        /** @lends MessagePoint.prototype */
        {
            /**
             * @augments DiagramElement
             * @constructs
             * @class MessagePoint Represents the connection point in a diagram.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.DiagramElement.prototype.initialize.call(this, attrs, options);
                this.centerPoint = new GeoCore.Models.Point({x: attrs.x, y: attrs.y});
                this.direction = attrs.direction;
            },

            modelName: "MessagePoint",

            nameSpace: sequenced,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0})
            },

            setY: function (y) {
                this.get('centerPoint').set('y', y);
            },

            setX: function (x) {
                this.get('centerPoint').set('x', x);
            },

            setMessage: function (Message) {
                this.message = Message;
            }

        });

    // set models
    models.Activation = Activation;
    models.Message = Message;
    models.LifeLine = LifeLine;
    models.Processor = Processor;
    models.MessagePoint = MessagePoint;
    models.MessageLink = MessageLink;

    sequenced.Models = models;

    return sequenced;

}(SequenceD || {}));
