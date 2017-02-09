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
define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'diagram_core'], function (require, $, d3, Backbone, _, DiagramCore) {

    var Processor = DiagramCore.Models.Shape.extend(
    /** @lends Processor.prototype */
    {

        selectedNode: null,
        /**
         * @augments DiagramElement
         * @constructs
         * @class Processor represents the model for processors in diagram.
         */
        initialize: function (attrs, options) {
            DiagramCore.Models.Shape.prototype.initialize.call(this, attrs, options);
            this.model = attrs.model;
            this.viewAttributes = attrs.viewAttributes;
            this.parameters = attrs.parameters;
            this.widestChild = null;

        },

        modelName: "Processor",

        idAttribute: this.cid,

        parameters: {},

        viewAttributes: {},

        setY: function (y) {
            this.get('centerPoint').set('y', y);
        },

        getWidth: function () {
            return this.get('width');
        },

        getHeight: function (){
            return this.get('height');
        },

        setWidth: function (width) {
            this.set('width', width);
        },

        setHeight: function (height) {
            this.set('height', height);
        },

        setX: function (x) {
            this.get('centerPoint').set('x', x);
        },

        getX: function () {
            this.get('centerPoint').get('x');
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

        defaults: {
            centerPoint: new DiagramCore.Models.Point({x: 0, y: 0}),
            width : 130,
            height : 60,
            title: "Processor"
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

    return Processor;
});

