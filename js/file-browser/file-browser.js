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

define(['require', 'jquery', 'd3', 'backbone', 'lodash', 'js_tree'], function (require, $, d3, Backbone, _) {

    var FileBrowser = Backbone.View.extend({

        initialize: function (options) {
            var opts = options || {};
            opts.container = opts.container || "#file-browser";
            this.options = opts;
            this.render();
        },

        render: function () {
            var self = this;
            self.selected = false;
            jQuery(function($) {
                $(self.options.container)
                    .jstree({
                        'core' : {
                            'data' : {
                                'url': function (node) {
                                    if(node.id === '#') {
                                        return workspaceServiceURL + "/root";
                                    }
                                    else {
                                        return workspaceServiceURL + "/list?path=" + btoa(node.id);
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
                                'responsive' : false,
                                'variant' : 'small',
                                'stripes' : true
                            }
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
            });

            return this;
        }
    });

    return FileBrowser;

});
