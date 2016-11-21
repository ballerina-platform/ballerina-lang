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
define(['jquery', 'lodash', 'backbone', './tree-item'], function ($, _, Backbone, TreeItemView) {

    var TreeView = Backbone.View.extend({

        initialize: function (options) {
           this._options = options;
            this._$parent_el = $(_.get(this._options, 'container'));
        },

        render: function () {
            var $el = $('<div></div>');
            this._$parent_el.append($el);
            var model = this.model;
            var rootModel = model.attributes.root;
            var root = new TreeItemView({model: rootModel}).render();
            //TODO: move style to tree.css
            $el.html('<div id="tree-highlight" style="background-color: #373737; position: absolute; width: 100%;"></div>');
            var $highlight = $el.children();
            $el.append(root.el);

            rootModel.on("select", function (e) {
                model.trigger("select", e);
                this.selectedEl = e.currentTarget;
                $highlight.offset({top: $(e.currentTarget).offset().top});
                $highlight.height(32);
                e.stopPropagation();
            });
            rootModel.on("viewChange", function (e) {
                if (this.selectedEl) {
                    var top = $(this.selectedEl).offset().top;
                    if (top == 0) {
                        $highlight.height(0);
                    }
                    $highlight.offset({top: top});
                }
            });
        }
    });

    return TreeView;
});

