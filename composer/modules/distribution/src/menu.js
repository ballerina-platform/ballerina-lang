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

const { ipcMain, Menu, app } = require('electron');

function registerMenuLoader() {
    // remove the stock menu
    Menu.setApplicationMenu(Menu.buildFromTemplate([]));

    ipcMain.on('main-menu-loaded', (event, menus) => {
        const template = menus.map((menu) => {
            const childrenWithSeparators = menu.children.reduce((r, e) => {
                if (e.divider) {
                    if (e.divider.before) {
                        r.push({ separator: true });
                        r.push(e);
                    } else if (e.divider.after) {
                        r.push(e);
                        r.push({ separator: true });
                    }
                } else {
                    r.push(e);
                }
                return r;
            }, []);
            return {
                label: menu.label,
                enabled: (typeof(menu.gen.isActive) === 'boolean')
                        ? menu.gen.isActive 
                        : true,
                submenu: childrenWithSeparators.map((childMenu) => {
                    if (childMenu.separator) {
                        return {
                            type: 'separator'
                        };
                    }
                    return {
                        label: childMenu.label,
                        click: () => {
                            event.sender.send('menu-item-clicked', childMenu.command);
                        },
                        accelerator: childMenu.gen.shortcut,
                        enabled:(typeof(childMenu.gen.isActive) === 'boolean')
                                ? childMenu.gen.isActive 
                                : true,
                    }
                })
            }
        });
        if (process.platform === 'darwin') {
            template.splice(0, 0, {
                label: 'Ballerina Composer',
                submenu: [
                    {
                        label: 'About',
                        click: () => {
                            event.sender.send('menu-item-clicked', 'help-about');
                        }
                    },
                    {
                        type: 'separator'
                    },
                    {
                        label: 'Quit Composer',
                        role: 'quit',
                        click: () => {
                            app.quit();
                        }
                    }
                ]
            });
        }
        Menu.setApplicationMenu(Menu.buildFromTemplate(template));
    });
}

module.exports = registerMenuLoader;
