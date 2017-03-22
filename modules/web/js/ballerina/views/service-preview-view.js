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

import $ from 'jquery';
import * as d3 from 'd3';
import D3Utils from 'd3utils';
import Backbone from 'backbone';
import _ from 'lodash';
import log from 'log';

class ServicePreviewView {
    constructor(config) {
        this._config = config;
    }

    render() {
        var config = this._config;
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
        var image = $("<img id='previewImg' class='preview-img' src='images/preview_"+config.sampleName.split('.')[0]+".png'/>");
        previewDiv.prepend(image);
        previewDiv.bind('click', config.clickEventCallback);

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
    }
}

ServicePreviewView.prototype.constructor = ServicePreviewView;

export default ServicePreviewView;
