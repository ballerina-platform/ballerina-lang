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

   var newTabView = Backbone.View.extend({
  	el : '.nav-tabs',


   	events:{
   	 'click .add-resource': 'addResourceTab',
   	 'click .remove-resource':'removeResourceTab',
   	'click .series':'defaultResourceTab'
   	},
   	initialise:function(){
   // var diagram = new Diagrams.Models.Diagram({});
    // Create the diagram view

    },

   	render: function(){
   	// render the current selected tab's view

   	return this.$el;
   	},



   defaultResourceTab : function(e){
   e.preventDefault();
  //$(e.currentTarget).tab('show');
   ///if (!$(e.currentTarget).hasClass('add-resource')) {
var href = $(e.currentTarget).context.childNodes[0].attributes[0].value;
    $('a[href=' +href +']').tab('show');



   },
// when creating a  new tab resource add an empty canvas
   addResourceTab: function(e){
   	e.preventDefault();
   	 var id = $(".nav-tabs").children().length;
     var tabId = 'seq_' + id;
    $(e.currentTarget).closest('li').before('<li class="series"><a href="#seq_' + id + '">New Seq</a> <span class="remove-resource"> x </span></li>');
       var obj =  $('.tab-content').children().length;
          $('.tab-content').append('<div class="tab-pane" id="' + tabId + '"> </div>');
      // var canvas = diagramView.render();
     //  document.getElementById("seq_2").innerHTML=canvas;
        //$("#" +tabId ).append(canvas);
        //$("#" +tabId ).html(diagramView.render());
            $('.nav-tabs li:nth-child(' + id + ') a').click();
   },
   //Remove tab and content when clicked on delete
   removeResourceTab: function(e){
   e.preventDefault();
    var anchor = $(e.currentTarget).siblings('a');
         $(anchor.attr('href')).remove();
          $(e.currentTarget).parent().remove();
   }

   });

     views.ResourceView = newTabView;
     dialogs.Views = views;
    return dialogs;
}(Dialogs || {}));
