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
define(['./processor'], function (ProcessorView) {
    var ComplexProcessorView = ProcessorView.extend(
        /** @lends ComplexProcessorView.prototype */
        {
            /**
             * @augments ProcessorView
             * @constructs
             * @class UnitProcessorView Represents the view for UnitProcessor(Eg: logger, header etc.) components in Sequence Diagrams.
             * @param {Object} options Rendering options for the view
             */
            initialize: function (options) {
                ProcessorView.Processor.prototype.initialize.call(this, options);
            },

            verticalDrag: function () {
                return false;
            },

            render: function (paperID) {

            }

        });

    return ComplexProcessorView;
});

