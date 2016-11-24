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
define(['require', 'log', 'jquery', 'lodash', './tab', 'ballerina', 'main_elements', 'diagram_core', 'workspace', 'app/ballerina/views/source', 'lib/beautify/beautify'],
    function (require, log, jquery, _, Tab, Ballerina, MainElements, DiagramCore, Workspace, SourceView, Beautify) {
    var  ServiceTab;

    ServiceTab = Tab.extend({
        initialize: function (options) {
            Tab.prototype.initialize.call(this, options);
            if(!_.has(options, 'file')){
                this._file = new Workspace.File({isTemp: true}, {storage: this.getParent().getBrowserStorage()});
            } else {
                this._file = _.get(options, 'file');
            }
        },
        getFile: function(){
            return this._file;
        },

        render: function () {
            Tab.prototype.render.call(this);
            var viewObj = this;

            var canvasContainer = this.$el.find(_.get(this.options, 'canvas.container'));
            var previewContainer = this.$el.find(_.get(this.options, 'preview.container'));
            var sourceContainer = this.$el.find(_.get(this.options, 'source.container'));
            var toggleControlsContainer = this.$el.find(_.get(this.options, 'toggle_controls.container'));
            var toggleSourceIcon = $(_.get(this.options, 'toggle_controls.sourceIcon')).find("img");
            var toggleDesignIcon = $(_.get(this.options, 'toggle_controls.designIcon')).find("img");
            if(!canvasContainer.length > 0){
                var errMsg = 'cannot find container to render svg';
                log.error(errMsg);
                throw errMsg;
            }
            var serviceViewOpts = {};
            _.set(serviceViewOpts, 'container', canvasContainer.get(0));
            _.set(serviceViewOpts, 'toolPalette', this.getParent().options.toolPalette);
            var serviceView = new Ballerina.Views.ServiceView(serviceViewOpts);

            serviceView.render();

            var sourceViewOptions = {
                sourceContainer: sourceContainer.attr('id')
            };

            $('source-container-id').hide();

            var sourceView = new SourceView(sourceViewOptions);

            toggleSourceIcon.on('click', function () {
                // Hide the tab components
                viewObj.getParent().hideTabComponents();
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
                // Show the tab components
                viewObj.getParent().showTabComponents();
                canvasContainer.removeClass('hide-div').addClass('show-div');
                previewContainer.removeClass('hide-div').addClass('show-div');
                toggleControlsContainer.find('.toggle-to-design').removeClass('show-div').addClass('hide-div');
                toggleControlsContainer.find('.toggle-to-source').removeClass('hide-div').addClass('show-div');
                sourceContainer.removeClass('source-view-enabled').addClass('source-view-disabled');
            });


        }
    });

    return ServiceTab;
});