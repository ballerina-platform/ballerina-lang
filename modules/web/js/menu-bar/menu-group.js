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
import $ from 'jquery';
import _ from 'lodash';
import EventChannel from 'event_channel';
import MenuItem from './menu-item';

const MenuGroup = function (args) {
    _.assign(this, args);
};

MenuGroup.prototype = Object.create(EventChannel.prototype);
MenuGroup.prototype.constructor = MenuGroup;

MenuGroup.prototype.render = function () {
    const menuItemDefinitions = _.get(this, 'definition.items');
    const parent = _.get(this, 'options.parent');
    const menuItemOpts = _.cloneDeep(_.get(this, 'options.menu_item'));

    const toolBarDiv = $('<div></div>');
    toolBarDiv.addClass(_.get(this, 'options.cssClass.group'));
    parent.append(toolBarDiv);

    const title = $('<a></a>');
    title.text(_.get(this, 'definition.label'));
    title.addClass(_.get(this, 'options.cssClass.toggle'));
    title.attr('data-toggle', 'dropdown');
    title.on('mouseover', (e) => {
        const thisElem = $(e.target).parent();
        const toggleClass = 'open';
        _.some(thisElem.siblings(), (el) => {
            if ($(el).hasClass(toggleClass)) {
                thisElem.addClass(toggleClass);
                $(el).removeClass(toggleClass);
            }
        });
    });
    toolBarDiv.append(title);

    const menu = $('<ul></ul>');
    menu.addClass(_.get(this, 'options.cssClass.menu'));
    toolBarDiv.append(menu);

    const self = this;

    // Iterate over menu items
    _.forEach(menuItemDefinitions, (menuItemDef) => {
        const menuItemOptions = { definition: _.cloneDeep(menuItemDef) };
        _.set(menuItemOptions, 'options', menuItemOpts);
        _.set(menuItemOptions, 'options.parent', menu);
        _.set(menuItemOptions, 'options.application', _.get(self, 'options.application'));
        const menuItem = new MenuItem(menuItemOptions);
        menuItem.render();
        _.set(self, menuItem.getID(), menuItem);
    });
};

MenuGroup.prototype.getID = function () {
    return _.get(this, 'definition.id');
};

export default MenuGroup;
