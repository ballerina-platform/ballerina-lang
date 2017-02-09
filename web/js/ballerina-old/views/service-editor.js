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

define(['require', 'jquery', 'd3', 'd3utils', 'backbone', 'lodash','log','./service', './source', 'beautify'],

    function (require, $, d3, D3Utils, Backbone,  _,log, ServiceView, SourceView, Beautify) {

    var ServicePreview = Backbone.View.extend(
        /** @lends ServicePreview.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class ServicePreview Represents the view for ballerina samples
             * @param {Object} options Rendering options for the view
             */
        initialize: function (options) {
            if(!_.has(options, 'container')) {
                throw "container is not defined."
            }
            var container = $(_.get(options, 'container'));
            if(!container.length > 0) {
                throw "container not found."
            }
            this._$parent_el = container;

            if(_.has(options, 'antlr_worker')){
                if(_.isUndefined(window.antlr4) && _.isUndefined(this.antlr4)){

                    var libPath = _.get(options, 'antlr_worker.libPath');
                    var reqScriptPath = _.get(options, 'antlr_worker.reqScriptPath');

                    var antlrWorker = new Worker('./../parser/worker.js');
                    antlrWorker.addEventListener('message', function(e){
                        console.log(e.data);
                    }, false);
                    //antlrWorker.postMessage({command:'loadAntlr', libPath: libPath, reqScriptPath: reqScriptPath});

                }
            }
            this.options = options;

         },

        render: function () {
            var canvasContainer = this._$parent_el.find(_.get(this.options, 'canvas.container'));
            var previewContainer = this._$parent_el.find(_.get(this.options, 'preview.container'));
            var sourceContainer = this._$parent_el.find(_.get(this.options, 'source.container'));
            var toggleControlsContainer = this._$parent_el.find(_.get(this.options, 'toggle_controls.container'));
            var toggleSourceIcon = $(_.get(this.options, 'toggle_controls.sourceIcon')).find("img");
            var toggleDesignIcon = $(_.get(this.options, 'toggle_controls.designIcon')).find("img");
            if(!canvasContainer.length > 0){
                var errMsg = 'cannot find container to render svg';
                log.error(errMsg);
                throw errMsg;
            }
            var serviceViewOpts = {};
            _.set(serviceViewOpts, 'container', canvasContainer.get(0));
            _.set(serviceViewOpts, 'toolPalette', this.options.toolPalette);
            var serviceView = new ServiceView(serviceViewOpts);

            serviceView.render();

            var sourceViewOptions = {
                sourceContainer: sourceContainer.attr('id')
            };

            $('source-container-id').hide();

            var sourceView = new SourceView(sourceViewOptions);

            toggleSourceIcon.on('click', function () {
                canvasContainer.removeClass('show-div').addClass('hide-div');
                previewContainer.removeClass('show-div').addClass('hide-div');
                toggleControlsContainer.find('.toggle-to-source').removeClass('show-div').addClass('hide-div');
                toggleControlsContainer.find('.toggle-to-design').removeClass('hide-div').addClass('show-div');
                sourceContainer.removeClass('source-view-disabled').addClass('source-view-enabled');

                // Get the parsed source from the design and pass it to the ace editor rendering
                var parsedSource = serviceView.model.parseTree();
                parsedSource = Beautify.js_beautify(parsedSource);
                var sourceViewOptions = {
                    source: parsedSource
                };
                sourceView.render(sourceViewOptions);
            });

            toggleDesignIcon.on('click', function () {
                canvasContainer.removeClass('hide-div').addClass('show-div');
                previewContainer.removeClass('hide-div').addClass('show-div');
                toggleControlsContainer.find('.toggle-to-design').removeClass('show-div').addClass('hide-div');
                toggleControlsContainer.find('.toggle-to-source').removeClass('hide-div').addClass('show-div');
                sourceContainer.removeClass('source-view-enabled').addClass('source-view-disabled');
            });

        }
    });
    return ServicePreview;
});
