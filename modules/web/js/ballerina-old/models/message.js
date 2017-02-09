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

    var Message = DiagramCore.Models.Link.extend(
        /** @lends Message.prototype */
        {
            /**
             * @augments Link
             * @constructs
             * @class Message Represents the model for a Message in Sequence Diagrams.
             */
            initialize: function (attrs, options) {
                DiagramCore.Models.Link.prototype.initialize.call(this, attrs, options);
            },

            modelName: "Message",

            defaults: {},

            sourcePoint: function (sourcePoint) {
                if (_.isUndefined(sourcePoint)) {
                    return this.get("sourcePoint");
                }
                this.set("sourcePoint", sourcePoint);
            },

            source: function (ConnectionPoint, x, y) {
                return DiagramCore.Models.Link.prototype.source.call(this, ConnectionPoint, x, y);
            },

            destination: function (ConnectionPoint, x, y) {
                return DiagramCore.Models.Link.prototype.destination.call(this, ConnectionPoint, x, y);
            },

            makeParallel: function () {
                return false;
            }
        });

    return Message;
});

