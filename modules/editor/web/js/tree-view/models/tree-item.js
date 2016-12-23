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
define(['lodash', 'backbone'], function (_, Backbone) {

    var TreeItem = Backbone.Model.extend({
        // This attribute is useful to identify the model at debug time
        modelName: "TreeItem",
        defaults: {
            isDir: false,
            /**@type {TreeItemList} */
            children: null
        },

        initialize: function () {
            var self = this;
            Backbone.Model.prototype.initialize.apply(this, arguments);
            var error = this.validate(this.attributes);
            if (error) {
                throw error;
            }

            if (this.attributes.isDir) {
                this.attributes.children.on("select", function (e) {
                    self.trigger("select", e);
                })
            }
        },

        /**
         * @param {TreeItem.defaults} attributes
         * @returns {string}
         */
        validate: function (attributes) {
            if (!attributes.isDir && attributes.children && attributes.children.length > 0) {
                return "only dirs can have children";
            }
            if (attributes.isDir && !attributes.children) {
                return "dirs must have children of type TreeItemList";
            }
        }
    });

    return TreeItem;
});

