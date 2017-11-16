/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

import { ipcRenderer } from 'electron';

// provide global jquery object because wso2 theme expects it
window.$ = window.jQuery = require('jquery');

const Application = require('./js/app').default;
const config = require('./config').default;

const app = new Application(config);

app.setElectronMode(true, ipcRenderer);
app.render();
app.displayInitialView();

ipcRenderer.on('menu-item-clicked', (e, commandId) => {
    app.commandManager.dispatch(commandId);
});

function _prepareMenuTemplate(menus, webContents) {
    const template = [];
    Object.keys(menus).forEach((menuId) => {
        const menu = menus[menuId];
        template.push(_prepareMenuItem(menu, webContents));
    });

    return template;
}

function _prepareMenuItem(menu, webContents) {
    const menuItem = {};
    menuItem.label = menu.definition.label;

    if (menu.definition.items) {
        menuItem.submenu = menu.definition.items.map(subItem => _prepareMenuItem(menu[subItem.id], webContents));
    }

    if (menu.definition.command) {
        menuItem.commandId = menu.definition.command.id;
    }

    return menuItem;
}

const menus = _prepareMenuTemplate(app.menuBar._menuGroups);
ipcRenderer.send('main-menu-loaded', menus);
