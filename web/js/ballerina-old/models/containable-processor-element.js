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
define(['require', 'diagram_core', './children', 'app/ballerina-old/utils/processor-factory'], function (require, DiagramCore, Children, ProcessorFactory) {

    var ContainableProcessorElement = DiagramCore.Models.Shape.extend(
        /** @lends ContainableProcessorElement.prototype */
        {
            selectedNode: null,
            /**
             * @augments ContainableProcessorElement
             * @constructs
             * @class ContainableProcessorElement represents the model for processor element which can contain processors.
             */
            initialize: function (attrs, options) {
                DiagramCore.Models.Shape.prototype.initialize.call(this, attrs, options);
                var children = new Children([], {diagram: this});
                this.type = "ContainableProcessorElement";
                this.children(children);
                this.widestChild = null;
            },

            modelName: "ContainableProcessorElement",

            idAttribute: this.cid,

            defaults: {
                centerPoint: new DiagramCore.Models.Point({x: 0, y: 0}),
                title: "ContainableProcessorElement",
                width: 130,
                height: 60,
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

            createProcessor: function (title, center, type, model, viewAttributes, parameters, utils,textModel, width, height,serviceView) {
                return new ProcessorFactory(title, center, model.type, model, viewAttributes, parameters, utils, textModel, width, height,serviceView);
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
            }


        });

    return ContainableProcessorElement;
});

