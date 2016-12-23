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
define(['diagram_core', './processor'], function (DiagramCore, Processor) {

    var CustomProcessor = Processor.extend(
        /** @lends CustomProcessor.prototype */
        {

            selectedNode: null,
            /**
             * @augments CustomProcessor
             * @constructs
             * @class CustomProcessor processor
             */
            initialize: function (attrs, options) {
                Processor.prototype.initialize.call(this, attrs, options);
                var children = new Children([], {diagram: this});
                this.children(children);
            },

            modelName: "CustomProcessor",

            idAttribute: this.cid,

            defaults: {
                centerPoint: new DiagramCore.Models.Point({x: 0, y: 0}),
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

    return CustomProcessor;

});

