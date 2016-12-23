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
define(['require', 'log', 'jquery', 'lodash', 'backbone', 'menu_bar_provider'],

    function (require, log, $, _, Backbone, MenuBarProvider) {


        var MenuBar = Backbone.View.extend(
            /** @lends ToolBar.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class ToolBar
                 * @param {Object} config configuration options for the ToolBar
                 */
                initialize: function (options) {

                    var errMsg;
                    if(!_.has(options, 'container')){
                        errMsg = 'Unable to find configuration for container';
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var container = $(_.get(options, 'container'));
                    this._$parent_el = container;
                    this._options = options;
                },

                render: function () {
                    var parent = this._$parent_el;
                    var self = this;
                    var _options = this._options;
                    var command = _options.application.commandManager;

                    //Iterate over menu groups
                    _.forEach(MenuBarProvider, function (menuGroupDefinition, menuGroupId) {
                            var toolBarDiv = $('<div></div>');
                            toolBarDiv.addClass(_.get(_options, 'cssClass.menu_group'));
                            parent.append(toolBarDiv);

                            var title = $('<a></a>');
                            title.text(menuGroupDefinition.label);
                            title.addClass(_.get(_options, 'cssClass.item'));
                            title.attr("data-toggle", "dropdown");
                            toolBarDiv.append(title);

                            var menu = $('<ul></ul>');
                            menu.addClass(_.get(_options, 'cssClass.menu'));
                            toolBarDiv.append(menu);

                            //Iterate over menu items
                            _.forEach(menuGroupDefinition.items, function (menuItem, menuItemId) {
                                var item = $('<li></li>');
                                var link = $('<a></a>');
                                link.text(menuItem.label);
                                link.click(function () {
                                    command.dispatch(menuItem.action);
                                });
                                item.append(link);
                                menu.append(item);
                            });
                        }
                    );
                },

                show: function(){
                    $(this._$parent_el).show();
                },

                hide: function(){
                    $(this._$parent_el).hide();
                }

            });

        return MenuBar;
});