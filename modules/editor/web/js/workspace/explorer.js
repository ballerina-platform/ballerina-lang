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

define(['log', 'jquery', 'backbone', 'lodash', 'tree_view', /** void module - jquery plugin **/ 'js_tree', 'nano_scroller'],

    function (log, $, Backbone, _, TreeMod) {

    var WorkspaceExplorer = Backbone.View.extend({

        initialize: function (config) {
            var errMsg;
            if (!_.has(config, 'container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            var container = $(_.get(config, 'container'));
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for file browser with selector: ' + _.get(config, 'container');
                log.error(errMsg);
                throw errMsg;
            }
            this._$parent_el = container;

            if(!_.has(config, 'application')){
                log.error('Cannot init file browser. config: application not found.')
            }
            this.application = _.get(config, 'application');
            this._options = config;
            this.workspaceServiceURL = _.get(this._options, 'application.config.services.workspace.endpoint');
            this._isActive = false;
            this._lastWidth = undefined;
            this._verticalSeparator = $(_.get(this._options, 'separator'));
            this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));

            // register command
            this.application.commandManager.registerCommand(config.command.id, {shortcuts: config.command.shortcuts});
            this.application.commandManager.registerHandler(config.command.id, this.toggleExplorer, this);
        },

        toggleExplorer: function(){
            if(this._isActive){
                this._$parent_el.parent().width('0px');
                this._containerToAdjust.css('margin-left', _.get(this._options, 'leftOffset'));
                this._verticalSeparator.css('left', _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
                this._isActive = false;
            } else {
                var width = this._lastWidth || _.get(this._options, 'defaultWidth');
                this._$parent_el.parent().width(width);
                this._containerToAdjust.css('margin-left', width + _.get(this._options, 'leftOffset'));
                this._verticalSeparator.css('left',  width + _.get(this._options, 'leftOffset') - _.get(this._options, 'separatorOffset'));
                this._isActive = true;
            }
        },

        render: function () {
            var self = this;
            var activateBtn = $(_.get(this._options, 'activateBtn'));

            var sliderContainer = $('<div></div>');
            sliderContainer.addClass(_.get(this._options, 'cssClass.container'));
            this._$parent_el.append(sliderContainer);

            activateBtn.on('click', function(){
                self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
            });

            this._verticalSeparator.on('drag', function(event){
                if( event.originalEvent.clientX >= _.get(self._options, 'resizeLimits.minX')
                    && event.originalEvent.clientX <= _.get(self._options, 'resizeLimits.maxX')){
                    self._verticalSeparator.css('left', event.originalEvent.clientX - _.get(self._options, 'separatorOffset'));
                    self._verticalSeparator.css('cursor', 'ew-resize');
                    var newWidth = event.originalEvent.clientX -  _.get(self._options, 'leftOffset');
                    self._$parent_el.parent().width(newWidth);
                    self._containerToAdjust.css('margin-left', event.originalEvent.clientX);
                    self._lastWidth = newWidth;
                    self._isActive = true;
                }
                event.preventDefault();
                event.stopPropagation();
            });

            sliderContainer
                .jstree({
                    'core' : {
                        'data' : {
                            'url': function (node) {
                                if(node.id === '#') {
                                    return self.workspaceServiceURL + "/root";
                                }
                                else {
                                    return self.workspaceServiceURL + "/list?path=" + btoa(node.id);
                                }
                            },
                            'dataType': "json",
                            'data' : function (node) {
                                return { 'id' : node.id };
                            }
                        },
                        'multiple' : false,
                        'check_callback' : false,
                        'force_text' : true,
                        'themes' : {
                            'stripes' : true
                        },
                        "plugins" : [ "contextmenu", "dnd", "search", "state", "types", "wholerow" ]

                    }
                })
                .on('changed.jstree', function (e, data) {
                    if(data && data.selected && data.selected.length) {
                        self.selected = data.selected[0];
                    }
                    else {
                        self.selected = false;
                    }
                });
            return this;
        }
    });

    return WorkspaceExplorer;

});
