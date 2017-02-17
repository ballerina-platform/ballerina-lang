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
define(['require', 'log', 'jquery', 'lodash', 'backbone', /* void modules */ 'jquery_ui'],

    function (require, log, $, _, Backbone) {

        var BreadcrumbControl = Backbone.View.extend(
        /** @lends BreadcrumbControl.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class BreadcrumbControl
             * @param {Object} config configuration options for the BreadcrumbControl
             */
            initialize: function (config) {
                if(!_.has(config, 'container')){
                    log.error('Cannot init breadcrumbs. config container not found.')
                }
                var container = $(_.get(config, 'container'));
                if(! container.length > 0){
                    log.error('Cannot init breadcrumbs. selector ' + _.get(config, 'container') + ' returned no elements.')
                } else {
                    this._$parent_el = container;
                }
                this._options = config;
            },
            render: function () {
                var list = $("<ol></ol>");
                this._$parent_el.append(list);
                this.$el = list;
                list.addClass(_.get(this._options, 'cssClass.list'));

                var tabController = _.get(this._options, 'application.tabController');
                this.listenTo(tabController, "active-tab-changed", function(evt){
                    var activeTab = evt.newActiveTab;
                    if(_.isFunction(activeTab.getFile)){
                        this.setPath(activeTab.getFile().getPath(), activeTab.getFile().getName());
                    } else {
                        this.setPath("Ballerina Composer", activeTab.getTitle());
                    }
                });

                this.listenTo(tabController, "last-tab-removed", function(evt){
                    this.setPath("", "");
                });
            },
            setPath: function(path, file){
                path = _.replace(path, /\\/gi, "/");
                var pathArr = _.split(path, "/");

                this.$el.empty();
                var self = this;
                pathArr.forEach(function(segment){
                    if(!_.isEmpty(segment)){
                        var li = $("<li>" + segment + "</li>");
                        li.addClass(_.get(self._options, 'cssClass.item'));
                        self.$el.append(li);
                    }
                });
                var fileLi = $("<li>" + file + "</li>");
                fileLi.addClass(_.get(this._options, 'cssClass.item'));
                fileLi.addClass(_.get(this._options, 'cssClass.active'));
                this.$el.append(fileLi);
            }
        });

        return BreadcrumbControl;
});