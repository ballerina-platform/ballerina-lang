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
define(['require', 'jquery', 'd3', 'd3utils', 'backbone', 'lodash', 'diagram_core', 'main_elements',
        './service-outline', 'processors', './life-line',
        'ballerina_models/containable-processor-element', 'ballerina_models/life-line',  'ballerina_models/message-point',
        'ballerina_models/message-link', 'svg_pan_zoom'],

function (require, $, d3, D3Utils, Backbone,  _, DiagramCore, MainElements, DiagramPreview, Processors, LifeLineView,
          ContainableProcessorElement, LifeLine, MessagePoint, MessageLink
) {

    var ResourceView = Backbone.View.extend(
    /** @lends ResourceView.prototype */
    {
        /**
         * @augments Backbone.View
         * @constructs
         * @class ResourceView Represents the view for a resource in a ballerina service
         * @param {Object} options Rendering options for the view
         */
        initialize: function (options) {
        }
    });

    return ResourceView;
});

