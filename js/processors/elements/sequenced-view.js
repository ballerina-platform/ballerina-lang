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

/**
 * SequenceD-Views Module extension.
 *
 * Definition of Backbone Views required for Sequence Diagrams.
 */
var SequenceD = (function (sequenced) {
    var views = sequenced.Views = sequenced.Views || {};

    var UnitProcessorView = Diagrams.Views.Processor.extend(
        /** @lends UnitProcessorView.prototype */
        {
            /**
             * @augments ProcessorView
             * @constructs
             * @class UnitProcessorView Represents the view for UnitProcessor(Eg: logger, header etc.) components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.Processor.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID) {

            }

        });


    var ComplexProcessorView = Diagrams.Views.Processor.extend(
        /** @lends ComplexProcessorView.prototype */
        {
            /**
             * @augments ProcessorView
             * @constructs
             * @class UnitProcessorView Represents the view for UnitProcessor(Eg: logger, header etc.) components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.Processor.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID) {

            }

        });


    var DynamicContainableProcessorView = Diagrams.Views.Processor.extend(
        /** @lends DynamicContainableProcessorView.prototype */
        {
            /**
             * @augments ProcessorView
             * @constructs
             * @class UnitProcessorView Represents the view for UnitProcessor(Eg: logger, header etc.) components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.Processor.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID) {

            }

        });

    var CustomProcessorView = Diagrams.Views.Processor.extend(
        /** @lends CustomProcessorView.prototype */
        {
            /**
             * @augments ProcessorView
             * @constructs
             * @class UnitProcessorView Represents the view for UnitProcessor(Eg: logger, header etc.) components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                Diagrams.Views.Processor.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID) {

            }

        });

    views.UnitProcessorView = UnitProcessorView;
    views.ComplexProcessorView = ComplexProcessorView;
    views.DynamicContainableProcessorView = DynamicContainableProcessorView;
    views.CustomProcessorView = CustomProcessorView;

    return sequenced;

}(SequenceD || {}));
