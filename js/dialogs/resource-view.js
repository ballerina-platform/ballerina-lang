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
     el: '.nav-tabs',

       template: _.template($('#resourceTabsTemplate').html()),


        initialize: function () {
          //_.bindAll(this, 'cleanup');
         // this.model.on("change",this.render());
        },

        events:{
           	 'click .add-resource': 'addResourceTab',
           	 'click .remove-resource':'removeResourceTab',
           	//'click .series':'defaultResourceTab'
           	},
// cleanup: function() {
//        this.undelegateEvents();
//        $(this.el).empty();
//    },
           	addResourceTab:function(e){
             e.preventDefault();
             var id = $(".nav-tabs").children().length;
             var resourceId = 'seq_' + id;

             var resourceModel = new Dialogs.Models.ResourceModel({
             resourceId:resourceId,
             resourceTitle: "title"
             });
          //   var list = new Dialogs.Models.ResourceCollection();
           //  list.add(resourceModel);

              var nextResourceView = new Dialogs.Views.ResourceView({model:resourceModel});
              //nextResourceView.render();
             //create tab content for new resource
            // var resourceContent = new Dialogs.Views.ResourceCollectionView({model:resourceModel});
            // var resourceContent = new Dialogs.Views.ResourceCollectionView({model:resourceModel});



           	},

        render: function () {
           var htmContent = this.template(this.model.attributes);
            //var additionDiv =$("ul>.add-resources").first(); //this.$el.find(".add-resources");
            ///additionDiv.insertAfter(htmContent);
            //this.$el.closest('li').before('<li><a href="#seq">New Seq</a></li>');
           // $(".add-resources").prev('<li>aaa</li>');
          // this.$el.insertBefore(htmContent);
          this.$el.append(htmContent);
           return this.$el;
                      }
    });

    views.ResourceView = resourceView;
    dialogs.Views = views;
    return dialogs;

}(Dialogs || {}));
