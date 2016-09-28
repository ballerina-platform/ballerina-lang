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

var Dialogs = (function (dialogs) {
    var views = dialogs.Views || {};

    var resourceCollectionView = Backbone.View.extend({
        el: '.resource-content',

        template: _.template($('#resourceContentTemplate').html()),

        initialize: function () {
        //create diagram view and object
        var diagram = new Diagrams.Models.Diagram({});

        // Create the diagram view

        },

        render: function () {
        // for each resource Model create a div content, and add a new canvas to each
            var htmContent = this.template(this.model.attributes);
            this.$el.append(htmContent); //added a new div content for resource
 var diagramOptions = {selector: '.resource-content'};
               //
            var diagramView = new Diagrams.Views.DiagramView({model: diagram, options: diagramOptions});

               // var wrapperHtml = diagramView.render();
               // var testhtml = "hello!";
               //diagramView.render();
                self.$el.append(diagramView.render());
                      return this.$el;
        },
    });

    views.ResourceCollectionView = resourceCollectionView;
    dialogs.Views = views;
    return dialogs;

}(Dialogs || {}));
