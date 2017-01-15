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

define(['lodash', 'event_channel', './menu-item'],
    function (_, EventChannel, MenuItem){

    var MenuGroup = function(args){
        _.assign(this, args);
    };

    MenuGroup.prototype = Object.create(EventChannel.prototype);
    MenuGroup.prototype.constructor = MenuGroup;

    MenuGroup.prototype.render = function(){

        var menuItemDefinitions = _.get(this, 'definition.items');
        var parent = _.get(this, 'options.parent');
        var menuItemOpts = _.cloneDeep(_.get(this, 'options.menu_item'));

        var toolBarDiv = $('<div></div>');
        toolBarDiv.addClass(_.get(this, 'options.cssClass.group'));
        parent.append(toolBarDiv);

        var title = $('<a></a>');
        title.text(_.get(this, 'definition.label'));
        title.addClass(_.get(this, 'options.cssClass.toggle'));
        title.attr("data-toggle", "dropdown");
        toolBarDiv.append(title);

        var menu = $('<ul></ul>');
        menu.addClass(_.get(this, 'options.cssClass.menu'));
        toolBarDiv.append(menu);

        var self = this;

        //Iterate over menu items
        _.forEach(menuItemDefinitions, function (menuItemDef) {

            var menuItemOptions = {definition: _.cloneDeep(menuItemDef)};
            _.set(menuItemOptions, 'options', menuItemOpts);
            _.set(menuItemOptions, 'options.parent', menu);
            _.set(menuItemOptions, 'options.application', _.get(self, 'options.application'));
            var menuItem = new MenuItem(menuItemOptions);
            menuItem.render();
            _.set(self, menuItem.getID(), menuItem);
        });
    };

    MenuGroup.prototype.getID = function(){
        return _.get(this, 'definition.id');
    };

    return MenuGroup;

});