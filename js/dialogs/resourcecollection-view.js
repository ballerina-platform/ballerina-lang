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
        el: '.editor',
       $cl:$(".editor").find(".resource-content"),

        template: _.template($('#resourceContentTemplate').html()),

        initialize: function () {
        //create diagram view and object
        var diagram = new Diagrams.Models.Diagram({});


        },
        render: function () {
            var htmContent = this.template(this.model.attributes);
            this.$cl.append(htmContent); //added a new div content for resource
           var resourceId =  this.model.attributes.resourceId;
            var current = document.getElementById(resourceId);
             var canvas= "hello";
             current.append(canvas);
            //todo
           this.model.on("initialSetup",this.setCanvasToResource(resourceId,current));
                      return this.$el;
        },
        setCanvasToResource : function(id,element){

        var diagramOptions = {selector: id};
         var diagramView = new Diagrams.Views.DiagramView({model: diagram, options: diagramOptions});
        //var canvas = diagramView.render();
        //var canvas= "hello";
         element.append(canvas);
        }
    });

    views.ResourceCollectionView = resourceCollectionView;
    dialogs.Views = views;
    return dialogs;

}(Dialogs || {}));
