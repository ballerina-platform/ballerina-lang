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

define(['require', 'backbone'], function (require, Backbone) {

    var tool = Backbone.Model.extend({
        initialize: function (args) {
            this.name = _.get(args, 'name', null);
            this.icon = _.get(args, 'icon', null);
            this.nodeFactoryMethod = _.get(args, 'nodeFactoryMethod', null);
            this.dragCursorOffset = _.get(args, 'dragCursorOffset', undefined);
        },

        modelName: "Tool",

        defaults: {
            id: "",
            title: "",
            icon: ""
        }
    });

    return tool;
});

