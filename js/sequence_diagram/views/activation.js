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
define(['lodash', 'diagram', 'geometry'], function ( _, Diagrams, GeoCore) {

    var ActivationView = Diagrams.Views.ConnectionPointView.extend(
        /** @lends ConnectionPointView.prototype */
        {
            /**
             * @augments LinkView
             * @constructs
             * @class ActivationView Represents the view for activations in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.ConnectionPointView.prototype.initialize.call(this, options);
            },

            render: function (paperID) {
                // call super
                Diagrams.Views.ConnectionPointView.prototype.render.call(this, paperID);

            },

            getNextAvailableConnectionPoint: function (connecion, x, y) {
                var nextYCoordinate = defaultView.model.deepestPointY + 50;
                var nextXCoordinate = this.model.owner().get('centerPoint').x();

                // TODO: Until the layout finalize we will be drawing the message without offsetting dynamically
                //if (_.isEqual(connecion.type(), "incoming")) {
                //    lifeLineOptions.diagram.deepestPointY = nextYCoordinate;
                //}
                return new GeoCore.Models.Point({'x': nextXCoordinate, 'y': defaultView.model.sourceLifeLineY});
            }
        });

    return ActivationView;
});

