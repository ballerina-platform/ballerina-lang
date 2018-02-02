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

const {ipcMain, Menu} = require('electron');

function registerMenuLoader() {
    // remove the stock menu
    Menu.setApplicationMenu(Menu.buildFromTemplate([]));

    ipcMain.on('main-menu-loaded', (event, menu) => {
        _addClickHandlers(menu, event.sender);
        Menu.setApplicationMenu(Menu.buildFromTemplate(menu));
    });
}

function _addClickHandlers(menus, webContents) {
    Object.keys(menus).forEach(menuId => {
        const menu = menus[menuId];
        _addClickHandler(menu, webContents);
    });
}

function _addClickHandler(menuItem, webContents) {
    if(menuItem.submenu){
        menuItem.submenu.map(subItem => {
            return _addClickHandler(subItem, webContents);
        });
    }

    if(menuItem.commandId){
        var commandId = menuItem.commandId;
        menuItem.click = () => {
            webContents.send('menu-item-clicked', commandId);
        };
        delete menuItem.commandId;
    }
}

module.exports = registerMenuLoader;
