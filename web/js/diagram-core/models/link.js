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

    var Link = DiagramElement.extend(
    /** @lends Link.prototype */
    {
        /**
         * @augments DiagramElement
         * @constructs
         * @class Link represents the model for a link between two elements in a diagrams.
         */
        initialize: function (attrs, options) {
            DiagramElement.prototype.initialize.call(this, attrs, options);
            this.source(attrs['source']['activation'], attrs['source']['x'], attrs['source']['y']);
            this.destination(attrs['destination']['activation'], attrs['destination']['x'], attrs['destination']['y']);
        },

        modelName: "Link",

        defaults: {},

        /**
         * Gets or sets source connectionPoint for the link.
         * @param {ConnectionPoint} [connectionPoint] Source connectionPoint
         */
        source: function (connectionPoint, x, y) {
            if (connectionPoint === undefined) {
                return this.get('source');
            }
            var connection = connectionPoint.connectLink(this, {type: 'outgoing', x: x, y: y});
            if (this.makeParallel()) {
                connection.point().y(this.destination().point().y());
            }
            this.set('source', connection);
        },
        /**
         * Gets or sets destination connectionPoint for the link.
         * @param {ConnectionPoint} [connectionPoint] Destination connectionPoint
         */
        destination: function (connectionPoint, x, y) {
            if (connectionPoint === undefined) {
                return this.get('destination');
            }
            var connection = connectionPoint.connectLink(this, {type: 'incoming', x: x, y: y});
            if (this.makeParallel()) {
                connection.point().y(this.source().point().y());
            }
            this.set('destination', connection);
        },
        makeParallel: function () {
            return true;
        }
    });

    return Link;
});

