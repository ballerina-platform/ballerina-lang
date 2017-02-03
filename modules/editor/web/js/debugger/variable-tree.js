/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['jquery', 'backbone', 'lodash', 'log', 'event_channel', /** void module - jquery plugin **/ 'js_tree'], function ($, Backbone, _, log, EventChannel) {

    var instance;

    var VariableTree = function (){
        this._treeConfig = {
            'core': {
                'data' : [],
                'multiple': false,
                'check_callback': false,
                'force_text': true,
                'expand_selected_onload': true,
                'themes': {
                    'responsive': false,
                    'variant': 'small',
                    'stripes': false,
                    'dots': false
                }
            },
            'types': {
                'default': {
                    'icon': 'fw-add'
                },
                'object': {
                    'icon': 'fw-add'
                },
                'value': {
                    'icon': 'fw-info'
                }
            },
            'plugins': ['types', 'wholerow']
        };
    };

    VariableTree.prototype = Object.create(EventChannel.prototype);
    VariableTree.prototype.constructor = VariableTree;

    VariableTree.prototype.render = function (container) {
        this.renderHeader(container);
        this.renderContentDiv(container);
        return this;
    };

    VariableTree.prototype.renderHeader = function (container) {
        var headerContainer =
            $('<div class="panel-heading"><a class="collapsed" data-toggle="collapse" href="#debugger-variable-tree">Variables</a></div>');
        container.append(headerContainer);
    };

    VariableTree.prototype.renderContentDiv = function (container) {
        var contentContainer = $('<div id="debugger-variable-tree" class="panel-collapse collapse in" role="tabpanel">' +
            '</div>');
        container.append(contentContainer);
        this.$_tree = $("#debugger-variable-tree").jstree(this._treeConfig);
        this.$_tree.on('open_node.jstree', function () {
            // opening a nested node
            // TODO :fetch children from server
        });

        this.updateVariableTree({});
    };

    VariableTree.prototype.updateVariableTree = function(variableTree) {
        var newData = _.flatMapDeep(variableTree, function (value, key) {

        });

        $('#debugger-variable-tree').jstree(true).settings.core.data = newData;
        $('#debugger-variable-tree').jstree(true).refresh();
    };


    return (instance = (instance || new VariableTree() ));
});