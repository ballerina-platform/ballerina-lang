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

var Tools = (function (tools) {
    var views = tools.Views || {};

    var toolGroupWrapperView = Backbone.View.extend({
        template: _.template($('#toolgroupWrapper').html()),

        initialize: function () {
            console.log("toolGroupWrapperView init");
        },

        render: function () {
            var htmContent = this.template(this.model.attributes);
            this.$el.html(htmContent);
            var toolGroupView = new Tools.Views.ToolGroupView({collection: this.model.attributes.toolGroup});
            var groupHtml = toolGroupView.render().el;
            var groupDiv = this.$el.find("#toolgroup-container-" + this.model.attributes.toolGroupID);
            groupDiv.html(groupHtml);
            return this;
        }
    });

    $(document).on('click', '.panel-heading span.clickable', function (e) {
        var $this = $(this);
        if (!$this.hasClass('panel-collapsed')) {
            $this.parents('.panel').find('.panel-body').slideUp();
            $this.addClass('panel-collapsed');
            $this.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
        } else {
            $this.parents('.panel').find('.panel-body').slideDown();
            $this.removeClass('panel-collapsed');
            $this.find('i').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
        }
    })

    views.ToolGroupWrapperView = toolGroupWrapperView;
    tools.Views = views;
    return tools;

}(Tools || {}));
