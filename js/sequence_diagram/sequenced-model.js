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
            },

            modelName: "Processor",

            nameSpace: sequenced,

            idAttribute: this.cid,

            parameters: {},

            viewAttributes: {},

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

                //var elements = new DiagramElements([], { diagram: this });

                var elements = new FixedSizedMediators([], {diagram: this});

                var fixedSizedMediators = new FixedSizedMediators([], {diagram: this});
                this.fixedSizedMediators(fixedSizedMediators);

                var children = new Children([], {diagram: this});
                this.children(children);

            },

            modelName: "LifeLine",

            nameSpace: sequenced,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "lifeline"
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
                this.children().add(element, opts);
                this.trigger("addChild", element, opts);
                this.trigger("addChildProcessor", element, opts);
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
                return Diagrams.Models.Link.prototype.source.call(this, ConnectionPoint,x, y);
            },

            destination: function (ConnectionPoint, x, y) {
                return Diagrams.Models.Link.prototype.destination.call(this, ConnectionPoint, x, y);
            },

            makeParallel: function () {
                return false;
            }
        });

    // set models
    models.Activation = Activation;
    models.Message = Message;
    models.LifeLine = LifeLine;
    models.FixedSizedMediator = FixedSizedMediator;
    models.FixedSizedMediators = FixedSizedMediators;
    models.Processor = Processor;

    sequenced.Models = models;

    return sequenced;

}(SequenceD || {}));
