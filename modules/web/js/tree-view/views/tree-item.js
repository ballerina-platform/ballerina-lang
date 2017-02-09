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
define(['require', 'lodash', 'backbone'], function (require, _, Backbone) {

    var TreeItemView = Backbone.View.extend({
        tagName: "li",

        events: {
            "click .file": "triggerSelect",
            "click .branch": "toggle"
        },

        render: function () {
            var model = this.model;
            var $el = this.$el;
            $el.attr('aria-expanded', "true");
            $el.html("<span>" + model.get("name") + "</span>");
            if (model.get("isDir")) {
                var TreeItemListView = require('./tree-item-list');
                var children = model.get("children");
                var listModel = {collection: children};
                $el.prepend('<i class="icon"></i>');
                $el.append(new TreeItemListView(listModel).render().el);
                $el.addClass("branch");
            } else {
                $el.addClass("file");
            }
            return this;
        },

        toggle: function (e) {
            var icon = $(e.currentTarget).children('i:first');
            var item = icon.closest('li');
            var attr = 'aria-expanded';
            if (item.attr(attr) == 'true') {
                item.attr(attr, 'false');
            } else {
                item.attr(attr, 'true');
            }
            this.model.trigger("viewChange", e);
            e.stopPropagation();
        },

        triggerSelect: function (e) {
            var path = "";
            var target = $(e.currentTarget);

            e.name = target.text();

            var parents = target.parentsUntil(".tree-view", "li");
            for (var i = 0; i < parents.length; i++) {
                var obj = parents[i];
                path = $(obj).children("span").text() + "/" + path;
            }
            e.path = path + e.name;
            this.model.trigger("select", e);
            e.stopPropagation();
        },


        startAdding: function () {
            // TODO: convert to Backbone
            // var removed = false;
            // $("#tree-add-api").on('click',function (e) {
            //     $tree.find("> li > ul").append("<li><input/></li>")
            //     removed = false;
            //     $tree.find('input').focus();
            // });
            // var addApi = function (e) {
            //     if(!removed){
            //         removed = true;
            //         var $input = $tree.find('input');
            //         $input.parent('li').remove();
            //         var name = $input.val();
            //         if(name != ""){
            //             $tree.find("> li > ul").append("<li>" + name + "</li>")
            //         }
            //     }
            // };
            // $tree.on("blur", "input", addApi);
            // $tree.on('keypress', function (e) {
            //     if (e.which === 13) {
            //         addApi(e)
            //     }
            // });
        }

    });

    return TreeItemView;
});

