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

var Tools = (function (tools) {
    var views = tools.Views || {};

    var fileBrowser = Backbone.View.extend({

        initialize: function (options) {
            var opts = options || {};
            opts.container = opts.container || "#file-browser";
            this.options = opts;
            this.render();
        },

        render: function () {
            var self = this;
            self.selected = false;
            $(this.options.container)
                .jstree({
                    'core' : {
                        'data' : {
                            'url': function (node) {
                                if(node.id === '#') {
                                    return "http://localhost:8080/service/workspace/root";
                                }
                                else {
                                    return "http://localhost:8080/service/workspace/list?path=" + node.id;
                                }
                            },
                            'dataType': "json",
                            'data' : function (node) {
                                return { 'id' : node.id };
                            }
                        },
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
                        self.selected = data.selected;
                        console.log("selected  ", data.selected);
                    }
                    else {
                        self.selected = false;
                    }
                });
            return this;
        }
    });

    views.FileBrowser = fileBrowser;
    tools.Views = views;
    return tools;

}(Tools || {}));
