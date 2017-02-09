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
define(['lodash', './element'], function (_, DiagramElement) {

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

    return Connection;
});

