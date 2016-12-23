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
define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'diagram_core',
    'app/ballerina-old/models/children', 'app/ballerina-old/models/processor', 'app/ballerina-old/utils/processor-factory'],
    function (require, $, d3, Backbone, _, DiagramCore, Children, Processor, ProcessorFactory) {

    var LifeLine = DiagramCore.Models.Shape.extend(
        /** @lends LifeLine.prototype */
        {
            /**
             * @augments Element
             * @constructs
             * @class LifeLine Represents the model for a LifeLine in Sequence Diagrams.
             */
            initialize: function (attrs, options) {
                DiagramCore.Models.Shape.prototype.initialize.call(this, attrs, options);

                var children = new Children([], {diagram: this});
                this.children(children);
                this.type = attrs.type;
                this.definition = attrs.definition;

                this.viewAttributes = {
                    class: attrs.cssClass
                };

                var textModel = new DiagramCore.Models.TextController({});
                this.attributes.textModel = textModel;
                textModel.hasParent = true;
                textModel.parentObject(this);
            },

            modelName: "LifeLine",

            defaults: {
                centerPoint: new DiagramCore.Models.Point({x: 0, y: 0}),
                title: "Lifeline",
                width : 0,
                height : 300,
                viewAttributes: {colour: "#ffffff"},
                definition: undefined,
                serviceView: undefined
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

            leftUpperCorner: function (point) {
                if (_.isUndefined(point)) {
                    return this.viewAttributes.leftUpperCorner;
                } else {
                    this.viewAttributes.leftUpperCorner = point;
                }
            },

            rightLowerCorner: function (point) {
                if (_.isUndefined(point)) {
                    return this.viewAttributes.rightLowerCorner;
                } else {
                    this.viewAttributes.rightLowerCorner = point;
                }
            },

            createPoint: function (x, y) {
                return new DiagramCore.Models.Point({'x': x, 'y': y});
            },

            createLifeLine: function (title, center, colour, type, definition) {
                return new LifeLine({title: title, centerPoint: center, colour: colour, type: type, definition: definition});
            },

            createProcessor: function (title, center, type, model, viewAttributes, parameters, utils, textModel, width, height,serviceView) {
                return new ProcessorFactory(title, center, model.type, model, viewAttributes, parameters, utils, textModel, width, height,serviceView);
            },

            addChild: function (element, opts) {
                //this.children().add(element, opts);

                element.parent(this);
                if (element instanceof Processor) {
                    var position = this.calculateIndex(element, element.get('centerPoint').get('y'));
                    var index = position.index;
                    this.children().add(element, {at: index});
                } else if (!_.isUndefined(opts)) {
                    //diagram.addElement(element, opts);
                    // element is a message point
                    var position = this.calculateIndex(element, element.y());
                    var index = position.index;
                    this.children().add(element, {at: index});
                }
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

            children: function (children) {
                if (_.isUndefined(children)) {
                    return this.get('children');
                } else {
                    this.set('children', children);
                }
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

            inputConnector: function (inputConnector){
                if(_.isUndefined(inputConnector)){
                    return this.get('inputConnector');
                }
                inputConnector.parent(this);
                this.set('inputConnector', inputConnector);
            },

            outputConnector: function (outputConnector) {
                if(_.isUndefined(outputConnector)){
                    return this.get('outputConnector');
                }
                outputConnector.parent(this);
                this.set('outputConnector', outputConnector);
            }

        });

    return LifeLine;
});

