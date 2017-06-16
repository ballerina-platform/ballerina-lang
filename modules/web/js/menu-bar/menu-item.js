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

const MenuItem = function (args) {
    _.assign(this, args);
    this._application = _.get(this, 'options.application');
};

MenuItem.prototype = Object.create(EventChannel.prototype);
MenuItem.prototype.constructor = MenuItem;

MenuItem.prototype.render = function () {
    const parent = _.get(this, 'options.parent');

    const item = $('<li></li>');
    const title = $('<span class="pull-left"></span>');
    const link = $('<a></a>');
    parent.append(item);
    item.append(link);
    link.append(title);

    title.text(_.get(this, 'definition.label'));
    this._linkElement = link;
    this._title = title;
    this._listItemElement = item;

    const shortcuts = _.get(this, 'definition.command.shortcuts');
    const commandId = _.get(this, 'definition.command.id');
    if (!_.isNil(shortcuts)) {
        this._application.commandManager.registerCommand(commandId, { shortcuts });
        this.renderShortcutLabel();
    } else {
        this._application.commandManager.registerCommand(commandId, {});
    }

    if (_.get(this, 'definition.disabled')) {
        this.disable();
    } else {
        this.enable();
    }
};

MenuItem.prototype.getID = function () {
    return _.get(this, 'definition.id');
};

MenuItem.prototype.renderShortcutLabel = function () {
    const shortcuts = _.get(this, 'definition.command.shortcuts');
    const shortcutLabel = $('<span></span>');
    const shortcut = this._application.isRunningOnMacOS() ? shortcuts.mac.label : shortcuts.other.label;
    shortcutLabel.addClass(_.get(this, 'options.cssClass.shortcut'));
    shortcutLabel.text(shortcut);
    this._linkElement.append(shortcutLabel);
};

MenuItem.prototype.disable = function () {
    this._listItemElement.addClass(_.get(this, 'options.cssClass.inactive'));
    this._listItemElement.removeClass(_.get(this, 'options.cssClass.active'));
    this._linkElement.off('click');
};

MenuItem.prototype.enable = function () {
    this._listItemElement.addClass(_.get(this, 'options.cssClass.active'));
    this._listItemElement.removeClass(_.get(this, 'options.cssClass.inactive'));
    this._linkElement.off('click');
    this._linkElement.click(() => {
        this._application.commandManager.dispatch(this.definition.command.id);
    });
};

MenuItem.prototype.addLabelSuffix = function (labelSuffix) {
    if (!_.isNil(labelSuffix)) {
        this._title.text(`${_.get(this, 'definition.label')} ${labelSuffix}`);
    }
};

MenuItem.prototype.clearLabelSuffix = function () {
    this._title.text(_.get(this, 'definition.label'));
};

export default MenuItem;
