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
define(['lodash', 'backbone', './element', './connection'], function (_, Backbone, DiagramElement, Connection) {

    var ConnectionPoint = DiagramElement.extend(
    /** @lends ConnectionPoint.prototype */
    {
        /**
         * @augments DiagramElement
         * @constructs
         * @class ConnectionPoint represents a connection point owned by an element in a diagrams.
         */
        initialize: function (attrs, options) {
            DiagramElement.prototype.initialize.call(this, attrs, options);
            this.connections = new Backbone.Collection([], {model: Connection, connectionPoint: this});
            this.owner().on("elementMoved", this.onOwnerMoved, this);
        },

        modelName: "ConnectionPoint",

        connectLink: function (link, options) {
            var connection = new Connection({type: options.type, link: link, connectionPoint: this});
            this.connections.add(connection);
            this.trigger("connectionMade", connection, options.x, options.y);
            return connection;
        },

        onOwnerMoved: function (event) {
            this.trigger("elementMoved", event);
        },

        /**
         * Sets or gets the instance of shape which owns this connection point
         * @param {Shape} [shape]
         * @returns {Shape|void}
         */
        owner: function (shape) {
            if (_.isUndefined(shape)) {
                return this.get('owner');
            }
            this.set('owner', shape);
        }

    });

    return ConnectionPoint;
});

