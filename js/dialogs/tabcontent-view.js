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

   var newTabContentView = Backbone.View.extend({
  	el : '.tab-panel',


   	events:{
   	 'click .series': 'viewResourceTab',
   	},

   	initialise:function(){
    var diagram = new Diagrams.Models.Diagram({});
    // Create the diagram view

    },

   	render: function(){
   	 var self = this;
     this.collection.each(function (diagram) {
       var diagramOptions = {selector: '.editor'};
       var diagramView = new Diagrams.Views.DiagramView({model: diagram, options: diagramOptions});
       var wrapperHtml = diagramView.render().el;
       self.$el.append(wrapperHtml);
        });
      return this;
   	},



   viewResourceTab : function(e){
   e.preventDefault();
  //$(e.currentTarget).tab('show');
   ///if (!$(e.currentTarget).hasClass('add-resource')) {
var href = $(e.currentTarget).context.childNodes[0].attributes[0].value;
    $('a[href=' +href +']').tab('show');
   }
   });

     views.ContentView = newTabContentView;
     dialogs.Views = views;
    return dialogs;
}(Dialogs || {}));
