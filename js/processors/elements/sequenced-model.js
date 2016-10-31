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

    var UnitProcessor = SequenceD.Models.Processor.extend(
        /** @lends DiagramElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Element represents the model for elements in a diagram.
             */
            initialize: function (attrs, options) {
                SequenceD.Models.Processor.prototype.initialize.call(this, attrs, options);
            },

            modelName: "UnitProcessor",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                width: 130,
                height: 65,
                title: "UnitProcessor"
            }
        });

    var ComplexProcessor = SequenceD.Models.Processor.extend(
        /** @lends DiagramElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Element represents the model for elements in a diagram.
             */
            initialize: function (attrs, options) {
                SequenceD.Models.Processor.prototype.initialize.call(this, attrs, options);
                var containableProcessorElements = new ContainableProcessorElements([], {diagram: this});
                this.containableProcessorElements(containableProcessorElements);
            },

            modelName: "ComplexProcessor",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "ComplexProcessor"
            },

            containableProcessorElements: function (containableProcessorElements) {
                if (_.isUndefined(containableProcessorElements)) {
                    return this.get('containableProcessorElements');
                } else {
                    this.set('containableProcessorElements', containableProcessorElements);
                }
            },

        });


    var DynamicContainableProcessor = SequenceD.Models.Processor.extend(
        /** @lends DiagramElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Element represents the model for elements in a diagram.
             */
            initialize: function (attrs, options) {
                SequenceD.Models.Processor.prototype.initialize.call(this, attrs, options);
            },

            modelName: "DynamicContainableProcessor",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "DynamicContainableProcessor"
            }
        });

    var CustomProcessor = SequenceD.Models.Processor.extend(
        /** @lends DiagramElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class Element represents the model for elements in a diagram.
             */
            initialize: function (attrs, options) {
                SequenceD.Models.Processor.prototype.initialize.call(this, attrs, options);
                var children = new Children([], {diagram: this});
                this.children(children);
            },

            modelName: "CustomProcessor",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "CustomProcessor"
            },

            children: function (children) {
                if (_.isUndefined(children)) {
                    return this.get('children');
                } else {
                    this.set('children', children);
                }
            },

            addChild: function (element, opts) {
                element.parent(this);
                var position = this.calculateIndex(element, element.get('centerPoint').get('y'));
                var index = position.index;
                this.children().add(element, {at: index});
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
            }
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


    var ContainableProcessorElement = Diagrams.Models.Shape.extend(
        /** @lends ContainableProcessorElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class ContainableProcessorElement represents the model for processor element which can contain processors.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.Shape.prototype.initialize.call(this, attrs, options);
                var children = new Children([], {diagram: this});
                this.type = attrs.type;
                this.children(children);
                this.widestChild = null;
            },

            modelName: "ContainableProcessorElement",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "ContainableProcessorElement",
                width: 130,
                height: 30,
                viewAttributes: {colour: "#998844"}
            },

            // Processors can override this method on order to define the behavior of drawing the messages from
            // the particular processor to the destination model (lifeline or any other processor)
            canConnect: function (destinationModel) {
                var availableConnects =  this.get('utils').canConnectTo();

                // Check whether the destination model is one of the parent of the source model
                var parent = this.get('parent');
                while (!_.isUndefined(parent)) {
                    if (parent.cid === destinationModel.cid) {
                        return false;
                    } else {
                        parent = parent.get('parent');
                    }
                }

                if (!_.isUndefined(availableConnects)) {
                    for (var itr = 0; itr < availableConnects.length; itr ++) {
                        if (availableConnects[itr] === destinationModel.type) {
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            },

            children: function (children) {
                if (_.isUndefined(children)) {
                    return this.get('children');
                } else {
                    this.set('children', children);
                }
            },

            addChild: function (element, opts) {
                element.parent(this);
                var position = this.calculateIndex(element, element.get('centerPoint').get('y'));
                var index = position.index;
                this.children().add(element, {at: index});
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

            createProcessor: function (title, center, type, model, viewAttributes, parameters, utils) {
                return new SequenceD.Models.ProcessorFactory(title, center, model.type, model, viewAttributes, parameters, utils);
            },

            setY: function (y) {
                this.get('centerPoint').set('y', y);
            },

            setX: function (x) {
                this.get('centerPoint').set('x', x);
            },

            getWidth: function () {
                return this.get('width');
            },

            getHeight: function () {
                return this.get('height');
            },

            setWidth: function (width) {
                this.set('width', width);
            },

            setHeight: function (height) {
                this.set('height', height);
            },


        });


    var ContainableProcessorElements = Backbone.Collection.extend(
        /** @lends ContainableProcessorElements.prototype */
        {
            /**
             * @augments Backbone.Collection
             * @constructs
             * @class ContainableProcessorElements represents the collection for elements in a diagram.
             */
            initialize: function (models, options) {
            },

            modelName: "ContainableProcessorElements",

            nameSpace: sequenced,

            model: ContainableProcessorElement

        });

    // set models
    models.UnitProcessor = UnitProcessor;
    models.ComplexProcessor = ComplexProcessor;
    models.DynamicContainableProcessor = DynamicContainableProcessor;
    models.CustomProcessor = CustomProcessor;
    models.ContainableProcessorElement = ContainableProcessorElement;
    models.ContainableProcessorElements = ContainableProcessorElements;

    sequenced.Models = models;

    return sequenced;

}(SequenceD || {}));
