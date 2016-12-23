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
define(['lodash', 'diagram_core'], function ( _, DiagramCore) {

    var MessagePoint = DiagramCore.Models.DiagramElement.extend(
        /** @lends MessagePoint.prototype */
        {
            /**
             * @augments DiagramElement
             * @constructs
             * @class MessagePoint Represents the connection point in a diagram.
             */
            initialize: function (attrs, options) {
                DiagramCore.Models.DiagramElement.prototype.initialize.call(this, attrs, options);
                this.type = attrs.model.type;
                this.model = attrs.model;
                this.set("centerPoint", new DiagramCore.Models.Point({x: attrs.x, y: attrs.y}));
                this.set("direction", attrs.direction);
            },

            modelName: "MessagePoint",

            defaults: {
                "centerPoint": new DiagramCore.Models.Point({x: 0, y: 0}),
                width : 0,
                height : 0
            },

            centerPoint: function (centerPoint) {
                if(_.isUndefined(centerPoint)){
                    return this.get('centerPoint');
                }
                this.set('centerPoint', centerPoint);
            },

            y: function (y) {
                if(_.isUndefined(y)){
                    return this.get('centerPoint').y();
                }
                this.get('centerPoint').y(y);
            },

            x: function (x) {
                if(_.isUndefined(x)){
                    return this.get('centerPoint').x();
                }
                this.get('centerPoint').x(x);
            },

            message: function (message) {
                if(_.isUndefined(message)){
                    return this.get("message");
                }
                this.set("message", message);
            },

            direction: function (direction) {
                if(_.isUndefined(direction)){
                    return this.get("direction");
                }
                this.set("direction", direction);
            },

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
            }

        });

    return MessagePoint;
});

