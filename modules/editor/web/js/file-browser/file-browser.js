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

define(['jquery', 'backbone', 'lodash', 'tree_view', /** void module - jquery plugin **/ 'js_tree'], function ($, Backbone, _, TreeMod) {

    var FileBrowser = Backbone.View.extend({

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

            if (!_.has(config, 'application')) {
                log.error('Cannot init file browser. config: application not found.')
            }

            this.application = _.get(config, 'application');
            this._options = config;
            this.workspaceServiceURL = _.get(this._options, 'application.config.services.workspace.endpoint');
            this._isActive = false;
            this._fetchFiles = _.get(config, 'fetchFiles', false);
            this._root = _.get(config, 'root');
        },

        /**
         * @param path a single path or an array of folder paths to select
         */
        select: function(path){
            this._$parent_el.jstree(true).deselect_all();
            this._$parent_el.jstree(true).select_node(path);
        },

        render: function () {
            var self = this;
            this._$parent_el
                .jstree({
                    'core': {
                        'data': {
                            'url': function (node) {
                                if (node.id === '#') {
                                    if(!_.isNil(self._root)){
                                        if (self._fetchFiles) {
                                            return self.workspaceServiceURL + "/listFiles?path=" + btoa(self._root);
                                        } else {
                                            return self.workspaceServiceURL + "/list?path=" + btoa(self._root);
                                        }
                                    }
                                    return self.workspaceServiceURL + "/root";
                                }
                                else {
                                    if (self._fetchFiles) {
                                        return self.workspaceServiceURL + "/listFiles?path=" + btoa(node.id);
                                    } else {
                                        return self.workspaceServiceURL + "/list?path=" + btoa(node.id);
                                    }
                                }

                            },
                            'dataType': "json",
                            'data': function (node) {
                                return {'id': node.id};
                            }
                        },
                        'multiple': false,
                        'check_callback': false,
                        'force_text': true,
                        'expand_selected_onload': true,
                        'themes': {
                            'responsive': false,
                            'variant': 'small',
                            'stripes': false,
                            'dots': false,

                        }
                    },
                    'types': {
                        'default': {
                            'icon': 'fw fw-folder'
                        },
                        'folder': {
                            'icon': 'fw fw-folder'
                        },
                        'file': {
                            'icon': 'fw-document'
                        }
                    },
                    'plugins': ['types']
                }).on('changed.jstree', function (e, data) {
                    if (data && data.selected && data.selected.length) {
                        self.selected = data.selected[0];
                        self.trigger("selected", data.selected[0]);
                    }
                    else {
                        self.selected = false;
                        self.trigger("selected", null);
                    }
                }).on('open_node.jstree', function (e, data) {
                    data.instance.set_icon(data.node, "fw fw-folder");
                }).on('close_node.jstree', function (e, data) {
                    data.instance.set_icon(data.node, "fw fw-folder");
                }).on('ready', function(){
                    self.trigger("ready");
                }).on("dblclick.jstree", function (event) {
                    var node = $(event.target).closest("li");
                    var id = node[0].id;
                    self.trigger("double-click-node", id);
                });
            return this;
        }
    });

    return FileBrowser;

});
