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

const { ipcMain, Menu } = require('electron');

function registerMenuLoader() {
    // remove the stock menu
    Menu.setApplicationMenu(Menu.buildFromTemplate([]));

    ipcMain.on('main-menu-loaded', (event, menus) => {
        
        const template = menus.map((menu) => {
            return {
                label: menu.label,
                submenu: menu.children.map((childMenu) => {
                    console.log(childMenu);
                    return {
                        label: childMenu.label,
                        click: () => {
                            event.sender.send('menu-item-clicked', childMenu.command);
                        }
                    }
                })
            }
        });
        Menu.setApplicationMenu(Menu.buildFromTemplate(template));
    });
}

module.exports = registerMenuLoader;
