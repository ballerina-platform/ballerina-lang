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

    var resourceView = Backbone.View.extend({
    el : '#sample',
    $ul :  $("#sample").find("ul"),


       template: _.template($('#resourceTabsTemplate').html()),


        initialize: function () {


        },

        events:{
           	 'click .add-resource': 'addResourceTab',
           	 'click .remove-resource':'removeResourceTab',
         // 'click .series':'showResourceContent'
           	},

           	addResourceTab:function(e){
           	this.undelegateEvents();
             e.preventDefault();
             var id = $(".nav-tabs").children().length;
             var hrefId = '#seq_' + id;
             var resourceId = 'seq_' + id;

             var resourceModel = new Dialogs.Models.ResourceModel({
             resourceId:resourceId,
             hrefId:hrefId,
             resourceTitle: "title"
             });

              var nextResourceView = new Dialogs.Views.ResourceView({model:resourceModel});
              nextResourceView.render();
             //create tab content for new resource
             var resourceContent = new Dialogs.Views.ResourceCollectionView({model:resourceModel});
            resourceContent.render();

           	},


           	removeResourceTab : function(e){
           //	this.undelegateEvents();
           	    e.preventDefault();
           	  var anchor = $(e.currentTarget).siblings('a');
               $(anchor.attr('href')).remove();
                $(e.currentTarget).parent().remove();
                 var removedId = anchor[0].attributes[0].value;
                 var divToRemove = document.getElementById(removedId);
                  divToRemove.parentNode.removeChild(divToRemove);


           	},

        render: function () {
           var htmContent = this.template(this.model.attributes);

          this.$ul.append(htmContent);
           return this.$el;
                      }
    });

    views.ResourceView = resourceView;
    dialogs.Views = views;
    return dialogs;

}(Dialogs || {}));
