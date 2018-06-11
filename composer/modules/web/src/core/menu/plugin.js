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
 */
import log from 'log';
import _ from 'lodash';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';

import { REGIONS, COMMANDS as LAYOUT_COMMANDS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { PLUGIN_ID, VIEW_IDS, MENU_DEF_TYPES } from './constants';

import AppMenuView from './views/AppMenu';
import { isOnElectron } from '../utils/client-info';

/**
 * MenuPlugin is responsible for rendering menu items.
 *
 * @class MenuPlugin
 */
class MenuPlugin extends Plugin {

    constructor() {
        super();
        this.menus = [];
        this.roots = [];
        this.getLabelForCommand = this.getLabelForCommand.bind(this);
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * Add a menu definition to application menu.
     *
     * @param {Object} menuDef Menu Definition
     */
    addMenu(menuDef) {
        if (!_.isNil(_.find(this.menus, ['id', menuDef.id]))) {
            log.error('Duplicate menu-definition for menu ' + menuDef.id);
        } else {
            this.menus.push(_.cloneDeep(menuDef));
        }
    }

    /**
     * Generate menu from contributed menu definitions
     *
     */
    generateMenuFromDefinitions() {
        const sortedMenus = _.sortBy(this.menus, ['order']);
        const roots = _.filter(sortedMenus, ['type', MENU_DEF_TYPES.ROOT]);
        const groups = _.filter(sortedMenus, ['type', MENU_DEF_TYPES.GROUP]);
        const items = _.filter(sortedMenus, ['type', MENU_DEF_TYPES.ITEM]);

        const findRoot = (id) => {
            return _.find(roots, ['id', id]);
        };

        const findGroup = (id) => {
            return _.find(groups, ['id', id]);
        };

        const nestMenuUnderParent = (menu) => {
            const { id, parent } = menu;
            if (_.isNil(parent)) {
                log.error('Invalid parent-id provied for menu ' + id);
            } else {
                const parentNode = (findRoot(parent)) ? findRoot(parent) : findGroup(parent);
                if (_.isNil(parentNode)) {
                    log.error('Unable to find a parent menu with id ' + parent);
                } else {
                    if (_.isNil(parentNode.children)) {
                        parentNode.children = [];
                    }
                    parentNode.children.push(menu);
                }
            }
        };

        groups.forEach(nestMenuUnderParent);
        items.forEach(nestMenuUnderParent);

        this.roots = roots;
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        this.generateMenuFromDefinitions();
    }

    /**
     * @inheritdoc
     */
    onAfterInitialRender() {
        if (isOnElectron()) {
            const { ipcRenderer } = require('electron');
            const { command: { dispatch, on } } = this.appContext;
            const populateNativeMenuItem = (node) => {
                node.gen = {
                    isActive: true,
                    shortcut: node.command ? this.getShortcutForCommand(node.command) : '',
                    subLabel: node.command ? this.getLabelForCommand(node.command) : '',
                };
                if (typeof node.isActive === 'function') {
                    node.gen.isActive = node.isActive();
                }
            };
            const populateNativeMenu = (roots) => {
                roots.forEach((root) => {
                    populateNativeMenuItem(root);
                    root.children.forEach((child) => {
                        populateNativeMenuItem(child);
                    });
                });
                return roots;
            };
            ipcRenderer.send('main-menu-loaded', populateNativeMenu(this.roots));
            ipcRenderer.on('menu-item-clicked', (e, commandId) => {
                dispatch(commandId);
            });
            on(LAYOUT_COMMANDS.UPDATE_ALL_ACTION_TRIGGERS, () => {
                ipcRenderer.send('main-menu-loaded', populateNativeMenu(this.roots));
            });
        }
    }

    /**
     * Gets the shortcut label for command
     * @param {String} cmdID command ID
     */
    getLabelForCommand(cmdID) {
        const cmd = this.appContext.command.findCommand(cmdID);
        return _.get(cmd, 'shortcut.derived.label', '');
    }

    /**
     * Gets the shortcut for command
     * @param {String} cmdID command ID
     */
    getShortcutForCommand(cmdID) {
        const cmd = this.appContext.command.findCommand(cmdID);
        return _.get(cmd, 'shortcut.derived.key', '');
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, VIEWS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.APP_MENU,
                    component: AppMenuView,
                    propsProvider: () => {
                        return {
                            dispatch: this.appContext.command.dispatch,
                            menu: this.roots,
                            getLabelForCommand: this.getLabelForCommand,
                        };
                    },
                    region: REGIONS.HEADER,
                    displayOnLoad: true,
                },
            ],
        };
    }
}

export default MenuPlugin;
