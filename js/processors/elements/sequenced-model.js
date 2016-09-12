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

var SequenceD = (function (sequenced) {
    var models = sequenced.Models || {};

    var UnitProcessorElement = Diagrams.Models.Shape.extend(
        /** @lends UnitProcessorElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class UnitProcessorElement represents the model for simple processor unit.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.Shape.prototype.initialize.call(this, attrs, options);
            },

            modelName: "UnitProcessorElement",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "UnitProcessorElement"
            }
        });

    var ContainableProcessorElement = Diagrams.Models.Shape.extend(
        /** @lends ContainableProcessorElement.prototype */
        {

            selectedNode: null,
            /**
             * @augments DiagramElement
             * @constructs
             * @class ContainableProcessorElement represents the model for processor element which can contain processors.
             */
            initialize: function (attrs, options) {
                Diagrams.Models.Shape.prototype.initialize.call(this, attrs, options);
            },

            modelName: "ContainableProcessorElement",

            nameSpace: sequenced,

            idAttribute: this.cid,

            defaults: {
                centerPoint: new GeoCore.Models.Point({x: 0, y: 0}),
                title: "ContainableProcessorElement"
            }
        });


    // set models
    models.UnitProcessorElement = UnitProcessorElement;
    models.ContainableProcessorElement = ContainableProcessorElement;

    sequenced.Models = models;

    return sequenced;

}(SequenceD || {}));
