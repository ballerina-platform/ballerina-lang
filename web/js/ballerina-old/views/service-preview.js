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

define(['require', 'jquery', 'd3', 'd3utils', 'backbone', 'lodash','log','./service'], function (require, $, d3, D3Utils, Backbone,  _,log, Service) {

    var ServicePreview = Service.extend(
        /** @lends ServicePreview.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class ServicePreview Represents the view for ballerina samples
             * @param {Object} options Rendering options for the view
             */
        initialize: function (config) {

                var errMsg;
                if (!_.has(config, 'parentContainer')) {
                    errMsg = 'unable to find configuration for parentContainer';
                    log.error(errMsg);
                    throw errMsg;
                }
                // get parent container which is innerSamples div
                var parentContainer = $(_.get(config, 'parentContainer'));

                this._sampleName = config.sampleName;
                //create the parent for drawn svg
                var previewDiv = $("<div class='preview-div'></div>");

                var options =
                {
                    "container": previewDiv
                }
                options.container = options.container[0];
                this._container = options.container;
                // the first item need to active for bootstrap carousel
                if (_.has(config, 'firstItem')) {
                    var listItem = $("<div class='item active'></div>");
                }
                else{
                    var listItem = $("<div class='item'></div>");
                }
                var previewParent = $("<div class='col-xs-3 preview-parent'></div>");
                var fileName = $("<div class='file-name'></div>");
                var fileNameContent = $("<span></span>");
                fileNameContent.text(this._sampleName);
                fileName.append(fileNameContent);
                var previewName = $("<div class='preview-name-div'></div>");
                previewName.append(fileName);
                previewParent.append(this._container);
                previewParent.append(previewName);
                listItem.append(previewParent);
                parentContainer.append(listItem);
                this.setPreviewMode(true);
                Service.prototype.initialize.call(this, options);

        },

        render: function () {

            Service.prototype.render.call(this);


        }
    });
    return ServicePreview;
});
